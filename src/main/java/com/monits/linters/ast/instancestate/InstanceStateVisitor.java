package com.monits.linters.ast.instancestate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.ast.BinaryExpression;
import lombok.ast.BinaryOperator;
import lombok.ast.Cast;
import lombok.ast.Expression;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Modifiers;
import lombok.ast.Node;
import lombok.ast.StringLiteral;
import lombok.ast.This;
import lombok.ast.TypeDeclaration;
import lombok.ast.TypeMember;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import lombok.ast.VariableReference;

import com.android.tools.lint.detector.api.JavaContext;
import com.google.common.collect.ImmutableList;
import com.monits.linters.ast.TreeTransversal;

/* default */ class InstanceStateVisitor extends ForwardingAstVisitor {

	/* default */ static final String METHOD_PREFIX_GET = "get";
	/* default */ static final String METHOD_PREFIX_PUT = "put";
	
	private static final List<String> VALID_METHOD_PREFIXES = ImmutableList.of(
			METHOD_PREFIX_GET, METHOD_PREFIX_PUT);
	
	private final String methodPrefix;
	private final String bundleName;
	/* default */ final List<BundleMethodInvocation> usedKeys;
	private final List<MethodDeclaration> analyzedMethods;
	private final JavaContext context;
	
	public InstanceStateVisitor(final String bundleName, final String interestMethodPrefix,
			final JavaContext context) {
		this.context = context;
		this.bundleName = bundleName;
		usedKeys = new ArrayList<BundleMethodInvocation>();
		analyzedMethods = new ArrayList<MethodDeclaration>();
		methodPrefix = interestMethodPrefix;
		if (!VALID_METHOD_PREFIXES.contains(methodPrefix)) {
			throw new IllegalArgumentException("Method prefix " + interestMethodPrefix + " is invalid.");
		}
	}

	@Override
	public boolean visitMethodInvocation(final MethodInvocation node) {
		// Is it a method we are interested in?
		final String methodName = node.astName().astValue();
		if (!methodName.startsWith(methodPrefix)) {
			if (node.astOperand() != null && !(node.astOperand() instanceof This)) {
				return super.visitMethodInvocation(node);
			}
			
			checkRecursiveMethodCall(node, methodName);
			return super.visitMethodInvocation(node);
		}
		
		// Make sure it's a call on the right bundle
		final Expression operand = node.astOperand();
		if (operand == null || !(operand instanceof VariableReference)) {
			return super.visitMethodInvocation(node);
		}
		
		final VariableReference var = (VariableReference) operand;
		if (!bundleName.equals(var.astIdentifier().astValue())) {
			return super.visitMethodInvocation(node);
		}
		
		// For all put* / get* bundle methods, the key is the first argument
		final Expression key = node.astArguments().first();
		final String keyValue = resolveStringExpressionValue(key);
		final String varName = getInvolvedField(node);
		usedKeys.add(new BundleMethodInvocation(node, keyValue,
				methodName.substring(methodPrefix.length()), varName));
		
		return super.visitMethodInvocation(node);
	}
	
	private void checkRecursiveMethodCall(final MethodInvocation node, final String methodName) {
		int idx = 0;
		// Is it a method taking the bundle as parameter?
		for (final Expression arg : node.astArguments()) {
			if (arg instanceof VariableReference) {
				final VariableReference var = (VariableReference) arg;
				if (bundleName.equals(var.astIdentifier().astValue())) {
					// Get this method's body and go through it
					final TypeDeclaration td = TreeTransversal.getClosestParent(node, TypeDeclaration.class);
					for (final TypeMember tm : td.astBody().astMembers()) {
						if (tm instanceof MethodDeclaration) {
							final MethodDeclaration md = (MethodDeclaration) tm;
							
							// Avoid infinite loops on recursive code
							if (analyzedMethods.contains(md)) {
								continue;
							}
							
							analyzedMethods.add(md);
							
							// Names matches, parameter count matches... we can't check types :(
							if (methodName.equals(md.astMethodName().astValue()) && md.astParameters().size() == node.astArguments().size()) {
								// Get the name of the bundle parameter in that method
								final Iterator<VariableDefinition> paramIterator = md.astParameters().iterator();
								VariableDefinition varDef;
								do {
									varDef = paramIterator.next();
									idx--;
								} while (idx >= 0);
								
								// Go over that method and check it out
								final String localBundleName = varDef.astVariables().first().astName().astValue();
								final InstanceStateVisitor visitor = new InstanceStateVisitor(localBundleName, methodPrefix, context);
								visitor.analyzedMethods.addAll(analyzedMethods); //share our knowledge with children
								md.astBody().accept(visitor);
								usedKeys.addAll(visitor.usedKeys);
							}
						}
					}
					break;
				}
			}
			
			idx++;
		}
	}

	/**
	 * Retrieves the name of the field involved (being stored / assigned) in the given invocation.
	 * May be null if it's not a field.
	 * 
	 * @param node The method invocation to analyze.
	 * @return The name of the field involved, or null if not a field.
	 */
	@Nullable
	private String getInvolvedField(@Nonnull final MethodInvocation node) {
		String varName = null;
		
		// Only for get* methods...
		if (METHOD_PREFIX_GET.equals(methodPrefix)) {
			// go up looking for an assignment...
			Node n = node.getParent();
			while (n != null && (n instanceof Cast || n instanceof BinaryExpression)) {
				if (n instanceof BinaryExpression) {
					final BinaryExpression assignment = (BinaryExpression) n;
					if (assignment.astOperator() == BinaryOperator.ASSIGN) {
						final VariableDefinitionEntry varDef = TreeTransversal.getDefinitionForWrappedVariable(assignment.astLeft());
						if (varDef != null && TreeTransversal.variableIsField(varDef)) {
							return varDef.astName().astValue();
						}
						break;
					}
				}
				
				n = n.getParent();
			}
		} else {
			// For put* method, we look into the second argument
			if (node.astArguments().size() >= 2) {
				final Iterator<Expression> argumentsIterator = node.astArguments().iterator();
				argumentsIterator.next(); // key is ignored
				final VariableDefinitionEntry varDef = TreeTransversal.getDefinitionForWrappedVariable(argumentsIterator.next());
				if (varDef != null && TreeTransversal.variableIsField(varDef)) {
					return varDef.astName().astValue();
				}
			}
		}
		
		return varName;
	}

	private String resolveStringExpressionValue(final Expression exp) {
		// TODO : Handle Select!!
		if (exp instanceof StringLiteral) {
			// TODO : Suggest the usage of static final fields?
			return ((StringLiteral) exp).astValue();
		} else if (exp instanceof BinaryExpression) {
			// Is it possible to have any other operators?
			if (((BinaryExpression) exp).astOperator() == BinaryOperator.PLUS) {
				final BinaryExpression binaryExp = (BinaryExpression) exp;
				return resolveStringExpressionValue(binaryExp.astLeft())
						+ resolveStringExpressionValue(binaryExp.astRight());
			}
		} else if (exp instanceof VariableReference) {
			final String fieldName = ((VariableReference) exp).astIdentifier().astValue();
			final VariableDefinitionEntry varDefEntry = TreeTransversal.getDefinitionForVariable((VariableReference) exp);
			
			// We don't even care if not a field!
			if (TreeTransversal.variableIsField(varDefEntry)) {
				final Modifiers modifiers = varDefEntry.upToVariableDefinition().astModifiers();
				if (!modifiers.isFinal() || !modifiers.isStatic()) {
					context.report(InstanceStateDetector.KEY_IS_NOT_CONSTANT, varDefEntry, context.getLocation(varDefEntry),
							String.format(InstanceStateDetector.KEY_IS_NOT_CONSTANT_MSG, fieldName));
				}
				
				return resolveStringExpressionValue(varDefEntry.astInitializer());
			}
		}
		
		// TODO : initialization in static blocks is not handled
		// TODO : initialization in constructors is not handled (impossible if fields are static!)
		return ""; // FIXME : Use a better default?
	}
}

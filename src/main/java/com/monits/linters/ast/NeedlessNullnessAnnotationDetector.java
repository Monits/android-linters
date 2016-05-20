/**
 *  Copyright 2010 - 2016 - Monits
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 *   file except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 *   ANY KIND, either express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package com.monits.linters.ast;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import lombok.ast.Annotation;
import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Identifier;
import lombok.ast.MethodDeclaration;
import lombok.ast.Modifiers;
import lombok.ast.Node;
import lombok.ast.StrictListAccessor;
import lombok.ast.TypeReference;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.ImmutableList;

public class NeedlessNullnessAnnotationDetector extends Detector implements JavaScanner {

	/* default */ static final String VOID_RETURN_TYPE_MSG = "Method %s returns void and can therefore never be null.";
	/* default */ static final String PRIMITIVE_RETURN_TYPE_MSG = "Method %s has a primitive return type and can therefore never be null.";
	/* default */ static final String VOID_VAR_MSG = "Variable %s is void and can therefore never be null.";
	/* default */ static final String PRIMITIVE_VAR_MSG = "Variable %s has a primitive data type and can therefore never be null.";
	
	public static final Issue NEEDLESS_NULLNESS_ANNOTATION = Issue.create("NeedlessNullnessAnnotation",
			"Checks that no nullness annotations are present on variables that simply can't be null",
			"Adding extra annotations jsut clutters code, and may cause confusion when they are contradictory",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(NeedlessNullnessAnnotationDetector.class, Scope.JAVA_FILE_SCOPE));

	@Override
	public boolean appliesTo(final Context context, final File file) {
		return true;
	}
	
	@Override
	public EnumSet<Scope> getApplicableFiles() {
		return Scope.JAVA_FILE_SCOPE;
	}
	
	@Override
	public List<Class<? extends Node>> getApplicableNodeTypes() {
		return ImmutableList.<Class<? extends Node>>of(Annotation.class);
	}
	
	@Override
	public AstVisitor createJavaVisitor(final JavaContext context) {
		return new NeedlessNullnessAnnotationChecker(context);
	}
	
	private static class NeedlessNullnessAnnotationChecker extends ForwardingAstVisitor {
		private static final String NULLABLE = "Nullable";
		private static final String FQCN_NULLABLE = "com.android.annotations." + NULLABLE;
		
		private static final String NONNULL = "NonNull";
		private static final String FQCN_NONNULL = "com.android.annotations." + NONNULL;
		
		private final JavaContext context;
		
		public NeedlessNullnessAnnotationChecker(final JavaContext context) {
			this.context = context;
		}

		@Override
		public boolean visitAnnotation(final Annotation node) {
			final String type = node.astAnnotationTypeReference().getTypeName();
			
            if (!NULLABLE.equals(type) && !FQCN_NULLABLE.equals(type)
            		&& !NONNULL.equals(type) && !FQCN_NONNULL.equals(type)) {
            	return super.visitAnnotation(node);
            }
            
            // What's the annotation's target?
        	final Node parentNode = node.getParent();
        	if (parentNode instanceof Modifiers) {
        		final Node targetNode = parentNode.getParent();
        		if (targetNode instanceof VariableDefinition) {
        			// We are on a variable definition (most probably a parameter)
        			final TypeReference typeReference = ((VariableDefinition) targetNode).astTypeReference();
        			final StrictListAccessor<VariableDefinitionEntry, VariableDefinition> variables = ((VariableDefinition) targetNode).astVariables();
        			
					for (final VariableDefinitionEntry varDef : variables) {
						if (typeReference.isPrimitive()) {
							context.report(NEEDLESS_NULLNESS_ANNOTATION, node, context.getLocation(node),
								String.format(PRIMITIVE_VAR_MSG, varDef.astName().astValue()));
						} else if (typeReference.isVoid()) {
							context.report(NEEDLESS_NULLNESS_ANNOTATION, node, context.getLocation(node),
									String.format(VOID_VAR_MSG, varDef.astName().astValue()));
						}
					}
        		} else if (targetNode instanceof MethodDeclaration) {
        			// We are on a method definition, check the return type
        			final TypeReference typeReference = ((MethodDeclaration) targetNode).astReturnTypeReference();
        			final Identifier methodName = ((MethodDeclaration) targetNode).astMethodName();
        			
					if (typeReference.isPrimitive()) {
						context.report(NEEDLESS_NULLNESS_ANNOTATION, node, context.getLocation(node),
							String.format(PRIMITIVE_RETURN_TYPE_MSG, methodName.astValue()));
					} else if (typeReference.isVoid()) {
						context.report(NEEDLESS_NULLNESS_ANNOTATION, node, context.getLocation(node),
								String.format(VOID_RETURN_TYPE_MSG, methodName.astValue()));
					}
        		}
            }
			
			return super.visitAnnotation(node);
		}
	}
}

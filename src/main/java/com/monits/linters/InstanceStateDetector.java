/**
 *  Copyright 2010 - 2015 - Monits
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
package com.monits.linters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.Sets;

/**
 * Check for missing instances states that are not saved or restored
 *
 * @author dtecheira
 */
public class InstanceStateDetector extends Detector implements Detector.ClassScanner {

	public static final String SAVED_BUT_NEVER_RESTORED = "The %s key is being saved but has never been restored";
	public static final String RESTORED_BUT_NEVER_SAVED = "The %s key is being restored but has never been saved";
	public static final String ALREADY_SAVED = "The %s key has been already saved";
	public static final String FIELD_ALREADY_RESTORED = "The %s field is already restored";
	public static final String FIELD_ALREADY_SAVED = "The %s field is already saved";
	public static final String SAVED_WITH_DIFERENT_TYPES =
			"The method %s expected a %s type, but the field %s is a %s type";
	public static final String RESTORED_WITH_DIFERENT_TYPES =
			"The field %s is a %s type, but the method %s is returning a %s type";

	public static final Issue MISSING_SAVED_INSTANCE_STATES = Issue.create("savedInstanceState",
			"Missing saved or restored instance states",
			"This will check for missing saved or restored instances states",
			Category.CORRECTNESS, 6, Severity.ERROR,
			new Implementation(InstanceStateDetector.class, Scope.CLASS_FILE_SCOPE));

	public static final Issue OVERWRITING_INSTANCE_STATES = Issue.create("overwritingInstanceState",
			"Overwriting instance states",
			"Check for instances states that are being overwriting",
			Category.CORRECTNESS, 6, Severity.ERROR,
			new Implementation(InstanceStateDetector.class, Scope.CLASS_FILE_SCOPE));

	public static final Issue OVERWRITING_FIELDS = Issue.create("overwritingFields",
			"Overwriting fields",
			"Check for fields that are being overwriting",
			Category.CORRECTNESS, 6, Severity.ERROR,
			new Implementation(InstanceStateDetector.class, Scope.CLASS_FILE_SCOPE));

	public static final Issue INVALID_TYPE = Issue.create("invalidType",
			"Save or restore an invalid type",
			"Check if the type saved or restored is valid",
			Category.CORRECTNESS, 6, Severity.ERROR,
			new Implementation(InstanceStateDetector.class, Scope.CLASS_FILE_SCOPE));

	private static final Set<String> METHOD_SAVE_INSTANCES = Sets.newHashSet("onSaveInstanceState");
	private static final Set<String> METHOD_RESTORE_INSTANCES =
			Sets.newHashSet(
					// Common methods
					"onCreate",
					// Activity methods
					"onPostCreate", "onRestoreInstanceState",
					// Fragment methods
					"onActivityCreated", "onCreateView", "onViewCreated");

	private final Map<String, AbstractInsnNode> savedStates = new HashMap<String, AbstractInsnNode>();
	private final Map<String, AbstractInsnNode> restoredStates = new HashMap<String, AbstractInsnNode>();
	private ClassContext classContext;

	@Override
	public List<String> getApplicableCallNames() {
		final ArrayList<String> list = new ArrayList<String>(METHOD_SAVE_INSTANCES);
		list.addAll(METHOD_RESTORE_INSTANCES);
		return list;
	}

	@Override
	public void checkCall(final ClassContext context, final ClassNode classNode, final MethodNode method,
			final MethodInsnNode call) {

		Set<MethodNode> methodCalls = checkMethodCall(context, classNode, method, method, call);
		final Set<MethodNode> container = new HashSet<MethodNode>();
		final Set<MethodNode> methodVisited = new HashSet<MethodNode>();
		while (!methodCalls.isEmpty()) {
			for (final MethodNode methodNode : methodCalls) {
				if (!methodVisited.contains(methodNode)) {
					container.addAll(checkMethodCall(context, classNode, methodNode, method, call));
					methodVisited.add(methodNode);
				}
			}
			methodCalls = new HashSet<MethodNode>(container);
			container.clear();
		}
		super.checkCall(context, classNode, method, call);
	}

	/**
	 * Walk the method looking for a method or methods that have a bundle as parameter.
	 * If the node analyzed is not a method we check the instruction
	 *
	 * @param context The context
	 * @param classNode The classNode to find the methods
	 * @param methodToIterate The method to iterate the nodes
	 * @param saveRestoreMethod The originary method
	 * @param call The node that match with the one of the applicable call names
	 *
	 * @return The methods that have a bundle as param
	 */
	@Nonnull
	private Set<MethodNode> checkMethodCall(@Nonnull final ClassContext context, @Nonnull final ClassNode classNode,
			@Nonnull final MethodNode methodToIterate, @Nonnull final MethodNode saveRestoreMethod,
			@Nonnull final MethodInsnNode call) {
		final Set<MethodNode> methods = new HashSet<MethodNode>();

		if (methodToIterate.localVariables != null) {
			// We are sorting 'methodToIterate.localVariables' because the index of the each item is always
			// a position of the Local Variable Table, but sometimes those index do not match with the position.
			Collections.sort(methodToIterate.localVariables, new Comparator<LocalVariableNode>() {
				@Override
				public int compare(final LocalVariableNode o1, final LocalVariableNode o2) {
					return o1.index - o2.index;
				}
			});
		}

		final AbstractInsnNode[] instructions = methodToIterate.instructions.toArray();
		for (final AbstractInsnNode abstractInsnNode : instructions) {
			if (abstractInsnNode instanceof MethodInsnNode
					// Ignore same method (ej super.onCreate(...))
					&& !((MethodInsnNode) abstractInsnNode).name.equals(call.name)) {
				final String descriptor = ((MethodInsnNode) abstractInsnNode).desc;
				// check if the parameter has a bundle paramenter
				if (descriptor.substring(descriptor.indexOf('(') + 1, descriptor.indexOf(')'))
						.contains("Landroid/os/Bundle;")) {
					for (final MethodNode element : (List<MethodNode>) classNode.methods) {
						// find the methodNode
						if (element instanceof MethodNode
								&& element.name.equals(((MethodInsnNode) abstractInsnNode).name)) {
							methods.add(element);
						}
					}
				} else {
					//we have something that we can pass to checkInstruction
					checkInstruction(context, classNode, methodToIterate, saveRestoreMethod, abstractInsnNode);
				}
			}
		}
		return methods;
	}

	/**
	 * Process a given instruction node
	 *
	 * @param context The context
	 * @param classNode The class node to generate the reports
	 * @param currentMethod The current method to get the local variables
	 * @param originaryMethod The originary method
	 * @param instruction The instruction to check
	 */
	public void checkInstruction(@Nonnull final ClassContext context, @Nonnull final ClassNode classNode,
			@Nonnull final MethodNode currentMethod, @Nonnull final MethodNode originaryMethod,
			@Nonnull final AbstractInsnNode instruction) {

		if (!"android/os/Bundle".equals(((MethodInsnNode) instruction).owner)
				|| instruction.getOpcode() != Opcodes.INVOKEVIRTUAL) {
			return;
		}

		if (classContext == null) {
			classContext = context;
		}

		// ignore those method that no have params
		final String descriptor = ((MethodInsnNode) instruction).desc;
		if (descriptor.substring(descriptor.indexOf('(') + 1, descriptor.indexOf(')')).isEmpty()) {
			return;
		}

		// Ignore containsKey method
		if ("containsKey".equals(((MethodInsnNode) instruction).name)) {
			return;
		}

		// ignore getIntent().getExtras() or getArgument() bundle
		final VarInsnNode ownerNode = getOwnerNode(instruction, METHOD_SAVE_INSTANCES.contains(originaryMethod.name));
		if (shouldIgnoreBundle((LocalVariableNode) currentMethod.localVariables.get(ownerNode.var))) {
			return;
		}

		if (METHOD_SAVE_INSTANCES.contains(originaryMethod.name)) {
			final String bundleKey = getBundleKey(instruction);
			if (savedStates.containsKey(bundleKey)) {
				context.report(OVERWRITING_INSTANCE_STATES, context.getLocation(instruction),
						String.format(ALREADY_SAVED, bundleKey));
			} else {
				savedStates.put(bundleKey, instruction);
			}
		} else if (METHOD_RESTORE_INSTANCES.contains(originaryMethod.name)) {
			final String bundleKey = getBundleKey(instruction);
			restoredStates.put(bundleKey, instruction);
		}
	}

	/**
	 * Check if the bundle variable is from getIntent().getExtras() or getArgument() method.
	 * @param localVariableNode The variable to check
	 */
	private boolean shouldIgnoreBundle(@Nonnull final LocalVariableNode localVariableNode) {
		final LabelNode startLabelNode = localVariableNode.start;
		AbstractInsnNode node = startLabelNode;

		while (node != null && !(node instanceof MethodInsnNode)) {
			node = node.getPrevious();
		}
		return node != null && "()Landroid/os/Bundle;".equals(((MethodInsnNode) node).desc)
				&& ("getArguments".equals(((MethodInsnNode) node).name)
				|| "android/content/Intent".equals(((MethodInsnNode) node).owner)
				&& "getExtras".equals(((MethodInsnNode) node).name));
	}

	/**
	 * Find the owner variable of this instruction
	 * @param instruction The instruction
	 * @param isASaveMethod A flag to know if it is a save or restore method
	 * @return Returns the owner variable
	 */
	@Nonnull
	private VarInsnNode getOwnerNode(@Nonnull final AbstractInsnNode instruction, final boolean isASaveMethod) {
		// the varNode have the index that we can match with the local variable table from the method.
		AbstractInsnNode varNode = instruction;
		while (!(varNode instanceof VarInsnNode)) {
			varNode = varNode.getPrevious();
		}
		if (isASaveMethod) {
			varNode = varNode.getPrevious();
			// if we are saving a state we need to go until the LineNumberNode and then get the varInsnNode
			while (!(varNode instanceof LineNumberNode)) {
				varNode = varNode.getPrevious();
			}
			varNode = varNode.getNext();
		}
		return (VarInsnNode) varNode;
	}

	@Nonnull
	private String getBundleKey(@Nonnull final AbstractInsnNode instruction) {
		AbstractInsnNode node = instruction;

		// get the key used
		while (!(node instanceof LdcInsnNode)) {
			node = node.getPrevious();
		}

		// check if we have local variables
		if (node.getPrevious() instanceof LdcInsnNode) {
			// get the previos node to get the key
			node = node.getPrevious();
		}

		return ((LdcInsnNode) node).cst.toString();
	}

	@Override
	public void afterCheckFile(@Nonnull final Context context) {
		final Map<String, AbstractInsnNode> restoredStatesToCheck = new HashMap<>(restoredStates);
		reportOverwritingFieldsAndInvalidTypeOnSave(context);
		reportOverwritingFieldsAndInvalidTypeOnRestore(restoredStatesToCheck, context);

		// if we have the same key in both list these are states that not are restored or saved in a field
		final Iterator<Entry<String, AbstractInsnNode>> it = savedStates.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<String, AbstractInsnNode> entry = it.next();
			if (restoredStates.containsKey(entry.getKey())) {
				restoredStates.remove(entry.getKey());
				it.remove();
			}
		}

		// report
		report(context, savedStates, SAVED_BUT_NEVER_RESTORED);
		report(context, restoredStates, RESTORED_BUT_NEVER_SAVED);
		// reset all
		classContext = null;
		savedStates.clear();
		restoredStates.clear();
	}

	private void reportOverwritingFieldsAndInvalidTypeOnSave(@Nonnull final Context context) {
		reportOverwritingFieldsAndInvalidType(savedStates, restoredStates, context,
				false, FIELD_ALREADY_SAVED, SAVED_WITH_DIFERENT_TYPES);
	}

	private void reportOverwritingFieldsAndInvalidTypeOnRestore(
			@Nonnull final Map<String, AbstractInsnNode> restoredStatesToCheck, @Nonnull final Context context) {
		reportOverwritingFieldsAndInvalidType(restoredStatesToCheck, savedStates, context,
				true, FIELD_ALREADY_RESTORED, RESTORED_WITH_DIFERENT_TYPES);
	}

	/**
	 * Report overwriting fields and invalid types
	 * @param statesToCheck The states to check
	 * @param statesToRemove The states to remove
	 * @param context The context to report the issue
	 * @param isRestoring A flag to look for saved or restore fields
	 * @param fieldErrorMessage The field error message to show
	 * @param invalidTypeErrorMessage The invalid type error message to show
	 */
	private void reportOverwritingFieldsAndInvalidType(@Nonnull final Map<String, AbstractInsnNode> statesToCheck,
			@Nonnull final Map<String, AbstractInsnNode> statesToRemove, @Nonnull final Context context,
			final boolean isRestoring, @Nonnull final String fieldErrorMessage,
			@Nonnull final String invalidTypeErrorMessage) {

		final Set<String> fields = new HashSet<String>();
		for (final Entry<String, AbstractInsnNode> entry : statesToCheck.entrySet()) {
			if (statesToRemove.containsKey(entry.getKey())) {
				statesToRemove.remove(entry.getKey());
			}

			final FieldInsnNode field = getField(entry.getValue(), isRestoring);
			if (field == null) {
				// we are restoring or saving a key locally
				statesToRemove.put(entry.getKey(), entry.getValue());
				continue;
			}

			final String nameSaved = field.name;
			if (fields.contains(nameSaved)) {
				context.report(OVERWRITING_FIELDS, classContext.getLocation(entry.getValue()),
						String.format(fieldErrorMessage, nameSaved));
			} else {
				fields.add(nameSaved);
			}

			final String descriptor = ((MethodInsnNode) entry.getValue()).desc;
			String type;
			if (isRestoring) {
				type = descriptor.substring(descriptor.indexOf(')') + 1);
			} else {
				// get the type of the second parameter
				type = descriptor.substring(descriptor.indexOf(';') + 1, descriptor.indexOf(')'));
			}
			reportSaveRestoreWithDifferentTypes(context, field, type, entry.getValue(), invalidTypeErrorMessage);
		}
		fields.clear();
	}

	/**
	 * Report save or restore variable with different types
	 * @param context The context to generate the report
	 * @param field The field to check the type
	 * @param expectedtype The expected type
	 * @param node The node that is being scanned
	 * @param message The message to report
	 */
	private void reportSaveRestoreWithDifferentTypes(@Nonnull final Context context, @Nonnull final FieldInsnNode field,
			@Nonnull final String expectedtype, @Nonnull final AbstractInsnNode node,
			@Nonnull final String message) {
		final String methodName = ((MethodInsnNode) node).name;

		// We are ignoring Serializable and Parcelable, this is due the code not compile
		// if we are trying to put an object that no implements Serializable or Pracelable
		if ("Ljava/io/Serializable;".equals(expectedtype)
				|| "Landroid/io/Parcelable;".equals(expectedtype)
				// Ignore Object too since all type extends from Object
				|| "Ljava/lang/Object;".equals(expectedtype)) {
			return;
		}

		// check the field type with the expected type
		if (!field.desc.equals(expectedtype)) {
			context.report(INVALID_TYPE, classContext.getLocation(node),
					String.format(message, methodName, expectedtype, field.name, field.desc));
		}
	}

	/**
	 * Look for the field of the instruction
	 * @param instruction the instruction to get the field
	 * @param goDownInTheTree true if we want to search a putField in the tree, false if we want to search a getField.
	 * @return The field of the instruction, null if not found.
	 */
	@Nullable
	private FieldInsnNode getField(@Nonnull final AbstractInsnNode instruction, final boolean goDownInTheTree) {
		AbstractInsnNode node = instruction;
		while (node != null) {
			node = goDownInTheTree ? node.getNext() : node.getPrevious();
			// check that we not are analyzing other fields
			if (node != null && isAnotherField(node)) {
				return null;
			}
			// find the field that are store our variable
			if (node != null && (goDownInTheTree && node.getOpcode() == Opcodes.PUTFIELD
					|| !goDownInTheTree && node.getOpcode() == Opcodes.GETFIELD) && node instanceof FieldInsnNode) {
				return (FieldInsnNode) node;
			}
		}
		return null;
	}

	/**
	 * Check that we are not analyzing another saved or restored field
	 * @param node The node to check
	 * @return True if the node is from other save or restore field
	 */
	private boolean isAnotherField(@Nonnull final AbstractInsnNode node) {
		// When we are restoring or saving a state in a field, the first opcode that we are going to find
		// should be a PUTFIELD or GETFIELD, but not necessarily is the next opcode, so
		// we need to check that the instruction is not another save or restore instruction too.
		return node.getOpcode() == Opcodes.INVOKEVIRTUAL && "android/os/Bundle".equals(((MethodInsnNode) node).owner)
				|| node.getOpcode() == Opcodes.GETSTATIC;
	}

	private void report(@Nonnull final Context context, @Nonnull final Map<String, AbstractInsnNode> states,
			@Nonnull final String message) {
		if (!states.isEmpty()) {
			for (final Entry<String, AbstractInsnNode> entry : states.entrySet()) {
				context.report(MISSING_SAVED_INSTANCE_STATES, classContext.getLocation(entry.getValue()),
						String.format(message, getBundleKey(entry.getValue())));
			}
		}
	}
}
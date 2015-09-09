package com.monits.linters;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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
	@Nonnull
	public List<String> getApplicableCallOwners() {
		return Collections.singletonList("android/os/Bundle");
	}

	@Override
	@Nonnull
	public int[] getApplicableAsmNodeTypes() {
		return new int[] { AbstractInsnNode.METHOD_INSN };
	}

	@Override
	public void checkInstruction(@Nonnull final ClassContext context,
			@Nonnull final ClassNode classNode, @Nonnull final MethodNode method,
			@Nonnull final AbstractInsnNode instruction) {
		if (instruction.getOpcode() != Opcodes.INVOKEVIRTUAL) {
			return;
		}

		if (classContext == null) {
			classContext = context;
		}

		if (METHOD_SAVE_INSTANCES.contains(method.name)) {
			final String bundleKey = getBundleKey(instruction);
			if (savedStates.containsKey(bundleKey)) {
				context.report(OVERWRITING_INSTANCE_STATES, context.getLocation(instruction),
						String.format(ALREADY_SAVED, bundleKey));
			} else {
				savedStates.put(bundleKey, instruction);
			}
		} else if (METHOD_RESTORE_INSTANCES.contains(method.name)) {
			final String bundleKey = getBundleKey(instruction);
			restoredStates.put(bundleKey, instruction);
		}

		super.checkInstruction(context, classNode, method, instruction);
	}

	@Nonnull
	private String getBundleKey(@Nonnull final AbstractInsnNode instruction) {
		AbstractInsnNode node = instruction;
		// get the key used
		while (!(node instanceof LdcInsnNode)) {
			node = node.getPrevious();
		}
		return ((LdcInsnNode) node).cst.toString();
	}

	@Override
	public void afterCheckFile(@Nonnull final Context context) {
		final Map<String, AbstractInsnNode> restoredStatesToCheck = new HashMap<>(restoredStates);
		final Set<String> fields = new HashSet<String>();

		for (final Entry<String, AbstractInsnNode> savedEntry : savedStates.entrySet()) {
			if (restoredStates.containsKey(savedEntry.getKey())) {
				restoredStates.remove(savedEntry.getKey());
			}
			// look for fields that already are being saved
			final FieldInsnNode fieldOnSaveState = getFieldOnSaveState(savedEntry.getValue());
			final String fieldNameSaved = fieldOnSaveState.name;
			if (fields.contains(fieldNameSaved)) {
				context.report(OVERWRITING_FIELDS, classContext.getLocation(savedEntry.getValue()),
						String.format(FIELD_ALREADY_SAVED, fieldNameSaved));
			} else {
				fields.add(fieldNameSaved);
			}

			final String descriptor = ((MethodInsnNode) savedEntry.getValue()).desc;
			// get the type of the second parameter
			final String expectedType = descriptor.substring(descriptor.indexOf(';') + 1, descriptor.indexOf(')'));
			reportSaveRestoreWithDifferentTypes(context, fieldOnSaveState, expectedType, savedEntry.getValue(),
					SAVED_WITH_DIFERENT_TYPES);
		}

		fields.clear();

		for (final Entry<String, AbstractInsnNode> restoredEntry : restoredStatesToCheck.entrySet()) {
			if (savedStates.containsKey(restoredEntry.getKey())) {
				savedStates.remove(restoredEntry.getKey());
			}
			final FieldInsnNode fieldOnRestoreState = getFieldOnRestoreState(restoredEntry.getValue());
			// look for fields that are being overwriting
			final String fieldNameRestore = fieldOnRestoreState.name;
			if (fields.contains(fieldNameRestore)) {
				context.report(OVERWRITING_FIELDS, classContext.getLocation(restoredEntry.getValue()),
						String.format(FIELD_ALREADY_RESTORED, fieldNameRestore));
			} else {
				fields.add(fieldNameRestore);
			}

			final String descriptor = ((MethodInsnNode) restoredEntry.getValue()).desc;
			final String returnType = descriptor.substring(descriptor.indexOf(')') + 1);
			reportSaveRestoreWithDifferentTypes(context, fieldOnRestoreState, returnType, restoredEntry.getValue(),
					RESTORED_WITH_DIFERENT_TYPES);
		}

		// report
		report(context, savedStates, SAVED_BUT_NEVER_RESTORED);
		report(context, restoredStates, RESTORED_BUT_NEVER_SAVED);
		// reset all
		classContext = null;
		savedStates.clear();
		restoredStates.clear();
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
		// check the field type with the expected type
		if (!field.desc.equals(expectedtype)) {
			context.report(INVALID_TYPE, classContext.getLocation(node),
					String.format(message, methodName, expectedtype, field.name, field.desc));
		}
	}

	@Nonnull
	private FieldInsnNode getFieldOnSaveState(@Nonnull final AbstractInsnNode instruction) {
		return getField(instruction, false);
	}

	@Nonnull
	private FieldInsnNode getFieldOnRestoreState(@Nonnull final AbstractInsnNode instruction) {
		return getField(instruction, true);
	}

	/**
	 * Look for the field of the instruction
	 * @param instruction the instruction to get the field
	 * @param goDownInTheTree true if we want to search a putField in the tree, false if we want to search a getField.
	 * @return The field of the instruction
	 */
	@Nonnull
	private FieldInsnNode getField(@Nonnull final AbstractInsnNode instruction, final boolean goDownInTheTree) {
		AbstractInsnNode node = instruction;
		while (!(node instanceof FieldInsnNode)) {
			if (goDownInTheTree) {
				node = node.getNext();
			} else {
				node = node.getPrevious();
			}
		}
		return (FieldInsnNode) node;
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
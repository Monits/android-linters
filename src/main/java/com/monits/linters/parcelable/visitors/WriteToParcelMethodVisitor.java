package com.monits.linters.parcelable.visitors;

import static org.objectweb.asm.Opcodes.GETFIELD;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import com.android.tools.lint.detector.api.ClassContext;
import com.google.common.collect.Multimap;
import com.monits.linters.parcelable.methods.AbstractInnerMethod;
import com.monits.linters.parcelable.methods.WriteInnerMethod;
import com.monits.linters.parcelable.models.Method;
import com.monits.linters.parcelable.models.ParcelMethodManager;
import com.monits.linters.parcelable.models.ParcelableField;
import com.monits.linters.parcelable.models.QueueManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "CD_CIRCULAR_DEPENDENCY",
		justification = "Solved in super class")
public class WriteToParcelMethodVisitor extends AbstractMethodVisitor {

	/**
	 * Creates a new WriteToParcelMethodVisitor instance
	 * See {@link AbstractMethodVisitor} for more information.
	 *
	 * @param api The api for {@link MethodVisitor}
	 * @param classNode The class node that represents analyzed object's class
	 * @param method The method's name
	 * @param desc The desc used to call super
	 * @param context The class context
	 * @param cr The class reader that parses the class
	 * @param queueManager The queue manager for methods invocation
	 */

	public WriteToParcelMethodVisitor(final int api,
		@Nonnull final ClassNode classNode, @Nonnull final String method,
		@Nonnull final String desc, @Nonnull final ClassContext context,
		@Nonnull final ClassReader cr, @Nonnull final QueueManager queueManager) {
		super(api, classNode, method, desc, context, cr, queueManager);
	}


	@Override
	public void addFieldToQueue(@Nonnull final ParcelableField field) {
		queueManager.getWriteFieldQueue().offer(field);
	}

	@Override
	public boolean opcodeNeedsToBeHandled(final int opcode) {
		return opcode == GETFIELD;
	}

	@Override
	public void addMethodToQueue(@Nonnull final Method method) {
		final Multimap<String, String> parcelableMethods = ParcelMethodManager.INSTANCE
				.getParcelableMethods();
		if (parcelableMethods.values().contains(method.getName())) {
			queueManager.getWriteMethodQueue().add(method);
		}
	}

	@Override
	public AbstractInnerMethod createInnerMethod(@Nonnull final String methodName,
			@Nonnull final String desc) {
		return new WriteInnerMethod(api, classNode, methodName,
				desc, context, cr, queueManager);
	}
}
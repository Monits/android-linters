package com.monits.linters.parcelable.methods;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import com.android.tools.lint.detector.api.ClassContext;
import com.monits.linters.parcelable.models.ParcelableField;
import com.monits.linters.parcelable.models.QueueManager;
import com.monits.linters.parcelable.visitors.ParcelConstructorMethodVisitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "CD_CIRCULAR_DEPENDENCY",
	justification = "This class must create a ParcelConstructorMethodVisitor "
	+ "instead of using a boolean")
public class ReadInnerMethod extends AbstractInnerMethod {
	private final MethodVisitor methodVisitor;

	/**
	 * Creates a new ReadInnerMethod instance
	 *
	 * @param api The api for {@link #methodVisitor}
	 * @param classNode The class node for {@link #methodVisitor}
	 * @param methodName This class method name
	 * @param desc The desc for {@link #methodVisitor}
	 * @param context The context for {@link #methodVisitor}
	 * @param cr The class reader for {@link #methodVisitor}
	 * @param queueManager The queue manager for {@link #methodVisitor}
	 */
	public ReadInnerMethod(final int api,
			@Nonnull final ClassNode classNode, @Nonnull final String methodName,
			@Nonnull final String desc, @Nonnull final ClassContext context,
			@Nonnull final ClassReader cr, @Nonnull final QueueManager<ParcelableField> queueManager) {
		super(methodName);
		this.methodVisitor = new ParcelConstructorMethodVisitor(api, classNode,
				methodName, desc, context, cr, queueManager);
	}

	@Nonnull
	@Override
	public MethodVisitor getMethodVisitor() {
		return methodVisitor;
	}

	@Override
	public String toString() {
		return "ReadInnerMethod [ methodVisitor=" + methodVisitor + " ]";
	}
}
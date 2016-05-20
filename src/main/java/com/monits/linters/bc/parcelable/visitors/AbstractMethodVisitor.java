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
package com.monits.linters.bc.parcelable.visitors;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.android.tools.lint.detector.api.ClassContext;
import com.monits.linters.bc.parcelable.methods.AbstractInnerMethod;
import com.monits.linters.bc.parcelable.models.Method;
import com.monits.linters.bc.parcelable.models.ParcelableField;
import com.monits.linters.bc.parcelable.models.QueueManager;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "CD_CIRCULAR_DEPENDENCY",
		justification = "This class must create a ParcelClassVisitor to visit"
	+ " the class private method.")
public abstract class AbstractMethodVisitor extends MethodVisitor {

	public static final String THIS = "this";
	private static final String OBJECT_CLASS = Type.getDescriptor(Object.class);
	private static final String PARCELABLE_INTERFACE = "android/os/Parcelable";
	private static final String PARCELABLE_OWNER = "android/os/Parcel";

	@SuppressFBWarnings(value = "MISSING_FIELD_IN_TO_STRING",
			justification = "Field used for local validation")
	private final String method;

	@SuppressFBWarnings(value = "MISSING_FIELD_IN_TO_STRING",
			justification = "Field used for local validation")
	private final String desc;

	@SuppressFBWarnings(value = "MISSING_FIELD_IN_TO_STRING",
			justification = "Volatile field. Doesn't provide meaningful "
			+ "value of class or instance")
	private int line;
	protected final ClassNode classNode;
	protected final ClassContext context;
	protected final ClassReader cr;
	protected QueueManager queueManager;

	/**
	 * Creates a new AbstractMethodVisitor instance
	 *
	 * @param api The api for {@link MethodVisitor}
	 * @param classNode The class node that represents analyzed object's class
	 * @param method The method's name
	 * @param desc The desc used to call super
	 * @param context The class context
	 * @param cr The class reader that parses the class
	 * @param queueManager The queue manager for methods invocation
	 */
	public AbstractMethodVisitor(final int api, @Nonnull final ClassNode classNode,
		@Nonnull final String method, @Nonnull final String desc,
		@Nonnull final ClassContext context, @Nonnull final ClassReader cr,
		@Nonnull final QueueManager queueManager) {
		super(api);
		this.classNode = classNode;
		this.method = method;
		this.desc = desc;
		this.context = context;
		this.cr = cr;
		this.queueManager = queueManager;
	}

	@Override
	public void visitLineNumber(final int line, @Nonnull final Label start) {
		this.line = line;
	}

	@Override
	public void visitFieldInsn(final int opcode, @Nonnull final String owner,
		@Nonnull final String name, @Nonnull final String desc) {
		if (opcodeNeedsToBeHandled(opcode)) {
			final ParcelableField field = new ParcelableField(name, desc,
					context.getLocationForLine(line, null, null, null), getMethodContainer(method));
			addFieldToQueue(field);
		}
	}

	@Override
	public void visitMethodInsn(final int opcode, @Nonnull final String owner,
			@Nonnull final String name, @Nonnull final String desc,
			final boolean itf) {
		if (INVOKESPECIAL == opcode) {
			handleInvokeSpecial(owner, name, desc);
		} else if (INVOKEVIRTUAL == opcode) {
			if (owner.equals(classNode.name)) {
				cr.accept(new ParcelClassVisitor(api, classNode,
					createInnerMethod(name, desc), context, queueManager, cr), 0);
			} else if (PARCELABLE_OWNER.equals(owner)) {
				addMethodToQueue(new Method(name, context.getLocationForLine(line, null, null, null),
						getMethodContainer(method)));
			}
		}
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	private void handleInvokeSpecial(@Nonnull final String owner,
		@Nonnull final String name, @Nonnull final String desc) {
		if (needToCallSuper(owner, name, desc)) {
			final ParcelableField field = new ParcelableField(THIS, owner,
				context.getLocationForLine(line, null, null, null), getMethodContainer(method));
			addFieldToQueue(field);
		} else if (!name.equals(method)
				&& owner.equals(classNode.name)) {
			/*
			 * The invoke special belongs to a private method
			 */
			cr.accept(new ParcelClassVisitor(api, classNode,
					createInnerMethod(name, desc), context, queueManager, cr), 0);
		}
	}

	@Nullable
	private MethodNode getMethodContainer(@Nonnull final String name) {
		final List<MethodNode> methods = classNode.methods;
		for (final MethodNode methodContainer : methods) {
			if (methodContainer.name.equals(name)) {
				return methodContainer;
			}
		}
		return null;
	}

	/**
	 * Check if you need to call the super method of your parcelable parent
	 * class
	 *
	 * @param owner The owner of the method instruction
	 * @param name The name of the method instruction
	 * @param desc The desc of the method instruction
	 * @return if you are calling super or not
	 */
	private boolean needToCallSuper(@Nonnull final String owner,
		@Nonnull final String name, @Nonnull final String desc) {
		return name.equals(method) && !owner.equals(classNode.name)
			&& !OBJECT_CLASS.equals(classNode.superName)
			&& !classNode.interfaces.contains(PARCELABLE_INTERFACE)
			&& desc.equals(this.desc);
	}

	/**
	 * Check if the you have to handle the opcode
	 *
	 * @param opcode The opcode to be handling
	 * @return if the opcode is handling
	 */
	public abstract boolean opcodeNeedsToBeHandled(final int opcode);

	/**
	 * Add the field in the write queue or in the read queue
	 *
	 * @param field The field that you must add into the queue
	 */
	public abstract void addFieldToQueue(@Nonnull final ParcelableField field);

	/**
	 * Add the method in the write queue or in the read queue
	 *
	 * @param method
	 *            The method that you must add into the queue
	 */
	public abstract void addMethodToQueue(@Nonnull final Method method);

	/**
	 * Create a {@link WriteToParcelMethodVisitor} or a
	 * {@link ParcelConstructorMethodVisitor} to visit the private
	 * method of the class
	 *
	 * @param methodName The name of the private method
	 * @param desc The descriptor of the private method
	 * @return The MethodVisitor for the private method
	 */
	@Nonnull
	public abstract AbstractInnerMethod createInnerMethod(@Nonnull final String methodName,
			@Nonnull final String desc);
}
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
package com.monits.linters.parcelable.visitors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import com.android.tools.lint.detector.api.ClassContext;
import com.monits.linters.parcelable.methods.AbstractInnerMethod;
import com.monits.linters.parcelable.models.QueueManager;

public class ParcelClassVisitor extends ClassVisitor {
	private static final String PARCEL_CONSTRUCTOR_DESC = "(Landroid/os/Parcel;)V";
	private static final String WRITE_TO_PARCEL_DESC = "(Landroid/os/Parcel;I)V";
	private static final String WRITE_TO_PARCEL_METHOD = "writeToParcel";
	private static final String CONSTRUCTOR = "<init>";
	private final AbstractInnerMethod innerMethod;
	private final ClassNode classNode;
	private final ClassContext context;
	private final ClassReader cr;
	private final QueueManager queueManager;

	/**
	 * Creates a new ParcelClassVisitor instance, with
	 * fewer parameters for automatic null setting of {@link #innerMethod}.
	 *
	 * @param api The api for {@link ClassVisitor}
	 * @param classNode The class node that represents analyzed object's class
	 * @param context The class context
	 * @param queueManager The queue manager for methods invocation
	 * @param classReader The class reader that parses the class
	 */
	public ParcelClassVisitor(final int api, @Nonnull final ClassNode classNode,
		@Nonnull final ClassContext context, @Nonnull final QueueManager queueManager,
		@Nonnull final ClassReader classReader) {
		this(api, classNode, null, context, queueManager, classReader);
	}

	/**
	 * Creates a new ParcelClassVisitor instance.
	 *
	 * @param api The api for {@link ClassVisitor}
	 * @param classNode The class node that represents analyzed class
	 * @param innerMethod The inner method
	 * @param context The class context
	 * @param queueManager The queue manager for methods invocation
	 * @param classReader The class reader that parses the class
	 */
	public ParcelClassVisitor(final int api, @Nonnull final ClassNode classNode,
		@Nullable final AbstractInnerMethod innerMethod, @Nonnull final ClassContext context,
		@Nonnull final QueueManager queueManager, @Nonnull final ClassReader classReader) {
		super(api);
		this.classNode = classNode;
		this.innerMethod = innerMethod;
		this.context = context;
		this.queueManager = queueManager;
		cr = classReader;
	}

	@Override
	public MethodVisitor visitMethod(final int access, @Nonnull final String name,
		@Nonnull final String desc, @Nonnull final String signature,
		@Nonnull final String[] exceptions) {
		final MethodVisitor writeMv = new WriteToParcelMethodVisitor(api,
			classNode, name, desc, context, cr, queueManager);
		final MethodVisitor constructorMv = new ParcelConstructorMethodVisitor(
			api, classNode, name, desc, context, cr, queueManager);
		if (innerMethod == null) {
			if (WRITE_TO_PARCEL_METHOD.equals(name)
				&& WRITE_TO_PARCEL_DESC.equals(desc)) {
				return writeMv;
			} else if (CONSTRUCTOR.equals(name)
				&& PARCEL_CONSTRUCTOR_DESC.equals(desc)) {
				return constructorMv;
			}
		} else if (innerMethod.getMethodName().equals(name)) {
			return innerMethod.getMethodVisitor();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Parcel constructor method with " + innerMethod;
	}
}
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
package com.monits.linters.parcelable.methods;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import com.android.tools.lint.detector.api.ClassContext;
import com.monits.linters.parcelable.models.QueueManager;
import com.monits.linters.parcelable.visitors.WriteToParcelMethodVisitor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "CD_CIRCULAR_DEPENDENCY",
	justification = "This class must create a WriteToParcelMethodVisitor "
	+ "instead of using a boolean")
public class WriteInnerMethod extends AbstractInnerMethod {
	private final MethodVisitor methodVisitor;

	/**
	 * Creates a new WriteInnerMethod instance
	 *
	 * @param api The api for {@link #methodVisitor}
	 * @param classNode The class node for {@link #methodVisitor}
	 * @param methodName This class method name
	 * @param desc The desc for {@link #methodVisitor}
	 * @param context The context for {@link #methodVisitor}
	 * @param cr The class reader for {@link #methodVisitor}
	 * @param queueManager The queue manager for {@link #methodVisitor}
	 */

	public WriteInnerMethod(final int api,
			@Nonnull final ClassNode classNode, @Nonnull final String methodName,
			@Nonnull final String desc, @Nonnull final ClassContext context,
			@Nonnull final ClassReader cr, @Nonnull final QueueManager queueManager) {
		super(methodName);
		methodVisitor = new WriteToParcelMethodVisitor(api, classNode,
				methodName, desc, context, cr, queueManager);
	}

	@Nonnull
	@Override
	public MethodVisitor getMethodVisitor() {
		return methodVisitor;
	}

	@Override
	public String toString() {
		return "WriteInnerMethod [ methodVisitor=" + methodVisitor + " ]";
	}
}
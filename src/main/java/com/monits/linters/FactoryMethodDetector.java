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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.ClassContext;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.Sets;

public class FactoryMethodDetector extends Detector implements Detector.ClassScanner {

	public static final String FACTORY_METHOD_ERROR_MESSAGE = "Avoid creating the fragment, "
			+ "use Factory Methods instead";

	public static final Issue USE_FACTORY_METHOD_INSTEAD_NEW_FRAGMENT = Issue.create(
			"UseFactoryMethodInsteadNewFragment",
			"Use Factory Methods to instanciate a Fragment",
			"This will prevent the possibility of accidentally forgetting "
			+ "to set the arguments when the Fragment is created.\n"
			+ "From the Fragment documentation:\n"
			+ "...since these constructors will not be called when the fragment is re-instantiated; "
			+ "instead, arguments can be supplied by the caller with setArguments(Bundle)...",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(FactoryMethodDetector.class, Scope.CLASS_FILE_SCOPE));

	private static final Set<String> FRAGMENT_TYPE =
			Sets.newHashSet("android/support/v4/app/Fragment", "android/app/Fragment");

	@Override
	@Nullable
	public int[] getApplicableAsmNodeTypes() {
		// TYPE_INSN check for opcodes as NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
		return new int[] { AbstractInsnNode.TYPE_INSN };
	}

	@Override
	public void checkInstruction(@Nonnull final ClassContext context,
			@Nonnull final ClassNode classNode, @Nonnull final MethodNode method,
			@Nonnull final AbstractInsnNode instruction) {

		if (instruction.getOpcode() != Opcodes.NEW) {
			return;
		}

		if (!isFragmentDescendant(context, ((TypeInsnNode) instruction).desc)) {
			return;
		}

		if (isInFactoryMethodOfSameFragment(classNode, method)) {
			return;
		}

		context.report(USE_FACTORY_METHOD_INSTEAD_NEW_FRAGMENT, method, instruction,
				context.getLocation(instruction.getNext().getNext()), FACTORY_METHOD_ERROR_MESSAGE);

		super.checkInstruction(context, classNode, method, instruction);
	}

	@Nullable
	private ClassNode getClassNode(@Nonnull final ClassContext context, @Nonnull final String descriptor) {
		return context.getDriver().findClass(context, descriptor, 0);
	}

	private boolean isFragmentDescendant(@Nonnull final ClassContext context, @Nonnull final String className) {
		ClassNode classNode = getClassNode(context, className);
		while (classNode != null && !FRAGMENT_TYPE.contains(classNode.superName)) {
			// check again if the parent is a fragment
			classNode = getClassNode(context, classNode.superName);
		}
		// is the classNode is null the class is not a Fragment descendant
		return classNode != null;
	}

	private boolean isInFactoryMethodOfSameFragment(@Nonnull final ClassNode classNode,
			@Nonnull final MethodNode method) {
		final String methodDesc = method.desc;
		final String returnType = methodDesc.substring(methodDesc.lastIndexOf(')') + 1, methodDesc.length());
		// if method.access & Opcodes.ACC_STATIC != 0 then the method is STATIC
		return (method.access & Opcodes.ACC_STATIC) != 0
				// if the return type is a class we have 'L[class name];'
				&& returnType.charAt(0) == 'L' && returnType.endsWith(";")
				// get the class name to check if it is the same as the class
				&& returnType.substring(returnType.indexOf('L') + 1, returnType.length() - 1)
				.equals(classNode.name);
	}
}
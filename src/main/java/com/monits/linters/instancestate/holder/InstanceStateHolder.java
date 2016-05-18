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
package com.monits.linters.instancestate.holder;

import javax.annotation.Nonnull;

import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Class to hold the instruction and the method of the state to be analyzed
 */
public class InstanceStateHolder {

	private final MethodInsnNode instruction;
	private final MethodNode methodNode;

	/**
	 * Create a new instance
	 *
	 * @param instruction The instruction of the state analyzed
	 * @param methodNode The methodNode of the state analyzed
	 */
	public InstanceStateHolder(@Nonnull final MethodInsnNode instruction, @Nonnull final MethodNode methodNode) {
		this.instruction = instruction;
		this.methodNode = methodNode;
	}

	/**
	 * @return the instruction
	 */
	@Nonnull
	public MethodInsnNode getInstruction() {
		return instruction;
	}

	/**
	 * @return the methodNode
	 */
	@Nonnull
	public MethodNode getMethodNode() {
		return methodNode;
	}
}
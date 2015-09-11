/*
	Copyright 2010-2015 Monits

	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
	file except in compliance with the License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software distributed under
	the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
	ANY KIND, either express or implied. See the License for the specific language governing
	permissions and limitations under the License.
 */
package com.monits.linters.parcelable.methods;

import javax.annotation.Nonnull;

import org.objectweb.asm.MethodVisitor;

public abstract class AbstractInnerMethod {
	private final String methodName;

	/**
	 * Creates a new AbstractInnerMethod instance
	 *
	 * @param methodName The method's name.
	 */
	public AbstractInnerMethod(@Nonnull final String methodName) {
		this.methodName = methodName;
	}

	@Override
	public String toString() {
		return "AbstractInnerMethod [ methodName=" + methodName + " ]";
	}

	@Nonnull
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Returns this class {@link MethodVisitor}. Subclasses MUST implement
	 * this method.
	 * @return methodVisitor encapsulated in the subclass.
	 */
	@Nonnull
	public abstract MethodVisitor getMethodVisitor();
}
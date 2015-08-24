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
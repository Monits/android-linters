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
package com.monits.linters.parcelable.models;

import javax.annotation.Nonnull;

import org.objectweb.asm.tree.MethodNode;

import com.android.tools.lint.detector.api.Location;

public class ParcelableField {
	private static final int SEVENTEEN_PRIME = 17;
	private static final int THIRTY_ONE_PRIME = 31;
	private final String name;
	private final String className;
	private final Location location;
	private final MethodNode methodContainer;

	/**
	 * Creates a new ParcelableField instance
	 *
	 * @param name The field's name
	 * @param className The field's class name
	 * @param location The field's location
	 * @param methodContainer the method container
	 */

	public ParcelableField(@Nonnull final String name,
		@Nonnull final String className,
		@Nonnull final Location location, @Nonnull final MethodNode methodContainer) {
		this.name = name;
		this.className = className;
		this.location = location;
		this.methodContainer = methodContainer;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ParcelableField) {
			final ParcelableField field = (ParcelableField) obj;
			return field.name.equals(name)
					&& field.className.equals(className);
		}
		return false;
	}

	@Nonnull
	public Location getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		return SEVENTEEN_PRIME * name.hashCode() + THIRTY_ONE_PRIME
			* className.hashCode();
	}

	@Override
	public String toString() {
		return "Field [ name=" + name + ", className="
			+ className + ", location=" + location + " ]";
	}

	@Nonnull
	public MethodNode getMethodContainer() {
		return methodContainer;
	}

	@Nonnull
	public String getName() {
		return name;
	}
}
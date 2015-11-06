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

public class Method {
	private final String name;
	private final Location location;
	private final MethodNode methodContainer;

	/**
	 *  Creates a new Method instance.
	 * @param name The method's name
	 * @param location The method's location
	 * @param methodContainer The method container
	 */
	public Method(@Nonnull final String name, @Nonnull final Location location,
			@Nonnull final MethodNode methodContainer) {
		this.name = name;
		this.location = location;
		this.methodContainer = methodContainer;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "Method [ name=" + name + ", location=" + location + " ]";
	}

	@Nonnull
	public MethodNode getMethodContainer() {
		return methodContainer;
	}
}
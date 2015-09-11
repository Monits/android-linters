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
package com.monits.linters.parcelable.models;

import javax.annotation.Nonnull;

import com.android.tools.lint.detector.api.Location;

public class Method {
	private final String name;
	private final Location location;

	/**
	 *  Creates a new Method instance.
	 * @param name The method's name
	 * @param location The method's location
	 */

	public Method(@Nonnull final String name, @Nonnull final Location location) {
		this.name = name;
		this.location = location;
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
}

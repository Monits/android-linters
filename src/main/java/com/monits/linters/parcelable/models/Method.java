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

package com.monits.linters.parcelable.models;

import javax.annotation.Nonnull;

import com.android.tools.lint.detector.api.Location;

public class ParcelableField {
	private static final int SEVENTEEN_PRIME = 17;
	private static final int THIRTY_ONE_PRIME = 31;
	private final String name;
	private final String className;
	private final Location location;

	/**
	 * Creates a new ParcelableField instance
	 *
	 * @param name The field's name
	 * @param className The field's class name
	 * @param location The field's location
	 */

	public ParcelableField(@Nonnull final String name,
		@Nonnull final String className,
		@Nonnull final Location location) {
		this.name = name;
		this.className = className;
		this.location = location;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ParcelableField) {
			final ParcelableField field = (ParcelableField) obj;
			return field.name.equals(this.name)
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


}
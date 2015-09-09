package com.monits.linters.parcelable.models;

import javax.annotation.Nonnull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public enum ParcelMethodManager {
	INSTANCE;
	private Multimap<String, String> parcelableMethods;

	private ParcelMethodManager() {
		this.parcelableMethods = HashMultimap.create();
		addMethods();
	}

	@SuppressWarnings("checkstyle:multiplestringliterals")
	private void addMethods() {
		parcelableMethods.put("readArray", "writeArray");
		parcelableMethods.put("readArrayList", "writeList");
		parcelableMethods.put("readBinderArray", "writeBinderArray");
		parcelableMethods.put("readBinderList", "writeBinderList");
		parcelableMethods.put("readBooleanArray", "writeBooleanArray");
		parcelableMethods.put("readBundle", "writeBundle");
		parcelableMethods.put("readByte", "writeByte");
		parcelableMethods.put("readByteArray", "writeByteArray");
		parcelableMethods.put("readCharArray", "writeCharArray");
		parcelableMethods.put("readDouble", "writeDouble");
		parcelableMethods.put("readDoubleArray", "writeDoubleArray");
		parcelableMethods.put("readException", "writeException");
		parcelableMethods.put("readFileDescriptor", "writeFileDescriptor");
		parcelableMethods.put("readFloat", "writeFloat");
		parcelableMethods.put("readFloatArray", "writeFloatArray");
		parcelableMethods.put("readHashMap", "writeMap");
		parcelableMethods.put("readInt", "writeInt");
		parcelableMethods.put("readIntArray", "writeIntArray");
		parcelableMethods.put("readList", "writeList");
		parcelableMethods.put("readLong", "writeLong");
		parcelableMethods.put("readLongArray", "writeLongArray");
		parcelableMethods.put("readMap", "writeMap");
		parcelableMethods.put("readParcelable", "writeParcelable");
		parcelableMethods.put("readParcelableArray", "writeParcelableArray");
		parcelableMethods.put("readPersistableBundle", "writePersistableBundle");
		parcelableMethods.put("readSerializable", "writeSerializable");
		parcelableMethods.put("readSize", "writeSize");
		parcelableMethods.put("readSizeF", "writeSizeF");
		parcelableMethods.put("readSparseArray", "writeSparseArray");
		parcelableMethods.put("readSparseBooleanArray", "writeSparseBooleanArray");
		parcelableMethods.put("readString", "writeString");
		parcelableMethods.put("readStringArray", "writeStringArray");
		parcelableMethods.put("readStringList", "writeStringList");
		parcelableMethods.put("readStrongBinder", "writeStrongBinder");
		parcelableMethods.put("readStrongBinder", "writeInterfaceToken");
		parcelableMethods.put("readStrongBinder", "writeStrongInterface");
		parcelableMethods.put("readTypedArray", "writeTypedArray");
		parcelableMethods.put("readTypedList", "writeTypedList");
		parcelableMethods.put("readValue", "writeValue");
	}

	@Nonnull
	public Multimap<String, String> getParcelableMethods() {
		return parcelableMethods;
	}
}

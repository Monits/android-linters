package com.monits.linters.ast.instancestate;

import lombok.ast.MethodInvocation;

/* default */ class BundleMethodInvocation {
	/* default */ final MethodInvocation node;
	/* default */ final String key;
	/* default */ final String dataType;
	/* default */ final String fieldName;
	
	public BundleMethodInvocation(final MethodInvocation node, final String key,
			final String dataType, final String fieldName) {
		super();
		this.node = node;
		this.key = key;
		this.dataType = dataType;
		this.fieldName = fieldName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BundleMethodInvocation)) {
			return false;
		}
		BundleMethodInvocation other = (BundleMethodInvocation) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BundleMethodInvocation [node=" + node + ", key=" + key
				+ ", dataType=" + dataType + ", fieldName=" + fieldName + "]";
	}
}
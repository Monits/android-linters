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
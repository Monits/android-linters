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
package com.monits.linters;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.monits.linters.ast.ViewInflateDetector;
import com.monits.linters.bc.FactoryMethodDetector;
import com.monits.linters.ast.instancestate.InstanceStateDetector;
import com.monits.linters.bc.parcelable.ParcelDetector;
import com.monits.linters.manifest.DuplicatePermissionDetector;

public class MonitsIssueRegistry extends IssueRegistry {

	@Nonnull
	@Override
	public List<Issue> getIssues() {
		return Arrays.asList(DuplicatePermissionDetector.DUPLICATE_USES_PERMISSION,
			ParcelDetector.MISSING_OR_OUT_OF_ORDER,
			ParcelDetector.INCOMPATIBLE_READ_WRITE_TYPE,
			FactoryMethodDetector.USE_FACTORY_METHOD_INSTEAD_NEW_FRAGMENT,
			InstanceStateDetector.DIFFERENT_FIELDS,
			InstanceStateDetector.INVALID_TYPE,
			InstanceStateDetector.KEY_IS_NOT_CONSTANT,
			InstanceStateDetector.RESTORED_BUT_NEVER_SAVED,
			InstanceStateDetector.SAVED_BUT_NEVER_RESTORED,
			InstanceStateDetector.STATE_ALREADY_RESTORED,
			InstanceStateDetector.STATE_ALREADY_SAVED,
			ViewInflateDetector.VIEW_INFLATE_IGNORES_THEME);
	}
}
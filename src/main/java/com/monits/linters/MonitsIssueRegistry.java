package com.monits.linters;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.monits.linters.parcelable.ParcelDetector;

public class MonitsIssueRegistry extends IssueRegistry {

	@Nonnull
	@Override
	public List<Issue> getIssues() {
		return Arrays.asList(ManifestDetector.DUPLICATE_USES_PERMISSION,
			ParcelDetector.MISSING_OR_OUT_OF_ORDER,
			ParcelDetector.INCOMPATIBLE_READ_WRITE_TYPE,
			FactoryMethodDetector.USE_FACTORY_METHOD_INSTEAD_NEW_FRAGMENT);
	}
}
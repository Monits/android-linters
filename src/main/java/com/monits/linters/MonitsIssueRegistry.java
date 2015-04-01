package com.monits.linters;


import java.util.Arrays;
import java.util.List;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

public class MonitsIssueRegistry extends IssueRegistry {

	@Override
	public List<Issue> getIssues() {
		return Arrays.asList(ManifestDetector.DUPLICATE_USES_PERMISSION);
	}
}
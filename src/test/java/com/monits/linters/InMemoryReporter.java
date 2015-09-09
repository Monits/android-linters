package com.monits.linters;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.android.tools.lint.Reporter;
import com.android.tools.lint.Warning;

public class InMemoryReporter extends Reporter {

	private final List<Warning> issues;

	protected InMemoryReporter() throws IOException {
		super(null, File.createTempFile("lint-test-", "report"));

		issues = new LinkedList<>();
	}

	@Override
	public void write(final int errorCount, final int warningCount,
		@Nonnull final List<Warning> issues) throws IOException {
		this.issues.addAll(issues);
	}

	@Nonnull
	public List<Warning> getIssues() {
		return issues;
	}
}

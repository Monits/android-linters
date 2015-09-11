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

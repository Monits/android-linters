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
package com.monits.linters.matchers;

import javax.annotation.Nonnull;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.android.tools.lint.Warning;

public class WarningMatcher extends BaseMatcher<Warning> {
	private final String fileName;
	private final String message;
	private final int line;

	/**
	 * Constructor
	 *
	 * @param fileName The file name where the warning is reported
	 * @param errorMessage The warning message
	 * @param line The line number where the warning is reported
	 */
	public WarningMatcher(@Nonnull final String fileName, @Nonnull final String errorMessage,
			final int line) {
		this.fileName = fileName;
		message = errorMessage;
		this.line = line;
	}

	@Override
	public boolean matches(final Object obj) {
		if (obj instanceof Warning) {
			final Warning warning = (Warning) obj;

			boolean criteriaMatches = true;

			criteriaMatches &= warning.file.getName().equals(fileName);

			// https://android.googlesource.com/platform/tools/base/+/1efa1e4500083a544191c5f2395ef67c0ec37aa5/
			// lint/cli/src/main/java/com/android/tools/lint/TextReporter.java#118
			// The code line reported in the Warning class is the previous code line,
			// when they are reporting the issue they are adding a 1.
			// We need to subtract a 1 to match with the reported code line.
			criteriaMatches &= warning.line == line - 1;

			criteriaMatches &= warning.message.equals(message);

			return criteriaMatches;
		} else {
			return false;
		}
	}

	@Override
	public void describeTo(final Description description) {
		description
			.appendText("Warning with:\n")
			.appendText("fileName=").appendValue(fileName).appendText(",")
			.appendText("message=").appendValue(message).appendText(",")
			.appendText("line=").appendValue(line);
	}

	@Override
	public String toString() {
		return "WarningMatcher [ fileName=" + fileName + ", "
			+ "message=" + message + ", line=" + line + " ]";
	}
}

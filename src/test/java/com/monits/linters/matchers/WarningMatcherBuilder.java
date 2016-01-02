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
package com.monits.linters.matchers;

import javax.annotation.Nonnull;

import org.hamcrest.Matcher;

import com.android.tools.lint.Warning;

public class WarningMatcherBuilder {
	private String fileName;
	private String message;
	private int line;

	/**
	 * Set the file name where the warning is reported
	 *
	 * @param fileName The file name
	 * @return The builder for the warning matcher
	 */
	@Nonnull
	public WarningMatcherBuilder fileName(@Nonnull final String fileName) {
		this.fileName = fileName;
		return this;
	}

	/**
	 * Set the message of the warning
	 *
	 * @param message The warning message
	 * @return The builder for the warning matcher
	 */
	@Nonnull
	public WarningMatcherBuilder message(@Nonnull final String message) {
		this.message = message;
		return this;
	}

	/**
	 * Set the line number where the warning is reported
	 *
	 * @param line The line number
	 * @return The builder for the warning matcher
	 */
	@Nonnull
	public WarningMatcherBuilder line(final int line) {
		this.line = line;
		return this;
	}

	/**
	 * Create a {@link WarningMatcher}
	 *
	 * @return The {@link WarningMatcher}
	 */
	@Nonnull
	public Matcher<Warning> build() {
		return new WarningMatcher(fileName, message, line);
	}

	@Override
	public String toString() {
		return "WarningMatchersBuilder [ fileName=" + fileName + ", "
				+ "message=" + message + ", line=" + line + " ]";
	}

}

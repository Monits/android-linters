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

			criteriaMatches &= warning.line == line;

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

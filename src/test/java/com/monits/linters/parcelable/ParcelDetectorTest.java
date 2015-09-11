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
package com.monits.linters.parcelable;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.TextFormat;
import com.monits.linters.AbstractTestCase;
import com.monits.linters.matchers.WarningMatcherBuilder;

public class ParcelDetectorTest extends AbstractTestCase {
	private static final String MISSING_OR_OUT_OF_ERROR_MESSAGE = ParcelDetector.MISSING_OR_OUT_OF_ORDER
		.getBriefDescription(TextFormat.TEXT);

	@Override
	protected Detector getDetector() {
		return new ParcelDetector();
	}

	@Override
	protected List<Issue> getIssues() {
		return Arrays.asList(ParcelDetector.INCOMPATIBLE_READ_WRITE_TYPE, ParcelDetector.MISSING_OR_OUT_OF_ORDER);
	}

	public void testReadLessVariables() throws Exception {
		lintProject(compile(file("ReadLessVariables.java.txt=>src/ReadLessVariables.java")));

		assertThat("Failed to retrieve all writing fields",
				getWarnings(),
				Matchers.contains(new WarningMatcherBuilder()
					.fileName("ReadLessVariables.java")
					.line(29)
					.message(MISSING_OR_OUT_OF_ERROR_MESSAGE)
					.build()));
	}

	public void testForgetCallingSuper() throws Exception {
		lintProject(compile(file("ForgetCallingSuperClass.java.txt=>src/ForgetCallingSuperClass.java",
			"SuperClass.java.txt=>src/SuperClass.java")));

		final WarningMatcherBuilder warningMatchersBuilder = new WarningMatcherBuilder()
			.fileName("ForgetCallingSuperClass.java")
			.message(MISSING_OR_OUT_OF_ERROR_MESSAGE);

		assertThat("Failed to check super method call",
				getWarnings(), Matchers.contains(Arrays.asList(
					warningMatchersBuilder.line(20).build(),
					warningMatchersBuilder.line(32).build())));
	}

	public void testWriteOutOfOrder() throws Exception {
		lintProject(compile(file("WriteOutOfOrder.java.txt=>src/WriteOutOfOrder.java")));

		assertThat("Failed to check writing order",
				getWarnings(), Matchers.contains(new WarningMatcherBuilder()
					.fileName("WriteOutOfOrder.java")
					.line(32)
					.message(MISSING_OR_OUT_OF_ERROR_MESSAGE)
					.build()));
	}

	public void testReadIncompatibleTypes() throws Exception {
		lintProject(compile(file("ReadIncompatibleTypes.java.txt=>src/ReadIncompatibleTypes.java")));

		assertThat("Failed to retrieve types of the reading variables",
				getWarnings(), Matchers.contains(new WarningMatcherBuilder()
					.fileName("ReadIncompatibleTypes.java")
					.line(20)
					.message("Incompatible types: readString - writeInt")
					.build()));
	}

	public void testCorrectParcelableClass() throws Exception {
		lintProject(compile(file("CorrectParcelableClass.java.txt=>src/CorrectParcelableClass.java",
				"SuperClass.java.txt=>src/SuperClass.java")));

		assertTrue("There are some warnings", getWarnings().isEmpty());
	}
}

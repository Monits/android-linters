/**
 *  Copyright 2010 - 2015 - Monits
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

import static com.monits.linters.InstanceStateDetector.ALREADY_SAVED;
import static com.monits.linters.InstanceStateDetector.FIELD_ALREADY_RESTORED;
import static com.monits.linters.InstanceStateDetector.FIELD_ALREADY_SAVED;
import static com.monits.linters.InstanceStateDetector.RESTORED_BUT_NEVER_SAVED;
import static com.monits.linters.InstanceStateDetector.RESTORED_WITH_DIFERENT_TYPES;
import static com.monits.linters.InstanceStateDetector.SAVED_BUT_NEVER_RESTORED;
import static com.monits.linters.InstanceStateDetector.SAVED_WITH_DIFERENT_TYPES;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.monits.linters.matchers.WarningMatcherBuilder;

public class InstanceStateDetectorTest extends AbstractTestCase {

	@Override
	protected Detector getDetector() {
		return new InstanceStateDetector();
	}

	@Override
	@Nonnull
	protected List<Issue> getIssues() {
		return Arrays.asList(InstanceStateDetector.MISSING_SAVED_INSTANCE_STATES);
	}

	public void testActivityWithStatesSavedAndRestored() throws Exception {
		lintProject(
			compile(
				file("instanceState/ActivityWithStatesSavedAndRestored.java.txt=>"
						+ "src/ActivityWithStatesSavedAndRestored.java")
				));

		assertTrue("There are unexpected warnings when checks saved and restored states in an Activity",
				getWarnings().isEmpty());
	}

	public void testActivityWithInstanceStatesSavedButNotRestored() throws Exception {
		lintProject(
			compile(
				file("instanceState/ActivityWithInstanceStatesSavedButNotRestored.java.txt=>"
						+ "src/ActivityWithInstanceStatesSavedButNotRestored.java")
				));

		final WarningMatcherBuilder matcher =
				new WarningMatcherBuilder().fileName("ActivityWithInstanceStatesSavedButNotRestored.java");

		assertThat("Failed to check missing keys to restore in an Activity", getWarnings(),
			Matchers.contains(
					matcher.line(14).message(String.format(SAVED_BUT_NEVER_RESTORED, "KEY_STRING_ARRAY")).build(),
					matcher.line(15).message(String.format(SAVED_BUT_NEVER_RESTORED, "KEY_CHAR")).build()
					));
	}

	public void testActivityWithInstanceStatesRestoredButNeverSaved() throws Exception {
		lintProject(
			compile(
				file("instanceState/ActivityWithInstanceStatesRestoredButNeverSaved.java.txt=>"
						+ "src/ActivityWithInstanceStatesRestoredButNeverSaved.java")
				));

		final WarningMatcherBuilder matcher =
				new WarningMatcherBuilder().fileName("ActivityWithInstanceStatesRestoredButNeverSaved.java");

		assertThat("Failed to check missing keys to save in an Activity", getWarnings(),
			Matchers.contains(
					matcher.line(16).message(String.format(RESTORED_BUT_NEVER_SAVED, "KEY_STRING_ARRAY")).build(),
					matcher.line(17).message(String.format(RESTORED_BUT_NEVER_SAVED, "KEY_CHAR")).build()
					));
	}

	public void testActivityWithStatesSavedAndRestoredInDifferentMethods() throws Exception {
		lintProject(
			compile(
				file("instanceState/ActivityWithStatesSavedAndRestoredInDifferentMethods.java.txt=>"
						+ "src/ActivityWithStatesSavedAndRestoredInDifferentMethods.java")
				));

		assertTrue("There are unexpected warnings when checks restored states in different methods in an Activity",
				getWarnings().isEmpty());
	}

	public void testFragmentWithInstancesStatesSavedAndRestored() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesSavedAndRestored.java.txt=>"
						+ "src/FragmentWithInstancesStatesSavedAndRestored.java")
				));

		assertTrue("There are unexpected warnings when checks saved and restored states in a Fragment",
				getWarnings().isEmpty());
	}

	public void testFragmentWithInstancesStatesSavedButNotRestored() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesSavedButNotRestored.java.txt=>"
						+ "src/FragmentWithInstancesStatesSavedButNotRestored.java")
				));

		assertThat("Failed to check missing keys to restore in a Fragment", getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("FragmentWithInstancesStatesSavedButNotRestored.java")
				.line(13)
				.message(String.format(SAVED_BUT_NEVER_RESTORED, "KEY_LOGIN_CLIENT"))
				.build()
				));
	}

	public void testFragmentWithInstancesStatesSavedAndRestoredInDifferentMethods() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java.txt=>"
						+ "src/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java")
				));

		assertTrue("There are unexpected warnings when checks restored states in different methods in a Fragment",
				getWarnings().isEmpty());
	}

	public void testFragmentWithInstancesStatesRestoredButNeverSaved() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesRestoredButNeverSaved.java.txt=>"
						+ "src/FragmentWithInstancesStatesRestoredButNeverSaved.java")
				));

		assertThat("Failed to check missing keys to save in a Fragment", getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("FragmentWithInstancesStatesRestoredButNeverSaved.java")
				.line(15)
				.message(String.format(RESTORED_BUT_NEVER_SAVED, "KEY_LOGIN_CLIENT"))
				.build()
				));
	}

	public void testOverwritingInstanceStatesWhenSave() throws Exception {
		lintProject(
			compile(
				file("instanceState/OverwritingInstanceStatesWhenSave.java.txt=>"
						+ "src/OverwritingInstanceStatesWhenSave.java")
				));

		assertThat("Failed to check overwritten instance states when trying to save to savedInstanceState",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("OverwritingInstanceStatesWhenSave.java")
				.line(24)
				.message(String.format(ALREADY_SAVED, "KEY_DOUBLE"))
				.build()
				));
	}

	public void testRestoreInSameVariableWithDifferentKeys() throws Exception {
		lintProject(
			compile(
				file("instanceState/RestoreInSameVariableWithDifferentKeys.java.txt=>"
						+ "src/RestoreInSameVariableWithDifferentKeys.java")
				));

		assertThat("Failed to check a restored variable with different keys",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("RestoreInSameVariableWithDifferentKeys.java")
				.line(20)
				.message(String.format(FIELD_ALREADY_RESTORED, "number"))
				.build()
				));
	}

	public void testSaveSameVariableWithDifferentKey() throws Exception {
		lintProject(
			compile(
				file("instanceState/SaveSameVariableWithDifferentKey.java.txt=>"
						+ "src/SaveSameVariableWithDifferentKey.java")
				));

		assertThat("Failed to check a saved variable with different keys",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("SaveSameVariableWithDifferentKey.java")
				.line(28)
				.message(String.format(FIELD_ALREADY_SAVED, "number"))
				.build()
				));
	}

	public void testSaveVariableWithDifferentTypes() throws Exception {
		lintProject(
			compile(
				file("instanceState/SaveVariableWithDifferentTypes.java.txt=>"
						+ "src/SaveVariableWithDifferentTypes.java")
				));

		assertThat("Failed to check different types when save a variable",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("SaveVariableWithDifferentTypes.java")
				.line(21)
				.message(String.format(SAVED_WITH_DIFERENT_TYPES, "putDouble", "D", "intNumber", "I"))
				.build()
				));
	}

	public void testRestoreVariableWithDifferentTypes() throws Exception {
		lintProject(
			compile(
				file("instanceState/RestoreVariableWithDifferentTypes.java.txt=>"
						+ "src/RestoreVariableWithDifferentTypes.java")
				));

		assertThat("Failed to check different types when restore a variable",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("RestoreVariableWithDifferentTypes.java")
				.line(15)
				.message(String.format(RESTORED_WITH_DIFERENT_TYPES, "getChar", "C", "number", "I"))
				.build()
				));
	}
}
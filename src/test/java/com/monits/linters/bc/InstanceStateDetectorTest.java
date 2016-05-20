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
package com.monits.linters.bc;

import static com.monits.linters.bc.InstanceStateDetector.ALREADY_SAVED;
import static com.monits.linters.bc.InstanceStateDetector.FIELD_ALREADY_RESTORED;
import static com.monits.linters.bc.InstanceStateDetector.FIELD_ALREADY_SAVED;
import static com.monits.linters.bc.InstanceStateDetector.NON_CONSTANT_KEY;
import static com.monits.linters.bc.InstanceStateDetector.RESTORED_BUT_NEVER_SAVED;
import static com.monits.linters.bc.InstanceStateDetector.RESTORED_WITH_DIFERENT_TYPES;
import static com.monits.linters.bc.InstanceStateDetector.SAVED_BUT_NEVER_RESTORED;
import static com.monits.linters.bc.InstanceStateDetector.SAVED_WITH_DIFERENT_TYPES;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.monits.linters.bc.InstanceStateDetector;
import com.monits.linters.test.AbstractTestCase;
import com.monits.linters.test.matchers.WarningMatcherBuilder;

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
				file("instanceState/R.java.txt=>src/R.java",
						"instanceState/ActivityWithStatesSavedAndRestored.java.txt=>"
						+ "src/ActivityWithStatesSavedAndRestored.java")
				));

		assertThat("There are unexpected warnings when checks saved and restored states in an Activity",
				getWarnings(), empty());
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

		assertThat("There are unexpected warnings when checks restored states in different methods in an Activity",
				getWarnings(), empty());
	}

	public void testFragmentWithInstancesStatesSavedAndRestored() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesSavedAndRestored.java.txt=>"
						+ "src/FragmentWithInstancesStatesSavedAndRestored.java")
				));

		assertThat("There are unexpected warnings when checks saved and restored states in a Fragment",
				getWarnings(), empty());
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
	
	public void testNonConstantKeys() throws Exception {
		lintProject(
			compile(
				file("instanceState/NonConstantKeys.java.txt=>"
						+ "src/NonConstantKeys.java")
				));

		assertThat("Non constant keys used but not reported on save", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NonConstantKeys.java")
				.line(17)
				.message(NON_CONSTANT_KEY)
				.build()
				));
		
		assertThat("Non constant keys used but not reported on restore", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NonConstantKeys.java")
				.line(23)
				.message(NON_CONSTANT_KEY)
				.build()
				));
	}

	public void testFragmentWithInstancesStatesSavedAndRestoredInDifferentMethods() throws Exception {
		lintProject(
			compile(
				file("instanceState/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java.txt=>"
						+ "src/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java")
				));

		assertThat("There are unexpected warnings when checks restored states in different methods in a Fragment",
				getWarnings(), empty());
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
				.line(18)
				.message(String.format(RESTORED_WITH_DIFERENT_TYPES, "getChar", "C", "number", "I"))
				.build()
				));
	}

	public void testSaveAndRestoreLocalInstanceStates() throws Exception {
		lintProject(
				compile(file("instanceState/SaveAndRestoreLocalInstanceStates.java.txt=>"
						+ "src/SaveAndRestoreLocalInstanceStates.java")));

		assertThat("There are unexpected warnings when checking local saved/restored states",
				getWarnings(), empty());
	}

	public void testCFGAnalysis() throws Exception {
		lintProject(
				compile(file("instanceState/CFGAnalysis.java.txt=>"
						+ "src/CFGAnalysis.java")));

		assertThat("Failed to check a save or restore state while doing a control flow scan",
				getWarnings(),
				Matchers.contains(new WarningMatcherBuilder()
					.fileName("CFGAnalysis.java")
					.line(46)
					.message(String.format(SAVED_BUT_NEVER_RESTORED, "PENDING_ATTACHMENTS"))
					.build()
				));
	}

	public void testRestoreFromExtras() throws Exception {
		lintProject(
				compile(file("instanceState/R.java.txt=>"
						+ "src/R.java",
						"instanceState/RestoreFromExtras.java.txt=>"
						+ "src/RestoreFromExtras.java")));

		assertThat("There are unexpected warnings when checking data from extras",
				getWarnings(), empty());
	}

	public void testFragmentWithLocalStates() throws Exception {
		lintProject(
				compile(file("instanceState/FragmentWithLocalStates.java.txt=>"
						+ "src/FragmentWithLocalStates.java")));

		assertThat("There are unexpected warning when check local states in the fragment",
				getWarnings(), empty());
	}

	public void testRestoreStateLocallyAndThenInAField() throws Exception {
		lintProject(
				compile(file("instanceState/RestoreStateLocallyAndThenInAField.java.txt=>"
						+ "src/RestoreStateLocallyAndThenInAField.java")));

		assertThat("There are unexpected warning when restore a state locally and then in a field",
				getWarnings(), empty());
	}

	public void testSaveRestoreLocallyStatesInAListFragment() throws Exception {
		lintProject(
			compile(
				file("instanceState/SaveRestoreLocallyStatesInAListFragment.java.txt=>"
						+ "src/SaveRestoreLocallyStatesInAListFragment.java")
				));

		assertThat("There are unexpected warnings when save and restore states locally in a ListFragment",
				getWarnings(), empty());
	}

	public void testRestoreLocalVariableInField() throws Exception {
		lintProject(
			compile(
				file("instanceState/RestoreLocalVariableInField.java.txt=>"
						+ "src/RestoreLocalVariableInField.java")
				));

		assertThat("There are unexpected warnings when restore a state locally and then in a Field",
				getWarnings(), empty());
	}

	public void testIgnoreMissingSaveInstanceStates() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreMissingSaveInstanceStates.java.txt=>"
						+ "src/TestIgnoreMissingSaveInstanceStates.java")
				));

		assertThat("Failed while trying to ignore a missing saved state",
			getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
				.fileName("TestIgnoreMissingSaveInstanceStates.java")
				.line(26)
				.message(String.format(RESTORED_BUT_NEVER_SAVED, "KEY_CHAR"))
				.build()
			)));
	}

	public void testIgnoreMissingRestoreInstanceStates() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreMissingRestoreInstanceStates.java.txt=>"
						+ "src/TestIgnoreMissingRestoreInstanceStates.java")
				));

		assertThat("There are unexpected warnings when ignore a missing restored state",
				getWarnings(), empty());
	}

	public void testIgnoreMissingSaveInstanceStateWithAuxiliarMethod() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreMissingSaveInstanceStateWithAuxiliarMethod.java.txt=>"
						+ "src/TestIgnoreMissingSaveInstanceStateWithAuxiliarMethod.java")
				));

		assertThat("Failed while trying to ignore a missing saved state when restore states with auxiliar methods",
			getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
				.fileName("TestIgnoreMissingSaveInstanceStateWithAuxiliarMethod.java")
				.line(36)
				.message(String.format(RESTORED_BUT_NEVER_SAVED, "QuestionHistoryMessages"))
				.build()
			)));
	}

	public void testIgnoreOverwritingSaveInstanceState() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreOverwritingSaveInstanceState.java.txt=>"
						+ "src/TestIgnoreOverwritingSaveInstanceState.java")
				));

		assertThat("There are unexpected warnings when trying to ignore a state that is being overwritten",
				getWarnings(), empty());
	}

	public void testIgnoreRestoredASavedStateInSameField() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreRestoredASavedStateInSameField.java.txt=>"
						+ "src/TestIgnoreRestoredASavedStateInSameField.java")
				));

		assertThat("Failed while trying to ignore a field that is being overwritten",
			getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
				.fileName("TestIgnoreRestoredASavedStateInSameField.java")
				.line(19)
				.message(String.format(FIELD_ALREADY_RESTORED, "number"))
				.build()
			)));
	}

	public void testIgnoreSaveAStateWithDifferentType() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreSaveAStateWithDifferentType.java.txt=>"
						+ "src/TestIgnoreSaveAStateWithDifferentType.java")
				));

		assertThat("There are unexpected warnings while trying to ignore an invalid type check"
				+ " when a state is begin saved", getWarnings(), empty());
	}

	public void testIgnoreRestoreAStateWithDifferentType() throws Exception {
		lintProject(
			compile(
				file("instanceState/TestIgnoreRestoreAStateWithDifferentType.java.txt=>"
						+ "src/TestIgnoreRestoreAStateWithDifferentType.java")
				));

		assertThat("Failed while trying to ignore an invalid type check when a state is begin restored",
			getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
				.fileName("TestIgnoreRestoreAStateWithDifferentType.java")
				.line(28)
				.message(String.format(RESTORED_WITH_DIFERENT_TYPES, "getInt", "I", "doubleValue", "D"))
				.build()
			)));
	}
}
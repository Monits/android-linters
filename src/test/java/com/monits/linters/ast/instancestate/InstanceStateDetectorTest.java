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
package com.monits.linters.ast.instancestate;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.google.common.collect.ImmutableList;
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
		return ImmutableList.<Issue>of(
				InstanceStateDetector.DIFFERENT_FIELDS,
				InstanceStateDetector.INVALID_TYPE,
				InstanceStateDetector.KEY_IS_NOT_CONSTANT,
				InstanceStateDetector.RESTORED_BUT_NEVER_SAVED,
				InstanceStateDetector.SAVED_BUT_NEVER_RESTORED,
				InstanceStateDetector.STATE_ALREADY_RESTORED,
				InstanceStateDetector.STATE_ALREADY_SAVED
		);
	}
	
	@Override
	protected boolean allowCompilationErrors() {
		return true;
	}

	public void testActivityWithStatesSavedAndRestored() throws Exception {
		lintProject(
			file("instanceState/R.java.txt=>src/R.java",
					"instanceState/ActivityWithStatesSavedAndRestored.java.txt=>"
					+ "src/ActivityWithStatesSavedAndRestored.java")
			);

		assertThat("There are unexpected warnings when checks saved and restored states in an Activity",
				getWarnings(), empty());
	}

	@SuppressWarnings("unchecked")
	public void testActivityWithInstanceStatesSavedButNotRestored() throws Exception {
		lintProject(
			file("instanceState/ActivityWithInstanceStatesSavedButNotRestored.java.txt=>"
					+ "src/ActivityWithInstanceStatesSavedButNotRestored.java")
			);

		final WarningMatcherBuilder matcher =
				new WarningMatcherBuilder().fileName("ActivityWithInstanceStatesSavedButNotRestored.java");
		
		assertThat("Failed to check missing keys to restore in an Activity", getWarnings(),
			Matchers.contains(
				matcher.line(14).message(String.format(InstanceStateDetector.SAVED_BUT_NEVER_RESTORED_MSG, "KEY_STRING_ARRAY")).build(),
				matcher.line(15).message(String.format(InstanceStateDetector.SAVED_BUT_NEVER_RESTORED_MSG, "KEY_CHAR")).build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testActivityWithInstanceStatesRestoredButNeverSaved() throws Exception {
		lintProject(
			file("instanceState/ActivityWithInstanceStatesRestoredButNeverSaved.java.txt=>"
					+ "src/ActivityWithInstanceStatesRestoredButNeverSaved.java")
			);

		final WarningMatcherBuilder matcher =
				new WarningMatcherBuilder().fileName("ActivityWithInstanceStatesRestoredButNeverSaved.java");

		assertThat("Failed to check missing keys to save in an Activity", getWarnings(),
			Matchers.contains(
				matcher.line(16).message(String.format(InstanceStateDetector.RESTORED_BUT_NEVER_SAVED_MSG, "KEY_STRING_ARRAY")).build(),
				matcher.line(17).message(String.format(InstanceStateDetector.RESTORED_BUT_NEVER_SAVED_MSG, "KEY_CHAR")).build()
			));
	}

	public void testActivityWithStatesSavedAndRestoredInDifferentMethods() throws Exception {
		lintProject(
			file("instanceState/ActivityWithStatesSavedAndRestoredInDifferentMethods.java.txt=>"
					+ "src/ActivityWithStatesSavedAndRestoredInDifferentMethods.java")
			);

		assertThat("There are unexpected warnings when checks restored states in different methods in an Activity",
				getWarnings(), empty());
	}

	public void testFragmentWithInstancesStatesSavedAndRestored() throws Exception {
		lintProject(
			file("instanceState/FragmentWithInstancesStatesSavedAndRestored.java.txt=>"
					+ "src/FragmentWithInstancesStatesSavedAndRestored.java")
			);

		assertThat("There are unexpected warnings when checks saved and restored states in a Fragment",
				getWarnings(), empty());
	}

	public void testFragmentWithInstancesStatesSavedButNotRestored() throws Exception {
		lintProject(
			file("instanceState/FragmentWithInstancesStatesSavedButNotRestored.java.txt=>"
					+ "src/FragmentWithInstancesStatesSavedButNotRestored.java")
			);

		assertThat("Failed to check missing keys to restore in a Fragment", getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("FragmentWithInstancesStatesSavedButNotRestored.java")
				.line(13)
				.message(String.format(InstanceStateDetector.SAVED_BUT_NEVER_RESTORED_MSG, "KEY_LOGIN_CLIENT"))
				.build()
			));
	}
	
	public void testNonConstantKeys() throws Exception {
		lintProject(
			file("instanceState/NonConstantKeys.java.txt=>"
					+ "src/NonConstantKeys.java")
			);
		
		// Line numbers are skewed...
		assertThat("Non constant keys used directly not reported", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NonConstantKeys.java")
				.line(12)
				.message(String.format(InstanceStateDetector.KEY_IS_NOT_CONSTANT_MSG, "KEY_STRING"))
				.build()
			));
		
		assertThat("Non constant keys used indirectly not reported", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NonConstantKeys.java")
				.line(8)
				.message(String.format(InstanceStateDetector.KEY_IS_NOT_CONSTANT_MSG, "KEY_PREFIX"))
				.build()
			));
	}

	public void testFragmentWithInstancesStatesSavedAndRestoredInDifferentMethods() throws Exception {
		lintProject(
			file("instanceState/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java.txt=>"
					+ "src/FragmentWithInstancesStatesSavedAndRestoredInDifferentMethods.java")
			);

		assertThat("There are unexpected warnings when checks restored states in different methods in a Fragment",
				getWarnings(), empty());
	}

	public void testFragmentWithInstancesStatesRestoredButNeverSaved() throws Exception {
		lintProject(
			file("instanceState/FragmentWithInstancesStatesRestoredButNeverSaved.java.txt=>"
					+ "src/FragmentWithInstancesStatesRestoredButNeverSaved.java")
			);

		assertThat("Failed to check missing keys to save in a Fragment", getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
				.fileName("FragmentWithInstancesStatesRestoredButNeverSaved.java")
				.line(15)
				.message(String.format(InstanceStateDetector.RESTORED_BUT_NEVER_SAVED_MSG, "KEY_LOGIN_CLIENT"))
				.build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testOverwritingInstanceStatesWhenSave() throws Exception {
		lintProject(
			file("instanceState/OverwritingInstanceStatesWhenSave.java.txt=>"
					+ "src/OverwritingInstanceStatesWhenSave.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("OverwritingInstanceStatesWhenSave.java")
			.message(String.format(InstanceStateDetector.ALREADY_SAVED_KEY, "KEY_DOUBLE"));
		
		assertThat("Failed to check overwritten instance states when trying to save to savedInstanceState",
			getWarnings(),
			Matchers.contains(
				matcherBuilder.line(23).build(),
				matcherBuilder.line(24).build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testRestoreInSameVariableWithDifferentKeys() throws Exception {
		lintProject(
			file("instanceState/RestoreInSameVariableWithDifferentKeys.java.txt=>"
					+ "src/RestoreInSameVariableWithDifferentKeys.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("RestoreInSameVariableWithDifferentKeys.java")
			.message(String.format(InstanceStateDetector.DIFFERENT_FIELDS_MSG, "KEY_DOUBLE_2"));
		
		assertThat("Failed to check a restored variable with different keys",
			getWarnings(),
			Matchers.contains(
				matcherBuilder.line(20).build(),
				matcherBuilder.line(27).build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testSaveSameVariableWithDifferentKey() throws Exception {
		lintProject(
			file("instanceState/SaveSameVariableWithDifferentKey.java.txt=>"
					+ "src/SaveSameVariableWithDifferentKey.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("SaveSameVariableWithDifferentKey.java")
			.message(String.format(InstanceStateDetector.DIFFERENT_FIELDS_MSG, "KEY_DOUBLE_3"));
		
		assertThat("Failed to check a restored variable with different keys",
			getWarnings(),
			Matchers.contains(
				matcherBuilder.line(20).build(),
				matcherBuilder.line(28).build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testSaveVariableWithDifferentTypes() throws Exception {
		lintProject(
			file("instanceState/SaveVariableWithDifferentTypes.java.txt=>"
					+ "src/SaveVariableWithDifferentTypes.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("SaveVariableWithDifferentTypes.java")
			.message(String.format(InstanceStateDetector.INVALID_TYPE_MSG, "KEY_INT", "Int", "Double"));
		
		assertThat("Failed to check different types when save a variable",
			getWarnings(),
			Matchers.contains(
				matcherBuilder.line(15).build(),
				matcherBuilder.line(21).build()
			));
	}

	@SuppressWarnings("unchecked")
	public void testRestoreVariableWithDifferentTypes() throws Exception {
		lintProject(
			file("instanceState/RestoreVariableWithDifferentTypes.java.txt=>"
					+ "src/RestoreVariableWithDifferentTypes.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("RestoreVariableWithDifferentTypes.java")
			.message(String.format(InstanceStateDetector.DIFFERENT_FIELDS_MSG, "KEY_CHAR"));
		
		assertThat("Failed to check different types when restore a variable",
			getWarnings(),
			Matchers.contains(
				matcherBuilder.line(18).build(),
				matcherBuilder.line(27).build()
			));
	}

	public void testSaveAndRestoreLocalInstanceStates() throws Exception {
		lintProject(
			file("instanceState/SaveAndRestoreLocalInstanceStates.java.txt=>"
						+ "src/SaveAndRestoreLocalInstanceStates.java"));

		assertThat("There are unexpected warnings when checking local saved/restored states",
				getWarnings(), empty());
	}

	@SuppressWarnings("unchecked")
	public void testCFGAnalysis() throws Exception {
		lintProject(
			file("instanceState/CFGAnalysis.java.txt=>"
						+ "src/CFGAnalysis.java"));

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("CFGAnalysis.java")
			.message(String.format(InstanceStateDetector.DIFFERENT_FIELDS_MSG, "PENDING_ATTACHMENTS"));

		assertThat("Failed to check a save or restore state while doing a control flow scan", getWarnings(),
			Matchers.contains(
				matcherBuilder.line(46).build(),
				matcherBuilder.line(72).build()
			));
	}

	public void testRestoreFromExtras() throws Exception {
		lintProject(
			file("instanceState/R.java.txt=>"
					+ "src/R.java",
					"instanceState/RestoreFromExtras.java.txt=>"
					+ "src/RestoreFromExtras.java"));

		assertThat("There are unexpected warnings when checking data from extras",
				getWarnings(), empty());
	}

	public void testFragmentWithLocalStates() throws Exception {
		lintProject(
			file("instanceState/FragmentWithLocalStates.java.txt=>"
					+ "src/FragmentWithLocalStates.java"));

		assertThat("There are unexpected warning when check local states in the fragment",
				getWarnings(), empty());
	}

	public void testSaveRestoreLocallyStatesInAListFragment() throws Exception {
		lintProject(
			file("instanceState/SaveRestoreLocallyStatesInAListFragment.java.txt=>"
					+ "src/SaveRestoreLocallyStatesInAListFragment.java")
			);

		assertThat("There are unexpected warnings when save and restore states locally in a ListFragment",
				getWarnings(), empty());
	}

	public void testRestoreLocalVariableInField() throws Exception {
		lintProject(
			file("instanceState/RestoreLocalVariableInField.java.txt=>"
					+ "src/RestoreLocalVariableInField.java")
			);

		assertThat("There are unexpected warnings when restore a state locally and then in a Field",
				getWarnings(), empty());
	}
	
	@SuppressWarnings("unchecked")
	public void testDoubleRestore() throws Exception {
		lintProject(
			file("instanceState/DoubleRestore.java.txt=>"
					+ "src/DoubleRestore.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("DoubleRestore.java")
			.message(String.format(InstanceStateDetector.ALREADY_RESTORED_KEY, "KEY_DOUBLE"));
		
		assertThat("Failed to detect a double restore", getWarnings(),
			Matchers.contains(
				matcherBuilder.line(15).build(),
				matcherBuilder.line(16).build()
			));
	}
	
	@SuppressWarnings("unchecked")
	public void testDoubleSave() throws Exception {
		lintProject(
			file("instanceState/DoubleSave.java.txt=>"
					+ "src/DoubleSave.java")
			);

		final WarningMatcherBuilder matcherBuilder = new WarningMatcherBuilder()
			.fileName("DoubleSave.java")
			.message(String.format(InstanceStateDetector.ALREADY_SAVED_KEY, "KEY_DOUBLE"));
		
		assertThat("Failed to detect a double save", getWarnings(),
			Matchers.contains(
				matcherBuilder.line(21).build(),
				matcherBuilder.line(22).build()
			));
	}
}
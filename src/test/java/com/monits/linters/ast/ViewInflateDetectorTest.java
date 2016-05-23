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
package com.monits.linters.ast;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.google.common.base.Joiner;
import com.monits.linters.test.AbstractTestCase;
import com.monits.linters.test.matchers.WarningMatcherBuilder;

public class ViewInflateDetectorTest extends AbstractTestCase {

	@Override
	protected Detector getDetector() {
		return new ViewInflateDetector();
	}

	@Override
	@Nonnull
	protected List<Issue> getIssues() {
		return Arrays.asList(ViewInflateDetector.VIEW_INFLATE_IGNORES_THEME);
	}
	
	@Override
	protected boolean allowCompilationErrors() {
		// We don't really care, since we are using the AST 
		return true;
	}

	public void testViewInflateIsReported() throws Exception {
		lintProject(
			androidView(),
			java("src/ViewInflate.java",
				Joiner.on('\n').join(
						"import android.view.View;",
						"public class ViewInflate {",
						"	public void myMethod() {",
						"		View.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflate.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testViewInflateSamePackageIsReported() throws Exception {
		lintProject(
			androidView(),
			java("src/android/view/ViewInflate.java",
				Joiner.on('\n').join(
						"package android.view;",
						"public class ViewInflate {",
						"	public void myMethod() {",
						"		View.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflate.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testViewInflateStarImportIsReported() throws Exception {
		lintProject(
			androidView(),
			java("src/ViewInflate.java",
				Joiner.on('\n').join(
						"import android.view.*;",
						"public class ViewInflate {",
						"	public void myMethod() {",
						"		View.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate using star import", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflate.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testViewInflateFQCNIsReported() throws Exception {
		lintProject(
			androidView(),
			java("src/ViewInflateFQCN.java",
				Joiner.on('\n').join(
						"public class ViewInflateFQCN {",
						"	public void myMethod() {",
						"		android.view.View.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate using FQCN", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflateFQCN.java")
				.line(3)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}

	public void testViewInflateStaticImportIsReported() throws Exception {
		lintProject(
			java("src/ViewInflateStaticImport.java",
				Joiner.on('\n').join(
						"import static android.view.View.inflate;",
						"public class ViewInflateStaticImport {",
						"	public void myMethod() {",
						"		inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate using static import", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflateStaticImport.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testViewInflateIsReportedWithoutOperand() throws Exception {
		lintProject(
			androidLinearLayout(),
			androidViewGroup(),
			androidView(),
			java("src/ViewInflate.java",
				Joiner.on('\n').join(
						"import android.widget.LinearLayout;",
						"public class ViewInflate extends LinearLayout {",
						"	public void myMethod() {",
						"		inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to View.inflate", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflate.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testLinearLayoutInflateIsReported() throws Exception {
		lintProject(
			androidLinearLayout(),
			androidViewGroup(),
			androidView(),
			java("src/ViewInflate.java",
				Joiner.on('\n').join(
						"import android.widget.LinearLayout;",
						"public class ViewInflate {",
						"	public void myMethod() {",
						"		LinearLayout.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect call to LinearLayout.inflate", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("ViewInflate.java")
				.line(4)
				.message(ViewInflateDetector.VIEW_INFLATE_MSG)
				.build()));
	}
	
	public void testViewInflateNotReportedOnLollipop() throws Exception {
		lintProject(
			manifest().minSdk(21),
			java("src/ViewInflate.java",
				Joiner.on('\n').join(
						"import android.view.View;",
						"public class ViewInflate {",
						"	public void myMethod() {",
						"		View.inflate(null, 1, null);",
						"	}",
						"}"
					)
				));

		assertThat("View.inflate detected even when targeting lollpop", getWarnings(),
			Matchers.empty());
	}
	
	private TestFile androidView() {
		return java("src/android/view/View.java",
			Joiner.on('\n').join(
					"package android.view;",
					"public class View {",
					"}"
				)
			);
	}
	
	private TestFile androidLinearLayout() {
		return java("src/android/widget/LinearLayout.java",
			Joiner.on('\n').join(
					"package android.widget;",
					"import android.view.ViewGroup;",
					"public class LinearLayout extends ViewGroup {",
					"}"
				)
			);
	}
	
	private TestFile androidViewGroup() {
		return java("src/android/view/ViewGroup.java",
			Joiner.on('\n').join(
					"package android.view;",
					"import android.view.View;",
					"public class ViewGroup extends View {",
					"}"
				)
			);
	}
}

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
import com.monits.linters.AbstractTestCase;
import com.monits.linters.matchers.WarningMatcherBuilder;


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
	
	public void testViewInflateFQCNIsReported() throws Exception {
		lintProject(
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
}
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


public class NeedlessNullnessAnnotationDetectorTest extends AbstractTestCase {

	@Override
	protected Detector getDetector() {
		return new NeedlessNullnessAnnotationDetector();
	}

	@Override
	@Nonnull
	protected List<Issue> getIssues() {
		return Arrays.asList(NeedlessNullnessAnnotationDetector.NEEDLESS_NULLNESS_ANNOTATION);
	}
	
	@Override
	protected boolean allowCompilationErrors() {
		// We don't really care, since we are using the AST 
		return true;
	}

	public void testPrimitiveParameterIsReported() throws Exception {
		lintProject(
			java("src/NeedlessNullable.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class NeedlessNullable {",
						"	public char myMethod(@Nullable int arg, @NonNull char arg2) {",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect needless @Nullable on primitive parameter", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullable.java")
				.line(4)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_VAR_MSG, "arg"))
				.build()));
		
		assertThat("Failed to detect needless @NonNull on primitive parameter", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullable.java")
				.line(4)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_VAR_MSG, "arg2"))
				.build()));
	}
	
	public void testFQCNPrimitiveParameterIsReported() throws Exception {
		lintProject(
			java("src/NeedlessNullableFQCN.java",
				Joiner.on('\n').join(
						"public class NeedlessNullableFQCN {",
						"	public char myMethod(@com.android.annotations.Nullable int arg,",
						"						@com.android.annotations.NonNull char arg2) {",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect needless @Nullable on primitive parameter using FQCN", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableFQCN.java")
				.line(2)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_VAR_MSG, "arg"))
				.build()));
		
		assertThat("Failed to detect needless @NonNull on primitive parameter using FQCN", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableFQCN.java")
				.line(3)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_VAR_MSG, "arg2"))
				.build()));
	}
	
	public void testNonPrimitiveParameterIsNotReported() throws Exception {
		lintProject(
			java("src/NeededNullable.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class NeedlessNullable {",
						"	public char myMethod(@Nullable Object arg, @NonNull Integer arg2) {",
						"	}",
						"}"
					)
				));

		assertThat("Non primitives should not be reported", getWarnings(), Matchers.empty());
	}
	
	public void testPrimitiveArrayParameterIsNotReported() throws Exception {
		lintProject(
			java("src/ArrayNullable.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class ArrayNullable {",
						"	public char myMethod(@Nullable int[] arg, @NonNull char[][] arg2) {",
						"	}",
						"}"
					)
				));

		assertThat("Arrays of primitives should not be reported", getWarnings(), Matchers.empty());
	}

	public void testPrimitiveReturnIsReported() throws Exception {
		lintProject(
			java("src/NeedlessNullableReturn.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class NeedlessNullableReturn {",
						"	@Nullable",
						"	public char myMethod() {",
						"	}",
						"",
						"	@NonNull",
						"	public char myOtherMethod() {",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect needless @Nullable on primitive return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableReturn.java")
				.line(4)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_RETURN_TYPE_MSG, "myMethod"))
				.build()));

		assertThat("Failed to detect needless @NonNull on primitive return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableReturn.java")
				.line(8)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_RETURN_TYPE_MSG, "myOtherMethod"))
				.build()));
	}
	
	public void testFQCNPrimitiveReturnIsReported() throws Exception {
		lintProject(
			java("src/NeedlessNullableReturnFQCN.java",
				Joiner.on('\n').join(
						"public class NeedlessNullableReturnFQCN {",
						"	@com.android.annotations.Nullable",
						"	public char myMethod() {",
						"	}",
						"",
						"	@com.android.annotations.NonNull",
						"	public char myOtherMethod() {",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect needless @Nullable on primitive return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableReturnFQCN.java")
				.line(2)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_RETURN_TYPE_MSG, "myMethod"))
				.build()));

		assertThat("Failed to detect needless @NonNull on primitive return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("NeedlessNullableReturnFQCN.java")
				.line(6)
				.message(String.format(NeedlessNullnessAnnotationDetector.PRIMITIVE_RETURN_TYPE_MSG, "myOtherMethod"))
				.build()));
	}
	
	public void testVoidReturnIsReported() throws Exception {
		lintProject(
			java("src/VoidReturn.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class VoidReturn {",
						"	@Nullable",
						"	public void myMethod() {",
						"	}",
						"",
						"	@NonNull",
						"	public void myOtherMethod() {",
						"	}",
						"}"
					)
				));

		assertThat("Failed to detect needless @Nullable on void return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("VoidReturn.java")
				.line(4)
				.message(String.format(NeedlessNullnessAnnotationDetector.VOID_RETURN_TYPE_MSG, "myMethod"))
				.build()));

		assertThat("Failed to detect needless @NonNull on void return type", getWarnings(),
			Matchers.hasItem(new WarningMatcherBuilder()
				.fileName("VoidReturn.java")
				.line(8)
				.message(String.format(NeedlessNullnessAnnotationDetector.VOID_RETURN_TYPE_MSG, "myOtherMethod"))
				.build()));
	}
	
	public void testNonPrimitiveReturnIsNotReported() throws Exception {
		lintProject(
			java("src/NeededNullableReturn.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class NeededNullableReturn {",
						"	@Nullable",
						"	public Object myMethod() {",
						"	}",
						"",
						"	@NonNull",
						"	public Integer myOtherMethod() {",
						"	}",
						"}"
					)
				));

		assertThat("Non primitive return types should not be reported", getWarnings(), Matchers.empty());
	}
	
	public void testPrimitiveArrayReturnIsNotReported() throws Exception {
		lintProject(
			java("src/NeededNullableReturn.java",
				Joiner.on('\n').join(
						"import com.android.annotations.Nullable;",
						"import com.android.annotations.NonNull;",
						"public class NeededNullableReturn {",
						"	@Nullable",
						"	public char[] myMethod() {",
						"	}",
						"",
						"	@NonNull",
						"	public int[] myOtherMethod() {",
						"	}",
						"}"
					)
				));

		assertThat("Primitive array return types should not be reported", getWarnings(), Matchers.empty());
	}
}
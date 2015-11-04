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

import static com.monits.linters.FactoryMethodDetector.FACTORY_METHOD_ERROR_MESSAGE;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matchers;

import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.monits.linters.matchers.WarningMatcherBuilder;


public class FactoryMethodDetectorTest extends AbstractTestCase {

	private static final List<String> ANDROID_SUPPORT_V4_PATH = Arrays.asList("/android-support-v4.jar");

	private final TestFile myStringFragment = java("src/MyStringFragment.java",
			"import android.os.Bundle;\n"
			+ "import android.app.Fragment;\n"
			+ "\n"
			+ "public class MyStringFragment extends Fragment {\n"
			+ "\n"
			+ "    public static MyStringFragment newInstance() {\n"
			+ "        Bundle args = new Bundle();\n"
			+ "        MyStringFragment fragment = new MyStringFragment();\n"
			+ "        fragment.setArguments(args);\n"
			+ "        return fragment;\n"
			+ "    }\n"
			+ "}\n");

	@Override
	protected Detector getDetector() {
		return new FactoryMethodDetector();
	}

	@Override
	@Nonnull
	protected List<Issue> getIssues() {
		return Arrays.asList(FactoryMethodDetector.USE_FACTORY_METHOD_INSTEAD_NEW_FRAGMENT);
	}

	public void testBadFactoryMethodCallInAFragment() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/BadFactoryMethodCallInAFragment.java.txt"
						+ "=>src/BadFactoryMethodCallInAFragment.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertThat("Failed while trying to check a missing factory method call in a Fragment", getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
			.fileName("BadFactoryMethodCallInAFragment.java")
			.line(10)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build()));
	}

	public void testBadFactoryMethodCallInAnActivity() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/BadFactoryMethodCallInAnActivity.java.txt"
						+ "=>src/BadFactoryMethodCallInAnActivity.java",
						"factorymethod/ChildFragment.java.txt=>src/ChildFragment.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertThat("Failed while trying to check a missing factory method call in an Activity",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
			.fileName("BadFactoryMethodCallInAnActivity.java")
			.line(7)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build()));
	}

	public void testBadFactoryMethodCallInAFragmentFactory() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/BadFactoryMethodCallInAFragmentFactory.java.txt"
						+ "=>src/BadFactoryMethodCallInAFragmentFactory.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertThat("Failed while trying to check a missing factory method call in a Fragment Factory",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
			.fileName("BadFactoryMethodCallInAFragmentFactory.java")
			.line(13)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build()));
	}

	public void testGoodFactoryMethodCallInAFragment() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/GoodFactoryMethodCallInAFragment.java.txt"
						+ "=>src/GoodFactoryMethodCallInAFragment.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertTrue("There are unexpected warnings when checks a Factory Method call in a Fragment",
				getWarnings().isEmpty());
	}

	public void testGoodFactoryMethod() throws Exception {
		lintProject(compile(myStringFragment));

		assertTrue("There are unexpected warnings when checks a Factory Method in the same Fragment",
				getWarnings().isEmpty());
	}

	public void testGoodFactoryMethodThatNotExtendsDirectlyFragment() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/ChildFragment.java.txt=>src/ChildFragment.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertTrue("There are unexpected warnings when checks a Factory Method that indirectly extends Fragment",
				getWarnings().isEmpty());
	}

	public void testBadFactoryMethodCallWithAFragmentAsParam() throws Exception {
		final TestFile myStringConfigFragment = java("src/MyStringConfigFragment.java",
				"import android.app.Fragment;\n"
				+ "\n"
				+ "public class MyStringConfigFragment extends Fragment {\n"
				+ "\n"
				+ "    public static void configFragment(final MyStringFragment f) {\n"
				+ "        new MyStringFragment();\n"
				+ "    }\n"
				+ "}");

		lintProject(compile(myStringFragment, myStringConfigFragment));

		assertThat("Failed while trying to check a missing factory method call in a method with a Fragment as param",
			getWarnings(),
			Matchers.contains(new WarningMatcherBuilder()
			.fileName("MyStringConfigFragment.java")
			.line(6)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build()));
	}

	public void testIgnoreNewFragmentInstance() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/TestIgnoreNewFragmentInstance.java.txt"
						+ "=>src/TestIgnoreNewFragmentInstance.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertThat("Failed while trying to ignore one new fragment instance", getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
			.fileName("TestIgnoreNewFragmentInstance.java")
			.line(12)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build())));
	}

	public void testIgnoreNewFragmentInstanceInFragmentFactory() throws Exception {
		lintProject(
			compile(ANDROID_SUPPORT_V4_PATH,
				file("factorymethod/TestIgnoreNewFragmentInstanceInFragmentFactory.java.txt"
						+ "=>src/TestIgnoreNewFragmentInstanceInFragmentFactory.java",
						"factorymethod/MyFragment.java.txt=>src/MyFragment.java")
				));

		assertThat("Failed while trying to ignore one new fragment instance in a fragment factory", getWarnings(),
			not(Matchers.contains(new WarningMatcherBuilder()
			.fileName("TestIgnoreNewFragmentInstanceInFragmentFactory.java")
			.line(15)
			.message(FACTORY_METHOD_ERROR_MESSAGE)
			.build())));
	}
}
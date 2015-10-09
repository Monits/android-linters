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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.android.tools.lint.Warning;
import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractTestCase extends LintDetectorTest {
	private static final String CLASS_PATH = "bin/classes/";
	private static final String DATA_PATH = "/data/";
	private static final String ANDROID_JAR_PATH = "/android.jar";
	private static final String CLASSPATH_OPTION_COMPILE = "-classpath";
	private static final String OUTPUT_DIR_OPTION_COMPILE = "-d";
	private static final char DOUBLE_COLON = ':';

	// the -g option have three keywords, line, source and vars by default line and source are enabled.
	// because we need to know the local variables (vars) we are adding this option to enable the local variables table.
	private static final String DEBUG_INFO_OPTION_COMPILE = "-g";

	private final InMemoryReporter inMemoryReporter;

	/**
	 * Constructor
	 */
	public AbstractTestCase() {
		super();
		try {
			inMemoryReporter = new InMemoryReporter();
		} catch (final IOException e) {
			throw new IOError(e);
		}
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		getWarnings().clear();
	}

	@Nonnull
	public List<Warning> getWarnings() {
		return inMemoryReporter.getIssues();
	}

	@Override
	protected InputStream getTestResource(final String relativePath, final boolean expectExists) {
		final String path = DATA_PATH + relativePath;
		final InputStream stream = AbstractTestCase.class.getResourceAsStream(path);
		if (!expectExists && stream == null) {
			return null;
		}
		return stream;
	}

	@Override
	protected TestLintClient createClient() {
		final TestLintClient lintClient = super.createClient();

		lintClient.getFlags().getReporters().add(inMemoryReporter);

		return lintClient;
	}

	@Nonnull
	@Override
	public ClassTestFile file() {
		return new ClassTestFile();
	}

	/**
	 * Compile a single or multiple java files
	 *
	 * @param javaFiles A list of {@link TestFile} with the target relative path and the content of the file
	 *
	 * @return An array of {@link TestFile} for the .class generated with its source files
	 *
	 * @throws IOException If the java file cannot be created
	 */
	@Nonnull
	public TestFile[] compile(@Nonnull final TestFile... javaFiles) throws IOException {
		return compile(Collections.<String>emptyList(), javaFiles);
	}

	/**
	 * Compile a single or multiple java files
	 *
	 * @param externalJars A list of jars to add to the compiler
	 * @param javaFiles A list of {@link TestFile} with the target relative path and the content of the file
	 *
	 * @return An array of {@link TestFile} for the .class generated with its source files
	 *
	 * @throws IOException If the java file cannot be created
	 */
	@Nonnull
	public TestFile[] compile(@Nonnull final List<String> externalJars, @Nonnull final TestFile... javaFiles)
			throws IOException {
		final File tempDir = Files.createTempDir();
		final List<TestFile> testFilesToAnalyze = new ArrayList<TestFile>();

		// generate java files
		final List<File> files = new ArrayList<File>(javaFiles.length);
		for (final TestFile tf : javaFiles) {
			files.add(tf.createFile(tempDir));
			// Add source files to generate a readable output error
			testFilesToAnalyze.add(tf);
		}

		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		List<String> jarList = new LinkedList<String>(externalJars);
		jarList.add(ANDROID_JAR_PATH);

		jarList = Lists.transform(jarList, new Function<String, String>() {
			@Override
			public String apply(final String input) {
				return getClass().getResource(input).getPath();
			}
		});

		final Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(files);
		// compile java files
		compiler.getTask(null, fileManager, null,
				Arrays.asList(DEBUG_INFO_OPTION_COMPILE, CLASSPATH_OPTION_COMPILE,
						Joiner.on(DOUBLE_COLON).join(jarList),
						OUTPUT_DIR_OPTION_COMPILE, tempDir.getAbsolutePath()),
				null, compilationUnits).call();

		for (final TestFile javaFile : javaFiles) {
			// add the class files to be analyzed
			testFilesToAnalyze.addAll(getClassTestFiles(tempDir, extractFileName(javaFile)));
		}

		return testFilesToAnalyze.toArray(new TestFile[testFilesToAnalyze.size()]);
	}

	/**
	 * Create a ClassTestFile list with all generated classes from the java file
	 * @param tempDir The dir where are located the files
	 * @param fileName The file name to find the classes generated
	 * @return A list of ClassTestFiles with the generated classes of the java file
	 * @throws IOException IF the file can't be read
	 */
	@Nonnull
	private List<ClassTestFile> getClassTestFiles(@Nonnull final File tempDir, @Nonnull final String fileName)
			throws IOException {
		final File[] classFiles =
				getGeneratedClasses(new File(tempDir.getAbsolutePath() + File.separator), fileName);

		// generate the class TestFiles
		if (classFiles != null) {
			final List<ClassTestFile> list = new ArrayList<ClassTestFile>(classFiles.length);
			for (final File file : classFiles) {
				final ClassTestFile classFile = file();
				//The targetRelativePath must be in bin/classses/ because the linter uses files from that folder
				classFile.to(CLASS_PATH + file.getName());
				classFile.bytes = Files.asByteSource(file).read();
				list.add(classFile);
			}
			return list;
		}
		return Collections.emptyList();
	}

	/**
	 * Look for all classes generated by the java file
	 * @param classDirPath The path to the class files
	 * @param fileName The file name to find the classes
	 * @return A list of class files
	 */
	@Nullable
	private File[] getGeneratedClasses(@Nonnull final File classDirPath, @Nonnull final String fileName) {
		final String fileNameWithoutExtension = fileName.substring(0, fileName.indexOf('.'));
		return classDirPath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(@Nonnull final File dir, @Nonnull final String name) {
				return name.startsWith(fileNameWithoutExtension);
			}
		});
	}

	/**
	 * Receive a list of resources and create TestFiles
	 * @param resources The resource list
	 * @return A list of TestFiles
	 */
	@Nonnull
	protected TestFile[] file(@Nonnull final String... resources) {
		final List<TestFile> testFiles = new ArrayList<>();
		for (final String resource : resources) {
			testFiles.add(file().copy(resource));
		}
		return testFiles.toArray(new TestFile[testFiles.size()]);
	}

	@Nonnull
	private String extractFileName(@Nonnull final TestFile file) {
		return file.targetRelativePath.substring(file.targetRelativePath.lastIndexOf(File.separator) + 1);
	}

	public class ClassTestFile extends TestFile {
		@SuppressFBWarnings(value = "MISSING_FIELD_IN_TO_STRING", justification = "Non readable for humans")
		byte[] bytes;

		@Nonnull
		@Override
		public File createFile(final @Nonnull File targetDir) throws IOException {
			if (bytes == null) {
				return super.createFile(targetDir);
			}
			final InputStream stream = new ByteArrayInputStream(bytes);
			assertNotNull(sourceRelativePath + " does not exist", stream);
			final int index = targetRelativePath.lastIndexOf('/');
			String relative = null;
			String name = targetRelativePath;
			if (index != -1) {
				name = targetRelativePath.substring(index + 1);
				relative = targetRelativePath.substring(0, index);
			}

			return makeTestFile(targetDir, name, relative, stream);
		}
	}
}
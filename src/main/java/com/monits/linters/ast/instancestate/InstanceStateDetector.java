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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import lombok.ast.AstVisitor;
import lombok.ast.CompilationUnit;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodDeclaration;
import lombok.ast.StrictListAccessor;
import lombok.ast.VariableDefinition;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.ImmutableSet;

public class InstanceStateDetector extends Detector implements JavaScanner {

	/* default */ static final String VIEW_INFLATE_MSG = "View.inflate does not apply "
			+ "theme / defaults to inflated views on pre-lollipop.";
	/* default */ static final String ALREADY_RESTORED_KEY = "There are multiple calls "
			+ "to restore the state under key %s. This lends itself to errors.";
	/* default */ static final String ALREADY_SAVED_KEY = "There are multiple calls "
			+ "to save the state under key %s. This lends itself to errors.";
	/* default */ static final String RESTORED_BUT_NEVER_SAVED_MSG = "No value is ever stored under key %s. "
			+ "You are missing a store, or this is dead code";
	/* default */ static final String SAVED_BUT_NEVER_RESTORED_MSG = "The value stored under the key %s is never read. "
			+ "You are missing to restore it, or this is dead code";
	/* default */ static final String INVALID_TYPE_MSG = "The value stored under the key %s is read as %s but stored as %s.";
	/* default */ static final String DIFFERENT_FIELDS_MSG = "The value stored under the key %s is not read and stored from the same field.";
	/* default */ static final String KEY_IS_NOT_CONSTANT_MSG = "The field %s used as key for bundle properties is not static final";
	
	public static final Issue SAVED_BUT_NEVER_RESTORED = Issue.create("InstanceStateNotRestored",
			"Instance state is saved but never restored",
			"All persisted state should be restored, or not stored at all",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue RESTORED_BUT_NEVER_SAVED = Issue.create("InstanceStateNotSaved",
			"Instance state is restored but never saved",
			"The state is never being stored, and is therefore always null. "
			+ "You are either missing a store or this is dead code.",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue STATE_ALREADY_RESTORED = Issue.create("AlreadyRestored",
			"The instance state is being restored more than once",
			"This usually leads to harder to mantain code, keep things simple by centralizing state restoration",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue STATE_ALREADY_SAVED = Issue.create("AlreadySaved",
			"A value has already been saved under this key and is beng overwritten",
			"This usually leads to harder to mantain code, keep things simple by centralizing state persistence",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue INVALID_TYPE = Issue.create("InvalidType",
			"Save / restore method calls use different types for same key",
			"Saving and restoring a value under a single key must always refer to it using the same type",
			Category.CORRECTNESS, 6, Severity.ERROR,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue DIFFERENT_FIELDS = Issue.create("DifferentFields",
			"Save / restore method calls for this key do not operate directly into the same field",
			"For simplicity and correctness, you should read from / into the field directly, without local variables",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	public static final Issue KEY_IS_NOT_CONSTANT = Issue.create("KeyIsNotConstant",
			"To ensure consistency, keys used to access bundles should be static final",
			"To ensure consistency, keys used to access bundles should be static final",
			Category.CORRECTNESS, 6, Severity.WARNING,
			new Implementation(InstanceStateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	@Override
	public EnumSet<Scope> getApplicableFiles() {
		return Scope.JAVA_FILE_SCOPE;
	}
	
	@Override
	public AstVisitor createJavaVisitor(final JavaContext context) {
		return new InstanceStateChecker(context);
	}
	
	private static class InstanceStateChecker extends ForwardingAstVisitor {
		
		// TODO : Persistent state is not currently handled by this detector
		private static final Set<String> METHOD_SAVE_INSTANCES = ImmutableSet.of("onSaveInstanceState");
		private static final Set<String> METHOD_RESTORE_INSTANCES =
				ImmutableSet.of(
					// Common methods
					"onCreate",
					// Activity methods
					"onPostCreate", "onRestoreInstanceState",
					// Fragment methods
					"onActivityCreated", "onCreateView", "onViewCreated", "onViewStateRestored", "onInflate");
		
		private static final Set<String> BUNDLE_NAMES = ImmutableSet.of(
			"Bundle",
			"android.os.Bundle"
		);
		
		private final JavaContext context;
		private Map<String, BundleMethodInvocation> storedState;
		private Map<String, BundleMethodInvocation> restoredState;
		
		public InstanceStateChecker(final JavaContext context) {
			this.context = context;
		}
		
		@Override
		public boolean visitCompilationUnit(final CompilationUnit node) {
			// TODO : Is this ok with nested classes?
			// TODO : Can't default methods in interface screw me over?
			
			// Reset keys being used
			storedState = new TreeMap<String, BundleMethodInvocation>();
			restoredState = new TreeMap<String, BundleMethodInvocation>();
			
			return super.visitCompilationUnit(node);
		}
		
		@Override
		public void afterVisitCompilationUnit(CompilationUnit node) {
			final Iterator<Entry<String, BundleMethodInvocation>> storedIterator
				= storedState.entrySet().iterator();
			final Iterator<Entry<String, BundleMethodInvocation>> restoredIterator
				= restoredState.entrySet().iterator();
			
			Entry<String, BundleMethodInvocation> currentStoredEntry = nextOrNull(storedIterator);
			Entry<String, BundleMethodInvocation> currentRestoredEntry = nextOrNull(restoredIterator);
			
			while (currentStoredEntry != null || currentRestoredEntry != null) {
				final int comparation;
				
				// null-safe "comparation"
				if (currentStoredEntry == null) {
					comparation = -1;
				} else if (currentRestoredEntry == null) {
					comparation = 1;
				} else {
					comparation = currentRestoredEntry.getKey().compareTo(currentStoredEntry.getKey());
				}
				
				if (comparation == 0) {
					// Check it and move both iterators
					final BundleMethodInvocation bmiRestore = currentRestoredEntry.getValue();
					final BundleMethodInvocation bmiStore = currentStoredEntry.getValue();
					
					// Both operations use the same data type?
					if (!bmiRestore.dataType.equals(bmiStore.dataType)) {
						// Report it at both locations
						final String message = String.format(INVALID_TYPE_MSG, bmiRestore.key,
								bmiRestore.dataType, bmiStore.dataType);
						context.report(INVALID_TYPE, bmiRestore.node, context.getLocation(bmiRestore.node),
								message);
						context.report(INVALID_TYPE, bmiStore.node, context.getLocation(bmiStore.node),
								message);
					}
					
					// Both operations use the same field?
					if ((bmiRestore.fieldName != null && !bmiRestore.fieldName.equals(bmiStore.fieldName))
							|| (bmiStore.fieldName != null && !bmiStore.fieldName.equals(bmiRestore.fieldName))) {
						// Report it at both locations
						final String message = String.format(DIFFERENT_FIELDS_MSG, bmiRestore.key);
						context.report(DIFFERENT_FIELDS, bmiRestore.node, context.getLocation(bmiRestore.node),
								message);
						context.report(DIFFERENT_FIELDS, bmiStore.node, context.getLocation(bmiStore.node),
								message);
					}
					
					currentRestoredEntry = nextOrNull(restoredIterator);
					currentStoredEntry = nextOrNull(storedIterator);
				} else if (comparation < 0) {
					// report it, and move just this iterator
					final BundleMethodInvocation bmi = currentRestoredEntry.getValue();
					context.report(RESTORED_BUT_NEVER_SAVED, bmi.node, context.getLocation(bmi.node),
							String.format(RESTORED_BUT_NEVER_SAVED_MSG, bmi.key));
					currentRestoredEntry = nextOrNull(restoredIterator);
				} else {
					// report it, and move just this iterator
					final BundleMethodInvocation bmi = currentStoredEntry.getValue();
					context.report(SAVED_BUT_NEVER_RESTORED, bmi.node, context.getLocation(bmi.node),
							String.format(SAVED_BUT_NEVER_RESTORED_MSG, bmi.key));
					currentStoredEntry = nextOrNull(storedIterator);
				}
			}
			
			super.afterVisitCompilationUnit(node);
		}
		
		private <T> T nextOrNull(final Iterator<T> iterator) {
			return iterator.hasNext() ? iterator.next() : null;
		}
		
		@Override
		public boolean visitMethodDeclaration(final MethodDeclaration node) {
			if (METHOD_SAVE_INSTANCES.contains(node.astMethodName().astValue())) {
				checkSavedState(node);
			} else if (METHOD_RESTORE_INSTANCES.contains(node.astMethodName().astValue())) {
				checkRestoredState(node);
			}
			
			return super.visitMethodDeclaration(node);
		}

		private void checkSavedState(final MethodDeclaration node) {
			// Is it an overload?
			final StrictListAccessor<VariableDefinition, MethodDeclaration> parameters = node.astParameters();
			if (parameters.size() != 1 || !BUNDLE_NAMES.contains(parameters.first().astTypeReference().getTypeName())) {
				return;
			}
			
			final String bundleName = parameters.first().astVariables().first().astName().astValue();
			final InstanceStateVisitor storeVisitor = new InstanceStateVisitor(bundleName,
					InstanceStateVisitor.METHOD_PREFIX_PUT, context);
			node.astBody().accept(storeVisitor);
			addAllUsedKeysReportinguplicates(storeVisitor, storedState, STATE_ALREADY_SAVED, ALREADY_SAVED_KEY);
		}
		
		private void checkRestoredState(final MethodDeclaration node) {
			// Check which one is the bundle parameter
			String bundleName = null;
			for (final VariableDefinition varDef : node.astParameters()) {
				if (BUNDLE_NAMES.contains(varDef.astTypeReference().getTypeName())) {
					bundleName = varDef.astVariables().first().astName().astValue();
					break;
				}
			}
			
			// probably an overload...
			if (bundleName == null) {
				return;
			}
			
			final InstanceStateVisitor storeVisitor = new InstanceStateVisitor(bundleName,
					InstanceStateVisitor.METHOD_PREFIX_GET, context);
			node.astBody().accept(storeVisitor);
			addAllUsedKeysReportinguplicates(storeVisitor, restoredState, STATE_ALREADY_RESTORED, ALREADY_RESTORED_KEY);
		}

		private void addAllUsedKeysReportinguplicates(final InstanceStateVisitor stateVisitor,
				final Map<String, BundleMethodInvocation> stateMap, final Issue issueToReport, final String msg) {
			for (final BundleMethodInvocation bmi : stateVisitor.usedKeys) {
				if (stateMap.containsKey(bmi.key)) {
					context.report(issueToReport, bmi.node, context.getLocation(bmi.node),
							String.format(msg, bmi.key));
					
					// report over the original too
					final BundleMethodInvocation original = stateMap.get(bmi.key);
					context.report(issueToReport, original.node, context.getLocation(original.node),
							String.format(msg, original.key));
				} else {
					stateMap.put(bmi.key, bmi);
				}
			}
		}
	}
}

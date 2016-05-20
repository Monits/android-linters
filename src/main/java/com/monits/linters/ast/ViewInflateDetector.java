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

import java.util.EnumSet;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Identifier;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.Select;
import lombok.ast.VariableReference;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.ImmutableList;

public class ViewInflateDetector extends Detector implements JavaScanner {

	/* default */ static final String VIEW_INFLATE_MSG = "View.inflate does not apply "
			+ "theme / defaults to inflated views on pre-lollipop.";
	
	public static final Issue VIEW_INFLATE_IGNORES_THEME = Issue.create("ViewInflate",
			"Prevents calls to View.inflate, which ignores applied themes and defaults",
			"Using the activity's layout inflater is much safer on pre-lollipop",
			Category.CORRECTNESS, 6, Severity.FATAL,
			new Implementation(ViewInflateDetector.class, Scope.JAVA_FILE_SCOPE));
	
	private static final int LOLLIPOP_API_LEVEL = 21;
	
	@Override
	public EnumSet<Scope> getApplicableFiles() {
		return EnumSet.of(Scope.JAVA_FILE, Scope.MANIFEST);
	}
	
	@Override
	public List<Class<? extends Node>> getApplicableNodeTypes() {
		return ImmutableList.<Class<? extends Node>>of(MethodInvocation.class);
	}
	
	@Override
	public AstVisitor createJavaVisitor(final JavaContext context) {
		return new ViewInflateChecker(context);
	}
	
	private static class ViewInflateChecker extends ForwardingAstVisitor {
		private static final String VIEW = "View";
		private static final String FQCN_VIEW = "android.view." + VIEW;
		private static final String INFLATE = "inflate";
		
		private final JavaContext context;
		
		public ViewInflateChecker(final JavaContext context) {
			this.context = context;
		}
		
		@Override
		public boolean visitMethodInvocation(final MethodInvocation node) {
			// If taarget is lollipop or newer, there is nothing to report
			if (context.getProject().getMinSdk() >= LOLLIPOP_API_LEVEL) {
				return super.visitMethodInvocation(node);
			}
				
			// Is the method's name "inflate"?
			if (!node.astName().astValue().equals(INFLATE)) {
				return super.visitMethodInvocation(node);
			}
			
			final List<Node> children = node.astOperand().getChildren();
			final String typeName = nodeListToString(children);
			
			if (typeName == null) {
				return super.visitMethodInvocation(node);
			}
			
			// We are looking for a static invocation
			if (typeName.equals(VIEW) || typeName.equals(FQCN_VIEW)) {
				context.report(VIEW_INFLATE_IGNORES_THEME, node, context.getLocation(node),
						VIEW_INFLATE_MSG);
			}
			
			return super.visitMethodInvocation(node);
		}
		
		private String nodeListToString(final List<Node> children) {
			if (children.size() == 1 && children.get(0) instanceof Identifier) {
				final Identifier type = (Identifier) children.get(0);
				return type.astValue();
			} else if (children.size() == 2 && children.get(0) instanceof Select
					&& children.get(1) instanceof Identifier) {
				final Identifier type = (Identifier) children.get(1);
				return nodeListToString(children.get(0).getChildren()) + "." + type.astValue();
			} else if (children.size() == 2 && children.get(0) instanceof VariableReference
					&& children.get(1) instanceof Identifier) {
				final Identifier type1 = ((VariableReference) children.get(0)).astIdentifier();
				final Identifier type2 = (Identifier) children.get(1);
				return type1.astValue() + "." + type2.astValue();
			}
			
			return null;
		}
	}
}

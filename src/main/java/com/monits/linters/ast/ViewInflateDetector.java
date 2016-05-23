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
import java.util.Set;

import lombok.ast.AstVisitor;
import lombok.ast.ClassDeclaration;
import lombok.ast.CompilationUnit;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Identifier;
import lombok.ast.ImportDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.Select;
import lombok.ast.TypeDeclaration;
import lombok.ast.VariableReference;
import lombok.ast.resolve.Resolver;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.JavaScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

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
		return Scope.JAVA_FILE_SCOPE;
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
		/*
		 * This list was obtained by going to https://developer.android.com/reference/android/view/View.html
		 * and in the Developer Console doing:
		 * 
		 * $('#subclasses-indirect-summary tr').add('#subclasses-direct-summary tr').find('td:first a')
		 * .map(function() { return '"' + $(this).attr('href').substring("https://developer.android.com/reference/".length,
		 * $(this).attr('href').lastIndexOf('.')).replace(/\//g, '.') + '"'; }).get().sort().join(',\n');
		 * 
		 * Remember to manually add android.view.View
		 */
		private static final Set<String> VIEW_CLASSES = ImmutableSet.of(
				"android.view.View",
				"android.app.FragmentBreadCrumbs",
				"android.app.MediaRouteButton",
				"android.appwidget.AppWidgetHostView",
				"android.gesture.GestureOverlayView",
				"android.inputmethodservice.ExtractEditText",
				"android.inputmethodservice.KeyboardView",
				"android.media.tv.TvView",
				"android.opengl.GLSurfaceView",
				"android.support.design.widget.AppBarLayout",
				"android.support.design.widget.CollapsingToolbarLayout",
				"android.support.design.widget.CoordinatorLayout",
				"android.support.design.widget.FloatingActionButton",
				"android.support.design.widget.NavigationView",
				"android.support.design.widget.TabItem",
				"android.support.design.widget.TabLayout",
				"android.support.design.widget.TextInputEditText",
				"android.support.design.widget.TextInputLayout",
				"android.support.percent.PercentFrameLayout",
				"android.support.percent.PercentRelativeLayout",
				"android.support.v17.leanback.widget.BaseCardView",
				"android.support.v17.leanback.widget.BrowseFrameLayout",
				"android.support.v17.leanback.widget.GuidedActionEditText",
				"android.support.v17.leanback.widget.HorizontalGridView",
				"android.support.v17.leanback.widget.ImageCardView",
				"android.support.v17.leanback.widget.ListRowHoverCardView",
				"android.support.v17.leanback.widget.ListRowView",
				"android.support.v17.leanback.widget.RowHeaderView",
				"android.support.v17.leanback.widget.SearchBar",
				"android.support.v17.leanback.widget.SearchEditText",
				"android.support.v17.leanback.widget.SearchOrbView",
				"android.support.v17.leanback.widget.ShadowOverlayContainer",
				"android.support.v17.leanback.widget.SpeechOrbView",
				"android.support.v17.leanback.widget.TitleView",
				"android.support.v17.leanback.widget.VerticalGridView",
				"android.support.v17.leanback.widget.picker.Picker",
				"android.support.v4.app.FragmentTabHost",
				"android.support.v4.view.PagerTabStrip",
				"android.support.v4.view.PagerTitleStrip",
				"android.support.v4.view.ViewPager",
				"android.support.v4.widget.ContentLoadingProgressBar",
				"android.support.v4.widget.DrawerLayout",
				"android.support.v4.widget.NestedScrollView",
				"android.support.v4.widget.SlidingPaneLayout",
				"android.support.v4.widget.SwipeRefreshLayout",
				"android.support.v7.widget.AppCompatAutoCompleteTextView",
				"android.support.v7.widget.AppCompatButton",
				"android.support.v7.widget.AppCompatCheckBox",
				"android.support.v7.widget.AppCompatCheckedTextView",
				"android.support.v7.widget.AppCompatEditText",
				"android.support.v7.widget.AppCompatImageButton",
				"android.support.v7.widget.AppCompatImageView",
				"android.support.v7.widget.AppCompatMultiAutoCompleteTextView",
				"android.support.v7.widget.AppCompatRadioButton",
				"android.support.v7.widget.AppCompatRatingBar",
				"android.support.v7.widget.AppCompatSeekBar",
				"android.support.v7.widget.AppCompatSpinner",
				"android.support.v7.widget.AppCompatTextView",
				"android.support.v7.widget.CardView",
				"android.support.v7.widget.LinearLayoutCompat",
				"android.support.v7.widget.RecyclerView",
				"android.support.v7.widget.Space",
				"android.support.v7.widget.SwitchCompat",
				"android.view.SurfaceView",
				"android.view.TextureView",
				"android.view.ViewGroup",
				"android.view.ViewStub",
				"android.webkit.WebView",
				"android.widget.AbsListView",
				"android.widget.AbsSeekBar",
				"android.widget.AbsSpinner",
				"android.widget.AbsoluteLayout",
				"android.widget.ActionMenuView",
				"android.widget.Adapter",
				"android.widget.AdapterView",
				"android.widget.AdapterViewAnimator",
				"android.widget.AdapterViewFlipper",
				"android.widget.AnalogClock",
				"android.widget.AutoCompleteTextView",
				"android.widget.Button",
				"android.widget.CalendarView",
				"android.widget.CheckBox",
				"android.widget.CheckedTextView",
				"android.widget.Chronometer",
				"android.widget.CompoundButton",
				"android.widget.DatePicker",
				"android.widget.DialerFilter",
				"android.widget.DigitalClock",
				"android.widget.EditText",
				"android.widget.ExpandableListView",
				"android.widget.FrameLayout",
				"android.widget.Gallery",
				"android.widget.GridLayout",
				"android.widget.GridView",
				"android.widget.HorizontalScrollView",
				"android.widget.ImageButton",
				"android.widget.ImageSwitcher",
				"android.widget.ImageView",
				"android.widget.LinearLayout",
				"android.widget.ListView",
				"android.widget.MediaController",
				"android.widget.MultiAutoCompleteTextView",
				"android.widget.NumberPicker",
				"android.widget.ProgressBar",
				"android.widget.QuickContactBadge",
				"android.widget.RadioButton",
				"android.widget.RadioGroup",
				"android.widget.RatingBar",
				"android.widget.RelativeLayout",
				"android.widget.ScrollView",
				"android.widget.SearchView",
				"android.widget.SeekBar",
				"android.widget.SlidingDrawer",
				"android.widget.Space",
				"android.widget.Spinner",
				"android.widget.StackView",
				"android.widget.Switch",
				"android.widget.TabHost",
				"android.widget.TabWidget",
				"android.widget.TableLayout",
				"android.widget.TableRow",
				"android.widget.TextClock",
				"android.widget.TextSwitcher",
				"android.widget.TextView",
				"android.widget.TimePicker",
				"android.widget.ToggleButton",
				"android.widget.Toolbar",
				"android.widget.TwoLineListItem",
				"android.widget.VideoView",
				"android.widget.ViewAnimator",
				"android.widget.ViewFlipper",
				"android.widget.ViewSwitcher",
				"android.widget.ZoomButton",
				"android.widget.ZoomControls"
			);
		private static final String INFLATE = "inflate";
		
		private final JavaContext context;
		
		public ViewInflateChecker(final JavaContext context) {
			this.context = context;
		}
		
		@Override
		public boolean visitMethodInvocation(final MethodInvocation node) {
			// If target is lollipop or newer, there is nothing to report
			if (context.getProject().getMinSdk() >= LOLLIPOP_API_LEVEL) {
				return super.visitMethodInvocation(node);
			}
			
			// Is the method's name "inflate"?
			if (!node.astName().astValue().equals(INFLATE)) {
				return super.visitMethodInvocation(node);
			}
			
			// The FQCN of the type on which we are calling inflate
			String typeName = null;
			
			if (node.astOperand() == null) {
				// Is it a static import?
				final CompilationUnit cu = TreeTransversal.getClosestParent(node, CompilationUnit.class);
				for (final ImportDeclaration id : cu.astImportDeclarations()) {
					if (id.astStaticImport() && INFLATE.equals(id.astParts().last().astValue())) {
						// Gotcha!
						final String importedValue = id.asFullyQualifiedName();
						typeName = importedValue.substring(0, importedValue.lastIndexOf('.'));
						break;
					}
				}
				
				if (typeName == null) {
					// Then we must extend View ourselves, or it's not the inflate we are looking for
					final TypeDeclaration td = TreeTransversal.getClosestParent(node, TypeDeclaration.class);
					if (!(td instanceof ClassDeclaration)) {
						return super.visitMethodInvocation(node);
					}
					
					// Are we extending another class?
					final ClassDeclaration cd = (ClassDeclaration) td;
					if (cd.astExtending() == null) {
						return super.visitMethodInvocation(node);
					}

					// Check our parent against all known View implementations
					final Resolver resolver = new Resolver();
					for (final String candidate : VIEW_CLASSES) {
						if (resolver.typesMatch(candidate, cd.astExtending())) {
							// TODO : Should we add this class to known View implementations and request a new pass?
							typeName = candidate;
							break;
						}
					}
				}
			} else {
				final List<Node> children = node.astOperand().getChildren();
				final String operand = nodeListToString(children);
				
				// Safeguard, in case our nodeListToString implementation is incomplete...
				if (operand == null) {
					return super.visitMethodInvocation(node);
				}
				
				if (children.size() > 1) {
					// Gotcha! It's a FQCN
					typeName = operand;
				} else {
					// Make sure this is an explicit static call and the class on which we are calling extends View
					final CompilationUnit cu = TreeTransversal.getClosestParent(node, CompilationUnit.class);
					for (final ImportDeclaration id : cu.astImportDeclarations()) {
						if (id.astStaticImport()) {
							continue;
						}
						
						if (id.astStarImport()) {
							/*
							 * This may produce FPs if a class with the same name exists in the same package
							 * and is NOT a View, but we can't tell!
							 */
							final String candidate = id.asFullyQualifiedName().replace("*", operand);
							if (VIEW_CLASSES.contains(candidate)) {
								typeName = candidate;
								// Keep looking, since non-star imports have precedence, and may conduct to a non-match
							}
						} else if (operand.equals(id.astParts().last().astValue())) {
							// Gotcha!
							typeName = id.asFullyQualifiedName();
							break;
						}
					}
					
					if (typeName == null) {
						// Still not found? Are we in the same package as a known View implementation?
						final String candidate = cu.astPackageDeclaration().getPackageName() + "." + operand;
						if (VIEW_CLASSES.contains(candidate)) {
							typeName = candidate;
						}
					}
				}
			}
			
			if (VIEW_CLASSES.contains(typeName)) {
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

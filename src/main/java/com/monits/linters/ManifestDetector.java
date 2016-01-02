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
package com.monits.linters;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.SdkConstants.TAG_USES_PERMISSION;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;


public class ManifestDetector extends Detector implements Detector.XmlScanner {

	/** Duplicate uses-permission */
	public static final Issue DUPLICATE_USES_PERMISSION = Issue.create("DuplicateUsesPermission",
			"Checks that a uses-permission is registered only once in the manifest",
			"A uses-permission should only be registered once in the manifest.", Category.CORRECTNESS, 6,
			Severity.WARNING, new Implementation(ManifestDetector.class, Scope.MANIFEST_SCOPE));
	private final Map<String, Integer> permissions;

	/**
	 *  Creates a new ManifestDetector instance.
	 */
	public ManifestDetector() {
		permissions = new HashMap<>();
	}

	@Nonnull
	@Override
	public Collection<String> getApplicableElements() {
		return Arrays.asList(TAG_USES_PERMISSION);
	}

	@Override
	public void visitElement(@Nonnull final XmlContext context, @Nonnull final Element element) {
		final Attr nameNode = element.getAttributeNodeNS(ANDROID_URI, ATTR_NAME);
		if (nameNode == null) {
			return;
		}
		final String name = nameNode.getValue();
		final Location location = context.getLocation(nameNode);
		if (!name.isEmpty()) {
			if (permissions.containsKey(name)) {
				final StringBuilder message = new StringBuilder("Duplicate registration for uses-permission ")
						.append(name).append(" at line ").append(permissions.get(name));
				context.report(DUPLICATE_USES_PERMISSION, element, location, message.toString());
			} else {
				permissions.put(name, location.getStart().getLine() + 1);
			}
		}
	}

}

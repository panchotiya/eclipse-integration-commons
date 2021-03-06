/*******************************************************************************
 * Copyright (c) 2013 VMWare, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMWare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.quicksearch.core.priority;

import org.eclipse.core.resources.IResource;
import org.springsource.ide.eclipse.commons.quicksearch.core.preferences.QuickSearchPreferences;

/**
 * Default implementation of PriorityFunction. It doesn't de-emphasize anything but
 * if has some list of extensions and names that are used to ignore some types of
 * files and directories. 
 * <p>
 * This class can be used as is and customised by changing the lists, or it can be
 * subclassed and inherit some of the default behaviour by calling super.priority()
 */
public class DefaultPriorityFunction extends PriorityFunction {
	
	/**
	 * If true, any resources marked as 'derived' in the Eclipse workspace will
	 * be ignored.
	 */
	public boolean ignoreDerived = true;
	
	/**
	 * The default priority function causes any resources that end with these strings to
	 * be ignored.
	 */
	public String[] ignoredExtensions = {
		".gif", ".exe", ".png", ".jpg", ".zip", ".jar", ".svg", ".psd", "~",
		".GIF", ".EXE", ".PNG", ".JPG", ".ZIP", ".JAR", ".SVG", ".PSD",
		".pdf", ".p12", ".odt", ".odp", ".doc", ".pptx", ".ppt", ".bin", ".docx", ".xls", ".xlsx",
		".PDF", ".P12", ".ODT", ".ODP", ".DOC", ".PPTX", ".PPT", ".BIN", ".DOCX", ".XLS", ".XLSX"
	};
	
	/**
	 * The default priority function causes any resource who's name (i.e last path segment)
	 * starts with any of these Strings to be ignored.
	 */
	public String[] ignoredPrefixes = {
		"."
	};
	
	/**
	 * The default priority function causes any resources who's name equals any of these
	 * Strings to be ignored.
	 */
	public String[] ignoredNames = {
		"bin", "target", "build"
	};

	@Override
	public double priority(IResource r) {
		if (r!=null && r.isAccessible()) {
			if (ignoreDerived && r.isDerived()) {
				return PRIORITY_IGNORE;
			}
			String name = r.getName();
			for (String ext : ignoredExtensions) {
				if (name.endsWith(ext)) {
					return PRIORITY_IGNORE;
				}
			}
			for (String pre : ignoredPrefixes) {
				if (name.startsWith(pre)) {
					return PRIORITY_IGNORE;
				}
			}
			for (String n : ignoredNames) {
				if (name.equals(n)) {
					return PRIORITY_IGNORE;
				}
			}
			return PRIORITY_DEFAULT;
		}
		return PRIORITY_IGNORE;
	}

	/**
	 * Initialise some configurable settings from an instance of QuickSearchPreferences
	 */
	public void configure(QuickSearchPreferences preferences) {
		String[] pref = preferences.getIgnoredExtensions();
		if (pref!=null) {
			this.ignoredExtensions = pref;
		}
		pref = preferences.getIgnoredNames();
		if (pref!=null) {
			this.ignoredNames = pref;
		}
		pref = preferences.getIgnoredPrefixes();
		if (pref!=null) {
			this.ignoredPrefixes = pref;
		}
	}
}
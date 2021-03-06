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
package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;


/**
 * Represents something you can search for with a 'quick search' text searcher. 
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class QuickTextQuery {

	//TODO: delete and use jface Region class instead.
	public class TextRange implements IRegion {
		public final int start;
		public final int len;
		public TextRange(int start, int len) {
			this.start = start;
			this.len = len;
		}
		public int getLength() {
			return len;
		}
		public int getOffset() {
			return start;
		}
	}

	private boolean caseSensitive;
	private String orgPattern; //Original pattern case preserved even if search is case insensitive.
	private Matcher matcher;
	private Pattern pattern;

	/**
	 * A query that matches anything.
	 */
	public QuickTextQuery() {
		this("", true);
	}
	
	public QuickTextQuery(String substring, boolean caseSensitive) {
		this.orgPattern = substring;
		this.caseSensitive = caseSensitive;
		createMatcher(substring, caseSensitive);
	}

	/**
	 * Compile a pattern string into an RegExp and create a Matcher for that
	 * regexp. This is so we can 'compile' the pattern once and then keep reusing
	 * the matcher or compiled pattern.
	 */
	private void createMatcher(String patString, boolean caseSensitive) {
		StringBuilder segment = new StringBuilder(); //Accumulates text that needs to be 'quoted'
		StringBuilder regexp = new StringBuilder(); //Accumulates 'compiled' pattern
		int pos = 0, len = patString.length();
		while (pos<len) {
			char c = patString.charAt(pos++);
			switch (c) {
			case '?':
				appendSegment(segment, regexp);
				regexp.append(".");
				break;
			case '*':
				appendSegment(segment, regexp);
				regexp.append(".*");
				break;
// For now not supporting escapes with '\'.
// If this code is enabled. Should also add special case code to deal with it
// in isSubFilter method. The naive 'contains' test will be subtly broken for
// patterns that end with '\' and '\*'  (one searches for a '\' and the other does not!).
//			case '\\': 
//				if (pos+1<=len) {
//					char nextChar = patString.charAt(pos+1);
//					if (nextChar=='*' || nextChar=='?') {
//						segment.append(nextChar);
//						pos++;
//						break;
//					}
//				}
			default:
				//Char is 'nothing special'. Add it to segment that will be wrapped in 'quotes'
				segment.append(c);
				break;
			}
		}
		//Don't forget to process that last segment.
		appendSegment(segment, regexp);
		
		this.pattern = Pattern.compile(regexp.toString(), caseSensitive?0:Pattern.CASE_INSENSITIVE);
		this.matcher = pattern.matcher("");
	}

	private void appendSegment(StringBuilder segment, StringBuilder regexp) {
		if (segment.length()>0) {
			regexp.append(Pattern.quote(segment.toString()));
			segment.setLength(0); //clear: ready for next segment
		}
		//else {
		// nothing to append
		//}
	}

//	public StringMatcher getPattern() {
//		return pattern;
//	}

	public boolean equalsFilter(QuickTextQuery o) {
		//TODO: actually for case insensitive matches we could relax this and treat patterns that
		// differ only in case as the same.
		return this.caseSensitive == o.caseSensitive && this.orgPattern.equals(o.orgPattern);
	}

	/**
	 * Returns true if the other query is a specialisation of this query. I.e. any results matching the other
	 * query must also match this query. If this method returns true then we can optimise the search for other
	 * re-using already found results for this query. 
	 * <p>
	 * If it is hard or impossible to decide whether other query is a specialisation of this query then this
	 * method is allowed to 'punt' and just return false. However, the consequence of this is that the query 
	 * will be re-run instead of incrementally updated.
	 */
	public boolean isSubFilter(QuickTextQuery other) {
		if (this.isTrivial()) {
			return false;
		}
		if (this.caseSensitive==other.caseSensitive) {
			if (this.caseSensitive) {
				return other.orgPattern.contains(this.orgPattern);
			} else {
				return other.orgPattern.toLowerCase().contains(this.orgPattern.toLowerCase());
			}
		}
		return false;
	}

	/**
	 * @return whether the LineItem text contains the search pattern.
	 */
	public boolean matchItem(LineItem item) {
		return matchItem(item.getText());
	}

//	/**
//	 * Same as matchItem except only takes the text of the item. This can
//	 * be useful for efficient processing. In particular to avoid creating 
//	 * LineItem instances for non-matching lines.
//	 */
//	public synchronized boolean matchItem(String item) {
//		matcher.reset(item);
//		return matcher.find();
//	}
	
	/**
	 * Same as matchItem except only takes the text of the item. This can
	 * be useful for efficient processing. In particular to avoid creating 
	 * LineItem instances for non-matching lines.
	 */
	public boolean matchItem(String item) {
		//Alternate implementation. This is thread safe without synchronized,
		// but it creates some garbage.
		Matcher matcher = pattern.matcher(item); //Creating garbage here
		return matcher.find();
	}

	/**
	 * A trivial query is one that either 
	 *   - matches anything
	 *   - matches nothing
	 * In other words, if a query is 'trivial' then it returns either nothing or all the text in the scope
	 * of the search.
	 */
	public boolean isTrivial() {
		return "".equals(this.orgPattern);
	}

	@Override
	public String toString() {
		return "QTQuery("+orgPattern+", "+(caseSensitive?"caseSens":"caseInSens")+")";
	}

//	public synchronized List<TextRange> findAll(String text) {
//		if (isTrivial()) {
//			return Arrays.asList();
//		} else {
//			List<TextRange> ranges = new ArrayList<QuickTextQuery.TextRange>();
//			matcher.reset(text);
//			while (matcher.find()) {
//				int start = matcher.start();
//				int end = matcher.end();
//				ranges.add(new TextRange(start, end-start));
//			}
//			return ranges;
//		}
//	}

	public List<TextRange> findAll(String text) {
		//alternate implementation without 'synchronized' but creates more garbage
		if (isTrivial()) {
			return Arrays.asList();
		} else {
			List<TextRange> ranges = new ArrayList<QuickTextQuery.TextRange>();
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				ranges.add(new TextRange(start, end-start));
			}
			return ranges;
		}
	}
	
	public TextRange findFirst(String str) {
		//TODO: more efficient implementation, just search the first one 
		// no need to find all matches then toss away everything except the
		// first one.
		List<TextRange> all = findAll(str);
		if (all!=null && !all.isEmpty()) {
			return all.get(0);
		}
		return null;
	}
	
	public String getPatternString() {
		return orgPattern;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	
}

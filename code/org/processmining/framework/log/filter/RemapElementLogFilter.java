/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 16, 2006 7:54:10 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;
import org.processmining.framework.ui.Message;

/**
 * Log filter for remapping log event names to replacement strings, based on the
 * matching of given regular expressions.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RemapElementLogFilter extends LogFilter {

	protected ArrayList<Pattern> patternList;
	protected ArrayList<String> replacementList;

	public RemapElementLogFilter() {
		super(LogFilter.MODERATE, "Remap Element Log Filter");
		patternList = new ArrayList<Pattern>();
		replacementList = new ArrayList<String>();
	}

	public RemapElementLogFilter(ArrayList<Pattern> aPatternList,
			ArrayList<String> aReplacementList) {
		super(LogFilter.MODERATE, "Remap Element Log Filter");
		patternList = aPatternList;
		replacementList = aReplacementList;
	}

	public ArrayList<Pattern> getPatternList() {
		return this.patternList;
	}

	public ArrayList<String> getReplacementList() {
		return this.replacementList;
	}

	public String match(String element) {
		for (int i = 0; i < patternList.size(); i++) {
			if (patternList.get(i).matcher(element).matches()) {
				String result = patternList.get(i).matcher(element).replaceAll(
						replacementList.get(i));
				return result;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#doFiltering(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		AuditTrailEntry ate = null;
		String replacement = null;
		try {
			for (int i = 0; i < ateList.size(); i++) {
				ate = ateList.get(i);
				replacement = match(ate.getElement());
				if (replacement != null) {
					if (replacement.length() > 0) {
						ate.setElement(replacement);
						ateList.replace(ate, i);
					} else {
						ateList.remove(i--);
					}
				}
			}
		} catch (IOException e) {
			Message.add(e.getMessage(), Message.ERROR);
			return false;
		}
		return ateList.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#getHelpForThisLogFilter()
	 */
	protected String getHelpForThisLogFilter() {
		String help = "<html>"
				+ "Each event whose element name matches the given regular expression "
				+ "has the element name replaced by the respective replacement string.<br>"
				+ "Replacement strings may contain backreferences (<code>$<BRACKET_NR></code>) "
				+ "to dynamically include matched subsequences of the original element.<br>"
				+ "When an empty replacement string is given, all events matching the "
				+ "given regular expression are removed from the log. Any faulty given "
				+ "regular expression will be gracefully ignored!" + "</html>";
		return help;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#getParameterDialog(org.
	 * processmining.framework.log.LogSummary)
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new RemapElementLogFilterParamDialog(summary, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#thisFilterChangesLog()
	 */
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#writeSpecificXML(java.io.
	 * BufferedWriter)
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		String pattern, replacement;
		for (int i = 0; i < patternList.size(); i++) {
			pattern = patternList.get(i).pattern();
			replacement = replacementList.get(i);
			output.write("<remap regex=\"" + pattern + "\" replacement=\""
					+ replacement + "\"/>\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#readSpecificXML(org.w3c.dom
	 * .Node)
	 */
	protected void readSpecificXML(Node logFilterSpecificNode)
			throws IOException {
		patternList = new ArrayList<Pattern>();
		replacementList = new ArrayList<String>();
		Node node;
		String regex, replacement;
		Node regexNode, replacementNode;
		for (int i = 0; i < logFilterSpecificNode.getChildNodes().getLength(); i++) {
			node = logFilterSpecificNode.getChildNodes().item(i);
			if (node.getNodeName().equals("remap")) {
				regexNode = node.getAttributes().getNamedItem("regex");
				replacementNode = node.getAttributes().getNamedItem(
						"replacement");
				if (regexNode != null && replacementNode != null) {
					regex = regexNode.getNodeValue().trim();
					replacement = replacementNode.getNodeValue().trim();
					if (regex != null && regex.length() > 0) {
						patternList.add(Pattern.compile(regex));
						replacementList.add(replacement);
					} else {
						patternList = new ArrayList<Pattern>();
						replacementList = new ArrayList<String>();
						throw new IOException(
								"Null or zero-length regular expression attribute!");
					}
				} else {
					patternList = new ArrayList<Pattern>();
					replacementList = new ArrayList<String>();
					throw new IOException(
							"Remap node without regular expression or replacement attribute!");
				}
			}
		}
	}

}

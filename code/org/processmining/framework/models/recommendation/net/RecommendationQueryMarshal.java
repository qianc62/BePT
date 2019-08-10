/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.framework.models.recommendation.net;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.XmlUtils;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.xml.Document;
import org.processmining.lib.xml.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.processmining.framework.log.LogEvent;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RecommendationQueryMarshal {

	protected static final Charset UTF8 = Charset.forName("UTF-8");
	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS");
	protected SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	public String marshal(RecommendationQuery query) throws Exception {
		StringWriter writer = new StringWriter();
		// start document
		Document doc = new Document(writer, RecommendationQueryMarshal.UTF8);
		Tag root = doc.addNode("RecommendationQuery");
		root.addAttribute("id", query.getId());
		root.addAttribute("processId", query.getProcessId());
		root.addAttribute("processInstanceId", query.getProcessInstanceId());
		// add filter section if applicable
		if (query.getFilterUsers() != null || query.getFilterTasks() != null
				|| query.getFilterRoles() != null
				|| query.getFilterGroups() != null) {
			// add all filters
			Tag filterTag = root.addChildNode("Filter");
			addFilterList(filterTag, "User", query.getFilterUsers());
			addFilterList(filterTag, "Role", query.getFilterRoles());
			addFilterList(filterTag, "Group", query.getFilterGroups());
			addFilterLogEventList(filterTag, query.getFilterTasks());
		}
		// add engine state section if applicable
		if (query.getAvailableTasks() != null
				|| query.getAvailableUsers() != null
				|| query.getProcessInstanceData() != null) {
			Tag engineStateTag = root.addChildNode("EngineState");
			// write resources / users
			if (query.getAvailableUsers() != null) {
				Tag availableResourcesTag = engineStateTag
						.addChildNode("Resources");
				for (String resource : query.getAvailableUsers()) {
					Tag userTag = availableResourcesTag.addChildNode("User");
					userTag.addTextNode(resource.trim());
				}
			}
			// write available tasks
			if (query.getAvailableTasks() != null) {
				Tag availableUsersTag = engineStateTag.addChildNode("Tasks");
				for (LogEvent task : query.getAvailableTasks()) {
					Tag logEventTag = availableUsersTag
							.addChildNode("LogEvent");
					Tag taskTag = logEventTag.addChildNode("task");
					taskTag.addTextNode(task.getModelElementName().trim());
					Tag eventTag = logEventTag.addChildNode("event");
					eventTag.addTextNode(task.getEventType().trim());
				}
			}
			// write process instance data attributes
			addDataSection(engineStateTag, query.getProcessInstanceData());
		}
		// add audit trail
		Tag auditTrailTag = root.addChildNode("AuditTrail");
		// write audit trail entries
		for (AuditTrailEntry entry : query.getAuditTrail()) {
			Tag ate = auditTrailTag.addChildNode("AuditTrailEntry");
			// write attributes
			addDataSection(ate, entry.getAttributes());
			// workflow model element is mandatory for MXML
			Tag wfme = ate.addChildNode("WorkflowModelElement");
			wfme.addTextNode(entry.getElement());
			// event type field is also mandatory (check for unknown event type)
			Tag eventType = ate.addChildNode("EventType");
			EventType type = EventType.getType(entry.getType());
			if (type.isWellKnown()) {
				eventType.addTextNode(type.toString().trim());
			} else {
				eventType.addAttribute("unknowntype", type.toString().trim());
				eventType.addTextNode("unknown");
			}
			// timestamp field is optional
			if (entry.getTimestamp() != null) {
				Tag timestamp = ate.addChildNode("Timestamp");
				timestamp.addTextNode(RecommendationQueryMarshal.dateFormat
						.format(entry.getTimestamp())
						+ "+01:00");
			}
			// originator field is optional
			if (entry.getOriginator() != null) {
				Tag originator = ate.addChildNode("Originator");
				originator.addTextNode(entry.getOriginator().trim());
			}
		}
		// close document and clean up
		doc.close();
		writer.flush();
		// done!
		return writer.getBuffer().toString();
	}

	/**
	 * Internal convenience method; appends a data section to the given parent
	 * node as child, containing the key-value pairs in the supplied Map as
	 * attribute child nodes.
	 * 
	 * @param parentNode
	 * @param attributes
	 * @throws IOException
	 */
	protected void addDataSection(Tag parentNode, Map<String, String> attributes)
			throws IOException {
		if (attributes == null) {
			return;
		}
		if (attributes.size() > 0) {
			Tag dataNode = parentNode.addChildNode("Data");
			Tag attributeNode = null;
			String key = null;
			String value = null;
			// write sorted by key name
			Object keys[] = attributes.keySet().toArray();
			Arrays.sort(keys);
			for (int i = 0; i < keys.length; i++) {
				key = (String) keys[i];
				value = (String) attributes.get(key);
				attributeNode = dataNode.addChildNode("Attribute");
				attributeNode.addAttribute("name", key.trim());
				attributeNode.addTextNode(value.trim());
			}
		}
	}

	protected void addFilterList(Tag parentNode, String filterItemName,
			Set<String> filterStrings) throws IOException {
		if (filterStrings != null) {
			for (String filter : filterStrings) {
				Tag nFilterTag = parentNode.addChildNode(filterItemName);
				nFilterTag.addTextNode(filter.trim());
			}
		}
	}

	protected void addFilterLogEventList(Tag parentNode,
			Set<LogEvent> filterStrings) throws IOException {
		if (filterStrings != null) {
			for (LogEvent filter : filterStrings) {
				Tag LogEventTag = parentNode.addChildNode("LogEvent");
				Tag filterTaskTag = LogEventTag.addChildNode("task");
				filterTaskTag.addTextNode(filter.getModelElementName().trim());
				Tag filterEventTag = LogEventTag.addChildNode("event");
				filterEventTag.addTextNode(filter.getEventType().trim());
			}
		}
	}

	public RecommendationQuery unmarshal(String queryXml) throws Exception {
		SAXParser parser = parserFactory.newSAXParser();
		RecommendationQueryHandler handler = new RecommendationQueryHandler();
		parser.parse(new InputSource(new StringReader(queryXml)), handler);
		return handler.getQuery();
	}

	protected class RecommendationQueryHandler extends DefaultHandler {

		protected StringBuilder buffer = null;
		protected RecommendationQuery query = null;
		protected boolean inFilter = false;
		protected boolean inEngineState = false;
		protected AuditTrailEntryImpl currentAuditTrailEntry = null;
		protected String currentLogEventTask = "";
		protected String currentAttributeKey = null;

		public RecommendationQueryHandler() {
			buffer = new StringBuilder();
		}

		public RecommendationQuery getQuery() {
			return query;
		}

		public void startDocument() throws SAXException {
			query = null;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// create tag name
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("Filter")) {
				// set filter state
				inFilter = true;
			} else if (tagName.equalsIgnoreCase("EngineState")) {
				// set engine state
				inEngineState = true;
			} else if (tagName.equalsIgnoreCase("RecommendationQuery")) {
				// create new recommendation query instance
				String id = attributes.getValue("id");
				String processId = attributes.getValue("processId");
				String processInstanceId = attributes
						.getValue("processInstanceId");
				query = new RecommendationQuery(id, processId,
						processInstanceId);
			} else if (tagName.equalsIgnoreCase("AuditTrailEntry")) {
				// create new audit trail entry instance
				currentAuditTrailEntry = new AuditTrailEntryImpl();
			} else if (tagName.equalsIgnoreCase("LogEvent")) {
				// new LogEvent
				currentLogEventTask = "";
			} else if (tagName.equalsIgnoreCase("Attribute")) {
				// store current attribute key
				currentAttributeKey = attributes.getValue("name");
			} else if (tagName.equalsIgnoreCase("EventType")
					&& currentAuditTrailEntry != null) {
				// probe for unknown event type
				String unknownType = attributes.getValue("unknowntype");
				if (unknownType != null) {
					currentAuditTrailEntry.setType(unknownType);
				}
			}
			// reset buffer
			buffer.delete(0, buffer.length());
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// create tag name
			String tagName = localName;
			if (tagName.equalsIgnoreCase("")) {
				tagName = qName;
			}
			if (tagName.equalsIgnoreCase("Filter")) {
				// leave filter state
				inFilter = false;
			} else if (tagName.equalsIgnoreCase("EngineState")) {
				// leave engine state
				inEngineState = false;
			} else if (tagName.equalsIgnoreCase("AuditTrailEntry")) {
				// audit trail entry completed; add to query's audit trail
				query.addAuditTrailEntry(currentAuditTrailEntry);
				currentAuditTrailEntry = null;
			} else if (tagName.equalsIgnoreCase("Attribute")) {
				// add attribute to respective entity
				if (inEngineState == true) {
					query.setProcessInstanceAttribute(currentAttributeKey,
							buffer.toString().trim());
				} else if (currentAuditTrailEntry != null) {
					currentAuditTrailEntry.setAttribute(currentAttributeKey,
							buffer.toString().trim());
				}
				currentAttributeKey = null;
			} else if (tagName.equalsIgnoreCase("task")) {
				currentLogEventTask = buffer.toString().trim();
			} else if (tagName.equalsIgnoreCase("event")) {
				if (inFilter == true) {
					query.addFilterTask(currentLogEventTask, buffer.toString()
							.trim());
				} else if (inEngineState == true) {
					query.addAvailableTask(currentLogEventTask, buffer
							.toString().trim());
				}
			} else if (tagName.equalsIgnoreCase("User")) {
				// add user to respective entity
				if (inFilter == true) {
					query.addFilterUser(buffer.toString().trim());
				} else if (inEngineState == true) {
					query.addAvailableUser(buffer.toString().trim());
				}
			} else if (tagName.equalsIgnoreCase("Role")) {
				// add role to filter
				if (inFilter == true) {
					query.addFilterRole(buffer.toString().trim());
				}
			} else if (tagName.equalsIgnoreCase("Group")) {
				// add group to filter
				if (inFilter == true) {
					query.addFilterGroup(buffer.toString().trim());
				}
			} else if (tagName.equalsIgnoreCase("WorkflowModelElement")
					&& currentAuditTrailEntry != null) {
				// set current ATE's WFME
				currentAuditTrailEntry.setElement(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("Originator")
					&& currentAuditTrailEntry != null) {
				// set current ATE's originator
				currentAuditTrailEntry.setOriginator(buffer.toString().trim());
			} else if (tagName.equalsIgnoreCase("EventType")
					&& currentAuditTrailEntry != null) {
				// set current ATE's event type
				String currentType = currentAuditTrailEntry.getType();
				if (currentType == null || currentType.length() == 0) {
					// no unknown type previously set, set type
					currentAuditTrailEntry.setType(buffer.toString().trim());
				}
			} else if (tagName.equalsIgnoreCase("Timestamp")
					&& currentAuditTrailEntry != null) {
				// set current ATE's timestamp
				String tsString = buffer.toString().trim();
				Date timestamp = XmlUtils.parseXsDateTime(tsString);
				if (timestamp != null) {
					currentAuditTrailEntry.setTimestamp(timestamp);
				}
			}
			// reset buffer
			buffer.delete(0, buffer.length());
		}

		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			buffer.append(arg0, arg1, arg2);
		}

		public void error(SAXParseException arg0) throws SAXException {
			System.err
					.println("Error parsing recommendation query (unmarshalling):");
			arg0.printStackTrace();
		}

		public void fatalError(SAXParseException arg0) throws SAXException {
			System.err
					.println("Fatal error parsing recommendation query (unmarshalling):");
			arg0.printStackTrace();
			query = null;
		}

	}

}

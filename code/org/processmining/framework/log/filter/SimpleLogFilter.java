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
package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SimpleLogFilter extends LogFilter {

	protected HashSet<String> processes;
	protected HashSet<String> removeTypes;
	protected HashSet<String> skipInstanceTypes;
	protected HashSet<LogEvent> filterEvents;
	protected HashSet<LogEvent> startEvents;
	protected HashSet<LogEvent> endEvents;

	/**
	 * @param load
	 * @param name
	 */
	public SimpleLogFilter() {
		super(LogFilter.FAST, "Simple Log Filter");
		processes = new HashSet<String>();
		removeTypes = new HashSet<String>();
		skipInstanceTypes = new HashSet<String>();
		filterEvents = new HashSet<LogEvent>();
		startEvents = new HashSet<LogEvent>();
		endEvents = new HashSet<LogEvent>();
	}

	public SimpleLogFilter(SimpleLogFilter template) {
		super(LogFilter.FAST, "Simple Log Filter");
		processes = new HashSet<String>(template.processes);
		removeTypes = new HashSet<String>(template.removeTypes);
		skipInstanceTypes = new HashSet<String>(template.skipInstanceTypes);
		filterEvents = new HashSet<LogEvent>(template.filterEvents);
		startEvents = new HashSet<LogEvent>(template.startEvents);
		endEvents = new HashSet<LogEvent>(template.endEvents);
	}

	public void setProcesses(String[] processes) {
		this.processes = new HashSet<String>();
		for (String process : processes) {
			this.processes.add(process);
		}
	}

	public void setRemoveTypes(String[] eventTypes) {
		this.removeTypes = new HashSet<String>();
		for (String type : eventTypes) {
			this.removeTypes.add(type);
		}
	}

	public void setSkipInstanceTypes(String[] eventTypes) {
		this.skipInstanceTypes = new HashSet<String>();
		for (String type : eventTypes) {
			this.skipInstanceTypes.add(type);
		}
	}

	public void setFilterEvents(LogEvent[] filterEvents) {
		this.filterEvents = new HashSet<LogEvent>();
		for (LogEvent event : filterEvents) {
			this.filterEvents.add(event);
		}
	}

	public void setStartEvents(LogEvent[] startEvents) {
		this.startEvents = new HashSet<LogEvent>();
		for (LogEvent event : startEvents) {
			this.startEvents.add(event);
		}
	}

	public void setEndEvents(LogEvent[] endEvents) {
		this.endEvents = new HashSet<LogEvent>();
		for (LogEvent event : endEvents) {
			this.endEvents.add(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#doFiltering(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	@Override
	protected boolean doFiltering(ProcessInstance instance) {
		try {
			if (processes.contains(instance.getProcess())) {
				// process OK
				AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
				if (ateList.size() == 0) {
					return false;
				} else {
					// valid list of audit trail entries, check first event
					AuditTrailEntry first = ateList.get(0);
					LogEvent eFirst = new LogEvent(first.getElement(), first
							.getType());
					if (startEvents.contains(eFirst) == false) {
						return false;
					}
					// check last event
					AuditTrailEntry last = ateList.get(ateList.size() - 1);
					LogEvent eLast = new LogEvent(last.getElement(), last
							.getType());
					if (endEvents.contains(eLast) == false) {
						return false;
					}
					// first and final event are okay, filter list
					AuditTrailEntry currentAte;
					LogEvent currentEvent;
					for (int i = 0; i < ateList.size(); i++) {
						currentAte = ateList.get(i);
						currentEvent = new LogEvent(currentAte.getElement(),
								currentAte.getType());
						if (filterEvents.contains(currentEvent) == false) {
							ateList.remove(i);
							i--; // correct index (will be incremented in next
							// iteration)
						} else if (removeTypes.contains(currentAte.getType()) == true) {
							ateList.remove(i);
							i--; // correct index (will be incremented in next
							// iteration)
						} else if (skipInstanceTypes.contains(currentAte
								.getType()) == true) {
							// instance is skipped
							return false;
						}
						// if not, all is ok, the event passes the filter
					}
					// the return value will depend on whether there are any
					// events
					// remaining in this instance after filtering
					return (ateList.size() > 0);
				}
			} else {
				// process filtered
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#getHelpForThisLogFilter()
	 */
	@Override
	protected String getHelpForThisLogFilter() {
		return "no help available (this is the *simple* filter, man!)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#getParameterDialog(org.
	 * processmining.framework.log.LogSummary)
	 */
	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		// no dialog here
		return new LogFilterParameterDialog(summary, SimpleLogFilter.this) {
			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new SimpleLogFilter();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#thisFilterChangesLog()
	 */
	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#writeSpecificXML(java.io.
	 * BufferedWriter)
	 */
	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		for (String process : this.processes) {
			output.write("<process name=\"" + process + "\"/>\n");
		}
		for (String type : this.removeTypes) {
			output.write("<removeType type=\"" + type + "\"/>\n");
		}
		for (String type : this.skipInstanceTypes) {
			output.write("<skipInstanceType type=\"" + type + "\"/>\n");
		}
		for (LogEvent event : this.filterEvents) {
			output.write("<filterEvent element=\""
					+ event.getModelElementName() + "\" type=\""
					+ event.getEventType() + "\"/>\n");
		}
		for (LogEvent event : this.startEvents) {
			output.write("<startEvent element=\"" + event.getModelElementName()
					+ "\" type=\"" + event.getEventType() + "\"/>\n");
		}
		for (LogEvent event : this.endEvents) {
			output.write("<endEvent element=\"" + event.getModelElementName()
					+ "\" type=\"" + event.getEventType() + "\"/>\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#readSpecificXML(org.w3c.dom
	 * .Node)
	 */
	@Override
	protected void readSpecificXML(Node logFilterSpecificNode)
			throws IOException {
		// initialize
		processes = new HashSet<String>();
		removeTypes = new HashSet<String>();
		skipInstanceTypes = new HashSet<String>();
		filterEvents = new HashSet<LogEvent>();
		startEvents = new HashSet<LogEvent>();
		endEvents = new HashSet<LogEvent>();
		// read data
		Node node = null;
		for (int i = 0; i < logFilterSpecificNode.getChildNodes().getLength(); i++) {
			node = logFilterSpecificNode.getChildNodes().item(i);
			if (node.getNodeName().equalsIgnoreCase("process") == true) {
				Node name = node.getAttributes().getNamedItem("name");
				if (name != null) {
					processes.add(name.getNodeValue().trim());
				}
			} else if (node.getNodeName().equalsIgnoreCase("removeType") == true) {
				Node type = node.getAttributes().getNamedItem("type");
				if (type != null) {
					removeTypes.add(type.getNodeValue().trim());
				}
			} else if (node.getNodeName().equalsIgnoreCase("skipInstanceType") == true) {
				Node type = node.getAttributes().getNamedItem("type");
				if (type != null) {
					skipInstanceTypes.add(type.getNodeValue().trim());
				}
			} else if (node.getNodeName().equalsIgnoreCase("filterEvent") == true) {
				Node element = node.getAttributes().getNamedItem("element");
				Node type = node.getAttributes().getNamedItem("type");
				if (element != null && type != null) {
					LogEvent event = new LogEvent(
							element.getNodeValue().trim(), type.getNodeValue()
									.trim());
					filterEvents.add(event);
				}
			} else if (node.getNodeName().equalsIgnoreCase("startEvent") == true) {
				Node element = node.getAttributes().getNamedItem("element");
				Node type = node.getAttributes().getNamedItem("type");
				if (element != null && type != null) {
					LogEvent event = new LogEvent(
							element.getNodeValue().trim(), type.getNodeValue()
									.trim());
					startEvents.add(event);
				}
			} else if (node.getNodeName().equalsIgnoreCase("endEvent") == true) {
				Node element = node.getAttributes().getNamedItem("element");
				Node type = node.getAttributes().getNamedItem("type");
				if (element != null && type != null) {
					LogEvent event = new LogEvent(
							element.getNodeValue().trim(), type.getNodeValue()
									.trim());
					endEvents.add(event);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleLogFilter) {
			SimpleLogFilter other = (SimpleLogFilter) obj;
			if (this.processes.equals(other.processes) == false) {
				return false;
			} else if (this.startEvents.equals(other.startEvents) == false) {
				return false;
			} else if (this.endEvents.equals(other.endEvents) == false) {
				return false;
			} else if (this.filterEvents.equals(other.filterEvents) == false) {
				return false;
			} else if (this.removeTypes.equals(other.removeTypes) == false) {
				return false;
			} else if (this.skipInstanceTypes.equals(other.skipInstanceTypes) == false) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}

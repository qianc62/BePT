package org.processmining.analysis.sequenceclustering;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.*;
import org.w3c.dom.Node;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class SCLogFilter extends LogFilter {

	protected HashSet<String> removeTypes;
	protected HashSet<LogEvent> filterEvents;
	protected ArrayList<String> filterSequences;
	protected int minSequenceSize, maxSequenceSize, flag;

	public SCLogFilter() {
		super(LogFilter.FAST, "Sequence Clustering Log Filter");
		removeTypes = new HashSet<String>();
		filterEvents = new HashSet<LogEvent>();
		filterSequences = new ArrayList<String>();

		minSequenceSize = 0;
		maxSequenceSize = 0;
	}

	public SCLogFilter(SCLogFilter template) {
		super(LogFilter.FAST, "SequenceClustering Log Filter");
		removeTypes = new HashSet<String>(template.removeTypes);
		filterEvents = new HashSet<LogEvent>(template.filterEvents);
		filterSequences = new ArrayList<String>(template.filterSequences);

		minSequenceSize = template.minSequenceSize;
		maxSequenceSize = template.maxSequenceSize;
	}

	public void setRemoveTypes(String[] eventTypes) {
		this.removeTypes = new HashSet<String>();
		for (String type : eventTypes) {
			this.removeTypes.add(type);
		}
	}

	public void setFilterEvents(LogEvent[] filterEvents) {
		this.filterEvents = new HashSet<LogEvent>();
		for (LogEvent event : filterEvents) {
			this.filterEvents.add(event);
		}
	}

	public void setFilterSequences(String[] filterSequences) {
		this.filterSequences = new ArrayList<String>();
		for (String sequence : filterSequences) {
			this.filterSequences.add(sequence);
		}
	}

	public void setMinSequenceSize(int minSize) {
		this.minSequenceSize = minSize;
	}

	public void setMaxSequenceSize(int maxSize) {
		this.maxSequenceSize = maxSize;
	}

	public void setFlag(int flag) {
		this.flag = flag;
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
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			if (ateList.size() == 0) {
				return false;
			} else {

				AuditTrailEntry currentAte;
				LogEvent currentEvent;

				if (flag == 0) {

					for (int i = 0; i < ateList.size(); i++) {

						currentAte = ateList.get(i);
						currentEvent = new LogEvent(currentAte.getElement(),
								currentAte.getType());

						if (!(currentAte.getType().equals("complete"))) {
							ateList.remove(i);
							if (i < 1)
								i--;
							else
								i = i - 2;
						} else if (filterEvents.contains(currentEvent) == true) {
							ateList.remove(i);
							if (i < 1)
								i--;
							else
								i = i - 2;
						} else if ((i < ateList.size() - 1 && currentAte
								.getElement().equals(
										ateList.get(i + 1).getElement()))
								&& (i < ateList.size() - 1 && currentAte
										.getType().equals(
												ateList.get(i + 1).getType()))) {
							ateList.remove(i + 1);
							i--;
						}
					}

					if (ateList.size() < minSequenceSize
							|| ateList.size() > maxSequenceSize) {
						return false;
					}

					if (filterSequences.contains(instance.getName()) == true) {
						return false;
					}
				} else if (flag == 1) {

					for (int i = 0; i < ateList.size(); i++) {
						currentAte = ateList.get(i);
						currentEvent = new LogEvent(currentAte.getElement(),
								currentAte.getType());

						if (!(currentAte.getType().equals("complete"))) {
							ateList.remove(i);
							if (i < 1)
								i--;
							else
								i = i - 2;
						} else if (filterEvents.contains(currentEvent) == true) {
							ateList.remove(i);
							if (i < 1)
								i--;
							else
								i = i - 2;
						} else if ((i < ateList.size() - 1 && currentAte
								.getElement().equals(
										ateList.get(i + 1).getElement()))
								&& (i < ateList.size() - 1 && currentAte
										.getType().equals(
												ateList.get(i + 1).getType()))) {
							ateList.remove(i + 1);
							i--;
						}
					}

					if (filterSequences.contains(instance.getName()) != true) {
						return false;
					}

				} else if (flag == 2) {

					for (int i = 0; i < ateList.size(); i++) {
						currentAte = ateList.get(i);
						currentEvent = new LogEvent(currentAte.getElement(),
								currentAte.getType());

						if (!(currentAte.getType().equals("complete"))) {
							ateList.remove(i);
							if (i < 1)
								i--;
							else
								i = i - 2;
						}
					}

					if (filterSequences.contains(instance.getName()) != true) {
						return false;
					}

				}
				// the return value will depend on whether there are any events
				// remaining in this instance after filtering
				return (ateList.size() > 0);
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
		return "no help available";
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
		return new LogFilterParameterDialog(summary, SCLogFilter.this) {
			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new SCLogFilter();
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
		for (String type : this.removeTypes) {
			output.write("<removeType type=\"" + type + "\"/>\n");
		}
		for (LogEvent event : this.filterEvents) {
			output.write("<filterEvent element=\""
					+ event.getModelElementName() + "\" type=\""
					+ event.getEventType() + "\"/>\n");
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
		removeTypes = new HashSet<String>();
		filterEvents = new HashSet<LogEvent>();
		// read data
		Node node = null;
		for (int i = 0; i < logFilterSpecificNode.getChildNodes().getLength(); i++) {
			node = logFilterSpecificNode.getChildNodes().item(i);

			if (node.getNodeName().equalsIgnoreCase("removeType") == true) {
				Node type = node.getAttributes().getNamedItem("type");
				if (type != null) {
					removeTypes.add(type.getNodeValue().trim());
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
		if (obj instanceof SCLogFilter) {
			SCLogFilter other = (SCLogFilter) obj;
			if (this.filterEvents.equals(other.filterEvents) == false) {
				return false;
			} else if (this.removeTypes.equals(other.removeTypes) == false) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

}

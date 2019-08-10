package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * This filter retains only the first occurrence of any activity in the process
 * instance
 * 
 * @author (Idea Proposed by: Prof. Wil van der Aaalst; Implemented by: jcbose
 *         (R. P. Jagadeesh Chandra 'JC' Bose))
 */

public class FirstOnlyActivityFilter extends LogFilter {
	protected LogEvents eventsToFilter;

	public FirstOnlyActivityFilter() {
		super(LogFilter.MODERATE, "First Occurrence Only Activity Filter");
	}

	public FirstOnlyActivityFilter(LogEvents eventsToFilter) {
		super(LogFilter.MODERATE, "First Occurrence Only Activity Filter");
		this.eventsToFilter = eventsToFilter;
	}

	protected String getHelpForThisLogFilter() {
		return "Retains only the first occurrence of the activity in a process instance";
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	protected boolean doFiltering(ProcessInstance instance) {
		assert (!instance.isEmpty());

		// System.out.println("Events to Filter Size: "+eventsToFilter.size());
		// for(LogEvent e : eventsToFilter){
		// System.out.println(e.getModelElementName());
		// }

		AuditTrailEntryList entries = instance.getAuditTrailEntryList();
		if (entries.size() <= 1) {
			return true;
		}

		int initialSize = entries.size();
		HashSet<String> activitiesEncountered = new HashSet<String>();
		Iterator<AuditTrailEntry> it = entries.iterator();
		String currentActivity;
		AuditTrailEntry currentAuditTrailEntry;

		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			currentActivity = currentAuditTrailEntry.getElement();
			LogEvent e = eventsToFilter.findLogEvent(currentActivity,
					currentAuditTrailEntry.getType());
			if (e != null && activitiesEncountered.contains(currentActivity)) {
				// System.out.println("AlreadyEncountered");
				it.remove();
			} else {
				activitiesEncountered.add(currentActivity);
			}
		}
		// System.out.println(initialSize+" @ "+entries.size());
		return !instance.isEmpty();
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				FirstOnlyActivityFilter.this) {
			LogEventCheckBox[] checks;

			public LogFilter getNewLogFilter() {
				LogEvents e = new LogEvents();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						e.add(checks[i].getLogEvent());
					}
				}
				return new FirstOnlyActivityFilter(e);
			}

			protected JPanel getPanel() {
				int size = summary.getLogEvents().size();
				checks = new LogEventCheckBox[size];
				JPanel p = new JPanel(new BorderLayout());
				// JPanel p = new JPanel(new SpringLayout());
				JPanel p1 = new JPanel(new GridLayout(size, 1));

				JButton deSelectAllButton = new JButton("  DeSelect All  ");
				deSelectAllButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (int i = 0; i < checks.length; i++) {
							if ((eventsToFilter != null)
									&& eventsToFilter.contains(checks[i]
											.getLogEvent()))
								eventsToFilter.remove(checks[i].getLogEvent());
							checks[i].setSelected(false);
						}
						setVisible(false);
					}
				});

				JButton selectAllButton = new JButton("  Select All  ");
				selectAllButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (int i = 0; i < checks.length; i++) {
							if ((eventsToFilter != null)
									&& (!eventsToFilter.contains(checks[i]
											.getLogEvent())))
								eventsToFilter.add(checks[i].getLogEvent());
							checks[i].setSelected(true);
						}
						setVisible(false);
					}
				});

				JPanel selectDeselectPanel = new JPanel();
				selectDeselectPanel.add(deSelectAllButton);
				selectDeselectPanel.add(selectAllButton);
				p.add(selectDeselectPanel, BorderLayout.NORTH);

				Iterator it = summary.getLogEvents().iterator();
				int i = 0;
				while (it.hasNext()) {
					checks[i++] = new LogEventCheckBox((LogEvent) it.next());
				}
				Arrays.sort(checks);
				for (i = 0; i < checks.length; i++) {
					p1.add(checks[i]);
					if ((eventsToFilter != null)
							&& (!eventsToFilter.contains(checks[i]
									.getLogEvent()))) {
						checks[i].setSelected(false);
					}
				}
				p.add(p1, BorderLayout.SOUTH);

				return p;
			}

			protected boolean getAllParametersSet() {
				return true;
			}
		};
	}

	/**
	 * Method to tell whether this LogFilter changes the log or not.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method. False otherwise.
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// do nothing

	}

	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// do nothing
	}

}

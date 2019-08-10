package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

/**
 * This filter retains only the last occurrence of any activity in the process
 * instance
 * 
 * @author (Idea Proposed by: Prof. Wil van der Aaalst; Implemented by: jcbose
 *         (R. P. Jagadeesh Chandra 'JC' Bose))
 */
public class LastOnlyActivityFilter extends LogFilter {
	protected LogEvents eventsToFilter;

	public LastOnlyActivityFilter() {
		super(LogFilter.MODERATE, "Last Occurrence Only Activity Filter");
	}

	public LastOnlyActivityFilter(LogEvents eventsToFilter) {
		super(LogFilter.MODERATE, "Last Occurrence Only Activity Filter");
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

		AuditTrailEntryList entries = instance.getAuditTrailEntryList();
		if (entries.size() <= 1) {
			return true;
		}

		int initialSize = entries.size();

		HashMap<String, Integer> activityLastOccurrenceMap = new HashMap<String, Integer>();
		Iterator<AuditTrailEntry> it = entries.iterator();
		AuditTrailEntry currentAuditTrailEntry;
		String currentActivity;
		int currentIndex = 0;
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			currentActivity = currentAuditTrailEntry.getElement();
			activityLastOccurrenceMap.put(currentActivity, currentIndex);
			currentIndex++;
		}

		// Iterator<String> it2 = activityLastOccurrenceMap.keySet().iterator();
		currentIndex = 0;
		it = entries.iterator();
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			LogEvent e = eventsToFilter.findLogEvent(currentAuditTrailEntry
					.getElement(), currentAuditTrailEntry.getType());
			if (e != null
					&& !activityLastOccurrenceMap.containsValue(currentIndex)) {
				it.remove();
			}

			currentIndex++;
		}

		// System.out.println(initialSize+" @ "+entries.size());
		// System.out.println(activityLastOccurrenceMap.values().toString());
		return !instance.isEmpty();
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				LastOnlyActivityFilter.this) {
			LogEventCheckBox[] checks;

			protected boolean getAllParametersSet() {
				return true;
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

			public LogFilter getNewLogFilter() {
				LogEvents e = new LogEvents();
				for (int i = 0; i < checks.length; i++) {
					if (checks[i].isSelected()) {
						e.add(checks[i].getLogEvent());
					}
				}

				return new LastOnlyActivityFilter(e);
			}
		};
	}

	protected boolean thisFilterChangesLog() {
		return true;
	}

	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// do nothing
	}

	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// do nothing
	}

}

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
 * This filter retains only the first and last occurrence of any activity in the
 * process instance
 * 
 * @author (Idea Proposed by: Prof. Wil van der Aaalst; Implemented by: jcbose
 *         (R. P. Jagadeesh Chandra 'JC' Bose))
 */

public class FirstLastActivityFilter extends LogFilter {
	protected LogEvents eventsToFilter;

	public FirstLastActivityFilter() {
		super(LogFilter.MODERATE, "First and Last Occurrence Activity Filter");
	}

	public FirstLastActivityFilter(LogEvents eventsToFilter) {
		super(LogFilter.MODERATE, "First and Last Occurrence Activity Filter");
		this.eventsToFilter = eventsToFilter;
	}

	protected String getHelpForThisLogFilter() {
		return "This filter retains the first and last occurrence of an activity and removes all the intermediate occurrences; For activities that occur only once in a trace, this retains the activity as is";
	}

	protected boolean doFiltering(ProcessInstance instance) {
		assert (!instance.isEmpty());

		AuditTrailEntryList entries = instance.getAuditTrailEntryList();
		if (entries.size() <= 1) {
			return true;
		}
		int initialSize = entries.size();

		HashMap<String, Integer> activityFirstOccurrenceMap = new HashMap<String, Integer>();
		HashMap<String, Integer> activityLastOccurrenceMap = new HashMap<String, Integer>();

		Iterator<AuditTrailEntry> it = entries.iterator();
		AuditTrailEntry currentAuditTrailEntry;
		String currentActivity;
		int currentIndex = 0;
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			currentActivity = currentAuditTrailEntry.getElement();
			if (!activityFirstOccurrenceMap.containsKey(currentActivity)) {
				activityFirstOccurrenceMap.put(currentActivity, currentIndex);
			}
			activityLastOccurrenceMap.put(currentActivity, currentIndex);
			currentIndex++;
		}

		it = entries.iterator();
		currentIndex = 0;
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			LogEvent e = eventsToFilter.findLogEvent(currentAuditTrailEntry
					.getElement(), currentAuditTrailEntry.getType());
			if (e != null
					&& !activityFirstOccurrenceMap.containsValue(currentIndex)
					&& !activityLastOccurrenceMap.containsValue(currentIndex)) {
				it.remove();
			} else if (e != null
					&& activityFirstOccurrenceMap.containsValue(currentIndex)
					&& activityLastOccurrenceMap.containsValue(currentIndex)) {
				// only one occurrence of the activity; change the type name to
				// start/complete
				currentAuditTrailEntry.setType("Start/Complete");
			} else if (e != null
					&& activityFirstOccurrenceMap.containsValue(currentIndex)) {
				currentAuditTrailEntry.setType("Start");
			} else if (e != null
					&& activityLastOccurrenceMap.containsValue(currentIndex)) {
				currentAuditTrailEntry.setType("Complete");
			}
			currentIndex++;
		}
		// System.out.println(initialSize+ " @ "+entries.size());
		return !instance.isEmpty();
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				FirstLastActivityFilter.this) {
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
				return new FirstLastActivityFilter(e);
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

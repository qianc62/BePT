package org.processmining.analysis.originatoravailability;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.ComparablePair;

public class OriginatorAvailability {

	private Map<String, Set<ComparablePair<Date, Date>>> shiftsPerOriginator;
	private Set<ComparablePair<Date, Date>> shifts;

	public OriginatorAvailability(LogReader log, ShiftTimeIntervals intervals) {
		shiftsPerOriginator = new HashMap<String, Set<ComparablePair<Date, Date>>>();
		shifts = new TreeSet<ComparablePair<Date, Date>>();
		for (ProcessInstance pi : log.getInstances()) {
			for (AuditTrailEntry ate : pi.getListOfATEs()) {
				ComparablePair<Date, Date> shift = intervals.getShift(ate
						.getTimestamp());
				shifts.add(shift);
				add(ate.getOriginator(), shift);
			}
		}
	}

	public void add(String originator, ComparablePair<Date, Date> shift) {
		if (!shiftsPerOriginator.containsKey(originator)) {
			shiftsPerOriginator.put(originator,
					new TreeSet<ComparablePair<Date, Date>>());
		}
		shiftsPerOriginator.get(originator).add(shift);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();

		for (Map.Entry<String, Set<ComparablePair<Date, Date>>> shift : shiftsPerOriginator
				.entrySet()) {
			result.append("ORIGINATOR: " + shift.getKey() + "\n");
			for (ComparablePair<Date, Date> window : shift.getValue()) {
				result.append("  " + window.toString() + "\n");
			}
		}
		return result.toString();
	}

	public TableModel toTableModel() {

		DefaultTableModel tableShiftsByOriginator = new DefaultTableModel();
		ComparablePair<Date, Date>[] columnWithAllShifts = new ComparablePair[shifts
				.size()];
		columnWithAllShifts = shifts.toArray(columnWithAllShifts);

		tableShiftsByOriginator.addColumn("Shifts", columnWithAllShifts);
		for (String originator : shiftsPerOriginator.keySet()) {
			Boolean[] columnWithShiftsPerOriginator = new Boolean[columnWithAllShifts.length];
			Set<ComparablePair<Date, Date>> shiftsForThisOriginator = shiftsPerOriginator
					.get(originator);
			for (int i = 0; i < columnWithAllShifts.length; i++) {
				if (shiftsForThisOriginator.contains(columnWithAllShifts[i])) {
					columnWithShiftsPerOriginator[i] = true;
				} else {
					columnWithShiftsPerOriginator[i] = false;
				}
			}
			tableShiftsByOriginator.addColumn(originator,
					columnWithShiftsPerOriginator);

		}

		return tableShiftsByOriginator;

	}

	public Map<String, Set<ComparablePair<Date, Date>>> getData() {
		return shiftsPerOriginator;
	}
}

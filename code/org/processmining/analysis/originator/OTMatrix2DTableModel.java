package org.processmining.analysis.originator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class OTMatrix2DTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7120313343982500402L;

	private List<String> originators = new ArrayList<String>();
	private List<String> tasks = new ArrayList<String>();
	private DoubleMatrix2D otMatrix = null;
	private LogReader log = null;

	public OTMatrix2DTableModel(LogReader log) {
		this.log = log;
	}

	protected void init() {
		if (otMatrix != null)
			return;
		List<String> allTasks = new ArrayList<String>();
		List<String> allOrigs = new ArrayList<String>();

		getOriginatorsAndTasks(log, allOrigs, allTasks);

		originators.addAll(allOrigs);
		tasks.addAll(allTasks);

		Collections.sort(originators);
		Collections.sort(tasks);

		otMatrix = makeOTMatrix(log);
	}

	protected void getOriginatorsAndTasks(LogReader log,
			List<String> originatorList, List<String> taskList) {
		LogSummary summary = log.getLogSummary();

		originatorList.addAll(Arrays.asList(summary.getOriginators()));
		taskList.addAll(Arrays.asList(summary.getModelElements()));
	}

	protected void getOriginators(AuditTrailEntry ate, List<String> result) {
		result.add(ate.getOriginator());
	}

	protected void getTasks(AuditTrailEntry ate, List<String> result) {
		result.add(ate.getElement());
	}

	public DoubleMatrix2D getOTMatrix() {
		if (otMatrix == null)
			init();
		return otMatrix;
	}

	public String getColumnName(int col) {
		if (otMatrix == null)
			init();
		return col == 0 ? "originator" : tasks.get(col - 1);
	}

	public String[] getUsers() {
		if (otMatrix == null)
			init();
		return (String[]) originators.toArray(new String[0]);
	}

	public String[] getTasks() {
		if (otMatrix == null)
			init();
		return (String[]) tasks.toArray(new String[0]);
	}

	public int getRowCount() {
		if (otMatrix == null)
			init();
		return originators.size();
	}

	public int getColumnCount() {
		if (otMatrix == null)
			init();
		return tasks.size() + 1;
	}

	public Object getValueAt(int row, int col) {
		if (otMatrix == null)
			init();
		return col == 0 ? originators.get(row) : String.valueOf(otMatrix.get(
				row, col - 1));
	}

	private DoubleMatrix2D makeOTMatrix(LogReader log) {
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(originators.size(),
				tasks.size(), 0);
		Iterator it = log.instanceIterator();
		List<String> ateTasks = new ArrayList<String>();
		List<String> ateOrigs = new ArrayList<String>();

		Map<String, Integer> taskMap = buildMap(tasks);
		Map<String, Integer> origMap = buildMap(originators);

		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();

				ateTasks.clear();
				ateOrigs.clear();
				getTasks(ate, ateTasks);
				getOriginators(ate, ateOrigs);

				for (String orig : ateOrigs) {
					if (orig != null) {
						int row = origMap.get(orig);

						for (String task : ateTasks) {
							if (task != null) {
								int column = taskMap.get(task);

								D.set(row, column, D.get(row, column) + 1.0);
							}
						}
					}
				}
			}
		}
		return D;
	}

	private Map<String, Integer> buildMap(List<String> items) {
		Map<String, Integer> mapping = new HashMap<String, Integer>();

		for (int i = 0; i < items.size(); i++) {
			mapping.put(items.get(i), i);
		}
		return mapping;
	}

	public DoubleMatrix2D getFilteredOTMatrix(String[] filteredUsers) {
		if (otMatrix == null)
			init();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(filteredUsers.length,
				tasks.size(), 0);

		ArrayList<String> userArrayList = new ArrayList<String>(Arrays
				.asList(filteredUsers));

		int k = 0;
		for (int i = 0; i < originators.size(); i++) {
			if (userArrayList.contains(originators.get(i))) {
				for (int j = 0; j < tasks.size(); j++) {
					D.set(k, j, otMatrix.get(i, j));
				}
				k++;
			}
		}
		return D;
	}

	public int getSumOfOTMatrix() {
		if (otMatrix == null)
			init();
		return (int) Math.round(otMatrix.zSum());
	}

	public int getMaxElement() {
		if (otMatrix == null)
			init();
		int result = -1;
		for (int row = 0; row < otMatrix.rows(); row++) {
			for (int col = 0; col < otMatrix.columns(); col++) {
				result = Math.max(result, (int) Math.round(otMatrix.get(row,
						col)));
			}
		}
		return result;
	}

	public void writeToTestLog() {
		if (otMatrix == null)
			init();
		// / PLUGIN TEST START
		Message.add("<SummaryOfMatrix numberOfUsers=\"" + originators.size()
				+ "\" numberOfRows=\"" + String.valueOf(getRowCount()) + "\">",
				Message.TEST);
		Message.add("<SummaryOfMatrix numberOfTasks=\"" + tasks.size()
				+ "\" numberOfColumns=\""
				+ String.valueOf(getColumnCount() - 1) + "\">", Message.TEST);
		// PLUGIN TEST END
	}
}

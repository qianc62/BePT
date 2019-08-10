package org.processmining.analysis.originator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.processmining.analysis.orgsimilarity.SimilarityModel;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.ui.Message;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class Mismatch2DTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7120313343982500402L;

	private List<String> orgentities1 = new ArrayList<String>();
	private List<String> orgentities2 = new ArrayList<String>();
	private List<String> orgentities3 = new ArrayList<String>();
	private List<String> orgentities4 = new ArrayList<String>();

	private DoubleMatrix2D mismatchMatrix = null;
	private DoubleMatrix2D matchedMatrix = null;
	private SimilarityModel simModel = null;
	private Map<OrgEntity, OrgEntity> relations = null;

	public Mismatch2DTableModel(SimilarityModel simModel) {
		this.simModel = simModel;
		relations = simModel.getMapping();
	}

	protected void init() {
		if (mismatchMatrix != null)
			return;
		List<String> allTasks = new ArrayList<String>();
		List<String> allOrigs = new ArrayList<String>();
		OrgModel orgModel1 = simModel.getOrgModel(0);
		OrgModel orgModel2 = simModel.getOrgModel(1);
		for (OrgEntity entity : orgModel1.getOrgEntityList()) {
			orgentities3.add(entity.getName());
		}
		for (OrgEntity entity : orgModel2.getOrgEntityList()) {
			orgentities4.add(entity.getName());
		}
		orgentities1 = orgModel1.getOrgEntityStringList();
		orgentities2 = orgModel2.getOrgEntityStringList();

		mismatchMatrix = DoubleFactory2D.sparse.make(orgentities1.size() + 1,
				orgentities2.size() + 1, 0);
		matchedMatrix = DoubleFactory2D.sparse.make(orgentities1.size() + 1,
				orgentities2.size() + 1, 0);

		for (OrgEntity tempEntity : relations.keySet()) {
			OrgEntity targetEntity = relations.get(tempEntity);
			int num = 0;

			ArrayList<String> sourceRes = orgModel1.getResourceList(tempEntity);
			ArrayList<String> targetRes = orgModel2
					.getResourceList(targetEntity);
			for (String res : sourceRes) {
				if (targetRes.contains(res))
					num++;
			}
			int row = orgentities1.indexOf(tempEntity.getID());
			int col = orgentities2.indexOf(targetEntity.getID());

			mismatchMatrix.set(row, col, num);
			matchedMatrix.set(row, col, sourceRes.size());
			mismatchMatrix.set(row, orgentities2.size(), mismatchMatrix.get(
					row, orgentities2.size())
					+ num);
			matchedMatrix.set(row, orgentities2.size(), matchedMatrix.get(row,
					orgentities2.size())
					+ sourceRes.size());
			mismatchMatrix.set(orgentities1.size(), col, mismatchMatrix.get(
					orgentities1.size(), col)
					+ num);
			matchedMatrix.set(orgentities1.size(), col, matchedMatrix.get(
					orgentities1.size(), col)
					+ sourceRes.size());
			mismatchMatrix.set(orgentities1.size(), orgentities2.size(),
					mismatchMatrix
							.get(orgentities1.size(), orgentities2.size())
							+ num);
			matchedMatrix.set(orgentities1.size(), orgentities2.size(),
					matchedMatrix.get(orgentities1.size(), orgentities2.size())
							+ sourceRes.size());

		}
	}

	public DoubleMatrix2D getOTMatrix() {
		if (mismatchMatrix == null)
			init();
		return mismatchMatrix;
	}

	public String getColumnName(int col) {
		if (mismatchMatrix == null)
			init();
		if (col == 0) {
			return "Name";
		} else if (col == getColumnCount() - 1) {
			return "sum";
		} else {
			return col == 0 ? "originator" : orgentities4.get(col - 1);
		}
	}

	public String[] getUsers() {
		if (mismatchMatrix == null)
			init();
		return (String[]) orgentities3.toArray(new String[0]);
	}

	public String[] getTasks() {
		if (mismatchMatrix == null)
			init();
		return (String[]) orgentities4.toArray(new String[0]);
	}

	public int getRowCount() {
		if (mismatchMatrix == null)
			init();
		return orgentities1.size() + 1;
	}

	public int getColumnCount() {
		if (mismatchMatrix == null)
			init();
		return orgentities2.size() + 2;
	}

	public Object getValueAt(int row, int col) {
		if (mismatchMatrix == null)
			init();
		if (col == 0) {
			if (row == getRowCount() - 1) {
				return "sum";
			} else {
				return orgentities3.get(row);
			}
		} else {
			if (matchedMatrix.get(row, col - 1) == 0)
				return "";
			String result = String.valueOf((int) mismatchMatrix.get(row,
					col - 1))
					+ "/"
					+ String.valueOf((int) matchedMatrix.get(row, col - 1));
			return result;
		}
	}

	private Map<String, Integer> buildMap(List<String> items) {
		Map<String, Integer> mapping = new HashMap<String, Integer>();

		for (int i = 0; i < items.size(); i++) {
			mapping.put(items.get(i), i);
		}
		return mapping;
	}

	public DoubleMatrix2D getFilteredOTMatrix(String[] filteredUsers) {
		if (mismatchMatrix == null)
			init();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(filteredUsers.length,
				orgentities2.size(), 0);

		ArrayList<String> userArrayList = new ArrayList<String>(Arrays
				.asList(filteredUsers));

		int k = 0;
		for (int i = 0; i < orgentities1.size(); i++) {
			if (userArrayList.contains(orgentities1.get(i))) {
				for (int j = 0; j < orgentities2.size(); j++) {
					D.set(k, j, mismatchMatrix.get(i, j));
				}
				k++;
			}
		}
		return D;
	}

	public int getSumOfOTMatrix() {
		if (mismatchMatrix == null)
			init();
		return (int) Math.round(mismatchMatrix.zSum());
	}

	public int getMaxElement() {
		if (mismatchMatrix == null)
			init();
		int result = -1;
		for (int row = 0; row < mismatchMatrix.rows(); row++) {
			for (int col = 0; col < mismatchMatrix.columns(); col++) {
				result = Math.max(result, (int) Math.round(mismatchMatrix.get(
						row, col)));
			}
		}
		return result;
	}

	public void writeToTestLog() {
		if (mismatchMatrix == null)
			init();
		// / PLUGIN TEST START
		Message.add("<SummaryOfMatrix numberOfUsers=\"" + orgentities1.size()
				+ "\" numberOfRows=\"" + String.valueOf(getRowCount()) + "\">",
				Message.TEST);
		Message.add("<SummaryOfMatrix numberOfTasks=\"" + orgentities2.size()
				+ "\" numberOfColumns=\""
				+ String.valueOf(getColumnCount() - 1) + "\">", Message.TEST);
		// PLUGIN TEST END
	}
}

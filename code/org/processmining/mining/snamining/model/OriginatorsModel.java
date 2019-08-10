package org.processmining.mining.snamining.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.analysis.summary.ExtendedLogSummary;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class OriginatorsModel {

	private String[] sOriginators;
	private HashMap mapOriginators = new HashMap();
	private boolean[] boolSelected;
	private DoubleMatrix2D matrix = null;
	private OrgModel orgModel;

	public OriginatorsModel() {
	}

	public OriginatorsModel(LogSummary summary) {
		sOriginators = summary.getOriginators();

		if (summary instanceof ExtendedLogSummary) {
			mapOriginators = ((ExtendedLogSummary) summary).getOriginatorMap();
		} else {
			for (int i = 0; i < sOriginators.length; i++) {
				Map<LogEvent, Integer> eventOccurrenceMap = summary
						.getEventsForOriginator(sOriginators[i]);
				int counter = 0;
				for (Integer occurrences : eventOccurrenceMap.values()) {
					counter += occurrences;
				}
				mapOriginators.put(sOriginators[i], counter);
			}
		}

		boolSelected = new boolean[sOriginators.length];

		for (int i = 0; i < sOriginators.length; i++)
			boolSelected[i] = true;
	}

	public OriginatorsModel(String[] setOri, HashMap mapOri) {
		sOriginators = setOri;
		mapOriginators = mapOri;
	}

	// guaranteed to be alphabetically sorted, so binary search can be used
	public int getNumberofOriginators() {
		return sOriginators.length;
	}

	public String getOriginator(int i) {
		return sOriginators[i];
	}

	public DoubleMatrix2D getMatrix() {
		return matrix;
	}

	public void setMatrix(DoubleMatrix2D matrix) {
		this.matrix = matrix;
	}

	public OrgModel getOrgModel() {
		return this.orgModel;
	}

	public void setOrgModel(OrgModel orgModel) {
		this.orgModel = orgModel;
	}

	public String getFrequency(String org) {
		return mapOriginators.get(org).toString();
	}

	public int getMinFrequency() {
		int min = Integer.MAX_VALUE, temp;
		for (int i = 0; i < sOriginators.length; i++) {
			temp = Integer.parseInt(mapOriginators.get(sOriginators[i])
					.toString());
			min = (temp < min) ? temp : min;
		}
		return min;
	}

	public int getMaxFrequency() {
		int max = Integer.MIN_VALUE, temp;
		for (int i = 0; i < sOriginators.length; i++) {
			temp = Integer.parseInt(mapOriginators.get(sOriginators[i])
					.toString());
			max = (temp < max) ? max : temp;
		}
		return max;
	}

	public void selectAll() {
		for (int i = 0; i < sOriginators.length; i++)
			boolSelected[i] = true;
	}

	public void deselectAll() {
		for (int i = 0; i < sOriginators.length; i++)
			boolSelected[i] = false;
	}

	public void filterOriginator(int min, int max) {
		int freq;
		for (int i = 0; i < sOriginators.length; i++) {
			freq = Integer.parseInt(mapOriginators.get(sOriginators[i])
					.toString());
			if (freq >= min && freq <= max)
				boolSelected[i] = true;
			else
				boolSelected[i] = false;
		}
	}

	public boolean isSelected(int i) {
		return boolSelected[i];
	}

	public void changeSelect(int i) {
		boolSelected[i] = !(boolSelected[i]);
	}

	public int getNumberOfSelectedOriginators() {
		int num = 0;
		for (int i = 0; i < sOriginators.length; i++)
			if (boolSelected[i])
				num++;
		return num;
	}

	public String[] getSelectedOriginators() {
		String[] s = new String[getNumberOfSelectedOriginators()];
		int j = 0;
		for (int i = 0; i < sOriginators.length; i++) {
			if (boolSelected[i]) {
				s[j] = sOriginators[i].toString();
				j++;
			}
		}
		Arrays.sort(s);
		return s;
	}

	public DoubleMatrix2D filterResultMatrix() {
		DoubleMatrix2D m = DoubleFactory2D.sparse.make(
				getNumberOfSelectedOriginators(),
				getNumberOfSelectedOriginators(), 0);

		int k = 0;
		for (int i = 0; i < sOriginators.length; i++) {
			if (!boolSelected[i])
				continue;
			int l = 0;
			for (int j = 0; j < sOriginators.length; j++) {
				if (!boolSelected[j])
					continue;
				m.set(k, l, matrix.get(i, j));
				l++;
			}
			k++;
		}

		return m;
	}

	public void removeOriginators(DoubleMatrix2D matrix) {
		int k = 0;
		for (int i = 0; i < sOriginators.length; i++) {
			if (!boolSelected[i])
				continue;
			int l = 0;
			for (int j = 0; j < sOriginators.length; j++) {
				if (!boolSelected[j])
					continue;
				l++;
			}
			k++;
		}
	}

	// org/role table
	public ArrayList<String> getRoleList() {
		return orgModel.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ROLE);
	}

	public ArrayList<String> getOrgUnitList() {
		return orgModel.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ORGUNIT);
	}

	// selected originators
	public DoubleMatrix2D getRoleAssignmentMatrix() {
		DoubleMatrix2D roleMatrix = null;

		if (orgModel != null) {
			ArrayList<String> roleList = orgModel
					.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ROLE);
			// ArrayList<String> roleList = orgModel.getRoleStringList();
			roleMatrix = DoubleFactory2D.sparse.make(getNumberofOriginators(),
					roleList.size(), 0);
			String[] sOriginators = getSelectedOriginators();

			for (int i = 0; i < sOriginators.length; i++) {
				Resource res = orgModel.getResource(sOriginators[i]);
				if (res != null) {
					List<String> roleListforOriginator = orgModel
							.getOrgEntityList(res, OrgEntity.ORGENTITYTYPE_ROLE);
					Iterator it = roleListforOriginator.iterator();
					while (it.hasNext()) {
						String roleName = (String) it.next();
						roleMatrix.set(i, roleList.indexOf(roleName), 1.0);
					}

				}
			}
		}
		return roleMatrix;
	}

	public DoubleMatrix2D getOrgUnitAssignmentMatrix() {
		DoubleMatrix2D orgUnitMatrix = null;

		if (orgModel != null) {
			ArrayList<String> orgUnitList = orgModel
					.getOrgEntityStringList(OrgEntity.ORGENTITYTYPE_ORGUNIT);
			orgUnitMatrix = DoubleFactory2D.sparse.make(
					getNumberofOriginators(), orgUnitList.size(), 0);
			String[] sOriginators = getSelectedOriginators();

			for (int i = 0; i < sOriginators.length; i++) {
				Resource res = orgModel.getResource(sOriginators[i]);
				if (res != null) {
					List<String> orgUnitListforOriginator = orgModel
							.getOrgEntityList(res,
									OrgEntity.ORGENTITYTYPE_ORGUNIT);
					Iterator it = orgUnitListforOriginator.iterator();
					while (it.hasNext()) {
						String orgUnitName = (String) it.next();
						orgUnitMatrix.set(i, orgUnitList.indexOf(orgUnitName),
								1.0);
					}

				}
			}
		}
		return orgUnitMatrix;
	}
}

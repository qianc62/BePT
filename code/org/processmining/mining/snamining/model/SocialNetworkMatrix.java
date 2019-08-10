package org.processmining.mining.snamining.model;

import java.util.ArrayList;
import java.util.Arrays;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class SocialNetworkMatrix implements Cloneable {

	private String[] nodeNames = null;
	private DoubleMatrix2D matrix = null;

	private ArrayList<String> orgUnitNameList = null;
	private ArrayList<String> roleNameList = null;

	private DoubleMatrix2D orgUnitMatrix = null;
	private DoubleMatrix2D roleMatrix = null;
	private DoubleMatrix2D otMatrix = null;

	public SocialNetworkMatrix() {
	}

	public SocialNetworkMatrix(String[] nodeNames) {
		this.nodeNames = nodeNames;
	}

	public SocialNetworkMatrix(String[] nodeNames, DoubleMatrix2D matrix) {
		this.nodeNames = nodeNames;
		this.matrix = matrix;
	}

	public String[] getNodeNames() {
		return nodeNames;
	}

	public DoubleMatrix2D getMatrix() {
		return matrix;
	}

	public void setMatrix(DoubleMatrix2D matrix) {
		this.matrix = matrix;
	}

	public void setOrgUnitName(ArrayList<String> orgUnitNameList) {
		this.orgUnitNameList = orgUnitNameList;
	}

	public ArrayList<String> getOrgUnitName() {
		return orgUnitNameList;
	}

	public void setRoleName(ArrayList<String> roleNameList) {
		this.roleNameList = roleNameList;
	}

	public ArrayList<String> getRoleName() {
		return roleNameList;
	}

	public DoubleMatrix2D getOrgUnitMatrix() {
		return orgUnitMatrix;
	}

	public DoubleMatrix2D getRoleMatrix() {
		return roleMatrix;
	}

	public DoubleMatrix2D getOTMatrix() {
		return otMatrix;
	}

	public void setOrgUnitMatrix(DoubleMatrix2D matrix) {
		orgUnitMatrix = matrix;
	}

	public void setRoleMatrix(DoubleMatrix2D matrix) {
		roleMatrix = matrix;
	}

	public void setOTMatrix(DoubleMatrix2D matrix) {
		otMatrix = matrix;
	}

	public double getFrequency(String user) {
		Double d = 0.0;
		for (int i = 0; i < nodeNames.length; i++)
			if (nodeNames[i].endsWith(user))
				for (int j = 0; j < otMatrix.columns(); j++)
					d += otMatrix.get(i, j);
		return d;
	}

	public DoubleMatrix2D applyThresholdValue(double threshold) {
		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				if (matrix.get(i, j) < threshold)
					matrix.set(i, j, 0.0);
		return matrix;
	}

	public DoubleMatrix2D applyThresholdValueToTempMatrix(double threshold) {
		DoubleMatrix2D tempMatrix = DoubleFactory2D.sparse.make(matrix.rows(),
				matrix.columns());
		for (int i = 0; i < tempMatrix.rows(); i++)
			for (int j = 0; j < tempMatrix.columns(); j++)
				if (matrix.get(i, j) < threshold)
					tempMatrix.set(i, j, 0.0);
				else
					tempMatrix.set(i, j, matrix.get(i, j));
		return tempMatrix;
	}

	public void removeDisconnectedOriginator() {
		ArrayList<String> removedNodeNames = new ArrayList<String>();
		String[] newNodeNames = null;
		DoubleMatrix2D newMatrix = null;

		for (int i = 0; i < nodeNames.length; i++) {
			boolean flag = false;
			for (int j = 0; j < nodeNames.length; j++) {
				if ((matrix.get(i, j) != 0) || (matrix.get(j, i) != 0)) {
					flag = true;
					break;
				}
			}
			if (!flag)
				removedNodeNames.add(nodeNames[i]);
		}

		newNodeNames = new String[nodeNames.length - removedNodeNames.size()];
		newMatrix = DoubleFactory2D.sparse.make(newNodeNames.length,
				newNodeNames.length, 0);

		int k = -1;
		for (int i = 0; i < nodeNames.length; i++) {
			if (removedNodeNames.contains(nodeNames[i]))
				continue;
			k++;
			newNodeNames[k] = nodeNames[i];
			int l = -1;
			for (int j = 0; j < nodeNames.length; j++) {
				if (removedNodeNames.contains(nodeNames[j]))
					continue;
				l++;
				newMatrix.set(k, l, matrix.get(i, j));
			}
		}

		nodeNames = null;
		nodeNames = newNodeNames;

		matrix = null;
		matrix = newMatrix;
	}

	public ArrayList listGroupOriginator(double threshold) {
		DoubleMatrix2D tempMatrix = DoubleFactory2D.sparse.make(matrix.rows(),
				matrix.columns());

		for (int i = 0; i < tempMatrix.rows(); i++)
			for (int j = 0; j < tempMatrix.columns(); j++)
				if (matrix.get(i, j) < threshold)
					tempMatrix.set(i, j, 0.0);
				else
					tempMatrix.set(i, j, matrix.get(i, j));

		ArrayList listGroup = new ArrayList();

		for (int i = 0; i < nodeNames.length; i++) {
			boolean flag = false;
			for (int k = 0; k < listGroup.size(); k++) {
				ArrayList<String> group = (ArrayList<String>) listGroup.get(k);
				for (int l = 0; l < group.size(); l++) {
					if (group.get(l).equals(nodeNames[i])) {
						flag = true;
						break;
					}
				}
				if (flag == true)
					break;
			}
			if (flag == true)
				continue;

			ArrayList<String> group = new ArrayList<String>();
			group.add(nodeNames[i]);
			for (int j = i + 1; j < nodeNames.length; j++) {
				if ((tempMatrix.get(i, j) != 0) || (tempMatrix.get(j, i) != 0)) {
					flag = false;
					for (int k = 0; k < group.size(); k++) {
						if (group.get(k).equals(nodeNames[j])) {
							flag = true;
							break;
						}
					}
					if (!flag)
						group = cal2(j, group, tempMatrix);
				}
			}
			listGroup.add(group);
		}

		return listGroup;
	}

	private ArrayList<String> cal2(int i, ArrayList<String> group,
			DoubleMatrix2D tempMatrix) {
		group.add(nodeNames[i]);

		for (int j = 0; j < nodeNames.length; j++) {
			if ((tempMatrix.get(i, j) != 0) || (tempMatrix.get(j, i) != 0)) {
				boolean flag = false;
				for (int k = 0; k < group.size(); k++) {
					if (group.get(k).equals(nodeNames[j])) {
						flag = true;
						break;
					}
				}
				if (!flag)
					group = cal2(j, group, tempMatrix);
			}
		}
		return group;
	}

	public ArrayList listGroupOriginator() {
		ArrayList listGroup = new ArrayList();

		for (int i = 0; i < nodeNames.length; i++) {
			boolean flag = false;
			for (int k = 0; k < listGroup.size(); k++) {
				ArrayList<String> group = (ArrayList<String>) listGroup.get(k);
				for (int l = 0; l < group.size(); l++) {
					if (group.get(l).equals(nodeNames[i])) {
						flag = true;
						break;
					}
				}
				if (flag == true)
					break;
			}
			if (flag == true)
				continue;

			ArrayList<String> group = new ArrayList<String>();
			group.add(nodeNames[i]);
			for (int j = i + 1; j < nodeNames.length; j++) {
				if ((matrix.get(i, j) != 0) || (matrix.get(j, i) != 0)) {
					flag = false;
					for (int k = 0; k < group.size(); k++) {
						if (group.get(k).equals(nodeNames[j])) {
							flag = true;
							break;
						}
					}
					if (!flag)
						group = cal2(j, group);
				}
			}
			listGroup.add(group);
		}

		return listGroup;
	}

	private ArrayList<String> cal2(int i, ArrayList<String> group) {
		group.add(nodeNames[i]);

		for (int j = 0; j < nodeNames.length; j++) {
			if ((matrix.get(i, j) != 0) || (matrix.get(j, i) != 0)) {
				boolean flag = false;
				for (int k = 0; k < group.size(); k++) {
					if (group.get(k).equals(nodeNames[j])) {
						flag = true;
						break;
					}
				}
				if (!flag)
					group = cal2(j, group);
			}
		}
		return group;
	}

	public void groupOriginators(String[] groupNames, String[] groupAssignments) {
		ArrayList<String> newNodeNames = new ArrayList(Arrays
				.asList(groupNames));
		groupOriginators(newNodeNames, groupAssignments);
	}

	public SocialNetworkMatrix groupOriginators(ArrayList<String> newNodeNames,
			String[] groupAssignments) {
		DoubleMatrix2D newMatrix = DoubleFactory2D.sparse.make(newNodeNames
				.size(), newNodeNames.size(), 0);
		;

		for (int i = 0; i < nodeNames.length; i++)
			for (int j = 0; j < nodeNames.length; j++) {
				int iIndex = newNodeNames.indexOf(groupAssignments[i]);
				int jIndex = newNodeNames.indexOf(groupAssignments[j]);
				newMatrix.set(iIndex, jIndex, newMatrix.get(iIndex, jIndex)
						+ matrix.get(i, j));
			}

		String[] newNames = new String[newNodeNames.size()];
		newNodeNames.toArray(newNames);

		return new SocialNetworkMatrix(newNames, newMatrix);
	}

	public String getOrgUnitOfOriginator(String originatorName) {
		String orgUnitName = null;
		ArrayList<String> st = new ArrayList(Arrays.asList(nodeNames));
		int index = st.indexOf(originatorName);

		for (int i = 0; i < orgUnitNameList.size(); i++)
			if (orgUnitMatrix.get(index, i) != 0)
				orgUnitName += orgUnitNameList.get(i);

		return orgUnitName;
	}

	public String getOrgUnitOfOriginator(int indexOfOri) {
		String orgUnitName = "";

		for (int i = 0; i < orgUnitNameList.size(); i++)
			if (orgUnitMatrix.get(indexOfOri, i) > 0)
				orgUnitName += orgUnitNameList.get(i);

		return orgUnitName;
	}

	public String getRoleOfOriginator(String originatorName) {
		String orgRoleName = null;
		ArrayList<String> st = new ArrayList(Arrays.asList(nodeNames));
		int index = st.indexOf(originatorName);

		for (int i = 0; i < roleNameList.size(); i++)
			if (roleMatrix.get(index, i) != 0)
				orgRoleName += roleNameList.get(i);

		return orgRoleName;
	}

	public String getRoleOfOriginator(int indexOfOri) {
		String orgRoleName = "";

		for (int i = 0; i < roleNameList.size(); i++)
			if (roleMatrix.get(indexOfOri, i) != 0)
				orgRoleName += roleNameList.get(i);

		return orgRoleName;
	}

	public boolean hasRoleModel() {
		if (roleMatrix == null)
			return false;
		else
			return true;
	}

	public boolean hasOrgUnitModel() {
		if (orgUnitMatrix == null)
			return false;
		else
			return true;
	}

	public double getMaxValue() {
		double max = Double.MIN_VALUE;

		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				if (max < matrix.get(i, j))
					max = matrix.get(i, j);
		return max;
	}

	public double getMinValue() {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				if (min > matrix.get(i, j))
					min = matrix.get(i, j);
		return min;
	}

	public double getMaxFlowValue() {
		double max = Double.MIN_VALUE;

		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				if (i != j && max < matrix.get(i, j))
					max = matrix.get(i, j);
		return max;
	}

	public double getMinFlowValue() {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				if (i != j && min > matrix.get(i, j))
					min = matrix.get(i, j);
		return min;
	}

	public double getSumOfMatrix() {
		double sum = 0.0;

		for (int i = 0; i < matrix.rows(); i++)
			for (int j = 0; j < matrix.columns(); j++)
				sum += matrix.get(i, j);
		return sum;
	}

	public Object clone() {
		SocialNetworkMatrix o = null;
		o = new SocialNetworkMatrix((String[]) nodeNames.clone());

		if (matrix != null) {
			DoubleMatrix2D tempSNMatrix = DoubleFactory2D.sparse.make(matrix
					.rows(), matrix.columns());
			tempSNMatrix.assign(matrix);
			o.setMatrix(tempSNMatrix);
		}
		o.setOrgUnitName((ArrayList<String>) orgUnitNameList.clone());
		o.setRoleName((ArrayList<String>) roleNameList.clone());

		if (orgUnitMatrix != null) {
			DoubleMatrix2D temporgUnitMatrix = DoubleFactory2D.sparse.make(
					orgUnitMatrix.rows(), orgUnitMatrix.columns());
			temporgUnitMatrix.assign(orgUnitMatrix);
			o.setOrgUnitMatrix(temporgUnitMatrix);
		}

		if (roleMatrix != null) {
			DoubleMatrix2D temproleMatrix = DoubleFactory2D.sparse.make(
					roleMatrix.rows(), roleMatrix.columns());
			temproleMatrix.assign(roleMatrix);
			o.setMatrix(temproleMatrix);
		}

		if (otMatrix != null) {
			DoubleMatrix2D tempotMatrix = DoubleFactory2D.sparse.make(otMatrix
					.rows(), otMatrix.columns());
			tempotMatrix.assign(otMatrix);
			o.setMatrix(tempotMatrix);
		}

		return o;
	}

}

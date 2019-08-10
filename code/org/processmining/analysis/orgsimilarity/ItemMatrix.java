package org.processmining.analysis.orgsimilarity;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.ArrayList;
import java.util.HashSet;

import org.processmining.framework.models.orgmodel.OrgModel;

import org.processmining.framework.models.orgmodel.OrgEntity;

public class ItemMatrix {
	private OrgModel orgModel;
	private ArrayList<String> originatorList;
	private DoubleMatrix2D matrix;

	public ItemMatrix(OrgModel om, ArrayList<String> olist) {
		this.orgModel = om;
		this.originatorList = new ArrayList<String>(olist);

		this.matrix = DoubleFactory2D.sparse.make(orgModel.getOrgEntities()
				.size(), originatorList.size(), 0);

		ArrayList<OrgEntity> orgEntityList = orgModel.getOrgEntityList();
		for (OrgEntity orgEntity : orgEntityList) {
			ArrayList<String> tempList = orgModel.getResourceList(orgEntity);
			int indexX = orgEntityList.indexOf(orgEntity);
			for (String resName : tempList) {
				int indexY = originatorList.indexOf(resName);
				matrix.set(indexX, indexY, matrix.get(indexX, indexY) + 1);
			}
		}
	}

	public int size() {
		return orgModel.getOrgEntities().size();
	}

	public DoubleMatrix2D getMatrix() {
		return matrix;
	}

	public OrgModel getOrgModel() {
		return orgModel;
	}
}

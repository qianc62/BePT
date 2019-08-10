package org.processmining.analysis.orgsimilarity;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import org.processmining.analysis.orgsimilarity.SimilarityItem;
import org.processmining.analysis.orgsimilarity.SimilarityResultTableModel;
import org.processmining.analysis.orgsimilarity.ui.SimilarityUI;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;

public class SimilarityModel {

	private List<SimilarityItem> simItemsList = null;
	private List<ItemMatrix> itemMatrixList = null;
	private ArrayList<String> originatorList = null;
	private Map<OrgEntity, OrgEntity> relations = null;
	private DoubleMatrix2D matrix;

	public SimilarityModel(List<SimilarityItem> simItems) {
		HashSet<String> originatorSet = new HashSet<String>();
		this.simItemsList = simItems;
		for (SimilarityItem item : simItems) {
			OrgModel om = item.getModel();
			ArrayList<Resource> resources = om.getResourceList();
			for (Resource res : resources) {
				originatorSet.add(res.getID());
			}
		}
		originatorList = new ArrayList<String>(originatorSet);
		itemMatrixList = new ArrayList<ItemMatrix>();
		for (SimilarityItem item : simItems) {
			ItemMatrix itemMatrix = new ItemMatrix(item.getModel(),
					originatorList);
			itemMatrixList.add(itemMatrix);
		}
	}

	public OrgModel getOrgModel(int i) {
		return simItemsList.get(i).getModel();
	}

	public Map<OrgEntity, OrgEntity> getMapping() {
		return relations;
	}

	public SimilarityResultTableModel getResult(int index1, int index2,
			String metrics) {
		SimilarityResultTableModel result = null;

		ItemMatrix item1 = itemMatrixList.get(index1);
		ItemMatrix item2 = itemMatrixList.get(index2);

		DoubleMatrix2D matrix1, matrix2;
		matrix1 = item1.getMatrix();
		matrix2 = item2.getMatrix();

		if (metrics == SimilarityUI.CORRELATION_COEFFICIENT) {
			matrix = correlationcoefficient(matrix1, matrix2);
		} else if (metrics == SimilarityUI.HURESTIC) {
			matrix = hurestics(matrix1, matrix2);
		} else if (metrics == SimilarityUI.EUCLIDIAN_DISTANCE) {
			matrix = euclidiandistance(matrix1, matrix2);
		} else if (metrics == SimilarityUI.HAMMING_DISTANCE) {
			matrix = hammingdistance(matrix1, matrix2);
		}

		mapping(item1, item2);
		result = new SimilarityResultTableModel(item1.getOrgModel(), item2
				.getOrgModel(), matrix, relations);
		return result;
	}

	protected void mapping(ItemMatrix item1, ItemMatrix item2) {
		OrgModel orgModel1 = item1.getOrgModel();
		OrgModel orgModel2 = item2.getOrgModel();

		relations = new HashMap<OrgEntity, OrgEntity>();

		for (int i = 0; i < matrix.rows(); i++) {
			double max = Double.MIN_VALUE;
			OrgEntity rowEntity = orgModel1.getOrgEntityList().get(i);
			OrgEntity colEntity = null;
			for (int j = 0; j < matrix.columns(); j++) {
				if (max < matrix.get(i, j)) {
					max = matrix.get(i, j);
					colEntity = orgModel2.getOrgEntityList().get(j);
				}
			}
			relations.put(rowEntity, colEntity);
		}
	}

	public static DoubleMatrix2D hurestics(DoubleMatrix2D m1, DoubleMatrix2D m2) {

		int row1 = m1.rows();
		int row2 = m2.rows();
		int column = m1.columns();

		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row1, row2, 0);

		for (int i = 0; i < row1; i++) {
			for (int j = 0; j < row2; j++) {
				for (int k = 0; k < column; k++) {
					if (m1.get(i, k) == 1 && m2.get(j, k) == 1) {
						D.set(i, j, D.get(i, j) + 1);
					}
				}
			}
		}
		return D;
	}

	public static DoubleMatrix2D correlationcoefficient(DoubleMatrix2D m1,
			DoubleMatrix2D m2) {

		int row1 = m1.rows();
		int row2 = m2.rows();
		int column = m1.columns();

		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row1, row2, 0);

		for (int i = 0; i < row1; i++) {
			for (int j = 0; j < row2; j++) {

				double temp1 = 0.0, temp2 = 0.0;
				double upper = 0.0, below1 = 0.0, below2 = 0.0;

				for (int k = 0; k < column; k++) {
					temp1 += m1.get(i, k);
					temp2 += m2.get(j, k);
				}

				temp1 = temp1 / column;
				temp2 = temp2 / column;

				for (int k = 0; k < column; k++) {
					upper += (m1.get(i, k) - temp1) * (m2.get(j, k) - temp2);
					below1 += (m1.get(i, k) - temp1) * (m1.get(i, k) - temp1);
					below2 += (m2.get(j, k) - temp2) * (m2.get(j, k) - temp2);
				}

				D.set(i, j, upper / Math.sqrt(below1 * below2));
			}
		}
		return D;
	}

	public static DoubleMatrix2D euclidiandistance(DoubleMatrix2D m1,
			DoubleMatrix2D m2) {

		int row1 = m1.rows();
		int row2 = m2.rows();
		int column = m1.columns();

		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row1, row2, 0);

		for (int i = 0; i < row1; i++) {
			for (int j = 0; j < row2; j++) {

				double temp = 0;

				for (int k = 0; k < column; k++) {
					temp += (double) ((m1.get(i, k) - m2.get(j, k)) * (m1.get(
							i, k) - m2.get(j, k)));
				}

				temp = Math.sqrt(temp);

				if (temp == 0) {
					D.set(i, j, Double.MAX_VALUE);
				} else {
					D.set(i, j, 1 / temp);
				}
			}
		}

		return D;
	}

	public static DoubleMatrix2D hammingdistance(DoubleMatrix2D m1,
			DoubleMatrix2D m2) {

		int row1 = m1.rows();
		int row2 = m2.rows();
		int column = m1.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row1, row2, 0);

		m1.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		m2.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		for (int i = 0; i < row1; i++) {
			for (int j = 0; j < row2; j++) {

				double temp = 0;

				for (int k = 0; k < column; k++) {
					if (m1.get(i, k) != m2.get(j, k))
						temp++;
				}

				D.set(i, j, (column - temp) / column);
			}
		}
		return D;
	}

	public static DoubleMatrix2D similaritycoefficient(DoubleMatrix2D m1,
			DoubleMatrix2D m2) {

		int row1 = m1.rows();
		int row2 = m2.rows();
		int column = m1.columns();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(row1, row2, 0);

		m1.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		m2.forEachNonZero(new cern.colt.function.IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				value = 1;
				return value;
			}
		});

		for (int i = 0; i < row1; i++) {
			for (int j = 0; j < row2; j++) {

				double temp1 = 0;
				double temp2 = 0;

				for (int k = 0; k < column; k++) {
					if (m1.get(i, k) == 1 && m2.get(j, k) == 1)
						temp1++;
					else
						temp2++;
				}

				if (temp2 != 0) {
					D.set(i, j, temp1 / temp2);
				} else {
					D.set(i, j, Double.MAX_VALUE);
				}
			}
		}
		return D;
	}
}

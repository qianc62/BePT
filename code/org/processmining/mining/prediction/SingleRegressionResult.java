package org.processmining.mining.prediction;

import java.util.*;

/**
 * @author Ronald Crooy this class holds a single part of the result of the
 *         cross validation
 */
public class SingleRegressionResult {
	protected Vector<Integer> pids;
	protected Vector<Integer> evnums;

	protected Vector<Double> nppredictions;
	protected Vector<Double> avgpreditions;
	protected Vector<Double> actualvalues;

	protected Vector<Double> nperrors;
	protected Vector<Double> avgerrors;

	protected Vector<Double> bandwidth;
	protected Vector<String> varnames;

	protected Double npMSE;
	protected Double npMAPE;
	protected Double npMAE;
	protected Double avgMSE;
	protected Double avgMAPE;
	protected Double avgMAE;

	public int k;// to track which part of the crossvalidation we did
	public int r;// to track which part of the crossvalidation we did

	public SingleRegressionResult() {
		pids = new Vector<Integer>();
		evnums = new Vector<Integer>();
		varnames = new Vector<String>();
	}

	/**
	 * type 1 to 6 are npMSE, npMAPE, npMAE, avgMSE, avgMAPE, avgMAE
	 * 
	 * @param mse
	 * @param type
	 */
	public void setResult(double val, int type) {
		switch (type) {
		case 1:
			npMSE = val;
			break;
		case 2:
			npMAPE = val;
			break;
		case 3:
			npMAE = val;
			break;
		case 4:
			avgMSE = val;
			break;
		case 5:
			avgMAPE = val;
			break;
		case 6:
			avgMAE = val;
			break;
		}
	}

	/**
	 * type 1,2,3,4,5,6 are NP prediction, avg prediction, actual value, np
	 * errors, avg errors, bandwidth
	 * 
	 * @param pred
	 * @param type
	 */
	public void setPrediction(double[] pred, int type) {
		switch (type) {
		case 1:
			nppredictions = Array2Vector(pred);
			break;
		case 2:
			avgpreditions = Array2Vector(pred);
			break;
		case 3:
			actualvalues = Array2Vector(pred);
			break;
		case 4:
			nperrors = Array2Vector(pred);
			break;
		case 5:
			avgerrors = Array2Vector(pred);
			break;
		case 6:
			bandwidth = Array2Vector(pred);
			break;
		}
	}

	public void setNames(ArrayList<String> names) {
		varnames = new Vector<String>();
		varnames.addAll(names);
	}

	public void setIds(Vector<Vector> ids) {
		Iterator<Vector> it = ids.iterator();
		pids = new Vector<Integer>();
		evnums = new Vector<Integer>();
		while (it.hasNext()) {
			Vector<Integer> row = it.next();
			pids.add(row.get(1));
			evnums.add(row.get(2));
		}
	}

	private Vector<Double> Array2Vector(double[] b) {
		Vector<Double> result = new Vector<Double>(b.length);
		for (int i = 0; i < b.length; i++) {
			result.add(b[i]);
		}
		return result;
	}
}

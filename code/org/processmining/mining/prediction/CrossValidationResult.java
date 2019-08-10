package org.processmining.mining.prediction;

import java.util.*;

/**
 * @author Ronald Crooy
 * 
 */
public class CrossValidationResult {

	private Vector<Vector<SingleRegressionResult>> list;

	/**
	 * initializes the vector with empty elements
	 * 
	 * @param K
	 * @param R
	 */
	public CrossValidationResult(int K, int R) {
		list = new Vector<Vector<SingleRegressionResult>>();
		for (int k = 0; k < K; k++) {
			Vector<SingleRegressionResult> j = new Vector<SingleRegressionResult>();
			for (int r = 0; r < R; r++) {
				j.add(new SingleRegressionResult());
			}
			list.add(j);
		}
	}

	/**
	 * stores the result for the k-th cross validtion part and the r-th
	 * repititions. note k and r start at 0
	 * 
	 * @param result
	 * @param k
	 * @param r
	 */
	public void setSingleResult(SingleRegressionResult result, int k, int r) {
		Vector<SingleRegressionResult> listAtK = list.get(k);
		listAtK.set(r, result);
		list.set(k, listAtK);
	}

	/**
	 * 
	 * returns a vector with 9 rows; containing all predictions which can be
	 * exported to the events in the log in order: y, np pred, avg pred, np sd,
	 * avg sd,pid,aid, np error, avg error
	 * 
	 * @return
	 */
	public Vector<Vector> exportablepredictions() {
		Vector<Vector> result = new Vector<Vector>();
		Vector<Double> row1 = new Vector<Double>();// row 1 is Y
		Vector<Double> row2 = new Vector<Double>();// row 2 is the NP estimation
		Vector<Double> row3 = new Vector<Double>();// row 3 is the average
		// estimation
		Vector<Double> row4 = new Vector<Double>();// row 4 is the NP standard
		// deviation over the
		// repititions
		Vector<Double> row5 = new Vector<Double>();// row 5 is the average
		// standard deviation
		Vector<Integer> row6 = new Vector<Integer>();// row 7 are the
		// pids(process instance
		// id's) corresponding
		// to these values
		Vector<Integer> row7 = new Vector<Integer>();// row 7 are the
		// evnums(event sequence
		// numbers)
		// corresponding to
		// these values
		Vector<Double> row8 = new Vector<Double>();// row 8 is the error of the
		// NP estimation
		Vector<Double> row9 = new Vector<Double>();// row 9 is the error of the
		// average estimation
		Vector<Double> row10 = new Vector<Double>();// row 10 is the relative
		// error of the average
		// estimation
		Vector<Double> row11 = new Vector<Double>();// row 11 is the relative
		// error of the NP
		// estimation
		Vector<Double> row12 = new Vector<Double>();// row 12 is the absolute
		// error of the NP
		// estimation
		Vector<Double> row13 = new Vector<Double>();// row 13 is the absolute
		// error of the average
		// estimation
		for (Vector<SingleRegressionResult> kresult : list) {// for all
			// crossvalidation
			// parts
			for (int i = 0; i < kresult.firstElement().nppredictions.size(); i++) {// for
				// all
				// estimations
				// in
				// this
				// cross
				// validation
				// part
				Double npsum = new Double(0);
				Double avgsum = new Double(0);
				Double nperrorsum = new Double(0);
				Double avgerrorsum = new Double(0);
				for (SingleRegressionResult s : kresult) {// for all repititions
					npsum += s.nppredictions.get(i);
					avgsum += s.avgpreditions.get(i);
					nperrorsum += s.nperrors.get(i);
					avgerrorsum += s.avgerrors.get(i);
				}
				Double npaverage = npsum / kresult.size();
				Double avgaverage = avgsum / kresult.size();
				Double nperroravg = nperrorsum / kresult.size();
				Double avgerroravg = avgerrorsum / kresult.size();
				// standard deviation
				for (SingleRegressionResult s : kresult) {// for all repititions
					npsum += Math.pow(s.nperrors.get(i) - nperroravg, 2);
					avgsum += Math.pow(s.avgerrors.get(i) - avgerroravg, 2);
				}
				Double npsd = Math.sqrt(npsum / kresult.size());
				Double avgsd = Math.sqrt(avgsum / kresult.size());
				row1.add(kresult.firstElement().actualvalues.get(i));
				row2.add(npaverage);
				row3.add(avgaverage);
				row4.add(npsd);
				row5.add(avgsd);
				row6.add(kresult.firstElement().pids.get(i));
				row7.add(kresult.firstElement().evnums.get(i));
				row8.add(nperroravg);
				row9.add(avgerroravg);

				row10.add(Math.abs(avgerroravg)
						/ kresult.firstElement().actualvalues.get(i));
				row11.add(Math.abs(nperroravg)
						/ kresult.firstElement().actualvalues.get(i));

				row12.add(Math.abs(nperroravg));
				row13.add(Math.abs(avgerroravg));
			}
		}
		result.add(row1);
		result.add(row2);
		result.add(row3);
		result.add(row4);
		result.add(row5);
		result.add(row6);
		result.add(row7);
		result.add(row8);
		result.add(row9);
		result.add(row10);
		result.add(row11);
		result.add(row12);
		result.add(row13);
		return result;
	}

	/**
	 * calculates the average and SD of the estimations
	 * 
	 * @return a vector ready for a JTable thingy
	 */
	public Vector<Vector<Double>> estimationsTable() {
		Vector<Vector<Double>> result = new Vector<Vector<Double>>();
		// for all crossvalidation parts
		for (Vector<SingleRegressionResult> kresult : list) {
			// for all estimations in this crossvalidation part
			for (int i = 0; i < kresult.firstElement().nppredictions.size(); i++) {
				Vector<Double> row = new Vector<Double>();
				Double npsum = new Double(0);
				Double avgsum = new Double(0);
				Double nperrorsum = new Double(0);
				Double avgerrorsum = new Double(0);
				for (SingleRegressionResult s : kresult) {// for all repititions
					npsum += s.nppredictions.get(i);
					avgsum += s.avgpreditions.get(i);
					nperrorsum += s.nperrors.get(i);
					avgerrorsum += s.avgerrors.get(i);
				}
				Double npaverage = npsum / kresult.size();
				Double avgaverage = avgsum / kresult.size();
				Double nperroravg = nperrorsum / kresult.size();
				double avgerroravg = avgerrorsum / kresult.size();
				for (SingleRegressionResult s : kresult) {// for all repititions
					npsum += Math.pow(s.nppredictions.get(i) - npaverage, 2);
					avgsum += Math.pow(s.avgpreditions.get(i) - avgaverage, 2);
				}
				Double npsd = Math.sqrt(npsum / kresult.size());
				Double avgsd = Math.sqrt(avgsum / kresult.size());
				row.add(kresult.firstElement().actualvalues.get(i));
				row.add(npaverage);
				row.add(avgaverage);
				row.add(nperroravg);
				row.add(avgerroravg);
				row.add(npsd);
				row.add(avgsd);
				result.add(row);
			}
		}
		return result;
	}

	/**
	 * returns a vector which is suited for as a header for the table returned
	 * by estimationsTable()
	 * 
	 * @return
	 */
	public Vector<String> estimationsTableHeader() {
		Vector<String> result = new Vector<String>(5);
		result.add("Y");
		result.add("my prediction");
		result.add("dumb predictor");
		result.add("my prediction error");
		result.add("dumb predictor error");
		result.add("my prediction SD");
		result.add("dumb predictor SD");
		return result;
	}

	/**
	 * retuns a table with the values for 'type' for each crossvalidation part
	 * 
	 * @param type
	 * @return
	 */
	public Vector<Vector> resultsTable(int type) {
		Vector<Vector> result = new Vector<Vector>();
		Integer k = 0;
		// add the results for each part of the crossvalidation
		for (Vector<SingleRegressionResult> kresult : list) {
			Vector row1 = new Vector<Double>();
			Vector row2 = new Vector<Double>();

			row1.add("part " + k + ", dumb prediction");
			row2.add("part " + k + ", my prediction");

			// add the results for each repitition
			for (SingleRegressionResult s : kresult) {
				switch (type) {
				case 0:
					row1.add(s.avgMAE);
					row2.add(s.npMAE);
					break;
				case 1:
					row1.add(s.avgMAPE);
					row2.add(s.npMAPE);
					break;
				case 2:
					row1.add(s.avgMSE);
					row2.add(s.npMSE);
					break;
				}
			}
			result.add(row1);
			result.add(row2);
			k++;
		}
		return result;
	}

	/**
	 * the header belonging to the resultsTable()
	 * 
	 * @return
	 */
	public Vector<String> resultsTableHeader() {
		Vector<String> result = new Vector<String>();
		result.add(" ");
		for (int i = 0; i < list.firstElement().size(); i++) {
			result.add("repitition " + i);
		}
		return result;
	}

	/**
	 * returns an array with the bandwidths from the first result, which should
	 * be the only result....
	 * 
	 * @return
	 */
	public Double[] getBandwidthValues() {
		Double[] bws = new Double[list.get(0).get(0).bandwidth.size()];
		for (int i = 0; i < bws.length; i++) {
			bws[i] = list.get(0).get(0).bandwidth.get(i);
		}
		return bws;
	}

	/**
	 * returns an array with the bandwidth-NAMES from the first result, which
	 * should be the only result....
	 * 
	 * @return
	 */
	public String[] getBandwidthNames() {
		String[] bws = new String[list.get(0).get(0).varnames.size()];
		for (int i = 0; i < bws.length; i++) {
			bws[i] = list.get(0).get(0).varnames.get(i);
		}
		return bws;
	}

	/**
	 * returns a vector of the form Vector(crossval-part) of Vector(repitition)
	 * of Vector(variable) of Double. This vector should be split in tabs with a
	 * table on each tab
	 */
	public Vector<Vector<Vector>> bandwidthsTable() {
		Vector<Vector<Vector>> result = new Vector<Vector<Vector>>();
		// for all crossvalidation parts ( for each table)
		for (Vector<SingleRegressionResult> kresult : list) {
			Vector<Vector> table = new Vector<Vector>();
			int rep = 0;
			// for each repitition (for each row)
			Vector firstrow = new Vector();
			firstrow.add("-");
			firstrow.addAll(kresult.get(0).varnames);
			firstrow.add("MAE");
			firstrow.add("MAPE");
			firstrow.add("MSE");
			for (SingleRegressionResult s : kresult) {
				Vector row = new Vector();
				row.add("repitition_" + rep);
				// for each variable ( for each variable)
				for (Double val : s.bandwidth) {
					row.add(val);
				}
				row.add(s.npMAE);
				row.add(s.npMAPE);
				row.add(s.npMSE);
				table.add(row);
				rep++;
			}
			result.add(table);
		}
		return result;
	}

	/**
	 * returns a vector of Strings which is the sequence of variables for the
	 * bandwidth tables headers
	 */
	public Vector<Vector<String>> bandwidthsHeader() {
		Vector<Vector<String>> table = new Vector<Vector<String>>();
		for (Vector<SingleRegressionResult> kresult : list) {
			Vector<String> row = new Vector<String>();
			row.add("------");
			row.addAll(kresult.get(0).varnames);
			row.add("MAE");
			row.add("MAPE");
			row.add("MSE");
			table.add(row);
		}
		return table;
	}
}

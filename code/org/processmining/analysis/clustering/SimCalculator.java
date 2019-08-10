package org.processmining.analysis.clustering;

import java.util.Set;
import java.util.TreeMap;

import org.processmining.analysis.clustering.model.LogSequence;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class SimCalculator {

	// Clutering Options
	double alpha = 0.0; // alpha is the weight of activity. i.e. if 0,
	// totalSim=transSim. If 1, totalSim=actSim.
	int tranType = 1; // if 1, imp; if 0, exp;
	int simMeasure = 1; // if 0, "Jaccard Coefficient"; if 1, "Cosine Function"
	int freqOption = 0; // if 0, not; if 1, freq

	// data structure
	public LogSequence logS;
	String[] proc;
	int[] proc_freq;
	int numOfProcess;

	double[][] actSimilarity;
	double[][] tranSimilarity;
	double[][] totalSimilarity; // now, it is weighted sum of actSim & impSim

	double meanActSimilarity = 1.0;
	double meanExpTranSimilarity = 1.0;
	double meanImpTranSimilarity = 1.0;

	// data for frequency calculation
	TreeMap actProcessFrequency = new TreeMap(); // activity Char & frequency
	TreeMap tranProcessFrequency = new TreeMap(); // activity Char & frequency
	TreeMap actIPFWeight = new TreeMap(); // activity Char & frequency
	TreeMap tranIPFWeight = new TreeMap(); // activity Char & frequency
	double[] actWeightSumOfProcess;
	double[] tranWeightSumOfProcess;

	public SimCalculator(LogSequence logS) { // default alpha=0(tran),
		// tranType=1(imp),
		// simMeasure=1(cosine), int
		// freqOption

		this(logS, 0, 1, 1, 0);
	}

	public SimCalculator(LogSequence logS, double alpha, int tranType,
			int simMeasure, int freqOption) {

		this.logS = logS;
		this.alpha = alpha;
		this.tranType = tranType;
		this.simMeasure = simMeasure;
		this.freqOption = freqOption;

		numOfProcess = logS.numOfProcess;
		proc = logS.proc;
		proc_freq = logS.proc_freq;

		actSimilarity = new double[numOfProcess][numOfProcess];
		tranSimilarity = new double[numOfProcess][numOfProcess];
		totalSimilarity = new double[numOfProcess][numOfProcess];

		if (freqOption == 1) {
			calculateActInversePFWeight(); // count the number of processes with
			// each activity
			calculateTranInversePFWeight(); // count the number of processes
			// with each activity
		}

		calSimProc();
	}

	public void calSimProc() {

		int k = 0;
		numOfProcess = proc.length;

		double self_connectivity = 0.0;
		double totalNumOfProcess = 0.0;
		for (int i = 0; i < numOfProcess; i++) {
			self_connectivity += proc_freq[i] * (proc_freq[i] - 1) / 2;
			totalNumOfProcess += proc_freq[i];
		}

		double inter_connectivity_act = 0.0;
		double inter_connectivity_tran = 0.0;
		for (int i = 0; i < (numOfProcess - 1); i++) {
			for (int j = i + 1; j < numOfProcess; j++) {
				if (alpha == 1) {
					actSimilarity[i][j] = calculateActivitySimilarity(i, j);
					totalSimilarity[i][j] = actSimilarity[i][j];
					System.out.println("actSimilarity[" + i + "][" + j + "]="
							+ actSimilarity[i][j]);
					inter_connectivity_act += proc_freq[i] * proc_freq[j]
							* actSimilarity[i][j];
				} else if (alpha == 0) {
					tranSimilarity[i][j] = calculateTransitionSimilarity(i, j);
					totalSimilarity[i][j] = tranSimilarity[i][j];
					System.out.println("tranSimilarity[" + i + "][" + j + "]="
							+ tranSimilarity[i][j]);
					inter_connectivity_tran += proc_freq[i] * proc_freq[j]
							* tranSimilarity[i][j];
				} else {

					actSimilarity[i][j] = calculateActivitySimilarity(i, j);
					System.out.println("actSimilarity[" + i + "][" + j + "]="
							+ actSimilarity[i][j]);
					inter_connectivity_act += proc_freq[i] * proc_freq[j]
							* actSimilarity[i][j];

					tranSimilarity[i][j] = calculateTransitionSimilarity(i, j);
					System.out.println("tranSimilarity[" + i + "][" + j + "]="
							+ tranSimilarity[i][j]);
					inter_connectivity_tran += proc_freq[i] * proc_freq[j]
							* tranSimilarity[i][j];

					totalSimilarity[i][j] = actSimilarity[i][j] * alpha
							+ tranSimilarity[i][j] * (1 - alpha);
					System.out.println("totalSimilarity[" + i + "][" + j + "]="
							+ totalSimilarity[i][j]);
				}

			}
		}

		// considering frequency
		if (numOfProcess > 1) {
			meanActSimilarity = (self_connectivity + inter_connectivity_act)
					/ (totalNumOfProcess * (totalNumOfProcess - 1) / 2);
			meanImpTranSimilarity = (self_connectivity + inter_connectivity_tran)
					/ (totalNumOfProcess * (totalNumOfProcess - 1) / 2);
		}

	}

	private double calculateActivitySimilarity(int indexProc1, int indexProc2) {

		String pro1 = proc[indexProc1];
		String pro2 = proc[indexProc2];

		double upper = 0.0;
		double lower = 1.0;

		for (int i = 0; i < pro1.length(); i++) {
			for (int j = 0; j < pro2.length(); j++) {

				if (pro1.charAt(i) == pro2.charAt(j)) {

					double weight1 = 1;
					double weight2 = 1; // weight = af(pro1) * af(pro2)
					if (freqOption == 1) { // in fact, pro1.charAt(i) equals
						// pro2.char(j)
						weight1 = ((Double) actIPFWeight.get(new Character(pro1
								.charAt(i)))).doubleValue();
						weight2 = ((Double) actIPFWeight.get(new Character(pro2
								.charAt(j)))).doubleValue();
					}
					if (simMeasure == 1) { // "Cosine Coefficient"
						upper = (1 * weight1) * (1 * weight2);
					} else if (simMeasure == 0) { // "jaccard Coefficient"
						upper = (1 * weight1 + 1 * weight2) / 2;
					}
				}
			}
		}

		if (simMeasure == 0) { // "Jaccard Coefficient"
			if (freqOption == 0) {
				lower = pro1.length() + pro2.length() - upper;
			} else { // inverse process frequency (sum of weights =
				// actWeightSumOfProcess[indexProc1])
				lower = actWeightSumOfProcess[indexProc1]
						+ actWeightSumOfProcess[indexProc2] - upper;
			}
		} else if (simMeasure == 1) { // "Cosine Function"
			if (freqOption == 0) {
				lower = Math.pow(pro1.length(), 0.5)
						* Math.pow(pro2.length(), 0.5);
			} else { // inverse process frequency (squared sum of weights =
				// actWeightSumOfProcess[indexProc1])
				lower = Math.pow(actWeightSumOfProcess[indexProc1], 0.5)
						* Math.pow(actWeightSumOfProcess[indexProc2], 0.5);
			}
		}

		return (lower == 0) ? 0 : upper / lower;
	}

	private double calculateTransitionSimilarity(int indexProc1, int indexProc2) {

		String pro1 = proc[indexProc1];
		String pro2 = proc[indexProc2];

		String[][] pro1Tran = new String[pro1.length()][pro1.length()];
		String[][] pro2Tran = new String[pro2.length()][pro2.length()];

		double tranAct_Exp = 0.0;
		double upper_Exp = 0.0;
		double lower_Exp = 0.0;

		double[][] pro1TranVal_Exp = new double[pro1.length()][pro1.length()]; // Explicit
		// Transition
		// Value
		double[][] pro2TranVal_Exp = new double[pro2.length()][pro2.length()];
		double sumOfPro1TranValSquare_Exp = 0.0;
		double sumOfPro2TranValSquare_Exp = 0.0;

		double tranAct_Imp = 0.0;
		double upper_Imp = 0.0;
		double lower_Imp = 0.0;

		double[][] pro1TranVal_Imp = new double[pro1.length()][pro1.length()]; // Implicit
		// Transition
		// Value
		double[][] pro2TranVal_Imp = new double[pro2.length()][pro2.length()];
		double sumOfPro1TranValSquare_Imp = 0.0;
		double sumOfPro2TranValSquare_Imp = 0.0;

		if (tranType == 0) { // exp transition

			for (int i = 0; i < pro1.length() - 1; i++) {
				pro1Tran[i][i + 1] = "" + pro1.charAt(i) + pro1.charAt(i + 1);
				pro1TranVal_Exp[i][i + 1] = 1.0;

				if (simMeasure == 1) { // cosine measure
					sumOfPro1TranValSquare_Exp += Math.pow(
							pro1TranVal_Exp[i][i + 1], 2);
				} else if (simMeasure == 0) { // jaccard measure
					sumOfPro1TranValSquare_Exp += pro1TranVal_Exp[i][i + 1];
				}
			}

			for (int i = 0; i < pro2.length() - 1; i++) {
				pro2Tran[i][i + 1] = "" + pro2.charAt(i) + pro2.charAt(i + 1);
				pro2TranVal_Exp[i][i + 1] = 1.0;

				if (simMeasure == 1) { // cosine measure
					sumOfPro2TranValSquare_Exp += Math.pow(
							pro2TranVal_Exp[i][i + 1], 2);
				} else if (simMeasure == 0) { // jaccard measure
					sumOfPro2TranValSquare_Exp += pro2TranVal_Exp[i][i + 1];
				}
			}

			// upper part ���
			for (int i = 0; i < pro1.length() - 1; i++) {

				for (int m = 0; m < pro2.length() - 1; m++) {

					if (pro1Tran[i][i + 1].equals(pro2Tran[m][m + 1])) {

						double weight1 = 1; // weight1 = af(pro1)
						double weight2 = 1; // weight2 = af(pro2)
						if (freqOption == 1) { // in fact, two value below are
							// the same..
							weight1 *= ((Double) tranIPFWeight
									.get(pro1Tran[i][i + 1])).doubleValue();
							weight2 *= ((Double) tranIPFWeight
									.get(pro2Tran[m][m + 1])).doubleValue();
						}

						if (simMeasure == 1) { // "Cosine Coefficient"
							upper_Exp += pro1TranVal_Exp[i][i + 1]
									* pro2TranVal_Exp[m][m + 1] * weight1
									* weight2;
						} else if (simMeasure == 0) { // "Jaccard Coefficient"
							upper_Exp += (weight1 + weight2) / 2;
						}

					}
				}
			}
		} else { // imp trans

			for (int i = 0; i < pro1.length(); i++) {
				for (int j = i + 1; j < pro1.length(); j++) {

					pro1Tran[i][j] = "" + pro1.charAt(i) + pro1.charAt(j);
					pro1TranVal_Imp[i][j] = 1.0 * (j - i);

					if (simMeasure == 1) { // cosine measure
						sumOfPro1TranValSquare_Imp += Math.pow(
								pro1TranVal_Imp[i][j], 2);
					} else if (simMeasure == 0) { // jaccard measure
						sumOfPro1TranValSquare_Imp += pro1TranVal_Imp[i][j];
					}

				}
			}

			for (int i = 0; i < pro2.length(); i++) {
				for (int j = i + 1; j < pro2.length(); j++) {

					pro2Tran[i][j] = "" + pro2.charAt(i) + pro2.charAt(j);
					pro2TranVal_Imp[i][j] = 1.0 * (j - i);

					if (simMeasure == 1) { // cosine measure
						sumOfPro2TranValSquare_Imp += Math.pow(
								pro2TranVal_Imp[i][j], 2);
					} else if (simMeasure == 0) { // jaccard measure
						sumOfPro2TranValSquare_Imp += pro2TranVal_Imp[i][j];
					}

				}
			}

			// upper part ���
			for (int i = 0; i < pro1.length(); i++) {
				for (int j = i + 1; j < pro1.length(); j++) {

					for (int m = 0; m < pro2.length(); m++) {
						for (int n = m + 1; n < pro2.length(); n++) {

							if (pro1Tran[i][j].equals(pro2Tran[m][n])) {

								double weight1 = 1; // weight1 = af(pro1)
								double weight2 = 1; // weight2 = af(pro2)
								if (freqOption == 1) { // in fact, two value
									// below are the same..
									weight1 *= ((Double) tranIPFWeight
											.get(pro1Tran[i][j])).doubleValue();
									weight2 *= ((Double) tranIPFWeight
											.get(pro2Tran[m][n])).doubleValue();
								}

								if (simMeasure == 1) { // "Cosine Coefficient"
									upper_Imp += pro1TranVal_Imp[i][j]
											* pro2TranVal_Imp[m][n] * weight1
											* weight2;
								} else if (simMeasure == 0) { // "Jaccard Coefficient"
									upper_Imp += (pro1TranVal_Imp[i][j]
											* weight1 + pro2TranVal_Imp[m][n]
											* weight2) / 2;
								}

							}
						}
					}
				}
			}

		}

		if (simMeasure == 1) { // "Cosine Coefficient"
			if (freqOption == 0) {
				if (tranType == 0) {
					lower_Exp = Math.pow(sumOfPro1TranValSquare_Exp, 0.5)
							* Math.pow(sumOfPro2TranValSquare_Exp, 0.5);
				} else {
					lower_Imp = Math.pow(sumOfPro1TranValSquare_Imp, 0.5)
							* Math.pow(sumOfPro2TranValSquare_Imp, 0.5);
				}
			} else { // inverse process frequency (sum of weights =
				// actWeightSumOfProcess[indexProc1])
				if (tranType == 0) {
					lower_Exp = Math.pow(tranWeightSumOfProcess[indexProc1],
							0.5)
							* Math.pow(tranWeightSumOfProcess[indexProc2], 0.5);
				} else {
					lower_Imp = Math.pow(tranWeightSumOfProcess[indexProc1],
							0.5)
							* Math.pow(tranWeightSumOfProcess[indexProc2], 0.5);
				}
			}
		} else if (simMeasure == 0) { // "Jaccard Coefficient"
			if (freqOption == 0) {
				if (tranType == 0) {
					lower_Exp = sumOfPro1TranValSquare_Exp
							+ sumOfPro2TranValSquare_Exp - upper_Exp;
				} else {
					lower_Imp = sumOfPro1TranValSquare_Imp
							+ sumOfPro2TranValSquare_Imp - upper_Imp;
				}
			} else { // inverse process frequency (sum of weights =
				// actWeightSumOfProcess[indexProc1])
				if (tranType == 0) {
					lower_Exp = tranWeightSumOfProcess[indexProc1]
							+ tranWeightSumOfProcess[indexProc2] - upper_Exp;
				} else {
					lower_Imp = tranWeightSumOfProcess[indexProc1]
							+ tranWeightSumOfProcess[indexProc2] - upper_Imp;
				}
			}
		}

		double tranSim = 0.0;
		if (tranType == 0) {
			tranAct_Exp = (lower_Exp == 0) ? 0 : upper_Exp / lower_Exp;
			tranSim = tranAct_Exp;
		} else {
			tranAct_Imp = (lower_Imp == 0) ? 0 : upper_Imp / lower_Imp;
			tranSim = tranAct_Imp;
		}
		return tranSim;
	}

	private void calculateActInversePFWeight() { // calculate the activity
		// Weight Sum of each
		// Process

		// count PF of each activity
		Character ch = null;
		Integer c = null;
		for (int i = 0; i < numOfProcess; i++) {

			TreeMap tmpMemory = new TreeMap();
			for (int j = 0; j < proc[i].length(); j++) {

				ch = new Character(proc[i].charAt(j));

				/*
				 * System.out.println("~~~~~~~~~~~~~~ "+i+" ~~~ "+j+" ~~~"+ch.toString
				 * ()); //if proc has more than one activity c = (Integer)
				 * tmpMemory.get(ch); if(c==null) tmpMemory.put(c, new
				 * Integer(1)); else break;
				 */
				c = (Integer) actProcessFrequency.get(ch);
				if (c == null) {
					actProcessFrequency.put(ch, new Integer(1));
				} else {
					actProcessFrequency.put(ch, new Integer(c.intValue() + 1));
				}
			}
		}

		// make Inverse PF map
		Set s = actProcessFrequency.keySet();
		Object[] keyA = s.toArray();
		for (int i = 0; i < keyA.length; i++) {
			Character keyStr = (Character) keyA[i];
			Integer freq = (Integer) actProcessFrequency.get(keyStr);
			double d = Math.log(((double) numOfProcess) / freq.doubleValue());
			d /= Math.log(2);
			actIPFWeight.put(keyStr, new Double(d));
		}

		// in case of Jaccard, calculate Sum of weights for each process
		// in case of Cosine, calculate Squared Sum of weights for each process
		actWeightSumOfProcess = new double[numOfProcess];
		if (simMeasure == 0) { // Jaccard coefficient
			for (int i = 0; i < numOfProcess; i++) {
				double sumOfProcess = 0;
				for (int j = 0; j < proc[i].length(); j++) {

					Double d = (Double) actIPFWeight.get(new Character(proc[i]
							.charAt(j)));
					sumOfProcess += d.doubleValue(); // jaccard -> sum of
					// weights
				}
				actWeightSumOfProcess[i] = sumOfProcess; // jaccard
			}
		} else if (simMeasure == 1) { // cosine measure
			for (int i = 0; i < numOfProcess; i++) {
				double sumOfProcess = 0;
				for (int j = 0; j < proc[i].length(); j++) {

					Double d = (Double) actIPFWeight.get(new Character(proc[i]
							.charAt(j)));
					sumOfProcess += Math.pow(d.doubleValue(), 2); // squared sum
				}
				actWeightSumOfProcess[i] = sumOfProcess; // root
			}
		}
	}

	private void calculateTranInversePFWeight() { // calculate the transition
		// Weight Sum of each
		// Process

		// count tatal frequency
		String str;
		Integer c = null;
		for (int i = 0; i < numOfProcess; i++) {

			TreeMap tmpMemory = new TreeMap();
			for (int j = 0; j < proc[i].length(); j++) {
				for (int k = j + 1; k < proc[i].length(); k++) {

					str = "" + proc[i].charAt(j) + proc[i].charAt(k);

					// if proc has more than one transition
					c = (Integer) tmpMemory.get(str);
					if (c != null) {
						break;
					} else {
						tmpMemory.put(str, new Integer(1));
					}

					c = (Integer) tranProcessFrequency.get(str);
					if (c == null) {
						tranProcessFrequency.put(str, new Integer(1));
					} else {
						tranProcessFrequency.put(str, new Integer(
								c.intValue() + 1));
					}
				}
			}
		}

		// make Inverse PF map
		Set s = tranProcessFrequency.keySet();
		Object[] keyA = s.toArray();
		for (int i = 0; i < keyA.length; i++) {
			String keyStr = (String) keyA[i];
			Integer freq = (Integer) tranProcessFrequency.get(keyStr);
			double d = Math.log(((double) numOfProcess) / freq.doubleValue());
			d /= Math.log(2);
			tranIPFWeight.put(keyStr, new Double(d));
		}

		// in case of Jaccard, calculate Sum of weights for each process
		// in case of Cosine, calculate Root of Squared Sum of weights for each
		// process
		tranWeightSumOfProcess = new double[numOfProcess];
		if (simMeasure == 0) { // Jaccard coefficient
			for (int i = 0; i < numOfProcess; i++) {
				double sumOfProcess = 0;
				for (int j = 0; j < proc[i].length(); j++) {
					for (int k = j + 1; k < proc[i].length(); k++) {

						Double d = (Double) tranIPFWeight.get(""
								+ proc[i].charAt(j) + proc[i].charAt(k));
						sumOfProcess += d.doubleValue(); // jaccard -> sum of
						// weights
					}
				}
				tranWeightSumOfProcess[i] = sumOfProcess; // jaccard
			}
		} else if (simMeasure == 1) { // cosine measure
			for (int i = 0; i < numOfProcess; i++) {
				double sumOfProcess = 0;
				for (int j = 0; j < proc[i].length(); j++) {
					for (int k = j + 1; k < proc[i].length(); k++) {

						Double d = (Double) tranIPFWeight.get(""
								+ proc[i].charAt(j) + proc[i].charAt(k));
						sumOfProcess += Math.pow(d.doubleValue(), 2); // squared
						// sum
					}
				}
				tranWeightSumOfProcess[i] = sumOfProcess; // root
			}
		}

	}

	public String[] getProc() {
		return proc;
	}

	public double getMeanActSimilarity() {
		return meanActSimilarity;
	}

	public double getMeanExpTranSimilarity() {
		return meanExpTranSimilarity;
	}

	public double getMeanImpTranSimilarity() {
		return meanImpTranSimilarity;
	}

	public double[][] getTotalSimilarity() {
		return totalSimilarity;
	}

	/*
	 * public double calculateIntraVar(){
	 * 
	 * calSimProc(); //if 1.0, it is Transition Similarity
	 * 
	 * double intraVar = 0.0; for(int i=0; i<numOfProcess; i++) {
	 * 
	 * double tmpVar = 0.0; for(int j=0; j<numOfProcess; j++) { if(i==j)
	 * continue; tmpVar += Math.pow( 1 - totalSimilarity[i][j] , 2); } tmpVar /=
	 * numOfProcess-1 ;
	 * 
	 * intraVar += tmpVar; } intraVar /= numOfProcess;
	 * 
	 * return intraVar; }
	 */
}

package org.processmining.analysis.clustering.model;

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
public class LogSimilarity {

	private String[] procSeq = null;
	private int[] proc_freq = null;

	private double[][] actSim = null;
	private double[][] expTransSim = null;
	private double[][] impTransSim = null;

	private double meanActSimilarity = 0;
	private double meanExpTranSimilarity = 0;
	private double meanImpTranSimilarity = 0;

	public LogSimilarity() {
	}

	public String[] getProcSeq() {
		return procSeq;
	}

	public int[] getProc_freq() {
		return proc_freq;
	}

	public double[][] getActSim() {
		return actSim;
	}

	public double[][] getExpTransSim() {
		return expTransSim;
	}

	public double[][] getImpTransSim() {
		return impTransSim;
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

	public void setProcSeq(String[] procSeq) {
		this.procSeq = procSeq;
	}

	public void setProc_freq(int[] proc_freq) {
		this.proc_freq = proc_freq;
	}

	public void setActSim(double[][] actSim) {
		this.actSim = actSim;
	}

	public void setExpTransSim(double[][] expTransSim) {
		this.expTransSim = expTransSim;
	}

	public void setImpTransSim(double[][] impTransSim) {
		this.impTransSim = impTransSim;
	}

	public void setMeanActSimilarity(double meanActSimilarity) {
		this.meanActSimilarity = meanActSimilarity;
	}

	public void setMeanExpTranSimilarity(double meanExpTranSimilarity) {
		this.meanExpTranSimilarity = meanExpTranSimilarity;
	}

	public void setMeanImpTranSimilarity(double meanImpTranSimilarity) {
		this.meanImpTranSimilarity = meanImpTranSimilarity;
	}
}

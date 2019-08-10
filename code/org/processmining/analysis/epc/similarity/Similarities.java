package org.processmining.analysis.epc.similarity;

import java.util.*;

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

public class Similarities {

	public final static double BEING_PROCESSED = Double.MAX_VALUE;
	public final static double INVALID = -1.0;

	private double[][] similarities;

	private String title;

	public Similarities(String title, int baseFootprints,
			int compareToFootprints) {
		similarities = new double[baseFootprints][compareToFootprints];
		for (int i = 0; i < baseFootprints; i++) {
			Arrays.fill(similarities[i], INVALID);
		}
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public double get(int i, int j) {
		return similarities[i][j];
	}

	public void set(int i, int j, double v) {
		synchronized (similarities) {
			similarities[i][j] = v;
		}
	}

	public void setAll(int row, int start, double v) {
		synchronized (similarities) {
			Arrays.fill(similarities[row], start, similarities[row].length, v);
		}
	}

	public double[][] getSimilarities() {
		return similarities;
	}

}

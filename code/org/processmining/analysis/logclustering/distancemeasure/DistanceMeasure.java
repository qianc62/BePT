package org.processmining.analysis.logclustering.distancemeasure;

import cern.colt.matrix.DoubleMatrix2D;

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
public abstract class DistanceMeasure {

	private String name;

	public abstract DoubleMatrix2D calculateDistance(DoubleMatrix2D m);

	public abstract DoubleMatrix2D calculateDistance(DoubleMatrix2D m,
			DoubleMatrix2D n);

	public DistanceMeasure(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}

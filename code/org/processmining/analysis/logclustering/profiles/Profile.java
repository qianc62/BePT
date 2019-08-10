package org.processmining.analysis.logclustering.profiles;

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
 * @author Minseok Song (m.s.song@tue.nl)
 * @version 1.0
 */
public abstract class Profile {

	protected String name;
	protected String description;
	protected double normalizationMaximum;
	protected boolean invert;

	/**
	 * ; The number of traces whose distances are measured.
	 */
	protected int traceSize;

	private Profile() {
		// standard constructor disabled.
	}

	public Profile(String aName, String aDescription, int aTraceSize) {
		name = aName;
		description = aDescription;
		traceSize = aTraceSize;
		normalizationMaximum = 1.0;
		invert = false;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public double getNormalizationMaximum() {
		return normalizationMaximum;
	}

	public void setName(String aName) {
		name = aName;
	}

	public void setDescription(String aDescription) {
		description = aDescription;
	}

	public void setTraceSize(int aTraceSize) {
		traceSize = aTraceSize;
	}

	public int getTraceSize() {
		return traceSize;
	}

	public void setNormalizationMaximum(double aNormalizationMaximum) {
		normalizationMaximum = aNormalizationMaximum;
	}

	public void setInvert(boolean inverted) {
		invert = inverted;
	}

	public boolean getInvert() {
		return invert;
	}

	public boolean isValid() {
		return true;
	}

	public String toString() {
		return name;
	}
}

package org.processmining.analysis.socialsuccess.clustering;

public class InvalidDimensionsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5117991391187765423L;

	public InvalidDimensionsException(int i) {
		super("Expected " + Integer.toString(i) + " dimensions.");
	}
}

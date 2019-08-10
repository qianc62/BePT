package org.processmining.analysis.sessionanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiUtilities;

public class SessionOptions {

	public enum DisplayType {
		MIN("Minimum", true), MAX("Maximum", true), ARITHMETIC_MEAN(
				"Arithmetic Mean", true), VARIANCE("Variance", false), STANDARD_DEVIATION(
				"Standard Deviation", false), GEOMETRIC_MEAN("Geometric Mean",
				false), SUM("Sum", true), NO_MEASUREMENTS(
				"No. of Measurements", false);
		public String toString() {
			return myName;
		}

		public boolean isTimed() {
			return myTimed;
		}

		private DisplayType(String name, boolean timed) {
			myName = name;
			myTimed = timed;
		}

		private String myName;
		private boolean myTimed;
	}

	GUIPropertyListEnumeration myDisplayProperty;

	/**
	 * Default constructor. Initializes default options.
	 */
	public SessionOptions(GuiNotificationTarget parent) {
		myDisplayProperty = new GUIPropertyListEnumeration(
				"Display in graph: ",
				"Determine which result type should be visualized in graph",
				Arrays.asList(DisplayType.values()), parent, 200);
	}

	/**
	 * Retrieves the GUI panel representing this object.
	 * 
	 * @return the GUI panel representing this object
	 */
	public JPanel getPanel() {
		return myDisplayProperty.getPropertyPanel();
	}

	/**
	 * Delivers the basic statistic values for the given statistic object in the
	 * requested result type (e.g., minimum or sum value).
	 * 
	 * @param type
	 *            the type of result value that is requested
	 * @param statistic
	 *            the statistic object from which the value is requested
	 * @return the requensted distance measurement value
	 */
	public static double getStatisticValue(SummaryStatistics statistic,
			DisplayType type) {
		if (type == DisplayType.MIN) {
			return statistic.getMin();
		} else if (type == DisplayType.MAX) {
			return statistic.getMax();
		} else if (type == DisplayType.ARITHMETIC_MEAN) {
			return statistic.getMean();
		} else if (type == DisplayType.VARIANCE) {
			return statistic.getVariance();
		} else if (type == DisplayType.STANDARD_DEVIATION) {
			return statistic.getStandardDeviation();
		} else if (type == DisplayType.GEOMETRIC_MEAN) {
			return statistic.getGeometricMean();
		} else if (type == DisplayType.SUM) {
			return statistic.getSum();
		} else if (type == DisplayType.NO_MEASUREMENTS) {
			return statistic.getN();
		} else {
			return -1;
		}
	}

}
package org.processmining.analysis.logstatistics;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GUIPropertyListEnumerationOfGuiDisplayables;
import org.processmining.framework.util.GuiDisplayable;

/**
 * Option panel displayed to the user in order to adjust the displayed log
 * statistics.
 * 
 * @author anne
 */
public class OptionPanel implements GuiDisplayable {

	public enum ResultType {
		ACTIVITY("Activity"), MIN("Minimum"), MAX("Maximum"), ARITHMETIC_MEAN(
				"Arithmetic Mean"), VARIANCE("Variance"), STANDARD_DEVIATION(
				"Standard Deviation"), GEOMETRIC_MEAN("Geometric Mean"), SUM(
				"Sum"), NO_MEASUREMENTS("No. of Measurements");

		public String toString() {
			return myName;
		}

		private ResultType(String name) {
			myName = name;
		}

		private String myName;
	}

	// the "show min values" option
	protected ResultConfiguration myShowMinValues;
	// the "show max values" option
	protected ResultConfiguration myShowMaxValues;
	// the "show arithmetic mean values" option
	protected ResultConfiguration myShowArithmeticMeanValues;
	// the "show variance values" option
	protected ResultConfiguration myShowVarianceValues;
	// the "show standard deviation values" option
	protected ResultConfiguration myShowStandardDeviationValues;
	// the "show geometric mean values" option
	protected ResultConfiguration myShowGeometricMeanValues;
	// the "show sum values" option
	protected ResultConfiguration myShowSumValues;
	// the "show no. of measurements values" option
	protected ResultConfiguration myShowNoMeasurementsValues;

	// the "result properties" part
	protected GUIPropertyListEnumerationOfGuiDisplayables myDisplayProperties;

	// the "viewing properties" part
	protected GUIPropertyListEnumerationOfGuiDisplayables myViewingProperties;

	/**
	 * Default constructor. Initializes default options.
	 */
	public OptionPanel() {

		ArrayList<GuiDisplayable> viewingOptions = new ArrayList<GuiDisplayable>();
		viewingOptions.add(new TextViewProperties());
		viewingOptions.add(new GraphViewProperties());
		myViewingProperties = new GUIPropertyListEnumerationOfGuiDisplayables(
				"Display results as:",
				"Determine in which mode the results shall be displayed",
				viewingOptions, null, 200);

		// initialize time options
		ArrayList<HLTypes.TimeUnit> timeOptions = new ArrayList<HLTypes.TimeUnit>();
		timeOptions.add(HLTypes.TimeUnit.MINUTES);
		timeOptions.add(HLTypes.TimeUnit.MILLISECONDS);
		timeOptions.add(HLTypes.TimeUnit.SECONDS);
		timeOptions.add(HLTypes.TimeUnit.HOURS);
		timeOptions.add(HLTypes.TimeUnit.DAYS);
		timeOptions.add(HLTypes.TimeUnit.WEEKS);
		timeOptions.add(HLTypes.TimeUnit.MONTHS);
		timeOptions.add(HLTypes.TimeUnit.YEARS);

		// initialize result options
		myShowMinValues = new ResultConfiguration("Minimum", true, timeOptions);
		myShowMaxValues = new ResultConfiguration("Maximum", true, timeOptions);
		;
		myShowArithmeticMeanValues = new ResultConfiguration("Arithmetic Mean",
				true, timeOptions);
		myShowVarianceValues = new ResultConfiguration("Variance", false);
		myShowStandardDeviationValues = new ResultConfiguration(
				"Standard Deviation", true);
		myShowGeometricMeanValues = new ResultConfiguration("Geometric Mean",
				true);
		myShowSumValues = new ResultConfiguration("Sum", true, timeOptions);
		myShowNoMeasurementsValues = new ResultConfiguration(
				"No. of measurements", true);

		ArrayList<GuiDisplayable> allProperties = new ArrayList<GuiDisplayable>();
		allProperties.add(myShowMinValues);
		allProperties.add(myShowMaxValues);
		allProperties.add(myShowArithmeticMeanValues);
		allProperties.add(myShowVarianceValues);
		allProperties.add(myShowStandardDeviationValues);
		allProperties.add(myShowGeometricMeanValues);
		allProperties.add(myShowSumValues);
		allProperties.add(myShowNoMeasurementsValues);
		myDisplayProperties = new GUIPropertyListEnumerationOfGuiDisplayables(
				"Adjust properties: ",
				"Change the properties for specific result values",
				allProperties, null, 200);
	}

	/**
	 * Returns the currently selected view mode object, which contains further
	 * information about the display options for the respective mode.
	 * 
	 * @return the current view mode object
	 */
	public ViewMode getCurrentViewMode() {
		return (ViewMode) myViewingProperties.getValue();
	}

	/**
	 * Returns the configuration object for the requested result type. The
	 * configuration object contains information about, e.g., in which time unit
	 * the result type should be displayed.
	 * 
	 * @param type
	 *            the result type for which the configuration object is
	 *            requested
	 * @return the requested configuration object
	 */
	public ResultConfiguration getProperty(ResultType type) {
		if (type == ResultType.MIN) {
			return myShowMinValues;
		} else if (type == ResultType.MAX) {
			return myShowMaxValues;
		} else if (type == ResultType.ARITHMETIC_MEAN) {
			return myShowArithmeticMeanValues;
		} else if (type == ResultType.VARIANCE) {
			return myShowVarianceValues;
		} else if (type == ResultType.STANDARD_DEVIATION) {
			return myShowStandardDeviationValues;
		} else if (type == ResultType.GEOMETRIC_MEAN) {
			return myShowGeometricMeanValues;
		} else if (type == ResultType.SUM) {
			return myShowSumValues;
		} else if (type == ResultType.NO_MEASUREMENTS) {
			return myShowNoMeasurementsValues;
		} else {
			// note that result configuration for "Activity" does not exist
			return null;
		}
	}

	/**
	 * Retrieves the GUI panel representing this object.
	 * 
	 * @return the GUI panel representing this object
	 */
	public JPanel getPanel() {
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.LINE_AXIS));

		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		result.add(myViewingProperties.getPropertyPanel());
		result.add(Box.createRigidArea(new Dimension(0, 5)));
		result.add(myDisplayProperties.getPropertyPanel());
		result.add(Box.createRigidArea(new Dimension(0, 15)));

		outerPanel.add(result);
		outerPanel.add(Box.createHorizontalGlue());
		return outerPanel;
	}

	/**
	 * Configures a specific type of basic log statistic result (e.g., minimum
	 * value). <br>
	 * It captures whether this option should be included in the HTML view, and
	 * optionally in which time unit it shall be displayed.
	 * 
	 * @author anne
	 */
	public class ResultConfiguration implements GuiDisplayable {

		private String myName;
		private GUIPropertyBoolean myShowValue;
		private GUIPropertyListEnumeration myTimeUnit;

		public ResultConfiguration(String name, boolean isEnabled) {
			this(name, isEnabled, null);
		}

		public ResultConfiguration(String name, boolean isEnabled,
				ArrayList timeValues) {
			myName = name;
			myShowValue = new GUIPropertyBoolean(
					"Include this value in overview",
					"Whether this value should be displayed for each activity in the textual overview",
					isEnabled);
			if (timeValues != null) {
				myTimeUnit = new GUIPropertyListEnumeration(
						"Time unit:",
						"Determine the time unit in which the results should be displayed",
						timeValues, null, 180);
			}
		}

		public boolean isEnabled() {
			return myShowValue.getValue();
		}

		public boolean hasTimeUnit() {
			if (myTimeUnit != null) {
				return true;
			} else {
				return false;
			}
		}

		public HLTypes.TimeUnit getTimeUnit() {
			return (HLTypes.TimeUnit) myTimeUnit.getValue();
		}

		public JPanel getPanel() {
			JPanel result = new JPanel();
			result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
			result.add(myShowValue.getPropertyPanel());
			if (this.hasTimeUnit() == true) {
				result.add(myTimeUnit.getPropertyPanel());
			}
			return result;
		}

		public String toString() {
			return myName;
		}
	}

	/**
	 * Dummy class to form common base class for viewing property classes.
	 * 
	 * @author anne
	 */
	class ViewMode {
	}

	/**
	 * Class holding the properties for the HTML overview modus.
	 * 
	 * @author anne
	 */
	class TextViewProperties extends ViewMode implements GuiDisplayable {

		// the "sort by" option
		private GUIPropertyListEnumeration mySortByProperty;

		/**
		 * Default constructor.
		 */
		public TextViewProperties() {
			// initialize "sort by" option
			ArrayList<ResultType> values = new ArrayList<ResultType>();
			for (ResultType type : ResultType.values()) {
				values.add(type);
			}
			mySortByProperty = new GUIPropertyListEnumeration("Sort by:  ",
					"Determine by what the list the results should be sorted",
					values, null, 180);
		}

		/**
		 * Retrieves the result type by which the HTML table should be sorted.
		 * 
		 * @return the result type by which the activity durations should be
		 *         sorted
		 */
		public ResultType getValue() {
			return (ResultType) mySortByProperty.getValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
		 */
		public JPanel getPanel() {
			JPanel result = new JPanel();
			result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
			result.add(mySortByProperty.getPropertyPanel());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Textual Overview";
		}
	}

	/**
	 * Class holding the properties for the graphical overview mode.
	 * 
	 * @author anne
	 */
	public class GraphViewProperties extends ViewMode implements GuiDisplayable {

		// the "sort by" option
		private GUIPropertyListEnumeration myVisualizeProperty;

		/**
		 * Default constructor.
		 */
		public GraphViewProperties() {
			// initialize "sort by" option
			ArrayList<ResultType> values = new ArrayList<ResultType>();
			values.add(ResultType.ARITHMETIC_MEAN);
			values.add(ResultType.MIN);
			values.add(ResultType.MAX);
			values.add(ResultType.VARIANCE);
			values.add(ResultType.STANDARD_DEVIATION);
			values.add(ResultType.GEOMETRIC_MEAN);
			values.add(ResultType.SUM);
			values.add(ResultType.NO_MEASUREMENTS);
			myVisualizeProperty = new GUIPropertyListEnumeration("Visualize:",
					"Determine which kind of values should be visualized",
					values, null, 180);
		}

		/**
		 * Retrieves the result type currently selected for visualization.
		 * 
		 * @return the result type which shall be visualized for the activity
		 *         durations
		 */
		public ResultType getValue() {
			return (ResultType) myVisualizeProperty.getValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
		 */
		public JPanel getPanel() {
			JPanel result = new JPanel();
			result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
			result.add(myVisualizeProperty.getPropertyPanel());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "Graphical Representation";
		}
	}
}

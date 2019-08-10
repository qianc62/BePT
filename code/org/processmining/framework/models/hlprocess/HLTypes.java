/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * Class defining static enums that represent simple data types used in the
 * context of the high level processes in ProM.
 */
public class HLTypes {

	/**
	 * Class representing a time unit, such as "hour" or "millisecond". The
	 * following time units can be represented by this class TimeUnit.YEARS,
	 * TimeUnit.MONTHS, TimeUnit.WEEKS, TimeUnit.DAYS, TimeUnit.HOURS,
	 * TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS.
	 */
	public static enum TimeUnit {
		YEARS("Years", 12 * 30 * 7 * 24 * 60 * 60), MONTHS("Months", 30 * 7
				* 24 * 60 * 60), WEEKS("Weeks", 7 * 24 * 60 * 60), DAYS("Days",
				24 * 60 * 60), HOURS("Hours", 60 * 60), MINUTES("Minutes", 60), SECONDS(
				"Seconds", 1), MILLISECONDS("Milliseconds", 1); // milliseconds
		// not supported
		// by cpn export

		/**
		 * Constructor for the enumeration type TimeUnit
		 */
		private TimeUnit(String name, int conversionValue) {
			myName = name;
			myConversionValue = conversionValue;
		}

		/**
		 * Retrieves all specified types in a list (same as returned by values()
		 * method).
		 * 
		 * @return a list of all specified values
		 */
		public static List<TimeUnit> getAllTypes() {
			ArrayList<TimeUnit> types = new ArrayList<TimeUnit>();
			TimeUnit[] values = TimeUnit.values();
			for (int i = 0; i < values.length; i++) {
				TimeUnit val = values[i];
				types.add(val);
			}
			return types;
		}

		public String toString() {
			return myName;
		}

		/**
		 * Returns the value by which a time value in seconds needs to be
		 * devided in order to yield the respective time unit.
		 * 
		 * @return the conversion value for the repsective time unit
		 */
		public int getConversionValue() {
			return myConversionValue;
		}

		private String myName;
		private int myConversionValue;
	}

	/**
	 * Class representing a perspective that may be covered by the
	 * HighLevelProcess A HighLevelProcess model may cover more than one
	 * perspective.
	 * <p>
	 * Is used, e.g., to make sensible pre-selections when merging different
	 * simulation models.
	 */
	public static enum Perspective {
		// Organizational model (roles and groups)
		ORGANIZATIONAL_MODEL("Organizational model"),
		// links required roles to activities in the process
		// (resource assignment)
		ROLES_AT_TASKS("Resource assignment"),
		// execution times for activities
		TIMING_EXECTIME("Execution times"),
		// waiting times for activities
		TIMING_WAITTIME("Waiting times"),
		// sojoiurn time (= execution + waiting time)
		TIMING_SOJTIME("Sojourn times"),
		// case arrival rate
		CASE_GEN_SCHEME("Case arrival rate"),
		// attributes (type, name and initial value)
		DATA_INITIAL_VAL("Initial data values"),
		// possible values for a given attribute (and type and name)
		DATA_VALUE_RANGE("Data value range"),
		// links attributes to activities in the process
		// that provide values for this attribute in the course of process
		DATA_AT_TASKS("Data at tasks"),
		// decision rules
		CHOICE_DATA("Decision rules"),
		// decision probabilities
		CHOICE_PROB("Decision probabilities"),
		// relative frequencies for the activities involved in choice
		CHOICE_FREQ("Decision frequencies"),
		// shows that the choice perspective is covered AND configured, i.e,
		// one choice may be based on data while another one is based on
		// probabilities
		CHOICE_CONF("Decision configuration");

		private String name;

		/**
		 * Constructor for the enumeration type ProvidedPerspective
		 */
		private Perspective(String aName) {
			name = aName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return name;
		}
	}

	/**
	 * Defines an enumeration type for a data attribute. <br>
	 * For example, an attribute may be of a numeric or boolean type.
	 */
	public static enum AttributeType {
		Numeric, // numeric values can have a distribution and an initial value
		Nominal, // nominal attributes are enumerations of values, among which
		// an initial value
		Boolean; // a boolean attribute can have the initial value true or false

		/**
		 * Retrieves all specified types in a list (same as returned by values()
		 * method).
		 * 
		 * @return a list of all specified values
		 */
		public static List<AttributeType> getAllTypes() {
			ArrayList<AttributeType> types = new ArrayList<AttributeType>();
			AttributeType[] values = AttributeType.values();
			for (int i = 0; i < values.length; i++) {
				AttributeType val = values[i];
				types.add(val);
			}
			return types;
		}
	}

	/**
	 * An enumeration type for the way in which input data to a task is
	 * transformed into output data for that task. <br>
	 * For example, a data attribute can be resampled or reused. As soon as
	 * there are input and output data attributes for an activity, more complex
	 * transformation types will become possible.
	 */
	public static enum TransformationType {
		Resample, // generate random value according to specification
		Reuse; // write back the same value as was read (unchanged but, e.g.,
		// logged)

		/**
		 * Retrieves all specified types in a list (same as returned by values()
		 * method).
		 * 
		 * @return a list of all specified values
		 */
		public static List<TransformationType> getAllTypes() {
			ArrayList<TransformationType> types = new ArrayList<TransformationType>();
			TransformationType[] values = TransformationType.values();
			for (int i = 0; i < values.length; i++) {
				TransformationType val = values[i];
				types.add(val);
			}
			return types;
		}
	}

	/**
	 * Defines which ways to look at a choice are available.
	 */
	public enum ChoiceEnum {
		NONE("unguided (random)"), DATA("data attributes"), PROB(
				"probabilities"), FREQ("frequencies");

		private final String name;

		ChoiceEnum(String type) {
			this.name = type;
		}

		public String toString() {
			return name;
		}

		/**
		 * Retrieves all specified types in a list (same as returned by values()
		 * method).
		 * 
		 * @return a list of all specified values
		 */
		public static List<ChoiceEnum> getAllTypes() {
			ArrayList<ChoiceEnum> types = new ArrayList<ChoiceEnum>();
			ChoiceEnum[] values = ChoiceEnum.values();
			for (int i = 0; i < values.length; i++) {
				ChoiceEnum val = values[i];
				types.add(val);
			}
			return types;
		}
	}

	/**
	 * Enumeration type for the different event types that the user can decide
	 * for which some specific type of timing information needs to be selected
	 * from.
	 */
	public enum EventType {
		SCHEDULE("schedule"), START("start"), COMPLETE("complete"), NORMAL(
				"normal");

		private final String myName;

		EventType(String name) {
			myName = name;
		}

		public String toString() {
			return myName;
		}
	}
}

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.pdm;

import java.util.*;
import java.lang.*;
import java.math.*;
import javax.swing.*;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title: PDMDesign
 * </p>
 * *
 * <p>
 * Description: Represents a PDM design, i.e. a grouping over the PDM model.
 * </p>
 * *
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * *
 * <p>
 * Company:
 * </p>
 * 
 * @author Irene Vanderfeesten
 * @version 1.0
 */
public class PDMDesign {

	public HashMap activities = new HashMap(); // the activities of the design
	public JTable activitiesTable = new JTable(); // the table contains all
	// cohesion values for all
	// activities
	public String[] tableHeader = { "Activity", "Activity Cohesion",
			"Relation Cohesion (lambda)", "Information Cohesion (mu)" };
	public Object[][] tableContent;
	private String designID; // the ID of the design

	public Double RoundDouble(Double d, Integer n)
	/* FUNCTION ADDED BY JOHFRA */
	/* rounds d to n decimals */
	{
		BigDecimal bd = new BigDecimal(d);
		bd = bd.setScale(n, BigDecimal.ROUND_HALF_UP);
		d = bd.doubleValue();
		return d;
	}

	/**
	 * Creates a PDM design with identifier 'id'
	 * 
	 * @param id
	 *            String
	 */
	public PDMDesign(String id) {
		this.designID = id;
		// String[] tableHeader = {"Activity", "Activity Cohesion",
		// "Relation Cohesion (lambda)", "Information Cohesion (mu)"};
	}

	/**
	 * Adds an activity to the list of activities.
	 * 
	 * @param activity
	 *            PDMActivity
	 */
	public void addActivity(PDMActivity activity) {
		activities.put(activity.getID(), activity);
	}

	/**
	 * Returns the identifier 'designID' of this PDM design.
	 * 
	 * @return String
	 */
	public String getID() {
		return designID;
	}

	public HashMap getActivities() {
		return activities;
	}

	/**
	 * Calculates the process cohesion for this design, by adding the cohesion
	 * values for every activity and dividing that number by the total number of
	 * activities.
	 * 
	 * @return Double
	 */
	public Double calculateProcessCohesion() {
		Double result = new Double(0.0);
		Double actcoh = new Double(0.0);
		Double relcoh = new Double(0.0);
		Double infcoh = new Double(0.0);
		// tableHeader = ["Activity", "Activity Cohesion",
		// "Relation Cohesion (lambda)", "Information Cohesion (mu)"];
		PDMActivity activity;
		Object[] array = new Object[1];
		array = activities.values().toArray();
		tableContent = new Object[(activities.size() + 2)][4];
		for (int i = 0; i < array.length; i++) {
			if (array[i] instanceof PDMActivity) {
				activity = (PDMActivity) array[i];
				relcoh = activity.calculateActivityRelationCohesion();
				infcoh = activity.calculateActivityInformationCohesion();
				actcoh = infcoh * relcoh;
				tableContent[i + 1][0] = activity.getID();
				// System.out.println(tableContent[i+1][0]);
				tableContent[i + 1][1] = RoundDouble(actcoh, 3).toString();
				// System.out.println(tableContent[i+1][1]);
				tableContent[i + 1][2] = RoundDouble(relcoh, 3).toString();
				// System.out.println(tableContent[i+1][2]);
				tableContent[i + 1][3] = RoundDouble(infcoh, 3).toString();
				// System.out.println(tableContent[i+1][3]);
				result = result + actcoh;
			}
		}
		result = (result / (array.length));
		return RoundDouble(result, 3);
	}

	/**
	 * Calculates the value for process coupling of the design.
	 * 
	 * @return Double
	 */
	public Double calculateProcessCoupling() {
		Double result = new Double(0.0);
		Double sum = new Double(0.0);
		int s = activities.size();
		if (s > 1) {
			Object[] act = new Object[1];
			act = activities.values().toArray();
			for (int i = 0; i < s; i++) {
				PDMActivity act1 = (PDMActivity) act[i];
				for (int j = 0; j < s; j++) {
					PDMActivity act2 = (PDMActivity) act[j];
					if (act1.isConnectedWith(act2)) {
						sum = sum + 1.0;
						// System.out.println(sum);
					}
				}
			}
			result = sum / (s * (s - 1));
		} else
			result = 0.0;

		return RoundDouble(result, 3);
	}

	/**
	 * Calculates the value for the coupling/cohesion ration of the design.
	 * 
	 * @return Double
	 */
	public Double calculateProcessRatio() {
		Double result = new Double(0.0);
		result = (calculateProcessCoupling() / calculateProcessCohesion());
		return RoundDouble(result, 3);
	}

	/**
	 * Returns the header of the table with the values for activity cohesion for
	 * every activity.
	 * 
	 * @return String[]
	 */
	public String[] getTableHeader() {
		return tableHeader;
	}

	/**
	 * Returns the content of the table with the values for activity cohesion
	 * for every activity.
	 * 
	 * @return Object[][]
	 */
	public Object[][] getTableContent() {
		return tableContent;
	}

}

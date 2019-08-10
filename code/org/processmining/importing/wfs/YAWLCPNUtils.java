package org.processmining.importing.wfs;

import org.processmining.analysis.petrinet.cpnexport.CpnUtils;

/**
 * Helper class for converting a WFState format to the corresponding SML initial
 * state file.
 * 
 * @author Moe Wynn (m.wynn at qut.edu.au)
 */
public class YAWLCPNUtils {

	/* The label added to a YAWL TaskID in ProM */
	public static String TASK_LABEL = "TASK_";
	/* The label added to a YAWL CONDID in ProM */
	public static String COND_LABEL = "COND_";
	/* The label added to an executing YAWL Task in ProM */
	public static String EXEC_LABEL = "E";
	/* The main page name in CPN model */
	public static String MAINPG_LABEL = "Process";
	/* The page and condition name for YAWL input condition */
	public static String INPUTCOND_FULLNAME = "Overview`Start";
	/* The page and condition name for YAWL input condition */
	public static String OUTPUTCOND_FULLNAME = "Overview`Start";

	/**
	 * Returns a valid name from ProM for YAWL elements.
	 * 
	 * @param name
	 *            String the original name in YAWL
	 * @elementType one of TASK, COND, EXEC to determine the YAWL element type
	 * @return String a name which is prefix with an appropriate label from ProM
	 */
	public static String getYawlCpnValidName(String name, String elementType) {
		if (name != null && name.equals("") == false) {
			String label = "";
			if (elementType.equalsIgnoreCase("TASK")) {
				label = TASK_LABEL;
			} else if (elementType.equalsIgnoreCase("COND")) {
				label = COND_LABEL;
			}
			name = CpnUtils.getCpnValidName(name);
			name = label + name;

			return name;
		} else
			return "";
	}

	/**
	 * Returns a valid name from ProM for YAWL elements including pagename.
	 * 
	 * @param name
	 *            String the original name in YAWL
	 * @elementType one of ENABLE, EXEC to determine the YAWL element type
	 * @return String a name which is prefix with an appropriate label from
	 *         ProM/CPN
	 */
	public static String getYawlCpnValidConditionName(String name,
			String elementType) {
		if (name != null && name.equals("") == false) {

			if (elementType.equalsIgnoreCase("ENABLE")) {
				name = MAINPG_LABEL + "`" + getYawlCpnValidName(name, "COND");

				/*
				 * Input and Output conditions are special cases. The token
				 * should be put into the Overview CPN page instead.
				 */
				if (name.startsWith("Process`COND_InputCondition_")) {
					name = INPUTCOND_FULLNAME;
				} else if (name.startsWith("Process`COND_OutputCondition_")) {
					name = OUTPUTCOND_FULLNAME;
				}
			} else if (elementType.equalsIgnoreCase("EXEC")) {
				name = getYawlCpnValidName(name, "TASK") + "`" + EXEC_LABEL;
			}
			return name;

		} else
			return "";
	}

}

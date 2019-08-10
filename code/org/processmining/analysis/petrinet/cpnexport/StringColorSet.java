package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Defines a string color set for cpn tools. For a string color set it also
 * possible to have some restrictions (like providing a range for the values
 * that are available), but this is not implemented in this class.
 * 
 * @author rmans
 * @author arozinat
 */
public class StringColorSet extends CpnColorSet {
	/**
	 * Is this string color set timed or not.
	 */
	private boolean myTimed = false;

	/**
	 * Default constructor
	 * 
	 * @param name
	 *            String the name of the string color set
	 */
	public StringColorSet(String name) {
		super(name);
	}

	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + CpnUtils.getCpnValidName(myNameColorSet)
				+ "</id>\n");
		if (myTimed) {
			bw.write("\t\t\t\t<timed/>\n");
		}
		bw.write("\t\t\t\t<alias>\n" + "\t\t\t\t\t<id>" + "STRING" + "</id>\n"
				+ "\t\t\t\t</alias>\n" + "\t\t\t\t<layout>" + "colset "
				+ CpnUtils.getCpnValidName(myNameColorSet) + " = " + "STRING");
		if (myTimed) {
			bw.write(" timed");
		}
		bw.write(";</layout>\n" + "\t\t\t</color>\n");
	}

	/**
	 * Indicates whether this string color set is timed or not.
	 * 
	 * @param timed
	 *            boolean <code>true</code> when this string color set needs to
	 *            be timed, <code>false</code> otherwise.
	 */
	public void setTimed(boolean timed) {
		myTimed = timed;
	}

}

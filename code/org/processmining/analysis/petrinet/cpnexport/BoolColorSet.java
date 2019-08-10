package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Defines a bool color set for cpn tools. For a bool color set it also possible
 * to have some restrictions (like providing a range for the values that are
 * available), but this is not implemented in this class.
 * 
 * @author rmans
 * @author arozinat
 */
public class BoolColorSet extends CpnColorSet {

	/**
	 * Default constructor
	 * 
	 * @param name
	 *            String the name of the bool color set
	 */
	public BoolColorSet(String name) {
		super(name);
	}

	/**
	 * Is this bool color set timed or not.
	 */
	private boolean myTimed = false;

	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + CpnUtils.getCpnValidName(myNameColorSet)
				+ "</id>\n");
		if (myTimed) {
			bw.write("\t\t\t\t<timed/>\n");
		}
		bw.write("\t\t\t\t<alias>\n" + "\t\t\t\t\t<id>" + "BOOL" + "</id>\n"
				+ "\t\t\t\t</alias>\n" + "\t\t\t\t<layout>" + "colset "
				+ CpnUtils.getCpnValidName(myNameColorSet) + " = " + "BOOL");
		if (myTimed) {
			bw.write(" timed");
		}
		bw.write(";</layout>\n" + "\t\t\t</color>\n");
	}

	/**
	 * Indicates whether this bool color set is timed or not.
	 * 
	 * @param timed
	 *            boolean <code>true</code> when this bool color set needs to be
	 *            timed, <code>false</code> otherwise.
	 */
	public void setTimed(boolean timed) {
		myTimed = timed;
	}
}

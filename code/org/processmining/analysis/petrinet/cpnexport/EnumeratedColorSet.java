package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class represents an enumerated color set in CPN tools
 * 
 * @author rmans
 * @author arozinat
 */
public class EnumeratedColorSet extends CpnColorSet {

	/**
	 * The possible values in a enumerated colorset.
	 */
	private HashSet<String> possibleValues = new HashSet<String>();

	/**
	 * default constructor
	 */
	public EnumeratedColorSet() {
		this("");
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            String the name of the enumerated colorset
	 */
	public EnumeratedColorSet(String name) {
		super(name);
	}

	/**
	 * Adds a value to the list of possible values that are available for the
	 * enumerated colorset. If a similar value already exists, it will not be
	 * added again
	 * 
	 * @param value
	 *            String the value to be added.
	 */
	public void addPossibleValue(String value) {
		possibleValues.add(value);
	}

	/**
	 * Writes the colorset to the CPN file (declarations part).
	 * 
	 * @param bw
	 *            BufferedWriter
	 * @param idMan
	 *            ManagerID
	 * @throws IOException
	 */
	public void write(BufferedWriter bw) throws IOException {
		// enumeration type
		String enumVals = "";
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + CpnUtils.getCpnValidName(myNameColorSet)
				+ "</id>\n" + "\t\t\t\t\t<enum>\n");
		Iterator<String> values = possibleValues.iterator();
		while (values.hasNext()) {
			String val = values.next();
			val = CpnUtils.getCpnValidName(val);
			enumVals = enumVals + " " + val + " |";
			bw.write("\t\t\t\t\t\t<id>" + val + "</id>\n");
		}
		// remove the last | from enumVals
		if ((enumVals.length() - 2) >= 0) {
			enumVals = enumVals.substring(0, enumVals.length() - 2);
		}
		bw.write("\t\t\t\t\t</enum>\n" + "\t\t\t\t\t<layout>" + "colset "
				+ CpnUtils.getCpnValidName(myNameColorSet) + " = with "
				+ enumVals.trim() + ";</layout>\n" + "\t\t\t</color>\n");
	}

}

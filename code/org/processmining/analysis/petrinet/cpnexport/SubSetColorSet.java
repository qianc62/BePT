package org.processmining.analysis.petrinet.cpnexport;

import java.util.HashSet;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Defines a subset color set of CPN tools
 * 
 * @author rmans
 * @author arozinat
 * 
 */
public class SubSetColorSet extends CpnColorSet {

	/**
	 * The possible value in a sub set colorset
	 */
	private HashSet<String> myPossibleValues = new HashSet<String>();

	/**
	 * the name of the sub set color set.
	 */
	private String mySubSetName;

	/**
	 * Default constructor
	 */
	public SubSetColorSet() {
		this("", "");
	}

	/**
	 * Constructor
	 * 
	 * @param nameColorSet
	 *            String the name of the subset color set
	 * @param subSetName
	 *            String the type of all the values in the subset color set.
	 */
	public SubSetColorSet(String nameColorSet, String subSetName) {
		super(nameColorSet);
		mySubSetName = subSetName;
	}

	/**
	 * Adds a value to this list of values in this subset.
	 * 
	 * @param value
	 *            String the value to be added.
	 */
	public void addPossibleValue(String value) {
		myPossibleValues.add(value);
	}

	/**
	 * Obtains all the values of this subset color set
	 * 
	 * @return HashSet a list of values. May be empty.
	 */
	public HashSet<String> getPossibleValues() {
		return myPossibleValues;
	}

	/**
	 * Retrieves an initial marking based on the possible values that have been
	 * declared for this subset color set.
	 * 
	 * @return String the initial marking.
	 */
	public String getInitMarking() {
		String initMarking = "";
		Iterator<String> posVals = myPossibleValues.iterator();
		while (posVals.hasNext()) {
			String posVal = CpnUtils.getCpnValidName(posVals.next());
			initMarking = initMarking + "&quot;" + posVal + "&quot;,\n";
		}
		// remove the last ,\n from initMarking
		if ((initMarking.length() - 2) > 0) {
			initMarking = initMarking.substring(0, initMarking.length() - 2);
		}
		initMarking = "[" + initMarking + "]";
		return initMarking;
	}

	/**
	 * The type that belongs to all the values that belong to this subset color
	 * set.
	 * 
	 * @param subSetName
	 *            String
	 */
	public void setSubSetName(String subSetName) {
		mySubSetName = subSetName;
	}

	public void write(BufferedWriter bw) throws IOException {
		String mlPart = "";
		Iterator<String> posVal = myPossibleValues.iterator();
		while (posVal.hasNext()) {
			String val = CpnUtils.getCpnValidName(posVal.next());
			mlPart = mlPart + "&quot;" + val + "&quot;, ";
		}
		// remove the last ,
		if ((mlPart.length() - 2) >= 0) {
			mlPart = mlPart.substring(0, mlPart.length() - 2);
		}
		mlPart = "[" + mlPart + "]";

		// write the colorset
		bw.write("\t\t\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t\t<id>"
				+ CpnUtils.getCpnValidName(getNameColorSet()) + "</id>\n"
				+ "\t\t\t\t\t\t<subset>\n" + "\t\t\t\t\t\t\t<id>"
				+ CpnUtils.getCpnValidName(mySubSetName) + "</id>\n"
				+ "\t\t\t\t\t\t\t<with>\n");
		bw.write("\t\t\t\t\t\t\t\t<ml>" + mlPart + "</ml>\n");
		bw.write("\t\t\t\t\t\t\t</with>\n" + "\t\t\t\t\t\t</subset>\n"
				+ "\t\t\t\t\t\t<layout>" + "colset "
				+ CpnUtils.getCpnValidName(myNameColorSet) + " = subset "
				+ CpnUtils.getCpnValidName(mySubSetName) + " with " + mlPart
				+ ";</layout>\n" + "\t\t\t\t\t</color>\n");
	}

}

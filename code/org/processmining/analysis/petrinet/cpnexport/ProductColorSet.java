package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Defines a product color set for CPN tools
 * 
 * @author rmans
 * @author arozinat
 */
public class ProductColorSet extends CpnColorSet {

	/**
	 * the names of the colorsets that form this product colorset.
	 */
	private ArrayList<String> myNamesColorSets = new ArrayList<String>();

	/**
	 * Needs this product color set to be timed or not.
	 */
	private boolean myTimed = false;

	/**
	 * default constructor
	 */
	public ProductColorSet() {
		this("");
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            String the name of the product color set.
	 */
	public ProductColorSet(String name) {
		super(name);
	}

	/**
	 * Set the name of this product color set.
	 * 
	 * @param name
	 *            String the name
	 */
	public void setName(String name) {
		myNameColorSet = name;
	}

	/**
	 * Adds a name of a colorset that together with the other names of colorsets
	 * has to form this product color set.
	 * 
	 * @param nameColorSet
	 *            String the name of the color set to be added.
	 */
	public void addNameColorSet(String nameColorSet) {
		myNamesColorSets.add(nameColorSet);
	}

	/**
	 * Indicates whether this product color set needs to be timed.
	 * 
	 * @param timed
	 *            boolean
	 */
	public void setTimed(boolean timed) {
		myTimed = timed;
	}

	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + CpnUtils.getCpnValidName(myNameColorSet)
				+ "</id>\n");
		if (myTimed) {
			bw.write("<timed/>\n");
		}
		bw.write("\t\t\t\t<product>\n");
		String csetString = "";
		// write names of the colorsets for the product
		Iterator<String> colorSets = myNamesColorSets.iterator();
		while (colorSets.hasNext()) {
			String colorSet = CpnUtils.getCpnValidName(colorSets.next());
			bw.write("\t\t\t\t\t<id>" + colorSet + "</id>\n");
			csetString = csetString + " " + colorSet + " *";
		}

		// remove the last * from csetString
		if ((csetString.length() - 2) >= 0) {
			csetString = csetString.substring(0, csetString.length() - 2);
		}
		bw.write("\t\t\t\t</product>\n");
		if (myTimed) {
			bw.write("\t\t\t\t<layout>" + "colset "
					+ CpnUtils.getCpnValidName(myNameColorSet) + " = product "
					+ csetString + " timed;</layout>\n" + "\t\t\t</color>\n");
		} else {
			bw.write("\t\t\t\t<layout>" + "colset "
					+ CpnUtils.getCpnValidName(myNameColorSet) + " = product "
					+ csetString + ";</layout>\n" + "\t\t\t</color>\n");
		}
	}

}

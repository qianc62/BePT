package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Defines a list color set for cpn tools.
 */
public class ListColorSet extends CpnColorSet {

	protected String myType;

	/**
	 * Creates a new list color set for the given type.
	 * 
	 * @param name
	 *            the name of the list type
	 * @param type
	 *            the type of which the list is composed (e.g., "BOOL")
	 */
	public ListColorSet(String name, String type) {
		super(name);
		myType = type;
	}

	@Override
	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + CpnUtils.getCpnValidName(myNameColorSet)
				+ "</id>\n");
		bw.write("\t\t\t\t<list>\n" + "\t\t\t\t\t<id>" + myType + "</id>\n"
				+ "\t\t\t\t</list>\n" + "\t\t\t\t<layout>" + "colset "
				+ CpnUtils.getCpnValidName(myNameColorSet) + " = " + "list "
				+ myType);
		bw.write(";</layout>\n" + "\t\t\t</color>\n");
	}

}

package org.processmining.analysis.petrinet.cpnexport;

import java.io.IOException;
import java.io.BufferedWriter;

/**
 * This class represents a cpn var and its type
 * 
 * @author rmans
 * @author arozinat
 */
public class CpnVarAndType {

	/**
	 * the name of the variable
	 */
	private String myVarName;

	/**
	 * the type of the variable
	 */
	private String myTypeName;

	/**
	 * Constructor for creating a cpn var with its accompanying type
	 * 
	 * @param varName
	 *            String the variable name
	 * @param typeName
	 *            String the type
	 */
	public CpnVarAndType(String varName, String typeName) {
		myVarName = varName;
		myTypeName = typeName;
	}

	/**
	 * Returns the name of the variable
	 * 
	 * @return String the name of the variable
	 */
	public String getVarName() {
		return myVarName;
	}

	/**
	 * Returns the type of the variable
	 * 
	 * @return String the type of the variable
	 */
	public String getTypeName() {
		return myTypeName.toUpperCase();
	}

	/**
	 * Writes this cpn variable and type to the cpn file (declarations part).
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<var id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<type>\n" + "\t\t\t\t\t<id>"
				+ CpnUtils.getCpnValidName(myTypeName) + "</id>\n"
				+ "\t\t\t\t</type>\n" + "\t\t\t\t<id>"
				+ CpnUtils.getCpnValidName(myVarName) + "</id>\n"
				+ "\t\t\t\t<layout>" + "var " + myVarName + " : " + myTypeName
				+ ";" + "</layout>\n" + "\t\t\t</var>\n");
	}

	/**
	 * Returns the complete cpn compliant representation for the variable and
	 * its type
	 * 
	 * @return String the cpn compliant representation for the variable and its
	 *         type
	 */
	public String toString() {
		return "var " + myVarName + " : " + myTypeName + ";";
	}

	/**
	 * Compares the specified object with this CpnVarAndType for equality.
	 * Returns true if both are a CpnVarAndType object and have the same
	 * variable name.
	 * 
	 * @param o
	 *            Object object to be compared for equality with this
	 *            CpnVarAndType.
	 * @return boolean if the specified object is equal to this CpnVarAndType.
	 */
	public boolean equals(Object o) {
		return (o instanceof CpnVarAndType && ((CpnVarAndType) o).getVarName()
				.equals(getVarName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + myVarName.hashCode();
		return result;
	}

}

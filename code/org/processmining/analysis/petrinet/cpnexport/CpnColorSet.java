package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An abstract class that defines the structure for each colorset that is
 * available in CPN tools. In the case that a new color set has to be defined,
 * only the write method needs to be implemented, because all the other
 * properties hold for each colorset in CPN tools. If needed any inheriting
 * class may be defined its own additional properties and methods
 * 
 * @author rmans
 * @author arozinat
 */
public abstract class CpnColorSet {

	/**
	 * the name of the colorset
	 */
	protected String myNameColorSet;

	/**
	 * Constructor needs to be called from sub classes.
	 * 
	 * @param nameColorSet
	 */
	protected CpnColorSet(String nameColorSet) {
		myNameColorSet = nameColorSet;
	}

	/**
	 * Retrieves the name of the colorset
	 * 
	 * @return String the name of the colorset
	 */
	public String getNameColorSet() {
		return myNameColorSet;
	}

	/**
	 * Sets the name of the colorset.
	 * 
	 * @param name
	 *            String the name of the colorset.
	 */
	public void setNameColorSet(String name) {
		myNameColorSet = name;
	}

	/**
	 * Compares this object with an other CpnColorSet object for equality.
	 * 
	 * @param o
	 *            Object the object that needs to be compared with this object.
	 * @return boolean true, if both objects are of the same type and have the
	 *         same name.
	 */
	public boolean equals(Object o) {
		return (o instanceof CpnColorSet && ((CpnColorSet) o).getNameColorSet()
				.equals(getNameColorSet()));
	}

	/**
	 * Calculates the hashcode.
	 * 
	 * @return int the hashcode.
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + getNameColorSet().hashCode();
		return result;
	}

	/**
	 * This method needs to be implemented, so that each colorset can write
	 * itself to the cpn-file (for the declarations part).
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public abstract void write(BufferedWriter bw) throws IOException;

}

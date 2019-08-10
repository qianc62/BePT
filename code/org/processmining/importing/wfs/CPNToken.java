package org.processmining.importing.wfs;

/**
 * Helper class in generating CPN token values in the current state file.
 * 
 * @author Moe Wynn (m.wynn at qut.edu.au)
 */
public class CPNToken {

	private String elementID = "";
	private String caseID = "";
	private String resource = "";
	private int count = 1;
	private String startTime = "0";

	public CPNToken() {
	}

	public CPNToken(String elementID, String caseID, String resource,
			int count, String startTime) {
		this.elementID = elementID;
		this.caseID = caseID;
		this.resource = resource;
		this.count = count;
		this.startTime = startTime;
	}

	public String getEleID() {
		return elementID;
	}

	public String getCaseID() {
		return caseID;
	}

	public String getResource() {
		return resource;
	}

	public int getCount() {
		return count;
	}

	public String getStartTime() {
		return startTime;
	}

	/**
	 * Convert a token into a string format.
	 * 
	 * @return a string representing the token value in the form of
	 *         (caseID,startTime,resource)
	 */
	public String convertToCPNTokenFormat() { // Format (1,"0","Moe")
		String result = "";
		if (resource == "") { // it could be for enabled conditions or tasks
			// without resources
			result = "(" + caseID + ",\"" + startTime + "\")";
		} else {
			result = "(" + caseID + ",\"" + startTime + "\",\"" + resource
					+ "\")";
		}
		return result;
	}

}

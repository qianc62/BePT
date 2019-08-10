package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Defines a record color set in CPN tools.
 * 
 * @author rmans
 * @author arozinat
 */
public class RecordColorSet extends CpnColorSet {

	/**
	 * The name and the name of the colorset for each record.
	 */
	private HashMap<String, String> myRecords = new HashMap<String, String>();

	/**
	 * default constructor
	 */
	public RecordColorSet() {
		this("");
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            String the name of this record colorset
	 */
	public RecordColorSet(String name) {
		super(name);
	}

	/**
	 * Adds a record to this record color set. A record has to consist of a name
	 * and the name of a colorset.
	 * 
	 * @param nameRecord
	 *            String the name of the record.
	 * @param csNameRecord
	 *            String the name of the color set.
	 */
	public void addRecord(String nameRecord, String csNameRecord) {
		myRecords.put(nameRecord, csNameRecord);
	}

	public void write(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t<id>" + myNameColorSet + "</id>\n"
				+ "\t\t\t\t<declare>\n" + "\t\t\t\t\t<id>set</id>\n"
				+ "\t\t\t\t</declare>" + "\t\t\t\t<record>\n");
		String recordsString = "";
		Iterator<String> records = myRecords.keySet().iterator();
		while (records.hasNext()) {
			String key = CpnUtils.getCpnValidName(records.next());
			String value = CpnUtils.getCpnValidName(myRecords.get(key));
			bw.write("\t\t\t\t\t<recordfield>\n" + "\t\t\t\t\t\t<id>" + key
					+ "</id>\n" + "\t\t\t\t\t\t<id>" + value + "</id>\n"
					+ "\t\t\t\t\t</recordfield>\n");
			recordsString = recordsString + " " + key + ":" + value + " *";
		}
		// remove the last * from recordsString
		if ((recordsString.length() - 2) >= 0) {
			recordsString = recordsString.substring(0,
					recordsString.length() - 2);
		}
		bw
				.write("\t\t\t\t</record>\n" + "\t\t\t\t<layout>" + "colset "
						+ CpnUtils.getCpnValidName(myNameColorSet)
						+ " = record " + recordsString
						+ " declare set;</layout>\n" + "\t\t\t</color>");
	}

}

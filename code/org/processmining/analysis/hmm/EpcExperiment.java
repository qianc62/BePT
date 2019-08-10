package org.processmining.analysis.hmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.processmining.exporting.epcs.EpmlExport;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.importing.epml.EpmlImport;
import org.processmining.mining.epcmining.EPCResult;

/**
 * Custom Helper class reading a set of EPC models from the folder "EPCExp" and
 * testing it for its convertability to a Simple Petri net. <br>
 * Was used to select suitable models from the SAP reference model.
 */
public class EpcExperiment {

	public static void evaluateEPCs() {
		new File("EPCExp").mkdir();
		File dir = new File("EPCs");
		File[] files = dir.listFiles();
		int totalNo = 0;
		int counter = 0;
		int problems = 0;
		try {
			for (File file : files) {
				totalNo++;
				String fileName = file.getName();
				FileInputStream stream = new FileInputStream(file);
				EpmlImport importplugin = new EpmlImport();
				try {
					EPCResult epcResult = (EPCResult) importplugin
							.importFile(stream);
					ConfigurableEPC epc = epcResult.getEPC();
					boolean isOK = true;
					for (EPCConnector conn : epc.getConnectors()) {
						if (conn.getType() != EPCConnector.XOR) {
							counter++;
							isOK = false;
							break;
						}
					}
					if (isOK == true) {
						File expfile = new File("EPCExp" + "/" + fileName);
						FileOutputStream expstream = new FileOutputStream(
								expfile);
						EpmlExport epcexport = new EpmlExport();
						epcexport.export(new ProvidedObject("EPC Hierarchy",
								new Object[] { epc }), expstream);
					}
				} catch (Exception ex) {
					problems++;
				}
			}
			Message.add("No. of Non-Xor models: " + counter
					+ ", No. of problems: " + problems
					+ " Total No. of models: " + totalNo);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

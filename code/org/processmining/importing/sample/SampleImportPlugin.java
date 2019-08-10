package org.processmining.importing.sample;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.dot.DotModel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.importing.Importer;
import org.processmining.importing.pnml.PnmlImport;
import org.processmining.mining.epcmining.EPCResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class SampleImportPlugin {
	@Importer(name = "Sample DOT importer", extension = "dot")
	public static ModelGraphPanel importDot(InputStream input)
			throws IOException {
		return new DotModel(read(input)).getGrappaVisualization();
	}

	public static String read(InputStream input) throws IOException {
		StringBuffer buffer = new StringBuffer();
		int c;
		while ((c = input.read()) != -1) {
			buffer.append((char) c);
		}
		return buffer.toString();
	}

	@Importer(name = "Sample Petri net importer", extension = "pnml", connectToLog = true)
	public static PetriNetResult importNet(InputStream input)
			throws IOException {
		PetriNetResult result = (PetriNetResult) new PnmlImport()
				.importFile(input);
		return result;
	}
}

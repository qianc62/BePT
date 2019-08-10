package org.processmining.importing.petrify;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class Petrify2PetriNet implements LogReaderConnectionImportPlugin {
	public Petrify2PetriNet() {
	}

	public String getName() {
		return "Petrify file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("pn");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			PetriNet net = PetrifyReader.read(input);

			PetriNetResult result = new PetriNetResult(net);
			return result;
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}

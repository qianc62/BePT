package org.processmining.importing.coloredcontrolflownet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.filechooser.FileFilter;

import org.processmining.importing.ImportPlugin;
import org.processmining.mining.MiningResult;

public class ColoredControlFlowNetImport implements ImportPlugin {

	public FileFilter getFileFilter() {
		return new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith("ccfn.cpn");
			}

			@Override
			public String getDescription() {
				return "Colored Control Flow Net";
			}
		};
	}

	public MiningResult importFile(InputStream input) throws IOException {
		ColoredControlFlowNetParser parser = new ColoredControlFlowNetParser();
		return parser.parse(input);
	}

	public String getHtmlDescription() {
		return "<h3>Colored Control Flow Net importer</h3>Import an Erlang net into ProM. Colored Control Flow Nets are made"
				+ " using CPN Tools, and are recognized by their file extension is ccfn.cpn. "
				+ "Each top page in the file is imported as a single Colored Control Flow Net.";
	}

	public String getName() {
		return "Colored Control Flow Net";
	}

}

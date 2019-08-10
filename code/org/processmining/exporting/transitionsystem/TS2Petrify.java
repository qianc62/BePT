package org.processmining.exporting.transitionsystem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */

public class TS2Petrify implements ExportPlugin {

	public TS2Petrify() {
	}

	public String getName() {
		return "TS to Petrify";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof TransitionSystem) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		TransitionSystem ts = null;
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++)
			if (o[i] instanceof TransitionSystem)
				ts = (TransitionSystem) o[i];
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));

		TSWriter.writeToPetrify(ts, bw);
		bw.close();
	}

	public String getFileExtension() {
		return "g";
	}

	public String getHtmlDescription() {
		return "";
	}
}

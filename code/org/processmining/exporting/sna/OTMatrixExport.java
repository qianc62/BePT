package org.processmining.exporting.sna;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.analysis.originator.OTMatrix2DTableModel;
import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

public class OTMatrixExport implements ExportPlugin {

	public OTMatrixExport() {
	}

	public String getName() {
		return "OTMatrix";
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/otmatrixexport";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OTMatrix2DTableModel) {
				return true;
			}
		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {

		Object[] o = object.getObjects();
		OTMatrix2DTableModel otMatrix = null;
		String[] users = null;

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OTMatrix2DTableModel) {
				otMatrix = (OTMatrix2DTableModel) o[i];
				// / PLUGIN TEST START
				Message.add("<OriginatorByTaskMatrixExport>", Message.TEST);
				otMatrix.writeToTestLog();
				Message.add("<SummaryOfMatrix NumberOfUsers=\""
						+ otMatrix.getSumOfOTMatrix() + "\">", Message.TEST);
				Message.add("</OriginatorByTaskMatrixExport>", Message.TEST);
				// PLUGIN TEST END
			}
		}

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof OTMatrix2DTableModel) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));

				for (int j = 0; j < otMatrix.getColumnCount(); j++) {
					bw.write(otMatrix.getColumnName(j) + "\t");
				}

				bw.write("\n");

				for (int j = 0; j < otMatrix.getRowCount(); j++) {
					for (int k = 0; k < otMatrix.getColumnCount(); k++)
						bw.write(otMatrix.getValueAt(j, k) + "\t");
					bw.write("\n");
				}

				bw.write("End\n");

				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "txt";
	}
}

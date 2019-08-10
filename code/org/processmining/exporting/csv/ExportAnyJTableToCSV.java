package org.processmining.exporting.csv;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.table.AbstractTableModel;

import org.processmining.exporting.Exporter;

public class ExportAnyJTableToCSV {

	@Exporter(name = "Standard CSV (Excel)", help = "Export a table to a CSV file. This file can be opened in Microsoft Excel or any other spreadsheet application.", extension = "csv")
	public static void export(AbstractTableModel model, OutputStream out)
			throws IOException {
		final byte[] newline = System.getProperty("line.separator").getBytes();
		final byte[] separator = ";".getBytes();

		for (int col = 0; col < model.getColumnCount(); col++) {
			if (col > 0) {
				out.write(separator);
			}
			out.write(quote(model.getColumnName(col)));
		}
		out.write(newline);
		for (int row = 0; row < model.getRowCount(); row++) {
			for (int col = 0; col < model.getColumnCount(); col++) {
				if (col > 0) {
					out.write(separator);
				}
				out.write(quote(model.getValueAt(row, col).toString()));
			}
			out.write(newline);
		}
	}

	private static byte[] quote(String s) {
		return ("\"" + s.replace("\"", "\"\"") + "\"").getBytes();
	}
}

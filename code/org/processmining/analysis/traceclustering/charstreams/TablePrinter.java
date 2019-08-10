package org.processmining.analysis.traceclustering.charstreams;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class TablePrinter {
	private PrintStream stream;

	private List<Integer> columnWidths;

	private int column;

	public TablePrinter(Integer... widths) {
		this(null, widths);
	}

	public TablePrinter(PrintStream stream, Integer... widths) {
		if (widths.length == 0) {
			throw new IllegalArgumentException(
					"you must provide at least one column");
		}

		int sum = 0;

		for (int i = 0; i < widths.length; i++) {
			if (widths[i] < 0) {
				throw new IllegalArgumentException(
						"negative widths are forbidden");
			} else {
				sum += widths[i];

				if (i > 0) {
					// for the space between two columns

					sum++;
				}
			}
		}

		// if (sum > 200) {
		// throw new IllegalArgumentException
		// ("the widths cannot exceed 80 characters");
		// }

		this.stream = (stream == null) ? System.out : stream;

		columnWidths = Arrays.<Integer> asList(widths);
		column = 0;
	}

	public void print(String value) {
		if (column == columnWidths.size()) {
			throw new RuntimeException(
					"all columns in this row have been printed - use newLine");
		}

		int length = value.length();
		int width = columnWidths.get(column);

		if (length > width) {
			throw new IllegalArgumentException("the value's length " + length
					+ " exceeds this column's width");
		} else {
			if (column > 0) {
				stream.print(" ");
			}

			stream.print(getSpaces(width - length));
			stream.print(value);

			column++;
		}
	}

	private String getSpaces(int amount) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < amount; i++) {
			builder.append(" ");
		}

		return builder.toString();
	}

	public void newLine() {
		stream.println();

		column = 0;
	}
}

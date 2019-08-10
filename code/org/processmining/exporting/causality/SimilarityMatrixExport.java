package org.processmining.exporting.causality;

import java.io.*;
import java.util.*;

import org.processmining.analysis.causality.*;
import org.processmining.exporting.*;
import org.processmining.framework.models.epcpack.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class SimilarityMatrixExport {

	private static final byte[] COMMA = "\t".getBytes();

	@Exporter(name = "Similarity-matrix Export", extension = "txt")
	public static void export(final FootprintSimilarityResult similarityMatrix,
			final OutputStream out) throws IOException {
		// Just write the output.
		List<String> basePaths = similarityMatrix.getBasePaths();
		List<ConfigurableEPC> baseEpcs = similarityMatrix.getBaseEpcs();
		List<String> compPaths = similarityMatrix.getCompareToPaths();
		List<ConfigurableEPC> compEpcs = similarityMatrix.getCompareToEpcs();
		double[][] similarities = similarityMatrix.getSimilarities()
				.getSimilarities();

		// title
		out.write(similarityMatrix.getSimilarities().getTitle().getBytes());
		out.write("\n".getBytes());

		// header row

		out.write("EPC path".getBytes());
		out.write(COMMA);
		out.write("EPC identifier".getBytes());
		out.write(COMMA);
		for (int i = 0; i < compEpcs.size(); i++) {
			out.write(compEpcs.get(i).getIdentifier().getBytes());
			out.write(COMMA);
		}
		out.write("\n".getBytes());

		// paths row
		out.write(COMMA);
		out.write(COMMA);
		out.write(COMMA);
		for (int i = 0; i < compEpcs.size(); i++) {
			out.write(compPaths.get(i).getBytes());
			out.write(COMMA);
		}
		out.write("\n".getBytes());

		// data
		for (int row = 0; row < baseEpcs.size(); row++) {
			out.write(basePaths.get(row).getBytes());
			out.write(COMMA);
			out.write(baseEpcs.get(row).getIdentifier().getBytes());
			out.write(COMMA);
			for (int col = 0; col < compEpcs.size(); col++) {
				out.write(Double.toString(similarities[row][col]).getBytes());
				out.write(COMMA);
			}
			out.write("\n".getBytes());
		}

	}

}

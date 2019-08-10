/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.exporting.sna;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class AgnaExport implements ExportPlugin {

	public AgnaExport() {
	}

	public String getName() {
		return "AGNA";
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/agnaexport";
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SocialNetworkMatrix) {
				return true;
			}

		}
		return false;
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {

		Object[] o = object.getObjects();
		SocialNetworkMatrix snMatrix = null;
		String[] users = null;
		DoubleMatrix2D matrix = null;

		Message.add("<AGNAExport>", Message.TEST);
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof SocialNetworkMatrix) {

				snMatrix = (SocialNetworkMatrix) o[i];
				users = snMatrix.getNodeNames();
				matrix = snMatrix.getMatrix();

				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));

				int matrixSize = users.length;

				bw
						.write("Agna Data File\nNetwork Name\t exported \nNetwork Size\t");
				bw.write(String.valueOf(matrixSize) + "\n");
				Message.add("<SummaryOfAGNA networkSize=\""
						+ String.valueOf(matrixSize) + "\">", Message.TEST);
				bw.write("Has Viewer\tyes\nNode Names\n");

				for (int j = 0; j < users.length; j++) {
					bw.write(users[j] + "\t");
				}
				bw.write("\nEnd Node Names\nNetwork Matrix\n");
				Message.add("<SummaryOfAGNA writeNodeName=\"OK\">",
						Message.TEST);

				for (int j = 0; j < matrixSize; j++) {
					for (int k = 0; k < matrixSize; k++)
						bw.write(Double.toString(matrix.get(j, k)) + "\t");
					bw.write("\n");
				}
				bw.write("End Network Matrix\n");
				Message.add("<SummaryOfAGNA writeMatrix=\"OK\">", Message.TEST);
				Message.add("</AGNAExport>", Message.TEST);
				bw.close();
				return;
			}
		}
	}

	public String getFileExtension() {
		return "AGN";
	}
}

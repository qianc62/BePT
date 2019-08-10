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

package org.processmining.exporting.petrinet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.algorithms.CpnWriter;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * Exports a given low-level Petri net to a coloured Petri net representation
 * that can be read by CPN Tools.
 * 
 * @see PetriNet
 * @see CpnWriter
 * 
 * @author Anne Rozinat
 */
public class CpnExport implements ExportPlugin {

	/**
	 * Default constructor.
	 */
	public CpnExport() {
	}

	/**
	 * Specifies the name of the plug-in. This is used for, e.g., labelling the
	 * corresponding menu item or the user documentation page.
	 * 
	 * @return the name (and the supported version) of the exported file format
	 */
	public String getName() {
		return "CPN Tools 1.4.0";
	}

	/**
	 * Determines whether a given object can be exported as a CPN.
	 * 
	 * @param object
	 *            the <code>ProvidedObject</code> which shall be tested for
	 *            being a valid input to this export plug-in
	 * @return <code>true</code> if the given object is a <code>PetriNet</code>,
	 *         <code>false</code> otherwise
	 */
	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Triggers the actual export, that is writes the given object as a CPN to
	 * the given output stream.
	 * 
	 * @param object
	 *            the <code>ProvidedObject</code> which shall be exported as a
	 *            CPN
	 * @param output
	 *            the <code>OutputStream</code> specifying the target of the
	 *            exported file
	 * @throws IOException
	 *             in the case a problem is encountered while writing the file
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				CpnWriter.write((PetriNet) o[i], bw);
				bw.close();
				return;
			}
		}
	}

	/**
	 * Specifies the file extension for the exported file.
	 * 
	 * @return the file extension string, that is the part of a file name which
	 *         is following the "." (fileName.fileExtension)
	 */
	public String getFileExtension() {
		return "cpn";
	}

	/**
	 * Provides user documentation for the plug-in.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return "This is a very simple plug-in only exporting the Petri net model as a CPN on one page. "
				+ "For generating a CPN including simulation environment, logging monitors, and with various configuration "
				+ "possibilities use the Analysis plug-in 'Export to CPN Tools 2.0'.";
	}
}

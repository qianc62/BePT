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

package org.processmining.analysis.ltlchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.analysis.ltlchecker.parser.ParseException;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

public class DefaultLTLChecker extends LTLChecker implements AnalysisPlugin {
	/**
	 * A plugin for the ProM framework to check for properties specified in LTL
	 * on workflow logs of processes. It is an analysis plugin, so it can be
	 * used after mining has been done on the log. Needed for this plugin are a
	 * logReader object and a imported LTL Template file by the import plugin.
	 * 
	 * @version 0.2
	 * @author HT de Beer
	 */

	// FIELDS
	/**
	 * The standard filename of the ltl file is `standard.ltl'. This must be in
	 * the lib/plugin directory.
	 */
	public final static String filename = "standard.ltl";

	// CONSTRUCTORS

	public DefaultLTLChecker() {
	}

	// METHODS

	/**
	 * Gets the name of this plugin. This name will be used in the gui of the
	 * framework to select this plugin.
	 * 
	 * @return The name of this plugin.
	 */
	public String getName() {
		return "LTL Checker";
	}

	/**
	 * Gets a description of this plugin in HTML, used by the framework to be
	 * displayed in the help system.
	 * 
	 * @return A description of this plugin in HTML.
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * Returns the input items needed by this analysis algorithm. The framework
	 * uses this information to let the user select appropriate inputs.
	 * 
	 * @return The input items accepted by this analysis algorithm, that is a
	 *         logReader and a imported LTL Template File.
	 */
	public AnalysisInputItem[] getInputItems() {

		AnalysisInputItem[] items = {

		new AnalysisInputItem("Workflow log of a process") {

			public boolean accepts(ProvidedObject object) {

				Object[] o = object.getObjects();
				boolean hasLogReader = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof LogReader) {
						hasLogReader = true;
					}
					;
				}
				;
				return hasLogReader;
			}

		}

		}; // End of new AnalysisInputItem[] inputs

		return items;
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {

		LogReader logReader = null;
		LTLParser ltlParser = null;

		for (int j = 0; j < inputs.length; j++) {

			if (inputs[j].getCaption().equals("Workflow log of a process")) {

				Object[] o = (inputs[j].getProvidedObjects())[0].getObjects();

				for (int i = 0; logReader == null; i++) {
					if (o[i] instanceof LogReader) {
						logReader = (LogReader) o[i];
					}
					;

				}
				;

			}

		}
		;

		String pathToFile = System.getProperty("user.dir") + File.separator
				+ "lib" + File.separator + "plugins" + File.separator
				+ filename;
		// parser

		FileInputStream fis = null;

		try {
			fis = new FileInputStream(pathToFile);
			ltlParser = new LTLParser(fis);
			ltlParser.setFilename(pathToFile);
			ltlParser.init();
			ltlParser.parse();
			fis.close();

			if (UISettings.getInstance().getTest()) {
				Message.add("<parsingLTLfile>", Message.TEST);
				ArrayList names = ltlParser.getVisibleFormulaNames();
				Iterator it = names.iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					Message.add("<formula " + name, Message.TEST);
				}
				Message.add("</parsingLTLfile>", Message.TEST);
			}

			return createGui(logReader, ltlParser, inputs);

		} catch (ParseException pe) {
			JOptionPane.showMessageDialog(null,
					"Error occured during parsing:\n\n\t" + pe.getMessage(),
					"Parse error", JOptionPane.ERROR_MESSAGE);
			return null;

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error while reading " + pathToFile + ". Check"
							+ " the file(name) and try again.", "IO error",
					JOptionPane.ERROR_MESSAGE);
			return null;

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unknown error: "
					+ e.getMessage(), "Unknown error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				JOptionPane
						.showMessageDialog(null, "Unable to close stream:  "
								+ e.getMessage(), "IO error",
								JOptionPane.ERROR_MESSAGE);

			}
		}

	}

	protected JComponent createGui(LogReader log, LTLParser parser,
			AnalysisInputItem[] inputs) {
		return new TemplateGui(log, parser, this, inputs);
	}
}

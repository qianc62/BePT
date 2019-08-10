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

package org.processmining.importing.ltltemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.mining.MiningResult;

public class LTLTemplateImport implements ImportPlugin {

	public LTLTemplateImport() {
	}

	public String getName() {
		return "LTL template file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("ltl");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		// Do the parsing
		try {
			// Open and parse the file

			BufferedReader buf = new BufferedReader(
					new InputStreamReader(input));

			String rule = "";
			String text = "";
			while ((rule = buf.readLine()) != null) {
				text += rule + "\n";
			}
			;
			buf.close();

			LTLParser parser = new LTLParser(new BufferedReader(
					new StringReader(text)));
			parser.setFilename(null);
			parser.init();
			parser.parse();
			Message.add("Parsing complete.");
			if (UISettings.getInstance().getTest()) {
				Message.add("<parsingLTLfile>", Message.TEST);
				ArrayList names = parser.getVisibleFormulaNames();
				Iterator it = names.iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					Message.add("<formula " + name, Message.TEST);
				}
				Message.add("</parsingLTLfile>", Message.TEST);
			}
			// Put the text into a window:
			return new LTLTemplateFileResult(parser, text);

		} catch (org.processmining.analysis.ltlchecker.parser.ParseException e) {
			// An error by parsing.
			throw new IOException(e.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "<h2>LTL Template File</h2>" + "<p>"
				+ "A LTL template file exists of attribute definitions"
				+ " and formula definitions specifying properties of a"
				+ " process log." + "</p";
	}
}

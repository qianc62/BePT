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

import java.awt.Dimension;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

public class LTLTemplateFileResult implements MiningResult, Provider {

	LTLParser parser;
	InputStream input;

	JScrollPane sp;

	public LTLTemplateFileResult(LTLParser parser, String text) {
		this.parser = parser;
		this.input = input;

		JEditorPane textContent = new JEditorPane();
		textContent.setText(text);
		textContent.setMinimumSize(new Dimension(200, 100));
		textContent.setPreferredSize(new Dimension(200, 100));
		textContent.setEditable(false);
		sp = new JScrollPane(textContent);

	}

	public LogReader getLogReader() {
		return null;
	}

	public JComponent getVisualization() {
		return this.sp;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("LTL Parser",
				new Object[] { this.parser }) };
	}
}

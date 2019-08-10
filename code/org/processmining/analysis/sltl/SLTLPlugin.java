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

package org.processmining.analysis.sltl;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.ltlchecker.DefaultLTLChecker;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ontology.OntologyCollection;

public class SLTLPlugin extends DefaultLTLChecker {

	/**
	 * Gets the name of this plugin. This name will be used in the gui of the
	 * framework to select this plugin.
	 * 
	 * @return The name of this plugin.
	 */
	public String getName() {
		return "Semantic LTL Checker";
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

	protected JComponent createGui(LogReader log, LTLParser parser,
			AnalysisInputItem[] inputs) {
		OntologyCollection semanticLog = log.getLogSummary().getOntologies();

		return new SLTLTemplateGui(semanticLog, log, parser, this, inputs);
	}
}

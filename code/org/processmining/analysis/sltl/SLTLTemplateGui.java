package org.processmining.analysis.sltl;

import java.util.List;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.ltlchecker.LTLChecker;
import org.processmining.analysis.ltlchecker.ParamData;
import org.processmining.analysis.ltlchecker.ParamTable;
import org.processmining.analysis.ltlchecker.TemplateGui;
import org.processmining.analysis.ltlchecker.parser.FormulaParameter;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ontology.OntologyCollection;

public class SLTLTemplateGui extends TemplateGui {

	private static final long serialVersionUID = -507559432832372820L;
	private OntologyCollection semanticLog;

	public SLTLTemplateGui(OntologyCollection semanticLog, LogReader log,
			LTLParser parser, LTLChecker checker, AnalysisInputItem[] inputs) {
		super();
		this.semanticLog = semanticLog;
		init(log, parser, checker, inputs);
	}

	@Override
	protected ParamData createDataModel(List<FormulaParameter> items) {
		return new ParamData(items, semanticLog);
	}

	protected ParamTable createParamTable() {
		return new SemanticParamTable();
	}

	protected int getParamPaneHeight() {
		return 150;
	}
}

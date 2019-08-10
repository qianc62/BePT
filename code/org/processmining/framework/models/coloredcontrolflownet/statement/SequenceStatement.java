package org.processmining.framework.models.coloredcontrolflownet.statement;

import java.util.ArrayList;
import java.util.List;

public class SequenceStatement implements Statement {

	public final List<Statement> statements;

	public SequenceStatement(Statement... statements) {
		this.statements = new ArrayList<Statement>();
		for (Statement statement : statements) {
			this.statements.add(statement);
		}
	}

	public SequenceStatement(List<Statement> statements) {
		this.statements = statements;
	}

}

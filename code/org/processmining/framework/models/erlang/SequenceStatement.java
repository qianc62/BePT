package org.processmining.framework.models.erlang;

import java.util.ArrayList;
import java.util.List;

public class SequenceStatement extends Statement {

	public final List<Statement> statements;

	public SequenceStatement(Statement... statements) {
		super();
		this.statements = new ArrayList<Statement>();
		for (Statement statement : statements) {
			if (statement instanceof SequenceStatement)
				this.statements
						.addAll(((SequenceStatement) statement).statements);
			else
				this.statements.add(statement);
		}
	}

	@Override
	public String toString(String pad) {
		return null;
	}

}

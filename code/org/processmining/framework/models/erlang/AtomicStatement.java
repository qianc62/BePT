package org.processmining.framework.models.erlang;

public class AtomicStatement extends Statement {

	public final String statement;

	public AtomicStatement(String statement) {
		this.statement = statement.substring(0, 1).toLowerCase()
				+ statement.substring(1);
	}

	@Override
	public String toString(String pad) {
		return pad + statement;
	}

}

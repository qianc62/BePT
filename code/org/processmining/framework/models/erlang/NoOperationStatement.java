package org.processmining.framework.models.erlang;

public class NoOperationStatement extends Statement {

	public final String envId;

	public NoOperationStatement(String envId) {
		super();
		this.envId = envId;
	}

	@Override
	public String toString(String pad) {
		return pad + "no_operation";
	}

}

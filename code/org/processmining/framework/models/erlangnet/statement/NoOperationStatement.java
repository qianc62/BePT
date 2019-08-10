package org.processmining.framework.models.erlangnet.statement;

public class NoOperationStatement implements Statement {

	public final String envId;

	public NoOperationStatement(String envId) {
		super();
		this.envId = envId;
	}

	@Override
	public String toString() {
		return envId;
	}

}

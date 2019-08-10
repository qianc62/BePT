package org.processmining.framework.models.erlangnet.statement;

public class ReceiveSenderStatement implements Statement {

	public final String sender;

	public ReceiveSenderStatement(String sender) {
		super();
		this.sender = sender;
	}

	@Override
	public String toString() {
		return "set(\"" + sender + "\",PID sender)";
	}

}

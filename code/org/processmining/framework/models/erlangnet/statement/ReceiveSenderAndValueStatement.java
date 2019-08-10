package org.processmining.framework.models.erlangnet.statement;

public class ReceiveSenderAndValueStatement implements Statement {

	public final String sender;
	public final String variable;

	public ReceiveSenderAndValueStatement(String sender, String variable) {
		this.sender = sender;
		this.variable = variable;
	}

	@Override
	public String toString() {
		return "sm(\"" + sender + "\",sender,\"" + variable + "\",value,env)";
	}
}

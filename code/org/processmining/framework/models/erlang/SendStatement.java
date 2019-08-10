package org.processmining.framework.models.erlang;

public class SendStatement extends Statement {

	private final String pid;
	private final String message;

	public SendStatement(String pid, String message) {
		this.pid = pid;
		this.message = message.replaceAll("\\\\n", "_");
	}

	@Override
	public String toString(String pad) {
		return pad + pid + " ! {" + message + "}";
	}

}

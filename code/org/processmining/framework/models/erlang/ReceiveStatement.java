package org.processmining.framework.models.erlang;

public class ReceiveStatement extends Statement {

	public final String waitForMessage;

	public ReceiveStatement(String waitForMessage) {
		this.waitForMessage = waitForMessage.replaceAll("\\\\n", "_");
	}

	@Override
	public String toString(String pad) {
		return pad + "receive\n" + pad + "\t{" + waitForMessage + "} -> "
				+ waitForMessage + "\n" + pad + "end";
	}

}

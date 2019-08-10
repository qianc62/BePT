package org.processmining.framework.models.erlangnet.inscription;

import org.processmining.framework.models.erlangnet.expression.Expression;

public class SendMessageInscription implements ArcInscription {

	public final String receiver;
	public final Expression expression;

	public SendMessageInscription(String receiver, Expression expression) {
		super();
		this.receiver = receiver;
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "(pid,getPid(\"" + receiver + "\")," + expression + ")";
	}

}

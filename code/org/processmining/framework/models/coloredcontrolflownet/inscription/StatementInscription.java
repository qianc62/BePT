package org.processmining.framework.models.coloredcontrolflownet.inscription;

import org.processmining.framework.models.coloredcontrolflownet.statement.Statement;

public class StatementInscription implements ArcInscription {

	public final Statement statement;

	public StatementInscription(Statement statement) {
		this.statement = statement;
	}

	@Override
	public String toString() {
		return "(pid," + statement + ")";
	}

}

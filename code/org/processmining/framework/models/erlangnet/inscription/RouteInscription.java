package org.processmining.framework.models.erlangnet.inscription;

import org.processmining.framework.models.erlangnet.statement.Statement;

public class RouteInscription implements ArcInscription {

	public final Statement statement;

	public RouteInscription(Statement statement) {
		super();
		this.statement = statement;
	}

	@Override
	public String toString() {
		return "(pid," + statement + ")";
	}

}

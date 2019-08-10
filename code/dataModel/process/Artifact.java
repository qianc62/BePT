package dataModel.process;

import java.util.ArrayList;

public class Artifact extends Element {
	
	protected int type;
	
	public Artifact(int id, String label, Lane lane, Pool pool, int type) {
		super(id, label, lane, pool);
		this.type = type;
	}

	public int getType() {
		return type;
	}
}

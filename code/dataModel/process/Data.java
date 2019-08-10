package dataModel.process;

import java.util.ArrayList;

public class Data extends Element {

	protected int type;
	
	public Data(int id, String label, Lane lane, Pool pool, int type) {
		super(id, label, lane, pool);
		this.type = type;
	}

	public int getType() {
		return type;
	}
}

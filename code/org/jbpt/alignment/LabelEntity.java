package org.jbpt.alignment;

import org.jbpt.hypergraph.abs.IEntity;


public class LabelEntity implements IEntity {

	protected String label;
	
	public LabelEntity(String label) {
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LabelEntity))
			return false;
		return ((LabelEntity)o).getLabel().equals(this.getLabel());
	}
	
	@Override
	public int hashCode() {
		return this.getLabel().hashCode();
	}
	
	@Override
	public String toString() {
		return this.getLabel();
	}
	
	@Override
	public Object clone() {
		return new LabelEntity(this.getLabel());
	}
	
}

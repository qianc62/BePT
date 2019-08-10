package org.processmining.framework.models.ui.treecomponent;

import javax.swing.tree.DefaultMutableTreeNode;

public class SelectableNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -768059251759288625L;

	private boolean selected;

	public SelectableNode(Object o) {
		super(o);
		selected = true;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void toggle() {
		setSelected(!isSelected());
	}
}

package org.processmining.framework.models.ui.treecomponent;

import java.util.List;

public interface InitialSelectionCallback {
	boolean isInitiallySelected(List<String> path);
}

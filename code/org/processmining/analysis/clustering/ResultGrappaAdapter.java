package org.processmining.analysis.clustering;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ResultGrappaAdapter extends GrappaAdapter {

	LogClusteringEngine engine = null;
	LogClusteringResultUI resultUI = null;

	public ResultGrappaAdapter(LogClusteringEngine engine) {
		this.engine = engine;
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {

		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);

		if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
			if ((clickCount == 2)
					&& ((modifiers & InputEvent.CTRL_MASK) != InputEvent.CTRL_MASK)) {
				// looks like Java has a single click occur on the way to a
				// multiple click, so this code always executes (which is
				// not necessarily a bad thing)
				if (subg.getGraph().isSelectable()) {
					if (modifiers == InputEvent.BUTTON1_MASK) {
						// select element
						if (elem != null) {
							if (subg.currentSelection != null) {
								if (subg.currentSelection instanceof Node) {
									String s = ((Node) (subg.currentSelection))
											.getName();
									engine.setObjectSelection(s);
								}
							}
						}
					}
				}
			} else {
				// multiple clicks
				// this code executes for each click beyond the first
				// System.err.println("clickCount="+clickCount);
			}
		}
	}

	public void actionPerformed(ActionEvent aev) {
		System.out.println("GrappaAdapter.actionPerformed(...)");
		super.actionPerformed(aev);
	}
}

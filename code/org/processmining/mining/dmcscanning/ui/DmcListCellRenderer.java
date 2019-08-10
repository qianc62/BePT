/*
 * Created on Jun 2, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.processmining.mining.dmcscanning.Admc;
import org.processmining.mining.dmcscanning.Dmc;

/**
 * Custom component to implement the in-list representation rendering of xDMC
 * objects (i.e., ADMCs or DMCs, etc.).
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcListCellRenderer extends DefaultListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1049600263439148557L;

	public Component getListCellRendererComponent(JList list, Object value, // value
			// to
			// display
			int index, // cell index
			boolean iss, // is the cell selected
			boolean chf) // the list and the cell have the focus
	{
		/*
		 * The DefaultListCellRenderer class will take care of the JLabels text
		 * property, it's foreground and background colors, and so on.
		 */
		super.getListCellRendererComponent(list, value, index, iss, chf);
		if (value instanceof Dmc) {
			Dmc obj = (Dmc) value;
			setText("Cluster " + obj.getIdNumber() + " (" + obj.size() + ")");
		} else if (value instanceof Admc) {
			Admc obj = (Admc) value;
			setText("Aggregated Cluster " + obj.getIdNumber() + " ("
					+ obj.size() + ")");
		} else {
			setText("ERROR!");
		}
		// return oneself (a JLabel instance) to be rendered
		return this;
	}
}

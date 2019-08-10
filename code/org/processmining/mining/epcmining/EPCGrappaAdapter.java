package org.processmining.mining.epcmining;

import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.ui.MainUI;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
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
public class EPCGrappaAdapter extends GrappaAdapter {

	private final EPCResult result;

	private JMenuItem orgMenu = new JCheckBoxMenuItem(
			"Show organizational entities");
	private JMenuItem dataMenu = new JCheckBoxMenuItem("Show data objects");
	private JMenuItem infSysMenu = new JCheckBoxMenuItem(
			"Show information systems");
	private JMenuItem connMenu = new JMenu("Select connector type");
	private JRadioButtonMenuItem andRad = new JRadioButtonMenuItem("AND");
	private JRadioButtonMenuItem orRad = new JRadioButtonMenuItem("OR");
	private JRadioButtonMenuItem xorRad = new JRadioButtonMenuItem("XOR");
	private EPCConnector conn;

	public EPCGrappaAdapter(EPCResult res) {
		this.result = res;
		orgMenu.setSelected(true);
		dataMenu.setSelected(false);
		infSysMenu.setSelected(false);
		orgMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				result.showEPC(result.getEPC());
			}
		});
		dataMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				result.showEPC(result.getEPC());
			}
		});
		infSysMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				result.showEPC(result.getEPC());
			}
		});
		ButtonGroup group = new ButtonGroup();

		group.add(andRad);
		andRad.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (andRad.isSelected() && (conn.getType() != EPCConnector.AND)) {
					conn.setType(EPCConnector.AND);
					result.repaint();
				}
			}
		});
		group.add(orRad);
		orRad.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (orRad.isSelected() && (conn.getType() != EPCConnector.OR)) {
					conn.setType(EPCConnector.OR);
					result.repaint();
				}
			}
		});
		group.add(xorRad);
		xorRad.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (xorRad.isSelected() && (conn.getType() != EPCConnector.XOR)) {
					conn.setType(EPCConnector.XOR);
					result.repaint();
				}
			}
		});

		connMenu.add(andRad);
		connMenu.add(orRad);
		connMenu.add(xorRad);
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		int i = InputEvent.BUTTON1_MASK;
		int j = InputEvent.SHIFT_MASK;
		if ((modifiers & i) == i && (modifiers & j) == j && clickCount == 1
				&& elem != null && elem.object != null
				&& elem.object instanceof EPCSubstFunction) {
			EPCSubstFunction t = (EPCSubstFunction) elem.object;
			if (t.getSubstitutedEPC() != null) {
				ConfigurableEPC epc = t.getSubstitutedEPC();
				epc.setShowObjects(showOrg(), showData(), showInfSys());
				result.selectEPC(epc);
			} else {
				JOptionPane
						.showMessageDialog(
								MainUI.getInstance(),
								"Substitution function does not point to underlying EPC",
								"No EPC found", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * The method is called when a mouse press occurs on a displayed subgraph.
	 * The returned menu is added to the end of the default right-click menu
	 * 
	 * @param subg
	 *            displayed subgraph where action occurred
	 * @param elem
	 *            subgraph element in which action occurred
	 * @param pt
	 *            the point where the action occurred (graph coordinates)
	 * @param modifiers
	 *            mouse modifiers in effect
	 * @param panel
	 *            specific panel where the action occurred
	 */
	protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
			GrappaPoint pt, int modifiers, GrappaPanel panel) {
		JMenuItem menu = new JMenu("Settings");
		menu.add(orgMenu);
		menu.add(dataMenu);
		menu.add(infSysMenu);
		if (elem != null && elem.object != null
				&& elem.object instanceof EPCConnector) {
			conn = (EPCConnector) elem.object;

			andRad.setSelected(conn.getType() == EPCConnector.AND);

			orRad.setSelected(conn.getType() == EPCConnector.OR);

			xorRad.setSelected(conn.getType() == EPCConnector.XOR);

			menu.add(connMenu);
		}

		return menu;
	}

	public boolean showOrg() {
		return orgMenu.isSelected();
	}

	public boolean showData() {
		return dataMenu.isSelected();
	}

	public boolean showInfSys() {
		return infSysMenu.isSelected();
	}

}

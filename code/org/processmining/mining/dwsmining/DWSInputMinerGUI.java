package org.processmining.mining.dwsmining;

import javax.swing.JPanel;
import java.awt.CardLayout;
import javax.swing.JSplitPane;

/**
 * Provides the graphical interface for running DWS mining plugin.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class DWSInputMinerGUI extends JPanel {

	private JSplitPane jSplitPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;

	/**
	 * This is the default constructor
	 */
	public DWSInputMinerGUI(JPanel panelHeuristic, JPanel panelDWS) {
		super();
		this.jPanel = panelHeuristic;
		this.jPanel1 = panelDWS;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setSize(300, 200);
		this.add(getJSplitPane(), getJSplitPane().getName());
	}

	/**
	 * This method initializes jSplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setName("jSplitPane");
			jSplitPane.setTopComponent(getJPanel());
			jSplitPane.setBottomComponent(getJPanel1());
			jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
		}
		return jPanel1;
	}

}

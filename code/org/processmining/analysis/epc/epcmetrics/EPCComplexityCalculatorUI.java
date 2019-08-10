package org.processmining.analysis.epc.epcmetrics;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.plugin.Provider;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Daniel Teixeira and Jo�o Sobrinho
 * @version 1.0
 */

public class EPCComplexityCalculatorUI extends JPanel implements Provider {

	private final static int MAINDIVIDERLOCATION = 320;
	private final static int LEFTDIVIDERLOCATION = 300;
	private ConfigurableEPC epc;
	private ConfigurableEPC epc2;

	private ModelGraphPanel epcVis = null;
	private JTextArea ErrorTextArea;
	private JTable tMetrics;

	private JSplitPane splitpaneMain;
	private JSplitPane splitpaneLeft;

	private JScrollPane spTable;
	private JScrollPane spErrorTextArea;

	public EPCComplexityCalculatorUI(ConfigurableEPC orgEPC) {
		this.epc = orgEPC;
		this.epc2 = orgEPC;
		this.epc.setShowObjects(false, false, false);
		this.epcVis = epc.getGrappaVisualization();

		Message.add("<EPCComplexityCalculator numoffunctions=\""
				+ epc.getFunctions().size() + "\" numofevents=\""
				+ epc.getEvents().size() + "\" numofedges=\""
				+ epc.getEdges().size() + "\">", Message.TEST);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Message.add("</EPCComplexityCalculator>", Message.TEST);

	}

	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		this.splitpaneMain = new JSplitPane();

		this.splitpaneLeft = new JSplitPane();
		this.splitpaneLeft
				.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		this.splitpaneLeft.setDividerLocation(LEFTDIVIDERLOCATION);
		this.splitpaneLeft.setEnabled(false);

		this.splitpaneMain.add(new JScrollPane(epcVis), JSplitPane.RIGHT);

		this.ErrorTextArea = new JTextArea();
		this.ErrorTextArea.setOpaque(false);
		this.ErrorTextArea.setEditable(false);
		this.ErrorTextArea.setText("Error Logging:\n");
		this.spErrorTextArea = new JScrollPane(ErrorTextArea);
		this.spErrorTextArea.setSize(300, 200);

		String[] columnNames = { "Name", "Type", "Value" };
		Object[][] data = this.getValues();

		this.tMetrics = new JTable(data, columnNames);
		this.tMetrics.setEnabled(false);
		this.spTable = new JScrollPane(tMetrics);
		this.spTable
				.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
					public void mouseMoved(java.awt.event.MouseEvent evt) {
						spTableMouseMoved(evt);
					}
				});

		this.splitpaneLeft.add(spTable, JSplitPane.LEFT);
		this.splitpaneLeft.add(spErrorTextArea, JSplitPane.RIGHT);

		this.splitpaneMain.add(splitpaneLeft, JSplitPane.LEFT);
		this.add(splitpaneMain, BorderLayout.CENTER);
		this.splitpaneMain.setDividerLocation(MAINDIVIDERLOCATION);
	}

	private String[][] getValues() {
		ICalculator calc = new ControlFlow(this.epc);
		String[] out1 = new String[3];
		out1[0] = calc.getName();
		out1[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Control Flow]"
					+ calc.VerifyBasicRequirements());
		}
		out1[2] = calc.Calculate();

		calc = new Density(this.epc);
		String[] out2 = new String[3];
		out2[0] = calc.getName();
		out2[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Density]"
					+ calc.VerifyBasicRequirements());
		}
		out2[2] = calc.Calculate();

		calc = new NumberOfFunctions(this.epc);
		String[] out4 = new String[3];
		out4[0] = calc.getName();
		out4[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Functions]"
					+ calc.VerifyBasicRequirements());
		}
		out4[2] = calc.Calculate();

		calc = new NumberOfEvents(this.epc);
		String[] out5 = new String[3];
		out5[0] = calc.getName();
		out5[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Events]"
					+ calc.VerifyBasicRequirements());
		}
		out5[2] = calc.Calculate();

		calc = new NumberOfORs(this.epc);
		String[] out6 = new String[3];
		out6[0] = calc.getName();
		out6[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[ORs]"
					+ calc.VerifyBasicRequirements());
		}
		out6[2] = calc.Calculate();

		calc = new NumberOfXORs(this.epc);
		String[] out8 = new String[3];
		out8[0] = calc.getName();
		out8[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[XORs]"
					+ calc.VerifyBasicRequirements());
		}
		out8[2] = calc.Calculate();

		calc = new NumberOfANDs(this.epc);
		String[] out9 = new String[3];
		out9[0] = calc.getName();
		out9[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[ANDs]"
					+ calc.VerifyBasicRequirements());
		}
		out9[2] = calc.Calculate();

		calc = new Coupling(this.epc);
		String[] out7 = new String[3];
		out7[0] = calc.getName();
		out7[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Coupling]"
					+ calc.VerifyBasicRequirements());
		}
		out7[2] = calc.Calculate();

		calc = new CrossConnectivity(this.epc);
		String[] out10 = new String[3];
		out7[0] = calc.getName();
		out7[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) { // returns the
			// error message
			// in case the
			// EPC has
			// something
			// wrong
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Cross-Connectivity]"
					+ calc.VerifyBasicRequirements());
		}
		out7[2] = calc.Calculate();

		String[][] final_output = { out1, out2, out7, out4, out5, out6, out8,
				out9 };
		return final_output;
	}

	private void spTableMouseMoved(java.awt.event.MouseEvent evt) {
		if (splitpaneMain.getDividerLocation() <= MAINDIVIDERLOCATION) {
			splitpaneMain.setDividerLocation(MAINDIVIDERLOCATION);
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("EPC",
				new Object[] { epc }), };
	}

}

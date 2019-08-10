package org.processmining.analysis.petrinet.petrinetmetrics;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.plugin.ProvidedObject;

public class PNComplexityCalculatorUI extends JPanel implements Provider {
	private final static int MAINDIVIDERLOCATION = 320;
	private final static int LEFTDIVIDERLOCATION = 300;
	private PetriNet net;

	private JTable tMetrics;
	private JScrollPane spTable;
	private JScrollPane spErrorTextArea;
	private ModelGraphPanel gp;
	private JTextArea ErrorTextArea;
	private JSplitPane splitpaneMain;
	private JSplitPane splitpaneLeft;

	public PNComplexityCalculatorUI(PetriNet net) {
		this.net = net;
		this.gp = net.getGrappaVisualization();

		Message.add("<PetriNetComplexityCalculator " + "NumOfArcs=\""
				+ net.getEdges().size() + "\" " + "NumOfPlaces=\""
				+ net.getPlaces().size() + "\" " + "NumOfTransitions=\""
				+ net.getTransitions().size() + "\" >", Message.TEST);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Message.add("</PetriNetComplexityCalculator>", Message.TEST);
	}

	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		this.splitpaneMain = new JSplitPane();
		this.splitpaneLeft = new JSplitPane();
		this.splitpaneLeft
				.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		this.splitpaneLeft.setDividerLocation(LEFTDIVIDERLOCATION);
		this.splitpaneLeft.setEnabled(false);
		this.splitpaneMain.add(new JScrollPane(gp), JSplitPane.RIGHT);
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
		ICalculator calc = new ControlFlow(net);
		String[] out1 = new String[3];
		out1[0] = calc.getName();
		out1[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Control Flow]"
					+ calc.VerifyBasicRequirements());
		}
		out1[2] = calc.Calculate();

		calc = new Density(net);
		String[] out3 = new String[3];
		out3[0] = calc.getName();
		out3[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Density]"
					+ calc.VerifyBasicRequirements());
		}
		out3[2] = calc.Calculate();

		calc = new NumberOfANDJoin(net);
		String[] out5 = new String[3];
		out5[0] = calc.getName();
		out5[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out5[2] = calc.Calculate();

		calc = new NumberOfANDSplit(net);
		String[] out6 = new String[3];
		out6[0] = calc.getName();
		out6[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out6[2] = calc.Calculate();

		calc = new NumberOfXORJoin(net);
		String[] out7 = new String[3];
		out7[0] = calc.getName();
		out7[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out7[2] = calc.Calculate();

		calc = new NumberOfXORSplit(net);
		String[] out8 = new String[3];
		out8[0] = calc.getName();
		out8[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out8[2] = calc.Calculate();

		calc = new NumberOfArcs(net);
		String[] out9 = new String[3];
		out9[0] = calc.getName();
		out9[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out9[2] = calc.Calculate();

		calc = new NumberOfPlaces(net);
		String[] out10 = new String[3];
		out10[0] = calc.getName();
		out10[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out10[2] = calc.Calculate();

		calc = new NumberOfTransitions(net);
		String[] out11 = new String[3];
		out11[0] = calc.getName();
		out11[1] = calc.getType();
		if (!calc.VerifyBasicRequirements().startsWith(".")) {
			String text = this.ErrorTextArea.getText();
			this.ErrorTextArea.setText(text + "\n[Size]"
					+ calc.VerifyBasicRequirements());
		}
		out11[2] = calc.Calculate();

		String[][] final_output = { out1, out3, out5, out6, out7, out8, out9,
				out10, out11 };
		return final_output;
	}

	private void spTableMouseMoved(java.awt.event.MouseEvent evt) {
		if (splitpaneMain.getDividerLocation() <= MAINDIVIDERLOCATION) {
			splitpaneMain.setDividerLocation(MAINDIVIDERLOCATION);
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("Petri net",
				new Object[] { net }), };
	}
}

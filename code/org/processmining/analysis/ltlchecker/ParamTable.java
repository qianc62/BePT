package org.processmining.analysis.ltlchecker;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;

import org.processmining.analysis.ltlchecker.parser.LTLParser;

public class ParamTable extends JPanel {

	private static final long serialVersionUID = 1702135392588465251L;

	private JTable table = null;
	protected ParamData data;

	public ParamTable() {
		buildGui();
	}

	public void setModel(ParamData data) {
		this.data = data;
		updateGui();
	}

	public Substitutes getSubstitutes(LTLParser parser) {
		return data.getSubstitutes(parser);
	}

	protected void buildGui() {
		table = new JTable();
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		this.setLayout(new BorderLayout());
		this.add(table, BorderLayout.NORTH);
	}

	protected void updateGui() {
		table.setModel(data);
	}
}

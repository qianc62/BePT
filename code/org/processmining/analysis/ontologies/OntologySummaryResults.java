package org.processmining.analysis.ontologies;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Progress;

/**
 * @author Ana Karla A. de Medeiros
 * @author Peter van den Brand
 */
public class OntologySummaryResults extends JPanel implements Provider {

	private static final long serialVersionUID = -7164422254795413578L;

	private JTable table;
	private OntologyTableModel tableModel;
	private JPanel graphPanel;
	private JCheckBox checkShowInstances;
	private List<OntologyModel> ontologies;
	private LogReader log;

	public OntologySummaryResults(OntologyCollection ontologies, LogReader log) {
		this.ontologies = ontologies.getOntologies();
		this.log = log;
		showOntology();
	}

	private void showOntology() {
		JSplitPane splitPane;
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
		JButton updateGraphButton = new JButton("Update graph");

		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setBackground(Color.WHITE);

		tableModel = new OntologyTableModel(ontologies);
		table = new DoubleClickTable(tableModel, updateGraphButton);
		updateGraphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraph();
			}
		});

		checkShowInstances = new JCheckBox("Display instances");
		checkShowInstances.setSelected(false);
		checkShowInstances.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateGraph();
			}
		});

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(table), graphPanel);
		splitPane.setDividerLocation(250);
		splitPane.setOneTouchExpandable(true);

		buttonsPanel.add(updateGraphButton, BorderLayout.WEST);
		bottomPanel.add(buttonsPanel, BorderLayout.WEST);
		bottomPanel.add(checkShowInstances, BorderLayout.EAST);

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (!ontologies.isEmpty()) {
			table.setRowSelectionInterval(0, 0);
		}
		updateGraph();

	}

	private void updateGraph() {
		graphPanel.removeAll();

		if (table.getSelectedRowCount() == 0) {
			JLabel msg = new JLabel(
					ontologies.isEmpty() ? "Log does not contain any ontologies."
							: "Please select an ontology first.");
			msg.setHorizontalAlignment(SwingConstants.CENTER);
			graphPanel.add(msg, BorderLayout.CENTER);
		} else {
			OntologyModel ontology = tableModel.getOntology(table
					.getSelectedRow());

			ModelGraphPanel gp = ontology.toModelGraph(
					checkShowInstances.isSelected()).getGrappaVisualization();
			JScrollPane scrollPane = new JScrollPane(gp);
			gp.setOriginalObject(ontology);
			graphPanel.add(scrollPane, BorderLayout.CENTER);
		}
		graphPanel.revalidate();
		graphPanel.repaint();
	}

	public ProvidedObject[] getProvidedObjects() {
		return log == null ? new ProvidedObject[0]
				: new ProvidedObject[] { new ProvidedObject("Annotated log",
						log) };
	}

}

class OntologyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -4813015041634774668L;
	private OntologyModel[] data;

	public OntologyTableModel(List<OntologyModel> data) {
		this.data = data.toArray(new OntologyModel[0]);
		Arrays.sort(this.data, new Comparator<OntologyModel>() {
			public int compare(OntologyModel a, OntologyModel b) {
				return a.getName().compareTo(b.getName());
			}
		});
	}

	public String getColumnName(int col) {
		return "Ontology";
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return 1;
	}

	public OntologyModel getOntology(int row) {
		return data[row];
	}

	public Object getValueAt(int row, int column) {
		return OntologyModel.getConceptPart(data[row].getName());
	}
}

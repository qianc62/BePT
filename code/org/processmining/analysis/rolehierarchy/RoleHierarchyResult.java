package org.processmining.analysis.rolehierarchy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.GUIPropertyDouble;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

public class RoleHierarchyResult extends JPanel implements Provider,
		GuiNotificationTarget {

	private static final int ORIGINATOR_COLUMN_WITH = 150;

	private static final long serialVersionUID = 4446982148172131106L;

	private RoleHierarchy hierarchy;
	private JPanel graphPanel;
	private JCheckBox showTasks;
	private JTable otMatrix;
	private ColoredCellRenderer otCellRenderer;
	private GUIPropertyInteger absoluteThreshold;
	private GUIPropertyDouble relativeThreshold;
	private LogReader log;
	private int maxAbsoluteFrequency;
	private OntologyModel ontology;

	public RoleHierarchyResult(LogReader log, RoleHierarchy hierarchy) {
		this.log = log;
		this.hierarchy = hierarchy;
		this.maxAbsoluteFrequency = hierarchy
				.getMaximumFrequencyForAnyOriginator();
		initGUI();
	}

	private void initGUI() {
		graphPanel = new JPanel(new BorderLayout());

		HeaderBar header = new HeaderBar("Role Hierarchy");
		header.setHeight(40);

		showTasks = new JCheckBox("Show tasks");
		showTasks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraph();
			}
		});

		absoluteThreshold = new GUIPropertyInteger("", 0, 0,
				maxAbsoluteFrequency, this);
		relativeThreshold = new GUIPropertyDouble("", 0, 0, 100, 1, this);

		JPanel thresholdsPanel = new JPanel(new GridBagLayout());
		thresholdsPanel.add(new JLabel("Absolute (0-" + maxAbsoluteFrequency
				+ ")"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		thresholdsPanel.add(absoluteThreshold.getPropertyPanel(),
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		thresholdsPanel.add(new JLabel("Relative (0-100%)"),
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		thresholdsPanel.add(relativeThreshold.getPropertyPanel(),
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		thresholdsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Thresholds"));

		JPanel settings = new JPanel(new GridBagLayout());
		settings.add(showTasks, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		settings.add(thresholdsPanel, new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 0, 0, 0), 0, 0));

		otMatrix = new JTable();
		otCellRenderer = new ColoredCellRenderer(0);
		otCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		updateOTMatrix(null);

		JPanel settingsPanel = new JPanel(new BorderLayout());
		settingsPanel.add(settings, BorderLayout.NORTH);
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel settingsAndGraphPanel = new JPanel(new BorderLayout());
		settingsAndGraphPanel.add(settingsPanel, BorderLayout.WEST);
		settingsAndGraphPanel.add(graphPanel, BorderLayout.CENTER);

		showTasks.setSelected(true);
		updateGraph();

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(new JScrollPane(otMatrix), BorderLayout.CENTER);

		JSplitPane contentPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				settingsAndGraphPanel, tablePanel);
		contentPanel.setDividerLocation(350);
		contentPanel.setResizeWeight(0.5);
		contentPanel.setOneTouchExpandable(true);

		this.setLayout(new BorderLayout());
		this.add(header, BorderLayout.NORTH);
		this.add(contentPanel, BorderLayout.CENTER);
	}

	protected void updateThresholds() {
		int absolute = absoluteThreshold.getValue();
		double relative = relativeThreshold.getValue();

		if (0 <= absolute && 0.0 <= relative && relative <= 100.0) {
			try {
				hierarchy = MineRoleHierarchy.mineRoleHierarchy(log, absolute,
						relative);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane
						.showMessageDialog(MainUI.getInstance(),
								"Error while mining role hierarchy:\n"
										+ e.getMessage());
			}
			updateGraph();
			updateOTMatrix(null);
		} else {
			JOptionPane
					.showMessageDialog(
							MainUI.getInstance(),
							"Absolute threshold should be a number greater than or equal to 0,\n"
									+ "relative threshold should be a number between 0 and 100 (inclusive).",
							"Invalid threshold value",
							JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void updateGraph() {
		boolean annotate = showTasks.isSelected();

		ModelGraphPanel graph = hierarchy.toModelGraph(annotate)
				.getGrappaVisualization();

		graph.addGrappaListener(new RoleHierarchyGrappaAdapter(this));

		graphPanel.removeAll();
		graphPanel.add(graph, BorderLayout.CENTER);

		ontology = hierarchy.toOntology();

		revalidate();
		repaint();
	}

	void updateOTMatrix(Set<String> taskSet) {
		if (taskSet == null) {
			otMatrix.setModel(hierarchy.getFullOTMatrix());
		} else {
			otMatrix.setModel(hierarchy.getLocalOTMatrix(taskSet));
		}

		if (otMatrix.getColumnCount() > 0) {
			TableColumn col = otMatrix.getColumnModel().getColumn(0);
			col.setPreferredWidth(ORIGINATOR_COLUMN_WITH);

			otCellRenderer.setModel(otMatrix.getModel());

			for (int i = 1; i < otMatrix.getColumnCount(); i++) {
				otMatrix.getColumnModel().getColumn(i).setCellRenderer(
						otCellRenderer);
			}
		}

		revalidate();
		repaint();
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] {
				new ProvidedObject("Log file", log),
				new ProvidedObject("Role hierarchy", hierarchy),
				new ProvidedObject("Role hierarchy Graph", hierarchy
						.toModelGraph(showTasks.isSelected())),
				new ProvidedObject("Originator by task matrix", otMatrix
						.getModel()),
				new ProvidedObject("Role hierarchy ontology", ontology) };
	}

	public void updateGUI() {
		updateThresholds();
	}
}

class RoleHierarchyGrappaAdapter extends GrappaAdapter {
	private RoleHierarchyResult result;

	public RoleHierarchyGrappaAdapter(RoleHierarchyResult result) {
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		if ((modifiers & InputEvent.BUTTON1_MASK) > 0 && clickCount == 1) {
			if (elem != null && elem.object != null
					&& elem.object instanceof ModelGraphVertex
					&& ((ModelGraphVertex) elem.object).object2 != null
					&& ((ModelGraphVertex) elem.object).object2 instanceof Set) {
				result
						.updateOTMatrix((Set<String>) ((ModelGraphVertex) elem.object).object2);
			} else {
				result.updateOTMatrix(null);
			}
		}
	}
}

class ColoredCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8540474103226881230L;
	private int maxValue;

	public ColoredCellRenderer(int maxValue) {
		this.maxValue = maxValue;
	}

	public void setModel(TableModel model) {
		this.maxValue = 0;
		for (int row = 0; row < model.getRowCount(); row++) {
			for (int col = 1; col < model.getColumnCount(); col++) {
				maxValue = Math.max(maxValue, (Integer) model.getValueAt(row,
						col));
			}
		}
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		int freq = (Integer) value;
		int color = maxValue <= 0 ? 0 : freq * 0xFF / maxValue;

		if (freq > 0) {
			setText("" + freq);
		} else {
			setText("");
		}
		setForeground(table.getForeground());
		setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		return this;
	}
}

package org.processmining.analysis.sltl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.processmining.analysis.ltlchecker.ParamData;
import org.processmining.analysis.ltlchecker.ParamTable;
import org.processmining.analysis.ltlchecker.Substitutes;
import org.processmining.analysis.ltlchecker.formulatree.SetValueNode;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;

import org.processmining.framework.ui.MainUI;

public class SemanticParamTable extends ParamTable {

	private static final long serialVersionUID = 587250179344861146L;
	private Vector<Setting> settings;

	protected void buildGui() {
		this.setLayout(new GridBagLayout());
		settings = new Vector<Setting>();
	}

	protected void updateGui() {
		this.removeAll();
		settings.clear();
		for (int i = 0; i < data.getRowCount(); i++) {
			addPanel(this, i);
		}
		this.validate();
		this.repaint();
	}

	public Substitutes getSubstitutes(LTLParser parser) {
		for (int i = 0; i < settings.size(); i++) {
			if (settings.get(i).isASet()) {
				SetValueNode value = settings.get(i).getValue(data);

				if (value == null) {
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Please enter a value for parameter "
									+ settings.get(i).getParameterName() + ".");
					return null;
				}
				data.setValueAt(value, i, 2);
			} else {
				data.setValueAt(settings.get(i).getValueAsString(data), i, 2);
			}
		}
		return super.getSubstitutes(parser);
	}

	private void addPanel(JPanel panel, int index) {
		String name = (String) data.getValueAt(index, 0);
		String type = (String) data.getValueAt(index, 1);
		String value = (String) data.getValueAt(index, 2);
		final int NUMROWS = 4;
		final int OFFSET = index * NUMROWS;
		boolean valueIsConcept = false;
		String initialOntology = null;
		String initialConcept = null;
		boolean initialIncludeSuper = false;
		boolean initialIncludeSub = false;

		boolean isSet = type.toLowerCase().trim().equals("set");

		if (isSet) {
			String[] modelRefs = value.trim().split(" ");
			boolean foundFirstModelRef = false;
			boolean foundSecondModelRef = false;

			if (modelRefs.length >= 2 && "[".equals(modelRefs[0])
					&& "]".equals(modelRefs[modelRefs.length - 1])) {
				valueIsConcept = true;
				for (int i = 1; valueIsConcept && i < modelRefs.length - 1; i++) {
					String ref = modelRefs[i];

					if (ref.length() > 0) {
						if (ref.startsWith("@")) {
							if (!foundFirstModelRef) {
								initialOntology = OntologyModel
										.getOntologyPart(ref.substring(1));
								initialConcept = OntologyModel
										.getConceptPart(ref.substring(1));
								value = "";
								foundFirstModelRef = true;
							} else if (!foundSecondModelRef
									&& "@include-sub-concepts".equals(ref)) {
								initialIncludeSub = true;
							} else if (!foundSecondModelRef
									&& "@include-super-concepts".equals(ref)) {
								initialIncludeSuper = true;
							} else {
								foundSecondModelRef = true;
							}
						} else {
							valueIsConcept = false;
						}
					}
				}
			}
		}
		panel.add(new JLabel(name), defaultConstraints(0, OFFSET + 0));
		panel.add(new JLabel(type), defaultConstraints(1, OFFSET + 0));

		final JTextField instanceText = new JTextField(value, 30);
		instanceText.setEnabled(!valueIsConcept);
		panel.add(instanceText, defaultConstraints(3, OFFSET + 0, 3, 1));

		ButtonGroup group = new ButtonGroup();
		JRadioButton instanceButton = new JRadioButton("Instance:",
				!valueIsConcept);
		instanceButton.setMnemonic(KeyEvent.VK_I);
		instanceButton.setActionCommand("Instance:");
		group.add(instanceButton);
		panel.add(instanceButton, defaultConstraints(2, OFFSET + 0));

		if (isSet) {
			OntologyCollection log = data.getSemanticLogReader();
			Vector<OntologyInCombo> items = new Vector<OntologyInCombo>();
			int ontologyIndex = 0, i = 0;
			for (OntologyModel ontology : log.getOntologies()) {
				items.add(new OntologyInCombo(ontology));
				if (OntologyModel.getOntologyPart(ontology.getName()).equals(
						initialOntology)) {
					ontologyIndex = i;
				}
				i++;
			}

			final JComboBox concepts = new JComboBox();
			concepts.setEnabled(valueIsConcept);
			if (concepts.getItemCount() > 0) {
				concepts.setSelectedIndex(0);
			}
			concepts.setMinimumSize(new Dimension(200, 20));
			concepts.setPreferredSize(new Dimension(200, 20));
			panel
					.add(new JLabel("Concept:"), defaultConstraints(4,
							OFFSET + 1));
			panel.add(concepts, defaultConstraints(5, OFFSET + 1));

			final JComboBox ontologies = new JComboBox(items);
			ontologies.setEnabled(valueIsConcept);
			ontologies.setMinimumSize(new Dimension(200, 20));
			ontologies.setPreferredSize(new Dimension(200, 20));
			if (ontologyIndex < ontologies.getModel().getSize()) {
				ontologies.setSelectedIndex(ontologyIndex);
			}
			panel.add(ontologies, defaultConstraints(3, OFFSET + 1));

			ontologies.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateConcepts(ontologies, concepts, null);
				}
			});
			updateConcepts(ontologies, concepts, initialConcept);

			final JCheckBox superConcepts = new JCheckBox(
					"Include super concepts", initialIncludeSuper);
			superConcepts.setEnabled(valueIsConcept);
			panel.add(superConcepts, defaultConstraints(6, OFFSET + 1));

			final JCheckBox subConcepts = new JCheckBox("Include sub concepts",
					initialIncludeSub);
			subConcepts.setEnabled(valueIsConcept);
			panel.add(subConcepts, defaultConstraints(6, OFFSET + 2));

			JRadioButton ontologyButton = new JRadioButton("Ontology:",
					valueIsConcept);
			ontologyButton.setMnemonic(KeyEvent.VK_O);
			ontologyButton.setActionCommand("Ontology:");
			group.add(ontologyButton);
			panel.add(ontologyButton, defaultConstraints(2, OFFSET + 1));

			instanceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					instanceText.setEnabled(true);
					ontologies.setEnabled(false);
					concepts.setEnabled(false);
					superConcepts.setEnabled(false);
					subConcepts.setEnabled(false);
				}
			});
			ontologyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					instanceText.setEnabled(false);
					ontologies.setEnabled(true);
					concepts.setEnabled(true);
					superConcepts.setEnabled(true);
					subConcepts.setEnabled(true);
				}
			});

			settings.add(new Setting(index, name, instanceText, ontologies,
					concepts, instanceButton, superConcepts, subConcepts, log));
		} else {
			settings.add(new Setting(index, name, instanceText));
		}

		JPanel line = new JPanel();
		line.setMaximumSize(new Dimension(100, 1));
		line.setMinimumSize(new Dimension(100, 1));
		line.setPreferredSize(new Dimension(100, 1));
		line.setBackground(Color.GRAY);
		panel.add(line, new GridBagConstraints(0, OFFSET + NUMROWS - 1, 7, 1,
				1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

	}

	private void updateConcepts(JComboBox ontologies, JComboBox concepts,
			String selectedConceptName) {
		int index = ontologies.getSelectedIndex();

		concepts.removeAllItems();

		if (index >= 0) {
			OntologyCollection log = data.getSemanticLogReader();
			OntologyModel ontology = log.getOntologies().get(index);
			int selectedIndex = 0, i = 0;

			for (ConceptModel concept : ontology.getAllConcepts()) {
				concepts.addItem(new ConceptInCombo(concept));
				if (concept.getShortName().equals(selectedConceptName)) {
					selectedIndex = i;
				}
				i++;
			}
			if (selectedIndex < concepts.getModel().getSize()) {
				concepts.setSelectedIndex(selectedIndex);
			}
		}
	}

	private GridBagConstraints defaultConstraints(int x, int y) {
		return defaultConstraints(x, y, 1, 1);
	}

	private GridBagConstraints defaultConstraints(int x, int y, int spanX,
			int spanY) {
		return new GridBagConstraints(x, y, spanX, spanY, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0);
	}
}

class OntologyInCombo {
	private OntologyModel ontology;

	public OntologyInCombo(OntologyModel ontology) {
		this.ontology = ontology;
	}

	public OntologyModel get() {
		return ontology;
	}

	public String toString() {
		return ontology.getShortName();
	}
}

class ConceptInCombo {
	private ConceptModel concept;

	public ConceptInCombo(ConceptModel concept) {
		this.concept = concept;
	}

	public ConceptModel get() {
		return concept;
	}

	public String toString() {
		return concept.getShortName();
	}
}

class Setting {
	private int index;
	private String name;
	private JRadioButton instanceButton;
	private JComboBox concepts;
	private JComboBox ontologies;
	private JTextField instanceText;
	private JCheckBox subConcepts;
	private JCheckBox superConcepts;
	private OntologyCollection ontologyCollection;

	public Setting(int index, String name, JTextField instanceText) {
		this(index, name, instanceText, null, null, null, null, null, null);
	}

	public Setting(int index, String name, JTextField instanceText,
			JComboBox ontologies, JComboBox concepts,
			JRadioButton instanceButton, JCheckBox superConcepts,
			JCheckBox subConcepts, OntologyCollection ontologyCollection) {
		this.index = index;
		this.name = name;
		this.instanceText = instanceText;
		this.ontologies = ontologies;
		this.concepts = concepts;
		this.instanceButton = instanceButton;
		this.superConcepts = superConcepts;
		this.subConcepts = subConcepts;
		this.ontologyCollection = ontologyCollection;
	}

	public boolean isASet() {
		return ontologies != null;
	}

	public String getValueAsString(ParamData data) {
		assert (!isASet());
		return instanceText.getText();
	}

	public SetValueNode getValue(ParamData data) {
		if (instanceButton == null || instanceButton.isSelected()) {
			return data.createSetValueNode(instanceText.getText(), index);
		} else if (ontologies.getSelectedIndex() >= 0
				&& concepts.getSelectedIndex() >= 0) {
			ConceptModel selectedConcept = ((ConceptInCombo) concepts
					.getSelectedItem()).get();
			List<String> concepts = new ArrayList<String>();
			SetValueNode setval = new SetValueNode(
					SetValueNode.MODEL_REFERENCE_SET, ontologyCollection);

			concepts.add(selectedConcept.getName());
			if (superConcepts.isSelected()) {
				concepts.add("include-super-concepts");
			}
			if (subConcepts.isSelected()) {
				concepts.add("include-sub-concepts");
			}

			setval.setModelReferenceSet(concepts);
			return setval;
		} else {
			return null;
		}
	}

	public String getParameterName() {
		return name;
	}

	// private List<ConceptModel> getConcepts(OntologyModel ontology,
	// ConceptModel concept, boolean includeSuper, boolean includeSub) {
	// List<ConceptModel> concepts = new ArrayList<ConceptModel>();
	//
	// if (includeSuper) {
	// concepts.addAll(ontology.getSuperConcepts(concept));
	// }
	// if (includeSub) {
	// concepts.addAll(ontology.getSubConcepts(concept));
	// }
	// concepts.add(concept);
	//		
	// return concepts;
	// }
	//	
	// private List<String> translateConceptsToURIsInLog(OntologyModel ontology,
	// List<ConceptModel> concepts) {
	// List<String> result = new ArrayList<String>();
	//
	// for (ConceptModel c : concepts) {
	// result.add(ontology.getConceptURIInLog(c));
	// }
	// return result;
	// }
	//	
	// private List<String> translateConceptsToURIsInOntology(OntologyModel
	// ontology, List<ConceptModel> concepts) {
	// List<String> result = new ArrayList<String>();
	//
	// for (ConceptModel c : concepts) {
	// result.add(c.getName());
	// }
	// return result;
	// }
}

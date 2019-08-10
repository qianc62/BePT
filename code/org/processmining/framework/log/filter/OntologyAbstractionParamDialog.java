package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.models.ui.treecomponent.InitialSelectionCallback;
import org.processmining.framework.models.ui.treecomponent.ModelGraphTreeComponent;
import org.processmining.framework.models.ui.treecomponent.NameProvider;
import org.processmining.framework.models.ui.treecomponent.SelectableNode;
import org.processmining.framework.models.ui.treecomponent.SelectionChangeListener;
import org.processmining.framework.models.ui.treecomponent.VertexNode;

public class OntologyAbstractionParamDialog extends LogFilterParameterDialog {

	private static final long serialVersionUID = -5950552006241649411L;

	private JList conceptList;
	private ModelGraphTreeComponent tree;
	private JRadioButton usePriorities;
	private JRadioButton dontUsePriorities;
	private JCheckBox replaceInstancesElement;
	private JCheckBox replaceInstancesType;
	private JCheckBox replaceInstancesOriginator;
	private JCheckBox removePI;
	private JCheckBox removeWfme;
	private JCheckBox removeType;
	private JCheckBox removeDataAttr;
	private JCheckBox removeOriginator;

	private JPanel listPanel;

	public OntologyAbstractionParamDialog(LogSummary summary,
			OntologyAbstractionFilter filter) {
		super(summary, filter);
	}

	protected boolean getAllParametersSet() {
		return true;
	}

	private boolean getInitialReplaceInstancesElement() {
		return ((OntologyAbstractionFilter) this.filter)
				.getReplaceInstancesElement();
	}

	private boolean getInitialReplaceInstancesType() {
		return ((OntologyAbstractionFilter) this.filter)
				.getReplaceInstancesType();
	}

	private boolean getInitialReplaceInstancesOriginator() {
		return ((OntologyAbstractionFilter) this.filter)
				.getReplaceInstancesOriginator();
	}

	private boolean getInitialUsePriority() {
		return ((OntologyAbstractionFilter) this.filter).getUsePriority();
	}

	private List<ConceptSelection> getInitialConceptPriority() {
		return ((OntologyAbstractionFilter) this.filter).getConceptPriority();
	}

	public LogFilter getNewLogFilter() {
		ConceptListModel model = (ConceptListModel) conceptList.getModel();

		return new OntologyAbstractionFilter(tree.getSelection(), model
				.getConceptPriorityList(), usePriorities.isSelected(),
				replaceInstancesElement.isSelected(), replaceInstancesType
						.isSelected(), replaceInstancesOriginator.isSelected(),
				removePI.isSelected(), removeWfme.isSelected(), removeType
						.isSelected(), removeDataAttr.isSelected(),
				removeOriginator.isSelected());
	}

	protected JPanel getPanel() {
		OntologyCollection ontologyCollection = summary.getOntologies();
		List<OntologyModel> ontologies = ontologyCollection.getOntologies();

		listPanel = createConceptList(ontologies);
		JPanel treePanel = createOntologyTree(ontologies);
		JPanel replacePanel = createReplaceInstancesPanel();
		JPanel removePanel = createRemoveInstancesPanel();

		JPanel innerOptionsPanel = new JPanel(new BorderLayout());
		innerOptionsPanel.add(removePanel, BorderLayout.NORTH);
		innerOptionsPanel.add(replacePanel, BorderLayout.SOUTH);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(innerOptionsPanel, BorderLayout.NORTH);
		rightPanel.add(listPanel, BorderLayout.CENTER);

		JSplitPane split = new JSplitPane();
		split.setDividerLocation(0.5);
		split.setResizeWeight(0.5);
		split.setOneTouchExpandable(true);
		split.setLeftComponent(treePanel);
		split.setRightComponent(rightPanel);

		JPanel main = new JPanel(new BorderLayout());
		main.add(split, BorderLayout.CENTER);

		return main;
	}

	private JPanel createRemoveInstancesPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1));

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		removePI = new JCheckBox(
				"Process instance, if it has no model references");
		removePI.setSelected(getInitialRemovePI());
		removeWfme = new JCheckBox(
				"Audit trail entry, if its workflow model element has no model references");
		removeWfme.setSelected(getInitialRemoveWfme());
		removeType = new JCheckBox(
				"Audit trail entry, if its event type has no model references");
		removeType.setSelected(getInitialRemoveType());
		removeDataAttr = new JCheckBox(
				"Data attribute, if it has no model references");
		removeDataAttr.setSelected(getInitialRemoveDataAttr());
		removeOriginator = new JCheckBox(
				"Originator, if it has no model references");
		removeOriginator.setSelected(getInitialRemoveOriginator());
		panel.add(new JLabel("2) Remove:"));
		panel.add(removePI);
		panel.add(removeDataAttr);
		panel.add(removeWfme);
		panel.add(removeType);
		panel.add(removeOriginator);

		JPanel result = new JPanel(new BorderLayout());
		result.add(panel, BorderLayout.WEST);
		return result;
	}

	private boolean getInitialRemovePI() {
		return ((OntologyAbstractionFilter) this.filter).getRemovePI();
	}

	private boolean getInitialRemoveWfme() {
		return ((OntologyAbstractionFilter) this.filter).getRemoveWfme();
	}

	private boolean getInitialRemoveType() {
		return ((OntologyAbstractionFilter) this.filter).getRemoveType();
	}

	private boolean getInitialRemoveDataAttr() {
		return ((OntologyAbstractionFilter) this.filter).getRemoveDataAttr();
	}

	private boolean getInitialRemoveOriginator() {
		return ((OntologyAbstractionFilter) this.filter).getRemoveOriginator();
	}

	private JPanel createReplaceInstancesPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1));
		ActionListener updater = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateConceptList();
			}
		};

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		replaceInstancesElement = new JCheckBox("Workflow model element");
		replaceInstancesElement
				.setSelected(getInitialReplaceInstancesElement());
		replaceInstancesElement.addActionListener(updater);
		replaceInstancesType = new JCheckBox("Event type");
		replaceInstancesType.setSelected(getInitialReplaceInstancesType());
		replaceInstancesType.addActionListener(updater);
		replaceInstancesOriginator = new JCheckBox("Originator");
		replaceInstancesOriginator
				.setSelected(getInitialReplaceInstancesOriginator());
		replaceInstancesOriginator.addActionListener(updater);
		panel.add(new JLabel("3) Replace instances by concepts for:"));
		panel.add(replaceInstancesElement);
		panel.add(replaceInstancesType);
		panel.add(replaceInstancesOriginator);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// we have to call updateConceptList from the Swing event thread
				// to avoid multithreading issues
				updateConceptList();
			}
		});

		JPanel result = new JPanel(new BorderLayout());
		result.add(panel, BorderLayout.WEST);
		return result;
	}

	protected void updateConceptList() {
		listPanel.setVisible(replaceInstancesElement.isSelected()
				|| replaceInstancesType.isSelected()
				|| replaceInstancesOriginator.isSelected());
	}

	private JPanel createConceptList(List<OntologyModel> ontologies) {
		conceptList = new JList(new ConceptListModel(ontologies,
				getInitialConceptPriority()));
		conceptList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		conceptList.setVisibleRowCount(-1);

		final JButton toTop = new JButton("Top");
		final JButton up = new JButton("Up");
		final JButton down = new JButton("Down");
		final JButton toBottom = new JButton("Bottom");
		toTop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ConceptListModel model = (ConceptListModel) conceptList
						.getModel();
				int[] newIndices = model.moveToTop(conceptList
						.getSelectedIndices());
				conceptList.setSelectedIndices(newIndices);
				conceptList.repaint();
			}
		});
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ConceptListModel model = (ConceptListModel) conceptList
						.getModel();
				int[] newIndices = model.moveUp(conceptList
						.getSelectedIndices());
				conceptList.setSelectedIndices(newIndices);
				conceptList.repaint();
			}
		});
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ConceptListModel model = (ConceptListModel) conceptList
						.getModel();
				int[] newIndices = model.moveDown(conceptList
						.getSelectedIndices());
				conceptList.setSelectedIndices(newIndices);
				conceptList.repaint();
			}
		});
		toBottom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ConceptListModel model = (ConceptListModel) conceptList
						.getModel();
				int[] newIndices = model.moveToBottom(conceptList
						.getSelectedIndices());
				conceptList.setSelectedIndices(newIndices);
				conceptList.repaint();
			}
		});

		JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
		buttonPanel.add(toTop);
		buttonPanel.add(up);
		buttonPanel.add(down);
		buttonPanel.add(toBottom);

		JPanel buttonPanelContainer = new JPanel();
		buttonPanelContainer.add(buttonPanel);

		usePriorities = new JRadioButton(
				"Replace instances only with the highest priority concept");
		usePriorities.setSelected(getInitialUsePriority());

		dontUsePriorities = new JRadioButton(
				"Replace instances with the concatenation of all their concepts");
		dontUsePriorities.setSelected(!getInitialUsePriority());

		ButtonGroup group = new ButtonGroup();
		group.add(usePriorities);
		group.add(dontUsePriorities);

		final JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(conceptList), BorderLayout.CENTER);
		centerPanel.add(buttonPanelContainer, BorderLayout.EAST);

		final ActionListener usePrioritiesActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				centerPanel.setVisible(usePriorities.isSelected());
			}
		};
		usePriorities.addActionListener(usePrioritiesActionListener);
		dontUsePriorities.addActionListener(usePrioritiesActionListener);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// we have to call this from the Swing event thread to avoid
				// multithreading issues
				usePrioritiesActionListener.actionPerformed(null);
			}
		});

		JPanel radioPanel = new JPanel(new BorderLayout());
		radioPanel.add(dontUsePriorities, BorderLayout.NORTH);
		radioPanel.add(usePriorities, BorderLayout.SOUTH);

		JPanel usePrioPanel = new JPanel(new BorderLayout());
		usePrioPanel
				.add(
						new JLabel(
								"4) When replacing instances which have more than one concept:"),
						BorderLayout.NORTH);
		usePrioPanel.add(radioPanel, BorderLayout.SOUTH);

		JPanel result = new JPanel(new BorderLayout());
		result.add(usePrioPanel, BorderLayout.NORTH);
		result.add(centerPanel, BorderLayout.CENTER);

		result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return result;
	}

	private JPanel createOntologyTree(List<OntologyModel> ontologies) {
		InitialSelectionCallback initialSelection = new InitialSelectionCallback() {
			public boolean isInitiallySelected(List<String> path) {
				List<List<String>> sel = ((OntologyAbstractionFilter) filter)
						.getSelection();
				return sel == null || sel.contains(path);
			}
		};
		SelectionChangeListener listener = new SelectionChangeListener() {
			public void selectionChanged(TreeNode root) {
				((ConceptListModel) conceptList.getModel()).update(root);
				conceptList.repaint();
			}
		};
		List<ModelGraph> models = new ArrayList<ModelGraph>();

		for (OntologyModel ontology : ontologies) {
			models.add(ontology.toModelGraph());
		}
		tree = new ModelGraphTreeComponent(models, initialSelection, listener,
				new NameProvider() {
					public String getName(Object vertex) {
						return OntologyModel
								.getConceptPart(((ModelGraph) vertex)
										.getIdentifier());
					}
				}, new NameProvider() {
					public String getName(Object vertex) {
						return OntologyModel
								.getConceptPart(((ModelGraphVertex) vertex)
										.getIdentifier());
					}
				});

		JPanel titlePanel = new JPanel(new GridLayout(1, 1));
		titlePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JLabel title = new JLabel("1) Projected Ontology Tree");
		title.setHorizontalAlignment(SwingConstants.LEFT);
		titlePanel.add(title);

		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(titlePanel, BorderLayout.NORTH);
		treePanel.add(tree, BorderLayout.CENTER);

		return treePanel;
	}
}

class ConceptSelection {
	private final String conceptUri;
	private final boolean selected;

	public ConceptSelection(String conceptUri, boolean selected) {
		this.conceptUri = conceptUri;
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getConceptURI() {
		return conceptUri;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ConceptSelection)) {
			return false;
		}
		ConceptSelection otherSelection = (ConceptSelection) other;
		return conceptUri.equals(otherSelection.conceptUri)
				&& selected == otherSelection.selected;
	}

	public int hashCode() {
		return (conceptUri + selected).hashCode();
	}
}

class ConceptListModel extends AbstractListModel {

	private static final long serialVersionUID = -5878989613247165495L;

	private List<ConceptListNode> conceptPriorityList;

	public ConceptListModel(List<OntologyModel> ontologies,
			List<ConceptSelection> initialConceptPriority) {
		conceptPriorityList = new ArrayList<ConceptListNode>();

		for (OntologyModel ontology : ontologies) {
			for (ConceptModel concept : ontology.getAllConcepts()) {
				conceptPriorityList.add(new ConceptListNode(concept));
			}
		}

		if (initialConceptPriority != null) {
			List<ConceptListNode> ordered = new ArrayList<ConceptListNode>();
			for (ConceptSelection selection : initialConceptPriority) {
				String uri = selection.getConceptURI();
				Iterator<ConceptListNode> nodes = conceptPriorityList
						.iterator();

				while (nodes.hasNext()) {
					ConceptListNode node = nodes.next();
					String nodeUri = node.getConcept().getName();

					if (uri.equals(nodeUri)) {
						ordered.add(node);
						nodes.remove();
					}
				}
			}
			ordered.addAll(conceptPriorityList);
			conceptPriorityList = ordered;
		}
	}

	public List<ConceptSelection> getConceptPriorityList() {
		List<ConceptSelection> result = new ArrayList<ConceptSelection>();

		for (ConceptListNode node : conceptPriorityList) {
			result.add(new ConceptSelection(node.getConcept().getName(), node
					.isSelected()));
		}
		return result;
	}

	public int[] moveToBottom(int[] selectedIndices) {
		int[] selection = translateSelectedIndices(selectedIndices);
		int[] newIndices = new int[selectedIndices.length];

		for (int i = 0; i < newIndices.length; i++) {
			newIndices[i] = getSize() - selectedIndices.length + i;
		}

		for (ConceptListNode node : remove(selection)) {
			conceptPriorityList.add(node);
		}
		return newIndices;
	}

	public int[] moveDown(int[] selectedIndices) {
		// increment the indices of all selected items
		int[] incremented = new int[selectedIndices.length];
		for (int i = 0; i < selectedIndices.length; i++) {
			incremented[i] = selectedIndices[i] + 1;
		}

		// make sure we don't go above the largest possible index, and that no
		// two indices become the same
		int maxIndex = getSize() - 1;
		for (int i = incremented.length - 1; i >= 0; i--) {
			incremented[i] = Math.min(incremented[i], maxIndex);
			maxIndex = incremented[i] - 1;
		}

		// do the actual moving
		int[] selected = translateSelectedIndices(selectedIndices);
		int[] newIndices = translateSelectedIndices(incremented);

		int i = 0;
		for (ConceptListNode node : remove(selected)) {
			conceptPriorityList.add(newIndices[i], node);
			i++;
		}
		return incremented;
	}

	public int[] moveUp(int[] selectedIndices) {
		// decrement the indices of all selected items
		int[] decremented = new int[selectedIndices.length];
		for (int i = 0; i < selectedIndices.length; i++) {
			decremented[i] = selectedIndices[i] - 1;
		}

		// make sure we don't go below zero, and that no two indices become the
		// same
		int minIndex = 0;
		for (int i = 0; i < decremented.length; i++) {
			decremented[i] = Math.max(decremented[i], minIndex);
			minIndex = decremented[i] + 1;
		}

		// do the actual moving
		int[] selected = translateSelectedIndices(selectedIndices);
		int[] newIndices = translateSelectedIndices(decremented);

		int i = 0;
		for (ConceptListNode node : remove(selected)) {
			conceptPriorityList.add(newIndices[i], node);
			i++;
		}
		return decremented;
	}

	public int[] moveToTop(int[] selectedIndices) {
		int[] indices = translateSelectedIndices(selectedIndices);
		int[] result = new int[selectedIndices.length];

		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];

			conceptPriorityList.add(i, conceptPriorityList.remove(index));
			result[i] = i;
		}
		return result;
	}

	private int[] translateSelectedIndices(int[] selectedIndices) {
		int[] result = new int[selectedIndices.length];
		int indexInSelected = 0;
		int i = 0;
		int j = 0;

		for (ConceptListNode concept : conceptPriorityList) {
			if (concept.isSelected()) {
				if (indexInSelected < selectedIndices.length
						&& i == selectedIndices[indexInSelected]) {
					result[indexInSelected] = j;
					indexInSelected++;
				}
				i++;
			}
			j++;
		}
		return result;
	}

	private List<ConceptListNode> remove(int[] indices) {
		List<ConceptListNode> result = new ArrayList<ConceptListNode>();

		for (int i = 0; i < indices.length; i++) {
			result.add(conceptPriorityList.remove(indices[i] - i));
		}
		return result;
	}

	public Object getElementAt(int index) {
		int i = 0;
		for (ConceptListNode concept : conceptPriorityList) {
			if (concept.isSelected()) {
				if (i == index) {
					return concept.getName();
				}
				i++;
			}
		}
		return null;
	}

	public int getSize() {
		int n = 0;
		for (ConceptListNode concept : conceptPriorityList) {
			if (concept.isSelected()) {
				n++;
			}
		}
		return n;
	}

	public void update(TreeNode root) {
		int oldSize = getSize();

		for (ConceptListNode concept : conceptPriorityList) {
			concept.setSelected(false);
		}
		if (oldSize > 0) {
			fireIntervalRemoved(this, 0, oldSize);
		}

		select(root);
		if (getSize() > 0) {
			fireIntervalAdded(this, 0, getSize() - 1);
		}
	}

	private void select(TreeNode node) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			select((TreeNode) e.nextElement());
		}

		if (node instanceof VertexNode) {
			VertexNode conceptNode = (VertexNode) node;

			if (conceptNode.isSelected()) {
				select(conceptNode);
			}
		}
	}

	private void select(VertexNode node) {
		for (ConceptListNode concept : conceptPriorityList) {
			if (concept.getConcept().getName().equals(
					node.getVertex().getIdentifier())) {
				concept.setSelected(true);
			}
		}
	}
}

class ConceptListNode extends SelectableNode {

	private static final long serialVersionUID = -7438327407115913249L;

	private ConceptModel concept;

	public ConceptListNode(ConceptModel concept) {
		super(concept.getShortName());
		this.concept = concept;
	}

	public ConceptModel getConcept() {
		return concept;
	}

	public String getName() {
		return concept.getOntology().getShortName()
				+ OntologyModel.ONTOLOGY_SEPARATOR + toString();
	}

	public boolean equals(Object other) {
		if (!(other instanceof ConceptListNode)) {
			return false;
		}
		ConceptListNode otherNode = (ConceptListNode) other;

		return concept.getName().equals(otherNode.concept.getName());
	}
}

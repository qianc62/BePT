package org.processmining.framework.models.ui.treecomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;

public class ModelGraphTreeComponent extends JPanel {

	private static final long serialVersionUID = -4085622239386104685L;

	private JTree tree;
	private DefaultMutableTreeNode root;
	private Collection<SelectionChangeListener> listeners = new ArrayList<SelectionChangeListener>();
	private NameProvider vertexNamer;
	private NameProvider graphNamer;

	public ModelGraphTreeComponent(Collection<ModelGraph> graphs,
			InitialSelectionCallback initialSelection,
			SelectionChangeListener listener, NameProvider graphNamer,
			NameProvider vertexNamer) {
		this(graphs, initialSelection, listener, BorderLayout.EAST, graphNamer,
				vertexNamer);
	}

	public ModelGraphTreeComponent(Collection<ModelGraph> graphs,
			InitialSelectionCallback initialSelection,
			SelectionChangeListener listener, String buttonPlacement,
			NameProvider graphNamer, NameProvider vertexNamer) {
		addSelectionChangeListener(listener);
		this.graphNamer = graphNamer;
		this.vertexNamer = vertexNamer;
		init(graphs, initialSelection, buttonPlacement);
	}

	public void expandAll(boolean expand) {
		expandTree(new TreePath(root), expand);
	}

	public void selectAll(boolean select) {
		selectTree(root, select);
	}

	public List<List<String>> getSelection() {
		return getSelection(root);
	}

	public Set<ModelGraphVertex> getSelectedNodes() {
		Set<ModelGraphVertex> result = new HashSet<ModelGraphVertex>();

		getSelectedNodes(root, result);
		return result;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public boolean isEmpty() {
		return root.getChildCount() == 0;
	}

	private void init(Collection<ModelGraph> graphs,
			InitialSelectionCallback initialSelection, String buttonPlacement) {
		root = new DefaultMutableTreeNode("");
		tree = new JTree(root);
		tree.setEditable(false);
		tree.setCellRenderer(new VertexNodeRenderer());
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		for (ModelGraph graph : graphs) {
			GraphNode graphNode = new GraphNode(graphNamer.getName(graph));

			graphNode.setSelected(initialSelection == null ? true
					: initialSelection
							.isInitiallySelected(getSelectionPath(graphNode)));

			root.add(graphNode);
			for (ModelGraphVertex vertex : graph.getVerticeList()) {
				if (vertex.inDegree() == 0) {
					createGraphTree(graph, vertex, graphNode, initialSelection);
				}
			}
		}

		expandAll(true);

		JPanel buttons;
		if (buttonPlacement.equals(BorderLayout.EAST)) {
			buttons = new JPanel(new GridLayout(4, 1));
		} else {
			buttons = new JPanel(new GridLayout(2, 2));
		}
		JButton expand = new JButton("Expand");
		expand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandAll(true);
				repaint();
			}
		});
		JButton collapse = new JButton("Collapse");
		collapse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandAll(false);
				repaint();
			}
		});
		JButton selectAll = new JButton("Select");
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll(true);
				fireSelectionChangedEvent();
				repaint();
			}
		});
		JButton deselectAll = new JButton("Deselect");
		deselectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll(false);
				fireSelectionChangedEvent();
				repaint();
			}
		});
		buttons.add(expand);
		buttons.add(collapse);
		buttons.add(selectAll);
		buttons.add(deselectAll);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(buttons);

		final MouseListener updateSuperSubSonceptsSelectionListener = new UpdateParentsAndChildrenListener(
				listeners, tree, root);
		final MouseListener independentSelectionListener = new IndependentSelectionListener(
				listeners, tree, root);

		final JCheckBox selectSuperSubCheckBox = new JCheckBox(
				"<html>Automatically select all parents / deselect all children</html>");
		ActionListener selectionActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree
						.removeMouseListener(updateSuperSubSonceptsSelectionListener);
				tree.removeMouseListener(independentSelectionListener);
				if (selectSuperSubCheckBox.isSelected()) {
					tree
							.addMouseListener(updateSuperSubSonceptsSelectionListener);
				} else {
					tree.addMouseListener(independentSelectionListener);
				}
			}
		};
		selectSuperSubCheckBox.setSelected(false);
		selectSuperSubCheckBox.addActionListener(selectionActionListener);
		selectionActionListener.actionPerformed(null);

		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(new JScrollPane(tree), BorderLayout.CENTER);
		treePanel.add(selectSuperSubCheckBox, BorderLayout.SOUTH);

		this.setLayout(new BorderLayout());
		this.add(treePanel, BorderLayout.CENTER);
		this.add(buttonsPanel, buttonPlacement);

		fireSelectionChangedEvent();
	}

	public void fireSelectionChangedEvent() {
		for (SelectionChangeListener listener : listeners) {
			listener.selectionChanged(root);
		}
	}

	private void createGraphTree(ModelGraph graph, ModelGraphVertex vertex,
			DefaultMutableTreeNode parent,
			InitialSelectionCallback initialSelection) {
		VertexNode node = new VertexNode(vertex, vertexNamer.getName(vertex));

		parent.add(node);

		node.setSelected(initialSelection == null ? true : initialSelection
				.isInitiallySelected(getSelectionPath(node)));

		for (Object subvertex : vertex.getSuccessors()) {
			createGraphTree(graph, (ModelGraphVertex) subvertex, node,
					initialSelection);
		}
	}

	private void expandTree(TreePath parent, boolean expand) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			TreeNode n = (TreeNode) e.nextElement();
			TreePath path = parent.pathByAddingChild(n);
			expandTree(path, expand);
		}
		if (expand) {
			tree.expandPath(parent);
		} else if (node instanceof SelectableNode) {
			tree.collapsePath(parent);
		}
	}

	private void selectTree(TreeNode node, boolean select) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			selectTree((TreeNode) e.nextElement(), select);
		}
		if (node instanceof SelectableNode) {
			((SelectableNode) node).setSelected(select);
		}
	}

	private List<List<String>> getSelection(TreeNode node) {
		List<List<String>> result = new ArrayList<List<String>>();

		for (Enumeration e = node.children(); e.hasMoreElements();) {
			result.addAll(getSelection((TreeNode) e.nextElement()));
		}
		if (node instanceof SelectableNode
				&& ((SelectableNode) node).isSelected()) {
			result.add(getSelectionPath(node));
		}
		return result;
	}

	private List<String> getSelectionPath(TreeNode node) {
		List<String> path = new LinkedList<String>();

		while (node != null && node instanceof SelectableNode) {
			path.add(0, node.toString());
			node = node.getParent();
		}
		return path;
	}

	private void getSelectedNodes(TreeNode node, Set<ModelGraphVertex> result) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			getSelectedNodes((TreeNode) e.nextElement(), result);
		}
		if (node instanceof VertexNode) {
			VertexNode vertexNode = (VertexNode) node;

			if (vertexNode.isSelected()) {
				result.add(vertexNode.getVertex());
			}
		}
	}
}

class VertexNodeRenderer implements TreeCellRenderer {

	private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
	private JPanel panel;
	private JLabel label;
	private JLabel label2;
	private JCheckBox checkBox;
	private Color selectedColor;
	private Color backgroundColor;

	public VertexNodeRenderer() {
		this.backgroundColor = defaultRenderer.getBackgroundNonSelectionColor();
		this.selectedColor = defaultRenderer.getBackgroundSelectionColor();
		checkBox = new JCheckBox();
		panel = new JPanel(new BorderLayout());
		label = new JLabel();
		panel.add(checkBox, BorderLayout.WEST);
		panel.add(label, BorderLayout.EAST);
		label2 = new JLabel();
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof SelectableNode) {
			label.setBackground(sel ? selectedColor : backgroundColor);
			panel.setBackground(sel ? selectedColor : backgroundColor);
			checkBox.setBackground(sel ? selectedColor : backgroundColor);
			label.setText(value.toString());
			checkBox.setSelected(((SelectableNode) value).isSelected());
			checkBox.setVisible(value instanceof VertexNode);
			return panel;
		} else {
			label2.setBackground(sel ? selectedColor : backgroundColor);
			label2.setText(value.toString());
			return label2;
		}
	}
}

abstract class CheckBoxTreeNodeListener implements MouseListener {

	private Collection<SelectionChangeListener> listeners;
	private JTree tree;
	private TreeNode root;

	public CheckBoxTreeNodeListener(
			Collection<SelectionChangeListener> listeners, JTree tree,
			TreeNode root) {
		this.listeners = listeners;
		this.tree = tree;
		this.root = root;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent me) {
		if (me.getSource().equals(tree) && me.getClickCount() == 1) {
			TreePath path = tree
					.getClosestPathForLocation(me.getX(), me.getY());
			Rectangle rect = tree.getPathBounds(path);

			if (rect.contains(me.getX(), me.getY())
					&& path.getLastPathComponent() instanceof VertexNode) {
				VertexNode node = (VertexNode) path.getLastPathComponent();

				if (node.isSelected()) {
					deselectNode(node);
				} else {
					selectNode(node);
				}
				for (SelectionChangeListener listener : listeners) {
					listener.selectionChanged(root);
				}
				tree.repaint();
			}
		}
	}

	public abstract void selectNode(TreeNode node);

	public abstract void deselectNode(TreeNode node);
}

class UpdateParentsAndChildrenListener extends CheckBoxTreeNodeListener {

	public UpdateParentsAndChildrenListener(
			Collection<SelectionChangeListener> listeners, JTree tree,
			TreeNode root) {
		super(listeners, tree, root);
	}

	@Override
	public void deselectNode(TreeNode node) {
		// deselect all children as well
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			deselectNode((TreeNode) e.nextElement());
		}
		if (node instanceof SelectableNode) {
			((SelectableNode) node).setSelected(false);
		}
	}

	@Override
	public void selectNode(TreeNode node) {
		// select all parents as well
		if (node != null) {
			if (node instanceof SelectableNode) {
				((SelectableNode) node).setSelected(true);
			}
			selectNode(node.getParent());
		}
	}
}

class IndependentSelectionListener extends CheckBoxTreeNodeListener {

	public IndependentSelectionListener(
			Collection<SelectionChangeListener> listeners, JTree tree,
			TreeNode root) {
		super(listeners, tree, root);
	}

	@Override
	public void deselectNode(TreeNode node) {
		if (node != null && node instanceof SelectableNode) {
			((SelectableNode) node).setSelected(false);
		}
	}

	@Override
	public void selectNode(TreeNode node) {
		if (node != null && node instanceof SelectableNode) {
			((SelectableNode) node).setSelected(true);
		}
	}
}

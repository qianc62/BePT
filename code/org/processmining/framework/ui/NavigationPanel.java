package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.menus.AnalysisMenu;
import org.processmining.framework.ui.menus.ConversionMenu;
import org.processmining.framework.ui.menus.ExportMenu;
import org.processmining.framework.ui.menus.MineMenu;

public class NavigationPanel extends JPanel {

	private static final long serialVersionUID = 4207590110156307024L;

	private MDIDesktopPane desktop;
	private boolean isVisible;

	private Map<JInternalFrame, NavTreeNode> frame2winnode;
	private Map<NavTreeNode, JInternalFrame> winnode2frame;
	private Map<NavTreeNode, ProvidedObject> ponode2object;
	private Map<NavTreeNode, JInternalFrame> ponode2owningFrame;
	private Map<JInternalFrame, Set<NavTreeNode>> owningFrame2ponode;
	private JTree tree;
	private NavTreeNode root;
	private DefaultTreeModel model;
	private JPanel multipleSelectionButtons;
	private NavigationCellRenderer renderer;

	public NavigationPanel(MDIDesktopPane desktop) {
		this.desktop = desktop;

		this.frame2winnode = new HashMap<JInternalFrame, NavTreeNode>();
		this.winnode2frame = new HashMap<NavTreeNode, JInternalFrame>();
		this.ponode2object = new HashMap<NavTreeNode, ProvidedObject>();
		this.ponode2owningFrame = new HashMap<NavTreeNode, JInternalFrame>();
		this.owningFrame2ponode = new HashMap<JInternalFrame, Set<NavTreeNode>>();

		setVisible(false);
		init();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		isVisible = visible;
	}

	private void startProvidedObjectsMonitor() {
		Thread monitor = new Thread("navigation-panel-monitor") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
					if (isVisible) {
						updateProvidedObjects();
					}
				}
			}
		};
		monitor.setDaemon(true);
		monitor.setPriority(Thread.MIN_PRIORITY);
		monitor.start();
	}

	private void init() {
		root = new NavTreeNode("ProM");
		model = new DefaultTreeModel(root);
		tree = new NavTree(model);
		renderer = new NavigationCellRenderer();

		tree.setEditable(true);
		tree.setRootVisible(false);
		tree.setScrollsOnExpand(true);
		tree.setShowsRootHandles(true);
		tree.setToggleClickCount(3); // disables expanding/collapsing of nodes
		// by double-clicking on them
		tree.setOpaque(false);
		tree.setCellRenderer(renderer);
		tree.setCellEditor(new DefaultTreeCellEditor(tree, renderer));

		tree.addMouseListener(new WindowNodeListener(this, tree));
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				multipleSelectionButtons
						.setVisible(getSelectedFramesIncludingOwningFrames()
								.size() > 1);
				revalidate();
				repaint();
			}
		});
		tree.getModel().addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				updateFrameTitles();
			}

			public void treeNodesInserted(TreeModelEvent e) {
			}

			public void treeNodesRemoved(TreeModelEvent e) {
			}

			public void treeStructureChanged(TreeModelEvent e) {
			}
		});

		desktop.addContainerListener(new ContainerListener() {
			public void componentAdded(ContainerEvent e) {
				added(e.getChild());
			}

			public void componentRemoved(ContainerEvent e) {
				removed(e.getChild());
			}
		});

		JButton tileHorizontallyButton = new JButton("Tile horizontally");
		JButton tileVerticallyButton = new JButton("Tile vertically");
		tileHorizontallyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				desktop
						.tileFramesHorizontally(getSelectedFramesIncludingOwningFrames());
			}
		});
		tileVerticallyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				desktop
						.tileFramesVertically(getSelectedFramesIncludingOwningFrames());
			}
		});

		multipleSelectionButtons = new JPanel(new GridLayout(0, 1));
		multipleSelectionButtons.setVisible(false);
		multipleSelectionButtons.add(tileHorizontallyButton);
		multipleSelectionButtons.add(tileVerticallyButton);
		multipleSelectionButtons.setBackground(tree.getBackground());

		JPanel gradient = new GradientPanel(new Color(0x98, 0x98, 0x98),
				new Color(0x55, 0x55, 0x55));
		gradient.setLayout(new BorderLayout());
		gradient.add(tree, BorderLayout.CENTER);

		JScrollPane scrollableTree = new JScrollPane(gradient);
		scrollableTree.setBorder(null);
		scrollableTree.getHorizontalScrollBar().setUI(
				new SlickerScrollBarUI(scrollableTree.getHorizontalScrollBar(),
						new Color(0, 0, 0), new Color(30, 30, 30), new Color(
								50, 50, 50), 3, 12));
		scrollableTree.getVerticalScrollBar().setUI(
				new SlickerScrollBarUI(scrollableTree.getVerticalScrollBar(),
						new Color(0, 0, 0), new Color(30, 30, 30), new Color(
								50, 50, 50), 3, 12));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(scrollableTree, BorderLayout.CENTER);
		mainPanel.add(multipleSelectionButtons, BorderLayout.SOUTH);
		mainPanel.setPreferredSize(new Dimension(200, 200));

		startProvidedObjectsMonitor();

		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	}

	protected synchronized void updateProvidedObjects() {
		boolean debug = false;

		if (debug) {
			Message.add("Updating provided objects");
		}

		for (Map.Entry<JInternalFrame, NavTreeNode> item : frame2winnode
				.entrySet()) {
			JInternalFrame frame = item.getKey();
			NavTreeNode winnode = item.getValue();
			boolean hadNoChildren = winnode.getChildCount() == 0;

			if (frame instanceof Provider) {
				Provider provider = (Provider) frame;
				Set<NavTreeNode> initialPoNodes = new HashSet<NavTreeNode>(
						owningFrame2ponode.get(frame));

				if (provider != null) {
					ProvidedObject[] objects = provider.getProvidedObjects();

					if (objects != null) {
						for (ProvidedObject object : provider
								.getProvidedObjects()) {
							boolean found = false;

							if (object == null) {
								continue;
							}

							for (NavTreeNode poNode : initialPoNodes) {
								if (poNode.isProvidedObject()
										&& poNode.toString().equals(
												object.getName())) {
									// PO is already in the tree; only update
									// the mapping so we always have the latest
									// provided object
									ponode2object.put(poNode, object);
									initialPoNodes.remove(poNode);
									found = true;
									break;
								}
							}
							if (!found) {
								// PO is not in the tree yet, so we add it now
								NavTreeNode poNode = new NavTreeNode(object);

								model.insertNodeInto(poNode, winnode, winnode
										.getChildCount());
								ponode2object.put(poNode, object);
								ponode2owningFrame.put(poNode, frame);
								owningFrame2ponode.get(frame).add(poNode);
								if (debug) {
									Message
											.add("  Detected new provided object: "
													+ object.getName()
													+ " in frame "
													+ frame.getTitle());
								}
							}
						}
					}
				}

				for (NavTreeNode node : initialPoNodes) {
					// node is no longer an object provided by this frame
					model.removeNodeFromParent(node);
					ponode2owningFrame.remove(node);
					ponode2object.remove(node);
					owningFrame2ponode.get(frame).remove(node);

					if (debug) {
						Message.add("  Detected deleted provided object: "
								+ node.toString() + " in frame "
								+ frame.getTitle());
					}
				}
				if (hadNoChildren && winnode.getChildCount() > 0) {
					tree.expandPath(new TreePath(winnode.getPath()));
				}
			}
		}
	}

	protected synchronized void updateFrameTitles() {
		for (Map.Entry<JInternalFrame, NavTreeNode> frame : frame2winnode
				.entrySet()) {
			frame.getKey().setTitle(frame.getValue().toString());
		}
		for (Map.Entry<NavTreeNode, ProvidedObject> frame : ponode2object
				.entrySet()) {
			frame.getValue().setName(frame.getKey().toString());
		}
	}

	protected synchronized Collection<JInternalFrame> getSelectedFramesIncludingOwningFrames() {
		Set<JInternalFrame> result = new HashSet<JInternalFrame>();

		if (tree.getSelectionPaths() != null) {
			for (TreePath path : tree.getSelectionPaths()) {
				NavTreeNode node = (NavTreeNode) path.getLastPathComponent();
				JInternalFrame frame = winnode2frame.get(node);

				if (frame != null) {
					result.add(frame);
				} else {
					JInternalFrame owningFrame = ponode2owningFrame.get(node);

					if (owningFrame != null) {
						result.add(owningFrame);
					}
				}
			}
		}
		return result;
	}

	protected synchronized void added(Component child) {
		if (child instanceof JInternalFrame) {
			JInternalFrame frame = (JInternalFrame) child;
			NavTreeNode node = new NavTreeNode(frame);
			boolean hierarchical = false; // TODO setting this to true is
			// possible, but it will give
			// problems when windows are closed

			if (hierarchical) {
				JInternalFrame parentFrame = desktop.getSelectedFrame();
				NavTreeNode parentNode = parentFrame != null
						&& frame2winnode.get(parentFrame) != null ? frame2winnode
						.get(parentFrame)
						: root;

				model.insertNodeInto(node, parentNode, parentNode
						.getChildCount());
			} else {
				model.insertNodeInto(node, root, root.getChildCount());
			}

			frame2winnode.put(frame, node);
			winnode2frame.put(node, frame);
			owningFrame2ponode.put(frame, new HashSet<NavTreeNode>());

			updateProvidedObjects();

			tree.expandPath(new TreePath(node.getPath()).getParentPath());
			tree.expandPath(new TreePath(node.getPath()));
		}
	}

	protected synchronized void removed(Component child) {
		if (child instanceof JInternalFrame) {
			JInternalFrame frame = (JInternalFrame) child;

			if (frame2winnode.containsKey(frame)) {
				NavTreeNode node = frame2winnode.get(frame);

				model.removeNodeFromParent(node);
				winnode2frame.remove(node);
				frame2winnode.remove(frame);

				for (NavTreeNode poNode : owningFrame2ponode.get(frame)) {
					ponode2owningFrame.remove(poNode);
					ponode2object.remove(poNode);
				}
				owningFrame2ponode.remove(frame);
			}
		}
	}

	protected synchronized void doubleClick(TreePath path) {
		NavTreeNode node = (NavTreeNode) path.getLastPathComponent();
		JInternalFrame frame = winnode2frame.get(node);

		if (frame != null) {
			focus(frame, true);
		} else {
			ProvidedObject providedObject = ponode2object.get(node);
			JInternalFrame owningFrame = ponode2owningFrame.get(node);

			if (providedObject != null && owningFrame != null
					&& owningFrame instanceof Provider) {
				focus(owningFrame, false);
				MainUI.getInstance().showLauncher((Provider) owningFrame,
						providedObject);
			}
		}
	}

	protected synchronized void rightClick(TreePath path, final int x,
			final int y) {
		NavTreeNode node = (NavTreeNode) path.getLastPathComponent();
		ProvidedObject providedObject = ponode2object.get(node);

		if (providedObject != null) {
			ProvidedObject[] objects = new ProvidedObject[] { providedObject };

			tree.setSelectionPath(new TreePath(node.getPath()));
			focus(ponode2owningFrame.get(node), false);

			final JPopupMenu popup = new JPopupMenu();
			popup.add(new MineMenu(objects));
			popup.add(new AnalysisMenu(objects));
			popup.add(new ConversionMenu(objects));
			popup.add(new ExportMenu(objects));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					popup.show(tree, x, y);
				}
			});
		}
	}

	private void focus(JInternalFrame frame, boolean maximize) {
		if (frame != null) {
			frame.toFront();
			try {
				frame.setSelected(true);
			} catch (PropertyVetoException e1) {
			}
			frame.requestFocusInWindow();
			if (maximize) {
				try {
					frame.setMaximum(true);
				} catch (PropertyVetoException e) {
				}
			}
		}
	}
}

class WindowNodeListener extends MouseAdapter {

	private JTree tree;
	private NavigationPanel panel;

	public WindowNodeListener(NavigationPanel panel, JTree tree) {
		this.panel = panel;
		this.tree = tree;
	}

	public void mouseClicked(MouseEvent me) {
		boolean leftDoubleClick = me.getClickCount() == 2
				&& me.getButton() == MouseEvent.BUTTON1;
		boolean rightClick = me.getClickCount() == 1
				&& (me.getButton() == MouseEvent.BUTTON2 || me.getButton() == MouseEvent.BUTTON3);
		TreePath path = getPath(me);

		if (path != null) {
			if (leftDoubleClick) {
				panel.doubleClick(path);
			} else if (rightClick) {
				panel.rightClick(path, me.getX(), me.getY());
			}
		}
	}

	private TreePath getPath(MouseEvent me) {
		if (me.getSource().equals(tree)) {
			TreePath path = tree
					.getClosestPathForLocation(me.getX(), me.getY());

			if (path != null) {
				Rectangle rect = tree.getPathBounds(path);

				if (rect != null && rect.contains(me.getX(), me.getY())) {
					return path;
				}
			}
		}
		return null;
	}
}

class NavTree extends JTree {

	private static final long serialVersionUID = 2049341022095778930L;

	public NavTree(DefaultTreeModel model) {
		super(model);
	}

	@Override
	public boolean isPathEditable(TreePath path) {
		return path != null && path.getLastPathComponent() != null
				&& path.getLastPathComponent() instanceof NavTreeNode
				&& ((NavTreeNode) path.getLastPathComponent()).isFrame();
	}
}

class NavTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -2103269063927748330L;

	private enum Kind {
		FRAME, PO, ROOT
	};

	private Kind kind;

	public NavTreeNode(JInternalFrame frame) {
		super(frame.getTitle());
		kind = Kind.FRAME;
	}

	public NavTreeNode(ProvidedObject object) {
		super(object.getName());
		kind = Kind.PO;
	}

	public NavTreeNode(String name) {
		super(name);
		kind = Kind.ROOT;
	}

	public boolean isFrame() {
		return kind == Kind.FRAME;
	}

	public boolean isProvidedObject() {
		return kind == Kind.PO;
	}
}

class NavigationCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1959614436228423297L;

	private JLabel label;
	private Color selectedBackground;
	private Color nonSelectedBackground;
	private Color selectedForeground;
	private Color nonSelectedForeground;
	private Icon leafIcon;
	private Icon nonLeafIcon;
	private Font leafFont;
	private Font nonLeafFont;

	public NavigationCellRenderer() {
		label = new JLabel();
		selectedBackground = new Color(0x17, 0x31, 0x56);
		nonSelectedBackground = new Color(0, 0, 0, 0); // transparent
		selectedForeground = new Color(0xC9, 0xC9, 0xC9);
		nonSelectedForeground = Color.black;
		leafIcon = Utils.getStandardIcon("provided_object.png",
				"text/AlignLeft16");
		nonLeafIcon = Utils.getStandardIcon("spreadsheet16.gif",
				"general/Open16");
		leafFont = label.getFont().deriveFont(Font.PLAIN);
		nonLeafFont = label.getFont().deriveFont(Font.BOLD);

		setLeafIcon(leafIcon);
		setOpenIcon(nonLeafIcon);
		setClosedIcon(nonLeafIcon);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		boolean isFrame = value != null && value instanceof NavTreeNode
				&& ((NavTreeNode) value).isFrame();

		label.setBackground(selected ? selectedBackground
				: nonSelectedBackground);
		label.setForeground(selected ? selectedForeground
				: nonSelectedForeground);
		label.setOpaque(selected);
		label.setIcon(isFrame ? nonLeafIcon : leafIcon);
		label.setText(value.toString());
		label.setFont(isFrame ? nonLeafFont : leafFont);
		return label;
	}
}

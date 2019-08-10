package org.processmining.analysis.dws;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

/**
 * Visualizes the results of DWS analysis plugin.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

/*
 * La classe implementa Provider, che � il meccanismo utilizzato per comunicare
 * al framework il risultato dell'algoritmo, mediante il metodo
 * getProvidedObjects
 */

public class DWSOutputGUI extends JPanel implements Provider {

	private JSplitPane jSplitPane = null;
	private JPanel jPanel = null;
	private JScrollPane jScrollPane = null;
	private JTree jTree = null;
	private JComponent jComponentModel = null; // pannello su cui si disegna il
	// modello
	private JPanel jPanel1 = null;
	private JButton jButtonSplit = null;
	private JList jList = null;
	private JScrollPane jScrollPane1 = null;
	private JScrollPane jScrollPaneDestra = null;
	private Cluster selectedCluster = null;
	private Cluster root; // � l'unico dato che passiamo al pannello

	/**
	 * The plugin provides a set of logs associated with each identified cluster
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] p = null;
		ArrayList a = new ArrayList();
		if (selectedCluster != null) {
			if (selectedCluster.getHeuristicsNet() != null) {
				a.add(new ProvidedObject("Heuristics net with "
						+ selectedCluster.getName(), new Object[] {
						selectedCluster.getHeuristicsNet(),
						selectedCluster.getLog() }));
				a.add(new ProvidedObject("Heuristics net with whole log",
						new Object[] { selectedCluster.getHeuristicsNet(),
								root.getLog() }));
			}
		}

		/*
		 * if (root.getChildren().size()>0){ for (int
		 * i=0;i<root.getChildren().size();i++){ Cluster
		 * c=(Cluster)root.getChildren().get(i); LogReader logc=c.getLog();
		 * a.add(new ProvidedObject(c.getName(), new Object[] { logc })); } }
		 */
		if (selectedCluster != null
				&& selectedCluster.getHeuristicsNet() == null) {
			Cluster c = selectedCluster;
			LogReader logc = c.getLog();
			a.add(new ProvidedObject(c.getName(), new Object[] { logc }));
		}
		p = new ProvidedObject[a.size()];
		for (int i = 0; i < a.size(); i++) {
			p[i] = (ProvidedObject) a.get(i);
		}
		return p;
	}

	/**
	 * Default constructors.
	 * 
	 * @param root
	 *            The root of the hierarchy.
	 */
	public DWSOutputGUI(Cluster root) {
		super();
		this.root = root;
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(root);
		createNodes(top);
		// Create a tree that allows one selection at a time.
		jTree = new JTree(top);
		jTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Listen for when the selection changes.
		jTree.addTreeSelectionListener(new TreeSelectionListener() {
			// ridefinire i metodi che gestiscono gli eventi
			public void valueChanged(TreeSelectionEvent evt) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree
						.getLastSelectedPathComponent();
				if (node == null) {
					return;
				}
				selectedCluster = (Cluster) node.getUserObject();
				if (selectedCluster.getHeuristicsNet() != null) {
					jComponentModel = selectedCluster.getHeuristicsNet()
							.getGrappaVisualization();
				} else {
					jComponentModel = new JPanel();
					jComponentModel.setBackground(Color.WHITE);
					jComponentModel.setLayout(new BorderLayout());
					jComponentModel
							.add(
									new JLabel(
											"          No heuristics net available for the selected cluster. "),
									"Center");
				}
				// jSplitPane.setRightComponent(getJComponentModel());
				jList = new JList(selectedCluster.getFeatures().toArray());
				jScrollPane1.setViewportView(getJList());
				jScrollPaneDestra.setViewportView(jComponentModel);
				jSplitPane.setDividerLocation(100);
			}
		});
		initialize();
	}

	/*
	 * Costruisce il jTree che viene visualizzato percorrend in modo ricorsivo
	 * la gerarchia.
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		Object nodo = top.getUserObject();
		if (nodo == null) {
			return;
		}
		Cluster nodoInfo = (Cluster) nodo;
		for (int i = 0; i < nodoInfo.getChildren().size(); i++) {
			if (nodoInfo.getChildren().get(i) == null) {
				continue;
			}
			DefaultMutableTreeNode next = new DefaultMutableTreeNode(
					(Cluster) nodoInfo.getChildren().get(i));
			top.add(next);
			createNodes(next);
		}
	}

	private void initialize() {
		this.setLayout(new CardLayout());
		this.setSize(570, 424);
		this.add(getJSplitPane(), getJSplitPane().getName());
	}

	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setName("jSplitPane");
			jSplitPane.setRightComponent(getJScrollPaneDestra());
			jSplitPane.setLeftComponent(getJPanel());
			jSplitPane.setDividerLocation(100);
		}
		return jSplitPane;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.Y_AXIS));
			jPanel.add(getJScrollPane(), null);
			jPanel.add(getJPanel1(), null);
		}
		return jPanel;
	}

	private JScrollPane getJScrollPaneDestra() {
		if (jScrollPaneDestra == null) {
			jScrollPaneDestra = new JScrollPane();
			jScrollPaneDestra.setViewportView(getJComponentModel());
		}
		return jScrollPaneDestra;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setName("jScrollPane");
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}

	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree();
		}
		return jTree;
	}

	private JComponent getJComponentModel() {
		if (jComponentModel == null) {
			jComponentModel = new JPanel();
		}
		return jComponentModel;
	}

	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new CardLayout());
			jPanel1.setName("jPanel1");
			jPanel1.add(getJScrollPane1(), getJScrollPane1().getName());
		}
		return jPanel1;
	}

	private JList getJList() {
		if (jList == null) {
			jList = new JList();
		}
		return jList;
	}

	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setName("jScrollPane1");
			jScrollPane1.setViewportView(getJList());
		}
		return jScrollPane1;
	}

} // @jve:decl-index=0:visual-constraint="10,10"

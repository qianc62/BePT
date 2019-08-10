/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cern.colt.list.IntArrayList;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class ModelHierarchy {

	/**
	 * Object to store the last object added to the hierarchy.
	 */
	private Object lastAddedObject = null;

	private final static int WIDTH = 150;

	private IntArrayList parents = new IntArrayList();
	private ArrayList children = new ArrayList();
	private ArrayList objects = new ArrayList();
	private ArrayList jtreeNodes = new ArrayList();
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode("Models");
	private DefaultTreeModel treeModel = new DefaultTreeModel(top);
	private JTree tree = new JTree(treeModel);

	public ModelHierarchy() {
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Object o = getSelectedNode();
				if ((o != null) && (objects.indexOf(o) != -1)) {
					selectionChanged(o);
				}
			}
		});
		// tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setEditable(false);
	}

	/**
	 * Add an object to the hierarchy of objects.
	 * 
	 * @param child
	 *            Object to add as a child
	 * @param label
	 *            String representing the label of the object in the tree
	 * @return Object That represents the parent to which the child is added
	 *         (i.e. parent).
	 */
	public Object addHierarchyObject(Object child, String label) {
		return addHierarchyObject(child, lastAddedObject, label);
	}

	/**
	 * Add an object to the hierarchy of objects.
	 * 
	 * @param child
	 *            Object to add as a child
	 * @param parent
	 *            Object that is the required parent of the child
	 * @param label
	 *            String representing the label of the object in the tree
	 * @return Object That represents the parent to which the child is added
	 *         (i.e. parent).
	 */
	public Object addHierarchyObject(Object child, Object parent,
			final String label) {
		int parentLoc = objects.indexOf(parent);
		lastAddedObject = child;

		// add the child to the data structure
		objects.add(child);
		DefaultMutableTreeNode n = new DefaultMutableTreeNode() {
			public String toString() {
				return label;
			}
		};
		n.setUserObject(child);
		jtreeNodes.add(n);
		int childLoc = objects.size() - 1;

		// add the relation between child and parent
		parents.add(parentLoc);
		children.add(new IntArrayList());
		if (parentLoc > -1) {
			// The parent exists
			IntArrayList parentsChildren = (IntArrayList) children
					.get(parentLoc);
			DefaultMutableTreeNode pn = (DefaultMutableTreeNode) jtreeNodes
					.get(parentLoc);
			pn.add(n);
			// Register the child with the parent
			parentsChildren.add(childLoc);
			return objects.get(parentLoc);
		} else {
			// the parent does not exist
			top.add(n);
			return top;
		}

	}

	/**
	 * This method returns a collection of objects representing the children of
	 * the given parent object, or null if the parent object does not exist.
	 * 
	 * @return Collection containing the children of parent
	 * @param parent
	 *            Object
	 */
	public Collection getChildren(Object parent) {
		int parentLoc = objects.indexOf(parent);
		if (parentLoc == -1) {
			return null;
		}
		IntArrayList l = ((IntArrayList) children.get(parentLoc));
		ArrayList childs = new ArrayList();
		for (int i = 0; i < l.size(); i++) {
			childs.add(objects.get(l.get(i)));
		}
		return childs;
	}

	/**
	 * 
	 * @return Object representing the parent of the child
	 * @param child
	 *            Object
	 */
	public Object getParent(Object child) {
		int childLoc = objects.indexOf(child);
		if (childLoc == -1) {
			return null;
		}
		int parentLoc = parents.get(childLoc);
		if (parentLoc == -1) {
			return null;
		}
		return objects.get(parentLoc);
	}

	/**
	 * This method returns a collection of the root elements of the hierarchy.
	 * 
	 * @return Collection storing the roots of the hierarchy.
	 */
	public Collection getRoots() {
		ArrayList roots = new ArrayList();
		for (int i = 0; i < objects.size(); i++) {
			if (parents.get(i) == -1) {
				roots.add(objects.get(i));
			}
		}
		return roots;
	}

	/**
	 * Returns the visualization of the stored hierarchy
	 * 
	 * @return A component with the tree visialization of this object
	 */
	public JComponent getTreeVisualization() {
		TreePath path = new TreePath(((DefaultMutableTreeNode) jtreeNodes
				.get(0)).getPath());
		tree.setSelectionPath(path);
		if (jtreeNodes.size() > 1) {
			JScrollPane sp = new JScrollPane(tree);
			sp.setPreferredSize(new Dimension(WIDTH, tree.getHeight()));
			sp.setSize(sp.getPreferredSize());
			tree.scrollPathToVisible(new TreePath(top));
			// Message.add("" + path, Message.DEBUG);
			tree.expandPath(path);

			return sp;
		} else {
			// 0 or 1 graph in the hiearchy, so no hierarchy to be seen...
			return null;
		}
	}

	/**
	 * Return the user object of the selected node in the JTree visualisation.
	 * 
	 * @return Object
	 */
	public Object getSelectedNode() {
		if (objects.size() == 1) {
			return objects.get(0);
		}

		DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (n == null) {
			return null;
		}

		return n.getUserObject();
	}

	/**
	 * Selects the node in the tree that points to the given object.
	 * 
	 * @param selectObject
	 *            the object to select.
	 */
	public void setSelectedNode(Object selectObject) {
		Iterator it = jtreeNodes.iterator();
		while (it.hasNext()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();
			if (node.getUserObject() == selectObject) {
				TreePath path = new TreePath(node.getPath());
				tree.setSelectionPath(path);
				tree.expandPath(path);
				tree.scrollPathToVisible(path);
				return;
			}
		}
	}

	public boolean copyFromHierarchy(ModelHierarchy org) {
		if (!org.getClass().equals(this.getClass())) {
			return false;
		}
		if (this.getRoots().iterator().hasNext()) {
			// there are roots (none empty hierarchy)
			return false;
		}

		Iterator it = org.objects.iterator();
		while (it.hasNext()) {
			Object orgObject = it.next();
			int orgIndex = org.objects.indexOf(orgObject);
			String orgLabel = org.jtreeNodes.get(orgIndex).toString();
			addHierarchyObject(orgObject, org.getParent(orgObject), orgLabel);
		}
		return true;
	}

	/**
	 * This method is called each time the user changes the selection in the
	 * list. Note that it is only called after the constructor of a subclass has
	 * set <code>initialized</code> to true
	 * 
	 * @param selectedObject
	 *            The object that was selected.
	 */
	protected abstract void selectionChanged(Object selectedObject);

	/**
	 * This method returns a collection of objects representing the nodes in
	 * this hierarchy.
	 * 
	 * @return Collection containing the nodes in this hierarchy
	 */
	public Collection getAllObjects() {
		return objects;
	}

	public void destroy() {
		children.clear();
		children = null;
		jtreeNodes.clear();
		jtreeNodes = null;
		lastAddedObject = null;
		objects.clear();
		objects = null;
		parents.clear();
		parents = null;
		top = null;
		tree.removeAll();
		tree = null;
		treeModel = null;
	}
}

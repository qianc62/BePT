/***********************************************************
 *      This software is part of the graphviz package      *
 *                http://www.graphviz.org/                 *
 *                                                         *
 *            Copyright (c) 1994-2004 AT&T Corp.           *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *                      by AT&T Corp.                      *
 *                                                         *
 *        Information and Software Systems Research        *
 *              AT&T Research, Florham Park NJ             *
 **********************************************************/

package att.grappa;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * This class describes a subgraph, which can consist of nodes, edges and other
 * subgraphs. Note: The topmost or root subgraph is the entire graph (the Graph
 * object), which is an extension of this class.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 * @see Graph
 */
public class Subgraph extends Element implements Comparator {
	/**
	 * Default graph name prefix used by setName().
	 * 
	 * @see Subgraph#setName()
	 */
	public final static String defaultNamePrefix = "G";

	// node, edge and graph dictionaries for this subgraph
	private Hashtable nodedict = null;
	private Hashtable edgedict = null;
	private Hashtable graphdict = null;

	// indicators for dislaying element labels when drawing
	private boolean nodeLabels = true;
	private boolean edgeLabels = true;
	private boolean subgLabels = true;

	// default node attributes
	private Hashtable nodeAttributes = null;

	// default edge attributes
	private Hashtable edgeAttributes = null;

	// for cluster subgraphs
	private boolean cluster = false;

	/**
	 * Reference to the current selection (or vector of selections). Normally
	 * set and used by a GrappaAdapter.
	 */
	public Object currentSelection = null;

	/**
	 * This constructor is needed by the Graph constructor
	 */
	Subgraph() {
		// super();
		cluster = true; // the root is a cluster subgraph

		subgraphAttrsOfInterest();
	}

	/**
	 * Use this constructor when creating a subgraph within a subgraph.
	 * 
	 * @param subg
	 *            the parent subgraph.
	 * @param name
	 *            the name of this subgraph.
	 */
	public Subgraph(Subgraph subg, String name) {
		super(SUBGRAPH, subg);
		setName(name);

		Enumeration enu = subg.getNodeAttributePairs();
		while (enu.hasMoreElements()) {
			setNodeAttribute((Attribute) enu.nextElement());
		}
		enu = subg.getEdgeAttributePairs();
		while (enu.hasMoreElements()) {
			setEdgeAttribute((Attribute) enu.nextElement());
		}
		enu = subg.getLocalAttributePairs();
		while (enu.hasMoreElements()) {
			setAttribute((Attribute) enu.nextElement());
		}

		subgraphAttrsOfInterest();
	}

	/**
	 * Use this constructor when creating a subgraph within a subgraph with an
	 * automatically generated name.
	 * 
	 * @param subg
	 *            the parent subgraph.
	 * @see Subgraph#setName()
	 */
	public Subgraph(Subgraph subg) {
		this(subg, (String) (null));
	}

	// a listing of the attributes of interest for Subgraphs
	private void subgraphAttrsOfInterest() {
		// attrOfInterest(BBOX_ATTR);
		attrOfInterest(MINBOX_ATTR);
		attrOfInterest(MINSIZE_ATTR);
		attrOfInterest(LABEL_ATTR);
		attrOfInterest(LP_ATTR);
		attrOfInterest(STYLE_ATTR);
	}

	/**
	 * Check if this element is a subgraph. Useful for testing the subclass type
	 * of a Element object.
	 * 
	 * @return true if this object is a Subgraph.
	 */
	public boolean isSubgraph() {
		return (true);
	}

	/**
	 * Get the type of this element. Useful for distinguishing Element objects.
	 * 
	 * @return the class variable constant SUBGRAPH.
	 * @see GrappaConstants#SUBGRAPH
	 */
	public int getType() {
		return (SUBGRAPH);
	}

	/**
	 * Generates and sets the name for this subgraph. The generated name is the
	 * concatenation of the Subgraph.defaultNamePrefix with the numeric id of
	 * this subgraph Instance. Implements the abstract Element method.
	 * 
	 * @see Element#getId()
	 */
	void setName() {
		String oldName = name;

		while (true) {
			name = Subgraph.defaultNamePrefix + getId() + "_"
					+ System.currentTimeMillis();
			if (getGraph().findSubgraphByName(name) == null) {
				break;
			}
		}

		// update subgraph graph dictionary
		if (getSubgraph() != null) {
			if (oldName != null) {
				getSubgraph().removeSubgraph(oldName);
			}
			getSubgraph().addSubgraph(this);
		}

		canonName = null;
	}

	/**
	 * Sets the subgraph name to a copy of the supplied argument. When the
	 * argument is null, setName() is called. When the name is not unique or
	 * when the name has the same format as that generated by setName(), a
	 * IllegalArgumentException is thrown.
	 * 
	 * @param newName
	 *            the new name for the subgraph.
	 * @see Subgraph#setName()
	 */
	public void setName(String newName) throws IllegalArgumentException {
		if (newName == null) {
			setName();
			return;
		}

		String oldName = name;

		// test if the new name is the same as the old name (if any)
		if (oldName != null && oldName.equals(newName)) {
			return;
		}

		// is name unique?
		if (getGraph().findSubgraphByName(newName) != null) {
			throw new IllegalArgumentException("graph name (" + newName
					+ ") is not unique");
		}
		name = newName;

		if (name.startsWith("cluster")) {
			cluster = true;
		}

		// update subgraph graph dictionary
		if (getSubgraph() != null) {
			if (oldName != null) {
				getSubgraph().removeSubgraph(oldName);
			}
			getSubgraph().addSubgraph(this);
		}

		canonName = null;
	}

	/**
	 * Check if the subgraph is a cluster subgraph.
	 * 
	 * @return true, if the graph is a cluster subgraph.
	 */
	public boolean isCluster() {
		return cluster;
	}

	/**
	 * Check if the subgraph is the root of the graph.
	 * 
	 * @return true, if the graph is the root of the graph.
	 */
	public boolean isRoot() {
		return (this == (Subgraph) getGraph());
	}

	/**
	 * Gets the subgraph-specific default attribute for the named node
	 * attribute.
	 * 
	 * @param key
	 *            the name of the node attribute pair to be retrieved.
	 * @return the requested attribute pair or null if not found.
	 */
	public Attribute getNodeAttribute(String key) {
		if (nodeAttributes == null) {
			return (null);
		}
		return ((Attribute) (nodeAttributes.get(key)));
	}

	/**
	 * Gets the subgraph-specific default value for the named node attribute.
	 * 
	 * @param key
	 *            the name of the node attribute pair to be retrieved.
	 * @return the requested attribute value or null if not found.
	 */
	public Object getNodeAttributeValue(String key) {
		Attribute attr;
		if (nodeAttributes == null) {
			return (null);
		}
		if ((attr = (Attribute) (nodeAttributes.get(key))) == null) {
			return (null);
		}
		return (attr.getValue());
	}

	/**
	 * Gets an enumeration of the subgraph-specific node attribute keys
	 * 
	 * @return an enumeration of String objects.
	 */
	public Enumeration getNodeAttributeKeys() {
		if (nodeAttributes == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return (nodeAttributes.keys());
	}

	/**
	 * Gets an enumeration of the subgraph-specific node attributes
	 * 
	 * @return an enumeration of Attribute objects.
	 */
	public Enumeration getNodeAttributePairs() {
		if (nodeAttributes == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return (nodeAttributes.elements());
	}

	/**
	 * Sets the subgraph-specific default for the specified node attribute. If
	 * the attribute is not from the parent subgraph, then
	 * setNodeAttribute(attr.getName(), attr.getValue()) is called.
	 * 
	 * @param attr
	 *            the node Attribute object to set as a default.
	 * @return the Attribute object previously stored for this attribute, if
	 *         any.
	 * @see Subgraph#setNodeAttribute(java.lang.String, java.lang.String)
	 */
	public Object setNodeAttribute(Attribute attr) {
		if (attr == null) {
			return null;
		}
		if (nodeAttributes == null) {
			nodeAttributes = new Hashtable();
		}
		// check to see if attr is being passed down the subgraph chain
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getNodeAttribute(attr
				.getName());
		if (attr != prntAttr) {
			// it's not, so use the other method;
			// use getStringValue to make sure value is treated properly
			// when converted to an Object
			return setNodeAttribute(attr.getName(), attr.getStringValue());
		}
		Object oldValue = null;
		Attribute newAttr = null;
		Attribute crntAttr = getNodeAttribute(attr.getName());
		if (attr == crntAttr) {
			return attr.getValue();
		}
		if (crntAttr == null) {
			if (attr.getValue() == null) {
				return null;
			}
			nodeAttributes.put(attr.getName(), crntAttr = attr);
			// System.err.println("Adding passthru1 node attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			// it's a pass down, so no need to set observers
		} else {
			oldValue = crntAttr.getValue();
			crntAttr.setChanged(); // so notifyObservers is sure to be called
			// it's a pass down, so pass it down
			nodeAttributes.put(attr.getName(), attr);
			// System.err.println("Adding passthru2 node attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			// this is why we need notifyObservers called
			newAttr = attr;
		}
		// this should only be possible when "else" above has occurred
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Object[] { newAttr,
					new Long(System.currentTimeMillis()) });
		}
		return oldValue;
	}

	/**
	 * Sets the subgraph-specific default using the specified name/value pair. A
	 * new attribute will be created if needed.
	 * 
	 * @param name
	 *            the node attribute name
	 * @param value
	 *            the node attribute value
	 * @return the Attribute object previously stored for this attribute, if
	 *         any.
	 */
	public Object setNodeAttribute(String name, Object value) {
		if (nodeAttributes == null) {
			nodeAttributes = new Hashtable();
		}
		if (name == null) {
			throw new IllegalArgumentException(
					"cannot set an attribute using a null name");
		}
		// check to see if this name value is the same as the parent default
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getNodeAttribute(name);
		// if(prntAttr != null && value != null ) {
		// System.err.println("check new node attr ("+name+","+value+") against ("+prntAttr.getName()+","+prntAttr.getValue()+")");
		// if(name.equals(prntAttr.getName()) &&
		// value.equals(prntAttr.getValue())) {
		// it is, so call other form
		// System.err.println("set node attr to same as default ("+name+","+value+")");
		// return setNodeAttribute(prntAttr);
		// }
		// }
		Object oldValue = null;
		Attribute crntAttr = getNodeAttribute(name);
		if (crntAttr == null || crntAttr == prntAttr) {
			if (value == null) {
				return null;
			}
			nodeAttributes.put(name, (crntAttr = new Attribute(NODE, name,
					value)));
			// TODO: scan subnodes to see if this attr is of interest and then
			// add it
			// to observer list, but for now leave it
			//
			// System.err.println("adding new node attr("+name+","+value+") to "+getName());
			/*
			 * just concerned with subgraphs that share the same default (or
			 * null) and nodes that do not have a local attribute
			 */
		} else {
			oldValue = crntAttr.getValue();
			if (value == null) {
				if (prntAttr == null) {
					removeNodeAttribute(name);
					return oldValue;
				} else {
					return setNodeAttribute(prntAttr);
				}
			} else {
				crntAttr.setValue(value);
				// System.err.println("changing node attr("+name+","+value+") in "+getName());
			}
		}
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Long(System.currentTimeMillis()));
		}
		return oldValue;
	}

	/*
	 * Remove named default node attribute (assumes there is no default in the
	 * subgraph chain).
	 * 
	 * @param name the name of the attribute to remove
	 */
	private void removeNodeAttribute(String name) {
		if (name == null || nodeAttributes == null) {
			return;
		}
		// System.err.println("Remove '" + name + "' from " + getName());
		Attribute attr = (Attribute) nodeAttributes.remove(name);
		if (attr == null) {
			return;
		}
		attr.setValue("");
		if (attr.hasChanged()) {
			attr.notifyObservers(new Long(System.currentTimeMillis()));
		}
		attr.deleteObservers();
	}

	/**
	 * Sets the subgraph-specific default for the specified edge attribute. If
	 * the attribute is not from the parent subgraph, then
	 * setEdgeAttribute(attr.getName(), attr.getValue()) is called.
	 * 
	 * @param attr
	 *            the edge attribute pair to set.
	 * @return the attribute pair previously stored for this attribute.
	 * @see Subgraph#setEdgeAttribute(java.lang.String, java.lang.String)
	 */
	public Object setEdgeAttribute(Attribute attr) {
		if (attr == null) {
			return null;
		}
		if (edgeAttributes == null) {
			edgeAttributes = new Hashtable();
		}
		// check to see if attr is being passed down the subgraph chain
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getEdgeAttribute(attr
				.getName());
		if (attr != prntAttr) {
			// it's not, so use the other method;
			// use getStringValue to make sure value is treated properly
			// when converted to an Object
			return setEdgeAttribute(attr.getName(), attr.getStringValue());
		}
		Object oldValue = null;
		Attribute newAttr = null;
		Attribute crntAttr = getEdgeAttribute(attr.getName());
		if (attr == crntAttr) {
			return attr.getValue();
		}
		if (crntAttr == null) {
			if (attr.getValue() == null) {
				return null;
			}
			edgeAttributes.put(attr.getName(), crntAttr = attr);
			// System.err.println("Adding passthru1 edge attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			// it's a pass down, so no need to set observers
		} else {
			oldValue = crntAttr.getValue();
			crntAttr.setChanged(); // so notifyObservers is sure to be called
			// it's a pass down, so pass it down
			edgeAttributes.put(attr.getName(), attr);
			// System.err.println("Adding passthru2 edge attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			newAttr = attr;
		}
		// this should only be possible when "else" above has occurred
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Object[] { newAttr,
					new Long(System.currentTimeMillis()) });
		}
		return oldValue;
	}

	/**
	 * Sets the subgraph-specific default using the specified name/value pair. A
	 * new attribute will be created if needed.
	 * 
	 * @param name
	 *            the edge attribute name
	 * @param value
	 *            the edge attribute value
	 * @return the attribute pair previously stored for this attribute.
	 */
	public Object setEdgeAttribute(String name, Object value) {
		if (edgeAttributes == null) {
			edgeAttributes = new Hashtable();
		}
		if (name == null) {
			throw new IllegalArgumentException(
					"cannot set an attribute using a null name");
		}
		// check to see if this name value is the same as the parent default
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getEdgeAttribute(name);
		// if(prntAttr != null && value != null ) {
		// if(name.equals(prntAttr.getName()) &&
		// value.equals(prntAttr.getValue())) {
		// it is, so call other form
		// return setEdgeAttribute(prntAttr);
		// }
		// }
		Object oldValue = null;
		Attribute crntAttr = getEdgeAttribute(name);
		if (crntAttr == null || crntAttr == prntAttr) {
			if (value == null) {
				return null;
			}
			edgeAttributes.put(name, (crntAttr = new Attribute(EDGE, name,
					value)));
			// System.err.println("adding new edge attr("+name+","+value+") to "+getName());
			/*
			 * just concerned with subgraphs that share the same default (or
			 * null) and edges that do not have a local attribute
			 */
		} else {
			oldValue = crntAttr.getValue();
			if (value == null) {
				if (prntAttr == null) {
					removeEdgeAttribute(name);
					return oldValue;
				} else {
					return setEdgeAttribute(prntAttr);
				}
			} else {
				crntAttr.setValue(value);
				// System.err.println("changing edge attr("+name+","+value+") in "+getName());
			}
		}
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Long(System.currentTimeMillis()));
		}
		return oldValue;
	}

	/*
	 * Remove named default edge attribute (assumes there is no default in the
	 * subgraph chain).
	 * 
	 * @param name the name of the attribute to remove
	 */
	private void removeEdgeAttribute(String name) {
		if (name == null || edgeAttributes == null) {
			return;
		}
		Attribute attr = (Attribute) edgeAttributes.remove(name);
		if (attr == null) {
			return;
		}
		attr.setValue("");
		if (attr.hasChanged()) {
			attr.notifyObservers(new Long(System.currentTimeMillis()));
		}
		attr.deleteObservers();
	}

	/**
	 * Sets the subgraph-specific default for the specified graph attribute. If
	 * the attribute is not from the parent subgraph, then
	 * setAttribute(attr.getName(), attr.getValue()) is called. Overrides
	 * Element method.
	 * 
	 * @param attr
	 *            the graph attribute pair to set.
	 * @return the attribute pair previously stored for this attribute.
	 * @see Subgraph#setAttribute(java.lang.String, java.lang.String)
	 */
	public Object setAttribute(Attribute attr) {
		if (attr == null) {
			return null;
		}
		if (attributes == null) {
			attributes = new Hashtable();
		}
		// check to see if attr is being passed down the subgraph chain
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getLocalAttribute(attr
				.getName());
		if (attr != prntAttr) {
			// it's not, so use the other method;
			// use getStringValue to make sure value is treated properly
			// when converted to an Object
			return setAttribute(attr.getName(), attr.getStringValue());
		}
		Object oldValue = null;
		Attribute newAttr = null;
		Attribute crntAttr = getLocalAttribute(attr.getName());
		if (attr == crntAttr) {
			return attr.getValue();
		}
		if (crntAttr == null) {
			if (attr.getValue() == null) {
				return null;
			}
			attributes.put(attr.getName(), crntAttr = attr);
			// System.err.println("Adding passthru1 graph attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			// it's a pass down, so no need to set observers
		} else {
			oldValue = crntAttr.getValue();
			crntAttr.setChanged(); // so notifyObservers is sure to be called
			// it's a pass down, so pass it down
			attributes.put(attr.getName(), attr);
			// System.err.println("Adding passthru2 graph attr("+attr.getName()+","+attr.getValue()+") to "+getName());
			// this is why we need notifyObservers called
			newAttr = attr;
		}
		// this should only be possible when "else" above has occurred
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Object[] { newAttr,
					new Long(System.currentTimeMillis()) });
		}
		return oldValue;
	}

	/**
	 * Sets the subgraph-specific default using the specified name/value pair. A
	 * new attribute will be created if needed. Overrides Element method.
	 * 
	 * @param name
	 *            the graph attribute name
	 * @param value
	 *            the graph attribute value
	 * @return the attribute pair previously stored for this attribute.
	 */
	public Object setAttribute(String name, Object value) {
		if (attributes == null) {
			attributes = new Hashtable();
		}
		if (name == null) {
			throw new IllegalArgumentException(
					"cannot set an attribute using a null name");
		}
		// check to see if this name value is the same as the parent default
		Subgraph sg = getSubgraph();
		Attribute prntAttr = (sg == null) ? null : sg.getLocalAttribute(name);
		// if(prntAttr != null && value != null ) {
		// if(name.equals(prntAttr.getName()) &&
		// value.equals(prntAttr.getValue())) {
		// it is, so call other form
		// return setAttribute(prntAttr);
		// }
		// }
		Object oldValue = null;
		Attribute crntAttr = getLocalAttribute(name);
		if (crntAttr == null || crntAttr == prntAttr) {
			if (value == null) {
				return null;
			} else if (value instanceof String
					&& ((String) value).trim().length() == 0
					&& Attribute.attributeType(getType(), name) != STRING_TYPE) {
				return null;
			}
			attributes.put(name, (crntAttr = new Attribute(SUBGRAPH, name,
					value)));
			if (grappaNexus != null && isOfInterest(name)) {
				crntAttr.addObserver(grappaNexus);
			}

			// System.err.println("adding new graph attr("+name+","+value+") to "+getName());
		} else {
			oldValue = crntAttr.getValue();
			if (value == null) {
				if (prntAttr == null) {
					// System.err.println("removing graph attr("+name+","+value+") in "+getName());
					super.setAttribute(name, null);
					return oldValue;
				} else {
					// System.err.println("defaulting graph attr("+name+","+value+") in "+getName());
					return setAttribute(prntAttr);
				}
			} else if (value instanceof String
					&& ((String) value).trim().length() == 0
					&& Attribute.attributeType(getType(), name) != STRING_TYPE) {
				if (prntAttr == null) {
					// System.err.println("removing graph attr("+name+","+value+") in "+getName());
					super.setAttribute(name, null);
					return oldValue;
				} else {
					// System.err.println("defaulting graph attr("+name+","+value+") in "+getName());
					return setAttribute(prntAttr);
				}
			} else {
				crntAttr.setValue(value);
				// System.err.println("changing graph attr("+name+","+value+") in "+getName());
			}
		}
		if (crntAttr.hasChanged()) {
			crntAttr.notifyObservers(new Long(System.currentTimeMillis()));
		}
		return oldValue;
	}

	/**
	 * Gets the subgraph-specific default attribute for the named edge
	 * attribute.
	 * 
	 * @param key
	 *            the name of the edge attribute pair to be retrieved.
	 * @return the requested attribute pair or null if not found.
	 */
	public Attribute getEdgeAttribute(String key) {
		if (edgeAttributes == null) {
			return (null);
		}
		return ((Attribute) (edgeAttributes.get(key)));
	}

	/**
	 * Gets the subgraph-specific default value for the named edge attribute.
	 * 
	 * @param key
	 *            the name of the edge attribute pair to be retrieved.
	 * @return the requested attribute value or null if not found.
	 */
	public Object getEdgeAttributeValue(String key) {
		Attribute attr;
		if (edgeAttributes == null) {
			return (null);
		}
		if ((attr = (Attribute) (edgeAttributes.get(key))) == null) {
			return (null);
		}
		return (attr.getValue());
	}

	/**
	 * Gets an enumeration of the subgraph-specific edge attribute keys
	 * 
	 * @return an enumeration of String objects.
	 */
	public Enumeration getEdgeAttributeKeys() {
		if (edgeAttributes == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return (edgeAttributes.keys());
	}

	/**
	 * Gets an enumeration of the subgraph-specific edge attributes
	 * 
	 * @return an enumeration of Attribute objects.
	 */
	public Enumeration getEdgeAttributePairs() {
		if (edgeAttributes == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return (edgeAttributes.elements());
	}

	/**
	 * Get the bounding box of the subgraph.
	 * 
	 * @return the bounding box of the subgraph.
	 */
	public java.awt.geom.Rectangle2D getBoundingBox() {
		java.awt.geom.Rectangle2D bbox = null;
		if (grappaNexus == null || (bbox = grappaNexus.bbox) == null) {
			if (grappaNexus == null) {
				buildShape();
			}
			bbox = null;
			Element elem = null;
			GraphEnumeration enu = elements();
			while (enu.hasMoreElements()) {
				elem = enu.nextGraphElement();
				if (elem == (Element) this) {
					continue;
				}
				switch (elem.getType()) {
				case NODE:
				case EDGE:
					elem.buildShape();
					if (bbox == null) {
						bbox = elem.grappaNexus.getBounds2D();
					} else {
						bbox.add(elem.grappaNexus.getBounds2D());
					}
					break;
				case SUBGRAPH:
					if (bbox == null) {
						bbox = ((Subgraph) elem).getBoundingBox();
					} else {
						bbox.add(((Subgraph) elem).getBoundingBox());
					}
					break;
				default: // cannot happen
					throw new InternalError("unknown type (" + elem.getType()
							+ ")");
				}
			}
			GrappaSize minSize = (GrappaSize) getAttributeValue(MINSIZE_ATTR);
			if (minSize != null) {
				if (bbox == null) {
					bbox = new java.awt.geom.Rectangle2D.Double(0, 0, minSize
							.getWidth(), minSize.getHeight());
				} else {
					bbox.add(new java.awt.geom.Rectangle2D.Double(bbox
							.getCenterX()
							- (minSize.getWidth() / 2.0), bbox.getCenterY()
							- (minSize.getHeight() / 2.0), minSize.getWidth(),
							minSize.getHeight()));
				}
			}
			GrappaBox minBox = (GrappaBox) getThisAttributeValue(MINBOX_ATTR);
			if (minBox != null) {
				if (bbox == null) {
					bbox = new java.awt.geom.Rectangle2D.Double(minBox.x,
							minBox.y, minBox.width, minBox.height);
				} else {
					bbox.add(new java.awt.geom.Rectangle2D.Double(minBox.x,
							minBox.y, minBox.width, minBox.height));
				}
			}
			minBox = (GrappaBox) getThisAttributeValue(BBOX_ATTR);
			if (minBox != null) {
				if (bbox == null) {
					bbox = new java.awt.geom.Rectangle2D.Double(minBox.x,
							minBox.y, minBox.width, minBox.height);
				} else {
					bbox.add(new java.awt.geom.Rectangle2D.Double(minBox.x,
							minBox.y, minBox.width, minBox.height));
				}
			}
			if (bbox == null) {
				bbox = new java.awt.geom.Rectangle2D.Double();
			}
			// PVDB commented out the next line
			// bbox.add(bbox.getX() + bbox.getWidth() + 1, bbox.getY() +
			// bbox.getHeight() + 1);
			// PVDB added the following two lines to make the bounding box of a
			// subgraph wider
			bbox.add(bbox.getX() + bbox.getWidth() + 11, bbox.getY()
					+ bbox.getHeight() + 11);
			bbox.add(bbox.getX() - 10, bbox.getY() - 10);

			grappaNexus.bbox = bbox;
			if (Grappa.provideBBoxAttribute) {
				setAttribute(BBOX_ATTR, new GrappaBox(bbox));
			}
			grappaNexus.updateShape();
		}
		return ((java.awt.geom.Rectangle2D) (bbox.clone()));
	}

	/**
	 * Removes bounding box information from this subgraph and any contained
	 * subgraphs including the BBOX_ATTR value and then recomputes the bounding
	 * boxes.
	 * 
	 * @return the new bounding box of the subgraph.
	 */
	public java.awt.geom.Rectangle2D resetBoundingBox() {
		Element elem = null;
		GraphEnumeration enu = elements(SUBGRAPH);
		while (enu.hasMoreElements()) {
			elem = enu.nextGraphElement();
			elem.grappaNexus.bbox = null;
			elem.setAttribute(BBOX_ATTR, null);
		}
		return (getBoundingBox());
	}

	/**
	 * Prints an ascii description of each graph element to the supplied stream.
	 * 
	 * @param output
	 *            the OutputStream for writing the graph description.
	 */
	public void printSubgraph(PrintWriter out) {
		Graph graph = getGraph();
		String indent = new String(graph.getIndent());

		if (Grappa.printVisibleOnly && (!visible || grappaNexus.style.invis)) {
			return;
		}

		if (getSubgraph() == null) {
			// this subgraph is the root
			out.println(indent + (graph.isStrict() ? "strict " : "")
					+ (graph.isDirected() ? "digraph" : "graph") + " "
					+ graph.toString() + " {");
		} else if (getName().startsWith(ANONYMOUS_PREFIX)) {
			out.println(indent + "{");
		} else {
			out.println(indent + "subgraph " + this.toString() + " {");
		}

		graph.incrementIndent();

		printDflt(out, SUBGRAPH);
		printDflt(out, NODE);
		printDflt(out, EDGE);

		if (graphdict != null && !graphdict.isEmpty()) {
			Enumeration elems = graphdict.elements();
			while (elems.hasMoreElements()) {
				((Subgraph) (elems.nextElement())).printSubgraph(out);
			}
		}

		if (nodedict != null && !nodedict.isEmpty()) {
			Enumeration elems = nodedict.elements();
			while (elems.hasMoreElements()) {
				((Node) (elems.nextElement())).printNode(out);
			}
		}

		if (edgedict != null && !edgedict.isEmpty()) {
			Enumeration elems = edgedict.elements();
			while (elems.hasMoreElements()) {
				((Edge) (elems.nextElement())).printEdge(out);
			}
		}

		graph.decrementIndent();

		out.println(indent + "}");
	}

	// print the subgraph default elements
	private void printDflt(PrintWriter out, int type) {
		String indent = new String(getGraph().getIndent());
		Hashtable attr = null;
		String label = null;

		switch (type) {
		case SUBGRAPH:
			attr = attributes;
			label = "graph";
			break;
		case NODE:
			attr = nodeAttributes;
			label = "node";
			break;
		case EDGE:
			attr = edgeAttributes;
			label = "edge";
			break;
		}

		if (attr == null || attr.isEmpty()) {
			getGraph().printError(
					"no " + label + " atrtibutes for " + getName());
			return;
		}

		getGraph().incrementIndent();
		printDfltAttr(out, attr, type, indent + label + " [", indent + "];");
		getGraph().decrementIndent();
	}

	// print the subgraph default element attribute values
	private void printDfltAttr(PrintWriter out, Hashtable dfltAttr, int type,
			String prefix, String suffix) {
		String indent = new String(getGraph().getIndent());
		String value;
		String key;
		Attribute attr;
		int nbr = 0;
		Enumeration attrs = dfltAttr.elements();
		Subgraph sg = getSubgraph();
		Hashtable printlist = null;

		if (type == SUBGRAPH && (Grappa.usePrintList || usePrintList)) {
			printlist = (Hashtable) getAttributeValue(PRINTLIST_ATTR);
		}

		while (attrs.hasMoreElements()) {
			attr = (Attribute) (attrs.nextElement());
			if (attr == null) {
				continue;
			}
			key = attr.getName();
			if (printlist != null && printlist.get(key) == null) {
				continue;
			}
			value = attr.getStringValue();
			if (Grappa.elementPrintAllAttributes
					|| Grappa.elementPrintDefaultAttributes
					|| printAllAttributes || printDefaultAttributes
					|| !attr.equalsValue(getParentDefault(type, key))) {
				nbr++;
				if (nbr == 1) {
					out.println(prefix);
					out.print(indent + key + " = " + canonString(value));
				} else {
					out.println(",");
					out.print(indent + key + " = " + canonString(value));
				}
			}
		}
		if (nbr > 0) {
			out.println();
			out.println(suffix);
			out.println();
		}
	}

	/**
	 * Returns the attribute conversion type for the supplied attribute name.
	 * After subgraph specific attribute name/type mappings are checked,
	 * mappings at the element level are checked.
	 * 
	 * @param attrname
	 *            the attribute name
	 * @return the currently associated attribute type
	 */
	public static int attributeType(String attrname) {
		int convtype = -1;
		int hashCode;

		if (attrname != null) {
			hashCode = attrname.hashCode();

			if (hashCode == MARGIN_HASH && attrname.equals(MARGIN_ATTR)) {
				convtype = SIZE_TYPE;
			} else if (hashCode == MCLIMIT_HASH
					&& attrname.equals(MCLIMIT_ATTR)) {
				convtype = DOUBLE_TYPE;
			} else if (hashCode == MINBOX_HASH && attrname.equals(MINBOX_ATTR)) {
				convtype = BOX_TYPE;
			} else if (hashCode == NODESEP_HASH
					&& attrname.equals(NODESEP_ATTR)) {
				convtype = DOUBLE_TYPE;
			} else if (hashCode == MINSIZE_HASH
					&& attrname.equals(MINSIZE_ATTR)) {
				convtype = SIZE_TYPE;
			} else if (hashCode == NODESEP_HASH
					&& attrname.equals(NODESEP_ATTR)) {
				convtype = DOUBLE_TYPE;
			} else if (hashCode == RANKSEP_HASH
					&& attrname.equals(RANKSEP_ATTR)) {
				convtype = DOUBLE_TYPE;
			} else if (hashCode == SIZE_HASH && attrname.equals(SIZE_ATTR)) {
				convtype = SIZE_TYPE;
			} else {
				return (Element.attributeType(attrname));
			}
		}

		return (convtype);
	}

	// get the parent default attribute value for the specified type and key
	private Attribute getParentDefault(int type, String key) {
		Attribute attr = null;
		Subgraph subg = getSubgraph();
		switch (type) {
		case SUBGRAPH:
			while (subg != null && (attr = subg.getLocalAttribute(key)) == null) {
				subg = subg.getSubgraph();
			}
			if (attr == null) {
				attr = getGraph().getGlobalAttribute(SUBGRAPH, key);
			}
			return attr;
		case NODE:
			while (subg != null && (attr = subg.getNodeAttribute(key)) == null) {
				subg = subg.getSubgraph();
			}
			if (attr == null) {
				attr = getGraph().getGlobalAttribute(NODE, key);
			}
			return attr;
		case EDGE:
			while (subg != null && (attr = subg.getEdgeAttribute(key)) == null) {
				subg = subg.getSubgraph();
			}
			if (attr == null) {
				attr = getGraph().getGlobalAttribute(EDGE, key);
			}
			return attr;
		}
		return null;
	}

	/*
	 * Find an Element by name.
	 * 
	 * @param type the type of the element
	 * 
	 * @param name the name of the element
	 * 
	 * @return the found element or null
	 * 
	 * @see Subgraph#findNodeByName(java.lang.String)
	 * 
	 * @see Subgraph#findEdgeByName(java.lang.String)
	 * 
	 * @see Subgraph#findSubgraphByName(java.lang.String)
	 */
	private Element findElementByName(int type, String name) {
		if (name == null) {
			return (null);
		}

		return findElementInSubgraphByName(type, name);
	}

	// used above
	private Element findElementInSubgraphByName(int type, String name) {
		Element elem = null;

		switch (type) {
		case NODE:
			if (nodedict != null) {
				elem = (Element) nodedict.get(name);
			}
			break;
		case EDGE:
			if (edgedict != null) {
				elem = (Element) edgedict.get(name);
			}
			break;
		case SUBGRAPH:
			if (graphdict != null) {
				elem = (Element) graphdict.get(name);
			}
			break;
		}

		if (elem != null || graphdict == null) {
			return elem;
		}

		Enumeration enu = graphdict.elements();
		while (enu.hasMoreElements()) {
			if ((elem = ((Subgraph) enu.nextElement())
					.findElementInSubgraphByName(type, name)) != null) {
				return elem;
			}
		}

		return elem;
	}

	/**
	 * Searches current subgraph and, by recursion, descendent subgraphs for the
	 * node matching the supplied name.
	 * 
	 * @param nodeName
	 *            the name of the node to be found.
	 * @return the Node matching the name or null, if there is no match.
	 */
	public Node findNodeByName(String nodeName) {
		return (Node) findElementByName(NODE, nodeName);
	}

	/**
	 * Searches current subgraph and, by recursion, descendent subgraphs for the
	 * edge matching the supplied name.
	 * 
	 * @param edgeName
	 *            the name of the edge to be found.
	 * @return the Edge matching the name or null, if there is no match.
	 */
	public Edge findEdgeByName(String edgeName) {
		return (Edge) findElementByName(EDGE, edgeName);
	}

	/**
	 * Searches current subgraph and, by recursion, descendent subgraphs for the
	 * subgraph matching the supplied name.
	 * 
	 * @param graphName
	 *            the name of the subgraph to be found.
	 * @return the Subgraph matching the name or null, if there is no match.
	 */
	public Subgraph findSubgraphByName(String graphName) {
		return (Subgraph) findElementByName(SUBGRAPH, graphName);
	}

	/**
	 * Creates a new element and adds it to the subgraph's element dictionary.
	 * For nodes, the <I>info</I> vector can be null or contains:
	 * <ul>
	 * <li>String - name of the node (optional, for automatic name generation)
	 * </ul>
	 * For edges, the <I>info</I> vector must contain (in this order) at least:
	 * <ul>
	 * <li>Node - head node,
	 * <li>String - headport tag (or null),
	 * <li>Node - tail node,
	 * </ul>
	 * Optionally, the <I>info</I> vector can also contain at its end (in this
	 * order):
	 * <ul>
	 * <li>String - tailport tag (or null),
	 * <li>String - a key for distinguishing multiple edges between the same
	 * nodes (or null),
	 * </ul>
	 * For subgraphs, the <I>info</I> vector can be null or contains:
	 * <ul>
	 * <li>String - name of the subgraph (optional, for automatic name
	 * generation)
	 * </ul>
	 * 
	 * @param type
	 *            type of the element to be added
	 * @param info
	 *            a vector specifics for the particular type of element being
	 *            created
	 * @param attrs
	 *            attributes describing the element to be created
	 * @exception InstantiationException
	 *                whenever element cannot be created
	 */
	public Element createElement(int type, Object[] info, Attribute[] attrs) {
		Element elem = null;

		switch (type) {
		case NODE:
			String nodeName = null;
			if (info != null && info.length >= 1) {
				nodeName = (String) info[0];
			}
			Node node = new Node(this, nodeName);
			if (attrs != null) {
				for (int i = 0; i < attrs.length; i++) {
					node.setAttribute(attrs[i]);
				}
			}
			elem = (Element) node;
			break;
		case EDGE:
			if (info == null || info.length < 3) {
				throw new IllegalArgumentException(
						"insufficient info supplied for edge creation");
			}
			Node head = (Node) info[0];
			String headPort = (String) info[1];
			Node tail = (Node) info[2];
			String tailPort = null;
			String key = null;
			if (info.length > 3) {
				tailPort = (String) info[3];
				if (info.length > 4) {
					key = (String) info[4];
				}
			}
			Edge edge = new Edge(this, tail, tailPort, head, headPort, key);
			if (attrs != null) {
				for (int i = 0; i < attrs.length; i++) {
					edge.setAttribute(attrs[i]);
				}
			}
			elem = (Element) edge;
			break;
		case SUBGRAPH:
			String subgName = null;
			if (info != null && info.length >= 1) {
				subgName = (String) info[0];
			}
			Subgraph newSubg = new Subgraph(this, subgName);
			if (attrs != null) {
				for (int i = 0; i < attrs.length; i++) {
					newSubg.setAttribute(attrs[i]);
				}
			}
			elem = (Subgraph) newSubg;
			break;
		default:
			return null;
		}
		return elem;
	}

	/**
	 * Adds the specified node to the subgraph's Node dictionary.
	 * 
	 * @param newNode
	 *            the node to be added to the dictionary.
	 */
	public void addNode(Node newNode) {
		if (newNode == null) {
			return;
		}
		if (nodedict == null) {
			nodedict = new Hashtable();
		}
		/*
		 * if (newNode.getSubgraph() != this) {Message.add(
		 * "A node was added to a graph that was not the same graph as given by the getSubGraph()."
		 * + " This should be corrected using setSubgraph()", Message.ERROR); }
		 */
		nodedict.put(newNode.getName(), newNode);
	}

	/**
	 * Removes the node matching the specified name from the subgraph's Node
	 * dictionary.
	 * 
	 * @param nodeName
	 *            the name of the node to be removed from the dictionary.
	 * @return the node that was removed.
	 */
	public Node removeNode(String nodeName) {
		if (nodedict == null) {
			return (null);
		}
		return ((Node) (nodedict.remove(nodeName)));
	}

	/**
	 * Adds the specified edge to the subgraph's Edge dictionary.
	 * 
	 * @param newEdge
	 *            the edge to be added to the dictionary.
	 */
	public void addEdge(Edge newEdge) {
		if (newEdge == null) {
			return;
		}
		if (edgedict == null) {
			edgedict = new Hashtable();
		}
		edgedict.put(newEdge.getName(), newEdge);
	}

	/**
	 * Removes the edge matching the specified name from the subgraph's Edge
	 * dictionary.
	 * 
	 * @param edgeName
	 *            the name of the edge to be removed from the dictionary.
	 * @return the edge that was removed.
	 */
	public Edge removeEdge(String edgeName) {
		if (edgedict == null) {
			return (null);
		}
		return ((Edge) (edgedict.remove(edgeName)));
	}

	/**
	 * Adds the specified subgraph to the subgraph's graph dictionary.
	 * 
	 * @param newGraph
	 *            the subgraph to be added to the dictionary.
	 */
	public void addSubgraph(Subgraph newGraph) {
		if (newGraph == null) {
			return;
		}
		if (graphdict == null) {
			graphdict = new Hashtable();
		}
		graphdict.put(newGraph.getName(), newGraph);
	}

	/**
	 * Removes the subgraph matching the specified name from the subgraph's
	 * graph dictionary.
	 * 
	 * @param graphName
	 *            the name of the subgraph to be removed from the dictionary.
	 * @return the subgraph that was removed.
	 */
	public Subgraph removeSubgraph(String graphName) {
		if (graphdict == null) {
			return (null);
		}
		return ((Subgraph) (graphdict.remove(graphName)));
	}

	/**
	 * Set flag to indicate if subgraph labels should be rendered
	 * 
	 * @return the previous value
	 */
	public boolean setShowSubgraphLabels(boolean value) {
		boolean oldValue = subgLabels;
		subgLabels = value;
		return (oldValue);
	}

	/**
	 * Set flag to indicate if node labels should be rendered
	 * 
	 * @return the previous value
	 */
	public boolean setShowNodeLabels(boolean value) {
		boolean oldValue = nodeLabels;
		nodeLabels = value;
		return (oldValue);
	}

	/**
	 * Set flag to indicate if edge labels should be rendered
	 * 
	 * @return the previous value
	 */
	public boolean setShowEdgeLabels(boolean value) {
		boolean oldValue = edgeLabels;
		edgeLabels = value;
		return (oldValue);
	}

	/**
	 * Get flag that indicates if subgraph labels should be rendered
	 * 
	 * @return the flag value
	 */
	public boolean getShowSubgraphLabels() {
		return (subgLabels);
	}

	/**
	 * Get flag that indicates if node labels should be rendered
	 * 
	 * @return the flag value
	 */
	public boolean getShowNodeLabels() {
		return (nodeLabels);
	}

	/**
	 * Get flag that indicates if edge labels should be rendered
	 * 
	 * @return the flag value
	 */
	public boolean getShowEdgeLabels() {
		return (edgeLabels);
	}

	/**
	 * Check if the orientation of this subgraph is LR (left-to-right) as
	 * opposed to TB (top-to-bottom).
	 * 
	 * @return true if the orientation is left-to-right.
	 */
	public boolean isLR() {
		Attribute attr = getAttribute("rankdir");

		if (attr == null) {
			return false; // the default
		}
		String value = attr.getStringValue();
		if (value == null) {
			return false; // the default
		}
		if (value.equals("LR")) {
			return true;
		}
		return false;
	}

	/**
	 * Adds a default tag for the specified element type within this subgraph.
	 * 
	 * @param type
	 *            the element type for this tag operation
	 * @param tag
	 *            the tag to associate with this element type.
	 */
	public void addTypeTag(int type, String tag) {
		if (tag == null || tag.indexOf(',') >= 0) {
			throw new RuntimeException("tag value null or contains a comma ("
					+ tag + ")");
		}
		Attribute attr = null;
		Hashtable tags;
		switch (type) {
		case NODE:
			attr = getNodeAttribute(TAG_ATTR);
			break;
		case EDGE:
			attr = getEdgeAttribute(TAG_ATTR);
			break;
		case SUBGRAPH:
			attr = getLocalAttribute(TAG_ATTR);
			break;
		}
		if (attr == null) {
			attr = new Attribute(type, TAG_ATTR, new Hashtable());
			setAttribute(attr);
			switch (type) {
			case NODE:
				setNodeAttribute(attr);
				break;
			case EDGE:
				setEdgeAttribute(attr);
				break;
			case SUBGRAPH:
				setAttribute(attr);
				break;
			}
		}
		tags = (Hashtable) (attr.getValue());

		tags.put(tag, tag);
		// if it becomes desireable to retain the original order, we
		// could always use the value in the following (instead of
		// what is done above) to reconstruct the original order
		// (Note that no code makes use of the value at this point,
		// so that would all have to be added in printAttributes, for
		// example)
		// tags.put(tag,new Long(System.currentTimeMillis()));
	}

	/**
	 * Check if the specified element type has the supplied default tag within
	 * this subgraph.
	 * 
	 * @param type
	 *            the element type for this tag operation
	 * @param tag
	 *            tag value to be searched for
	 * @return true, if this subgraph contains the supplied tag as a default for
	 *         the given type
	 */
	public boolean hasTypeTag(int type, String tag) {
		Attribute attr = null;
		Hashtable tags;
		switch (type) {
		case NODE:
			attr = getNodeAttribute(TAG_ATTR);
			break;
		case EDGE:
			attr = getEdgeAttribute(TAG_ATTR);
			break;
		case SUBGRAPH:
			attr = getLocalAttribute(TAG_ATTR);
			break;
		}
		if (attr == null) {
			return false;
		}
		tags = (Hashtable) (attr.getValue());
		if (tags == null || tags.size() == 0) {
			return false;
		}
		return (tags.containsKey(tag));
	}

	/**
	 * Check if this element type has any default tags at all.
	 * 
	 * @param type
	 *            the element type for this tag operation
	 * @return true, if this Element has any tags
	 */
	public boolean hasTypeTags(int type) {
		Attribute attr = null;
		Hashtable tags;
		switch (type) {
		case NODE:
			attr = getNodeAttribute(TAG_ATTR);
			break;
		case EDGE:
			attr = getEdgeAttribute(TAG_ATTR);
			break;
		case SUBGRAPH:
			attr = getLocalAttribute(TAG_ATTR);
			break;
		}
		if (attr == null) {
			return false;
		}
		tags = (Hashtable) (attr.getValue());
		if (tags == null || tags.size() == 0) {
			return false;
		}
		return (true);
	}

	/**
	 * Removes any and all default tags associated with this element type.
	 * 
	 * @param type
	 *            the element type for this tag operation
	 */
	public void removeTypeTags(int type) {
		Attribute attr = null;
		Hashtable tags;
		switch (type) {
		case NODE:
			attr = getNodeAttribute(TAG_ATTR);
			break;
		case EDGE:
			attr = getEdgeAttribute(TAG_ATTR);
			break;
		case SUBGRAPH:
			attr = getLocalAttribute(TAG_ATTR);
			break;
		}
		if (attr == null) {
			return;
		}
		tags = (Hashtable) (attr.getValue());
		if (tags == null || tags.size() == 0) {
			return;
		}
		tags.clear();
	}

	/**
	 * Removes the specified tag from this element.
	 * 
	 * @param type
	 *            the element type for this tag operation
	 * @param tag
	 *            the tag value to remove
	 */
	public void removeTypeTag(int type, String tag) {
		Attribute attr = null;
		Hashtable tags;
		switch (type) {
		case NODE:
			attr = getNodeAttribute(TAG_ATTR);
			break;
		case EDGE:
			attr = getEdgeAttribute(TAG_ATTR);
			break;
		case SUBGRAPH:
			attr = getLocalAttribute(TAG_ATTR);
			break;
		}
		if (attr == null) {
			return;
		}
		tags = (Hashtable) (attr.getValue());
		if (tags == null || tags.size() == 0) {
			return;
		}
		tags.remove(tag);
	}

	/**
	 * Get a count of elements in this subgraph. No recursion to descendants is
	 * done.
	 * 
	 * @param types
	 *            a bitwise-oring of NODE, EDGE, SUBGRAPH to determine which
	 *            element types should be in the count
	 * @return a count of the specified elements in this subgraph.
	 * @see GrappaConstants#NODE
	 * @see GrappaConstants#EDGE
	 * @see GrappaConstants#SUBGRAPH
	 */
	public int countOfLocalElements(int types) {
		int count = 0;
		if ((types & NODE) != 0 && nodedict != null) {
			count += nodedict.size();
		}
		if ((types & EDGE) != 0 && edgedict != null) {
			count += edgedict.size();
		}
		if ((types & SUBGRAPH) != 0 && graphdict != null) {
			count += graphdict.size();
		}
		return count;
	}

	/**
	 * Get a count of elements in this subgraph and, by recursion, descendant
	 * subgraphs. The subgraph itself is not counted.
	 * 
	 * @param types
	 *            a bitwise-oring of NODE, EDGE, SUBGRAPH to determine which
	 *            element types should be in the count
	 * @return a count of the specified elements in this subgraph and its
	 *         descendants.
	 * @see GrappaConstants#NODE
	 * @see GrappaConstants#EDGE
	 * @see GrappaConstants#SUBGRAPH
	 */
	public int countOfElements(int types) {
		int count = 0;
		if ((types & NODE) != 0 && nodedict != null) {
			count += nodedict.size();
		}
		if ((types & EDGE) != 0 && edgedict != null) {
			count += edgedict.size();
		}
		if (graphdict != null) {
			if ((types & SUBGRAPH) != 0) {
				count += graphdict.size();
			}
			Enumeration enu = graphdict.elements();
			while (enu.hasMoreElements()) {
				count += ((Subgraph) enu.nextElement()).countOfElements(types);
			}
		}
		return count;
	}

	/**
	 * Delete this subgraph or any contained subgraph, at any depth, if the
	 * subgraph contains no elements.
	 */
	public void removeEmptySubgraphs() {
		if ((graphdict == null || graphdict.size() == 0)
				&& (nodedict == null || nodedict.size() == 0)
				&& (edgedict == null || edgedict.size() == 0)) {
			delete();
			return;
		}
		if (graphdict != null) {
			Enumeration enu = graphdict.elements();
			while (enu.hasMoreElements()) {
				((Subgraph) enu.nextElement()).removeEmptySubgraphs();
			}
		}
	}

	/**
	 * @return true if this subgraph or any subgraph contained within this
	 *         subgraph, at any depth, is empty.
	 */
	public boolean hasEmptySubgraphs() {
		if ((graphdict == null || graphdict.size() == 0)
				&& (nodedict == null || nodedict.size() == 0)
				&& (edgedict == null || edgedict.size() == 0)) {
			return (true);
		}
		if (graphdict != null) {
			Enumeration enu = graphdict.elements();
			while (enu.hasMoreElements()) {
				if (((Subgraph) enu.nextElement()).hasEmptySubgraphs()) {
					return (true);
				}
			}
		}
		return (false);
	}

	//
	// Start PatchWork (similar to TreeMap) stuff
	//

	private double PATCHEDGE = 2;
	private double PATCHEDGE2 = 2.0 * PATCHEDGE;
	private Element[] sgPatches = null;
	private Element[] elPatches = null;
	private GrappaBox patch = null;

	public void clearPatchWork() {

		prepPatchWork(null, -1);
	}

	public void patchWork(java.awt.geom.Rectangle2D.Double r, boolean square,
			int mode) {

		preparePatchWork(mode);
		computePatchWork(r instanceof GrappaBox ? r : new GrappaBox(r), square);
		if (mode == 0) {
			Subgraph sg;
			String style;
			Attribute attr;
			Enumeration enu = elements(Grappa.SUBGRAPH);
			while (enu.hasMoreElements()) {
				sg = (Subgraph) (enu.nextElement());
				if (sg != this) {
					attr = sg.getAttribute(STYLE_ATTR);
					if (attr != null) {
						style = attr.getStringValue();
						sg.setAttribute(STYLE_ATTR, style != null
								&& style.length() > 0 ? style
								+ ",filled(false)" : null);
					}
				}
			}
		} else {
			float sgtot = countOfElements(Grappa.SUBGRAPH) - 2;
			float nbr = 0;
			Subgraph sg;
			String style;
			Attribute attr;
			Enumeration enu = elements(Grappa.SUBGRAPH);
			while (enu.hasMoreElements()) {
				sg = (Subgraph) (enu.nextElement());
				if (sg != this) {
					sg.setAttribute(COLOR_ATTR, java.awt.Color.getHSBColor(
							(float) (0.05 + 0.9 * (nbr++ / sgtot)),
							(float) 1.0, (float) 1.0));
					attr = sg.getAttribute(STYLE_ATTR);
					if (attr == null) {
						sg.setAttribute(STYLE_ATTR, "filled");
					} else {
						style = attr.getStringValue();
						sg.setAttribute(STYLE_ATTR, style == null
								|| style.length() == 0 ? "filled" : style
								+ ",filled");
					}
				}
			}
		}
	}

	public double preparePatchWork(int mode) {

		double total;

		total = prepPatchWork(PATCH_ATTR, mode);

		if (mode == 0) {
			combPatchWork();
			if (elPatches != null) {
				Arrays.sort(elPatches, 0, elPatches.length, this);
			}
		}

		return (total);
	}

	Element[] getPatches() {
		return (elPatches);
	}

	private void combPatchWork() {

		Enumeration enu;
		Hashtable dict;
		Subgraph sg;
		Element[] patches;
		Element[] elpat;
		Element[] sgpat;
		Element[] tmparr;

		patches = elPatches;

		sgpat = sgPatches; // snapshot

		if (sgpat != null && sgpat.length > 0) {
			for (int i = 0; i < sgpat.length; i++) {
				sg = (Subgraph) sgpat[i];
				sg.combPatchWork();
				elpat = sg.getPatches();
				if (elpat != null && elpat.length > 0) {
					if (patches == null || patches.length == 0) {
						patches = elpat;
					} else {
						tmparr = new Element[patches.length + elpat.length];
						System.arraycopy(patches, 0, tmparr, 0, patches.length);
						System.arraycopy(elpat, 0, tmparr, patches.length,
								elpat.length);
						patches = tmparr;
					}
				}
			}
		}

		sgPatches = null;
		elPatches = patches;
	}

	private double prepPatchWork(String attrname, int mode) {

		double total;
		Enumeration enu;
		Hashtable dict;
		Object obj;
		int m;
		int n;
		Subgraph sg;
		Element el;
		Element[] tmparr;

		total = 0;

		dict = graphdict; // snapshot

		sgPatches = null;

		if (dict != null && dict.size() > 0) {
			if (attrname != null) {
				sgPatches = new Element[dict.size()];
			}
			n = 0;
			enu = dict.elements();
			while (enu.hasMoreElements()) {
				sg = (Subgraph) enu.nextElement();
				total += sg.prepPatchWork(attrname, mode);
				if (attrname != null) {
					sgPatches[n++] = sg;
				}
			}
		}

		dict = nodedict; // snapshot;

		elPatches = null;

		if (attrname != null && dict != null && dict.size() > 0) {
			m = 0;
			n = 0;
			if (mode <= 0) {
				elPatches = new Element[dict.size()];
			} else if (sgPatches == null) {
				elPatches = new Element[dict.size()];
			} else {
				n = sgPatches.length;
				elPatches = new Element[n + dict.size()];
				System.arraycopy(sgPatches, 0, elPatches, 0, n);
				sgPatches = null;
			}
			enu = dict.elements();
			while (enu.hasMoreElements()) {
				el = (Element) enu.nextElement();
				if ((obj = el.getAttributeValue(attrname)) != null) {
					if (obj instanceof Number) {
						el.setPatchSize(((Number) obj).doubleValue());
						total += el.getPatchSize();
						elPatches[n++] = el;
					} else {
						m++;
					}
				} else {
					m++;
				}
			}
			if (m > 0) {
				if (n == m) {
					elPatches = null;
				} else {
					tmparr = new Element[n - m];
					System.arraycopy(elPatches, 0, tmparr, 0, tmparr.length);
					elPatches = tmparr;
				}
			}
		}

		if (mode != 0) {
			if (sgPatches != null) {
				Arrays.sort(sgPatches, 0, sgPatches.length, this);
			}
			if (elPatches != null) {
				Arrays.sort(elPatches, 0, elPatches.length, this);
			}
		}

		setPatchSize(total);

		return (total);
	}

	// squarified layout
	double aspect(java.awt.geom.Rectangle2D.Double r) {
		return (r.getWidth() == 0 ? 1 : r.getHeight() / r.getWidth());
	}

	double score(double wd, double ht) {
		return ((ht <= PATCHEDGE2 || wd <= PATCHEDGE2) ? Double.MAX_VALUE
				: (ht > wd ? (wd == 0 ? (ht == 0 ? 1 : Double.MAX_VALUE) : ht
						/ wd) : (ht == 0 ? (wd == 0 ? 1 : Double.MAX_VALUE)
						: wd / ht)));
	}

	public void computePatchWork(java.awt.geom.Rectangle2D.Double r,
			boolean square) {
		if (square) {
			compSqPatchWork(r, true);
		} else {
			compStdPatchWork(r, true);
		}
	}

	private void compSqPatchWork(java.awt.geom.Rectangle2D.Double r, boolean top) {
		double frac;
		double total;
		double previous;
		double next;
		double tot;
		double prv;
		double nxt;
		double dir;
		java.awt.geom.Rectangle2D.Double box;
		java.awt.geom.Rectangle2D.Double p;
		java.awt.geom.Rectangle2D.Double pp;
		Element el;
		Attribute attr;
		String style;
		int i;
		int j;
		double pscore;
		double nscore;
		double sz;
		double psz;
		double tsz;
		double tfrac;

		setPatch(r);
		setAttribute(MINSIZE_ATTR, new GrappaSize(r.getWidth(), r.getHeight()
				+ (top ? 0 : 1)));
		dir = aspect(r);

		total = getPatchSize();
		if (top) {
			box = new GrappaBox(r);
		} else {
			box = new GrappaBox(r.getX() + PATCHEDGE, r.getY() + PATCHEDGE, r
					.getWidth()
					- PATCHEDGE2, r.getHeight() - PATCHEDGE2);

		}
		if (dir > 1) {
			previous = box.getY();
		} else {
			previous = box.getX();

		}
		if (sgPatches != null) {
			i = 0;
			while (i < sgPatches.length) {
				el = (Element) sgPatches[i];
				sz = el.getPatchSize();
				if ((i + 1) < sgPatches.length) {
					psz = 0;
					frac = sz / total;
					if (dir > 1) {
						pscore = score(box.getWidth(), frac * box.getHeight());
					} else {
						pscore = score(frac * box.getWidth(), box.getHeight());
					}
					j = i + 1;

					for (;;) {
						tsz = ((Element) sgPatches[j]).getPatchSize();
						tot = psz + sz + tsz;
						tfrac = tot / total;
						if (dir > 1) {
							nscore = score(box.getWidth() * sz / tot, tfrac
									* box.getHeight());
						} else {
							nscore = score(tfrac * box.getWidth(), box
									.getHeight()
									* sz / tot);
						}
						if (nscore <= pscore) {
							if (dir > 1) {
								pscore = score(box.getWidth() * tsz / tot,
										tfrac * box.getHeight());
							} else {
								pscore = score(tfrac * box.getWidth(), box
										.getHeight()
										* tsz / tot);
							}
							psz += sz;
							sz = tsz;
							tsz = 0;
							j++;
							if (j < sgPatches.length) {
								continue;
							}
						} else {
							tsz = 0;
						}
						tot = psz + sz + tsz;
						frac = tot / total;
						if (dir > 1) {
							prv = box.getX();
							next = frac * box.getHeight();
						} else {
							prv = box.getY();
							next = frac * box.getWidth();
						}
						for (; i < j; i++) {
							el = (Element) sgPatches[i];
							if (dir > 1) {
								p = new GrappaBox(prv, previous, nxt = box
										.getWidth()
										* el.getPatchSize() / tot, next);
							} else {
								p = new GrappaBox(previous, prv, next,
										nxt = box.getHeight()
												* el.getPatchSize() / tot);
							}
							((Subgraph) el).compSqPatchWork(p, false);
							prv += nxt;
						}
						break;
					}
				} else {
					frac = sz / total;
					if (dir > 1) {
						p = new GrappaBox(box.getX(), previous, box.getWidth(),
								(next = frac * box.getHeight()));
					} else {
						p = new GrappaBox(previous, box.getY(), (next = frac
								* box.getWidth()), box.getHeight());
					}
					((Subgraph) el).compSqPatchWork(p, false);
					i++;
				}
				previous += next;
			}
		}
		if (elPatches != null) {
			i = 0;
			while (i < elPatches.length) {
				el = (Element) elPatches[i];
				sz = el.getPatchSize();
				if ((i + 1) < elPatches.length) {
					psz = 0;
					frac = sz / total;
					if (dir > 1) {
						pscore = score(box.getWidth(), frac * box.getHeight());
					} else {
						pscore = score(frac * box.getWidth(), box.getHeight());
					}
					j = i + 1;
					for (;;) {
						tsz = ((Element) elPatches[j]).getPatchSize();
						tot = psz + sz + tsz;
						tfrac = tot / total;
						if (dir > 1) {
							nscore = score(box.getWidth() * sz / tot, tfrac
									* box.getHeight());
						} else {
							nscore = score(tfrac * box.getWidth(), box
									.getHeight()
									* sz / tot);
						}
						if (nscore <= pscore) {
							if (dir > 1) {
								pscore = score(box.getWidth() * tsz / tot,
										tfrac * box.getHeight());
							} else {
								pscore = score(tfrac * box.getWidth(), box
										.getHeight()
										* tsz / tot);
							}
							psz += sz;
							sz = tsz;
							tsz = 0;
							j++;
							if (j < elPatches.length) {
								continue;
							}
						} else {
							tsz = 0;
						}
						tot = psz + sz + tsz;
						frac = tot / total;
						if (dir > 1) {
							prv = box.getX();
							next = frac * box.getHeight();
						} else {
							prv = box.getY();
							next = frac * box.getWidth();
						}
						for (; i < j; i++) {
							el = (Element) elPatches[i];
							if (el instanceof Node) {
								if (dir > 1) {
									el.setPatch(prv, previous, nxt = box
											.getWidth()
											* el.getPatchSize() / tot, next);
								} else {
									el.setPatch(previous, prv, next, nxt = box
											.getHeight()
											* el.getPatchSize() / tot);
								}
								p = el.getPatch();
								el.setAttribute(POS_ATTR, new GrappaPoint(p
										.getCenterX(), -p.getCenterY()));
								el.setAttribute(WIDTH_ATTR, new Double(p
										.getWidth() / 72.0));
								el.setAttribute(HEIGHT_ATTR, new Double(p
										.getHeight() / 72.0));
								if (el.getLocalAttribute(COLOR_ATTR) == null) {
									el.setAttribute(COLOR_ATTR, "white");
								}
								attr = el.getAttribute(STYLE_ATTR);
								if (attr == null) {
									el.setAttribute(STYLE_ATTR,
											"filled,lineColor(black)");
								} else {
									style = attr.getStringValue();
									el
											.setAttribute(
													STYLE_ATTR,
													style == null
															|| style.length() == 0 ? "filled,lineColor(black)"
															: style
																	+ ",filled,lineColor(black)");
								}
							} else {
								if (dir > 1) {
									p = new GrappaBox(prv, previous, nxt = box
											.getWidth()
											* el.getPatchSize() / tot, next);
								} else {
									p = new GrappaBox(previous, prv, next,
											nxt = box.getHeight()
													* el.getPatchSize() / tot);
								}
								((Subgraph) el).compSqPatchWork(p, false);
							}
							prv += nxt;
						}
						break;
					}
				} else {
					frac = sz / total;
					if (el instanceof Node) {
						if (dir > 1) {
							el.setPatch(box.getX(), previous, box.getWidth(),
									(next = frac * box.getHeight()));
						} else {
							el.setPatch(previous, box.getY(), (next = frac
									* box.getWidth()), box.getHeight());
						}
						p = el.getPatch();
						el.setAttribute(POS_ATTR, new GrappaPoint(p
								.getCenterX(), -p.getCenterY()));
						el.setAttribute(WIDTH_ATTR, new Double(
								p.getWidth() / 72.0));
						el.setAttribute(HEIGHT_ATTR, new Double(
								p.getHeight() / 72.0));
						if (el.getLocalAttribute(COLOR_ATTR) == null) {
							el.setAttribute(COLOR_ATTR, "white");
						}
						attr = el.getAttribute(STYLE_ATTR);
						if (attr == null) {
							el.setAttribute(STYLE_ATTR,
									"filled,lineColor(black)");
						} else {
							style = attr.getStringValue();
							el
									.setAttribute(
											STYLE_ATTR,
											style == null
													|| style.length() == 0 ? "filled,lineColor(black)"
													: style
															+ ",filled,lineColor(black)");
						}
					} else {
						if (dir > 1) {
							p = new GrappaBox(box.getX(), previous, box
									.getWidth(),
									(next = frac * box.getHeight()));
						} else {
							p = new GrappaBox(previous, box.getY(),
									(next = frac * box.getWidth()), box
											.getHeight());
						}
						((Subgraph) el).compSqPatchWork(p, false);
					}
					i++;
				}
				previous += next;
			}
		}
	}

	private void compStdPatchWork(java.awt.geom.Rectangle2D.Double r,
			boolean top) {
		double sz;
		double frac;
		double total;
		double previous;
		double next;
		double dir;
		java.awt.geom.Rectangle2D.Double box;
		java.awt.geom.Rectangle2D.Double p;
		Element el;
		Attribute attr;
		String style;

		setPatch(r);
		setAttribute(MINSIZE_ATTR, new GrappaSize(r.getWidth(), r.getHeight()
				+ (top ? 0 : 1)));
		dir = aspect(r);

		total = getPatchSize();
		if (top) {
			box = new GrappaBox(r);
		} else {
			box = new GrappaBox(r.getX() + PATCHEDGE, r.getY() + PATCHEDGE, r
					.getWidth()
					- PATCHEDGE2, r.getHeight() - PATCHEDGE2);

		}
		if (dir > 1) {
			previous = box.getY();
		} else {
			previous = box.getX();

		}
		if (sgPatches != null) {
			for (int i = 0; i < sgPatches.length; i++) {
				el = (Element) sgPatches[i];
				sz = el.getPatchSize();
				frac = sz / total;
				if (dir > 1) {
					((Subgraph) el).compStdPatchWork(new GrappaBox(box.getX(),
							previous, box.getWidth(), (next = frac
									* box.getHeight())), false);
				} else {
					((Subgraph) el).compStdPatchWork(new GrappaBox(previous,
							box.getY(), (next = frac * box.getWidth()), box
									.getHeight()), false);
				}
				previous += next;
			}
		}
		if (elPatches != null) {
			for (int i = 0; i < elPatches.length; i++) {
				el = (Element) elPatches[i];
				sz = el.getPatchSize();
				frac = sz / total;
				if (el instanceof Node) {
					if (dir > 1) {
						el.setPatch(box.getX(), previous, box.getWidth(),
								(next = frac * box.getHeight()));
					} else {
						el.setPatch(previous, box.getY(), (next = frac
								* box.getWidth()), box.getHeight());
					}
					p = el.getPatch();
					el.setAttribute(POS_ATTR, new GrappaPoint(p.getCenterX(),
							-p.getCenterY()));
					el
							.setAttribute(WIDTH_ATTR, new Double(
									p.getWidth() / 72.0));
					el.setAttribute(HEIGHT_ATTR, new Double(
							p.getHeight() / 72.0));
					if (el.getLocalAttribute(COLOR_ATTR) == null) {
						el.setAttribute(COLOR_ATTR, "white");
					}
					attr = el.getAttribute(STYLE_ATTR);
					if (attr == null) {
						el.setAttribute(STYLE_ATTR, "filled,lineColor(black)");
					} else {
						style = attr.getStringValue();
						el
								.setAttribute(
										STYLE_ATTR,
										style == null || style.length() == 0 ? "filled,lineColor(black)"
												: style
														+ ",filled,lineColor(black)");
					}
				} else {
					if (dir > 1) {
						((Subgraph) el).compStdPatchWork(new GrappaBox(box
								.getX(), previous, box.getWidth(), (next = frac
								* box.getHeight())), false);
					} else {
						((Subgraph) el).compStdPatchWork(new GrappaBox(
								previous, box.getY(), (next = frac
										* box.getWidth()), box.getHeight()),
								false);
					}
				}
				previous += next;
			}
		}
	}

	// Comparator for patchArea

	public int compare(Object o1, Object o2) {

		if (o1 instanceof Element) {
			if (o2 instanceof Element) {
				// biggest to smallest
				double diff = ((Element) o2).getPatchSize()
						- ((Element) o1).getPatchSize();
				return (diff < 0 ? -1 : diff > 0 ? 1 : 0);
			} else {
				return (0);
			}
		} else {
			return (0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		// needs to be overridden as Comparator interface is implemented
		// Object.equals() tests for object identitiy
		return super.equals(obj);
	}

	//
	// End PatchWork stuff
	//

	/**
	 * Get an enumeration of the node elements in this subgraph.
	 * 
	 * @return an Enumeration of Node objects
	 */
	public Enumeration nodeElements() {
		if (nodedict == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return nodedict.elements();
	}

	/**
	 * Get an enumeration of the edge elements in this subgraph.
	 * 
	 * @return an Enumeration of Edge objects
	 */
	public Enumeration edgeElements() {
		if (edgedict == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return edgedict.elements();
	}

	/**
	 * Get an enumeration of the subgraph elements in this subgraph.
	 * 
	 * @return an Enumeration of Subgraph objects
	 */
	public Enumeration subgraphElements() {
		if (graphdict == null) {
			return Grappa.emptyEnumeration.elements();
		}
		return graphdict.elements();
	}

	/**
	 * Get an enumeration of elements in this subgraph and any subgraphs under
	 * this one.
	 * 
	 * @param types
	 *            a bitwise-oring of NODE, EDGE, SUBGRAPH to determine which
	 *            element types should be in the enumeration
	 * @return a GraphEnumeration containing Element objects.
	 * @see GrappaConstants#NODE
	 * @see GrappaConstants#EDGE
	 * @see GrappaConstants#SUBGRAPH
	 */
	public GraphEnumeration elements(int types) {
		return new Enumerator(types);
	}

	/**
	 * Get an enumeration of all elements in this subgraph and any subgraphs
	 * under this one. A convenience method equivalent to: <code>
	 * elements(NODE|EDGE|SUBGRAPH)
	 * </code>
	 * 
	 * @return a GraphEnumeration containing Element objects.
	 * @see Subgraph#elements(int)
	 */
	public GraphEnumeration elements() {
		return new Enumerator(NODE | EDGE | SUBGRAPH);
	}

	class Enumerator implements GraphEnumeration {
		private Subgraph root = null;
		private int types = 0;
		private Enumeration enu = null;
		private GraphEnumeration subEnum = null;
		private Element elem = null;
		private int dictType = 0;

		Enumerator(int t) {
			root = Subgraph.this;
			types = t;

			if ((types & SUBGRAPH) != 0) {
				elem = (Element) (root);
			} else {
				elem = null;
			}
			enu = subgraphElements();
			if (enu.hasMoreElements()) {
				dictType = SUBGRAPH;
				while (enu.hasMoreElements()) {
					subEnum = ((Subgraph) (enu.nextElement())).new Enumerator(
							types);
					if (subEnum.hasMoreElements()) {
						if (elem == null) {
							elem = (Element) subEnum.nextElement();
						}
						break;
					}
				}
			} else {
				dictType = 0;
				enu = null;
				subEnum = null;
			}
			if (enu == null) {
				if ((types & NODE) != 0
						&& (enu = nodeElements()).hasMoreElements()) {
					dictType = NODE;
					if (elem == null) {
						elem = (Element) enu.nextElement();
					}
				} else if ((types & EDGE) != 0
						&& (enu = edgeElements()).hasMoreElements()) {
					dictType = EDGE;
					if (elem == null) {
						elem = (Element) enu.nextElement();
					}
				} else {
					enu = null;
				}
			}
		}

		public boolean hasMoreElements() {
			return elem != null;
		}

		public Object nextElement() {
			if (elem == null) {
				throw new NoSuchElementException("Subgraph$Enumerator");
			}
			Element el = elem;
			if (subEnum != null && subEnum.hasMoreElements()) {
				elem = (Element) subEnum.nextElement();
			} else if (enu != null && enu.hasMoreElements()) {
				do {
					elem = (Element) enu.nextElement();
					if (elem.isSubgraph()) {
						subEnum = ((Subgraph) elem).new Enumerator(
								getEnumerationTypes());
						if (subEnum.hasMoreElements()) {
							elem = (Element) subEnum.nextElement();
							break;
						} else {
							elem = null;
						}
					} else {
						break;
					}
				} while (enu.hasMoreElements());
			} else {
				elem = null;
			}
			if (elem == null) {
				if (dictType != 0) {
					if (dictType == SUBGRAPH) {
						if ((getEnumerationTypes() & NODE) != 0
								&& (enu = nodeElements()).hasMoreElements()) {
							dictType = NODE;
							elem = (Element) enu.nextElement();
						} else if ((getEnumerationTypes() & EDGE) != 0
								&& (enu = edgeElements()).hasMoreElements()) {
							dictType = EDGE;
							elem = (Element) enu.nextElement();
						} else {
							dictType = 0;
							enu = null;
						}
					} else if (dictType == NODE) {
						if ((getEnumerationTypes() & EDGE) != 0
								&& (enu = edgeElements()).hasMoreElements()) {
							dictType = EDGE;
							elem = (Element) enu.nextElement();
						} else {
							dictType = 0;
							enu = null;
						}
					} else {
						dictType = 0;
						enu = null;
					}
				}
			}
			return el;
		}

		public Element nextGraphElement() {
			return (Element) nextElement();
		}

		public Subgraph getSubgraphRoot() {
			return root;
		}

		public int getEnumerationTypes() {
			return types;
		}
	}

	/**
	 * Get a vector of elements in this subgraph and, by recursion, descendant
	 * subgraphs.
	 * 
	 * @param types
	 *            a bitwise-oring of NODE, EDGE, SUBGRAPH to determine which
	 *            element types should be in the count
	 * @return a vector of the specified elements in this subgraph and its
	 *         descendants (excluding the current subgraph itself).
	 * @see GrappaConstants#NODE
	 * @see GrappaConstants#EDGE
	 * @see GrappaConstants#SUBGRAPH
	 */
	public Vector vectorOfElements(int types) {
		Vector retVec = new Vector();
		int count = 0;
		Enumeration elems = null;
		if ((types & NODE) != 0 && nodedict != null) {
			count += nodedict.size();
			retVec.ensureCapacity(count);
			elems = nodedict.elements();
			while (elems.hasMoreElements()) {
				retVec.addElement(elems.nextElement());
			}
		}
		if ((types & EDGE) != 0 && edgedict != null) {
			count += edgedict.size();
			retVec.ensureCapacity(count);
			elems = edgedict.elements();
			while (elems.hasMoreElements()) {
				retVec.addElement(elems.nextElement());
			}
		}
		if (graphdict != null) {
			if ((types & SUBGRAPH) != 0) {
				count += graphdict.size();
				retVec.ensureCapacity(count);
			}
			elems = graphdict.elements();
			while (elems.hasMoreElements()) {
				((Subgraph) (elems.nextElement())).recurseVectorOfElements(
						types, retVec, count);
			}
		}
		return (retVec);
	}

	// used above
	void recurseVectorOfElements(int types, Vector retVec, int count) {
		if ((types & SUBGRAPH) != 0) {
			retVec.addElement(this);
		}
		Enumeration elems = null;
		if ((types & NODE) != 0 && nodedict != null) {
			count += nodedict.size();
			retVec.ensureCapacity(count);
			elems = nodedict.elements();
			while (elems.hasMoreElements()) {
				retVec.addElement(elems.nextElement());
			}
		}
		if ((types & EDGE) != 0 && edgedict != null) {
			count += edgedict.size();
			retVec.ensureCapacity(count);
			elems = edgedict.elements();
			while (elems.hasMoreElements()) {
				retVec.addElement(elems.nextElement());
			}
		}
		if (graphdict != null) {
			if ((types & SUBGRAPH) != 0) {
				count += graphdict.size();
				retVec.ensureCapacity(count);
			}
			elems = graphdict.elements();
			while (elems.hasMoreElements()) {
				((Subgraph) (elems.nextElement())).recurseVectorOfElements(
						types, retVec, count);
			}
		}
	}

	public void clearSubgraph() {
		currentSelection = null;
		edgeAttributes = null;
		edgedict = null;
		elPatches = null;
		graphdict = null;
		nodeAttributes = null;
		nodedict = null;
		patch = null;
		sgPatches = null;
	}
}

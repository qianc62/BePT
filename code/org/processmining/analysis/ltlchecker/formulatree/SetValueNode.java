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

package org.processmining.analysis.ltlchecker.formulatree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.processmining.analysis.ltlchecker.parser.Attribute;
import org.processmining.analysis.ltlchecker.parser.AttributeNoValueException;
import org.processmining.analysis.ltlchecker.parser.ConceptSetAttribute;
import org.processmining.analysis.ltlchecker.parser.ParseAttributeException;
import org.processmining.analysis.ltlchecker.parser.SetAttribute;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyCollection;

/**
 * SetValueNode is a representation of a set literal, attribute or set.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class SetValueNode extends ValueNode {

	// FIELDS

	/** It is an attribute of this ate. */
	public static final int VALUE = 0;

	/** It is a concept */
	public static final int MODEL_REFERENCE_SET = 1;

	/** It is a set */
	public static final int SET = 2;

	/**
	 * If this node is an attributevalue or a literal, this field contains the
	 * attribute.
	 */
	private SetAttribute value;

	/** If this node is an set itself. */
	private TreeSet set;

	/** The `type' of this node, either attribute or literal. */
	private int type;

	/** The model references if this node is a MODEL_REFERENCE_SET. */
	private List<String> modelReferencesInOntology;

	private OntologyCollection ontologies = null;

	private Set<String> cachedModelRefs = null;

	// CONSTRUCTORS
	public SetValueNode(int type) {
		this.type = type;
	}

	public SetValueNode(int type, OntologyCollection ontologies) {
		this.type = type;
		this.ontologies = ontologies;
	}

	// METHODS

	/**
	 * Set the value.
	 * 
	 * @param val
	 *            The value to set.
	 */
	public void setValue(SetAttribute val) {
		this.value = val;
	}

	/**
	 * Set the set.
	 * 
	 * @param set
	 *            The set to set.
	 */
	public void setSet(TreeSet set) {
		this.set = set;
	}

	public TreeSet getSet() {
		return set;
	}

	public void setModelReferenceSet(List<String> modelReferencesInOntology) {
		this.modelReferencesInOntology = modelReferencesInOntology;
	}

	/**
	 * Is a string in this set?
	 * 
	 * @param name
	 *            String to test.
	 * 
	 * @return If name in set return true, else false.
	 */
	public boolean in(String name) {
		if (name == null) {
			return false;
		}
		return this.set.contains(name);
	}

	/**
	 * Compute the value of this node, either getting the string representation
	 * fo the attribute or giving the literal.
	 * 
	 * @param pi
	 *            The current process instance.
	 * @param ate
	 *            The current audit trail entry of this pi.
	 * 
	 * @return The string of this node.
	 */
	public String value(ProcessInstance pi, LinkedList ates, int ateNr)
			throws AttributeNoValueException, ParseAttributeException {
		try {
			nr = ateNr;
			String result = null;

			if (this.type == VALUE) {
				result = this.value.value(pi, ates, this.getBinder().getNr());
			}
			return result;

		} catch (ParseAttributeException aep) {
			throw aep;
		} catch (AttributeNoValueException anve) {
			throw anve;
		}
	}

	public int getType() {
		return type;
	}

	public List<String> getModelReferencesInOntology() {
		if (modelReferencesInOntology != null) {
			return modelReferencesInOntology;
		} else if (value != null && value.isConceptSet()) {
			return ((ConceptSetAttribute) value).getModelReferences();
		} else {
			return new ArrayList<String>(0);
		}
	}

	public Set<String> getModelReferencesInLog() {
		if (cachedModelRefs != null) {
			return cachedModelRefs;
		}
		List<String> refsInOntologies = getModelReferencesInOntology();
		Set<String> result = new HashSet<String>();

		for (int i = 0; i < refsInOntologies.size(); i++) {
			String uri = refsInOntologies.get(i);
			boolean includeSuper = false;
			boolean includeSub = false;

			if (i + 1 < refsInOntologies.size()) {
				includeSuper = "include-super-concepts".equals(refsInOntologies
						.get(i + 1));
				includeSub = "include-sub-concepts".equals(refsInOntologies
						.get(i + 1));

				if (i + 2 < refsInOntologies.size()
						&& (includeSub || includeSuper)) {
					includeSuper = includeSuper
							|| "include-super-concepts".equals(refsInOntologies
									.get(i + 2));
					includeSub = includeSub
							|| "include-sub-concepts".equals(refsInOntologies
									.get(i + 2));
				}
			}
			if (ontologies == null) {
				result.add(uri);
			} else {
				result.addAll(ontologies.translateToReferenceInLog(uri,
						includeSuper, includeSub));
			}
		}
		cachedModelRefs = result;
		return result;
	}

	public String toString() {
		if (type == VALUE) {
			return value.toString();
		} else if (type == MODEL_REFERENCE_SET) {
			StringBuffer result = new StringBuffer("[ ");

			for (String uri : getModelReferencesInOntology()) {
				result.append("@" + uri + " ");
			}
			result.append("]");
			return result.toString();
		} else {
			StringBuffer result = new StringBuffer();

			for (Object s : set) {
				if (result.length() > 0) {
					result.append(", ");
				}
				result.append("\"" + s.toString() + "\"");
			}
			return "[" + result.toString() + "]";
		}
	}

	public boolean hasInstanceOf(List<String> refs) {
		Set<String> set = getModelReferencesInLog();

		for (String uri : refs) {
			if (set.contains(uri)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String asParseableDefaultValue() {
		if (type == VALUE) {
			return "\"" + value.toString() + "\"";
		} else if (type == MODEL_REFERENCE_SET) {
			StringBuffer result = new StringBuffer("[ ");

			for (String uri : getModelReferencesInOntology()) {
				result.append("@" + uri + " ");
			}
			result.append("]");
			return result.toString();
		} else {
			StringBuffer result = new StringBuffer();

			for (Object s : set) {
				if (result.length() > 0) {
					result.append("|");
				}
				result.append(s.toString());
			}
			return "\"" + result.toString() + "\"";
		}
	}

	public Attribute getValue() {
		return value;
	}
}

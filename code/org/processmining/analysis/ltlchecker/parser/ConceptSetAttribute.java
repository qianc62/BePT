package org.processmining.analysis.ltlchecker.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.processmining.framework.log.ProcessInstance;

public class ConceptSetAttribute extends SetAttribute {

	private List<String> values;

	public ConceptSetAttribute(List<String> values, int kind, Attribute type) {
		super(toString(values), type.getScope(), Attribute.CONCEPTSET, kind);
		assert(kind == Attribute.LITERAL);
		assert(isConceptSet());
		this.values = values;
	}

	public String value(ProcessInstance pi, LinkedList ates, int ateNr) throws AttributeNoValueException, ParseAttributeException {
		return ateNr < ates.size() ? this.getValue() : "";
	}
	
	public List<String> modelReferences(ProcessInstance pi, LinkedList ates, int ateNr) throws AttributeNoValueException {
		return ateNr < ates.size() ? this.values : new ArrayList<String>(0);
	}
	
	public List<String> getModelReferences() {
		return this.values;
	}
	
	private static String toString(List<String> values) {
		String result = "";
		
		for (String uri : values) {
			result += " @" + uri;
		}
		return "[" + result + " ]";
	}
}

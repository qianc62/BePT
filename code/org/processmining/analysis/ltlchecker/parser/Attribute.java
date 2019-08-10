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

package org.processmining.analysis.ltlchecker.parser;

import java.text.SimpleDateFormat;

/**
 * Attribute is an class containing an defined attribute, that is, its
 * identifier and its scope (that is, is it an attribute of an process
 * instance or of an audit trail entry). Every type can be represented by
 * subclassing this class. If none fields are added, an (almost) empty
 * implementation of the subclass is enough.
 *
 * The class can model renamings and argument definitions too. It may be
 * clumsy, but the code of the parser and other components using the parser
 * may be cleaner.
 *
 *
 * @version 0.2
 * @author HT de Beer
 */
public class Attribute {

	/* Fields */

	/** Scope is denoted by an integer constant. */
	public final static int PI = 0;
	public final static int ATE = 1;

	/** The types */
	public final static int NUMBER = 2;
	public final static int STRING = 3;
	public final static int SET = 4;
	public final static int DATE = 5;
	public final static int CONCEPTSET = 6;

	/** Kind of definition */
	public final static int ATTRIBUTE = 6;
	//public final static int RENAMING  = 7;
	//public final static int ARGUMENT  = 8;
	//public final static int DUMMY     = 9;
	public final static int LITERAL = 10;

	/** The value of this attribute, either an identifier, an literal or an
	 * renaming. */
	String value;

	/**
	 * The attribute of this definition. If this is an attribute definition
	 * itself, this is equal the name of this. Else, in case it is an renaming or an
	 * argumentdefinition, it contains the attribute id it is about..
	 */
	String attributeId;

	/** The scope of the attribute, either PI or ATE. */
	int scope;

	/** The type of the attribute: CONCEPTSET, SET, DATE, STRING or NUMBER. */
	int type;

	/** The kind of the attribute, ATTRIBUTE, RENAMING, ARGUMENT, LITERAL or DUMMY. */
	int kind;

	/** The date parser, in case this definition is about an date. */
	SimpleDateFormat sdf;

	/* Constructor */

	public Attribute(String value, int scope, int type, int kind) {
		this.value = value;
		this.scope = scope;
		this.type = type;
		this.kind = kind;
		this.attributeId = value;
		this.sdf = new SimpleDateFormat();
	}

	public Attribute(String value, int kind, Attribute type) {
		this.value = value;
		this.scope = type.getScope();
		this.type = type.getType();
		this.kind = kind;
		this.attributeId = type.getAttributeId();
		if (type.isDate()) {
			this.sdf = type.getDateParser();
		} else {
			this.sdf = new SimpleDateFormat();
		}
		;
	}

	/* Methods */

	/** Get the value of this attribute.
	 *
	 * @return The value of this attribute.
	 */
	public String getValue() {
		return this.value;
	}

	/** Get the scope of this attribute.
	 *
	 * @return The scope of this attribute.
	 */
	public int getScope() {
		return this.scope;
	}

	/** Get the type of this attribute.
	 *
	 * @return The type of this attribute.
	 */
	public int getType() {
		return this.type;
	}

	/** Get the kind of this attributedefinition.
	 *
	 * @return The kind of this attribute.
	 */
	public int getKind() {
		return this.kind;
	}

	/** Get the attribute of this definition, if it is an attribute definitoni
	 * itself, return null.
	 *
	 * @return The attribute id of this attribute.
	 */
	public String getAttributeId() {
		return this.attributeId;
	}

	/** Set the dateparser in case this attribute is an date.
	 *
	 * @param sdf The dateparer.
	 */
	public void setDateParser(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}

	/** Get the dateparser of this attribute, that is, it must be an date to
	 * have one.
	 *
	 * @return The dateparser.
	 */
	public SimpleDateFormat getDateParser() {
		return this.sdf;
	}

	/** Is a date? */
	public boolean isDate() {
		return (this.type == DATE);
	}

	/** Is a number? */
	public boolean isNumber() {
		return (this.type == NUMBER);
	}

	/** Is a set? */
	public boolean isSet() {
		return (this.type == SET);
	}

	/** Is a set? */
	public boolean isConceptSet() {
		return (this.type == CONCEPTSET);
	}

	/** Is a string? */
	public boolean isString() {
		return (this.type == STRING);
	}

	/** Is an ate? */
	public boolean isAte() {
		return (this.scope == ATE);
	}

	/** Is a pi? */
	public boolean isPi() {
		return (this.scope == PI);
	}

	/** Is a attribute? */
	public boolean isAttribute() {
		return (this.kind == ATTRIBUTE);
	}

	/** Is a literal? */
	public boolean isLiteral() {
		return (this.kind == LITERAL);
	}

	public String toString() {
        return value;// + " : " + typeString();
    }

    public String toString(boolean verbose) {
		return verbose
            ? String.format("%s : %s (%s) kind=%s scope=%s sdf=%s",
                value,
                typeString(),
                getAttributeId(),
                isLiteral() ? "LITERAL" : "ATTRIBUTE",
                isPi() ? "PI" : "ATE",
                (sdf == null ? "null" : sdf.toPattern()))
            : toString();
	};

	private String typeString() {
		String result = "";
		switch (type) {
			case NUMBER:
				result = "number";
				break;
			case SET:
				result = "set";
				break;
			case STRING:
				result = "string";
				break;
			case DATE:
				result = "date";
				break;
			case CONCEPTSET:
				result = "conceptset";
				break;
		}
		;
		return result;
	}
	
	public String asParseableAttributeDefinition() {
		assert(isAttribute());
		String s = typeString() + " " + attributeId;
		
		if (isDate()) {
			s += " := \"" + sdf.toPattern() + "\"";
		}
		return s + ";";
	}
	
	public String asParseableRenaming() {
		assert(isAttribute());
		return "rename " + attributeId + " as " + value + ";";
	}

	public String asParseableArgument() {
		assert(isAttribute());
		return value + " : " + attributeId;
	}

	public String asParseableName() {
		return attributeId;
	}
	
	public String asParseableValue() {
		return value;
	}
	
	public String asParseableLiteralValue() {
		switch (type) {
			case NUMBER:
				return value;
			case SET:
			case STRING:
			case DATE:
				return "\"" + value + "\"";
			case CONCEPTSET:
				String result = " [ ";
				ConceptSetAttribute conceptSet = (ConceptSetAttribute) this;
				for (String uri : conceptSet.getModelReferences()) {
					result += "@" + uri + " ";
				}
				return result + "] ";
			default:
				assert(false);
				return "";
		}
	}
}

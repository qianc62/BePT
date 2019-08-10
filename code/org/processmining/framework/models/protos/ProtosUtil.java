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

package org.processmining.framework.models.protos;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos utilities
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
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosUtil {
	public ProtosUtil() {
	}

	/**
	 * Returns the contents of the given node.
	 * 
	 * @param node
	 *            Node The fiven node.
	 * @return String Its contents.
	 */
	static public String readString(Node node) {
		NodeList nodes = node.getChildNodes();
		String value = "";
		for (int i = 0; i < nodes.getLength(); i++) {
			value += nodes.item(i).getTextContent();
		}
		return value;
	}

	/**
	 * Returns the boolean value represented by the contents of the given node.
	 * 
	 * @param node
	 *            Node The given node.
	 * @return boolean Its boolean value.
	 */
	static public boolean readBool(Node node) {
		return readString(node).equals(ProtosString.True);
	}

	/**
	 * Returns the int value represented by the contents of the given node.
	 * 
	 * @param node
	 *            Node The given node.
	 * @return int Its int value.
	 */
	static public int readInt(Node node) {
		return Integer.parseInt(readString(node));
	}

	/**
	 * Returns the float value represented by the contents of the given node.
	 * 
	 * @param node
	 *            Node The given node.
	 * @return float Its float value.
	 */
	static public float readFloat(Node node) {
		return Float.parseFloat(readString(node));
	}

	/**
	 * Returns the double value represented by the contents of the given node.
	 * 
	 * @param node
	 *            Node The given node.
	 * @return double Its double value.
	 */
	static public double readDouble(Node node) {
		return Double.parseDouble(readString(node));
	}

	/**
	 * Tags (XML-style) the given value with the given tag.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            String The given value.
	 * @return String The tagged value.
	 */
	static public String writeString(String tag, String value) {
		String xml = "<" + tag + ">";
		if (value != null) {
			xml += value.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;");
		}
		xml += "</" + tag + ">";
		return xml;
	}

	/**
	 * Tags (XML-style) the given value with the given tag, if the given value
	 * is non-empty.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            String The given value.
	 * @return String The tagged value, if non-empty. Otherwise the empty
	 *         string..
	 */
	static public String writeStringIfNonEmpty(String tag, String value) {
		return value != null && value.length() > 0 ? writeString(tag, value)
				: "";
	}

	/**
	 * Tags (XML-style) the given value with the given tag.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            int The given value.
	 * @return String The tagged value.
	 */
	static public String writeInt(String tag, int value) {
		return "<" + tag + ">" + String.valueOf(value) + "</" + tag + ">";
	}

	/**
	 * Tags (XML-style) the given value with the given tag.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            boolean The given value.
	 * @return String The tagged value.
	 */
	static public String writeBool(String tag, boolean value) {
		return "<" + tag + ">"
				+ (value ? ProtosString.True : ProtosString.False) + "</" + tag
				+ ">";
	}

	/**
	 * Tags (XML-style) the given value with the given tag.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            float The given value.
	 * @return String The tagged value.
	 */
	static public String writeFloat(String tag, float value) {
		return "<" + tag + ">" + String.valueOf(value) + "</" + tag + ">";
	}

	/**
	 * Tags (XML-style) the given value with the given tag.
	 * 
	 * @param tag
	 *            String The given tag.
	 * @param value
	 *            double The given value.
	 * @return String The tagged value.
	 */
	static public String writeDouble(String tag, double value) {
		return "<" + tag + ">" + String.valueOf(value) + "</" + tag + ">";
	}

}

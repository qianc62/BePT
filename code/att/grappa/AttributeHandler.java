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

/**
 * An interface for methods that perform attribute value conversions.
 * 
 * @version 1.2, 07 Jul 2003; Copyright 1996 - 2003 by AT&T Corp.
 * @author <a href="mailto:john@research.att.com">John Mocenigo</a>, <a
 *         href="http://www.research.att.com">Research @ AT&T Labs</a>
 */
public interface AttributeHandler {
	/**
	 * Convert the supplied value to a string. How to convert the value is based
	 * on the type, name and attrtype information supplied. Note: this method
	 * really could be declared static except that it hides the instance method
	 * declared in the AttributeHandler interface and a class method cannot hide
	 * an instance method.
	 * 
	 * @param elemType
	 *            the element type to which the named attribute applies
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the object value to be converted to a string
	 * @param convType
	 *            the object-to-string conversion type of the value object
	 * @return a string representation of the supplied value
	 */
	public String convertValue(int elemType, String name, Object value,
			int convType);

	/**
	 * Convert the supplied string value to the appropriate Object. How to
	 * convert the value is based on the type, name and attrtype information
	 * supplied. Note: this method really could be declared static except that
	 * it hides the instance method declared in the AttributeHandler interface
	 * and a class method cannot hide an instance method.
	 * 
	 * @param elemType
	 *            the element type to which the named attribute applies
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the string value to be converted to an object
	 * @param convType
	 *            the string-to-object conversion type of the value object
	 * @return an object representation of the supplied value
	 */
	public Object convertStringValue(int elemType, String name,
			String stringValue, int convType);

	/**
	 * Make a copy of the supplied value. How to copy the value is based on the
	 * type, name and attrtype information supplied. Note: this method really
	 * could be declared static except that it hides the instance method
	 * declared in the AttributeHandler interface and a class method cannot hide
	 * an instance method.
	 * 
	 * @param elemType
	 *            the element type to which the named attribute applies
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the attribute value to be copied
	 * @param convType
	 *            the conversion type of the value object
	 * @return a copy of the supplied value
	 */
	public Object copyValue(int elemType, String name, Object value,
			int convType);
}

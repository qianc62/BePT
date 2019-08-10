/**
 * Project: ProM
 * File: XmlUtils.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jun 2, 2006, 3:23:31 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.processmining.framework.ui.Message;

/**
 * This class serves as a container for static XML-related manipulation and
 * parsing methods (in the context of MXML processing)
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class XmlUtils {

	/**
	 * Date/Time parsing format including milliseconds and time zone information
	 */
	protected static final String XSDATETIME_FORMAT_STRING_MILLIS_TZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	/**
	 * Date/Time parsing format including milliseconds and NO time zone
	 * information
	 */
	protected static final String XSDATETIME_FORMAT_STRING_MILLIS_NOTZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	/**
	 * Date/Time parsing format NOT including milliseconds but time zone
	 * information
	 */
	protected static final String XSDATETIME_FORMAT_STRING_NOMILLIS_TZONE = "yyyy-MM-dd'T'HH:mm:ssZ";
	/**
	 * Date/Time parsing format including NEITHER milliseconds NOR time zone
	 * information
	 */
	protected static final String XSDATETIME_FORMAT_STRING_NOMILLIS_NOTZONE = "yyyy-MM-dd'T'HH:mm:ss";
	/**
	 * Date/Time parsing instance with milliseconds and time zone information
	 */
	protected static final SimpleDateFormat DF_MILLIS_TZONE = new SimpleDateFormat(
			XmlUtils.XSDATETIME_FORMAT_STRING_MILLIS_TZONE);
	/**
	 * Date/Time parsing instance with milliseconds and NO time zone information
	 */
	protected static final SimpleDateFormat DF_MILLIS_NOTZONE = new SimpleDateFormat(
			XmlUtils.XSDATETIME_FORMAT_STRING_MILLIS_NOTZONE);
	/**
	 * Date/Time parsing instance with NO milliseconds but time zone information
	 */
	protected static final SimpleDateFormat DF_NOMILLIS_TZONE = new SimpleDateFormat(
			XmlUtils.XSDATETIME_FORMAT_STRING_NOMILLIS_TZONE);
	/**
	 * Date/Time parsing instance with NEITHER milliseconds NOR time zone
	 * information
	 */
	protected static final SimpleDateFormat DF_NOMILLIS_NOTZONE = new SimpleDateFormat(
			XmlUtils.XSDATETIME_FORMAT_STRING_NOMILLIS_NOTZONE);

	/**
	 * Expects an XML xs:dateTime lexical format string, as in
	 * <code>2005-10-24T11:57:31.000+01:00</code>. Some bad MXML files miss
	 * timezone or milliseconds information, thus a certain amount of tolerance
	 * is applied towards malformed timestamp string representations. If
	 * unparseable, this method will return <code>null</code>.
	 * <p>
	 * <b>OLD IMPLEMENTATION - use newer one preferably (3x+ performance
	 * boost)</b>
	 * 
	 * @param xmlLexicalString
	 * @return
	 */
	public static Date parseXsDateTimeOld(String xsDateTime) {
		Date result = null;
		xsDateTime = xsDateTime.trim();
		if (xsDateTime.length() < XmlUtils.XSDATETIME_FORMAT_STRING_NOMILLIS_NOTZONE
				.length()) {
			// unable to parse timestamp from this string - too short!
			return null;
		}
		if (xsDateTime.charAt(xsDateTime.length() - 6) == '+') {
			// time zone information present; fix colon mismatch
			int colonPos = xsDateTime.lastIndexOf(':');
			xsDateTime = xsDateTime.substring(0, colonPos)
					+ xsDateTime.substring(colonPos + 1);
			try {
				// try to parse including millisecond information
				result = XmlUtils.DF_MILLIS_TZONE.parse(xsDateTime);
			} catch (Exception e) {
				try {
					// resort to parsing without millisecond information
					result = XmlUtils.DF_NOMILLIS_TZONE.parse(xsDateTime);
				} catch (Exception e2) {
					// give up
					result = null;
				}
			}
		} else {
			// no time zone information present
			try {
				// try to parse including millisecond information
				result = XmlUtils.DF_MILLIS_NOTZONE.parse(xsDateTime);
			} catch (Exception e) {
				try {
					// resort to parsing without millisecond information
					result = XmlUtils.DF_NOMILLIS_NOTZONE.parse(xsDateTime);
				} catch (Exception e2) {
					// give up
					result = null;
				}
			}
		}
		if (result == null) {
			// notify of errors
			System.err.println("ERROR in XmlUtils: xs:dateTime parsing fault ("
					+ xsDateTime + ")!");
			Message.add("XmlUtils: xs:dateTime parsing fault (" + xsDateTime
					+ ")!", Message.ERROR);
		}
		return result;
	}

	/**
	 * Pattern used for matching the XsDateTime formatted timestamp strings
	 */
	protected static final Pattern xsDtPattern = Pattern
			.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?(.+)?");

	/**
	 * Calendar instance used for calculating dates for timestamps
	 */
	protected static GregorianCalendar cal = new GregorianCalendar();

	/**
	 * Expects an XML xs:dateTime lexical format string, as in
	 * <code>2005-10-24T11:57:31.000+01:00</code>. Some bad MXML files miss
	 * timezone or milliseconds information, thus a certain amount of tolerance
	 * is applied towards malformed timestamp string representations. If
	 * unparseable, this method will return <code>null</code>.
	 * 
	 * @param xmlLexicalString
	 * @return
	 */
	public static Date parseXsDateTime(String xsDateTime) {
		// match pattern against timestamp string
		Matcher matcher = xsDtPattern.matcher(xsDateTime);
		if (matcher.matches() == true) {
			// extract data particles from matched groups / subsequences
			int year = Integer.parseInt(matcher.group(1));
			int month = Integer.parseInt(matcher.group(2)) - 1;
			int day = Integer.parseInt(matcher.group(3));
			int hour = Integer.parseInt(matcher.group(4));
			int minute = Integer.parseInt(matcher.group(5));
			int second = Integer.parseInt(matcher.group(6));
			int millis = 0;
			// probe for successful parsing of milliseconds
			if (matcher.group(7) != null) {
				millis = Integer.parseInt(matcher.group(8));
			}
			cal.set(year, month, day, hour, minute, second);
			cal.set(GregorianCalendar.MILLISECOND, millis);
			String tzString = matcher.group(9);
			if (tzString != null) {
				// timezone matched
				tzString = "GMT" + tzString.replace(":", "");
				cal.setTimeZone(TimeZone.getTimeZone(tzString));
			} else {
				cal.setTimeZone(TimeZone.getTimeZone("GMT"));
			}
			return cal.getTime();
		} else {
			return null;
		}
	}

}

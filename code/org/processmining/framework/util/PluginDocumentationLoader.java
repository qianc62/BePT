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

package org.processmining.framework.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.processmining.framework.plugin.Plugin;

/**
 * <p>
 * Title: PluginDocumentationLoader
 * </p>
 * 
 * <p>
 * Description: Static class to load documentation from external file in
 * /lib/documentation/ subfolder
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * @author Boudewijn van Dongen
 * @author Christian W. Guenther (christian@deckfour.org)
 * @version 1.5
 */
public class PluginDocumentationLoader {

	/**
	 * Regular expression matching the body of an HTML document in group 4
	 */
	protected static final String HTML_BODY_REGEX = "\\A(.*)<(body|Body|BODY)(.*?)>(.+)</(body|Body|BODY)(.*)\\Z$";

	/**
	 * Call load with a reference to the calling plugin to load the appropriate
	 * HTML body from a file with the name p.getName()+".html"
	 * 
	 * @param p
	 *            Plugin to load HTML body for
	 * @return String the HTML body, or null if the file is not found or read
	 *         errors occur
	 */
	public static String load(Plugin p) {
		return load(p.getClass().getName());
	}

	public static String load(String className) {
		String filename = System.getProperty("user.dir", "") + File.separator
				+ "lib" + File.separator + "documentation" + File.separator
				+ className.substring(className.lastIndexOf(".") + 1)
				+ File.separator + className + ".html";
		StringBuffer htmlBuffer = new StringBuffer();
		String html = null;
		try {
			BufferedReader is = new BufferedReader(new FileReader(filename));
			String line = is.readLine();
			while (line != null) {
				htmlBuffer.append(line);
				line = is.readLine();
			}
			is.close();
			html = htmlBuffer.toString();
			// remove everything but the body (if corresponding tags found)
			if (html.matches(PluginDocumentationLoader.HTML_BODY_REGEX)) {
				html = html.replaceAll(
						PluginDocumentationLoader.HTML_BODY_REGEX, "$4");
			}
		} catch (FileNotFoundException ex) {
			// No file and no description
			return null;
		} catch (IOException ex) {
			// No file and no description
			return null;
		}
		return html;
	}
}

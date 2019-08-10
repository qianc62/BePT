/**
 * Project: ProM
 * File: RuntimeUtils.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 7, 2006, 5:26:34 PM
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
package org.processmining.framework.util;

import att.grappa.Grappa;

/**
 * @author christian
 * 
 */
public class RuntimeUtils {

	static {
		// smooth like that...
		Grappa.useAntiAliasing = true;
		Grappa.antiAliasText = true;
	}

	public static final String OS_WIN32 = "Windows";
	public static final String OS_MACOSX = "Mac OS X";
	public static final String OS_MACOSCLASSIC = "Mac OS 7-9";
	public static final String OS_LINUX = "Linux";
	public static final String OS_BSD = "BSD";
	public static final String OS_RISCOS = "RISC OS";
	public static final String OS_BEOS = "BeOS";
	public static final String OS_UNKNOWN = "unknown";

	public static String currentOs = null;

	public static String determineOS() {
		if (currentOs == null) {
			String osString = System.getProperty("os.name").trim()
					.toLowerCase();
			if (osString.startsWith("windows")) {
				currentOs = RuntimeUtils.OS_WIN32;
			} else if (osString.startsWith("mac os x")) {
				currentOs = RuntimeUtils.OS_MACOSX;
			} else if (osString.startsWith("mac os")) {
				currentOs = RuntimeUtils.OS_MACOSCLASSIC;
			} else if (osString.startsWith("risc os")) {
				currentOs = RuntimeUtils.OS_RISCOS;
			} else if ((osString.indexOf("linux") > -1)
					|| (osString.indexOf("debian") > -1)
					|| (osString.indexOf("redhat") > -1)
					|| (osString.indexOf("lindows") > -1)) {
				currentOs = RuntimeUtils.OS_LINUX;
			} else if ((osString.indexOf("freebsd") > -1)
					|| (osString.indexOf("openbsd") > -1)
					|| (osString.indexOf("netbsd") > -1)
					|| (osString.indexOf("irix") > -1)
					|| (osString.indexOf("solaris") > -1)
					|| (osString.indexOf("sunos") > -1)
					|| (osString.indexOf("hp/ux") > -1)
					|| (osString.indexOf("risc ix") > -1)
					|| (osString.indexOf("dg/ux") > -1)) {
				currentOs = RuntimeUtils.OS_BSD;
			} else if (osString.indexOf("beos") > -1) {
				currentOs = RuntimeUtils.OS_BEOS;
			} else {
				currentOs = RuntimeUtils.OS_UNKNOWN;
			}
		}
		return currentOs;
	}

	public static boolean isRunningWindows() {
		return (RuntimeUtils.determineOS() == RuntimeUtils.OS_WIN32);
	}

	public static boolean isRunningMacOsX() {
		return (RuntimeUtils.determineOS() == RuntimeUtils.OS_MACOSX);
	}

	public static boolean isRunningLinux() {
		return (RuntimeUtils.determineOS() == RuntimeUtils.OS_LINUX);
	}

	public static boolean isRunningUnix() {
		String os = RuntimeUtils.determineOS();
		if ((os == RuntimeUtils.OS_BSD) || (os == RuntimeUtils.OS_LINUX)
				|| (os == RuntimeUtils.OS_MACOSX)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the runtime environment is detected to be Mac OS X, this method will
	 * strip the parameter string of all HTML tags and return the result. On all
	 * other platforms, the parameter string is returned unchanged.
	 * 
	 * @param menuText
	 * @return
	 */
	public static String stripHtmlForOsx(String menuText) {
		if (isRunningMacOsX() == true) {
			menuText = stripHtml(menuText);
		}
		return menuText;
	}

	/**
	 * This method will strip the parameter string of all HTML tags and return
	 * the result.
	 * 
	 * @param text
	 * @return
	 */
	public static String stripHtml(String text) {
		return text.replaceAll("<[^<>]*>", "");
	}
}

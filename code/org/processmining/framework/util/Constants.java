package org.processmining.framework.util;

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
public class Constants {
	private Constants() {
	}

	public static String BVD_HTTP_PREFIX = "http://ga1717.tm.tue.nl/user/boudewijn/";

	public static String get_BVD_URLString(String filename, String text) {
		return "<a href=\""
				+ org.processmining.framework.util.Constants.BVD_HTTP_PREFIX
				+ filename + "/\">" + text + "</a>";
	}
}

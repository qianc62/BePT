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
public class StringNormalizer {
	private StringNormalizer() {
	}

	public static String normalize(String in) {
		char[] s = toCharArray(in);
		String str = new String(s);
		str = str.trim();
		str = str.toLowerCase();
		str = str.replaceAll("\\\\n", " ");
		str = str.replaceAll("\\\\r", " ");
		str = str.replaceAll("\\\\t", " ");
		str = str.replaceAll(",|:|/|\\[|\\]|\\(|\\)", "");
		str = str.replaceAll("-", " ");
		return str;
	}

	private static char[] toCharArray(String in) {
		char[] s = new char[in.length()];

		int j = 0;
		char last = ' ';
		for (int i = 0; i < in.length(); i++) {
			char current = in.charAt(i);
			if (!(current == '\\' && current == last)) {
				s[j++] = in.charAt(i);
			}
			last = current;
		}
		return s;
	}

	public static String escapeXMLCharacters(String in) {
		char[] s = toCharArray(in);

		String result = "";
		for (int i = 0; i < in.length(); i++) {
			switch (s[i]) {
			case '<':
				result += "&lt;";
				break;
			case '>':
				result += "&gt;";
				break;
			case '&':
				result += "&amp;";
				break;
			case '\'':
				result += "&apos;";
				break;
			case '\"':
				result += "&quot;";
				break;
			default:
				result += String.valueOf(s[i]);

			}
		}
		return result;
	}

}

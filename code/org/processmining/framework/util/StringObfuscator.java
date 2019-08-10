/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.framework.util;

import java.util.Random;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StringObfuscator {

	protected static Random random = new Random();

	protected static final char[] CHARTABLE = new char[] { 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9'
	/*
	 * ,'\'', '"', '/', '\\', '.', ',', ':', ';', '-', '_', '|', '?', '!', '(',
	 * ')', ']', '[', '}', '{', '<', '>', '@', '#', '$', '%', '^', '&', '*',
	 * '+', '=', '~', '`'
	 */
	};

	protected char[] charmap;

	public StringObfuscator() {
		initialize();
	}

	public void initialize() {
		// initialize character map with identity mapping
		charmap = new char[CHARTABLE.length];
		for (int i = 0; i < charmap.length; i++) {
			charmap[i] = CHARTABLE[i];
		}
		// shuffle character map
		for (int i = 0; i < charmap.length; i++) {
			int r = i + random.nextInt(charmap.length - i);
			// swap
			char swap = charmap[r];
			charmap[r] = charmap[i];
			charmap[i] = swap;
		}
	}

	protected char map(char original) {
		for (int i = 0; i < CHARTABLE.length; i++) {
			if (CHARTABLE[i] == original) {
				return charmap[i];
			}
		}
		return '-';
	}

	public String obfuscate(String original) {
		if (original == null) {
			return null;
		}
		original = original.trim();
		if (original.length() == 0) {
			return original;
		}
		char[] strArr = original.toCharArray();
		for (int i = 0; i < strArr.length; i++) {
			strArr[i] = map(strArr[i]);
		}
		return new String(strArr);
	}

}

/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 11, 2006 8:41:07 PM
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

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * This class provides handy utilities for text-output-based tests.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class TestUtils {

	/**
	 * Returns a hash string for the given matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public static String hash(DoubleMatrix1D matrix) {
		String hash = Integer.toHexString(matrix.size()) + ".";
		long hashVal = 0;
		for (int i = 0; i < matrix.size(); i++) {
			hashVal += matrix.get(i);
		}
		hash += Long.toHexString(hashVal);
		return hash;
	}

	/**
	 * Returns a hash string for the given matrix
	 * 
	 * @param matrix
	 * @return
	 */
	public static String hash(DoubleMatrix2D matrix) {
		String hash = Integer.toHexString(matrix.columns()) + "."
				+ Integer.toHexString(matrix.rows()) + ".";
		long hashVal = 0;
		for (int x = 0; x < matrix.columns(); x++) {
			for (int y = 0; y < matrix.rows(); y++) {
				hashVal += matrix.get(x, y);
			}
		}
		hash += Long.toHexString(hashVal);
		return hash;
	}
}

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
package org.processmining.framework.models.recommendation.net;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.processmining.lib.mxml.LogException;
import org.processmining.lib.mxml.writing.persistency.LogPersistency;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StringLogPersistency extends LogPersistency {

	protected ByteArrayOutputStream outStream = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.lib.mxml.writing.persistency.LogPersistency#closeFile()
	 */
	@Override
	protected void closeFile() throws LogException {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.lib.mxml.writing.persistency.LogPersistency#createFile
	 * (java.lang.String)
	 */
	@Override
	protected OutputStream createFile(String arg0) throws LogException {
		outStream = new ByteArrayOutputStream();
		return outStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.lib.mxml.writing.persistency.LogPersistency#finish()
	 */
	@Override
	public void finish() throws LogException {
		// ignore
	}

	public String getOutputString() {
		return outStream.toString();
	}

}

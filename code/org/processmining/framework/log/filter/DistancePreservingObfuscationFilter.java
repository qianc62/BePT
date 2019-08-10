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
package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.StringObfuscator;
import org.w3c.dom.Node;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DistancePreservingObfuscationFilter extends LogFilter {

	protected StringObfuscator obfuscator;
	protected long timeOffset;

	/**
	 * @param load
	 * @param name
	 */
	public DistancePreservingObfuscationFilter() {
		super(LogFilter.MODERATE, "Distance-preserving obfuscation");
		obfuscator = new StringObfuscator();
		timeOffset = (long) ((new Random()).nextDouble() * (double) 315360000000l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#doFiltering(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	@Override
	protected boolean doFiltering(ProcessInstance instance) {
		instance.setDataAttributes(obfuscate(instance.getDataAttributes()));
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		AuditTrailEntry ate;
		for (int i = 0; i < ateList.size(); i++) {
			try {
				ate = ateList.get(i);
				ate.setDataAttributes(obfuscate(ate.getDataAttributes()));
				ate.setElement(obfuscator.obfuscate(ate.getElement()));
				ate.setOriginator(obfuscator.obfuscate(ate.getOriginator()));
				if (ate.getTimestamp() != null) {
					ate.setTimestamp(new Date(ate.getTimestamp().getTime()
							+ timeOffset));
				}
				ateList.replace(ate, i);
			} catch (IOException e) {
				// major fuckup in IO
				e.printStackTrace();
			}
		}
		return true;
	}

	protected DataSection obfuscate(DataSection data) {
		DataSection obfuscated = new DataSection();
		for (String key : data.keySet()) {
			obfuscated.put(obfuscator.obfuscate(key), obfuscator.obfuscate(data
					.get(key)));
		}
		return obfuscated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#getHelpForThisLogFilter()
	 */
	@Override
	protected String getHelpForThisLogFilter() {
		return "Obfuscates a log while preserving the relative string distance between tokens";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#getParameterDialog(org.
	 * processmining.framework.log.LogSummary)
	 */
	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		// no parameters
		// no dialog here
		return new LogFilterParameterDialog(summary, this) {
			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new DistancePreservingObfuscationFilter();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#thisFilterChangesLog()
	 */
	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#readSpecificXML(org.w3c.dom
	 * .Node)
	 */
	@Override
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// not applicable
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#writeSpecificXML(java.io.
	 * BufferedWriter)
	 */
	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// not applicable
	}

}

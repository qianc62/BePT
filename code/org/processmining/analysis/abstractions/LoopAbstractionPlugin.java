/*
 * Copyright (c) 2009 R. P. Jagadeesh Chandra Bose (j.c.b.rantham.prabhakara@tue.nl)
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
//Om Ganesayanamaha
package org.processmining.analysis.abstractions;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.abstractions.ui.LoopAbstractionUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;

public class LoopAbstractionPlugin implements AnalysisPlugin {

	protected LoopAbstractionUI ui;

	public LoopAbstractionPlugin() {

	}

	public String getName() {
		return "Loop Abstractions";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		// look for LogReader instance to open GUI
		Object[] o = (inputs[0].getProvidedObjects())[0].getObjects();
		LogReader log = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			}
		}
		ui = new LoopAbstractionUI(log);
		return ui;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Low-level Log") {
			public boolean accepts(ProvidedObject object) {
				for (Object obj : object.getObjects()) {
					if (obj instanceof LogReader) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}

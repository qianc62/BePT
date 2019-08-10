/*
 * Copyright (c) 2008 Minseok Song
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

package org.processmining.analysis.orgsimilarity;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.orgsimilarity.ui.SimilarityUI;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.orgmodel.OrgModel;

/**
 * @author Minseok Song
 * 
 */
public class SimilarityPlugin implements AnalysisPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		return new SimilarityUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		// needs any instance of LogReader to work
		AnalysisInputItem[] items = { new AnalysisInputItem("Org Model") {
			public boolean accepts(ProvidedObject object) {
				for (Object obj : object.getObjects()) {
					if (obj instanceof OrgModel) {
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Organizational Model Similarity";
	}

}

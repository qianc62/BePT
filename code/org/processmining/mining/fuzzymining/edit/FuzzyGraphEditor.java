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
package org.processmining.mining.fuzzymining.edit;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyGraphEditor extends JPanel implements Provider,
		AnalysisPlugin {

	private static final long serialVersionUID = 923826429972114173L;

	protected MutableFuzzyGraph originalGraph = null;
	protected MutableFuzzyGraph modifiedGraph = null;

	public FuzzyGraphEditor(MutableFuzzyGraph originalGraph) {
		this.originalGraph = originalGraph;
		if (originalGraph != null) {
			this.modifiedGraph = (MutableFuzzyGraph) originalGraph.clone();
		} else {
			originalGraph = null;
		}
		// setup GUI
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		if (modifiedGraph != null) {
			this.add(new FuzzyGraphEditorPanel(modifiedGraph),
					BorderLayout.CENTER);
		}
	}

	public FuzzyGraphEditor() {
		this(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ArrayList<ProvidedObject> provided = new ArrayList<ProvidedObject>();
		if (modifiedGraph != null) {
			provided.add(new ProvidedObject("Modified Fuzzy Graph",
					new Object[] { modifiedGraph }));
		}
		if (originalGraph != null) {
			provided.add(new ProvidedObject("Original Fuzzy Graph",
					new Object[] { originalGraph }));
		}
		return provided.toArray(new ProvidedObject[provided.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		for (AnalysisInputItem item : inputs) {
			for (ProvidedObject po : item.getProvidedObjects()) {
				for (Object o : po.getObjects()) {
					if (o instanceof MutableFuzzyGraph) {
						originalGraph = (MutableFuzzyGraph) o;
						modifiedGraph = (MutableFuzzyGraph) originalGraph
								.clone();
						break;
					}
				}
				if (modifiedGraph != null) {
					break;
				}
			}
		}
		this.removeAll();
		this.add(new FuzzyGraphEditorPanel(modifiedGraph), BorderLayout.CENTER);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		return new AnalysisInputItem[] { new AnalysisInputItem("Fuzzy Graph") {
			public boolean accepts(ProvidedObject object) {
				for (Object o : object.getObjects()) {
					if (o instanceof MutableFuzzyGraph) {
						return true;
					}
				}
				return false;
			}
		} };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Edits a Fuzzy Model";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Model Editor";
	}

}

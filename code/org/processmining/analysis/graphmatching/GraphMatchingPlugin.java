package org.processmining.analysis.graphmatching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.graphmatching.algos.DistanceAlgoAbstr;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceAStarSim;
import org.processmining.analysis.graphmatching.graph.SimpleGraph;
import org.processmining.analysis.graphmatching.graph.TwoVertices;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.StringNormalizer;

public class GraphMatchingPlugin implements AnalysisPlugin {

	public GraphMatchingPlugin() {
	}

	public String getName() {
		return ("Graph Matching Analysis");
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem base = new AnalysisInputItem("Search Models", 1, 1000) {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof ConfigurableEPC) {
						return true;
					}
					if (o[i] instanceof EPCHierarchy) {
						return true;
					}
				}
				return false;
			}
		};
		AnalysisInputItem comp = new AnalysisInputItem("Document Models", 1,
				1000) {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof ConfigurableEPC) {
						return true;
					}
					if (o[i] instanceof EPCHierarchy) {
						return true;
					}
				}
				return false;
			}
		};

		return new AnalysisInputItem[] { base, comp };
	}

	public JComponent analyse(AnalysisInputItem[] inputs) {
		GraphMatchingResults ui = null;

		GraphMatchingOptionsDialog optionsDialog = new GraphMatchingOptionsDialog();
		DistanceAlgoAbstr algo = optionsDialog.showDialog();

		if (algo != null) {
			// Build search graphs
			AnalysisInputItem searchModelInput = inputs[0];
			ArrayList<ConfigurableEPC> searchModels = new ArrayList<ConfigurableEPC>();
			buildList(searchModelInput, searchModels);
			List<String> searchModelNames = new ArrayList<String>(searchModels
					.size());
			List<SimpleGraph> searchGraphs = new ArrayList<SimpleGraph>(
					searchModels.size());
			for (ConfigurableEPC searchModel : searchModels) {
				SimpleGraph searchGraph = new SimpleGraph(searchModel);
				searchGraph = searchGraph.removeVertices(searchGraph
						.getConnectorVertices());
				if (!algo.useEvents()) {
					searchGraph = searchGraph.removeVertices(searchGraph
							.getEventVertices());
				}
				searchGraphs.add(searchGraph);
				searchModelNames.add(searchModel.getIdentifier());
			}

			// Build doc graphs
			AnalysisInputItem docModelInput = inputs[1];
			ArrayList<ConfigurableEPC> docModels = new ArrayList<ConfigurableEPC>();
			buildList(docModelInput, docModels);
			List<String> docModelNames = new ArrayList<String>(docModels.size());
			List<SimpleGraph> docGraphs = new ArrayList<SimpleGraph>(docModels
					.size());
			for (ConfigurableEPC docModel : docModels) {
				SimpleGraph docGraph = new SimpleGraph(docModel);
				docGraph = docGraph.removeVertices(docGraph
						.getConnectorVertices());
				if (!algo.useEvents()) {
					docGraph = docGraph.removeVertices(docGraph
							.getEventVertices());
				}
				docGraphs.add(docGraph);
				docModelNames.add(docModel.getIdentifier());
			}

			double similarities[][] = new double[docModels.size()][searchModels
					.size()];

			Progress progress = new Progress("Computing similarity", 0,
					searchModels.size() * docModels.size());
			progress.setProgress(0);

			int colIndex = 0;
			for (SimpleGraph searchGraph : searchGraphs) {
				int rowIndex = 0;
				for (SimpleGraph docGraph : docGraphs) {
					if (algo.useEvents()
							&& (algo instanceof GraphEditDistanceAStarSim)) {
						((GraphEditDistanceAStarSim) algo).setPartitions(
								searchGraph.getFunctionVertices(), docGraph
										.getFunctionVertices());
					}

					similarities[rowIndex][colIndex] = 1.0 - algo.compute(
							searchGraph, docGraph);
					progress.inc();
					if (progress.isCanceled()) {
						return null;
					}
					rowIndex++;
				}
				colIndex++;
			}
			String matches = "";
			if ((searchGraphs.size() > 1) || (docGraphs.size() > 1)) {
				matches = "Matches are only computed if there is a single search model and a single document model.";
			} else {
				for (TwoVertices e : algo.bestMapping()) {
					matches += "(\"" + searchGraphs.get(0).getLabel(e.v1)
							+ "\",\"" + docGraphs.get(0).getLabel(e.v2)
							+ "\")\n";
				}
			}
			ui = new GraphMatchingResults(searchModelNames, docModelNames,
					similarities, matches);
		} else {
			Message.add("Cancelled by user.");
		}

		return ui;
	}

	public String getHtmlDescription() {
		return "Computes the similarity of a collection of document models with a collection of search models.";
	}

	private String getPath(ModelGraph epc, EPCHierarchy hierarchy) {
		String s = epc.getIdentifier();
		if (hierarchy != null && hierarchy.getParent(epc) != null) {
			s = getPath(hierarchy.getParent(epc), hierarchy) + "." + s;
		}
		return s;
	}

	private String getPath(Object o, EPCHierarchy hierarchy) {
		String s = o.toString();
		if (hierarchy.getParent(o) != null) {
			return getPath(hierarchy.getParent(o), hierarchy) + "." + s;
		} else {
			return s;
		}
	}

	private void addConditionalToList(ConfigurableEPC epc,
			ArrayList<ConfigurableEPC> epcs) {
		boolean startEvent = false;
		boolean endEvent = false;
		Iterator<EPCEvent> it = epc.getEvents().iterator();
		while (!(startEvent && endEvent) && it.hasNext()) {
			EPCEvent e = it.next();
			startEvent |= e.inDegree() == 0;
			endEvent |= e.outDegree() == 0;
		}
		if (epc.getFunctions().size() > 0 && startEvent && endEvent
				&& epc.isValidEPC().length() == 0) {
			epcs.add(epc);
			// paths.add(label);
		} else {
			return;
		}
	}

	private void buildList(AnalysisInputItem item,
			ArrayList<ConfigurableEPC> list) {
		for (ProvidedObject obj : item.getProvidedObjects()) {
			for (Object o : obj.getObjects()) {
				if (o instanceof ConfigurableEPC) {
					((ConfigurableEPC) o).object = StringNormalizer
							.normalize(getPath((ConfigurableEPC) o, null));
					addConditionalToList((ConfigurableEPC) o, list);
				} else if (o instanceof EPCHierarchy) {
					EPCHierarchy hierarchy = (EPCHierarchy) o;
					for (Object o2 : hierarchy.getAllObjects()) {
						if (o2 instanceof ConfigurableEPC) {
							((ConfigurableEPC) o2).object = StringNormalizer
									.normalize(getPath((ConfigurableEPC) o2,
											hierarchy));
							addConditionalToList((ConfigurableEPC) o2, list);
						}
					}
				}
			}
		}
	}
}
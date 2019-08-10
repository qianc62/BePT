package org.processmining.converting;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.instancemining.ModelGraphResult;

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
public class TransitiveReduction implements ConvertingPlugin {
	public TransitiveReduction() {
	}

	public String getName() {
		return "Transitive Reduction";
	}

	public String getHtmlDescription() {
		return "This plugin calculates the transitive reduction of a graph. It only works if the graph is a "
				+ "\"simple directed graph\", i.e. a graph that has at most one edge between two nodes and no loops.";
	}

	public MiningResult convert(ProvidedObject original) {
		int i = 0;
		ModelGraph g = null;
		while ((g == null) && (i < original.getObjects().length)) {
			if (original.getObjects()[i] instanceof ModelGraph) {
				g = (ModelGraph) original.getObjects()[i];
			}
			i++;
		}
		Message.add("<TransitiveReduction>", Message.TEST);
		g.Test("inputGraph");
		g.reduceTransitively();
		g.Test("outputGraph");
		Message.add("</TransitiveReduction>", Message.TEST);
		return new ModelGraphResult(g);
	}

	public boolean accepts(ProvidedObject original) {
		int i = 0;
		boolean b = false;
		while (!b && (i < original.getObjects().length)) {
			b |= (original.getObjects()[i] instanceof ModelGraph);
			i++;
		}
		return b;
	}
}

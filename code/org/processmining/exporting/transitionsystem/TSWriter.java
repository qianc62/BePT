package org.processmining.exporting.transitionsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.transitionsystem.PetrifyConstants;
import org.processmining.framework.models.transitionsystem.TransitionSystem;
import org.processmining.framework.models.transitionsystem.TransitionSystemEdge;

public class TSWriter {
	public static void writeToPetrify(TransitionSystem ts, Writer bw)
			throws IOException {
		File temp = new File("_temp");
		BufferedWriter tw = new BufferedWriter(new FileWriter(temp));
		BufferedReader tr = new BufferedReader(new FileReader(temp));

		// Eric: Keep track of sources and destination to determine which is the
		// start node (should be source but not destination)
		HashSet<ModelGraphVertex> sources = new HashSet<ModelGraphVertex>();
		HashSet<ModelGraphVertex> dests = new HashSet<ModelGraphVertex>();

		ArrayList transitions = ts.getEdges();
		HashSet<String> events = new HashSet<String>();

		for (int i = 0; i < transitions.size(); i++) {
			TransitionSystemEdge transition = (TransitionSystemEdge) transitions
					.get(i);
			events.add(replaceBadSymbols(transition.getIdentifier()));

			// Eric: add source and destination.
			sources.add(transition.getSource());
			dests.add(transition.getDest());

			if (ts.getStateNameFlag() == TransitionSystem.ID) {
				tw.write("s" + transition.getSource().getId() + " ");
				tw.write(replaceBadSymbols(transition.getIdentifier()) + " ");
				tw.write("s" + transition.getDest().getId() + "\n");
			} else {
				tw.write(replaceBadSymbols(transition.getSource()
						.getIdentifier())
						+ " ");
				tw.write(replaceBadSymbols(transition.getIdentifier()) + " ");
				tw
						.write(replaceBadSymbols(transition.getDest()
								.getIdentifier())
								+ "\n");
			}
		}
		tw.close();

		bw.write(".model " + ts.getName().replaceAll(" ", "_") + "\n");
		bw.write(".dummy ");
		Iterator it = events.iterator();
		while (it.hasNext())
			bw.write(it.next() + " ");
		bw.write("\n");
		bw.write(".state graph" + "\n");

		// Eric: Odd construct: Write to temp file, read from temp file, write
		// again.
		int c;
		while ((c = tr.read()) != -1)
			bw.write(c);
		tr.close();
		temp.delete();

		// Eric: Remove all sources that are destinations as well. Should leave
		// us the start node.
		for (ModelGraphVertex dest : dests) {
			if (sources.contains(dest)) {
				sources.remove(dest);
			}
		}
		ModelGraphVertex source = sources.isEmpty() ? null : sources.iterator()
				.next();

		if (ts.getStateNameFlag() == TransitionSystem.ID) {
			if (!ts.hasExplicitEnd())
				bw.write(".marking {s0}" + "\n");
			else
				bw.write(".marking {s" + source.getId() + "}\n");
		} else if (source != null) {
			bw.write(".marking {" + replaceBadSymbols(source.getIdentifier())
					+ "}\n");
		}
		bw.write(".end");
	}

	public static String replaceBadSymbols(String st) {
		// Eric. Remove everything from the \n. Petrify deos not like
		// backslashes.
		int newline = st.indexOf("\\n");
		if (newline != -1) {
			st = st.substring(0, newline);
		}

		// String s1 = PetrifyConstants.BadSymbolsMap.get(" ");
		Set keys = PetrifyConstants.BadSymbolsMap.keySet();
		Iterator it = keys.iterator();

		while (it.hasNext()) {
			String aKey = (String) it.next();
			st = st.replace(aKey, PetrifyConstants.BadSymbolsMap.get(aKey));
		}
		return st;
	}

}

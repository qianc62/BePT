package org.processmining.exporting.fsm;

import org.processmining.exporting.Exporter;
import org.processmining.framework.models.fsm.FSM;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.FSMState;
import java.util.ArrayList;
import org.processmining.framework.models.fsm.FSMTransition;

/**
 * <p>
 * Title: FSMExport
 * </p>
 * 
 * <p>
 * Description: Export an FSM to an FSM file using as less binary attributes as
 * possible.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FSMExport {

	@Exporter(name = "Binary-attributed FSM file", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:fsm2bfsm", extension = "fsm")
	public static void FSMExport(FSM fsm, OutputStream out) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		int i, m = 1, n = 0, c = fsm.getVerticeList().size();
		while (m < c) {
			m *= 2;
			n++;
		}
		for (i = 0; i < n; i++) {
			bw.write("attribute" + i + "(3) binary \"0\" \"1\" \"attribute" + i
					+ "\"\n");
		}
		bw.write("---\n");
		FSMExportStates(fsm, bw, m, n);
		bw.write("---\n");
		FSMExportTransitions(fsm, bw);
		bw.close();
	}

	public static void FSMExportStates(FSM fsm, BufferedWriter bw, int m, int n)
			throws IOException {
		int k = 1;
		for (ModelGraphVertex vertex : fsm.getVerticeList()) {
			int j = k, l = m;
			FSMState state = (FSMState) vertex;
			state.setIdentifier(((Integer) k).toString());
			for (int i = 0; i < n; i++) {
				l /= 2;
				if (j >= l) {
					bw.write("1 ");
					j -= l;
				} else {
					bw.write("0 ");
				}
			}
			bw.write("\n");
			k++;
		}
	}

	public static void FSMExportTransitions(FSM fsm, BufferedWriter bw)
			throws IOException {
		for (Object object : fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			bw.write(((FSMState) transition.getTail()).getIdentifier() + " ");
			bw.write(((FSMState) transition.getHead()).getIdentifier() + " ");
			bw.write(transition.getCondition() + "\n");
		}
	}
}

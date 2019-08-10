/**
 * Project: ProM HPLR
 * File: ProcessInstanceVisualization.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Apr 26, 2006, 7:26:49 PM
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
package org.processmining.framework.log;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import java.util.HashSet;

/**
 * This class provides Dot and Grappa visualization for a process instance.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class ProcessInstanceVisualization implements DotFileWriter {

	/**
	 * The wrapped process instance
	 */
	protected ProcessInstance instance = null;

	/**
	 * Creates a new visualization wrapper for the given process instance
	 * 
	 * @param anInstance
	 */
	public ProcessInstanceVisualization(ProcessInstance anInstance) {
		instance = anInstance;
	}

	/**
	 * Writes a Dot representation of the wrapped process instance to the given
	 * writer.
	 * 
	 * @param bw
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		if (instance.getAttributes().containsKey(ProcessInstance.ATT_PI_PO)
				&& instance.getAttributes().get(ProcessInstance.ATT_PI_PO)
						.equals("true")) {
			writeToDotPartialOrder(bw);
		} else {
			writeToDotLinearOrder(bw);
		}
	}

	/**
	 * Writes a Dot representation of the wrapped process instance to the given
	 * writer, where the process instance is represented as a partial order
	 * 
	 * @param bw
	 * @throws IOException
	 */
	private void writeToDotPartialOrder(Writer bw) throws IOException {
		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";\n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");

		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		Iterator it;

		bw
				.write("pidata [shape=\"box\",rank=\"source\",label=\"Process Instance Data:");

		it = instance.getAttributes().keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			bw.write("\\n" + key + " = "
					+ (String) instance.getAttributes().get(key));
		}
		bw.write("\"];\n");

		String firstId = null;
		Iterator ateIt = ateList.iterator();
		HashSet<String> ateIDs = new HashSet<String>();
		while (ateIt.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();
			String ateID = ate.getAttributes().get(ProcessInstance.ATT_ATE_ID);
			if (firstId == null) {
				firstId = ateID;
			}
			ateIDs.add(ateID);
			Date d = ate.getTimestamp();
			String s = "";
			if (d != null) {
				SimpleDateFormat dateParser = new SimpleDateFormat("Z");
				String timezone = dateParser.format(d);
				timezone = timezone.substring(0, 3) + ":"
						+ timezone.substring(3, 5);

				dateParser = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss.SSS  ");
				s = dateParser.format(d);
				s += timezone;
			}

			bw.write("ate" + ateID + " [shape=\"box\",label=\""
					+ ate.getElement() + "\\n" + ate.getType() + "\\n" + s
					+ "\"];\n");

			bw.write("atedata" + ateID + " [shape=\"box\",label=\"");
			bw.write("Originator = " + ate.getOriginator() + "\\n");

			it = ate.getAttributes().keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				bw.write(key + " = " + (String) ate.getAttributes().get(key)
						+ "\\n");
			}

			bw.write("\"];\n");
			bw.write("ate" + ateID + " -> atedata" + ateID + ";");
			bw.write("{ rank = same; " + "ate" + ateID + ";atedata" + ateID
					+ ";}");
		}
		bw.write("pidata -> ate" + firstId + " [color=\"white\"];\n");

		ateIt = ateList.iterator();
		while (ateIt.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();
			String ateID = ate.getAttributes().get(ProcessInstance.ATT_ATE_ID);
			String ateSucs = ate.getAttributes().get(
					ProcessInstance.ATT_ATE_POST);
			StringTokenizer st = new StringTokenizer(ateSucs, ",", false);
			while (st.hasMoreTokens()) {
				String nextAteId = st.nextToken();
				if (ateIDs.contains(nextAteId)) {
					bw.write("ate" + ateID + " -> ate" + nextAteId + ";");
				}
			}
		}

		bw.write("}\n");

	}

	/**
	 * Writes a Dot representation of the wrapped process instance to the given
	 * writer, where the process instance is represented as a linear order
	 * 
	 * @param bw
	 * @throws IOException
	 */
	private void writeToDotLinearOrder(Writer bw) throws IOException {
		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";\n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");

		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		Iterator it;

		bw
				.write("pidata [shape=\"box\",rank=\"source\",label=\"Process Instance Data:");

		it = instance.getAttributes().keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			bw.write("\\n" + key + " = "
					+ (String) instance.getAttributes().get(key));
			writeModelReferences(instance.getDataAttributes()
					.getModelReferences(key), bw, " (model references:", ")");
		}
		bw.write("\"];\n");

		int i = 0;
		Iterator ateIt = ateList.iterator();
		while (ateIt.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();

			Date d = ate.getTimestamp();
			String s = "";
			if (d != null) {
				SimpleDateFormat dateParser = new SimpleDateFormat("Z");
				String timezone = dateParser.format(d);
				timezone = timezone.substring(0, 3) + ":"
						+ timezone.substring(3, 5);

				dateParser = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss.SSS  ");
				s = dateParser.format(d);
				s += timezone;
			}

			bw.write("ate" + i + " [shape=\"box\",label=\"" + ate.getElement()
					+ "\\n" + ate.getType() + "\\n" + s + "\"];\n");

			bw.write("atedata" + i + " [shape=\"box\",label=\"");
			bw.write("Originator = " + ate.getOriginator() + "\\n");

			it = ate.getAttributes().keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				bw.write(key + " = " + (String) ate.getAttributes().get(key));
				writeModelReferences(ate.getDataAttributes()
						.getModelReferences(key), bw, " (model references:",
						")");
				bw.write("\\n");
			}
			writeModelReferences(ate.getElementModelReferences(), bw,
					"Element model references:", "");
			bw.write("\\n");
			writeModelReferences(ate.getTypeModelReferences(), bw,
					"Event type model references:", "");
			bw.write("\\n");
			writeModelReferences(ate.getOriginatorModelReferences(), bw,
					"Originator model references:", "");
			bw.write("\\n");

			bw.write("\"];\n");
			bw.write("ate" + i + " -> atedata" + i + ";");
			if (i > 0) {
				bw.write("ate" + (i - 1) + " -> ate" + i + ";");
			}
			bw.write("{ rank = same; " + "ate" + i + ";atedata" + i + ";}");
			i++;
		}
		bw.write("pidata -> ate0 [color=\"white\"];\n");

		bw.write("}\n");

	}

	private void writeModelReferences(List<String> modelRefs, Writer bw,
			String prefix, String postfix) throws IOException {
		if (!modelRefs.isEmpty()) {
			bw.write(prefix);
			for (String uri : modelRefs) {
				bw.write(" ");
				bw.write(uri);
			}
			bw.write(postfix);
		}
	}

	/**
	 * Retrieves the grappa visualization of the wrapped process instance as a
	 * ModelGraphPanel.
	 * 
	 * @return
	 */
	public ModelGraphPanel getGrappaVisualization() {
		ModelGraphPanel p = (new ProcessInstanceModelGraph(this))
				.getGrappaVisualization();
		if (p != null) {
			p.setOriginalObject(instance);
		}
		return p;
	}

	/**
	 * Retrieves the grappa visualization of the wrapped process instance as a
	 * ModelGraph
	 * 
	 * @return
	 */
	public ProcessInstanceModelGraph getModelGraph() {
		return new ProcessInstanceModelGraph(this);
	}

	/**
	 * Helper class. Wraps the grappa visualization of a process instance.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected class ProcessInstanceModelGraph extends ModelGraph {
		private ProcessInstanceVisualization pi = null;

		public ProcessInstanceModelGraph(ProcessInstanceVisualization pi) {
			super("ProcessInstanceModelGraph");
			this.pi = pi;
		}

		public void writeToDot(Writer bw) throws IOException {
			nodeMapping.clear();
			pi.writeToDot(bw);
		}
	}

}

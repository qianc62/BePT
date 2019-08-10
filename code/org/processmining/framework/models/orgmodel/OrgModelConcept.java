package org.processmining.framework.models.orgmodel;

import java.util.HashMap;
import org.processmining.framework.util.Dot;
import att.grappa.GrappaPanel;
import java.io.FileWriter;
import java.io.BufferedWriter;
import att.grappa.GrappaAdapter;
import javax.swing.JPanel;
import java.io.PrintWriter;
import java.io.IOException;
import org.processmining.framework.ui.Message;
import java.io.Writer;
import att.grappa.Graph;
import java.io.File;
import java.text.NumberFormat;

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
public class OrgModelConcept extends OrgModel {
	public OrgModelConcept() {
		super();
	}

	public OrgModelConcept(HashMap orgEntities, HashMap resources, HashMap tasks) {
		super(orgEntities, resources, tasks);
	}

	/**
	 * Writes a DOT representation of this graph to the given
	 * <code>Writer</code>. This representation is used by the
	 * <code>getGrappaVisualization</code> method to generate the visualization.
	 * Note that this function should have a call to <code>
	 * nodeMapping.clear()</code>
	 * at the beginning and it should call
	 * <code>nodeMapping.put(new String(</code>nodeID<code>),</code>nodeObject
	 * <code>);</code> after writing a node to the dot file
	 * 
	 * @param bw
	 *            the DOT representation will be written using this
	 *            <code>Writer</code>
	 * @throws IOException
	 *             in case there is a problem with writing to <code>bw</code>
	 */
	public void writeToDot(Writer bw, boolean bOrgEntity, boolean bResource,
			boolean bTask, boolean bInstance) throws IOException {

		HashMap nodelist = new HashMap();

		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";\n");
		bw
				.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");
		bw
				.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"8\",stype=\"filled\",fillcolor=\"palegoldenrod\"];\n");

		int nodenum = 0;
		String[] key;
		if (bOrgEntity) {
			key = (String[]) orgEntities.keySet().toArray(
					new String[orgEntities.keySet().size()]);
			for (int i = 0; i < key.length; i++) {
				nodelist.put((OrgEntity) orgEntities.get(key[i]), String
						.valueOf("node" + nodenum));
				bw
						.write("node"
								+ nodenum++
								+ " [shape=\"house\",style=\"filled\",fillcolor=\"mediumturquoise\",label=\""
								+
								// ((OrgEntity) orgEntities.get(key[i])).getID()
								// + "\\n" +
								((OrgEntity) orgEntities.get(key[i])).getName()
								+ "\"];\n");
			}
		}
		int instanceId = 0;
		if (bResource) {
			key = (String[]) resources.keySet().toArray(
					new String[resources.keySet().size()]);
			for (int i = 0; i < key.length; i++) {

				nodelist.put((Resource) resources.get(key[i]), String
						.valueOf("node" + nodenum));
				bw
						.write("node"
								+ nodenum
								+ " [shape=\"ellipse\",style=\"filled\",fillcolor=\"lightpink\",label=\""
								+
								// ((Resource) resources.get(key[i])).getID() +
								// "\\n" +
								((Resource) resources.get(key[i])).getName()
								+ "\"];\n");
				if (bInstance) {
					Resource res = (Resource) resources.get(key[i]);
					if (res instanceof ResourceConcept) {
						for (String str : ((ResourceConcept) res)
								.getInstances()) {
							bw.write("I" + instanceId
									+ " [shape=\"plaintext\",label=\""
									+ str.replace('"', '\'') + "\"];\n");
							bw
									.write("I"
											+ instanceId
											+ " -> node"
											+ nodenum
											+ " [color=\"gray\",arrowtail=\"none\",arrowhead=\"normal\"];\n");
							instanceId++;
						}
					}
				}
				nodenum++;
			}
		}

		if (bTask) {
			key = (String[]) tasks.keySet().toArray(
					new String[tasks.keySet().size()]);
			for (int i = 0; i < key.length; i++) {
				nodelist.put((Task) tasks.get(key[i]), String.valueOf("node"
						+ nodenum));
				bw
						.write("node"
								+ nodenum
								+ " [shape=\"box\",style=\"filled\",fillcolor=\"wheat\",label=\""
								+ ((Task) tasks.get(key[i])).getName() + "\\n"
								+ ((Task) tasks.get(key[i])).getEventType()
								+ "\"];\n");
				if (bInstance) {
					Task task = (Task) tasks.get(key[i]);
					if (task instanceof TaskConcept) {
						for (String str : ((TaskConcept) task).getInstances()) {
							bw.write("I" + instanceId
									+ " [shape=\"plaintext\",label=\""
									+ str.replace('"', '\'') + "\"];\n");
							bw
									.write("node"
											+ nodenum
											+ " -> I"
											+ instanceId
											+ " [color=\"gray\",arrowtail=\"normal\",arrowhead=\"none\"];\n");
							instanceId++;
						}
					}
				}
				nodenum++;
			}
		}

		if (bResource && bOrgEntity) {
			key = (String[]) resources.keySet().toArray(
					new String[resources.keySet().size()]);
			for (int i = 0; i < key.length; i++) {
				Resource res = (Resource) resources.get(key[i]);

				String[] key2 = (String[]) orgEntities.keySet().toArray(
						new String[orgEntities.keySet().size()]);
				for (int j = 0; j < key2.length; j++) {
					OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);

					if (res.hasOrgEntity(orgEntity))
						bw.write((String) nodelist.get(res) + " -> "
								+ (String) nodelist.get(orgEntity)
								+ " [label=\" \"];");
				}
			}
		}
		if (bTask && bOrgEntity) {
			key = (String[]) tasks.keySet().toArray(
					new String[tasks.keySet().size()]);
			for (int i = 0; i < key.length; i++) {
				String[] key2 = (String[]) orgEntities.keySet().toArray(
						new String[orgEntities.keySet().size()]);
				Task task = (Task) tasks.get(key[i]);
				for (int j = 0; j < key2.length; j++) {
					OrgEntity orgEntity = (OrgEntity) orgEntities.get(key2[j]);

					if (task.hasOrgEntity(orgEntity))
						bw.write((String) nodelist.get(orgEntity) + " -> "
								+ (String) nodelist.get(task)
								+ " [label=\" \"];");
				}
			}
		}
		bw.write("}\n");
	}

	public JPanel getGraphPanel(boolean bOrgEntity, boolean bResource,
			boolean bTask, boolean bInstance) {
		BufferedWriter bw;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			writeToDot(bw, bOrgEntity, bResource, bTask, bInstance);
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			graph = Dot.execute(dotFile.getAbsolutePath());

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			GrappaPanel gp = new GrappaPanel(graph);
			gp.addGrappaListener(new GrappaAdapter());
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

}

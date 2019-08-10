/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.exporting.epcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelHierarchyDirectory;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;

import att.grappa.GrappaConstants;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class EpmlExport implements ExportPlugin {
	private String definitionlist = "";
	private String epclist = "";
	private int currentDef = 1;
	// private Vector definitions = new Vector();
	// private Vector directory = new Vector();
	private ArrayList EPCs = new ArrayList();

	public EpmlExport() {
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof EPCHierarchy || o[i] instanceof ConfigurableEPC) {
				return true;
			}
		}
		return false;
	}

	public String getFileExtension() {
		return "epml";
	}

	public String getName() {
		return "EPML format";
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/EPMLExport";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof ConfigurableEPC) {
				enumerateEPCs(o[i]);
				epclist = writeFlat((ConfigurableEPC) o[i]);
				break;
			}
		}
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof EPCHierarchy) {
				enumerateEPCs(o[i]);
				EPCHierarchy hierarchy = (EPCHierarchy) o[i];
				epclist = writeHierarchy(hierarchy);
				Message.add("at least hierarchy");
				break;
			}
		}
		writeAll(output);
		output.close();
	}

	public void writeAll(final OutputStream output) throws IOException {
		writeLn("<?xml version=\"1.0\"?>", output);
		writeLn("<epml:epml xmlns:epml='http://www.epml.de'>", output);
		writeLn("<definitions>", output);
		writeLn(definitionlist, output);
		writeLn("</definitions>", output);
		writeLn(epclist, output);
		writeLn("</epml:epml>", output);
	}

	public void enumerateEPCs(Object obj) {
		if (obj instanceof ConfigurableEPC) {
			ConfigurableEPC cepc = (ConfigurableEPC) obj;
			if (!EPCs.contains(cepc)) {
				EPCs.add(cepc);
			}
			Iterator it = cepc.getFunctions().iterator();
			while (it.hasNext()) {
				EPCFunction func = (EPCFunction) it.next();
				if (func instanceof EPCSubstFunction) {
					EPCSubstFunction sfunc = (EPCSubstFunction) func;
					if (!EPCs.contains(sfunc.getSubstitutedEPC())) {
						enumerateEPCs(sfunc.getSubstitutedEPC());
					}
				}
			}
		}
		if (obj instanceof EPCHierarchy) {
			EPCHierarchy hierarchy = (EPCHierarchy) obj;
			Iterator it = hierarchy.getAllObjects().iterator();
			while (it.hasNext()) {
				Object potentialEPC = it.next();
				if (potentialEPC instanceof ConfigurableEPC) {
					if (!EPCs.contains((ConfigurableEPC) potentialEPC)) {
						EPCs.add((ConfigurableEPC) potentialEPC);
					}
				}
			}
		}
	}

	public String writeFlat(ConfigurableEPC epc) {
		Iterator it = EPCs.iterator();
		String epcs = "";
		while (it.hasNext()) {
			epcs = epcs + writeEPC((ConfigurableEPC) it.next());
		}
		return "<directory name='main'>\n" + epcs + "</directory>\n";
	}

	public String writeHierarchy(EPCHierarchy hierarchy) {
		Iterator it = hierarchy.getRoots().iterator();
		String epcs = "";
		while (it.hasNext()) {
			Object obj = it.next();
			epcs = epcs + writeHierarchyRecursive(obj, hierarchy);
		}
		return epcs;
	}

	public String writeHierarchyRecursive(Object obj, EPCHierarchy hierarchy) {
		if (obj instanceof ConfigurableEPC) {
			Message.add("at least cepc");
			ConfigurableEPC cepc = (ConfigurableEPC) obj;
			return writeEPC(cepc);
		}
		if (obj instanceof String) {
			Iterator children = hierarchy.getChildren(obj).iterator();
			String text = (String) obj;
			String childstring = "";
			while (children.hasNext()) {
				childstring = childstring
						+ writeHierarchyRecursive(children.next(), hierarchy);
			}
			return "<directory name='" + text + "'>\n" + childstring
					+ "</directory>\n";
		}
		if (obj instanceof ModelHierarchyDirectory) {
			Iterator children = hierarchy.getChildren(obj).iterator();
			String text = ((ModelHierarchyDirectory) obj).toString();
			String childstring = "";
			while (children.hasNext()) {
				childstring = childstring
						+ writeHierarchyRecursive(children.next(), hierarchy);
			}
			return "<directory name='" + text + "'>\n" + childstring
					+ "</directory>\n";
		}
		return "";
	}

	public String writeEPC(ConfigurableEPC epc) {
		String elements = "";
		ArrayList allnodes = new ArrayList();
		allnodes.addAll((Collection) epc.getFunctions());
		allnodes.addAll((Collection) epc.getEvents());
		allnodes.addAll((Collection) epc.getConnectors());
		int currentId = allnodes.size() + 1;

		Iterator nodes = allnodes.iterator();
		while (nodes.hasNext()) {
			ModelGraphVertex node = (ModelGraphVertex) nodes.next();
			if (node instanceof EPCFunction) {
				EPCFunction f = (EPCFunction) node;
				String graphics = "";
				if (f.visualObject != null) {
					int fwidth = (int) (((Double) f.visualObject
							.getAttributeValue(GrappaConstants.WIDTH_ATTR))
							.doubleValue());
					int fheight = (int) (((Double) f.visualObject
							.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
							.doubleValue());
					int fcX = (int) f.visualObject.getCenterPoint().getX();
					int fcY = (int) f.visualObject.getCenterPoint().getY(); // invert
					// the
					// y
					// axis
					// (everything
					// would
					// be
					// upside
					// down
					// otherwise)
					fwidth = 60;
					fheight = 20;
					int ftopX = fcX - (fwidth / 2);
					int ftopY = fcY - (fheight / 2); // invert the y axis
					// (everything would be
					// upside down
					// otherwise)
					graphics = "<graphics>\n<position height='" + fheight
							+ "' width='" + fwidth + "' x='" + ftopX + "' y='"
							+ ftopY + "'/>\n</graphics>\n";
				}
				definitionlist = definitionlist + "<definition defId='"
						+ currentDef + "'/>\n";
				elements = elements + "<function id='"
						+ (allnodes.indexOf(f) + 1) + "' defRef='"
						+ currentDef++ + "'>\n";
				elements = elements + graphics;
				elements = elements + "<name>" + f.getIdentifier()
						+ "</name>\n";
				if (f instanceof EPCSubstFunction) {
					EPCSubstFunction sf = (EPCSubstFunction) f;
					elements = elements + "<toProcess linkToEpcId='"
							+ (allnodes.indexOf(sf.getEPC()) + 1) + "'/>\n";
				}
				if (f.isConfigurable()) {
					elements = elements + "<configurableFunction>\n";
					// configuration values
					elements = elements + "</configurableFunction>\n";
				}
				elements = elements + "</function>\n";
				for (int i = 0; i < f.getNumDataObjects(); i++) {
					elements = elements + "<relation id='" + (currentId++)
							+ "' from='" + (allnodes.indexOf(f) + 1) + "' to='"
							+ (currentId) + "'/>\n" + "<dataField id='"
							+ (currentId++) + "' defRef='" + (currentDef++)
							+ "'>\n" + "<name>\n"
							+ f.getDataObject(i).getLabel() + "</name>\n"
							+ "</dataField>\n";
					definitionlist = definitionlist + "<definition defId='"
							+ currentDef + "'/>\n";
				}
				for (int i = 0; i < f.getNumInfSysObjects(); i++) {
					elements = elements + "<relation id='" + (currentId++)
							+ "' from='" + (allnodes.indexOf(f) + 1) + "' to='"
							+ (currentId) + "'/>\n" + "<application id='"
							+ (currentId++) + "' defRef='" + (currentDef++)
							+ "'>\n" + "<name>"
							+ f.getInfSysObject(i).getLabel() + "</name>\n"
							+ "</application>\n";
					definitionlist = definitionlist + "<definition defId='"
							+ currentDef + "'/>\n";
				}
				for (int i = 0; i < f.getNumOrgObjects(); i++) {
					elements = elements + "<relation id='" + (currentId++)
							+ "' from='" + (allnodes.indexOf(f) + 1) + "' to='"
							+ (currentId) + "'/>\n" + "<participant id='"
							+ (currentId++) + "' defRef='" + (currentDef++)
							+ "'>\n" + "<name>" + f.getOrgObject(i).getLabel()
							+ "</name>\n" + "</participant>\n";
					definitionlist = definitionlist + "<definition defId='"
							+ currentDef + "'/>\n";
				}
			}
			if (node instanceof EPCEvent) {
				EPCEvent e = (EPCEvent) node;
				String graphics = "";
				if (e.visualObject != null) {
					int ewidth = (int) (((Double) e.visualObject
							.getAttributeValue(GrappaConstants.WIDTH_ATTR))
							.doubleValue());
					int eheight = (int) (((Double) e.visualObject
							.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
							.doubleValue());
					int ecX = (int) e.visualObject.getCenterPoint().getX();
					int ecY = (int) e.visualObject.getCenterPoint().getY(); // invert
					// the
					// y
					// axis
					// (everything
					// would
					// be
					// upside
					// down
					// otherwise)
					ewidth = 60;
					eheight = 20;
					int etopX = ecX - (ewidth / 2);
					int etopY = ecY - (eheight / 2); // invert the y axis
					// (everything would be
					// upside down
					// otherwise)
					graphics = "<graphics>\n<position height='" + eheight
							+ "' width='" + ewidth + "' x='" + etopX + "' y='"
							+ etopY + "'/>\n</graphics>\n";
				}
				definitionlist = definitionlist + "<definition defId='"
						+ currentDef + "'/>\n";
				elements = elements + "<event id='" + (allnodes.indexOf(e) + 1)
						+ "' defRef='" + currentDef++ + "'>\n";
				elements = elements + graphics;
				elements = elements + "<name>" + e.getIdentifier() + "</name>";
				elements = elements + "</event>\n";
			}
			if (node instanceof EPCConnector) {
				EPCConnector c = (EPCConnector) node;
				String type = c.toString().toLowerCase().substring(0,
						c.toString().indexOf("-")).trim();
				String graphics = "";
				if (c.visualObject != null) {
					int cwidth = (int) (((Double) c.visualObject
							.getAttributeValue(GrappaConstants.WIDTH_ATTR))
							.doubleValue());
					int cheight = (int) (((Double) c.visualObject
							.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
							.doubleValue());
					int ccX = (int) c.visualObject.getCenterPoint().getX();
					int ccY = (int) c.visualObject.getCenterPoint().getY(); // invert
					// the
					// y
					// axis
					// (everything
					// would
					// be
					// upside
					// down
					// otherwise)
					cwidth = 20;
					cheight = 20;
					int ctopX = ccX - (cwidth / 2);
					int ctopY = ccY - (cheight / 2); // invert the y axis
					// (everything would be
					// upside down
					// otherwise)
					graphics = "<graphics>\n<position height='" + cheight
							+ "' width='" + cwidth + "' x='" + ctopX + "' y='"
							+ ctopY + "'/>\n</graphics>\n";
				}
				elements = elements + "<" + type + " id='"
						+ (allnodes.indexOf(c) + 1) + "'>\n";
				elements = elements + graphics;
				if (c.isConfigurable()) {
					elements = elements + "<configurableConnector>\n";
					// configuration values
					elements = elements + "</configurableConnector>\n";
				}
				elements = elements + "</" + type + ">\n";
			}

			Iterator successors = node.getSuccessors().iterator();
			while (successors.hasNext()) {
				ModelGraphVertex next = (ModelGraphVertex) successors.next();
				elements = elements
						+ writeArc(node, next, allnodes.indexOf(node) + 1,
								allnodes.indexOf(next) + 1, currentId++);
			}
		}

		Message.add("at least cepc completed");

		return "<epc epcId='" + (EPCs.indexOf(epc) + 1) + "' name='"
				+ epc.getIdentifier() + "'>\n" + elements + "</epc>\n";
	}

	public String writeArc(ModelGraphVertex snode, ModelGraphVertex tnode,
			int source, int target, int id) {
		int height = 0;
		int cX = 0;
		int cY = 0;
		int topY = 0;
		if (snode instanceof EPCConnector && snode.visualObject != null) {
			EPCConnector c = (EPCConnector) snode;
			height = (int) (((Double) c.visualObject
					.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
					.doubleValue());
			cX = (int) c.visualObject.getCenterPoint().getX();
			cY = (int) c.visualObject.getCenterPoint().getY();
			topY = cY - (height / 2);
		} else {
			if (snode instanceof EPCEvent && snode.visualObject != null) {
				EPCEvent e = (EPCEvent) snode;
				height = (int) (((Double) e.visualObject
						.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
						.doubleValue());
				cX = (int) e.visualObject.getCenterPoint().getX();
				cY = (int) e.visualObject.getCenterPoint().getY();
				topY = cY - (height / 2);
			} else {
				if (snode instanceof EPCFunction && snode.visualObject != null) {
					EPCFunction f = (EPCFunction) snode;
					height = (int) (((Double) f.visualObject
							.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
							.doubleValue());
					cX = (int) f.visualObject.getCenterPoint().getX();
					cY = (int) f.visualObject.getCenterPoint().getY();
					topY = cY - (height / 2);
				} else {
					cX = 0;
					cY = 0;
				}
			}
		}
		int theight = 0;
		int tcX = 0;
		int tcY = 0;
		int ttopY = 0;
		if (tnode instanceof EPCConnector && tnode.visualObject != null) {
			EPCConnector c = (EPCConnector) tnode;
			theight = (int) (((Double) c.visualObject
					.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
					.doubleValue());
			tcX = (int) c.visualObject.getCenterPoint().getX();
			tcY = (int) c.visualObject.getCenterPoint().getY();
			ttopY = tcY - (theight / 2);
		} else {
			if (tnode instanceof EPCEvent && tnode.visualObject != null) {
				EPCEvent e = (EPCEvent) tnode;
				theight = (int) (((Double) e.visualObject
						.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
						.doubleValue());
				tcX = (int) e.visualObject.getCenterPoint().getX();
				tcY = (int) e.visualObject.getCenterPoint().getY();
				ttopY = tcY - (theight / 2);
			} else {
				if (tnode instanceof EPCFunction && tnode.visualObject != null) {
					EPCFunction f = (EPCFunction) tnode;
					theight = (int) (((Double) f.visualObject
							.getAttributeValue(GrappaConstants.HEIGHT_ATTR))
							.doubleValue());
					tcX = (int) f.visualObject.getCenterPoint().getX();
					tcY = (int) f.visualObject.getCenterPoint().getY();
					ttopY = tcY - (theight / 2);
				} else {
					tcX = 0;
					tcY = 0;
				}
			}
		}
		return "<arc id='" + id + "'>\n" + "<graphics>\n" + "<position x='"
				+ cX + "' y='" + (cY + 10) + "'/>\n" + "<position x='" + tcX
				+ "' y='" + (tcY - 10) + "'/>\n" + "</graphics>\n"
				+ "<flow source='" + source + "' target='" + target + "'/>\n"
				+ "</arc>\n";
	}

	private void writeLn(String s, OutputStream out) {
		try {
			if (s.length() != 0) {
				out.write(s.getBytes());
			}
			out.write("\n".getBytes());
		} catch (IOException ex) {
		}
	}

}

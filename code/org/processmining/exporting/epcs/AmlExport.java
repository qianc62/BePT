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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelHierarchyDirectory;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.plugin.ProvidedObject;

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

public class AmlExport implements ExportPlugin {
	private String definitionlist = "";
	private String epclist = "";
	private int currentDef = 1;
	private int currentGroup = 1;
	private ArrayList allnodes = new ArrayList();
	private ArrayList EPCs = new ArrayList();
	private static final int GROUP = 1;
	private static final int OBJOCC = 2;
	private static final int OBJDEF = 3;
	private static final int CXNOCC = 4;
	private static final int CXNDEF = 5;
	private static final int MODEL = 6;

	public AmlExport() {
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
		return "xml";
	}

	public String getName() {
		return "AML format";
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/AMLExport";
	}

	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof EPC || o[i] instanceof ConfigurableEPC) {
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
				break;
			}
		}
		writeAll(output);
		output.close();
	}

	public void writeAll(final OutputStream output) throws IOException {
		writeLn("<?xml version=\"1.0\"?>", output);
		writeLn("<!DOCTYPE AML SYSTEM 'ARIS-Export.dtd' [", output);
		writeLn("<!ENTITY LocaleId.DEde '1031'>", output);
		writeLn("<!ENTITY Codepage.DEde '1252'>", output);
		writeLn("<!ENTITY LocaleId.USen '1033'>", output);
		writeLn("<!ENTITY Codepage.USen '1252'>", output);
		writeLn("]>", output);
		writeLn("<AML>", output);
		writeAMLHeader(output);
		int index = epclist.indexOf("<Model");
		String epcsNdefinitions = epclist.substring(0, index) + "\n"
				+ definitionlist + epclist.substring(index, epclist.length());
		writeLn(epcsNdefinitions, output);
		writeLn("</AML>", output);
	}

	public void enumerateEPCs(Object obj) {
		if (obj instanceof ConfigurableEPC) {
			ConfigurableEPC cepc = (ConfigurableEPC) obj;
			if (!EPCs.contains(cepc)) {
				EPCs.add(cepc);
				allnodes.addAll((Collection) cepc.getFunctions());
				allnodes.addAll((Collection) cepc.getEvents());
				allnodes.addAll((Collection) cepc.getConnectors());
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
					ConfigurableEPC cepc = (ConfigurableEPC) potentialEPC;
					if (!EPCs.contains(cepc)) {
						EPCs.add(cepc);
						allnodes.addAll((Collection) cepc.getFunctions());
						allnodes.addAll((Collection) cepc.getEvents());
						allnodes.addAll((Collection) cepc.getConnectors());
					}
				}
			}
		}
		currentDef = allnodes.size() + 2;
	}

	public String writeFlat(ConfigurableEPC epc) {
		Iterator it = EPCs.iterator();
		String epcs = "";
		while (it.hasNext()) {
			epcs = epcs + writeEPC((ConfigurableEPC) it.next());
		}
		return "<Group Group.ID='Group.Root'>\n" + epcs + "</Group>\n";
	}

	public String writeHierarchy(EPCHierarchy hierarchy) {
		Iterator it = hierarchy.getRoots().iterator();
		String epcs = "";
		while (it.hasNext()) {
			Object obj = it.next();
			epcs = epcs + writeHierarchyRecursive(obj, hierarchy);
		}
		return "<Group Group.ID='Group.Root'>\n" + epcs + "</Group>\n";
	}

	public String writeHierarchyRecursive(Object obj, EPCHierarchy hierarchy) {
		if (obj instanceof ConfigurableEPC) {
			ConfigurableEPC cepc = (ConfigurableEPC) obj;
			return writeEPC(cepc);
		}
		if (obj instanceof String) {
			Iterator children = hierarchy.getChildren(obj).iterator();
			String childstring = "";
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof ModelHierarchyDirectory) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
				if (child instanceof String) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
			}
			children = hierarchy.getChildren(obj).iterator();
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof ConfigurableEPC) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
			}
			return "<Group Group.ID='" + AmlIdFormat(currentGroup++, GROUP)
					+ "'>\n" + "<AttrDef AttrDef.Type='AT_NAME'>\n"
					+ "<AttrValue LocaleId='&LocaleId.USen;'>" + obj
					+ "</AttrValue>\n"
					+ "<AttrValue LocaleId='&LocaleId.DEde;'>" + obj
					+ "</AttrValue>\n" + "</AttrDef>\n" + childstring
					+ "</Group>\n";
		}
		if (obj instanceof ModelHierarchyDirectory) {
			Iterator children = hierarchy.getChildren(obj).iterator();
			String childstring = "";
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof ModelHierarchyDirectory) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
				if (child instanceof String) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
			}
			children = hierarchy.getChildren(obj).iterator();
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof ConfigurableEPC) {
					childstring = childstring
							+ writeHierarchyRecursive(child, hierarchy);
				}
			}
			return "<Group Group.ID='" + AmlIdFormat(currentGroup++, GROUP)
					+ "'>\n" + "<AttrDef AttrDef.Type='AT_NAME'>\n"
					+ "<AttrValue LocaleId='&LocaleId.USen;'>" + obj.toString()
					+ "</AttrValue>\n"
					+ "<AttrValue LocaleId='&LocaleId.DEde;'>" + obj.toString()
					+ "</AttrValue>\n" + "</AttrDef>\n" + childstring
					+ "</Group>\n";
		}
		return "";
	}

	public String writeEPC(ConfigurableEPC epc) {
		String elements = "";

		Iterator nodes = allnodes.iterator();
		while (nodes.hasNext()) {
			ModelGraphVertex node = (ModelGraphVertex) nodes.next();
			if (!epc.getFunctions().contains(node)) {
				if (!epc.getEvents().contains(node)) {
					if (!epc.getConnectors().contains(node)) {
						continue;
					}
				}
			}
			Iterator successors = node.getSuccessors().iterator();
			String cxnDef = "";
			String cxnDefIdRefs = "";
			String cxnOcc = "";
			String cxnOccIdRefs = "";
			String symbolNum = "";
			String typeNum = "";
			if (node instanceof EPCEvent) {
				symbolNum = "ST_EV";
				typeNum = "OT_EVT";
			}
			if (node instanceof EPCConnector) {
				EPCConnector c = (EPCConnector) node;
				String type = c.toString().toUpperCase().substring(0,
						c.toString().indexOf("-")).trim();
				symbolNum = "ST_OPR_" + type + "_1";
				typeNum = "OT_RULE";
				if (c.isConfigurable()) {
					// configurable
				}
			}
			if (node instanceof EPCFunction) {
				symbolNum = "ST_FUNC";
				typeNum = "OT_FUNC";
				EPCFunction f = (EPCFunction) node;
				if (f.isConfigurable()) {
					// configurable
				}
				HashMap LabelType = new HashMap();
				for (int i = 0; i < f.getNumDataObjects(); i++) {
					LabelType.put(f.getDataObject(i).getLabel(), "OT_ENT_TYPE");
				}
				for (int i = 0; i < f.getNumInfSysObjects(); i++) {
					LabelType.put(f.getInfSysObject(i).getLabel(),
							"OT_APPL_SYS");
				}
				for (int i = 0; i < f.getNumOrgObjects(); i++) {
					LabelType.put(f.getOrgObject(i).getLabel(), "OT_ORG_UNIT");
				}
				Iterator nonControlFlow = LabelType.keySet().iterator();
				while (nonControlFlow.hasNext()) {
					String label = (String) nonControlFlow.next();
					String type = (String) LabelType.get(label);
					// objocc, objdef, cxnocc, cxndef
				}
			}
			while (successors.hasNext()) {
				ModelGraphVertex succ = (ModelGraphVertex) successors.next();
				String toObjDef = AmlIdFormat(allnodes.indexOf(succ) + 1,
						OBJDEF);
				String toObjOcc = AmlIdFormat(allnodes.indexOf(succ) + 1,
						OBJOCC);
				String cxnDefType = "";
				if (node instanceof EPCFunction) {
					if (succ instanceof EPCEvent) {
						cxnDefType = "CT_CRT_1";
						cxnDefType = "OT_FUNC.CT_CRT_1.OT_EVT";
					}
					if (succ instanceof EPCConnector) {
						cxnDefType = "CT_LEADS_TO_1";
						cxnDefType = "OT_FUNC.CT_LEADS_TO_1.OT_RULE";
					}
				}
				if (node instanceof EPCEvent) {
					if (succ instanceof EPCFunction) {
						cxnDefType = "CT_ACTIV_1";
						cxnDefType = "OT_EVT.CT_ACTIV_1.OT_FUNC";
					}
					if (succ instanceof EPCConnector) {
						cxnDefType = "CT_ACTIV_1";
						cxnDefType = "OT_EVT.CT_IS_EVAL_BY_1.OT_RULE";
					}
				}
				if (node instanceof EPCConnector) {
					if (succ instanceof EPCEvent) {
						cxnDefType = "CT_ACTIV_1";
						cxnDefType = "OT_RULE.CT_LEADS_TO_2.OT_EVT";
					}
					if (succ instanceof EPCFunction) {
						cxnDefType = "CT_ACTIV_1";
						cxnDefType = "OT_RULE.CT_ACTIV_1.OT_FUNC";
					}
					if (succ instanceof EPCConnector) {
						cxnDefType = "CT_ACTIV_1";
						cxnDefType = "OT_RULE.CT_LNK_2.OT_RULE";
					}
				}
				cxnDef = cxnDef + "<CxnDef CxnDef.ID='"
						+ AmlIdFormat(currentDef, CXNDEF) + "'\n "
						+ "CxnDef.Type='" + cxnDefType + "'\n "
						+ "ToObjDef.IdRef='" + toObjDef + "'>\n"
						+ "</CxnDef>\n";
				cxnDefIdRefs = cxnDefIdRefs + AmlIdFormat(currentDef++, CXNDEF)
						+ " ";
				cxnOcc = cxnOcc + "<CxnOcc CxnOcc.ID='"
						+ AmlIdFormat(currentDef, CXNOCC) + "'\n "
						+ "CxnDef.IdRef='"
						+ AmlIdFormat((currentDef - 1), CXNDEF) + "'\n "
						+ "ToObjOcc.IdRef='" + toObjOcc + "'>\n"
						+ "</CxnOcc>\n";
				cxnOccIdRefs = cxnOccIdRefs + AmlIdFormat(currentDef++, CXNOCC)
						+ " ";
				;
			}
			definitionlist = definitionlist + "<ObjDef ObjDef.ID='"
					+ AmlIdFormat((allnodes.indexOf(node) + 1), OBJDEF)
					+ "'\n " + "TypeNum='" + typeNum + "'\n ";
			if (node instanceof EPCSubstFunction) {
				EPCSubstFunction sf = (EPCSubstFunction) node;
				if (sf.getSubstitutedEPC() != null
						&& EPCs.indexOf(sf.getSubstitutedEPC()) > -1) {
					definitionlist = definitionlist
							+ "LinkedModels.IdRefs='"
							+ AmlIdFormat(
									(EPCs.indexOf(sf.getSubstitutedEPC()) + 1),
									MODEL) + "'\n ";
				}
			}
			if (cxnDefIdRefs.length() > 0) {
				definitionlist = definitionlist + "ToCxnDefs.IdRefs='"
						+ cxnDefIdRefs + "'\n ";
			}
			definitionlist = definitionlist + "SymbolNum='" + symbolNum
					+ "'>\n" + "<AttrDef AttrDef.Type='AT_NAME'>\n"
					+ "<AttrValue LocaleId='&LocaleId.USen;'>"
					+ ConfigurableEPC.editName(node.getIdentifier())
					+ "</AttrValue>\n"
					+ "<AttrValue LocaleId='&LocaleId.DEde;'>"
					+ ConfigurableEPC.editName(node.getIdentifier())
					+ "</AttrValue>\n" + "</AttrDef>\n" + cxnDef
					+ "</ObjDef>\n";
			elements = elements + "<ObjOcc ObjOcc.ID='"
					+ AmlIdFormat(allnodes.indexOf(node) + 1, OBJOCC) + "'\n "
					+ "ObjDef.IdRef='"
					+ AmlIdFormat(allnodes.indexOf(node) + 1, OBJDEF) + "'\n ";
			if (cxnOccIdRefs.length() > 0) {
				elements = elements + "ToCxnOccs.IdRefs='" + cxnOccIdRefs
						+ "'\n ";
			}
			elements = elements
					+ "SymbolNum='"
					+ symbolNum
					+ "'\n "
					+ "Active='YES'\n Shadow='YES'\n Visible='YES'\n Zorder='1'\n Hints='0'>\n"
					+ cxnOcc
					+ "<AttrOcc AttrTypeNum='AT_NAME'\n"
					+ "Port='CENTER'\n OrderNum='0' SymbolFlag='TEXT'\n FontSS.IdRef='FontSS.d-----0-----c--'\n OffsetX='0'\n OffsetY='0'/>\n"
					+ "</ObjOcc>\n";
		}
		elements = "<Model Model.ID='"
				+ AmlIdFormat((EPCs.indexOf(epc) + 1), MODEL)
				+ "' Model.Type='MT_EEPC'>\n" + "<Flag>180</Flag>\n"
				+ "<AttrDef AttrDef.Type='AT_NAME'>\n"
				+ "<AttrValue LocaleId='&LocaleId.USen;'>"
				+ epc.getIdentifier() + "</AttrValue>\n"
				+ "<AttrValue LocaleId='&LocaleId.DEde;'>"
				+ epc.getIdentifier() + "</AttrValue>\n" + "</AttrDef>\n"
				+ elements + "</Model>\n";
		return elements;
	}

	public String writeArc(int source, int target, int id) {
		return "<arc id='" + id + "'>\n" + "<flow source='" + source
				+ "' target='" + target + "'/>\n" + "</arc>\n";
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

	private String AmlIdFormat(int input, int AmlElement) {
		String in = String.valueOf(input);
		while (in.length() < 12) {
			in = "0" + in;
		}
		switch (AmlElement) {
		case GROUP:
			in = "Group." + in + "k--";
			break;
		case MODEL:
			in = "Model." + in + "u--";
			break;
		case OBJDEF:
			in = "ObjDef." + in + "p--";
			break;
		case CXNDEF:
			in = "CxnDef." + in + "q--";
			break;
		case OBJOCC:
			in = "ObjOcc." + in + "x--";
			break;
		case CXNOCC:
			in = "CxnOcc." + in + "y--";
			break;
		}
		return in;
	}

	private void writeAMLHeader(OutputStream out) {
		SimpleDateFormat TimeParser = new SimpleDateFormat("HH:mm:ss.SSS");
		SimpleDateFormat DateParser = new SimpleDateFormat("dd-MM-yyyy");
		SimpleDateFormat Date2Parser = new SimpleDateFormat("MM/dd/yyyy");
		Calendar cal = new GregorianCalendar();
		writeLn("<Header-Info\n CreateTime='"
				+ TimeParser.format(cal.getTime()) + "'", out);
		writeLn("CreateDate='" + DateParser.format(cal.getTime()) + "'", out);
		writeLn(
				"DatabaseName='ProM-Data'\n UserName='ProM'\n ArisExeVersion='61'\n />",
				out);
		writeLn(
				"<Language LocaleId='&LocaleId.USen;' Codepage='&Codepage.USen;'>\n <LogFont \n FaceName='Arial'\n Height='-13'\n Width='0'\n Escapement='0'\n Orientation='0'\n Weight='400'\n CharSet='0'\n  OutPrecision='0'\n ClipPrecision='0'\n Quality='0'\n PitchAndFamily='0'\n Color='0'/>\n </Language>",
				out);
		writeLn(
				"<Language LocaleId='&LocaleId.DEde;' Codepage='&Codepage.DEde;'>\n <LogFont \n FaceName='Arial'\n Height='-13'\n Width='0'\n Escapement='0'\n Orientation='0'\n Weight='400'\n CharSet='0'\n  OutPrecision='0'\n ClipPrecision='0'\n Quality='0'\n PitchAndFamily='0'\n Color='0'/>\n </Language>",
				out);
		writeLn(
				"<Database>\n <AttrDef AttrDef.Type='AT_CREATOR'>\n <AttrValue LocaleId='&LocaleId.USen;'>ProM</AttrValue>\n </AttrDef>\n <AttrDef AttrDef.Type='AT_CREAT_TIME_STMP'>",
				out);
		writeLn("<AttrValue LocaleId='&LocaleId.USen;'>"
				+ TimeParser.format(cal.getTime()) + ";"
				+ Date2Parser.format(cal.getTime()) + "</AttrValue>", out);
		writeLn("<AttrValue LocaleId='&LocaleId.DEde;'>"
				+ TimeParser.format(cal.getTime()) + ";"
				+ Date2Parser.format(cal.getTime()) + "</AttrValue>", out);
		writeLn("</AttrDef>", out);
		writeLn(
				"</Database>\n <FontStyleSheet FontSS.ID='FontSS.d-----0-----c--'>\n <AttrDef AttrDef.Type='AT_NAME'>\n"
						+ "<AttrValue LocaleId='&LocaleId.USen;'>Standard</AttrValue>\n"
						+ "<AttrValue LocaleId='&LocaleId.DEde;'>Standard</AttrValue>\n"
						+ "</AttrDef>\n <FontNode LocaleId='&LocaleId.USen;' \n FaceName='Arial'\n Height='-13' \n Width='0'\n Escapement='0'\n Orientation='0'\n Weight='400'\n CharSet='0'\n OutPrecision='0'\n ClipPrecision='0'\n Quality='0'\n PitchAndFamily='0'\n Color='0'/>\n </FontStyleSheet>",
				out);
	}
}

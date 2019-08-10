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

package org.processmining.analysis.petrinet.cpnexport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.analysis.petrinet.cpnexport.SubpageMapping.Mapping;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;

/**
 * This class provides helper methods that are needed for making an export to
 * CPN tools 2.0
 * 
 * @author Ronny Mans
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 * @version 1.0
 */
public class ManagerXml {

	/**
	 * Gives the location of the Document Type Definition (DTD) specifying the
	 * CPN Tools format.
	 */
	public final static String DtdUri = "http://www.daimi.au.dk/~cpntools/bin/DTD/5/cpn.dtd";

	/**
	 * Writes the header of the CPN file.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public static void writeHeader(BufferedWriter bw) throws IOException {
		/* write header information */
		bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		bw
				.write("<!DOCTYPE workspaceElements PUBLIC \"-//CPN//DTD CPNXML 1.0//EN\" \""
						+ DtdUri + "\">\n\n");

		/* start writing the cpn model */
		bw.write("<workspaceElements>\n"
				+ "\t<generator tool=\"ProM\" version=\"3.0\" format=\"5\"/>\n"
				+ "\t<cpnet>\n");
	}

	/**
	 * Writes the starting tag of the globbox element
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file
	 * @throws IOException
	 */
	public static void writeStartGlobbox(BufferedWriter bw) throws IOException {
		bw.write("\t\t<globbox>\n");
	}

	/**
	 * Writes the closing tag of the globbox element
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file
	 * @throws IOException
	 */
	public static void writeEndGlobbox(BufferedWriter bw) throws IOException {
		bw.write("\t\t</globbox>\n");
	}

	/**
	 * Writes the block for the standard declarations in the cpn-file. In this
	 * block the declarations for the types E, INT, BOOL and STRING can be
	 * found.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeStandDecl(BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Standard declarations</id>\n");

		/* default color sets defined by CPN Tools */
		bw.write("\t\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<id>E</id>\n" + "\t\t\t\t\t<enum>\n"
				+ "\t\t\t\t\t\t<id>e</id>\n" + "\t\t\t\t\t</enum>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<id>INT</id>\n" + "\t\t\t\t\t<int/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<id>BOOL</id>\n" + "\t\t\t\t\t<bool/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<id>STRING</id>\n" + "\t\t\t\t\t<string/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the block for the control flow declarations in the cpn-file. In
	 * this block the declarations for the color set for representing the case
	 * IDs and the variable that is used to bind the CASE_ID token while moving
	 * it through the process model can be found.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param cpnVarForCaseId
	 *            the cpn variable for the case id.
	 * @param colorSetForCaseId
	 *            the color set for the case id.
	 * @param timed
	 *            needs the color set to be timed or not.
	 * @throws IOException
	 */
	public static void writeControlFlowDec(BufferedWriter bw,
			CpnVarAndType cpnVarForCaseId, IntegerColorSet colorSetForCaseId,
			boolean timed) throws IOException {
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Control Flow declarations</id>\n");

		/*
		 * The color set and the variable which are needed for depicting the
		 * control flow
		 */
		colorSetForCaseId.setTimed(timed);
		colorSetForCaseId.write(bw);

		/*
		 * variable used to bind the CASE_ID token while moving it through the
		 * process model
		 */
		cpnVarForCaseId.write(bw);
		bw.write("\t\t\t</block>\n");
	}

	public static void writeRedesignDec(BufferedWriter bw, String folderPref)
			throws IOException {
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Redesign declarations</id>\n");
		// writing the file locations for simulation settings
		// writing the number of cases-file
		bw.write("\t\t\t\t\t<use id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<ml>&quot;" + folderPref
				+ "/arrivalRate.sml&quot;" + "</ml>\n"
				+ "\t\t\t\t\t\t<layout>use &quot;" + folderPref
				+ "/arrivalRate.sml&quot;" + ";" + "</layout>\n"
				+ "\t\t\t\t\t</use>\n");
		// writing the arrival rate-file
		bw.write("\t\t\t\t\t<use id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<ml>&quot;" + folderPref
				+ "/numberOfCases.sml&quot;" + "</ml>\n");
		bw.write("\t\t\t\t\t\t<layout>use &quot;" + folderPref
				+ "/numberOfCases.sml&quot;" + ";" + "</layout>\n");
		bw.write("\t\t\t\t\t</use>\n");
		// writing the location of the folder-file
		bw.write("\t\t\t\t\t<use id=\"" + ManagerID.getNewID() + "\">\n"
				+ "\t\t\t\t\t<ml>&quot;" + folderPref + "/valFolder.sml&quot;"
				+ "</ml>\n" + "\t\t\t\t\t\t<layout>use &quot;" + folderPref
				+ "/valFolder.sml&quot;" + ";" + "</layout>\n"
				+ "\t\t\t\t\t</use>\n");
		// writing PREFIX_FILE
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">val PREFIX_FILE = &quot;TPT&quot;\n");
		bw
				.write("\t\t\t\t\t\t<layout>val PREFIX_FILE = &quot;TPT&quot;</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");
		// writing FILE_EXTENSION
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">val FILE_EXTENSION = &quot;.txt&quot;\n");
		bw
				.write("\t\t\t\t\t\t<layout>val FILE_EXTENSION = &quot;.txt&quot;</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");

		// write the functions, but first create them
		CpnFunction funcAdd = new CpnFunction(
				"add(file_id,  tpt)",
				"let val _ = TextIO.output(file_id, tpt ^\"\\n\") in TextIO.closeOut(file_id) end");
		funcAdd.write(bw);
		CpnFunction funcAddToFile = new CpnFunction(
				"addToFile(caseID, tpt)",
				"let val caseIDString = Int.toString(caseID) val tptString = Int.toString(tpt) val file_id = TextIO.openAppend(OS.Path.concat(FOLDER, PREFIX_FILE) ^ FILE_EXTENSION) in add(file_id, tptString) end");
		funcAddToFile.write(bw);
		CpnFunction funcGetTPT = new CpnFunction("getTPT(t)",
				"IntInf.toInt(time())-valOf(Int.fromString(t))");
		funcGetTPT.write(bw);
		CpnFunction funcCreateFile = new CpnFunction(
				"createFile()",
				"let val file_id = TextIO.openOut(OS.Path.concat(FOLDER, PREFIX_FILE) ^ FILE_EXTENSION) in TextIO.closeOut(file_id) end");
		funcCreateFile.write(bw);
		// end block
		bw.write("\t\t\t</block>\n");
	}

	/**
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param cpnVarForStartCase
	 *            CpnVarAndType the cpn variable that has to keep the start
	 *            timestamp for a case
	 * @param colorSetStartCase
	 *            StringColorSet the color set for keeping the start timestamp
	 *            for a case
	 * @throws IOException
	 */
	public static void writeStartCase(BufferedWriter bw,
			CpnVarAndType cpnVarForStartCase, StringColorSet colorSetStartCase)
			throws IOException {
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Start case declarations</id>\n");

		/* the color set and variable for keeping the start time stamp of a case */
		colorSetStartCase.write(bw);
		cpnVarForStartCase.write(bw);

		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the block for the group and the resources declarations in the
	 * cpn-file
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param cpnVarsGroups
	 *            List the cpn-variables that are related to the resources and
	 *            the groups
	 * @param colorSetsGroups
	 *            List the color sets that are related to the resources and the
	 *            groups
	 * @throws IOException
	 */
	public static void writeResourceDec(BufferedWriter bw,
			List<CpnVarAndType> cpnVarsGroups,
			List<SubSetColorSet> colorSetsGroups) throws IOException {
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Groups and Resources declarations</id>\n");
		// write the colorsets
		Iterator<SubSetColorSet> colorSets = colorSetsGroups.iterator();
		while (colorSets.hasNext()) {
			CpnColorSet colorSet = colorSets.next();
			colorSet.write(bw);
		}
		// write the variables
		Iterator<CpnVarAndType> vars = cpnVarsGroups.iterator();
		while (vars.hasNext()) {
			CpnVarAndType var = vars.next();
			var.write(bw);
		}

		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the block for the data declarations in the cpn-file
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param cpnVarsDataAttr
	 *            List the variables that are used for representing all data
	 *            attributes in the cpn-file
	 * @param colorSetDataAttr
	 *            CpnColorSet the color that contains all the data attributes
	 * @param colorSetsSepDatAttr
	 *            List the color sets for all the separate data attributes
	 * @throws IOException
	 */
	public static void writeDataDec(BufferedWriter bw,
			List<CpnVarAndType> cpnVarsDataAttr, CpnColorSet colorSetDataAttr,
			List<CpnColorSet> colorSetsSepDatAttr) throws IOException {

		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Data declarations</id>\n");
		// write the color sets for the separate data attributes
		Iterator<CpnColorSet> sepDatAttrs = colorSetsSepDatAttr.iterator();
		while (sepDatAttrs.hasNext()) {
			CpnColorSet datAttr = sepDatAttrs.next();
			datAttr.write(bw);
		}

		// write the color set that represents all resources
		colorSetDataAttr.write(bw);

		// write the variables
		Iterator<CpnVarAndType> dataAttrVars = cpnVarsDataAttr.iterator();
		while (dataAttrVars.hasNext()) {
			CpnVarAndType dataAttrVar = dataAttrVars.next();
			dataAttrVar.write(bw);
		}

		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the product color set declarations that are needed for some places
	 * in the cpn model.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param productColorSetsForPlaces
	 *            Set the set in which the product color sets can be found that
	 *            are needed for some places in the cpn model.
	 * @param timed
	 *            is the net timed or not.
	 * @throws IOException
	 */
	public static void writeProductColorSetsForPlaces(BufferedWriter bw,
			Set<ProductColorSet> productColorSetsForPlaces, boolean timed)
			throws IOException {
		if (productColorSetsForPlaces != null
				&& productColorSetsForPlaces.size() > 0) {
			// start writing the product color set for places declarations block
			bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
			bw.write("\t\t\t\t<id>Product Color Sets for places</id>\n");

			Iterator<ProductColorSet> colorSets = productColorSetsForPlaces
					.iterator();
			while (colorSets.hasNext()) {
				ProductColorSet colorSet = colorSets.next();
				colorSet.setTimed(timed);
				colorSet.write(bw);
			}

			// finish writing the product color set for places declarations
			// block
			bw.write("\t\t\t</block>\n");
		}
	}

	/**
	 * Writes the functions that generate random values which are needed for
	 * some data attributes
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param numericTypeDataAttrCpnFunc
	 *            a list with the cpn functions of data attributes that are of a
	 *            numeric type
	 * @throws IOException
	 */
	public static void writeRandomValuesFunctions(BufferedWriter bw,
			ArrayList<CpnFunction> numericTypeDataAttrCpnFunc)
			throws IOException {
		boolean firstTime = true;
		Iterator<CpnFunction> cpnFuncs = numericTypeDataAttrCpnFunc.iterator();
		while (cpnFuncs.hasNext()) {
			// for each data attribute that is of a numeric type, write the
			// function that generates random
			// values for that data attribute
			CpnFunction cpnFunc = cpnFuncs.next();
			if (firstTime) {
				// start writing the random values declarations block
				bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
				bw.write("\t\t\t\t<id>Random functions</id>\n");
				cpnFunc.write(bw);
				firstTime = false;
			} else {
				cpnFunc.write(bw);
			}
			if (!cpnFuncs.hasNext()) {
				// finish writing the random values declarations block
				bw.write("\t\t\t</block>\n");
			}
		}
	}

	/**
	 * Writes the block for the log declarations in the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param offset
	 *            the year offset for logging
	 * @throws IOException
	 */
	public static void writeLogDeclarations(BufferedWriter bw, int offset,
			HLTypes.TimeUnit timeunit) throws IOException {
		// start writing the log declarations block
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Log declarations</id>\n");
		// writing FOLDER val in CPN
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">val FOLDER = &quot;./logs&quot;\n");
		bw
				.write("\t\t\t\t\t\t<layout>val FOLDER = &quot;./logs&quot;</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");
		// writing PREFIX_FILE
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">val PREFIX_FILE = &quot;logsCPN&quot;\n");
		bw
				.write("\t\t\t\t\t\t<layout>val PREFIX_FILE = &quot;logsCPN&quot;</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");
		// writing FILE_EXTENSION
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">val FILE_EXTENSION = &quot;.cpnxml&quot;\n");
		bw
				.write("\t\t\t\t\t\t<layout>val FILE_EXTENSION = &quot;.cpnxml&quot;</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");
		// start writing the block for the logging functions
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>logging functions</id>\n");
		// determine the values that are needed for the time unit
		String timeUnitValue = "";
		if (timeunit == timeunit.SECONDS) {
			timeUnitValue = "";
		} else if (timeunit == timeunit.MINUTES) {
			timeUnitValue = "*60";
		} else if (timeunit == timeunit.HOURS) {
			timeUnitValue = "*60*60";
		} else if (timeunit == timeunit.DAYS) {
			timeUnitValue = "*60*60*24";
		} else if (timeunit == timeunit.WEEKS) {
			timeUnitValue = "*60*60*24*7";
		} else if (timeunit == timeunit.MONTHS) {
			timeUnitValue = "*60*60*24*7*30";
		} else if (timeunit == timeunit.YEARS) {
			timeUnitValue = "*60*60*24*7*30*12";
		}
		// write all the logging functions
		if (ManagerConfiguration.getInstance().isCurrentStateSelected()) {
			// change time stamp function to take current state offset time into
			// accout
			new CpnFunction(
					"calculateTimeStamp()",
					"\nlet\n"
							+ "\t\tval curtimeint = IntInf.toLarge(time())"
							+ timeUnitValue
							+ ";\n"
							+ "\t\tval offsettime = IntInf.toLarge(getTimeOffset(getCurrentTimeStamp()));\n"
							+ "\t\tval curtime = SMLTime.fromSeconds(curtimeint+offsettime);\n"
							+ "\t\tval curdate = Date.fromTimeLocal(curtime);\n"
							+ "\t\tval curDateOffset = Date.date{\n"
							+ "\t\t\t\t\t\t\t\t\tday = Date.day(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\thour = Date.hour(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tminute = Date.minute(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tmonth =  Date.month(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\toffset = NONE,\n"
							+ "\t\t\t\t\t\t\t\t\tsecond = Date.second(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tyear = Date.year(curdate)}\n"
							+ "\t\tval timestamp = (Date.fmt &quot;%Y-%m-%dT%H:%M:%S&quot; curDateOffset);\n"
							+ "in\n" + "\t\ttimestamp^&quot;.000+01:00&quot;\n"
							+ "end\n").write(bw);
		} else {
			// write as usually with simple year offset
			new CpnFunction(
					"calculateTimeStamp()",
					"\nlet\n"
							+ "\t\tval curtimeint = IntInf.toLarge(time())"
							+ timeUnitValue
							+ ";\n"
							+ "\t\tval curtime = SMLTime.fromSeconds(curtimeint);\n"
							+ "\t\tval curdate = Date.fromTimeLocal(curtime);\n"
							+ "\t\tval curDateOffset = Date.date{\n"
							+ "\t\t\t\t\t\t\t\t\tday = Date.day(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\thour = Date.hour(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tminute = Date.minute(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tmonth =  Date.month(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\toffset = NONE,\n"
							+ "\t\t\t\t\t\t\t\t\tsecond = Date.second(curdate),\n"
							+ "\t\t\t\t\t\t\t\t\tyear = Date.year(curdate)+"
							+ offset
							+ "}\n"
							+ "\t\tval timestamp = (Date.fmt &quot;%Y-%m-%dT%H:%M:%S&quot; curDateOffset);\n"
							+ "in\n" + "\t\ttimestamp^&quot;.000+01:00&quot;\n"
							+ "end\n").write(bw);
		}
		new CpnFunction(
				"writeTimeStamp(file_id, timestamp)",
				"\nlet\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;Timestamp&gt;&quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, timestamp)\n"
						+ "in\n"
						+ "\t\tTextIO.output(file_id, &quot;&lt;/Timestamp&gt;\\n&quot;)\n"
						+ "end\n").write(bw);
		new CpnFunction(
				"writeWorkflowModelElement(file_id, workflowModelElement)",
				"\nlet\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;WorkflowModelElement&gt;&quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, workflowModelElement)\n"
						+ "in\n"
						+ "\t\tTextIO.output(file_id, &quot;&lt;/WorkflowModelElement&gt;\\n&quot;)\n"
						+ "end\n").write(bw);
		new CpnFunction("getDescription(description)",
				"\n\t\tif length(description) = 0\n"
						+ "\t\tthen &quot;&quot;\n"
						+ "\t\telse hd(description)").write(bw);
		new CpnFunction(
				"getComplement(event, description)",
				"\nlet\n"
						+ "\t\tval desc = getDescription(description)\n"
						+ "\t\tval complement = &quot;unknowntype=\\&quot;&quot; ^ desc ^ &quot;\\&quot;&quot;\n"
						+ "in\n" + "\t\tif event = &quot;unknown&quot;\n"
						+ "\t\tthen  complement\n" + "\t\telse  &quot;&quot;\n"
						+ "end").write(bw);
		new CpnFunction(
				"writeEventType(file_id, event :: description)",
				"\nlet\n"
						+ "\t\tval complement = getComplement(event, description)\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;EventType &quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, complement)\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&gt;&quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, event)\n"
						+ "in\n"
						+ "\t\tTextIO.output(file_id, &quot;&lt;/EventType&gt;\\n&quot;)\n"
						+ "end\n" + "| writeEventType(file_id, []) = ();")
				.write(bw);
		new CpnFunction(
				"writeOriginator(file_id, Originator)",
				"\nlet\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;Originator&gt;&quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, Originator)\n"
						+ "in\n"
						+ "\t\tTextIO.output(file_id, &quot;&lt;/Originator&gt;\\n&quot;)\n"
						+ "end").write(bw);
		new CpnFunction(
				"writeDataAttributes(nil)",
				"&quot;&quot;\n"
						+ "| writeDataAttributes(name::nil) =  &quot;&lt;Attribute name = \\&quot;&quot; ^ name ^ &quot;\\&quot;&gt; &lt;/Attribute&gt;\\n&quot;\n"
						+ "| writeDataAttributes(name::value::tail) = &quot;&lt;Attribute name = \\&quot;&quot; ^ name ^ &quot;\\&quot;&gt;&quot;^value^&quot;&lt;/Attribute&gt;\\n&quot; ^ writeDataAttributes(tail)\n")
				.write(bw);
		new CpnFunction(
				"writeData(file_id, data)",
				"\nlet\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;Data&gt;\\n&quot;)\n"
						+ "\t\tval _ = TextIO.output(file_id, writeDataAttributes(data))\n"
						+ "in\n"
						+ "\t\tTextIO.output(file_id, &quot;&lt;/Data&gt;\\n&quot;)\n"
						+ "end").write(bw);
		new CpnFunction("testWriteData (file_id, data)",
				"\nif length(data) = 0\n"
						+ "then TextIO.output(file_id, &quot;&quot;)\n"
						+ "else writeData(file_id, data)\n").write(bw);
		new CpnFunction(
				"add (file_id, workflowModelElement, EventType, TimeStamp, Originator, Data)",
				"\nlet\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;AuditTrailEntry&gt;\\n&quot;)\n"
						+ "\t\tval _ = testWriteData(file_id, Data)\n"
						+ "\t\tval _ = writeWorkflowModelElement(file_id, workflowModelElement)\n"
						+ "\t\tval _ = writeEventType(file_id, EventType)\n"
						+ "\t\tval _ = writeTimeStamp(file_id, TimeStamp)\n"
						+ "\t\tval _ = writeOriginator(file_id, Originator)\n"
						+ "\t\tval _ = TextIO.output(file_id, &quot;&lt;/AuditTrailEntry&gt;\\n&quot;)\n"
						+ "in\n" + "\t\tTextIO.closeOut(file_id)\n" + "end")
				.write(bw);
		new CpnFunction(
				"addATE (caseID, workflowModelElement, EventType, TimeStamp, Originator, Data)",
				"\nlet\n"
						+ "\t\tval file_id = TextIO.openAppend(OS.Path.concat(FOLDER, PREFIX_FILE)^Int.toString(caseID)^FILE_EXTENSION)\n"
						+ "in\n"
						+ "\t\tadd(file_id, workflowModelElement, EventType, TimeStamp, Originator, Data)\n"
						+ "end").write(bw);
		new CpnFunction(
				"createCaseFile(caseID)",
				"\nlet\n"
						+ "\t\tval caseIDString = Int.toString(caseID)\n"
						+ "\t\tval file_id = TextIO.openOut(OS.Path.concat(FOLDER, PREFIX_FILE) ^ caseIDString  ^ FILE_EXTENSION)\n"
						+ "\t\tval _ = TextIO.output(file_id, caseIDString  ^ &quot;\\n&quot;)\n"
						+ "in\n" + "\t\tTextIO.closeOut(file_id)\n" + "end")
				.write(bw);
		new CpnFunction(
				"createCaseFileAddATE(caseID, workflowModelElement, EventType, TimeStamp, Originator, Data)",
				"\nlet\n"
						+ "\t\tval caseIDString = Int.toString(caseID)\n"
						+ "\t\tval file_id = TextIO.openOut(OS.Path.concat(FOLDER, PREFIX_FILE) ^ caseIDString  ^ FILE_EXTENSION)\n"
						+ "\t\tval _ = TextIO.output(file_id, caseIDString  ^ &quot;\\n&quot;)\n"
						+ "in\n"
						+ "\t\tadd(file_id, workflowModelElement, EventType, TimeStamp, Originator, Data);"
						+ "\t\tTextIO.closeOut(file_id)\n" + "end").write(bw);
		new CpnFunction(
				"fileExists(caseID)",
				"\nlet\n"
						+ "\t\tval caseIDString = Int.toString(caseID)\n"
						+ "\t\tval _ = OS.FileSys.fullPath(OS.Path.concat(FOLDER, PREFIX_FILE) ^ caseIDString  ^ FILE_EXTENSION)\n"
						+ "in\n" + "\t\ttrue\n" + "end\n"
						+ "handle OS.SysErr (msg)  =&gt;" + "\nlet\n" + "\n"
						+ "in\n" + "\t\tfalse\n" + "end").write(bw);
		// finish writing the block for the logging functions
		bw.write("\t\t\t</block>\n");
		// finish writing the log declarations block
		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the block for the current state declarations in the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeCurrentStateDeclarations(BufferedWriter bw,
			String smlName) throws IOException {
		// make file name relative
		if (smlName.contains("/")) {
			smlName = smlName.replaceAll(".*/(.*)", "$1");
		} else if (smlName.contains("\\")) {
			smlName = smlName.replaceAll(".*\\\\(.*)", "$1");
		}
		// start writing the log declarations block
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Input file declarations</id>\n");
		// writing string list type declaration
		ListColorSet listType = new ListColorSet("slist", "STRING");
		listType.write(bw);
		// writing sml file location
		bw.write("\t\t\t\t\t<use id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t\t\t<ml>&quot;" + smlName + ".sml" + "&quot;</ml>\n");
		bw.write("\t\t\t\t\t\t<layout>use &quot;" + smlName + ".sml"
				+ "&quot;;</layout>\n");
		bw.write("\t\t\t\t\t</use>\n");
		if (ManagerConfiguration.getInstance().isResourcePerspectiveEnabled()) {
			// writing get unavailable resources functions
			bw
					.write("\t\t\t\t\t<ml id=\""
							+ ManagerID.getNewID()
							+ "\">val busy:slist = getBusyResources();\nfun freeResources i = if mem busy i then false else true;\n");
			bw
					.write("\t\t\t\t\t\t<layout>val busy:slist = getBusyResources();\nfun freeResources i = if mem busy i then false else true;</layout>\n");
			bw.write("\t\t\t\t\t</ml>\n");
			// writing get FREE color set
			bw.write("\t\t\t<color id=\"" + ManagerID.getNewID() + "\">\n"
					+ "\t\t\t\t<id>" + "FREE" + "</id>\n");
			bw.write("\t\t\t\t<subset>\n" + "\t\t\t\t\t<id>" + "ANYBODY"
					+ "</id>\n" + "\t\t\t\t\t<by>" + "<ml>freeResources</ml>"
					+ "</by>\n" + "\t\t\t\t</subset>\n" + "\t\t\t\t<layout>"
					+ "colset " + "FREE" + " = "
					+ "subset ANYBODY by freeResources");
			bw.write(";</layout>\n" + "\t\t\t</color>\n");
		}
		// writing get time offset function
		bw.write("\t\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">fun getTimeOffset(s) = valOf(IntInf.fromString(s));\n");
		bw
				.write("\t\t\t\t\t\t<layout>fun getTimeOffset(s) = valOf(IntInf.fromString(s));</layout>\n");
		bw.write("\t\t\t\t\t</ml>\n");
		// finish writing the log declarations block
		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Write information about the possibility dependencies to the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter.
	 * @param idMan
	 *            ManagerID the BufferedWriter used to stream the data to the
	 *            file.
	 * @param probDep
	 *            CpnVarAndType the cpn variable that has to represent the value
	 *            for one of the possibility dependencies
	 * @throws IOException
	 */
	public static void writePossibilityDependencies(BufferedWriter bw,
			HashSet<CpnVarAndType> probDeps) throws IOException {
		// start writing the possibility declarations block
		bw.write("\t\t\t<block id=\"" + ManagerID.getNewID() + "\">\n");
		bw.write("\t\t\t\t<id>Possibility declarations</id>\n");
		Iterator<CpnVarAndType> probDepsIt = probDeps.iterator();
		while (probDepsIt.hasNext()) {
			CpnVarAndType probDep = probDepsIt.next();
			probDep.write(bw);
		}
		// finish writing the log declarations block
		bw.write("\t\t\t</block>\n");
	}

	/**
	 * Writes the instances elements of the cpn-file. This element is needed for
	 * displaying the separate models in the CPN Tools program itself.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param pn
	 *            ColoredPetriNet the Simulated Petri Net that is the root of
	 *            the hierarchical structure of ColoredPetriNet models.
	 * @throws IOException
	 */
	public static void writeInstances(BufferedWriter bw, ColoredPetriNet pn)
			throws IOException {

		/*
		 * specifies instantiations of modules (note that in the chosen
		 * representation there will be exactly one instance per page)
		 */
		// create a pageInstanceID for the page in CPN on which this Petri Net
		// will
		// be located
		String pageInstanceIDpn = ManagerID.getNewID();
		pn.addPageInstanceID(pageInstanceIDpn);
		bw.write("\t\t<instances>\n");
		bw.write("\t\t\t<instance id=\"" + pageInstanceIDpn + "\" page=\""
				+ pn.getCpnID() + "\">\n");
		writeInstance(bw, pn);
		bw.write("\t\t\t</instance>\n");
		bw.write("\t\t</instances>\n");
	}

	/**
	 * Writes the instance elements in the cpn-file. If a transition has a link
	 * to another simulatedPetriNet then an instantation of the transition has
	 * to be made in the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param pn
	 *            ColoredPetriNet the ColoredPetriNet that has transitions that
	 *            have a link to another Simulated PetriNet.
	 * @throws IOException
	 */
	public static void writeInstance(BufferedWriter bw, ColoredPetriNet pn)
			throws IOException {
		Iterator transitions = pn.getTransitions().iterator();
		while (transitions.hasNext()) {
			ColoredTransition transition = (ColoredTransition) transitions
					.next();
			if (transition.getSubpage() != null) {
				// create a pageInstanceID for the page in CPN on which the
				// subpn of this
				// transition will be located.
				String pageInstanceIDsubPN = ManagerID.getNewID();
				transition.getSubpage().addPageInstanceID(pageInstanceIDsubPN);
				// write the instance tag
				bw.write("\t\t\t\t<instance id=\"" + pageInstanceIDsubPN
						+ "\" trans= \"" + transition.getCpnID() + "\">\n");
				writeInstance(bw, transition.getSubpage());
				bw.write("\t\t\t</instance>\n");
			}
		}
	}

	/**
	 * Writes the tags for the options and binders elements at almost the end of
	 * the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public static void writeOptionsBinders(BufferedWriter bw)
			throws IOException {
		/* write options */
		bw.write("\t\t<options/>\n");
		/* write binders */
		/* specifies open windows in the tool - not used */
		bw.write("\t\t<binders/>\n");
	}

	/**
	 * Writes the fusion places to the CPN file.
	 * 
	 * @param bw
	 *            BufferedWriter
	 * @param idMan
	 *            ManagerID
	 * @param fusionPlaces
	 *            HashMap
	 * @throws IOException
	 */
	public static void writeFusionPlaces(BufferedWriter bw,
			HashMap<String, HashSet<ColoredPlace>> fusionPlaces)
			throws IOException {
		Iterator<String> keys = fusionPlaces.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			HashSet places = fusionPlaces.get(key);
			// write fusion information
			if (places.size() > 0) {
				bw.write("\t\t<fusion id=\"" + ManagerID.getNewID()
						+ "\" name=\"" + key + "\">\n");
				Iterator<ColoredPlace> it = places.iterator();
				while (it.hasNext()) {
					ColoredPlace place = it.next();
					bw
							.write("<fusion_elm idref=\"" + place.getCpnID()
									+ "\"/>");
				}
				bw.write("</fusion>\n");
			}
		}
	}

	/**
	 * Writes the end of the cpn-file. So, the tag for the indexNode element is
	 * written and also the closing tags for the cpnet and workspaceElements
	 * elements are written.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public static void writeEnd(BufferedWriter bw) throws IOException {
		/* write indexNode */
		bw.write("\t\t<IndexNode expanded=\"true\"/>\n");
		/* finish writing the cpn model */
		bw.write("\t</cpnet>\n");
		bw.write("</workspaceElements>");
	}

	/**
	 * Writes the closing tag for a page in the cpn-file.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public static void writeEndOfPage(BufferedWriter bw) throws IOException {
		bw.write("\t\t</page>\n");
	}

	/**
	 * Writes the starting tag for the monitor block
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public static void writeStartTagMonitorBlock(BufferedWriter bw)
			throws IOException {
		bw.write("\t\t<monitorblock name=\"Monitors\">\n");
	}

	/**
	 * Writes the end tag for the monitor block
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @throws IOException
	 */
	public static void writeEndTagMonitorBlock(BufferedWriter bw)
			throws IOException {
		bw.write("\t\t</monitorblock>\n");
	}

	/**
	 * Writes information that is needed for creating a monitor for a function
	 * in cpn-tools.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param nameMonitor
	 *            String the name of the monitor (usually the name of the
	 *            transition for which this monitor has been defined)
	 * @param nodeidref
	 *            String the CPN id of the transition
	 * @param pageinstanceidrefs
	 *            String the instance ID of the page on which the transition
	 *            will be located
	 * @throws IOException
	 */
	public static void writeBeginMonitorForFunction(BufferedWriter bw,
			String nameMonitor, String nodeidref,
			ArrayList<String> pageinstanceidrefs) throws IOException {
		bw.write("\t\t\t<monitor id=\"" + ManagerID.getNewID() + "\"\n");
		bw.write("\t\t\t\tname=\"" + nameMonitor + "Monitor\"\n");
		bw.write("\t\t\t\ttype=\"2\"\n");
		bw.write("\t\t\t\ttypedescription=\"User defined\"\n");
		bw.write("\t\t\t\tdisabled=\"false\">\n");
		for (int i = 0; i < pageinstanceidrefs.size(); i++) {
			bw.write("\t\t\t<node idref=\"" + nodeidref + "\"\n");
			bw.write("\t\t\t\tpageinstanceidref=\"" + pageinstanceidrefs.get(i)
					+ "\"/>\n");
		}
	}

	/**
	 * Writes the init function for the monitor (of a transition) that is used
	 * for the logging functionality in cpn-tools.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeMonitorInitFunction(BufferedWriter bw)
			throws IOException {
		bw.write("\t\t\t<declaration name=\"Init\">\n");
		new CpnFunction("init ()", "()").write(bw);
		bw.write("\t\t\t</declaration>\n");
	}

	/**
	 * Writes the init function for generating the logs folder in which the logs
	 * need to be stored.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeMonitorInitFunctionForFolderGeneration(
			BufferedWriter bw) throws IOException {
		bw.write("\t\t\t<declaration name=\"Init\">\n");
		new CpnFunction("init ()", "\t\tOS.FileSys.mkDir(FOLDER)\n"
				+ "\t\thandle Io => ()").write(bw);
		bw.write("\t\t\t</declaration>\n");
	}

	/**
	 * Writes the predicate function for the monitor (of a transition) that is
	 * used for the logging functionality in cpn-tools.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param namePage
	 *            String the name of the page on which the transition that will
	 *            be monitored is located.
	 * @param nameTransition
	 *            String the name of the transition that will be monitored.
	 * @param boundedVars
	 *            ArrayList a list of <code>String</code>s that represent the
	 *            variables that have to be bound when the transition is going
	 *            to be executed.
	 * @param numberInstantiations
	 *            the number of instantations of the transition to which the
	 *            monitor is attached.
	 * @throws IOException
	 */
	public static void writeMonitorPredicateFunction(BufferedWriter bw,
			String namePage, String nameTransition,
			ArrayList<String> boundedVars, int numberInstantiations)
			throws IOException {
		bw.write("\t\t\t<declaration name=\"Predicate\">\n");
		bw.write("\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">fun pred (bindelem) = \n");
		bw.write("let\n");
		bw.write("  fun predBindElem (" + namePage + "&apos;" + nameTransition
				+ " (1, {");
		bw.write(writeVariablesRow(boundedVars) + "})) = true\n");
		// if there are more than one instantiations
		for (int i = 2; i < numberInstantiations + 1; i++) {
			bw.write("  | predBindElem (" + namePage + "&apos;"
					+ nameTransition + " (" + i + ", {");
			bw.write(writeVariablesRow(boundedVars) + "})) = true\n");
		}
		bw.write("  | predBindElem _ = false\n");
		bw.write("in\n");
		bw.write("  predBindElem bindelem\n");
		bw.write("end\n");
		// write the layout tag
		bw.write("\t\t\t\t\t<layout>fun pred (bindelem) = \n");
		bw.write("let\n");
		bw.write("  fun predBindElem (" + namePage + "&apos;" + nameTransition
				+ " (1, {");
		// write the variables that are bounded for this transiton
		bw.write(writeVariablesRow(boundedVars) + "})) = true\n");
		for (int i = 2; i < numberInstantiations + 1; i++) {
			bw.write("  | predBindElem (" + namePage + "&apos;"
					+ nameTransition + " (" + i + ", {");
			bw.write(writeVariablesRow(boundedVars) + "})) = true\n");
		}
		bw.write("  | predBindElem _ = false\n");
		bw.write("in\n");
		bw.write("  predBindElem bindelem\n");
		bw.write("end</layout>\n");
		bw.write("");
		bw.write("\t\t\t\t</ml>\n");
		bw.write("\t\t\t</declaration>\n");
	}

	/**
	 * Writes the observer function for the monitor (of a transition) that is
	 * used for the logging functionality in cpn-tools.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param namePage
	 *            String the name of the page on which the transition that will
	 *            be monitored is located.
	 * @param nameTransition
	 *            String the name of the transition that will be monitored.
	 * @param boundedVars
	 *            ArrayList a list of <code>String</code>s that represent the
	 *            variables that have to be bound when the transition is going
	 *            to be executed.
	 * @param valuesTrue
	 *            ArrayList a list of <code>String</code>s that represent the
	 *            values (may be a variable) that are extracted from the net and
	 *            send to the action function in the case that the vars in
	 *            <code>boundedVars</code> are all bounded
	 * @param valuesFalse
	 *            ArrayList a list of <code>String</code>s that represent values
	 *            that are needed when not all vars in <code>boundedVars</code>
	 *            are bounded when the transition is executed. Although these
	 *            values will never be used, they need to be provided because of
	 *            the way how the observer function is set-up in cpn
	 *            (if-then-else). Actually, the observer function will only be
	 *            executed when the transition is executed and that means that
	 *            all variables are bounded.
	 * @param numberInstantiations
	 *            the number of instantiations of the transition to which this
	 *            monitor is attached.
	 * @throws IOException
	 */
	public static void writeMonitorObserverFunction(BufferedWriter bw,
			String namePage, String nameTransition,
			ArrayList<String> boundedVars, ArrayList<String> valuesTrue,
			ArrayList<String> valuesFalse, int numberInstantiations)
			throws IOException {
		bw.write("\t\t\t<declaration name=\"Observer\">\n");
		bw.write("\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">fun obs (bindelem) = \n");
		bw.write("let\n");
		bw.write("  fun obsBindElem (" + namePage + "&apos;" + nameTransition
				+ " (1, {");
		bw.write(writeVariablesRow(boundedVars) + "})) = ("
				+ writeVariablesRow(valuesTrue) + ")\n");
		for (int i = 2; i < numberInstantiations + 1; i++) {
			bw.write("  | obsBindElem (" + namePage + "&apos;" + nameTransition
					+ " (" + i + ", {");
			bw.write(writeVariablesRow(boundedVars) + "})) = ("
					+ writeVariablesRow(valuesTrue) + ")\n");
		}
		bw.write("  | obsBindElem _ = (" + writeVariablesRow(valuesFalse)
				+ ")\n");
		bw.write("in\n");
		bw.write("  obsBindElem bindelem\n");
		bw.write("end\n");
		bw.write("\t\t\t\t\t<layout>fun obs (bindelem) = \n");
		bw.write("let\n");
		bw.write("  fun obsBindElem (" + namePage + "&apos;" + nameTransition
				+ " (1, {");
		bw.write(writeVariablesRow(boundedVars) + "})) = ("
				+ writeVariablesRow(valuesTrue) + ")\n");
		for (int i = 2; i < numberInstantiations + 1; i++) {
			bw.write("  | obsBindElem (" + namePage + "&apos;" + nameTransition
					+ " (" + i + ", {");
			bw.write(writeVariablesRow(boundedVars) + "})) = ("
					+ writeVariablesRow(valuesTrue) + ")\n");
		}
		bw.write("  | obsBindElem _ = (" + writeVariablesRow(valuesFalse)
				+ ")\n");
		bw.write("in\n");
		bw.write("  obsBindElem bindelem\n");
		bw.write("end</layout>\n");
		bw.write("\t\t\t\t</ml>\n");
		bw.write("\t\t\t</declaration>\n");
	}

	/**
	 * Writes the action function for the monitor of the initialisation
	 * transition that will be used for creating separate files for each case
	 * that enters the net.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeMonitorActionFunctionForInit(BufferedWriter bw)
			throws IOException {
		bw.write("\t\t\t<declaration name=\"Action\">\n");
		bw
				.write("\t\t\t\t<ml id=\""
						+ ManagerID.getNewID()
						+ "\">fun action (observedval) = createCaseFile(observedval)\n");
		bw
				.write("\t\t\t\t\t<layout>fun action (observedval) = createCaseFile(observedval)</layout>\n");
		bw.write("\t\t\t\t</ml>\n");
		bw.write("\t\t\t</declaration>\n");
	}

	/**
	 * Writes the action function for the monitor (of a transition) that is used
	 * for the logging functionality in cpn-tools. The action function calls the
	 * addATE method that adds an audit trail entry to the xml file that
	 * corresponds to the case-id for which the transition is executed.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param nameTransition
	 *            String the name of the transition which needs to be monitored
	 * @param eventType
	 *            String the eventType (e.g. schedule, start, complete)
	 * @param cpnTypeOfCaseId
	 *            String the cpn-type of the case id (uppercase).
	 * @param cpnTypeOfOriginator
	 *            String the cpn-type of the originator. <code>""</code> if no
	 *            originator can be provided.
	 * @param cpnTypeOfDataVar
	 *            String the cpn-type of the variable that represents the data
	 *            attributes. <code>""</code> if no data attributes can be
	 *            provided.
	 * @param nameDataAttrAndType
	 *            HashMap if data attributes can be provided then the hashmap
	 *            should be filled with the name of the separate data attribute
	 *            and the corresponding cpn-type.
	 * @throws IOException
	 */
	public static void writeMonitorActionFunctionNormal(BufferedWriter bw,
			String nameTransition, String eventType, String cpnTypeOfCaseId,
			String cpnTypeOfOriginator, String cpnTypeOfDataVar,
			HashMap nameDataAttrAndType) throws IOException {
		bw.write("\t\t\t<declaration name=\"Action\">\n");
		bw.write("\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">fun action (f: " + cpnTypeOfCaseId);
		if (!cpnTypeOfOriginator.equals("")) {
			bw.write(",g: " + CpnUtils.getCpnValidName(cpnTypeOfOriginator));
		}
		if (!cpnTypeOfDataVar.equals("")) {
			bw.write(",h: " + cpnTypeOfDataVar);
		}
		bw.write(") = \n");
		// 18012007 The name of the transition that is written to the log file
		// by CPN tools
		// may not contain anymore as suffix the name of the eventType together
		// with the
		// underscore before it.
		// start writing ATE function
		int ti = nameTransition.lastIndexOf("_" + eventType);
		String nameTransitionWithoutEventTypeName = nameTransition.substring(0,
				ti);
		bw.write("if fileExists(f) then addATE("
				+ ateParameters(nameTransitionWithoutEventTypeName, eventType,
						cpnTypeOfOriginator, cpnTypeOfDataVar,
						nameDataAttrAndType)
				+ ")\n"
				+ "else createCaseFileAddATE("
				+ ateParameters(nameTransitionWithoutEventTypeName, eventType,
						cpnTypeOfOriginator, cpnTypeOfDataVar,
						nameDataAttrAndType) + ")");
		// layout part
		bw.write("\t\t\t\t\t<layout>fun action (f: " + cpnTypeOfCaseId);
		if (!cpnTypeOfOriginator.equals("")) {
			bw.write(",g: " + CpnUtils.getCpnValidName(cpnTypeOfOriginator));
		}
		if (!cpnTypeOfDataVar.equals("")) {
			bw.write(",h: " + cpnTypeOfDataVar);
		}
		bw.write(") = \n");
		// start writing ATE function
		bw.write("if fileExists(f) then addATE("
				+ ateParameters(nameTransitionWithoutEventTypeName, eventType,
						cpnTypeOfOriginator, cpnTypeOfDataVar,
						nameDataAttrAndType)
				+ ")\n"
				+ "else createCaseFileAddATE("
				+ ateParameters(nameTransitionWithoutEventTypeName, eventType,
						cpnTypeOfOriginator, cpnTypeOfDataVar,
						nameDataAttrAndType) + ")");
		bw.write("</layout>\n");
		bw.write("\t\t\t\t</ml>\n");
		bw.write("\t\t\t</declaration>\n");
	}

	/*
	 * Helper method to create the parameters within the addATE or
	 * createFileAddATE function, depending on what is given in the simulation
	 * model.
	 */
	private static String ateParameters(
			String nameTransitionWithoutEventTypeName, String eventType,
			String cpnTypeOfOriginator, String cpnTypeOfDataVar,
			HashMap nameDataAttrAndType) {
		String params = "";
		params = params + "f, &quot;" + nameTransitionWithoutEventTypeName
				+ "&quot;,[&quot;" + eventType
				+ "&quot;], calculateTimeStamp(), ";
		// originator
		if (!cpnTypeOfOriginator.equals("")) {
			params = params + "g,";
		} else {
			params = params + "&quot;system&quot;,";
		}
		// data attributes
		if (!cpnTypeOfDataVar.equals("")) {
			params = params + "[";
			Iterator it = nameDataAttrAndType.keySet().iterator();
			while (it.hasNext()) {
				Object nameObj = it.next();
				Object valObj = nameDataAttrAndType.get(nameObj);
				String nameObjStr = CpnUtils.getCpnValidName((String) nameObj);
				String valObjStr = CpnUtils.getCpnValidName((String) valObj);
				if (it.hasNext()) {
					params = params + "&quot;" + (String) nameObjStr
							+ "&quot;," + (String) valObjStr + ".mkstr(#"
							+ (String) nameObjStr + " h),";
				} else {
					// last one, no comma needed
					params = params + "&quot;" + (String) nameObjStr
							+ "&quot;," + (String) valObjStr + ".mkstr(#"
							+ (String) nameObjStr + " h)]";
				}
			}
		} else {
			params = params + "[]";
		}
		return params;
	}

	/**
	 * Writes the stop function for the monitor (of a transition) that is used
	 * for the logging functionality in cpn-tools. Furthermore, the closing tag
	 * for the monitor itself is written.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @throws IOException
	 */
	public static void writeMonitorStopFunctionAndEndMonitor(BufferedWriter bw)
			throws IOException {
		bw.write("\t\t\t<declaration name=\"Stop\">");
		bw.write("\t\t\t\t<ml id=\"" + ManagerID.getNewID()
				+ "\">fun stop () = ()");
		bw.write("\t\t\t\t\t<layout>fun stop () = ()</layout>");
		bw.write("\t\t\t\t</ml>");
		bw.write("\t\t\t</declaration>");
		// write the closing tag of the monitor
		bw.write("\t\t</monitor>");
	}

	/**
	 * Returns a <code>String</code> to which the <code>String</code>
	 * representation of each variable given in <code>variables</code> is
	 * appended with a comma between each separate <code>String</code> value.
	 * 
	 * @param variables
	 *            ArrayList a list with <code>String</code> values.
	 * @return String a <code>String</code> to which the <code>String</code>
	 *         representation of each variable given in <code>variables</code>
	 *         is appended with a comma between each separate
	 *         <code>String</code> value.
	 */
	private static String writeVariablesRow(ArrayList<String> variables) {
		String returnVariablesRow = "";
		Iterator<String> vars = variables.iterator();
		while (vars.hasNext()) {
			String var = vars.next();
			returnVariablesRow = returnVariablesRow + var + ",";
		}
		// remove the last , if needed
		if (returnVariablesRow.length() - 1 >= 0) {
			returnVariablesRow = returnVariablesRow.substring(0,
					returnVariablesRow.length() - 1);
		}

		return returnVariablesRow;
	}

	/**
	 * Writes the monitor for keeping track of the resources in the global
	 * resources place.
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param infoForMonitoringResources
	 *            HashMap In the case that the global fusion place is attached
	 *            to transitions the hashmap contains the cpnid of these
	 *            transitions and the corresponding pageinstanceidrefs of the
	 *            pages on which the transition is located. In the case that the
	 *            global fusion place is not attached to a transition, the
	 *            hashmap only contains the cpnid of the place and the
	 *            corresponding pageinstanceidrefs of the pages on which the
	 *            transition is located.
	 * @throws IOException
	 */
	public static void writeMarkingSizeMonitorResources(BufferedWriter bw,
			HashMap<String, ArrayList> infoForMonitoringResources)
			throws IOException {
		bw
				.write("\t\t<monitor id=\""
						+ ManagerID.getNewID()
						+ "\" name=\"Marking_size_Environment&apos;Resources_1\" type=\"0\""
						+ " typedescription=\"Marking size\" disabled=\"false\">\n");
		Iterator<String> keys = infoForMonitoringResources.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			// get the corresponding values for key
			ArrayList values = infoForMonitoringResources.get(key);
			Iterator<String> valuesIt = values.iterator();
			while (valuesIt.hasNext()) {
				String value = valuesIt.next();
				bw.write("\t\t\t<node idref=\"" + key
						+ "\" pageinstanceidref=\"" + value + "\"/>\n");
			}
		}
		bw.write("<option name=\"Logging\" value=\"true\"/>");
		bw.write("\t\t</monitor>\n");
	}

	/**
	 * Writes the monitor for calculating the throughput time to the cpn file
	 * 
	 * @param bw
	 *            BufferedWriter the BufferedWriter used to stream the data to
	 *            the file.
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param cpnID
	 *            String the cpn ID of the transition to which the monitor has
	 *            been attached.
	 * @param pageInstanceIdRefs
	 *            String the pageinstanceidref of the page on which the
	 *            transition is located
	 * @param dataVar
	 *            the cpn variable for the data. "" may be used if no data
	 *            information is available
	 * @exception IOException
	 */
	public static void writeThroughputTimeMonitor(BufferedWriter bw,
			String cpnID, ArrayList<String> pageInstanceIdRefs, String dataVar,
			TimeUnit timeUnit) throws IOException {
		bw.write("\t\t<monitor id=\"" + ManagerID.getNewID()
				+ "\" name=\"Throughput_Time_Monitor\" type=\"3\""
				+ " typedescription=\"Data collection\" disabled=\"false\">\n");
		bw.write("\t\t<node idref=\"" + cpnID + "\" pageinstanceidref=\"");
		String pageInstanceIdRefsStr = "";
		for (int i = 0; i < pageInstanceIdRefs.size(); i++) {
			if (i == pageInstanceIdRefs.size() - 1) {
				pageInstanceIdRefsStr = pageInstanceIdRefsStr
						+ pageInstanceIdRefs.get(i);
			} else {
				pageInstanceIdRefsStr = pageInstanceIdRefsStr
						+ pageInstanceIdRefs.get(i) + ",";
			}
		}
		bw.write(pageInstanceIdRefsStr + "\"/>");
		// write the predicate function
		bw.write("<declaration name=\"Predicate\">");
		String bodyPred = "\nlet\n"
				+ "\t\tfun predBindElem (Environment&apos;Clean_up (1, {c,t";
		if (!dataVar.equals("")) {
			bodyPred = bodyPred + "," + dataVar;
		}
		bodyPred = bodyPred + "})) = true\n"
				+ "\t\t\t\t\t\t| predBindElem _ = false\n" + "in\n"
				+ "\t\tpredBindElem bindelem\n" + "end";
		CpnFunction predicate = new CpnFunction("pred (bindelem) ", bodyPred);
		predicate.write(bw);
		bw.write("</declaration>");

		// write the observer function
		bw.write("<declaration name=\"Observer\">");
		String bodyObs = "\nlet\n"
				+ "\t\tfun obsBindElem (Environment&apos;Clean_up (1, {c,t";
		if (!dataVar.equals("")) {
			bodyObs = bodyObs + "," + dataVar;
		}
		bodyObs = bodyObs
				+ "})) = IntInf.-(IntInf.*(time(), valOf(IntInf.fromString(\""
				+ timeUnit.getConversionValue()
				+ "\"))), valOf(IntInf.fromString(t)))\n"
				+ "\t\t\t\t\t\t| obsBindElem _ = IntInf.fromInt(1)\n" + "in\n"
				+ "\t\tobsBindElem bindelem\n" + "end";

		CpnFunction observer = new CpnFunction("obs (bindelem)", bodyObs);
		observer.write(bw);
		bw.write("</declaration>");

		// write the init function
		bw.write("<declaration name=\"Init function\">");
		CpnFunction init = new CpnFunction("init()", "\nNONE");
		init.write(bw);
		bw.write("</declaration>");

		// write the stop function
		bw.write("<declaration name=\"Stop\">");
		CpnFunction stop = new CpnFunction("stop()", "\nNONE");
		stop.write(bw);
		bw.write("</declaration>");

		// write the remainder of the monitor
		bw.write("\t\t\t<option name=\"Timed\" value=\"false\"/>\n");
		bw.write("\t\t\t<option name=\"Logging\" value=\"true\"/>\n");
		bw.write("\t\t</monitor>\n");
	}

	/**
	 * Writes a complete place tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param placeID
	 *            the ID number for the place
	 * @param idMan
	 *            the ID Manager of the simulated Petri Net in which the place
	 *            can be found
	 * @param xPos
	 *            the position of the place on the x axis
	 * @param yPos
	 *            the position of the place on the y axis
	 * @param label
	 *            the name of the place
	 * @param width
	 *            the width of the ellipse forming the place's shape
	 * @param height
	 *            the height of the ellipse forming the place's shape
	 * @param placeType
	 *            the type of the place (must be existing color set)
	 * @param initMark
	 *            the initial marking of the place
	 * @param portType
	 *            the port type (an empty String should be passed if the place
	 *            is not a port place)
	 * @param nameFusionPlace
	 *            The name of the fusion place, <code>""</code> needs to be
	 *            provided if the place is not a fusion place.
	 * @throws IOException
	 */
	public static void writePlaceTag(BufferedWriter bw, String placeID,
			int xPos, int yPos, String label, int width, int height,
			String placeType, String initMark, String portType,
			String nameFusionPlace) throws IOException {

		label = CpnUtils.getCpnValidName(label);

		bw.write("\t\t\t<place id=\""
				+
				/* unique ID of the XML element (not displayed) */
				placeID
				+ "\">\n"
				+
				/* center position of the place */
				getElementPositionAttributeTag(xPos, yPos)
				+
				/* fill, line, and label properties of the place */
				getDefaultElementAttributeTags()
				+
				/* label of the place (to be displayed inside) */
				"\t\t\t\t<text>"
				+ label
				+ "</text>\n"
				+
				/* shape of the place (width and height) */
				"\t\t\t\t<ellipse w=\""
				+ width
				+ "\" h=\""
				+ height
				+ "\"/>\n"
				+
				/* position of token (together with attached value) */
				"\t\t\t\t<token x=\"-10.0\" y=\"0.0\"/>\n"
				+
				/*
				 * position of the attached value only (if moved without
				 * changing the position of the token)
				 */
				"\t\t\t\t<marking x=\"0.0\" y=\"0.0\" hidden=\"false\"/>\n"
				+
				// /////// type specification
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<type id=\""
				+ ManagerID.getNewID()
				+ "\">\n"
				+
				/* position of the type label for the place */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						ManagerLayout.getInstance().getPlaceTypeOffset_X(),
						ManagerLayout.getInstance().getPlaceTypeOffset_Y())
				+
				/* fill, line, and label properties of the type */
				getDefaultElementAdditionAttributeTags()
				+
				/* type of the place */
				getElementAdditionTextTag(placeType)
				+ "\t\t\t\t</type>\n"
				+
				// /////// initial marking specification
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<initmark id=\""
				+ ManagerID.getNewID()
				+ "\">\n"
				+
				/* position of the initial marking label for the place */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						ManagerLayout.getInstance().getPlaceInitMarkOffset_X(),
						ManagerLayout.getInstance().getPlaceInitMarkOffset_Y())
				+
				/* fill, line, and label properties of the initial marking */
				getDefaultElementAdditionAttributeTags() +
				/* initial marking of the place */
				getElementAdditionTextTag(initMark) + "\t\t\t\t</initmark>\n");

		// /////// port tag (only for port places)
		if (portType != "") {
			/* unique ID of the XML element (not displayed) */
			bw.write("\t\t\t\t<port id=\""
					+ ManagerID.getNewID()
					+
					/* port type: In | Out | InOut | General */
					"\" type=\""
					+ portType
					+ "\">\n"
					+
					/* position of the initial marking label for the place */
					getElementAdditionPositionAttributeTag(xPos, yPos,
							ManagerLayout.getInstance().getPlacePortOffset_X(),
							ManagerLayout.getInstance().getPlacePortOffset_Y())
					+
					/* fill, line, and label properties of the initial marking */
					getDefaultElementAdditionAttributeTags() +
					/* ??? */
					// getElementAdditionTextTag("") + NOT ALLOWED
					"\t\t\t\t</port>\n");
		}
		// ///////// fusion place information (only for fusion places)
		if (!nameFusionPlace.equals("")) {
			bw.write("\t\t\t\t<fusioninfo id=\""
					+ ManagerID.getNewID()
					+ "\""
					+
					/* name of the fusion place */
					" name=\""
					+ nameFusionPlace
					+ "\">\n"
					+
					/* position of the fusion label for the place */
					getElementAdditionPositionAttributeTag(xPos, yPos,
							ManagerLayout.getInstance()
									.getPlaceFusionOffset_X(), ManagerLayout
									.getInstance().getPlaceFusionOffset_Y()) +
					/*
					 * fill, line, and label properties of the label for the
					 * fusion place
					 */
					getDefaultElementAdditionAttributeTags()
					+ "\t\t\t\t</fusioninfo>");

		}

		bw.write("\t\t\t</place>\n");
	}

	/**
	 * Writes a complete transition tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param transitionID
	 *            the cpn-id for the transition
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param xPos
	 *            the position of the transition on the x axis
	 * @param yPos
	 *            the position of the transition on the y axis
	 * @param label
	 *            the name of the transition
	 * @param width
	 *            the width of the rectangle forming the transition's shape
	 * @param height
	 *            the height of the rectangle forming the transition's shape
	 * @param guard
	 *            the guard condition (an empty String should be passed if the
	 *            guard is not specified for this transition)
	 * @param timeDelay
	 *            the time delay specification (an empty String should be passed
	 *            if the time delay is not specified for this transition)
	 * @param codeInscription
	 *            the code inscription for this transition (an empty String
	 *            should be passed if no code inscription is specified for this
	 *            transition)
	 * @param subpageID
	 *            the complete ID of the related subpage (<code>null</code>
	 *            should be passed if the transition is no substitution
	 *            transition)
	 * @param subpageTagName
	 *            the name to be displayed as the subpage tag. Without a given
	 *            subpageID the subpageTagName will be ignored
	 * @param portsToSockets
	 *            a list specifying the assignment from port to socket places
	 *            for this substitution transition. The list should contain a
	 *            list of two ID numbers [socketIDno,portIDno] for each element.
	 *            Without a given subpageID the list will be ignored
	 * @param isInvisibleTask
	 *            <code>true</code> if the transition corresponds to an
	 *            invisible task, <code>false</code> otherwise
	 * @throws IOException
	 */
	public static void writeTransitionTag(BufferedWriter bw,
			String transitionID, int xPos, int yPos, String label, int width,
			int height, String guard, String timeDelay, String codeInscription,
			String subpageID, String subpageTagName, ArrayList portsToSockets,
			boolean isInvisibleTask) throws IOException {

		bw.write("\t\t\t<trans id=\"" +
		/* unique ID of the XML element (not displayed) */
		transitionID + "\">\n" +
		/* center position of the transition */
		getElementPositionAttributeTag(xPos, yPos));

		/* fill, line, and label properties of the transition */
		if (isInvisibleTask == true) {
			bw.write(getInvisibleTaskAttributeTags());
		} else {
			bw.write(getDefaultElementAttributeTags());
		}

		/* label of the transition (to be displayed inside) */
		bw.write("\t\t\t\t<text>" + CpnUtils.getCpnValidName(label)
				+ "</text>\n" +
				/* shape of the transition (width and height) */
				"\t\t\t\t<box w=\"" + width + "\" h=\"" + height + "\"/>\n");

		// write subst tag only if transition is substitution transition
		if (subpageID != null) {
			/* substitution tag (links to suppage) */
			bw.write("\t\t\t\t<subst subpage=\"" + subpageID +
			/*
			 * assign port places (sub page level) to sockets (super page level)
			 */
			"\" portsock=\"");

			/* for each port socket pair write entry (portID,socketID) */
			Iterator it = portsToSockets.iterator();
			while (it.hasNext()) {
				Mapping mapping = (Mapping) it.next();
				bw.write(mapping.toString());
			}

			bw.write("\">\n"
					+ "\t\t\t\t\t<subpageinfo id=\""
					+ ManagerID.getNewID()
					+ "\" name=\""
					+ CpnUtils.getCpnValidName(subpageTagName)
					+ "\">\n"
					+ "\t"
					+ getElementAdditionPositionAttributeTag(xPos, yPos,
							ManagerLayout.getInstance()
									.getTransitionSubpageinfoOffset_X(),
							ManagerLayout.getInstance()
									.getTransitionSubpageinfoOffset_Y()) +
					/* fill, line, and label properties */
					/** @todo: put one extra intent */
					getDefaultElementAdditionAttributeTags()
					+ "\t\t\t\t\t</subpageinfo>\n" + "\t\t\t\t</subst>\n");
		}
		// make sure that the < character and the > character that can appear in
		// the guard are
		// replaced by <code>&lt;</code> and <code>&gt;</code> respectively
		if (guard.contains("<")) {
			guard = guard.replace("<", "&lt;");
		}
		if (guard.contains(">")) {
			guard = guard.replace(">", "&gt;");
		}
		/* (offset) position of the possible bindings */
		bw
				.write("\t\t\t\t<binding x=\"7.0\" y=\"-3.0\"/>\n"
						+
						// /////// guard condition specification (not used)
						/* unique ID of the XML element (not displayed) */
						"\t\t\t\t<cond id=\""
						+ ManagerID.getNewID()
						+ "\">\n"
						+
						/* position of the guard label for the transition */
						getElementAdditionPositionAttributeTag(xPos, yPos,
								ManagerLayout.getInstance()
										.getTransitionConditionOffset_X(),
								ManagerLayout.getInstance()
										.getTransitionConditionOffset_Y())
						+
						/* fill, line, and label properties of the guard */
						getDefaultElementAdditionAttributeTags()
						+
						/* actual guard condition */
						getElementAdditionTextTag("[" + guard + "]")
						+ "\t\t\t\t</cond>\n"
						+
						// /////// time delay specification
						/* unique ID of the XML element (not displayed) */
						"\t\t\t\t<time id=\""
						+ ManagerID.getNewID()
						+ "\">\n"
						+
						/* position of the time label for the transition */
						getElementAdditionPositionAttributeTag(xPos, yPos,
								ManagerLayout.getInstance()
										.getTransitionTimeOffset_X(),
								ManagerLayout.getInstance()
										.getTransitionTimeOffset_Y()) +
						/* fill, line, and label properties of the time */
						getDefaultElementAdditionAttributeTags());
		/* actual time delay */
		if (timeDelay.equals("")) {
			bw.write(getElementAdditionTextTag(""));
		} else {
			bw.write(getElementAdditionTextTag("@+" + timeDelay));
		}
		bw
				.write("\t\t\t\t</time>\n"
						+
						// /////// input, output, action specification
						/* unique ID of the XML element (not displayed) */
						"\t\t\t\t<code id=\""
						+ ManagerID.getNewID()
						+ "\">\n"
						+
						/* position of the code label for the transition */
						getElementAdditionPositionAttributeTag(xPos, yPos,
								ManagerLayout.getInstance()
										.getTransitionCodeOffset_X(),
								ManagerLayout.getInstance()
										.getTransitionCodeOffset_Y())
						+
						/* fill, line, and label properties of the code */
						getDefaultElementAdditionAttributeTags()
						+
						/* actual code specification */
						getElementAdditionTextTag(codeInscription)
						+ "\t\t\t\t</code>\n"
						+
						// /////// channel specification (not used)
						/* unique ID of the XML element (not displayed) */
						"\t\t\t\t<channel id=\""
						+ ManagerID.getNewID()
						+ "\">\n"
						+
						/* position of the channel label for the transition */
						getElementAdditionPositionAttributeTag(xPos, yPos,
								ManagerLayout.getInstance()
										.getTransitionChannelOffset_X(),
								ManagerLayout.getInstance()
										.getTransitionChannelOffset_Y()) +
						/* fill, line, and label properties of the channel */
						getDefaultElementAdditionAttributeTags() +
						/* actual channel specification (always empty) */
						getElementAdditionTextTag("") + "\t\t\t\t</channel>\n"
						+ "\t\t\t</trans>\n");
	}

	/**
	 * Writes a complete arc tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param idMan
	 *            ManagerID the idManager that generates new cpn IDs.
	 * @param orientation
	 *            the orientation of the arc (PtoT | TtoP)
	 * @param transEnd
	 *            the ID number for the connected transition
	 * @param placeEnd
	 *            the ID number for the connected place
	 * @param xPos
	 *            the x coordinate of the arc inscription
	 * @param yPos
	 *            the y coordinate of the arc inscription
	 * @param inscription
	 *            the actual arc inscription (should be a valid variable or
	 *            expression)
	 * @param bendpoints
	 *            a list of bendpoints, each consisting of a list with two
	 *            elements [xCoord,yCoord] (should be null if there are no
	 *            bendpoints specified)
	 * @throws IOException
	 */
	public static void writeArcTag(BufferedWriter bw, String orientation,
			String transEnd, String placeEnd, int xPos, int yPos,
			String inscription, ArrayList bendpoints) throws IOException {

		/* unique ID of the XML element (not displayed) */
		bw.write("\t\t\t<arc id=\"" + ManagerID.getNewID() +
		/* orientation of the arc */
		"\" orientation=\"" + orientation + "\">\n"
				+ "\t\t\t\t<posattr x=\"0\" y=\"0\"/>\n" +
				/* default fill, line, and label properties */
				getDefaultElementAttributeTags() +
				/* default arc attributes */
				"\t\t\t\t<arrowattr headsize=\"1.200000\" "
				+ "currentcyckle=\"2\"/>\n" +
				/* specifies connected transition node */
				"\t\t\t\t<transend idref=\"" + transEnd + "\"/>\n" +
				/* specifies connected place node */
				"\t\t\t\t<placeend idref=\"" + placeEnd + "\"/>\n");

		// /////// bend points (optional)
		if (bendpoints != null) {
			Iterator it = bendpoints.iterator();
			while (it.hasNext()) {
				ArrayList XandY = (ArrayList) it.next();
				/* unique ID of the XML element (not displayed) */
				bw.write("\t\t\t\t<bendpoint id=\""
						+ ManagerID.getNewID()
						+ "\" serial=\"1\">\n"
						+
						// x coordinate bend point
						"\t\t\t\t\t\t<posattr x=\""
						+ XandY.get(0)
						+
						// y coordinate bend point
						"\" y=\"" + XandY.get(1) + "\"/>\n"
						+ getDefaultElementAdditionAttributeTags()
						+ "\t\t\t\t</bendpoint>\n");
			}
		}

		// /////// arc inscription
		/* unique ID of the XML element (not displayed) */
		bw.write("\t\t\t\t<annot id=\"" + ManagerID.getNewID() + "\">\n" +
		/* position of the arc inscription */
		getElementAdditionPositionAttributeTag(xPos, yPos, 0, 0)
				+ getDefaultElementAdditionAttributeTags() +
				/* the actual arc inscription (empty on this level) */
				getElementAdditionTextTag(inscription) + "\t\t\t\t</annot>\n"
				+ "\t\t\t</arc>\n");
	}

	/**
	 * Generates an empty current state file to fill the CPN model.
	 * <p>
	 * This is intended to be replaced by an actual current state file, but the
	 * empty file is needed to ensure the generated CPN being without syntax
	 * errors.
	 * 
	 * @param bw
	 *            the file handle to the current state file
	 */
	public static void writeCurrentStateFile(BufferedWriter bw)
			throws IOException {
		bw.write("fun getInitialCaseData() = [];\n\n");
		bw.write("fun getNextCaseID() = 1;\n\n");
		bw.write("fun getInitialTokensExePlace(pname:STRING) = empty;\n\n");
		bw.write("fun getInitialTokens(pname:STRING) = empty;\n\n");
		bw.write("fun getBusyResources() = [];\n\n");
		// calculate current date in seconds
		Date now = new Date();
		long milliSeconds = now.getTime();
		long seconds = milliSeconds / 1000;
		bw.write("fun getCurrentTimeStamp() = \"" + seconds + "\";\n\n");
	}

	// ////////////////////////// HELPER METHODS ///////////////////////////////
	/**
	 * Creates a position attribute specifying the given position coordinates
	 * for a graph element (i.e., a node or an edge). The generated intent is 4
	 * tabs.
	 * 
	 * @param x
	 *            the position of the node on the x axis (in pixels)
	 * @param y
	 *            the position of the node on the y axis (in pixels)
	 * @return the <code>String</code> specifying a <posattr> tag
	 */
	private static String getElementPositionAttributeTag(int x, int y) {
		return "\t\t\t\t<posattr x=\"" + x + "\" y=\"" + y + "\"/>\n";
	}

	/**
	 * Creates the default tags specifying the fill properties, the line
	 * properties, and the label properties for a graph element (i.e, a node or
	 * an edge).
	 * 
	 * @return the <code>String</code> specifying the concatenation of a
	 *         <fillattr>, <lineattr>, and <textattr> tag
	 */
	private static String getDefaultElementAttributeTags() {

		return
		/* fill properties of the node */
		"\t\t\t\t<fillattr colour=\"White\" pattern=\"\" filled=\"false\"/>\n" +
		/* line properties of the node */
		"\t\t\t\t<lineattr colour=\"Black\" thick=\"1\" type=\"Solid\"/>\n" +
		/* properties of the label of the node */
		"\t\t\t\t<textattr colour=\"Black\" bold=\"false\"/>\n";
	}

	/**
	 * Creates the tags specifying the fill properties, the line properties, and
	 * the label properties for an invisible task (that is, a transition which
	 * is not associated to any log event).
	 * 
	 * @return the <code>String</code> specifying the concatenation of a
	 *         <fillattr>, <lineattr>, and <textattr> tag
	 */
	private static String getInvisibleTaskAttributeTags() {

		return
		/* fill properties of the node */
		"\t\t\t\t<fillattr colour=\"Black\" pattern=\"\" filled=\"true\"/>\n" +
		/* line properties of the node */
		"\t\t\t\t<lineattr colour=\"Black\" thick=\"1\" type=\"Solid\"/>\n" +
		/* properties of the label of the node */
		"\t\t\t\t<textattr colour=\"White\" bold=\"false\"/>\n";
	}

	/**
	 * Creates a position attribute specifying the absolute position coordinates
	 * for a graph element addition. An addition is considered to be anything
	 * which further specifies the node or edge and appears in CPN Tools
	 * pressing TAB while the corresponding graph element is marked. Examples
	 * are the type of a place, a guard condition of a transition, or an arc
	 * inscription. The generated intent is 5 tabs.
	 * 
	 * @param x
	 *            the position of the element on the x axis (in pixels)
	 * @param y
	 *            the position of the element on the y axis (in pixels)
	 * @param offsetX
	 *            the offset of the element addition (with respect to the node
	 *            position) on the x axis
	 * @param offsetY
	 *            the offset of the element addition (with respect to the node
	 *            position) on the y axis
	 * @return the <code>String</code> specifying a <posattr> tag
	 */
	private static String getElementAdditionPositionAttributeTag(int x, int y,
			int offsetX, int offsetY) {
		return "\t\t\t\t\t<posattr x=\"" + (x + offsetX) + "\" y=\""
				+ (y + offsetY) + "\"/>\n";
	}

	/**
	 * Creates the default tags specifying the fill properties, the line
	 * properties, and the label properties for a graph element addition. An
	 * addition is considered to be anything which further specifies the node or
	 * edge and appears in CPN Tools pressing TAB while the corresponding graph
	 * element is marked. Examples are the type of a place, a guard condition of
	 * a transition, or an arc inscription.
	 * 
	 * @return the <code>String</code> specifying the concatenation of a
	 *         <fillattr>, <lineattr>, and <textattr> tag
	 */
	private static String getDefaultElementAdditionAttributeTags() {

		return
		/* fill properties */
		"\t\t\t\t\t<fillattr colour=\"White\" pattern=\"Solid\" filled=\"false\"/>\n"
				+
				/* line properties */
				"\t\t\t\t\t<lineattr colour=\"Black\" thick=\"0\" type=\"Solid\"/>\n"
				+
				/* properties of the text label */
				"\t\t\t\t\t<textattr colour=\"Black\" bold=\"false\"/>\n";
	}

	/**
	 * Creates a text tag for any graph element addition. An addition is
	 * considered to be anything which further specifies the node or edge and
	 * appears in CPN Tools pressing TAB while the corresponding graph element
	 * is marked. Examples are the type of a place, a guard condition of a
	 * transition, or an arc inscription.
	 * 
	 * @param text
	 *            the actual specification of the, e.g., guard condition, the
	 *            initial marking, or the arc inscription
	 * @return the <code>String</code> of a <text> tag containing the passed
	 *         specification
	 */
	private static String getElementAdditionTextTag(String text) {
		return "\t\t\t\t\t<text tool=\"CPN Tools\" version=\"2.0.0\">" + text
				+ "</text>\n";
	}

	/**
	 * Calculates the the position of the arc inscription on the x axis.
	 * 
	 * @param node1
	 *            the head node connected to the arc
	 * @param node2
	 *            the tail node connected to the arc
	 * @return int the position on the x axis
	 */
	public static int getXCoordinateArcInscription(int node1, int node2) {
		/* determine min and max value --> min + ((max - min) / 2) */
		if (node1 > node2) {
			return (int) (node2 + ((node1 - node2) / 2.0));
		} else { // node2 > node1
			return (int) (node1 + ((node2 - node1) / 2.0));
		}
	}

	/**
	 * Calculates the the position of the arc inscription on the y axis.
	 * 
	 * @param node1
	 *            the head node connected to the arc
	 * @param node2
	 *            the tail node connected to the arc
	 * @return int the position on the y axis
	 */
	public static int getYCoordinateArcInscription(int node1, int node2) {

		/* determine min and max value --> min + ((max - min) / 2) */
		/*
		 * move arc insription a bit up, so that it looks nicer for a horizontal
		 * arc
		 */
		if (node1 > node2) {
			return (int) (node2 + ((node1 - node2) / 2.0)) + 10;
		} else { // node2 > node1
			return (int) (node1 + ((node2 - node1) / 2.0)) + 10;
		}
	}
}

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

package org.processmining.framework.models.petrinet.algorithms;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.petrinet.*;
import att.grappa.*;

/**
 * Exports a given low-level Petri net to a coloured Petri net representation
 * that can be read by CPN Tools.
 * 
 * @see PetriNet
 * @see CpnExport
 * 
 * @author Anne Rozinat
 */
public class CpnWriter {

	/**
	 * Gives the location of the Document Type Definition (DTD) specifying the
	 * CPN Tools format.
	 */
	public final static String DtdUri = "http://www.daimi.au.dk/~cpntools/bin/DTD/4/cpn.dtd";

	/**
	 * Offset specifying the displacement of the type label for the place on the
	 * x axis. The reference point is the center position of the place itself
	 * and the value is taken from the CPN Tools default position of the type
	 * label, if the type is CASE_ID.
	 */
	private static int Place_Type_Offset_X = 52;

	/**
	 * Offset specifying the displacement of the type label for a place on the y
	 * axis. The reference point is the center position of the place itself and
	 * the value is taken from the corresponding CPN Tools default position.
	 */
	private static int Place_Type_Offset_Y = -24;

	/**
	 * Offset specifying the displacement of the initial marking label for the
	 * place on the x axis. The reference point is the center position of the
	 * place itself and the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	private static int Place_InitMark_Offset_X = 57;

	/**
	 * Offset specifying the displacement of the initial marking label for a
	 * place on the y axis. The reference point is the center position of the
	 * place itself and the the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	private static int Place_InitMark_Offset_Y = 23;

	/**
	 * Offset specifying the displacement of the port type tag for the place on
	 * the x axis. The reference point is the center position of the place
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	private static int Place_Port_Offset_X = -20;

	/**
	 * Offset specifying the displacement of the port type tag for the place on
	 * the y axis. The reference point is the center position of the place
	 * itself and the the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	private static int Place_Port_Offset_Y = -20;

	/**
	 * Offset specifying the displacement of the guard condition label for a
	 * transition on the x axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	private static int Transition_Cond_Offset_X = -39;

	/**
	 * Offset specifying the displacement of the guard condition label for a
	 * transition on the y axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	private static int Transition_Cond_Offset_Y = 31;

	/**
	 * Offset specifying the displacement of the time delay label for a
	 * transition on the x axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	private static int Transition_Time_Offset_X = 45;

	/**
	 * Offset specifying the displacement of the time delay label for a
	 * transition on the y axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	private static int Transition_Time_Offset_Y = 31;

	/**
	 * Offset specifying the displacement of the code specification for a
	 * transition on the x axis (that is input, output, action). The reference
	 * point is the center position of the transition itself and the value is
	 * taken from the corresponding CPN Tools default position.
	 */
	private static int Transition_Code_Offset_X = 65;

	/**
	 * Offset specifying the displacement of the code specification for a
	 * transition on the y axis (that is input, output, action). The reference
	 * point is the center position of the transition itself and the value is
	 * taken from the corresponding CPN Tools default position.
	 */
	private static int Transition_Code_Offset_Y = -52;

	/**
	 * Offset specifying the displacement of the channel specification for a
	 * transition on the x axis (not used). The reference point is the center
	 * position of the transition itself and the value is taken from the
	 * corresponding CPN Tools default position.
	 */
	private static int Transition_Channel_Offset_X = -64;

	/**
	 * Offset specifying the displacement of the channel specification for a
	 * transition on the y axis (not used). The reference point is the center
	 * position of the transition itself and the value is taken from the
	 * corresponding CPN Tools default position.
	 */
	private static int Transition_Channel_Offset_Y = 0;

	/**
	 * Offset specifying the displacement of the subpage tag for a transition on
	 * the x axis). The reference point is the center position of the transition
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	private static int Transition_Subpageinfo_Offset_X = 0;

	/**
	 * Offset specifying the displacement of the subpage tag for a transition on
	 * the y axis. The reference point is the center position of the transition
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	private static int Transition_Subpageinfo_Offset_Y = -32;

	/**
	 * Constant factor used to scale the nodes further away from each other.
	 */
	private static int SCALE_FACTOR = 2;

	/**
	 * The ID of the top level page of the exported CPN model. It will be used
	 * to link the module to an instance.
	 */
	private static String overviewPageID;

	/**
	 * The ID of the page containing the simulation environment of the exported
	 * CPN model. It will be used to link the module to an instance.
	 */
	private static String environmentPageID;

	/**
	 * The ID of the page containing the actual process model to be exported. It
	 * will be used to link the module to an instance.
	 */
	private static String processPageID;

	/**
	 * @todo: check whether necessary The ID of the instance built from the
	 *        Overview module. Needs to be linked in order to establish
	 *        hierarchy.
	 */
	private static String overviewInstanceID;

	/**
	 * The ID of the instance built from the Environment module. Needs to be
	 * linked in order to establish hierarchy.
	 */
	private static String environmentInstanceID;

	/**
	 * The ID of the instance built from the Process module. Needs to be linked
	 * in order to establish hierarchy.
	 */
	private static String processInstanceID;

	/**
	 * Substitution transition ID no. on top level page.
	 */
	private static int environmentTransitionID_overviewPage;

	/**
	 * Substitution transition ID no. on top level page.
	 */
	private static int processTransitionID_overviewPage;

	/**
	 * Port place ID no. on corresponding sub level page.
	 */
	private static int startPlaceID_environmentPage;

	/**
	 * Port place ID no. on corresponding sub level page.
	 */
	private static int endPlaceID_environmentPage;

	/**
	 * Port place ID no. on corresponding sub level page.
	 */
	private static int startPlaceID_processPage;

	/**
	 * Port place ID no. on corresponding sub level page.
	 */
	private static int endPlaceID_processPage;

	/**
	 * ID no. of the global fusion place for the case data.
	 */
	private static int caseDataPlaceID_environmentPage;

	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Private default constructor prevents instantiation of this class. Use the
	 * static method {@link #write write} to actually write a file.
	 */
	private CpnWriter() {
	}

	/**
	 * 
	 * @param net
	 *            PetriNet the PetriNet to be exported to CPN Tools
	 * @param bw
	 *            BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public synchronized static void write(PetriNet net, BufferedWriter bw)
			throws IOException {

		Subgraph graph = net.getGrappaVisualization().getSubgraph();
		/*
		 * an ID counter for creating unique XML element IDs across the whole
		 * document
		 */
		int i = 1;

		/* write header information */
		bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		bw
				.write("<!DOCTYPE workspaceElements PUBLIC \"-//CPN//DTD CPNXML 1.0//EN\" \""
						+ DtdUri + "\">\n\n");

		/* start writing the cpn model */
		bw.write("<workspaceElements>\n"
				+ "\t<generator tool=\"ProM\" version=\"2.2\" format=\"4\"/>\n"
				+ "\t<cpnet>\n");

		/* remember page IDs, socket IDs, etc., to establish hierarchy */
		i = assignIDs(i);

		/* write type definitions for CPN model */
		i = writeDeclarations(bw, i);
		/* write top level page (independent of exported process model) */
		i = writeOverviewPage(bw, i);
		/*
		 * write subpage handling initialization and clean-up of cases
		 * (independent of exported process model)
		 */
		i = writeEnvironmentPage(bw, i);
		/* write subpage containing the process model that is actually exported */
		i = writeProcessPage(graph, bw, i);

		/*
		 * specifies instantiations of modules (note that in the chosen
		 * representation there will be exactly one instance per page)
		 */
		bw.write("\t\t<instances>\n");
		bw.write("\t\t\t<instance id=\"" + overviewInstanceID + "\" page=\""
				+ overviewPageID + "\">\n");
		bw.write("\t\t\t\t<instance id=\"" + environmentInstanceID
				+ "\" trans=\"ID" + environmentTransitionID_overviewPage
				+ "\"/>\n");
		bw
				.write("\t\t\t\t<instance id=\"" + processInstanceID
						+ "\" trans=\"ID" + processTransitionID_overviewPage
						+ "\"/>\n");
		bw.write("\t\t\t</instance>\n");
		bw.write("\t\t</instances>\n");

		/* specifies open windows in the tool - not used */
		bw.write("\t\t<binders/>\n");

		/* finish writing the cpn model */
		bw.write("\t</cpnet>\n");
		bw.write("</workspaceElements>");
	}

	/**
	 * Generates and remembers IDs which are needed to interconnect the pages
	 * from different levels (hierarchy).
	 * 
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 *            across the whole document
	 * @return the ID counter
	 */
	private static int assignIDs(int i) {

		/* remember IDs to link pages */
		overviewPageID = "ID" + i++;
		environmentPageID = "ID" + i++;
		processPageID = "ID" + i++;

		environmentTransitionID_overviewPage = i;
		i = i + 6;
		processTransitionID_overviewPage = i;
		i = i + 6;

		/*
		 * start and end place IDs on the sub page levels (always keep following
		 * IDs for the node additions)
		 */
		startPlaceID_environmentPage = i;
		i = i + 4;
		endPlaceID_environmentPage = i;
		i = i + 4;
		startPlaceID_processPage = i;
		i = i + 4;
		endPlaceID_processPage = i;
		i = i + 4;
		caseDataPlaceID_environmentPage = i;
		i = i + 4;

		// assign the instance IDs
		overviewInstanceID = "ID" + i++;
		environmentInstanceID = "ID" + i++;
		processInstanceID = "ID" + i++;

		return i;
	}

	/**
	 * Writes the type declarations, that is the color sets of the CPN. Includes
	 * the default color sets defined by CPN Tools in a block called
	 * "Standard declarations" and in addition defines the custom color sets
	 * necessary for the chosen representation of a low-level Petri net in terms
	 * of a CPN.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 *            across the whole document
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeDeclarations(BufferedWriter bw, int i)
			throws IOException {

		bw.write("\t\t<globbox>\n");
		bw.write("\t\t\t<block id=\"ID" + (i++) + "\">\n");
		bw.write("\t\t\t\t<id>Standard declarations</id>\n");

		/**
		 * @todo Anne: provide general methods for writing types and variables
		 */

		/* default color sets defined by CPN Tools */
		bw.write("\t\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t\t<id>E</id>\n" + "\t\t\t\t\t<enum>\n"
				+ "\t\t\t\t\t\t<id>e</id>\n" + "\t\t\t\t\t</enum>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t\t<id>INT</id>\n" + "\t\t\t\t\t<int/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t\t<id>BOOL</id>\n" + "\t\t\t\t\t<bool/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t\t<id>STRING</id>\n" + "\t\t\t\t\t<string/>\n"
				+ "\t\t\t\t</color>\n");
		bw.write("\t\t\t</block>\n");

		/*
		 * custom color set defining the union of all custom attribute values
		 * contained in the case data of the model
		 */
		bw.write("\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t<id>VAL</id>\n" + "\t\t\t\t<union>\n"
				+ "\t\t\t\t\t<unionfield>\n" +
				/* dummy value as there are no custom attribute values yet */
				"\t\t\t\t\t\t<id>Dummy</id>\n" + "\t\t\t\t\t</unionfield>\n"
				+ "\t\t\t\t</union>\n"
				+ "\t\t\t\t<layout>colset VAL = union Dummy;</layout>\n"
				+ "\t\t\t</color>\n");

		/* custom color set defining a list of case attributes */
		bw
				.write("\t\t\t<color id=\"ID"
						+ (i++)
						+ "\">\n"
						+ "\t\t\t\t<id>CASE_ATTRIBUTES</id>\n"
						+ "\t\t\t\t<list>\n"
						+ "\t\t\t\t\t<id>VAL</id>\n"
						+ "\t\t\t\t</list>\n"
						+ "\t\t\t\t<layout>colset CASE_ATTRIBUTES = list VAL;</layout>\n"
						+ "\t\t\t</color>\n");

		/*
		 * custom color set defining the ID of a case (used for all the
		 * low-level places on the actual process page)
		 */
		bw.write("\t\t\t<color id=\"ID" + (i++) + "\">\n"
				+ "\t\t\t\t<id>CASE_ID</id>\n" + "\t\t\t\t<alias>\n"
				+ "\t\t\t\t\t<id>INT</id>\n" + "\t\t\t\t</alias>\n"
				+ "\t\t\t\t<layout>colset CASE_ID = INT;</layout>\n"
				+ "\t\t\t</color>\n");

		/* custom color set defining a case --> (case ID, case attributes) */
		bw
				.write("\t\t\t<color id=\"ID"
						+ (i++)
						+ "\">\n"
						+ "\t\t\t\t<id>CASE</id>\n"
						+ "\t\t\t\t<product>\n"
						+ "\t\t\t\t\t<id>CASE_ID</id>\n"
						+ "\t\t\t\t\t<id>CASE_ATTRIBUTES</id>\n"
						+ "\t\t\t\t</product>\n"
						+ "\t\t\t\t<layout>colset CASE = product CASE_ID * CASE_ATTRIBUTES;</layout>\n"
						+ "\t\t\t</color>\n");

		/*
		 * variable used to bind the CASE_ID token while moving it through the
		 * process model
		 */
		bw.write("\t\t\t<var id=\"ID" + (i++) + "\">\n" + "\t\t\t\t<type>\n"
				+ "\t\t\t\t\t<id>CASE_ID</id>\n" + "\t\t\t\t</type>\n"
				+ "\t\t\t\t<id>c</id>\n"
				+ "\t\t\t\t<layout>var c : CASE_ID;</layout>\n"
				+ "\t\t\t</var>\n");

		/*
		 * variable used to bind all the case attributes at once when cleaning
		 * up the case
		 */
		bw
				.write("\t\t\t<var id=\"ID"
						+ (i++)
						+ "\">\n"
						+ "\t\t\t\t<type>\n"
						+ "\t\t\t\t\t<id>CASE_ATTRIBUTES</id>\n"
						+ "\t\t\t\t</type>\n"
						+ "\t\t\t\t<id>attributes</id>\n"
						+ "\t\t\t\t<layout>var attributes : CASE_ATTRIBUTES;</layout>\n"
						+ "\t\t\t</var>\n");

		bw.write("\t\t</globbox>\n");
		return i;
	}

	/**
	 * Writes the top level page of the CPN model. It only contains two
	 * transitions which are substituted by the Environment and the actual
	 * Process page.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeOverviewPage(BufferedWriter bw, int i)
			throws IOException {

		// remember place and transition IDs to link sub pages and arcs
		int startPlaceID = i;
		i = i + 3;
		int endPlaceID = i;
		i = i + 3;

		bw.write("\t\t<page id=\"" + overviewPageID + "\">\n");
		bw.write("\t\t\t<pageattr name=\"Overview\"/>\n");

		/* write Start place */
		writePlaceTag(bw, startPlaceID, -289, -10, "Start", 48, 48, "CASE_ID",
				"", "");
		/* write End place */
		writePlaceTag(bw, endPlaceID, 65, -10, "End", 48, 48, "CASE_ID", "", "");

		/* write Environment (substitution) transition */
		ArrayList portToSocketList = new ArrayList();
		ArrayList portToSocket1 = new ArrayList();
		portToSocket1.add("ID" + endPlaceID_environmentPage);
		portToSocket1.add("ID" + endPlaceID);
		ArrayList portToSocket2 = new ArrayList();
		portToSocket2.add("ID" + startPlaceID_environmentPage);
		portToSocket2.add("ID" + startPlaceID);
		portToSocketList.add(portToSocket1);
		portToSocketList.add(portToSocket2);
		writeTransitionTag(bw, environmentTransitionID_overviewPage, -116, 98,
				"Environment", 176, 38, "", environmentPageID, "Environment",
				portToSocketList, false);

		/* write Process (substitution) transition */
		portToSocketList = new ArrayList();
		portToSocket1 = new ArrayList();
		portToSocket1.add("ID" + endPlaceID_processPage);
		portToSocket1.add("ID" + endPlaceID);
		portToSocket2 = new ArrayList();
		portToSocket2.add("ID" + startPlaceID_processPage);
		portToSocket2.add("ID" + startPlaceID);
		portToSocketList.add(portToSocket1);
		portToSocketList.add(portToSocket2);
		writeTransitionTag(bw, processTransitionID_overviewPage, -116, -10,
				"Process", 176, 94, "", processPageID, "Process",
				portToSocketList, false);

		/* write arc 1 */
		ArrayList bendpoints = new ArrayList();
		ArrayList bendpoint1 = new ArrayList();
		bendpoint1.add("65");
		bendpoint1.add("98");
		bendpoints.add(bendpoint1);
		i = writeArcTag(bw, i, "PtoT", environmentTransitionID_overviewPage,
				endPlaceID, 0, 0, "", bendpoints);

		/* write arc 2 */
		bendpoints = new ArrayList();
		bendpoint1 = new ArrayList();
		bendpoint1.add("-289");
		bendpoint1.add("98");
		bendpoints.add(bendpoint1);
		i = writeArcTag(bw, i, "TtoP", environmentTransitionID_overviewPage,
				startPlaceID, 0, 0, "", bendpoints);

		/* write arc 3 */
		i = writeArcTag(bw, i, "PtoT", processTransitionID_overviewPage,
				startPlaceID, 0, 0, "", null);

		/* write arc 4 */
		i = writeArcTag(bw, i, "TtoP", processTransitionID_overviewPage,
				endPlaceID, 0, 0, "", null);

		bw.write("\t\t</page>\n");
		return i;
	}

	/**
	 * Writes the page of the CPN model which contains the simulation
	 * environment for the exported process. It generates cases and initializes
	 * their case data.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeEnvironmentPage(BufferedWriter bw, int i)
			throws IOException {

		// remember IDs to link arcs
		int caseCounterPlaceID = i;
		i = i + 3;
		int initTransitionID = i;
		i = i + 5;
		int cleanupTransitionID = i;
		i = i + 5;

		bw.write("\t\t<page id=\"" + environmentPageID + "\">\n");
		bw.write("\t\t\t<pageattr name=\"Environment\"/>\n");

		/* write Start place (PORT place) */
		writePlaceTag(bw, startPlaceID_environmentPage, -292, -11, "Start", 48,
				48, "CASE_ID", "", "Out");
		/* write End place (PORT place) */
		writePlaceTag(bw, endPlaceID_environmentPage, 259, -14, "End", 48, 48,
				"CASE_ID", "", "In");
		/* write case data place (FUSION) */
		writePlaceTag(bw, caseDataPlaceID_environmentPage, -18, 143,
				"Case data", 120, 58, "CASE", "", "");
		/* write case ID counting place (initial marking is 1) */
		writePlaceTag(bw, caseCounterPlaceID, -18, 263, "next\ncase ID", 120,
				58, "CASE_ID", "1", "");
		/* write init transition (generating and initializing cases) */
		writeTransitionTag(bw, initTransitionID, -292, 140, "Init", 92, 56, "",
				null, null, null, false);
		/* write cleanup transition (removing cases) */
		writeTransitionTag(bw, cleanupTransitionID, 259, 139, "Clean-up", 92,
				56, "", null, null, null, false);

		/* write arc Init --> Start */
		i = writeArcTag(bw, i, "TtoP", initTransitionID,
				startPlaceID_environmentPage, -317, 54, "c", null);
		/* write arc Init --> Casedata */
		i = writeArcTag(bw, i, "TtoP", initTransitionID,
				caseDataPlaceID_environmentPage, -157, 129, "(c,[])", null);
		/* write arc Casedata --> Cleanup */
		i = writeArcTag(bw, i, "PtoT", cleanupTransitionID,
				caseDataPlaceID_environmentPage, 122, 129, "(c,attributes)",
				null);
		/* write arc End --> Cleanup */
		i = writeArcTag(bw, i, "PtoT", cleanupTransitionID,
				endPlaceID_environmentPage, 284, 61, "c", null);
		/* write arc Init --> CaseIDcounter */
		i = writeArcTag(bw, i, "TtoP", initTransitionID, caseCounterPlaceID,
				-185, 212, "c+1", null);
		/* write arc CaseIDcounter --> Init */
		ArrayList bendpoints = new ArrayList();
		ArrayList bendpoint1 = new ArrayList();
		bendpoint1.add("-292");
		bendpoint1.add("263");
		bendpoints.add(bendpoint1);
		i = writeArcTag(bw, i, "PtoT", initTransitionID, caseCounterPlaceID,
				-317, 214, "c", bendpoints);

		bw.write("\t\t</page>\n");
		return i;
	}

	/**
	 * Writes the subpage in the CPN model that actually contains the process
	 * model which is exported.
	 * 
	 * @param graph
	 *            the graph structure containing the process model
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeProcessPage(Subgraph graph, BufferedWriter bw, int i)
			throws IOException {

		bw.write("\t\t<page id=\"" + processPageID + "\">\n");
		bw.write("\t\t\t<pageattr name=\"Process\"/>\n");

		// include the actual model
		i = writePlaces(graph, bw, i);
		i = writeTransitions(graph, bw, i);
		i = writeArcs(graph, bw, i);

		bw.write("\t\t</page>\n");
		return i;
	}

	/**
	 * Writes the places of the Petri net to be exported. Re-uses graphical
	 * information assigned by the DOT graph layouter used within the ProM
	 * framework.
	 * 
	 * @param graph
	 *            the graph structure containing the process model
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writePlaces(Subgraph graph, BufferedWriter bw, int i)
			throws IOException {

		/*
		 * on this graph level places and transitons cannot be distinguished
		 * (only nodes and edges)
		 */
		Enumeration e = graph.nodeElements();

		// first, enumerate the nodes on this level
		while (e.hasMoreElements()) {
			Element el = (Element) e.nextElement();

			if (el.object != null && el.object instanceof Place) {
				Node n = (Node) el;
				Place p = (Place) el.object;
				// nodes are scaled further away from each other
				int x = (int) n.getCenterPoint().getX() * SCALE_FACTOR;
				// invert the y axis (everything would be upside down otherwise)
				int y = -(int) n.getCenterPoint().getY() * SCALE_FACTOR;
				// width and height are scaled a bit since the label will be
				// put inside
				int w = ((int) (((Double) n
						.getAttributeValue(Grappa.WIDTH_ATTR)).doubleValue() * 120.0));
				int h = ((int) (((Double) n
						.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * 120.0));

				/*
				 * if place is Start place of the process -> use pre-assigned ID
				 */
				if (p.inDegree() == 0) {
					p.setNumber(startPlaceID_processPage);
					writePlaceTag(bw, p.getNumber(), x, y, p.getIdentifier(),
							w, h, "CASE_ID", "", "In");
				}
				/*
				 * if place is End place of the process -> use pre-assigned ID
				 */
				else if (p.outDegree() == 0) {
					p.setNumber(endPlaceID_processPage);
					writePlaceTag(bw, p.getNumber(), x, y, p.getIdentifier(),
							w, h, "CASE_ID", "", "Out");
				}
				/*
				 * els use running counter to generate unique IDs for each XML
				 * element. Since the <type> and the <initmark> elements do also
				 * get such an ID, increment accordingly. Remember the counter
				 * for the node in order to be able to make a reference to it
				 * when writing the edges.
				 */
				else {
					p.setNumber(i);
					writePlaceTag(bw, p.getNumber(), x, y, p.getIdentifier(),
							w, h, "CASE_ID", "", "");
					i = i + 3;
				}
			}
		}

		// now enumerate the nodes on lower levels (graph may be hierarchical)
		e = graph.subgraphElements();
		while (e.hasMoreElements()) {
			i = writePlaces((Subgraph) e.nextElement(), bw, i);
		}

		return i;
	}

	/**
	 * Writes the transitions of the Petri net to be exported. Re-uses graphical
	 * information assigned by the DOT graph layouter used within the ProM
	 * framework.
	 * 
	 * @param graph
	 *            the graph structure containing the process model
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeTransitions(Subgraph graph, BufferedWriter bw, int i)
			throws IOException {

		/*
		 * on this graph level places and transitons cannot be distinguished
		 * (only nodes and edges)
		 */
		Enumeration e = graph.nodeElements();

		// first, enumerate the nodes on this level
		while (e.hasMoreElements()) {
			Element el = (Element) e.nextElement();

			if (el.object != null && el.object instanceof Transition) {
				Node n = (Node) el;
				Transition t = (Transition) el.object;
				// nodes are scaled further away from each other
				int x = (int) n.getCenterPoint().getX() * SCALE_FACTOR;
				// invert the y axis (everything would be upside down otherwise)
				int y = -(int) n.getCenterPoint().getY() * SCALE_FACTOR;
				// width and height are scaled a bit since the label will be
				// put inside
				int w = (int) (((Double) n.getAttributeValue(Grappa.WIDTH_ATTR))
						.doubleValue() * 100.0);
				int h = (int) (((Double) n
						.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue() * 100.0);
				/*
				 * use counter to generate unique IDs for each XML element.
				 * Since the <cond>, <time>, <code> and <channel> elements do
				 * also get such an ID, increment accordingly. Remember the
				 * counter for the node in order to be able to make a reference
				 * to it when writing the edges.
				 */
				t.setNumber(i);
				i = i + 5;
				// no guard, no substitution transition
				boolean isInvisible = ((Transition) el.object)
						.isInvisibleTask();
				// writeTransitionTag(bw, t.getNumber(), x, y,
				// t.getIdentifier(), w, h, "", null, null, null);
				writeTransitionTag(bw, t.getNumber(), x, y, labelHack(t
						.getIdentifier()), w, h, "", null, null, null,
						isInvisible);
			}
		}

		// now enumerate the nodes on lower levels (graph may be hierarchical)
		e = graph.subgraphElements();
		while (e.hasMoreElements()) {
			i = writeTransitions((Subgraph) e.nextElement(), bw, i);
		}

		return i;
	}

	/**
	 * Writes the arcs of the Petri net to be exported.
	 * 
	 * @param graph
	 *            the graph structure containing the process model
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param i
	 *            the global ID counter used to uniquely identify XML elements
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeArcs(Subgraph graph, BufferedWriter bw, int i)
			throws IOException {

		Enumeration e = graph.edgeElements();

		while (e.hasMoreElements()) {
			Edge edge = (Edge) e.nextElement();
			Node head = edge.getHead();
			Node tail = edge.getTail();
			int transIDno;
			int placeIDno;
			String orientation;

			if (edge.goesForward()) {
				if (tail.object instanceof Transition) {
					transIDno = ((Transition) tail.object).getNumber();
					placeIDno = ((Place) head.object).getNumber();
					/* arc is directed from transition to place */
					orientation = "TtoP";
				} else {
					placeIDno = ((Place) tail.object).getNumber();
					transIDno = ((Transition) head.object).getNumber();
					/* arc is directed from place to transition */
					orientation = "PtoT";
				}
			} else {
				if (head.object instanceof Transition) {
					transIDno = ((Transition) head.object).getNumber();
					placeIDno = ((Place) tail.object).getNumber();
					/* arc is directed from transition to place */
					orientation = "TtoP";
				} else {
					placeIDno = ((Place) head.object).getNumber();
					transIDno = ((Transition) tail.object).getNumber();
					/* arc is directed from place to transition */
					orientation = "PtoT";
				}
			}
			// actually write edge to file
			i = writeArcTag(bw, i, orientation, transIDno, placeIDno,
					getXCoordinateArcInscription(head, tail),
					getYCoordinateArcInscription(head, tail), "c", null);
		}

		// now enumerate the edges on lower levels (graph may be hierarchical)
		e = graph.subgraphElements();
		while (e.hasMoreElements()) {
			i = writeArcs((Subgraph) e.nextElement(), bw, i);
		}

		return i + 3;
	}

	// ////////////////////////// HELPER METHODS ///////////////////////////////

	/**
	 * Writes a complete place tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param placeIDno
	 *            the ID number for the place (actual ID is "ID" + placeIDno)
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
	 * @param portType
	 *            the port type (an empty String should be passed if the place
	 *            is not a port place)
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writePlaceTag(BufferedWriter bw, int placeIDno,
			int xPos, int yPos, String label, int width, int height,
			String placeType, String initMark, String portType)
			throws IOException {

		bw.write("\t\t\t<place id=\"ID"
				+
				/* unique ID of the XML element (not displayed) */
				placeIDno++
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
				"\t\t\t\t<type id=\"ID"
				+ placeIDno++
				+ "\">\n"
				+
				/* position of the type label for the place */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Place_Type_Offset_X, Place_Type_Offset_Y) +
				/* fill, line, and label properties of the type */
				getDefaultElementAdditionAttributeTags()
				+
				/* type of the place */
				getElementAdditionTextTag(placeType)
				+ "\t\t\t\t</type>\n"
				+
				// /////// initial marking specification
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<initmark id=\"ID"
				+ placeIDno++
				+ "\">\n"
				+
				/* position of the initial marking label for the place */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Place_InitMark_Offset_X, Place_InitMark_Offset_Y) +
				/* fill, line, and label properties of the initial marking */
				getDefaultElementAdditionAttributeTags() +
				/* initial marking of the place */
				getElementAdditionTextTag(initMark) + "\t\t\t\t</initmark>\n");

		// /////// port tag (only for port places)
		if (portType != "") {
			/* unique ID of the XML element (not displayed) */
			bw.write("\t\t\t\t<port id=\"ID"
					+ placeIDno++
					+
					/* port type: In | Out | InOut | General */
					"\" type=\""
					+ portType
					+ "\">\n"
					+
					/* position of the initial marking label for the place */
					getElementAdditionPositionAttributeTag(xPos, yPos,
							Place_Port_Offset_X, Place_Port_Offset_Y) +
					/* fill, line, and label properties of the initial marking */
					getDefaultElementAdditionAttributeTags() +
					/* ??? */
					getElementAdditionTextTag("") + "\t\t\t\t</port>\n");
		}

		bw.write("\t\t\t</place>\n");
		return placeIDno;
	}

	/**
	 * Writes a complete transition tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param transIDno
	 *            the ID number for the transition (actual ID is "ID" +
	 *            transIDno)
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
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeTransitionTag(BufferedWriter bw, int transIDno,
			int xPos, int yPos, String label, int width, int height,
			String guard, String subpageID, String subpageTagName,
			ArrayList portsToSockets, boolean isInvisibleTask)
			throws IOException {

		bw.write("\t\t\t<trans id=\"ID" +
		/* unique ID of the XML element (not displayed) */
		transIDno++ + "\">\n" +
		/* center position of the transition */
		getElementPositionAttributeTag(xPos, yPos));

		/* fill, line, and label properties of the transition */
		if (isInvisibleTask == true) {
			bw.write(getInvisibleTaskAttributeTags());
		} else {
			bw.write(getDefaultElementAttributeTags());
		}

		/* label of the transition (to be displayed inside) */
		bw.write("\t\t\t\t<text>" + label + "</text>\n" +
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
				ArrayList portSocketPair = (ArrayList) it.next();
				bw.write("(" + portSocketPair.get(0) + ","
						+ portSocketPair.get(1) + ")");
			}

			bw.write("\">\n"
					+ "\t\t\t\t\t<subpageinfo id=\"ID"
					+ transIDno++
					+ "\" name=\""
					+ subpageTagName
					+ "\">\n"
					+ "\t"
					+ getElementAdditionPositionAttributeTag(xPos, yPos,
							Transition_Subpageinfo_Offset_X,
							Transition_Subpageinfo_Offset_Y) +
					/* fill, line, and label properties */
					/** @todo: put one extra intent */
					getDefaultElementAdditionAttributeTags()
					+ "\t\t\t\t\t</subpageinfo>\n" + "\t\t\t\t</subst>\n");
		}
		/* (offset) position of the possible bindings */
		bw.write("\t\t\t\t<binding x=\"7.0\" y=\"-3.0\"/>\n" +
		// /////// guard condition specification (not used)
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<cond id=\"ID"
				+ transIDno++
				+ "\">\n"
				+
				/* position of the guard label for the transition */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Transition_Cond_Offset_X, Transition_Cond_Offset_Y)
				+
				/* fill, line, and label properties of the guard */
				getDefaultElementAdditionAttributeTags()
				+
				/* actual guard condition */
				getElementAdditionTextTag(guard)
				+ "\t\t\t\t</cond>\n"
				+
				// /////// time delay specification (not used)
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<time id=\"ID"
				+ transIDno++
				+ "\">\n"
				+
				/* position of the time label for the transition */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Transition_Time_Offset_X, Transition_Time_Offset_Y)
				+
				/* fill, line, and label properties of the time */
				getDefaultElementAdditionAttributeTags()
				+
				/* actual time delay (always empty) */
				getElementAdditionTextTag("")
				+ "\t\t\t\t</time>\n"
				+
				// /////// input, output, action specification (not used)
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<code id=\"ID"
				+ transIDno++
				+ "\">\n"
				+
				/* position of the code label for the transition */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Transition_Code_Offset_X, Transition_Code_Offset_Y)
				+
				/* fill, line, and label properties of the code */
				getDefaultElementAdditionAttributeTags()
				+
				/* actual code specification (always empty) */
				getElementAdditionTextTag("")
				+ "\t\t\t\t</code>\n"
				+
				// /////// channel specification (not used)
				/* unique ID of the XML element (not displayed) */
				"\t\t\t\t<channel id=\"ID"
				+ transIDno++
				+ "\">\n"
				+
				/* position of the channel label for the transition */
				getElementAdditionPositionAttributeTag(xPos, yPos,
						Transition_Channel_Offset_X,
						Transition_Channel_Offset_Y) +
				/* fill, line, and label properties of the channel */
				getDefaultElementAdditionAttributeTags() +
				/* actual channel specification (always empty) */
				getElementAdditionTextTag("") + "\t\t\t\t</channel>\n"
				+ "\t\t\t</trans>\n");

		return transIDno;
	}

	/**
	 * Writes a complete arc tag in the CPN Tools format.
	 * 
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @param arcIDno
	 *            the ID number for the arc (actual ID is "ID" + arcIDno)
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
	 * @return the ID counter
	 * @throws IOException
	 */
	private static int writeArcTag(BufferedWriter bw, int arcIDno,
			String orientation, int transEnd, int placeEnd, int xPos, int yPos,
			String inscription, ArrayList bendpoints) throws IOException {

		/* unique ID of the XML element (not displayed) */
		bw.write("\t\t\t<arc id=\"ID" + arcIDno++ +
		/* orientation of the arc */
		"\" orientation=\"" + orientation + "\">\n"
				+ "\t\t\t\t<posattr x=\"0\" y=\"0\"/>\n" +
				/* default fill, line, and label properties */
				getDefaultElementAttributeTags() +
				/* default arc attributes */
				"\t\t\t\t<arrowattr headsize=\"1.200000\" "
				+ "currentcyckle=\"2\"/>\n" +
				/* specifies connected transition node */
				"\t\t\t\t<transend idref=\"ID" + transEnd + "\"/>\n" +
				/* specifies connected place node */
				"\t\t\t\t<placeend idref=\"ID" + placeEnd + "\"/>\n");

		// /////// bend points (optional)
		if (bendpoints != null) {
			Iterator it = bendpoints.iterator();
			while (it.hasNext()) {
				ArrayList XandY = (ArrayList) it.next();
				/* unique ID of the XML element (not displayed) */
				bw.write("\t\t\t\t<bendpoint id=\"ID"
						+ arcIDno++
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
		bw.write("\t\t\t\t<annot id=\"ID" + arcIDno++ + "\">\n" +
		/* position of the arc inscription */
		getElementAdditionPositionAttributeTag(xPos, yPos, 0, 0)
				+ getDefaultElementAdditionAttributeTags() +
				/* the actual arc inscription (empty on this level) */
				getElementAdditionTextTag(inscription) + "\t\t\t\t</annot>\n"
				+ "\t\t\t</arc>\n");

		return arcIDno;
	}

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
	 * Calculates the the position of the arc inscription on the x axis.
	 * 
	 * @param trans
	 *            the head node connected to the arc
	 * @param place
	 *            the tail node connected to the arc
	 * @return int the position on the x axis
	 */
	private static int getXCoordinateArcInscription(Node head, Node tail) {
		// node positions have been scaled by SCALE_FACTOR
		int headX = (int) head.getCenterPoint().getX() * SCALE_FACTOR;
		int tailX = (int) tail.getCenterPoint().getX() * SCALE_FACTOR;

		/* determine min and max value --> min + ((max - min) / 2) */
		if (headX > tailX) {
			return (int) (tailX + ((headX - tailX) / 2.0));
		} else {
			return (int) (headX + ((tailX - headX) / 2.0));
		}
	}

	/**
	 * Calculates the the position of the arc inscription on the y axis.
	 * 
	 * @param trans
	 *            the head node connected to the arc
	 * @param place
	 *            the tail node connected to the arc
	 * @return int the position on the y axis
	 */
	private static int getYCoordinateArcInscription(Node head, Node tail) {
		// node positions have been scaled by SCALE_FACTOR
		int headY = -(int) head.getCenterPoint().getY() * SCALE_FACTOR;
		int tailY = -(int) tail.getCenterPoint().getY() * SCALE_FACTOR;

		/* determine min and max value --> min + ((max - min) / 2) */
		/*
		 * move arc insription a bit up, so that it looks nicer for a horizontal
		 * arc
		 */
		if (headY > tailY) {

			return (int) (tailY + ((headY - tailY) / 2.0)) + 10;
		} else {
			return (int) (headY + ((tailY - headY) / 2.0)) + 10;
		}
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
		return "\t\t\t\t\t<text tool=\"CPN Tools\" version=\"1.4.0\">" + text
				+ "</text>\n";
	}

	/**
	 * @todo remove this hack
	 * @param input
	 *            String
	 * @return String
	 */
	private static String labelHack(String input) {
		return input.replaceAll("\\\\n", "\n");
	}
}

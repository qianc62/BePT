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

package org.processmining.framework.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;

import att.grappa.Edge;
import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.Node;
import att.grappa.Subgraph;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import qc.common.Common;

//import com.chinamobile.bpmspace.core.util;

/*
 * September 5, 2005, Eric Verbeek
 * Added functionality to cope with weighted arcs (duplicate edges) in PetriNets.
 * Any PetriNet will set the weightedArc attribute, which effectively disables
 * the check on multiple arcs in the ModelGraph.
 */

/**
 * Encapsulates a general graph, and it is used as a base class for all types of
 * graphs in the processmining project.
 * <p>
 * Since it extends <code>Graph</code>, all functionality of the GRAPPA graph
 * library is available.
 * <p>
 * In addition, a <code>ModelGraph</code> provides a method
 * <code>getGrappaVisualization</code> to create a visualization of the
 * underlying graph. First, the DOT representation as given by the
 * <code>writeToDot</code> method is layed out with the dot.exe tool. The output
 * of dot.exe is then visualized using the Grappa package.
 * 
 * Note that the visualization depends on the <code>writeToDot</code> method.
 * Subclasses that want to customize the visualization should override the
 * <code>writeToDot</code> method and customize the DOT representation, instead
 * of overriding the <code>getGrappaVisualization</code> method.
 */

/*
 * Code rating
 * 
 * DotFileAttributeWriter 20071122, hverbeek: Red (working on writeToDot)
 */
public class ModelGraph extends Graph implements DotFileWriter,
		DotFileAttributeWriter {

	public final static int EXITS_REGION = 1;
	public final static int ENTERS_REGION = 2;
	public final static int DOESNTCROSS_REGION = 3;

	public Graph visualObject;

	protected String identifier = "";
	/**
	 * @todo anne: this should be reset to private access as soon as the cloning
	 *       problem is resolved for PetriNet (see, e.g.,
	 *       PetriNet.addAndLinkTransition)
	 */
	protected ArrayList vertices;
	private ArrayList edges;

	/*
	 * September 27, 2007, Eric Verbeek Added dummy vertices and dummy edges.
	 * These vertices and edges are only used when writing to Dot.
	 */
	private ArrayList<ModelGraphVertex> dummyVertices;
	private ArrayList<ModelGraphEdge> dummyEdges;

	/*
	 * September 27, 2007, Eric Verbeek Added attributes used when writing to
	 * Dot.
	 */
	private HashMap<String, String> graphAttributes;
	private HashMap<String, String> nodeAttributes;
	private HashMap<String, String> edgeAttributes;

	private ArrayList<ModelGraphCluster> clusters;

	/*
	 * September 5, 2005, Eric Verbeek Added weightedArcs attribute. If set,
	 * multiple arcs between two nodes should be allowed. If not set, multiple
	 * arcs should not be allowed. By default, it is not set. Any PetriNet will
	 * set it on construction.
	 */
	private boolean weightedArcs = false;
	protected final static HashMap nodeMapping = new HashMap();

	public void clearModelGraph() {
		clusters = null;
		dummyEdges = null;
		dummyVertices = null;
		edgeAttributes = null;
		edges = null;
		graphAttributes = null;
		nodeAttributes = null;
		vertices = null;
		visualObject = null;
	}

	/**
	 * September 5, 2005, Eric Verbeek Sets whether multiple arcs should be
	 * allowed or not.
	 * 
	 * @param w
	 *            specifies whether multiple arcs should be allowed.
	 */
	public void setWeightedArcs(boolean w) {
		weightedArcs = w;
	}

	/**
	 * September 5, 2005, Eric Verbeek Gets whether multiple arcs should be
	 * allowed or not.
	 * 
	 * @return whether multiple arcs are allowed.
	 */
	public boolean getWeightedArcs() {
		return weightedArcs;
	}

	public void setIdentifier(String s) {
		identifier = s;
	}

	public ArrayList<ModelGraphVertex> getVerticeList() {
		return vertices;
	}

	public ArrayList getEdges() {
		return edges;
	}

	public ArrayList<PNEdge> getPNEdges() {
		ArrayList<PNEdge> edgeList = new ArrayList<>();
		for ( Object obj: edges ) {
			PNEdge edge = (PNEdge)obj;
			edgeList.add( edge );
		}
		return edgeList;
	}

	// added by Mariska Netjes
	public ArrayList<ModelGraphEdge> getRealEdges() {
		return edges;
	}

	public ModelGraphVertex addVertex(ModelGraphVertex v) {
		vertices.add(v);
		super.addNode(v);
		return v;
	}

	public void removeVertex(ModelGraphVertex v) {
		Enumeration e = v.edgeElements();
		while (e.hasMoreElements()) {
			removeEdge((ModelGraphEdge) e.nextElement());
		}
		vertices.remove(v);
		super.removeNode(v.getName());
	}

	/**
	 * Added by Mariska Netjes Returns the first source found in the nodes of
	 * this model graph
	 * 
	 * @return A source of this Petri net
	 */
	public ModelGraphVertex getSource() {
		for (Object object : vertices) {
			ModelGraphVertex v = (ModelGraphVertex) object;
			if (v.inDegree() == 0) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Added by Mariska Netjes Returns the first sink found in the nodes of this
	 * model graph
	 * 
	 * @return A sink of this Petri net
	 */
	public ModelGraphVertex getSink() {
		for (Object object : vertices) {
			ModelGraphVertex v = (ModelGraphVertex) object;
			if (v.outDegree() == 0) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Added by Mariska Netjes
	 * 
	 * @return startNodes All start nodes of a net
	 */
	public HashSet<ModelGraphVertex> getStartNodes() {
		HashSet<ModelGraphVertex> startNodes = new HashSet<ModelGraphVertex>();
		for (Object object : vertices) {
			ModelGraphVertex v = (ModelGraphVertex) object;
			if (v.inDegree() == 0) {
				startNodes.add(v);
			}
		}
		return startNodes;
	}

	/**
	 * Added by Mariska Netjes
	 * 
	 * @return endNodes All end nodes of a net
	 */
	public HashSet<ModelGraphVertex> getEndNodes() {
		HashSet<ModelGraphVertex> endNodes = new HashSet<ModelGraphVertex>();
		for (Object object : vertices) {
			ModelGraphVertex v = (ModelGraphVertex) object;
			if (v.outDegree() == 0) {
				endNodes.add(v);
			}
		}

		return endNodes;
	}

	public String getIdentifier() {
		return identifier;
	}

	public ModelGraph(String graphName) {
		super(graphName, true, false);
		clearGraph();
		clearDotAttributes();
		setIdentifier(graphName);
	}

	/*
	 * public boolean isIsomorphic(ModelGraph G, ArrayList mapping, ArrayList
	 * exclude) { // mapping is assumed to be empty and if the result is true,
	 * // it is filled with Pairs of Vertices, giving an isomorphic mapping,
	 * which // is not allready contained in exclude.
	 * 
	 * return false; }
	 * 
	 * public boolean isIsomorphic(ModelGraph G, ArrayList mapping) { return
	 * isIsomorphic(G, mapping, new ArrayList()); }
	 */

	/**
	 * September 5, 2005, Eric Verbeek OK for multiple arcs.
	 * 
	 * Gets the set of edges from the first node to the second node.
	 * 
	 * @param v1
	 *            the first node
	 * @param v2
	 *            the second node
	 * @return the set of edges from the first node to the second node
	 */
	public HashSet getEdgesBetween(ModelGraphVertex v1, ModelGraphVertex v2) {
		HashSet s = new HashSet();
		Iterator it = v1.getOutEdgesIterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			if (e.getDest() == v2) {
				s.add(e);
			}
		}
		return s;
	}

	/**
	 * September 5, 2005, Eric Verbeek Renamed from hasEdge to getFirstEdge
	 * 
	 * Gets the first edge from the first node to the second node.
	 * 
	 * @param source
	 *            the first node
	 * @param destination
	 *            the second node
	 * @return the first edge from the first node to the second node
	 */
	public ModelGraphEdge getFirstEdge(ModelGraphVertex source,
			ModelGraphVertex destination) {
		Iterator it = source.getOutEdgesIterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			if (e.getDest() == destination) {
				return e;
			}
		}
		return null;
	}

	public void removeEdges(ArrayList edges) {
		Iterator it = edges.iterator();
		while (it.hasNext()) {
			removeEdge((ModelGraphEdge) it.next());
		}
	}

	public void delEdge(ModelGraphVertex source, ModelGraphVertex destination) {
		ModelGraphEdge e = getFirstEdge(source, destination);
		if (e != null) {
			removeEdge(e);
		}
	}

	public void removeEdge(ModelGraphEdge e) {
		super.removeEdge(e.getName());
		e.getSource().removeEdge(e, false);
		e.getDest().removeEdge(e, true);
		edges.remove(e);
	}

	public int getNumberOfEdges() {
		return edges.size();
	}

	/**
	 * Adds an edge from the first node to the second node, if no such adge
	 * already exists. If such an edge already exists, the ModelGraph's
	 * weightedArc attribute specifies whether a duplicate edge is added (true)
	 * or not (false).
	 * 
	 * @param source
	 *            the first node
	 * @param destination
	 *            the second node
	 * @return the created edge (could be null if weightedArc is not set!)
	 */
	public ModelGraphEdge addEdge(ModelGraphVertex source,
										ModelGraphVertex destination) {
		/*
		 * September 5, 2005, Eric Verbeek Check the weightedArc attribute. If
		 * set, add the edge even if sich an edge already exists.
		 */
		if (!weightedArcs) {
			ModelGraphEdge e = getFirstEdge(source, destination);
			if (e != null) {
				return e;
			}
		}
		return addEdge(new ModelGraphEdge(source, destination));
	}

	public ModelGraphEdge addEdge(ModelGraphEdge e) {
		super.addEdge(e);
		edges.add(e);
		return e;
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
	public void writeToDot(Writer bw) throws IOException {
		ArrayList a = new ArrayList();
		ArrayList s = getVerticeList();

		Message.add("writeToDot", Message.DEBUG);

		bw.write("digraph G {\n");
		writePreambleAttributes(bw);

		nodeMapping.clear();
		/**
		 * All nodes, edges, and clusters have a predefined dot id.
		 */
		for (ModelGraphVertex vertex : dummyVertices) {
			bw.write("\tnode" + vertex.getIdKey());
			bw.write(" [");
			writeVertexAttributes(bw, vertex);
			bw.write("];\n");
		}
		for (Object object : vertices) {
			ModelGraphVertex vertex = (ModelGraphVertex) object;
			bw.write("\tnode" + vertex.getIdKey());
			bw.write(" [");
			writeVertexAttributes(bw, vertex);
			bw.write("];\n");
			nodeMapping.put(new String("node" + vertex.getIdKey()), vertex);
		}
		for (ModelGraphEdge edge : dummyEdges) {
			bw.write("\tnode" + edge.getTail().getIdKey() + "-> node"
					+ edge.getHead().getIdKey());
			bw.write(" [");
			writeEdgeAttributes(bw, edge);
			bw.write("];\n");
		}
		for (Object object : edges) {
			ModelGraphEdge edge = (ModelGraphEdge) object;
			bw.write("\tnode" + edge.getTail().getIdKey() + "-> node"
					+ edge.getHead().getIdKey());
			bw.write(" [");
			writeEdgeAttributes(bw, edge);
			bw.write("];\n");
		}
		for (ModelGraphCluster cluster : clusters) {
			bw.write("\tsubgraph \"cluster_" + cluster.getLabel() + "\" {\n");
			writeClusterAttributes(bw, cluster);
			for (ModelGraphVertex vertex : cluster.getVertices()) {
				bw.write("\t\tnode" + vertex.getIdKey() + ";\n");
			}
			bw.write("\t}\n");
		}
		bw.write("}\n");
	}

	/**
	 * Writes preamble (default) attributes
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 * 
	 *             Code rating: Red Review rating: Red
	 */
	public void writePreambleAttributes(Writer bw) throws IOException {
		for (String attribute : graphAttributes.keySet()) {
			bw.write("\t" + attribute + "=\""
					+ graphAttributes.get(attribute).replaceAll("\"", "\\\\\"")
					+ "\";\n");
		}
		String prefix = "\tedge [";
		for (String attribute : edgeAttributes.keySet()) {
			bw.write(prefix + attribute + "=\""
					+ edgeAttributes.get(attribute).replaceAll("\"", "\\\\\"")
					+ "\"");
			prefix = ",";
		}
		if (prefix == ",") {
			bw.write("];\n");
		}
		prefix = "\tnode [";
		for (String attribute : nodeAttributes.keySet()) {
			bw.write(prefix + attribute + "=\""
					+ nodeAttributes.get(attribute).replaceAll("\"", "\\\\\"")
					+ "\"");
			prefix = ",";
		}
		if (prefix == ",") {
			bw.write("];\n");
		}
	}

	/**
	 * Writes vertex attributes
	 * 
	 * @param bw
	 *            Writer
	 * @param vertex
	 *            ModelGraphVertex
	 * @throws IOException
	 * 
	 *             Code rating: Red Review rating: Red
	 */
	public void writeVertexAttributes(Writer bw, ModelGraphVertex vertex)
			throws IOException {
		bw.write(vertex.getDotAttributes());
	}

	/**
	 * Writes edge attributes
	 * 
	 * @param bw
	 *            Writer
	 * @param edge
	 *            ModelGraphEdge
	 * @throws IOException
	 * 
	 *             Code rating: Red Review rating: Red
	 */
	public void writeEdgeAttributes(Writer bw, ModelGraphEdge edge)
			throws IOException {
		bw.write(edge.getDotAttributes());
	}

	/**
	 * Writes cluster attributes
	 * 
	 * @param bw
	 *            Writer
	 * @param cluster
	 *            ModelGraphCluster
	 * @throws IOException
	 * 
	 *             Code rating: Red Review rating: Red
	 */
	public void writeClusterAttributes(Writer bw, ModelGraphCluster cluster)
			throws IOException {
		String prefix = "\t\t";
		for (String attribute : cluster.getAttributes().keySet()) {
			bw.write(prefix
					+ attribute
					+ "=\""
					+ cluster.getAttributes().get(attribute).replaceAll("\"",
							"\\\\\"") + "\";\n");
			prefix = "\t\t,";
		}
	}

	/**
	 * Returns a grappa panel containing the visualization of the graph. Note
	 * that the visualization is created from the DOT representation that is
	 * created with the <code>writeToDot</code> method. Subclasses that want to
	 * customize the visualization should override the <code>writeToDot</code>
	 * method instead of this one.
	 * 
	 * @return the visualization of this graph in a grappa panel
	 */
	public final ModelGraphPanel getGrappaVisualization() {
		BufferedWriter bw;
		Graph graph;
		File dotFile;

		try {
			// create temporary DOT file
			//File tempdot = new File(ModelGraph.getWebAppRoot() + System.getProperty("file.separator") + "tempdot");
			//tempdot.mkdirs();
			//dotFile = File.createTempFile("pmt", ".dot", tempdot);
			dotFile = File.createTempFile("pmt", ".dot", new File("."));

			// dotFile.deleteOnExit();
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			writeToDot(bw);
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			Message.add("Parsing DOT-file: " + dotFile.getAbsolutePath(),
					Message.DEBUG);
			graph = Dot.execute(dotFile.getAbsolutePath());
			dotFile.delete();

			visualObject = graph;
			Iterator it = getVerticeList().iterator();
			while (it.hasNext()) {
				((ModelGraphVertex) it.next()).visualObject = null;
			}
			it = getEdges().iterator();
			while (it.hasNext()) {
				((ModelGraphEdge) it.next()).visualObject = null;
			}

			if (graph == null) {
				return null;
			}

		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}

		buildNodeMapping(graph);
		buildEdgeMapping(graph);

		// adjust some settings
		graph.setEditable(true);
		graph.setMenuable(true);
		graph.setErrorWriter(new PrintWriter(System.err, true));

		// create the visual component and return it
		ModelGraphPanel gp = new ModelGraphPanel(graph, this);

		gp.setScaleToFit(true);

		gp.addGrappaListener(new GrappaAdapter());

		return gp;
	}

	/**
	 * Returns a mapping of all nodes and edges of the graph given in subgraph
	 * onto the original nodes and edges in the ModelGraph. The mapping is
	 * stored in the Object field.
	 */
	protected void buildNodeMapping(Subgraph graph) {
		// First, enumerate the nodes on this level
		Enumeration e = graph.nodeElements();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			n.object = nodeMapping.get(n.getName());
			if ((n.object == null) || !(n.object instanceof ModelGraphVertex)) {
				continue;
			}
			((ModelGraphVertex) n.object).visualObject = n;
		}

		// Now enumerate the nodes on lower levels
		e = graph.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph g = (Subgraph) e.nextElement();
			buildNodeMapping(g);
		}
	}

	protected void buildEdgeMapping(Subgraph graph) {
		Enumeration e = graph.edgeElements();
		while (e.hasMoreElements()) {
			Edge edge = (Edge) e.nextElement();
			Node n1 = edge.getTail();
			Node n2 = edge.getHead();

			ModelGraphVertex v1 = (ModelGraphVertex) n1.object;
			ModelGraphVertex v2 = (ModelGraphVertex) n2.object;

			if ((v1 == null) || (v1.getOutEdges() == null) || (v2 == null)) {
				continue;
			}

			ModelGraphEdge edge2 = null;
			Iterator it = v1.getOutEdges().iterator();
			boolean found = false;
			while (!found && it.hasNext()) {
				edge2 = (ModelGraphEdge) it.next();
				found = (edge2.getDest() == v2) && (edge2.visualObject == null);
			}
			edge.object = edge2;
			if (edge2 != null) {
				edge2.visualObject = edge;
			}
		}

		// Now enumerate the edges on lower levels
		e = graph.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph g = (Subgraph) e.nextElement();
			buildEdgeMapping(g);
		}
	}

	public DoubleMatrix2D getShortestDistances() {
		int size = vertices.size();
		DoubleMatrix2D distances = DoubleFactory2D.dense.make(size, size, 0);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i == j) {
					continue;
				}
				if (getFirstEdge((ModelGraphVertex) vertices.get(i),
						(ModelGraphVertex) vertices.get(j)) != null) {
					distances.set(i, j, 1);
				} else {
					distances.set(i, j, -1);
				}
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; i < size; i++) {
				for (int k = 0; i < size; i++) {
					int old = (int) distances.get(j, k);
					int new_ji = (int) distances.get(j, i);
					int new_ik = (int) distances.get(i, k);
					if ((new_ji == -1) || (new_ik == -1)) {
						continue;
					}
					if (((new_ji + new_ik) < old) || (old == -1)) {
						distances.set(j, k, new_ji + new_ik);
					}
				}
			}
		}
		return distances;
	}

	/**
	 * Makes a shallow copy of the object. Note that cloning on this level is
	 * not supported but that the lists of vertices and edges are reset to the
	 * empty list, respectively, in order to properly support cloning for
	 * deriving subclasses.
	 * 
	 * @returnthe cloned object
	 */
	protected Object clone() {
		ModelGraph o = null;
		try {
			o = (ModelGraph) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// reset referenced lists to support reconstruction for deriving
		// subclasses
		o.edges = new ArrayList();
		o.vertices = new ArrayList();
		o.dummyEdges = new ArrayList();
		o.dummyVertices = new ArrayList();
		o.clusters = new ArrayList<ModelGraphCluster>();
		return o;
	}

	/**
	 * This method calculates all minimal regions of this graph. A region is a
	 * set of nodes, such that for each objects in the given list of objects
	 * holds that: 1) All edges corresponding to that object have a source in
	 * the region and a destination outside of the region 2) All edges
	 * corresponding to that object have a source outside of the region and a
	 * destination inside the region 3) All edges corresponding to that object
	 * have a source in the region and a destination inside the region 4) All
	 * edges corresponding to that object have a source outside of the region
	 * and a destination outside of the region
	 * 
	 * @param objects
	 *            The list of objects to which edges can refer
	 * @return An ArrayList<HashSet>, containing HashSets of states in the
	 *         statespace.
	 */
	public RegionList calculateMinimalRegions(Collection objects) {
		synchronized (vertices) {
			RegionList result = new RegionList();
			calculateRegions(result, new HashSet(0), vertices.size() - 1,
					vertices, true, vertices.size(), objects);
			return result;
		}
	}

	/**
	 * This method calculates all regions of this graph. A region is a set of
	 * nodes, such that for each objects in the given list of objects holds
	 * that: 1) All edges corresponding to that object have a source in the
	 * region and a destination outside of the region 2) All edges corresponding
	 * to that object have a source outside of the region and a destination
	 * inside the region 3) All edges corresponding to that object have a source
	 * in the region and a destination inside the region 4) All edges
	 * corresponding to that object have a source outside of the region and a
	 * destination outside of the region
	 * 
	 * @param objects
	 *            The list of objects to which edges can refer
	 * @return An ArrayList<HashSet>, containing HashSets of states in the
	 *         statespace.
	 */
	public RegionList calculateAllRegions(Collection objects) {
		synchronized (vertices) {
			RegionList result = new RegionList();
			result.add(new Region(new HashSet(), new HashSet()));
			calculateRegions(result, new HashSet(0), vertices.size() - 1,
					vertices, false, vertices.size(), objects);
			return result;
		}
	}

	/**
	 * This method calculates all non-complementary regions of this graph. A
	 * region is a set of nodes, such that for each objects in the given list of
	 * objects holds that: 1) All edges corresponding to that object have a
	 * source in the region and a destination outside of the region 2) All edges
	 * corresponding to that object have a source outside of the region and a
	 * destination inside the region 3) All edges corresponding to that object
	 * have a source in the region and a destination inside the region 4) All
	 * edges corresponding to that object have a source outside of the region
	 * and a destination outside of the region
	 * 
	 * @param objects
	 *            The list of objects to which edges can refer
	 * @return An ArrayList<HashSet>, containing HashSets of states in the
	 *         statespace.
	 */
	public RegionList calculateAllNonCompRegions(Collection objects) {
		synchronized (vertices) {
			RegionList result = new RegionList();
			result.add(new Region(new HashSet(), new HashSet()));
			calculateRegions(result, new HashSet(0), vertices.size() - 1,
					vertices, false, vertices.size() / 2, objects);
			return result;
		}
	}

	/**
	 * This method calculates all regions of this graph that contain at most
	 * <code>size</code> states. A region is a set of nodes, such that for each
	 * objects in the given list of objects holds that: 1) All edges
	 * corresponding to that object have a source in the region and a
	 * destination outside of the region 2) All edges corresponding to that
	 * object have a source outside of the region and a destination inside the
	 * region 3) All edges corresponding to that object have a source in the
	 * region and a destination inside the region 4) All edges corresponding to
	 * that object have a source outside of the region and a destination outside
	 * of the region
	 * 
	 * @param objects
	 *            The list of objects to which edges can refer
	 * @param size
	 *            the maximum size of the region
	 * @return An ArrayList<HashSet>, containing HashSets of states in the
	 *         statespace.
	 */
	public RegionList calculateRegionsMaxSize(Collection objects, int size) {
		synchronized (vertices) {
			RegionList result = new RegionList();
			result.add(new Region(new HashSet(), new HashSet()));
			calculateRegions(result, new HashSet(0), vertices.size() - 1,
					vertices, false, size, objects);
			return result;
		}
	}

	private void calculateRegions(RegionList result,
			HashSet<ModelGraphVertex> currentSet, int currentIndex,
			final ArrayList states, boolean minimalOnly, int comp,
			final Collection objects) {
		if (currentSet.size() >= comp) {
			return;
		}
		for (int i = currentIndex; i >= 0; i--) {
			// Add state i to the set
			HashSet newSet = new HashSet(currentSet.size() + 1);
			newSet.addAll(currentSet);
			newSet.add(states.get(i));
			// Check if this set is a region
			Region r = isRegion(newSet, objects);
			if (r != null) {
				if (!minimalOnly) {
					calculateRegions(result, newSet, i - 1, states,
							minimalOnly, comp, objects);
					result.add(r);
				} else {
					result.addAndRemoveLarger(r);
				}
			} else {
				calculateRegions(result, newSet, i - 1, states, minimalOnly,
						comp, objects);
			}
		}
	}

	/**
	 * Determines if a set of nodes is a region. A set of states is a region if
	 * for each object in the list objects: 1) All edges corresponding to that
	 * object have a source in the region and a destination outside of the
	 * region 2) All edges corresponding to that object have a source outside of
	 * the region and a destination inside the region 3) All edges corresponding
	 * to that object have a source in the region and a destination inside the
	 * region 4) All edges corresponding to that object have a source outside of
	 * the region and a destination outside of the region
	 * 
	 * @param possibleRegion
	 *            HashSet
	 * @param objects
	 *            the arraylist containing the objects to which edges can refer
	 * @return Region, null if this set is not a region, an object otherwise
	 */
	private Region isRegion(HashSet<ModelGraphVertex> possibleRegion,
			Collection objects) {
		Iterator it = objects.iterator();
		HashSet internal = new HashSet();
		HashSet external = new HashSet();
		HashSet input = new HashSet();
		HashSet output = new HashSet();

		boolean isOK = true;
		while (it.hasNext() && isOK) {
			ArrayList edges = getAllEdges(it.next());
			if (edges.size() == 0) {
				continue;
			}
			// Now check if for all edges, either:
			// The source is in the possibleRegion,
			// The destination is in the possibleRegion
			// They are both outside
			// They are both inside
			ModelGraphEdge e = (ModelGraphEdge) edges.get(0);

			boolean s = possibleRegion.contains(e.getSource());
			boolean d = possibleRegion.contains(e.getDest());

			int status;
			if (s == d) {
				status = DOESNTCROSS_REGION;
				if (s) {
					internal.add(e.object);
				} else {
					external.add(e.object);
				}
			} else if (s) {
				status = EXITS_REGION;
				output.add(e.object);
			} else {
				status = ENTERS_REGION;
				input.add(e.object);
			}

			boolean stop = false;
			for (int i = 1; i < edges.size() && !stop; i++) {
				e = (ModelGraphEdge) edges.get(i);

				boolean s2 = possibleRegion.contains(e.getSource());
				boolean d2 = possibleRegion.contains(e.getDest());

				int status2;
				if (s2 == d2) {
					status2 = DOESNTCROSS_REGION;
				} else if (s2) {
					status2 = EXITS_REGION;
				} else {
					status2 = ENTERS_REGION;
				}
				stop = (status != status2);
			}
			if (stop) {
				return null;
			}
		}
		return new Region(input, output, possibleRegion);
	}

	/**
	 * Returns a HashSet containing all objects that appear on edges, i.e.
	 * through the edge.object method.
	 * 
	 * @return HashSet
	 */
	public HashSet getEdgeObjects() {
		HashSet result = new HashSet();
		Iterator it = getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			if (e.object != null) {
				result.add(e.object);
			}
		}
		return result;
	}

	/**
	 * Returns all edges in the statespace that refer to the given object
	 * 
	 * @param o
	 *            Object
	 * @return ArrayList
	 */
	public ArrayList getAllEdges(Object o) {
		ArrayList edges = new ArrayList();
		Iterator it = getEdges().iterator();
		while (it.hasNext()) {
			ModelGraphEdge e = (ModelGraphEdge) it.next();
			if ((e.object != null) && (e.object.equals(o))) {
				edges.add(e);
			}
		}
		return edges;
	}

	/**
	 * This method returns a subset of the given set of regions, which
	 * represents the PreRegions of the given object.
	 * 
	 * @param regions
	 *            ArrayList
	 * @param t
	 *            Object
	 * @return ArrayList
	 */
	public RegionList getPreRegions(RegionList regions, Object t) {
		RegionList result = new RegionList();
		ArrayList edges = getAllEdges(t);

		Iterator<Region> it2 = regions.iterator();
		while (it2.hasNext()) {
			Region region = it2.next();
			Iterator it = edges.iterator();
			boolean ok = false;
			while (it.hasNext() && !ok) {
				ModelGraphEdge e = (ModelGraphEdge) it.next();
				if (region.contains(e.getSource())
						&& !region.contains(e.getDest())) {
					// There is an edge labeled with trasition t, coming
					// from this region, going out of this region.
					result.add(region);
					ok = true;
				}
			}
		}
		return result;
	}

	/**
	 * This method returns a subset of the given set of regions, which
	 * represents the PreRegions of the given object.
	 * 
	 * @param regions
	 *            ArrayList
	 * @param t
	 *            Transition
	 * @return ArrayList
	 */
	public RegionList getPostRegions(RegionList regions, Object t) {
		RegionList result = new RegionList();
		ArrayList edges = getAllEdges(t);

		Iterator<Region> it2 = regions.iterator();
		while (it2.hasNext()) {
			Region region = it2.next();
			Iterator it = edges.iterator();
			boolean ok = false;
			while (it.hasNext() && !ok) {
				ModelGraphEdge e = (ModelGraphEdge) it.next();
				if (!region.contains(e.getSource())
						&& region.contains(e.getDest())) {
					// There is an edge labeled with trasition t, coming
					// from outside this region, going into this region.
					result.add(region);
					ok = true;
				}
			}
		}
		return result;
	}

	/**
	 * Transitively reduces this graph, i.e. removes an edge if there is another
	 * path between two nodes. The result is unique for a-cyclic graphs
	 */
	public void reduceTransitively() {

		for (ModelGraphVertex v : getVerticeList()) {
			if (v.outDegree() <= 1) {
				continue;
			}
			HashSet<Long> removed = new HashSet();
			for (ModelGraphVertex w : v.getVerticesOnlySuccessor()) {
				if (!removed.contains(w.getIdKey())) {
					// There is an edge v -> w
					HashSet<Long> done = new HashSet();
					done.add(v.getIdKey());
					done.add(w.getIdKey());
					removeDirectSuc(v, w, done, removed);
				}
			}
		}
	}

	private void removeDirectSuc(ModelGraphVertex v, ModelGraphVertex w,
			HashSet<Long> done, HashSet<Long> removed) {
		for (ModelGraphVertex x : w.getVerticesOnlySuccessor()) {
			// There is a path: v -..-> w -> x
			if (!done.contains(x.getIdKey())) {
				done.add(x.getIdKey());

				ModelGraphEdge e = getFirstEdge(v, x);
				if (e == null) {
					// There is NO edge v -> x;
				} else {
					// There is an edge v -> x
					removeEdge(e);
					removed.add(x.getIdKey());
				}
				if (v.outDegree() > 1) {
					// There might be more edges to remove
					removeDirectSuc(v, x, done, removed);
				}
			}
		}
	}

	/**
	 * Print key indicators of the graph to the Test tab.
	 * 
	 * @param tag
	 *            String The tag to use for the indicators.
	 */
	public void Test(String tag) {
		Message.add("<" + tag + " nofNodes=\"" + vertices.size()
				+ "\" nofEdges=\"" + edges.size() + "\"/>", Message.TEST);
	}

	/*
	 * September 27, 2007, Eric Verbeek Added dummy vertices and dummy edges.
	 * These vertices and edges are only used when writing to Dot.
	 */
	public void addDummy(ModelGraphVertex vertex) {
		dummyVertices.add(vertex);
	}

	public void addDummy(ModelGraphEdge edge) {
		dummyEdges.add(edge);
	}

	public void clearGraph() {
		vertices = new ArrayList<ModelGraphVertex>();
		edges = new ArrayList<ModelGraphEdge>();
		dummyVertices = new ArrayList<ModelGraphVertex>();
		dummyEdges = new ArrayList<ModelGraphEdge>();
		clusters = new ArrayList<ModelGraphCluster>();
	}

	/*
	 * September 27, 2007, Eric Verbeek Added attributes used when writing to
	 * Dot.
	 */
	public void setDotAttribute(String attribute, String value) {
		// Message.add("setDotAttribute("+ attribute + "," + value + ")",
		// Message.DEBUG);
		graphAttributes.put(attribute, value);
	}

	public void setDotNodeAttribute(String attribute, String value) {
		// Message.add("setDotNodeAttribute("+ attribute + "," + value + ")",
		// Message.DEBUG);
		nodeAttributes.put(attribute, value);
	}

	public void setDotEdgeAttribute(String attribute, String value) {
		// Message.add("setDotEdgeAttribute("+ attribute + "," + value + ")",
		// Message.DEBUG);
		edgeAttributes.put(attribute, value);
	}

	public void clearDotAttributes() {

		// Message.add("clearDotAtributes", Message.DEBUG);

		graphAttributes = new HashMap<String, String>();
		graphAttributes.put("fontsize", "8");
		graphAttributes.put("remincross", "true");
		graphAttributes.put("fontname", "Arial");
		graphAttributes.put("rankdir", "TB");

		nodeAttributes = new HashMap<String, String>();
		nodeAttributes.put("height", ".8");
		nodeAttributes.put("width", ".2");
		nodeAttributes.put("fontname", "Arial");
		nodeAttributes.put("fontsize", "8");

		edgeAttributes = new HashMap<String, String>();
		edgeAttributes.put("arrowsize", ".7");
		edgeAttributes.put("fontname", "Arial");
		edgeAttributes.put("fontsize", "8");
	}

	public void addCluster(ModelGraphCluster cluster) {
		if (clusters.contains(cluster)) {
			return;
		}
		clusters.add(cluster);
	}

	public ArrayList<ModelGraphCluster> get_Clusters() {
		return clusters;
	}
	
	// get webapp root
	public static String getWebAppRoot() {
		Map<String, String> envMap = System.getenv();
		if(envMap.containsKey("PROCESSPROFILE")) {
			String processProfile = envMap.get("PROCESSPROFILE");
			if(!processProfile.endsWith(File.separator)) {
				processProfile += File.separator;
			}
			return processProfile;
		}
		return "";
	}
}

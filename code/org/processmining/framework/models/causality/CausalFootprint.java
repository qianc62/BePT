package org.processmining.framework.models.causality;

import java.util.*;

import org.processmining.framework.models.*;

import java.io.IOException;
import java.io.Writer;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.CancelationComponent;
import java.io.Serializable;
import java.io.OutputStream;
import org.processmining.framework.log.LogFilter;
import org.w3c.dom.Node;
import org.processmining.framework.log.filter.LogFilterCollection;
import java.io.BufferedWriter;
import org.processmining.framework.util.StringNormalizer;

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
 * 
 * 
 * @author not attributable
 * @version 1.0
 */
public class CausalFootprint extends ModelGraph implements Serializable {

	public ArrayList<ForwardEdge> forwardEdges = new ArrayList();
	public ArrayList<BackwardEdge> backwardEdges = new ArrayList();
	private ArrayList<ModelGraphVertex> baseObjects = new ArrayList();
	private ArrayList<ModelGraphVertex> vertices = new ArrayList();
	private HashMap node2forward = new HashMap();
	private HashMap node2backward = new HashMap();
	private ModelGraphVertex target;
	private ModelGraphVertex source;
	private ModelGraph baseModel;

	CausalFootprint(String graphName, ModelGraph baseModel) {
		super(graphName);
		this.baseModel = baseModel;
		target = new ModelGraphVertex(this);
		target.setIdentifier("sink");
		source = new ModelGraphVertex(this);
		source.setIdentifier("source");
		node2forward.put(target.getIdKey(), new HashSet());
		node2backward.put(source.getIdKey(), new HashSet());
		node2forward.put(source.getIdKey(), new HashSet());
		node2backward.put(target.getIdKey(), new HashSet());
	}

	public ModelGraph getBaseModel() {
		return baseModel;
	}

	public void displayBackwardEdge() {
		Iterator it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge f = (BackwardEdge) it.next();
			Iterator<ModelGraphVertex> s = f.sources.iterator();
			while (s.hasNext()) {
				System.out.print(s.next().getIdentifier());
			}
			System.out.print(",");
			System.out.print(f.destination.getIdentifier());
			System.out.println();
		}
	}

	public void displayForwardEdge() {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge f = (ForwardEdge) it.next();
			System.out.print(f.source.getIdentifier());
			Iterator<ModelGraphVertex> de = f.destinations.iterator();
			System.out.print(",");
			while (de.hasNext()) {
				System.out.print(de.next().getIdentifier());
			}
			System.out.println();
		}
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	public void writeXML(BufferedWriter output) throws IOException {
		output.write("<causalfootprint name=\"" + getName() + "\">\n");
		output.write("<vertices>\n");
		for (ModelGraphVertex v : vertices) {
			writeVertexXML(v, output);
		}
		output.write("</vertices>\n");
		output.write("<forwardedges>\n");
		for (ForwardEdge edge : forwardEdges) {
			output.write("<forwardedge>\n");
			edge.writeXML(output);
			output.write("</forwardedge>\n");
		}
		output.write("</forwardedges>\n");
		output.write("<backwardedges>\n");
		for (BackwardEdge edge : backwardEdges) {
			output.write("<backwardedge>\n");
			edge.writeXML(output);
			output.write("</backwardedge>\n");
		}
		output.write("</backwardedges>\n");
		output.write("</causalfootprint>\n");
	}

	void writeVertexXML(ModelGraphVertex v, BufferedWriter output)
			throws IOException {
		output.write("<vertex id=\"" + v.getIdKey() + "\" ");
		output.write("identifier=\""
				+ StringNormalizer.escapeXMLCharacters(v.getIdentifier())
				+ "\" ");
		if (v != source && v != target) {
			ModelGraphVertex base = getBaseVertex(v);
			output.write("objectclass=\""
					+ StringNormalizer.escapeXMLCharacters(base.getClass()
							.getName()) + "\" ");
			output.write("objectidentifier=\"" + base.getIdKey() + "\"");
		}
		output.write(">\n</vertex>\n");

	}

	/**
	 * Read the inside of the <CausalFootprint> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	public static CausalFootprint readXML(Node causalFootprintNode,
			ModelGraph baseModel) throws IOException, ClassNotFoundException,
			IllegalAccessException, InstantiationException {
		// Assume causalFootprintNode is the Node with the CausalFootprint tag;
		CausalFootprint result = new CausalFootprint(causalFootprintNode
				.getAttributes().getNamedItem("name").getNodeValue(), baseModel);

		HashMap<Long, ModelGraphVertex> stored2retieved = null;

		for (int i = 0; i < causalFootprintNode.getChildNodes().getLength(); i++) {
			if (causalFootprintNode.getChildNodes().item(i).getNodeName()
					.equals("forwardedges")) {
				// Read the nodeSpecific part of this LogFilter
				result.readForwardEdgesXML(causalFootprintNode.getChildNodes()
						.item(i), stored2retieved);
			} else if (causalFootprintNode.getChildNodes().item(i)
					.getNodeName().equals("backwardedges")) {
				// Read the nodeSpecific part of this LogFilter
				result.readBackwardEdgesXML(causalFootprintNode.getChildNodes()
						.item(i), stored2retieved);
			} else if (causalFootprintNode.getChildNodes().item(i)
					.getNodeName().equals("vertices")) {
				// Read the nodeSpecific part of this LogFilter
				stored2retieved = result.readVerticesXML(causalFootprintNode
						.getChildNodes().item(i), baseModel);
			}
		}

		return result;
	}

	/**
	 * Read the inside of the <ForwardEdges> tag in the XML export file from the
	 * InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	void readForwardEdgesXML(Node forwardEdgeNode,
			HashMap<Long, ModelGraphVertex> stored2retrieved)
			throws IOException, ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		for (int i = 0; i < forwardEdgeNode.getChildNodes().getLength(); i++) {
			if (forwardEdgeNode.getChildNodes().item(i).getNodeName().equals(
					"forwardedge")) {
				ForwardEdge edge = ForwardEdge.readXML(forwardEdgeNode
						.getChildNodes().item(i), stored2retrieved, this);
				addEdge(edge);
			}
		}
	}

	/**
	 * Read the inside of the <ForwardEdges> tag in the XML export file from the
	 * InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	void readBackwardEdgesXML(Node backwardEdgeNode,
			HashMap<Long, ModelGraphVertex> stored2retrieved)
			throws IOException, ClassNotFoundException, IllegalAccessException,
			InstantiationException {
		for (int i = 0; i < backwardEdgeNode.getChildNodes().getLength(); i++) {
			if (backwardEdgeNode.getChildNodes().item(i).getNodeName().equals(
					"backwardedge")) {
				BackwardEdge edge = BackwardEdge.readXML(backwardEdgeNode
						.getChildNodes().item(i), stored2retrieved, this);
				addEdge(edge);
			}
		}
	}

	/**
	 * Read the inside of the <ForwardEdges> tag in the XML export file from the
	 * InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	HashMap<Long, ModelGraphVertex> readVerticesXML(Node verticesNode,
			ModelGraph baseModel) throws IOException, ClassNotFoundException,
			IllegalAccessException, InstantiationException {
		HashMap<Long, ModelGraphVertex> result = new HashMap<Long, ModelGraphVertex>();
		for (int i = 0; i < verticesNode.getChildNodes().getLength(); i++) {
			// each childnode is a "vertex" tag
			// read it and add to the mapping
			if (verticesNode.getChildNodes().item(i).getNodeName().equals(
					"vertex")) {
				readVertexXML(verticesNode.getChildNodes().item(i), baseModel,
						result);
			}
		}
		return result;
	}

	/**
	 * Read the inside of the <ForwardEdges> tag in the XML export file from the
	 * InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	ModelGraphVertex readVertexXML(Node vertexNode, ModelGraph baseModel,
			HashMap<Long, ModelGraphVertex> stored2retrieved)
			throws IOException, ClassNotFoundException, IllegalAccessException,
			InstantiationException {

		Long storedId = Long.decode(vertexNode.getAttributes().getNamedItem(
				"id").getNodeValue());
		if (stored2retrieved.containsKey(storedId)) {
			return stored2retrieved.get(storedId);
		}
		String identifier = vertexNode.getAttributes().getNamedItem(
				"identifier").getNodeValue();
		if (identifier.equals("source")) {
			return source;
		}
		if (identifier.equals("sink")) {
			return target;
		}

		String objectClass = vertexNode.getAttributes().getNamedItem(
				"objectclass").getNodeValue();
		String objectIdentifier = vertexNode.getAttributes().getNamedItem(
				"objectidentifier").getNodeValue();

		ModelGraphVertex baseVertex = null;
		Iterator<ModelGraphVertex> it = baseModel.getVerticeList().iterator();
		while (baseVertex == null && it.hasNext()) {
			ModelGraphVertex v = it.next();
			if (v.getClass().getName().equals(objectClass)
					&& v.getIdKey().toString().equals(objectIdentifier)) {
				baseVertex = v;
			}
		}
		ModelGraphVertex v = new ModelGraphVertex(baseVertex);
		v.object = baseVertex;
		addVertex(v, baseVertex);
		v.setIdentifier(identifier);
		stored2retrieved.put(storedId, v);
		return v;
	}

	/**
	 * Adds an edge from the source to a set of destinations, if no such adge
	 * already exists. If such an edge already exists, the ModelGraph's
	 * weightedArc attribute specifies whether a duplicate edge is added (true)
	 * or not (false).
	 * 
	 * @param source
	 *            the source node
	 * @param destinations
	 *            the set of destination nodes
	 * @return true if the edge was added, false if it existed allready
	 */
	public boolean addEdge(ModelGraphVertex source, HashSet destinations) {
		if (!containsEdge(source, destinations)) {
			ForwardEdge edge = new ForwardEdge(source, destinations, this);
			addEdge(edge);
			return true;
		}
		return false;
	}

	private void addEdge(ForwardEdge edge) {
		forwardEdges.add(edge);
		HashSet s = (HashSet) node2forward.get(edge.source.getIdKey());
		s.add(edge);
	}

	/**
	 * Checks if an edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public boolean containsEdge(ModelGraphVertex source, HashSet destinations) {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if (e.equals(source, destinations)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a smaller edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public ForwardEdge getSmallerEdge(ModelGraphVertex source,
			HashSet destinations) {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if ((e.source == source)
					&& (e.destinations.size() <= destinations.size())
					&& destinations.containsAll(e.destinations)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Checks if a strictly smaller edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public ForwardEdge getStrictSmallerEdge(ModelGraphVertex source,
			HashSet destinations) {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if ((e.source == source)
					&& (e.destinations.size() < destinations.size())
					&& destinations.containsAll(e.destinations)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Adds an edge from the source to a set of destinations, if no such adge
	 * already exists. If such an edge already exists, the ModelGraph's
	 * weightedArc attribute specifies whether a duplicate edge is added (true)
	 * or not (false).
	 * 
	 * @param source
	 *            the source node
	 * @param destinations
	 *            the set of destination nodes
	 * @return true if the edge was added, false if it existed allready
	 */
	public boolean addEdge(HashSet sources, ModelGraphVertex destination) {
		if (!containsEdge(sources, destination)) {
			BackwardEdge edge = new BackwardEdge(sources, destination, this);
			addEdge(edge);
			return true;
		}
		return false;
	}

	private void addEdge(BackwardEdge edge) {
		backwardEdges.add(edge);
		HashSet s = (HashSet) node2backward.get(edge.destination.getIdKey());
		s.add(edge);
	}

	/**
	 * Checks if an edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public boolean containsEdge(HashSet sources, ModelGraphVertex destination) {
		Iterator it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if (e.equals(sources, destination)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a smaller edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public BackwardEdge getSmallerEdge(HashSet sources,
			ModelGraphVertex destination) {
		Iterator it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if ((e.destination == destination)
					&& (e.sources.size() <= sources.size())
					&& sources.containsAll(e.sources)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Checks if a strictly smaller edge exists in the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public BackwardEdge getStrictSmallerEdge(HashSet sources,
			ModelGraphVertex destination) {
		Iterator it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if ((e.destination == destination)
					&& (e.sources.size() < sources.size())
					&& sources.containsAll(e.sources)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * gets a backwardEdge from the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public BackwardEdge getEdge(HashSet sources, ModelGraphVertex destination) {
		Iterator it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if (e.equals(sources, destination)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * gets a forwardEdge from the graph
	 * 
	 * @param source
	 *            The source node
	 * @param destinations
	 *            The set of destination nodes
	 * @return boolean true if it exists, false if not.
	 */
	public ForwardEdge getEdge(ModelGraphVertex source, HashSet destinations) {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if (e.equals(source, destinations)) {
				return e;
			}
		}
		return null;
	}

	/**
	 * adds a node in the graph with a reference to the original object in the
	 * base graph
	 * 
	 * @param v
	 *            ModelGraphVertex
	 * @return ModelGraphVertex
	 */
	public ModelGraphVertex addVertex(ModelGraphVertex newVertex,
			ModelGraphVertex baseVertex) {
		vertices.add(newVertex);
		node2forward.put(newVertex.getIdKey(), new HashSet());
		node2backward.put(newVertex.getIdKey(), new HashSet());
		baseObjects.add(baseVertex);
		if (baseObjects.indexOf(baseVertex) != vertices.indexOf(newVertex)) {
			throw new IndexOutOfBoundsException();
		}
		return newVertex;
	}

	public ModelGraphVertex getTarget() {
		return target;
	}

	public ModelGraphVertex getSource() {
		return source;
	}

	/**
	 * Returns the vertex in the basegraph that is referenced by this vertex in
	 * the causal structure
	 * 
	 * @param vertex
	 *            ModelGraphVertex referring to the object in this graph
	 * @return ModelGraphVertex referring to the object in the base graph
	 */
	public ModelGraphVertex getBaseVertex(ModelGraphVertex vertex) {
		return (ModelGraphVertex) baseObjects.get(vertices.indexOf(vertex));
	}

	/**
	 * Returns the vertex in the causalStructure that is referenced by this
	 * vertex in the basegraph
	 * 
	 * @param vertex
	 *            ModelGraphVertex referring to the object in the base graph
	 * @return ModelGraphVertex referring to the object in this graph
	 */
	public ModelGraphVertex getCausalVertex(ModelGraphVertex baseVertex) {
		int i = baseObjects.indexOf(baseVertex);
		if (i < 0) {
			return null;
		} else {
			return (ModelGraphVertex) vertices.get(i);
		}
	}

	/**
	 * Returns the vertex in the basegraph that is referenced by this vertex in
	 * the causal structure
	 * 
	 * @param vertex
	 *            ModelGraphVertex referring to the object in this graph
	 * @return ModelGraphVertex referring to the object in the base graph
	 */
	public HashSet getBaseVertices(Collection causalVertices) {
		HashSet set = new HashSet(causalVertices.size());
		Iterator it = causalVertices.iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			if (v != target && v != source) {
				set.add(getBaseVertex(v));
			}
		}

		return set;
	}

	/**
	 * Returns the vertex in the causalStructure that is referenced by this
	 * vertex in the basegraph
	 * 
	 * @param vertex
	 *            ModelGraphVertex referring to the object in the base graph
	 * @return ModelGraphVertex referring to the object in this graph
	 */
	public HashSet getCausalVertices(Collection baseVertices) {
		HashSet set = new HashSet(baseVertices.size());
		Iterator it = baseVertices.iterator();
		while (it.hasNext()) {
			set.add(getCausalVertex((ModelGraphVertex) it.next()));
		}

		return set;
	}

	/**
	 * Tells whether or not this graph holds a reference to a given vertex in
	 * the base graph.
	 * 
	 * @param baseVertex
	 *            ModelGraphVertex referring to the vertex in the base graph
	 * @return true if this graph has a reference to the baseVertex, false
	 *         otherwise.
	 */
	public boolean containsBaseVertex(ModelGraphVertex baseVertex) {
		return baseObjects.contains(baseVertex);
	}

	public ArrayList getLookAheadEdges() {
		return forwardEdges;
	}

	public ArrayList getLookBackEdges() {
		return backwardEdges;
	}

	public HashSet<ForwardEdge> getOutgoingLookAheadEdges(ModelGraphVertex v) {
		return (HashSet) node2forward.get(v.getIdKey());
	}

	public HashSet<BackwardEdge> getIncomingLookBackEdges(ModelGraphVertex v) {
		return (HashSet) node2backward.get(v.getIdKey());
	}

	public void removeObsoleteEdges(CancelationComponent cancel) {
		Iterator it = forwardEdges.iterator();
		while (it.hasNext() && !cancel.isCanceled()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if (getStrictSmallerEdge(e.source, e.destinations) != null) {
				((HashSet) node2forward.get(e.source.getIdKey())).remove(e);
				it.remove();
			}
		}
		it = backwardEdges.iterator();
		while (it.hasNext() && !cancel.isCanceled()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if (getStrictSmallerEdge(e.sources, e.destination) != null) {
				((HashSet) node2backward.get(e.destination.getIdKey()))
						.remove(e);
				it.remove();
			}
		}
	}

	public void closeTransitively(CancelationComponent cancel) {

		// There is only one rule.
		// Find (a, B) in forwardEdges
		int i = 0;
		while (i < forwardEdges.size()) {
//			if (cancel.isCanceled()) {
//				return;
//			}
			ForwardEdge aB = (ForwardEdge) forwardEdges.get(i);
			ModelGraphVertex a = aB.source;
			HashSet B = aB.destinations;

			// loop over the elements of e.destinations
			Iterator it = B.iterator();
			while (it.hasNext()) {
//				if (cancel.isCanceled()) {
//					return;
//				}
				// Find b in B
				ModelGraphVertex b = (ModelGraphVertex) it.next();
				HashSet outEdges = (HashSet) node2forward.get(b.getIdKey());
				Iterator it2 = outEdges.iterator();
				while (it2.hasNext()) {
//					if (cancel.isCanceled()) {
//						return;
//					}
					ForwardEdge bC = (ForwardEdge) it2.next();
					HashSet C = bC.destinations;
					// Now add a new edge, namely: (a, B\{b} + C)
					HashSet newSet = new HashSet(B.size() + C.size());
					newSet.addAll(B);
					newSet.remove(b);
					newSet.addAll(C);
					if (getSmallerEdge(a, newSet) == null) {
						addEdge(a, newSet);
					}
				}

			}
			i++;
		}

		// Find (A, b) in forwardEdges
		i = 0;
		while (i < backwardEdges.size()) {
			BackwardEdge Ab = (BackwardEdge) backwardEdges.get(i);
			ModelGraphVertex b = Ab.destination;
			HashSet A = Ab.sources;

			// loop over the elements of e.destinations
			Iterator it = A.iterator();
			while (it.hasNext()) {
				// Find a in A
				ModelGraphVertex a = (ModelGraphVertex) it.next();
				HashSet inEdges = (HashSet) node2backward.get(a.getIdKey());
				Iterator it2 = inEdges.iterator();
				while (it2.hasNext()) {
					BackwardEdge Ca = (BackwardEdge) it2.next();
					HashSet C = Ca.sources;
					// Now add a new edge, namely: ( A\{a} + C, b)
					HashSet newSet = new HashSet(A.size() + C.size());
					newSet.addAll(A);
					newSet.remove(a);
					newSet.addAll(C);
					if (getSmallerEdge(newSet, b) == null) {
						addEdge(newSet, b);
					}
				}

			}
			i++;
		}
//		System.out.println("over");

	}

	private Collection writeSelection = null;
	private ModelGraphVertex selectedNode = null;

	/**
	 * This method tells the causal footprint whether the dot visualization
	 * should only write the output for the selected node.
	 * 
	 * @param selection
	 *            ModelGraphVertex This vertex will be the center of the written
	 *            output. If the whole graph needs to be shown, set this to null
	 */
	public void setWriteSelection(ModelGraphVertex selection) {
		if (selection == null) {
			writeSelection = null;
			selectedNode = null;
		} else {
			selectedNode = selection;
			writeSelection = new HashSet();
			writeSelection.add(selection);
			for (BackwardEdge edge : getIncomingLookBackEdges(selection)) {
				writeSelection.addAll(edge.sources);
			}
			for (ForwardEdge edge : getOutgoingLookAheadEdges(selection)) {
				writeSelection.addAll(edge.destinations);
			}
		}
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
		bw.write("digraph G {fontsize=\"8\"; remincross=true;");
		bw.write("fontname=\"Arial\";rankdir=\"LR\",ranksep=\"0.15\"\n");
		bw
				.write("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
		bw.write("node [fontname=\"Arial\",fontsize=\"8\"];\n");

		// HashMap nodeMapping = new HashMap();
		int num = 0;
		nodeMapping.clear();
		Iterator it = vertices.iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = (ModelGraphVertex) it.next();
			if (writeSelection != null && !writeSelection.contains(v)) {
				continue;
			}
			((HashSet) node2backward.get(v.getIdKey())).size();
			String label = v.object.toString();
			bw.write("node" + v.getId() + " [shape=\"box\",label=\"" + label
					+ "\"];\n");
			nodeMapping.put(new String("node" + v.getId()), v);
		}
		if (writeSelection == null || writeSelection.contains(source)) {
			bw.write("node" + source.getId()
					+ " [shape=\"circle\",label=\"I\"];\n");
			nodeMapping.put(new String("node" + source.getId()), source);
		}
		if (writeSelection == null || writeSelection.contains(target)) {
			bw.write("node" + target.getId()
					+ " [shape=\"circle\",label=\"O\"];\n");
			nodeMapping.put(new String("node" + target.getId()), target);
		}
		if (writeSelection == null) {
			it = vertices.iterator();
			while (it.hasNext()) {
				ModelGraphVertex v = (ModelGraphVertex) it.next();
				num = writeDotEdgesForVertex(bw, v, num);
			}
			num = writeDotEdgesForVertex(bw, source, num);
			num = writeDotEdgesForVertex(bw, target, num);
		} else {

			num = writeDotEdgesForVertex(bw, selectedNode, num);
			HashSet s = new HashSet(1);
			s.add(selectedNode);

			BackwardEdge be;
			ForwardEdge fe;
			Iterator it2 = vertices.iterator();
			while (it2.hasNext()) {
				ModelGraphVertex v = (ModelGraphVertex) it2.next();
				be = getEdge(s, v);
				if (be != null) {
					writeBackwardEdge(bw, be, v, num++);
				}
				fe = getEdge(v, s);
				if (fe != null) {
					writeForwardEdge(bw, fe, v, num++);
				}
			}
			be = getEdge(s, source);
			if (be != null) {
				writeBackwardEdge(bw, be, source, num++);
			}
			fe = getEdge(source, s);
			if (fe != null) {
				writeForwardEdge(bw, fe, source, num++);
			}
			be = getEdge(s, target);
			if (be != null) {
				writeBackwardEdge(bw, be, target, num++);
			}
			fe = getEdge(target, s);
			if (fe != null) {
				writeForwardEdge(bw, fe, target, num++);
			}
		}
	}

	private int writeDotEdgesForVertex(Writer bw, ModelGraphVertex v, int num)
			throws IOException {
		HashSet inEdges = ((HashSet) node2backward.get(v.getIdKey()));
		Iterator it2 = inEdges.iterator();
		for (int i = 0; i < inEdges.size(); i++) {
			BackwardEdge edge = (BackwardEdge) it2.next();
			num = writeBackwardEdge(bw, edge, v, num);
		}

		HashSet outEdges = ((HashSet) node2forward.get(v.getIdKey()));
		it2 = outEdges.iterator();
		for (int i = 0; i < outEdges.size(); i++) {
			ForwardEdge edge = (ForwardEdge) it2.next();
			num = writeForwardEdge(bw, edge, v, num);
		}
		return num;
	}

	private int writeForwardEdge(Writer bw, ForwardEdge edge,
			ModelGraphVertex v, int num) throws IOException {
		bw
				.write("rel"
						+ num
						+ " [fixedsize=\"true\",label=\"\",shape=\"circle\",style=\"filled\",width=\"0.1\",color=\"blue\"];\n");
		bw
				.write("node"
						+ v.getId()
						+ " -> rel"
						+ num
						+ "[color=\"blue\",arrowhead=\"none\",arrowtail=\"none\",weight=\"100\"];\n");
		Iterator it3 = edge.destinations.iterator();
		while (it3.hasNext()) {
			ModelGraphVertex dest = (ModelGraphVertex) it3.next();
			bw.write("rel" + num + " -> node" + dest.getId()
					+ " [color=\"blue\",minlen=\"10\"];\n");
		}
		return ++num;
	}

	private int writeBackwardEdge(Writer bw, BackwardEdge edge,
			ModelGraphVertex v, int num) throws IOException {
		bw
				.write("rel"
						+ num
						+ " [fixedsize=\"true\",label=\"\",shape=\"circle\",style=\"filled\",width=\"0.1\",color=\"red\"];\n");
		bw.write("rel" + num + "  -> node" + v.getId()
				+ "[color=\"red\",weight=\"100\"];\n");
		Iterator it3 = edge.sources.iterator();
		while (it3.hasNext()) {
			ModelGraphVertex source = (ModelGraphVertex) it3.next();
			bw
					.write("node"
							+ source.getId()
							+ " -> rel"
							+ num
							+ "[color=\"red\",arrowhead=\"none\",arrowtail=\"none\",minlen=\"10\"];\n");
		}
		return ++num;
	}

	/**
	 * Remove all objects with a reference to one of the vertices in the given
	 * collection
	 * 
	 * @param vertices
	 *            Collection
	 * @return boolean true if the graph changed
	 */
	public boolean removeAllBaseVertices(Collection vertices) {
		boolean changed = false;
		Iterator it = vertices.iterator();
		while (it.hasNext()) {
			ModelGraphVertex v = getCausalVertex((ModelGraphVertex) it.next());
			if (v != null) {
				changed = true;
				removeVertex(v);
			}
		}
		return changed;
	}

	public void removeVertex(ModelGraphVertex v) {
		if (v != source && v != target) {
			baseObjects.remove(getBaseVertex(v));
		}
		vertices.remove(v);

		Iterator it = forwardEdges.iterator();
		while (it.hasNext()) {
			ForwardEdge e = (ForwardEdge) it.next();
			if (e.destinations.contains(v)) {
				((HashSet) node2forward.get(e.source.getIdKey())).remove(e);
				it.remove();
			} else if (e.source == v) {
				it.remove();
			}
		}
		it = backwardEdges.iterator();
		while (it.hasNext()) {
			BackwardEdge e = (BackwardEdge) it.next();
			if (e.sources.contains(v)) {
				((HashSet) node2backward.get(e.destination.getIdKey()))
						.remove(e);
				it.remove();
			} else if (e.destination == v) {
				it.remove();
			}
		}
		node2forward.keySet().remove(v.getIdKey());
		node2backward.keySet().remove(v.getIdKey());
	}

	public ArrayList getVerticeList() {
		return vertices;
	}

	/**
	 * Print key indicators of the EPC to the Test tab.
	 * 
	 * @param tag
	 *            String The tag to use for the indicators.
	 */
	public void Test(String tag) {
		Message.add("<" + tag + " nofBaseObjects=\"" + baseObjects.size()
				+ "\" nofVertices=\"" + vertices.size()
				+ "\" nofBackwardEdges=\"" + backwardEdges.size()
				+ "\" nofForwardEdges=\"" + forwardEdges.size() + "\"/>",
				Message.TEST);
	}
}

package org.processmining.framework.models.ontology;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.omwg.ontology.Concept;
import org.omwg.ontology.Ontology;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.ui.Message;
import org.wsml.reasoner.api.WSMLReasoner;
import org.wsml.reasoner.api.WSMLReasonerFactory;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsml.reasoner.impl.DefaultWSMLReasonerFactory;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;
import org.wsmo.factory.Factory;
import org.wsmo.factory.WsmoFactory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;
import org.wsmo.wsml.Serializer;

public class OntologyModel {

	public static final String ONTOLOGY_SEPARATOR = "#";
	public static final Pattern ONTOLOGY_SEPARATOR_PATTERN = Pattern
			.compile(ONTOLOGY_SEPARATOR); // for faster splitting of URIs

	private Ontology ontology;
	private WSMLReasoner reasoner;
	private Collection<ConceptModel> directlyReferencedConcepts;
	private Map<String, ConceptModel> allConceptsByShortName;
	private Map<String, ConceptModel> allReferencedConceptsByName;
	private String uriInLog;
	private Map<ConceptModel, Collection<ConceptModel>> cachedSuperConcepts;
	private Map<ConceptModel, Collection<ConceptModel>> cachedSubConcepts;
	private WsmoFactory factory;
	private boolean changed;
	private Map<String, Object> reasonerParams;

	/**
	 * Constructor for reading an existing ontology.
	 * 
	 * @param uriInLog
	 *            URI of ontology as it is used in a log file.
	 * @param instancesPerConcept
	 *            Mapping from concept URIs in the log to a set of instance
	 *            labels.
	 * @throws IOException
	 * @throws ParserException
	 * @throws InvalidModelException
	 * @throws InconsistencyException
	 * @throws InvalidModelException
	 * @throws org.processmining.framework.models.ontology.InvalidModelException
	 */

	public OntologyModel(String uriInLog,
			Map<String, Set<String>> instancesPerConcept) throws IOException,
			ParserException, InconsistencyException, InvalidModelException,
			org.processmining.framework.models.ontology.InvalidModelException {
		cachedSuperConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();
		cachedSubConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();
		changed = false;

		Parser wsmlParser = Factory.createParser(null);
		final TopEntity[] identifiable = wsmlParser
				.parse(new InputStreamReader(new URL(uriInLog).openStream()));

		if (identifiable.length > 0 && identifiable[0] instanceof Ontology) {
			Ontology ontology = (Ontology) identifiable[0];

			this.uriInLog = uriInLog;
			this.ontology = ontology;
			init(instancesPerConcept);
		} else {
			throw new ParserException(
					"First element of file is not an ontology!", null);
		}
	}

	/**
	 * Constructor for a new (empty) ontology.
	 * 
	 * @param uri
	 *            The URI of the new ontology.
	 * @param name
	 *            The name of the ontology
	 * @throws InconsistencyException
	 * @throws org.processmining.framework.models.ontology.InvalidModelException
	 */
	public OntologyModel(String namespace, String name)
			throws InconsistencyException,
			org.processmining.framework.models.ontology.InvalidModelException {
		cachedSuperConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();
		cachedSubConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();

		this.factory = Factory.createWsmoFactory(null);
		this.ontology = factory.createOntology(factory.createIRI(namespace
				+ ONTOLOGY_SEPARATOR + name));
		this.ontology.setDefaultNamespace(factory.createNamespace("_", factory
				.createIRI(namespace)));
		this.ontology
				.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-flight");

		this.uriInLog = namespace;
		init(new HashMap<String, Set<String>>());

		changed = true;
	}

	/**
	 * Constructor for an ontology constructed from a model graph.
	 * 
	 * @param graph
	 *            The model graph which contains the ontology (nodes are
	 *            concepts, edges are is-a relations)
	 * @param uri
	 *            The URI of the new ontology.
	 * @param name
	 *            The name of the ontology
	 * @throws InconsistencyException
	 * @throws org.processmining.framework.models.ontology.InvalidModelException
	 */
	public OntologyModel(ModelGraph graph, String namespace, String name)
			throws InconsistencyException,
			org.processmining.framework.models.ontology.InvalidModelException {
		cachedSuperConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();
		cachedSubConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();

		this.factory = Factory.createWsmoFactory(null);
		this.ontology = factory.createOntology(factory.createIRI(namespace
				+ ONTOLOGY_SEPARATOR + name));
		this.ontology.setDefaultNamespace(factory.createNamespace("_", factory
				.createIRI(namespace)));
		this.ontology
				.setWsmlVariant("http://www.wsmo.org/wsml/wsml-syntax/wsml-flight");

		this.uriInLog = namespace;
		init(new HashMap<String, Set<String>>());

		// System.out.println("CREATING " + namespace + "#" + name);
		Map<ModelGraphVertex, ConceptModel> vertexMapping = new HashMap<ModelGraphVertex, ConceptModel>();
		for (ModelGraphVertex vertex : graph.getVerticeList()) {
			String label = vertex.getDotAttribute("label");

			vertexMapping.put(vertex, addConcept(label == null ? vertex
					.getName() : label));
		}
		for (Object o : graph.getEdges()) {
			ModelGraphEdge edge = (ModelGraphEdge) o;
			vertexMapping.get(edge.getDest()).addSuperConcept(
					vertexMapping.get(edge.getSource()));
		}

		changed = true;
	}

	public String serialize() {
		Serializer serializer = Factory.createSerializer(null);
		StringBuffer buffer = new StringBuffer();

		serializer.serialize(new TopEntity[] { ontology }, buffer);
		return buffer.toString();
	}

	private void init(Map<String, Set<String>> instancesPerConcept)
			throws InconsistencyException,
			org.processmining.framework.models.ontology.InvalidModelException {
		Set<String> conceptsInLog = instancesPerConcept.keySet();

		reasonerParams = new HashMap<String, Object>();
		// reasonerParams.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER,
		// WSMLReasonerFactory.BuiltInReasoner.IRIS);
		reasonerParams.put(WSMLReasonerFactory.PARAM_BUILT_IN_REASONER,
				WSMLReasonerFactory.BuiltInReasoner.KAON2);
		recreateReasoner();

		directlyReferencedConcepts = new TreeSet<ConceptModel>();
		allConceptsByShortName = new HashMap<String, ConceptModel>();
		for (Concept concept : reasoner.getAllConcepts((IRI) ontology
				.getIdentifier())) {
			String conceptShortName = getConceptPart(concept.getIdentifier()
					.toString());
			ConceptModel conceptModel = new ConceptModel(this, concept,
					instancesPerConcept.get(conceptShortName));

			if (conceptsInLog.contains(conceptShortName)) {
				directlyReferencedConcepts.add(conceptModel);
				conceptsInLog.remove(conceptShortName);
			}
			allConceptsByShortName.put(conceptShortName, conceptModel);
		}

		if (!conceptsInLog.isEmpty()) {
			Message
					.add("Log contains model references to concepts which are not in the ontology:");
			Message.add("  Concepts not in ontology: "
					+ conceptsInLog.toString());
			Message.add("  Ontology URI: " + getName());
			Message.add("  Ontology location: " + uriInLog);
		}

		structureChanged();
		changed = false;
	}

	void deinit() {
		reasoner.deRegisterOntology((IRI) ontology.getIdentifier());
	}

	/**
	 * Returns true if the ontology model was changed (ie. concepts or
	 * attributes have been added/removed etc).
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Returns a list of all concepts which are directly referenced in the log.
	 * Directly referenced means that there is at least one element in this log
	 * which is annotated with such a concept.
	 * 
	 * @return All directly referenced concepts.
	 */
	public Collection<ConceptModel> getConcepts() {
		return Collections.unmodifiableCollection(directlyReferencedConcepts);
	}

	/**
	 * Returns a list of all concepts which are directly referenced in the log,
	 * or which are superconcepts of any concept which is directly referenced in
	 * the log. Note that this is not necessarily the list of all concepts in
	 * the ontology.
	 * 
	 * @return All directly referenced concepts in the log plus all their
	 *         superconcepts.
	 */
	public Collection<ConceptModel> getAllConcepts() {
		return Collections.unmodifiableCollection(allReferencedConceptsByName
				.values());
	}

	/**
	 * Returns the full URI of the ontology.
	 * 
	 * @return the full ontology URI.
	 */
	public String getName() {
		return ontology.getIdentifier().toString();
	}

	/**
	 * Returns the URI of the file in which the ontology is stored (can be a
	 * file or an http URL for example). Note that this can be different from
	 * the name of the ontology returned by getName().
	 * 
	 * @return the full ontology URI.
	 */
	public String getUriInLog() {
		return uriInLog;
	}

	/**
	 * Returns the name of the ontology without the full URI path.
	 * 
	 * @return The name of the ontology without the full URI path.
	 */
	public String getShortName() {
		return getConceptPart(getName());
	}

	/**
	 * Returns a collection of all superconcepts of a given concept. The
	 * reasoner is used to determine the superconcepts. This includes all
	 * superconcepts (not only the direct superconcepts), though it only returns
	 * strict superconcepts (so the returned set will not include the given
	 * concept). The reasoner is used to determine the subconcepts.
	 */
	public Collection<ConceptModel> getSuperConcepts(ConceptModel concept) {
		Collection<ConceptModel> result = cachedSuperConcepts.get(concept);

		assert concept.getConcept().getOntology() == ontology;

		if (result == null) {
			result = translateConceptsToConceptModels(reasoner
					.getSuperConcepts((IRI) ontology.getIdentifier(), concept
							.getConcept()), concept, false);
			cachedSuperConcepts.put(concept, result);
		}
		return result;
	}

	/**
	 * Returns a collection of all subconcepts of a given concept. This includes
	 * all subconcepts (not only the direct subconcepts), though it only returns
	 * strict sub concepts (so the returned set will not include the given
	 * concept). The reasoner is used to determine the subconcepts.
	 */
	public Collection<ConceptModel> getSubConcepts(ConceptModel concept) {
		Collection<ConceptModel> result = cachedSubConcepts.get(concept);

		assert concept.getConcept().getOntology() == ontology;

		if (result == null) {
			result = translateConceptsToConceptModels(reasoner.getSubConcepts(
					(IRI) ontology.getIdentifier(), concept.getConcept()),
					concept, false);
			cachedSubConcepts.put(concept, result);
		}
		return result;
	}

	/**
	 * Returns a collection of the direct subconcepts which are directly
	 * referenced in the log.
	 * 
	 * @param concept
	 * @return
	 */
	public Collection<ConceptModel> getDirectSubConcepts(ConceptModel concept) {
		assert concept.getConcept().getOntology() == ontology;
		return translateConceptsToConceptModels(concept.getConcept()
				.listSubConcepts(), null, true);
	}

	/**
	 * Returns a collection of all direct subconcepts, including concepts which
	 * are not directly referenced in the log.
	 * 
	 * @param concept
	 * @return
	 */
	public Collection<ConceptModel> getAllDirectSubConcepts(ConceptModel concept) {
		assert concept.getConcept().getOntology() == ontology;
		return translateConceptsToConceptModels(concept.getConcept()
				.listSubConcepts(), null, false);
	}

	/**
	 * Returns a collection of the direct superconcepts which are directly
	 * referenced in the log.
	 * 
	 * @param concept
	 * @return
	 */
	public Collection<ConceptModel> getDirectSuperConcepts(ConceptModel concept) {
		assert concept.getConcept().getOntology() == ontology;
		return translateConceptsToConceptModels(concept.getConcept()
				.listSuperConcepts(), null, true);
	}

	/**
	 * Returns a collection of all direct superconcepts, including concepts
	 * which are not directly referenced in the log.
	 * 
	 * @param concept
	 * @return
	 */
	public Collection<ConceptModel> getAllDirectSuperConcepts(
			ConceptModel concept) {
		assert concept.getConcept().getOntology() == ontology;
		return translateConceptsToConceptModels(concept.getConcept()
				.listSuperConcepts(), null, false);
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Returns the part of the URI before the "#" character.
	 * 
	 * @param uri
	 * @return
	 */
	public static String getOntologyPart(String uri) {
		String[] components = ONTOLOGY_SEPARATOR_PATTERN.split(uri);
		return components.length >= 1 ? components[0] : null;
	}

	/**
	 * Returns the part of the URI after the "#" character.
	 * 
	 * @param uri
	 * @return
	 */
	public static String getConceptPart(String uri) {
		String[] components = ONTOLOGY_SEPARATOR_PATTERN.split(uri);
		return components.length >= 2 ? components[1] : null;
	}

	/**
	 * Returns the model reference of the given concept as it is used in the
	 * log.
	 * 
	 * @param c
	 * @return
	 */
	public String getConceptURIInLog(ConceptModel c) {
		return uriInLog + ONTOLOGY_SEPARATOR + c.getShortName();
	}

	/**
	 * Returns the concept with the given short name, or null if no such concept
	 * exists in this ontology.
	 * 
	 * @param conceptShortName
	 * @return
	 */
	public ConceptModel findConcept(String conceptShortName) {
		return allConceptsByShortName.get(conceptShortName);
	}

	private Collection<ConceptModel> translateConceptsToConceptModels(
			Set concepts, ConceptModel conceptToExclude,
			boolean haveToBeReferenced) {
		Collection<ConceptModel> result = new TreeSet<ConceptModel>();

		for (Object concept : concepts) {
			String conceptName = ((Concept) concept).getIdentifier().toString();

			if (conceptToExclude == null
					|| !conceptName.equals(conceptToExclude.getName())) {
				ConceptModel conceptModel = haveToBeReferenced ? allReferencedConceptsByName
						.get(conceptName)
						: allConceptsByShortName
								.get(getConceptPart(conceptName));

				if (conceptModel != null) {
					result.add(conceptModel);
				}
			}
		}
		return result;
	}

	/**
	 * Adds a new concept with the given name to this ontology. Note that this
	 * concept is not referenced in any log, so it does not show up in any
	 * visualization yet!
	 * 
	 * @param conceptShortName
	 *            The short name of the concept to create
	 * @return The created concept
	 * @throws org.processmining.framework.models.ontology.InvalidModelException
	 */
	public ConceptModel addConcept(String conceptShortName)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		// System.out.println("ADDING: " + conceptShortName);
		if (findConcept(conceptShortName) != null) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					"Concept already exists in this ontology: "
							+ conceptShortName);
		}

		String uri = getOntologyPart(getName()) + ONTOLOGY_SEPARATOR
				+ conceptShortName;
		Concept concept = factory.createConcept(factory.createIRI(uri));
		try {
			concept.setOntology(ontology);
			ontology.addConcept(concept);
		} catch (SynchronisationException e) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					e.getMessage());
		} catch (InvalidModelException e) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					e.getMessage());
		}
		ConceptModel conceptModel = new ConceptModel(this, concept, null);

		allConceptsByShortName.put(conceptModel.getShortName(), conceptModel);
		changed = true;

		recreateReasoner();

		return conceptModel;
	}

	void structureChanged()
			throws org.processmining.framework.models.ontology.InvalidModelException {

		changed = true;

		cachedSuperConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();
		cachedSubConcepts = new HashMap<ConceptModel, Collection<ConceptModel>>();

		// this is quite brute force and possibly slow, but it works for now

		// rebuild the list with all referenced concepts
		allReferencedConceptsByName = new TreeMap<String, ConceptModel>();
		for (ConceptModel concept : directlyReferencedConcepts) {
			allReferencedConceptsByName.put(concept.getName(), concept);
			for (ConceptModel superConcept : getSuperConcepts(concept)) {
				allReferencedConceptsByName.put(superConcept.getName(),
						superConcept);
			}
		}
		recreateReasoner();
	}

	void setDirectlyReferenced(ConceptModel concept)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		if (directlyReferencedConcepts.add(concept)) {
			structureChanged();
		}
	}

	public ModelGraph toModelGraph(boolean showInstances) {
		return new OntologyModelGraph(this, showInstances);
	}

	public ModelGraph toModelGraph() {
		return new OntologyModelGraph(this, false);
	}

	private void recreateReasoner()
			throws org.processmining.framework.models.ontology.InvalidModelException {
		reasoner = DefaultWSMLReasonerFactory.getFactory()
				.createWSMLFlightReasoner(reasonerParams);
		try {
			reasoner.registerOntology(ontology);
		} catch (InconsistencyException e) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					e.getMessage());
		}
	}

	WsmoFactory getFactory() {
		return factory;
	}

	Ontology getWsmoOntology() {
		return ontology;
	}
}

class OntologyModelGraph extends ModelGraph {

	private OntologyModel ontology;
	private boolean showInstances;

	public OntologyModelGraph(OntologyModel ontology, boolean showInstances) {
		super(ontology.getName());

		this.ontology = ontology;
		this.showInstances = showInstances;

		Map<ConceptModel, OntologyVertex> mapping = new HashMap<ConceptModel, OntologyVertex>();
		for (ConceptModel concept : ontology.getAllConcepts()) {
			OntologyVertex vertex = new OntologyVertex(concept.getName(), this);

			mapping.put(concept, vertex);
			addVertex(vertex);
		}

		for (ConceptModel concept : ontology.getAllConcepts()) {
			OntologyVertex to = mapping.get(concept);

			for (ConceptModel superConcept : ontology
					.getDirectSuperConcepts(concept)) {
				addEdge(new OntologyEdge(mapping.get(superConcept), to));
			}
		}
	}

	public void writeToDot(Writer bw) throws IOException {
		writeToDot(bw, showInstances);
	}

	public void writeToDot(Writer bw, boolean withInstances) throws IOException {
		Collection<ConceptModel> concepts = ontology.getAllConcepts();
		Map<ConceptModel, Integer> ids = new HashMap<ConceptModel, Integer>();

		int i = 0;
		for (ConceptModel concept : concepts) {
			ids.put(concept, i);
			i++;
		}

		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Arial\"; fontsize=\"11\";\n");
		bw
				.write("  node [style=\"filled\",color=\"antiquewhite\",shape=\"ellipse\",fontname=\"Arial\",fontsize=\"11\"];\n");

		bw
				.write("\"ontology URI\" [shape=\"plaintext\",style=\"filled\",color=\"antiquewhite3\",label=\""
						+ OntologyModel.getOntologyPart(getName()).replace('"',
								'\'')
						+ OntologyModel.ONTOLOGY_SEPARATOR
						+ "\"];\n");
		bw
				.write("\"ontology name\" [shape=\"box\",style=\"filled\",color=\"antiquewhite3\",label=\""
						+ ontology.getShortName().replace('"', '\'') + "\"];\n");
		bw
				.write("\"ontology URI\" -> \"ontology name\" [style=\"filled\",color=\"antiquewhite3\",arrowtail=\"none\",arrowhead=\"none\"];\n");

		// write nodes
		for (ConceptModel concept : concepts) {
			bw.write("E" + ids.get(concept) + " [label=\""
					+ concept.getShortName().replace('"', '\'') + "\"];\n");
		}
		if (withInstances) {
			int instanceId = 0;
			for (ConceptModel concept : concepts) {
				for (String instance : concept.getInstances()) {
					bw.write("I" + instanceId
							+ " [shape=\"plaintext\",label=\""
							+ instance.replace('"', '\'') + "\"];\n");
					instanceId++;
				}
			}
		}

		// write edges
		for (ConceptModel concept : concepts) {
			Collection<ConceptModel> superConcepts = ontology
					.getDirectSuperConcepts(concept);

			if (superConcepts.isEmpty()) {
				bw
						.write("\"ontology name\" -> E"
								+ ids.get(concept)
								+ " [style=\"filled\",arrowtail=\"normal\",arrowhead=\"none\"];\n");
			} else {
				for (ConceptModel superConcept : superConcepts) {
					bw
							.write("E"
									+ ids.get(superConcept)
									+ " -> E"
									+ ids.get(concept)
									+ " [style=\"filled\",arrowtail=\"normal\",arrowhead=\"none\"];\n");
				}
			}
		}
		if (withInstances) {
			int instanceId = 0;
			for (ConceptModel concept : concepts) {
				for (String instance : concept.getInstances()) {
					bw
							.write("E"
									+ ids.get(concept)
									+ " -> I"
									+ instanceId
									+ " [color=\"gray\",arrowtail=\"normal\",arrowhead=\"none\"];\n");
					instanceId++;
				}
			}
		}

		bw.write("}\n");
	}

}

class OntologyVertex extends ModelGraphVertex {
	public OntologyVertex(String label, ModelGraph g) {
		super(g);
		setIdentifier(label);
	}
}

class OntologyEdge extends ModelGraphEdge {
	public OntologyEdge(ModelGraphVertex source, ModelGraphVertex destination) {
		super(source, destination);
	}
}

package org.processmining.framework.models.ontology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.ProgressFromAnyThread;
import org.wsml.reasoner.api.InternalReasonerException;
import org.wsml.reasoner.api.inconsistency.InconsistencyException;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.wsml.ParserException;

/**
 * @author Ana Karla A. de Medeiros
 * @author Peter van den Brand
 */

public class OntologyCollection {

	private LogSummary summary;
	private Map<String, OntologyModel> ontologies;
	private boolean loadedOntologies;

	public OntologyCollection(LogSummary summary) {
		this.summary = summary;
		this.ontologies = new TreeMap<String, OntologyModel>();
		this.loadedOntologies = false;
	}

	public List<OntologyModel> getOntologies() {
		loadOntologies();
		return new ArrayList<OntologyModel>(ontologies.values());
	}

	/**
	 * Creates a new ontology and adds it to the collection. If a ontology with
	 * the same URI exists before creating a new ontology, then the old ontology
	 * will be removed from the collection.
	 * 
	 * @param namespace
	 *            The namespace URI of the new ontology (example:
	 *            http://www.processmining.org/ontologies/TaskOntology)
	 * @param name
	 *            The name of the new ontology (example: TaskOntology)
	 * @return The new ontology (or null if it could not be created because of a
	 *         reasoner problem).
	 */
	public OntologyModel createOntology(String namespace, String name) {
		loadOntologies(); // creates the reasoner, among other things

		String uri = namespace + OntologyModel.ONTOLOGY_SEPARATOR + name;

		if (ontologies.get(uri) != null) {
			ontologies.get(uri).deinit();
			ontologies.remove(uri);
		}

		try {
			ontologies.put(uri, new OntologyModel(namespace, name));
		} catch (InconsistencyException e) {
			e.printStackTrace();
		} catch (org.processmining.framework.models.ontology.InvalidModelException e) {
			e.printStackTrace();
		}
		return ontologies.get(uri);
	}

	public ConceptModel findConceptByUriInLog(String uriInLog) {
		loadOntologies();

		String conceptName = OntologyModel.getConceptPart(uriInLog);
		String ontologyName = OntologyModel.getOntologyPart(uriInLog);
		OntologyModel ontology = ontologies.get(ontologyName);

		return ontology == null ? null : ontology.findConcept(conceptName);
	}

	public ConceptModel findConceptByUriInOntology(String uriInOntology) {
		loadOntologies();

		String conceptName = OntologyModel.getConceptPart(uriInOntology);
		String ontologyName = OntologyModel.getOntologyPart(uriInOntology);

		for (OntologyModel ontology : ontologies.values()) {
			if (OntologyModel.getOntologyPart(ontology.getName()).equals(
					ontologyName)) {
				return ontology.findConcept(conceptName);
			}
		}
		return null;
	}

	public Set<String> translateToReferencesInOntology(
			Collection<String> urisInLog) {
		Set<String> result = new HashSet<String>();

		if (urisInLog != null) {
			for (String uri : urisInLog) {
				ConceptModel concept = findConceptByUriInLog(uri);

				if (concept != null) {
					result.add(concept.getName());
				}
			}
		}
		return result;
	}

	public Set<String> translateToReferenceInLog(String uriInOntology,
			boolean includeSuperConcepts, boolean includeSubConcepts) {
		Set<String> result = new HashSet<String>();

		loadOntologies();

		String conceptName = OntologyModel.getConceptPart(uriInOntology);
		String ontologyName = OntologyModel.getOntologyPart(uriInOntology);

		for (Map.Entry<String, OntologyModel> item : ontologies.entrySet()) {
			String uriInLog = item.getKey();
			OntologyModel ontology = item.getValue();

			if (ontologyName.equals(OntologyModel.getOntologyPart(ontology
					.getName()))) {
				if (includeSuperConcepts) {
					for (ConceptModel concept : ontology
							.getSuperConcepts(ontology.findConcept(conceptName))) {
						result.add(ontology.getConceptURIInLog(concept));
					}
				}
				if (includeSubConcepts) {
					for (ConceptModel concept : ontology
							.getSubConcepts(ontology.findConcept(conceptName))) {
						result.add(ontology.getConceptURIInLog(concept));
					}
				}
				result.add(uriInLog + OntologyModel.ONTOLOGY_SEPARATOR
						+ conceptName);
				return result;
			}
		}
		result.add(uriInOntology);
		return result;
	}

	private OntologyToConceptToInstances getConceptsPerOntology() {
		OntologyToConceptToInstances result = new OntologyToConceptToInstances();

		for (Map.Entry<String, Set<String>> item : summary
				.getConceptsAndInstances().entrySet()) {
			String uri = item.getKey();
			Set<String> instances = item.getValue();

			String ontology = OntologyModel.getOntologyPart(uri);
			String concept = OntologyModel.getConceptPart(uri);

			ConceptToInstances concepts = result.get(ontology);
			if (concepts == null) {
				concepts = new ConceptToInstances();
				result.put(ontology, concepts);
			}
			concepts.put(concept, instances);
		}
		return result;
	}

	private void loadOntologies() {
		if (!loadedOntologies) {
			loadedOntologies = true;

			Progress progress = new ProgressFromAnyThread("Loading ontologies");
			progress
					.setNote("Building a list of all concepts used in the log...");

			OntologyToConceptToInstances conceptsPerOntology = getConceptsPerOntology();

			if (!conceptsPerOntology.isEmpty()) {
				List<String> errors = new ArrayList<String>();

				progress.setMinMax(0, conceptsPerOntology.size() + 1);
				progress.setProgress(1);

				for (Map.Entry<String, ConceptToInstances> item : conceptsPerOntology
						.entrySet()) {
					String uri = item.getKey();
					ConceptToInstances conceptsToInstances = item.getValue();

					progress.setNote(uri);
					Message.add("Loading ontology: " + uri);

					try {
						ontologies.remove(uri);
						ontologies.put(uri, new OntologyModel(uri,
								conceptsToInstances));
					} catch (MalformedURLException e) {
						errors.add(uri + ": malformed url: " + e.getMessage());
					} catch (IOException e) {
						errors.add(uri + ": could not read ontology: "
								+ e.getMessage());
					} catch (ParserException e) {
						errors.add(uri + ": could not parse ontology: "
								+ e.getMessage());
					} catch (InvalidModelException e) {
						errors.add(uri + ": invalid model: " + e.getMessage());
					} catch (InconsistencyException e) {
						errors.add(uri + ": inconsistency error: "
								+ e.getMessage());
					} catch (InternalReasonerException e) {
						errors.add(uri + ": internal ontology reasoner error: "
								+ e.getMessage());
					} catch (org.processmining.framework.models.ontology.InvalidModelException e) {
						errors.add(uri + ": invalid model: " + e.getMessage());
					}
					progress.inc();
				}

				if (errors.size() > 0) {
					StringBuffer message = new StringBuffer(
							"The following ontologies could not be loaded due to errors:\n\n");
					int i = 0;
					for (String s : errors) {
						message.append(++i + ") " + s + "\n\n");
					}
					JOptionPane.showMessageDialog(MainUI.getInstance(), message
							.toString());
				}
			}
			progress.close();
		}
	}

}

class ConceptToInstances extends HashMap<String, Set<String>> {
	private static final long serialVersionUID = 616958461599772749L;
}

class OntologyToConceptToInstances extends HashMap<String, ConceptToInstances> {
	private static final long serialVersionUID = -6612533878053208422L;
}

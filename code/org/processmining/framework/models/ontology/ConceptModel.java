package org.processmining.framework.models.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omwg.ontology.Concept;
import org.omwg.ontology.Instance;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.InfoItem;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.common.exception.SynchronisationException;

public class ConceptModel implements Comparable<ConceptModel> {
	private Concept concept;
	private OntologyModel ontology;
	private Set<String> instances;

	public final static int WORKFLOW_MODEL_ELEMENT = 1;
	public final static int EVENTTYPE = 2;
	public final static int ORIGINATOR = 4;

	// has package visibility, so it can only be used by OntologyModel
	ConceptModel(OntologyModel ontology, Concept concept, Set<String> instances) {
		this.ontology = ontology;
		this.concept = concept;
		this.instances = instances == null ? new HashSet<String>(0) : instances;
	}

	public OntologyModel getOntology() {
		return ontology;
	}

	public String getName() {
		return concept.getIdentifier().toString();
	}

	public String getShortName() {
		return OntologyModel.getConceptPart(getName());
	}

	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ConceptModel)) {
			return false;
		}
		return ((ConceptModel) other).getName().equals(getName());
	}

	Concept getConcept() {
		return concept;
	}

	public Set<String> getInstances() {
		return instances;
	}

	public int compareTo(ConceptModel other) {
		return getName().compareTo(((ConceptModel) other).getName());
	}

	public void addSuperConcept(ConceptModel superConcept)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		try {
			getConcept().addSuperConcept(superConcept.getConcept());
			ontology.structureChanged();
		} catch (SynchronisationException e) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					e.getMessage());
		} catch (InvalidModelException e) {
			throw new org.processmining.framework.models.ontology.InvalidModelException(
					e.getMessage());
		}
	}

	/**
	 * Add ATE elements as instances to this concept and annotate the ATE with
	 * this concept.
	 * 
	 * @param pi
	 *            The process instance of the ATE to annotate
	 * @param index
	 *            The index in the PI of the ATE to annotate
	 * @param elementType
	 *            The element(s) to annotate: WORKFLOW_MODEL_ELEMENT, EVENTTYPE
	 *            or ORIGINATOR.
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 * @throws org.processmining.framework.models.ontology.InvalidModelException
	 */
	public void addInstance(AuditTrailEntry ate, int elementType)
			throws IOException,
			org.processmining.framework.models.ontology.InvalidModelException {
		List<String> newRefs;

		switch (elementType) {
		case WORKFLOW_MODEL_ELEMENT:
			newRefs = addInstance(ate.getElementModelReferences(), ate
					.getElement());
			ate.setElementModelReferences(newRefs);
			break;
		case ORIGINATOR:
			newRefs = addInstance(ate.getOriginatorModelReferences(), ate
					.getOriginator());
			ate.setOriginatorModelReferences(newRefs);
			break;
		case EVENTTYPE:
			newRefs = addInstance(ate.getTypeModelReferences(), ate.getType());
			ate.setTypeModelReferences(newRefs);
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid elementType in call to ConceptModel.addInstance");
		}
	}

	public void addInstance(ProcessInstance pi)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		pi
				.setModelReferences(addInstance(pi.getModelReferences(), pi
						.getName()));
	}

	public void addInstance(Process process)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		process.setModelReferences(addInstance(process.getModelReferences(),
				process.getName()));
	}

	public void addInstance(InfoItem item)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		item.setModelReferences(addInstance(item.getModelReferences(), item
				.getName()));
	}

	public void addInstance(DataSection dataAttributes, String key)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		dataAttributes.setModelReferences(key, addInstance(dataAttributes
				.getModelReferences(key), dataAttributes.get(key)));
	}

	private List<String> addInstance(List<String> inputModelRefs,
			String instanceName)
			throws org.processmining.framework.models.ontology.InvalidModelException {
		if (inputModelRefs.contains(instanceName)) { // TODO this is a linear
			// search now!
			return inputModelRefs;
		}

		List<String> refs = new ArrayList<String>(inputModelRefs);

		refs.add(getName());

		addInstance(sanitizeName(instanceName));
		ontology.setDirectlyReferenced(this);
		return refs;
	}

	public void addInstance(String instanceName) {
		String uri = OntologyModel.getOntologyPart(ontology.getName())
				+ OntologyModel.ONTOLOGY_SEPARATOR + instanceName;

		try {
			Instance instance = ontology.getFactory().createInstance(
					ontology.getFactory().createIRI(uri), concept);
			ontology.getWsmoOntology().addInstance(instance);
			instances.add(instanceName);
		} catch (SynchronisationException e) {
			e.printStackTrace();
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}
	}

	private static boolean isValidChar(char ch) {
		return isValidFirstChar(ch) || '0' <= ch && ch <= '9';
	}

	private static boolean isValidFirstChar(char ch) {
		return ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
	}

	public static String sanitizeName(String name) {
		StringBuffer result = new StringBuffer();
		boolean capitalizeNextChar = false;

		// proceeding with the sanitization
		name = name.trim();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (i == 0 && !isValidFirstChar(ch)) {
				result.append('_');
			} else if ((i == 0 && isValidFirstChar(ch))
					|| (i > 0 && isValidChar(ch))) {
				if (capitalizeNextChar) {
					result.append(Character.toUpperCase(ch));
					capitalizeNextChar = false;
				} else {
					result.append(ch);
				}
			} else if (Character.isWhitespace(ch)) {
				capitalizeNextChar = true;
			}
		}
		return result.toString();
	}

}

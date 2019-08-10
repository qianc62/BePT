package org.processmining.framework.models.orgmodel;

import org.processmining.framework.models.ontology.ConceptModel;
import java.util.HashSet;
import org.processmining.framework.ui.Message;
import java.util.ArrayList;
import org.processmining.framework.models.ontology.OntologyModel;

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
public class ResourceConcept extends Resource {

	private ArrayList<String> resources;

	public ArrayList<String> getInstances() {
		return resources;
	}

	public ResourceConcept(ConceptModel cm) {
		super(cm.getName(), cm.getOntology().getShortName()
				+ OntologyModel.ONTOLOGY_SEPARATOR + cm.getShortName());
		resources = new ArrayList<String>();
	}

	// methods for OrgEntity
	public void removeResource(String res) {
		if (resources == null) {
			Message
					.add(
							(new StringBuilder(
									"Tried to remove a null-value to the Org Entity of Resource"))
									.append(toString()).toString(), 3);
			return;
		}

		if (resources.contains(res))
			resources.remove(res);
	}

	public void addResource(String res) {
		if (resources == null) {
			Message
					.add(
							"Tried to add a null-value to the OrgEntity-list of Resource",
							4);
			// Message.add((new StringBuilder(
			// "Tried to add a null-value to the OrgEntity-list of Resource")).append(toString()).toString(),
			// 3);
			return;
		}
		if (!resources.contains(res))
			resources.add(res);
	}

	public boolean hasResource(String res) {
		boolean bResult = false;

		if (res != null) {
			if (resources.contains(res))
				bResult = true;
		}

		return bResult;
	}
}

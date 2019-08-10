package org.processmining.framework.models.orgmodel;

import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.Message;
import java.util.ArrayList;

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
public class TaskConcept extends Task {

	private ArrayList<String> tasks;

	public ArrayList<String> getInstances() {
		return tasks;
	}

	public TaskConcept(ConceptModel cm) {
		super(cm.getName(), cm.getOntology().getShortName()
				+ OntologyModel.ONTOLOGY_SEPARATOR + cm.getShortName(), " ");
		tasks = new ArrayList<String>();
	}

	// methods for OrgEntity
	public void removeTask(String res) {
		if (tasks == null) {
			Message
					.add(
							(new StringBuilder(
									"Tried to remove a null-value to the Org Entity of Resource"))
									.append(toString()).toString(), 3);
			return;
		}

		if (tasks.contains(res))
			tasks.remove(res);
	}

	public void addTask(String res) {
		if (tasks == null) {
			Message.add(
					"Tried to add a null-value to the OrgEntity-list of Task",
					4);
			// Message.add((new StringBuilder(
			// "Tried to add a null-value to the OrgEntity-list of Task")).append(toString()).toString(),
			// 3);
			return;
		}
		if (!tasks.contains(res))
			tasks.add(res);
	}

	public boolean hasTask(String res) {
		boolean bResult = false;

		if (res != null) {
			if (tasks.contains(res))
				bResult = true;
		}

		return bResult;
	}
}

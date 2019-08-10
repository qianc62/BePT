package org.processmining.analysis.originator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyCollection;

public class SemanticOTMatrix2DTableModel extends OTMatrix2DTableModel {

	private static final long serialVersionUID = 7066122944186464781L;
	private OntologyCollection ontologies;
	private List<ConceptModel> orgConcepts;
	private List<ConceptModel> taskConcepts;
	private boolean bSuper4Task, bSub4Task, bSuper4Ori, bSub4Ori;

	public SemanticOTMatrix2DTableModel(LogReader log) {
		super(log);
	}

	public SemanticOTMatrix2DTableModel(LogReader log, boolean bSuper4Task,
			boolean bSub4Task, boolean bSuper4Ori, boolean bSub4Ori) {
		super(log);
		this.bSuper4Task = bSuper4Task;
		this.bSub4Task = bSub4Task;
		this.bSuper4Ori = bSuper4Ori;
		this.bSub4Ori = bSub4Ori;
	}

	@Override
	protected void getOriginatorsAndTasks(LogReader log,
			List<String> originatorList, List<String> taskList) {
		Set<String> originators = new HashSet<String>();
		Set<String> tasks = new HashSet<String>();
		Iterator it = log.instanceIterator();

		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();

				originators.addAll(ate.getOriginatorModelReferences());
				tasks.addAll(ate.getElementModelReferences());
			}
		}

		ontologies = log.getLogSummary().getOntologies();
		taskConcepts = new ArrayList<ConceptModel>();
		orgConcepts = new ArrayList<ConceptModel>();
		translateConcepts(tasks, taskConcepts, true);
		translateConcepts(originators, orgConcepts, false);
		sortConcepts(taskConcepts);
		sortConcepts(orgConcepts);
		translateURIs(taskConcepts, taskList);
		translateURIs(orgConcepts, originatorList);
	}

	private void sortConcepts(List<ConceptModel> items) {
		Collections.sort(items, new Comparator<ConceptModel>() {
			public int compare(ConceptModel o1, ConceptModel o2) {
				int result = o1.getShortName().compareTo(o2.getShortName());
				return result == 0 ? o1.getOntology().getName().compareTo(
						o2.getOntology().getName()) : result;
			}
		});
	}

	@Override
	protected void getOriginators(AuditTrailEntry ate, List<String> result) {
		translateURIs(ate.getOriginatorModelReferences(), result, false);
	}

	@Override
	protected void getTasks(AuditTrailEntry ate, List<String> result) {
		translateURIs(ate.getElementModelReferences(), result, true);
	}

	public OntologyCollection getOntologyCollection() {
		init();
		return ontologies;
	}

	public List<ConceptModel> getOrgConcepts() {
		init();
		return orgConcepts;
	}

	public List<ConceptModel> getTaskConcepts() {
		init();
		return taskConcepts;
	}

	private void translateConcepts(Collection<String> from,
			List<ConceptModel> to, boolean isTask) {
		if (from == null) {
			return;
		}
		for (String uri : from) {
			ConceptModel concept = ontologies.findConceptByUriInLog(uri);

			if (concept != null) {
				getSuperSub(concept, to, isTask);
			}
		}
	}

	private void getSuperSub(ConceptModel concept, List<ConceptModel> to,
			boolean isTask) {
		if ((isTask && bSuper4Task) || (!isTask && bSuper4Ori)) {
			for (ConceptModel cp : concept.getOntology().getSuperConcepts(
					concept)) {
				to.add(cp);
			}
		}
		if ((isTask && bSub4Task) || (!isTask && bSub4Ori)) {
			for (ConceptModel cp : concept.getOntology()
					.getSubConcepts(concept)) {
				to.add(cp);
			}
		}
		to.add(concept);
	}

	private void translateURIs(Collection<ConceptModel> concepts,
			Collection<String> to) {
		for (ConceptModel concept : concepts) {
			to.add(concept.getShortName() + " ("
					+ concept.getOntology().getName() + ")");
		}
	}

	private void translateURIs(Collection<String> from, Collection<String> to,
			boolean isTask) {
		List<ConceptModel> concepts = new ArrayList<ConceptModel>();

		translateConcepts(from, concepts, isTask);
		translateURIs(concepts, to);
	}
}

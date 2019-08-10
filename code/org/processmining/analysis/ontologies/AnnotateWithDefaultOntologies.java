package org.processmining.analysis.ontologies;

import java.io.IOException;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.InvalidModelException;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.Progress;

public class AnnotateWithDefaultOntologies {

	private static final String RESUME = "Resume";
	private static final String SUSPEND = "Suspend";
	private static final String COMPLETE = "Complete";
	private static final String WITHDRAW = "Withdraw";
	private static final String ATE_ABORT = "AteAbort";
	private static final String PI_ABORT = "PiAbort";
	private static final String MANUAL_SKIP = "ManualSkip";
	private static final String AUTO_SKIP = "AutoSkip";
	private static final String START = "Start";
	private static final String REASSIGN = "Reassign";
	private static final String ASSIGN = "Assign";
	private static final String SCHEDULE = "Schedule";

	private static void createConcept(OntologyModel ontology,
			ConceptModel superConcept, String newConceptName,
			AuditTrailEntry ate, int type) throws InvalidModelException,
			IndexOutOfBoundsException, IOException {
		String sanitized = ConceptModel.sanitizeName(newConceptName);
		ConceptModel concept = ontology.findConcept(sanitized);

		if (concept == null) {
			concept = ontology.addConcept(sanitized);
			concept.addSuperConcept(superConcept);
		}
		concept.addInstance(ate, type);
	}

	private static void annotateDataSection(OntologyModel ontology,
			ConceptModel datafieldConcept, DataSection dataAttributes)
			throws InvalidModelException {
		for (String key : dataAttributes.keySet()) {
			String sanitized = ConceptModel.sanitizeName(key);
			ConceptModel concept = ontology.findConcept(sanitized);

			if (concept == null) {
				concept = ontology.addConcept(sanitized);
				concept.addSuperConcept(datafieldConcept);
			}
			concept.addInstance(dataAttributes, key);
		}
	}

	private static void createEVO(OntologyModel evo)
			throws InvalidModelException {
		ConceptModel schedule = evo.addConcept(SCHEDULE);
		ConceptModel start = evo.addConcept(START);
		ConceptModel manualSkip = evo.addConcept(MANUAL_SKIP);
		ConceptModel autoSkip = evo.addConcept(AUTO_SKIP);
		ConceptModel piAbort = evo.addConcept(PI_ABORT);
		ConceptModel complete = evo.addConcept(COMPLETE);
		ConceptModel assign = evo.addConcept(ASSIGN);
		ConceptModel ateAbort = evo.addConcept(ATE_ABORT);
		ConceptModel withdraw = evo.addConcept(WITHDRAW);
		ConceptModel suspend = evo.addConcept(SUSPEND);
		ConceptModel reassign = evo.addConcept(REASSIGN);
		ConceptModel resume = evo.addConcept(RESUME);
		ConceptModel successfulExecutionEvent = evo
				.addConcept("SuccessfulExecutionEvent");
		ConceptModel intermediateEvent = evo.addConcept("IntermediateEvent");
		ConceptModel executionEvent = evo.addConcept("ExecutionEvent");
		ConceptModel skip = evo.addConcept("Skip");
		ConceptModel initialEvent = evo.addConcept("InitialEvent");
		ConceptModel processEvent = evo.addConcept("ProcessEvent");
		ConceptModel managementEvent = evo.addConcept("ManagementEvent");
		ConceptModel unsuccessfulExecutionEvent = evo
				.addConcept("UnsuccessfulExecutionEvent");
		ConceptModel finalEvent = evo.addConcept("FinalEvent");
		ConceptModel monitoringEvent = evo.addConcept("MonitoringEvent");
		ConceptModel piComplete = evo.addConcept("PiComplete");

		schedule.addSuperConcept(managementEvent);
		start.addSuperConcept(initialEvent);
		manualSkip.addSuperConcept(skip);
		autoSkip.addSuperConcept(skip);
		successfulExecutionEvent.addSuperConcept(finalEvent);
		complete.addSuperConcept(successfulExecutionEvent);
		intermediateEvent.addSuperConcept(executionEvent);
		assign.addSuperConcept(managementEvent);
		executionEvent.addSuperConcept(processEvent);
		ateAbort.addSuperConcept(unsuccessfulExecutionEvent);
		initialEvent.addSuperConcept(executionEvent);
		processEvent.addSuperConcept(monitoringEvent);
		managementEvent.addSuperConcept(processEvent);
		unsuccessfulExecutionEvent.addSuperConcept(finalEvent);
		finalEvent.addSuperConcept(executionEvent);
		suspend.addSuperConcept(intermediateEvent);
		reassign.addSuperConcept(assign);
		resume.addSuperConcept(intermediateEvent);
		piAbort.addSuperConcept(managementEvent);
		piAbort.addSuperConcept(unsuccessfulExecutionEvent);
		skip.addSuperConcept(managementEvent);
		skip.addSuperConcept(successfulExecutionEvent);
		withdraw.addSuperConcept(managementEvent);
		withdraw.addSuperConcept(unsuccessfulExecutionEvent);
		piComplete.addSuperConcept(complete);
		piComplete.addSuperConcept(managementEvent);
	}

	private static ConceptModel getEVOConcept(OntologyModel evo, String type) {
		if (type.equals("schedule")) {
			return evo.findConcept(SCHEDULE);
		} else if (type.equals("assign")) {
			return evo.findConcept(ASSIGN);
		} else if (type.equals("reassign")) {
			return evo.findConcept(REASSIGN);
		} else if (type.equals("start")) {
			return evo.findConcept(START);
		} else if (type.equals("autoskip")) {
			return evo.findConcept(AUTO_SKIP);
		} else if (type.equals("manualskip")) {
			return evo.findConcept(MANUAL_SKIP);
		} else if (type.equals("pi_abort")) {
			return evo.findConcept(PI_ABORT);
		} else if (type.equals("ate_abort")) {
			return evo.findConcept(ATE_ABORT);
		} else if (type.equals("withdraw")) {
			return evo.findConcept(WITHDRAW);
		} else if (type.equals("complete")) {
			return evo.findConcept(COMPLETE);
		} else if (type.equals("suspend")) {
			return evo.findConcept(SUSPEND);
		} else if (type.equals("resume")) {
			return evo.findConcept(RESUME);
		} else {
			return null;
		}
	}

	@Analyzer(name = "Annotate with default ontologies", names = { "Log" })
	public static OntologySummaryResults analyse(LogReader inputLog)
			throws Exception {

		Progress progress = new Progress("Annotating the log "
				+ inputLog.getFile().getShortName()
				+ " with default ontologies.");

		// create a copy so we don't modify the original log
		LogReader log = LogReaderFactory.createInstance(
				inputLog.getLogFilter(), inputLog);

		OntologyModel tasks = log.getLogSummary().getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/TaskOntology",
						"TaskOntology");
		OntologyModel originators = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/OriginatorOntology",
						"OriginatorOntology");
		OntologyModel eventtypes = log.getLogSummary().getOntologies()
				.createOntology("http://www.processmining.org/ontologies/EVO",
						"EVO");
		OntologyModel PIs = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/ProcessInstanceOntology",
						"ProcessInstanceOntology");
		OntologyModel processes = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/ProcessOntology",
						"ProcessOntology");
		OntologyModel workflowlogs = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/WorkFlowLogOntology",
						"WorkFlowLogOntology");
		OntologyModel sources = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/SourceOntology",
						"SourceOntology");
		OntologyModel datafields = log
				.getLogSummary()
				.getOntologies()
				.createOntology(
						"http://www.processmining.org/ontologies/DataFieldOntology",
						"DataFieldOntology");
		ConceptModel task = tasks.addConcept("Task");
		ConceptModel originator = originators.addConcept("Originator");
		ConceptModel processinstance = PIs.addConcept("ProcessInstance");
		ConceptModel process = processes.addConcept("Process");
		ConceptModel workflowlog = workflowlogs.addConcept("WorkFlowLog");
		ConceptModel source = sources.addConcept("Source");
		ConceptModel datafield = datafields.addConcept("DataField");
		ConceptModel ate_data = datafields.addConcept("AuditTrailEntryData");
		ConceptModel pi_data = datafields.addConcept("ProcessInstanceData");
		ConceptModel process_data = datafields.addConcept("ProcessData");
		ConceptModel source_data = datafields.addConcept("SourceData");
		ConceptModel log_data = datafields.addConcept("LogData");

		createEVO(eventtypes);

		ate_data.addSuperConcept(datafield);
		pi_data.addSuperConcept(datafield);
		process_data.addSuperConcept(datafield);
		source_data.addSuperConcept(datafield);
		log_data.addSuperConcept(datafield);

		int barProgress = 0;
		progress.setMinMax(barProgress, log.getInstances().size());

		for (ProcessInstance pi : log.getInstances()) {
			if (progress != null && progress.isCanceled()) {
				return new OntologySummaryResults(log.getLogSummary()
						.getOntologies(), log);
			}
			progress.setNote("Annotating the process instances...");
			progress.setProgress(barProgress++);
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			for (int index = 0; index < ates.size(); index++) {
				AuditTrailEntry ate = ates.get(index);
				ConceptModel eventConcept = getEVOConcept(eventtypes, ate
						.getType());

				createConcept(tasks, task, ate.getElement(), ate,
						ConceptModel.WORKFLOW_MODEL_ELEMENT);
				if (eventConcept != null) {
					eventConcept.addInstance(ate, ConceptModel.EVENTTYPE);
				}
				if (ates.get(index).getOriginator() != null
						&& ate.getOriginator().trim().length() > 0) {
					createConcept(originators, originator, ate.getOriginator(),
							ate, ConceptModel.ORIGINATOR);
				}

				annotateDataSection(datafields, ate_data, ate
						.getDataAttributes());

				// we need to replace the ATE so the BufferedLogReader knows it
				// has changed
				pi.getAuditTrailEntryList().replace(ate, index);
			}

			annotateDataSection(datafields, pi_data, pi.getDataAttributes());
			processinstance.addInstance(pi);
		}

		progress.setMinMax(0, 4);
		progress
				.setNote("Building the graphical representation of the mined ontologies.");
		progress.setProgress(1);

		for (int index = 0; index < log.numberOfProcesses(); index++) {
			process.addInstance(log.getProcess(index));
			annotateDataSection(datafields, process_data, log.getProcess(index)
					.getDataAttributes());
		}
		progress.setProgress(2);

		workflowlog.addInstance(log.getLogSummary().getWorkflowLog());
		annotateDataSection(datafields, log_data, log.getLogSummary()
				.getWorkflowLog().getData());

		progress.setProgress(3);

		source.addInstance(log.getLogSummary().getSource());
		annotateDataSection(datafields, source_data, log.getLogSummary()
				.getSource().getData());
		progress.setProgress(4);
		progress.close();

		return new OntologySummaryResults(log.getLogSummary().getOntologies(),
				log);
	}

}

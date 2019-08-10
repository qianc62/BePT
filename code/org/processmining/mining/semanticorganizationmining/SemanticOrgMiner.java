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

package org.processmining.mining.semanticorganizationmining;

import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.OperationFactory;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleFactory2D;
import java.util.*;
import org.processmining.mining.snamining.miningoperation.UtilOperation;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.analysis.originator.OriginatorUI;
import org.processmining.analysis.originator.SemanticOTMatrix2DTableModel;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.models.orgmodel.ResourceConcept;
import org.processmining.framework.models.orgmodel.TaskConcept;
import org.processmining.framework.models.orgmodel.OrgModelConcept;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: TU/e IS department
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class SemanticOrgMiner implements MiningPlugin {

	// private OTMatrix2DTableModel dataMatrix = null;
	private OrgModelConcept orgModel = null;
	private SemanticOrgMinerOptions ui = null;
	private SocialNetworkMatrix snMatrix = null;

	DoubleMatrix2D conceptMatrix = null;
	List<ConceptModel> orgConceptList = null;
	List<ConceptModel> taskConceptList = null;
	SemanticOTMatrix2DTableModel semanticOTMatrix2DTableModel = null;

	public SemanticOrgMiner() {

	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new SemanticOrgMinerOptions();
		}
		return ui;
	}

	public MiningResult mine(final LogReader log) {
		orgModel = null;
		orgModel = new OrgModelConcept();
		LogSummary summary = log.getLogSummary();

		int indexType = 0;

		Message.add("<SemanticOrganizationalModelMining>", Message.TEST);
		BasicOperation baseOprtation;
		// makeConceptsList(log);
		semanticOTMatrix2DTableModel = new SemanticOTMatrix2DTableModel(log, ui
				.isSuperTask(), ui.isSubTask(), ui.isSuperOri(), ui.isSubOri());
		conceptMatrix = semanticOTMatrix2DTableModel.getOTMatrix();
		orgConceptList = new ArrayList<ConceptModel>(
				semanticOTMatrix2DTableModel.getOrgConcepts());
		taskConceptList = new ArrayList<ConceptModel>(
				semanticOTMatrix2DTableModel.getTaskConcepts());
		switch (ui.getSelectedMetrics()) {
		case SemanticOrgMinerOptions.DEFAULT_MINING:
			Message.add("<Options value=\"default\">", Message.TEST);
			defalutMiner(log);
			writeToTestLog();
			Message.add("</SemanticOrganizationalModelMining>", Message.TEST);
			return new SemanticOrgMiningResult(log, orgModel, null);
		case SemanticOrgMinerOptions.SIMILAR_TASK:
			return calculateDoingSimilarTask(log);

			/*
			 * case SemanticOrgMinerOptions.WORKING_TOGETHER:
			 * Message.add("<Options value=\"working_together\">",
			 * Message.TEST); snMatrix = null; indexType =
			 * SemanticOrgMinerOptions.WORKING_TOGETHER +
			 * ui.getWorkingTogetherSetting(); baseOprtation =
			 * OperationFactory.getOperation(indexType, summary, log); if
			 * (baseOprtation!=null){
			 * 
			 * snMatrix = new SocialNetworkMatrix(summary.getOriginators(),
			 * baseOprtation.calculation(0, 0)); } writeToTestLog();
			 * Message.add("</SemanticOrganizationalModelMining>",
			 * Message.TEST); // default mining result is required to assign a
			 * originator to a new group // assignment funtion is perforemd in
			 * SimilarTaskResultPanel. defalutMiner(log, summary);
			 * 
			 * return new OrgMiningResult(log, orgModel, snMatrix);
			 */
		}

		return null;

	}

	public SemanticOrgMiningResult calculateDoingSimilarTask(LogReader log) {
		snMatrix = null;

		switch (ui.getSimilarTaskSetting()) {
		case SemanticOrgMinerOptions.EUCLIDIAN_DISTANCE:
			snMatrix = new SocialNetworkMatrix(
					translateOntologyToString(orgConceptList), UtilOperation
							.euclidiandistance(conceptMatrix));
			break;

		case SemanticOrgMinerOptions.HAMMING_DISTANCE:
			snMatrix = new SocialNetworkMatrix(
					translateOntologyToString(orgConceptList), UtilOperation
							.hammingdistance(conceptMatrix));
			break;
		case SemanticOrgMinerOptions.CORRELATION_COEFFICIENT:
			snMatrix = new SocialNetworkMatrix(
					translateOntologyToString(orgConceptList), UtilOperation
							.correlationcoefficient(conceptMatrix));
			break;

		case SemanticOrgMinerOptions.SIMILARITY_COEFFICIENT:
			snMatrix = new SocialNetworkMatrix(
					translateOntologyToString(orgConceptList), UtilOperation
							.similaritycoefficient(conceptMatrix));
			break;
		default:
			break;
		}

		defalutMiner(log);
		return new SemanticOrgMiningResult(log, orgModel, snMatrix);
	}

	public String[] translateOntologyToString(List<ConceptModel> concepts) {
		String[] results = new String[concepts.size()];

		int i = 0;
		for (ConceptModel concept : concepts) {
			results[i++] = concept.getName();
		}

		return results;
	}

	public void defalutMiner(LogReader log) {

		for (int i = 0; i < orgConceptList.size(); i++) {
			ResourceConcept resource = new ResourceConcept(orgConceptList
					.get(i));
			orgModel.addResource(resource);

			for (String temp : orgConceptList.get(i).getInstances()) {
				resource.addResource(temp);
			}
		}

		for (int i = 0; i < taskConceptList.size(); i++) {
			TaskConcept tempTask = new TaskConcept(taskConceptList.get(i));
			OrgEntity tempEntity = new OrgEntity("minedRole" + i, tempTask
					.getID(), OrgEntity.ORGENTITYTYPE_MININGRESULT);
			orgModel.addOrgEntity(tempEntity);

			tempTask.addOrgEntity(tempEntity);
			orgModel.addTask(tempTask);
			System.out.println("test task = "
					+ taskConceptList.get(i).getInstances());
			for (String temp : taskConceptList.get(i).getInstances()) {
				tempTask.addTask(temp);
			}
		}

		log.reset();

		OntologyCollection ontologies = semanticOTMatrix2DTableModel
				.getOntologyCollection();

		Iterator it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				List<String> tempList = ate.getOriginatorModelReferences();
				List<String> tempList2 = ate.getElementModelReferences();
				for (String tempString : tempList) {
					ConceptModel cm = ontologies
							.findConceptByUriInLog(tempString);
					if (cm == null)
						continue;
					for (ConceptModel temp : getSuperSub(cm, false)) {
						Resource res = orgModel.getResource(temp.getName());
						if (ate.getOriginator() == null) {
							res = orgModel.getResource("");
						}
						for (String tempString2 : tempList2) {
							// assign roles related to a given task
							cm = ontologies.findConceptByUriInLog(tempString2);
							if (cm == null)
								continue;
							for (ConceptModel temp2 : getSuperSub(cm, true)) {
								// Task task = orgModel.getTask(temp.getName());
								if (orgModel.getTask(
										translateConceptName(temp2), " ") == null) {
									continue;
								}
								orgModel.getTask(translateConceptName(temp2),
										" ").addResourceToOrgEntity(res);
							}

						}
					}
				}
			}
		}
	}

	private Set<ConceptModel> getSuperSub(ConceptModel concept, boolean isTask) {
		Set<ConceptModel> to = new HashSet<ConceptModel>();
		if ((isTask && ui.isSuperTask()) || (!isTask && ui.isSuperOri())) {
			for (ConceptModel cp : concept.getOntology().getSuperConcepts(
					concept)) {
				to.add(cp);
			}
		}
		if ((isTask && ui.isSubTask()) || (!isTask && ui.isSubTask())) {
			for (ConceptModel cp : concept.getOntology()
					.getSubConcepts(concept)) {
				to.add(cp);
			}
		}
		to.add(concept);
		return to;
	}

	public String getName() {
		return "Semantic Organizational Miner";
	}

	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/semanticorganizationalminer";
	}

	public String translateConceptName(ConceptModel cm) {
		return cm.getOntology().getShortName()
				+ OntologyModel.ONTOLOGY_SEPARATOR + cm.getShortName();
	}

	public void writeToTestLog() {
		orgModel.writeToTestLog();
		if (snMatrix != null) {
			Message.add("<OMSummary SNMatrix =\"true\"\\>", Message.TEST);
			Message.add("<SNASummary minimumFlowValue=\""
					+ snMatrix.getMinFlowValue() + "\" maximumFlowValue=\""
					+ snMatrix.getMaxFlowValue() + "\"\\>", Message.TEST);
			Message.add("<SNASummary sumOfMatrix=\""
					+ snMatrix.getSumOfMatrix() + "\"\\>", Message.TEST);
		}
	}
}

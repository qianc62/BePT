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

package org.processmining.mining.organizationmining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.processmining.mining.organizationmining.algorithm.AHCAlgorithm;
import org.processmining.mining.organizationmining.algorithm.SOMAlgorithm;
import org.processmining.mining.organizationmining.algorithm.ClusteringAlgorithm;
import org.processmining.mining.organizationmining.algorithm.ClusteringInput;
import org.processmining.mining.organizationmining.distance.DistanceMetric;
import org.processmining.mining.organizationmining.distance.EuclideanDistance;
import org.processmining.mining.organizationmining.profile.ActivityProfile;
import org.processmining.mining.organizationmining.profile.AggregateProfile;
import org.processmining.mining.organizationmining.profile.Profile;
import org.processmining.mining.snamining.miningoperation.BasicOperation;
import org.processmining.mining.snamining.miningoperation.OperationFactory;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

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

public class OrgMiner implements MiningPlugin {

	private OrgModel orgModel = null;
	private OrgMinerOptions ui = null;
	private SocialNetworkMatrix snMatrix = null;
	protected List<Profile> profiles = null;
	protected AggregateProfile aggregateProfile = null;
	protected ClusteringAlgorithm algorithm = null;

	public OrgMiner() {

	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new OrgMinerOptions();
		}
		return ui;
	}

	public MiningResult mine(LogReader log) {
		orgModel = null;
		orgModel = new OrgModel();
		LogSummary summary = log.getLogSummary();

		int indexType = 0;

		Message.add("<OrganizationalModelMining>", Message.TEST);
		BasicOperation baseOprtation;
		switch (ui.getSelectedMetrics()) {
		case OrgMinerOptions.DEFAULT_MINING:
			Message.add("<Options value=\"default\">", Message.TEST);
			doDefalutMining(log, summary);
			writeToTestLog();
			Message.add("</OrganizationalModelMining>", Message.TEST);
			return new OrgMiningResult(log, orgModel, null);
		case OrgMinerOptions.AHC_MINING:
			Message.add("<Options value=\"AHC mining\">", Message.TEST);
			doAHCMining(log);
			doDefalutMining(log, summary);
			writeToTestLog();
			Message.add("</OrganizationalModelMining>", Message.TEST);
			return new HierOrgMiningResult(log, orgModel, algorithm);
		case OrgMinerOptions.SOM_MINING:
			Message.add("<Options value=\"SOM mining\">", Message.TEST);
			doSOMMining(log);
			doDefalutMining(log, summary);
			writeToTestLog();
			Message.add("</OrganizationalModelMining>", Message.TEST);
			return new HierOrgMiningResult(log, orgModel, algorithm);
		case OrgMinerOptions.SIMILAR_TASK:
			Message.add("<Options value=\"similar_task\">", Message.TEST);
			snMatrix = null;
			indexType = OrgMinerOptions.SIMILAR_TASK
					+ ui.getSimilarTaskSetting();
			baseOprtation = OperationFactory.getOperation(indexType, summary,
					log);
			if (baseOprtation != null) {

				snMatrix = new SocialNetworkMatrix(summary.getOriginators(),
						baseOprtation.calculation(0, 0));
			}
			writeToTestLog();
			Message.add("</OrganizationalModelMining>", Message.TEST);
			// default mining result is required to assign a originator to a new
			// group
			// assignment function is performed in SimilarTaskResultPanel.
			doDefalutMining(log, summary);

			return new OrgMiningResult(log, orgModel, snMatrix);
		case OrgMinerOptions.WORKING_TOGETHER:
			Message.add("<Options value=\"working_together\">", Message.TEST);
			snMatrix = null;
			indexType = OrgMinerOptions.WORKING_TOGETHER
					+ ui.getWorkingTogetherSetting();
			baseOprtation = OperationFactory.getOperation(indexType, summary,
					log);
			if (baseOprtation != null) {

				snMatrix = new SocialNetworkMatrix(summary.getOriginators(),
						baseOprtation.calculation(0, 0));
			}
			writeToTestLog();
			Message.add("</OrganizationalModelMining>", Message.TEST);
			// default mining result is required to assign a originator to a new
			// group
			// assignment function is performed in SimilarTaskResultPanel.
			doDefalutMining(log, summary);

			return new OrgMiningResult(log, orgModel, snMatrix);

		}

		return null;

	}

	public void doDefalutMining(LogReader log, LogSummary summary) {
		String users[] = summary.getOriginators();

		for (int i = 0; i < users.length; i++) {

			Resource resource = new Resource(users[i], users[i]);
			orgModel.addResource(resource);
		}

		LogEvents modelElements = summary.getLogEvents();
		for (int i = 0; i < modelElements.size(); i++) {
			OrgEntity tempEntity = new OrgEntity("minedRole" + i, modelElements
					.get(i).getModelElementName()
					+ "_" + modelElements.get(i).getEventType(),
					OrgEntity.ORGENTITYTYPE_MININGRESULT);
			orgModel.addOrgEntity(tempEntity);
			Task tempTask = new Task("minedtask" + i, modelElements.get(i)
					.getModelElementName(), modelElements.get(i).getEventType());
			tempTask.addOrgEntity(tempEntity);
			orgModel.addTask(tempTask);
		}

		log.reset();

		Iterator it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = (ProcessInstance) it.next();
			Iterator ates = pi.getAuditTrailEntryList().iterator();

			while (ates.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ates.next();
				// Which logEvent happened?
				LogEvent le = modelElements.findLogEvent(ate.getElement(), ate
						.getType());
				Resource res = orgModel.getResource(ate.getOriginator());
				if (ate.getOriginator() == null) {
					res = orgModel.getResource("");
				}
				if (res == null) {
					if (ate.getOriginator() != null)
						res = new Resource(ate.getOriginator(), ate
								.getOriginator());
					else
						res = new Resource("", "");
				}
				// add to org model
				orgModel.addResource(res);
				// assign roles related to a given task
				orgModel.getTask(le).addResourceToOrgEntity(res);
			}
		}
	}

	public void doAHCMining(LogReader log) {
		// make profile
		profiles = new ArrayList<Profile>();
		try {
			profiles.add(new ActivityProfile(log));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		aggregateProfile = new AggregateProfile(log);
		for (Profile profile : profiles) {
			if (profile.getNormalizationMaximum() > 0.0) {
				aggregateProfile.addProfile(profile);
			}
		}
		algorithm = new AHCAlgorithm();
		DistanceMetric metric = ui.getDistanceMetrics();// new
		// EuclideanDistance();
		ClusteringInput input = new ClusteringInput(aggregateProfile, metric);
		algorithm.setInput(input);
	}

	public void doSOMMining(LogReader log) {
		// make profile
		profiles = new ArrayList<Profile>();
		try {
			profiles.add(new ActivityProfile(log));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		aggregateProfile = new AggregateProfile(log);
		for (Profile profile : profiles) {
			if (profile.getNormalizationMaximum() > 0.0) {
				aggregateProfile.addProfile(profile);
			}
		}
		algorithm = new SOMAlgorithm();
		DistanceMetric metric = ui.getDistanceMetrics();// new
		// EuclideanDistance();
		ClusteringInput input = new ClusteringInput(aggregateProfile, metric);
		algorithm.setInput(input);
	}

	public String getName() {
		return "Organizational Miner";
	}

	public String getHtmlDescription() {
		return "This plugin only returns the relationship among activites and originators.";
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

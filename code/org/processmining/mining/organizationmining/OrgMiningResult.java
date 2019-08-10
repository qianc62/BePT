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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;
import org.processmining.mining.organizationmining.ui.OrgMiningResultPanel;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgMiningResult extends JPanel implements MiningResult, Provider,
		LogReaderConnection {

	protected OrgModel orgModel = new OrgModel();

	protected LogReader log;
	protected HLActivitySet actSet = new HLActivitySet(new ActivityGraph());
	protected SocialNetworkMatrix snMatrix;
	protected OrgMiningResultPanel gPanel;

	/**
	 * If this plugin is used multiple times, each time the simulation model to
	 * be provided will have an incremented number (in order to distinguish them
	 * later when they e.g., need to be joined)
	 */
	protected static int simulationModelCounter = 0;

	public OrgMiningResult(LogReader log, OrgModel orgModel) {
		this.log = log;
		this.orgModel = orgModel;
	}

	public OrgMiningResult(LogReader log, OrgModel orgModel,
			SocialNetworkMatrix snMatrix) {
		this.log = log;
		this.orgModel = orgModel;
		this.snMatrix = snMatrix;
	}

	public ProvidedObject[] getProvidedObjects() {
		int i = 0;
		if (log != null)
			i++;
		if (orgModel != null)
			i++;
		if (actSet != null)
			i++;
		ProvidedObject[] objects = new ProvidedObject[i];
		if (log != null)
			objects[--i] = new ProvidedObject("Whole Log", new Object[] { log });
		if (orgModel != null)
			objects[--i] = new ProvidedObject("Organization Model",
					new Object[] { orgModel });
		if (actSet != null)
			objects[--i] = new ProvidedObject(
					"Organizational Simulation Model No."
							+ simulationModelCounter, new Object[] { actSet });
		return objects;
	}

	public JComponent getVisualization() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public LogReader getLogReader() {
		return log;
	}

	private void jbInit() throws Exception {
		gPanel = new OrgMiningResultPanel(orgModel, snMatrix, this);
		generateActivitySet();
		this.setLayout(new BorderLayout());
		this.add(gPanel, BorderLayout.CENTER);
	}

	public OrgMiningResultPanel getOrgMiningResultPanel() {
		return gPanel;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		log = newLog;

		if (eventsMapping != null) {
			Iterator it = orgModel.getTaskList().iterator();
			while (it.hasNext()) {
				Task tempTask = (Task) it.next();
				Object[] mapped = (Object[]) eventsMapping.get(tempTask);
				// if the imported transition does not specify a log event type,
				// it is invisible by nature
				tempTask.setLogEvent((LogEvent) mapped[0]);
				System.out.println((String) mapped[1]);
			}
		}
	}

	public ArrayList getConnectableObjects() {
		ArrayList result = new ArrayList();
		result.addAll(orgModel.getTaskList());
		return result;
	}

	public void generateActivitySet() {
		// increment static sim model counter for this plugin
		simulationModelCounter = simulationModelCounter + 1;

		List<Task> orgTasks = orgModel.getTaskList();
		actSet = null;
		actSet = new HLActivitySet(new ActivityGraph());
		actSet.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.ORGANIZATIONAL_MODEL);
		actSet.getHLProcess().getGlobalInfo().setName(
				"Organizational Simulation Model No." + simulationModelCounter);

		if (orgTasks != null) {
			actSet.getHLProcess().getGlobalInfo().addPerspective(
					HLTypes.Perspective.ROLES_AT_TASKS);
			updateActivitySet();
		}
	}

	public void updateActivitySet() {

		HashMap<String, HLID> idMappingResource = new HashMap<String, HLID>();
		HashMap<String, HLID> idMappingOrgEntity = new HashMap<String, HLID>();

		List<Task> orgTasks = orgModel.getTaskList();
		List<Resource> orgResources = orgModel.getResourceList();
		List<OrgEntity> orgEntities = orgModel.getOrgEntityList();
		// / remove all groups
		actSet = null;
		actSet = new HLActivitySet(new ActivityGraph());
		actSet.getHLProcess().getGlobalInfo().addPerspective(
				HLTypes.Perspective.ORGANIZATIONAL_MODEL);
		actSet.getHLProcess().getGlobalInfo().setName(
				"Organizational Simulation Model No." + simulationModelCounter);

		if (orgTasks != null) {
			actSet.getHLProcess().getGlobalInfo().addPerspective(
					HLTypes.Perspective.ROLES_AT_TASKS);
			// add resources
			for (int i = 0; i < orgResources.size(); i++) {
				Resource tempRes = (Resource) orgResources.get(i);
				HLID resID = new HLID(tempRes.getID());
				// use given IDs to create high-level resource
				HLResource res = new HLResource(tempRes.getName(), actSet
						.getHLProcess(), resID);
				idMappingResource.put(tempRes.getID(), res.getID());
			}
			// add groups
			for (int i = 0; i < orgEntities.size(); i++) {
				OrgEntity tempEntity = (OrgEntity) orgEntities.get(i);
				HLID grpID = new HLID(tempEntity.getID());
				// use given IDs to create high level group
				HLGroup hlGroup = new HLGroup(tempEntity.getName(), actSet
						.getHLProcess(), grpID);
				idMappingOrgEntity.put(tempEntity.getID(), hlGroup.getID());
				ArrayList<String> tempOrgResources = orgModel
						.getResourceList(tempEntity);
				for (int j = 0; j < tempOrgResources.size(); j++) {
					HLResource res = (HLResource) actSet.getHLProcess()
							.getResource(
									idMappingResource.get(tempOrgResources
											.get(j)));
					hlGroup.addResource(res);
				}
			}
			// add activity
			for (int i = 0; i < orgTasks.size(); i++) {

				Task tempTask = (Task) orgTasks.get(i);
				HLActivity act = new HLActivity(tempTask.getName() + " "
						+ tempTask.getEventType(), actSet.getHLProcess());
				// to create vertices for activities
				actSet.addActivity(act);
				HashSet tempOrgEntitySet = tempTask.getOrgEntities();

				if (tempOrgEntitySet.size() == 0) {
					continue;
				} else if (tempOrgEntitySet.size() == 1) {
					// A Task has an OrgEntity
					OrgEntity orgEntity = (OrgEntity) tempOrgEntitySet
							.iterator().next();
					act.setGroup(idMappingOrgEntity.get(orgEntity.getID()));
				} else {
					// // A Task has two or more OrgEntities
					String listOrgEntity = tempTask.getEntityNameListString();
					HLGroup hlGroup = (HLGroup) actSet.getHLProcess().getGroup(
							idMappingOrgEntity.get(listOrgEntity));
					if (hlGroup == null) {
						hlGroup = new HLGroup(listOrgEntity, actSet
								.getHLProcess());
						idMappingOrgEntity.put(listOrgEntity, hlGroup.getID());
						for (Iterator iterator = tempOrgEntitySet.iterator(); iterator
								.hasNext();) {
							OrgEntity orgEntity = (OrgEntity) iterator.next();
							ArrayList<String> tempOrgResources = orgModel
									.getResourceList(orgEntity);
							for (int j = 0; j < tempOrgResources.size(); j++) {
								HLResource res = (HLResource) actSet
										.getHLProcess().getResource(
												idMappingResource
														.get(tempOrgResources
																.get(j)));
								hlGroup.addResource(res);
							}
						}
					}
					act.setGroup(hlGroup.getID());
				}
			}
		}
	}
}

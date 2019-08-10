package org.processmining.framework.models.orgmodel.algorithms;

import org.processmining.framework.models.orgmodel.OrgModel;
import java.util.HashMap;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.Task;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import org.processmining.framework.ui.Message;

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
public class OrgModelUtil {
	public OrgModelUtil() {
	}

	public static void aggregateTasks(OrgModel orgModel) {
		HashMap oldTasks = orgModel.getTasks();
		HashMap newTasks = new HashMap();

		String[] key = (String[]) oldTasks.keySet().toArray(
				new String[oldTasks.keySet().size()]);
		for (int i = 0; i < key.length; i++) {
			Task oldTask = (Task) oldTasks.get(key[i]);

			Task newTask;
			if (newTasks.containsKey(oldTask.getName()))
				newTask = (Task) newTasks.get(oldTask.getName());
			else {
				newTask = new Task(oldTask.getName(), oldTask.getName());
				newTasks.put(newTask.getID(), newTask);
			}

			HashSet tempEntities = oldTask.getOrgEntities();
			Iterator it = tempEntities.iterator();
			while (it.hasNext()) {
				OrgEntity tempEntity = (OrgEntity) it.next();
				newTask.addOrgEntity(tempEntity);
			}
		}

		oldTasks = null;
		orgModel.setTasks(newTasks);
	}

	public static void removeRedundantOrgEntity(OrgModel orgModel) {
		HashMap resources = orgModel.getResources();
		HashMap orgEntities = orgModel.getOrgEntities();

		HashMap tasks = orgModel.getTasks();

		String[] key = (String[]) orgEntities.keySet().toArray(
				new String[orgEntities.keySet().size()]);
		for (int i = 0; i < key.length - 1; i++) {
			OrgEntity firstOrgEntity = (OrgEntity) orgEntities.get(key[i]);
			if (firstOrgEntity == null)
				continue;
			ArrayList<String> firstList = orgModel
					.getResourceList(firstOrgEntity);

			for (int j = i + 1; j < key.length; j++) {
				OrgEntity secondOrgEntity = (OrgEntity) orgEntities.get(key[j]);
				if (secondOrgEntity == null)
					continue;
				ArrayList<String> secondList = orgModel
						.getResourceList(secondOrgEntity);

				if (firstList.size() != secondList.size())
					continue;

				boolean bFlag = true;
				for (int k = 0; k < firstList.size(); k++) {

					if (!firstList.get(k).equals(secondList.get(k))) {
						bFlag = false;
						break;
					}
				}

				if (bFlag) {
					String[] key2 = (String[]) tasks.keySet().toArray(
							new String[tasks.keySet().size()]);
					for (int k = 0; k < key2.length; k++) {
						Task tempTask = (Task) tasks.get(key2[k]);
						if (tempTask.hasOrgEntity(secondOrgEntity)) {
							tempTask.removeOrgEntity(secondOrgEntity);
							tempTask.addOrgEntity(firstOrgEntity);
						}
					}
					key2 = (String[]) resources.keySet().toArray(
							new String[resources.keySet().size()]);
					for (int k = 0; k < key2.length; k++) {
						Resource tempRes = (Resource) resources.get(key2[k]);
						if (tempRes.hasOrgEntity(secondOrgEntity)) {
							tempRes.removeOrgEntity(secondOrgEntity);
						}
					}
					orgEntities.remove(secondOrgEntity.getID());
					secondOrgEntity = null;
				}
			}
		}
	}

}

package org.processmining.analysis.socialsuccess.bigfive.extraversion;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;

public class ModeratingGroups extends Behaviour {
	// Make friends Easily + Take Charge
	public ModeratingGroups(Trait tr) {
		super(tr);
	}

	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		OrgModel om = this.trait.getData().getOrgModel();
		HashMap<String, Double> membersPerOwner = new HashMap<String, Double>();
		HashMap<String, Resource> resources = om.getResources();
		Iterator<String> keyIterator = resources.keySet().iterator();
		while (keyIterator.hasNext()) {
			String name = keyIterator.next();
			Resource resource = resources.get(name);
			Iterator<String> admins = om.getOrgEntityList(resource,
					"Group/admin").iterator();
			double sum = 0;
			while (admins.hasNext()) {
				sum += om.getResourceList(om.getOrgEntity(admins.next()))
						.size();
			}
			membersPerOwner.put(name, sum);
		}
		calculateResults(membersPerOwner);
	}
}

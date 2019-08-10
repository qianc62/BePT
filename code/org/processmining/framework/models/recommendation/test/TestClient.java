package org.processmining.framework.models.recommendation.test;

import java.io.IOException;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.models.recommendation.net.client.RecommendationProviderProxy;

public class TestClient {

	public static void main(String args[]) {
		RecommendationProvider provider = new RecommendationProviderProxy(
				"localhost", 4444);
		RecommendationQuery query = new RecommendationQuery("ProcessId",
				"ProcessInstanceId");
		query.addAuditTrailEntry(generateAuditTrailEntry("A"));
		// query.addAuditTrailEntry(generateAuditTrailEntry("TaskB"));
		// query.addAuditTrailEntry(generateAuditTrailEntry("TaskC"));
		// query.addAuditTrailEntry(generateAuditTrailEntry("TaskD"));
		// query.setProcessInstanceAttribute("one", "The number 1");
		// query.setProcessInstanceAttribute("two", "The number 2");
		// query.setProcessInstanceAttribute("three", "The number 3");
		// query.addAvailableTask("E");
		// query.addAvailableTask("F");
		// query.addAvailableTask("G");
		// query.addAvailableUser("John Doe");
		// query.addAvailableUser("Bela Lugosi");
		// query.addAvailableUser("Boris Karloff");
		// query.addFilterUser("Filter user 1");
		// query.addFilterUser("Filter user 2");
		// query.addFilterRole("Filter role 1");
		// query.addFilterRole("Filter role 2");
		// query.addFilterGroup("Filter group 1");
		// query.addFilterGroup("Filter group 2");
		// get recommendation from provider proxy
		System.out.println("Sending query:");
		System.out.println(query.toString());
		RecommendationResult result = null;
		try {
			result = provider.getRecommendation(query);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\n\n\n");
		System.out.println("received result: ");
		for (Recommendation rec : result) {
			System.out.println(rec.toString());
		}
	}

	protected static AuditTrailEntry generateAuditTrailEntry(String task) {
		AuditTrailEntryImpl ate = new AuditTrailEntryImpl();
		ate.setElement(task);
		ate.setTimestamp(System.currentTimeMillis());
		ate.setType("start");
		// ate.setOriginator("John Doe");
		// ate.setAttribute("one", "the number 1");
		// ate.setAttribute("two", "the number 2");
		// ate.setAttribute("three", "the number 3");
		return ate;
	}
}

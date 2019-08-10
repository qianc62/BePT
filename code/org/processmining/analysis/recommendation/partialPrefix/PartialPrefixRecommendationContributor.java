package org.processmining.analysis.recommendation.partialPrefix;

import org.processmining.analysis.recommendation.contrib.LogBasedContributor;
import org.processmining.framework.models.logabstraction.PartialPrefixAbstractionFactory;

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
public class PartialPrefixRecommendationContributor extends LogBasedContributor {

	public PartialPrefixRecommendationContributor() {
		super();
		logAbstractionFactory = new PartialPrefixAbstractionFactory();
	};

	public String getName() {
		return "Partial Prefix Abstraction";
	}

	public String getHtmlDescription() {
		return "Calculate the partial prefix abstraction of a process instance";
	}

	/*
	 * public RecommendationResult
	 * generateFilteredRecommendations(RecommendationQuery query, String
	 * process) { ProcessInstance pi = query.toProcessInstance(process);
	 * 
	 * // TODO generalize the int LogAbstraction incompletePIAbstraction =
	 * ((PartialPrefixAbstractionFactory
	 * )logAbstractionFactory).getAbstraction(pi, scale, 3); String queryId =
	 * query.getId();
	 * 
	 * return generateFilteredRecommendations(incompletePIAbstraction, queryId);
	 * }
	 */

}

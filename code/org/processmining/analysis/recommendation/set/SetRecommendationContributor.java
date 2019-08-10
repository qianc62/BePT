package org.processmining.analysis.recommendation.set;

import org.processmining.analysis.recommendation.contrib.LogBasedContributor;
import org.processmining.framework.models.logabstraction.MultiSetAbstractionFactory;

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
public class SetRecommendationContributor extends LogBasedContributor {

	public SetRecommendationContributor() {
		super();
		logAbstractionFactory = new MultiSetAbstractionFactory(false);
	};

	public String getName() {
		return "Set Abstraction";
	}

	public String getHtmlDescription() {
		return "Calculate the set abstraction of a process instance";
	}

}

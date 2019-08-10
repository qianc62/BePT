package org.processmining.framework.models.recommendation.net.client;

import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.analysis.recommendation.contrib.RecommendationContributor;

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
public class RestartRequest {

	private String scale;
	private String contributor;

	public RestartRequest(String scale, String contributor) {
		this.scale = scale;
		this.contributor = contributor;
	}

	public String getScale() {
		return scale;
	}

	public String getContributor() {
		return contributor;
	}

}

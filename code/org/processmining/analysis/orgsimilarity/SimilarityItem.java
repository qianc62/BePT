/*
 * Copyright (c) 2007 Minseok Song
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */

package org.processmining.analysis.orgsimilarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.orgmodel.OrgModel;

;

/**
 * Encapsulates one item to be calculated, i.e. a org model which is named after
 * the algorithm having mined it. Provides storage for named measurements (use
 * metrics for names / keys).
 * 
 * @author Minseok Song
 */
public class SimilarityItem {

	protected String name;
	protected String modelName;
	protected OrgModel model;
	protected LogReader log;
	protected Map<String, Double> measurements;

	public SimilarityItem(String aName, OrgModel aModel, String aModelName) {
		name = aName + " (Originator:" + aModel.getResourceList().size()
				+ ",OrgEntity:" + aModel.getOrgEntityList().size() + ")";
		model = aModel;
		modelName = aModelName;
		measurements = new HashMap<String, Double>();
	}

	public String getName() {
		return name;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModel(OrgModel aModel) {
		model = aModel;
	}

	public OrgModel getModel() {
		return model;
	}

	public void setLog(LogReader aLog) {
		log = aLog;
	}

	public LogReader getLog() {
		return log;
	}

	public Set<String> getMeasurementKeys() {
		return measurements.keySet();
	}

	public double getMeasurement(String metricName) {
		return measurements.get(metricName);
	}

	public void setMeasurement(String metricName, double measurement) {
		measurements.put(metricName, measurement);
	}

	public String toString() {
		return name;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SimilarityItem) {
			SimilarityItem other = (SimilarityItem) obj;
			return (other.name.equals(name) && other.model.equals(model));
		} else {
			return false;
		}
	}

	public int hashCode() {
		return name.hashCode();
	}

}

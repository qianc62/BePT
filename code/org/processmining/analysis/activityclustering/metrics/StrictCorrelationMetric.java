/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.activityclustering.metrics;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.util.StringSimilarity;

/**
 * @author christian
 * 
 */
public class StrictCorrelationMetric implements CorrelationMetric {

	protected boolean enforceSameOriginator;
	protected boolean useProximity;
	protected long proximityZone;
	protected boolean useDataAttributes;
	protected boolean useDataValues;
	protected boolean useEventNames;
	protected boolean useEventTypes;

	public StrictCorrelationMetric() {
		enforceSameOriginator = true;
		useProximity = true;
		proximityZone = 600000; // 10 minutes
		useDataAttributes = true;
		useDataValues = true;
		useEventNames = true;
		useEventTypes = true;
	}

	/**
	 * @return the enforceSameOriginator
	 */
	public boolean isEnforceSameOriginator() {
		return enforceSameOriginator;
	}

	/**
	 * @param enforceSameOriginator
	 *            the enforceSameOriginator to set
	 */
	public void setEnforceSameOriginator(boolean enforceSameOriginator) {
		this.enforceSameOriginator = enforceSameOriginator;
	}

	/**
	 * @return the useProximity
	 */
	public boolean isUseProximity() {
		return useProximity;
	}

	/**
	 * @param useProximity
	 *            the useProximity to set
	 */
	public void setUseProximity(boolean useProximity) {
		this.useProximity = useProximity;
	}

	/**
	 * @return the proximityZone
	 */
	public long getProximityZone() {
		return proximityZone;
	}

	/**
	 * @param proximityZone
	 *            the proximityZone to set
	 */
	public void setProximityZone(long proximityZone) {
		this.proximityZone = proximityZone;
	}

	/**
	 * @return the useDataAttributes
	 */
	public boolean isUseDataAttributes() {
		return useDataAttributes;
	}

	/**
	 * @param useDataAttributes
	 *            the useDataAttributes to set
	 */
	public void setUseDataAttributes(boolean useDataAttributes) {
		this.useDataAttributes = useDataAttributes;
	}

	/**
	 * @return the useDataValues
	 */
	public boolean isUseDataValues() {
		return useDataValues;
	}

	/**
	 * @param useDataValues
	 *            the useDataValues to set
	 */
	public void setUseDataValues(boolean useDataValues) {
		this.useDataValues = useDataValues;
	}

	/**
	 * @return the useEventNames
	 */
	public boolean isUseEventNames() {
		return useEventNames;
	}

	/**
	 * @param useEventNames
	 *            the useEventNames to set
	 */
	public void setUseEventNames(boolean useEventNames) {
		this.useEventNames = useEventNames;
	}

	/**
	 * @return the useEventTypes
	 */
	public boolean isUseEventTypes() {
		return useEventTypes;
	}

	/**
	 * @param useEventTypes
	 *            the useEventTypes to set
	 */
	public void setUseEventTypes(boolean useEventTypes) {
		this.useEventTypes = useEventTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.activityclustering.metrics.CorrelationMetric
	 * #measureCorrelation(org.processmining.framework.log.AuditTrailEntry,
	 * org.processmining.framework.log.AuditTrailEntry)
	 */
	public double measureCorrelation(AuditTrailEntry a, AuditTrailEntry b) {
		double correlation = 0.0;
		int divisor = 0;
		if (enforceSameOriginator == true && sameOriginators(a, b) == false) {
			return 0.0;
		}
		if (useProximity == true) {
			double proximity = measureProximity(a, b);
			if (proximity >= 0.0) {
				correlation += divisor++;
			}
		}
		if (useDataAttributes == true) {
			correlation += measureDataAttributes(a, b);
			divisor++;
		}
		if (useDataValues == true) {
			correlation += measureDataValues(a, b);
			divisor++;
		}
		if (useEventNames == true) {
			correlation += measureEventNames(a, b);
			divisor++;
		}
		if (useEventTypes == true) {
			correlation += measureEventTypes(a, b);
			divisor++;
		}
		if (divisor > 0) {
			return correlation / divisor;
		} else {
			return 0.0;
		}
	}

	protected double measureEventNames(AuditTrailEntry a, AuditTrailEntry b) {
		return StringSimilarity.similarity(a.getElement(), b.getElement());
	}

	protected double measureEventTypes(AuditTrailEntry a, AuditTrailEntry b) {
		if (a.getType().equals(b.getType()) == true) {
			return 0.0;
		} else {
			return 1.0;
		}
	}

	protected boolean sameOriginators(AuditTrailEntry a, AuditTrailEntry b) {
		String origA = a.getOriginator();
		String origB = b.getOriginator();
		boolean aValid = (origA != null && origA.length() > 0);
		boolean bValid = (origB != null && origB.length() > 0);
		if (aValid == true && bValid == true) {
			return origA.equals(origB);
		} else if (aValid == true || bValid == true) {
			return false;
		} else {
			return true;
		}
	}

	protected double measureDataAttributes(AuditTrailEntry a, AuditTrailEntry b) {
		Set<String> aKeys = a.getAttributes().keySet();
		Set<String> bKeys = b.getAttributes().keySet();
		int size = aKeys.size();
		int bSize = bKeys.size();
		if (bSize < size) {
			size = bSize;
		}
		int intersection = 0;
		for (String key : aKeys) {
			if (bKeys.contains(key)) {
				intersection++;
			}
		}
		return (double) intersection / (double) size;
	}

	protected double measureDataValues(AuditTrailEntry a, AuditTrailEntry b) {
		Map<String, String> aData = a.getAttributes();
		Map<String, String> bData = b.getAttributes();
		int size = aData.size();
		int bSize = bData.size();
		if (bSize < size) {
			size = bSize;
		}
		double similarity = 0.0;
		for (String key : aData.keySet()) {
			if (bData.containsKey(key) == true) {
				similarity += StringSimilarity.similarity(aData.get(key), bData
						.get(key));
			}
		}
		similarity /= size;
		return similarity;
	}

	protected double measureProximity(AuditTrailEntry a, AuditTrailEntry b) {
		Date dA = a.getTimestamp();
		Date dB = b.getTimestamp();
		if (dA != null && dB != null) {
			long tDifSec = (dB.getTime() - dA.getTime()) / proximityZone;
			if (tDifSec > 0) {
				return 1.0 / (double) tDifSec;
			} else {
				return 1.0;
			}
		} else {
			return -1;
		}
	}

}

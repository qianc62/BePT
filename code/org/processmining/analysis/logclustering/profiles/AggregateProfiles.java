package org.processmining.analysis.logclustering.profiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

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
 * @author Minseok Song (m.s.song@tue.nl)
 * @version 1.0
 */
public class AggregateProfiles extends Profile {

	protected List<Profile> profiles;

	public AggregateProfiles(int aTraceSize) {
		super("AggregateLog profile", "Profile for Aggregation", aTraceSize);
		profiles = new ArrayList<Profile>();
	}

	public void addProfileMetrics(Profile aMetric) {
		if (aMetric.getNormalizationMaximum() != 0)
			profiles.add(aMetric);
	}

	public DoubleMatrix2D calcuateDistance(DistanceMeasure dm) {
		DoubleMatrix2D distance = DoubleFactory2D.dense.make(traceSize,
				traceSize, 0);
		for (Profile aProfile : profiles) {
			if (aProfile instanceof VectorProfile) {
				VectorProfile tempProfile = (VectorProfile) aProfile;
				if (tempProfile.getNormalizationMaximum() != 0)
					distance.assign(tempProfile.calculateDistance(dm),
							cern.jet.math.Functions.plus);
			}
		}
		return distance;
	}

	public DoubleMatrix2D calcuateDistance(DistanceMeasure dm,
			DoubleMatrix2D centers) {
		DoubleMatrix2D distance = DoubleFactory2D.dense.make(traceSize, centers
				.rows(), 0);

		int index = 0;
		for (Profile aProfile : profiles) {
			if (aProfile instanceof VectorProfile) {
				VectorProfile tempProfile = (VectorProfile) aProfile;
				DoubleMatrix2D tempCenter = centers.viewPart(0, index, centers
						.rows(), tempProfile.getProfileSize());
				index += tempProfile.getProfileSize();
				if (tempProfile.getNormalizationMaximum() != 0)
					distance.assign(tempProfile.calculateDistance(dm,
							tempCenter), cern.jet.math.Functions.plus);
			}
		}
		return distance;
	}

	public DoubleMatrix1D generateRandomPoint() {
		int nProfileSize = getProfileSize();
		DoubleMatrix1D point = DoubleFactory1D.dense.make(nProfileSize);

		int k = 0;
		for (Profile aProfile : profiles) {
			if (aProfile instanceof VectorProfile) {
				VectorProfile tempProfile = (VectorProfile) aProfile;
				DoubleMatrix1D tempMatrix = tempProfile.generateRandomPoint();
				for (int i = k; i < k + tempMatrix.size(); i++) {
					point.set(i, tempMatrix.get(i - k));
				}
				k += tempMatrix.size();
			}
		}
		return point;
	}

	public double getSumOfNormalizationMaximum() {
		double size = 0;
		for (Profile aMetric : profiles) {
			size += aMetric.getNormalizationMaximum();
		}
		return size;
	}

	/*
	 * return center of the matrix
	 */
	public DoubleMatrix1D calcuateCenter(HashSet<Integer> traceSet) {

		int nProfileSize = getProfileSize();
		DoubleMatrix1D center = DoubleFactory1D.dense.make(nProfileSize);

		int k = 0;
		for (Profile aProfile : profiles) {
			if (aProfile instanceof VectorProfile) {
				VectorProfile tempProfile = (VectorProfile) aProfile;
				DoubleMatrix1D tempMatrix = tempProfile
						.calcuateCenter(traceSet);
				for (int i = k; i < k + tempMatrix.size(); i++) {
					center.set(i, tempMatrix.get(i - k));
				}
				k += tempMatrix.size();
			}
		}
		return center;
	}

	/*
	 * return size of Profiles, until now it only considers VectorProfiles
	 */
	public int getProfileSize() {
		int size = 0;
		for (Profile aMetric : profiles) {
			if (aMetric instanceof VectorProfile) {
				size += ((VectorProfile) aMetric).getProfileSize();
			}
		}
		return size;
	}
}

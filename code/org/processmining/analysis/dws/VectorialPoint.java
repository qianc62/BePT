package org.processmining.analysis.dws;

import java.util.ArrayList;

/**
 * Defines data structures and procedures for projecting each trace in the
 * feature space.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class VectorialPoint implements Comparable {
	double[] coordinates;
	int ID; // ID della traccia

	public VectorialPoint(Trace t, ArrayList features, int ID) {
		this.ID = ID;
		coordinates = new double[features.size()];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = project(t, (Feature) features.get(i));
		}
	}

	public double distance(VectorialPoint v) {
		double ris = 0;
		for (int i = 0; i < coordinates.length; i++) {
			ris += (coordinates[i] - v.coordinates[i])
					* (coordinates[i] - v.coordinates[i]);
		}
		return Math.sqrt(ris);
	}

	public double project(Trace t, Feature f) {
		Trace head = f.getHead();
		if (t.contains(head)) {
			return 0;
		}
		double occurrence = t.overlapDecaying(f.getBody());
		double whole = f.getBody().overlapDecaying(f.getBody());
		// double occurrence=t.overlap(f.getBody());
		// if (occurrence==f.getBody().size()) return 1; else return 0;
		// return occurrence/f.getBody().size();
		return occurrence / whole;
	}

	public int compareTo(Object o) {
		VectorialPoint vp = (VectorialPoint) o;
		for (int i = 0; i < coordinates.length; i++) {
			if (coordinates[i] < vp.coordinates[i]) {
				return -1;
			}
			if (coordinates[i] > vp.coordinates[i]) {
				return 1;
			}
		}
		return 0;
	}

	public String toString() {
		String ris = "ID:" + ID + " ";
		for (int i = 0; i < coordinates.length - 1; i++) {
			ris += coordinates[i] + ";";
		}
		ris += coordinates[coordinates.length - 1];
		return ris;
	}
}

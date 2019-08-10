package org.processmining.analysis.socialsuccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.processmining.analysis.socialsuccess.bigfive.*;
import org.processmining.analysis.socialsuccess.clustering.KMeans5D;
import org.processmining.framework.ui.Message;

public class BigFive {
	public static final Double additionConst = 0.25;
	private static final boolean testModus = false;
	private int genWithNClusters = 0;
	private PersonalityData data;
	private Openness openness;
	private Conscientiousness conscientiousness;
	private Extraversion extraversion;
	private Agreeableness agreeableness;
	private Neuroticism neuroticism;
	private ArrayList<TreeMap<String, Vector<Double>>> clusters = null;

	// TODO
	private TreeMap<String, Vector<Double>> test = null;

	public BigFive(PersonalityData ssd) {
		this.data = ssd;
		openness = new Openness(this.data);
		conscientiousness = new Conscientiousness(this.data);
		extraversion = new Extraversion(this.data);
		agreeableness = new Agreeableness(this.data);
		neuroticism = new Neuroticism(this.data);

		if (testModus) {
			test = new TreeMap<String, Vector<Double>>();
			Vector<Double> t = new Vector<Double>();
			t.add(0, 0.25 + additionConst);
			t.add(1, 0.6666666666 + additionConst);
			t.add(2, 0.916666 + additionConst);
			t.add(3, 0.0833333 + additionConst);
			t.add(4, 0.25 + additionConst);
			test.put("U_1", new Vector<Double>(t));
			t.removeAllElements();
			t.add(0, 0.5 + additionConst);
			t.add(1, 0.58333 + additionConst);
			t.add(2, 0.41666 + additionConst);
			t.add(3, 0.3333 + additionConst);
			t.add(4, 0.625 + additionConst);
			test.put("U_2", new Vector<Double>(t));
			t.removeAllElements();
			t.add(0, 0.75 + additionConst);
			t.add(1, 0.6666 + additionConst);
			t.add(2, 0.5 + additionConst);
			t.add(3, 0.75 + additionConst);
			t.add(4, 0.75 + additionConst);
			test.put("U_3", new Vector<Double>(t));
			t.removeAllElements();
			t.add(0, 1. + additionConst);
			t.add(1, 0.58333 + additionConst);
			t.add(2, 0.41666 + additionConst);
			t.add(3, 0.41666 + additionConst);
			t.add(4, 0.75 + additionConst);
			test.put("U_4", new Vector<Double>(t));
			t.removeAllElements();
			t.add(0, 0. + additionConst);
			t.add(1, 0. + additionConst);
			t.add(2, 0.25 + additionConst);
			t.add(3, 0.91666 + additionConst);
			t.add(4, 0.125 + additionConst);
			test.put("U_5", new Vector<Double>(t));
			t.removeAllElements();
		}
	}

	public TreeSet<String> getUsers() {
		if (testModus) {
			return new TreeSet<String>(test.keySet());
		} else {
			TreeSet<String> set = new TreeSet<String>();
			Message.add("SSA: Get users.");
			set.addAll(openness.getUsers());
			set.addAll(conscientiousness.getUsers());
			set.addAll(extraversion.getUsers());
			set.addAll(agreeableness.getUsers());
			set.addAll(neuroticism.getUsers());
			return set;
		}
	}

	public HashMap<String, Vector<Double>> getResults() {
		HashMap<String, Vector<Double>> ret = new HashMap<String, Vector<Double>>();
		Iterator<String> userIt = this.getUsers().iterator();
		if (testModus) {
			while (userIt.hasNext()) {
				String uid = userIt.next();
				ret.put(uid, test.get(uid));
			}
		} else {
			while (userIt.hasNext()) {
				String uid = userIt.next();
				ret.put(uid, getResults(uid));
			}
		}
		return ret;
	}

	public Vector<Double> getResults(String uid) {
		if (uid.startsWith("Cluster")) {
			int index = Integer.parseInt(uid.substring(7).trim());
			return getClusters().get(index).get("_reserved");
		} else {
			if (testModus) {
				return test.get(uid);
			} else {
				Vector<Double> scores = new Vector<Double>();
				scores.add(0, openness.getAnalysis(uid));
				scores.add(1, conscientiousness.getAnalysis(uid));
				scores.add(2, extraversion.getAnalysis(uid));
				scores.add(3, agreeableness.getAnalysis(uid));
				scores.add(4, neuroticism.getAnalysis(uid));
				return scores;
			}
		}
	}

	public Double[][] getDetailedResults(String uid) {
		Double[][] scores = new Double[5][];
		scores[0] = openness.getDetailedAnalysis(uid);
		scores[1] = conscientiousness.getDetailedAnalysis(uid);
		scores[2] = extraversion.getDetailedAnalysis(uid);
		scores[3] = agreeableness.getDetailedAnalysis(uid);
		scores[4] = neuroticism.getDetailedAnalysis(uid);
		return scores;
	}

	public ArrayList<TreeMap<String, Vector<Double>>> getClusters() {
		if (clusters == null || genWithNClusters != data.getNrOfClusters()) {
			genWithNClusters = data.getNrOfClusters();
			HashMap<String, Vector<Double>> scores = getResults();
			KMeans5D k = new KMeans5D(data.getNrOfClusters(), scores);
			clusters = k.getClusters();
		}
		return clusters;
	}
}

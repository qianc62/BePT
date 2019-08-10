package org.processmining.analysis.socialsuccess.bigfive;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import org.processmining.analysis.socialsuccess.*;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

/**
 * This file contains the core functionality which is used for the 5
 * characteristics mentioned in the big-5 method. Those characteristics are: #
 * Openness - appreciation for art, emotion, adventure, unusual ideas,
 * imagination, curiosity, and variety of experience. # Conscientiousness - a
 * tendency to show self-discipline, act dutifully, and aim for achievement;
 * planned rather than spontaneous behaviour. # Extraversion - energy, positive
 * emotions, surgency, and the tendency to seek stimulation and the company of
 * others. # Agreeableness - a tendency to be compassionate and cooperative
 * rather than suspicious and antagonistic towards others. # Neuroticism - a
 * tendency to experience unpleasant emotions easily, such as anger, anxiety,
 * depression, or vulnerability; sometimes called emotional instability.
 * 
 * @author mvanwingerden
 */
abstract public class Trait {
	protected Behaviour[] b;
	protected PersonalityData data;
	protected TreeSet<String> users;

	/**
	 * 
	 * @param bigFive
	 */
	public Trait(PersonalityData inp) {
		data = inp;
		users = new TreeSet<String>();
		b = new Behaviour[5];
		loadBehaviour();
	}

	/**
	 * Load the behaviour you want to be analyzed
	 */
	abstract protected void loadBehaviour();

	/**
	 * This function calculated the score for this Trait
	 * 
	 * @param userIdentifier
	 *            the identifier of a user.
	 * @return the average sum of the behaviours
	 */
	final public double getAnalysis(String userIdentifier) {
		Message.add("SSA: Trait: getAnalysis");
		int n = 0;
		double sum = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i] == null)
				continue;
			double score = b[i].getAnalysis(userIdentifier);
			if (score != -1) {
				sum += score;
				n++; // er is er weer een gevonden
			}
		}
		double score = (double) ((int) ((sum / n + BigFive.additionConst) * 1000)) / 1000;
		return score;
	}

	final public Double[] getDetailedAnalysis(String userIdentifier) {
		Double[] scores = new Double[5];
		for (int i = 0; i < b.length; i++) {
			if (b[i] == null) {
				scores[i] = null;
			} else if (b[i].getAnalysis(userIdentifier) != -1) {
				scores[i] = b[i].getAnalysis(userIdentifier);
			}
		}
		return scores;
	}

	final protected void analyseAll() {
		for (int i = 0; i < b.length; i++) {
			if (b[i] == null)
				continue;
			b[i].analyseAllUsers();
		}
	}

	public boolean isAvailable(LogReader log, String attributeName) {
		Iterator<ProcessInstance> it = log.getInstances().iterator();
		if (it.hasNext()) {
			ProcessInstance p = it.next();
			DataSection d = p.getDataAttributes();
			return d.containsKey(attributeName);
		} else {
			return false;
		}
	}

	public PersonalityData getData() {
		return data;
	}

	public String calculateDigest(String str) {
		StringBuffer s = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();
			for (byte d : digest) {
				s.append(Integer.toHexString((int) (d & 0xff)));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s.toString();
	}

	public HashMap<String, Double[]> getResults() {
		// voer alle analyses uit.
		this.analyseAll();
		// initialiseer result
		HashMap<String, Double[]> result = new HashMap<String, Double[]>();
		// haal al gebruikers voor deze trait op
		Iterator<String> users = this.getUsers().iterator();
		while (users.hasNext()) {
			String u = users.next();
			Double[] scores = new Double[b.length];
			for (int i = 0; i < b.length; i++) {
				if (b[i] != null) {
					scores[i] = b[i].getAnalysis(u);
				}
			}
			result.put(u, scores);
		}
		return result;
	}

	public void addUser(String uid) {
		users.add(uid);
	}

	public void addUsers(Collection<String> _users) {
		users.addAll(_users);
	}

	public TreeSet<String> getUsers() {
		if (users.size() == 0)
			analyseAll();
		return users;
	}
}

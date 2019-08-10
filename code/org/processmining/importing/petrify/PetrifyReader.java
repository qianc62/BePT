package org.processmining.importing.petrify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.transitionsystem.PetrifyConstants;

/**
 * @author Vladimir Rubin
 * @version 1.0
 */
public class PetrifyReader {

	public static PetriNet read(InputStream input) throws IOException {
		PetriNet pn = new PetriNet();
		ArrayList<Transition> transitions = new ArrayList<Transition>();
		ArrayList<Place> places = new ArrayList<Place>();

		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		ArrayList<String> events;
		ArrayList<String> stParts;
		String sRead, s;

		do { // go through comments
			sRead = in.readLine();
		} while (sRead.charAt(0) == '#');
		sRead = in.readLine();
		// get the set of events and create PN transitions
		s = sRead.substring(sRead.indexOf("  ") + 2);
		events = new ArrayList<String>(Arrays.asList(s.split(" ")));
		for (String o : events) {
			// Transition t = new Transition(o, pn);
			o = repairTransitionString(o);
			Transition t = new Transition(new LogEvent(o, ""), pn);
			t.setIdentifier(o);
			pn.addTransition(t);
			transitions.add(t);
		}
		in.readLine();
		// reading the main lines
		sRead = in.readLine();
		sRead = sRead.replace("/", PetrifyConstants.EDGEDOCSSEPARATOR); // dot
		// doesn't
		// eat
		// '/'
		// symbols
		while (sRead.charAt(0) != '.') {
			// look for new transitions (with "__") and new places and add them
			// to the lists
			stParts = checkForNewTransitionsAndPlaces(sRead, transitions,
					places, pn);
			if (isPlace(stParts.get(0))) {
				Place placeFirst = findPlace(stParts.get(0), places);
				for (int i = 1; i < stParts.size(); i++) {
					String stRepaired = repairTransitionString(stParts.get(i));
					Transition aTransition = findTransition(stRepaired,
							transitions);
					PNEdge newEdge = new PNEdge(placeFirst, aTransition);
					pn.addEdge(newEdge);
				}
			} else {
				String stRepaired = repairTransitionString(stParts.get(0));
				Transition transitionFirst = findTransition(stRepaired,
						transitions);
				for (int i = 1; i < stParts.size(); i++) {
					Place aPlace = findPlace(stParts.get(i), places);
					PNEdge newEdge = new PNEdge(transitionFirst, aPlace);
					pn.addEdge(newEdge);
				}
			}
			sRead = in.readLine();
			sRead = sRead.replace("/", PetrifyConstants.EDGEDOCSSEPARATOR);
		}
		// makeEndsBlack(transitions); // end transitions must be black
		addFinalPlace(places, transitions, pn);
		return pn;
	}

	static void addFinalPlace(ArrayList<Place> places,
			ArrayList<Transition> transitions, PetriNet pn) {
		Place p = null;
		boolean endTransitionsExist = false;

		for (int i = 0; i < transitions.size(); i++) {
			Transition t = transitions.get(i);
			String id = t.toString();
			if (id.length() > 2 && id.substring(0, 3).equals("END")) {
				if (!endTransitionsExist) {
					p = new Place("END", pn);
					pn.addPlace(p);
					places.add(p);
				}
				PNEdge finalEdge = new PNEdge(t, p);
				pn.addEdge(finalEdge);
				endTransitionsExist = true;
			}
		}
	}

	static ArrayList<String> checkForNewTransitionsAndPlaces(String sRead,
			ArrayList<Transition> transitions, ArrayList<Place> places,
			PetriNet pn) {
		ArrayList<String> stParts = new ArrayList<String>(Arrays.asList(sRead
				.split(" ")));
		int k;

		for (int i = 0; i < stParts.size(); i++) {
			String st = stParts.get(i);
			if (isPlace(st)) {
				if (findPlace(st, places) == null) {
					Place p = new Place(st, pn);
					pn.addPlace(p);
					places.add(p);
				}
			} else {
				st = repairTransitionString(st);
				k = st.indexOf(PetrifyConstants.EDGEDOCSSEPARATOR);
				if (k != -1) {
					if (findTransition(st, transitions) == null) {
						Transition t = new Transition(new LogEvent(st, ""), pn);
						pn.addTransition(t);
						t.setIdentifier(st);
						transitions.add(t);
					}
				}
			}
		}
		return stParts;
	}

	static void makeEndsBlack(ArrayList<Transition> transitions) {
		for (int i = 0; i < transitions.size(); i++) {
			Transition t = transitions.get(i);
			String id = t.toString();
			if (id.length() > 2 && id.substring(0, 3).equals("END")) {
				t.setLogEvent(null);
			}
		}
	}

	public static boolean isPlace(String st) {
		if (st.charAt(0) == 'p'
				&& Character.getType(st.charAt(1)) == Character.DECIMAL_DIGIT_NUMBER
				&& Character.getType(st.charAt(st.length() - 1)) == Character.DECIMAL_DIGIT_NUMBER) {
			return true;
		} else {
			return false;
		}
	}

	public static Place findPlace(String st, ArrayList<Place> places) {
		for (Place p : places) {
			if (st.equals(p.toString())) {
				return p;
			}
		}
		return null;
	}

	public static Transition findTransition(String identifier,
			ArrayList<Transition> transitions) {
		for (Transition t : transitions) {
			if (t.getIdentifier().equals(identifier)) {
				return t;
			}
		}
		return null;
	}

	public static String repairTransitionString(String st) {
		Set keys = PetrifyConstants.BadSymbolsMapBack.keySet();
		Iterator it = keys.iterator();

		while (it.hasNext()) {
			String aKey = (String) it.next();
			st = st.replace(aKey, PetrifyConstants.BadSymbolsMapBack.get(aKey));
		}
		String result = repairTransitionStringForMapping(st);

		return result;
	}

	public static String repairTransitionStringForMapping(String st) {
		String result = "";
		int startIndex = 0, newStartIndex = 0;
		int newk = 0;
		int k = st.indexOf(PetrifyConstants.EVENTTYPESEPARATOR, startIndex);
		boolean repaired = false;
		boolean labeled = false;

		while (k != -1) {
			repaired = true;
			result = result.concat(st.substring(startIndex, k)).concat(" (");
			newStartIndex = k + PetrifyConstants.EVENTTYPESEPARATOR.length();

			newk = st
					.indexOf(PetrifyConstants.EDGEDOCSSEPARATOR, newStartIndex);
			if (newk != -1) { // there is a docs separator in the string
				result = result.concat(st.substring(newStartIndex, newk))
						.concat(")");
				startIndex = newk
				/*- 1 + PetrifyConstants.EDGEDOCSSEPARATOR.length() */
				;
				k = st.indexOf(PetrifyConstants.EVENTTYPESEPARATOR, startIndex);
				if (st.substring(
						newk + PetrifyConstants.EDGEDOCSSEPARATOR.length(),
						st.length()).matches("\\d+")) {
					result = result.concat(st.substring(newk, st.length()));
				}
			} else { // we are at the end of the string
				result = result
						.concat(st.substring(newStartIndex, st.length()))
						.concat(")");
				break;
			}
		}
		if (!repaired) { // we haven't worked with the string
			result = new String(st);
		}
		return result;
	}
}

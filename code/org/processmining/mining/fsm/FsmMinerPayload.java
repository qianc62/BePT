package org.processmining.mining.fsm;

import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Map;

import org.processmining.framework.models.fsm.FSMPayload;

/**
 * <p>
 * Title: FsmMinerPayload
 * </p>
 * 
 * <p>
 * Description: Payload for the states mined by the FsmMiner
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FsmMinerPayload extends FSMPayload {

	/**
	 * Possible abstractions. LAST is not an abstraction but it signals the end
	 * of the list of abstractions. Its value should always be equal to the
	 * number of real abstractions.
	 */
	public static final int MODELELEMENT = 0;
	public static final int ORIGINATOR = 1;
	public static final int EVENTTYPE = 2;
	public static final int LAST = 3;

	/**
	 * The settings used to obtain this payload. To store the settings for every
	 * payload seems like overkill, but because we have the settings we can
	 * nicely forget about abstractions that were not selected. Thus, by adding
	 * the 1 handle (settings) we can remove 4 (the abstractions that were not
	 * selected).
	 */
	private FsmSettings settings;

	/**
	 * To store every possible abstraction. As the classes of the different
	 * abstractiosn differ, we use the very generic Object class here. Ny using
	 * the settings, we can tell which class the abstraction should be and cast
	 * it to this class.
	 */
	private Object[] bwdPayload = new Object[LAST];
	private Object[] fwdPayload = new Object[LAST];

	// To store attributes and values.
	private TreeMap<String, String> attributePayload;

	/**
	 * Create an initial payload given the settings.
	 * 
	 * @param settings
	 *            FsmSettings
	 */
	public FsmMinerPayload(FsmSettings settings) {
		this.settings = settings;

		for (int mode = 0; mode < LAST; mode++) {
			FsmHorizonSettings horizonSettings = settings.getHorizonSettings(
					true, mode);
			if (horizonSettings.getUse()) {
				switch (horizonSettings.getAbstraction()) {
				case (FsmHorizonSettings.SEQ): {
					bwdPayload[mode] = new TreeMap<Integer, String>();
					break;
				}
				case (FsmHorizonSettings.SET): {
					bwdPayload[mode] = new TreeSet<String>();
					break;
				}
				case (FsmHorizonSettings.BAG): {
					bwdPayload[mode] = new TreeMap<String, Integer>();
					break;
				}
				}
			}
			horizonSettings = settings.getHorizonSettings(false, mode);
			if (horizonSettings.getUse()) {
				switch (horizonSettings.getAbstraction()) {
				case (FsmHorizonSettings.SEQ): {
					fwdPayload[mode] = new TreeMap<Integer, String>();
					break;
				}
				case (FsmHorizonSettings.SET): {
					fwdPayload[mode] = new TreeSet<String>();
					break;
				}
				case (FsmHorizonSettings.BAG): {
					fwdPayload[mode] = new TreeMap<String, Integer>();
					break;
				}
				}
			}
		}

		attributePayload = new TreeMap<String, String>();
	}

	/**
	 * Merges another payload with this.
	 * 
	 * @param payload
	 *            FsmMinerPayload
	 * @return FsmMinerPayload
	 */
	public FsmMinerPayload merge(FSMPayload payload) {
		FsmMinerPayload mergedPayload = new FsmMinerPayload(settings);
		if (payload instanceof FsmMinerPayload) {
			FsmMinerPayload minerPayload = (FsmMinerPayload) payload;
			for (int mode = 0; mode < LAST; mode++) {
				FsmHorizonSettings horizonSettings = settings
						.getHorizonSettings(true, mode);
				if (horizonSettings.getUse()) {
					switch (horizonSettings.getAbstraction()) {
					case (FsmHorizonSettings.SEQ): {
						int k = 0;
						for (int i : getBwdSeq(mode).keySet()) {
							mergedPayload.getBwdSeq(mode).put(k++,
									getBwdSeq(mode).get(i));
						}
						mergedPayload.getBwdSeq(mode).put(k++, "|");
						for (int i : minerPayload.getBwdSeq(mode).keySet()) {
							mergedPayload.getBwdSeq(mode).put(k++,
									minerPayload.getBwdSeq(mode).get(i));
						}
						break;
					}
					case (FsmHorizonSettings.SET): {
						mergedPayload.getBwdSet(mode).addAll(getBwdSet(mode));
						mergedPayload.getBwdSet(mode).addAll(
								minerPayload.getBwdSet(mode));
						break;
					}
					case (FsmHorizonSettings.BAG): {
						for (String key : getBwdBag(mode).keySet()) {
							mergedPayload.getBwdBag(mode).put(key,
									getBwdBag(mode).get(key));
						}
						for (String key : minerPayload.getBwdBag(mode).keySet()) {
							int k = 0;
							if (mergedPayload.getBwdBag(mode).containsKey(key)) {
								k = mergedPayload.getBwdBag(mode).get(key);
							}
							mergedPayload.getBwdBag(mode).put(key,
									k + minerPayload.getBwdBag(mode).get(key));
						}
						break;
					}
					}
				}
			}
			for (int mode = 0; mode < LAST; mode++) {
				FsmHorizonSettings horizonSettings = settings
						.getHorizonSettings(false, mode);
				if (horizonSettings.getUse()) {
					switch (horizonSettings.getAbstraction()) {
					case (FsmHorizonSettings.SEQ): {
						int k = 0;
						for (int i : getFwdSeq(mode).keySet()) {
							mergedPayload.getFwdSeq(mode).put(k++,
									getFwdSeq(mode).get(i));
						}
						mergedPayload.getFwdSeq(mode).put(k++, "|");
						for (int i : minerPayload.getFwdSeq(mode).keySet()) {
							mergedPayload.getFwdSeq(mode).put(k++,
									minerPayload.getFwdSeq(mode).get(i));
						}
						break;
					}
					case (FsmHorizonSettings.SET): {
						mergedPayload.getFwdSet(mode).addAll(getFwdSet(mode));
						mergedPayload.getFwdSet(mode).addAll(
								minerPayload.getFwdSet(mode));
						break;
					}
					case (FsmHorizonSettings.BAG): {
						for (String key : getFwdBag(mode).keySet()) {
							mergedPayload.getFwdBag(mode).put(key,
									getFwdBag(mode).get(key));
						}
						for (String key : minerPayload.getFwdBag(mode).keySet()) {
							int k = 0;
							if (mergedPayload.getFwdBag(mode).containsKey(key)) {
								k = mergedPayload.getFwdBag(mode).get(key);
							}
							mergedPayload.getFwdBag(mode).put(key,
									k + minerPayload.getFwdBag(mode).get(key));
						}
						break;
					}
					}
				}
			}
			for (String attribute : attributePayload.keySet()) {
				String value = "<undefined>";
				if (minerPayload.getAttributePayload().containsKey(attribute)) {
					value = minerPayload.getAttributePayload().get(attribute);
				}
				if (attributePayload.get(attribute).equals(value)) {
					mergedPayload.getAttributePayload().put(attribute, value);
				} else {
					mergedPayload.getAttributePayload().put(attribute,
							attributePayload.get(attribute) + "|" + value);
				}
			}
		}
		return mergedPayload;
	}

	/**
	 * Gets the attribute payload.
	 * 
	 * @return Map
	 */
	public Map<String, String> getAttributePayload() {
		return attributePayload;
	}

	/**
	 * Gets the setting sused to obtain this payload.
	 * 
	 * @return FsmSettings
	 */
	public FsmSettings getSettings() {
		return settings;
	}

	/**
	 * Gets a label for the given mode.
	 * 
	 * @param mode
	 *            int
	 * @return String
	 */
	public static String getLabel(int mode) {
		String s = "";
		switch (mode) {
		case (MODELELEMENT): {
			s = "Model element";
			break;
		}
		case (ORIGINATOR): {
			s = "Originator";
			break;
		}
		case (EVENTTYPE): {
			s = "Event type";
			break;
		}
		}
		return s;
	}

	/**
	 * Gets a backward payload, given which one is needed, assuming that the
	 * this payload is a sequence.
	 * 
	 * @param mode
	 *            int
	 * @return TreeMap
	 */
	public TreeMap<Integer, String> getBwdSeq(int mode) {
		return (TreeMap<Integer, String>) bwdPayload[mode];
	}

	/**
	 * Gets a backward payload, given which one is needed, assuming that the
	 * this payload is a set.
	 * 
	 * @param mode
	 *            int
	 * @return TreeSet
	 */
	public TreeSet<String> getBwdSet(int mode) {
		return (TreeSet<String>) bwdPayload[mode];
	}

	/**
	 * Gets a backward payload, given which one is needed, assuming that the
	 * this payload is a bag.
	 * 
	 * @param mode
	 *            int
	 * @return TreeMap
	 */
	public TreeMap<String, Integer> getBwdBag(int mode) {
		return (TreeMap<String, Integer>) bwdPayload[mode];
	}

	/**
	 * Gets a backward payload in string representation.
	 * 
	 * @param mode
	 *            int
	 * @return String
	 */
	public String getBwdPayload(int mode) {
		String s = "";
		FsmHorizonSettings horizonSettings = settings.getHorizonSettings(true,
				mode);
		switch (horizonSettings.getAbstraction()) {
		case (FsmHorizonSettings.SEQ): {
			s = getBwdSeq(mode).toString();
			break;
		}
		case (FsmHorizonSettings.BAG): {
			s = getBwdBag(mode).toString();
			break;
		}
		case (FsmHorizonSettings.SET): {
			s = getBwdSet(mode).toString();
			break;
		}
		}
		return s;
	}

	/**
	 * And the same, but now in the forward direction.
	 */

	public TreeMap<Integer, String> getFwdSeq(int mode) {
		return (TreeMap<Integer, String>) fwdPayload[mode];
	}

	public TreeSet<String> getFwdSet(int mode) {
		return (TreeSet<String>) fwdPayload[mode];
	}

	public TreeMap<String, Integer> getFwdBag(int mode) {
		return (TreeMap<String, Integer>) fwdPayload[mode];
	}

	public String getFwdPayload(int mode) {
		String s = "";
		FsmHorizonSettings horizonSettings = settings.getHorizonSettings(false,
				mode);
		switch (horizonSettings.getAbstraction()) {
		case (FsmHorizonSettings.SEQ): {
			s = getFwdSeq(mode).toString();
			break;
		}
		case (FsmHorizonSettings.BAG): {
			s = getFwdBag(mode).toString();
			break;
		}
		case (FsmHorizonSettings.SET): {
			s = getFwdSet(mode).toString();
			break;
		}
		}
		return s;
	}

	/**
	 * The following methods add a string to a sequence, a set, or a bag, for
	 * either the backward or the forward direction.
	 */

	public void addBwdSeq(int mode, String s) {
		TreeMap<Integer, String> map = getBwdSeq(mode);
		if (map != null) {
			map.put(map.keySet().size(), s);
		}
		bwdPayload[mode] = map;
	}

	public void addBwdSet(int mode, String s) {
		TreeSet<String> set = getBwdSet(mode);
		if (set != null) {
			set.add(s);
		}
		bwdPayload[mode] = set;
	}

	public void addBwdBag(int mode, String s) {
		TreeMap<String, Integer> map = getBwdBag(mode);
		if (map != null) {
			Integer n = 1;
			if (map.containsKey(s)) {
				n = map.get(s) + 1;
			}
			map.put(s, n);
		}
		bwdPayload[mode] = map;
	}

	public void addFwdSeq(int mode, String s) {
		TreeMap<Integer, String> map = getFwdSeq(mode);
		if (map != null) {
			map.put(map.keySet().size(), s);
		}
		fwdPayload[mode] = map;
	}

	public void addFwdSet(int mode, String s) {
		TreeSet<String> set = getFwdSet(mode);
		if (set != null) {
			set.add(s);
		}
		fwdPayload[mode] = set;
	}

	public void addFwdBag(int mode, String s) {
		TreeMap<String, Integer> map = getFwdBag(mode);
		if (map != null) {
			Integer n = 1;
			if (map.containsKey(s)) {
				n = map.get(s) + 1;
			}
			map.put(s, n);
		}
		fwdPayload[mode] = map;
	}

	/**
	 * Gets a string represenation for this payload.
	 * 
	 * @return String
	 */
	public String toString() {
		String s = "";
		String d = "[";

		for (int mode = 0; mode < LAST; mode++) {
			if (settings.getHorizonSettings(true, mode).getUse()) {
				s += d + getBwdPayload(mode);
				d = ",";
			}
			if (settings.getHorizonSettings(false, mode).getUse()) {
				s += d + getFwdPayload(mode);
				d = ",";
			}
		}
		if (settings.getUseAttributes()) {
			s += d + attributePayload.toString();
			d = ",";
		}
		if (d == "[") {
			s = "[]";
		} else {
			s += "]";
		}
		return s;
	}

	/**
	 * Compares this payload to another payload.
	 * 
	 * @param object
	 *            Object the other payload.
	 * @return int
	 */
	public int compareTo(Object object) {
		FsmMinerPayload payload = (FsmMinerPayload) object;
		// For sake of simplicity, we revert to string comparison.
		return toString().compareTo(payload.toString());
	}
}

package org.processmining.analysis.socialsuccess.bigfive.neuroticism;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class OverwhelmedByEmotions extends Behaviour {
	/*
	 * 
	 * 'eeee', 'oooo', '!!!!', '.....', '????', ':)', ':-)', ':d', ':-d' ECHT
	 */

	public OverwhelmedByEmotions(Trait tr) {
		super(tr);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date beginTime, Date endTime) {
		// process 'NODES+TAGS', 'ACTION-USER_ORIG' of 'ACTION-REFER_ORIG' is
		// nodig
		LogReader log = null;
		PersonalityData dt = trait.getData();
		if (dt.isProcessAvailable(PersonalityData.NODE_ACTION_REFER_ORIG)) {
			log = dt.getProcess(PersonalityData.NODE_ACTION_REFER_ORIG);
		} else {
			log = dt.getProcess(PersonalityData.NODE_ACTION_USER_ORIG);
		}

		if (log != null) {
			HashMap<String, Double> nodesPerCreator = new HashMap<String, Double>();
			HashMap<String, Double> overwhelmedPerCreator = new HashMap<String, Double>();

			Iterator<ProcessInstance> instance = log.instanceIterator();
			SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				DataSection d = p.getDataAttributes();
				try {
					if (d.containsKey("GenericType")
							&& (d.get("GenericType").equals("node") || d.get(
									"GenericType").equals("reply"))
							&& d.containsKey("TimestampCreate")
							&& beginTime.before(fd.parse(d
									.get("TimestampCreate")))
							&& d.containsKey("Text")
							&& d.containsKey("CreatedBy")) {
						double score = 0.;
						HashSet<String> matches = new HashSet<String>();
						String text = d.get("Text");
						String textLC = text.toLowerCase();
						String author = d.get("CreatedBy");
						// klinkers matchen
						Pattern klinkers = Pattern
								.compile(".*(e{4,5}|u{4,5}|i{4,5}|o{4,5}|a{4,5}|y{4,5}).*");
						Pattern klinkers2 = Pattern
								.compile(".*(e{6,}|u{6,}|i{6,}|o{6,}|a{6,}|y{6,}).*");
						if (klinkers2.matcher(textLC).matches()) {
							matches.add("Klinkers6+");
							score += 3.;
						} else if (klinkers.matcher(textLC).matches()) {
							matches.add("Klinkers4/5");
							score += 2.;
						}

						// leestekens herhalen
						Pattern leestekens = Pattern
								.compile(".*(!{4,5}|\\.{4,5}|\\?{4,5}).*");
						Pattern leestekens2 = Pattern
								.compile(".*(!{6,}|\\.{6,}|\\?{6,}).*");
						if (leestekens2.matcher(textLC).matches()) {
							matches.add("Leestekens6+");
							score += 3.;
						} else if (leestekens.matcher(textLC).matches()) {
							matches.add("Leestekens4/5");
							score += 2.;
						}

						// Smilies
						Pattern smilies = Pattern
								.compile(".*(:-?\\)|:-?d|:-?\\().*");
						if (smilies.matcher(textLC).matches()) {
							matches.add("Smiley");
							score += 1.;
						}

						// BrEeZaH
						Pattern breezah = Pattern
								.compile(".*(\\p{Lu}[\\p{L}&&[^\\p{Lu}]]){2,}[\\p{Lu}]?(\\p{Space}|\\z).*");
						if (breezah.matcher(text).matches()) {
							matches.add("BrEeZaH");
							score += 5.;
						}

						// capitals = ECHT, NU, UNLIMITED DELICIOUS, FREE
						Pattern capitals = Pattern
								.compile(".*(ECHT|NU|NOW|UNLIMITED|DELICIOUS|FREE).*");
						if (capitals.matcher(text).matches()) {
							matches.add("CAPITALS");
							score += 3.;
						}
						if (score > 0) {
							if (nodesPerCreator.containsKey(author)) {
								nodesPerCreator.put(author, nodesPerCreator
										.get(author) + 1);
								overwhelmedPerCreator.put(author,
										overwhelmedPerCreator.get(author)
												+ score);
							} else {
								nodesPerCreator.put(author, 1.);
								overwhelmedPerCreator.put(author, score);
							}
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			HashMap<String, Double> scorePerUser = new HashMap<String, Double>();
			Iterator<String> it = overwhelmedPerCreator.keySet().iterator();
			while (it.hasNext()) {
				String user = it.next();
				scorePerUser.put(user, overwhelmedPerCreator.get(user)
						/ nodesPerCreator.get(user));
			}

			calculateResults(scorePerUser);
		}
	}
}

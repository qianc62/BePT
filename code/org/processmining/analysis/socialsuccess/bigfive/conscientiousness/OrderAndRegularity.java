package org.processmining.analysis.socialsuccess.bigfive.conscientiousness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.bigfive.Behaviour;
import org.processmining.analysis.socialsuccess.bigfive.Trait;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;

public class OrderAndRegularity extends Behaviour {
	private static final double DAY_RATIO = 3;
	private static final double WEEK_RATIO = 2;
	private static final double MONTH_RATIO = 1;

	// http://mtin.nl/deloitte/prom_demo_tagh.php en
	// MineRoleHierarchy zie package org.processmining.analysis.rolehierarchy;

	public OrderAndRegularity(Trait tr) {
		super(tr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void analyseAllUsers(Date startTime, Date endTime) {
		Message.add("SSA: Order And Regularity");
		HashMap<String, TreeMap<Integer, Integer>> nodesPerDay = new HashMap<String, TreeMap<Integer, Integer>>();
		int minDay = Integer.MAX_VALUE, maxDay = Integer.MIN_VALUE;
		HashMap<String, TreeMap<Integer, Integer>> nodesPerWeek = new HashMap<String, TreeMap<Integer, Integer>>();
		int minWeek = Integer.MAX_VALUE, maxWeek = Integer.MIN_VALUE;
		HashMap<String, TreeMap<Integer, Integer>> nodesPerMonth = new HashMap<String, TreeMap<Integer, Integer>>();
		int minMonth = Integer.MAX_VALUE, maxMonth = Integer.MIN_VALUE;
		HashMap<String, Integer> nodesPerUser = new HashMap<String, Integer>();
		// process 'NODES+TAGS' of 'NODE_ACTION_REFER_ORIG' /
		// NODE_ACTION_USER_ORIG is nodig.
		LogReader log = null;
		if (trait.getData().isProcessAvailable(
				PersonalityData.NODE_ACTION_REFER_ORIG))
			log = trait.getData().getProcess(
					PersonalityData.NODE_ACTION_REFER_ORIG);
		else if (trait.getData().isProcessAvailable(
				PersonalityData.NODE_ACTION_USER_ORIG))
			log = trait.getData().getProcess(
					PersonalityData.NODE_ACTION_USER_ORIG);
		else if (trait.getData().isProcessAvailable(PersonalityData.NODES_TAGS))
			log = trait.getData().getProcess(PersonalityData.NODES_TAGS);

		if (log != null) {
			// regelmatigheid van posten van berichten

			Iterator<ProcessInstance> instance = log.instanceIterator();
			while (instance.hasNext()) {
				ProcessInstance p = instance.next();
				if (p.getAttributes().containsKey("CreatedBy")) {
					// sla het aantal nodes per creator op
					String creator = p.getAttributes().get("CreatedBy");
					SimpleDateFormat fd = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					try {
						Date creationDate = fd.parse(p.getAttributes().get(
								"TimestampCreate"));
						GregorianCalendar d = new GregorianCalendar();
						d.setTime(creationDate);
						if (startTime.before(creationDate)
								&& endTime.after(creationDate)) {
							if (nodesPerUser.containsKey(creator))
								nodesPerUser.put(creator, nodesPerUser
										.get(creator) + 1);
							else
								nodesPerUser.put(creator, 1);
							addToBasket(nodesPerDay, creator, d
									.get(Calendar.DAY_OF_YEAR)
									+ 366 * d.get(Calendar.YEAR));
							minDay = Math.min(minDay, d
									.get(Calendar.DAY_OF_YEAR)
									+ 366 * d.get(Calendar.YEAR));
							maxDay = Math.max(maxDay, d
									.get(Calendar.DAY_OF_YEAR)
									+ 366 * d.get(Calendar.YEAR));
							addToBasket(nodesPerWeek, creator, d
									.get(Calendar.WEEK_OF_YEAR)
									+ 54 * d.get(Calendar.YEAR));
							minWeek = Math.min(minWeek, d
									.get(Calendar.WEEK_OF_YEAR)
									+ 54 * d.get(Calendar.YEAR));
							maxWeek = Math.max(maxWeek, d
									.get(Calendar.WEEK_OF_YEAR)
									+ 54 * d.get(Calendar.YEAR));
							addToBasket(nodesPerMonth, creator, d
									.get(Calendar.MONTH)
									+ 12 * d.get(Calendar.YEAR));
							minMonth = Math.min(minMonth, d.get(Calendar.MONTH)
									+ 12 * d.get(Calendar.YEAR));
							maxMonth = Math.max(maxMonth, d.get(Calendar.MONTH)
									+ 12 * d.get(Calendar.YEAR));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			HashMap<String, Double> derivation = new HashMap<String, Double>();
			int numberOfDays = maxDay - minDay + 1;
			int numberOfWeeks = maxWeek - minWeek + 1;
			int numberOfMonths = maxMonth - minMonth + 1;

			Iterator<String> it = nodesPerUser.keySet().iterator();
			while (it.hasNext()) {
				String user = it.next();

				if (!nodesPerDay.containsKey(user)
						|| nodesPerDay.get(user).size() < 3) {
					// gebruiker heeft op minder dan 2 dagen iets gepost
					// dan is deze niet intessant voor de statistieken
					continue;
				}

				double valueD = 0, valueW = 0, valueM = 0;
				double avgValueD = (double) nodesPerUser.get(user)
						/ numberOfDays;
				for (int i = minDay; i < maxDay; i++) {
					// wat is het percentage afwijking
					if (nodesPerDay.containsKey(user)
							&& nodesPerDay.get(user).containsKey(i)) {
						valueD += Math.abs(avgValueD
								- nodesPerDay.get(user).get(i))
								/ avgValueD;
					} else { // hij wijkt 100% af
						valueD += 1;
					}
				}
				if (maxDay - minDay != 0) {
					valueD = valueD / (double) (maxDay - minDay + 1);
				}

				double avgValueW = (double) nodesPerUser.get(user)
						/ numberOfWeeks;
				for (int i = minWeek + 1; i < maxWeek; i++) {
					if (nodesPerWeek.containsKey(user)
							&& nodesPerWeek.get(user).containsKey(i)) {
						valueW += Math.abs(avgValueW
								- nodesPerWeek.get(user).get(i))
								/ avgValueW;
					} else {
						valueW += 1;
					}
				}
				if (maxWeek - minWeek != 1) {
					valueW = valueW / (double) (maxWeek - minWeek - 1);
				}

				double avgValueM = (double) nodesPerUser.get(user)
						/ numberOfMonths;
				for (int i = minMonth + 1; i < maxMonth; i++) {
					if (nodesPerMonth.containsKey(user)
							&& nodesPerMonth.get(user).containsKey(i)) {
						valueM += Math.abs(avgValueM
								- nodesPerMonth.get(user).get(i))
								/ avgValueM;
					} else {
						valueM += 1;
					}
				}
				if (maxMonth - minMonth != 1) {
					valueM = valueM / (double) (maxMonth - minMonth - 1);
				}

				double value = (valueD * DAY_RATIO + valueW * WEEK_RATIO + valueM
						* MONTH_RATIO)
						/ (DAY_RATIO + WEEK_RATIO + MONTH_RATIO);
				derivation.put(user, value);
			}

			// we hebben de afwijking van het gemiddelde berekend, dus de
			// score moet hoog zijn als de afwijking laag is.
			calculateResultsInv(derivation);

		} else {
			// TODO I might throw an exception here
			return;
		}

	}

	private void addToBasket(
			HashMap<String, TreeMap<Integer, Integer>> collection, String user,
			Integer basket) {

		if (collection.containsKey(user)) {
			if (collection.get(user).containsKey(basket)) {
				collection.get(user).put(basket,
						collection.get(user).get(basket) + 1);
			} else {
				collection.get(user).put(basket, 1);
			}
		} else {
			TreeMap<Integer, Integer> h = new TreeMap<Integer, Integer>();
			h.put(basket, 1);
			collection.put(user, h);
		}
	}
}

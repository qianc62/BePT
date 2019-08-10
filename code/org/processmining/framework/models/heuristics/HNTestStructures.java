/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.heuristics;

import java.util.*;

import org.processmining.framework.util.*;

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
 * @author not attributable
 * @version 1.0
 */
public class HNTestStructures {

	public HNTestStructures() {

	}

	public static Integer[] generateSubset(Random r) {
		Integer[] subset = new Integer[100];

		for (int i = 0; i < subset.length; i++) {
			subset[i] = new Integer(i); // r.nextInt(100));
		}
		return subset;
	}

	// -----------------------------------------------------------------

	public static long timeTreeSetIteration(int N) {
		Random r = new Random(1234);
		TreeSet tree = new TreeSet(Arrays.asList(generateSubset(r)));

		System.out.println("TreeSet length: " + tree.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			Iterator iter = tree.iterator();
			Integer I;
			int i;
			while (iter.hasNext()) {
				I = (Integer) iter.next();
				i = I.intValue();

				i++;
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSubSetSetIteration(int N) {
		Random r = new Random(1234);
		Integer[] subset = generateSubset(r);
		HNSubSet set = new HNSubSet();

		System.out.println("HNSubSetSet length: " + subset.length);

		for (int i = 0; i < subset.length; i++) {
			set.add(subset[i].intValue());
		}

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			int i;
			for (int iter = 0; iter < set.size(); iter++) {
				i = set.get(iter);
				i++;
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeTreeSetAdd(int N) {
		Random r = new Random(1234);

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			int size = r.nextInt(100);
			TreeSet set = new TreeSet();

			for (int i = 0; i < size; i++) {
				set.add(new Integer(r.nextInt(100)));
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSubSetSetAdd(int N) {
		Random r = new Random(1234);

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			int size = r.nextInt(100);
			HNSubSet set = new HNSubSet();

			for (int i = 0; i < size; i++) {
				set.add(r.nextInt(100));
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeTreeSetCopy(int N) {
		Random r = new Random(1234);
		TreeSet tree = new TreeSet(Arrays.asList(generateSubset(r)));

		System.out.println("TreeSet length: " + tree.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			TreeSet set = MethodsToMakeDeepCopiesOfDataStructures
					.cloneTreeSet(tree);
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSubSetSetCopy(int N) {
		Random r = new Random(1234);
		Integer[] subset = generateSubset(r);
		HNSubSet set = new HNSubSet();

		System.out.println("HNSubSetSet length: " + subset.length);

		for (int i = 0; i < subset.length; i++) {
			set.add(subset[i].intValue());
		}

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HNSubSet setCopy = set.deepCopy();
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeTreeSetContains(int N) {
		Random r = new Random(1234);
		TreeSet tree = new TreeSet(Arrays.asList(generateSubset(r)));
		Integer[] i = new Integer[100];

		for (int k = 0; k < 100; k++) {
			i[k] = new Integer(r.nextInt(200));
		}

		System.out.println("TreeSet length: " + tree.size());

		long time = System.currentTimeMillis();

		int c = 0;
		for (int n = 0; n < N; n++) {
			boolean b = tree.contains(i[c % 100]);
			b = !b;

			c++;
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSubSetSetContains(int N) {
		Random r = new Random(1234);
		Integer[] subset = generateSubset(r);
		HNSubSet set = new HNSubSet();

		System.out.println("HNSubSetSet length: " + subset.length);

		for (int i = 0; i < subset.length; i++) {
			set.add(subset[i].intValue());
		}

		int[] i = new int[100];
		for (int k = 0; k < 100; k++) {
			i[k] = r.nextInt(200);
		}

		long time = System.currentTimeMillis();

		int c = 0;
		for (int n = 0; n < N; n++) {
			boolean b = set.contains(i[c % 100]);
			b = !b;

			c++;
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeTreeSetRemove(int N) {
		Random r = new Random(1234);
		TreeSet tree = new TreeSet(Arrays.asList(generateSubset(r)));
		Integer[] i = new Integer[100];

		for (int k = 0; k < 100; k++) {
			i[k] = new Integer(r.nextInt(200));
		}

		System.out.println("TreeSet length: " + tree.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			TreeSet copy = MethodsToMakeDeepCopiesOfDataStructures
					.cloneTreeSet(tree);

			for (int k = 0; k < 100; k++) {
				copy.remove(i[k]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSubSetSetRemove(int N) {
		Random r = new Random(1234);
		Integer[] subset = generateSubset(r);
		HNSubSet set = new HNSubSet();

		System.out.println("HNSubSetSet length: " + subset.length);

		for (int i = 0; i < subset.length; i++) {
			set.add(subset[i].intValue());
		}

		int[] i = new int[100];
		for (int k = 0; k < 100; k++) {
			i[k] = r.nextInt(200);
		}

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HNSubSet copy = set.deepCopy();

			for (int k = 0; k < 100; k++) {
				set.remove(i[k]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeHashSetAdd(int N) {
		Random r = new Random(1234);

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			int size = r.nextInt(100);
			HashSet set = new HashSet();

			for (int i = 0; i < size; i++) {
				HNSubSet subset = new HNSubSet();
				int subsetsize = r.nextInt(100);

				for (int k = 0; k < subsetsize; k++) {
					subset.add(r.nextInt(100));
				}
				set.add(subset);
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSetSetAdd(int N) {
		Random r = new Random(1234);

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			int size = r.nextInt(100);
			HNSet set = new HNSet();

			for (int i = 0; i < size; i++) {
				HNSubSet subset = new HNSubSet();
				int subsetsize = r.nextInt(100);

				for (int k = 0; k < subsetsize; k++) {
					subset.add(r.nextInt(100));
				}
				set.add(subset);
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static HashSet deepCopyHashSet(HashSet set) {
		HashSet newSet = new HashSet();
		Iterator iter = set.iterator();

		while (iter.hasNext()) {
			newSet.add(((HNSubSet) iter.next()).deepCopy());
		}
		return newSet;
	}

	public static long timeHashSetCopy(int N) {
		Random r = new Random(1234);
		HashSet set = new HashSet();

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
		}
		System.out.println("HashSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HashSet copy = deepCopyHashSet(set);
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSetSetCopy(int N) {
		Random r = new Random(1234);
		HNSet set = new HNSet();

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
		}
		System.out.println("HNSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HNSet copy = set.deepCopy();
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeHashSetRemove(int N) {
		Random r = new Random(1234);
		HashSet set = new HashSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HashSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HashSet copy = deepCopyHashSet(set);

			for (int k = 0; k < 100; k++) {
				copy.remove(toRemove[r.nextInt(100)]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSetRemove(int N) {
		Random r = new Random(1234);
		HNSet set = new HNSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HNSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			HNSet copy = set.deepCopy();

			for (int k = 0; k < 100; k++) {
				copy.remove(toRemove[r.nextInt(100)]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeHashSetContains(int N) {
		Random r = new Random(1234);
		HashSet set = new HashSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HashSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			for (int k = 0; k < 100; k++) {
				set.contains(toRemove[r.nextInt(100)]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSetContains(int N) {
		Random r = new Random(1234);
		HNSet set = new HNSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HNSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			for (int k = 0; k < 100; k++) {
				set.contains(toRemove[r.nextInt(100)]);
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static long timeHashSetIterate(int N) {
		Random r = new Random(1234);
		HashSet set = new HashSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HashSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			Iterator iter = set.iterator();

			while (iter.hasNext()) {
				HNSubSet subset = (HNSubSet) iter.next();
			}
		}

		return System.currentTimeMillis() - time;
	}

	public static long timeHNSetIterate(int N) {
		Random r = new Random(1234);
		HNSet set = new HNSet();
		HNSubSet[] toRemove = new HNSubSet[100];

		for (int i = 0; i < 100; i++) {
			HNSubSet subset = new HNSubSet();
			int size = r.nextInt(100);

			for (int j = 0; j < size; j++) {
				subset.add(r.nextInt(100));
			}
			set.add(subset);
			toRemove[i] = subset;
		}
		System.out.println("HNSet size: " + set.size());

		long time = System.currentTimeMillis();

		for (int n = 0; n < N; n++) {
			for (int k = 0; k < set.size(); k++) {
				HNSubSet subset = set.get(k);
			}
		}

		return System.currentTimeMillis() - time;
	}

	// -----------------------------------------------------------------

	public static void main(String[] args) {
		int iterationN = 3000000;
		int addN = 300000;
		int copyN = 300000;
		int containsN = 30000000;
		int removeN = 300000;
		int hashAddN = 3000;
		int hashCopyN = 30000;
		int hashRemoveN = 100000;
		int hashContainsN = 300000;
		int hashIterationN = 300000;

		// 3000000 -> 16000ms
		// System.out.println("timeTreeSetIteration: " +
		// timeTreeSetIteration(iterationN));
		// 3000000 -> 1900ms
		// System.out.println("timeHNSubSetSetIteration: " +
		// timeHNSubSetSetIteration(iterationN));

		// System.out.println("timeTreeSetAdd: " + timeTreeSetAdd(addN)); //
		// 300000 -> 4900ms
		// System.out.println("timeHNSubSetSetAdd: " +
		// timeHNSubSetSetAdd(addN)); // 300000 -> 3800ms

		// 300000 -> 15600ms
		// System.out.println("timeTreeSetCopy: " + timeTreeSetCopy(copyN));
		// 300000 -> 450ms
		// System.out.println("timeHNSubSetSetCopy: " +
		// (timeHNSubSetSetCopy(copyN * 10) / 10));

		// 30000000 -> 7100ms
		// System.out.println("timeTreeSetContains: " +
		// timeTreeSetContains(containsN));
		// 30000000 -> 2900ms
		// System.out.println("timeHNSubSetSetContains: " +
		// (timeHNSubSetSetContains(containsN)));

		// 300000 -> 7350ms
		// System.out.println("timeTreeSetRemove: " +
		// (timeTreeSetRemove(removeN) - timeTreeSetCopy(removeN)));
		// 300000 -> 2200ms
		// System.out.println("timeHNSubSetSetRemove: " +
		// (timeHNSubSetSetRemove(removeN) - timeHNSubSetSetCopy(removeN)));

		// 3000 -> 1800
		// System.out.println("timeHashSetAdd: " + timeHashSetAdd(hashAddN));
		// 3000 -> 1800
		// System.out.println("timeHNSetSetAdd: " + timeHNSetSetAdd(hashAddN));

		// 300000 -> 2850ms
		// System.out.println("timeHashSetCopy: " + timeHashSetCopy(hashCopyN));
		// 300000 -> 2050ms
		// System.out.println("timeHNSetSetCopy: " +
		// (timeHNSetSetCopy(hashCopyN)));

		// 100000 -> 3200ms
		// System.out.println("timeHashSetRemove: " +
		// (timeHashSetRemove(hashRemoveN) - timeHashSetCopy(hashRemoveN)));
		// 100000 -> 4400ms
		// System.out.println("timeHNSetRemove: " +
		// (timeHNSetRemove(hashRemoveN) - timeHNSetSetCopy(hashRemoveN)));

		// 300000 -> 3650ms
		// System.out.println("timeHashSetContains: " +
		// timeHashSetContains(hashContainsN));
		// 300000 -> 12500ms
		// System.out.println("timeHNSetContains: " +
		// (timeHNSetContains(hashContainsN)));

		// 300000 -> 1650ms
		// System.out.println("timeHashSetIteration: " +
		// timeHashSetIterate(hashIterationN));
		// 300000 -> 185ms
		// System.out.println("timeHNSetIteration: " +
		// timeHNSetIterate(hashIterationN * 10) / 10);

		HashSet hashSet = new HashSet();
		TreeSet a = new TreeSet();
		TreeSet b = new TreeSet();

		a.add(new Integer(1));
		b.add(new Integer(2));

		hashSet.add(a);
		hashSet.add(b);

		System.out.println("a.hashCode = " + a.hashCode());
		System.out.println("b.hashCode = " + b.hashCode());
		System.out.println("hashSet.size = " + hashSet.size());

		b.remove(new Integer(2));
		System.out.println("a.hashCode = " + a.hashCode());
		System.out.println("b.hashCode = " + b.hashCode());
		System.out.println("hashSet.size = " + hashSet.size());

		System.out.println(hashSet.contains(b));
		b.add(new Integer(-1));
		System.out.println("a.hashCode = " + a.hashCode());
		System.out.println("b.hashCode = " + b.hashCode());
		System.out.println("hashSet.size = " + hashSet.size());
		System.out.println(hashSet.contains(b));

		HNSubSet subset = new HNSubSet();
		subset.add(1);
		subset.add(0);
		subset.add(-1); // we only support integers > 0!!!
		subset.add(0);
		subset.add(100);
		subset.add(75);
		subset.add(22);
		subset.add(987654);
		subset.add(7);
		subset.add(987654);
		System.out.println(subset.toString());
		HNSet hnset = new HNSet();
		System.out.println("Set = " + hnset.toString());
		hnset.add(subset);
		hnset.add(subset);
		subset.remove(1);
		subset.remove(22);
		hnset.add(subset);
		hnset.add(new HNSubSet());
		System.out.println("Set = " + hnset.toString());

	}

}

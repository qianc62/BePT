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

package org.processmining.framework.models.heuristics.util;

import java.util.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description: Singleton that is used during the firing of tasks.
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
public class CacheMarking extends HashMap {

	private static CacheMarking singleton = null;
	private static final int MAX_SIZE = 500000;
	private static Random gen = null;

	// public static double cacheHit = 0;
	// public static double cacheMiss = 0;

	private CacheMarking() {
		super(MAX_SIZE);
		gen = new Random(MAX_SIZE);
	}

	public static synchronized CacheMarking getInstance() {
		if (singleton == null) {
			singleton = new CacheMarking();
		}
		return singleton;
	}

	public Object put(Object key, Object value) {

		if (this.size() > MAX_SIZE) {
			int removeIndex = gen.nextInt(size());
			double percentageToRemove = MAX_SIZE * 0.20;

			Iterator keys = this.keySet().iterator();

			for (int i = 0; i < removeIndex; i++) {
				keys.next();
			}

			for (int j = removeIndex, l = 0; j < size()
					&& l < percentageToRemove; l++, j++) {
				remove(keys.next());
			}

		}

		return super.put(key, value);
	}

	// public void clear() {
	// super.clear();
	// cacheHit = 0;
	// cacheMiss = 0;
	// }

}

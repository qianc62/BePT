package org.processmining.framework.util;

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
public class StringSimilarity {

	private StringSimilarity() {
	};

	private static short minimum(int a, int b, int c) {
		return (short) Math.min(a, Math.min(b, c));
	}

	/**
	 * Returns a number to express the difference between two strings. The
	 * smaller the number, the more equal the strings are. Equal strings return
	 * 0. The result is the number of operations required to go from one string
	 * to the other by inserting, removing or replacing characters. Therefore,
	 * for the result r holds that 0 <= r <= s.length()+t.length()
	 * 
	 * @param s
	 *            String
	 * @param t
	 *            String
	 * @return int
	 * 
	 */
	public static int similarity(String s, String t) {

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}
		int MAX_N = m + n;

		short[] swap; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		short i; // iterates through s
		short j; // iterates through t

		Object t_j = null; // jth object of t

		short cost; // cost

		short[] d = new short[MAX_N + 1];
		short[] p = new short[MAX_N + 1];

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			Object s_i = null; // ith object of s
			for (i = 1; i <= n; i++) {
				s_i = s.charAt(i - 1);
				cost = s_i.equals(t_j) ? (short) 0 : (short) 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = minimum(d[i - 1] + 1, p[i] + 1, p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			swap = p;
			p = d;
			d = swap;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}
}

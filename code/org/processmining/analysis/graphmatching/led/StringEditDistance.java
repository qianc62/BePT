package org.processmining.analysis.graphmatching.led;

public class StringEditDistance {
	public static int editDistance(String label1, String label2) {
		String s = label1;
		String t = label2;

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
				d[i] = (short) Math.min(Math.min(d[i - 1] + 1, p[i] + 1),
						p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			swap = p;
			p = d;
			d = swap;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		int costcount = p[n];

		// equivalence score = 1 - (costcount / max_costcount)
		// where max_costcount = sum of string lengths
		return costcount;
		// return 1 - (costcount * 1.0) / (s.length() * 1.0 + t.length() * 1.0);
	}

	public static double similarity(String label1, String label2) {
		if ((label1.length() == 0) && (label2.length() == 0)) {
			return 1.0;
		}
		return 1 - (editDistance(label1, label2) * 1.0)
				/ (Math.max(label1.length(), label2.length()) * 1.0);
	}
}

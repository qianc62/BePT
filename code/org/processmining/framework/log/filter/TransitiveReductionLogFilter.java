package org.processmining.framework.log.filter;

import java.util.Arrays;
import org.processmining.framework.log.LogFilter;

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
public class TransitiveReductionLogFilter extends
		TransitiveCalculationLogFilter {

	public TransitiveReductionLogFilter() {
		super(LogFilter.SLOW, "Calculate Transitive Reduction");
	}

	protected String getHelpForThisLogFilter() {
		return "This LogFilter changes each process instance if it is a partial order. "
				+ "In that case, it builds the transitive reduction of the partial order.";
	}

	protected void doCalculation(int[][] adj, int N) {
		transitivelyReduce(adj, N);
	}

	private void transitivelyReduce(int[][] adj, int N) {
		int n = adj.length;

		for (int i = 0; i < n; i++) {

			int[] removed = new int[n];
			Arrays.fill(removed, 0);

			for (int j = 0; j < n; j++) {
				if (removed[j] > 0) {
					// the edge to j has been removed
					continue;
				}
				if (adj[i][j] > 0 && adj[i][j] < N) {
					// there is an edge i -> j
					int[] done = new int[n];
					Arrays.fill(done, 0);
					done[i] = 1;
					done[j] = 1;
					removeDirectSuc(adj, i, j, done, removed, n, N);
				}
			}
		}
	}

	private void removeDirectSuc(int[][] adj, int v, int w, int[] done,
			int[] removed, final int n, final int N) {
		for (int x = 0; x < n; x++) {
			if (adj[w][x] > 0 && adj[w][x] < N) {
				// there is a path v -..-> w -> x
				if (done[x] == 0) {
					done[x] = 1;
					if (adj[v][x] == 0 || adj[v][x] >= N) {
						// there is no edge v -> x
					} else {
						adj[v][x] = N;
						removed[x] = 1;
					}
					removeDirectSuc(adj, v, x, done, removed, n, N);
				}
			}
		}
	}

}

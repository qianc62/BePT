package org.processmining.framework.log.filter;

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
public class TransitiveClosureLogFilter extends TransitiveCalculationLogFilter {

	public TransitiveClosureLogFilter() {
		super(LogFilter.SLOW, "Calculate Transitive Closure");
	}

	protected String getHelpForThisLogFilter() {
		return "This LogFilter changes each process instance if it is a partial order. "
				+ "In that case, it builds the transitive closure of the partial order.";
	}

	protected void doCalculation(int[][] adj, int N) {
		transitivelyClose(adj, N);
	}

	private void transitivelyClose(int[][] adj, int N) {
		// Floyd-Warshall algorithm
		// see http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm,
		// where N = infinity;
		// and the predecessor matrix is of no interest.

		int n = adj.length;
		// Initialization
		/*
		 * int[][] pred = new int[n][n]; for (int i = 0; i < n; i++) { for (int
		 * j = 0; j < n; j++) { pred[i][j] = 0; if (adj[i][j] > 0 && adj[i][j] <
		 * N) { pred[i][j] = i; } } }
		 */
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if ((adj[i][j] > adj[i][k] + adj[k][j])) {
						adj[i][j] = adj[i][k] + adj[k][j];
						// pred[i][j] = pred[k][j];
					}
				}
			}
		}
	}

}

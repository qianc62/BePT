package org.processmining.analysis.traceclustering.distance;

import java.util.Vector;

import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.ActivityCharStreamProfile;
import org.processmining.analysis.traceclustering.profile.Profile;

public class LevenshteinDistance extends DistanceMetric {
	int encodingLength;
	DistanceMatrix distanceMatrix;

	public LevenshteinDistance() {
		super(
				"Levenshtein Edit Distance",
				"Levenshtein Edit Distance-unit cost model for substitution and indel operations");
	}

	@Override
	public double getDistance(InstancePoint pointA, InstancePoint pointB) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DistanceMatrix getDistanceMatrix(Profile profile) {
		System.out.println("In Levenshtein Distance");
		int numberOfInstances = profile.numberOfInstances();

		if (numberOfInstances < 500) {
			distanceMatrix = new DoubleDistanceMatrix(numberOfInstances);
		} else {
			distanceMatrix = new FloatDistanceMatrix(numberOfInstances);
		}

		encodingLength = ((ActivityCharStreamProfile) profile)
				.getEncodingLength();
		computeLevenshteinDistanceLinearSpace(((ActivityCharStreamProfile) profile)
				.getCharStreams());

		return distanceMatrix;
	}

	protected void computeLevenshteinDistanceLinearSpace(
			Vector<String> charStreams) {
		int noCharStreams = charStreams.size();

		String seq1, seq2;
		int lengthSeq1, lengthSeq2, maxLength, dist;
		float distanceNorm1;

		for (int i = 0; i < noCharStreams; i++) {
			if ((i + 1) % 100 == 0) {
				System.out.println("Done 100 Profiles");
			}

			seq1 = charStreams.get(i);
			lengthSeq1 = seq1.length() / encodingLength;

			for (int j = 0; j < i; j++) {
				seq2 = charStreams.get(j);

				if (seq1.equals(seq2)) {
					distanceNorm1 = 0;
				} else {

					lengthSeq2 = seq2.length() / encodingLength;

					maxLength = lengthSeq1;
					if (lengthSeq2 > maxLength)
						maxLength = lengthSeq2;

					dist = levenshteinDistanceLinearSpace(seq1, seq2);
					distanceNorm1 = dist / (float) (lengthSeq1 + lengthSeq2);
				}

				distanceMatrix.set(i, j, distanceNorm1);
			}
		}
	}

	protected int levenshteinDistanceLinearSpace(String seq1, String seq2) {

		String sI, tJ;
		int lengthSeq1, lengthSeq2, cost;

		lengthSeq1 = seq1.length() / encodingLength;
		lengthSeq2 = seq2.length() / encodingLength;

		int[] S = new int[lengthSeq2 + 1];

		S[0] = 0;
		S[1] = S[0] + 1; // Insert first symbol
		for (int j = 2; j <= lengthSeq2; j++) {
			S[j] = S[j - 1] + 1;
		}

		int s, c;
		for (int i = 1; i <= lengthSeq1; i++) {
			s = S[0];
			S[0] = c = S[0] + 1; // Insertion of first symbol
			sI = seq1.substring((i - 1) * encodingLength, i * encodingLength);
			for (int j = 1; j <= lengthSeq2; j++) {
				tJ = seq2.substring((j - 1) * encodingLength, j
						* encodingLength);

				cost = 0;
				if (!sI.equals(tJ))
					cost = 1;
				c = Minimum(S[j] + 1, s + cost, c + 1);
				s = S[j];
				S[j] = c;
			}
		}

		return S[lengthSeq2];

	}

	protected void computeLevenshteinDistance(Vector<String> charStreams) {
		int noCharStreams = charStreams.size();

		String seq1, seq2, sI, tJ;
		int lengthSeq1, lengthSeq2, maxLength, cost;
		float distanceNorm1;
		int[][] d;

		for (int i = 0; i < noCharStreams; i++) {
			if ((i + 1) % 100 == 0) {
				System.out.println("Done 100 Profiles");
			}
			seq1 = charStreams.get(i);
			lengthSeq1 = seq1.length() / encodingLength;

			distanceNorm1 = 0;

			for (int j = 0; j < i; j++) {
				seq2 = charStreams.get(j);
				lengthSeq2 = seq2.length() / encodingLength;

				maxLength = lengthSeq1;

				if (lengthSeq2 > maxLength)
					maxLength = lengthSeq2;

				if (seq1.equals(seq2)) {
					distanceNorm1 = 0;
				} else {
					// Step 1

					if (lengthSeq1 == 0) {
						distanceNorm1 = lengthSeq2
								/ ((float) (lengthSeq1 + lengthSeq2));
					} else if (lengthSeq2 == 0) {
						distanceNorm1 = lengthSeq1
								/ ((float) (lengthSeq1 + lengthSeq2));
					}

					// Step 2
					d = new int[lengthSeq1 + 1][lengthSeq2 + 1];
					for (int m = 0; m <= lengthSeq1; m++) {
						d[m][0] = m;
					}

					for (int n = 0; n <= lengthSeq2; n++) {
						d[0][n] = n;
					}

					// Step 3

					for (int m = 1; m <= lengthSeq1; m++) {
						sI = seq1.substring((m - 1) * encodingLength, m
								* encodingLength);

						for (int n = 1; n <= lengthSeq2; n++) {
							tJ = seq2.substring((n - 1) * encodingLength, n
									* encodingLength);

							if (sI.equals(tJ)) {
								cost = 0;
							} else {
								cost = 1;
							}

							d[m][n] = Minimum(d[m - 1][n] + 1, d[m][n - 1] + 1,
									d[m - 1][n - 1] + cost);
						}
					}

					distanceNorm1 = d[lengthSeq1][lengthSeq2]
							/ ((float) (lengthSeq1 + lengthSeq2));
				}
				distanceMatrix.set(i, j, distanceNorm1);
			}
		}
	}

	private int Minimum(int a, int b, int c) {
		int mi;

		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;

	}
}

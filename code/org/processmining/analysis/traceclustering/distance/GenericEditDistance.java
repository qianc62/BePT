package org.processmining.analysis.traceclustering.distance;

import java.util.HashMap;
import java.util.Vector;

import org.processmining.analysis.traceclustering.charstreams.NGram;
import org.processmining.analysis.traceclustering.charstreams.NGramSubstitution;
import org.processmining.analysis.traceclustering.model.InstancePoint;
import org.processmining.analysis.traceclustering.profile.ActivityCharStreamProfile;
import org.processmining.analysis.traceclustering.profile.Profile;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class GenericEditDistance extends DistanceMetric {
	int encodingLength;
	private HashMap<String, Integer> substitutionScore;
	private HashMap<String, Integer> indelScore;

	DistanceMatrix distanceMatrix;

	public GenericEditDistance() {
		super(
				"Generic Edit Distance",
				"Generic Edit Distance-costs for substitution and indel operations automatically derived");
	}

	@Override
	public double getDistance(InstancePoint pointA, InstancePoint pointB) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DistanceMatrix getDistanceMatrix(Profile profile) {
		System.out.println("In Generic Edit Distance");
		int numberOfInstances = profile.numberOfInstances();

		if (numberOfInstances < 500) {
			distanceMatrix = new DoubleDistanceMatrix(numberOfInstances);
		} else {
			distanceMatrix = new FloatDistanceMatrix(numberOfInstances);
		}

		encodingLength = ((ActivityCharStreamProfile) profile)
				.getEncodingLength();
		Vector<String> charStreams = ((ActivityCharStreamProfile) profile)
				.getCharStreams();

		// Process charStreams
		// 1. Generate N Grams
		new NGram(3, encodingLength, charStreams);
		NGramSubstitution ngramSubstitution = new NGramSubstitution(3,
				encodingLength, "C:\\Temp\\ProM\\NGrams\\3", "allNGrams.out",
				((ActivityCharStreamProfile) profile).getActivityCharMap());

		substitutionScore = ngramSubstitution.getScoringMatrix();
		indelScore = ngramSubstitution.getInsRightGivenLeft();

		computeEditDistance(charStreams, distanceMatrix);

		return distanceMatrix;
	}

	protected void computeEditDistance(Vector<String> charStreams,
			DistanceMatrix distanceMatrix) {
		int noCharStreams = charStreams.size();

		int bestScore;

		String seq1, seq2;
		int lengthSeq1, lengthSeq2, maxLength;
		float distanceNorm1;

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
					bestScore = globalAlignLinearSpace(seq1, seq2);
					if (bestScore < 0) {
						System.out.println("Best Score < 0");
					} else if (bestScore != 0) {
						distanceNorm1 = (float) (lengthSeq1 + lengthSeq2)
								/ (float) bestScore;
					}
				}

				distanceMatrix.set(i, j, distanceNorm1);
			}
		}
	}

	protected int globalAlignLinearSpace(String seq1, String seq2) {
		int bestScore;
		int gapCost = 0;

		int lengthSeq1 = seq1.length() / encodingLength;
		int lengthSeq2 = seq2.length() / encodingLength;
		int indelValue, indelIValue, indelJValue;
		Integer indelObj;
		String leftSymbol, currentSymbol, currentISymbol, currentJSymbol, leftISymbol, leftJSymbol;

		int[] S = new int[lengthSeq2 + 1];
		S[0] = 0;
		S[1] = S[0] + gapCost; // It is ok to insert the first symbol
		for (int j = 2; j <= lengthSeq2; j++) {
			indelValue = gapCost;

			leftSymbol = seq2.substring((j - 2) * encodingLength, (j - 1)
					* encodingLength);
			currentSymbol = seq2.substring((j - 1) * encodingLength, j
					* encodingLength);
			indelObj = indelScore.get(leftSymbol + "@" + currentSymbol);
			if (indelObj != null)
				indelValue = indelObj.intValue();
			S[j] = S[j - 1] + indelValue;
		}

		int s, c, subScore;
		Integer subObj;
		for (int i = 1; i <= lengthSeq1; i++) {
			s = S[0];
			currentISymbol = seq1.substring((i - 1) * encodingLength, i
					* encodingLength);
			indelIValue = gapCost;
			if (i > 1) {
				leftISymbol = seq1.substring((i - 2) * encodingLength, (i - 1)
						* encodingLength);
				indelObj = indelScore.get(leftISymbol + "@" + currentISymbol);
				if (indelObj != null)
					indelIValue = indelObj.intValue();
			}
			S[0] = c = S[0] + indelIValue;
			for (int j = 1; j <= lengthSeq2; j++) {
				currentJSymbol = seq2.substring((j - 1) * encodingLength, j
						* encodingLength);
				indelJValue = gapCost;
				if (j > 1) {
					leftJSymbol = seq2.substring((j - 2) * encodingLength,
							(j - 1) * encodingLength);
					indelObj = indelScore.get(leftJSymbol + "@"
							+ currentJSymbol);
					if (indelObj != null)
						indelJValue = indelObj.intValue();
				}

				subScore = gapCost;
				subObj = substitutionScore.get(currentISymbol + "@"
						+ currentJSymbol);
				if (subObj != null)
					subScore = subObj.intValue();

				c = max(S[j] + indelIValue, s + subScore, c + indelJValue);
				s = S[j];
				S[j] = c;
			}
		}

		bestScore = S[lengthSeq2];
		return bestScore;
	}

	protected int max(int a, int b, int c) {
		return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
	}

}

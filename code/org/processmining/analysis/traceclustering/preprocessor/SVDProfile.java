package org.processmining.analysis.traceclustering.preprocessor;

import java.io.IOException;

import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.log.LogReader;

import weka.core.matrix.SingularValueDecomposition;
import weka.core.matrix.Matrix;

public class SVDProfile extends AbstractPreProcessor {

	public SVDProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("SVD", "Apply SVD to the profiles", log);
	}

	public void buildProfile(AggregateProfile aggregateProfile) {
		this.buildProfile(aggregateProfile, 10);
	}

	public void buildProfile(AggregateProfile aggregateProfile, int dim) {
		Matrix in = null;

		boolean bMoreItem = true;

		// make matrix
		if (aggregateProfile.getItemKeys().size() >= log.numberOfInstances()) {
			in = new Matrix(aggregateProfile.getItemKeys().size(), log
					.numberOfInstances());
			int row = 0;
			for (String key : aggregateProfile.getItemKeys()) {
				for (int i = 0; i < log.numberOfInstances(); i++) {
					in.set(row, i, aggregateProfile.getValue(i, key));
					// if(i==10)break;
				}
				row++;
			}
		} else {
			bMoreItem = false;
			in = new Matrix(log.numberOfInstances(), aggregateProfile
					.getItemKeys().size());
			int row = 0;
			for (String key : aggregateProfile.getItemKeys()) {
				for (int i = 0; i < log.numberOfInstances(); i++) {
					in.set(i, row, aggregateProfile.getValue(i, key));
				}
				row++;
			}
		}

		SingularValueDecomposition svd = new SingularValueDecomposition(in);

		Matrix mat = null;
		for (int k = 0; k < svd.getSingularValues().length; k++) {
			System.out.print("sing[" + k + "] = " + svd.getSingularValues()[k]);
		}
		System.out.println();
		if (bMoreItem) {
			System.out.println("row = "
					+ svd.getV().transpose().getRowDimension() + ", col = "
					+ svd.getV().transpose().getColumnDimension());
			System.out.println("u row = " + svd.getU().getRowDimension()
					+ ", col = " + svd.getU().getColumnDimension());
			mat = svd.getU().getMatrix(0, dim - 1, 0,
					log.numberOfInstances() - 1);
		} else {
			// result
			System.out.println("row = "
					+ svd.getV().transpose().getRowDimension() + ", col = "
					+ svd.getV().transpose().getColumnDimension());
			System.out.println("u row = " + svd.getU().getRowDimension()
					+ ", col = " + svd.getU().getColumnDimension());
			mat = svd.getU().transpose().getMatrix(0, dim - 1, 0,
					log.numberOfInstances() - 1);
		}

		/*
		 * for(int k=0;k<svd.getS().getRowDimension();k++) { for(int
		 * j=0;j<svd.getS().getColumnDimension();j++)
		 * System.out.print("ss["+k+"]["+j+"]="+svd.getS().get(k, j)+",  ");
		 * System.out.println(); }
		 */

		for (int k = 0; k < mat.getRowDimension(); k++) {
			String key = aggregateProfile.getItemKey(k);
			for (int j = 0; j < log.numberOfInstances(); j++) {
				incrementValue(j, key, mat.get(k, j));
				// System.out.print("("+k+","+j+")="+mat.get(k,j)+"  ");
			}
			// System.out.println();
		}
		this.setNormalizationMaximum(1.0);
	}
}

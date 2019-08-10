package org.processmining.analysis.traceclustering.preprocessor;

import java.io.IOException;

import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.log.LogReader;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.attributeSelection.PrincipalComponents;

public class PCAProfile extends AbstractPreProcessor {

	public PCAProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("PCA", "Apply PCA to the profiles", log);
	}

	public void buildProfile(AggregateProfile aggregateProfile) {
		this.buildProfile(aggregateProfile, 10);
	}

	public void buildProfile(AggregateProfile aggregateProfile, int dim) {
		PrincipalComponents pca = new PrincipalComponents();

		// create attribute information
		FastVector attributeInfo = new FastVector();
		// make attribute
		// clean the relevant attribute list and re-fill based on new selection
		// scope
		for (int i = 0; i < aggregateProfile.numberOfItems(); i++) {
			String name = CpnUtils.replaceSpecialCharacters(aggregateProfile
					.getItemKey(i));
			Attribute wekaAtt = new Attribute(name);
			attributeInfo.addElement(wekaAtt);
		}
		attributeInfo.trimToSize();

		// learning
		Instances data = new Instances("Clustering", attributeInfo, 0);
		try {
			for (int i = 0; i < log.numberOfInstances(); i++) {
				Instance instance0 = new Instance(attributeInfo.size());
				for (int j = 0; j < aggregateProfile.numberOfItems(); j++) {
					String name = CpnUtils
							.replaceSpecialCharacters(aggregateProfile
									.getItemKey(j));
					Attribute wekaAtt = data.attribute(name);
					if (wekaAtt != null) {
						double doubleAttValue = (new Double(aggregateProfile
								.getValue(i, j))).doubleValue();
						instance0.setValue(wekaAtt, doubleAttValue);
					} else {
						System.out.println("fail to add");
					}
				}
				instance0.setDataset(data);
				data.add(instance0);
			}
			pca.buildEvaluator(data);

			// for(String str :pca.getOptions())
			// {
			// System.out.println("option = "+ str);
			// }
			/*
			 * for(int i=0;i<data.numAttributes();i++) { double db[] =
			 * data.attributeToDoubleArray(i); System.out.print("{"); for (int
			 * j=0;j<db.length;j++) { System.out.print("["+i+"]["+j+"]"+db[j]+
			 * ", "); } System.out.println("}"); }
			 */
			Instances data2 = pca.transformedData();

			// for(int i=0;i<data2.numAttributes();i++) {
			// double db[] = data2.attributeToDoubleArray(i);
			// System.out.print("{");
			// for (int j=0;j<db.length;j++)
			// {
			// System.out.print("["+i+"]["+j+"]"+db[j]+ ", ");
			// }
			// System.out.println("}");
			// }

			for (int i = 0; i < data2.numAttributes(); i++) {
				double db[] = data2.attributeToDoubleArray(i);
				for (int c = 0; c < log.numberOfInstances(); c++) {
					incrementValue(c, String.valueOf(i), db[c]);
				}
			}
			this.setNormalizationMaximum(1.0);

		} catch (Exception c) {
			System.out.println(c.toString());
		}
	}
}

package org.processmining.mining.armining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

import weka.associations.Apriori;
import weka.associations.AprioriItemSet;
import weka.core.FastVector;
import weka.core.Utils;

/**
 * <p>
 * Title: MyApriori
 * </p>
 * 
 * <p>
 * Description:MyApriori extends Apriori()
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
 * @author Shaifali Gupta (s.gupta@student.tue.nl)
 * @version 1.0
 * 
 */

public class MyApriori extends Apriori {

	protected LogReader myLog;
	protected boolean m_eventTypeCare;
	protected boolean m_noName;
	ArrayList myLHSList = new ArrayList();
	ArrayList myRHSList = new ArrayList();

	public MyApriori() {
	}

	public ArrayList<String> get_m_allTheRules() {

		ArrayList<String> myRulesArray = new ArrayList<String>();
		ArrayList<String> myRulesArray2 = new ArrayList<String>();
		ArrayList myPremiseList = new ArrayList();
		ArrayList myConsList = new ArrayList();
		ArrayList<String> myPremise = null;
		ArrayList<String> myCons = null;
		TreeSet ruleIndToKeep = new TreeSet();
		TreeSet ruleIndToRemove = new TreeSet();
		ArrayList<String> myLHS = new ArrayList<String>();
		ArrayList<String> myRHS = new ArrayList<String>();
		ArrayList myLHSList = new ArrayList();
		ArrayList myRHSList = new ArrayList();

		for (int j = 0; j < m_allTheRules[0].size(); j++) {
			AprioriItemSet tempSet = (AprioriItemSet) m_allTheRules[0]
					.elementAt(j);
			myPremise = new ArrayList<String>();
			for (int i = 0; i < m_instances.numAttributes(); i++) {
				if (tempSet.itemAt(i) != -1) {
					myPremise
							.add(m_instances.attribute(i).name()
									+ "="
									+ m_instances.attribute(i).value(
											tempSet.itemAt(i)));
				}
			}
			myPremiseList.add(myPremise);

			tempSet = (AprioriItemSet) m_allTheRules[1].elementAt(j);
			myCons = new ArrayList<String>();
			for (int i = 0; i < m_instances.numAttributes(); i++) {
				if (tempSet.itemAt(i) != -1) {
					myCons
							.add(m_instances.attribute(i).name()
									+ "="
									+ m_instances.attribute(i).value(
											tempSet.itemAt(i)));
				}
			}
			myConsList.add(myCons);
		}

		// original rules from Weka can be obtained from this commented portion
		// -----
		// for (int q = 0; q < myPremiseList.size(); q++) {
		// String confValue2 = Utils.doubleToString(((Double)
		// m_allTheRules[2].elementAt(q)).
		// doubleValue(), 2);
		// String numberRules2 = (Utils.doubleToString((double) q + 1,
		// (int) (Math.log(m_numRules) / Math.log(10) + 1), 0) + ". ");
		//
		// Object myA2[] = ((ArrayList<String>) myPremiseList.get(q)).toArray();
		// String tempString2 = "";
		// for (int p = 0; p < myA2.length; p++) {
		// String individualPremise2 = (String) myA2[p];
		//
		// String newPremiseName2 = "";
		// if (individualPremise2.contains("=yes")) {
		// newPremiseName2 = individualPremise2.split("\\=")[0];
		//
		// } else {
		// if (individualPremise2.contains("=no")) {
		// newPremiseName2 = "!" + individualPremise2.split("\\=")[0];
		// }
		// }
		// tempString2 += newPremiseName2;
		// if (p != myA2.length - 1) {
		// tempString2 += ",";
		// }
		// }
		// tempString2 += "=>";
		// Object myB2[] = ((ArrayList<String>) myConsList.get(q)).toArray();
		// for (int r = 0; r < myB2.length; r++) {
		// String individualCons2 = (String) myB2[r];
		// String newConsName2 = "";
		// if (individualCons2.contains("=yes")) {
		// newConsName2 = individualCons2.split("\\=")[0];
		// } else {
		// if (individualCons2.contains("=no")) {
		// newConsName2 = "!" + individualCons2.split("\\=")[0];
		// }
		// }
		// tempString2 += newConsName2;
		//
		// if (r != myB2.length - 1) {
		// tempString2 += ", ";
		// }
		// }
		// myRulesArray2.add(numberRules2 + tempString2 + "   " + "(conf: " +
		// confValue2 + ")");
		// }
		// System.out.println("original rules from weka: " + myRulesArray2);

		// ------------------------------------------------------------------------------------
		// support value that is calculated on basis of values of lower bound
		// and upper bound provided by the user
		// StringBuffer text = new StringBuffer();
		// text.append("Minimum support: "
		// + Utils.doubleToString(m_minSupport, 2)
		// + " (" + ((int) (m_minSupport * (double) m_instances.numInstances() +
		// 0.5))
		// + " instances)"
		// + '\n');
		// System.out.println("m_minSupport: " + m_minSupport);
		// System.out.println("m_instances.numInstances: " +
		// m_instances.numInstances());
		// ------------------------------------------------------------------------------------

		outer: for (int i = 0; i < myPremiseList.size(); i++) {
			if (ruleIndToRemove.contains(i)) {
				continue;
			}
			ruleIndToKeep.removeAll(ruleIndToRemove);
			for (int j = i + 1; j < myPremiseList.size(); j++) {
				if (ruleIndToRemove.contains(i)) {
					continue outer;
				}
				if (ruleIndToRemove.contains(j)) {
					continue;
				}
				ArrayList<String> ielement = (ArrayList<String>) myPremiseList
						.get(i);
				ArrayList<String> jelement = (ArrayList<String>) myPremiseList
						.get(j);
				// if L1=L2
				if (ielement.toString().equals(jelement.toString())) {

					ArrayList<String> ielementRHS = (ArrayList<String>) myConsList
							.get(i);
					ArrayList<String> jelementRHS = (ArrayList<String>) myConsList
							.get(j);

					if (ielementRHS.containsAll(jelementRHS)) { // R1 is
						// superset of
						// R2, then keep
						// Rule1
						ruleIndToKeep.add(i);
						ruleIndToRemove.add(j);
					} else {
						if (jelementRHS.containsAll(ielementRHS)) { // if R2 is
							// superset
							// of R1,
							// keep
							// Rule2
							ruleIndToKeep.add(j);
							ruleIndToRemove.add(i);
						} else { // keep both rules but we must check if R1 and
							// R2 are not dummy.
							String iContent = ielementRHS.toString();
							int iSize = ielementRHS.size();
							String jContent = jelementRHS.toString();
							int jSize = jelementRHS.size();
							if (iSize == 1) {
								if (iContent.contains("noname")) {
									ruleIndToRemove.add(i);
								} else {
									ruleIndToKeep.add(i);
								}
							} else { // size of R1 is more than 1
								ruleIndToKeep.add(i);
							}
							if (jSize == 1) {
								if (jContent.contains("noname")) {
									ruleIndToRemove.add(j);
								} else {
									ruleIndToKeep.add(j);
								}
							} else { // size of R2 is more than 1
								ruleIndToKeep.add(j);
							}
						}
					}
				}
				// L1 IS NOT EQUAL TO L2 then first we check if L1 is subset of
				// L2. If not, then we check if
				// L2 is subset of L1
				else {
					// if L1 is subset of L2
					if (jelement.containsAll(ielement)) { // L1 is subset of L2
						ArrayList<String> ielementRHS = (ArrayList<String>) myConsList
								.get(i);
						ArrayList<String> jelementRHS = (ArrayList<String>) myConsList
								.get(j);
						String iContent = ielementRHS.toString();
						int iSize = ielementRHS.size();
						String jContent = jelementRHS.toString();
						int jSize = jelementRHS.size();

						if (ielementRHS.containsAll(jelementRHS)) { // if R1 is
							// superset
							// of R2
							ruleIndToRemove.add(j); // retain Rule1
							if (iSize == 1) {
								if (iContent.contains("noname")) {
									ruleIndToRemove.add(i);
								} else {
									ruleIndToKeep.add(i);
								}
							} else { // size of R1 is more than 1
								ruleIndToKeep.add(i);
							}
						} else { // keep both rules
							// first we chk if the RHS of both the rules is not
							// dummy.
							if (iSize == 1) {
								if (iContent.contains("noname")) {
									ruleIndToRemove.add(i);
								} else {
									ruleIndToKeep.add(i);
								}
							} else { // size of R1 is more than 1
								ruleIndToKeep.add(i);
							}
							if (jSize == 1) {
								if (jContent.contains("noname")) {
									ruleIndToRemove.add(j);
								} else {
									ruleIndToKeep.add(j);
								}
							} else { // size of R2 is more than 1
								ruleIndToKeep.add(j);
							}
						}
					} else { // if L2 is subset of L1
						if (ielement.containsAll(jelement)) {
							ArrayList<String> ielementRHS = (ArrayList<String>) myConsList
									.get(i);
							ArrayList<String> jelementRHS = (ArrayList<String>) myConsList
									.get(j);
							String iContent = ielementRHS.toString();
							int iSize = ielementRHS.size();
							String jContent = jelementRHS.toString();
							int jSize = jelementRHS.size();

							if (jelementRHS.containsAll(ielementRHS)) { // if R2
								// is
								// superset
								// of R1
								ruleIndToRemove.add(i); // remove R1
								// we need to keep only R2 but first we check if
								// it is not dummy!
								if (jSize == 1) {
									if (jContent.contains("noname")) {
										ruleIndToRemove.add(j);
									} else {
										ruleIndToKeep.add(j);
									}
								} else { // size of R2 is more than 1
									ruleIndToKeep.add(j);
								}
							} else { // keep both rules
								// first we chk if they are not dummy
								if (iSize == 1) {
									if (iContent.contains("noname")) {
										ruleIndToRemove.add(i);
									} else {
										ruleIndToKeep.add(i);
									}
								} else { // size of R1 is more than 1
									ruleIndToKeep.add(i);
								}
								if (jSize == 1) {
									if (jContent.contains("noname")) {
										ruleIndToRemove.add(j);
									} else {
										ruleIndToKeep.add(j);
									}
								} else { // size of R2 is more than 1
									ruleIndToKeep.add(j);
								}
							}
						} else {
							// no case is true
							ArrayList<String> ielementRHS = (ArrayList<String>) myConsList
									.get(i);
							ArrayList<String> jelementRHS = (ArrayList<String>) myConsList
									.get(j);
							int iSize = ielementRHS.size();
							if (iSize == 1) {
								String iContent = ielementRHS.toString();
								if (iContent.contains("noname")) {
									ruleIndToRemove.add(i);
								} else {
									ruleIndToKeep.add(i);
								}
							} else { // size of R1 is more than 1
								ruleIndToKeep.add(i);
							}
							int jSize = jelementRHS.size();
							if (jSize == 1) {
								String jContent = jelementRHS.toString();
								if (jContent.contains("noname")) {
									ruleIndToRemove.add(j);
								} else {
									ruleIndToKeep.add(j);
								}
							} else { // size of R2 is more than 1
								ruleIndToKeep.add(j);
							}
						}
					}
				}

			} // j=i+1 end
		} // iloop end

		ruleIndToKeep.removeAll(ruleIndToRemove);

		Iterator<Integer> indexRulesToKeep = ruleIndToKeep.iterator();
		while (indexRulesToKeep.hasNext()) {
			int index = indexRulesToKeep.next().intValue();
			myLHS = (ArrayList<String>) myPremiseList.get(index);
			myLHSList.add(myLHS);
			myRHS = (ArrayList<String>) myConsList.get(index);
			myRHSList.add(myRHS);
		}

		// removing the unnecessary rules
		Iterator<Integer> indexRulesToRemove = ruleIndToRemove.iterator();
		int toDecreaseFromIndex = 0;
		while (indexRulesToRemove.hasNext()) {
			int index = indexRulesToRemove.next().intValue();
			index -= toDecreaseFromIndex;
			m_allTheRules[0].removeElementAt(index);
			m_allTheRules[1].removeElementAt(index);
			m_allTheRules[2].removeElementAt(index);
			toDecreaseFromIndex++;
		}

		for (int k = 0; k < myLHSList.size(); k++) {
			String confValue = Utils.doubleToString(((Double) m_allTheRules[2]
					.elementAt(k)).doubleValue(), 2);
			String numberRules = (Utils.doubleToString((double) k + 1,
					(int) (Math.log(m_numRules) / Math.log(10) + 1), 0) + ". ");

			Object myA[] = ((ArrayList<String>) myLHSList.get(k)).toArray();

			String tempString = "";
			String newTempString = "";
			for (int i = 0; i < myA.length; i++) {
				String individualPremise = (String) myA[i];
				String newPremiseName = "";
				if (individualPremise.contains("noname")) {
					individualPremise = individualPremise.replace("noname",
							"\u03A6");
				}
				if (individualPremise.contains("=yes")) {
					newPremiseName = individualPremise.split("\\=")[0];
				} else {
					if (individualPremise.contains("=no")) {
						newPremiseName = "!"
								+ individualPremise.split("\\=")[0];
					}
				}
				tempString += newPremiseName;
				if (i != myA.length - 1) {
					tempString += ",";
				}
			}
			tempString += "=>";
			Object myB[] = ((ArrayList<String>) myRHSList.get(k)).toArray();
			for (int i = 0; i < myB.length; i++) {
				String individualCons = (String) myB[i];
				String newConsName = "";
				if (individualCons.contains("=yes")) {
					newConsName = individualCons.split("\\=")[0];
				}
				// If we also have 'no' values , decomment this portion...
				// else {
				// if (individualCons.contains("=no")) {
				// newConsName = "!" + individualCons.split("\\=")[0];
				// }
				// }
				tempString += newConsName;

				if (i != myB.length - 1) {
					tempString += ", ";
				}
			}
			if (tempString.contains(",noname")) {
				newTempString = tempString.replace(",noname", " ");
				myRulesArray.add(numberRules + newTempString + "   "
						+ "(conf: " + confValue + ")");
			} else {
				if (tempString.contains(", noname")) {
					newTempString = tempString.replace(", noname", " ");
					myRulesArray.add(numberRules + newTempString + "   "
							+ "(conf: " + confValue + ")");
				} else {
					if (tempString.contains("noname,")) {
						newTempString = tempString.replace("noname,", " ");
						myRulesArray.add(numberRules + newTempString + "   "
								+ "(conf: " + confValue + ")");
					} else {
						if (tempString.contains("noname, ")) {
							newTempString = tempString.replace("noname, ", " ");
							myRulesArray.add(numberRules + newTempString
									+ "   " + "(conf: " + confValue + ")");
						} else {
							myRulesArray.add(numberRules + tempString + "   "
									+ "(conf: " + confValue + ")");
						}
					}
				}
			}
		}
		return myRulesArray;
	}

	public static JScrollPane newDisplay(ArrayList<String> myList) {

		JTextArea ta = new JTextArea();
		ta.setEditable(false);

		for (int i = 0; i < myList.size(); i++) {

			ta.append("\n");
			ta.append(myList.get(i) + "\n");
		}
		JScrollPane scrollPane = new JScrollPane(ta);
		return scrollPane;
	}

	// Get frequent itemsets
	public ArrayList<String> get_m_Ls() {

		ArrayList<String> myFISArray = new ArrayList<String>();
		ArrayList<String> myFIS = null;
		ArrayList myFISList = new ArrayList();

		for (int j = 0; j < m_Ls.size(); j++) {

			FastVector myCurrentItemSets = (FastVector) m_Ls.elementAt(j);

			for (int i = 0; i < myCurrentItemSets.size(); i++) {
				AprioriItemSet myCurrentItemSet = (AprioriItemSet) myCurrentItemSets
						.elementAt(i);
				myFIS = new ArrayList<String>();
				for (int k = 0; k < m_instances.numAttributes(); k++) {

					if (myCurrentItemSet.itemAt(k) != -1
							&& m_instances.attribute(k) != null) {
						myFIS.add(m_instances.attribute(k).name()
								+ "="
								+ m_instances.attribute(k).value(
										myCurrentItemSet.itemAt(k)));
					}
				}
				myFISList.add(myFIS);
			}
		}
		for (int k = 0; k < myFISList.size(); k++) {
			Object myA[] = ((ArrayList<String>) myFISList.get(k)).toArray();
			String tempString = "";
			for (int i = 0; i < myA.length; i++) {
				String individualFIS = (String) myA[i];
				String newFIS = "";
				if (individualFIS.contains("=yes")) {
					newFIS = individualFIS.split("\\=")[0];
				} else {
					if (individualFIS.contains("=no")) {
						newFIS = "!" + individualFIS.split("\\=")[0];
					}
				}
				if ((individualFIS.contains("noname=yes"))) {
					newFIS = individualFIS.replace("noname=yes", "\u03A6");
				}

				tempString += newFIS;
				if (i != myA.length - 1) {
					tempString += ", ";
				}
			}
			myFISArray.add((k + 1) + ". " + tempString);
		}
		return myFISArray;
	}

	// Check whether a particular FIS is present in a log trace, do not consider
	// event type information
	public Boolean checkForFIS(ProcessInstance pi, int FISIndex) {

		String myItem = " ";
		boolean flag1 = true;
		boolean flag2;

		int position = -1; // position keeps a track of where are we comparing

		for (int k = 0; k < m_Ls.size(); k++) {

			FastVector mySet = (FastVector) m_Ls.elementAt(k);

			for (int i = 0; i < mySet.size(); i++) {
				position++;
				if (FISIndex != position) {
					continue;
				}
				AprioriItemSet myAIS = (AprioriItemSet) mySet.elementAt(i);

				for (int j = 0; j < myAIS.items().length; j++) {
					if (myAIS.itemAt(j) == -1) {
						continue;
					}

					if (myAIS.itemAt(j) == 0) { // =yes
						flag2 = false;
						myItem = m_instances.attribute(j).name().toString();

						Iterator it = pi.getAuditTrailEntryList().iterator();
						while (it.hasNext()) {
							AuditTrailEntry individualAte = (AuditTrailEntry) it
									.next();
							try {
								String ateDetail = (String) individualAte
										.getElement();
								if (ateDetail.equals(myItem)) {
									flag2 = true;
									break;
								} // end of if
							} catch (Exception ie1) { // end of try
								ie1.printStackTrace();
							}
						} // end of while

						if (flag2 == false) {
							flag1 = false;
							return flag1;
						}
					} // end of if LHSSet.item(i)==0

					if (myAIS.itemAt(j) == 1) { // =no
						flag2 = true;
						myItem = m_instances.attribute(j).name().toString();

						Iterator it2 = pi.getAuditTrailEntryList().iterator();
						while (it2.hasNext()) {
							AuditTrailEntry individualAte = (AuditTrailEntry) it2
									.next();
							try {
								String ateDetail = (String) individualAte
										.getElement();

								if (ateDetail.equals(myItem)) {
									flag2 = false;
									break;
								} // end of if
							} catch (Exception ie1) {
								ie1.printStackTrace();
							}
						} // end of while

						if (flag2 == false) {
							flag1 = false;
							return flag1;
						}
					} // end of if
				}
			}
		}
		return flag1;
	}

	// Check whether a particular FIS is present in a log trace, consider event
	// type information
	public Boolean checkFISWithEventCare(ProcessInstance pi, int FISIndex) {

		String myItem = " ";
		boolean flag1 = true;
		boolean flag2;

		int position = -1; // position keeps a track of where are we comparing

		for (int k = 0; k < m_Ls.size(); k++) {
			FastVector mySet = (FastVector) m_Ls.elementAt(k);
			for (int i = 0; i < mySet.size(); i++) {
				position++;
				if (FISIndex != position) {
					continue;
				}
				AprioriItemSet myAIS = (AprioriItemSet) mySet.elementAt(i);

				for (int j = 0; j < myAIS.items().length; j++) {
					if (myAIS.itemAt(j) == -1) {
						continue;
					}

					if (myAIS.itemAt(j) == 0) { // =yes
						flag2 = false;
						myItem = m_instances.attribute(j).name().toString();

						Iterator it = pi.getAuditTrailEntryList().iterator();
						while (it.hasNext()) {
							AuditTrailEntry individualAte = (AuditTrailEntry) it
									.next();
							try {
								String ateDetail = (String) individualAte
										.getElement()
										+ " (" + individualAte.getType() + ")";
								if (ateDetail.equals(myItem)) {
									flag2 = true;
									break;
								} // end of if
							} catch (Exception ie1) { // end of try
								ie1.printStackTrace();
							}
						} // end of while

						if (flag2 == false) {
							flag1 = false;
							return flag1;
						}
					} // end of if LHSSet.item(i)==0

					if (myAIS.itemAt(j) == 1) { // =no
						flag2 = true;
						myItem = m_instances.attribute(j).name().toString();

						Iterator it2 = pi.getAuditTrailEntryList().iterator();
						while (it2.hasNext()) {
							AuditTrailEntry individualAte = (AuditTrailEntry) it2
									.next();
							try {
								String ateDetail = (String) individualAte
										.getElement()
										+ " (" + individualAte.getType() + ")";
								if (ateDetail.equals(myItem)) {
									flag2 = false;
									break;
								} // end of if
							} catch (Exception ie1) {
								ie1.printStackTrace();
							}
						} // end of while

						if (flag2 == false) {
							flag1 = false;
							return flag1;
						}
					} // end of if
				}
			}
		}
		return flag1;
	}

	// Check whether a particular association rule is present in a log trace, do
	// not consider event type information
	public Boolean checkForRule(ProcessInstance pi, int ruleIndex) {
		// flag for the complete rule
		boolean flag1 = true;
		// flag for individual element of a rule
		boolean flag2;
		String myPremise2 = "";
		String myCons2 = "";

		AprioriItemSet LHSSet = (AprioriItemSet) m_allTheRules[0]
				.elementAt(ruleIndex);

		for (int i = 0; i < LHSSet.items().length; i++) {
			if (LHSSet.itemAt(i) == -1) {
				continue;
			}
			if (LHSSet.itemAt(i) == 0) { // =yes
				flag2 = false;
				myPremise2 = LHSSet.toString();
				myPremise2 = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {

						String ateDetail = (String) individualAte.getElement();
						if (ateDetail.equals(myPremise2)) {
							flag2 = true;
							break;
						} // end of if
					} catch (Exception ie1) { // end of try
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of if LHSSet.item(i)==0

			if (LHSSet.itemAt(i) == 1) { // =no
				flag2 = true;
				// myPremise2 = m_instances.attribute(i).name().toString();
				myPremise2 = LHSSet.toString();

				Iterator it2 = pi.getAuditTrailEntryList().iterator();
				while (it2.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it2
							.next();
					try {
						// String ateDetail = (String)
						// individualAte.getElement() + " ("
						// +individualAte.getType() + ")";
						String ateDetail = (String) individualAte.getElement();

						if (ateDetail.equals(myPremise2)) {
							flag2 = false;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}

			} // end of if
		} // end of outer for

		// ----for RHS
		AprioriItemSet RHSSet = (AprioriItemSet) m_allTheRules[1]
				.elementAt(ruleIndex);

		for (int i = 0; i < RHSSet.items().length; i++) {

			if (RHSSet.itemAt(i) == -1) {
				continue;
			}
			if (RHSSet.itemAt(i) == 0) { // =yes
				flag2 = false;
				myCons2 = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {
						// String ateDetail = (String)
						// individualAte.getElement() + " ("
						// +individualAte.getType() + ")";
						String ateDetail = (String) individualAte.getElement();
						if (ateDetail.equals(myCons2)) {
							flag2 = true;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of if(RHSSet.itemAt(i) == 0)

			if (RHSSet.itemAt(i) == 1) { // =no
				flag2 = true;
				myCons2 = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {
						// String ateDetail = (String)
						// individualAte.getElement() + " (" +
						// individualAte.getType() + ")";
						String ateDetail = (String) individualAte.getElement();
						if (ateDetail.equals(myCons2)) {
							flag2 = false;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of (RHSSet.itemAt(i) == 1)
		} // end of for (int i=0; i<RHSSet.items(i).length;i++)
		return flag1;
	}

	// Check whether a particular association rule is present in a log trace, do
	// not consider event type information
	public Boolean checkRuleWithEventCare(ProcessInstance pi, int ruleIndex) {

		// flag for the complete rule
		boolean flag1 = true;
		// flag for individual element of a rule
		boolean flag2;
		String myPremise = "";
		String myCons = "";

		AprioriItemSet LHSSet = (AprioriItemSet) m_allTheRules[0]
				.elementAt(ruleIndex);

		for (int i = 0; i < LHSSet.items().length; i++) {
			if (LHSSet.itemAt(i) == -1) {
				continue;
			}

			if (LHSSet.itemAt(i) == 0) { // =yes
				flag2 = false;
				myPremise = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {
						String ateDetail = (String) individualAte.getElement()
								+ " (" + individualAte.getType() + ")";
						if (ateDetail.equals(myPremise)) {
							flag2 = true;
							break;
						} // end of if
					} catch (Exception ie1) { // end of try
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of if LHSSet.item(i)==0

			if (LHSSet.itemAt(i) == 1) { // =no
				flag2 = true;
				myPremise = m_instances.attribute(i).name().toString();

				Iterator it2 = pi.getAuditTrailEntryList().iterator();
				while (it2.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it2
							.next();
					try {
						String ateDetail = (String) individualAte.getElement()
								+ " (" + individualAte.getType() + ")";

						if (ateDetail.equals(myPremise)) {
							flag2 = false;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of if
		} // end of outer for

		// ----for RHS

		AprioriItemSet RHSSet = (AprioriItemSet) m_allTheRules[1]
				.elementAt(ruleIndex);

		for (int i = 0; i < RHSSet.items().length; i++) {
			if (RHSSet.itemAt(i) == -1) {
				continue;
			}

			if (RHSSet.itemAt(i) == 0) { // =yes
				flag2 = false;
				myCons = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {
						String ateDetail = (String) individualAte.getElement()
								+ " (" + individualAte.getType() + ")";

						if (ateDetail.equals(myCons)) {
							flag2 = true;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of if(RHSSet.itemAt(i) == 0)

			if (RHSSet.itemAt(i) == 1) { // =no
				flag2 = true;
				myCons = m_instances.attribute(i).name().toString();

				Iterator it = pi.getAuditTrailEntryList().iterator();
				while (it.hasNext()) {
					AuditTrailEntry individualAte = (AuditTrailEntry) it.next();
					try {
						String ateDetail = (String) individualAte.getElement()
								+ " (" + individualAte.getType() + ")";

						if (ateDetail.equals(myCons)) {
							flag2 = false;
							break;
						} // end of if
					} catch (Exception ie1) {
						ie1.printStackTrace();
					}
				} // end of while

				if (flag2 == false) {
					flag1 = false;
					return flag1;
				}
			} // end of (RHSSet.itemAt(i) == 1)
		} // end of for (int i=0; i<RHSSet.items(i).length;i++)
		return flag1;
	}

	public void setOutputItemSets(boolean flag) {
		m_outputItemSets = flag;
	}

	public boolean getOutputItemSets() {
		return m_outputItemSets;
	}

	public String outputItemSetsTipText() {
		return "If enabled the itemsets are output as well.";
	}

	public String eventTypeCareTipText() {
		return "If enabled then FIS/Rules are generated with the event type information.";
	}

	public String myNumRulesTipText() {
		return "Choose the population size from which the rules will be generated.";
	}

	public boolean getEventTypeCare() {
		return m_eventTypeCare;
	}

	public void setEventTypeCare(boolean flag) {
		m_eventTypeCare = flag;
	}

	public boolean getNoName() {
		return m_noName;
	}

	public String insertNoNameActivityTipText() {
		return "If enabled then an activity 'noname' with type 'notype' is inserted in the log and the rules and FIS are derived from this log.";
	}

	public void setNoNameActivity(boolean flag) {
		m_noName = flag;
	}

}

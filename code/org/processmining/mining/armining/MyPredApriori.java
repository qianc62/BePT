package org.processmining.mining.armining;

import weka.associations.PredictiveApriori;
import weka.associations.ItemSet;
import java.util.ArrayList;
import weka.core.Utils;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.AuditTrailEntry;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * <p>
 * Title: MyPredApriori
 * </p>
 * 
 * <p>
 * Description: MyPredApriori extends PredictiveApriori()
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

public class MyPredApriori extends PredictiveApriori {

	protected boolean p_eventTypeCare;
	protected boolean p_noName;

	public MyPredApriori() {
	}

	public ArrayList<String> get_m_allTheRules() {
		ArrayList<String> myRulesArray = new ArrayList<String>();
		ArrayList<String> myRulesArray2 = new ArrayList<String>();
		ArrayList myPremiseList = new ArrayList();
		ArrayList myConsList = new ArrayList();
		ArrayList<String> myPremise;
		ArrayList<String> myCons;
		TreeSet ruleIndToKeep = new TreeSet();
		TreeSet ruleIndToRemove = new TreeSet();
		ArrayList<String> myLHS = new ArrayList<String>();
		ArrayList<String> myRHS = new ArrayList<String>();
		ArrayList myLHSList = new ArrayList();
		ArrayList myRHSList = new ArrayList();

		for (int j = 0; j < m_allTheRules[0].size(); j++) {
			ItemSet tempSet = (ItemSet) m_allTheRules[0].elementAt(j);
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
			tempSet = (ItemSet) m_allTheRules[1].elementAt(j);
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

		// //**************************************************************************************
		// //original rules from Weka can be obtained from this commented
		// portion
		// System.out.println("original rules from Weka");
		// for (int q = 0; q < myPremiseList.size(); q++) {
		// String accValue2 = Utils.doubleToString(((Double) m_allTheRules[2].
		// elementAt(q)).doubleValue(), 5);
		//
		// String numberRules2 = (Utils.doubleToString((double) q + 1,
		// (int) (Math.log(m_numRules) / Math.log(10) + 1), 0) + ". ");
		//
		// Object myA2[] = ((ArrayList<String>) myPremiseList.get(q)).toArray();
		// String tempString2 = "";
		// for (int r = 0; r < myA2.length; r++) {
		// String individualPremise2 = (String) myA2[r];
		// String newPremiseName2 = "";
		// if (individualPremise2.contains("=yes")) {
		// newPremiseName2 = individualPremise2.split("\\=")[0];
		// } else {
		// if (individualPremise2.contains("=no")) {
		// newPremiseName2 = "!" + individualPremise2.split("\\=")[0];
		// }
		// }
		// tempString2 += newPremiseName2;
		// if (r != myA2.length - 1) {
		// tempString2 += ",";
		// }
		// }
		//
		// tempString2 += "=>";
		// Object myB2[] = ((ArrayList<String>) myConsList.get(q)).toArray();
		// for (int s = 0; s < myB2.length; s++) {
		// String individualPremise2 = (String) myB2[s];
		//
		// String newPremiseName2 = "";
		// if (individualPremise2.contains("=yes")) {
		// newPremiseName2 = individualPremise2.split("\\=")[0];
		// } else {
		// if (individualPremise2.contains("=no")) {
		// newPremiseName2 = "!" + individualPremise2.split("\\=")[0];
		// }
		// }
		// tempString2 += newPremiseName2;
		// if (s != myB2.length - 1) {
		// tempString2 += ", ";
		// }
		// }
		// myRulesArray2.add(numberRules2 + tempString2 + " " + " (Accuracy: " +
		// accValue2 + ")");
		// }
		// System.out.println("original rules from weka: " + myRulesArray2);

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
				// if L1=L2, first if statement
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
				else { // main else
					// first if in this else-if L1 is subset of L2
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
			String accValue = Utils.doubleToString(((Double) m_allTheRules[2]
					.elementAt(k)).doubleValue(), 5);

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
				tempString += newConsName;

				if (i != myB.length - 1) {
					tempString += ", ";
				}
			}
			if (tempString.contains(",noname")) {
				newTempString = tempString.replace(",noname", " ");
				myRulesArray.add(numberRules + newTempString + "   "
						+ "(accuracy: " + accValue + ")");
			} else {
				if (tempString.contains(", noname")) {
					newTempString = tempString.replace(", noname", " ");
					myRulesArray.add(numberRules + newTempString + "   "
							+ "(accuracy: " + accValue + ")");
				} else {
					if (tempString.contains("noname,")) {
						newTempString = tempString.replace("noname,", " ");
						myRulesArray.add(numberRules + newTempString + "   "
								+ "(accuracy: " + accValue + ")");
					} else {
						if (tempString.contains("noname, ")) {
							newTempString = tempString.replace("noname, ", " ");
							myRulesArray.add(numberRules + newTempString
									+ "   " + "(accuracy: " + accValue + ")");
						} else {
							myRulesArray.add(numberRules + tempString + "   "
									+ "(accuracy: " + accValue + ")");
						}
					}
				}
			}
		}
		return myRulesArray;
	}

	public static JScrollPane newDisplay(ArrayList<String> myList) {
		JTextArea ta = new JTextArea();
		ta.setSize(20, 10);
		ta.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(ta);
		for (int i = 0; i < myList.size(); i++) {
			ta.append("\n");
			ta.append(myList.get(i) + "\n");
		}
		return scrollPane;
	}

	public Boolean checkFISInRule(ProcessInstance pi, int ruleIndex) {
		return false;
	}

	// Check whether a particular association rule is present in a log trace, do
	// not consider event type information
	public Boolean checkForRule(ProcessInstance pi, int ruleIndex) {
		// flag for the complete rule
		boolean flag1 = true;
		// flag for individual element of a rule
		boolean flag2;
		String myPremise = "";
		String myCons = "";

		ItemSet LHSSet = (ItemSet) m_allTheRules[0].elementAt(ruleIndex);

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
						String ateDetail = (String) individualAte.getElement();
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
						String ateDetail = (String) individualAte.getElement();

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

		ItemSet RHSSet = (ItemSet) m_allTheRules[1].elementAt(ruleIndex);

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
						String ateDetail = (String) individualAte.getElement();
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
						String ateDetail = (String) individualAte.getElement();
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

	// Check whether a particular association rule is present in a log trace, do
	// not consider event type information
	public Boolean checkRuleWithEventCare(ProcessInstance pi, int ruleIndex) {
		// flag for the complete rule
		boolean flag1 = true;
		// flag for individual element of a rule
		boolean flag2;
		String myPremise = "";
		String myCons = "";

		ItemSet LHSSet = (ItemSet) m_allTheRules[0].elementAt(ruleIndex);

		for (int i = 0; i < LHSSet.items().length; i++) {
			System.out.println(LHSSet.itemAt(i));
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

		ItemSet RHSSet = (ItemSet) m_allTheRules[1].elementAt(ruleIndex);

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

	public String p_eventTypeCareTipText() {
		return "If enabled then we care about Event Type information as well.";
	}

	public boolean p_getEventTypeCare() {
		return p_eventTypeCare;
	}

	public void setEventTypeCare(boolean flag) {
		p_eventTypeCare = flag;
	}

	public boolean getNoName() {
		return p_noName;
	}

	public String insertNoNameActivityTipText() {
		return "If enabled then an activity 'noname' with type 'notype' is inserted in the log and the rules and FIS are derived from this log.";
	}

	public void setNoNameActivity(boolean flag) {
		p_noName = flag;
	}
}

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.heuristics;

import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.util.*;
import java.io.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class MarkingHeuristicsNet {

	private HashMap[] marking = null;
	private MappingToSubsets auxMapping = null;
	private int startPlace = 0;
	private int endPlace = 0;
	private HeuristicsNet hNet = null;
	private int numberTokens = 0;
	private int size = 0;
	private CombinationTasksToFire bestCombination = null;
	private Random generator = null;

	private HNSubSet possiblyEnabledElements = new HNSubSet();

	private static final int ROOT_TASK_ID = -1; // invalid index used as root.

	/**
	 * Build an initial marking for a given heuristics net.
	 * 
	 * @param net
	 *            an enhanced heuristics net to which the initial marking must
	 *            be built.
	 * @throws java.lang.NullPointerException
	 *             whenever the net has disconnected elements. An element is
	 *             disconnected when its INPUT or OUTPUT set is null.
	 */
	public MarkingHeuristicsNet(HeuristicsNet net, Random generator)
			throws NullPointerException {

		this.generator = generator;
		size = net.size();
		hNet = net;
		// checking if all input and output sets are DIFFERENT from null...
		for (int i = 0; i < size; i++) {
			if (hNet.getInputSet(i) == null || hNet.getOutputSet(i) == null) {
				throw new NullPointerException(
						"Net has disconnected elements!!");
			}

		}
		createMarking();
	}

	/**
	 * Creates an initial marking.
	 */
	private void createMarking() {

		HNSet set = null;

		marking = null;
		marking = new HashMap[size];

		// Building the marking data structure. This structure is an array of
		// hashmaps.
		// For a given hashmap, we have:
		// - Key: Integer = element
		// - Value: HashMap = subsets + the number of tokens associated to them
		// The idea is to keep track of which tasks fired and the number of
		// tokens at their output subsets.
		// Initially, the number of tokens is 0.
		for (int i = 0; i < size; i++) {

			set = hNet.getOutputSet(i);
			HashMap map = new HashMap();
			for (int j = 0; j < set.size(); j++) {
				map.put(set.get(j), new Integer(0));
			}
			marking[i] = map;
		}

		auxMapping = new MappingToSubsets(hNet);
		this.reset();
	}

	/**
	 * Sets the marking to the initial marking.
	 */
	public void reset() {

		Iterator outElements = null;
		HNSubSet outSubset = null;

		numberTokens = 1; // in the source place
		startPlace = 1;
		endPlace = 0;

		// initially, only the single start tasks is enabled
		possiblyEnabledElements = new HNSubSet();
		for (int i = 0; i < hNet.getStartTasks().size(); i++) {
			possiblyEnabledElements.add(hNet.getStartTasks().get(i));
		}
		for (int i = 0; i < size; i++) {
			outElements = marking[i].keySet().iterator();
			while (outElements.hasNext()) {
				outSubset = (HNSubSet) outElements.next();
				marking[i].put(outSubset, new Integer(0));
			}
			outElements = null;
		}

	}

	/**
	 * Prints the currents marking.
	 * 
	 * @return String containing the current marking.
	 */
	public String printCurrentMarking() {

		StringBuffer sb = new StringBuffer();

		sb.append("\n start place = ").append(this.startPlace);
		sb.append("\n end place = ").append(this.endPlace);

		for (int i = 0; i < size; i++) {

			sb.append("\n====>  task = ").append(i)
					.append("\n       Marking: ");
			sb.append("\n       ").append(marking[i].toString());
		}

		return sb.toString();
	}

	/**
	 * Removes one token from the marked output places (subsets) of the tasks in
	 * <i>tasks</i> that contain <i>element</i>.
	 * 
	 * @param element
	 *            element to fire
	 * @param tasks
	 *            whose output places point to element
	 */
	private void removeTokensOutputPlaces(int element, HNSubSet tasks) {

		HNSubSet subset = null;
		HNSet subsets = null;

		Integer tokens = null;
		int taskToFire;
		int task;

		// Checking if element is a start element
		if (hNet.getInputSet(element).size() == 0) {
			if (startPlace > 0) {
				startPlace--;
				numberTokens--;
			}
		} else {
			taskToFire = element;
			for (int iTasks = 0; iTasks < tasks.size(); iTasks++) {
				task = tasks.get(iTasks);
				subsets = auxMapping.getRelatedSubsets(taskToFire, task);
				if (subsets != null) {
					for (int iSubsets = 0; iSubsets < subsets.size(); iSubsets++) {
						subset = subsets.get(iSubsets);
						tokens = getNumberTokens(task, subset);
						if (tokens.intValue() > 0) {
							decreaseNumberTokens(task, subset);
							numberTokens--;
						}
					}
				}
			}
		}
	}

	private int decreaseNumberTokens(int inElement, HNSubSet outElement) {
		int numTokens = 0;

		numTokens = getNumberTokens(inElement, outElement).intValue();
		numTokens--;
		setNumberTokens(inElement, outElement, new Integer(numTokens));

		return numTokens;
	}

	/**
	 * @throws java.lang.NullPointerException
	 *             when inElement is not in the hash. I.e, inElement has OUT=[]
	 * @throws java.lang.NumberFormatException
	 *             when outElement does not belong to the OUT set of inElement.
	 */
	private Integer getNumberTokens(int inElement, HNSubSet outElement)
			throws NullPointerException, NumberFormatException {
		return (Integer) marking[inElement].get(outElement);
	}

	/**
	 * @throws java.lang.NullPointerException
	 *             when inElement is not in the hash. I.e, inElement has OUT=[]
	 * @throws java.lang.NumberFormatException
	 *             when outElement does not belong to the OUT set of inElement.
	 */
	private void setNumberTokens(int inElement, HNSubSet outElement,
			Integer numTokens) throws NullPointerException,
			NumberFormatException {
		marking[inElement].put(outElement, numTokens);
	}

	/**
	 * The number of places that are marked in the current net
	 * 
	 * @return number of marked-places
	 */
	public int getNumberTokens() {
		return numberTokens;
	}

	public boolean properlyCompleted() {
		if (endPlace == 1 && numberTokens == 1) {
			return true;
		}
		return false;
	}

	public boolean endPlace() {
		return (endPlace > 0);
	}

	public int getNumTokensEndPlace() {
		return endPlace;
	}

	/**
	 * Adds tokens to the output places of a given element
	 * 
	 * @param element
	 *            element to fire
	 */
	private void addTokensOutputPlaces(int element) {

		/*
		 * Pseudo-code: 1. Retrieve all elements in the OUT set of 'element' 2.
		 * Increase the number of tokens for every output element
		 */

		HNSet set = null;

		// update global counter for number of tokens
		// note that OR-situations count as a single token.

		set = hNet.getOutputSet(element);
		if (set.size() == 0) { // element is connected to the end place
			numberTokens++;
			endPlace++;
		} else {
			numberTokens += set.size();
		}

		// update marking...
		for (int iSet = 0; iSet < set.size(); iSet++) {
			increaseNumberTokens(element, set.get(iSet));

		}

	}

	private int increaseNumberTokens(int inElement, HNSubSet subset) {
		int numTokens = 0;

		numTokens = getNumberTokens(inElement, subset).intValue();
		numTokens++;
		setNumberTokens(inElement, subset, new Integer(numTokens));

		return numTokens;
	}

	/**
	 * Fires a element even if it is not enabled. When the element has
	 * duplicates, it looks ahead to set which duplicate to fire.
	 * <p>
	 * <b>Note:</b> The element MUST be in the net.
	 * 
	 * @param element
	 *            element to be fired.
	 * @param pi
	 *            process instance where the element to be fired is.
	 * @param elementPositionInPi
	 *            element position.
	 * @return int number of tokens that needed to be added to fire this
	 *         element.
	 */
	public int fire(int element, ProcessInstance pi, int elementPositionInPi) {

		int addedTokens = 0;
		int elementDuplicates;

		if ((hNet.getReverseDuplicatesMapping()[element]).size() == 1) {
			elementDuplicates = hNet.getReverseDuplicatesMapping()[element]
					.get(0);
		} else {
			// identify which duplicate to fire
			HNSubSet duplicates = hNet.getReverseDuplicatesMapping()[element]
					.deepCopy();

			// getting the duplicates that are enabled
			for (int i = 0; i < duplicates.size(); i++) {

				if (!isEnabled(duplicates.get(i))) {
					duplicates.remove(duplicates.get(i));
				}

			}

			if (duplicates.size() > 0) {
				if (duplicates.size() == 1) {
					elementDuplicates = duplicates.get(0);
				} else {
					// getting the output tasks of the duplicates. These output
					// are used to
					// look ahead at the process instance
					HNSubSet unionMappedToATEsCode = getAllOutputElementsOfDuplicates(duplicates);
					AuditTrailEntryList ATEntriesList = pi
							.getAuditTrailEntryList();
					// advancing the pointer in the ATEntries till the current
					// element + 1

					AuditTrailEntry ATEntry;
					int elementInATE = -1;
					for (int i = elementPositionInPi + 1; i < ATEntriesList
							.size(); i++) {
						try {
							ATEntry = ATEntriesList.get(i);
							elementInATE = this.hNet.getLogEvents()
									.findLogEventNumber(ATEntry.getElement(),
											ATEntry.getType());
							if (unionMappedToATEsCode.contains(elementInATE)) {
								break;
							}

						} catch (IOException ex) {
							break;
						} catch (IndexOutOfBoundsException ex) {
							break;
						}
					}
					elementDuplicates = identifyDuplicateToFire(duplicates,
							elementInATE);
				}
			} else {
				// because no duplicate is enabled, a random one is chosen to
				// fire...
				elementDuplicates = (hNet.getReverseDuplicatesMapping()[element])
						.get(generator.nextInt(hNet
								.getReverseDuplicatesMapping()[element].size()));
			}

		}

		bestCombination = findBestSetTasks(elementDuplicates);
		addedTokens += bestCombination.getNumberMissingTokens();
		removeTokensOutputPlaces(elementDuplicates, bestCombination.getTasks());
		addTokensOutputPlaces(elementDuplicates);
		addToPossiblyEnabledElements(elementDuplicates);

		// registering the firing of element...
		hNet.increaseElementActualFiring(elementDuplicates,
				MethodsForWorkflowLogDataStructures
						.getNumberSimilarProcessInstances(pi));
		// updating the arc usage for the individual...
		hNet.increaseArcUsage(bestCombination.getElementToFire(),
				bestCombination.getTasks(), MethodsForWorkflowLogDataStructures
						.getNumberSimilarProcessInstances(pi));

		return addedTokens;
	}

	private int identifyDuplicateToFire(HNSubSet duplicates, int elementInATE) {

		HNSubSet candidateDuplicates = new HNSubSet();
		HNSubSet allElements;

		for (int i = 0; i < duplicates.size(); i++) {
			allElements = hNet.getAllElementsOutputSet(duplicates.get(i));
			for (int j = 0; j < allElements.size(); j++) {
				if (elementInATE == hNet.getDuplicatesMapping()[allElements
						.get(j)]) {
					candidateDuplicates.add(duplicates.get(i));
					break;
				}

			}

		}

		if (candidateDuplicates.size() <= 0) {
			candidateDuplicates = duplicates; // we can choose any of the tasks
			// because none has
			// followers in the process instance...
		}

		return candidateDuplicates.get(generator.nextInt(candidateDuplicates
				.size()));
	}

	private HNSubSet getAllOutputElementsOfDuplicates(HNSubSet duplicates) {
		// Returns the union set of the output tasks of the tasks in
		// "duplicates".
		// The returned union set has already the codes mapped to the ATEs in
		// the log!
		HNSubSet union = new HNSubSet();
		HNSubSet allElements;

		for (int i = 0; i < duplicates.size(); i++) {
			allElements = hNet.getAllElementsOutputSet(duplicates.get(i));
			for (int j = 0; j < allElements.size(); j++) {
				union.add(hNet.getDuplicatesMapping()[allElements.get(j)]);
			}
		}

		return union;
	}

	private void addToPossiblyEnabledElements(int element) {
		HNSubSet subset = hNet.getAllElementsOutputSet(element);
		for (int i = 0; i < subset.size(); i++) {
			possiblyEnabledElements.add(subset.get(i));
		}
	}

	private CombinationTasksToFire findBestSetTasks(int element) {
		CombinationTasksToFire bCombination = null;
		CombinationTasksToFire combination = null;
		HNSubSet noTokensFromTasks = null;
		HNSubSet treatedTasks = null;
		HNSet inputSet = null;
		HNSet temp_inputSet = null;

		HNSubSet subset = null;

		int numberMissingTokens = 0;
		int rootTask = ROOT_TASK_ID;

		bCombination = new CombinationTasksToFire();

		inputSet = hNet.getInputSet(element);

		if (inputSet.size() == 0) {
			if (startPlace <= 0) {
				numberMissingTokens++; // one token is missing
			}
		} else {
			// inputSubset is not empty. Search for tasks that "have tokens" to
			// element

			noTokensFromTasks = getTasksWithEmptyOutputPlaces(element);

			// >>>>>>>>>>>>>>>>>> Hint!!! I think that's why I don't run into
			// problems...
			// /// Idea -> shrink the subsets without using a temp variable, get
			// / the size before shrinking, shrink them, reorder the set and
			// /remove the empty set (do this via a method in the class
			// /HNSet, get the new size. This is the number of missing tokens.

			// make a copy to avoid destroying the original net
			inputSet = inputSet.deepCopy();
			temp_inputSet = new HNSet();
			// removing the tasks whose output subsets that contain element are
			// empty
			for (int iInputSubsets = 0; iInputSubsets < inputSet.size(); iInputSubsets++) {
				subset = inputSet.get(iInputSubsets);
				subset.removeAll(noTokensFromTasks);
				if (subset.size() == 0) {
					numberMissingTokens += 1;
				} else {
					temp_inputSet.add(subset);
				}
			}
			inputSet = temp_inputSet;
			// retrieving the best combination of tasks that can fire to enable
			// element

			if (inputSet.size() > 0) {
				combination = new CombinationTasksToFire();
				treatedTasks = new HNSubSet();
				bCombination = findBestCombination(bCombination, inputSet,
						rootTask, combination, treatedTasks);
			}
		}

		bCombination.setElementToFire(element);
		bCombination.setTokens(bCombination.getNumberMissingTokens()
				+ numberMissingTokens);

		return bCombination;
	}

	private CombinationTasksToFire findBestCombination(
			CombinationTasksToFire bCombination, HNSet inputSet, int rootTask,
			CombinationTasksToFire combination, HNSubSet treatedTasks) {

		int task = -1;
		HNSet alreadyMarkedPlaces = null;
		HNSet temp_inputSet = null;
		HNSubSet noTokensFromTasks = null;
		HNSubSet subset = null;

		if ((bCombination.getTasks().size() == 0)
				|| (bCombination.getNumberMissingTokens() > combination
						.getNumberMissingTokens())) {
			if (rootTask != ROOT_TASK_ID) {
				alreadyMarkedPlaces = getAlreadyMarkedPlaces(inputSet, rootTask);
				noTokensFromTasks = HNSet.getUnionSet(alreadyMarkedPlaces);
				inputSet.removeAll(alreadyMarkedPlaces);
				combination.getTasks().add(rootTask);
			}

			if (inputSet.size() == 0) {
				bCombination = combination.copy();
			} else {

				// akam: I stoppe here - 10/07/2005
				if (rootTask != ROOT_TASK_ID) {
					temp_inputSet = new HNSet();
					for (int iInputSet = 0; iInputSet < inputSet.size(); iInputSet++) {
						subset = inputSet.get(iInputSet);
						subset.removeAll(noTokensFromTasks);
						subset.removeAll(treatedTasks);
						if (subset.size() == 0) {
							combination.setTokens(combination
									.getNumberMissingTokens() + 1);
						} else {
							temp_inputSet.add(subset);
						}
					}
					inputSet = temp_inputSet;
				}

				for (int iInputSet = 0; iInputSet < inputSet.size(); iInputSet++) {
					subset = inputSet.get(iInputSet);
					while (subset.size() > 0) {
						task = subset.get(generator.nextInt(subset.size()));
						bCombination = findBestCombination(bCombination,
								inputSet.deepCopy(), task, combination.copy(),
								treatedTasks.deepCopy());
						treatedTasks.add(task);
						subset.remove(task);

					}
				}
			}
		}

		return bCombination;
	}

	private HNSet getAlreadyMarkedPlaces(HNSet set, int task) {
		HNSet markedPlaces = null;
		HNSubSet subset = null;

		markedPlaces = new HNSet();

		for (int iSet = 0; iSet < set.size(); iSet++) {
			subset = set.get(iSet);
			if (subset.contains(task)) {
				markedPlaces.add(subset);
			}

		}

		return markedPlaces;

	}

	private HNSubSet getTasksWithEmptyOutputPlaces(int task) {

		HNSubSet tasksEmptyOutPlaces;
		HNSubSet inputTasks;

		int inputTask;
		HNSet outputSubsets;

		inputTasks = hNet.getAllElementsInputSet(task);

		tasksEmptyOutPlaces = new HNSubSet();

		for (int iInputTasks = 0; iInputTasks < inputTasks.size(); iInputTasks++) {
			inputTask = inputTasks.get(iInputTasks);
			outputSubsets = auxMapping.getRelatedSubsets(task, inputTask);
			if (outputSubsets != null) {
				if (!allSubsetsAreMarked(inputTask, outputSubsets)) {
					tasksEmptyOutPlaces.add(inputTask);
				}
			} else {
				tasksEmptyOutPlaces.add(inputTask);
			}
		}

		return tasksEmptyOutPlaces;

	}

	private boolean allSubsetsAreMarked(int inputTask, HNSet outputSet) {

		HNSubSet subset = null;

		for (int iOutputSet = 0; iOutputSet < outputSet.size(); iOutputSet++) {
			subset = outputSet.get(iOutputSet);
			if (((Integer) marking[inputTask].get(subset)).intValue() <= 0) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Checks if a element is enabled at the current marking. <p> <b>Note:</b>
	 * The element MUST be in the Element.
	 * 
	 * @param element element/transition to check
	 * 
	 * @return true if the element is enabled at the current marking. false
	 * otherwise.
	 */
	public boolean isEnabled(int element) {
		if (hNet.getInputSet(element) == null) {
			return false;
		}

		if (hNet.getInputSet(element).size() == 0) {
			if (startPlace < 1) {
				return false;
			}
		} else {
			bestCombination = findBestSetTasks(element);
			if (bestCombination.getNumberMissingTokens() > 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the current enabled elements at the current marking.
	 */

	public HNSubSet getCurrentEnabledElements() {

		int element = -1;
		HNSubSet disabledElements = null;

		disabledElements = new HNSubSet();
		for (int iPossiblyEnabledElements = 0; iPossiblyEnabledElements < possiblyEnabledElements
				.size(); iPossiblyEnabledElements++) {
			element = possiblyEnabledElements.get(iPossiblyEnabledElements);
			if (!isEnabled(element)) {
				disabledElements.add(element);
			}
		}

		possiblyEnabledElements.removeAll(disabledElements);

		return possiblyEnabledElements;

	}

	/**
	 * Returns the current number of enabled elements at the current marking.
	 */
	public int getCurrentNumEnabledElements() {
		return getCurrentEnabledElements().size();
	}

}

class MappingToSubsets {

	private HNSet[][] mapping = null;

	public MappingToSubsets(HeuristicsNet hn) {
		buildMapping(hn);
	}

	private void buildMapping(HeuristicsNet hn) {

		HNSet outputSubsets;
		HNSubSet outputSubset;
		int taskInSubset;

		mapping = new HNSet[hn.size()][hn.size()];

		for (int task = 0; task < hn.size(); task++) {
			outputSubsets = hn.getOutputSet(task);
			for (int iOutputSubsets = 0; iOutputSubsets < outputSubsets.size(); iOutputSubsets++) {
				// inserting for every element
				outputSubset = outputSubsets.get(iOutputSubsets);
				for (int iSubset = 0; iSubset < outputSubset.size(); iSubset++) {
					taskInSubset = outputSubset.get(iSubset);
					insertInMapping(taskInSubset, task, outputSubset);
				}
			}
		}
	}

	private void insertInMapping(int taskInSubset, int task, HNSubSet subset) {

		HNSet hSet = mapping[taskInSubset][task];

		if (hSet == null) {
			hSet = new HNSet();
		}

		hSet.add(subset);
		mapping[taskInSubset][task] = hSet;

	}

	/**
	 * Returns the output subsets of <i>task</i> that contains
	 * <i>taskInSubset</i>
	 * 
	 * @return a HashSet with the subsets. Returns null if there are no subsets.
	 */
	public HNSet getRelatedSubsets(int taskInSubset, int task) {
		return mapping[taskInSubset][task];
	}
}

class CombinationTasksToFire {

	private HNSubSet tasks = null;
	private int numberMissingTokens = 0;
	private int elementToFire = -1;

	public CombinationTasksToFire() {
		tasks = new HNSubSet();
	}

	/**
	 * This method sets the element that is going to fire for this combination
	 * of tasks. This method is useful when dealing with duplicate tasks because
	 * we precisely know which duplicate requires this combination. Note: This
	 * method is useful when counting arc usage for an individual!
	 */

	public void setElementToFire(int element) {
		elementToFire = element;
	}

	/**
	 * This method returns the element that is going to fire for this
	 * combination of tasks. This method is useful when dealing with duplicate
	 * tasks because we precisely know which duplicate requires this
	 * combination. Note: This method is useful when counting arc usage for an
	 * individual!
	 * 
	 * @return -1 if no element has been set.
	 */
	public int getElementToFire() {
		return elementToFire;
	}

	/**
	 * Returns the stored set of tasks;
	 */
	public HNSubSet getTasks() {
		return tasks;
	}

	/**
	 * Returns the stored number of missing tokens;
	 */
	public int getNumberMissingTokens() {
		return numberMissingTokens;
	}

	/**
	 * Sets stored the set of task.
	 */

	public void setTasks(HNSubSet newSetOftasks) {
		tasks = newSetOftasks;
	}

	/**
	 * Sets the stored number of missing tokens. The number of missing tokens
	 * ranges from 0 to maximum integer. So, if <i>newNumberMissingTokens</i> is
	 * smaller than 0, the number of missing tokens is automatically set to 0.
	 * 
	 * @return the number of missing tokens.
	 */

	public int setTokens(int newNumberMissingTokens) {
		if (newNumberMissingTokens > 0) {
			numberMissingTokens = newNumberMissingTokens;
		} else {
			numberMissingTokens = 0;
		}
		return numberMissingTokens;
	}

	/**
	 * Makes a deep copy of this object.
	 */
	protected Object clone() {

		CombinationTasksToFire clone = new CombinationTasksToFire();

		clone.setTokens(this.getNumberMissingTokens());
		clone.setTasks(this.getTasks().deepCopy());

		return clone;
	}

	/**
	 * Returns the casted Object from the method clone().
	 */
	public CombinationTasksToFire copy() {
		return (CombinationTasksToFire) this.clone();

	}

	/**
	 * Returns the String representation of CombinationTasksToFire. The format
	 * is "[set of tasks to fire] : number_of_missing_tokens"
	 * 
	 * @return
	 */
	public String toString() {
		return this.tasks.toString() + " : " + this.numberMissingTokens;
	}
}

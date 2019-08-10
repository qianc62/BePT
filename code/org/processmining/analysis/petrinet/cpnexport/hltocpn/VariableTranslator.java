package org.processmining.analysis.petrinet.cpnexport.hltocpn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.petrinet.cpnexport.CpnVarAndType;
import org.processmining.analysis.petrinet.cpnexport.HLToCPNTranslator;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;

/**
 * Facilities to create CPN variables.
 */
public class VariableTranslator {

	/**
	 * the object containing the high-level information referring to this
	 * process (i.e., the source of the translation process).
	 */
	private HLPetriNet highLevelPN;
	/**
	 * Saves the cpn vars that are used as probability variable
	 */
	private HashSet<CpnVarAndType> cpnVarsForProbDeps = new HashSet<CpnVarAndType>();

	/**
	 * Default constructor.
	 * 
	 * @param hlpn
	 *            the high-level Petri net
	 */
	public VariableTranslator(HLPetriNet hlpn) {
		highLevelPN = hlpn;
	}

	/**
	 * Resets data structures to be used when writing to file.
	 */
	public void reset() {
		cpnVarsForProbDeps = new HashSet<CpnVarAndType>();
	}

	/**
	 * Returns the cpn variable and type for the given group
	 * 
	 * @param g
	 *            Group the group for which we want to obtain the cpn variable
	 *            and type
	 * @return CpnVarAndType the cpn variable and type for the given group
	 */
	public static CpnVarAndType getCpnVarForGroup(HLGroup g) {
		String groupName = CpnUtils.getCpnValidName(g.toString());
		return new CpnVarAndType(groupName, groupName.toUpperCase());
	}

	/**
	 * Returns the cpn variable and type that has to represent the variable for
	 * the data attributes in cpn
	 * 
	 * @return CpnVarAndType the variable in cpn for the data attributes
	 */
	public static CpnVarAndType getCpnVarForDataAttributes() {
		return new CpnVarAndType(HLToCPNTranslator.cpnVarNameForDataAttributes,
				HLToCPNTranslator.cpnColorSetNameForDataAttributes);
	}

	/**
	 * Returns the cpn variable and type that has to represent the variable for
	 * the modified data attributes in cpn
	 * 
	 * @return CpnVarAndType the variable in cpn for the modified data
	 *         attributes
	 */
	public static CpnVarAndType getCpnVarForModifiedDataAttributes() {
		return new CpnVarAndType(
				HLToCPNTranslator.cpnVarNameForModifiedDataAttributes,
				HLToCPNTranslator.cpnColorSetNameForDataAttributes);
	}

	/**
	 * Returns the cpn variable and type for the variable that has to represent
	 * the given data attribute
	 * 
	 * @param attrib
	 *            HLAttribute the data attribute for which we want to obtain a
	 *            cpn compliant variable
	 * @return CpnVarAndType the variable and type in cpn for the given data
	 *         attribute
	 */
	public static CpnVarAndType getCpnVarForDataAttribute(HLAttribute attrib) {
		return new CpnVarAndType(attrib.getName(), attrib.getName());
	}

	/**
	 * Returns the cpn variable and type for the variable that has to represent
	 * the case id in cpn.
	 * 
	 * @return CpnVarAndType the cpn variable and type for the case id
	 */
	public static CpnVarAndType getCpnVarForCaseId() {
		return new CpnVarAndType(HLToCPNTranslator.cpnVarNameForCaseId,
				HLToCPNTranslator.cpnColorSetNameForCaseId);
	}

	/**
	 * Returns the cpn variable and type for the variable that has to represent
	 * the start time stamp of a case
	 * 
	 * @return CpnVarAndType the cpn variable and type for the start case
	 */
	public static CpnVarAndType getCpnVarForStartCase() {
		return new CpnVarAndType(HLToCPNTranslator.cpnVarNameForStartCase,
				HLToCPNTranslator.cpnColorSetNameForStartCase);
	}

	/**
	 * Returns the cpn variable and type for the variable that has to represent
	 * the probability dependencies in cpn.
	 * 
	 * @param probDep
	 *            the condition for which the probability dependencies are
	 *            requested
	 * @return CpnVarAndType the cpn variable and type for the probability
	 *         dependency
	 */
	public CpnVarAndType getCpnVarForProbDep(HLCondition probDep) {
		HLActivity targetAct = probDep.getTarget();
		// when the targetNode is involved in more than one decisionpoint then
		// the
		// cpn var name should also contain the name of the source node. The
		// same for
		// the colorset.
		String nameProbVar = HLToCPNTranslator.cpnVarNameForProbDep;
		if (highLevelPN.getHLProcess().getChoicesForTargetActivity(
				targetAct.getID()).size() > 1) {
			ModelGraphVertex place = highLevelPN
					.findModelGraphVertexForChoice(probDep.getChoice().getID());
			nameProbVar = HLToCPNTranslator.cpnVarNameForProbDep
					+ place.getIdentifier();
		}
		CpnVarAndType cpnVar = new CpnVarAndType(nameProbVar,
				HLToCPNTranslator.cpnColorSetNameForProbDep);
		cpnVarsForProbDeps.add(cpnVar);
		return cpnVar;
	}

	/**
	 * Returns the cpn variables for the probability variables
	 * 
	 * @return HashSet
	 */
	public HashSet<CpnVarAndType> getCpnVarsForProbDep() {
		return cpnVarsForProbDeps;
	}

	/**
	 * Returns the cpn variable and type for the variable in cpn that has to
	 * represent the group that contains each resource that can be found in the
	 * high level process and each high level activity
	 * 
	 * @return CpnVarAndType the cpn variable and type for the group that
	 *         contains each resource
	 */
	public static CpnVarAndType getCpnVarForGroupAllResources() {
		return new CpnVarAndType(
				HLToCPNTranslator.cpnVarNameForGroupAllResources,
				HLToCPNTranslator.cpnColorSetNameForGroupAllResources);
	}

	/**
	 * Returns a list with the cpn vars for all groups, except the cpn-var that
	 * has to represent all resources.
	 * 
	 * @return ArrayList
	 */
	public ArrayList<CpnVarAndType> getCpnVarsForGroups() {
		ArrayList<CpnVarAndType> returnCpnVars = new ArrayList<CpnVarAndType>();
		Iterator<HLGroup> groups = highLevelPN.getHLProcess().getGroups()
				.listIterator();
		while (groups.hasNext()) {
			HLGroup group = groups.next();
			returnCpnVars.add(getCpnVarForGroup(group));
		}
		return returnCpnVars;
	}

}

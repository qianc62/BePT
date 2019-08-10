package org.processmining.analysis.petrinet.cpnexport.hltocpn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.processmining.analysis.petrinet.cpnexport.BoolColorSet;
import org.processmining.analysis.petrinet.cpnexport.ColoredPetriNet;
import org.processmining.analysis.petrinet.cpnexport.CpnColorSet;
import org.processmining.analysis.petrinet.cpnexport.EnumeratedColorSet;
import org.processmining.analysis.petrinet.cpnexport.HLToCPNTranslator;
import org.processmining.analysis.petrinet.cpnexport.IntegerColorSet;
import org.processmining.analysis.petrinet.cpnexport.ProductColorSet;
import org.processmining.analysis.petrinet.cpnexport.RecordColorSet;
import org.processmining.analysis.petrinet.cpnexport.StringColorSet;
import org.processmining.analysis.petrinet.cpnexport.SubSetColorSet;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;

/**
 * Facilities to create CPN colorsets.
 */
public class ColorSetTranslator {

	/**
	 * the object containing the high-level information referring to this
	 * process (i.e., the source of the translation process).
	 */
	private HLPetriNet highLevelPN;
	/**
	 * the CPN-like object that needs to be filled with the translated
	 * information (i.e., the target of the translation process).
	 */
	protected ColoredPetriNet simulatedPN;

	/**
	 * Saves the product color sets that can be needed for some places
	 */
	private HashSet<ProductColorSet> productColorSetsForPlaces = new HashSet<ProductColorSet>();

	/**
	 * Constructor.
	 * 
	 * @param hlpn
	 *            the high-level petri net
	 * @param cpn
	 *            the colored petr net
	 */
	public ColorSetTranslator(HLPetriNet hlpn, ColoredPetriNet cpn) {
		highLevelPN = hlpn;
		simulatedPN = cpn;
	}

	/**
	 * Resets data structures to be used when writing to file.
	 */
	public void reset() {
		productColorSetsForPlaces = new HashSet<ProductColorSet>();
	}

	/**
	 * Returns the corresponding cpn color set representation for the given
	 * group
	 * 
	 * @param g
	 *            Group the group for which we want to obtain the corresponding
	 *            cpn color set representation
	 * @return CpnColorSet the corresponding cpn color set representation
	 */
	public static SubSetColorSet getColorSetGroup(HLGroup g) {
		SubSetColorSet cset = new SubSetColorSet(g.toString().toUpperCase(),
				"STRING");
		List<HLResource> resources = g.getResources();
		if (resources != null) {
			Iterator<HLResource> resourceIt = resources.iterator();
			while (resourceIt.hasNext()) {
				HLResource resource = resourceIt.next();
				cset.addPossibleValue(resource.getName());
			}
		}
		return cset;
	}

	/**
	 * Returns the cpn color set representation for all groups that can be found
	 * in the highlevelprocess and in the highlevelactivities
	 * 
	 * @return ArrayList the cpn color representations for all groups. If no
	 *         groups exists, then an empty <code>ArrayList</code> is returned
	 */
	public ArrayList<SubSetColorSet> getColorSetsGroups() {
		ArrayList returnArrayList = null;
		List groupsList = highLevelPN.getHLProcess().getGroups();
		if (groupsList != null) {
			returnArrayList = new ArrayList<SubSetColorSet>();
			Iterator<HLGroup> groups = groupsList.iterator();
			while (groups.hasNext()) {
				HLGroup group = groups.next();
				CpnColorSet colorSetForGroup = getColorSetGroup(group);
				returnArrayList.add(colorSetForGroup);
			}
		}
		return returnArrayList;
	}

	/**
	 * Returns the cpn color set representation for the color set in cpn that
	 * has to represent all separate data attributes
	 * 
	 * @return CpnColorSet the cpn color set representation for the data types
	 *         that represents all separate data attributes. If no data
	 *         attibutes exist in the process, <code>null</code> is returned
	 */
	public CpnColorSet getColorSetDataAttributes() {
		RecordColorSet cset = new RecordColorSet(
				HLToCPNTranslator.cpnColorSetNameForDataAttributes);
		if (highLevelPN.getHLProcess().getAttributes() != null) {
			Iterator dataAttributes = highLevelPN.getHLProcess()
					.getAttributes().iterator();
			while (dataAttributes.hasNext()) {
				HLAttribute attribute = (HLAttribute) dataAttributes.next();
				cset.addRecord(attribute.getName(), attribute.getName()
						.toUpperCase());
			}
		}
		return cset;
	}

	/**
	 * Returns the cpn color set representation for this data attribute
	 * 
	 * @param attrib
	 *            HLAttribute the data attribute for which we want to obtain the
	 *            cpn color set
	 * @return CpnColorSet the color set representation for this data attribute.
	 */
	public static CpnColorSet getColorSetDataAttribute(HLAttribute attrib) {
		CpnColorSet cset = null;
		if (attrib instanceof HLNominalAttribute) {
			cset = new EnumeratedColorSet(attrib.getName().toUpperCase());
			ListIterator possibleValues = ((HLNominalAttribute) attrib)
					.getPossibleValues().getValues().listIterator();
			while (possibleValues.hasNext()) {
				String value = (String) possibleValues.next();
				((EnumeratedColorSet) cset).addPossibleValue(value);
			}
		} else if (attrib instanceof HLNumericAttribute) {
			// integer color set
			cset = new IntegerColorSet(attrib.getName().toUpperCase());
		} else if (attrib instanceof HLBooleanAttribute) {
			// boolean color set
			cset = new BoolColorSet(attrib.getName().toUpperCase());
		}
		return cset;
	}

	/**
	 * Return the corresponding cpn color set representation for each separate
	 * data attribute that can be found in the high level process and the high
	 * level activities
	 * 
	 * @return ArrayList the cpn color representations for each separate data
	 *         attributes. If no data attributes exist, an empty
	 *         <code>ArrayList</code> is returned
	 */
	public ArrayList<CpnColorSet> getColorSetsSeparateDataAttributes() {
		ArrayList returnArrayList = new ArrayList<CpnColorSet>();
		if (this.highLevelPN.getHLProcess().getAttributes() != null) {
			Iterator dataAttributes = this.highLevelPN.getHLProcess()
					.getAttributes().iterator();
			while (dataAttributes.hasNext()) {
				HLAttribute attribute = (HLAttribute) dataAttributes.next();
				returnArrayList.add(getColorSetDataAttribute(attribute));
			}
		} else {
			returnArrayList = null;
		}
		return returnArrayList;
	}

	/**
	 * Returns the cpn color set representation for the case id.
	 * 
	 * @return CpnColorSet the cpn color set representation for the case id.
	 */
	public static IntegerColorSet getColorSetCaseID() {
		return new IntegerColorSet(HLToCPNTranslator.cpnColorSetNameForCaseId);
	}

	/**
	 * Returns the cpn color set representation for the start case.
	 * 
	 * @return StringColorSet the cpn color set representation for the start
	 *         case.
	 */
	public static StringColorSet getColorSetStartCase() {
		return new StringColorSet(HLToCPNTranslator.cpnColorSetNameForStartCase);
	}

	/**
	 * Returns the cpn color set representation that has to be assigned to the
	 * resources places in the cpn net.
	 * 
	 * @return CpnColorSet the color set representation for all resources
	 */
	public SubSetColorSet getColorSetForResourcesPlace() {
		SubSetColorSet cset = new SubSetColorSet(
				HLToCPNTranslator.cpnColorSetNameForGroupAllResources, "STRING");
		List<HLResource> resources = highLevelPN.getHLProcess().getResources();
		if (resources != null) {
			Iterator<HLResource> res = resources.iterator();
			while (res.hasNext()) {
				HLResource resource = res.next();
				cset.addPossibleValue(resource.getName());
			}
		}
		return cset;
	}

	/**
	 * Returns the product of two or more cpn color sets. In case that the
	 * ColoredPetriNet is written to a cpn-file the color set (that is the
	 * product of two or more cpn color sets) will occur as a color set in this
	 * file
	 * 
	 * @param cSets
	 *            ArrayList the color sets for which we want to obtain the color
	 *            that is the product of them
	 * @return CpnColorSet the color set which is the product of two or more
	 *         color sets
	 */
	public ProductColorSet productCpnColorSet(ArrayList<CpnColorSet> cSets) {
		ProductColorSet cset = new ProductColorSet();
		String nameCset = "";
		Iterator<CpnColorSet> colorSets = cSets.iterator();
		while (colorSets.hasNext()) {
			CpnColorSet colorSet = colorSets.next();
			cset.addNameColorSet(colorSet.getNameColorSet().toUpperCase());
			nameCset = nameCset + "x"
					+ colorSet.getNameColorSet().toUpperCase();
		}
		// remove the first x from nameCset
		nameCset = nameCset.replaceFirst("x", "");
		cset.setName(nameCset);
		if (cSets.size() > 1) {
			productColorSetsForPlaces.add(cset);
		}
		return cset;
	}

	/**
	 * Returns all the product color sets that has been produced by
	 * <code>ProductCpnColorSet</code>
	 * 
	 * @see ProductCpnColorSet
	 * @return Set a set with product color sets
	 */
	public Set<ProductColorSet> getProductColorSetsForPlaces() {
		return productColorSetsForPlaces;
	}

}

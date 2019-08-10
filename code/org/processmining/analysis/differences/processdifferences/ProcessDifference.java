package org.processmining.analysis.differences.processdifferences;

import java.util.Iterator;
import java.util.Set;

import org.processmining.framework.models.ModelGraphVertex;

public class ProcessDifference {

	public static final int TYPE_ITERATIVE = 1;
	public static final int TYPE_ADD_DEPENDENCIES = 11;
	public static final int TYPE_DIFF_DEPENDENCIES = 12;
	public static final int TYPE_DIFF_MOMENTS = 21;
	public static final int TYPE_DIFF_CONDITIONS = 22;
	public static final int TYPE_ADD_CONDITIONS = 23;
	public static final int TYPE_SKIPPED_ACTIVITY = 31;
	public static final int TYPE_DIFF_START = 51;

	public static final int DIRECTION_UNI = 1;
	public static final int DIRECTION_PROVMORE = 2;
	public static final int DIRECTION_REQMORE = 3;

	private Set<ModelGraphVertex> ofTransitionsfromProvBehaviour;
	private Set<ModelGraphVertex> ofTransitionsfromReqBehaviour;

	private Set<ModelGraphVertex> involvedTransitionsfromProvBehaviour;
	private Set<ModelGraphVertex> involvedTransitionsfromReqBehaviour;

	private String characteristicProvBehaviour;
	private String characteristicReqBehaviour;
	private int differenceType;
	private int differenceDirection;

	public ProcessDifference(
			Set<ModelGraphVertex> ofTransitionsfromProvBehaviour,
			Set<ModelGraphVertex> ofTransitionsfromReqBehaviour,
			Set<ModelGraphVertex> involvedTransitionsfromProvBehaviour,
			Set<ModelGraphVertex> involvedTransitionsfromReqBehaviour,
			String characteristicProvBehaviour,
			String characteristicReqBehaviour, int differenceType,
			int differenceDirection) {
		this.ofTransitionsfromProvBehaviour = ofTransitionsfromProvBehaviour;
		this.ofTransitionsfromReqBehaviour = ofTransitionsfromReqBehaviour;
		this.involvedTransitionsfromProvBehaviour = involvedTransitionsfromProvBehaviour;
		this.involvedTransitionsfromReqBehaviour = involvedTransitionsfromReqBehaviour;
		this.characteristicProvBehaviour = characteristicProvBehaviour;
		this.characteristicReqBehaviour = characteristicReqBehaviour;
		this.differenceType = differenceType;
		this.differenceDirection = differenceDirection;
	}

	public String getCharacteristicProvBehaviour() {
		return characteristicProvBehaviour;
	}

	public void setCharacteristicProvBehaviour(
			String characteristicProvBehaviour) {
		this.characteristicProvBehaviour = characteristicProvBehaviour;
	}

	public String getCharacteristicReqBehaviour() {
		return characteristicReqBehaviour;
	}

	public void setCharacteristicReqBehaviour(String characteristicReqBehaviour) {
		this.characteristicReqBehaviour = characteristicReqBehaviour;
	}

	public int getDifferenceDirection() {
		return differenceDirection;
	}

	public void setDifferenceDirection(int differenceDirection) {
		this.differenceDirection = differenceDirection;
	}

	public int getDifferenceType() {
		return differenceType;
	}

	public void setDifferenceType(int differenceType) {
		this.differenceType = differenceType;
	}

	public Set<ModelGraphVertex> getinvolvedTransitionsfromProvBehaviour() {
		return involvedTransitionsfromProvBehaviour;
	}

	public void setinvolvedTransitionsfromProvBehaviour(
			Set<ModelGraphVertex> involvedTransitionsfromProvBehaviour) {
		this.involvedTransitionsfromProvBehaviour = involvedTransitionsfromProvBehaviour;
	}

	public Set<ModelGraphVertex> getinvolvedTransitionsfromReqBehaviour() {
		return involvedTransitionsfromReqBehaviour;
	}

	public void setinvolvedTransitionsfromReqBehaviour(
			Set<ModelGraphVertex> involvedTransitionsfromReqBehaviour) {
		this.involvedTransitionsfromReqBehaviour = involvedTransitionsfromReqBehaviour;
	}

	public Set<ModelGraphVertex> getOfTransitionsfromProvBehaviour() {
		return ofTransitionsfromProvBehaviour;
	}

	public void setOfTransitionsfromProvBehaviour(
			Set<ModelGraphVertex> ofTransitionsfromProvBehaviour) {
		this.ofTransitionsfromProvBehaviour = ofTransitionsfromProvBehaviour;
	}

	public Set<ModelGraphVertex> getOfTransitionsfromReqBehaviour() {
		return ofTransitionsfromReqBehaviour;
	}

	public void setOfTransitionsfromReqBehaviour(
			Set<ModelGraphVertex> ofTransitionsfromReqBehaviour) {
		this.ofTransitionsfromReqBehaviour = ofTransitionsfromReqBehaviour;
	}

	public String transitionNames(Set<ModelGraphVertex> trSet) {
		String result = "";
		for (Iterator<ModelGraphVertex> i = trSet.iterator(); i.hasNext();) {
			result += "'" + i.next().getIdentifier().replace("\\n", " ") + "'"; // getname
			// changed
			// intos
			// get
			// identifier
			// by
			// Marian
			if (i.hasNext()) {
				result += ", ";
			}
		}
		return result;
	}

	public String toString() {
		String result = "Difference " + differenceType + ": ";

		switch (differenceType) {
		case TYPE_SKIPPED_ACTIVITY:
			result += "Skipped activity; ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour has no equivalent in required behaviour";
			} else {
				result += "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour has no equivalent in provided behaviour";
			}
			break;
		case TYPE_DIFF_START:
			result += "Different start of process; ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour have ";
			} else {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour have ";
			}
			result += "an additional possibility to start immediately as opposed to ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour"
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour";
			} else {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour"
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour";
			}
			break;
		case TYPE_ITERATIVE:
			result += "Iterative versus once-off; ";
			result += (differenceDirection == DIRECTION_PROVMORE) ? "provided behaviour "
					: "required behaviour ";
			result += "has the additional (normalized) cycle ";
			result += (differenceDirection == DIRECTION_PROVMORE) ? characteristicProvBehaviour
					: characteristicReqBehaviour;
			break;
		case TYPE_ADD_DEPENDENCIES:
			result += "Additional dependencies; ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour have ";
			} else {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour have ";
			}
			result += "additional dependencies with respect to ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour on "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour on ";
				result += transitionNames(involvedTransitionsfromProvBehaviour);
			} else {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour on "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour on ";
				result += transitionNames(involvedTransitionsfromReqBehaviour);
			}
			break;
		case TYPE_DIFF_DEPENDENCIES:
			result += "Different dependencies; ";

			result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromProvBehaviour)
					+ " from provided behaviour has "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromProvBehaviour)
							+ " from provided behaviour have ";
			result += "additional dependencies with respect to ";
			result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromReqBehaviour)
					+ " from required behaviour on "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromReqBehaviour)
							+ " from required behaviour on ";
			result += transitionNames(involvedTransitionsfromProvBehaviour);

			result += " and ";

			result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromReqBehaviour)
					+ " from required behaviour has "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromReqBehaviour)
							+ " from required behaviour have ";
			result += "additional dependencies with respect to ";
			result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromProvBehaviour)
					+ " from provided behaviour on "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromProvBehaviour)
							+ " from provided behaviour on ";
			result += transitionNames(involvedTransitionsfromReqBehaviour);

			break;
		case TYPE_DIFF_MOMENTS:
			result += "Different moments; ";

			result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromProvBehaviour)
					+ " from provided behaviour occurs "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromProvBehaviour)
							+ " from provided behaviour occur ";
			result += "at a different moment than ";
			result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromReqBehaviour)
					+ " from required behaviour"
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromReqBehaviour)
							+ " from required behaviour";

			break;
		case TYPE_DIFF_CONDITIONS:
			result += "Different conditions; ";

			result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromProvBehaviour)
					+ " from provided behaviour has "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromProvBehaviour)
							+ " from provided behaviour have ";
			result += "additional conditions with respect to ";
			result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromReqBehaviour)
					+ " from required behaviour "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromReqBehaviour)
							+ " from required behaviour ";
			result += "characterized by ";
			result += characteristicProvBehaviour;

			result += " and ";

			result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromReqBehaviour)
					+ " from required behaviour has "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromReqBehaviour)
							+ " from required behaviour have ";
			result += "additional conditions with respect to ";
			result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
					+ transitionNames(ofTransitionsfromProvBehaviour)
					+ " from provided behaviour "
					: "equivalent transitions "
							+ transitionNames(ofTransitionsfromProvBehaviour)
							+ " from provided behaviour ";
			result += "characterized by ";
			result += characteristicReqBehaviour;

			break;
		case TYPE_ADD_CONDITIONS:
			result += "Additional conditions; ";

			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour have ";
			} else {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour has "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour have ";
			}
			result += "additional conditions with respect to ";
			if (differenceDirection == DIRECTION_PROVMORE) {
				result += (ofTransitionsfromReqBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromReqBehaviour)
						+ " from required behaviour "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromReqBehaviour)
								+ " from required behaviour ";
				result += "characterized by ";
				result += this.characteristicProvBehaviour;
			} else {
				result += (ofTransitionsfromProvBehaviour.size() == 1) ? "transition "
						+ transitionNames(ofTransitionsfromProvBehaviour)
						+ " from provided behaviour "
						: "equivalent transitions "
								+ transitionNames(ofTransitionsfromProvBehaviour)
								+ " from provided behaviour ";
				result += "characterized by ";
				result += this.characteristicReqBehaviour;
			}
			break;
		default:
			break;
		}

		return result;
	}
}

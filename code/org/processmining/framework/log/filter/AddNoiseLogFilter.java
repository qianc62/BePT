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

package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyFloat;
import org.processmining.framework.util.GUIPropertyLong;
import org.w3c.dom.Node;

/**
 * <p>
 * Title: AddNoiseLogFilter
 * </p>
 * 
 * <p>
 * Description: This class adds noise to a log. There are 5 possible noise
 * types: <i>head</i>, <i>body</i>, <i>tail</i>, <i>swap</i> and <i>remove</i>.
 * The noise types <i>head</i>, <i>body</i> and <i>tail</i> respectively remove
 * <i>at most!</i> the first, second or third 1/3 of a process instance. The
 * noise type <i>swap</i> randomly swaps two audit trail entries (or tasks) in a
 * process instance. The noise type <i>remove</i> randomly removes one audit
 * trail entry (or task) from a process instance.
 * </p>
 * <p>
 * NOTE: This class does not treat input logs in which the process instances are
 * grouped!
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class AddNoiseLogFilter extends LogFilter {

	// noise types
	public static final String HEAD = "Remove Head";
	public static final String BODY = "Remove Body";
	public static final String TAIL = "Remove Tail";
	public static final String SWAP = "Swap Tasks";
	public static final String REMOVE = "Remove Task";

	public static final int HEAD_INDEX = 0;
	public static final int BODY_INDEX = 1;
	public static final int TAIL_INDEX = 2;
	public static final int SWAP_INDEX = 3;
	public static final int REMOVE_INDEX = 4;

	public static final String[] NOISE_TYPES = { HEAD, BODY, TAIL, SWAP, REMOVE };
	private boolean[] selectedNoiseTypes = { true, true, true, true, true };
	private int[] selectedNoiseTypesIndeces;

	private float noisePercentage = 0.05f;
	private long seed = 123456789;

	public AddNoiseLogFilter() {
		super(LogFilter.MODERATE, "Add Noise Log Filter");
		selectedNoiseTypesIndeces = getSelectedNoiseTypesIndeces(selectedNoiseTypes);

	}

	private AddNoiseLogFilter(boolean[] selectedNoiseTypes,
			float noisePercentage, long seed) {
		super(LogFilter.MODERATE, "Add Noise Log Filter");
		this.selectedNoiseTypes = selectedNoiseTypes;
		this.noisePercentage = noisePercentage / 100;
		this.seed = seed;
		selectedNoiseTypesIndeces = getSelectedNoiseTypesIndeces(selectedNoiseTypes);

	}

	private static int[] getSelectedNoiseTypesIndeces(boolean[] types) {
		Vector v = new Vector();
		int[] selectedNoiseTypesIndeces;

		for (int i = 0; i < types.length; i++) {
			if (types[i]) {
				v.add(new Integer(i));
			}
		}

		selectedNoiseTypesIndeces = new int[v.size()];

		for (int i = 0; i < selectedNoiseTypesIndeces.length; i++) {
			selectedNoiseTypesIndeces[i] = ((Integer) v.get(i)).intValue();
		}

		return selectedNoiseTypesIndeces;
	}

	private int safeNextInt(Random r, int maxInt) {
		return r.nextInt(maxInt > 0 ? maxInt : 1);
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	protected boolean doFiltering(ProcessInstance instance) {

		Random r = new Random(seed + instance.toString().hashCode());

		if (r.nextDouble() < noisePercentage
				&& selectedNoiseTypesIndeces.length > 0) {

			AuditTrailEntryList entries = instance.getAuditTrailEntryList();
			double oneThird = entries.size() / 3.0;

			switch (selectedNoiseTypesIndeces[safeNextInt(r,
					selectedNoiseTypesIndeces.length)]) {

			case HEAD_INDEX:

				// first, advancing the pointer so that the whole head is not
				// removed in all cases
				// the head goes from position 0 until position ((1/3) - 1)
				int startToRemoveHeadAtPosition = safeNextInt(r, (int) oneThird);

				// defining the size of the head to be remove. The whole head
				// should not be removed in all cases.
				int sizeSubstringToRemoveFromHead = safeNextInt(r,
						((int) oneThird) - startToRemoveHeadAtPosition + 1); // because
				// it
				// is
				// exclusive...

				for (int index = 0; index < sizeSubstringToRemoveFromHead; index++) {
					try {
						entries.remove(startToRemoveHeadAtPosition);
					} catch (IOException ex) {
						Message.add(ex.getMessage(), Message.ERROR);
						break;
					} catch (IndexOutOfBoundsException ex) {
						Message.add(ex.getMessage(), Message.ERROR);
						break;
					}
				}

				break;

			case BODY_INDEX:

				// remove the second 1/3 (at most!) of the trace

				// first, advancing the pointer so that the whole body is not
				// removed in all cases
				// the body goes from position 1/3 until position ((2/3) - 1)
				int startToRemoveBodyAtPosition = safeNextInt(r, (int) oneThird)
						+ (int) oneThird;

				// the actual removal happens here
				int sizeSubstringToRemoveFromBody = safeNextInt(
						r,
						(int) oneThird
								- (startToRemoveBodyAtPosition - (int) oneThird)
								+ 1);

				for (int index = 0; index < sizeSubstringToRemoveFromBody; index++) {
					try {
						entries.remove(startToRemoveBodyAtPosition);
					} catch (IOException ex1) {
						Message.add(ex1.getMessage(), Message.ERROR);
						break;
					} catch (IndexOutOfBoundsException ex1) {
						Message.add(ex1.getMessage(), Message.ERROR);
						break;
					}

				}
				break;

			case TAIL_INDEX:

				// removing the last 1/3 (at most!) of the trace

				// first randomly advancing the pointer, so that the whole tail
				// may be removed or not
				int startToRemoveTailAtPosition = safeNextInt(r, (int) oneThird)
						+ (int) (2 * oneThird);

				// the actual removal happens here
				int sizeSubstringToRemoveFromTail = safeNextInt(
						r,
						(int) oneThird
								- (startToRemoveTailAtPosition - (int) (2 * oneThird))
								+ 1);
				for (int index = 0; index < sizeSubstringToRemoveFromTail; index++) {
					try {
						entries.remove(startToRemoveTailAtPosition);
					} catch (IOException ex2) {
						Message.add(ex2.getMessage(), Message.ERROR);
						break;
					} catch (IndexOutOfBoundsException ex2) {
						Message.add(ex2.getMessage(), Message.ERROR);
						break;
					}
				}
				break;

			case SWAP_INDEX:

				// removing the first task
				int indexFirstTaskToSwap = safeNextInt(r, entries.size());
				int indexSecondTaskToSwap = safeNextInt(r, entries.size());
				AuditTrailEntry firstTaskToSwap = null;
				AuditTrailEntry secondTaskToSwap = null;

				if (indexFirstTaskToSwap != indexSecondTaskToSwap) {
					// it makes sense to swap...

					try {
						// first, getting the tasks...
						firstTaskToSwap = entries.get(indexFirstTaskToSwap);
						secondTaskToSwap = entries.get(indexSecondTaskToSwap);

						// second, replacing the tasks...
						entries.replace(secondTaskToSwap, indexFirstTaskToSwap);
						entries.replace(firstTaskToSwap, indexSecondTaskToSwap);

					} catch (IOException ex4) {
						break;
					} catch (IndexOutOfBoundsException ex4) {
						break;
					}
				}
				break;

			case REMOVE_INDEX:

				int taskToRemove = safeNextInt(r, entries.size());
				try {
					entries.remove(taskToRemove);
				} catch (IOException ex3) {
					Message.add(ex3.getMessage(), Message.ERROR);
					break;
				} catch (IndexOutOfBoundsException ex3) {
					Message.add(ex3.getMessage(), Message.ERROR);
					break;
				}

				break;
			}
		}

		return !instance.isEmpty();
	}

	public boolean[] getSelectedNoiseTypes() {
		return selectedNoiseTypes;

	}

	public void setSelectedNoiseTypes(int index, boolean value) {
		selectedNoiseTypes[index] = value;

	}

	protected boolean thisFilterChangesLog() {
		return true;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, AddNoiseLogFilter.this) {

			GUIPropertyFloat noisePercentageProperty;
			GUIPropertyLong seedProperty;

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				// noise percentage
				noisePercentageProperty = new GUIPropertyFloat(
						"Noise Percentage",
						"Set the percentage of noise to be inserted in the log.",
						noisePercentage * 100, 0.0f, 100.0f, 1.0f);

				// noise types
				NoiseTypesCheckBoxPanel noiseTypeCheckBoxes = new NoiseTypesCheckBoxPanel(
						AddNoiseLogFilter.this);

				// seed to use for the random number
				seedProperty = new GUIPropertyLong(
						"Seed",
						"Set the seed to be used during the random insertion of noise in the log.",
						seed, Long.MIN_VALUE, Long.MAX_VALUE);

				JPanel settingsPanel = new JPanel(new BorderLayout());
				settingsPanel.add(noisePercentageProperty.getPropertyPanel(),
						BorderLayout.NORTH);
				settingsPanel.add(noiseTypeCheckBoxes, BorderLayout.CENTER);
				settingsPanel.add(seedProperty.getPropertyPanel(),
						BorderLayout.SOUTH);

				return settingsPanel;
			}

			public LogFilter getNewLogFilter() {
				return new AddNoiseLogFilter(selectedNoiseTypes,
						noisePercentageProperty.getValue(), seedProperty
								.getValue());
			}
		};
	}

	protected String getHelpForThisLogFilter() {
		return "This filter <i>randomly</i> adds noise to the log."
				+ " There are 5 possible noise types:"
				+ " <i>remove head</i>, <i>remove body</i>, <i>remove tail</i>, <i>swap tasks</i> and <i>remove task</i>. The"
				+ "  noise types <i>remove head</i>, <i>remove body</i> and <i>remove tail</i> respectively remove"
				+ "  <i>at most</i> the first, second or third 1/3 of a process instance (or trace or case). The noise type <i>swap</i> randomly"
				+ " swaps two audit trail entries (or tasks) in a process instance. The noise type <i>remove</i> randomly"
				+ "  removes one audit trail entry (or task) from a process instance. </p>"
				+ " <p> <b>IMPORTANT!!!</b> The filter does not distinguishes "
				+ "between grouped and ungrouped logs. Thus, please use it with ungrouped logs only.";

	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<selectedNoiseTypes>"
				+ toStringBooleanArray(selectedNoiseTypes)
				+ "</selectedNoiseTypes>\n");
		output.write("<noisePercentage>" + noisePercentage
				+ "</noisePercentage>\n");
		output.write("<seed>" + seed + "</seed>\n");
	}

	private String toStringBooleanArray(boolean[] array) {
		String s = "";
		for (int i = 0; i < array.length; i++) {
			s += array[i] + " ";
		}
		return s;
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		for (int i = logFilterSpecifcNode.getChildNodes().getLength() - 1; i >= 0; i--) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("selectedNoiseTypes")) {
				selectedNoiseTypes = fromStringToBooleanArray(n.getFirstChild()
						.getNodeValue());
			} else if (n.getNodeName().equals("noisePercentage")) {
				noisePercentage = Float.parseFloat(n.getFirstChild()
						.getNodeValue());
			} else if (n.getNodeName().equals("seed")) {
				seed = Long.parseLong(n.getFirstChild().getNodeValue());
			}
		}
	}

	private boolean[] fromStringToBooleanArray(String str) {
		StringTokenizer st = new StringTokenizer(str);
		boolean[] array = new boolean[st.countTokens()];
		for (int i = 0; i < array.length; i++) {
			String s = st.nextToken().trim();
			array[i] = new Boolean(s).booleanValue();
		}
		return array;
	}
}

class NoiseTypesCheckBoxPanel extends JPanel implements ItemListener {

	private JCheckBox[] noiseTypesCheckBoxes;
	private AddNoiseLogFilter parentClass;

	public NoiseTypesCheckBoxPanel(AddNoiseLogFilter parentClass) {
		super(new BorderLayout());
		this.parentClass = parentClass;
		noiseTypesCheckBoxes = new JCheckBox[AddNoiseLogFilter.NOISE_TYPES.length];
		for (int i = 0; i < noiseTypesCheckBoxes.length; i++) {
			noiseTypesCheckBoxes[i] = new JCheckBox(
					AddNoiseLogFilter.NOISE_TYPES[i]);
			noiseTypesCheckBoxes[i].addItemListener(this);
			noiseTypesCheckBoxes[i].setSelected(this.parentClass
					.getSelectedNoiseTypes()[i]);
		}

		// Put the check boxes in a column in a panel
		JPanel checkPanel = new JPanel(new GridLayout(0, 1));
		for (int i = 0; i < noiseTypesCheckBoxes.length; i++) {
			checkPanel.add(noiseTypesCheckBoxes[i]);
		}

		JLabel noiseTypesLabel = new JLabel(" Noise Types");
		noiseTypesLabel.setVerticalAlignment(JLabel.TOP);
		noiseTypesLabel
				.setToolTipText("Select the types of noise to be inserted in the log.");
		add(noiseTypesLabel, BorderLayout.WEST);
		add(checkPanel, BorderLayout.EAST);

	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		int index;
		for (index = 0; index < noiseTypesCheckBoxes.length; index++) {
			if (source == noiseTypesCheckBoxes[index]) {
				break;
			}
		}
		// Now that we know which button was pushed, find out
		// whether it was selected or deselected.
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			parentClass.setSelectedNoiseTypes(index, false);
		} else {
			parentClass.setSelectedNoiseTypes(index, true);
		}

	}

}

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

package org.processmining.framework.models.petrinet.algorithms.logReplay;

import java.util.*;
import java.util.Iterator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.*;

/**
 * A configuration object represents some unit or category within the respective
 * analysis domain. It is structured in a hierarchical way, that is, a category
 * may contain arbitrary levels of sub-categories, which are automatically
 * layouted according to this scheme in the {@link #getOptionsPanel
 * getOptionsPanel} method, which can be used to display the available settings
 * to the user before actual analysis takes place. <br>
 * Furthermore the configuration object may be passed to the respective
 * {@link AnalysisMethod#analyse analyse} passed in order to only take those
 * measurements that are needed for the analysis options chosen by the user. <br>
 * Typically, each leaf node represents a concrete metric and can be associated
 * to some kind of analysis method, which needs to be applied in order to obtain
 * the information needed for calculating the metric. Note that the hierarchical
 * structure represented by an object of this class does not necessarily
 * correspond to a grouping according to the kind of analysis method, but rather
 * forms a semantic scheme. <br>
 * Note that the public methods are <code>synchronized</code> as the
 * configuration object may be accessed by concurrent threads executing the
 * different analysis methods.
 * 
 * @author Anne Rozinat
 */
public class AnalysisConfiguration implements Cloneable {

	// // semantic attributes
	private String myName = "";
	private String myToolTip = "";
	private String myDescription = "";
	private boolean mySelectionState = true; // whether this option has been
	// chosen or not
	private ArrayList<AnalysisConfiguration> myChildConfigurations = new ArrayList<AnalysisConfiguration>(); // subordinate
	// options

	/**
	 * The results needed to initialize the GUI (only used for top level
	 * categories). A set of AnalysisResult objects.
	 */
	private Set<AnalysisResult> myResultObjects = new HashSet<AnalysisResult>();

	/**
	 * The methods that are requested to provide their results (only used for
	 * top level categories). A set of AnalysisMethodEnum objects.
	 */
	private HashSet<AnalysisMethodEnum> myRequestedMethods = new HashSet<AnalysisMethodEnum>();

	/**
	 * The method for which this option is relevant, that is, it would either
	 * collect or not collect data related to this option (only used for
	 * metrics).
	 * 
	 * TODO - merge this field with the one above (check whether top level
	 * categories indeed need to be treated differently here
	 */
	private HashSet<AnalysisMethodEnum> myAnalysisMethods = new HashSet<AnalysisMethodEnum>();

	// // GUI-related attributes
	private JCheckBox myCheckbox;
	private JEditorPane myEditorPane;
	private String myBackgroundColor_HEX;
	private Color myBackgroundColor_OBJ;

	/** used by top level configuration objects to store the analysis result GUI */
	private AnalysisGUI myResultPanel;

	/**
	 * Default constructor.
	 */
	public AnalysisConfiguration() {
		/**
		 * @todo anne: in the future use configuration manager for making the
		 *       selection state persistent
		 */
	}

	// ///////////////// INSTANCE LEVEL METHODS
	// /////////////////////////////////

	/**
	 * Adds another result object to the list of analysis results held by this
	 * category. This only used for top level categories as they get a seperate
	 * tab in the results frame, and need to initialize their GUI classes with
	 * the analysis results obtained.
	 * 
	 * @param result
	 *            the new result object
	 */
	public synchronized void addAnalysisResult(AnalysisResult result) {
		// if result object of that type is already stored: replace it
		// TODO - check if this works, otherwise make copy of analysis
		// configuration
		// before recalculating
		Iterator<AnalysisResult> it = myResultObjects.iterator();
		AnalysisResult foundResultOfSameType = null;
		while (it.hasNext()) {
			AnalysisResult current = it.next();
			if (result != null
					&& current.getClass().getName().equals(
							result.getClass().getName()) == true) {
				foundResultOfSameType = current;
				break;
			}
		}
		if (foundResultOfSameType != null) {
			myResultObjects.remove(foundResultOfSameType);
		}

		myResultObjects.add(result);
	}

	/**
	 * Gets the results needed to initialize the GUI for this top level
	 * category.
	 * 
	 * @return a list of AnalysisResult objects
	 */
	public synchronized Set<AnalysisResult> getResultObjects() {
		return myResultObjects;
	}

	/**
	 * Gets the results of the child configurations.
	 * 
	 * @return the set of analysis result objects
	 */
	public synchronized Set<AnalysisResult> getChildrenResultObjects() {
		Iterator<AnalysisConfiguration> children = this
				.getChildConfigurations().iterator();
		Set<AnalysisResult> results = new HashSet<AnalysisResult>();
		while (children.hasNext()) {
			results.addAll(children.next().getResultObjects());
		}
		return results;
	}

	/**
	 * Registers this top level category to be provided by the results from the
	 * specified analysis method. Synchronized access guaranteed as each
	 * {@link AnalysisMethod AnalysisMethod} is executed in a seperate thread
	 * and their {@link CustomSwingworker CustomSwingworker} asks for registered
	 * configuration objects in order to add the respective
	 * {@link AnalysisResult AnalysisResult} object.
	 * 
	 * @see #hasRegisteredFor
	 * @param methodEnum
	 *            the AnalysisMethodEnum of the analysis method to register for
	 */
	public synchronized void addRequestedMethod(AnalysisMethodEnum methodEnum) {
		myRequestedMethods.add(methodEnum);
	}

	/**
	 * Determines whether this category has been registered to be provided with
	 * the results for the given analysis method. Synchronized access guaranteed
	 * as each {@link AnalysisMethod AnalysisMethod} is executed in a seperate
	 * thread and their {@link CustomSwingworker CustomSwingworker} asks for
	 * registered configuration objects in order to add the respective
	 * {@link AnalysisResult AnalysisResult} object.
	 * 
	 * @see #addRequestedMethod
	 * @param methodEnum
	 *            the identifier of the analysis method
	 * @return <code>true</code> if the results of this method have been
	 *         requested, <code>false</code> otherwise
	 */
	public synchronized boolean hasRegisteredFor(AnalysisMethodEnum methodEnum) {
		if (myRequestedMethods.contains(methodEnum)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a new analysis option as a sub category of this kind of analysis.
	 * 
	 * @param newChild
	 *            the new sub-ordinate analysis option
	 */
	public synchronized void addChildConfiguration(
			AnalysisConfiguration newChild) {
		myChildConfigurations.add(newChild);
	}

	// ///////////////// HIERARCHY LEVEL METHODS
	// ////////////////////////////////

	/**
	 * Recursively retrieves all those enabled analysis options that require the
	 * given type of analysis method to be performed.
	 * 
	 * @param enabledOptions
	 *            the list of the enabled options found for that analysis method
	 *            so far
	 * @param analysisMethod
	 *            the type of analysis method for which the list of enabled
	 *            options is requested
	 * @return ArrayList the extended list, containing all the enabled child
	 *         options which require that analysis method
	 */
	public synchronized ArrayList<AnalysisConfiguration> getEnabledOptionsForAnalysisMethod(
			ArrayList<AnalysisConfiguration> enabledOptions,
			AnalysisMethodEnum analysisMethod) {
		if (this.isSelected() == true) {
			// check whether current node requires the given method type
			if (myAnalysisMethods.contains(analysisMethod)) {
				enabledOptions.add(this);
			}
			// only trace children if parent node is selected
			Iterator allChilds = myChildConfigurations.iterator();
			while (allChilds.hasNext()) {
				AnalysisConfiguration currentChild = (AnalysisConfiguration) allChilds
						.next();
				enabledOptions = currentChild
						.getEnabledOptionsForAnalysisMethod(enabledOptions,
								analysisMethod);
			}
		}
		return enabledOptions;
	}

	/**
	 * Traverses the configuration option hierarchy in order to obtain the
	 * maximum number of levels in the tree.
	 * 
	 * @param currentLevel
	 *            indicates the current level of traversal (initially 0 should
	 *            be passed)
	 * @param currentMaxDepth
	 *            indicates the maximum depth so far encountered in the
	 *            recursive process
	 * @return <code>0</code> if there is no element below this one,
	 *         <code>1</code> if there are subordinate elements that do not have
	 *         children, <code>2</code> if at least one of the subordinates has
	 *         another child element which does not have any children, ...
	 */
	public synchronized int getTreeDepth(int currentLevel, int currentMaxDepth) {
		if (this.getChildConfigurations().size() == 0) {
			return currentMaxDepth;
		} else {
			// child nodes are one level lower already
			int newLevel = currentLevel + 1;
			int myMaxDepth = newLevel;
			Iterator allChilds = myChildConfigurations.iterator();
			while (allChilds.hasNext()) {
				AnalysisConfiguration currentChild = (AnalysisConfiguration) allChilds
						.next();
				int newDepth = currentChild.getTreeDepth(newLevel, myMaxDepth);
				if (newDepth > myMaxDepth) {
					myMaxDepth = newDepth;
				}
			}
			if (myMaxDepth > currentMaxDepth) {
				return myMaxDepth;
			} else {
				return currentMaxDepth;
			}
		}
	}

	/**
	 * Traverses the configuration option hierarchy in order to determine the
	 * number of all subordinate child nodes (including leaf nodes).
	 * 
	 * @return the total amount of (direct and indirect) child nodes
	 */
	public synchronized int getNumberOfChildNodes() {
		// add up children
		int number = 0;
		Iterator allChilds = myChildConfigurations.iterator();
		while (allChilds.hasNext()) {
			AnalysisConfiguration currentChild = (AnalysisConfiguration) allChilds
					.next();
			number = number + currentChild.getNumberOfChildNodes() + 1;
		}
		return number;
	}

	/**
	 * Adds GUI elements to the given panel that reflect the structure of this
	 * configuration object.
	 * 
	 * @param optionsPanel
	 *            the <code>JPanel</code> that should be filled with GUI
	 *            elements
	 * @param currentLevel
	 *            the current hierarchical level (initially 0 should be passed)
	 * @param currentRow
	 *            the options row in the panel to be written (initially 0 should
	 *            be passed)
	 * @param totalDepth
	 *            the total depth of the configuration option hierarchy (
	 *            {@link #getTreeDepth getTreeDepth} can be used to obtain this
	 *            value)
	 * @return the resulting <code>JPanel</code> component
	 */
	public JPanel getOptionsPanel(JPanel optionsPanel, int currentLevel,
			int currentRow, int totalDepth) {
		// starts with the root element: initialization
		if (currentLevel == 0) {
			optionsPanel = new JPanel(new GridBagLayout());
			optionsPanel.setBackground(new Color(255, 255, 255));
			// write dummy first line in order to initialize grid bag size
			for (int i = 0; i < totalDepth; i++) {
				JPanel dummyPanel = new JPanel();
				dummyPanel.setBackground(new Color(255, 255, 255));
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = i;
				c.gridy = 0;
				// give the description columns all available space to stretch
				if (i < totalDepth) {
					c.weightx = 0.0;
				} else {
					c.weightx = 1.0;
				}
				optionsPanel.add(dummyPanel, c);
			}
		} else {

			// alternate the background color for every row
			if (currentRow % 2 == 1) {
				// white
				myBackgroundColor_HEX = "#FFFFFF";
				myBackgroundColor_OBJ = new Color(255, 255, 255);
			} else {
				// grey
				myBackgroundColor_HEX = "#E6E6E6";
				myBackgroundColor_OBJ = new Color(230, 230, 230);
			}

			// fill empty space with proper background color
			for (int i = 0; i < currentLevel - 1; i++) {
				JPanel dummyPanel = new JPanel();
				dummyPanel.setBackground(myBackgroundColor_OBJ);
				// add at the proper position within the optionsPanel
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.gridx = i;
				c.gridy = currentRow;
				optionsPanel.add(dummyPanel, c);
			}

			// // write this level
			myCheckbox = new JCheckBox(getName(), isSelected());
			myCheckbox.addActionListener(new ActionListener() {
				/**
				 * The method to be automatically invoked changing the selection
				 * state of the checkbox.
				 * 
				 * @param e
				 *            The passed change event (not used).
				 */
				public void actionPerformed(ActionEvent e) {
					// keep status variable up to date
					mySelectionState = !mySelectionState;
					// enable/disable child options
					if (mySelectionState == true) {
						enableChildOptions();
					} else {
						// call method with false in order to not disable the
						// root level
						disableChildOptions(false);
					}
				}
			});

			myCheckbox.setToolTipText(getToolTip());
			myCheckbox.setBackground(myBackgroundColor_OBJ);
			// myCheckbox.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			// add at the proper position within the optionsPanel
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = currentLevel - 1;
			c.gridy = currentRow;
			c.gridwidth = totalDepth - (currentLevel - 1);
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			optionsPanel.add(myCheckbox, c);

			// embed description into HTML document (makes the lines wrap in the
			// editor pane)
			myEditorPane = new JEditorPane();
			myEditorPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			myEditorPane.setEditorKit(new HTMLEditorKit());
			setEditorPaneContent("#000000");
			myEditorPane.setEditable(false);
			// add at the proper position within the optionsPanel
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = totalDepth;
			c.gridy = currentRow;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			optionsPanel.add(myEditorPane, c);
		}
		// invoke writing of the next levels
		Iterator children = myChildConfigurations.iterator();
		int nextLevel = currentLevel + 1;
		int nextRow = currentRow + 1;
		while (children.hasNext()) {
			AnalysisConfiguration currentChild = (AnalysisConfiguration) children
					.next();
			optionsPanel = currentChild.getOptionsPanel(optionsPanel,
					nextLevel, nextRow, totalDepth);
			int usedSubRows = currentChild.getNumberOfChildNodes();
			nextRow = nextRow + usedSubRows + 1;
		}
		return optionsPanel;
	}

	/**
	 * Recursively enables all child configuration options (i.e., makes them
	 * editable). Note that this level is also affected by that.
	 */
	private void enableChildOptions() {
		// enable this option (necessary for recursive application)
		myCheckbox.setEnabled(true);
		if (mySelectionState == true) {
			// change text color to black
			setEditorPaneContent("#000000");
		}
		myEditorPane.updateUI();
		// enable child options
		Iterator children = myChildConfigurations.iterator();
		while (children.hasNext()) {
			AnalysisConfiguration currentChild = (AnalysisConfiguration) children
					.next();
			currentChild.enableChildOptions();
		}
	}

	/**
	 * Recursively disables all child configuration options (i.e., makes them
	 * non-editable).
	 * 
	 * @param disableParent
	 *            specifies whether this level is also affected (or the child
	 *            nodes only)
	 */
	private void disableChildOptions(boolean disableParent) {
		if (disableParent == true) {
			myCheckbox.setEnabled(false);
		}
		// change text color to grey
		setEditorPaneContent("#C0C0C0");
		myEditorPane.updateUI();
		// disable child options
		Iterator children = myChildConfigurations.iterator();
		while (children.hasNext()) {
			AnalysisConfiguration currentChild = (AnalysisConfiguration) children
					.next();
			currentChild.disableChildOptions(true);
		}
	}

	/**
	 * Updates the editor pane appearance with respect to the given text color.
	 * 
	 * @param newTextColor
	 *            the text color to be set (e.g., in hexadecimal)
	 */
	private void setEditorPaneContent(String newTextColor) {
		/**
		 * @todo anne: make the actual width depend on the size of the frame (in
		 *       order to fill all horizontal space) this will require a
		 *       calculation of the available space each time the settings
		 *       window is resized
		 */
		myEditorPane
				.setText("<html><head></head><body><div style=\"background-color:"
						+ myBackgroundColor_HEX
						+ "; width:"
						+ 670
						+ "px; text-align:left; color:" + newTextColor +
						// ";font-size:" + myFontSize +
						"\">" + getDescription() + "</div></body></html>");
	}

	// ///////////////////////// GET + SET METHOS
	// ///////////////////////////////

	/**
	 * Gets the list of subordinate analysis configuration options. Note that
	 * only the adjacent level of subordineates is returned.
	 * 
	 * @return the list of child objects
	 */
	public synchronized ArrayList<AnalysisConfiguration> getChildConfigurations() {
		return myChildConfigurations;
	}

	/**
	 * Gets the current selection status of this analysis option. Note that the
	 * default value is <code>true</code> but that this attribute is held
	 * persistent via the configuration manager.
	 * 
	 * @see #isEnabled
	 * @return <code>true</code> if currently selected, <code>false</code>
	 *         otherwise
	 */
	public synchronized boolean isSelected() {
		return mySelectionState;
	}

	/**
	 * Sets the current selection status of this analysis option. Note that the
	 * default value is <code>true</code> but that this attribute can be
	 * initialized differently using this method.
	 * 
	 * @see #isEnabled
	 * @return <code>true</code> if currently selected, <code>false</code>
	 *         otherwise
	 */
	public synchronized void setSelected(boolean init) {
		mySelectionState = init;
	}

	/**
	 * Gets the name attribute, which represents the name of this analysis
	 * aspect. Furthermore it serves as the label for the corresponding item in
	 * the settings window and the result window.
	 * 
	 * @return the String containing the name of this kind of analysis option
	 */
	public synchronized String getName() {
		return myName;
	}

	/**
	 * Sets the name attribute, which represents the name of this analysis
	 * aspect. Furthermore it serves as the label for the corresponding item in
	 * the settings window and the result window.
	 * 
	 * @param name
	 *            the String containing the name of this kind of analysis option
	 */
	public void setName(String name) {
		myName = name;
	}

	/**
	 * Gets the tool tip attribute, which serves as a tool tip for the
	 * corresponding item in the results window.
	 * 
	 * @return the String containing a brief description of this kind of
	 *         analysis option
	 */
	public String getToolTip() {
		return myToolTip;
	}

	/**
	 * Sets the tool tip attribute, which serves as a tool tip for the
	 * corresponding item in the results window.
	 * 
	 * @param toolTip
	 *            the String containing a brief description of this kind of
	 *            analysis option
	 */
	public void setToolTip(String toolTip) {
		myToolTip = toolTip;
	}

	/**
	 * Sets the actual result contents of the panel representing the belonging
	 * kind of analysis perspective (only used for man categories, that is,
	 * top-level objects in the tree). It serves as an intermediate storage to
	 * be packed genericly in the {@link AnalyisResults AnalysisResults} class.
	 * 
	 * @see #setResultPanel
	 * @return the result panel if assigned before, an empty JPanel otherwise
	 */
	public JPanel getResultPanel() {
		if (myResultPanel == null) {
			return new JPanel();
		} else {
			return myResultPanel;
		}
	}

	/**
	 * Assigns the GUI containing the results for that main analysis category.
	 * It serves as an intermediate storage to be packed genericly in the
	 * {@link AnalyisResults AnalysisResults} class.
	 * 
	 * @param panel
	 *            the results panel
	 */
	public void setResultPanel(AnalysisGUI panel) {
		myResultPanel = panel;
	}

	/**
	 * Gets the description attribute, which should contain sufficient
	 * information about this analysis option that the user is able to choose
	 * for it or not.
	 * 
	 * @return the String describing this kind of analysis option
	 */
	public String getDescription() {
		return myDescription;
	}

	/**
	 * Sets the description attribute, which should contain sufficient
	 * information about this analysis option that the user is able to choose
	 * for it or not.
	 * 
	 * @param description
	 *            the String describing this kind of analysis option
	 */
	public void setDescription(String description) {
		myDescription = description;
	}

	/**
	 * Gets the analysis method attributes, which indicates which kind of
	 * technique must be used in order to make use of this analysis option. Note
	 * that this may be
	 * <code>null<code> for configuration objects not representing a concrete metric.
	 * 
	 * @return the belonging analysis methods
	 */
	public synchronized HashSet<AnalysisMethodEnum> getAnalysisMethods() {
		return myAnalysisMethods;
	}

	/**
	 * Adds the analysis method attribute, which indicates which kind of
	 * technique must be used in order to make use of this analysis option. Note
	 * that this may be
	 * <code>null<code> for configuration objects not representing a concrete metric.
	 * 
	 * @param analysisMethod
	 *            the belonging analysis method
	 */
	public void setNewAnalysisMethod(AnalysisMethodEnum analysisMethod) {
		myAnalysisMethods.add(analysisMethod);
	}

	/**
	 * Finds the first node in the configuration tree that has the given name.
	 * 
	 * @param name
	 *            the name of the node to be found
	 * @return the node if found, <code>false</code> otherwise
	 */
	public synchronized AnalysisConfiguration findNode(String name) {
		AnalysisConfiguration foundNode = null;
		Iterator allChilds = myChildConfigurations.iterator();
		if (this.getName().equals(name)) {
			return this;
		} else {
			// search recursively
			while (allChilds.hasNext()) {
				AnalysisConfiguration currentChild = (AnalysisConfiguration) allChilds
						.next();
				foundNode = currentChild.findNode(name);
				if (foundNode != null) {
					return foundNode;
				}
			}
		}
		return foundNode;
	}

	/**
	 * Makes a deep copy of this object. Gets, for example, invoked on the
	 * current hierarchical {@link ConformanceAnalysisSettings#analysisOptions
	 * analysisOptions} object before the analysis is started in order to avoid
	 * side effects when spawning multiple result frames from the same settings
	 * frame. <br>
	 * Note that the result objects are not cloned (but thrown away) as the
	 * clone method is intended to be used before invoking the actual analysis.
	 * Note further that the GUI-related members neither are cloned as they are
	 * not needed in the analysis stage anymore. Overrides {@link Object#clone
	 * clone}.
	 * 
	 * @return the cloned object
	 */
	public Object clone() {
		AnalysisConfiguration newObject = null;
		try {
			newObject = (AnalysisConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// clone referenced objects (deep copy)
		// --> not necessary for the enum type (fixed)
		// but clean-up previous result objects
		newObject.myResultObjects = new HashSet();
		// recursively clone sub nodes
		newObject.myChildConfigurations = new ArrayList();
		Iterator subNodes = myChildConfigurations.iterator();
		while (subNodes.hasNext()) {
			AnalysisConfiguration currentChild = (AnalysisConfiguration) subNodes
					.next();
			newObject
					.addChildConfiguration((AnalysisConfiguration) currentChild
							.clone());
		}
		return newObject;
	}
}

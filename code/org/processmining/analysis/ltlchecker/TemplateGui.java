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

package org.processmining.analysis.ltlchecker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.ltlchecker.formulatree.FormulaNode;
import org.processmining.analysis.ltlchecker.formulatree.RootNode;
import org.processmining.analysis.ltlchecker.formulatree.ValueNode;
import org.processmining.analysis.ltlchecker.parser.ASTformulaDefinition;
import org.processmining.analysis.ltlchecker.parser.FormulaParameter;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.analysis.ltlchecker.parser.ParseException;
import org.processmining.analysis.ltlchecker.parser.SimpleNode;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * TemplateGui specifies a gui for selecting an template formula to check, see
 * te description of such formula and to valuate the parameters of the formula.
 * Furthermore some checkoptions can be set. After valuating all parameters, the
 * check can be performed.
 * 
 * @version 0.1
 * @author HT de Beer
 */
public class TemplateGui extends JScrollPane implements ActionListener {

	// FIELDS

	/** List with formula templates to select from. */
	ToolTipComboBox formulaList;

	/** A field for the description. */
	JEditorPane descriptionPane;

	/** A table for valuate the parameters. */
	ParamTable paramTable;

	/** Check option: till first success. */
	boolean firstSuccess;

	/** Check option: till first failure. */
	boolean firstFailure;

	/** Check option: skip checked formulas. */
	boolean skipReady;

	/** Checkbutton. */
	JButton checkButton;

	/** Exitbutton. */
	JButton exitButton;

	/** HelpButton. */
	JButton helpButton;

	JButton saveButton = new JButton("Save LTL file");

	/** Parser with parsedata. */
	LTLParser parser;

	/** Sets with the sets. */
	SetsSet sets;

	/** The log to check. */
	private LogReader log;

	ArrayList goodResults;
	int numGood;
	ArrayList badResults;
	int numBad;

	private LTLChecker checker;
	private AnalysisInputItem[] inputs;

	JPanel mainPanel;

	private boolean modified; // keep track of whether the formulas in the LTL

	// parser have been changed (deleted, copied,
	// etc)

	// CONSTRUCTORS

	public TemplateGui(LogReader logreader, LTLParser parser,
			LTLChecker checker, AnalysisInputItem[] inputs) {
		init(logreader, parser, checker, inputs);
	}

	protected TemplateGui() {
	}

	protected void init(LogReader logreader, LTLParser parser,
			LTLChecker checker, AnalysisInputItem[] inputs) {
		this.checker = checker;
		this.inputs = inputs;

		this.parser = parser;
		this.log = logreader;
		setModified(false);

		// Build the setsset
		sets = null;

		// Checkoption is `whole process':
		this.firstSuccess = false;
		this.firstFailure = false;
		this.skipReady = log instanceof BufferedLogReader;

		createGui();

		// Finalize the gui:

		// this.pack();
		this.setVisible(true);
	}

	// METHODS

	/**
	 * Get the option `till first success'
	 * 
	 * @return Till first success?
	 */
	public boolean getTillFirstSuccess() {
		return this.firstSuccess;
	}

	/**
	 * Get the option `till first failure'
	 * 
	 * @return Till first fialure?
	 */
	public boolean getTillFirstFailure() {
		return this.firstFailure;
	}

	/**
	 * Get the option `the whole process', that is, not till first failure and
	 * not till first success implies whole process.
	 * 
	 * @return The whole process?
	 */
	public boolean getWholeProcess() {
		return ((!this.firstSuccess) && (!this.firstFailure));
	}

	private String makeHTMLPage(String text) {
		return "<html>\n" + "\t" + "<head></head>\n" + "\t" + "<body>\n" + "\t"
				+ "\t" + text + "\t" + "</body>\n" + "</html>";
	}

	private void initFormulaList() {
		formulaList.removeAllItems();
		Iterator i = parser.getVisibleFormulaNames().iterator();
		while (i.hasNext()) {
			formulaList.addItem((String) i.next());
		}
		if (formulaList.getModel().getSize() <= 0) {
			formulaList.addItem("no formula");
		}
		formulaList.setSelectedItem(formulaList.getItemAt(0));
	}

	private void createGui() {
		JPanel centerPanel = new JPanel(new GridBagLayout());

		formulaList = new ToolTipComboBox();
		formulaList.setActionCommand("formula");

		descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane
				.setText(makeHTMLPage("<H1>No description available.</H1>"));

		JScrollPane descPane = new JScrollPane(descriptionPane);
		descPane.setPreferredSize(new Dimension(400, 140));
		descPane.setMinimumSize(new Dimension(400, 140));

		ParamData pd = createDataModel(new ArrayList());
		paramTable = createParamTable();
		paramTable.setModel(pd);

		int height = getParamPaneHeight();
		JScrollPane paramPane = new JScrollPane(paramTable);
		paramPane.setPreferredSize(new Dimension(400, height));
		paramPane.setMinimumSize(new Dimension(400, height));

		JPanel checkOptionsPanel = createCheckOptionsPanel();
		JPanel buttonPanel = createCheckButtonPanel();
		JPanel openSaveButtonsPanel = createOpenSaveButtonsPanel();
		JPanel setDefaultButtonsPanel = createSetDefaultButtonsPanel();

		JLabel selectFormulaLabel = new JLabel("<html>Select formula : </html>");
		selectFormulaLabel.setLabelFor(formulaList);
		selectFormulaLabel.setDisplayedMnemonic(KeyEvent.VK_F);

		// descriptionPane
		centerPanel.add(new JLabel("<html>Description :</html>"),
				new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0),
						0, 0));

		centerPanel.add(descPane, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 5, 0, 5), 0, 0));

		// Add the parameter Panel
		centerPanel.add(new JLabel("<html>Valuate the parameters :</html>"),
				new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0),
						0, 0));

		centerPanel.add(paramPane, new GridBagConstraints(0, 6, 2, 2, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 0, 0));

		centerPanel.add(setDefaultButtonsPanel, new GridBagConstraints(2, 6, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel leftTopPanel = new JPanel(new BorderLayout());
		JPanel leftPanel = new JPanel(new BorderLayout());

		topPanel.add(selectFormulaLabel, BorderLayout.NORTH);
		leftPanel.add(formulaList, BorderLayout.NORTH);
		leftPanel.add(buttonPanel, BorderLayout.CENTER);

		JPanel temp1 = new JPanel(new GridLayout());
		temp1.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));
		temp1.add(checkOptionsPanel);

		leftTopPanel.add(leftPanel, BorderLayout.CENTER);
		leftTopPanel.add(temp1, BorderLayout.EAST);

		topPanel.add(openSaveButtonsPanel, BorderLayout.EAST);
		topPanel.add(leftTopPanel, BorderLayout.CENTER);

		mainPanel = new JPanel(new BorderLayout());
		JPanel temp2 = new JPanel(new GridLayout());
		temp2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		temp2.add(topPanel);
		mainPanel.add(temp2, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		formulaList.addActionListener(this);
		initFormulaList();

		this.setViewportView(mainPanel);
	}

	private JPanel createCheckButtonPanel() {
		JPanel buttonPanel = new JPanel();

		/*
		 * helpButton = new JButton( "Help" ); helpButton.setActionCommand(
		 * "help" ); helpButton.setMnemonic( KeyEvent.VK_H );
		 * helpButton.addActionListener( this ); helpButton.setToolTipText(
		 * "View the help files for the ltlchecker"); buttonPanel.add(
		 * helpButton );
		 */

		checkButton = new JButton("Check formula");
		checkButton.setEnabled(false);
		checkButton.setActionCommand("check");
		checkButton.setMnemonic(KeyEvent.VK_C);
		checkButton.addActionListener(this);
		checkButton
				.setToolTipText("Check the selected property, results in a new window.");
		buttonPanel.add(checkButton);

		buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
		return buttonPanel;
	}

	private JPanel createOpenSaveButtonsPanel() {
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

		JButton openButton = new JButton("Open LTL file...");
		openButton.setActionCommand("open");
		openButton.setMnemonic(KeyEvent.VK_O);
		openButton.addActionListener(this);
		openButton.setToolTipText("Open an LTL file with formulas");
		buttonPanel.add(openButton);

		saveButton.setActionCommand("save");
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(this);
		saveButton.setToolTipText("Save the currently opened LTL file");
		buttonPanel.add(saveButton);

		JButton saveAsButton = new JButton("Save LTL file as...");
		saveAsButton.setActionCommand("saveas");
		saveAsButton.addActionListener(this);
		saveAsButton
				.setToolTipText("Save the currently opened LTL file in another file");
		buttonPanel.add(saveAsButton);

		JPanel actualPanel = new JPanel();
		actualPanel.add(buttonPanel);
		return actualPanel;
	}

	private JPanel createSetDefaultButtonsPanel() {
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));

		JButton setDefaultButton = new JButton("Set values as default");
		setDefaultButton.setActionCommand("setdefault");
		setDefaultButton.setMnemonic(KeyEvent.VK_D);
		setDefaultButton.addActionListener(this);
		setDefaultButton
				.setToolTipText("Save the parameter values in the table below as the default values for this formula");
		buttonPanel.add(setDefaultButton);

		// JButton copyButton = new JButton("Save formula as...");
		// copyButton.setActionCommand("copy");
		// copyButton.addActionListener(this);
		// copyButton.setToolTipText("Create a copy the currently selected formula with a different name, using the current settings as defaults");
		// buttonPanel.add(copyButton);

		JButton deleteButton = new JButton("Delete formula");
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);
		deleteButton.setToolTipText("Delete the currently selected formula");
		buttonPanel.add(deleteButton);

		return buttonPanel;
	}

	private JPanel createCheckOptionsPanel() {

		JPanel checkOptionsPanel = new JPanel();
		checkOptionsPanel.setLayout(new BoxLayout(checkOptionsPanel,
				BoxLayout.Y_AXIS));

		TitledBorder tb = BorderFactory.createTitledBorder("Check options");
		checkOptionsPanel.setBorder(tb);

		JRadioButton wholeRB = new JRadioButton("Check whole process");
		wholeRB.setMnemonic(KeyEvent.VK_W);
		wholeRB.setActionCommand("whole");
		wholeRB.addActionListener(this);
		wholeRB.setSelected(true);

		JRadioButton failureRB = new JRadioButton("Check untill first failure");
		failureRB.setMnemonic(KeyEvent.VK_F);
		failureRB.setActionCommand("failure");
		failureRB.addActionListener(this);
		failureRB.setSelected(false);

		JRadioButton successRB = new JRadioButton("Check untill first success");
		successRB.setMnemonic(KeyEvent.VK_S);
		successRB.setActionCommand("success");
		successRB.addActionListener(this);
		successRB.setSelected(false);

		ButtonGroup bg = new ButtonGroup();
		bg.add(wholeRB);
		bg.add(failureRB);
		bg.add(successRB);

		JCheckBox skipCB = new JCheckBox("Skip if result is known");
		skipCB.setMnemonic(KeyEvent.VK_K);
		skipCB.setActionCommand("skip");
		skipCB.addActionListener(this);
		skipCB.setSelected((log instanceof BufferedLogReader));
		skipCB.setEnabled((log instanceof BufferedLogReader));

		checkOptionsPanel.add(wholeRB);
		checkOptionsPanel.add(failureRB);
		checkOptionsPanel.add(successRB);
		checkOptionsPanel.add(skipCB);

		return checkOptionsPanel;
	}

	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();

		// Per actioncommand do an action:
		if (action.equals("help")) {
			System.out.println("Help");
		} else if (action.equals("skip")) {
			skipReady = ((JCheckBox) ae.getSource()).isSelected();
		} else if (action.equals("whole")) {
			firstFailure = false;
			firstSuccess = false;
		} else if (action.equals("failure")) {
			firstFailure = true;
			firstSuccess = false;
		} else if (action.equals("success")) {
			firstFailure = false;
			firstSuccess = true;
		} else if (action.equals("formula")) {
			// Get the selected formula
			String selectedFormula = (String) formulaList.getSelectedItem();

			if (selectedFormula != null
					&& !selectedFormula.equals("no formula")) {
				SimpleNode formula = parser.getFormula(selectedFormula);

				// Update description this
				ASTformulaDefinition form = (ASTformulaDefinition) formula;
				String description = form.getDescription();
				descriptionPane.setText(makeHTMLPage(description));

				// Update param table

				ParamData pd = createDataModel(parser
						.getParameters(selectedFormula));
				paramTable.setModel(pd);

				// enable checking
				checkButton.setEnabled(true);

			} else {
				// no formula, so there is nothing to do.
				// Maybe emty the handel
			}
			;
		} else if (action.equals("open")) {
			openLTLFile();
		} else if (action.equals("save")) {
			saveLTLFile();
		} else if (action.equals("saveas")) {
			saveLTLFileAs();
		} else if (action.equals("copy")) {
			JOptionPane.showMessageDialog(MainUI.getInstance(),
					"To be implemented.");
		} else if (action.equals("delete")) {
			deleteFormula();
		} else if (action.equals("setdefault")) {
			setOptionsAsDefault();
		} else if (action.equals("check")) {
			// Do the check, that is, build the formulatree and check
			// it afterwards. If it is not final, supply the tree with
			// the substitutions.

			checkButton.setEnabled(false);

			final String formulaName = (String) formulaList.getSelectedItem();

			if (!((formulaName.equals("")) || (formulaName == null))) {
				// There is something to build, so do it.

				org.processmining.framework.ui.SwingWorker w = new org.processmining.framework.ui.SwingWorker() {
					private Substitutes sss;

					public Object construct() {

						Progress p;
						int teller = 0;
						Message.add("<LTLChecker>", Message.TEST);
						if (sets == null) {
							p = new Progress("Checking the property", 0,
									2 * log.getLogSummary()
											.getNumberOfProcessInstances());
							p.setNote("Creating the attribute sets");

							sets = new SetsSet(parser, log.getLogSummary());
							sets.fill(log, p);
							System.out.println(sets);

							teller = log.getLogSummary()
									.getNumberOfProcessInstances();
						} else {
							p = new Progress("Checking the property", 0, log
									.getLogSummary()
									.getNumberOfProcessInstances());
						}
						;

						p.setNote("Building formula tree");

						TreeBuilder tb = new TreeBuilder(parser, formulaName,
								sets, log.getLogSummary().getOntologies());
						Message.add("  <FormulaTreeBuild success>",
								Message.TEST);

						// Create the set of substitutes if any. THat is, if
						// it is an template, it can have already substitution
						// now, so add them. Otherwise, an empty substitutes
						// set is enough.

						RootNode root = new RootNode();
						sss = paramTable.getSubstitutes(parser);

						if (sss == null) {
							p.close();
							return new Boolean(true);
						}
						sss.setBinder(root);

						// Do the verification

						goodResults = new ArrayList(log.numberOfInstances());
						badResults = new ArrayList(log.numberOfInstances());
						numGood = 0;
						numBad = 0;

						int piNumber = 0;

						boolean run = true;
						boolean fullfill = false;

						Iterator piIt = log.instanceIterator();
						while (piIt.hasNext() && run && !p.isCanceled()) {
							FormulaNode formula = (FormulaNode) tb.build(parser
									.getFormula(formulaName), sss, root);
							root.setFormula(formula);

							ProcessInstance pi = (ProcessInstance) piIt.next();
							String result = (String) pi.getAttributes().get(
									formula.toString());
							fullfill = Boolean.valueOf(result).booleanValue();

							p.setNote(pi.getName());
							p.setProgress(teller);
							teller++;

							if ((result == null) || !skipReady) {

								AuditTrailEntryList ates = pi
										.getAuditTrailEntryList();

								// Because the pi must be walked through in
								// reverse order, and the
								// framework does not support this, the ates of
								// this pi are first
								// readed in into a list.

								LinkedList atesList = new LinkedList();
								Iterator ateIt = ates.iterator();
								while (ateIt.hasNext()) {
									AuditTrailEntry at = (AuditTrailEntry) ateIt
											.next();
									atesList.add(at);
								}
								;

								// But first initialize the formula, that is ,
								// create the initial
								// values, or the values of the n+1th ate of pi,
								// given that there
								// are just n ates in pi. This can also be seen
								// as computing te
								// value of he formula for an empty ateslist.
								// fullfill = formulaTree.init( );
								fullfill = false;

								for (int j = atesList.size(); j >= 0; j--) {
									// start with n + 1 ...
									fullfill = root.value(pi, atesList, j);
								}
								;

								pi.setAttribute(formula.toString(),
										(fullfill ? "true" : "false"));

							}
							// The computed fullfill of the last ate of the pi
							// is the value of
							// the whole pi, because by dynamic programming the
							// other ates are
							// already computed and used.
							if (!fullfill && firstFailure) {
								// stop at first failure
								run = false;
							} else if (fullfill && firstSuccess) {
								// stop at first success
								run = false;
							}
							;

							if (fullfill) {
								goodResults.add(new CheckResult(piNumber, pi));
								numGood += MethodsForWorkflowLogDataStructures
										.getNumberSimilarProcessInstances(pi);
							} else {
								badResults.add(new CheckResult(piNumber, pi));
								numBad += MethodsForWorkflowLogDataStructures
										.getNumberSimilarProcessInstances(pi);
							}
							;

							piNumber++;
						}
						;
						Message.add(
								"  <NumberProcessInstances=" + teller + ">",
								Message.TEST);
						Message.add("  <NumberGoodResults=" + numGood + ">",
								Message.TEST);
						Message.add("  <NumberBadResults=" + numBad + ">",
								Message.TEST);
						Boolean b = new Boolean(p.isCanceled());
						p.close();
						return b;
					};

					public void finished() {
						if (!((Boolean) get()).booleanValue()) {
							MainUI.getInstance().createAnalysisResultFrame(
									checker,
									inputs,
									new LTLVerificationResult(formulaName, log,
											goodResults, numGood, badResults,
											numBad, sss));
						}
						;
						Message.add("</LTLChecker>", Message.TEST);
						checkButton.setEnabled(true);
					};
				};
				w.start();

			}
			;

		}
		;

	}

	private void setModified(boolean value) {
		modified = value;
		saveButton.setEnabled(modified);
	}

	private void setOptionsAsDefault() {
		String formulaName = (String) formulaList.getSelectedItem();

		if (formulaName == null || formulaName.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please select a formula first.");
			return;
		}

		Map<String, String> defaults = new HashMap<String, String>();
		for (Map.Entry<String, ValueNode> item : paramTable.getSubstitutes(
				parser).getAll().entrySet()) {
			defaults.put(item.getKey(), item.getValue()
					.asParseableDefaultValue());

		}
		parser.setDefaultValues(formulaName, defaults);
		setModified(true);
	}

	private void deleteFormula() {
		String name = (String) formulaList.getSelectedItem();

		if (name == null || name.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please select a formula first.");
		} else if (JOptionPane
				.showConfirmDialog(
						this,
						"Are you sure you want to delete the formula '"
								+ name
								+ "'?\n\n"
								+ "Note that the changes are not saved to the LTL file automatically. To really delete\n"
								+ "it from the LTL file you need to save it using the 'Save LTL file' button later.",
						"Delete formula", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			this.parser.deleteFormula(name);
			setModified(true);
			initFormulaList();
		}
	}

	private void openLTLFile() {
		if (modified) {
			int response = JOptionPane
					.showConfirmDialog(
							this,
							"The formulas have been modified. Do you want to save the changes to the LTL file?",
							"Save changes", JOptionPane.YES_NO_CANCEL_OPTION);
			if (response == JOptionPane.CANCEL_OPTION) {
				return;
			} else if (response == JOptionPane.YES_OPTION) {
				saveLTLFile();
			}
		}
		JFileChooser chooser = new JFileChooser(this.parser.getFilename());

		if (this.parser.getFilename() != null) {
			chooser.setSelectedFile(new File(this.parser.getFilename()));
		}

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();

			try {
				FileInputStream fis = new FileInputStream(name);
				LTLParser newparser = new LTLParser(fis);
				newparser.setFilename(name);
				newparser.init();
				newparser.parse();
				fis.close();
				this.parser = newparser;
				setModified(false);
				sets = null;
				initFormulaList();
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(this,
						"Error while parsing LTL file: " + e.getMessage());
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(this, "File not found: " + name);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this,
						"Error while closing LTL file: " + e.getMessage());
			}
		}
	}

	private void saveLTLFile() {
		if (this.parser.getFilename() == null) {
			saveLTLFileAs();
		} else {
			saveLTLFile(this.parser.getFilename(), null);
		}
	}

	private void saveLTLFileAs() {
		Set<String> formulas = getFormulasToSave();

		if (formulas != null) {
			JFileChooser chooser = new JFileChooser(this.parser.getFilename());

			if (this.parser.getFilename() != null) {
				chooser.setSelectedFile(new File(this.parser.getFilename()));
			}

			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();

				if (f.exists()) {
					if (f.isFile()) {
						if (JOptionPane.showConfirmDialog(this,
								"File already exists: " + f.getPath()
										+ "\n\nOverwrite existing file?",
								"File exists", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							return;
						}
					} else {
						JOptionPane.showMessageDialog(this,
								"Please select a filename, not a directory.");
						return;
					}
				}
				saveLTLFile(f.getPath(), formulas);
			}
		}
	}

	boolean userClickedOk;

	private void selectAll(JList list) {
		int[] indices = new int[list.getModel().getSize()];
		for (int i = 0; i < list.getModel().getSize(); i++) {
			indices[i] = i;
		}
		list.setSelectedIndices(indices);
	}

	private Set<String> getSelected(JList list) {
		Set<String> result = new HashSet<String>();

		for (int i = 0; i < list.getModel().getSize(); i++) {
			if (list.isSelectedIndex(i)) {
				result.add((String) list.getModel().getElementAt(i));
			}
		}
		return result;
	}

	private Set<String> getFormulasToSave() {
		final JDialog dialog = new JDialog(MainUI.getInstance(),
				"Select formulae to save", true);
		final JList list = new JList(new Vector<Object>(parser
				.getVisibleFormulaNames()));
		JButton selectAllButton = new JButton("Select all");
		JButton selectNoneButton = new JButton("Select none");
		JButton okButton = new JButton("   Ok   ");
		JButton cancelButton = new JButton("Cancel");

		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectAll(list);

		JPanel selectButtonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		selectButtonsPanel.add(selectAllButton);
		selectButtonsPanel.add(selectNoneButton);
		selectButtonsPanel.setBorder(BorderFactory
				.createEmptyBorder(5, 5, 5, 5));

		JPanel selectButtonsPanel2 = new JPanel(new BorderLayout());
		selectButtonsPanel2.add(selectButtonsPanel, BorderLayout.NORTH);

		JPanel okCancelButtonsPanel = new JPanel();
		okCancelButtonsPanel.add(okButton);
		okCancelButtonsPanel.add(cancelButton);

		JPanel centerPane = new JPanel(new BorderLayout());
		centerPane.add(new JScrollPane(list), BorderLayout.CENTER);
		centerPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.setLayout(new BorderLayout());
		dialog.add(selectButtonsPanel2, BorderLayout.EAST);
		dialog.add(centerPane, BorderLayout.CENTER);
		dialog.add(okCancelButtonsPanel, BorderLayout.SOUTH);

		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAll(list);
			}
		});
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				list.setSelectedIndices(new int[0]);
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userClickedOk = true;
				dialog.setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		dialog.pack();
		CenterOnScreen.center(dialog);

		userClickedOk = false;
		dialog.setVisible(true);

		if (userClickedOk) {
			return getSelected(list);
		} else {
			return null;
		}
	}

	private void saveLTLFile(String name, Set<String> formulas) {
		try {
			this.parser.writeToFile(name, formulas);
			this.parser.setFilename(name);
			setModified(false);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error writing file " + name
					+ ":\n" + e.getMessage());
		}
	}

	protected ParamData createDataModel(List<FormulaParameter> items) {
		return new ParamData(items);
	}

	protected ParamTable createParamTable() {
		return new ParamTable();
	}

	protected int getParamPaneHeight() {
		return 70;
	}
}

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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.deckfour.slickerbox.components.ActionBar;
import org.deckfour.slickerbox.components.glasspane.DialogGlassPane;
import org.deckfour.slickerbox.components.glasspane.ExposePanel;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.analysis.log.scale.ProcessInstanceScale;
import org.processmining.analysis.log.scale.ScaleCollection;
import org.processmining.analysis.recommendation.RecommendationCollection;
import org.processmining.analysis.recommendation.contrib.LogBasedContributor;
import org.processmining.analysis.recommendation.contrib.RecommendationContributor;
import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.ConvertingPluginCollection;
import org.processmining.exporting.ExportPluginCollection;
import org.processmining.exporting.log.SAMXMLLogExport;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.log.rfb.ProcessInstanceImpl;
import org.processmining.framework.log.rfb.io.monitor.NikeFsDialogMenuItem;
import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationProvider;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.models.recommendation.net.RecommendationServiceHandler;
import org.processmining.framework.models.recommendation.net.client.RecommendationProviderProxy;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.remote.Service;
import org.processmining.framework.ui.actions.AnalyseAction;
import org.processmining.framework.ui.actions.CatchOutOfMemoryAction;
import org.processmining.framework.ui.actions.ConvertAction;
import org.processmining.framework.ui.actions.ExportHistoryAction;
import org.processmining.framework.ui.actions.FullScreenAction;
import org.processmining.framework.ui.actions.ImportAnyFileAction;
import org.processmining.framework.ui.actions.MostRecentAnalyseAction;
import org.processmining.framework.ui.actions.MostRecentLogAction;
import org.processmining.framework.ui.actions.ShowExposeAction;
import org.processmining.framework.ui.actions.ShowLauncherAction;
import org.processmining.framework.ui.actions.ShowMessagesAction;
import org.processmining.framework.ui.actions.ShowNavigationPanelAction;
import org.processmining.framework.ui.menus.AnalysisMenu;
import org.processmining.framework.ui.menus.ConversionMenu;
import org.processmining.framework.ui.menus.DesktopBackgroundMenu;
import org.processmining.framework.ui.menus.ExportMenu;
import org.processmining.framework.ui.menus.HelpMenu;
import org.processmining.framework.ui.menus.IconThemeMenu;
import org.processmining.framework.ui.menus.ImportMenu;
import org.processmining.framework.ui.menus.LogReaderMenu;
import org.processmining.framework.ui.menus.MineMenu;
import org.processmining.framework.ui.menus.SetDotExeMenuItem;
import org.processmining.framework.ui.slicker.ScreenshotUtility;
import org.processmining.framework.ui.slicker.console.SlickerMessageBar;
import org.processmining.framework.ui.slicker.launch.LaunchGlassPane;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.framework.util.StopWatch;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.ImportPluginCollection;
import org.processmining.importing.LogReaderConnection;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.importing.ProMInputStream;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningPluginCollection;
import org.processmining.mining.MiningResult;

import com.holub.tools.Archive;

/**
 * @author Peter van den Brand
 * @author Christian W. Guenther (christian@deckfour.org)
 * @version 2.0
 */

public class MainUI extends JFrame implements Provider {

	private static final long serialVersionUID = -5251849261371249059L;

	public static final String STARTEDFROMCOMMANDLINE = "This analysisplugin was started from the command line.";
	static final String PROCESS = "ProM_Process";
	public static final int RECOM_PORT = 4446;
	private boolean recomRunning = false;

	private static MainUI mainInstance;

	private ArrayList<ProvidedObject> globalProvidedObjects = new ArrayList<ProvidedObject>();

	private MDIDesktopPane desktop = new MDIDesktopPane();
	private BorderLayout borderLayout1 = new BorderLayout();
	protected ActionBar actionBar;
	protected ExposePanel exposePanel;
	protected LaunchGlassPane launchPanel;
	protected DialogGlassPane dialogPanel;
	private JMenuBar menuBar = new JMenuBar();
	private JPanel contentPanel = new JPanel();
	protected SlickerMessageBar console = new SlickerMessageBar();
	private JCheckBoxMenuItem keepLog = new JCheckBoxMenuItem("Keep History");
	private Service recomService = null;
	private GetRecommendationAction getRecomAction;

	private JMenu helpMenu = new JMenu("Help");

	private PluginReference reference = new PluginReference();

	private ProcessInstanceImpl historyPI;

	private JSplitPane desktopAndNavigation = null;
	private int lastDividerLocation;
	private NavigationPanel navigationPanel;

	private void buildNewExecutionLog(String fn) {
		// fn is filename without extensions
		try {
			FileOutputStream zipFile = new FileOutputStream(fn + ".zip");
			ZipOutputStream output = new ZipOutputStream(
					new BufferedOutputStream(zipFile));
			output.putNextEntry(new ZipEntry(fn + " content.xml"));

			// No logfile present, lets create it
			writeLn("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", output);
			writeLn(
					"<WorkflowLog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ " xsi:noNamespaceSchemaLocation=\"WorkflowLog.xsd\""
							+ " description=\"ProM execution log\">", output);

			// Write the source section
			writeLn("<Source program=\"" + About.NAME + "\">", output);
			writeLn("</Source>", output);

			// Now, write the process section, for one process
			writeLn(
					"<Process id=\""
							+ PROCESS
							+ "\""
							+ " description=\"Process containing the ProM execution Log\">",
					output);

			// writeLn("</Process>", output);

			// writeLn("</WorkflowLog>", output);
			output.closeEntry();
			output.close();

		} catch (FileNotFoundException ex) {
			Message.add("Could not open execution log", Message.ERROR);
		} catch (IOException ex) {
			Message.add("Could not open execution log", Message.ERROR);
		}

	}

	/**
	 * construct the MainUI. This method should only be called once. All further
	 * communication with the UI should be done through MainUI.getInstance()
	 */
	public MainUI() {

		/*
		 * TODO: implement 'unified toolbar' look on OS X
		 * if(RuntimeUtils.isRunningMacOsX()) {
		 * this.getRootPane().putClientProperty("apple.awt.brushMetalLook",
		 * Boolean.TRUE); }
		 */

		ActionListener showAbout = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				About about = new About(MainUI.this);
				about.setVisible(true);
			}
		};
		actionBar = new ActionBar(new ImageIcon(About.IMAGELOCATION()
				+ "actionbarlogo.png"), showAbout);

		keepLog.setSelected(UISettings.getInstance().getKeepHistory());

		if (keepLog.isSelected()
				&& LogReaderFactory.getLogReaderClass().equals(
						BufferedLogReader.class)) {
			String fn = UISettings.getInstance().getExecutionLogFileName();
			if (!(new File(fn + ".zip").exists())) {
				buildNewExecutionLog(fn);
			}

			initializeHistoryInstance("Started history @"
					+ new Date(System.currentTimeMillis()).toString(),
					new DataSection());

		} else {
		}

		mainInstance = this;

		try {
			jbInit();
			pack();
			setExtendedState(JFrame.MAXIMIZED_BOTH);
			if (desktopAndNavigation != null) {
				desktopAndNavigation.setDividerLocation(desktopAndNavigation
						.getSize().width - 200);
				lastDividerLocation = desktopAndNavigation.getDividerLocation();
				// toggleNavigationPanelVisible();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeLn(String s, OutputStream out) {
		try {
			if (s.length() != 0) {
				out.write(s.getBytes());
			}
			out.write("\n".getBytes());
		} catch (IOException ex) {
		}
	}

	public static MainUI getInstance() {
		return mainInstance;
	}

	public void showReference(Plugin plugin) {
		reference.show(plugin);
	}

	public synchronized void quit() {
		// update the execution log. First, we find the </process> tag and we
		// add
		// a new instance in front of that tag
		mainInstance = null;
		try {
			if (keepLog.isSelected()) {
				flushHistory();
			}
			// A call to dispose() here messes up the situation where the
			// MainUI.getInstance().quit()
			// is called before the ProMSplash is removed from the screen.
			// dispose();
		} catch (Exception e) {
			// No reason to worry, exiting
		}
		System.exit(0);
	}

	private void jbInit() throws Exception {
		String name = About.IMAGELOCATION() + "icon.gif";

		setIconImage(Toolkit.getDefaultToolkit().getImage(name));

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		helpMenu.add(new PluginReferenceAction());
		helpMenu.add(new AboutAction());
		helpMenu.add(new ParametersAction());
		helpMenu.addSeparator();
		helpMenu.add(new HelpMenu());
		helpMenu.addSeparator();

		/* REMOVED FOR RELEASE THE FUNCTIONALITY TO KEEP HISTORY */
		helpMenu.add(keepLog);
		helpMenu.add(new ShowHistoryAction()).setEnabled(keepLog.isSelected());
		helpMenu.add(new StartNewHistoryPIAction()).setEnabled(
				keepLog.isSelected());
		helpMenu.add(new MainUIExportHistoryAction(desktop)).setEnabled(
				keepLog.isSelected());
		helpMenu.add(new ResetHistoryAction()).setEnabled(keepLog.isSelected());

		helpMenu.addSeparator();

		getRecomAction = new GetRecommendationAction();
		getRecomAction.setEnabled(false);
		helpMenu.add(new StartRecommendationAction());
		helpMenu.add(getRecomAction);
		helpMenu.addSeparator();
		/**/
		helpMenu.add(new LogReaderMenu());
		helpMenu.add(new NikeFsDialogMenuItem());
		helpMenu.add(new IconThemeMenu());
		helpMenu.add(new DesktopBackgroundMenu());
		helpMenu.add(new SetDotExeMenuItem());

		JMenuItem appShotItem = new JMenuItem(
				"Capture application screenshot...");
		appShotItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenshotUtility.captureApplicationScreenshot();
			}
		});
		JMenuItem frameShotItem = new JMenuItem(
				"Capture active frame screenshot...");
		frameShotItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenshotUtility.captureActiveFrameScreenshot();
			}
		});
		helpMenu.addSeparator();
		helpMenu.add(frameShotItem);
		helpMenu.add(appShotItem);

		keepLog.addItemListener(new HistoryCheckBoxActionListener());

		final ActionListener resetGlassPaneListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetGlassPane();
			}
		};

		exposePanel = new ExposePanel(desktop, resetGlassPaneListener);
		this.setGlassPane(exposePanel);
		exposePanel.setVisible(false);

		launchPanel = new LaunchGlassPane();
		launchPanel.setVisible(false);
		launchPanel.setSize(this.getSize());

		dialogPanel = new DialogGlassPane(resetGlassPaneListener);
		dialogPanel.setVisible(false);
		dialogPanel.setSize(this.getSize());

		actionBar.add(new ImportAnyFileAction(desktop, !RuntimeUtils
				.isRunningMacOsX()));
		actionBar.add(new AnalyseAction(desktop));
		actionBar.add(new ConvertAction(desktop));
		actionBar.addSeperator(15);
		actionBar.add(new MostRecentLogAction(desktop));
		actionBar.add(new MostRecentAnalyseAction(desktop));
		actionBar.addSeperator(30);
		actionBar.add(new ShowMessagesAction(desktop));
		actionBar.add(new ShowNavigationPanelAction(desktop));
		actionBar.addSeperator(15);
		actionBar.add(new ShowExposeAction(desktop));
		actionBar.add(new FullScreenAction(desktop));
		actionBar.addSeperator(15);
		actionBar.add(new ShowLauncherAction(desktop));

		menuBar.add(new ImportMenu(desktop));
		menuBar.add(new MineMenu(desktop));
		menuBar.add(new AnalysisMenu(desktop));
		menuBar.add(new ConversionMenu(desktop));
		menuBar.add(new ExportMenu(desktop));
		menuBar.add(new WindowMenu(desktop));
		menuBar.add(helpMenu);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle(About.NAME + " [" + About.VERSION + "]");
		this.setJMenuBar(menuBar);

		contentPanel.setLayout(borderLayout1);
		contentPanel.add(actionBar, BorderLayout.NORTH);
		contentPanel.add(desktop, BorderLayout.CENTER);
		contentPanel.add(console, BorderLayout.SOUTH);
		this.setContentPane(contentPanel);

		// create navigation panel, but set invisible (i.e. no updates) for now
		// (until made visible consciously)
		desktopAndNavigation = null;
		navigationPanel = new NavigationPanel(getDesktop());

		// register application-wide keyboard shortcuts
		registerKeyStrokes();
	}

	/**
	 * Registers global key strokes for ProM:
	 * <ul>
	 * <li>Modifier + E: Expose feature</li>
	 * <li>Modifier + D: Action trigger</li>
	 * <li>Modifier + W: Open active window and select next in z-order, if
	 * available</li>
	 * <li>Modifier + R: Open last opened log file</li>
	 * <li>Modifier + T: Perform last triggered analysis</li>
	 * <li>Modifier + O: Open any type of file</li>
	 * </ul>
	 * <p>
	 * The modifier is the control key for Windows and Linux, and the command
	 * (or, 'Apple') key for Mac OS X.
	 */
	protected void registerKeyStrokes() {
		// register key strokes
		int modifierCode = KeyEvent.CTRL_DOWN_MASK;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			// on os x, use the command (i.e., 'apple') modifier key
			modifierCode = KeyEvent.META_DOWN_MASK;
		}
		// action trigger stroke:
		JComponent cPane = (JComponent) this.getContentPane();
		KeyStroke launchStroke = KeyStroke.getKeyStroke('D', modifierCode);
		ActionListener launchListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getGlassPane().isVisible() == false) {
					JInternalFrame activeFrame = getDesktop()
							.getSelectedFrame();
					if (activeFrame != null && activeFrame instanceof Provider) {
						Provider provider = (Provider) activeFrame;
						if (provider.getProvidedObjects().length > 0) {
							MainUI.getInstance().showLauncher();
						} else {
							MainUI
									.getInstance()
									.showGlassDialog(
											"No action available!",
											"The currently selected frame does not have any"
													+ " provided objects which could be used by any available plugin. Please select another frame.");
						}
					} else {
						MainUI
								.getInstance()
								.showGlassDialog(
										"No action available!",
										"The currently selected frame does not have any"
												+ " provided objects which could be used by any available plugin. Please select another frame.");
					}
				}
			}
		};
		cPane.registerKeyboardAction(launchListener, launchStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// expose stroke
		KeyStroke exposeStroke = KeyStroke.getKeyStroke('E', modifierCode);
		ActionListener exposeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getGlassPane().isVisible() == false) {
					if (getDesktop().getAllFrames().length > 1) {
						showExpose();
					} else {
						showGlassDialog(
								"Expos\u00E9 not applicable",
								"The Expos\u00E9 feature has the purpose of helping you switch between a number of "
										+ "internal windows rapidly. You need to have more than one internal window "
										+ "open to make use of this functionality.");
					}
				}
			}
		};
		cPane.registerKeyboardAction(exposeListener, exposeStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// full screen stroke
		KeyStroke fullScreenStroke = KeyStroke.getKeyStroke('F', modifierCode);
		ActionListener fullScreenListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				(new FullScreenAction(desktop)).execute(null);
			}
		};
		cPane.registerKeyboardAction(fullScreenListener, fullScreenStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// frame closing stroke
		KeyStroke frameCloseStroke = KeyStroke.getKeyStroke('W', modifierCode);
		ActionListener frameCloseListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JInternalFrame activeFrame = getDesktop().getSelectedFrame();
				if (activeFrame != null) {
					activeFrame.dispose();
					// try selecting the next frame in z-order
					JInternalFrame frames[] = getDesktop().getAllFrames();
					if (frames.length > 0) {
						desktop.moveToFront(frames[0]);
					}
				}
			}
		};
		cPane.registerKeyboardAction(frameCloseListener, frameCloseStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// frame maximize toggle stroke
		KeyStroke frameMaximizeStroke = KeyStroke.getKeyStroke('M',
				modifierCode);
		ActionListener frameMaximizeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JInternalFrame activeFrame = getDesktop().getSelectedFrame();
				if (activeFrame != null) {
					// toggle maximized property
					try {
						activeFrame.setMaximum(!activeFrame.isMaximum());
					} catch (PropertyVetoException e) {
						// yeah, whatever..
						e.printStackTrace();
					}
				}
			}
		};
		cPane.registerKeyboardAction(frameMaximizeListener,
				frameMaximizeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// file open stroke
		KeyStroke fileOpenStroke = KeyStroke.getKeyStroke('O', modifierCode);
		ActionListener fileOpenListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				(new ImportAnyFileAction(desktop, !RuntimeUtils
						.isRunningMacOsX())).actionPerformed(evt);
			}
		};
		cPane.registerKeyboardAction(fileOpenListener, fileOpenStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// open recent log stroke
		KeyStroke recentLogStroke = KeyStroke.getKeyStroke('R', modifierCode);
		ActionListener recentLogListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				(new MostRecentLogAction(desktop)).actionPerformed(evt);
			}
		};
		cPane.registerKeyboardAction(recentLogListener, recentLogStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		// start recent analysis stroke
		KeyStroke recentAnalysisStroke = KeyStroke.getKeyStroke('T',
				modifierCode);
		ActionListener recentAnalysisListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				(new MostRecentAnalyseAction(desktop)).actionPerformed(evt);
			}
		};
		cPane.registerKeyboardAction(recentAnalysisListener,
				recentAnalysisStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	public void toggleMessagesVisible() {
		console.toggleExpanded();
	}

	public void showExpose() {
		desktop.rescueAllFrames();
		this.setGlassPane(exposePanel);
		exposePanel.revalidate();
		exposePanel.showExpose();
	}

	public void showLauncher() {
		if (getGlassPane().isVisible() == false) {
			JInternalFrame activeFrame = getDesktop().getSelectedFrame();
			if (activeFrame != null && activeFrame instanceof Provider) {
				Provider provider = (Provider) activeFrame;

				if (provider != null) {
					ProvidedObject[] objects = provider.getProvidedObjects();

					if (objects != null && objects.length > 0) {
						this.setGlassPane(launchPanel);
						launchPanel.revalidate();
						launchPanel.fadeIn();
						return;
					}
				}
			}
		}
		// impossible to perform action
		MainUI
				.getInstance()
				.showGlassDialog(
						"No action available!",
						"The currently selected frame does not have any"
								+ " provided objects which could be used by any available plugin. Please select another frame.");
	}

	public void showLauncher(Provider provider, ProvidedObject object) {
		if (getGlassPane().isVisible()) {
			return;
		}
		if (provider != null && object != null
				&& provider.getProvidedObjects().length > 0) {
			setGlassPane(launchPanel);
			launchPanel.revalidate();
			launchPanel.fadeIn(object);
		} else {
			MainUI
					.getInstance()
					.showGlassDialog(
							"No action available!",
							"The currently selected frame does not have any"
									+ " provided objects which could be used by any available plugin. Please select another frame.");
		}
	}

	public void showGlassDialog(String title, String message) {
		this.setGlassPane(dialogPanel);
		dialogPanel.revalidate();
		dialogPanel.showDialog(title, message);
	}

	public void resetGlassPane() {
		JPanel emptyGlass = new JPanel();
		emptyGlass.setOpaque(false);
		emptyGlass.setBorder(BorderFactory.createEmptyBorder());
		this.setGlassPane(emptyGlass);
	}

	/**
	 * Adds a frame to the desktop. This frame contains the JComponent result
	 * that was created by the analysisplugin algorithm on the given input.
	 * 
	 * @param algorithm
	 *            AnalysisPlugin
	 * @param input
	 *            AnalysisInputItem[]
	 * @param result
	 *            JComponent
	 */
	public void createAnalysisResultFrame(AnalysisPlugin algorithm,
			AnalysisInputItem[] input, JComponent result) {
		AnalysisFrame frame;
		if (result != null) {
			frame = new AnalysisFrame(algorithm, input, result);
			desktop.add(frame, algorithm);
		}
	}

	/**
	 * Returns the desktop of this user interface,
	 * 
	 * @return JDesktopPane
	 */
	public MDIDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Adds a frame to the desktop. This frame contains the JComponent result
	 * that was created by the conversionPlugin algorithm.
	 * 
	 * @param algorithm
	 *            ConvertingPlugin
	 * @param result
	 *            MiningResult
	 */
	public void createConversionResultFrame(ConvertingPlugin algorithm,
			MiningResult result) {
		ConversionFrame frame;
		frame = new ConversionFrame(algorithm, result);
		desktop.add(frame, algorithm);
	}

	/**
	 * Adds a frame to the desktop. This frame is split in two parts. The
	 * lefthand side contains the default options for a log file. The righthand
	 * side is defined in the getOptionsPanel() method of the NoMiningAction
	 * MiningPlugin. Note that in this method the whole log is read once to
	 * construct a summary required for the options panel.
	 * 
	 * @param file
	 *            LogFile
	 * @param algorithm
	 *            MiningPlugin
	 * @return OpenLogSettings A pointer to the frame that was creates (used in
	 *         setParameters)
	 */
	public OpenLogSettings createOpenLogFrame(LogFile file) {
		StopWatch timer = new StopWatch();
		timer.start();
		OpenLogSettings settings = new SlickerOpenLogSettings(file);
		settings.setVisible(true);
		desktop.add(settings);
		UISettings.getInstance().setLastOpenedLogFile(file.toString());
		try {
			settings.setSelected(true);
		} catch (PropertyVetoException e) {
			// no big deal...
			e.printStackTrace();
		}
		timer.stop();
		Message.add("Loaded log in " + timer.formatDuration());
		return settings;
	}

	/**
	 * Called to import a given file by a given ImportPlugin. This method is
	 * called by the import menu.
	 * 
	 * @param algorithm
	 *            ImportPlugin
	 * @param filename
	 *            String
	 * @param log
	 *            LogReader to connect to (can be null if none selected). If not
	 *            null, the algorithm should be instance of
	 *            LogReaderConnectionImportPlugin.
	 */
	public void importFromFile(ImportPlugin algorithm, String filename,
			LogReader log) {
		InputStream in = null;
		MiningResult result = null;
		boolean connected = false;
		if (filename != null && !filename.equals("")) {
			try {
				in = new ProMInputStream(filename);
				MainUI.getInstance().addAction(algorithm,
						LogStateMachine.START, new Object[] { filename });
				boolean shouldFuzzyMatch = true;
				try {
					ImportPlugin pl;

					if (algorithm instanceof DoNotCreateNewInstance) {
						pl = algorithm;
					} else {
						pl = (ImportPlugin) algorithm.getClass().newInstance();
					}

					result = pl.importFile(in);
					if (pl instanceof LogReaderConnectionImportPlugin) {
						shouldFuzzyMatch = ((LogReaderConnectionImportPlugin) pl)
								.shouldFindFuzzyMatch();
					}
				} catch (IllegalAccessException ex2) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					Message.add(ex2.getMessage(), Message.ERROR);
					result = algorithm.importFile(in);
				} catch (InstantiationException ex2) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					Message.add(ex2.getMessage(), Message.ERROR);
					result = algorithm.importFile(in);
				}
				MainUI.getInstance().addAction(algorithm,
						LogStateMachine.COMPLETE, new Object[] { filename });

				connected = true;
				if (result != null && (log != null)) {
					connected = connectResultWithLog(
							(LogReaderConnection) result, log, algorithm,
							shouldFuzzyMatch, false);
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				connected = false;
			}
			if (connected) {
				createVisualizationFrame("Imported - "
						+ (new File(filename)).getName() + " - "
						+ algorithm.getName(), result, algorithm);
				Message.add("Import of " + filename + " successful");
			} else {
				Message.add("Import of " + filename + " aborted.");
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException ex1) {
				}
			}
		}
	}

	/**
	 * Connects a (discovered or imported) model with a log reader. Used in
	 * HNLogReaderConnectionPlugin, PetriNetLogReaderConnectionPlugin, and
	 * MainUI.importFromFile (when a model is connected to log during import).
	 * 
	 * @param conn
	 * @param newLog
	 * @param plugin
	 * @return
	 */
	public boolean connectResultWithLog(LogReaderConnection conn,
			LogReader newLog, Plugin plugin, boolean shouldFindFuzzyMatch,
			boolean autoConnect) {
		boolean success = false;
		LogFile logFile = newLog.getFile();
		UISettings.getInstance().setLastOpenedLogFile(logFile.toString());
		if (UISettings.getInstance().getTest()) {
			Message.add("<ConnectWithLogReader>", Message.TEST);
		}
		Object[] ol = conn.getConnectableObjects().toArray();
		if (UISettings.getInstance().getTest()) {
			Message.add("  <connectableobjects " + ol.length + ">",
					Message.TEST);
		}
		Comparator<Object> c = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}

			public boolean equals(Object obj) {
				return this == obj;
			}
		};
		Arrays.sort(ol, c);
		ArrayList<Object> importedObjects = new ArrayList<Object>();
		for (int j = 0; j < ol.length; j++) {
			importedObjects.add(ol[j]);
		}
		boolean yesOrCancel = false;
		while (!yesOrCancel) {
			yesOrCancel = true;
			ImportSettings settings = new ImportSettings(newLog, plugin
					.getName(), importedObjects, shouldFindFuzzyMatch);
			if (UISettings.getInstance().getTest()) {
				Message.add("  <mappedObjects "
						+ settings.getMapping().keySet().size() + ">",
						Message.TEST);
			}

			if (autoConnect || settings.showModal()) {
				// connect with chosen log reader and map log events
				int i = settings.isLogReaderReady(autoConnect);
				if (i == ImportSettings.READY) {
					// user pressed yes
					conn.connectWith(settings.getLogReader(), settings
							.getMapping());
					success = true;
				} else if (i == ImportSettings.ABORT) {
					// user pressed no
					yesOrCancel = false;
					success = false;
				} // user pressed cancel
			}
		}
		if (UISettings.getInstance().getTest()) {
			Message.add("</ConnectWithLogReader>", Message.TEST);
		}
		return success;
	}

	/**
	 * Adds a frame to the desktop with the given title. The content of the
	 * frame is the given MiningResult.
	 * 
	 * @param title
	 *            String
	 * @param result
	 *            MiningResult
	 * @param plugin
	 *            Plugin representing the plugin that created this frame
	 */
	public void createVisualizationFrame(String title, MiningResult result,
			Plugin plugin) {
		if (result == null) {
			return;
		}
		JComponent comp = result.getVisualization();
		if (comp != null) {
			VisualizationUI frame = new VisualizationUI(title, result, comp);
			desktop.add(frame, plugin);
		}
	}

	public void createFrame(String title, JComponent comp) {
		ComponentFrame frame = new ComponentFrame(title, comp);
		frame.setVisible(true);
		desktop.add(frame);
		try {
			frame.setSelected(true);
		} catch (PropertyVetoException ex) {
		}
	}

	// added by Mariska Netjes
	public ComponentFrame createAndReturnFrame(String title, JComponent comp) {
		ComponentFrame frame = new ComponentFrame(title, comp);
		frame.setVisible(true);
		desktop.add(frame);
		try {
			frame.setSelected(true);
		} catch (PropertyVetoException ex) {
		}
		return frame;
	}

	/**
	 * Adds an action to the history of this execution of the framework
	 * 
	 * @param plugin
	 *            The plugin called
	 * @param eventType
	 *            The eventType of the action. Preferably, this String should
	 *            refer to one of the public constants in
	 *            org.processmining.framework.log.LogStateMachine
	 */
	public void addAction(String actionName, String eventType,
			Object[] parameters) {
		if (!keepLog.isSelected()) {
			return;
		}
		DataSection data = new DataSection();
		if ((parameters != null) && (parameters.length > 0)) {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] == null) {
					data.put("par [" + i + "] class", "null");
					data.put("par [" + i + "] string", "null");
					data.put("par [" + i + "] hashcode", "null");
				} else {
					String cn = parameters[i].getClass().getName();
					data.put("par [" + i + "] class", cn.substring(cn
							.lastIndexOf(".") + 1));
					data
							.put("par [" + i + "] string", parameters[i]
									.toString());
					data.put("par [" + i + "] hashcode", ""
							+ parameters[i].hashCode());
				}
			}
		}
		AuditTrailEntry ate = new AuditTrailEntryImpl(data, actionName,
				eventType, new Date(), System.getProperty("user.name"), null,
				null, null);
		try {
			historyPI.getAuditTrailEntryList().append(ate);
		} catch (IOException ex) {
			// Could not write to the history
			Message.add("Failed to write event to event history.",
					Message.ERROR);
		}
	}

	public void addAction(Plugin plugin, String eventType, Object[] parameters) {
		if (!keepLog.isSelected()) {
			return;
		}
		String actionName = "No plugin";
		if (plugin != null) {
			if (MiningPluginCollection.getInstance().isValidPlugin(plugin)) {
				actionName = "Mining: " + plugin.getName();
			} else if (ExportPluginCollection.getInstance().isValidPlugin(
					plugin)) {
				actionName = "Export: " + plugin.getName();
			} else if (AnalysisPluginCollection.getInstance().isValidPlugin(
					plugin)) {
				actionName = "Analyze: " + plugin.getName();
			} else if (ConvertingPluginCollection.getInstance().isValidPlugin(
					plugin)) {
				actionName = "Convert: " + plugin.getName();
			} else if (ImportPluginCollection.getInstance().isValidPlugin(
					plugin)) {
				actionName = "Import : " + plugin.getName();
			}
		}
		addAction(actionName, eventType, parameters);
	}

	/**
	 * Adds a provided object to the list of global provided objects .
	 * 
	 * @param object
	 *            The ProvidedObject to add to the global provided objects.
	 */
	public void addGlobalProvidedObject(ProvidedObject object) {
		globalProvidedObjects.add(object);
	}

	/**
	 * This returns the global providedObjects known to the framework
	 * 
	 * @return ProvidedObject[]
	 */
	public ProvidedObject[] getGlobalProvidedObjects() {
		return (ProvidedObject[]) globalProvidedObjects
				.toArray(new ProvidedObject[0]);
	}

	/**
	 * This returns the providedObjects known to the framework as well as the
	 * provided objects of the selected frame.
	 * 
	 * @return ProvidedObject[]
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] po = new ProvidedObject[0];
		if (desktop.getSelectedFrame() instanceof Provider) {
			po = ((Provider) desktop.getSelectedFrame()).getProvidedObjects();
		} else {
			po = new ProvidedObject[0];
		}

		// Set the length to po.length + the number of global objects.
		int lpo = (po == null ? 0 : po.length);
		int lgo = globalProvidedObjects.size();
		ProvidedObject[] output = new ProvidedObject[lpo + lgo];
		if (po != null) {
			System.arraycopy(po, 0, output, 0, lpo);
		}
		System.arraycopy(globalProvidedObjects.toArray(), 0, output, lpo, lgo);
		// Now add providedObjects to output
		return output;
	}

	/**
	 * setParameters sets the input parameters used by the main() method of ProM
	 * Note that it is assumed that logArgument is not empty;
	 * 
	 * @param miningPluginArgument
	 *            String to specify the algorithm to load. This can be the
	 *            getName() value, or the classname
	 * @param analysisPluginArgument
	 *            String to specify the algorithm to load. This can be the
	 *            getName() value, or the classname
	 * @param logArgument
	 *            String to specify the log file to read. Can start with
	 *            "zip://" etc for loading from zip.
	 * @param processArgument
	 *            String to specify the process to start mining immediately.
	 *            Note that if this argument is not empty, the framework will
	 *            include all events in the given process of the given log.
	 */
	public void setParameters(final String miningPluginArgument,
			final String analysisPluginArgument, final String logArgument) {
		// Either analysisPluginArgument=="" or miningPluginArgument==""
		SwingWorker worker = new SwingWorker() {
			private LogFile file;
			private Plugin plugin;
			private LogFilter filter;

			public Object construct() {
				// Find the mining algorithm to instantiate
				try {
					if (!miningPluginArgument.equals("")) {
						plugin = MiningPluginCollection.getInstance().get(
								miningPluginArgument);
					} else {
						plugin = AnalysisPluginCollection.getInstance().get(
								analysisPluginArgument);
					}
					if (!(plugin instanceof DoNotCreateNewInstance)) {
						plugin = (Plugin) plugin.getClass().newInstance();
					}
				} catch (InstantiationException ex) {
					Message.add(
							"Could not create a new instance of the selected algorithm: "
									+ miningPluginArgument
									+ analysisPluginArgument, Message.ERROR);
				} catch (IllegalAccessException ex) {
					Message.add(
							"Could not create a new instance of the selected algorithm: "
									+ miningPluginArgument
									+ analysisPluginArgument, Message.ERROR);
				} catch (NullPointerException ex) {
					Class pluginClass = null;
					try {
						pluginClass = Class.forName(miningPluginArgument
								+ analysisPluginArgument, true, Thread
								.currentThread().getContextClassLoader());
						plugin = (Plugin) pluginClass.newInstance();
					} catch (ClassNotFoundException ex1) {
						Message
								.add(
										"Could not create a new instance of the selected algorithm: "
												+ miningPluginArgument
												+ analysisPluginArgument,
										Message.ERROR);
					} catch (IllegalAccessException ex2) {
						Message
								.add(
										"Could not create a new instance of the selected algorithm: "
												+ miningPluginArgument
												+ analysisPluginArgument,
										Message.ERROR);
					} catch (InstantiationException ex2) {
						Message
								.add(
										"Could not create a new instance of the selected algorithm: "
												+ miningPluginArgument
												+ analysisPluginArgument,
										Message.ERROR);
					}
				}

				try {
					file = LogFile.getInstance(logArgument);
					file.getInputStream();
				} catch (Exception e) {
					file = null;
				}
				if (!logArgument.equals("") && file == null) {
					if (plugin != null && plugin instanceof AnalysisPlugin) {
						// Instantiate an empty log file for the analysisplugin
						try {
							file = LogFile.instantiateEmptyLogFile(logArgument);
							filter = new DefaultLogFilter(
									DefaultLogFilter.INCLUDE);
						} catch (IOException ex3) {
							Message.add("Cannot create empty Log file: "
									+ logArgument, Message.ERROR);
							file = null;
							filter = null;
						}
					}
				} else if (file != null) {
					// An existing log file has to be opened
					// now open the settings frame
					OpenLogSettings frame = MainUI.getInstance()
							.createOpenLogFrame(file);
					// and wait for the providedObjects to appear.
					while (frame.getProvidedObjects() == null
							|| frame.getProvidedObjects().length == 0) {
						try {
							sleep(1000);
						} catch (InterruptedException ex4) {
							// don't care.
							break;
						}
					}
					filter = frame.getLogFilter();
				}
				return null;
			}

			public void finished() {
				if (plugin == null && file == null) {
					// nothing found
					return;
				}
				// No process given, so mining does not start yet
				// final LogFile file = (LogFile) ((Object[]) get())[0];
				// The first object equals LogFile file
				// Two objects, which are {LogFile file, Plugin plugin}
				Message.add("Selected log file succesfully loaded.");
				// if (((Object[]) get()).length == 1) {
				// // we are done
				// return;
				// }
				// Now start looking for the process

				// final Plugin plugin = (Plugin) ((Object[]) get())[1];
				SwingWorker w = new SwingWorker() {
					public Object construct() {
						MainUI.getInstance().addAction(plugin,
								LogStateMachine.START, new Object[] { file });
						LogReader log = null;
						try {
							if (file != null) {
								log = LogReaderFactory.createInstance(filter,
										file);
							}
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
						if (!miningPluginArgument.equals("")) {
							return ((MiningPlugin) plugin).mine(log);
						} else {
							AnalysisInputItem item = new AnalysisInputItem(
									"Provided Log file");
							ProvidedObject logObject = new ProvidedObject(
									"Log file",
									(log == null ? new Object[] { STARTEDFROMCOMMANDLINE }
											: new Object[] { log,
													STARTEDFROMCOMMANDLINE }));
							item
									.setProvidedObjects(new ProvidedObject[] { logObject });
							return ((AnalysisPlugin) plugin)
									.analyse(new AnalysisInputItem[] { item });
						}
					}

					public void finished() {
						if (get() != null) {
							MainUI.getInstance().addAction(
									plugin,
									LogStateMachine.COMPLETE,
									new Object[] { ((Provider) get())
											.getProvidedObjects() });

							if (get() instanceof MiningResult) {
								MainUI.getInstance().createVisualizationFrame(
										"Results - " + logArgument + " using "
												+ miningPluginArgument,
										(MiningResult) get(), plugin);
							} else if (get() instanceof JComponent) {
								MainUI.getInstance().createFrame(
										"Results - " + logArgument + " using "
												+ analysisPluginArgument,
										(JComponent) get());
							}
						}
					}
				};
				w.start();

			}
		};
		worker.start();
	}

	private synchronized void flushHistory() {
		try {
			String fn = UISettings.getInstance().getExecutionLogFileName();
			Message.add("Writing execution log to file");
			Archive zipArchive = new Archive(fn + ".zip");
			OutputStream executionLog = zipArchive.output_stream_for(fn
					+ " content.xml", true);
			(new SAMXMLLogExport()).writeProcessInstance(historyPI,
					executionLog);
			executionLog.flush();
			executionLog.close();
			zipArchive.close();
		} catch (InterruptedException ex1) {
			Message.add("Could not write execution log", Message.ERROR);
		} catch (IOException ex) {
			Message.add("Could not write execution log", Message.ERROR);
		}
	}

	private void initializeHistoryInstance(String description, DataSection map) {
		try {
			initializeHistoryInstance(description, map,
					new AuditTrailEntryListImpl());
		} catch (IOException ex) {
			// COuld not create history
			Message.add("Failed to initialize new instance for history.",
					Message.ERROR);
		}
	}

	private void initializeHistoryInstance(String description, DataSection map,
			AuditTrailEntryListImpl entries) {
		int x = Integer.parseInt(UISettings.getInstance().getLastExecutionID());
		historyPI = new ProcessInstanceImpl(PROCESS, entries,
				new ArrayList<String>());
		historyPI.setName("Execution_" + (x + 1));
		historyPI
				.setDescription(description.equals("") ? "Execution of "
						+ About.NAME + " started on: "
						+ new Date(System.currentTimeMillis()).toString()
						: description);
		historyPI.setAttributes(map);
		UISettings.getInstance().setLastExecutionID("" + (x + 1));
	}

	class PluginReferenceAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = -8188862992562622955L;

		public PluginReferenceAction() {
			super("Plugin reference...", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION, "Info about all available plugins");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		}

		public void execute(ActionEvent e) {
			reference.show(PluginReference.INDEX);
		}

		public void handleOutOfMem() {

		}
	}

	class ParametersAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = 7251235522376726283L;

		public ParametersAction() {
			super("CMD-Parameters", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION,
					"Info about the possible command line options.");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
		}

		public void execute(ActionEvent e) {
			final JDialog dialog = new JDialog(MainUI.this,
					"Command line parameters", true);

			String args = About.getCommandLineArgumentsHTML();
			JLabel argLabel = new JLabel(args);

			JButton okButton = new JButton("    Ok    ");

			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			});

			dialog.getContentPane().setLayout(new GridBagLayout());

			dialog.getContentPane().add(
					argLabel,
					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE,
							new Insets(5, 5, 5, 5), 0, 0));
			dialog.getContentPane().add(
					okButton,
					new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.NONE,
							new Insets(15, 5, 5, 5), 0, 0));

			dialog.pack();
			CenterOnScreen.center(dialog);
			dialog.setVisible(true);
		}

		public void handleOutOfMem() {
		}
	}

	class AboutAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = -5045915200909781363L;

		public AboutAction() {
			super("About...", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION, "Info about the " + About.NAME);
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		}

		public void execute(ActionEvent e) {
			About about = new About(MainUI.this);

			about.setVisible(true);
		}

		public void handleOutOfMem() {
		}
	}

	class ShowHistoryAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = -2191150802460778447L;

		public ShowHistoryAction() {
			super("Show History", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION,
					"Shows execution history of current session in the Message window");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		public void execute(ActionEvent e) {
			Message.add("History of current ProM session:");
			Message.add(historyPI.toString());
		}

		public void handleOutOfMem() {
		}
	}

	class StartNewHistoryPIAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = 1082606211828220272L;

		public StartNewHistoryPIAction() {
			super("Reinitialize History", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION,
					"Closes and saves the current history process instance and starts a new one");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void execute(ActionEvent e) {
			flushHistory();
			// reinitiate the history
			Integer.parseInt(UISettings.getInstance().getLastExecutionID());
			initializeHistoryInstance("", new DataSection());
		}

		public void handleOutOfMem() {
		}
	}

	class GetRecommendationAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = -1268931637394290842L;

		public GetRecommendationAction() {
			super("Recommend Next Action", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION,
					"Asks a recommendation engine for a recommended next step");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		}

		protected void execute(ActionEvent e) {
			RecommendationProvider provider = new RecommendationProviderProxy(
					"localhost", RECOM_PORT);
			RecommendationQuery query = new RecommendationQuery("ProcessId",
					"ProcessInstanceId");
			query.setProcessInstanceData(historyPI.getAttributes());
			Iterator it = historyPI.getAuditTrailEntryList().iterator();
			while (it.hasNext()) {
				query.addAuditTrailEntry((AuditTrailEntry) it.next());
			}
			RecommendationResult result = null;
			try {
				result = provider.getRecommendation(query);
				for (Recommendation r : result) {
					Message.add(r.toString());
				}
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				Message.add("Could not find the recommendation provider.",
						Message.ERROR);
				ex.printStackTrace();
			}
		}

		protected void handleOutOfMem() {
		}
	}

	class StartRecommendationAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = -4298583707987560007L;

		public StartRecommendationAction() {
			super("Start/Stop Recommendation Engine", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION, "Starts the recommendation engine");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		protected void execute(ActionEvent e) {
			if (recomRunning) {
				recomService.stop();
				recomRunning = false;
				Message.add("Recommendation Service Stopped");
				getRecomAction.setEnabled(false);
			} else {
				LogBasedContributor logBasedContributor;
				try {
					logBasedContributor = (LogBasedContributor) RecommendationCollection
							.getInstance().getByKey("default");
				} catch (ClassCastException ex) {
					Message
							.add("Cannot instantiate default recommendation engine, since it is not"
									+ " a log based engine.");
					Message.add("Make sure there is one with key \"default\"");
					return;
				}
				if (logBasedContributor == null) {
					Message.add("Cannot find default recommendation engine.");
					Message.add("Make sure there is one with key \"default\"");
					return;
				}

				ProcessInstanceScale scale = (ProcessInstanceScale) ScaleCollection
						.getInstance().getByKey("default");
				if (scale == null) {
					Message.add("Cannot find default process instance scale.");
					Message.add("Make sure there is one with key \"default\"");
					return;
				}
				// We only deal with log based recommendations for now
				try {
					File tempfile = File.createTempFile(
							"ProM_recommendation_temp", ".zip");
					new MainUIExportHistoryAction(MainUI.this.desktop).execute(
							e, tempfile.getAbsolutePath());

					DefaultLogFilter filter = new DefaultLogFilter(
							DefaultLogFilter.INCLUDE);
					// filter.filterEventType(LogStateMachine.SCHEDULE,
					// DefaultLogFilter.INCLUDE);
					// filter.filterEventType(LogStateMachine.START,
					// DefaultLogFilter.INCLUDE);

					// zip\://C\:\\Documents and Settings\\bfvdonge\\Local
					// Settings\\Temp\\ProM_recommendation_temp61748.zip\#C\:\\Documents
					// and Settings\\bfvdonge\\ProM execution log content.xml

					String name = "zip://";
					name += tempfile.getAbsolutePath();
					name += "#";
					name += UISettings.getInstance().getExecutionLogFileName();
					name += " content.xml";

					LogFile file = LogFile.getInstance(name);

					LogReader logReader = LogReaderFactory.createInstance(
							filter, file);
					logBasedContributor.initialize(logReader, scale);

					RecommendationServiceHandler handler = new RecommendationServiceHandler(
							new MainUIRecomProvider(logBasedContributor));

					recomService = new Service(RECOM_PORT, handler);
					recomService.start();

					recomRunning = true;
					Message.add("Recommendation Service Running");
					getRecomAction.setEnabled(true);

				} catch (Exception ex3) {
					Message.add(ex3.toString(), Message.ERROR);
					Message
							.add("Cannot instantiate logReader on copy of the history log (does it exist)");
				}
			}
			helpMenu.validate();

		}

		protected void handleOutOfMem() {
		}
	}

	class ResetHistoryAction extends CatchOutOfMemoryAction {
		private static final long serialVersionUID = 5084018315910978583L;

		public ResetHistoryAction() {
			super("Reset execution history", MainUI.this.desktop);
			putValue(SHORT_DESCRIPTION, "Resets execution history");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void execute(ActionEvent e) {
			int i = JOptionPane
					.showConfirmDialog(
							null,
							"Are you sure you want to reset the execution log? \n"
									+ "All previous executions will be lost, including\n"
									+ "the history of the current session!\n",
							"Confirm execution log reset",
							JOptionPane.YES_NO_OPTION);
			if (i == JOptionPane.NO_OPTION) {
				return;
			}
			if (i == JOptionPane.YES_OPTION) {
				UISettings.getInstance().setLastExecutionID("-1");

				initializeHistoryInstance(historyPI.getDescription(), historyPI
						.getDataAttributes());

				String fn = UISettings.getInstance().getExecutionLogFileName();
				if ((new File(fn + ".zip").delete())) {
					buildNewExecutionLog(fn);
				}
			}
		}

		public void handleOutOfMem() {
		}
	}

	class MainUIExportHistoryAction extends ExportHistoryAction {
		private static final long serialVersionUID = 5839202913524155590L;

		public MainUIExportHistoryAction(MDIDesktopPane desktop) {
			super("Export History", desktop);
			putValue(SHORT_DESCRIPTION,
					"Exports execution history to XML Log File");
			putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		}

		public void execute(ActionEvent e, String filename) {
			flushHistory();
			super.executeWithFile(e, filename);
			initializeHistoryInstance(historyPI.getDescription(), historyPI
					.getDataAttributes(), (AuditTrailEntryListImpl) historyPI
					.getAuditTrailEntryList());
		}

		public void execute(ActionEvent e) {
			flushHistory();
			super.execute(e);
			initializeHistoryInstance(historyPI.getDescription(), historyPI
					.getDataAttributes(), (AuditTrailEntryListImpl) historyPI
					.getAuditTrailEntryList());
		}
	}

	class HistoryCheckBoxActionListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			Message.add("click", Message.DEBUG);
			// value changed.
			helpMenu.getItem(7).setEnabled(
					e.getStateChange() == ItemEvent.SELECTED);
			helpMenu.getItem(8).setEnabled(
					e.getStateChange() == ItemEvent.SELECTED);
			helpMenu.getItem(9).setEnabled(
					e.getStateChange() == ItemEvent.SELECTED);
			helpMenu.getItem(10).setEnabled(
					e.getStateChange() == ItemEvent.SELECTED);
			UISettings.getInstance().setKeepHistory(
					e.getStateChange() == ItemEvent.SELECTED);
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// from false to true
				initializeHistoryInstance("", new DataSection());
				String fn = UISettings.getInstance().getExecutionLogFileName();
				if (!(new File(fn + ".zip").exists())) {
					buildNewExecutionLog(fn);
				}
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				// from true to false
				flushHistory();
			}
		}
	}

	public void toggleNavigationPanelVisible() {
		if (desktopAndNavigation != null) {
			navigationPanel.setVisible(false);
			contentPanel.remove(desktopAndNavigation);
			desktopAndNavigation = null;
			contentPanel.add(desktop, BorderLayout.CENTER);
			contentPanel.revalidate();
		} else {
			navigationPanel.setVisible(true);
			desktopAndNavigation = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					desktop, navigationPanel);
			desktopAndNavigation.setOneTouchExpandable(true);
			desktopAndNavigation.setResizeWeight(1);
			desktopAndNavigation.setDividerSize(3);
			desktopAndNavigation.setBorder(null);
			contentPanel.remove(desktop);
			contentPanel.add(desktopAndNavigation, BorderLayout.CENTER);
			contentPanel.revalidate();
			desktopAndNavigation.setDividerLocation(this.getWidth() - 300);
		}
	}
}

class MainUIRecomProvider implements RecommendationProvider {

	private RecommendationContributor contrib;

	public MainUIRecomProvider(RecommendationContributor contrib) {
		this.contrib = contrib;
	}

	public RecommendationResult getRecommendation(RecommendationQuery query)
			throws IOException, Exception {

		RecommendationResult result = contrib.generateRecommendations(query,
				MainUI.PROCESS);

		return result;
	}

	public void signalPickedResult(RecommendationResult result, int index) {
		// no implementation
	}

	public void signalPickedResult(RecommendationResult result,
			Recommendation picked) {
		// no implementation
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.recommendation.RecommendationProvider
	 * #transmitCompletedExecution
	 * (org.processmining.framework.log.ProcessInstance)
	 */
	public void handleCompletedExecution(ProcessInstance instance) {
		// no implementation
	}

	public void requestRestart(String contributor, String scale)
			throws Exception {
	}

	public void requestClose() throws Exception {
	}

}

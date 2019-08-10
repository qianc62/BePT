package org.processmining.analysis.petrinet.cpnexport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GuiUtilities;

/**
 * The layout manager maintains the grahical parameters that apply for the
 * generation of CPN simulation models.
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class ManagerLayout {

	/** The default setting for the scale factor */
	public final static int DEFAULT_SCALE_FACTOR = 2;

	/** the default setting for the stretch factor */
	public final static int DEFAULT_STRETCH_FACTOR = 110;

	// GUI attributes
	private JLabel layoutInstructions;

	private GUIPropertyInteger myPlace_Type_Offset_X = new GUIPropertyInteger(
			"Place Type (X-coordinate)",
			"X Offset for the \'color\' of a place", 52, -100, 100);

	/**
	 * Offset specifying the displacement of the type label for the place on the
	 * x axis. The reference point is the center position of the place itself
	 * and the value is taken from the CPN Tools default position of the type
	 * label, if the type is CASE_ID.
	 */
	public int getPlaceTypeOffset_X() {
		return myPlace_Type_Offset_X.getValue();
	}

	private GUIPropertyInteger myPlace_Type_Offset_Y = new GUIPropertyInteger(
			"Place Type (Y-coordinate)",
			"Y Offset for the \'color\' of a place", -24, -100, 100);

	/**
	 * Offset specifying the displacement of the type label for a place on the y
	 * axis. The reference point is the center position of the place itself and
	 * the value is taken from the corresponding CPN Tools default position.
	 */
	public int getPlaceTypeOffset_Y() {
		return myPlace_Type_Offset_Y.getValue();
	}

	private GUIPropertyInteger myPlace_InitMark_Offset_X = new GUIPropertyInteger(
			"Initial Marking (X-coordinate)",
			"X Offset for the initial marking of a place", 57, -100, 100);

	/**
	 * Offset specifying the displacement of the initial marking label for the
	 * place on the x axis. The reference point is the center position of the
	 * place itself and the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	public int getPlaceInitMarkOffset_X() {
		return myPlace_InitMark_Offset_X.getValue();
	}

	private GUIPropertyInteger myPlace_InitMark_Offset_Y = new GUIPropertyInteger(
			"Initial Marking (Y-coordinate)",
			"Y Offset for the initial marking of a place", 23, -100, 100);

	/**
	 * Offset specifying the displacement of the initial marking label for a
	 * place on the y axis. The reference point is the center position of the
	 * place itself and the the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	public int getPlaceInitMarkOffset_Y() {
		return myPlace_InitMark_Offset_Y.getValue();
	}

	private GUIPropertyInteger myPlace_Port_Offset_X = new GUIPropertyInteger(
			"Port (X-coordinate)", "X Offset for the port type tag of a place",
			-25, -100, 100);

	/**
	 * Offset specifying the displacement of the port type tag for the place on
	 * the x axis. The reference point is the center position of the place
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	public int getPlacePortOffset_X() {
		return myPlace_Port_Offset_X.getValue();
	}

	private GUIPropertyInteger myPlace_Port_Offset_Y = new GUIPropertyInteger(
			"Port (Y-coordinate)", "Y Offset for the port type tag of a place",
			-25, -100, 100);

	/**
	 * Offset specifying the displacement of the port type tag for the place on
	 * the y axis. The reference point is the center position of the place
	 * itself and the the value is taken from the corresponding CPN Tools
	 * default position.
	 */
	public int getPlacePortOffset_Y() {
		return myPlace_Port_Offset_Y.getValue();
	}

	private GUIPropertyInteger myPlace_Fusion_Offset_X = new GUIPropertyInteger(
			"Fusion (X-coordinate)",
			"X Offset for the fusion set tag of a place", -50, -150, 100);

	/**
	 * Offset specifying the displacement of the fusion name tag for the place
	 * on the x axis. The reference point is the center position of the place
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	public int getPlaceFusionOffset_X() {
		return myPlace_Fusion_Offset_X.getValue();
	}

	private GUIPropertyInteger myPlace_Fusion_Offset_Y = new GUIPropertyInteger(
			"Fusion (Y-coordinate)",
			"Y Offset for the fusion set tag of a place", -25, -100, 100);

	/**
	 * Offset specifying the displacement of the fusion name tag for the place
	 * on the y axis. The reference point is the center position of the place
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	public int getPlaceFusionOffset_Y() {
		return myPlace_Fusion_Offset_Y.getValue();
	}

	private GUIPropertyInteger myTransition_Cond_Offset_X = new GUIPropertyInteger(
			"Guard Condition (X-coordinate)",
			"X Offset for the guard condition of a transition", -39, -100, 100);

	/**
	 * Offset specifying the displacement of the guard condition label for a
	 * transition on the x axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	public int getTransitionConditionOffset_X() {
		return myTransition_Cond_Offset_X.getValue();
	}

	private GUIPropertyInteger myTransition_Cond_Offset_Y = new GUIPropertyInteger(
			"Guard Condition (Y-coordinate)",
			"Y Offset for the guard condition of a transition", 31, -100, 100);

	/**
	 * Offset specifying the displacement of the guard condition label for a
	 * transition on the y axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	public int getTransitionConditionOffset_Y() {
		return myTransition_Cond_Offset_Y.getValue();
	}

	private GUIPropertyInteger myTransition_Time_Offset_X = new GUIPropertyInteger(
			"Time (X-coordinate)",
			"X Offset for the time delay inscription of a transition", 45,
			-100, 100);

	/**
	 * Offset specifying the displacement of the time delay label for a
	 * transition on the x axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	public int getTransitionTimeOffset_X() {
		return myTransition_Time_Offset_X.getValue();
	}

	private GUIPropertyInteger myTransition_Time_Offset_Y = new GUIPropertyInteger(
			"Time (Y-coordinate)",
			"Y Offset for the time delay inscription of a transition", 31,
			-100, 100);

	/**
	 * Offset specifying the displacement of the time delay label for a
	 * transition on the y axis. The reference point is the center position of
	 * the transition itself and the value is taken from the corresponding CPN
	 * Tools default position.
	 */
	public int getTransitionTimeOffset_Y() {
		return myTransition_Time_Offset_Y.getValue();
	}

	private GUIPropertyInteger myTransition_Code_Offset_X = new GUIPropertyInteger(
			"Code (X-coordinate)",
			"X Offset for the code inscription of a transition", 65, -100, 100);

	/**
	 * Offset specifying the displacement of the code specification for a
	 * transition on the x axis (that is input, output, action). The reference
	 * point is the center position of the transition itself and the value is
	 * taken from the corresponding CPN Tools default position.
	 */
	public int getTransitionCodeOffset_X() {
		return myTransition_Code_Offset_X.getValue();
	}

	private GUIPropertyInteger myTransition_Code_Offset_Y = new GUIPropertyInteger(
			"Code (Y-coordinate)",
			"Y Offset for the code inscription of a transition", -52, -100, 100);

	/**
	 * Offset specifying the displacement of the code specification for a
	 * transition on the y axis (that is input, output, action). The reference
	 * point is the center position of the transition itself and the value is
	 * taken from the corresponding CPN Tools default position.
	 */
	public int getTransitionCodeOffset_Y() {
		return myTransition_Code_Offset_Y.getValue();
	}

	private GUIPropertyInteger myTransition_Channel_Offset_X = new GUIPropertyInteger(
			"Channel (X-coordinate",
			"X Offset for the channel (not actually used)", -64, -100, 100);

	/**
	 * Offset specifying the displacement of the channel specification for a
	 * transition on the x axis (not used). The reference point is the center
	 * position of the transition itself and the value is taken from the
	 * corresponding CPN Tools default position.
	 */
	public int getTransitionChannelOffset_X() {
		return myTransition_Channel_Offset_X.getValue();
	}

	private GUIPropertyInteger myTransition_Channel_Offset_Y = new GUIPropertyInteger(
			"Channel (Y-coordinate)",
			"Y Offset for the channel (not actually used)", 0, -100, 100);

	/**
	 * Offset specifying the displacement of the channel specification for a
	 * transition on the y axis (not used). The reference point is the center
	 * position of the transition itself and the value is taken from the
	 * corresponding CPN Tools default position.
	 */
	public int getTransitionChannelOffset_Y() {
		return myTransition_Channel_Offset_Y.getValue();
	}

	private GUIPropertyInteger myTransition_Subpageinfo_Offset_X = new GUIPropertyInteger(
			"Subpage Tag (X-coordinate)",
			"X Offset for the sup page tag of a transition", 0, -100, 100);

	/**
	 * Offset specifying the displacement of the subpage tag for a transition on
	 * the x axis). The reference point is the center position of the transition
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	public int getTransitionSubpageinfoOffset_X() {
		return myTransition_Subpageinfo_Offset_X.getValue();
	}

	private GUIPropertyInteger myTransition_Subpageinfo_Offset_Y = new GUIPropertyInteger(
			"Subpage Tag (Y-coordinate)",
			"Y Offset for the sup page tag of a transition", -32, -100, 100);

	/**
	 * Offset specifying the displacement of the subpage tag for a transition on
	 * the y axis. The reference point is the center position of the transition
	 * itself and the value is taken from the corresponding CPN Tools default
	 * position.
	 */
	public int getTransitionSubpageinfoOffset_Y() {
		return myTransition_Subpageinfo_Offset_Y.getValue();
	}

	private GUIPropertyInteger myScaleFactor = new GUIPropertyInteger(
			"Scale Factor", "Influences the node distance",
			DEFAULT_SCALE_FACTOR, 1, 50);

	/**
	 * Constant factor used to scale the nodes further away from each other.
	 */
	public int getScaleFactor() {
		return myScaleFactor.getValue();
	}

	private GUIPropertyInteger myStretchFactor = new GUIPropertyInteger(
			"Stretch Factor", "Influences the size of the nodes",
			DEFAULT_STRETCH_FACTOR, 1, 1000);

	/**
	 * Constant factor used to scale the nodes further away from each other.
	 */
	public int getStretchFactor() {
		return myStretchFactor.getValue();
	}

	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * Private constructor to prevent the creation of more than one layout
	 * manager object (Singleton pattern). Use {@link #getInstance()} instead to
	 * retrieve the single object available.
	 */
	private ManagerLayout() {
	}

	private static ManagerLayout myInstance = new ManagerLayout();

	/**
	 * Retrieves the only instance of this Layout manager (Singleton pattern).
	 * Note that this means that the effect of changing a layout setting will
	 * not only last for one export session, but for one whole session of ProM.
	 * 
	 * @return the global layout manager
	 */
	public static ManagerLayout getInstance() {
		return myInstance;
	}

	/**
	 * Creates GUI panel containg the available layout settings, ready to
	 * display in some settings dialog.
	 * 
	 * @return the graphical panel representing the available layout settings
	 *         for the CPN Tools export
	 */
	public JPanel getOptionsPanel() {
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setOpaque(false);
		JPanel outmostLayer = new JPanel();
		outmostLayer.setOpaque(false);
		outmostLayer
				.setLayout(new BoxLayout(outmostLayer, BoxLayout.LINE_AXIS));
		JPanel layoutOptions = new JPanel();
		layoutOptions.setOpaque(false);
		layoutOptions.setLayout(new BoxLayout(layoutOptions,
				BoxLayout.PAGE_AXIS));
		// place offsets
		JLabel placeOffsetsLabel = new JLabel(
				"Offsets from center position of a Place:");
		placeOffsetsLabel.setForeground(new Color(100, 100, 100));
		layoutOptions.add(GuiUtilities.packLeftAligned(placeOffsetsLabel));
		layoutOptions.add(Box.createRigidArea(new Dimension(0, 5)));
		layoutOptions.add(myPlace_Type_Offset_X.getPropertyPanel());
		layoutOptions.add(myPlace_Type_Offset_Y.getPropertyPanel());
		layoutOptions.add(myPlace_InitMark_Offset_X.getPropertyPanel());
		layoutOptions.add(myPlace_InitMark_Offset_Y.getPropertyPanel());
		layoutOptions.add(myPlace_Port_Offset_X.getPropertyPanel());
		layoutOptions.add(myPlace_Port_Offset_Y.getPropertyPanel());
		layoutOptions.add(myPlace_Fusion_Offset_X.getPropertyPanel());
		layoutOptions.add(myPlace_Fusion_Offset_Y.getPropertyPanel());
		layoutOptions.add(Box.createRigidArea(new Dimension(0, 10)));
		// transition offsets
		JLabel transitionOffsetsLabel = new JLabel(
				"Offsets from center position of a Transition:");
		transitionOffsetsLabel.setForeground(new Color(100, 100, 100));
		layoutOptions.add(GuiUtilities.packLeftAligned(transitionOffsetsLabel));
		layoutOptions.add(Box.createRigidArea(new Dimension(0, 5)));
		layoutOptions.add(myTransition_Cond_Offset_X.getPropertyPanel());
		layoutOptions.add(myTransition_Cond_Offset_Y.getPropertyPanel());
		layoutOptions.add(myTransition_Time_Offset_X.getPropertyPanel());
		layoutOptions.add(myTransition_Time_Offset_Y.getPropertyPanel());
		layoutOptions.add(myTransition_Code_Offset_X.getPropertyPanel());
		layoutOptions.add(myTransition_Code_Offset_Y.getPropertyPanel());
		// layoutOptions.add(myTransition_Channel_Offset_X.getPropertyPanel());
		// layoutOptions.add(myTransition_Channel_Offset_Y.getPropertyPanel());
		layoutOptions.add(myTransition_Subpageinfo_Offset_X.getPropertyPanel());
		layoutOptions.add(myTransition_Subpageinfo_Offset_Y.getPropertyPanel());
		layoutOptions.add(Box.createRigidArea(new Dimension(0, 10)));
		// general parameters
		JLabel generalOptionsLabel = new JLabel("General options:");
		generalOptionsLabel.setForeground(new Color(100, 100, 100));
		layoutOptions.add(GuiUtilities.packLeftAligned(generalOptionsLabel));
		layoutOptions.add(Box.createRigidArea(new Dimension(0, 5)));
		layoutOptions.add(myScaleFactor.getPropertyPanel());
		layoutOptions.add(myStretchFactor.getPropertyPanel());
		layoutOptions
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		outmostLayer.add(Box.createHorizontalGlue());
		outmostLayer.add(layoutOptions);
		outmostLayer.add(Box.createHorizontalGlue());
		resultPanel.add(outmostLayer, BorderLayout.CENTER);
		return resultPanel;
	}

	/**
	 * Creates a panel containing both the layout options as well as the help
	 * figure.
	 * 
	 * @return the layout properties plus help
	 */
	public JPanel getPanel() {
		String layoutImg = System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator")
				+ "CpnExportLayoutInstructions.gif";
		ImageIcon icon = new ImageIcon(layoutImg);
		layoutInstructions = new JLabel(icon);
		layoutInstructions.setOpaque(false);
		JPanel jointPanel = new JPanel();
		jointPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jointPanel.setOpaque(false);
		jointPanel.setLayout(new GridLayout(1, 2));
		jointPanel
				.add(GuiUtilities
						.configureAnyScrollable(
								ManagerLayout.getInstance().getOptionsPanel(),
								"Properties",
								"Here you can influence the layout of the generated CPN model. "
										+ "The initial offset values correspond to the default offsets in CPN Tools.",
								new Color(190, 190, 190)));
		jointPanel
				.add(GuiUtilities
						.configureAnyScrollable(
								layoutInstructions,
								"Layout Help",
								"The following figure should help you to interpet the layout properties on the left side.",
								new Color(190, 190, 190)));
		return jointPanel;
	}

}

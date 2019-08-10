package org.processmining.framework.util;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.util.SlickerSwingUtils;

public class GuiUtilities {

	/**
	 * Puts the given component in a rounded panel, with the given title and
	 * description. Scrollbars will appear for the component if needed.
	 * 
	 * @param scrollable
	 *            the component to be made scrollable
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param bgColor
	 *            the background color of the rounded panel
	 * @return the panel containing the given component in a scrollpane and with
	 *         title and description
	 */
	public static JComponent configureAnyScrollable(JComponent scrollable,
			String title, String description, Color bgColor) {
		RoundedPanel enclosure = new RoundedPanel(10, 5, 5);
		enclosure.setBackground(bgColor);
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.Y_AXIS));
		JLabel headerLabel = new JLabel(title);
		headerLabel.setOpaque(false);
		headerLabel.setForeground(new Color(10, 10, 10));
		headerLabel.setFont(headerLabel.getFont().deriveFont(14f));
		JLabel descriptionLabel = new JLabel("<html>" + description + "</html>");
		descriptionLabel.setOpaque(false);
		descriptionLabel.setForeground(new Color(60, 60, 60));
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(11f));
		JScrollPane listScrollPane = new JScrollPane(scrollable);
		listScrollPane.setOpaque(false);
		listScrollPane.getViewport().setOpaque(false);
		listScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScrollPane.getVerticalScrollBar().setUI(
				new SlickerScrollBarUI(listScrollPane.getVerticalScrollBar(),
						bgColor, new Color(70, 70, 70),
						new Color(120, 120, 120), 2, 11));
		listScrollPane.getHorizontalScrollBar().setUI(
				new SlickerScrollBarUI(listScrollPane.getHorizontalScrollBar(),
						bgColor, new Color(70, 70, 70),
						new Color(120, 120, 120), 2, 11));
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		enclosure.add(packLeftAligned(headerLabel));
		enclosure.add(Box.createVerticalStrut(3));
		enclosure.add(packLeftAligned(descriptionLabel));
		enclosure.add(Box.createVerticalStrut(5));
		enclosure.add(listScrollPane);
		return enclosure;
	}

	/**
	 * Creates a slicker scrollbar around the given component and respects the
	 * given background color.
	 * 
	 * @param scrollable
	 *            the item to be made scrollable
	 * @param bgColor
	 *            the background color to be used for scrollbar
	 * @return the scrollable component
	 */
	public static JComponent getSimpleScrollable(JComponent scrollable,
			Color bgColor) {
		JScrollPane listScrollPane = new JScrollPane(scrollable);
		listScrollPane.setOpaque(false);
		listScrollPane.getViewport().setOpaque(false);
		listScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScrollPane.getVerticalScrollBar().setUI(
				new SlickerScrollBarUI(listScrollPane.getVerticalScrollBar(),
						bgColor, new Color(70, 70, 70),
						new Color(120, 120, 120), 2, 11));
		listScrollPane.getHorizontalScrollBar().setUI(
				new SlickerScrollBarUI(listScrollPane.getHorizontalScrollBar(),
						bgColor, new Color(70, 70, 70),
						new Color(120, 120, 120), 2, 11));
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		SlickerSwingUtils.injectTransparency(listScrollPane);
		return listScrollPane;
	}

	/**
	 * Packs the given component to the left.
	 * 
	 * @param component
	 *            the component to be aligned
	 * @return the panel containing the aligned component
	 */
	public static JComponent packLeftAligned(JComponent component) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(component);
		packed.add(Box.createHorizontalGlue());
		return packed;
	}

	/**
	 * Packs the given component to the right.
	 * 
	 * @param component
	 *            the component to be aligned
	 * @return the panel containing the aligned component
	 */
	public static JComponent packRightAligned(JComponent component) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalGlue());
		packed.add(component);
		return packed;
	}

	/**
	 * Surrounds the given component by horizontal glue from both sides.
	 * 
	 * @param component
	 *            the component to be placed in the center.
	 * @return the panel containing the aligned component
	 */
	public static JComponent packCenterHorizontally(JComponent component) {
		JPanel optPanel = new JPanel();
		optPanel.setOpaque(false);
		optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.X_AXIS));
		optPanel.add(Box.createHorizontalGlue());
		optPanel.add(component);
		optPanel.add(Box.createHorizontalGlue());
		return optPanel;
	}

	/**
	 * Surrounds the given component by vertical glue from both sides.
	 * 
	 * @param component
	 *            the component to be placed in the middle.
	 * @return the panel containing the aligned component
	 */
	public static JComponent packMiddleVertically(JComponent component) {
		JPanel optPanel = new JPanel();
		optPanel.setOpaque(false);
		optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
		optPanel.add(Box.createVerticalGlue());
		optPanel.add(component);
		optPanel.add(Box.createVerticalGlue());
		return optPanel;
	}

}

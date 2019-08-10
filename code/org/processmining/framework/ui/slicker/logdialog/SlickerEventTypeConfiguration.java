/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.framework.ui.slicker.logdialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.GradientPanel;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SlickerEventTypeConfiguration extends JPanel {

	protected static Color colorText = new Color(255, 255, 255, 160);
	protected static Color colorKeep1 = new Color(10, 90, 1);
	protected static Color colorKeep2 = new Color(20, 140, 20);
	protected static Color colorRemove1 = new Color(90, 60, 10);
	protected static Color colorRemove2 = new Color(140, 100, 20);
	protected static Color colorSkipInstance1 = new Color(90, 10, 10);
	protected static Color colorSkipInstance2 = new Color(140, 20, 20);

	public enum EventTypeAction {
		KEEP, REMOVE, SKIP_INSTANCE;
	}

	protected String[] eventTypes;
	protected EventTypeConfigurationItem[] configurationItems;
	protected ChangeListener updateListener = null;

	public SlickerEventTypeConfiguration(String[] eventTypes) {
		this.eventTypes = eventTypes;
		this.setBackground(new Color(60, 60, 60));
		this.setMinimumSize(new Dimension(70, 40));
		this.setMaximumSize(new Dimension(1000, 5000));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.configurationItems = new EventTypeConfigurationItem[eventTypes.length];
		int height = 0;
		for (int i = 0; i < eventTypes.length; i++) {
			this.configurationItems[i] = new EventTypeConfigurationItem(
					eventTypes[i]);
			this.add(this.configurationItems[i]);
			height += this.configurationItems[i].getPreferredSize().height + 2;
		}
		this.add(Box.createVerticalGlue());
		this.setPreferredSize(new Dimension(120, height));
		this.revalidate();
	}

	public void setUpdateListener(ChangeListener updateListener) {
		this.updateListener = updateListener;
	}

	public String[] getFilteredEventTypes(EventTypeAction action) {
		ArrayList<String> types = new ArrayList<String>();
		for (EventTypeConfigurationItem item : this.configurationItems) {
			if (item.getAction() == action) {
				types.add(item.getName());
			}
		}
		return types.toArray(new String[0]);
	}

	protected class EventTypeConfigurationItem extends GradientPanel {

		protected String name;
		protected EventTypeAction action;
		protected JLabel actionLabel;
		protected JLabel nameLabel;

		public EventTypeConfigurationItem(String type) {
			super(colorKeep2, colorKeep1);
			this.setMinimumSize(new Dimension(70, 28));
			this.setMaximumSize(new Dimension(500, 28));
			this.setPreferredSize(new Dimension(120, 28));
			this.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.name = type;
			this.nameLabel = new JLabel(type);
			this.nameLabel.setOpaque(false);
			this.nameLabel.setForeground(colorText);
			this.nameLabel.setVerticalAlignment(JLabel.CENTER);
			this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(13f));
			this.actionLabel = new JLabel("change_me");
			this.actionLabel.setOpaque(false);
			this.actionLabel.setForeground(colorText);
			this.actionLabel.setVerticalAlignment(JLabel.CENTER);
			this.actionLabel.setFont(this.actionLabel.getFont().deriveFont(12f)
					.deriveFont(Font.ITALIC));
			if (type.equals("reassign") || type.equals("suspend")
					|| type.equals("resume")) {
				setAction(EventTypeAction.REMOVE);
			} else if (type.equals("withdraw") || type.equals("ate_abort")
					|| type.equals("pi_abort")) {
				setAction(EventTypeAction.SKIP_INSTANCE);
			} else {
				// schedule, assign, start, complete, autoskip, manualskip, rest
				setAction(EventTypeAction.KEEP);
			}
			this.add(nameLabel);
			this.add(Box.createHorizontalGlue());
			this.add(actionLabel);
			this.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					if (action == EventTypeAction.KEEP) {
						setAction(EventTypeAction.REMOVE);
					} else if (action == EventTypeAction.REMOVE) {
						setAction(EventTypeAction.SKIP_INSTANCE);
					} else if (action == EventTypeAction.SKIP_INSTANCE) {
						setAction(EventTypeAction.KEEP);
					}
					// notify update listener
					if (updateListener != null) {
						updateListener.stateChanged(new ChangeEvent(this));
					}
				}

				public void mouseEntered(MouseEvent arg0) { /* ignore */
				}

				public void mouseExited(MouseEvent arg0) { /* ignore */
				}

				public void mousePressed(MouseEvent arg0) { /* ignore */
				}

				public void mouseReleased(MouseEvent arg0) { /* ignore */
				}
			});
		}

		public EventTypeAction getAction() {
			return this.action;
		}

		public String getName() {
			return this.name;
		}

		public void setAction(EventTypeAction action) {
			this.action = action;
			if (action == EventTypeAction.KEEP) {
				this.actionLabel.setText("(keep)");
				super.setColors(colorKeep2, colorKeep1);
			} else if (action == EventTypeAction.REMOVE) {
				this.actionLabel.setText("(remove)");
				super.setColors(colorRemove2, colorRemove1);
			} else if (action == EventTypeAction.SKIP_INSTANCE) {
				this.actionLabel.setText("(discard instance)");
				super.setColors(colorSkipInstance2, colorSkipInstance1);
			}
		}
	}
}

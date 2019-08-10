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
package org.processmining.framework.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public abstract class OpenLogSettings extends JInternalFrame implements
		Provider {

	protected OpenLogSettings(LogFile file) {
		super(file.getShortName(), true, true, true, true);
	}

	public abstract LogReader getSelectedLogReader();

	public abstract LogFile getFile();

	/**
	 * getLogSummary
	 * 
	 * @return LogSummary
	 */
	public abstract LogSummary getLogSummary();

	public abstract LogFilter getLogFilter();

	public JComponent getConfigurationPanel() {
		return new SettingsResult(this);
	}

	protected class SettingsResult extends JPanel implements Provider {

		protected OpenLogSettings settings;

		public SettingsResult(OpenLogSettings settings) {
			this.settings = settings;
			this.setBorder(BorderFactory.createEmptyBorder());
			this.setLayout(new BorderLayout());
			this.add(settings.getContentPane(), BorderLayout.CENTER);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
		 */
		public ProvidedObject[] getProvidedObjects() {
			return settings.getProvidedObjects();
		}

	}

}
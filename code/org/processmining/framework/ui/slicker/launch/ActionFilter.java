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
package org.processmining.framework.ui.slicker.launch;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author christian
 * 
 */
public class ActionFilter {

	protected Pattern searchPattern;
	protected boolean useMining;
	protected boolean useAnalysis;
	protected boolean useConversion;
	protected boolean useExport;

	public ActionFilter(String search, boolean mining, boolean analysis,
			boolean conversion, boolean export) {
		useMining = mining;
		useAnalysis = analysis;
		useConversion = conversion;
		useExport = export;
		search = search.trim().toLowerCase();
		search = search.replaceAll("(\\s)+", "(.*)");
		search = "(.*)" + search + "(.*)";
		try {
			searchPattern = Pattern.compile(search);
		} catch (PatternSyntaxException pse) {
			searchPattern = Pattern.compile("(.*)");
		}
	}

	public boolean filter(String name) {
		return searchPattern.matcher(name).matches();
	}

	public boolean useMining() {
		return useMining;
	}

	public boolean useAnalysis() {
		return useAnalysis;
	}

	public boolean useConversion() {
		return useConversion;
	}

	public boolean useExport() {
		return useExport;
	}

}

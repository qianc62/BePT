/* jahmm package - v0.6.1 *
 *  Copyright (c) 2004-2006, Jean-Marc Francois.
 *
 *  This file is part of Jahmm.
 *  Jahmm is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Jahmm is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jahmm; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */

/* 
 * Adapted by Anne Rozinat for tailored visualization.
 */
package org.processmining.analysis.hmm.jahmmext;

import be.ac.ulg.montefiore.run.jahmm.Hmm;

/**
 * An HMM to <i>dot</i> file converter. See
 * <url>http://www.research.att.com/sw/tools/graphviz/</url> for more
 * information on the <i>dot</i> tool.
 * <p>
 * The command <tt>dot -Tps -o &lt;outputfile&gt; &lt;inputfile&gt;</tt> should
 * produce a Postscript file describing an HMM.
 * 
 * Included for Hmm experiment package by Anne Rozinat.
 */
public class GenericHmmDrawerDot extends HmmDrawerDot<Hmm<?>> {
}

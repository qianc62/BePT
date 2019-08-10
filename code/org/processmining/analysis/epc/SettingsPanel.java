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

package org.processmining.analysis.epc;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.algorithms.ConnectorStructureExtractor;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class SettingsPanel extends JPanel {
	private RedRulePanel[] rr = new RedRulePanel[] {
			new SingleInSingleOutRedRule(), new SplitSameJoinRedRule(),
			new SplitOrJoinRedRule(), new SimilarSplitSeqRedRule(),
			new SimilarJoinSeqRedRule(), new XorLoopRedRule(),
			new OrLoopRedRule()

	};

	public SettingsPanel() {
		setLayout(new BorderLayout());

		JTabbedPane reductionRules = new JTabbedPane();

		for (int i = 0; i < rr.length; i++) {
			reductionRules.addTab("" + (i + 1), rr[i]);
		}
		add(reductionRules, BorderLayout.CENTER);

	}

	public ConfigurableEPC reduce(ConfigurableEPC epc) {
		return ConnectorStructureExtractor.extract(epc, rr[0].include(), rr[1]
				.include(), rr[2].include(), rr[3].include(), rr[4].include(),
				rr[5].include(), rr[6].include());
	}

	public boolean giveImprovementSuggestions() {
		return false;
	}
}

abstract class RedRulePanel extends JPanel {

	private JCheckBox checkBox = new JCheckBox(
			"<html>Include this reduction rule in the reduction process</html>");

	public RedRulePanel() {
		setLayout(new BorderLayout());
		add(new JLabel(getImage()), BorderLayout.WEST);
		JPanel p = new JPanel();
		p.add(checkBox);
		add(p, BorderLayout.SOUTH);
		checkBox.setSelected(true);
		JPanel p2 = new JPanel();
		p2.add(new JLabel("<html><center><B>" + getDescription()
				+ "</B></center></html>"));
		add(p2, BorderLayout.NORTH);
		JTextArea ta = new JTextArea(getHelp());
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBackground(getBackground());
		ta.setFont(getFont());
		add(ta, BorderLayout.CENTER);
	}

	public boolean include() {
		return checkBox.isSelected();
	}

	protected abstract ImageIcon getImage();

	protected abstract String getDescription();

	protected abstract String getHelp();
}

class SingleInSingleOutRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule1.gif");
	}

	protected String getDescription() {
		return "Single Input and Output";
	}

	protected String getHelp() {
		return "Removes any node that has a single input and single output."
				+ "Since such a node does not have influence on the possible outcomes"
				+ "of the EPC, it can safely be removed.";
	}
}

class SplitSameJoinRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule2.gif");
	}

	protected String getDescription() {
		return "Split followed by join of same type";
	}

	protected String getHelp() {
		return "Removes multiple paths from split nodes that are directly followed by a join node of the same type."
				+ "Note that the paths are not removed if they contain any node, such as an event or function.";
	}
}

class SplitOrJoinRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule3.gif");
	}

	protected String getDescription() {
		return "Split followed by or-join";
	}

	protected String getHelp() {
		return "Removes multiple paths from split nodes that are directly followed by a join node of type \"OR\"."
				+ "Note that the paths are not removed if they contain any node, such as an event or function.";
	}
}

class SimilarSplitSeqRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule5.gif");
	}

	protected String getDescription() {
		return "Two similar splits in sequence";
	}

	protected String getHelp() {
		return "Combines two split connectors if they are of the same type and in sequence.";
	}
}

class SimilarJoinSeqRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule4.gif");
	}

	protected String getDescription() {
		return "Two similar joins in sequence";
	}

	protected String getHelp() {
		return "Combines two join connectors if they are of the same type and in sequence.";
	}
}

class XorLoopRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule6.gif");
	}

	protected String getDescription() {
		return "Loop with xor join and split";
	}

	protected String getHelp() {
		return "Removes a loopback path from a \"XOR\"-split node that is directly followed by a join node of type \"XOR\"."
				+ "Note that the path is not removed if it contains any node, such as an event or function.";
	}
}

class OrLoopRedRule extends RedRulePanel {
	protected ImageIcon getImage() {
		return new ImageIcon(System.getProperty("user.dir", "")
				+ System.getProperty("file.separator") + "images"
				+ System.getProperty("file.separator") + "EPCRedRule7.gif");
	}

	protected String getDescription() {
		return "Loop with or join and (x)or split";
	}

	protected String getHelp() {
		return "Removes a loopback path from a \"XOR\"-split or \"OR\"-split node that is directly followed by a join node of type \"OR\"."
				+ "Note that the path is not removed if it contains any node, such as an event or function.";
	}
}

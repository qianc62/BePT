/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.streamscope;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerComboBoxUI;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.ui.SlickerSliderUI;
import org.processmining.analysis.streamscope.cluster.ClusterNode;
import org.processmining.analysis.streamscope.cluster.ClusterSet;
import org.processmining.analysis.streamscope.cluster.Node;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DuplicateTasksLogFilter;
import org.processmining.framework.ui.Progress;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StreamLogView extends GradientPanel {

	protected class PlayThread extends Thread {

		protected int index = -1;
		protected boolean running = false;

		protected PlayThread(int index) {
			this.index = index;
		}

		public void run() {
			this.running = true;
			AuditTrailEntryList ateList = log.getInstance(index)
					.getAuditTrailEntryList();
			EventClassTable ecTable = clusters.getOrderedEventClassTable();
			double factor = 97.0 / (double) ecTable.size();
			int foreLastNote = -1;
			int lastNote = -1;
			try {
				Receiver synthRcvr = synth.getReceiver();
				for (int i = 0; i < ateList.size(); i++) {
					if (running == false) {
						break;
					}
					views.get(index).setActiveIndex(i);
					AuditTrailEntry ate = ateList.get(i);
					int ecIndex = ecTable.getIndex(ate.getElement());
					int note = 112 - (int) (ecIndex * factor);
					if (foreLastNote >= 0) {
						ShortMessage myMsg = new ShortMessage();
						// stop forelast note
						myMsg.setMessage(ShortMessage.NOTE_OFF, 4,
								foreLastNote, 110);
						synthRcvr.send(myMsg, -1); // -1 means no time stamp
					}
					ShortMessage myMsg = new ShortMessage();
					// Play the note Middle C (60) moderately loud
					// (velocity = 110)on channel 4 (zero-based).
					myMsg.setMessage(ShortMessage.NOTE_ON, 4, note, 110);
					synthRcvr.send(myMsg, -1); // -1 means no time stamp
					foreLastNote = lastNote;
					lastNote = note;
					try {
						Thread.sleep(220);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for (MidiChannel channel : synth.getChannels()) {
					channel.allNotesOff();
				}
				for (StreamView view : views) {
					view.setActiveIndex(-1);
				}
				playButton.setText("play");
				playThread = null;
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void stopPlaying() {
			running = false;
		}
	}

	protected Synthesizer synth;

	protected LogReader log;
	protected ClusterSet clusters;
	protected ArrayList<StreamView> views;

	protected Color labelColor = new Color(30, 30, 30);
	protected static Color backgroundTopColor = new Color(80, 80, 80);
	protected static Color backgroundBottomColor = new Color(60, 60, 60);
	protected static Color panelBackgroundColor = new Color(140, 140, 140, 140);

	protected JSlider levelSlider;
	protected JButton playButton;
	protected JComboBox instanceBox;
	protected PlayThread playThread;

	public StreamLogView(LogReader log, Progress progress) {
		super(backgroundTopColor, backgroundBottomColor);
		try {
			synth = MidiSystem.getSynthesizer();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
		setup(log, progress);
	}

	public ClusterSet getClusters() {
		return clusters;
	}

	public LogReader getLog() {
		return log;
	}

	public void setup(LogReader log, Progress progress) {
		int settingsDistance = 5;
		this.log = log;
		this.views = new ArrayList<StreamView>();
		try {
			clusters = new ClusterSet(log, progress);
			progress.setNote("Setting up UI...");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JPanel enclosure = new JPanel();
		enclosure.setOpaque(true);
		enclosure.setBackground(new Color(0, 0, 0));
		enclosure.setLayout(new BoxLayout(enclosure, BoxLayout.Y_AXIS));
		enclosure.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		for (int i = 0; i < log.numberOfInstances(); i++) {
			StreamView view = new StreamView(log.getInstance(i), this);
			view.setAlignmentX(LEFT_ALIGNMENT);
			views.add(view);
			// enclosure.add(packLeftAligned(view));
			enclosure.add(view);
			enclosure.add(Box.createVerticalStrut(5));
		}
		JScrollPane scrollPane = new JScrollPane(enclosure);
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setBackground(Color.BLACK);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getHorizontalScrollBar().setUI(
				new SlickerScrollBarUI(scrollPane.getHorizontalScrollBar(),
						Color.BLACK, new Color(120, 120, 120), new Color(50,
								50, 50), 3, 12));
		scrollPane.getVerticalScrollBar().setUI(
				new SlickerScrollBarUI(scrollPane.getVerticalScrollBar(),
						Color.BLACK, new Color(120, 120, 120), new Color(50,
								50, 50), 3, 12));
		scrollPane.getHorizontalScrollBar().setBlockIncrement(10);
		scrollPane.getVerticalScrollBar().setBlockIncrement(10);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		RoundedPanel scrollEnclosure = new RoundedPanel(15, 5, 0);
		scrollEnclosure.setBackground(Color.BLACK);
		scrollEnclosure.setLayout(new BorderLayout());
		scrollEnclosure.add(scrollPane, BorderLayout.CENTER);
		final JSlider hSlider = new JSlider();
		hSlider.setOrientation(JSlider.HORIZONTAL);
		hSlider.setMinimum(1);
		hSlider.setMaximum(1000);
		hSlider.setValue(300);
		hSlider.setUI(new SlickerSliderUI(hSlider));
		hSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float zoom = (float) hSlider.getValue() / 100f;
				for (StreamView view : views) {
					view.setHorizontalZoom(zoom);
				}
			}
		});
		final JSlider vSlider = new JSlider();
		vSlider.setOrientation(JSlider.HORIZONTAL);
		vSlider.setMinimum(1);
		vSlider.setMaximum(1000);
		vSlider.setValue(100);
		vSlider.setUI(new SlickerSliderUI(vSlider));
		vSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float zoom = (float) vSlider.getValue() / 100f;
				for (StreamView view : views) {
					view.setVerticalZoom(zoom);
				}
			}
		});
		final JSlider fSlider = new JSlider();
		fSlider.setOrientation(JSlider.HORIZONTAL);
		fSlider.setMinimum(0);
		fSlider.setMaximum(800);
		fSlider.setValue(200);
		fSlider.setUI(new SlickerSliderUI(fSlider));
		fSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float fuzz = (float) fSlider.getValue() / 100f;
				for (StreamView view : views) {
					view.setFuzziness(fuzz);
				}
			}
		});
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rightPanel.setOpaque(false);
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setMinimumSize(new Dimension(200, 100));
		rightPanel.setMaximumSize(new Dimension(200, 1000));
		rightPanel.setPreferredSize(new Dimension(200, 500));
		RoundedPanel adjustPanel = new RoundedPanel(10);
		adjustPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
		adjustPanel.setLayout(new BoxLayout(adjustPanel, BoxLayout.Y_AXIS));
		adjustPanel.setBackground(panelBackgroundColor);
		JLabel adjustTitle = new JLabel("View");
		Font bigFong = adjustTitle.getFont().deriveFont(13f);
		adjustTitle.setFont(bigFong);
		adjustTitle.setOpaque(false);
		adjustTitle.setForeground(labelColor);
		adjustTitle.setAlignmentX(LEFT_ALIGNMENT);
		adjustPanel.add(packLeftAligned(adjustTitle));
		adjustPanel.add(Box.createVerticalStrut(settingsDistance));
		JLabel hLabel = new JLabel("Horizontal Zoom");
		Font labelFont = hLabel.getFont().deriveFont(10f);
		hLabel.setFont(labelFont);
		hLabel.setOpaque(false);
		hLabel.setForeground(labelColor);
		hLabel.setAlignmentX(LEFT_ALIGNMENT);
		adjustPanel.add(packLeftAligned(hLabel));
		adjustPanel.add(hSlider);
		adjustPanel.add(Box.createVerticalStrut(settingsDistance));
		JLabel vLabel = new JLabel("Vertical Zoom");
		vLabel.setFont(labelFont);
		vLabel.setOpaque(false);
		vLabel.setForeground(labelColor);
		vLabel.setAlignmentX(LEFT_ALIGNMENT);
		adjustPanel.add(packLeftAligned(vLabel));
		adjustPanel.add(vSlider);
		adjustPanel.add(Box.createVerticalStrut(settingsDistance));
		JLabel fLabel = new JLabel("Fuzziness");
		fLabel.setAlignmentX(LEFT_ALIGNMENT);
		fLabel.setFont(labelFont);
		fLabel.setOpaque(false);
		fLabel.setForeground(labelColor);
		adjustPanel.add(packLeftAligned(fLabel));
		adjustPanel.add(fSlider);
		// abstraction panel
		RoundedPanel abstractionPanel = new RoundedPanel(10);
		abstractionPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
		abstractionPanel.setLayout(new BoxLayout(abstractionPanel,
				BoxLayout.Y_AXIS));
		abstractionPanel.setBackground(panelBackgroundColor);
		JLabel abstractionTitle = new JLabel("Abstraction");
		abstractionTitle.setFont(bigFong);
		abstractionTitle.setOpaque(false);
		abstractionTitle.setForeground(labelColor);
		abstractionTitle.setAlignmentX(LEFT_ALIGNMENT);
		abstractionPanel.add(packLeftAligned(abstractionTitle));
		abstractionPanel.add(Box.createVerticalStrut(settingsDistance));
		JLabel levelLabel = new JLabel("Abstraction Level");
		levelLabel.setFont(labelFont);
		levelLabel.setOpaque(false);
		levelLabel.setForeground(labelColor);
		levelLabel.setAlignmentX(LEFT_ALIGNMENT);
		final JLabel levelValueLabel = new JLabel("Level 0: "
				+ clusters.getOrderedEventClassTable().size()
				+ " entities (0 clusters)");
		levelValueLabel.setFont(levelValueLabel.getFont().deriveFont(10f));
		levelValueLabel.setOpaque(false);
		levelValueLabel.setForeground(labelColor);
		levelValueLabel.setAlignmentX(RIGHT_ALIGNMENT);
		levelSlider = new JSlider();
		levelSlider.setOrientation(JSlider.HORIZONTAL);
		levelSlider.setMinimum(0);
		levelSlider.setMaximum(clusters.getNumberOfLevels());
		levelSlider.setValue(0);
		levelSlider.setUI(new SlickerSliderUI(levelSlider));
		levelSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int level = levelSlider.getValue();
				List<Node> levelNodes = clusters.getOrderedNodesForLevel(level);
				int clusters = 0;
				for (Node node : levelNodes) {
					if (node instanceof ClusterNode) {
						clusters++;
					}
				}
				levelValueLabel.setText("Level " + level + ": "
						+ levelNodes.size() + " entities (" + clusters
						+ " clusters)");
				levelValueLabel.revalidate();
				for (StreamView view : views) {
					view.setLevel(level, levelNodes);
				}
			}
		});
		abstractionPanel.add(packLeftAligned(levelLabel));
		abstractionPanel.add(levelSlider);
		abstractionPanel.add(packLeftAligned(levelValueLabel));
		// play panel
		RoundedPanel playPanel = new RoundedPanel(10);
		playPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
		playPanel.setLayout(new BoxLayout(playPanel, BoxLayout.Y_AXIS));
		playPanel.setBackground(panelBackgroundColor);
		JLabel playTitle = new JLabel("Sound Mapping");
		playTitle.setFont(bigFong);
		playTitle.setOpaque(false);
		playTitle.setForeground(labelColor);
		playTitle.setAlignmentX(LEFT_ALIGNMENT);
		String[] instanceNames = new String[log.numberOfInstances()];
		for (int i = 0; i < log.numberOfInstances(); i++) {
			instanceNames[i] = log.getInstance(i).getName();
		}
		instanceBox = new JComboBox(instanceNames);
		instanceBox.setMaximumSize(new Dimension(160, 25));
		instanceBox.setUI(new SlickerComboBoxUI());
		playButton = new SlickerButton("play");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (playThread != null) {
					playThread.stopPlaying();
				} else {
					playThread = new PlayThread(instanceBox.getSelectedIndex());
					playThread.start();
					playButton.setText("stop");
				}
			}
		});
		try {
			synth.open();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Instrument[] instruments = synth.getAvailableInstruments();
		final JComboBox instrumentBox = new JComboBox(instruments);
		instrumentBox.setUI(new SlickerComboBoxUI());
		instrumentBox.setMinimumSize(new Dimension(160, 25));
		instrumentBox.setMaximumSize(new Dimension(160, 25));
		instrumentBox.setPreferredSize(new Dimension(160, 25));
		instrumentBox.setSize(new Dimension(160, 25));
		instrumentBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Instrument instr = (Instrument) instrumentBox.getSelectedItem();
				synth.loadInstrument(instr);
				Patch patch = instr.getPatch();
				synth.getChannels()[4].programChange(patch.getBank(), patch
						.getProgram());
			}
		});
		playPanel.add(packLeftAligned(playTitle));
		playPanel.add(Box.createVerticalStrut(settingsDistance));
		playPanel.add(packLeftAligned(instanceBox));
		playPanel.add(Box.createVerticalStrut(settingsDistance));
		playPanel.add(packLeftAligned(instrumentBox));
		playPanel.add(Box.createVerticalStrut(settingsDistance));
		playPanel.add(packLeftAligned(playButton));
		rightPanel.add(adjustPanel);
		rightPanel.add(Box.createVerticalStrut(8));
		rightPanel.add(abstractionPanel);
		rightPanel.add(Box.createVerticalStrut(8));
		rightPanel.add(playPanel);
		rightPanel.add(Box.createVerticalGlue());
		JLabel infoLabel = new JLabel();
		infoLabel.setOpaque(false);
		infoLabel.setForeground(new Color(140, 140, 140));
		infoLabel.setFont(infoLabel.getFont().deriveFont(10f));
		infoLabel.setText("Analyzing '" + log.getFile().getShortName() + "': "
				+ log.getLogSummary().getNumberOfAuditTrailEntries()
				+ " events, from "
				+ log.getLogSummary().getModelElements().length
				+ " classes, in " + log.numberOfInstances() + " traces.");
		infoLabel.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 3));
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.add(scrollEnclosure, BorderLayout.CENTER);
		mainPanel.add(packLeftAligned(infoLabel), BorderLayout.SOUTH);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(rightPanel, BorderLayout.EAST);
	}

	protected JComponent packLeftAligned(JComponent comp) {
		Box enclosure = Box.createHorizontalBox();
		enclosure.setOpaque(false);
		enclosure.add(comp);
		enclosure.add(Box.createHorizontalGlue());
		return enclosure;
	}

	public LogReader getFilteredLog() {
		if (this.levelSlider.getValue() > 0) {
			DuplicateTasksLogFilter dupFilter = new DuplicateTasksLogFilter();
			StreamScopeFilter scopeFilter = new StreamScopeFilter(clusters,
					this.levelSlider.getValue());
			dupFilter.setLowLevelFilter(scopeFilter);
			scopeFilter.setLowLevelFilter(log.getLogFilter());
			try {
				return LogReaderFactory.createInstance(dupFilter, log);
			} catch (Exception e) {
				return log;
			}
		} else {
			return log;
		}
	}

}

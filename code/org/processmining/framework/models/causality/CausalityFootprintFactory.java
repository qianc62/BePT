package org.processmining.framework.models.causality;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.CancelationComponent;

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
public class CausalityFootprintFactory {
	private CausalityFootprintFactory() {
	}

	public static boolean canConvert(Object o) {
		return (o instanceof ConfigurableEPC) || (o instanceof PetriNet);
	}

	public static CausalFootprint make(Object o) {

		FootprintFactoryDialog dialog = new FootprintFactoryDialog(o);
		dialog.showDialog();

		return dialog.getResult();
	}

	/**
	 * This method builds a causal footprint from the given object, under the
	 * assumption that the canConvert() method on that object returns true.
	 * 
	 * The provided progress dialog is used for cancellation
	 * 
	 * @param o
	 *            Object
	 * @param p
	 *            Progress
	 * @return CausalFootprint
	 */
	public static CausalFootprint make(Object o, CancelationComponent p) {
		if (o instanceof ConfigurableEPC) {
			return make_internal((ConfigurableEPC) o, p);
		}
		if (o instanceof PetriNet) {
			return make_internal((PetriNet) o, p);
		}

		return null;
	}

	private static CausalFootprint make_internal(ConfigurableEPC epc,
			CancelationComponent progress) {
		CausalFootprint struct = new CausalFootprint("Causality structure "
				+ epc.getIdentifier(), epc);

		// Add functions if necessary
		Iterator itf = epc.getFunctions().iterator();
		while (itf.hasNext()) {
			if (progress.isCanceled()) {
				return struct;
			}
			EPCFunction f = (EPCFunction) itf.next();
			ModelGraphVertex v = new ModelGraphVertex(f);
			v.object = f;
			struct.addVertex(v, f);
		}

		Iterator itc = epc.getConnectors().iterator();
		while (itc.hasNext()) {
			if (progress.isCanceled()) {
				return struct;
			}
			EPCConnector c = (EPCConnector) itc.next();
			ModelGraphVertex v = new ModelGraphVertex(c);
			v.object = c;
			v.setIdentifier("(" + c.toString() + c.getId() + ")");
			struct.addVertex(v, c);
		}

		HashSet toTarget = new HashSet();

		Iterator ite = epc.getEdges().iterator();
		while (ite.hasNext()) {
			EPCEdge e = (EPCEdge) ite.next();
			EPCObject source = (EPCObject) e.getSource();
			HashSet dests = new HashSet();

			if (source instanceof EPCEvent) {
				continue;
			}
			if (progress.isCanceled()) {
				return struct;
			}

			Iterator it = source.getSuccessors().iterator();
			while (it.hasNext()) {
				if (progress.isCanceled()) {
					return struct;
				}
				EPCObject dest = (EPCObject) it.next();
				if (dest instanceof EPCEvent) {
					if (dest.outDegree() == 0) {
						dests.add(struct.getTarget());
						toTarget.add(struct.getCausalVertex(source));
					} else {
						dests.add(struct.getCausalVertex((EPCObject) dest
								.getSuccessors().iterator().next()));
					}
				} else {
					dests.add(struct.getCausalVertex(dest));
				}
			}
			if ((source instanceof EPCConnector)
					&& ((EPCConnector) source).getType() == EPCConnector.AND) {
				Iterator it2 = dests.iterator();
				while (it2.hasNext()) {
					if (progress.isCanceled()) {
						return struct;
					}
					ModelGraphVertex dest = (ModelGraphVertex) it2.next();
					HashSet s = new HashSet();
					s.add(dest);
					struct.addEdge(struct.getCausalVertex(source), s);
					s = new HashSet();
					s.add(struct.getCausalVertex(source));
					struct.addEdge(s, dest);
				}
			} else {
				struct.addEdge(struct.getCausalVertex(source), dests);
			}
		}
		struct.addEdge(toTarget, struct.getTarget());

		HashSet fromSource = new HashSet();
		ite = epc.getEdges().iterator();
		while (ite.hasNext()) {
			EPCEdge e = (EPCEdge) ite.next();
			EPCObject dest = (EPCObject) e.getDest();
			HashSet sources = new HashSet();

			if (dest instanceof EPCEvent) {
				continue;
			}
			if (progress.isCanceled()) {
				return struct;
			}

			Iterator it = dest.getPredecessors().iterator();
			while (it.hasNext()) {
				if (progress.isCanceled()) {
					return struct;
				}
				EPCObject source = (EPCObject) it.next();
				if (source instanceof EPCEvent) {
					if (source.inDegree() == 0) {
						sources.add(struct.getSource());
						fromSource.add(struct.getCausalVertex(dest));
					} else {
						sources.add(struct.getCausalVertex((EPCObject) source
								.getPredecessors().iterator().next()));
					}
				} else {
					sources.add(struct.getCausalVertex(source));
				}
			}

			if ((dest instanceof EPCConnector)
					&& ((EPCConnector) dest).getType() == EPCConnector.AND) {
				Iterator it2 = sources.iterator();
				while (it2.hasNext()) {
					if (progress.isCanceled()) {
						return struct;
					}
					ModelGraphVertex source = (ModelGraphVertex) it2.next();
					HashSet s = new HashSet();
					s.add(source);
					struct.addEdge(s, struct.getCausalVertex(dest));
					s = new HashSet();
					s.add(struct.getCausalVertex(dest));
					struct.addEdge(source, s);
				}
			} else {
				struct.addEdge(sources, struct.getCausalVertex(dest));
			}
		}
		struct.addEdge(struct.getSource(), fromSource);
		struct.closeTransitively(progress);
		if (progress.isCanceled()) {
			return struct;
		}
		struct.removeObsoleteEdges(progress);
		if (progress.isCanceled()) {
			return struct;
		}
		return struct;
	}

	private static CausalFootprint make_internal(PetriNet petrinet,
			CancelationComponent progress) {
		//
		CausalFootprint struct = new CausalFootprint("Causality structure "
				+ petrinet.getIdentifier(), petrinet);
		Iterator it = petrinet.getTransitions().iterator();
		while (it.hasNext()) {
//			if (progress.isCanceled()) {
//				return struct;
//			}
			Transition t = (Transition) it.next();
			ModelGraphVertex v = new ModelGraphVertex(t);
			v.object = t;
			struct.addVertex(v, t);
		}

		HashSet inPlaceSucc = new HashSet();
		HashSet outPlacePre = new HashSet();
		it = petrinet.getPlaces().iterator();
		while (it.hasNext()) {
//			if (progress.isCanceled()) {
//				return struct;
//			}
			Place p = (Place) it.next();

			HashSet inTrans = new HashSet(p.getPredecessors().size());
			Iterator it2 = p.getPredecessors().iterator();
			while (it2.hasNext()) {
				inTrans.add(struct.getCausalVertex((Transition) it2.next()));
			}

			HashSet outTrans = new HashSet(p.getSuccessors().size());
			it2 = p.getSuccessors().iterator();
			while (it2.hasNext()) {
				outTrans.add(struct.getCausalVertex((Transition) it2.next()));
			}

			if (p.inDegree() == 0) {
				inTrans.add(struct.getSource());
				inPlaceSucc.addAll(struct.getCausalVertices(p.getSuccessors()));
				// struct.addEdge(struct.getSource(), outTrans);
			}

			if (p.outDegree() == 0) {
				outTrans.add(struct.getTarget());
				outPlacePre.addAll(struct
						.getCausalVertices(p.getPredecessors()));
				// struct.addEdge(inTrans, struct.getTarget());
			}

			Iterator sources = p.getPredecessors().iterator();
			while (sources.hasNext()) {
				Transition source = (Transition) sources.next();
				struct.addEdge(struct.getCausalVertex(source), outTrans);
			}

			Iterator dests = p.getSuccessors().iterator();
			while (dests.hasNext()) {
				Transition dest = (Transition) dests.next();
				struct.addEdge(inTrans, struct.getCausalVertex(dest));
			}
		}
		struct.addEdge(struct.getSource(), inPlaceSucc);
		struct.addEdge(outPlacePre, struct.getTarget());
		/*
		 * struct.displayForwardEdge(); System.out.println();
		 * struct.displayBackwardEdge();
		 */

		struct.closeTransitively(null);
		/*
		 * struct.displayForwardEdge(); System.out.println();
		 * struct.displayBackwardEdge();
		 */

//		if (progress.isCanceled()) {
//			return struct;
//		}
		ArrayList invis = new ArrayList();
		it = petrinet.getTransitions().iterator();
		while (it.hasNext()) {
			Transition t = (Transition) it.next();
			if (t.isInvisibleTask()) {
				invis.add(t);
			}
		}
		struct.removeAllBaseVertices(invis);

//		if (progress.isCanceled()) {
//			return struct;
//		}
//		struct.removeObsoleteEdges(progress);

//		struct.displayForwardEdge();
//		System.out.println();
//		struct.displayBackwardEdge();

//		if (progress.isCanceled()) {
//			return struct;
//		}
		return struct;
	}

	static class FootprintFactoryDialog extends JDialog implements
			CancelationComponent {

		private CausalFootprint result = null;
		private SwingWorker worker;
		private boolean canceled = false;

		public FootprintFactoryDialog(final Object toConvert) {
			super(MainUI.getInstance(), "Building Causal FootPrint", true);

			JPanel main = new JPanel(new BorderLayout());
			getContentPane().add(main);

			JPanel p = new JPanel();
			p
					.add(new JLabel(
							"<html><center>Click \"cancel\" to abort <br> building the footprint</center></html>"));

			main.add(p, BorderLayout.CENTER);
			JButton button = new JButton("Cancel");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					canceled = true;
				}
			});
			JPanel cancelPanel = new JPanel(new FlowLayout());
			cancelPanel.add(button);

			main.add(cancelPanel, BorderLayout.SOUTH);

			worker = new SwingWorker() {
				public Object construct() {
					try {
						sleep(20);
						sleep(20);
						if (toConvert instanceof ConfigurableEPC) {
							return CausalityFootprintFactory.make_internal(
									(ConfigurableEPC) toConvert,
									FootprintFactoryDialog.this);
						}
						if (toConvert instanceof PetriNet) {
							return CausalityFootprintFactory.make_internal(
									(PetriNet) toConvert,
									FootprintFactoryDialog.this);
						}
						return null;
					} catch (Exception ex) {
						Message.add(
								"Error while performing building footprint: "
										+ ex.getMessage(), Message.ERROR);
					}
					return null;
				}

				public void finished() {
					if (get() != null) {
						result = (CausalFootprint) get();
					}
					setVisible(false);
				}
			};

			pack();

			setSize(Math.min(700, getSize().width) + 65, Math.min(500,
					getSize().height));

			CenterOnScreen.center(this);
			repaint();

		}

		public boolean isCanceled() {
			return canceled;
		}

		public void showDialog() {
			worker.start();
			setVisible(true);
		}

		public CausalFootprint getResult() {
			return result;
		}
	}
}

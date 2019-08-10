package org.jbpt.petri.structure;

import org.jbpt.algo.graph.DirectedGraphAlgorithms;
import org.jbpt.petri.*;


/**
 * Collection of methods for performing structural checks on Petri nets.
 *
 * @param <F> Flow template.
 * @param <N> Node template.
 * @param <P> Place template.
 * @param <T> Transition template.
 * @author Artem Polyvyanyy
 * @author Matthias Weidlich
 */
public class PetriNetStructuralChecks<F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition> {

    private DirectedGraphAlgorithms<F, N> DGA = new DirectedGraphAlgorithms<F, N>();

    /**
     * Check if a given Petri net is free-choice.
     * A net is free-choice if and only if for every its place p that has multiple output transitions holds *(p*)={p}.
     *
     * @param net A Petri net.
     * @return <tt>true</tt> if the net is free-choice; otherwise <tt>false</tt>.
     */
    public boolean isFreeChoice(IPetriNet<F, N, P, T> net) {
        for (P p : net.getPlaces()) {
            if (net.getPostset(p).size() > 1)
                if (net.getPresetPlaces(net.getPostset(p)).size() != 1) return false;
        }

        return true;
    }

    /**
     * Check if a given Petri net is extended free-choice.
     * A Petri net is extended free-choice if all transitions that share a place in their presets have to coincide w.r.t. their presets.
     *
     * @param net A Petri net.
     * @return <tt>true</tt> if the net is extended free-choice; otherwise <tt>false</tt>.
     */
    public boolean isExtendedFreeChoice(IPetriNet<F, N, P, T> net) {
        boolean isFC = true;

        outer:
        for (T t1 : net.getTransitions()) {
            for (T t2 : net.getTransitions()) {
                for (P p : net.getPlaces()) {
                    if (net.getPreset(t1).contains(p) && net.getPreset(t2).contains(p))
                        isFC &= net.getPreset(t1).equals(net.getPreset(t2));
                    if (!isFC)
                        break outer;
                }
            }
        }
        return isFC;
    }

    /**
     * Check if a given Petri net is an S-net.
     * A net is an S-net if and only if every its transition has exactly one input place and exactly one output place.
     *
     * @param net A Petri net.
     * @return <tt>true</tt> if the net is S-net; otherwise <tt>false</tt>.
     */
    public boolean isSNet(IPetriNet<F, N, P, T> net) {
        for (T t : net.getTransitions())
            if (net.getPreset(t).size() != 1 || net.getPostset(t).size() != 1)
                return false;

        return true;
    }

    /**
     * Check if a given net is a T-net.
     * A net is a T-net if and only if every its place has exactly one input transition and exactly one output transition.
     *
     * @param net A Petri net
     * @return <tt>true</tt> if the net is T-net; otherwise <tt>false</tt>.
     */
    public boolean isTNet(IPetriNet<F, N, P, T> net) {
        for (P p : net.getPlaces())
            if (net.getPreset(p).size() != 1 || net.getPostset(p).size() != 1)
                return false;

        return true;
    }

    /**
     * Check if a net is a conflict-free net.
     * A net is an conflict-free if and only if every its place has at most one input place and at most one output place.
     *
     * @param net A Petri net.
     * @return <tt>true</tt> if the net is conflict-free; otherwise <tt>false</tt>.
     */
    public boolean isConflictFree(IPetriNet<F, N, P, T> net) {
        for (P p : net.getPlaces())
            if (net.getPreset(p).size() > 1 || net.getPostset(p).size() > 1)
                return false;

        return true;
    }

    /**
     * Check if a given Petri net is a workflow net (WF-net).
     * <p>
     * A WF-net has exactly one source place, exactly one sink place, and
     * every node is on a path from the source to the sink.
     *
     * @param net A Petri net.
     * @return <tt>true</tt> if the net is a WF-net; otherwise <tt>false</tt>.
     */
    public boolean isWorkflowNet(IPetriNet<F, N, P, T> net) {
        return this.DGA.isTwoTerminal(net);
    }

    /**
     * Check if a given Petri net is T-restricted.
     * A net is T-restricted if presets and postsets of all transitions are not empty.
     *
     * @return <tt>true</tt> if this net is T-restricted; otherwise <tt>false</tt>.
     */
    public boolean isTRestricted(IPetriNet<F, N, P, T> net) {
        return net.getSourceTransitions().isEmpty() && net.getSinkTransitions().isEmpty();
    }
}

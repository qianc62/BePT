package com.iise.shudi.exroru.dependency.sda;

import com.iise.shudi.exroru.dependency.lc.LeastCommonPredecessorsAndSuccessors;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SequentialDirectAdjacency {

    private NetSystem _sys;
    private ProperCompletePrefixUnfolding _cpu;
    private LeastCommonPredecessorsAndSuccessors _lc;
    private Set<Marking> visitedMarkings = new HashSet<>();
    private Map<Event, Marking> enabledMarkingMap = new HashMap<>();
    private Map<Transition, Set<Transition>> sdaRelations = new HashMap<>();

    public SequentialDirectAdjacency(ProperCompletePrefixUnfolding cpu,
                                     LeastCommonPredecessorsAndSuccessors lc) {
        this._cpu = cpu;
        this._sys = (NetSystem) cpu.getOriginativeNetSystem();
        this._lc = lc;
        generateSDA();
//		System.out.print(toString());
    }

    private void generateSDA() {
        Marking initialMarking = getInitialMarking();
        initialMarking.getPostEnabledEvents().stream()
                .forEach(e -> enabledMarkingMap.put(e, initialMarking));
        dfsMarking(initialMarking);
    }

    private void dfsMarking(Marking m) {
        if (visitedMarkings.contains(m)) {
            return;
        }
        visitedMarkings.add(m);
        if (m.getPreVisEvent() == null) {
            // initialMarking
            for (Event postEvent : m.getPostEnabledEvents()) {
                Marking newMarking = m.clone();
                newMarking.fire(postEvent);
                dfsMarking(newMarking);
            }
        } else {
            // firstly, deal with postEnabledEvents
            for (Event postEvent : m.getPostEnabledEvents()) {
                if (!postEvent.getTransition().isSilent()) {
                    // if postEnabledEvent is visible, add <pre, post> to sda
                    sdaRelations.putIfAbsent(
                            m.getPreVisEvent().getTransition(),
                            new HashSet<>());
                    sdaRelations.get(m.getPreVisEvent().getTransition()).add(
                            postEvent.getTransition());
                }
                // fire it and dfs
                Marking newMarking = m.clone();
                newMarking.fire(postEvent);
                dfsMarking(newMarking);
            }
            // secondly, fire all the enabled events which is concurrent with
            // preEvent, then check postDisabledEvents
            Marking newMarking = m.clone();
            Set<Integer> visited = new HashSet<>();
            dfsPostDisabledEvents(newMarking, visited);
        }
    }

    @SuppressWarnings("rawtypes")
    private void dfsPostDisabledEvents(Marking m, Set<Integer> visited) {
        // fire all the enabled events which is concurrent with preEvent,
        // check postDisabledEvents in every fire
        boolean canFire = true;
        // 1. try to fire all the enabled events which are not in a non-free
        // choice structure
        while (canFire) {
            canFire = false;
            if (visited.contains(m.hashCode())) {
                return;
            }
            visited.add(m.hashCode());
            for (Event e : m.getEnabledEvents()) {
                if (this.checkEventInNFC(e)) {
                    boolean isConcurrent = false;
                    boolean canFireVis = true;
                    if (m.getPreInvEvent() == null) {
                        Set<IBPNode> lcpSet = this._lc.getLcpCpuMap().get(e)
                                .get(m.getPreVisEvent());
                        isConcurrent = !lcpSet.stream()
                                .anyMatch(
                                        lcp -> (lcp instanceof Condition
                                                || lcp == e || lcp == m
                                                .getPreVisEvent()));
                    } else {
                        Set<IBPNode> lcpSetInv = this._lc.getLcpCpuMap().get(e)
                                .get(m.getPreInvEvent());
                        Set<IBPNode> lcpSetVis = this._lc.getLcpCpuMap().get(e)
                                .get(m.getPreVisEvent());
                        boolean isConcurrentInv = !lcpSetInv.stream()
                                .anyMatch(
                                        lcp -> (lcp instanceof Condition
                                                || lcp == e || lcp == m
                                                .getPreInvEvent()));
                        boolean isConcurrentVis = !lcpSetVis.stream()
                                .anyMatch(
                                        lcp -> (lcp instanceof Condition
                                                || lcp == e || lcp == m
                                                .getPreVisEvent()));
                        if (isConcurrentInv && isConcurrentVis) {
                            isConcurrent = true;
                            canFireVis = true;
                        } else if (isConcurrentInv) {
                            isConcurrent = true;
                            canFireVis = false;
                        }
                    }
                    if (isConcurrent) {
                        if (!canFireVis && !e.getTransition().isSilent()) {
                            continue;
                        }
                        m.onlyFire(e);
                        // check postDisabledEvent every time
                        m.getPostDisabledEvents().stream().filter(m::isEnabled).forEach(
                                postEvent -> {
                                    if (!postEvent.getTransition().isSilent()) {
                                        // if postDisabledEvent is visible, add
                                        // <pre, post> to sda
                                        sdaRelations.putIfAbsent(m.getPreVisEvent()
                                                        .getTransition(),
                                                new HashSet<>());
                                        sdaRelations.get(
                                                m.getPreVisEvent().getTransition())
                                                .add(postEvent.getTransition());
                                    }
                                    // fire it and dfs
                                    Marking postMarking = m.clone();
                                    postMarking.fire(postEvent);
                                    dfsMarking(postMarking);
                                }
                        );
                        canFire = true;
                    }
                }
            }
        }
        // 2. if there is no such events, traverse to fire other enabled events
        for (Event e : m.getEnabledEvents()) {
            boolean isConcurrent = false;
            boolean canFireVis = true;
            if (m.getPreInvEvent() == null) {
                Set<IBPNode> lcpSet = this._lc.getLcpCpuMap().get(e)
                        .get(m.getPreVisEvent());
                isConcurrent = !lcpSet
                        .stream()
                        .anyMatch(
                                lcp -> (lcp instanceof Condition || lcp == e || lcp == m
                                        .getPreVisEvent()));
            } else {
                Set<IBPNode> lcpSetInv = this._lc.getLcpCpuMap().get(e)
                        .get(m.getPreInvEvent());
                Set<IBPNode> lcpSetVis = this._lc.getLcpCpuMap().get(e)
                        .get(m.getPreVisEvent());
                boolean isConcurrentInv = !lcpSetInv
                        .stream()
                        .anyMatch(
                                lcp -> (lcp instanceof Condition || lcp == e || lcp == m
                                        .getPreInvEvent()));
                boolean isConcurrentVis = !lcpSetVis
                        .stream()
                        .anyMatch(
                                lcp -> (lcp instanceof Condition || lcp == e || lcp == m
                                        .getPreVisEvent()));
                if (isConcurrentInv && isConcurrentVis) {
                    isConcurrent = true;
                    canFireVis = true;
                } else if (isConcurrentInv) {
                    isConcurrent = true;
                    canFireVis = false;
                }
            }
            if (isConcurrent) {
                if (!canFireVis && !e.getTransition().isSilent()) {
                    continue;
                }
                Marking copyMarking = m.clone();
                copyMarking.onlyFire(e);
                // check postDisabledEvent every time
                copyMarking.getPostDisabledEvents().stream().filter(copyMarking::isEnabled).forEach(
                        postEvent -> {
                            if (!postEvent.getTransition().isSilent()) {
                                // if postDisabledEvent is visible, add <pre, post>
                                // to sda
                                sdaRelations.putIfAbsent(copyMarking
                                                .getPreVisEvent().getTransition(),
                                        new HashSet<>());
                                sdaRelations.get(
                                        copyMarking.getPreVisEvent()
                                                .getTransition()).add(
                                        postEvent.getTransition());
                            }
                            // fire it and dfs
                            Marking postMarking = copyMarking.clone();
                            postMarking.fire(postEvent);
                            dfsMarking(postMarking);
                        }
                );
                dfsPostDisabledEvents(copyMarking, visited);
            }
        }
    }

    /**
     * check if a event is in a non-free choice structure
     */
    private boolean checkEventInNFC(Event e) {
        Set<Condition> preConditions = e.getPreConditions();
        for (Condition c : preConditions) {
            Set<Event> postEvents = c.getPostE();
            for (Event pe : postEvents) {
                if (pe == e
                        || e.getPreConditions().equals(pe.getPreConditions())) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private Marking getInitialMarking() {
        Marking initialMarking = Marking.createMarking(this._cpu);
        if (initialMarking != null) {
            initialMarking.fromMultiSet(this._cpu.getConditions(this._sys
                    .getSourcePlaces().iterator().next()));
        }
        return initialMarking;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.sdaRelations
                .keySet()
                .stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .forEach(
                        t -> {
                            sb.append("\"");
                            sb.append(t.getLabel());
                            sb.append("\":[");
                            this.sdaRelations
                                    .get(t)
                                    .stream()
                                    .sorted((a, b) -> a.getLabel().compareTo(
                                            b.getLabel()))
                                    .forEach(
                                            tt -> {
                                                sb.append("\"");
                                                sb.append(tt.getLabel());
                                                sb.append("\",");
                                            });
                            sb.append("]");
                            sb.append("\r\n");
                        });
        return sb.toString();
    }

    public Map<Transition, Set<Transition>> getSdaRelations() {
        return sdaRelations;
    }

}

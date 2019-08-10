package com.iise.shudi.exroru;

import com.iise.shudi.exroru.dependency.importance.RelationImportance;
import com.iise.shudi.exroru.dependency.lc.LeastCommonPredecessorsAndSuccessors;
import com.iise.shudi.exroru.dependency.sda.SequentialDirectAdjacency;
import org.jbpt.petri.INode;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.unfolding.IBPNode;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;

import java.util.*;

@SuppressWarnings("rawtypes")
public class RefinedOrderingRelationsMatrix {

    public static final int FORWARD = 1;
    public static final int BACKWARD = 0;
    public static final String NEW_PS = "NEW_PS";
    public static final String NEW_PE = "NEW_PE";
    public static final String NEW_TS = "NEW_TS";
    public static final String NEW_TE = "NEW_TE";

    private NetSystem _sys;
    private ProperCompletePrefixUnfolding _cpu;
    private boolean _valid;
    private boolean _extend;
    private RefinedOrderingRelation[][] causalMatrix;
    private RefinedOrderingRelation[][] inverseCausalMatrix;
    private RefinedOrderingRelation[][] concurrentMatrix;
    private Map<String, Double[]> importance = new HashMap<>();
    private List<String> tName;

    private Set<Condition> _loopJoinConditions = new HashSet<>();
    private LeastCommonPredecessorsAndSuccessors _lc;
    private SequentialDirectAdjacency _sda;

    private long cpuTime = 0;
    private long lcTime = 0;
    private long causalTime = 0;
    private long concurrentTime = 0;
    private long sdaTime = 0;
    private long importanceTime = 0;

    public RefinedOrderingRelationsMatrix(NetSystem sys) {
        this(sys, true);
    }

    public RefinedOrderingRelationsMatrix(NetSystem sys, boolean extend) {
        long start = System.currentTimeMillis();
        this._extend = extend;
        this._valid = initialiseNetSystem(sys);
        if (!this._valid) {
            return;
        }
        this._sys = sys;
        this._cpu = new ProperCompletePrefixUnfolding(this._sys);
        this.cpuTime = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        this._lc = new LeastCommonPredecessorsAndSuccessors(this._cpu);
        this.lcTime = System.currentTimeMillis() - start;
        this._loopJoinConditions = getLoopJoinConditions();
        getTransitionNames();
        this.causalMatrix = new RefinedOrderingRelation[this.tName.size()][this.tName.size()];
        this.inverseCausalMatrix = new RefinedOrderingRelation[this.tName.size()][this.tName.size()];
        this.concurrentMatrix = new RefinedOrderingRelation[this.tName.size()][this.tName.size()];
        for (int i = 0; i < this.tName.size(); ++i) {
            for (int j = 0; j < this.tName.size(); ++j) {
                this.causalMatrix[i][j] = new RefinedOrderingRelation(Relation.NEVER, false, 0);
                this.inverseCausalMatrix[i][j] = new RefinedOrderingRelation(Relation.NEVER, false, 0);
                this.concurrentMatrix[i][j] = new RefinedOrderingRelation(Relation.NEVER, false, 0);
            }
        }
        start = System.currentTimeMillis();
        generateCausalAndInverseCausalMatrix();
        this.causalTime = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        generateConcurrentMatrix();
        this.concurrentTime = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        generateSequentialDirectAdjacency();
        this.sdaTime = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        generateRelationImportance();
        this.importanceTime = System.currentTimeMillis() - start;
    }

    public long[] getComputationTime() {
        return new long[]{this.cpuTime, this.lcTime, this.causalTime, this.concurrentTime,
                this.sdaTime, this.importanceTime};
    }

    private void generateCausalAndInverseCausalMatrix() {
        List<Transition> alObTransitions = new ArrayList<>(this._sys.getObservableTransitions());
        Collections.sort(alObTransitions, (t1, t2) -> {
            if (t1.getLabel().equals(NEW_TS) || t2.getLabel().equals(NEW_TE)) {
                return -1;
            } else if (t1.getLabel().equals(NEW_TE) || t2.getLabel().equals(NEW_TS)) {
                return 1;
            } else {
                return t1.getLabel().compareTo(t2.getLabel());
            }
        });
        for (int i = 0; i < alObTransitions.size(); ++i) {
            Transition fromTransition = alObTransitions.get(i);
            Set<Event> fromEvents = this._cpu.getEvents(fromTransition);
            for (int j = 0; j < alObTransitions.size(); ++j) {
                Transition toTransition = alObTransitions.get(j);
                Set<Event> toEvents = this._cpu.getEvents(toTransition);

                // if a == b && there is a trace a -> b, then a ->(S) b
                if (i == j) {
                    boolean selfLoop = false;
                    for (Event a : fromEvents) {
                        for (Event b : toEvents) {
                            if (findTrace(a, b, new HashSet<>()) != null) {
                                this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                                        .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                                this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                                        .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                                selfLoop = true;
                            }
                        }
                    }
                    if (!selfLoop) {
                        this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                                .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                        this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                                .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                    }
                    continue;
                }

                // a may have some shadow events
                // which need to be checked one by one
                // in the meanwhile
                // we only need to check one of b shadow events
                int forwardCount = 0;
                boolean hasSkipOrLoopForwardTrace = false;
                for (Event a : fromEvents) {
                    List<IBPNode> forwardTrace = null;
                    for (Event b : toEvents) {
                        // find one trace a -> b
                        forwardTrace = findTrace(a, b, new HashSet<>());
                        if (forwardTrace != null) {
                            ++forwardCount;
                            break;
                        }
                    }
                    // check if there is a trace from a -> a or a -> sink
                    // if so, end this loop
                    if (forwardTrace != null && hasSkipOrLoopTrace(forwardTrace, FORWARD)) {
                        hasSkipOrLoopForwardTrace = true;
                        break;
                    }
                }

                int backwardCount = 0;
                boolean hasSkipOrLoopBackwardTrace = false;
                for (Event a : fromEvents) {
                    List<IBPNode> backwardTrace = null;
                    for (Event b : toEvents) {
                        // find one trace a <- b
                        backwardTrace = findTrace(b, a, new HashSet<>());
                        if (backwardTrace != null) {
                            ++backwardCount;
                            break;
                        }
                    }
                    // check if there is a trace a <- a or source <- a
                    // if so, end this loop
                    if (backwardTrace != null && hasSkipOrLoopTrace(backwardTrace, BACKWARD)) {
                        hasSkipOrLoopBackwardTrace = true;
                        break;
                    }
                }

                // if there is no trace from a to b, a->(NEVER)->b
                // else if there is trace from part of a to b, a->(SOMETIMES)->b
                // else if there is always trace from a to b but some can be
                // skiped or looped, a->(SOMETIMES)->b
                // else if there is always trace from a to b and cannot be
                // skiped or looped, a->(ALWAYS)->b
                if (forwardCount == 0) {
                    this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                } else if (forwardCount < fromEvents.size()) {

                    this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else if (hasSkipOrLoopForwardTrace) {
                    this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else {
                    this.causalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.ALWAYS);
                }

                // analogously, a <- b has four cases
                if (backwardCount == 0) {
                    this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                } else if (backwardCount < fromEvents.size()) {
                    this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else if (hasSkipOrLoopBackwardTrace) {
                    this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else {
                    this.inverseCausalMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.ALWAYS);
                }
            }
        }
    }

    private boolean hasSkipOrLoopTrace(List<IBPNode> trace, int direction) {
        // a -> b FORWARD and a <- b BACKWARD
        IBPNode start = trace.get(0);
        IBPNode end = trace.get(trace.size() - 1);
        if (direction == FORWARD) {
            // self-loop
            if (start.getPetriNetNode() == end.getPetriNetNode()) {
                return true;
            }
            // check all the XOR-split conditions in this trace
            // if there is another trace from the XOR-split to a or sink
            // without b, return true
            // else return false
            for (int i = 1; i < trace.size() - 1; ++i) {
                IBPNode cur = trace.get(i);
                if (cur instanceof Condition) {
                    Condition curCondition = (Condition) cur;
                    if (curCondition.isCutoffPost() && curCondition.getPostE().isEmpty()) {
                        curCondition = curCondition.getCorrespondingCondition();
                    }
                    if (curCondition.getPostE().size() > 1) {
                        for (IBPNode succ : curCondition.getPostE()) {
                            if (succ != trace.get(i + 1)) {
                                // if has a loop
                                // the loop must not contain end
                                Set<IBPNode> visited = new HashSet<>();
                                // visited.add(end);
                                if (end instanceof Event) {
                                    visited.addAll(this._cpu.getEvents(((Event) end).getTransition()));
                                } else if (end instanceof Condition) {
                                    visited.addAll(this._cpu.getConditions(((Condition) end).getPlace()));
                                }
                                if (findTrace(succ, start, visited) != null) {
                                    return true;
                                }
                                // if succ skips end
                                if (this._lc.getForwardSysSkip().get(succ).get(end)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (direction == BACKWARD) {
            // self-loop
            if (start.getPetriNetNode() == end.getPetriNetNode()) {
                return true;
            }
            // check all the XOR-join conditions in this trace
            // if there is another backward trace from the XOR-join to a or
            // source without b, return true
            // else return false
            for (int i = trace.size() - 2; i > 0; --i) {
                IBPNode cur = trace.get(i);
                if (cur instanceof Condition) {
                    Condition curCondition = (Condition) cur;
                    Set<Condition> mappingConditions = new HashSet<>();
                    if (curCondition.getMappingConditions() != null) {
                        curCondition.getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                                .forEach(mappingConditions::add);
                    }
                    mappingConditions.add(curCondition);
                    if (mappingConditions.size() > 1) {
                        for (Condition curC : mappingConditions) {
                            IBPNode pred = curC.getPreEvent();
                            if (pred != trace.get(i - 1)) {
                                // if has a loop
                                // the loop must not contain start
                                Set<IBPNode> visited = new HashSet<>();
                                // visited.add(start);
                                if (start instanceof Event) {
                                    visited.addAll(this._cpu.getEvents(((Event) start).getTransition()));
                                } else if (start instanceof Condition) {
                                    visited.addAll(this._cpu.getConditions(((Condition) start).getPlace()));
                                }
                                if (findTrace(end, pred, visited) != null) {
                                    return true;
                                }
                                // if pred skips start
                                if (this._lc.getBackwardSysSkip().get(pred).get(start)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void generateConcurrentMatrix() {
        List<Transition> alObTransitions = new ArrayList<>(this._sys.getObservableTransitions());
        Collections.sort(alObTransitions, (t1, t2) -> {
            if (t1.getLabel().equals(NEW_TS) || t2.getLabel().equals(NEW_TE)) {
                return -1;
            } else if (t1.getLabel().equals(NEW_TE) || t2.getLabel().equals(NEW_TS)) {
                return 1;
            } else {
                return t1.getLabel().compareTo(t2.getLabel());
            }
        });
        for (int i = 0; i < alObTransitions.size(); ++i) {
            Transition fromTransition = alObTransitions.get(i);
            Set<Event> fromEvents = this._cpu.getEvents(fromTransition);
            for (int j = i + 1; j < alObTransitions.size(); ++j) {
                Transition toTransition = alObTransitions.get(j);
                Set<Event> toEvents = this._cpu.getEvents(toTransition);

                // a may have some shadow events
                // which need to be checked one by one
                // when determining the relation of a||b
                Map<IBPNode, Map<IBPNode, IBPNode>> aConcurrentIn = new HashMap<>();
                Map<IBPNode, Map<IBPNode, IBPNode>> bConcurrentIn = new HashMap<>();
                for (Event a : fromEvents) {
                    for (Event b : toEvents) {
                        boolean hasFoundAConcurrent = false;
                        Set<IBPNode> lcpSet = this._lc.getLcpCpuMap().get(a).get(b);
                        for (IBPNode lcp : lcpSet) {
                            if (lcp instanceof Event && lcp != a && lcp != b) {
                                aConcurrentIn.put(a, new HashMap<>());
                                aConcurrentIn.get(a).put(b, lcp);
                                hasFoundAConcurrent = true;
                                break;
                            }
                        }
                        if (hasFoundAConcurrent) {
                            break;
                        }
                    }
                }
                for (Event b : toEvents) {
                    for (Event a : fromEvents) {
                        boolean hasFoundAConcurrent = false;
                        Set<IBPNode> lcpSet = this._lc.getLcpCpuMap().get(b).get(a);
                        for (IBPNode lcp : lcpSet) {
                            if (lcp instanceof Event && lcp != b && lcp != a) {
                                bConcurrentIn.put(b, new HashMap<>());
                                bConcurrentIn.get(b).put(a, lcp);
                                hasFoundAConcurrent = true;
                                break;
                            }
                        }
                        if (hasFoundAConcurrent) {
                            break;
                        }
                    }
                }

                boolean aHasSometimesConcurrent = false;
                boolean bHasSometimesConcurrent = false;
                boolean noConcurrent = false;
                Iterator<Map.Entry<IBPNode, Map<IBPNode, IBPNode>>> outerIter = aConcurrentIn.entrySet().iterator();
                while (outerIter.hasNext() && (!aHasSometimesConcurrent || !bHasSometimesConcurrent)) {
                    Map.Entry<IBPNode, Map<IBPNode, IBPNode>> outerEntry = outerIter.next();
                    Event a = (Event) outerEntry.getKey();
                    Iterator<Map.Entry<IBPNode, IBPNode>> innerIter = outerEntry.getValue().entrySet().iterator();
                    while (innerIter.hasNext() && (!aHasSometimesConcurrent || !bHasSometimesConcurrent)) {
                        Map.Entry<IBPNode, IBPNode> innerEntry = innerIter.next();
                        Event b = (Event) innerEntry.getKey();
                        Event lcp = (Event) innerEntry.getValue();
                        boolean[] result = hasSometimesConcurrent(a, b, lcp);
                        if (result.length == 3) {
                            noConcurrent = true;
                        }
                        aHasSometimesConcurrent = result[0] ? result[0] : aHasSometimesConcurrent;
                        bHasSometimesConcurrent = result[1] ? result[1] : bHasSometimesConcurrent;
                    }
                }

                if (noConcurrent) {
                    this.concurrentMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                    this.concurrentMatrix[this.tName.indexOf(toTransition.getLabel())][this.tName
                            .indexOf(fromTransition.getLabel())].setRelation(Relation.NEVER);
                    continue;
                }

                if (aConcurrentIn.size() == 0) {
                    this.concurrentMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.NEVER);
                } else if (aConcurrentIn.size() < fromEvents.size()) {
                    this.concurrentMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else if (aHasSometimesConcurrent) {
                    this.concurrentMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else {
                    this.concurrentMatrix[this.tName.indexOf(fromTransition.getLabel())][this.tName
                            .indexOf(toTransition.getLabel())].setRelation(Relation.ALWAYS);
                }

                if (bConcurrentIn.size() == 0) {
                    this.concurrentMatrix[this.tName.indexOf(toTransition.getLabel())][this.tName
                            .indexOf(fromTransition.getLabel())].setRelation(Relation.NEVER);
                } else if (bConcurrentIn.size() < toEvents.size()) {
                    this.concurrentMatrix[this.tName.indexOf(toTransition.getLabel())][this.tName
                            .indexOf(fromTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else if (bHasSometimesConcurrent) {
                    this.concurrentMatrix[this.tName.indexOf(toTransition.getLabel())][this.tName
                            .indexOf(fromTransition.getLabel())].setRelation(Relation.SOMETIMES);
                } else {
                    this.concurrentMatrix[this.tName.indexOf(toTransition.getLabel())][this.tName
                            .indexOf(fromTransition.getLabel())].setRelation(Relation.ALWAYS);
                }
            }
        }
    }

    private boolean[] hasSometimesConcurrent(Event a, Event b, Event lcp) {
        boolean aHasSometimesConcurrent = false;
        boolean bHasSometimesConcurrent = false;
        Place sourcePlace = this._sys.getSourcePlaces().iterator().next();
        Condition sourceCondition = this._cpu.getConditions(sourcePlace).iterator().next();
        List<IBPNode> aTrace = findTrace(lcp, a, new HashSet<>());
        List<IBPNode> bTrace = findTrace(lcp, b, new HashSet<>());
        if (aTrace == null || bTrace == null) {
            return new boolean[]{false, false, false};
        }
        // find all the events after xor-split who can skip a|b
        Set<IBPNode> aSkipSplits = new HashSet<>();
        Set<IBPNode> bSkipSplits = new HashSet<>();
        // and all the events before xor-join who can skip lcp or loop a|b
        Set<IBPNode> aSkipJoins = new HashSet<>();
        Set<IBPNode> bSkipJoins = new HashSet<>();
        for (int i = 1; i < aTrace.size() - 1; ++i) {
            final int iCopy = i;
            IBPNode cur = aTrace.get(i);
            if (cur instanceof Condition) {
                Condition curCondition = (Condition) cur;
                // xor-split
                if (curCondition.isCutoffPost() && curCondition.getPostE().isEmpty()) {
                    curCondition = curCondition.getCorrespondingCondition();
                }
                if (curCondition.getPostE().size() > 1) {
                    curCondition.getPostE().stream().filter(succ -> succ != aTrace.get(iCopy + 1)
                            && this._lc.getForwardCpuSkip().get(succ).get(a)).forEach(aSkipSplits::add);
                }
                // xor-join
                Set<Condition> mappingConditions = new HashSet<>();
                if (curCondition.getMappingConditions() != null) {
                    curCondition.getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(mappingConditions::add);
                }
                mappingConditions.add(curCondition);
                if (mappingConditions.size() > 1) {
                    for (Condition curC : mappingConditions) {
                        Event pred = curC.getPreEvent();
                        if (pred != aTrace.get(i - 1)) {
                            Set<IBPNode> visited = new HashSet<>(aTrace);
                            if (findTrace(sourceCondition, pred, visited) != null) {
                                aSkipJoins.add(pred);
                                continue;
                            }
                            visited = new HashSet<>(aTrace.subList(0, aTrace.size() - 1));
                            if (a == pred || findTrace(a, pred, visited) != null) {
                                aSkipJoins.add(pred);
                            }
                        }
                    }
                }
            }
        }
        for (int j = 1; j < bTrace.size() - 1; ++j) {
            final int jCopy = j;
            IBPNode cur = bTrace.get(j);
            if (cur instanceof Condition) {
                Condition curCondition = (Condition) cur;
                // xor-split
                if (curCondition.isCutoffPost() && curCondition.getPostE().isEmpty()) {
                    curCondition = curCondition.getCorrespondingCondition();
                }
                if (curCondition.getPostE().size() > 1) {
                    curCondition.getPostE().stream().filter(succ -> succ != bTrace.get(jCopy + 1)
                            && this._lc.getForwardCpuSkip().get(succ).get(b)).forEach(bSkipSplits::add);
                }
                // xor-join
                Set<Condition> mappingConditions = new HashSet<>();
                if (curCondition.getMappingConditions() != null) {
                    curCondition.getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(mappingConditions::add);
                }
                mappingConditions.add(curCondition);
                if (mappingConditions.size() > 1) {
                    for (Condition curC : mappingConditions) {
                        Event pred = curC.getPreEvent();
                        if (pred != bTrace.get(j - 1)) {
                            Set<IBPNode> visited = new HashSet<>(bTrace);
                            if (findTrace(sourceCondition, pred, visited) != null) {
                                bSkipJoins.add(pred);
                                continue;
                            }
                            visited = new HashSet<>(bTrace.subList(0, bTrace.size() - 1));
                            if (b == pred || findTrace(b, pred, visited) != null) {
                                bSkipJoins.add(pred);
                            }
                        }
                    }
                }
            }
        }
        // check xor-split
        Iterator<IBPNode> itAi = aSkipSplits.iterator();
        while (itAi.hasNext()) {
            IBPNode ai = itAi.next();
            Iterator<IBPNode> itBj = bSkipSplits.iterator();
            while (itBj.hasNext()) {
                IBPNode bj = itBj.next();
                if (ai == bj) {
                    itAi.remove();
                    itBj.remove();
                } else {
                    return new boolean[]{true, true};
                }
            }
        }
        if (!aSkipSplits.isEmpty()) {
            bHasSometimesConcurrent = true;
        }
        if (!bSkipSplits.isEmpty()) {
            aHasSometimesConcurrent = true;
        }
        // check xor-join
        itAi = aSkipJoins.iterator();
        while (itAi.hasNext() && (!aHasSometimesConcurrent || !bHasSometimesConcurrent)) {
            IBPNode ai = itAi.next();
            Iterator<IBPNode> itBj = bSkipJoins.iterator();
            while (itBj.hasNext()) {
                IBPNode bj = itBj.next();
                Set<IBPNode> _lcpSet = this._lc.getLcpCpuMap().get(ai).get(bj);
                boolean hasConditionLcp = false;
                for (IBPNode _lcp : _lcpSet) {
                    if (_lcp instanceof Condition || _lcp == lcp) {
                        hasConditionLcp = true;
                        break;
                    }
                }
                if (!hasConditionLcp) {
                    itAi.remove();
                    itBj.remove();
                }
            }
        }
        if (!aSkipJoins.isEmpty()) {
            aHasSometimesConcurrent = true;
        }
        if (!bSkipJoins.isEmpty()) {
            bHasSometimesConcurrent = true;
        }
        return new boolean[]{aHasSometimesConcurrent, bHasSometimesConcurrent};
    }

    private void generateSequentialDirectAdjacency() {
        this._sda = new SequentialDirectAdjacency(this._cpu, this._lc);
        for (Map.Entry<Transition, Set<Transition>> entry : this._sda.getSdaRelations().entrySet()) {
            Transition key = entry.getKey();
            for (Transition value : entry.getValue()) {
                this.causalMatrix[this.tName.indexOf(key.getLabel())][this.tName
                        .indexOf(value.getLabel())].setAdjacency(true);
                this.inverseCausalMatrix[this.tName.indexOf(key.getLabel())][this.tName
                        .indexOf(value.getLabel())].setAdjacency(true);
            }
        }
    }

    private void generateRelationImportance() {
        RelationImportance _ri = new RelationImportance(this._cpu);
        for (Map.Entry<IBPNode, Map<IBPNode, Double>> outerEntry : _ri.getImportance().entrySet()) {
            IBPNode from = outerEntry.getKey();
            for (Map.Entry<IBPNode, Double> innerEntry : outerEntry.getValue().entrySet()) {
                IBPNode to = innerEntry.getKey();
                double im = innerEntry.getValue();
                if (from instanceof Event) {
                    this.importance.putIfAbsent(((Event) from).getTransition().getLabel(), new Double[2]);
                    this.importance.get(((Event) from).getTransition().getLabel())[0] = im;
                } else if (to instanceof Event) {
                    this.importance.putIfAbsent(((Event) to).getTransition().getLabel(), new Double[2]);
                    this.importance.get(((Event) to).getTransition().getLabel())[1] = im;
                }
            }
        }
        for (int i = 0; i < tName.size(); ++i) {
            for (int j = 0; j < tName.size(); ++j) {
                String fromTransition = tName.get(i);
                String toTransition = tName.get(j);
                double coef = Math.min(this.importance.get(fromTransition)[0], this.importance.get(toTransition)[1]);
                this.causalMatrix[i][j].setImportance(coef);
                this.inverseCausalMatrix[i][j].setImportance(coef);
                this.concurrentMatrix[i][j].setImportance(coef);
            }
        }
    }

    // find a trace from start to end return a trace without a loop xor-join if possible
    private List<IBPNode> findTrace(IBPNode start, IBPNode end, Set<IBPNode> visited) {
        if (visited.contains(start) || visited.contains(end)) {
            return null;
        }
        List<IBPNode> trace = new ArrayList<>();
        Place sinkPlace = this._sys.getSinkPlaces().iterator().next();
        Map<List<IBPNode>, Boolean> traceMap = new HashMap<>();
        dfsFindTrace(start, end, trace, sinkPlace, false, traceMap, visited);
        if (traceMap.size() == 0) {
            return null;
        }
        for (Map.Entry<List<IBPNode>, Boolean> entry : traceMap.entrySet()) {
            if (!entry.getValue()) {
                return entry.getKey();
            }
        }
        return traceMap.keySet().iterator().next();
    }

    // dfs to find all the traces from start to end while marking if the trace contains a loop xor-join
    private void dfsFindTrace(IBPNode cur, IBPNode end, List<IBPNode> trace, Place sinkPlace, boolean containLoop,
                              Map<List<IBPNode>, Boolean> traceMap, Set<IBPNode> visited) {
        if (cur == end && !trace.isEmpty()) {
            List<IBPNode> tmp = new ArrayList<>(trace);
            tmp.add(cur);
            traceMap.put(tmp, containLoop);
        } else if (visited.contains(cur) || (cur instanceof Condition && ((Condition) cur).getPlace() == sinkPlace)) {
            System.out.print("");
        } else if (cur instanceof Condition && this._loopJoinConditions.contains(cur)) {
            cur = ((Condition) cur).getCorrespondingCondition();
            dfsFindTrace(cur, end, trace, sinkPlace, true, traceMap, visited);
        } else if (cur instanceof Condition && ((Condition) cur).isCutoffPost()
                && ((Condition) cur).getPostE().isEmpty()) {
            cur = ((Condition) cur).getCorrespondingCondition();
            dfsFindTrace(cur, end, trace, sinkPlace, containLoop, traceMap, visited);
        } else {
            trace.add(cur);
            visited.add(cur);
            if (cur instanceof Condition) {
                Set<Event> curSuccSet = ((Condition) cur).getPostE();
                for (Event curSucc : curSuccSet) {
                    dfsFindTrace(curSucc, end, trace, sinkPlace, containLoop, traceMap, visited);
                }
            } else if (cur instanceof Event) {
                Set<Condition> curSuccSet = ((Event) cur).getPostConditions();
                for (Condition curSucc : curSuccSet) {
                    dfsFindTrace(curSucc, end, trace, sinkPlace, containLoop, traceMap, visited);
                }
            }
            visited.remove(cur);
            trace.remove(trace.size() - 1);
        }
    }

    // dfs to get all the XOR-join conditions which ends a loop
    private Set<Condition> getLoopJoinConditions() {
        Set<Condition> loopJoinConditions = new HashSet<>();
        Condition source = this._cpu.getInitialCut().iterator().next();
        Set<INode> visited = new HashSet<>();
        dfsLoopJoin(source, visited, loopJoinConditions);
        return loopJoinConditions;
    }

    private void dfsLoopJoin(IBPNode u, Set<INode> visited, Set<Condition> loopJoinConditions) {
        if (u instanceof Condition && visited.contains(u.getPetriNetNode()) && ((Condition) u).isCutoffPost()
                && ((Condition) u).getPostE().isEmpty()) {
            loopJoinConditions.add((Condition) u);
            return;
        }
        visited.add(u.getPetriNetNode());
        if (u instanceof Condition) {
            for (Event uSucc : ((Condition) u).getPostE()) {
                dfsLoopJoin(uSucc, visited, loopJoinConditions);
            }
        } else {
            for (Condition uSucc : ((Event) u).getPostConditions()) {
                dfsLoopJoin(uSucc, visited, loopJoinConditions);
            }
        }
        visited.remove(u.getPetriNetNode());
    }

    private void getTransitionNames() {
        this.tName = new ArrayList<>();
        this._sys.getTransitions().stream().filter(Transition::isObservable).forEach(t -> tName.add(t.getLabel()));
        Collections.sort(tName, (t1, t2) -> {
            if (t1.equals(NEW_TS) || t2.equals(NEW_TE)) {
                return -1;
            } else if (t1.equals(NEW_TE) || t2.equals(NEW_TS)) {
                return 1;
            } else {
                return t1.compareTo(t2);
            }
        });
    }

    private boolean initialiseNetSystem(NetSystem net) {
        if (!checkNetSystem(net)) {
            return false;
        }
        if (this._extend) {
            extendNetSystem(net);
        }
        net.getNodes().forEach(n -> n.setName(n.getLabel()));
        net.getTransitions().stream().filter(t -> t.getLabel().toLowerCase().startsWith("inv_")).forEach(t -> t.setLabel(""));
        net.getPlaces().stream().filter(p -> net.getIncomingEdges(p).isEmpty()).forEach(p -> net.getMarking().put(p, 1));
        return true;
    }

    private boolean checkNetSystem(NetSystem net) {
        if (net.getSourcePlaces().size() == 1 && net.getSourceTransitions().size() == 0
                && net.getSinkPlaces().size() == 1 && net.getSinkTransitions().size() == 0) {
            return true;
        }
        return false;
    }

    private void extendNetSystem(NetSystem net) {
        Place source = net.getSourcePlaces().iterator().next();
        Place sink = net.getSinkPlaces().iterator().next();
        Place ps = new Place(NEW_PS);
        Place pe = new Place(NEW_PE);
        Transition ts = new Transition(NEW_TS);
        Transition te = new Transition(NEW_TE);
        net.addFlow(ps, ts);
        net.addFlow(ts, source);
        net.addFlow(sink, te);
        net.addFlow(te, pe);
    }

    public void printMatrix() {
        int n = this.tName.size();
        System.out.print("[");
        for (int i = 0; i < n - 1; ++i) {
            System.out.print("\"" + this.tName.get(i) + "\", ");
        }
        System.out.println("\"" + this.tName.get(n - 1) + "\"]");

        System.out.println("Causal Matrix");
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.print(this.causalMatrix[i][j].toString() + " ");
            }
            System.out.println();
        }

        System.out.println("Inverse Causal Matrix");
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.print(this.inverseCausalMatrix[i][j].toString() + " ");
            }
            System.out.println();
        }

        System.out.println("Concurrent Matrix");
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.print(this.concurrentMatrix[i][j].toString() + " ");
            }
            System.out.println();
        }

        System.out.println("Sequential Direct Adjacency");
        System.out.println(this._sda.toString());
    }

    public void print() {
        int n = this.tName.size();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.print(this.tName.get(i) + "-" + this.tName.get(j) + ": ");
                System.out.println("[" + this.causalMatrix[i][j] + " " + this.inverseCausalMatrix[i][j]
                        + " " + this.concurrentMatrix[i][j] + "]");
            }
        }
        System.out.println("Sequential Direct Adjacency");
        System.out.println(this._sda.toString());
    }

    public RefinedOrderingRelation[][] getCausalMatrix() {
        return causalMatrix;
    }

    public RefinedOrderingRelation[][] getInverseCausalMatrix() {
        return inverseCausalMatrix;
    }

    public RefinedOrderingRelation[][] getConcurrentMatrix() {
        return concurrentMatrix;
    }

    public List<String> gettName() {
        return tName;
    }

    public boolean isValid() {
        return _valid;
    }

}

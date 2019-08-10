package com.iise.shudi.exroru.dependency.lc;

import org.jbpt.petri.INode;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.unfolding.*;

import java.util.*;

@SuppressWarnings("rawtypes")
public class LeastCommonPredecessorsAndSuccessors {

    private NetSystem _sys;
    private ProperCompletePrefixUnfolding _cpu;
    private Set<Condition> _loopJoinConditions;
    private Map<INode, Map<INode, Boolean>> sysReachMap;
    private Map<IBPNode, Map<IBPNode, Boolean>> cpuReachMap;

    // lcp and lcs based on sysReachMap
    private Map<IBPNode, Map<IBPNode, Set<IBPNode>>> lcpSysMap = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Set<IBPNode>>> lcsSysMap = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Boolean>> forwardSysSkip = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Boolean>> backwardSysSkip = new HashMap<>();
    // lcp and lcs based on cpuReachmap
    private Map<IBPNode, Map<IBPNode, Set<IBPNode>>> lcpCpuMap = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Set<IBPNode>>> lcsCpuMap = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Boolean>> forwardCpuSkip = new HashMap<>();
    private Map<IBPNode, Map<IBPNode, Boolean>> backwardCpuSkip = new HashMap<>();

    public LeastCommonPredecessorsAndSuccessors(ProperCompletePrefixUnfolding cpu) {
        this._cpu = cpu;
        this._sys = (NetSystem) cpu.getOriginativeNetSystem();
        this._loopJoinConditions = getLoopJoinConditions();
        initReachMap();

        List<IBPNode> vertices = new ArrayList<>();
        vertices.addAll(this._cpu.getEvents());
        vertices.addAll(this._cpu.getConditions());
        for (int i = 0; i < vertices.size(); ++i) {
            IBPNode u = vertices.get(i);
            this.lcpSysMap.put(u, new HashMap<>());
            this.lcsSysMap.put(u, new HashMap<>());
            this.forwardSysSkip.put(u, new HashMap<>());
            this.backwardSysSkip.put(u, new HashMap<>());
            this.lcpCpuMap.put(u, new HashMap<>());
            this.lcsCpuMap.put(u, new HashMap<>());
            this.forwardCpuSkip.put(u, new HashMap<>());
            this.backwardCpuSkip.put(u, new HashMap<>());
            for (IBPNode v : vertices) {
                this.lcpSysMap.get(u).put(v, new HashSet<>());
                this.lcsSysMap.get(u).put(v, new HashSet<>());
                this.forwardSysSkip.get(u).put(v, false);
                this.backwardSysSkip.get(u).put(v, false);
                this.lcpCpuMap.get(u).put(v, new HashSet<>());
                this.lcsCpuMap.get(u).put(v, new HashSet<>());
                this.forwardCpuSkip.get(u).put(v, false);
                this.backwardCpuSkip.get(u).put(v, false);
            }
        }
        generateLcpAndLcsMap();
    }

    private void generateLcpAndLcsMap() {
        List<Event> events = new ArrayList<>(this._cpu.getEvents());
        for (int i = 0; i < events.size(); ++i) {
            Event e1 = events.get(i);
            for (int j = i + 1; j < events.size(); ++j) {
                Event e2 = events.get(j);
                boolean[] skipForwardSys = hasSkipForwardSys(e1, e2);
                this.forwardSysSkip.get(e1).put(e2, skipForwardSys[0]);
                this.forwardSysSkip.get(e2).put(e1, skipForwardSys[1]);
                boolean[] skipBackwardSys = hasSkipBackwardSys(e1, e2);
                this.backwardSysSkip.get(e1).put(e2, skipBackwardSys[0]);
                this.backwardSysSkip.get(e2).put(e1, skipBackwardSys[1]);
                boolean[] skipForwardCpu = hasSkipForwardCpu(e1, e2);
                this.forwardCpuSkip.get(e1).put(e2, skipForwardCpu[0]);
                this.forwardCpuSkip.get(e2).put(e1, skipForwardCpu[1]);
                boolean[] skipBackwardCpu = hasSkipBackwardCpu(e1, e2);
                this.backwardCpuSkip.get(e1).put(e2, skipBackwardCpu[0]);
                this.backwardCpuSkip.get(e2).put(e1, skipBackwardCpu[1]);
            }
        }
    }

    /**
     * used to check if e1 skips e2 and if e2 skips e1, forwards
     */
    private boolean[] hasSkipForwardSys(Event e1, Event e2) {
        Set<IBPNode> lcsSet = new HashSet<>();
        boolean hasSinkSucc1 = false, hasSinkSucc2 = false;
        // boolean hasLoopSucc1 = false, hasLoopSucc2 = false;
        Place sink = this._sys.getSinkPlaces().iterator().next();
        // step e1
        Queue<IBPNode> queue = new LinkedList<>();
        queue.offer(e1);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e2.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopSucc1 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == sink) {
                hasSinkSucc1 = true;
                if (this.sysReachMap.get(e2.getPetriNetNode()).get(u.getPetriNetNode())) {
                    lcsSet.add(u);
                }
            } else if (u instanceof Condition && ((Condition) u).isCutoffPost() && ((Condition) u).getPostE().isEmpty()) {
                u = ((Condition) u).getCorrespondingCondition();
                queue.offer(u);
            } else {
                if (this.sysReachMap.get(e2.getPetriNetNode()).get(u.getPetriNetNode())) {
                    lcsSet.add(u);
                    continue;
                }
                if (u instanceof Condition) {
                    ((Condition) u).getPostE().forEach(queue::offer);
                } else if (u instanceof Event) {
                    ((Event) u).getPostConditions().forEach(queue::offer);
                }
            }
        }
        // step e2
        queue.clear();
        queue.offer(e2);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e1.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopSucc2 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == sink) {
                hasSinkSucc2 = true;
                if (this.sysReachMap.get(e1.getPetriNetNode()).get(u.getPetriNetNode())) {
                    lcsSet.add(u);
                }
            } else if (u instanceof Condition && ((Condition) u).isCutoffPost() && ((Condition) u).getPostE().isEmpty()) {
                u = ((Condition) u).getCorrespondingCondition();
                queue.offer(u);
            } else {
                if (this.sysReachMap.get(e1.getPetriNetNode()).get(u.getPetriNetNode())) {
                    lcsSet.add(u);
                    continue;
                }
                if (u instanceof Condition) {
                    ((Condition) u).getPostE().forEach(queue::offer);
                } else if (u instanceof Event) {
                    ((Event) u).getPostConditions().forEach(queue::offer);
                }
            }
        }
        lcsSet = filterLcsSysSet(lcsSet);
        // analysis
        this.lcsSysMap.get(e1).get(e2).addAll(lcsSet);
        this.lcsSysMap.get(e2).get(e1).addAll(lcsSet);
        boolean e1SkipE2 = false, e2SkipE1 = false;
        for (IBPNode lcs : lcsSet) {
            // if(lcs instanceof Condition) {
            if (lcs != e1 && lcs != e2) {
                e1SkipE2 = true;
                e2SkipE1 = true;
                break;
            } else if (lcs == e1) {
                e1SkipE2 = true;
            } else {
                e2SkipE1 = true;
            }
        }
        if (hasSinkSucc1) {
            e1SkipE2 = true;
        }
        if (hasSinkSucc2) {
            e2SkipE1 = true;
        }
        return new boolean[]{e1SkipE2, e2SkipE1};
    }

    /**
     * filter lcs set to only keep the least common successors
     */
    private Set<IBPNode> filterLcsSysSet(Set<IBPNode> oriLcsSet) {
        Set<IBPNode> lcsSet = new HashSet<>();
        for (IBPNode lcs : oriLcsSet) {
            Iterator<IBPNode> it = lcsSet.iterator();
            boolean filter = false;
            while (it.hasNext()) {
                IBPNode v = it.next();
                if (this.sysReachMap.get(v.getPetriNetNode()).get(lcs.getPetriNetNode())) {
                    filter = true;
                    break;
                }
                if (this.sysReachMap.get(lcs.getPetriNetNode()).get(v.getPetriNetNode())) {
                    it.remove();
                }
            }
            if (!filter) {
                lcsSet.add(lcs);
            }
        }
        return lcsSet;
    }

    /**
     * used to check if e1 skips e2 and if e2 skips e1, backwards
     */
    private boolean[] hasSkipBackwardSys(Event e1, Event e2) {
        Set<IBPNode> lcpSet = new HashSet<>();
        boolean hasSourcePred1 = false, hasSourcePred2 = false;
        // boolean hasLoopPred1 = false, hasLoopPred2 = false;
        Place source = this._sys.getSourcePlaces().iterator().next();
        // step e1
        Queue<IBPNode> queue = new LinkedList<>();
        queue.offer(e1);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e2.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopPred1 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == source) {
                hasSourcePred1 = true;
                if (this.sysReachMap.get(u.getPetriNetNode()).get(e2.getPetriNetNode())) {
                    lcpSet.add(u);
                }
            } else if (u instanceof Condition) {
                Set<Condition> conditions = new HashSet<>();
                if (((Condition) u).getMappingConditions() != null) {
                    ((Condition) u).getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(conditions::add);
                }
                conditions.add((Condition) u);
                for (Condition c : conditions) {
                    if (this._loopJoinConditions.contains(c)) {
                        continue;
                    }
                    if (this.sysReachMap.get(c.getPetriNetNode()).get(e2.getPetriNetNode())) {
                        lcpSet.add(c);
                        continue;
                    }
                    queue.offer(c.getPreEvent());
                }
            } else if (u instanceof Event) {
                if (this.sysReachMap.get(u.getPetriNetNode()).get(e2.getPetriNetNode())) {
                    lcpSet.add(u);
                    continue;
                }
                ((Event) u).getPreConditions().forEach(queue::offer);
            }
        }
        // step e2
        queue.clear();
        queue.offer(e2);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e1.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopPred2 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == source) {
                hasSourcePred2 = true;
                if (this.sysReachMap.get(u.getPetriNetNode()).get(e1.getPetriNetNode())) {
                    lcpSet.add(u);
                }
            } else if (u instanceof Condition) {
                Set<Condition> conditions = new HashSet<>();
                if (((Condition) u).getMappingConditions() != null) {
                    ((Condition) u).getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(conditions::add);
                }
                conditions.add((Condition) u);
                for (Condition c : conditions) {
                    if (this._loopJoinConditions.contains(c)) {
                        continue;
                    }
                    if (this.sysReachMap.get(c.getPetriNetNode()).get(e1.getPetriNetNode())) {
                        lcpSet.add(c);
                        continue;
                    }
                    queue.offer(c.getPreEvent());
                }
            } else if (u instanceof Event) {
                if (this.sysReachMap.get(u.getPetriNetNode()).get(e1.getPetriNetNode())) {
                    lcpSet.add(u);
                    continue;
                }
                ((Event) u).getPreConditions().forEach(queue::offer);
            }
        }
        lcpSet = filterLcpSysSet(lcpSet);
        // analysis
        this.lcpSysMap.get(e1).get(e2).addAll(lcpSet);
        this.lcpSysMap.get(e2).get(e1).addAll(lcpSet);
        boolean e1SkipE2 = false, e2SkipE1 = false;
        for (IBPNode lcp : lcpSet) {
            // if(lcp instanceof Condition) {
            if (lcp != e1 && lcp != e2) {
                e1SkipE2 = true;
                e2SkipE1 = true;
                break;
            } else if (lcp == e1) {
                e1SkipE2 = true;
            } else {
                e2SkipE1 = true;
            }
        }
        if (hasSourcePred1) {
            e1SkipE2 = true;
        }
        if (hasSourcePred2) {
            e2SkipE1 = true;
        }
        return new boolean[]{e1SkipE2, e2SkipE1};
    }

    /**
     * filter lcp set to only keep the least common predecessors
     */
    private Set<IBPNode> filterLcpSysSet(Set<IBPNode> oriLcpSet) {
        Set<IBPNode> lcpSet = new HashSet<>();
        for (IBPNode lcp : oriLcpSet) {
            Iterator<IBPNode> it = lcpSet.iterator();
            boolean filter = false;
            while (it.hasNext()) {
                IBPNode v = it.next();
                if (this.sysReachMap.get(lcp.getPetriNetNode()).get(v.getPetriNetNode())) {
                    filter = true;
                    break;
                }
                if (this.sysReachMap.get(v.getPetriNetNode()).get(lcp.getPetriNetNode())) {
                    it.remove();
                }
            }
            if (!filter) {
                lcpSet.add(lcp);
            }
        }
        return lcpSet;
    }

    /**
     * used to check if e1 skips e2 and if e2 skips e1, forwards
     */
    private boolean[] hasSkipForwardCpu(Event e1, Event e2) {
        Set<IBPNode> lcsSet = new HashSet<>();
        boolean hasSinkSucc1 = false, hasSinkSucc2 = false;
        // boolean hasLoopSucc1 = false, hasLoopSucc2 = false;
        Place sink = this._sys.getSinkPlaces().iterator().next();
        // step e1
        Queue<IBPNode> queue = new LinkedList<>();
        queue.offer(e1);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e2.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopSucc1 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == sink) {
                hasSinkSucc1 = true;
                if (this.cpuReachMap.get(e2).get(u)) {
                    lcsSet.add(u);
                }
            } else if (u instanceof Condition && ((Condition) u).isCutoffPost() && ((Condition) u).getPostE().isEmpty()) {
                u = ((Condition) u).getCorrespondingCondition();
                queue.offer(u);
            } else {
                if (this.cpuReachMap.get(e2).get(u)) {
                    lcsSet.add(u);
                    continue;
                }
                if (u instanceof Condition) {
                    ((Condition) u).getPostE().forEach(queue::offer);
                } else if (u instanceof Event) {
                    ((Event) u).getPostConditions().forEach(queue::offer);
                }
            }
        }
        // step e2
        queue.clear();
        queue.offer(e2);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e1.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopSucc2 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == sink) {
                hasSinkSucc2 = true;
                if (this.cpuReachMap.get(e1).get(u)) {
                    lcsSet.add(u);
                }
            } else if (u instanceof Condition && ((Condition) u).isCutoffPost() && ((Condition) u).getPostE().isEmpty()) {
                u = ((Condition) u).getCorrespondingCondition();
                queue.offer(u);
            } else {
                if (this.cpuReachMap.get(e1).get(u)) {
                    lcsSet.add(u);
                    continue;
                }
                if (u instanceof Condition) {
                    ((Condition) u).getPostE().forEach(queue::offer);
                } else if (u instanceof Event) {
                    ((Event) u).getPostConditions().forEach(queue::offer);
                }
            }
        }
        lcsSet = filterLcsCpuSet(lcsSet);
        // analysis
        this.lcsCpuMap.get(e1).get(e2).addAll(lcsSet);
        this.lcsCpuMap.get(e2).get(e1).addAll(lcsSet);
        boolean e1SkipE2 = false, e2SkipE1 = false;
        for (IBPNode lcs : lcsSet) {
            // if(lcs instanceof Condition) {
            if (lcs != e1 && lcs != e2) {
                e1SkipE2 = true;
                e2SkipE1 = true;
                break;
            } else if (lcs == e1) {
                e1SkipE2 = true;
            } else {
                e2SkipE1 = true;
            }
        }
        if (hasSinkSucc1) {
            e1SkipE2 = true;
        }
        if (hasSinkSucc2) {
            e2SkipE1 = true;
        }
        return new boolean[]{e1SkipE2, e2SkipE1};
    }

    /**
     * filter lcs set to only keep the least common successors
     */
    private Set<IBPNode> filterLcsCpuSet(Set<IBPNode> oriLcsSet) {
        Set<IBPNode> lcsSet = new HashSet<>();
        for (IBPNode lcs : oriLcsSet) {
            Iterator<IBPNode> it = lcsSet.iterator();
            boolean filter = false;
            while (it.hasNext()) {
                IBPNode v = it.next();
                if (this.cpuReachMap.get(v).get(lcs)) {
                    filter = true;
                    break;
                }
                if (this.cpuReachMap.get(lcs).get(v)) {
                    it.remove();
                }
            }
            if (!filter) {
                lcsSet.add(lcs);
            }
        }
        return lcsSet;
    }

    /**
     * used to check if e1 skips e2 and if e2 skips e1, backwards
     */
    private boolean[] hasSkipBackwardCpu(Event e1, Event e2) {
        Set<IBPNode> lcpSet = new HashSet<>();
        boolean hasSourcePred1 = false, hasSourcePred2 = false;
        // boolean hasLoopPred1 = false, hasLoopPred2 = false;
        Place source = this._sys.getSourcePlaces().iterator().next();
        // step e1
        Queue<IBPNode> queue = new LinkedList<>();
        queue.offer(e1);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e2.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopPred1 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == source) {
                hasSourcePred1 = true;
                if (this.cpuReachMap.get(u).get(e2)) {
                    lcpSet.add(u);
                }
            } else if (u instanceof Condition) {
                Set<Condition> conditions = new HashSet<>();
                if (((Condition) u).getMappingConditions() != null) {
                    ((Condition) u).getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(conditions::add);
                }
                conditions.add((Condition) u);
                for (Condition c : conditions) {
                    if (this._loopJoinConditions.contains(c)) {
                        continue;
                    }
                    if (this.cpuReachMap.get(c).get(e2)) {
                        lcpSet.add(c);
                        continue;
                    }
                    queue.offer(c.getPreEvent());
                }
            } else if (u instanceof Event) {
                if (this.cpuReachMap.get(u).get(e2)) {
                    lcpSet.add(u);
                    continue;
                }
                ((Event) u).getPreConditions().forEach(queue::offer);
            }
        }
        // step e2
        queue.clear();
        queue.offer(e2);
        while (!queue.isEmpty()) {
            IBPNode u = queue.poll();
            if (u instanceof Event && ((Event) u).getTransition() == e1.getTransition()) {
                System.out.print("");
            } else if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
                // hasLoopPred2 = true;
                System.out.print("");
            } else if (u instanceof Condition && ((Condition) u).getPlace() == source) {
                hasSourcePred2 = true;
                if (this.cpuReachMap.get(u).get(e1)) {
                    lcpSet.add(u);
                }
            } else if (u instanceof Condition) {
                Set<Condition> conditions = new HashSet<>();
                if (((Condition) u).getMappingConditions() != null) {
                    ((Condition) u).getMappingConditions().stream().filter(c -> c.getPostE().isEmpty())
                            .forEach(conditions::add);
                }
                conditions.add((Condition) u);
                for (Condition c : conditions) {
                    if (this._loopJoinConditions.contains(c)) {
                        continue;
                    }
                    if (this.cpuReachMap.get(c).get(e1)) {
                        lcpSet.add(c);
                        continue;
                    }
                    queue.offer(c.getPreEvent());
                }
            } else if (u instanceof Event) {
                if (this.cpuReachMap.get(u).get(e1)) {
                    lcpSet.add(u);
                    continue;
                }
                ((Event) u).getPreConditions().forEach(queue::offer);
            }
        }
        lcpSet = filterLcpCpuSet(lcpSet);
        // analysis
        this.lcpCpuMap.get(e1).get(e2).addAll(lcpSet);
        this.lcpCpuMap.get(e2).get(e1).addAll(lcpSet);
        boolean e1SkipE2 = false, e2SkipE1 = false;
        for (IBPNode lcp : lcpSet) {
            // if(lcp instanceof Condition) {
            if (lcp != e1 && lcp != e2) {
                e1SkipE2 = true;
                e2SkipE1 = true;
                break;
            } else if (lcp == e1) {
                e1SkipE2 = true;
            } else {
                e2SkipE1 = true;
            }
        }
        if (hasSourcePred1) {
            e1SkipE2 = true;
        }
        if (hasSourcePred2) {
            e2SkipE1 = true;
        }
        return new boolean[]{e1SkipE2, e2SkipE1};
    }

    /**
     * filter lcp set to only keep the least common predecessors
     */
    private Set<IBPNode> filterLcpCpuSet(Set<IBPNode> oriLcpSet) {
        Set<IBPNode> lcpSet = new HashSet<>();
        for (IBPNode lcp : oriLcpSet) {
            Iterator<IBPNode> it = lcpSet.iterator();
            boolean filter = false;
            while (it.hasNext()) {
                IBPNode v = it.next();
                if (this.cpuReachMap.get(lcp).get(v)) {
                    filter = true;
                    break;
                }
                if (this.cpuReachMap.get(v).get(lcp)) {
                    it.remove();
                }
            }
            if (!filter) {
                lcpSet.add(lcp);
            }
        }
        return lcpSet;
    }

    /**
     * dfs to get all the XOR-join conditions which ends a loop
     */
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

    /**
     * dfs to get reachable matrix
     */
    private void initReachMap() {
        this.sysReachMap = new HashMap<>();
        List<INode> sysNodes = new ArrayList<>();
        sysNodes.addAll(this._sys.getTransitions());
        sysNodes.addAll(this._sys.getPlaces());
        for (int i = 0; i < sysNodes.size(); ++i) {
            INode u = sysNodes.get(i);
            this.sysReachMap.put(u, new HashMap<>());
            for (INode v : sysNodes) {
                this.sysReachMap.get(u).put(v, false);
            }
        }

        this.cpuReachMap = new HashMap<>();
        List<IBPNode> cpuNodes = new ArrayList<>();
        cpuNodes.addAll(this._cpu.getEvents());
        cpuNodes.addAll(this._cpu.getConditions());
        for (int i = 0; i < cpuNodes.size(); ++i) {
            IBPNode u = cpuNodes.get(i);
            this.cpuReachMap.put(u, new HashMap<>());
            for (IBPNode v : cpuNodes) {
                this.cpuReachMap.get(u).put(v, false);
            }
        }

        Condition source = this._cpu.getInitialCut().iterator().next();
        Place sink = this._sys.getSinkPlaces().iterator().next();
        List<IBPNode> trace = new ArrayList<>();
        dfsReachMap(source, trace, sink);
    }

    private void dfsReachMap(IBPNode u, List<IBPNode> trace, Place sink) {
        if (u instanceof Condition && this._loopJoinConditions.contains(u)) {
            generateReachMap(trace);
        } else if (u instanceof Condition && u.getPetriNetNode() == sink) {
            Condition sinkCondition = (Condition) u;
            if (sinkCondition.isCutoffPost()) {
                sinkCondition = sinkCondition.getCorrespondingCondition();
            }
            trace.add(sinkCondition);
            generateReachMap(trace);
            trace.remove(trace.size() - 1);
        } else if (u instanceof Condition && ((Condition) u).isCutoffPost() && ((Condition) u).getPostE().isEmpty()) {
            u = ((Condition) u).getCorrespondingCondition();
            dfsReachMap(u, trace, sink);
        } else {
            trace.add(u);
            if (u instanceof Condition) {
                Set<Event> uSuccSet = ((Condition) u).getPostE();
                for (Event uSucc : uSuccSet) {
                    dfsReachMap(uSucc, trace, sink);
                }
            } else if (u instanceof Event) {
                Set<Condition> uSuccSet = ((Event) u).getPostConditions();
                for (Condition uSucc : uSuccSet) {
                    dfsReachMap(uSucc, trace, sink);
                }
            }
            trace.remove(trace.size() - 1);
        }
    }

    private void generateReachMap(List<IBPNode> trace) {
        for (int i = 0; i < trace.size(); ++i) {
            IBPNode u = trace.get(i);
            for (int j = i + 1; j < trace.size(); ++j) {
                IBPNode v = trace.get(j);
                this.sysReachMap.get(u.getPetriNetNode()).put(v.getPetriNetNode(), true);
                this.cpuReachMap.get(u).put(v, true);
            }
        }
    }

    public Map<INode, Map<INode, Boolean>> getSysReachMap() {
        return sysReachMap;
    }

    public Map<IBPNode, Map<IBPNode, Boolean>> getCpuReachMap() {
        return cpuReachMap;
    }

    public Map<IBPNode, Map<IBPNode, Set<IBPNode>>> getLcpSysMap() {
        return lcpSysMap;
    }

    public Map<IBPNode, Map<IBPNode, Set<IBPNode>>> getLcsSysMap() {
        return lcsSysMap;
    }

    public Map<IBPNode, Map<IBPNode, Boolean>> getForwardSysSkip() {
        return forwardSysSkip;
    }

    public Map<IBPNode, Map<IBPNode, Boolean>> getBackwardSysSkip() {
        return backwardSysSkip;
    }

    public Map<IBPNode, Map<IBPNode, Set<IBPNode>>> getLcpCpuMap() {
        return lcpCpuMap;
    }

    public Map<IBPNode, Map<IBPNode, Set<IBPNode>>> getLcsCpuMap() {
        return lcsCpuMap;
    }

    public Map<IBPNode, Map<IBPNode, Boolean>> getForwardCpuSkip() {
        return forwardCpuSkip;
    }

    public Map<IBPNode, Map<IBPNode, Boolean>> getBackwardCpuSkip() {
        return backwardCpuSkip;
    }

}

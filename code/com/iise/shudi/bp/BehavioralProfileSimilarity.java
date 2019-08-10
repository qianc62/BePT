package com.iise.shudi.bp;

import org.jbpt.alignment.Alignment;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.construct.BPCreatorUnfolding;
import org.jbpt.bp.sim.AggregatedSimilarity;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;

public class BehavioralProfileSimilarity {

    public float similarity(NetSystem net1, NetSystem net2) {
        final NetSystem net1copy = (NetSystem) net1.clone();
        final NetSystem net2copy = (NetSystem) net2.clone();
        net1copy.getSourcePlaces().stream().forEach(p -> net1copy.getMarking().put(p, 1));
        net2copy.getSourcePlaces().stream().forEach(p -> net2copy.getMarking().put(p, 1));
        BehaviouralProfile<NetSystem, Node> bp1 = BPCreatorUnfolding.getInstance().deriveRelationSet(net1copy);
        BehaviouralProfile<NetSystem, Node> bp2 = BPCreatorUnfolding.getInstance().deriveRelationSet(net2copy);
        Alignment<BehaviouralProfile<NetSystem, Node>, Node> alignment = new Alignment<>(bp1, bp2);
        net1copy.getTransitions().forEach(t1 -> net2copy.getTransitions().stream()
            .filter(t2 -> t2.getLabel().equals(t1.getLabel()))
            .forEach(t2 -> alignment.addElementaryCorrespondence(t1, t2)));
        AggregatedSimilarity<BehaviouralProfile<NetSystem, Node>, NetSystem, Node>
                agg = new AggregatedSimilarity<>();
        return (float) agg.score(alignment);
    }

}

package qc;

import de.hpi.bpt.graph.algo.rpst.RPSTNode;

import java.util.Comparator;
import java.util.HashSet;
import qc.common.Common;

class AdequateOrder_SPF implements Comparator {
    public int compare( Object o1, Object o2 ){
        QcPetriNet petri1 = (QcPetriNet)o1;
        QcPetriNet petri2 = (QcPetriNet)o2;

        QcNode sources1_ = petri1.getOnlySource();
        QcNode sources2_ = petri2.getOnlySource();
        QcNode sink1_ = petri1.getOnlySink();
        QcNode sink2_ = petri2.getOnlySink();

        if ( sources1_==null || sources2_==null || sink1_==null || sink2_==null ) {
            return petri1.nodes.size() - petri2.nodes.size();
        }

        int sourceValue1 = dyeTypeValue( sources1_ );
        int sourceValue2 = dyeTypeValue( sources2_ );
        int sinkValue1   = dyeTypeValue( sink1_ );
        int sinkValue2   = dyeTypeValue( sink2_ );

        if ( sourceValue1 != sourceValue2 ) {
            return sourceValue1 - sourceValue2;
        }
        if ( sinkValue1 != sinkValue2 ) {
            return sinkValue2 - sinkValue1;
        }

        return petri1.nodes.size() - petri2.nodes.size();
    }

    public int dyeTypeValue( QcNode node ){
        switch( node.dyeType ){
            case Common.StartNode: return 0;
            case Common.EndNode: return 1;
            case Common.ShadowNode: return 2;
            case Common.NormalNode: return 3;
        }
        return -1;
    }
}

class AdequateOrder_LPF implements Comparator {
    public int compare( Object o1, Object o2 ){
        QcPetriNet petri1 = (QcPetriNet)o1;
        QcPetriNet petri2 = (QcPetriNet)o2;

        return petri2.nodes.size() - petri1.nodes.size();
    }
}

//class BondPathNodeNum implements Comparator {
//    public int compare( Object o1, Object o2 ){
//        RPSTNode node1 = (RPSTNode)o1;
//        RPSTNode node2 = (RPSTNode)o2;
//        return node1.get - net2.nodes.size();
//    }
//}

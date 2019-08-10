package org.jbpt.petri;

import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.Collection;
import qc.common.Common;


/**
 * Implementation of a net system.
 * <p>
 * TODO lift to interfaces
 *
 * @author Artem Polyvyanyy
 */
public class NetSystem extends AbstractNetSystem<Flow, Node, Place, Transition, Marking> {

    public NetSystem() {
        super();

        try {
            this.marking = Marking.class.newInstance();
            this.marking.setPetriNet(this);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public NetSystem(PetriNet petriNet) {
        this();
        for (Node n : petriNet.getNodes())
            this.addNode(n);
        for (Flow f : petriNet.getFlow())
            this.addFlow(f.getSource(), f.getTarget());
    }

    @Override
    public Flow addFlow(Node from, Node to) {
        if (from == null || to == null) return null;

        if ((from instanceof Place && to instanceof Transition) ||
                from instanceof Transition && to instanceof Place) {

            Collection<Node> ss = new ArrayList<Node>();
            ss.add(from);
            Collection<Node> ts = new ArrayList<Node>();
            ts.add(to);

            if (!this.checkEdge(ss, ts)) return null;

            return new Flow(this, from, to);
        }

        return null;
    }

    //By QC
    public void print(){

        Common.printLine('>');
        for( Node node: this.getNodes() ){
            if ( node.getClass().toString().contains("Place") ) {
                System.out.println( "Place: " + node.getId() );
            }
            if ( node.getClass().toString().contains("Transition") ) {
                System.out.println( "Transition: " + node.getId() );
            }
        }

        for ( Flow edge: this.getEdges() ) {
            System.out.println( edge.getSource().getId() + " -> " + edge.getTarget().getId() );
        }
        Common.printLine('<');


//        System.out.println( this.getEdges().size() );
//        System.out.println( this.getMarking() );
//        System.out.println( this.getNodes().size() );
    }

    //By QC
    public void setInitMarking(){
        for ( Node node: this.getSourceNodes() ) {
            System.out.println( this.getMarking() );
            System.out.println( node.getId() );

            System.out.println( this.getMarking() );
        }
    }
}

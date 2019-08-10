package qc;

import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Task;
import org.processmining.framework.models.petrinet.*;
import de.hpi.bpt.process.Process;
import qc.common.Common;

import java.util.HashMap;

public class QcFormatConverter {

    public Process transformPetriNetToProcess( PetriNet petri ){

        Process p = new Process();
        HashMap<Integer, Node> elementMap = new HashMap<>();
        HashMap<Integer, Boolean> numMap = new HashMap<>();

        for (PNNode node1: petri.getNodes() ) {
            Integer id = Integer.valueOf( node1.getId() );
            if ( numMap.get( id ) == null ) {
                numMap.put( id, true );
            } else {
                Common.printError( "public Process transformToPetriNet( PetriNet petri )" );
                return null;
            }
        }

        for ( Place place: petri.getPlaces() ){
            Task task = new Task( place.getIdentifier() );
            task.setId( Integer.toString( place.getId() ) );
            elementMap.put( Integer.valueOf( task.getId() ), task );
        }

        for ( Transition transition: petri.getTransitions() ){
            Task task = new Task( transition.getIdentifier() );
            task.setId( Integer.toString( transition.getId() ) );
            elementMap.put( Integer.valueOf( transition.getId() ), task );
        }

        for ( Object e : petri.getEdges() ){
            PNEdge edge = (PNEdge)e;
            Node source = elementMap.get( Integer.valueOf( edge.getSource().getId() ) );
            Node target = elementMap.get( Integer.valueOf( edge.getDest().getId() ) );
            p.addControlFlow( source, target );
        }

        return p;
    }
}

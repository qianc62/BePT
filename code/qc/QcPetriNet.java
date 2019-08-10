package qc;

import com.iise.util.PetriNetConversion;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import de.hpi.bpt.process.Task;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.unfolding.ProperCompletePrefixUnfolding;
import org.jbpt.petri.unfolding.QCUnfoldingTest;
import org.jfree.chart.HashUtilities;
import org.processmining.exporting.DotPngExport;
import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.plugin.ProvidedObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import qc.common.Common;
import textPlanning.PlanningHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class QcPetriNet {

    public HashSet<QcNode> nodes = null;
    public HashSet<QcEdge> edges = null;

    public HashSet<QcNode> addedNodes = null;
    public HashSet<QcNode> deletedNodes = null;

    //Tested
    public void init(){
        nodes = new HashSet<>();
        edges = new HashSet<>();

        addedNodes = new HashSet<>();
        deletedNodes = new HashSet<>();
    }

    //Tested
    public QcPetriNet() {
        init();
    }

    //Tested
    public QcPetriNet( String path ) throws Exception {

        init();

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse( new File( path ) );
            Element rootElement = document.getDocumentElement();
            dfsParse( rootElement, 0 );
        } catch ( Exception e ) {
            throw e;
        }
    }

    //Tested
    private void dfsParse( Element rootElement, int depth ){

//        System.out.println( Common.getTab(depth)+rootElement.getTagName() );

        if ( rootElement.getTagName().equals("place") ) {
            String id = rootElement.getAttributeNode("id").getValue();
            String name = dfsGetName( rootElement );
            QcNode qcNode = new QcNode(id, name, Common.Place, Common.NormalNode);
            this.addQcNode(qcNode);
            return ;
        } else if( rootElement.getTagName().equals("transition") ) {
            String id = rootElement.getAttributeNode("id").getValue();
            String name = dfsGetName( rootElement );
            QcNode qcNode = new QcNode(id, name, Common.Transition, Common.NormalNode);
            this.addQcNode(qcNode);
            return ;
        } else if( rootElement.getTagName().equals("arc") ) {
            String id = rootElement.getAttributeNode("id").getValue();
            String sourceId = rootElement.getAttributeNode("source").getValue();
            String targetId = rootElement.getAttributeNode("target").getValue();
            QcNode source = this.getQcNodeById( sourceId );
            QcNode target = this.getQcNodeById( targetId );
            this.addQcEdge( source, target );
            return ;
        }

        NodeList nodes = rootElement.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item( i );
            if ( node.getNodeType() == Element.ELEMENT_NODE ) {
                Element child = (Element)node;
                dfsParse( child, depth+1 );
            }
        }
    }

    //Tested
    private String dfsGetName( Element rootElement ){

        if ( rootElement.getTagName().equals("text") ) {
            return rootElement.getTextContent();
        }

        NodeList nodes = rootElement.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item( i );
            if ( node.getNodeType() == Element.ELEMENT_NODE ) {
                Element child = (Element)node;
                String str = dfsGetName( child );
                if ( str != "" ) {
                    return str;
                }
            }
        }
        return "";
    }

    public QcPetriNet workflowFy(){
        QcPetriNet pi = new QcPetriNet();
        pi.addSubPetri( this );

        if ( pi.getSources().size()==1 ) {
            QcNode onlySource = pi.getOnlySource();
            if ( onlySource.shapeType == Common.Transition ) {
                QcNode superSource = new QcNode( "", "superSource"+pi.nodes.size(), Common.Place, Common.NormalNode );
                pi.addQcNode( superSource );
                pi.addedNodes.add( superSource );
                pi.addQcEdge( superSource, onlySource );
            }
        } else if ( pi.getSources().size()>1 ) {
            for ( QcNode source: pi.getSources() ) {
                if ( source.shapeType == Common.Place ) {
                    pi.removeQcNode( source );
                    pi.deletedNodes.add( source );
                }
            }
            QcNode superSource = new QcNode( "", "superSource"+pi.nodes.size(), Common.Place, Common.NormalNode );
            pi.addedNodes.add( superSource );
            for ( QcNode source: pi.getSources() ) {
                pi.addQcEdge( superSource, source );
            }
        }

        if ( pi.getSinks().size()==1 ) {
            QcNode onlySink = pi.getOnlySink();
            if ( onlySink.shapeType == Common.Transition ) {
                QcNode superSink = new QcNode( "", "superSource"+pi.nodes.size(), Common.Place, Common.NormalNode );
                pi.addQcNode( superSink );
                pi.addedNodes.add( superSink );
                pi.addQcEdge( onlySink, superSink );
            }
        } else if ( pi.getSinks().size()>1 ) {
            for ( QcNode sink: pi.getSinks() ) {
                if ( sink.shapeType == Common.Place ) {
                    pi.removeQcNode( sink );
                    pi.deletedNodes.add( sink );
                }
            }
            QcNode superSink = new QcNode( "", "superSource"+pi.nodes.size(), Common.Place, Common.NormalNode );
            pi.addedNodes.add( superSink );
            for ( QcNode sink: pi.getSinks() ) {
                pi.addQcEdge( sink, superSink );
            }
        }

        return pi;
    }

    //Tested
    public QcNode addQcNode( QcNode node ){

        String cleanName = Common.getCleanName(node.id);

        if ( node.id != "" ) {
            for ( QcNode node_: nodes ) {
                if ( node_.id.equals( cleanName ) ) {
                    return node_;
                }
            }
        } else {
            node.id = "N" + nodes.size();
        }

        node.id = Common.getCleanName( node.id );
        node.name = Common.getCleanName( node.name );
        if ( node.name.length()==0 ) {
            node.name = node.id;
        }

        nodes.add( node );
//        nodeMap.put( node.id, node );
        return node;
    }

    //Tested
    public QcEdge addQcEdge( QcNode node1, QcNode node2 ){
        QcNode node1_ = addQcNode( node1 );
        QcNode node2_ = addQcNode( node2 );

        for ( QcEdge edge_: edges ) {
            if ( edge_.id.equals( node1.id+"_to_"+node2.id ) ) {
                return edge_;
            }
        }

        QcEdge edge = new QcEdge( node1_, node2_ );
        edges.add( edge );
        return edge;
    }

    public boolean removeQcNode( QcNode node ){

        if ( this.nodes.contains( node ) == true ) {
            HashSet<QcEdge> lastEdges = this.getLastEdges( node );
            HashSet<QcEdge> nextEdges = this.getNextEdges( node );

            for ( QcEdge edge_: lastEdges ) {
                this.edges.remove( edge_ );
            }

            for ( QcEdge edge_: nextEdges ) {
                this.edges.remove( edge_ );
            }

            this.nodes.remove( node );

            return true;
        }

        return false;
    }

    //Tested
    public void print(){

        Common.printLine( '>', "QcPetriNet" );

        System.out.format( "Nodes(%d): ", nodes.size() );
        for ( QcNode node_: nodes ) {
            System.out.format( "%s(%s)  ", node_.name, node_.id );
        }
        System.out.println();

        System.out.format( "Edges(%d): ", edges.size() );
        for ( QcEdge edge_: edges ) {
            System.out.format( "%s(%s)  ", edge_.name, edge_.id );
        }
        System.out.println();

        HashSet<QcNode> sources = getSources();
        System.out.print( "Source: " );
        if ( sources!=null ) {
            for ( QcNode node_: sources ) {
                System.out.print( node_.name + "  " );
            }
        }
        System.out.println();

        HashSet<QcNode> sinks = getSinks();
        System.out.print( "Sink: " );
        if ( sinks!=null ) {
            for (QcNode node_ : sinks) {
                System.out.print(node_.name + "  ");
            }
        }
        System.out.println();

        if ( isStructured()==true ) {
            System.out.println( "Structured." );
        } else {
            System.out.println( "Unstructured." );
        }

        Common.printLine( '<', "QcPetriNet" );
    }

    //Tested
    public void saveImage( String path ) throws Exception {
        PetriNet net = transformToPetriNet();
        ProvidedObject po1 = new ProvidedObject( "petrinet", net );
        DotPngExport dpe1 = new DotPngExport();
        OutputStream image1 = new FileOutputStream( path );
        dpe1.export( po1, image1 );
    }

    //Tested
    public HashSet<QcNode> getPlaces() {
        HashSet<QcNode> places = new HashSet<>();

        for ( QcNode node_ : nodes ) {
            if ( node_.shapeType.equals(Common.Place) ) {
                places.add( node_ );
            }
        }

        return places;
    }

    //Tested
    public HashSet<QcNode> getTransitions() {
        HashSet<QcNode> transitions = new HashSet<>();

        for ( QcNode node_ : nodes ) {
            if ( node_.shapeType.equals(Common.Transition) ) {
                transitions.add( node_ );
            }
        }

        return transitions;
    }

    //Tested
    public HashSet<QcNode> getSources() {
        HashSet<QcNode> sourceNodes = new HashSet<>();

        HashMap<QcNode,Integer> inDegreeMap = new HashMap<>();

        for ( QcNode node_ : nodes ) {
            inDegreeMap.put( node_, 0 );
        }

        for ( QcEdge edge_: edges ) {
            QcNode targetNode = edge_.target;
            inDegreeMap.replace( targetNode, inDegreeMap.get(targetNode)+1 );
        }

        for ( QcNode node_ : nodes ) {
            if ( inDegreeMap.get( node_ ) == 0 ) {
                sourceNodes.add( node_ );
            }
        }

        return sourceNodes;
    }

    //Tested
    public HashSet<QcNode> getSinks() {
        HashSet<QcNode> sinkNodes = new HashSet<>();

        HashMap<QcNode,Integer> outDegreeMap = new HashMap<>();

        for ( QcNode node_ : nodes ) {
            outDegreeMap.put( node_, 0 );
        }

        for ( QcEdge edge_: edges ) {
            QcNode sourceNode = edge_.source;
            outDegreeMap.replace( sourceNode, outDegreeMap.get(sourceNode)+1 );
        }

        for ( QcNode node_ : nodes ) {
            if ( outDegreeMap.get( node_ ) == 0 ) {
                sinkNodes.add( node_ );
            }
        }

        return sinkNodes;
    }

    public QcNode getOnlySource() {
        HashSet<QcNode> sourceNodes = getSources();

        if ( sourceNodes.size() == 1 ) {
            for ( QcNode node_: sourceNodes ) {
                return node_;
            }
        }

        return null;
    }

    public QcNode getOnlySink() {
        HashSet<QcNode> sinkNodes = getSinks();

        if ( sinkNodes.size() == 1 ) {
            for ( QcNode node_: sinkNodes ) {
                return node_;
            }
        }

        return null;
    }

    public HashSet<QcNode> getLastNodes( QcNode node ){
        HashSet<QcNode> lastNodes = new HashSet<>();

        for ( QcEdge edge_: edges ) {
            QcNode source = edge_.source;
            QcNode target = edge_.target;
            if ( target.id.equals( node.id ) ) {
                lastNodes.add( source );
            }
        }
        return lastNodes;
    }

    public HashSet<QcEdge> getLastEdges( QcNode node ){
        HashSet<QcEdge> lastEdges = new HashSet<>();

        for ( QcEdge edge_: edges ) {
            QcNode target = edge_.target;
            if ( target.id.equals( node.id ) ) {
                lastEdges.add( edge_ );
            }
        }
        return lastEdges;
    }

    public QcNode getLastOnlyNode( QcNode node ){

        HashSet<QcNode> lastNodes = getLastNodes(node);

        if( lastNodes.size() == 1 ){
            for ( QcNode node_: lastNodes ) {
                return node_;
            }
        }

        return null;
    }

    public HashSet<QcNode> getNextNodes( QcNode node ){

        HashSet<QcNode> nextNodes = new HashSet<>();

        for ( QcEdge edge_: edges ) {
            QcNode source = edge_.source;
            QcNode target = edge_.target;
            if ( source.id.equals( node.id ) ) {
                nextNodes.add( target );
            }
        }
        return nextNodes;
    }

    public HashSet<QcNode> getNextTransitions( QcNode transition ){

        if ( transition.shapeType==Common.Place ) {
            return null;
        }

        HashSet<QcNode> nextTransitions = new HashSet<>();

        for ( QcNode node: this.getNextNodes( transition ) ) {
            for ( QcNode node_: this.getNextNodes( node ) ) {
                if ( node_.shapeType==Common.Transition ) {
                    nextTransitions.add( node_ );
                }
            }
        }

        return nextTransitions;
    }

    public HashSet<QcEdge> getNextEdges( QcNode node ){
        HashSet<QcEdge> nextEdges = new HashSet<>();

        for ( QcEdge edge_: edges ) {
            QcNode source = edge_.source;
            if ( source.id.equals( node.id ) ) {
                nextEdges.add( edge_ );
            }
        }
        return nextEdges;
    }

    public QcNode getNextOnlyNode( QcNode node ){

        HashSet<QcNode> nextNodes = getNextNodes(node);

        if ( nextNodes.size()==1 ) {
            for ( QcNode node_: nextNodes ) {
                return node_;
            }
        }

        return null;
    }

    public QcNode getQcNodeById( String str ){
        for ( QcNode node_: this.nodes ) {
            if( node_.id.equals( str ) ){
                return node_;
            }
        }
        return null;
    }

    public QcNode getOnlyQcNodeByname( String str ){
        HashSet<QcNode> nodeList = new HashSet<>();

        for ( QcNode node_: this.nodes ) {
            if( node_.name.equals( str ) ){
                nodeList.add( node_ );
            }
        }

        if ( nodeList.size()==1 ) {
            for ( QcNode node_: nodeList ) {
                return node_;
            }
        }

        return null;
    }

    public boolean containQcNodeByName( String str ){

        for ( QcNode node_: this.nodes ) {
            if( node_.name.equals( str ) ){
                return true;
            }
        }

        return false;
    }

    public QcEdge getQcEdgeById( String str ){
        for ( QcEdge edge_: this.edges ) {
            if( edge_.id.equals( str ) ){
                return edge_;
            }
        }
        return null;
    }

    public QcEdge getOnlyQcEdgeByname( String str ){
        HashSet<QcEdge> edgeList = new HashSet<>();

        for ( QcEdge edge_: this.edges ) {
            if( edge_.name.equals( str ) ){
                edgeList.add( edge_ );
            }
        }

        if ( edgeList.size()==1 ) {
            for ( QcEdge edge_: edgeList ) {
                return edge_;
            }
        }

        return null;
    }

    public void transformFromPetriNet( PetriNet petri, QcPetriNet oriPetri ){

        for ( PNEdge edge: petri.getPNEdges() ) {
            String sourceId = edge.getSource().getIdentifier();
            String targetId = edge.getDest().getIdentifier();
            if ( petri.findPlace(sourceId) != null ) {
                QcNode source = new QcNode( sourceId, oriPetri.getQcNodeById(Common.getPrefix(sourceId)).name, Common.Place, Common.NormalNode );
                QcNode target = new QcNode( targetId, oriPetri.getQcNodeById(Common.getPrefix(targetId)).name, Common.Transition, Common.NormalNode );
                this.addQcEdge( source, target );
            } else {
                QcNode source = new QcNode( sourceId, oriPetri.getQcNodeById(Common.getPrefix(sourceId)).name, Common.Transition, Common.NormalNode );
                QcNode target = new QcNode( targetId, oriPetri.getQcNodeById(Common.getPrefix(targetId)).name, Common.Place, Common.NormalNode );
                this.addQcEdge( source, target );
            }
        }
    }

    //Tested
    public PetriNet transformToPetriNet(){

        PetriNet petri_ = new PetriNet();

        HashMap<String,Place> pMap = new HashMap<>();
        HashMap<String,Transition> tMap = new HashMap<>();

        for ( QcEdge edge_: this.edges ) {
            QcNode source = edge_.source;
            QcNode target = edge_.target;

            if ( source.shapeType.equals(Common.Place) ) {
                Place place_;
                if ( pMap.containsKey( source.id ) ) {
                    place_ = pMap.get( source.id );
                } else {
                    place_ = new Place( source.id, petri_ );
                }
                Transition transition_;
                if ( tMap.containsKey( target.id ) ) {
                    transition_ = tMap.get( target.id );
                } else {
                    transition_ = new Transition( target.id, petri_ );
                }
                pMap.put( source.id, place_ );
                tMap.put( target.id, transition_ );
                petri_.addPlace( place_ );
                petri_.addTransition( transition_ );
                petri_.addEdge( place_, transition_ );
            } else if (source.shapeType == Common.Transition) {
                Transition transition_;
                if ( tMap.containsKey( source.id ) ) {
                    transition_ = tMap.get( source.id );
                } else {
                    transition_ = new Transition( source.id, petri_ );
                }
                Place place_;
                if ( pMap.containsKey( target.id ) ) {
                    place_ = pMap.get( target.id );
                } else {
                    place_ = new Place( target.id, petri_ );
                }
                tMap.put( source.id, transition_ );
                pMap.put( target.id, place_ );
                petri_.addTransition( transition_ );
                petri_.addPlace( place_ );
                petri_.addEdge( transition_, place_ );
            }
        }

        return petri_;
    }

    public Process transformToProcess(){
        Process process = new Process();

        HashMap<String,Task> taskMap = new HashMap<>();

        for ( QcEdge edge: this.edges ) {
            QcNode source = edge.source;
            QcNode target = edge.target;

            String label1 = source.id;
            String label2 = target.id;

            Task task1_ = taskMap.get( label1 );
            Task task2_ = taskMap.get( label2 );

            Task task1 = task1_;
            Task task2 = task2_;

            if ( task1 == null ) {
                task1 = new Task( label1 );
                task1.setId( label1 );
                process.addTask( task1 );
                taskMap.put( label1, task1 );
            }

            if ( task2 == null ) {
                task2 = new Task( label2 );
                task2.setId( label2 );
                process.addTask( task2 );
                taskMap.put( label2, task2 );
            }

            process.addControlFlow( task1, task2 );
        }

        return process;
    }

    public QcPetriNet getCPU(){
        PetriNet net = this.transformToPetriNet();

        NetSystem ns = PetriNetConversion.convert( net );

        ProperCompletePrefixUnfolding cpu_ = new ProperCompletePrefixUnfolding( ns );

        PetriNet cpu = PetriNetConversion.convert( cpu_ );

        QcPetriNet petri = new QcPetriNet();
        petri.transformFromPetriNet( cpu, this );

        return petri;
    }

    //Tested
    public RPST getRPST(){

        Process process = this.transformToProcess();

        return new RPST( process );
    }

    public void addSubPetri( QcPetriNet petri_ ){

        for ( QcNode node_: petri_.nodes ) {
            addQcNode( node_ );
        }

        for ( QcEdge edge_: petri_.edges ) {
            QcNode source = edge_.source;
            QcNode target = edge_.target;
            addQcEdge( source, target );
        }
    }

    public void addSimpleSubPetri( QcPetriNet petri_ ){

        for ( QcEdge edge_: petri_.edges ) {
            QcNode source_ = edge_.source;
            QcNode target_ = edge_.target;

            QcNode source = this.getOnlyQcNodeByname( source_.name );
            QcNode target = this.getOnlyQcNodeByname( target_.name );

            if ( source == null ) {
                source = new QcNode( source_.id, Common.getPrefix(source_.name), source_.shapeType, source_.dyeType );
            }
            if ( target == null ) {
                target = new QcNode( target_.id, Common.getPrefix(target_.name), target_.shapeType, target_.dyeType );
            }

            addQcEdge( source, target );
        }
    }

    public void addSubPetri( QcPetriNet petri, ArrayList<RPSTNode> rpstNodes ) {
        for ( RPSTNode rpstNode : rpstNodes ) {
            String sourceLabel = rpstNode.getEntry().getName();
            String targetLabel = rpstNode.getExit().getName();

            QcNode source = petri.getQcNodeById( sourceLabel );
            QcNode target = petri.getQcNodeById( targetLabel );

            this.addQcEdge( source, target );
        }
    }

    public void dyeNodes( QcNode sourceNode, QcNode sinkNode ){

        for ( QcNode node_ : this.nodes ) {
            if ( node_.isSameName(sourceNode) ) {
                node_.dyeType = Common.StartNode;
            }
        }

        for ( QcNode node_ : this.nodes ) {
            if ( node_.isSameName(sinkNode) ) {
                node_.dyeType = Common.EndNode;
            }
        }

        for ( QcNode node : getSinks() ) {
            if ( node.dyeType == Common.NormalNode ) {
                for ( QcNode node_: nodes ) {
                    if ( Common.isSamePrefix(node_.name,node.name) && node_.dyeType==Common.NormalNode ) {
                        node_.dyeType = Common.ShadowNode;
                    }
                }
            }
        }
    }

    public QcPetriNet getSubPetri( ArrayList<RPSTNode> rpstNodes ){

        QcPetriNet petri_ = new QcPetriNet();

        for ( RPSTNode rpstNode : rpstNodes ) {
            QcNode source_ = this.getQcNodeById( rpstNode.getEntry().getName() );
            QcNode target_ = this.getQcNodeById( rpstNode.getExit().getName() );

            QcNode source = petri_.getOnlyQcNodeByname( source_.name );
            QcNode target = petri_.getOnlyQcNodeByname( target_.name );

            if ( source == null ) {
                source = new QcNode( source_.id, Common.getPrefix(source_.name), source_.shapeType, source_.dyeType );
            }
            if ( target == null ) {
                target = new QcNode( target_.id, Common.getPrefix(target_.name), target_.shapeType, target_.dyeType );
            }

            petri_.addQcEdge( source, target );
        }

        return petri_;
    }

//    public QcPetriNet getTopPetri( HashMap<QcNode,QcPetriNet> node_petri_map ){
//
////        this.print();
//        RPST rpst = this.getRPST();
////        rpst.print(rpst.getRoot(),0);
//
//        HashMap<QcNode,QcNode> findQcNodeMap = new HashMap<>();
//        dfsGetTopPetri_find( rpst.getRoot(), rpst, 0, findQcNodeMap );
//
//        for ( QcNode node_:nodes ) {
//            if ( findQcNodeMap.get(node_) == null ) {
//                findQcNodeMap.put( node_, node_ );
//            }
//        }
//
//        QcPetriNet petri_ = new QcPetriNet();
//        dfsGetTopPetri_create( rpst.getRoot(), rpst, 0, findQcNodeMap, node_petri_map, petri_ );
//
//        return petri_;
//    }
//
//    //Tested
//    private void dfsGetTopPetri_find( RPSTNode root, RPST rpst, int depth, HashMap<QcNode,QcNode> findQcNodeMap ) {
//
//        char type = root.getName().charAt(0);
//        if ( (type == 'B' || type == 'R') && depth>0 ) {
//            QcNode superNode = nodeMap.get( root.getEntry().getName() );
//            ArrayList<RPSTNode> rpstNodes = rpst.getLeaves(root);
//            for ( RPSTNode rpstNode: rpstNodes ) {
//                QcNode source = nodeMap.get( rpstNode.getEntry().getName() );
//                QcNode target = nodeMap.get( rpstNode.getExit().getName() );
//                findQcNodeMap.put( source, superNode );
//                findQcNodeMap.put( target, superNode );
//            }
//        } else {
////            ArrayList<RPSTNode<ControlFlow, de.hpi.bpt.process.Node>> orderedTopNodes = PlanningHelper.sortTreeLevel( (RPSTNode<ControlFlow, de.hpi.bpt.process.Node>)root, (de.hpi.bpt.process.Node)root.getEntry(), (RPST<ControlFlow, de.hpi.bpt.process.Node>)rpst );
//            for ( Object obj: rpst.getChildren(root) ){
//                RPSTNode child = (RPSTNode) obj;
//                dfsGetTopPetri_find( child, rpst, depth+1, findQcNodeMap );
//            }
//        }
//    }
//
//    //Tested
//    private void dfsGetTopPetri_create( RPSTNode root, RPST rpst, int depth, HashMap<QcNode,QcNode> findQcNodeMap, HashMap<QcNode,QcPetriNet> node_petri_map, QcPetriNet pi ) {
//
//        char type = root.getName().charAt(0);
//
//        if ( type == 'T' ) {
//            QcNode source_ = nodeMap.get( root.getEntry().getName() );
//            QcNode target_ = nodeMap.get( root.getExit().getName() );
//            QcNode source = findQcNodeMap.get( source_ );
//            QcNode target = findQcNodeMap.get( target_ );
//            pi.addEdgeWithTwoQcNodes( source, target );
//        } else if ( (type == 'B' || type == 'R') && depth>0 ) {
//            return ;
//        } else {
//            for ( Object obj: rpst.getChildren(root) ){
//                RPSTNode child = (RPSTNode) obj;
//                dfsGetTopPetri_create( child, rpst, depth+1, findQcNodeMap, node_petri_map, pi );
//            }
//        }
//    }

    public QcNode findEnabledQcNode( HashMap<QcEdge,Integer> edgeEnabledFlagMap ){
        for ( QcNode node_: nodes ) {
            if ( node_.shapeType == Common.Place ) {
                continue;
            }

            HashSet<QcEdge> lastEdges = this.getLastEdges(node_);
            int lastEnabledNum = getEnabledNumber( lastEdges, edgeEnabledFlagMap );

            HashSet<QcEdge> nextEdges = this.getNextEdges(node_);
            int nextEnabledNum = getEnabledNumber( nextEdges, edgeEnabledFlagMap );

            if ( lastEnabledNum == lastEdges.size() && nextEnabledNum < nextEdges.size() ) {
                return node_;
            }
        }

        return null;
    }

    private int getEnabledNumber( HashSet<QcEdge> edges_, HashMap<QcEdge,Integer> edgeEnabledFlagMap ){
        int counter = 0;

        for ( QcEdge edge_: edges_ ) {
            if ( edgeEnabledFlagMap.get(edge_) != 0 ) {
                counter++;
            }
        }

        return counter;
    }

    public QcPetriNet getSubPathFromTransitionToTransition( QcNode lastNode_, QcNode node_ ){
        QcPetriNet petri = new QcPetriNet();

        QcNode nowNode = node_;
        while( true ){
            HashSet<QcEdge> lastEdges = this.getLastEdges( nowNode );
            HashSet<QcEdge> nextEdges = this.getNextEdges( nowNode );
            if ( lastEdges.size()==1 && nextEdges.size()==1 ) {
                for ( QcEdge nextEdge: nextEdges ) {
                    petri.addQcEdge( nowNode, nextEdge.target );
                    nowNode = nextEdge.target;
                }
            } else {
                break;
            }
        }

        QcNode place_s = new QcNode( "", "P_tmp_start" , Common.Place, Common.NormalNode );
        QcNode place_e = new QcNode( "", "P_tmp_end" , Common.Place, Common.NormalNode );
        petri.addQcEdge( place_s, lastNode_ );
        petri.addQcEdge( lastNode_, node_ );
        petri.addQcEdge( nowNode, place_e );

        return petri;
    }

    public QcPetriNet getSubPetriFromQcNodeToINF( QcNode node ){
        QcPetriNet pi = new QcPetriNet();

        dfsGetSubPetriFromQcNodeToINF( null,  node, pi );

        return pi;
    }

    public QcPetriNet getSubPetriFromSimpliedLabel( HashSet<String> simLabels ){
        QcPetriNet pi = new QcPetriNet();
        for ( QcEdge edge_: this.edges ) {
            QcNode source = edge_.source;
            QcNode target = edge_.target;

            if ( simLabels.contains(source.name) && simLabels.contains(target.name) ) {
                pi.addQcEdge( source, target );
            }
        }
        return pi;
    }

    private void dfsGetSubPetriFromQcNodeToINF(QcNode lastNode, QcNode node, QcPetriNet pi ){
        if ( lastNode == null ) {
            pi.addQcNode( node );
        } else {
            pi.addQcEdge( lastNode, node );
        }

        HashSet<QcNode> nextNodes = this.getNextNodes( node );
        for ( QcNode nextNode: nextNodes ) {
            dfsGetSubPetriFromQcNodeToINF( node, nextNode, pi );
        }
    }

    public ArrayList<QcPetriNet> getSubPetriFromQcNodeToQcNode( QcNode node1, QcNode node2 ){
        ArrayList<QcPetriNet> paths = new ArrayList<>();

        QcPetriNet pi = new QcPetriNet();
        pi.addQcNode( node1 );

        dfsGetSubPetriFromQcNodeToQcNode( pi, node2, 0, paths );

        return paths;
    }

    private void dfsGetSubPetriFromQcNodeToQcNode( QcPetriNet pi, QcNode node2, int depth, ArrayList<QcPetriNet> paths ){
        QcNode sinkNode = pi.getOnlySink();

        if ( depth>0 && sinkNode.isSamePrefixName(node2) ) {
            paths.add( pi );
            return ;
        }

        HashSet<QcNode> nextNodes = this.getNextNodes( sinkNode );
        for ( QcNode nextNode: nextNodes ) {

            QcPetriNet pj = new QcPetriNet();
            pj.addSubPetri( pi );
            pj.addQcEdge( sinkNode, nextNode );

            dfsGetSubPetriFromQcNodeToQcNode( pj, node2, depth+1, paths );
        }
    }

    public QcPetriNet getShortestSubPetriFromQcNodeToQcNode( QcNode node1, QcNode node2 ){
        ArrayList<QcPetriNet> paths = getSubPetriFromQcNodeToQcNode( node1, node2 );

        int min = Common.INF;
        QcPetriNet minPetri = null;

        for ( QcPetriNet petri_: paths ) {
            if ( petri_.nodes.size() < min ) {
                min = petri_.nodes.size();
                minPetri = petri_;
            }
        }

        return minPetri;
    }

    public QcPetriNet getShortestSubPetriFromQcNodeToQcNodeWithGoals( QcNode node1, QcNode node2, HashMap<String, ArrayList<String>> goals ){
        ArrayList<QcPetriNet> paths = getSubPetriFromQcNodeToQcNode( node1, node2 );

        int min = Common.INF;
        QcPetriNet minPetri = null;

        for ( QcPetriNet petri_: paths ) {
            int num = petri_.nodes.size();

            for ( QcNode node1_: petri_.nodes ) {
                for ( QcNode node2_: petri_.nodes ) {
                    if ( node1_!=node2_ && goals.get(node1_.id)!=null ) {
                        if ( goals.get(node1_.id).contains(node2_.id) ) {
                            num -= petri_.nodes.size();
                        }
                    }
                }
            }

            if ( num < min ) {
                min = num;
                minPetri = petri_;
            }
        }

        return minPetri;
    }

    public HashSet<String> getAllSimpliedLabel(){
        HashSet<String> simLabels = new HashSet<>();
        for ( QcNode node_: this.nodes ) {
            simLabels.add( node_.name );
        }
        return simLabels;
    }

    public boolean isLoop_Label(){

        //标签Loop
        for ( QcNode node1: this.getPlaces() ) {
            for ( QcNode node2: this.getPlaces() ) {
                if ( node1 == node2 ) {
                    continue;
                }
                if ( node1.isSameName(node2) ) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isLoop_Structure(){

        //结构Loop
        QcPetriNet pi = new QcPetriNet();
        pi.addSubPetri( this );

        while( pi.nodes.size() > 0 ){
            HashSet<QcNode> sources = pi.getSources();
            if ( sources.size()==0 ) {
                return true;
            }
            for ( QcNode node_: sources ) {
                pi.removeQcNode( node_ );
            }
        }

        return false;
    }

    public boolean isStructured(){
        RPST rpst = this.getRPST();

        if ( rpst.containR( rpst.getRoot() ) == true ) {
            return false;
        }

        return true;
    }

    public boolean isLoop(){
        HashMap<QcNode,Integer> visitedFlag = new HashMap<>();
        for ( QcNode node: this.nodes ) {
            visitedFlag.put( node, 0 );
        }

        QcNode source = this.getOnlySource();

        visitedFlag.replace( source, 1 );

        ArrayList<Integer> listFlag = new ArrayList<>();

        dfsIsLoop( source, visitedFlag, listFlag );

        if ( listFlag.size()>1 ) {
            return true;
        }

        return false;
    }

    public void dfsIsLoop( QcNode root, HashMap<QcNode,Integer> visitedFlag, ArrayList<Integer> listFlag ){

        if ( listFlag.size()>0 ) {
            return ;
        }

        HashSet<QcNode> nextNodes = this.getNextNodes( root );
        for ( QcNode node: nextNodes ) {
            if ( visitedFlag.get(node)==0 ) {
                visitedFlag.replace( node, 1 );
                dfsIsLoop( node, visitedFlag, listFlag );
                visitedFlag.replace( node, 0 );
            } else {
                listFlag.add( 1 );
            }
        }
    }

    //Tested
    public int getNumberOfVisibleTasks(){
        int num = 0;
        for ( QcNode node_: this.getTransitions() ) {
            if ( node_.name!="" ) {
                num++;
            }
        }
        return num;
    }

    //Tested
    public int getNumberOfInvisibleTasks(){
        return this.getTransitions().size() - getNumberOfVisibleTasks();
    }

    //Tested
    public int getNumberOfDuplicateTasks(){
        HashMap<String,Integer> counter = new HashMap<>();
        for ( QcNode node_: this.getTransitions() ) {
            if ( counter.get(node_.name)==null ) {
                counter.put( node_.name, 1 );
            } else {
                counter.put( node_.name, counter.get(node_.name)+1 );
            }
        }
        int num = 0;
        for ( String key: counter.keySet() ) {
            if ( counter.get(key)>1 ) {
                num += counter.get(key);
            }
        }
        return num;
    }

    //Tested
    public int getNumberOfNonDuplicateTasks(){
        return this.getTransitions().size() - getNumberOfDuplicateTasks();
    }

    public HashSet<QcNode> getNextTransitionGateway( QcNode node ){
        HashSet<QcNode> transitions = new HashSet<>();
        for ( QcNode node_: getNextNodes(node) ) {
            dfsGetNextTransitionGateway( node_, transitions );
        }
        return transitions;
    }

    public void dfsGetNextTransitionGateway( QcNode node, HashSet<QcNode> transitions ){
        if ( node.shapeType==Common.Transition ) {
            int inNum  = this.getLastNodes(node).size();
            int outNum = this.getNextNodes(node).size();
            if ( inNum>1 || outNum>1 ) {
                transitions.add( node );
                return ;
            }
        }

        for ( QcNode nextNode: getNextNodes(node) ) {
            dfsGetNextTransitionGateway( nextNode, transitions );
        }
    }
}

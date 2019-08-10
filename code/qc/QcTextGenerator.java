package qc;

import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import qc.common.Common;
import textPlanning.PlanningHelper;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

public class QcTextGenerator {

    public HashMap<String,String> templateMap = null;   //模板句型
    public ArrayList<QcSentence> sentences = null;  //文本系统的所有句子,用于生成QcTreeNode数据结构
    public Attribute attribute = null;  //文本生成过程中的各类属性,用于实验统计
    public boolean flag;    //该标志为算法是否能够应对该模型的flag

    //Tested
    private void init(){
        templateMap = new HashMap<>();
        sentences = new ArrayList<>();

        attribute = new Attribute();
        this.flag = true;
    }

    //Tested
    public QcTextGenerator( HashMap<String,String> templateMap_ ){
        init();
        templateMap = templateMap_;
    }

    //Tested
    public QcTextGenerator( String path ) throws Exception {
        init();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().parse( path );

        NodeList children = doc.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node root = children.item(i);
            dfsLanguageTemplates( root );
        }
    }

    //Tested
    private void dfsLanguageTemplates( Node root ){

        if ( root.getNodeName().contains( "_Template" ) ) {
            templateMap.put( root.getNodeName(), root.getTextContent() );
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node_ = children.item(i);
            dfsLanguageTemplates( node_ );
        }
    }

    private void addSentence( QcSentence sentence ){

        if ( sentences == null ) {
            sentences = new ArrayList<>();
        }

        sentences.add( sentence );
    }

    private void addSentences( ArrayList<QcSentence> sentences_ ){
        for ( QcSentence sentence: sentences_ ) {
            addSentence( sentence );
        }
    }

    //Tested
    private void addTemplateSentence( String key, int depth1 ){
        QcSentence sentence = new QcSentence( depth1, key, templateMap.get(key) );
        addSentence( sentence );
    }

    //Tested
    private void addTrivialSentence( QcNode node, int depth1 ) {
        QcSentence sentence = new QcSentence( depth1, "Trivial_Sentence", node.name );
        addSentence( sentence );
        this.attribute.depthMap.put( node.name, depth1 );
    }

    //Tested
    public Attribute generateText( QcPetriNet petri, int depth ) throws Exception {

        if ( petri.getOnlySource()==null ) {
            for ( QcNode node_: petri.nodes ) {
                if ( node_.dyeType.equals(Common.StartNode) ) {
                    QcNode transition = new QcNode( "", "", Common.Transition, Common.NormalNode );
                    petri.addQcEdge( transition, node_ );
                    break;
                }
            }
        }

        petri = petri.workflowFy();

        RPST rpst = petri.getRPST();
        QcPetriNet cpu = petri.getCPU();
        dfsGenerateNewText( petri, cpu, rpst, rpst.getRoot(), depth );

        if ( depth == 0 ) {
            attribute.petri = petri;
            attribute.rpst = rpst;

            //无法生成文本的情况
            if ( this.flag == false ) {
                attribute.startTime = System.currentTimeMillis();
                attribute.endTime = System.currentTimeMillis();
                attribute.string = "Generation Failed.";
                return this.attribute;
            }

            QcTreeNode root = QcTreeNode.createTree( sentences );

            attribute.rootNode = root;
            attribute.startTime = System.currentTimeMillis();
            String str = "";

            //写入补充的模型元素

            if ( petri.addedNodes.size()>0 || petri.deletedNodes.size()>0 ) {
                String preString = "";
                String addedNodesString = "";
                String deletedNodesString = "";

                for ( QcNode node: petri.addedNodes ) {
                    addedNodesString += node.name + " ";
                }
                for ( QcNode node: petri.deletedNodes ) {
                    deletedNodesString += node.name + " ";
                }
                preString += templateMap.get("Workflowfy_Begin_Template")+": \n";
                if ( petri.addedNodes.size()>0 ) {
                    preString += "[1]" + templateMap.get("Workflowfy_addNode_Template")+": "+addedNodesString+". \n";
                }
                if ( petri.deletedNodes.size()>0 ) {
                    preString += "[1]" + templateMap.get("Workflowfy_deleteNode_Template")+": "+deletedNodesString+". \n";
                }
                preString += templateMap.get("Workflowfy_End_Template") + ". \n\n";

                str += preString;
            }

            str += root.toText( 0, templateMap.get("Workflow_Begin_Template"), ". " + templateMap.get("Workflow_End_Template") + ". ", templateMap, attribute.depthMap );

            str = QcTreeNode.postProcessText( petri, str );
            attribute.string = str;
            attribute.endTime = System.currentTimeMillis();
            attribute.caculate();
        }

        return this.attribute;
    }

    //Tested
    public void dfsGenerateNewText( QcPetriNet petri, QcPetriNet cpu, RPST rpst, RPSTNode rpstNode, int depth ) throws Exception {

        if ( rpstNode==null || this.flag==false ) {
            return ;
        }

        if ( rpstNode.getRPSTType() == 'T' ) {
            QcNode node = petri.getQcNodeById( rpstNode.getEntry().getName() );
            if ( node.shapeType==Common.Transition ) {
                addTemplateSentence( "Trivial_Begin_Template", depth );
                addTrivialSentence( node, depth );
                addTemplateSentence( "Trivial_End_Template", depth );
            }
        } else if ( rpstNode.getRPSTType() == 'P' ) {
            addTemplateSentence( "Polygon_Begin_Template", depth );
            boolean loopFlag = petri.isLoop_Label();
            if ( loopFlag == true ) {
                addTemplateSentence( "Loop_Begin_Template", depth );
            }
            ArrayList<RPSTNode<ControlFlow, de.hpi.bpt.process.Node>> orderedTopNodes = PlanningHelper.sortTreeLevel( (RPSTNode<ControlFlow, de.hpi.bpt.process.Node>)rpstNode, (de.hpi.bpt.process.Node)rpstNode.getEntry(), (RPST<ControlFlow, de.hpi.bpt.process.Node>)rpst );
            for (int i = 0; i < orderedTopNodes.size(); i++) {
                if ( i-1>=0 && ( orderedTopNodes.get(i-1).getRPSTType()=='B' || orderedTopNodes.get(i-1).getRPSTType()=='R' ) && orderedTopNodes.get(i).getRPSTType()=='T' ) {
                    continue;
                }
                dfsGenerateNewText( petri, cpu, rpst, orderedTopNodes.get(i), depth+1 );
            }
            if ( loopFlag == true ) {
                addTemplateSentence( "Loop_End_Template", depth );
            }
            addTemplateSentence( "Polygon_End_Template", depth );
        } else if ( rpstNode.getRPSTType() == 'B' ) {

            QcNode source = petri.getQcNodeById( rpstNode.getEntry().getName() );
            QcNode sink   = petri.getQcNodeById( rpstNode.getExit().getName() );

            source.dyeType = Common.StartNode;
            sink.dyeType   = Common.EndNode;

            ArrayList<QcPetriNet> paths = new ArrayList<>();

            Object[] objs = rpst.getChildren( rpstNode ).toArray();
            for (int i = 0; i < objs.length; i++) {
                RPSTNode child = (RPSTNode)objs[i];
                QcPetriNet petri_ = new QcPetriNet();
                petri_.addSubPetri( petri, rpst.getLeaves(child) );
                QcNode source_ = petri_.getOnlySource();
                if ( source_==null || source_.isSameId(source) ) {
                    paths.add( petri_ );
                }
            }
            for (int i = 0; i < objs.length; i++) {
                RPSTNode child = (RPSTNode)objs[i];
                QcPetriNet petri_ = new QcPetriNet();
                petri_.addSubPetri( petri, rpst.getLeaves(child) );
                QcNode source_ = petri_.getOnlySource();
                if( source_!=null && source_.isSameId(sink) ){
                    paths.add( petri_ );
                }
            }

            if ( source.shapeType==Common.Place && sink.shapeType==Common.Place ) {
                addTemplateSentence( "BondPlace_Begin_Template", depth );
                sortPaths( paths );
                for ( QcPetriNet petri_: paths ) {
                    QcTextGenerator textGenerator_ = new QcTextGenerator(templateMap);

                    QcNode node_ = petri_.getOnlySource();
                    if ( node_==null || node_.dyeType == Common.StartNode ) {
                        textGenerator_.generateText(petri_, depth + 1);
                        addSentences(textGenerator_.sentences);
                    } else {
                        addTemplateSentence( "Loop_Begin_Template", depth+1 );
                        textGenerator_.generateText(petri_, depth + 1);
                        addSentences(textGenerator_.sentences);
                        addTemplateSentence( "Loop_End_Template", depth+1 );
                    }

                    this.flag = textGenerator_.flag;
                    if ( this.flag == false ) {
                        break;
                    }
                }
                addTemplateSentence( "BondPlace_End_Template", depth );
            } else if ( source.shapeType==Common.Transition && sink.shapeType==Common.Transition ) {
                addTemplateSentence( "BondTransition_Begin_Template", depth );
                addTemplateSentence( "Trivial_Begin_Template", depth+1 );
                addTrivialSentence( source, depth+1 );
                addTemplateSentence( "Trivial_End_Template", depth+1 );
                for ( QcPetriNet petri_: paths ) {
                    petri_.removeQcNode( petri_.getOnlySource() );
                    petri_.removeQcNode( petri_.getOnlySink() );
                }
                sortPaths( paths );
                for ( QcPetriNet petri_: paths ) {
                    if ( petri_.nodes.size()<=1 ) {
                        continue;
                    }
                    QcTextGenerator textGenerator_ = new QcTextGenerator( templateMap );
                    textGenerator_.generateText( petri_, depth+1 );
                    addSentences( textGenerator_.sentences );

                    this.flag = textGenerator_.flag;
                    if ( this.flag == false ) {
                        break;
                    }
                }
                addTemplateSentence( "Trivial_Begin_Template", depth+1 );
                addTrivialSentence( sink, depth+1 );
                addTemplateSentence( "Trivial_End_Template", depth+1 );
                addTemplateSentence( "BondTransition_End_Template", depth );
            } else {
                Common.printError( "public void dfsGenerateText( RPSTNode rpstNode, RPST rpst, int depth, QcPetriNet petri )" );
            }
        } else {
            QcNode sourceNode = petri.getQcNodeById( rpstNode.getEntry().getName() );
            QcNode sinkNode   = petri.getQcNodeById( rpstNode.getExit().getName() );

            ArrayList<RPSTNode> rpstNodes =  rpst.getLeaves( rpstNode );
            QcPetriNet petri_cmp = petri.getSubPetri( rpstNodes );
//                petri_cmp.print();

            HashSet<String> simLabels = petri_cmp.getAllSimpliedLabel();
            QcPetriNet cpu_cmp = cpu.getSubPetriFromSimpliedLabel( simLabels );
//                cpu_cmp.print();

            QcPetriNet cpu_cmp_workflow = cpu_cmp.workflowFy();
//            cpu_cmp_workflow.print();

            if ( cpu_cmp_workflow.isStructured() == true ) {
                RPST rpstWorkflow = cpu_cmp_workflow.getRPST();
//                rpstWorkflow.print( rpstWorkflow.getRoot(), 0 );
                QcTextGenerator textGenerator_ = new QcTextGenerator( templateMap );
                textGenerator_.generateText( cpu_cmp_workflow, depth+1 );
                addSentences( textGenerator_.sentences );
            } else {
                if ( sourceNode.shapeType == Common.Place ) {
                    addTemplateSentence("RigidPlace_Begin_Template", depth);

                    cpu_cmp.dyeNodes(sourceNode, sinkNode);

                    ArrayList<QcPetriNet> subPaths = getSubPaths(cpu_cmp);

                    ArrayList<QcPetriNet> paths = getLinkedPaths(subPaths);

                    sortPaths(paths);

                    paths = combine(paths);

                    cleanPaths(paths);

                    for (int i = 0; i < paths.size(); i++) {
                        QcPetriNet petri_ = paths.get(i);
//                    petri_.print();
                        if (petri_.nodes.size() == 0) {
                            continue;
                        }

                        if (i > 0) {
                            QcNode pathSource = petri_.getOnlySource();
                            if (pathSource.shapeType == Common.Place) {
                                QcNode sourcePlace = petri.getOnlyQcNodeByname(pathSource.name);
                                if (sourcePlace != null) {
                                    QcNode lastTransition = petri.getLastOnlyNode(sourcePlace);
                                    if (lastTransition != null) {
                                        petri_.addQcEdge(lastTransition, pathSource);
                                    }
                                }
                            }

                            QcNode pathSink = petri_.getOnlySink();
                            if (pathSink.shapeType == Common.Place) {
                                QcNode sinkPlace = petri.getOnlyQcNodeByname(pathSink.name);
                                if (sinkPlace != null) {
                                    QcNode nextTransition = petri.getNextOnlyNode(sinkPlace);
                                    if (nextTransition != null) {
                                        petri_.addQcEdge(pathSink, nextTransition);
                                    }
                                }
                            }
                        }

                        QcTextGenerator textGenerator_ = new QcTextGenerator(templateMap);
                        textGenerator_.generateText(petri_, depth + 1);

                        addSentences(textGenerator_.sentences);

                        this.flag = textGenerator_.flag;
                        if (this.flag == false) {
                            break;
                        }
                    }

                    addTemplateSentence("RigidPlace_End_Template", depth);
                } else {
                    addTemplateSentence( "RigidTransition_Begin_Template", depth );

                    ArrayList<QcPetriNet> paths = new ArrayList<>();
                    HashMap<QcEdge,Integer> edgeEnabledFlagMap = new HashMap<>();

                    for ( QcEdge edge_: petri_cmp.edges ) {
                        edgeEnabledFlagMap.put( edge_, 0 );
                    }

                    HashMap<QcNode,Integer> flag = new HashMap<>();

                    while( flag.keySet().size()<petri_cmp.getTransitions().size() ) {

                        QcNode enabledQcNode = petri_cmp.findEnabledQcNode( edgeEnabledFlagMap );

                        if ( enabledQcNode == null ) {
                            break;
                        }

                        for ( QcNode nextTransition: petri_cmp.getNextTransitionGateway(enabledQcNode) ) {
                            ArrayList<QcPetriNet> paths_ = petri_cmp.getSubPetriFromQcNodeToQcNode( enabledQcNode, nextTransition );
                            QcPetriNet pi = new QcPetriNet();
                            for ( QcPetriNet petri1_: paths_ ) {
                                pi.addSubPetri( petri1_ );
                            }
                            paths.add( pi );
                            for ( QcNode trans: pi.getTransitions() ) {
                                flag.put( trans, 1 );
                            }

                            for ( QcEdge edge_:pi.edges ) {
                                String sourceLabel = edge_.source.id;
                                String targetLabel = edge_.target.id;
                                if ( petri_cmp.getQcNodeById(sourceLabel)!=null && petri_cmp.getQcNodeById(targetLabel)!=null ) {
                                    edgeEnabledFlagMap.put( petri_cmp.getQcEdgeById(sourceLabel+"_to_"+targetLabel), 1 );
                                }
                            }
                        }
                    }

                    for (int i = 0; i < paths.size(); i++) {
                        QcPetriNet petri_ = paths.get(i);
//                    petri_.print();

                        QcTextGenerator textGenerator_ = new QcTextGenerator( templateMap );
                        textGenerator_.generateText( petri_, depth+1 );

                        addSentences( textGenerator_.sentences );

                        this.flag = textGenerator_.flag;
                        if ( this.flag == false ) {
                            break;
                        }
                    }

                    addTemplateSentence( "RigidTransition_End_Template", depth );
                }
            }
        }
    }

    //Tested
    private void dfsDyedPetri( QcNode lastNode, QcNode node, int depth, QcPetriNet pi, ArrayList<QcPetriNet> subPaths, QcPetriNet cpu ){

        if ( lastNode == null ) {
            pi.addQcNode( node );
        } else {
            pi.addQcEdge( lastNode, node );
        }

        //并发问题
        if ( depth != 0 && (node.dyeType != Common.NormalNode) ) {
            subPaths.add( pi );
            return ;
        }

        HashSet<QcNode> nextNodes = cpu.getNextNodes( node );

        if ( node.shapeType == Common.Place ) {
            for ( QcNode nextNode: nextNodes ) {
                QcPetriNet petri_ = new QcPetriNet();
                petri_.addSubPetri( pi );
                dfsDyedPetri( node, nextNode, depth+1, petri_, subPaths, cpu );
            }
        } else if ( node.shapeType == Common.Transition ) {
            for ( QcNode nextNode: nextNodes ) {
                dfsDyedPetri( node, nextNode, depth+1, pi, subPaths, cpu );
            }
        }
    }

    //Tested
    public ArrayList<QcPetriNet> getSubPaths( QcPetriNet petri ){

        ArrayList<QcPetriNet> subPaths = new ArrayList<>();

        for ( QcNode node_: petri.nodes ) {
            if ( node_.dyeType != Common.NormalNode ) {
                QcPetriNet pi = new QcPetriNet();
                dfsDyedPetri( null, node_, 0, pi, subPaths, petri );
            }
        }

        return subPaths;
    }

    //Tested
    public ArrayList<QcPetriNet> getLinkedPaths( ArrayList<QcPetriNet> subPaths ) {

        ArrayList<QcPetriNet> paths = new ArrayList<>();

        for ( QcPetriNet subPath : subPaths ) {
            dfsLinkSubPaths( subPath, 0, subPaths, paths );
        }

        return paths;
    }

    //Tested
    private void dfsLinkSubPaths( QcPetriNet subPath, int depth, ArrayList<QcPetriNet> subPaths, ArrayList<QcPetriNet> paths ) {

//        subPath.print();

        for ( QcNode sinkNode: subPath.nodes ) {
            for ( QcNode node: subPath.nodes ) {
                if ( sinkNode == node ) {
                    continue;
                }
                if ( node.dyeType==Common.StartNode && sinkNode.dyeType==Common.EndNode ) {
                    paths.add( subPath );
                    return ;
                }
                else if ( node.dyeType==Common.EndNode && sinkNode.dyeType==Common.StartNode ) {
                    paths.add( subPath );
                    return ;
                }
                else if ( sinkNode.isSameName(node) == true ) {
                    paths.add( subPath );
                    return ;
                }
            }
        }

        for ( QcPetriNet subPath_ : subPaths ) {
            if ( subPath != subPath_ ) {
                HashMap<QcNode,QcNode> linkedNodeMap2 = canLink( subPath, subPath_ );
                if ( subPath!=subPath_ && linkedNodeMap2!=null ) {
                    QcPetriNet pi = link( subPath, subPath_, linkedNodeMap2 );
                    dfsLinkSubPaths( pi, depth+1, subPaths, paths );
                }
            }
        }
    }

    //Tested
    private HashMap<QcNode,QcNode> canLink( QcPetriNet subPath, QcPetriNet subPath_ ){

        HashMap<QcNode,QcNode> linkedNodeMap1 = new HashMap<>();
        HashMap<QcNode,QcNode> linkedNodeMap2 = new HashMap<>();

        HashSet<QcNode> sinkList   = subPath.getSinks();
        HashSet<QcNode> sourceList = subPath_.getSources();

        if ( sinkList.size() != sourceList.size() ) {
            return null;
        }

        for ( QcNode node1: sinkList ) {
            linkedNodeMap1.put( node1, null );
        }
        for ( QcNode node2: sourceList ) {
            linkedNodeMap2.put( node2, null );
        }

        for ( QcNode node1: sinkList ) {
            for ( QcNode node2: sourceList ) {
                if ( node1.name.equals(node2.name) && linkedNodeMap1.get(node1)==null && linkedNodeMap2.get(node2)==null ) {
                    linkedNodeMap1.put( node1, node2 );
                    linkedNodeMap2.put( node2, node1 );
                    break;
                }
            }
        }

        for ( QcNode key: linkedNodeMap1.keySet() ) {
            QcNode value = linkedNodeMap1.get(key);
            if ( value == null ) {
                return null;
            }
        }

        for ( QcNode key: linkedNodeMap2.keySet() ) {
            QcNode value = linkedNodeMap2.get(key);
            if ( value == null ) {
                return null;
            }
        }

        return linkedNodeMap2;
    }

    //Tested
    private QcPetriNet link( QcPetriNet subPath, QcPetriNet subPath_, HashMap<QcNode,QcNode> linkedNodeMap2 ){

//        subPath.print();
//        subPath_.print();
//        System.out.println( linkedNodeMap2.keySet() );
//        System.out.println( linkedNodeMap2.values() );

        QcPetriNet pi = new QcPetriNet();

        pi.addSubPetri( subPath );

        HashSet<QcNode> sourceNodes = subPath_.getSources();

        for ( QcEdge edge: subPath_.edges ) {
            QcNode source = edge.source;
            QcNode target = edge.target;
            if ( sourceNodes.contains(source) ) {
                source = linkedNodeMap2.get( source );
            }
            pi.addQcEdge( source, target );
        }

        return pi;
    }

    //Tested
    private void sortPaths( ArrayList<QcPetriNet> paths ) {
        Collections.sort( paths, new AdequateOrder_SPF() );
    }

    private void cleanPaths( ArrayList<QcPetriNet> paths ) {

        HashMap<String,Integer> RdescribedFlag = new HashMap<>();

        for (int i = 0; i < paths.size(); i++) {
            HashSet<QcNode> toBeDescribed = new HashSet<>();
            for ( QcNode node_: paths.get(i).nodes ) {
                if ( node_.shapeType==Common.Transition && RdescribedFlag.get(node_.name.split("-")[0])==null ) {

                    toBeDescribed.add( node_ );

                    QcNode nextNode = paths.get(i).getNextOnlyNode(node_);
                    if ( nextNode != null ) {
                        toBeDescribed.add( nextNode );
//                        if ( i>0 ) {
//                            QcNode nextNextNode = paths.get(i).getNextOnlyNode(nextNode);
//                            if ( nextNextNode != null ) {
//                                toBeDescribed.add( nextNextNode );
//                            }
//                        }
                    }

                    QcNode lastNode = paths.get(i).getLastOnlyNode(node_);
                    if ( lastNode != null ) {
                        toBeDescribed.add( lastNode );
//                        if ( i>0 ) {
//                            QcNode lastLastNode = paths.get(i).getLastOnlyNode(lastNode);
//                            if ( lastLastNode != null ) {
//                                toBeDescribed.add( lastLastNode );
//                            }
//                        }
                    }
                }
            }

            QcPetriNet pi = new QcPetriNet();
            pi.addSubPetri( paths.get(i) );
            for ( QcNode node_: paths.get(i).nodes ) {
                if ( toBeDescribed.contains(node_)==false ) {
                    pi.removeQcNode( pi.getQcNodeById(node_.id) );
                }
            }

            if( pi.nodes.size() > 0 ) {
                if (pi.getOnlySource().shapeType == Common.Transition) {
                    QcNode source = new QcNode("","SuperSource", Common.Place, Common.NormalNode);
                    pi.addQcEdge(source, pi.getOnlySource());
                }
                if (pi.getOnlySink().shapeType == Common.Transition) {
                    QcNode sink = new QcNode("","SuperSink", Common.Place, Common.NormalNode);
                    pi.addQcEdge(pi.getOnlySink(), sink);
                }
                paths.set(i, pi);

                for (QcNode node_ : paths.get(i).nodes) {
                    RdescribedFlag.put(node_.name.split("-")[0], 1);
                }
            } else {
                paths.set(i, pi);
            }
        }
    }

    public ArrayList<QcPetriNet> combine( ArrayList<QcPetriNet> paths_ ){
        ArrayList<QcPetriNet> paths = new ArrayList<>();

        QcPetriNet pi = new QcPetriNet();
        for (int i = 0; i < paths_.size(); i++) {

            QcPetriNet pj = new QcPetriNet();
            pj.addSubPetri( pi );

            pi.addSimpleSubPetri( paths_.get(i) );

            if ( pi.isStructured()==false ) {
                paths.add( pj );
                for (int j = i; j < paths_.size(); j++) {
                    paths.add( paths_.get(j) );
                }
                return paths;
            }

            if ( i == paths_.size()-1 ) {
                paths.add( pi );
            }
        }

        return null;
    }
}

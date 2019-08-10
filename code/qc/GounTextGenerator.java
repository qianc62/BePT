package qc;

import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import org.odmg.QueryParameterCountInvalidException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import qc.common.Common;
import textPlanning.PlanningHelper;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.SocketImpl;
import java.util.*;

public class GounTextGenerator {

    public HashMap<String,String> templateMap = null;   //模板句型
    public ArrayList<QcSentence> sentences = null;  //文本系统的所有句子,用于生成QcTreeNode数据结构
    public Attribute attribute = null;  //文本生成过程中的各类属性,用于实验统计
    public boolean flag;    //该标志为算法是否能够应对该模型的flag

    //Tested
    private void init(){
        templateMap = new HashMap<>();
        sentences = new ArrayList<>();

        attribute = new Attribute();

        flag = true;
    }

    //Tested
    public GounTextGenerator( HashMap<String,String> templateMap_ ){
        init();
        templateMap = templateMap_;
    }

    //Tested
    public GounTextGenerator( String path ) throws Exception {
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
        dfsGenerateGounText( petri, rpst, rpst.getRoot(), depth );

        if ( depth == 0 ) {
            attribute.petri = petri;
            attribute.rpst = rpst;

            if ( this.flag == false ) {
                attribute.startTime = System.currentTimeMillis();
                attribute.endTime = System.currentTimeMillis();
                attribute.string = "Generation Failed.";
                return this.attribute;
            }

            QcTreeNode root = QcTreeNode.createTree( sentences );
//            root.print( 0 );

            attribute.rootNode = root;
            attribute.startTime = System.currentTimeMillis();
            String str = root.toText( 0, templateMap.get("Workflow_Begin_Template"), ". " + templateMap.get("Workflow_End_Template") + ". ", templateMap, attribute.depthMap );
            str = QcTreeNode.postProcessText( petri, str );
            attribute.endTime = System.currentTimeMillis();
            attribute.string = str;

            attribute.caculate();

        } else {
            attribute.string = "";
        }

        return this.attribute;
    }

    //Tested
    public void dfsGenerateGounText( QcPetriNet petri, RPST rpst, RPSTNode rpstNode, int depth ) throws Exception {

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
                dfsGenerateGounText( petri, rpst, orderedTopNodes.get(i), depth+1 );
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
                for ( QcPetriNet petri_: paths ) {
                    GounTextGenerator textGenerator_ = new GounTextGenerator(templateMap);
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
                //BondTransition类型的描述模式与BondPlace一致
                addTemplateSentence( "BondPlace_Begin_Template", depth );
                for ( QcPetriNet petri_: paths ) {
                    GounTextGenerator textGenerator_ = new GounTextGenerator( templateMap );
                    textGenerator_.generateText( petri_, depth+1 );
                    addSentences( textGenerator_.sentences );
                    this.flag = textGenerator_.flag;
                    if ( this.flag == false ) {
                        break;
                    }
                }
                addTemplateSentence( "BondPlace_End_Template", depth );
            } else {
                this.flag = false;
            }
        } else {
            QcNode source = petri.getQcNodeById( rpstNode.getEntry().getName() );
            QcNode sink   = petri.getQcNodeById( rpstNode.getExit().getName() );

            if ( source.shapeType==Common.Place && sink.shapeType==Common.Place ) {
                addTemplateSentence("RigidPlace_Begin_Template", depth);
            } else if ( source.shapeType==Common.Transition && sink.shapeType==Common.Transition ) {
                addTemplateSentence("RigidTransition_Begin_Template", depth);
            } else {
                addTemplateSentence("Fail_Algorithm_Template", depth);
                this.flag = false;
                return ;
            }

            ArrayList<RPSTNode> rpstNodes = rpst.getLeaves(rpstNode);
            QcPetriNet petri_cmp = petri.getSubPetri(rpstNodes);

            QcPetriNet petri_cmp_top = petri_cmp;

            QcPetriNet petri_cmp_top_ = new QcPetriNet();
            petri_cmp_top_.addSubPetri(petri_cmp_top);

            boolean loopFlag = petri_cmp_top.isLoop_Structure();

            if (loopFlag == false) {

//                petri_cmp_top.addSubPetri(petri_cmp_top_);

                //可多个目标节点
                HashMap<String,ArrayList<String>> goals = new HashMap<>();

                while (petri_cmp_top_.nodes.size() != 0) {
                    match( petri_cmp_top_, petri_cmp_top, goals );
                    removeWithSimplify( petri_cmp_top_, petri_cmp_top, goals );
                }

//                System.out.println( "Goals:" );
//                for (String key : goals.keySet()) {
//                    System.out.format( "%s: ", key );
//                    ArrayList<String> goalIds = goals.get( key );
//                    for ( String str: goalIds ) {
//                        System.out.format( "%s ", str );
//                    }
//                    System.out.println();
//                }

                ArrayList<QcPetriNet> paths = GoalUnfolding( petri_cmp_top, goals );

//                addPaths( paths, petri_cmp_top );

                if (paths.size() > 0) {
                    for (int i = 0; i < paths.size(); i++) {
                        QcPetriNet petri_ = paths.get(i);
//                        petri_.print();
                        if ( petri_.nodes.size()==0 ) {
                            continue;
                        }

                        if ( i>0 ) {
                            QcNode pathSource = petri_.getOnlySource();
                            if ( pathSource!=null && pathSource.shapeType==Common.Place ) {
                                QcNode sourcePlace = petri.getOnlyQcNodeByname( pathSource.name );
                                if ( sourcePlace!=null ) {
                                    QcNode lastTransition = petri.getLastOnlyNode( sourcePlace );
                                    if ( lastTransition!=null ) {
                                        petri_.addQcEdge( lastTransition, pathSource );
                                    }
                                }
                            }

                            QcNode pathSink = petri_.getOnlySink();
                            if ( pathSink!=null && pathSink.shapeType==Common.Place ) {
                                QcNode sinkPlace = petri.getOnlyQcNodeByname(pathSink.name);
                                if (sinkPlace != null) {
                                    QcNode nextTransition = petri.getNextOnlyNode(sinkPlace);
                                    if (nextTransition != null) {
                                        petri_.addQcEdge(pathSink, nextTransition);
                                    }
                                }
                            }
                        }

                        GounTextGenerator textGenerator_ = new GounTextGenerator(templateMap);
                        textGenerator_.generateText(petri_, depth + 1);

                        addSentences(textGenerator_.sentences);
                        this.flag = textGenerator_.flag;
                        if ( this.flag == false ) {
                            break;
                        }
                    }
                } else {
                    addTemplateSentence("Fail_Algorithm_Template", depth);
                    this.flag = false;
                }
            } else {
                addTemplateSentence("Fail_Algorithm_Template", depth);
                this.flag = false;
            }

            if ( source.shapeType == Common.Place ) {
                addTemplateSentence("RigidPlace_End_Template", depth);
            } else {
                addTemplateSentence("RigidTransition_End_Template", depth);
            }
        }
    }

    public void addGoalNode( HashMap<String,ArrayList<String>> goals, String nodeStr, String goalStr, final QcPetriNet oriPetri ){

        QcNode sourceNode = oriPetri.getQcNodeById( nodeStr );
        QcNode goalNode   = oriPetri.getQcNodeById( goalStr );
        HashSet<QcNode> nextNodes = oriPetri.getNextNodes( sourceNode );
        for ( QcNode node_: nextNodes ) {
            if ( oriPetri.getSubPetriFromQcNodeToQcNode( node_, goalNode ).size() == 0 ) {
                return ;
            }
        }

        if ( goals.get( nodeStr ) == null ) {
            ArrayList<String> list = new ArrayList<>();
            list.add( goalStr );
            goals.put( nodeStr, list );
        } else if ( goals.get(nodeStr).contains( goalStr )==false ) {
            goals.get( nodeStr ).add( goalStr );
        }
    }

    public void match( QcPetriNet petri, final QcPetriNet oriPetri, HashMap<String,ArrayList<String>> goals ){
        HashSet<QcNode> sources = petri.getSources();
        HashSet<QcNode> sinks = petri.getSinks();

        for ( QcNode source: sources ) {
            int min = Common.INF;
            QcNode minDisNode = null;
            for ( QcNode sink: sinks ) {
                int dis = GounDistance( petri, source, sink );
                if ( dis < min ) {
                    min = dis;
                    minDisNode = sink;
                }
            }

            addGoalNode( goals, source.id, minDisNode.id, oriPetri );
        }
    }

    public int GounDistance( QcPetriNet petri_cmp_top, QcNode node1, QcNode node2 ){
        ArrayList<Integer> dises = new ArrayList<>();

        dfsGounDistance( node1, node2, 0, dises, petri_cmp_top );

        int min = Common.INF;
        for ( Integer integer: dises ) {
            if ( integer.intValue() < min ) {
                min = integer.intValue();
            }
        }

        return min;
    }

    public void dfsGounDistance( QcNode node1, QcNode node2, int len, ArrayList<Integer> dises, QcPetriNet petri_cmp_top ){

        if ( node1.id.equals( node2.id ) ) {
            dises.add( len );
            return ;
        }

        HashSet<QcNode> nextNodes = petri_cmp_top.getNextNodes( node1 );
        for (QcNode nextNode : nextNodes) {
            dfsGounDistance( nextNode, node2, len+1, dises, petri_cmp_top );
        }
    }

//    public void remove( QcPetriNet petri, ArrayList<String> matchedNodes ){
//        for ( String key: matchedNodes ) {
//            QcNode node_ = petri.getQcNodeById( key );
//            petri.removeQcNode( node_ );
//        }
//    }
//
//    public void simplify( QcPetriNet petri ){
//
//        while( petri.nodes.size()>0 ){
//            HashSet<QcNode> sources = petri.getSources();
//            HashSet<QcNode> sinks = petri.getSinks();
//
//            boolean changeFlag = false;
//
//            for ( QcNode node_: sources ) {
//                if ( petri.getNextNodes(node_).size() <= 1 ) {
//                    if ( petri.removeQcNode( node_ ) == true ) {
//                        changeFlag = true;
//                    }
//                }
//            }
//
//            for ( QcNode node_: sinks ) {
//                if ( petri.getQcNodeById( node_.id )==null ) {
//                    continue;
//                }
//                if ( petri.getLastNodes(node_).size() <= 1 ) {
//                    if ( petri.removeQcNode( node_ ) == true ) {
//                        changeFlag = true;
//                    }
//                }
//            }
//
//            if ( changeFlag==false ) {
//                break;
//            }
//        }
//    }

    public void removeWithSimplify( QcPetriNet petri, final QcPetriNet oriPetri, HashMap<String,ArrayList<String>> goals ) {

        int times = 0;

        while( petri.nodes.size()>0 ){
            HashSet<QcNode> sources = petri.getSources();
            HashSet<QcNode> sinks = petri.getSinks();

            int nodeNum = petri.nodes.size();

            for ( QcNode source: sources ) {
                if ( times!=0 && petri.getNextNodes(source).size() > 1 ) {
                    continue;
                }

                HashSet<QcNode> sourceNextNodes = petri.getNextNodes(source);
                petri.removeQcNode( source );

                for ( QcNode sourceNextNode: sourceNextNodes ) {
                    QcNode nowNode = sourceNextNode;
                    while( true ){
                        QcNode nextNode = petri.getNextOnlyNode( nowNode );
                        if ( petri.getLastNodes(nowNode).size()==0 && nextNode!=null ) {
                            if ( petri.getLastNodes(nextNode).size()>1 ) {
                                addGoalNode( goals, source.id, nextNode.id, oriPetri );
                            }
                            petri.removeQcNode( nowNode );
                            nowNode = nextNode;
                        } else {
                            break;
                        }
                    }
                }
            }

            for ( QcNode sink: sinks ) {
                if ( times!=0 && petri.getLastNodes(sink).size() > 1 ) {
                    continue;
                }

                HashSet<QcNode> sinkLastNodes = petri.getLastNodes(sink);
                petri.removeQcNode( sink );

                for ( QcNode sinkLastNode: sinkLastNodes ) {
                    QcNode nowNode = sinkLastNode;
                    while( true ){
                        QcNode lastNode = petri.getLastOnlyNode( nowNode );
                        if ( lastNode!=null && petri.getNextNodes(nowNode).size()==0 ) {
                            if ( petri.getNextNodes(lastNode).size()>1 ) {
                                addGoalNode( goals, lastNode.id, sink.id, oriPetri );
                            }
                            petri.removeQcNode( nowNode );
                            nowNode = lastNode;
                        } else {
                            break;
                        }
                    }
                }
            }

            times++;

            if ( nodeNum == petri.nodes.size() ) {
                break;
            }
        }
    }

    public ArrayList<QcPetriNet> GoalUnfolding( QcPetriNet petri, HashMap<String,ArrayList<String>> goals ){
        ArrayList<QcPetriNet> paths = new ArrayList<>();

        QcNode source = petri.getOnlySource();
        QcNode sink   = petri.getOnlySink();

        QcPetriNet pi = new QcPetriNet();
        QcNode sourceCopy = new QcNode( source.id+"_copy", source.name, source.shapeType , source.dyeType );
        QcNode sinkCopy   = new QcNode( sink.id+"_copy", sink.name, sink.shapeType , sink.dyeType );

        //结构化模型
        dfsGoalUnfolding( petri, source, sink, sourceCopy, sinkCopy, pi, goals );
        paths.add( pi );

        //Omissive图
        QcPetriNet pj = new QcPetriNet();
        for ( QcNode node_: petri.nodes ) {
            if ( node_.shapeType==Common.Transition && pi.containQcNodeByName( node_.name )==false ) {
                QcNode lastNode = petri.getLastOnlyNode( node_ );
                QcNode nextNode = petri.getNextOnlyNode( node_ );
                pj.addQcEdge( lastNode, node_ );
                pj.addQcEdge( node_, nextNode );
            }
        }
//        paths.add( pj );

        //根据Omissive图生成多个Omissive子Petri
        for ( QcNode node_: pj.getSources() ) {
            QcPetriNet pk = pj.getSubPetriFromQcNodeToINF( node_ );
            paths.add( pk );
        }

        return paths;
    }

    public void dfsGoalUnfolding( QcPetriNet Petri, QcNode Source, QcNode Sink, QcNode source, QcNode sink, QcPetriNet pi, HashMap<String,ArrayList<String>> goals ){

        HashSet<QcNode> nextNodes = Petri.getNextNodes( Source );
        for ( QcNode pathSource: nextNodes ) {
            QcPetriNet path = Petri.getShortestSubPetriFromQcNodeToQcNodeWithGoals( pathSource, Sink, goals );
            path.addQcEdge( Source, pathSource );

            HashMap<QcNode,QcNode> copyNodeMap = new HashMap<>();
            for ( QcNode node_: path.nodes ) {
                QcNode node1_ = new QcNode( node_.id+"_copy"+pi.nodes.size(), node_.name, node_.shapeType, node_.dyeType );
                copyNodeMap.put( node_, node1_ );
            }
            copyNodeMap.put( Source, source );
            copyNodeMap.put( Sink, sink );

            boolean descriptionFlag = true;
            QcNode nowNode = path.getOnlySource();
            while( nowNode != null ){
                if ( descriptionFlag == true ) {
                    QcNode lastNode = path.getLastOnlyNode( nowNode );
                    if ( lastNode != null ) {
                        pi.addQcEdge( copyNodeMap.get(lastNode), copyNodeMap.get(nowNode) );
                    }
                }

                descriptionFlag = true;

                String goalId = null;
                if ( goals.get( nowNode.id ) != null ) {
                    for ( String goalLabel: goals.get(nowNode.id) ) {
                        if ( nowNode.isSameId(Source) && Sink.id.equals(goalLabel) ) {
                            continue;
                        }
                        if ( goalLabel != null ) {
                            if ( path.getQcNodeById( goalLabel ) != null ) {
                                goalId = goalLabel;
                                break;
                            }
                        }
                    }
                }

                if ( goalId==null ) {
                    nowNode = path.getNextOnlyNode( nowNode );
                } else {
                    QcNode goalNode = path.getQcNodeById( goalId );
                    QcNode sourceCopy = copyNodeMap.get( nowNode );
                    QcNode sinkCopy   = copyNodeMap.get( goalNode );
                    dfsGoalUnfolding( Petri, nowNode, goalNode, sourceCopy, sinkCopy, pi, goals );
                    nowNode = goalNode;
                    descriptionFlag = false;
                }
            }
        }
    }

    public QcPetriNet getShortesetSubPath( QcNode source, QcNode sink, HashMap<QcEdge,Integer> edgeVisitedMap, QcPetriNet petri ){

        ArrayList<QcPetriNet> subPaths = new ArrayList<>();

        QcPetriNet pi = new QcPetriNet();
        pi.addQcNode( source );

        dfsGetShortestSubPath( pi, sink, 0, edgeVisitedMap, petri, subPaths );

        int min = Common.INF;
        QcPetriNet minNet = null;
        for ( QcPetriNet subPath: subPaths ) {
            if ( subPath.nodes.size() < min ) {
                min = subPath.nodes.size();
                minNet = subPath;
            }
        }

        return minNet;
    }

    public void dfsGetShortestSubPath( QcPetriNet pi, QcNode sink, int depth, HashMap<QcEdge,Integer> edgeVisitedMap, QcPetriNet petri, ArrayList<QcPetriNet> subPaths ){

        QcNode source = pi.getOnlySink();

        if ( source.id.equals( sink.id ) ) {
            subPaths.add( pi );
            return ;
        }

        HashSet<QcNode> nextNodes = petri.getNextNodes( source );

        //TBD
        for ( QcNode node_: nextNodes ) {
            QcEdge edge_ = petri.getQcEdgeById( source.id + "_to_" + node_.id );
            if ( edgeVisitedMap.get(edge_) != 0 ) {
                continue;
            }

            QcPetriNet pj = new QcPetriNet();
            pj.addSubPetri( pi );
            pj.addQcEdge( source, node_ );

            dfsGetShortestSubPath( pj, sink, depth+1, edgeVisitedMap, petri, subPaths );
        }
    }

    public ArrayList<QcPetriNet> getOmissive( QcPetriNet petri_, QcPetriNet gu ){

        ArrayList<QcPetriNet> omissivePaths = new ArrayList<>();

        for ( QcEdge edge_: petri_.edges ) {
            QcNode node1 = edge_.source;
            QcNode node2 = edge_.target;
            if ( gu.getQcNodeById( node1.name + "_to_" + node2.name ) == null ) {
                QcPetriNet pi = new QcPetriNet();
                dfsGetOmissive( null, node2, pi, omissivePaths, petri_, gu );
                pi.addQcEdge( node1, node2 );
            }
        }

        return omissivePaths;
    }

    public void dfsGetOmissive( QcNode lastNode, QcNode node, QcPetriNet pi, ArrayList<QcPetriNet> omissivePaths, QcPetriNet petri_, QcPetriNet gu ){
        if ( lastNode == null ) {
            pi.addQcNode( node );
        } else {
            pi.addQcEdge( lastNode, node );
        }

        if ( gu.getQcNodeById( node.name ) != null ) {
            if ( pi.nodes.size() > 1 ) {
                omissivePaths.add( pi );
            }
            return ;
        }

        HashSet<QcNode> nextNodes = petri_.getNextNodes( node );

        for ( QcNode nextNode: nextNodes ) {
            dfsGetOmissive( node, nextNode, pi, omissivePaths, petri_, gu );
        }
    }

//    public String getQcNodeNameWithNum( QcNode node, HashMap<String,Integer> numMap ){
//        String simpName = node.name.split("-")[0];
//        int value = numMap.get( simpName );
//        return simpName + "-" + value;
//    }

//    public void addNumMap( QcNode node, HashMap<String,Integer> numMap ){
//        String simpName = node.name.split("-")[0];
//        int value = numMap.get( simpName );
//        numMap.replace( simpName, value+1 );
//    }

    public boolean isGoalNode( QcNode node, HashMap<String,String> goals ){
        String sim = node.name.split("-")[0];
        if ( goals.containsKey(sim)==true || goals.containsValue(sim)==true ) {
            return true;
        }
        return false;
    }

    //Omissive中补充前后各一个变迁
    private void addPaths( ArrayList<QcPetriNet> paths, QcPetriNet petri ) {

        for ( int i=1; i<paths.size(); i++ ) {
            QcPetriNet pi = new QcPetriNet();
            pi.addSubPetri( paths.get(i) );

            pi.removeQcNode( pi.getOnlySource() );
            pi.removeQcNode( pi.getOnlySink() );

            QcNode source_ = pi.getOnlySource();
            QcNode target_ = pi.getOnlySink();

            QcNode lastNode = petri.getLastOnlyNode(petri.getOnlyQcNodeByname(source_.name));
            pi.addQcEdge( lastNode, source_ );
            QcNode lastLastNode = petri.getLastOnlyNode(petri.getOnlyQcNodeByname(lastNode.name));
            if ( lastLastNode != null ) {
                pi.addQcEdge( lastLastNode, lastNode );
            }

            QcNode nextNode = petri.getNextOnlyNode(petri.getOnlyQcNodeByname(target_.name));
            pi.addQcEdge( target_, nextNode );
            QcNode nextNextNode = petri.getNextOnlyNode(petri.getOnlyQcNodeByname(nextNode.name));
            if ( nextNextNode != null ) {
                pi.addQcEdge( nextNode, nextNextNode );
            }

            if (pi.getOnlySource().shapeType == Common.Transition) {
                QcNode Source = new QcNode("","SuperSource", Common.Place, Common.NormalNode);
                pi.addQcEdge( Source, pi.getOnlySource() );
            }
            if (pi.getOnlySink().shapeType == Common.Transition) {
                QcNode Sink = new QcNode("","SuperSink", Common.Place, Common.NormalNode);
                pi.addQcEdge( pi.getOnlySink(), Sink );
            }

            paths.set( i, pi );
        }
    }
}

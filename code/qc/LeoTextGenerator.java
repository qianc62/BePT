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

public class LeoTextGenerator {

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
    public LeoTextGenerator( HashMap<String,String> templateMap_ ){
        init();
        templateMap = templateMap_;
    }

    //Tested
    public LeoTextGenerator( String path ) throws Exception {
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
    public String getString(){
        String str = "";

        boolean enter = false;

//        for ( int i = 0; i < sentences.size(); i++ ) {
//            QcSentence sentence = sentences.get(i);
//            if ( sentence.typeString == "" ) {
//                if ( enter == true ) {
//                    str += "\n";
//                    str += Common.getTab(sentence.depth);
//                }
//                str += sentence;
//                enter = false;
//            } else if ( sentence.typeString.contains("Trivial")==false ) {
//                enter = true;
//                if ( sentence.typeString.contains("Process")==false ) {
//                    str += "\n";
//                    str += Common.getTab(sentence.depth) + sentence;
//                }
//            }
//        }
        str += "\n";

        return str;
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
        dfsGenerateHenText( petri, rpst, rpst.getRoot(), depth );

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
            String str = root.toText( 0, templateMap.get("Workflow_Begin_Template"), ". " + templateMap.get("Workflow_End_Template")+". ", templateMap, attribute.depthMap );
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
    public void dfsGenerateHenText( QcPetriNet petri, RPST rpst, RPSTNode rpstNode, int depth ) throws Exception {

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
                dfsGenerateHenText( petri, rpst, orderedTopNodes.get(i), depth+1 );
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
                    HenTextGenerator textGenerator_ = new HenTextGenerator(templateMap);

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
                sortPaths( paths );
                for ( QcPetriNet petri_: paths ) {
                    HenTextGenerator textGenerator_ = new HenTextGenerator( templateMap );
                    textGenerator_.generateText( petri_, depth+1 );
                    addSentences( textGenerator_.sentences );

                    this.flag = textGenerator_.flag;
                    if ( this.flag == false ) {
                        break;
                    }
                }
                addTemplateSentence( "BondPlace_End_Template", depth );
            } else {
                Common.printError( "public void dfsGenerateText( RPSTNode rpstNode, RPST rpst, int depth, QcPetriNet petri )" );
            }
        } else {
            addTemplateSentence( "RigidPlace_Begin_Template", depth );

            ArrayList<RPSTNode> rpstNodes =  rpst.getLeaves( rpstNode );
            QcPetriNet petri_cmp = petri.getSubPetri( rpstNodes );
//            petri_cmp.print();

            ArrayList<QcPetriNet> paths = new ArrayList<>();

            if ( petri_cmp.getSources().size()==1 && petri_cmp.getSinks().size()==1 ) {
                for ( QcNode node_ : petri_cmp.nodes ) {
                    if ( node_.shapeType==Common.Transition && (petri_cmp.getNextNodes(node_).size()>1 || petri_cmp.getLastNodes(node_).size()>1) ) {
                        this.flag = false;
                    }
                }
            } else {
                this.flag = false;
            }

            if ( petri.isLoop_Structure()==true ) {
                this.flag = false;
            }

            if ( this.flag == true ) {

                QcPetriNet petri_cmp_top = petri_cmp;

                QcNode source = petri_cmp_top.getOnlySource();

                QcPetriNet pi = new QcPetriNet();
                pi.addQcNode( new QcNode( source ) );

                dfsFindHenSubPaths( pi, petri_cmp_top, paths );
            }

            if ( paths.size() > 0 ) {
                sortPaths( paths );
                cleanPaths( paths );
                sortPaths( paths );

                for (int i = 0; i < paths.size(); i++) {
                    QcPetriNet petri_ = paths.get(i);
//                petri_.print();
                    if ( petri_.nodes.size()==0 ) {
                        continue;
                    }

                    HenTextGenerator textGenerator_ = new HenTextGenerator( templateMap );
                    textGenerator_.generateText( petri_, depth+1 );

                    addSentences( textGenerator_.sentences );

                    this.flag = textGenerator_.flag;
                    if ( this.flag == false ) {
                        break;
                    }
                }
            } else {
                addTemplateSentence( "Fail_Algorithm_Template", depth );
            }

            addTemplateSentence( "RigidPlace_End_Template", depth );
        }
    }

    public void dfsFindHenSubPaths( QcPetriNet pi, QcPetriNet petri_cmp_top, ArrayList<QcPetriNet> paths ){

        if ( this.flag == false ) {
            return ;
        }

        //循环
        for ( QcNode node1_ : pi.nodes ) {
            for ( QcNode node2_ : pi.nodes ) {
                if ( node1_!=node2_ && node1_.id.equals( node2_.id ) ) {
                    this.flag = false;
                    return ;
                }
            }
        }

        QcNode sink = pi.getOnlySink();
        if ( sink == null ) {
            this.flag = false;
        }

        HashSet<QcNode> nextNodes = petri_cmp_top.getNextNodes( sink );

        if ( nextNodes.size() == 0 ) {
            paths.add( pi );
        }

        for (QcNode nextNode : nextNodes) {
            QcPetriNet pj = new QcPetriNet();
            pj.addSubPetri( pi );
            pj.addQcEdge( sink, nextNode );
            dfsFindHenSubPaths( pj, petri_cmp_top, paths );
        }
    }

    //Tested
    private void sortPaths( ArrayList<QcPetriNet> paths ) {
        Collections.sort( paths, new AdequateOrder_LPF() );
    }

    private void cleanPaths( ArrayList<QcPetriNet> paths ) {

        HashMap<String, Integer> RdescribedFlag = new HashMap<>();

        for (int i = 0; i < paths.size(); i++) {
            HashSet<QcNode> toBeDescribed = new HashSet<>();
            for (QcNode node_ : paths.get(i).nodes) {
                if (node_.shapeType == Common.Transition && RdescribedFlag.get(Common.getPrefix(node_.name)) == null) {

                    toBeDescribed.add(node_);

                    QcNode nextNode = paths.get(i).getNextOnlyNode(node_);
                    if (nextNode != null) {
                        toBeDescribed.add(nextNode);
                        if (i > 0) {
                            QcNode nextNextNode = paths.get(i).getNextOnlyNode(nextNode);
                            if (nextNextNode != null) {
                                toBeDescribed.add(nextNextNode);
                            }
                        }
                    }

                    QcNode lastNode = paths.get(i).getLastOnlyNode(node_);
                    if (lastNode != null) {
                        toBeDescribed.add(lastNode);
                        if (i > 0) {
                            QcNode lastLastNode = paths.get(i).getLastOnlyNode(lastNode);
                            if (lastLastNode != null) {
                                toBeDescribed.add(lastLastNode);
                            }
                        }
                    }
                }
            }

            QcPetriNet pi = new QcPetriNet();
            pi.addSubPetri(paths.get(i));
            for (QcNode node_ : paths.get(i).nodes) {
                if (toBeDescribed.contains(node_) == false) {
                    pi.removeQcNode(pi.getQcNodeById(node_.name));
                }
            }
            if( pi.nodes.size() > 0 ) {
                if (pi.getOnlySource().shapeType == Common.Transition) {
                    QcNode source = new QcNode("", "SuperSource", Common.Place, Common.NormalNode);
                    pi.addQcEdge(source, pi.getOnlySource());
                }
                if (pi.getOnlySink().shapeType == Common.Transition) {
                    QcNode sink = new QcNode("", "SuperSink", Common.Place, Common.NormalNode);
                    pi.addQcEdge(pi.getOnlySink(), sink);
                }
                paths.set(i, pi);

                for (QcNode node_ : paths.get(i).nodes) {
                    RdescribedFlag.put(Common.getPrefix(node_.name), 1);
                }
            } else {
                paths.set(i, pi);
            }
        }
    }
}

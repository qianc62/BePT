package qc;

import com.iise.bpplus.FileNameSelector;
import de.hpi.bpt.graph.algo.rpst.RPST;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.importing.pnml.PnmlImport;
import qc.common.Common;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

class PetriFeature{
    public QcPetriNet petri;

    public String path;
    public String name;
    public String type;

    public int nodeNum;
    public int placeNum;
    public int transitionNum;
    public int edgeNum;

    public int sourceNum;
    public int sinkNum;

//    public int componentNum;
//    public int componentTNum;
//    public int componentPNum;
//    public int componentBNum;
//    public int componentRNum;

    public int visibleTaskNum;
    public int invisibleTaskNum;
    public int duplicatedTaskNum;
    public int nonDuplicatedTaskNum;
//    public int cycleNum;

    public int memeNodeNum;
    public int seseNodeNum;
    public int semeNodeNum;
    public int meseNodeNum;

    public int rpstDepth;

    //    public int longestPathLen;
    //    public int shortestPathLen;

    //    public boolean linkedFlag;
    public boolean workflowFlag;
    public boolean structuredFlag;
//    public boolean loopFlag;
    public boolean selectedFlag;
//    public boolean safeFlag;
//    public boolean soundFlag;

    public PetriFeature() {
        this.petri = null;

        this.path = "";
        this.name = "";
        this.type = "";

        this.nodeNum = 0;
        this.placeNum = 0;
        this.transitionNum = 0;
        this.edgeNum = 0;

        this.sourceNum = 0;
        this.sinkNum = 0;

//        this.componentNum = 0;
//        this.componentTNum = 0;
//        this.componentPNum = 0;
//        this.componentBNum = 0;
//        this.componentRNum = 0;

        this.visibleTaskNum = 0;
        this.invisibleTaskNum = 0;
        this.duplicatedTaskNum = 0;
        this.nonDuplicatedTaskNum = 0;
//        this.cycleNum = 0;

        this.memeNodeNum = 0;
        this.seseNodeNum = 0;
        this.semeNodeNum = 0;
        this.meseNodeNum = 0;

        this.rpstDepth = 0;

        //        this.longestPathLen = 0;
        //        this.shortestPathLen = 0;

        //        this.linkedFlag = false;
        this.workflowFlag = false;
        this.structuredFlag = false;
//        this.loopFlag = false;
        this.selectedFlag = false;
//        this.safeFlag = false;
//        this.soundFlag = false;

    }

    public String toString(){
//        String str = "";
//        str += String.format( "%s(name) %d(nodeNum) %d(placeNum) %d(transitionNum) %d(edgeNum) %d(sourceNum) %d(sinkNum) ", name, nodeNum, placeNum, transitionNum, edgeNum, sourceNum, sinkNum );
//        str += String.format( "%d(visibleTaskNum) %d(invisibleTaskNum) %d(duplicatedTaskNum) %d(nonDuplicatedTaskNum) ", visibleTaskNum, invisibleTaskNum, duplicatedTaskNum, nonDuplicatedTaskNum );
//        str += String.format( "%d(memeNodeNum) %d(seseNodeNum) %d(semeNodeNum) %d(meseNodeNum) ", memeNodeNum, seseNodeNum, semeNodeNum, meseNodeNum );
//        str += String.format( "%d(rpstDepth) ", rpstDepth );
//        str += String.format( "%s(workflowFlag) %s(structuredFlag) %s(loopFlag) %s(selectedFlag) ", workflowFlag, structuredFlag, loopFlag, selectedFlag );
//        return str;

        String str = "";
        str += String.format( "%s %d %d %d %d %d %d ", name, nodeNum, placeNum, transitionNum, edgeNum, sourceNum, sinkNum );
        str += String.format( "%d %d %d %d ", visibleTaskNum, invisibleTaskNum, duplicatedTaskNum, nonDuplicatedTaskNum );
        str += String.format( "%d %d %d %d ", memeNodeNum, seseNodeNum, semeNodeNum, meseNodeNum );
        str += String.format( "%d ", rpstDepth );
        str += String.format( "%s %s %s ", workflowFlag, structuredFlag, selectedFlag );
        if ( str.contains("-1") ) {
            Common.printError("public String toString()");
        }
        return str;
    }
}

public class DataSet {
    public static void main(String[] args) throws Exception {
//        pickPetris( "model/dataSets/DG", 20000 );
//        pickPetris( "model/dataSets/SAP", 20000 );
//        pickPetris( "model/dataSets/TC", 20000 );
//        pickPetris( "model/dataSets/IBM", 200 );
//        pickPetris( "./dataSets/BAI", 200 );
//        pickPetris( "./dataSets/SPM", 200 );
//        pickPetris( "./dataSets/GPM", 200 );
//        pickPetris( "./dataSets/Test", 200 );

//        visualize( "./dataSets/DG" );
//        visualize( "./dataSets/SAP" );
//        visualize( "./dataSets/TC" );
//        visualize( "./dataSets/IBM" );
//        visualize( "./dataSets/BAI" );
//        visualize( "./dataSets/GPM" );
//        visualize( "./dataSets/SPM" );
//        visualize( "./dataSets/Test" );

        //特征统计
        ArrayList<PetriFeature> features = getFeatures( "./dataSets/Test" );
        for ( int i=0; i<features.size(); i++ ) {
            PetriFeature feature = features.get(i);
            System.out.format( "%d %s\n", i, feature );
        }

        HashMap<Integer,Integer> nodeNumMap = getNodeNumReversedIndex( features );
        for ( Integer int_:nodeNumMap.keySet() ) {
            Integer value = nodeNumMap.get( int_ );
            System.out.format( "%d %d\n", int_, value );
        }
    }

    //Tested
    public static void pickPetris( String oriPath, int max ) throws Exception{
        String[] str = oriPath.split("/");
        String datasetName = str[str.length-1];

        File folder = new File( oriPath );
        File[] models = folder.listFiles( new FileNameSelector( "pnml" ) );
        if ( models==null || models.length==0 ) {
            models = folder.listFiles( new FileNameSelector( "xml" ) );
        }

        if ( models==null || models.length==0 ) {
            return ;
        }

        shuffle( models );

        int K = 0;
        for( int i=0; i<models.length && K<max; i++ ){
            File model = models[i];
            String name = model.getName();
            String path = model.getAbsolutePath();
            System.out.println( name+"  "+path );

            File file = new File( oriPath+"/" + datasetName+K + ".pnml" );
            model.renameTo( file );
            K++;
        }
    }

    //Tested
    public static void shuffle( File[] models ){
        Random rand = new Random();

        int times = models.length * 2;
        while( times!=0 ){
            int i1 = rand.nextInt( models.length );
            int i2 = rand.nextInt( models.length );

            File tmp = models[i1];
            models[i1] = models[i2];
            models[i2] = tmp;

            times--;
        }
    }

    //Tested
    public static void visualize( String path ) throws Exception{

        File folder = new File( path );
        File[] models = folder.listFiles( new FileNameSelector( "pnml" ) );

        for( int i=0; i<models.length; i++ ){
            File model = models[i];
            String filePath = model.getAbsolutePath();
            QcPetriNet petri = new QcPetriNet( filePath );
            petri.print();

            String[] strs = filePath.split("/");
            String imageName = strs[strs.length-1];
            imageName = imageName.substring(0,imageName.indexOf(".pnml"))+"_ori.png";
            strs[strs.length-1] = imageName;
            String imagePath = "";
            for (int j = 0; j < strs.length; j++) {
                if ( strs[j].length()>0 ) {
                    imagePath += "/"+strs[j];
                }
            }
            petri.saveImage( imagePath );
        }
    }

    //Tested
    public static ArrayList<PetriFeature> getFeatures( String path ) throws Exception{

        ArrayList<PetriFeature> features = new ArrayList<>();

        File folder = new File( path );
        File[] models = folder.listFiles( new FileNameSelector( "pnml" ) );

        for( int i=0; i<models.length; i++ ){

            File modelFile = models[i];
            QcPetriNet qcPetri = new QcPetriNet( modelFile.getAbsolutePath() );

            PetriFeature feature = new PetriFeature();

            if ( qcPetri.getSources().size()>1 ) {
                HashSet<QcNode> nodes = qcPetri.getSources();
                for ( QcNode node_: nodes ) {
                    if ( node_.shapeType.equals(Common.Transition) ) {
                        QcNode place = new QcNode( "", "AddedPlace", Common.Place, Common.NormalNode );
                        qcPetri.addQcEdge( place, node_ );
                    }
                }
                QcNode superPlaceSource = new QcNode( "", "AddedPlace", Common.Place, Common.NormalNode );
                QcNode superTransitionSource = new QcNode( "", "AddedTransition", Common.Transition, Common.NormalNode );
                feature.invisibleTaskNum++;
                nodes = qcPetri.getSources();
                for ( QcNode node_: nodes ) {
                    qcPetri.addQcEdge( superPlaceSource, superTransitionSource );
                    qcPetri.addQcEdge( superTransitionSource, node_ );
                }
            }
            if ( qcPetri.getSinks().size()>1 ) {
                HashSet<QcNode> nodes = qcPetri.getSinks();
                for ( QcNode node_: nodes ) {
                    if ( node_.shapeType.equals(Common.Transition) ) {
                        QcNode place = new QcNode( "", "AddedTransition", Common.Place, Common.NormalNode );
                        qcPetri.addQcEdge( node_, place );
                    }
                }
                QcNode superPlaceSink = new QcNode( "", "AddedPlace", Common.Place, Common.NormalNode );
                QcNode superTransitionSink = new QcNode( "", "AddedTransition", Common.Transition, Common.NormalNode );
                feature.invisibleTaskNum++;
                nodes = qcPetri.getSinks();
                for ( QcNode node_: nodes ) {
                    qcPetri.addQcEdge( node_, superTransitionSink );
                    qcPetri.addQcEdge( superTransitionSink, superPlaceSink );
                }
            }

            feature.petri = qcPetri;
            feature.path = modelFile.getAbsolutePath();
            feature.name = modelFile.getName();
            feature.type = "Academic";

            feature.nodeNum = qcPetri.nodes.size();
            feature.placeNum = qcPetri.getPlaces().size();
            feature.transitionNum = qcPetri.getTransitions().size();
            feature.edgeNum = qcPetri.edges.size();

            feature.sourceNum = qcPetri.getSources().size();
            feature.sinkNum = qcPetri.getSinks().size();

            if ( feature.sourceNum==1 && feature.sinkNum==1 ) {
                if ( qcPetri.getOnlySource().shapeType==Common.Place && qcPetri.getOnlySink().shapeType==Common.Place ) {
                    feature.workflowFlag = true;
                    feature.selectedFlag = true;
                }
            }
            if ( qcPetri.isStructured()==true ) {
                feature.structuredFlag = true;
            }

            feature.visibleTaskNum = qcPetri.getNumberOfVisibleTasks();
            feature.invisibleTaskNum = qcPetri.getNumberOfInvisibleTasks();
            feature.duplicatedTaskNum = qcPetri.getNumberOfDuplicateTasks();
            feature.nonDuplicatedTaskNum = qcPetri.getNumberOfNonDuplicateTasks();

            for ( QcNode node: qcPetri.nodes ) {
                int lastNodesNum = qcPetri.getLastNodes( node ).size();
                int nextNodesNum = qcPetri.getNextNodes( node ).size();
                if ( lastNodesNum==1 && nextNodesNum==1 ) {
                    feature.seseNodeNum++;
                }
                if ( lastNodesNum>1 && nextNodesNum>1 ) {
                    feature.memeNodeNum++;
                }
                if ( lastNodesNum==1 && nextNodesNum>1 ) {
                    feature.meseNodeNum++;
                }
                if ( lastNodesNum>1 && nextNodesNum==1 ) {
                    feature.semeNodeNum++;
                }
            }

            RPST rpst = qcPetri.getRPST();
            feature.rpstDepth = rpst.maxDepth( rpst.getRoot() );

            features.add( feature );
        }

        return features;
    }

    //Tested
    public static HashMap<Integer,Integer> getNodeNumReversedIndex( ArrayList<PetriFeature> features ){
        HashMap<Integer,Integer> map = new HashMap<>();

        for ( int i=0; i<features.size(); i++ ) {
            PetriFeature feature = features.get(i);
            if ( map.get(feature.nodeNum)==null ) {
                map.put( feature.nodeNum, 1 );
            } else {
                map.put( feature.nodeNum, map.get(feature.nodeNum)+1 );
            }
        }

        return map;
    }
}

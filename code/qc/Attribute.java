package qc;

import de.hpi.bpt.graph.algo.rpst.RPST;
import qc.common.Common;

import java.util.ArrayList;
import java.util.HashMap;

public class Attribute {

    public QcPetriNet petri;
    public RPST rpst;
    public QcTreeNode rootNode;
    public String string;

    //时间性能
    public long startTime;
    public long endTime;

//    文本
    public int wordNum;
    public int sentenceNum;
    public HashMap<String,Integer> depthMap = null;
    public HashMap<String,Integer> goldDepthMap = null;

    //信息增益
    public ArrayList<ArrayList<String>> trivials = null;
    public ArrayList<Double> gains = null;

    ////    覆盖率
//    public double nodeCoverage;
//    public double tarCoverage;
//    public double behaviorCoverage;
//
////    比例
//    public double wrongBehaviorGenerationRatio;
//    public double wrongBehaviorDescriptionRatio;

    public Attribute(){
        this.petri = null;
        this.rpst = null;

        this.rootNode = null;
        this.string = "";

        this.startTime = 0;
        this.endTime = 0;

        this.wordNum = 0;
        this.sentenceNum = 0;
        this.depthMap = new HashMap<>();
        this.goldDepthMap = new HashMap<>();

        this.trivials = new ArrayList<>();
        this.gains = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format( "%d", endTime - startTime );
    }

    public void print( String str ){
        Common.printLine( '>', str );

        if ( this.string.contains("Generation Failed.") ) {
            System.out.println( "Text:" );
            System.out.println( "Generation Failed." );
            return ;
        }

        System.out.println( "Text:" );
        System.out.println( this.string );

        System.out.println();
        System.out.format( "Word Number: %d\n", this.wordNum );
        System.out.format( "Sentence Number: %d\n", this.sentenceNum );

//        System.out.println();
//        System.out.println( "Depth Distribution:" );
//        for ( String key: depthMap.keySet() ) {
//            int value = depthMap.get( key );
//            System.out.format( "'%s' depth: %d\n", key, value );
//        }

        int mapDis = 0;
        System.out.println();
        System.out.println( "GoldDepth Distribution:" );
        for ( String key: goldDepthMap.keySet() ) {
            int goldValue = goldDepthMap.get( key );
            if ( depthMap.get( key )!=null ) {
                int textValue = depthMap.get( key );
                System.out.format( "'%s' goldDepth:%d textDepth:%d\n", key, goldValue, textValue );
                mapDis += Math.abs( goldValue - textValue );
            } else {
                System.out.format( "'%s' goldDepth:%d textDepth:***\n", key, goldValue );
            }
        }
        System.out.format( "Distance:%d\n", mapDis );

        System.out.println();
        System.out.println( "Trivials: " );
        for ( int i=0; i<trivials.size(); i++ ) {
            for ( int j=0; j<trivials.get(i).size(); j++ ) {
                System.out.print( "<" + trivials.get(i).get(j) + "> " );
            }
//            System.out.format( "%.2f\n", gains.get(i) );
            System.out.println();
        }

        System.out.println();
        System.out.println( "Gains: " );
        for ( int i=0; i<gains.size(); i++ ) {
            System.out.format( "%.2f  ", gains.get(i) );
        }
        System.out.println();
        double sum = 0.0;
        for ( int i=0; i<gains.size(); i++ ) {
            sum += gains.get(i);
            System.out.format( "%.2f  ", sum );
        }
        System.out.println();

        System.out.println();
        System.out.println( "Time: " + this + "ms" );

        Common.printLine( '<', str );
    }

    public void caculate(){
        String text = this.string;
        while( text.indexOf("  ") != -1 ){
            text = text.replaceAll( "  ", " " );
        }
        this.wordNum = text.split(" ").length;

        for ( int i=0; i<this.string.length(); i++ ) {
            char ch = this.string.charAt( i );
            if ( ch=='.' || ch==':' ) {
                this.sentenceNum++;
            }
        }

        this.rpst.getDepthMap( this.rpst.getRoot(), 0, this.petri, goldDepthMap );

        this.trivials = this.rootNode.getTrivialLabels();
        this.computeGains();
    }

    private void computeGains(){
        HashMap<String,Integer> orderRelationMap = new HashMap<>();
        for (int i = 0; i < this.trivials.size(); i++) {
            ArrayList<String> lableList = this.trivials.get(i);

            int pGain;
            int tGain = 0;
            int eGain = 0;

            for (int j = 0; j < lableList.size(); j++) {
                String nLabel = lableList.get(j);

                tGain++;

                if (j + 1 < lableList.size()) {
                    String eLabel = nLabel + lableList.get(j+1);
                    if (orderRelationMap.get(eLabel) == null) {
                        eGain += 2;
                    }
                }
            }

            pGain = tGain + 1;
            eGain += 2;

            double gain = computeGain(pGain, tGain, eGain);
            this.gains.add(gain);

            for (int j = 0; j < lableList.size(); j++) {
                String label = lableList.get(j);
                orderRelationMap.put(label, 1);
                if (j + 1 < lableList.size()) {
                    orderRelationMap.put(lableList.get(j) + lableList.get(j + 1), 1);
                }
            }
        }
    }

    private double computeGain( int pGain, int tGain, int eGain ) {
        int sum = pGain + tGain + eGain;
        double p1 = pGain * 1.0 / sum;
        double p2 = tGain * 1.0 / sum;
        double p3 = eGain * 1.0 / sum;
        double entropy = - ( p1*QcMath.log(2.0,p1) + p2*QcMath.log(2.0,p2) + p3*QcMath.log(2.0,p3) );
        double entropy_ = QcMath.exp( 2.0, entropy );
        return entropy_ * sum;
    }
}

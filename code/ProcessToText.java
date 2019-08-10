
import com.iise.bpplus.FileNameSelector;
import de.hpi.bpt.graph.algo.rpst.RPST;
import qc.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

class ProcessToText {

    public static String LeoTemplatePath = "./Leo_templates.xml";
    public static String HenTemplatePath = "./Hen_templates.xml";
    public static String GounTemplatePath = "./Goun_templates.xml";
    public static String NewTemplatePath = "./New_templates.xml";

    public static int datasetNum = 0;

    public static int Leo_Word = 0;
    public static int Leo_Sentence = 0;
    public static int Leo_Node = 0;

    public static int Hen_Word = 0;
    public static int Hen_Sentence = 0;
    public static int Hen_Node = 0;

    public static int Goun_Word = 0;
    public static int Goun_Sentence = 0;
    public static int Goun_Node = 0;

    public static int Bepa_Word = 0;
    public static int Bepa_Sentence = 0;
    public static int Bepa_Node = 0;

    public static int Total_Word = 0;
    public static int Total_Sentence = 0;
    public static int Total_Node = 0;

    public static double Leo_CC = 0.0f;
    public static double Hen_CC = 0.0f;
    public static double Goun_CC = 0.0f;
    public static double Bepa_CC = 0.0f;
    public static double Total_CC = 0.0f;

    public static void main( String[] args ) throws Exception {

        String directory = "dataSets/SPM/";
        String fileName = "DG25";
        String format = "pnml";

//        singleGenerate( directory, fileName, format );
        batchGenerate( directory, format );

//        System.out.format( "Leo %.2f %.2f %.2f\n", Leo_Word*1.0/datasetNum, Leo_Sentence*1.0/datasetNum , Leo_Node*1.0/datasetNum );
//        System.out.format( "Hen %.2f %.2f %.2f\n", Hen_Word*1.0/datasetNum, Hen_Sentence*1.0/datasetNum , Hen_Node*1.0/datasetNum );
//        System.out.format( "Goun %.2f %.2f %.2f\n", Goun_Word*1.0/datasetNum, Goun_Sentence*1.0/datasetNum , Goun_Node*1.0/datasetNum );
//        System.out.format( "Bepa %.2f %.2f %.2f\n", Bepa_Word*1.0/datasetNum, Bepa_Sentence*1.0/datasetNum , Bepa_Node*1.0/datasetNum );
//        System.out.format( "Total %.2f %.2f %.2f\n", Total_Word*1.0/(datasetNum*4), Total_Sentence*1.0/(datasetNum*4) , Total_Node*1.0/(datasetNum*4) );

        System.out.format( "%.2f %.2f %.2f %.2f %.2f\n", Leo_CC*1.0/datasetNum, Hen_CC*1.0/datasetNum, Goun_CC*1.0/datasetNum, Bepa_CC*1.0/datasetNum, Total_CC*1.0/(datasetNum*4.0));
    }

    public static void batchGenerate( String dirPath, String format ) throws Exception{

        File folder = new File( dirPath );
        File[] models = folder.listFiles( new FileNameSelector( format ) );

        for( int i=0; i<models.length; i++ ){
            File model = models[i];
            String name = model.getName().split( "."+format )[0];
            System.out.println( name );
            singleGenerate( dirPath, name, format );
            datasetNum++;
        }
    }

    public static void singleGenerate( String directory, String fileName, String format ) throws Exception {
        QcPetriNet petri = new QcPetriNet( directory + fileName + "." + format );
//        petri.print();
        QcPetriNet workflow = petri.workflowFy();
//        workflow.print();
//        petri.saveImage( directory + fileName + "." + "png" );

//        QcPetriNet cpu = petri.getCPU();
        RPST rpst = workflow.getRPST();
//        rpst.print( rpst.getRoot(), 0 );

//        通过Leo算法生成文本
        LeoTextGenerator textGenerator_Leo = new LeoTextGenerator( LeoTemplatePath );
        Attribute attribute_Leo = textGenerator_Leo.generateText( workflow, 0 );

//        通过Hen算法生成文本
        HenTextGenerator textGenerator_Hen = new HenTextGenerator( HenTemplatePath );
        Attribute attribute_Hen = textGenerator_Hen.generateText( workflow, 0 );

//        通过Goun算法生成文本
        GounTextGenerator textGenerator_Goun = new GounTextGenerator( HenTemplatePath );
        Attribute attribute_Goun = textGenerator_Goun.generateText( workflow, 0 );

//        通过New算法生成文本
        QcTextGenerator textGenerator_New = new QcTextGenerator( NewTemplatePath );
        Attribute attribute_New = textGenerator_New.generateText( workflow, 0 );

//        输出三类属性信息
//        Common.printEnter(6);
//        attribute_Hen.print( "Hen Results" );
//        attribute_Goun.print( "Goun Results" );
//        attribute_New.print( "New Results" );

//        存储三类属性信息
        saveInformation( workflow, rpst, attribute_Leo, attribute_Hen, attribute_Goun, attribute_New, directory+fileName );
    }

    public static void saveInformation( QcPetriNet petri, RPST rpst, Attribute attribute_Leo, Attribute attribute_Hen, Attribute attribute_Goun, Attribute attribute_New, String dirName ) throws Exception {

        File directory = new File( dirName );
        if ( directory.exists() ) {
            directory.delete();
        }
        directory.mkdir();

        {
            String petriPath = dirName + "/petri.txt";
            String str = "";
            BufferedWriter writer = new BufferedWriter( new FileWriter( new File( petriPath ) ) );
            for ( QcNode node: petri.nodes ) {
                str += String.format( "%s %s %s\n", node.shapeType, node.id, node.name );
            }
            str += "\n";
            for ( QcEdge edge: petri.edges ) {
                str += String.format( "%s -> %s\n", edge.source.id, edge.target.id );
            }
            writer.write( str );
            writer.close();
        }
        {
            String petriPath = dirName + "/rpst.txt";
            BufferedWriter writer = new BufferedWriter( new FileWriter( new File( petriPath ) ) );
            rpst.dfsRpstWrite( rpst.getRoot(), 0, writer );
            writer.close();
        }

        saveStatistics( dirName, attribute_Leo,  "Leo" );
        saveStatistics( dirName, attribute_Hen,  "Hen" );
        saveStatistics( dirName, attribute_Goun, "Goun" );
        saveStatistics( dirName, attribute_New,  "New" );
    }

    public static void saveStatistics( String dirName, Attribute attribute, String alName ) throws Exception {
        String filePrefix = dirName + "/" + alName;

        BufferedWriter algorithm_writer = new BufferedWriter( new FileWriter( new File( filePrefix+"_algorithm.txt" ) ) );
        BufferedWriter text_writer = new BufferedWriter(new FileWriter(new File( filePrefix+"_text.txt") ));
        BufferedWriter model_text_writer = new BufferedWriter(new FileWriter(new File( filePrefix+"_model+text.txt") ));

        //算法级
        algorithm_writer.write( String.format("StartTime: %d\n\n", attribute.startTime ) );
        algorithm_writer.write( String.format("EndTime: %d\n\n", attribute.endTime ) );
        algorithm_writer.write( String.format("DuationTime: %d\n\n", attribute.endTime-attribute.startTime ) );

        //文本级
        text_writer.write( String.format("String: %s\n\n", attribute.string ) );
        if ( attribute.string.contains("Generation Failed.") == false ) {
            //文本树
            BufferedWriter writer = new BufferedWriter( new FileWriter( new File( filePrefix+"_QcTreeNode.txt" ) ) );
            attribute.rootNode.dfsWrite( 0, writer );
            writer.close();

            //词数
            text_writer.write( String.format("WordNumber: %d\n\n", attribute.wordNum ) );

            //句数
            text_writer.write( String.format("SentenceNumber: %d\n\n", attribute.sentenceNum ) );

            //深度分布及相关系数
            ArrayList<Double> goldDepthArray = new ArrayList<Double>();
            ArrayList<Double> depthArray = new ArrayList<Double>();
            QcMath.getTwoArray( attribute.goldDepthMap, attribute.depthMap, goldDepthArray, depthArray );

            text_writer.write( String.format("Gold Depth: ") );
            for ( double item: goldDepthArray ) {
                text_writer.write( String.format("%d ",(int)item) );
            }
            text_writer.write( "\n\n" );

            text_writer.write( String.format("Depth: ") );
            for ( double item: depthArray ) {
                text_writer.write( String.format("%d ",(int)item) );
            }
            text_writer.write( "\n\n" );

            double p = QcMath.getCorrelationCoefficient( goldDepthArray, depthArray );
            text_writer.write( String.format("Correlation Coefficient: %.2f\n\n", p) );

            //信息增益
            text_writer.write( String.format("Sentence Gain: ") );
            for ( int i=0; i<attribute.gains.size(); i++ ) {
                text_writer.write( String.format("%.2f ",attribute.gains.get(i)) );
            }
            text_writer.write( "\n\n" );

            double sum = 0.0;
            text_writer.write( String.format("Sentence Sum Gain: ") );
            for ( int i=0; i<attribute.gains.size(); i++ ) {
                sum += attribute.gains.get(i);
                text_writer.write( String.format("%.2f ", sum) );
            }
            text_writer.write( "\n\n" );

            if ( alName == "Leo" ) {
                Leo_CC += p;
            } else if ( alName == "Hen" ) {
                Hen_CC += p;
            }  else if ( alName == "Goun" ) {
                Goun_CC += p;
            }  else if ( alName == "New" ) {
                Bepa_CC += p;
            }
            Total_CC += p;
        }

        algorithm_writer.close();
        text_writer.close();
        model_text_writer.close();

        if ( alName == "Leo" ) {
            Leo_Word += attribute.wordNum;
            Leo_Sentence += attribute.sentenceNum;
            Leo_Node += attribute.petri.nodes.size();
        } else if ( alName == "Hen" ) {
            Hen_Word += attribute.wordNum;
            Hen_Sentence += attribute.sentenceNum;
            Hen_Node += attribute.petri.nodes.size();
        }  else if ( alName == "Goun" ) {
            Goun_Word += attribute.wordNum;
            Goun_Sentence += attribute.sentenceNum;
            Goun_Node += attribute.petri.nodes.size();
        }  else if ( alName == "New" ) {
            Bepa_Word += attribute.wordNum;
            Bepa_Sentence += attribute.sentenceNum;
            Bepa_Node += attribute.petri.nodes.size();
        }

        Total_Word += attribute.wordNum;
        Total_Sentence += attribute.sentenceNum;
        Total_Node += attribute.petri.nodes.size();

        /*
    //信息增益
    ArrayList<ArrayList<String>> trivials = null;
    ArrayList<Double> gains = null;
        * */
    }


}

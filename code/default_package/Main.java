package default_package;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import net.didion.jwnl.JWNLException;
import sentencePlanning.DiscourseMarker;
import sentencePlanning.ReferringExpressionGenerator;
import sentencePlanning.SentenceAggregator;
import sentenceRealization.SurfaceRealizer;
import textPlanning.TextPlanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;

import contentDetermination.labelAnalysis.EnglishLabelDeriver;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import dataModel.dsynt.DSynTSentence;
import dataModel.jsonReader.JSONReader;
import dataModel.jsonStructure.Doc;
import dataModel.process.Arc;
import dataModel.process.EPSF;
import dataModel.process.Element;
import dataModel.process.Event;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.algo.rpst.RPST;


public class Main {
	
	public static int TEXT  = 0;
	public static int DSYNTS = 1;
	public static int EPSF = 2;
	
	/**
	 * Main function. 
	 */
	public static void main(String[] args) throws Exception{

//		File file1 = new File( "/Users/qianchen/Downloads/JavaWorkspace/ProcessToText2/WordNet-3.0/dict\index.sense" );
//		//System.out.println( file1.exists() );

		//输入控制：
		String file = null;
		Scanner scanner = new Scanner( System.in );
		System.out.println( "请输入工程顶层文件夹内的json格式文件名.如:\"bpmn1.json\"" );
		while( ( file = scanner.nextLine() ) != null ){
			if( file.contains( ".json" ) ){
				File filePointer = new File( file );
				if( filePointer.exists() == true ){
					break;
				}
				else{
					System.out.println( "文件不存在，请输入工程顶层文件夹内的json格式文件名.如:\"bpmn1.json\"" );
				}
			}
			else{
				System.out.println( "格式错误，请输入工程顶层文件夹内的json格式文件名.如:\"bpmn1.json\"" );
			}
		}
		
		// Set up label parsing classes
		TextPlanner.lHelper = new EnglishLabelHelper();
		TextPlanner.lDeriver  = new EnglishLabelDeriver(TextPlanner.lHelper);
		
		//实验次数
		ExpTime.times = 1;
		ExpTime.startTime = System.currentTimeMillis();
		
		for( int i=1 ; i<=ExpTime.times ; i++ ){
			String string = ( String )createFromFile( file , TEXT );
			System.out.println( "\n\n" + string );
		}
		
		ExpTime.endTime = System.currentTimeMillis();
		////System.out.println( ExpTime.startTime );
		////System.out.println( ExpTime.endTime );
		ExpTime.runTime += ExpTime.endTime - ExpTime.startTime;
		////System.out.println( ExpTime.endTime - ExpTime.startTime );
		System.out.println( "平均开销：" +  ExpTime.runTime * 1.0 / ExpTime.times );
	}
	
	/**
	 * Loads JSON files from directory and writes generated texts 
	 */
	public static Object createFromFile( String file , int option ) throws JsonSyntaxException, IOException {
		
		JSONReader reader = new JSONReader();
		Gson gson = new Gson();
		int counter = 0;
		
		Doc modelDoc = gson.fromJson(reader.getJSONStringFromFile(file), Doc.class);
//		modelDoc.print();
		
		if (modelDoc.getChildShapes() != null) {
			try {
				reader.init();
				reader.getIntermediateProcessFromFile(modelDoc);
				ProcessModel model = reader.getProcessModelFromIntermediate();
				
//				model.print();
				
				EPSF epsf = new EPSF( );
				
				epsf.addProcessModels( model );
				
				epsf.initialModels( );
				
				epsf.annotateModels( 0  );
				
				epsf.createSubprocessRPSTs( );
				
				epsf.createAlternativeRPSTs( );
				
				epsf.addMessageFlows( model );
				
//				epsf.print();
				
				if( option == TEXT ){
					String str = "";
					switch( model.getPools().size() ){
						case 1: str = "\nThe model contains 1 pool: "; break;
						default : str = "\nThe model contains " + model.getPools().size() +" pools: "; 
					}
					
					int count = 1;
					for ( String role : model.getPools() ) {
						if( count > 1 ){
							str += " and ";
						}
						str += role + " (" + count + ")";
						count++;
					}
					return str + toText( epsf , option );
				}
				else if( option == DSYNTS ){
					return toText( epsf , option );
				}
				else if( option == EPSF ){
					return epsf;
				}
				else{
					//System.out.println( "参数错误" );
					return new Object();
				}
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return new Object();
	}

	/**
	 *  Function for generating text from a model. The according process model must be provided to the function.
	 */
	public static Object toText( EPSF epsf , int option ) throws JWNLException, IOException, ClassNotFoundException, DocumentException {
		
		String surfaceTexts = "";
		ArrayList <DSynTSentence> sentencePlans = new ArrayList <DSynTSentence>(); 
		
		for( Integer id : epsf.getRPSTs().keySet() ){

			RPST rpst = epsf.getRPSTs().get(id);
			ProcessModel model = epsf.getModels().get(id);
//			rpst.qc_print(rpst, rpst.getRoot(), 0);
			
			String surfaceText = "";
			
			// Convert to Text
			TextPlanner converter = new TextPlanner( model ,epsf, rpst, "" , false , false);
			converter.convertToTextOnlySkeleton(rpst.getRoot(), 0);
			
			//DSyn-T Message Generation
			ArrayList <DSynTSentence> sentencePlan = converter.getSentencePlan();
		
			//Message Refinement
				// Aggregation
				SentenceAggregator sentenceAggregator = new SentenceAggregator(TextPlanner.lHelper);
				sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan, model);
				
				// Referring Expression
				ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(TextPlanner.lHelper);
				sentencePlan  = refExpGenerator.insertReferringExpressions(sentencePlan, model, false);
				
				// Discourse Marker 
				DiscourseMarker discourseMarker = new DiscourseMarker();
				sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);
			
			if( option == TEXT ){
				SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
				surfaceText =  surfaceRealizer.realizePlan(sentencePlan);
				surfaceText = surfaceText.replaceAll( "The process" , "The " + model.getPools().get( 0 ) + " process" );
				surfaceText = surfaceRealizer.postProcessText(surfaceText);
				surfaceTexts += surfaceText;
			}
			else if( option == DSYNTS ){
				sentencePlans.addAll( sentencePlan );
			}
		}
		
		surfaceTexts += convertMessageFlows( epsf , option );
		
		if( option == TEXT ){
			return surfaceTexts;
		}
		else if( option == DSYNTS ){
			return sentencePlans;
		}
		
		return new Object();
	}
	
	public static String convertMessageFlows( EPSF epsf , int option ) throws JWNLException, JsonSyntaxException, IOException{
		String string = "";
		HashMap<Integer,Arc> messageFlows = epsf.getMessageFlows();
		
		switch( messageFlows.size() ){
			case 0: string +=  ""; break;
			case 1: string +=  "\nIn the meantime , there is 1 message that should be transmitted and received between these pools.\n"; break;
			default: string +=  "\nIn the meantime , there are " + epsf.getMessageFlows().size() + " messages that should be transmitted and received between these pools.\n"; break;
		}
		
		if( messageFlows.size() > 0 ){
			Arc[] objs = new Arc[messageFlows.size()];
			
			messageFlows.values().toArray( objs );
			
			for( int i=0 ; i < objs.length ; i++ ){
				int p = Math.min( objs[i].getSource().getId() , objs[i].getTarget().getId() );
				int seNum = p , index = i;
				for( int j=i+1 ; j<objs.length ; j++ ){
					seNum = Math.min( objs[j].getSource().getId() , objs[j].getTarget().getId() );
					index = j;
				}
				if( seNum < p ){
					Arc arc = objs[i];
					objs[i] = objs[index];
					objs[index] = arc;
				}
			}
		
			for( Arc mFlow : objs ){
	
				Element source = mFlow.getSource();
				Element target = mFlow.getTarget();
				
				String sourceString = source.getDescriptionString();
				String targetString = target.getDescriptionString();
				
				//System.out.println( sourceString );
				
				sourceString = sanitate( sourceString );
				targetString = sanitate( targetString );
				
				if( source instanceof Event ){
					string += "After " + source.getLane().getName().toLowerCase() + " sends messages, " +  "then " + target.getLane().getName().toLowerCase() + " reveives these messages and continues processing.\n";
				}else{
					string += "After " + sourceString + ", it sends messages to " + target.getLane().getName().toLowerCase() + ", then " + targetString + ".\n";
				}
			}
		}
		
		return string;
	}
	
	public static String sanitate( String string ){
		
		int begin = 0 , end = 0;
		
		string = string.toLowerCase();
		
		string = string.replaceAll( "[^a-zA-Z]" , " " );
		
		string = string.replaceAll( "the process begins when" , " " );
		
		for( int i=0 ; i<string.length() ; i++ ){
			char ch = string.charAt( i );
			if( ch >= 'a' && ch <= 'z' ){
				begin = i;
				break;
			}
		}
		for( int i=string.length()-1 ; i>=0 ; i-- ){
			char ch = string.charAt( i );
			if( ch >= 'a' && ch <= 'z' ){
				end = i;
				break;
			}
		}
		
		if( end > begin ){
			string = string.substring( begin , end + 1 );
		}
		else{
			string = "";
		}
		
		return string;
	}
}
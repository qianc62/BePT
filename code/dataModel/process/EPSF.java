package dataModel.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//import org.apache.bcel.classfile.PMGClass;
import org.jbpt.petri.unfolding.order.McMillanAdequateOrder;

import com.google.gson.JsonSyntaxException;

import contentDetermination.labelAnalysis.EnglishLabelCategorizer;
import contentDetermination.labelAnalysis.EnglishLabelDeriver;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import contentDetermination.labelAnalysis.EnglishLabelProperties;
import dataModel.dsynt.DSynTSentence;
import dataModel.process.*;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import default_package.Main;
import preprocessing.FormatConverter;
import textPlanning.PlanningHelper;
import textPlanning.TextPlanner;

public class EPSF {

	int id;
	static int idNum = 99999;
	
	//单个泳道的过程模型集
	private HashMap<Integer,ProcessModel> processModels;
	private HashMap<Integer,ProcessModel> alternativeModels;

	//单个泳道的RPST集
	private HashMap<Integer, RPST> rpsts;
	private HashMap<Integer, RPST> alternativeRpsts;
	
	//信息流
	private HashMap<Integer,Arc> sequenceFlows;
	
	public EPSF(){
		processModels = new HashMap<Integer,ProcessModel>();
		sequenceFlows = new HashMap<Integer,Arc>();
		rpsts = new HashMap<Integer, RPST>();
		alternativeModels = new HashMap<Integer,ProcessModel>();
		alternativeRpsts = new HashMap<Integer, RPST>();
		id = idNum--;
	}
	
	public int getID(){
		return id;
	}
	
	public HashMap<Integer, ProcessModel> getModels(){
		return processModels;
	}
	
	public HashMap<Integer, RPST> getRPSTs(){
		return rpsts;
	}
	
	public HashMap<Integer,Arc> getSequenceFlow(){
		return sequenceFlows;
	}
	
	public void addProcessModels( ProcessModel models ){

		HashMap<Integer,ProcessModel> newModels = models.getModelForEachPool();
		
		for( ProcessModel model : newModels.values() ){
			model.normalize();
			model.normalizeEndEvents();
			processModels.put( model.getId() , model );
		}
		
		for( Activity activity : models.getActivites().values() ){
			for( Integer attID : activity.getAttachedEvents() ){
				ProcessModel model0 = newModels.get( activity.getPool().getId() );
				ProcessModel alternativeModel = models.getAlternativePaths().get( attID );
				model0.addAlternativePath( alternativeModel , attID );
				alternativeModels.put( attID , alternativeModel );
			}
		}
	}
	
	public void addRPST( RPST rpst ){
		if( rpsts == null ){
			rpsts = new HashMap<Integer, RPST>();
		}
		rpsts.put( Integer.valueOf( rpst.getId() ) , rpst );
	}
	
	public void addSequenceFlow( Arc flow ){
		sequenceFlows.put( Integer.valueOf( flow.getId() ) , flow );
	}

	public void initialModels( ){
		
		for( ProcessModel model : processModels.values() ){
		
			Object[] activities = model.getActivites().values().toArray();
			
			for( int i=0 ; i<activities.length ; i++ ){
				Activity activity = ( Activity )activities[i];
				activity.setOriginalLabel( activity.getLabel() );
				if( activity.getType() == 2 ){
					activity.setLabel( "continue with a fold subprocess" );
					//System.out.println( activity.getLabel() );
				}
			}
		}
	}
	
	public void annotateModels( int option ) {
		
		for( ProcessModel model : processModels.values() ){
		
			HashMap<Integer, Activity> activities = model.getActivites();
			HashMap<Integer, Event> events = model.getEvents();
			HashMap<Integer, Gateway> gateways = model.getGateways();
			
			////System.out.println(activities.size() + "\t" + events.size() + "\t" + gateways.size());
			
			EnglishLabelCategorizer lC = new EnglishLabelCategorizer(TextPlanner.lHelper.getDictionary(), TextPlanner.lHelper, TextPlanner.lDeriver);
			ArrayList<contentDetermination.labelAnalysis.structure.Activity> modela = new ArrayList<contentDetermination.labelAnalysis.structure.Activity>();
			
			for (Activity a: activities.values()) {
				EnglishLabelProperties props = new EnglishLabelProperties();
					try {
						String label = a.getLabel().toLowerCase().replaceAll("\n", " ");
						label = label.replaceAll("  ", " ");
						
						if (label.contains("glossary://")) {
							label = label.replace("glossary://", "");
							label = label.substring(label.indexOf("/")+1,label.length());
							label = label.replace(";;", "");
						}
						
						String[] labelSplit = label.split(" ");
						
						contentDetermination.labelAnalysis.structure.Activity act = new contentDetermination.labelAnalysis.structure.Activity(label, label, "",modela);
						TextPlanner.lDeriver.deriveFromVOS(a.getLabel(), labelSplit, props);
					
						Annotation anno = new Annotation();
						
						// No Conjunction label
						if (props.hasConjunction() == false) {
							
							// If no verb-object label
							if (TextPlanner.lHelper.isVerb(labelSplit[0]) == false) {
								anno.addAction("conduct");
								anno.addBusinessObjects(a.getLabel().toLowerCase());
								a.addAnnotation(anno);
							
							// If verb-object label
							} else {
								anno.addAction(props.getAction());
								String bo = props.getBusinessObject();
								if (bo.startsWith("the ")) {
									bo = bo.replace("the ", "");
								}
								if (bo.startsWith("an ")) {
									bo = bo.replace("an ", "");
								}
								anno.addBusinessObjects((bo));
								String add = props.getAdditionalInfo();
								String[] splitAdd = add.split(" "); 
								if (splitAdd.length > 2 && splitAdd[1].equals("the")) {
									add = add.replace("the ", "");
								}
								anno.setAddition(add);
								a.addAnnotation(anno);
							}
						// Conjunction label	
						} else {
							for (String action: props.getMultipleActions()) {
								anno.addAction(action);
							}
							for (String bo: props.getMultipleBOs()) {
								String temp = bo;
								if (temp.startsWith("the ")) {
									temp = temp.replace("the ", "");
								}
								if (temp.startsWith("an ")) {
									temp = temp.replace("an ", "");
								}
								anno.addBusinessObjects(temp);
							}
							anno.setAddition("");
							a.addAnnotation(anno);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	public void createSubprocessRPSTs( ){
		for( ProcessModel model : processModels.values() ){
			
			Object[] activities = model.getActivites().values().toArray();
			
			for( int i=0 ; i<activities.length ; i++ ){
				Activity activity = ( Activity )activities[i];
				
				if( activity.getType() == 2 ){
					try {
						EPSF subEpsf = (EPSF)Main.createFromFile( activity.getOriginalLabel() + ".json" , 2 );
						for( RPST rpst : subEpsf.getRPSTs().values() ){
							alternativeRpsts.put( subEpsf.getID() , rpst );
						}
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		}
	}
	
	public void createAlternativeRPSTs( ){

		for( ProcessModel model : processModels.values() ){
			FormatConverter formatConverter = new FormatConverter();
			Process p = formatConverter.transformToRPSTFormat( model );
			RPST<ControlFlow,Node> rpst = new RPST<>( p );
			rpsts.put( model.getId() , rpst );
			
			for( ProcessModel model1 : model.getAlternativePaths().values() ){
				Process p1 = formatConverter.transformToRPSTFormat( model1 );
				RPST<ControlFlow,Node> rpst1 = new RPST<ControlFlow,Node>( p1 );
				alternativeRpsts.put( model1.getId() , rpst1 );
			}
		}
	}
	
	public void addMessageFlows( ProcessModel model ){
		for ( Arc arc: model.getArcs().values() ) {
			//System.out.println("Arc: (s: " + arc.getSource().getId() + " t: " + arc.getTarget().getId() + ")" + "- " + arc.getId() + " " +  arc.getLabel());
			if( arc.getType().equals( "MessageFlow" ) ){
				sequenceFlows.put( arc.getId() , arc );
			}
		}
	}

	public HashMap<Integer,Arc> getMessageFlows(){
		return sequenceFlows;
	}
	
	public void setAlternativeModels( HashMap<Integer,ProcessModel> almodels ){
		alternativeModels = almodels;
	}
	
	public HashMap<Integer,ProcessModel> getAlternativeModels(){
		return alternativeModels;
	}
	
	public void setAlternativeRPSTs( HashMap<Integer,RPST> alrpsts ){
		alternativeRpsts = alrpsts;
	}
	
	public HashMap<Integer,RPST> getAlternativeRPSTs(){
		return alternativeRpsts;
	}
	
	public void print( ){
//		private HashMap<Integer,ProcessModel> processModels;
//		private HashMap<Integer,ProcessModel> alternativeModels;
//
//		//单个泳道的RPST集
//		private HashMap<Integer, RPST> rpsts;
//		private HashMap<Integer, RPST> alternativeRpsts;
//		
//		//信息流
//		private HashMap<Integer,Arc> sequenceFlows;
		
		int poolNum = 0;
		int laneNum = 0;
		int eventNum = 0;
		int activityNum = 0;
		int gatewayNum = 0;
		int sequenceFlowNum = 0;
		
		for( Integer id : processModels.keySet() ){
			ProcessModel pm = processModels.get( id );
			poolNum += pm.getPools().size();
			laneNum += pm.getLanes().size();
			eventNum += pm.getEvents().size();
			activityNum += pm.getActivites().size();
			gatewayNum += pm.getGateways().size();
			sequenceFlowNum += pm.getArcs().size();
		}
		
		for( Integer id : alternativeModels.keySet() ){
			ProcessModel pm = alternativeModels.get( id );
			poolNum += pm.getPools().size();
			laneNum += pm.getLanes().size();
			eventNum += pm.getEvents().size();
			activityNum += pm.getActivites().size();
			gatewayNum += pm.getGateways().size();
			sequenceFlowNum += pm.getArcs().size();
		}
		
		//System.out.println( "Pool " + poolNum );
		//System.out.println( "Lane " + laneNum );
		//System.out.println( "Node " + ( eventNum + activityNum + gatewayNum ) );
		//System.out.println( "Event " + eventNum );
		//System.out.println( "Activity " + activityNum );
		//System.out.println( "Gateway " + gatewayNum );
		//System.out.println( "Sequence-Flow " + sequenceFlowNum );
	}
}

package textPlanning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.chrono.ThaiBuddhistDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xerces.impl.xpath.regex.Match;
import org.jbpt.bp.sim.ExtendedInterleavingSimilarity;
import org.jbpt.petri.untangling.ReductionBasedRepresentativeUntangling;
//import org.junit.validator.PublicClassValidator;

//import com.gnu.hcode.in_c;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.SplitCharacter;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.list.PointerTargetTreeNodeList.FindNodeOperation;
import preprocessing.FormatConverter;
import qc.QcPrintDsynT;
import sentencePlanning.*;
import sentenceRealization.SurfaceRealizer;
import templates.TemplateLoader;
import textPlanning.recordClasses.ConverterRecord;
import textPlanning.recordClasses.GatewayPropertyRecord;
import textPlanning.recordClasses.ModifierRecord;
import contentDetermination.extraction.GatewayExtractor;
import contentDetermination.labelAnalysis.EnglishLabelDeriver;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import dataModel.Pair;
import dataModel.dsynt.DSynTConditionSentence;
import dataModel.dsynt.DSynTMainSentence;
import dataModel.dsynt.DSynTSentence;
import dataModel.intermediate.AbstractFragment;
import dataModel.intermediate.ConditionFragment;
import dataModel.intermediate.ExecutableFragment;
import dataModel.jsonReader.JSONReader;
import dataModel.jsonStructure.Doc;
import dataModel.process.Activity;
import dataModel.process.Annotation;
import dataModel.process.Arc;
import dataModel.process.Artifact;
import dataModel.process.ArtifactType;
import dataModel.process.Data;
import dataModel.process.Direction;
import dataModel.process.EPSF;
import dataModel.process.Element;
import dataModel.process.Event;
import dataModel.process.EventType;
import dataModel.process.Gateway;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.GatewayType;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import default_package.Main;
import edu.stanford.nlp.util.IntPair;
import ee.ut.bpstruct2.RestrictedRestructurerVisitor;

public class TextPlanner {
	
	private EPSF epsf;
	private RPST<ControlFlow,Node> rpst;
	private ProcessModel process;
	
	private TextToIntermediateConverter textToIMConverter;
	private ArrayList <ConditionFragment> passedFragments;
	private ModifierRecord passedMod = null; // used for AND-Splits
	private ArrayList<ModifierRecord> passedMods; // used for Skips 

	private boolean tagWithBullet = false;
	private boolean start = true;
	private boolean end = false;
	private boolean isAlternative = false;
	private int isolatedXORCount = 0;
	
	private ArrayList<DSynTSentence> sentencePlan;
	private ArrayList<Pair<Integer,DSynTSentence>> activitiySentenceMap;
	public static EnglishLabelHelper lHelper;
	public static EnglishLabelDeriver lDeriver;
	
	static String[] quantifiers = {"a", "the", "all", "any", "more", "most", "none", "some", "such", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
	
	private boolean imperative;
	private String imperativeRole;
	
	public static int SPLIT = 0;
	public static int JOIN = 1;
	RPSTNode<ControlFlow,Node> savedOriginalNode = null;
	
	public TextPlanner(ProcessModel process_, EPSF epsf_, RPST<ControlFlow,Node> rpst_, String imperativeRole, boolean imperative, boolean isAlternative) throws FileNotFoundException, JWNLException {
		this.process = process_;
		this.epsf = epsf_;
		this.rpst = rpst_;
		
		this.lHelper = lHelper;
		this.lDeriver = lDeriver;
		textToIMConverter = new TextToIntermediateConverter(rpst, process, lHelper, imperativeRole, imperative);
		passedFragments = new ArrayList<ConditionFragment>();
		sentencePlan = new ArrayList<DSynTSentence>();
		activitiySentenceMap = new ArrayList<Pair<Integer,DSynTSentence>>();
		passedMods = new ArrayList<ModifierRecord>();
		this.imperative = imperative;
		this.imperativeRole = imperativeRole;
		this.isAlternative = isAlternative;
	}

	//By QC
	public void convertToTextOnlySkeleton(RPSTNode<ControlFlow, Node> root, int level) throws JWNLException, FileNotFoundException {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);

		for (RPSTNode<ControlFlow,Node> node: orderedTopNodes) {

			if ((PlanningHelper.isEvent(node.getExit()) && orderedTopNodes.indexOf(node) == orderedTopNodes.size()-1)) {
				end = true;
			} else {
				end = false;
			}

			int depth = PlanningHelper.getDepth(node, rpst);

			if (PlanningHelper.isBond(node)) {
				System.out.print( "Bond Structure" );

				// Converter Record
				ConverterRecord convRecord = null;

				//**************************************  LOOP - SPLIT  **************************************
				if (PlanningHelper.isLoop(node,rpst)) {
					System.out.print( "( Loop )" );
					convRecord = getLoopConverterRecord(node);
				}
				//**************************************  SKIP - SPLIT  **************************************
				if (PlanningHelper.isSkip(node,rpst)) {
					System.out.print( "( Skip )" );
					convRecord = getSkipConverterRecord(orderedTopNodes, node);
				}
				//**************************************  XOR - SPLIT  **************************************
				if (PlanningHelper.isXORSplit(node, rpst)) {
					System.out.print( "( XORSplit )" );
					convRecord = getXORConverterRecord(node);
				}
				//**************************************  EVENT BASED - SPLIT  **************************************
				if (PlanningHelper.isEventSplit(node, rpst)) {
					//convRecord = getXORConverterRecord(node);
					//By Chen Qian
					System.out.print( "( EventSplit )" );
					convRecord = getEventSplitConverterRecord(node);
				}
				//**************************************  OR - SPLIT  **************************************
				if (PlanningHelper.isORSplit(node, rpst)) {
					System.out.print( "( ORSplit )" );
					convRecord = getORConverterRecord(node);
				}
				//**************************************  AND - SPLIT  **************************************
				if (PlanningHelper.isANDSplit(node, rpst)) {
					System.out.print( "( ANDSplit )" );
					convRecord = getANDConverterRecord(node);
				}

				if (PlanningHelper.isLoop(node,rpst) || PlanningHelper.isSkip(node,rpst)) {
					convertToText(node, level);
				}
				if (PlanningHelper.isXORSplit(node,rpst) || PlanningHelper.isORSplit(node, rpst) || PlanningHelper.isEventSplit(node, rpst)) {
//					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
//					for (RPSTNode<ControlFlow, Node> path: paths) {
//						tagWithBullet = true;
//						convertToText(path, level+1);
//					}

					//qc
					Object[] objects = rpst.getChildren( node ).toArray();
					for( int i=0 ; i<objects.length ; i++ ){
						RPSTNode<ControlFlow, Node> child = (RPSTNode<ControlFlow, Node>)objects[i];
						tagWithBullet = true;
						convertToText(child, level+1);
					}
				}
				if (PlanningHelper.isANDSplit(node,rpst)) {

//					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
//					for (RPSTNode<ControlFlow, Node> path: paths) {
//						tagWithBullet = true;
//						convertToText(path, level+1);
//					}

					//qc
					Object[] objects = rpst.getChildren( node ).toArray();
					for( int i=0 ; i<objects.length ; i++ ){
						RPSTNode<ControlFlow, Node> child = (RPSTNode<ControlFlow, Node>)objects[i];
						tagWithBullet = true;
						convertToText(child, level+1);
					}
				}

				// Add post statement to sentence plan
				if (convRecord != null && convRecord.postStatements != null) {
					for (DSynTSentence postStatement: convRecord.postStatements) {
						postStatement.getExecutableFragment().sen_level = level;
						sentencePlan.add(postStatement);
						qc.QcPrintDsynT.print( postStatement );//By Chen Qian
					}
				}

				// Pass post fragment
				if (convRecord != null && convRecord.post != null) {
					//输出B型子结构的"聚合"信息
					passedFragments.add(convRecord.post);
				}

				//**************************************  RIGIDS *******************************************
			} else if (PlanningHelper.isRigid(node)){
				System.out.println( "Rigid Structure" );

				TemplateLoader loader = new TemplateLoader();
				loader.loadTemplate(TemplateLoader.RIGID);

				ExecutableFragment eFrag = new ExecutableFragment(loader.getAction(),loader.getAddition() , loader.getObject(),"");
				eFrag.bo_hasIndefArticle = true;
				eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
				sentencePlan.add(new DSynTMainSentence(eFrag));
				qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag) );

				//idMap每次运行结果不一定相等，得到Gateway Graph
				GatewayGraph gatewayGraph = new GatewayGraph( process, epsf, rpst, node );

				//创建可变模型
				gatewayGraph.createChangedGG();

				//矫正
				gatewayGraph.correctLoopEdges();

				//多入多出网关分裂
				gatewayGraph.SplitGateway( );

				//目标配对算法
				gatewayGraph.matchAll( );

				//展开成结构化模型，并记录重复和剔除分支
				gatewayGraph.unfoldAndDescribe();
				gatewayGraph.rProcessModel.print();

				//矫正反边及合并网关
				gatewayGraph.createBPMNIdMap( );
				gatewayGraph.postProcessing( );

				//生成文本
				gatewayGraph.toText( );
				sentencePlan.addAll( gatewayGraph.rConverter.getSentencePlan() );

				//补充重边和漏边
				ArrayList<Arc> arcss = new ArrayList<Arc>();
				getArcs( node, arcss );
				gatewayGraph.recordOtherInformation( arcss );
				sentencePlan.addAll( gatewayGraph.describeOtherInformation() );

			} else if (PlanningHelper.isTask(node.getEntry())) {

				Activity activity = (Activity) process.getActivity(Integer.parseInt(node.getEntry().getId()));

				System.out.println( activity.getId() );
			}
			//**************************************  EVENTS  **************************************
//			 else if (PlanningHelper.isEvent(node.getEntry()) && orderedTopNodes.indexOf(node) > 0) {
			else if (PlanningHelper.isEvent(node.getEntry())) {
				System.out.println( "Event" );

			} else {
				if (depth > 0) {
					//递归解决问题
					convertToText(node, level);
				}
			}
			if(end){
				System.out.println( "End Event" );
			}
		}

		if( orderedTopNodes.size() == 0 && PlanningHelper.isRigid(root) == true ){

			TemplateLoader loader = new TemplateLoader();
			loader.loadTemplate(TemplateLoader.RIGID);

			ExecutableFragment eFrag = new ExecutableFragment(loader.getAction(),loader.getAddition() , loader.getObject(),"");
			eFrag.bo_hasIndefArticle = true;
			eFrag.addAssociation(Integer.valueOf(root.getEntry().getId()));
			sentencePlan.add(new DSynTMainSentence(eFrag));
			qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag) );

			//idMap每次运行结果不一定相等，得到Gateway Graph
			GatewayGraph gatewayGraph = new GatewayGraph( process, epsf, rpst, root );

			//创建可变模型
			gatewayGraph.createChangedGG();

			//矫正
			gatewayGraph.correctLoopEdges();

			//多入多出网关分裂
			gatewayGraph.SplitGateway( );

			//目标配对算法
			gatewayGraph.matchAll( );

			//展开成结构化模型，并记录重复和剔除分支
			gatewayGraph.unfoldAndDescribe();
			gatewayGraph.rProcessModel.print();

			//矫正反边及合并网关
			gatewayGraph.createBPMNIdMap( );
			gatewayGraph.postProcessing( );

			//生成文本
			gatewayGraph.toText( );
			sentencePlan.addAll( gatewayGraph.rConverter.getSentencePlan() );

			//补充重边和漏边
			ArrayList<Arc> arcss = new ArrayList<Arc>();
			getArcs( root, arcss );
			gatewayGraph.recordOtherInformation( arcss );
			sentencePlan.addAll( gatewayGraph.describeOtherInformation() );
		}
	}

	/**
	 * Text Planning Main 
	 * @throws FileNotFoundException 
	 */
	public void convertToText(RPSTNode<ControlFlow, Node> root, int level) throws JWNLException, FileNotFoundException {
		// Order nodes of current level with respect to control flow
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);

		//qc
		for (RPSTNode<ControlFlow,Node> node: orderedTopNodes) {
			//////System.out.println( node.getEntry().getId() + "----->>" + node.getExit().getId() + " " + PlanningHelper.getDepth(node, rpst) );
		}
		//////System.out.println( );
		
		// For each node of current level
		for (RPSTNode<ControlFlow,Node> node: orderedTopNodes) {
			
			//qc
			//System.out.print( node.getEntry().getId() + ">>----->>" + node.getExit().getId() + "  " );
			
			// If we face an end event
			if ((PlanningHelper.isEvent(node.getExit()) && orderedTopNodes.indexOf(node) == orderedTopNodes.size()-1)) {
				end = true;
			} else {
				end = false;
			}
			
			int depth = PlanningHelper.getDepth(node, rpst);
			
			//////System.out.println( "end : " + end + "  depth:" + depth );
			
 			if (PlanningHelper.isBond(node)) {
				System.out.println( "Bond Stricture" );

				// Converter Record
				ConverterRecord convRecord = null;

				//**************************************  LOOP - SPLIT  **************************************
				if (PlanningHelper.isLoop(node,rpst)) {
					convRecord = getLoopConverterRecord(node);
				}	
				//**************************************  SKIP - SPLIT  **************************************
				if (PlanningHelper.isSkip(node,rpst)) {
					convRecord = getSkipConverterRecord(orderedTopNodes, node);
				}	
				//**************************************  XOR - SPLIT  **************************************
				if (PlanningHelper.isXORSplit(node, rpst)) {
					convRecord = getXORConverterRecord(node);
				}
				//**************************************  EVENT BASED - SPLIT  **************************************
				if (PlanningHelper.isEventSplit(node, rpst)) {
					//convRecord = getXORConverterRecord(node);
					//By Chen Qian
					convRecord = getEventSplitConverterRecord(node);
				}
				//**************************************  OR - SPLIT  **************************************
				if (PlanningHelper.isORSplit(node, rpst)) {
					convRecord = getORConverterRecord(node);
				}
				//**************************************  AND - SPLIT  **************************************
				if (PlanningHelper.isANDSplit(node, rpst)) {
					convRecord = getANDConverterRecord(node);
				}	
				
				//不能直接continue，可能passedFragments不为空
//				if( convRecord == null ){
//					continue;
//				}
				
				// Add pre statements 
				if (convRecord != null && convRecord.preStatements != null) {
					for (DSynTSentence preStatement: convRecord.preStatements) {
						//输出passedFragments文本
						if (passedFragments.size()>0) {
							if(tagWithBullet){
								preStatement.getExecutableFragment().sen_hasBullet = true;
								preStatement.getExecutableFragment().sen_level = level;
								passedFragments.get(0).sen_hasBullet = true;
								passedFragments.get(0).sen_level = level;
								tagWithBullet = false;
							}
							DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(preStatement.getExecutableFragment(), passedFragments.get(0));
							if (passedFragments.size() > 1) {
								for (int i = 1; i < passedFragments.size(); i++) {
									dsyntSentence.addCondition(passedFragments.get(i), true);
									dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
								}
							}
							passedFragments.clear();
							sentencePlan.add(dsyntSentence);
							qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
						} else {
							//没有passedFragments情况
							//输出B型子结构的"分裂"信息
							if(tagWithBullet){
								preStatement.getExecutableFragment().sen_hasBullet = true;
								preStatement.getExecutableFragment().sen_level = level;
								tagWithBullet = false;
							}
							preStatement.getExecutableFragment().sen_level = level;
							if (passedMods.size() > 0 ) {
								preStatement.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
								preStatement.getExecutableFragment().sen_hasConnective = true;
								passedMods.clear();
							}
							sentencePlan.add(new DSynTMainSentence(preStatement.getExecutableFragment()));
							qc.QcPrintDsynT.print( new DSynTMainSentence(preStatement.getExecutableFragment()));//By Chen Qian
						}
					}
				}
				
				// Pass precondition
				if (convRecord != null && convRecord.pre != null) {
					if (passedFragments.size() > 0) {
						if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
							ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
							eFrag.bo_isSubject = true;
							DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
							sentencePlan.add(dsyntSentence);
							qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
							passedFragments.clear();
						}
					}
					passedFragments.add(convRecord.pre);
				}
				
				// ################ Convert to Text #################
				if (PlanningHelper.isLoop(node,rpst) || PlanningHelper.isSkip(node,rpst)) {
					convertToText(node, level);
				}
				if (PlanningHelper.isXORSplit(node,rpst) || PlanningHelper.isORSplit(node, rpst) || PlanningHelper.isEventSplit(node, rpst)) {
//					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
//					for (RPSTNode<ControlFlow, Node> path: paths) {
//						tagWithBullet = true;
//						convertToText(path, level+1);
//					}
					
					//qc
					Object[] objects = rpst.getChildren( node ).toArray();
					for( int i=0 ; i<objects.length ; i++ ){
						RPSTNode<ControlFlow, Node> child = (RPSTNode<ControlFlow, Node>)objects[i];
						tagWithBullet = true;
						convertToText(child, level+1);
					}
				}
				if (PlanningHelper.isANDSplit(node,rpst)) {
					
//					ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
//					for (RPSTNode<ControlFlow, Node> path: paths) {
//						tagWithBullet = true;
//						convertToText(path, level+1);
//					}
					
					//qc
					Object[] objects = rpst.getChildren( node ).toArray();
					for( int i=0 ; i<objects.length ; i++ ){
						RPSTNode<ControlFlow, Node> child = (RPSTNode<ControlFlow, Node>)objects[i];
						tagWithBullet = true;
						convertToText(child, level+1);
					}
				}
				
				// Add post statement to sentence plan
				if (convRecord != null && convRecord.postStatements != null) {
					for (DSynTSentence postStatement: convRecord.postStatements) {
						postStatement.getExecutableFragment().sen_level = level;
						sentencePlan.add(postStatement);
						qc.QcPrintDsynT.print( postStatement );//By Chen Qian
					}
				}
					
				// Pass post fragment
				if (convRecord != null && convRecord.post != null) {
					//输出B型子结构的"聚合"信息
					passedFragments.add(convRecord.post);
				}
			
			//**************************************  RIGIDS *******************************************
			} else if (PlanningHelper.isRigid(node)){
				System.out.println( "Rigid Stricture" );

				TemplateLoader loader = new TemplateLoader();
				loader.loadTemplate(TemplateLoader.RIGID);
				
				ExecutableFragment eFrag = new ExecutableFragment(loader.getAction(),loader.getAddition() , loader.getObject(),"");
				eFrag.bo_hasIndefArticle = true;
				eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
				sentencePlan.add(new DSynTMainSentence(eFrag));
				qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag) );
				
				//idMap每次运行结果不一定相等，得到Gateway Graph
				GatewayGraph gatewayGraph = new GatewayGraph( process, epsf, rpst, node );

				//创建可变模型
				gatewayGraph.createChangedGG();
				
				//矫正
				gatewayGraph.correctLoopEdges();

				//多入多出网关分裂
				gatewayGraph.SplitGateway( );
				
				//目标配对算法
				gatewayGraph.matchAll( );
				
				//展开成结构化模型，并记录重复和剔除分支
				gatewayGraph.unfoldAndDescribe();
				gatewayGraph.rProcessModel.print();
				
				//矫正反边及合并网关
				gatewayGraph.createBPMNIdMap( );
				gatewayGraph.postProcessing( );
				
				//生成文本
				gatewayGraph.toText( );
				sentencePlan.addAll( gatewayGraph.rConverter.getSentencePlan() );
				
				//补充重边和漏边
				ArrayList<Arc> arcss = new ArrayList<Arc>();
				getArcs( node, arcss );
				gatewayGraph.recordOtherInformation( arcss );
				sentencePlan.addAll( gatewayGraph.describeOtherInformation() );
					
			} else if (PlanningHelper.isTask(node.getEntry())) {

				Activity activity = (Activity) process.getActivity(Integer.parseInt(node.getEntry().getId()));

				System.out.println( activity );

				try {
					convertActivities(node, level, depth);
				} catch ( Exception e) {
					e.printStackTrace();
				}
				
				// Handle End Event
				if (PlanningHelper.isEvent(node.getExit())) {
					end = false;
					Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
					if (event.getType() == EventType.END_EVENT && orderedTopNodes.indexOf(node) == orderedTopNodes.size()-1) {
						// Adjust level and add to sentence plan
						DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
						sen.getExecutableFragment().sen_level = level;
						if (event.getSubProcessID() > 0) {
							sen.getExecutableFragment().sen_level = level+1;
						}
						sentencePlan.add(sen);
						event.setDescriptionString( qc.QcPrintDsynT.print( sen ) );//By Chen Qian
					}
					else{
						//end = true;
					}
				}
			} 
			//**************************************  EVENTS  **************************************	
//			 else if (PlanningHelper.isEvent(node.getEntry()) && orderedTopNodes.indexOf(node) > 0) {
			 else if (PlanningHelper.isEvent(node.getEntry())) {
				System.out.println( "Event" );

				Event event = process.getEvents().get((Integer.valueOf(node.getEntry().getId())));
				int currentPosition = orderedTopNodes.indexOf(node);
				// Start Event
				if (currentPosition == 0) {
					
					// Start event should be printed
					if (start == true && isAlternative == false) {
						
						// Event is followed by gateway --> full sentence
						if (event.getType() == EventType.START_EVENT && currentPosition < orderedTopNodes.size()-1 && PlanningHelper.isBond(orderedTopNodes.get(currentPosition+1))) {
							start = false;
							ExecutableFragment eFrag = new ExecutableFragment("start", "process", "", "with a decision");
							eFrag.add_hasArticle = false;
							eFrag.bo_isSubject = true;
							sentencePlan.add(new DSynTMainSentence(eFrag));
							event.setDescriptionString( qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag) ) );//By Chen Qian
						}
						if (event.getType() != EventType.START_EVENT) {
							start = false;
							ConverterRecord convRecord = textToIMConverter.convertEvent(event);
							if (convRecord != null && convRecord.hasPreStatements() == true) {
								sentencePlan.add(convRecord.preStatements.get(0));
								event.setDescriptionString( qc.QcPrintDsynT.print( convRecord.preStatements.get(0) ) );//By Chen Qian
							}
						}
					}
					
					
				// Intermediate Events	
				} else {
					ConverterRecord convRecord = textToIMConverter.convertEvent(event);
					
					// Add fragments if applicable
					if (convRecord != null && convRecord.pre != null) {
						passedFragments.add(convRecord.pre);
					}
					
					// Adjust level and add to sentence plan (first sentence not indented)
					if (convRecord != null && convRecord.hasPreStatements() == true) {
						for (int i = 0; i <convRecord.preStatements.size(); i++) {
							
							DSynTSentence sen = convRecord.preStatements.get(i);
							
							//qc
//							ArrayList <DSynTSentence> sentencePlan1 = new ArrayList <DSynTSentence>();
//							sentencePlan1.add( sen );
//							SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
//							//////System.out.println( surfaceRealizer.realizePlan(sentencePlan1) );
							
							// If only one sentence (e.g. "Intermediate" End Event)
							if (convRecord.preStatements.size() == 1) {
								sen.getExecutableFragment().sen_level = level;
							}
							
							if (tagWithBullet == true) {
								sen.getExecutableFragment().sen_hasBullet = true;
								sen.getExecutableFragment().sen_level = level;
								tagWithBullet = false;
							}

							if (i>0) {
								sen.getExecutableFragment().sen_level = level;
							}
							if (event.getSubProcessID() > 0) {
								sen.getExecutableFragment().sen_level = level+1;
							}
							
							if (passedMods.size() > 0 ) {
								String mod = passedMods.get(0).getLemma();
								if (mod.equals("alternatively,") && sen.getExecutableFragment().sen_hasBullet) {
									passedMods.clear();
								} else {
									sen.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
									sen.getExecutableFragment().sen_hasConnective = true;
									passedMods.clear();
								}
							}
							
							if (passedFragments.size() > 0 ) {
								DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), passedFragments.get(0));
								if (passedFragments.size() > 1) {
									for (i = 1; i < passedFragments.size(); i++) {
										dsyntSentence.addCondition(passedFragments.get(i), true);
										dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
									}
								}
								sentencePlan.add(dsyntSentence);
								event.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
								passedFragments.clear();
							} else {
								if (sen.getClass().toString().endsWith("DSynTConditionSentence")) {
									DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), ((DSynTConditionSentence) sen).getConditionFragment());
									sentencePlan.add(dsyntSentence);
									event.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
								} else {
									DSynTMainSentence dsyntSentence = new DSynTMainSentence(sen.getExecutableFragment());
									sentencePlan.add(dsyntSentence);
									event.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
								}
							}
						}
					}
				}
			} else {
				if (depth > 0) {
					//递归解决问题
					convertToText(node, level);
				}
			}	
			if(end){
				Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
				
				ExecutableFragment eFragment = textToIMConverter.convertEvent(event).preStatements.get(0).getExecutableFragment();
				if (passedFragments.size()>0) {
					if(tagWithBullet){
						eFragment.sen_hasBullet = true;
						eFragment.sen_level = level;
						passedFragments.get(0).sen_hasBullet = true;
						passedFragments.get(0).sen_level = level;
						tagWithBullet = false;
					}
					DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFragment, passedFragments.get(0));
					if (passedFragments.size() > 1) {
						for (int i = 1; i < passedFragments.size(); i++) {
							dsyntSentence.addCondition(passedFragments.get(i), true);
							dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
						}
					}
					passedFragments.clear();
					sentencePlan.add(dsyntSentence);
					qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
				}
			}
		}
		
		if( orderedTopNodes.size() == 0 && PlanningHelper.isRigid(root) == true ){
				
			TemplateLoader loader = new TemplateLoader();
			loader.loadTemplate(TemplateLoader.RIGID);
			
			ExecutableFragment eFrag = new ExecutableFragment(loader.getAction(),loader.getAddition() , loader.getObject(),"");
			eFrag.bo_hasIndefArticle = true;
			eFrag.addAssociation(Integer.valueOf(root.getEntry().getId()));
			sentencePlan.add(new DSynTMainSentence(eFrag));
			qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag) );
			
			//idMap每次运行结果不一定相等，得到Gateway Graph
			GatewayGraph gatewayGraph = new GatewayGraph( process, epsf, rpst, root );

			//创建可变模型
			gatewayGraph.createChangedGG();
			
			//矫正
			gatewayGraph.correctLoopEdges();

			//多入多出网关分裂
			gatewayGraph.SplitGateway( );
			
			//目标配对算法
			gatewayGraph.matchAll( );
			
			//展开成结构化模型，并记录重复和剔除分支
			gatewayGraph.unfoldAndDescribe();
			gatewayGraph.rProcessModel.print();
			
			//矫正反边及合并网关
			gatewayGraph.createBPMNIdMap( );
			gatewayGraph.postProcessing( );
			
			//生成文本
			gatewayGraph.toText( );
			sentencePlan.addAll( gatewayGraph.rConverter.getSentencePlan() );
			
			//补充重边和漏边
			ArrayList<Arc> arcss = new ArrayList<Arc>();
			getArcs( root, arcss );
			gatewayGraph.recordOtherInformation( arcss );
			sentencePlan.addAll( gatewayGraph.describeOtherInformation() );
		}
	}

	private void getArcs(RPSTNode<ControlFlow, Node> node, ArrayList<Arc> arcss) {
		
		Object[] childs = rpst.getChildren( node ).toArray();
		
		if( childs.length == 0 ){
			int u = Integer.valueOf( node.getEntry().getId() );
			int v = Integer.valueOf( node.getExit().getId() );
			arcss.add( getArc( u , v ) );
		}else{
			for( int i=0 ; i<childs.length ; i++ ){
				RPSTNode<ControlFlow, Node> child = ((RPSTNode<ControlFlow, Node>)childs[i]);
				getArcs( child , arcss );
			}
		}
	}
	
	public Arc getArc( int u, int v ) {
		
		for( Arc arc : process.getArcs().values() ){
			if( arc.getSource().getId() == u && arc.getTarget().getId() == v ){
				return arc;
			}
		}
		
		return null;
	}

	private void convertIsolatedRigidActivity(int id, int prevId, int level) {
		
		Activity currActivity = (Activity) process.getActivity(id);
		Activity prevActivity = (Activity) process.getActivity(prevId);
		Annotation currAnno = currActivity.getAnnotations().get(0);
		Annotation prevAnno = prevActivity.getAnnotations().get(0);
	
		
		ExecutableFragment eFrag = null;
		String modLemma = "after " + prevAnno.getActions().get(0) + "ing " + prevAnno.getBusinessObjects().get(0);
		ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
		modRecord.addAttribute("adv-type", "sentential");
				
		eFrag = new ExecutableFragment("may", "also "  + currAnno.getActions().get(0) + " the " + currAnno.getBusinessObjects().get(0), "", "");
		String role = getRole(currActivity, eFrag);
		eFrag.setRole(role);
		eFrag.bo_hasArticle = false;
		eFrag.sen_hasBullet = true;
		eFrag.sen_level = level +1;
		eFrag.addMod(modLemma, modRecord);
		
		DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
		sentencePlan.add(dsyntSentence);
		qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
	}
	
	private void convertRigidEndActivity(int id, int level) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment("may", "also end with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			eFrag.bo_hasArticle = false;
			eFrag.sen_hasBullet = true;
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
		} 
	}
	
	private void convertRigidStartActivity(int id, int level) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment("may", "also begin with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			eFrag.bo_hasArticle = false;
			eFrag.sen_hasBullet = true;
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
		} else {
		}
	}
	
	private void convertRigidElement(int id, int level, boolean first) {
		Activity activity = (Activity) process.getActivity(id);
		if (activity != null) {
			Annotation anno1 = activity.getAnnotations().get(0);
			
			ExecutableFragment eFrag = null;
			eFrag = new ExecutableFragment(anno1.getActions().get(0), anno1.getBusinessObjects().get(0), "", anno1.getAddition());
			eFrag.addAssociation(activity.getId());
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			
			if (first) {
				eFrag.sen_hasBullet = true;
			}
			eFrag.sen_level = level +1;
			
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
		} else {
		}
	}

	private void convertActivities(RPSTNode<ControlFlow, Node> node, int level, int depth ) throws JWNLException, JsonSyntaxException, IOException {
		
		boolean planned = false;
		
		Activity activity = (Activity) process.getActivity(Integer.parseInt(node.getEntry().getId()));
		activity.setNlgNum( );
		
		Annotation anno = activity.getAnnotations().get(0);
		ExecutableFragment eFrag = null;
		
		ConditionFragment cFrag = null;
		
		// Start of the process
		if (start == true && isAlternative == false) {
			start = false;
			ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
			modRecord.addAttribute("starting_point", "+");
			
			String bo = anno.getBusinessObjects().get(0);

			eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition() + activity.getAddtion() );
			
			eFrag.addAssociation(activity.getId());
			eFrag.addMod("the process begins when", modRecord);
			
			String role = getRole(activity, eFrag);
			eFrag.setRole(role);
			if (anno.getActions().size() == 2) {
				ExecutableFragment eFrag2 = null;
				if (anno.getBusinessObjects().size() == 2) {
					eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
					eFrag2.addAssociation(activity.getId());
				} else {
					eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
					eFrag2.addAssociation(activity.getId());
				}
				
				correctArticleSettings(eFrag2);
				eFrag.addSentence(eFrag2);
			}
			
			if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0,bo.length()-1))) {
				eFrag.bo_hasArticle = true;
			} else {
				eFrag.bo_hasIndefArticle = true;
			}
			
			// If imperative mode
			if (imperative == true && imperativeRole.equals(role) == true) {
				eFrag.verb_isImperative = true;
				eFrag.role_isImperative = true;
			}
			correctArticleSettings(eFrag);
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			activity.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
			planned = true;
		} 
		
		// Standard case
		eFrag = new ExecutableFragment(anno.getActions().get(0), anno.getBusinessObjects().get(0), "", anno.getAddition()+activity.getAddtion() );
		eFrag.addAssociation(activity.getId());
		String role = getRole(activity, eFrag);
		eFrag.setRole(role);
		if (anno.getActions().size() == 2) {
			ExecutableFragment eFrag2 = null;
			if (anno.getBusinessObjects().size() == 2) {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
				if (eFrag.verb_IsPassive == true) {
					if (anno.getBusinessObjects().get(0).equals("") == true) {
						eFrag2.verb_IsPassive = true;
						eFrag.setBo(eFrag2.getBo());
						eFrag2.setBo("");
						eFrag.bo_hasArticle = true;
					} else {
						eFrag2.verb_IsPassive = true;
						eFrag2.bo_isSubject = true;
					}
					
				}
			} else {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
				if (eFrag.verb_IsPassive == true) {
					eFrag2.verb_IsPassive = true;
				}
			}
			
			correctArticleSettings(eFrag2);
			eFrag2.addAssociation(activity.getId());
			eFrag.addSentence(eFrag2);
		}
		
		eFrag.sen_level = level;
		if (imperative == true && imperativeRole.equals(role) == true) {
			correctArticleSettings(eFrag);	
			eFrag.verb_isImperative = true;
			eFrag.setRole("");
		}
		if (activity.getSubProcessID() > 0) {
			eFrag.sen_level = level+1;
		}
		
		// In case of passed modifications (NOT AND - Split) 
		if (passedMods.size() > 0 && planned == false) {
			correctArticleSettings(eFrag);	
			eFrag.addMod(passedMods.get(0).getLemma(), passedMods.get(0));	
			eFrag.sen_hasConnective = true;
			passedMods.clear();
		}
			
		// In case of passed modifications (e.g. AND - Split) 
		if (passedMod != null && planned == false){
			correctArticleSettings(eFrag);
			eFrag.addMod(passedMod.getLemma(), passedMod);	
			eFrag.sen_hasConnective = true;
			passedMod = null;
		}	
			
		if (tagWithBullet == true) {
			eFrag.sen_hasBullet = true;
			tagWithBullet = false;
		}
			
		// In case of passed fragments (General handling)
		if (passedFragments.size() > 0 && planned == false) {
			correctArticleSettings(eFrag);
			DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
			if (passedFragments.size() > 1) {
				for (int i = 1; i < passedFragments.size(); i++) {
					dsyntSentence.addCondition(passedFragments.get(i), true);
					dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
				}
			}
			sentencePlan.add(dsyntSentence);
			activity.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
			passedFragments.clear();
			planned = true;
		}

		if (planned == false) {
			correctArticleSettings(eFrag);
			DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
			sentencePlan.add(dsyntSentence);
			activity.setDescriptionString( qc.QcPrintDsynT.print( dsyntSentence ) );//By Chen Qian
			activitiySentenceMap.add(new Pair<Integer,DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
			
			if( activity.getType() == 2 ){
				ExecutableFragment eFrag_subBegin = new ExecutableFragment("contain", "fold subprocess", "",
						"the following steps");
				eFrag_subBegin.bo_isSubject = true;
				eFrag_subBegin.bo_hasArticle = true;
				eFrag_subBegin.add_hasArticle = false;
				eFrag_subBegin.sen_hasBullet = true;
				sentencePlan.add(new DSynTMainSentence(eFrag_subBegin));
				activity.setDescriptionString( qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag_subBegin) ) );//By Chen Qian
				
				ArrayList<DSynTSentence> dSynTSentences =  (ArrayList<DSynTSentence>)( Main.createFromFile( activity.getOriginalLabel() + ".json" , 1 ) );
				ExecutableFragment eFrag0 = dSynTSentences.get( 0 ).getExecutableFragment();
				eFrag0.setModList( new HashMap<String, ModifierRecord>() );
				DSynTMainSentence dsyntSentence0 = new DSynTMainSentence( eFrag0 );
				dSynTSentences.set( 0 , dsyntSentence0 );
				dSynTSentences.remove( dSynTSentences.size() - 1 );
				sentencePlan.addAll( dSynTSentences );
				
				ExecutableFragment eFrag_subEnd = new ExecutableFragment("finish", "fold subprocess", "", "");
				eFrag_subEnd.verb_IsPassive = true;
				eFrag_subEnd.bo_isSubject = true;
				eFrag_subEnd.bo_hasArticle = true;
				sentencePlan.add(new DSynTMainSentence(eFrag_subEnd));
				activity.setDescriptionString( qc.QcPrintDsynT.print( new DSynTMainSentence(eFrag_subEnd) ) );//By Chen Qian
			}
		}
		

		// If activity has attached Events
		if (activity.hasAttachedEvents()) {
			ArrayList<Integer>attachedEvents = activity.getAttachedEvents();
			HashMap<Integer,ProcessModel>alternativePaths = process.getAlternativePaths();
			for (Integer attEvent: attachedEvents) {
				if (alternativePaths.keySet().contains(attEvent)) {
					// Transform alternative
					ProcessModel alternative = epsf.getAlternativeModels().get( attEvent );
					
					alternative.print();
					
					EPSF aEpsf = new EPSF( );
					aEpsf.addProcessModels( alternative );
					aEpsf.initialModels( );
					aEpsf.annotateModels( 0  );
					aEpsf.createSubprocessRPSTs( );
					aEpsf.createAlternativeRPSTs( );
					aEpsf.addMessageFlows( alternative );
					
//					RPST<ControlFlow,Node> aRpst = epsf.getAlternativeRPSTs().get( attEvent );
					RPST<ControlFlow,Node> aRpst = epsf.getAlternativeRPSTs().get( attEvent );
					TextPlanner converter = new TextPlanner( alternative, aEpsf, aRpst, imperativeRole, imperative, true);
					aRpst.print( aRpst.getRoot() , 0 );
					
					converter.convertToText(aRpst.getRoot(), level+1);

					ArrayList <DSynTSentence> subSentencePlan = converter.getSentencePlan();
					for (int i = 0; i <subSentencePlan.size(); i++) {
						DSynTSentence sen = subSentencePlan.get(i);
						if (i==0) {
							sen.getExecutableFragment().sen_level = level;
						}
						if (i==1) {
							sen.getExecutableFragment().sen_hasBullet = true;
						}
						sentencePlan.add(sen);
						qc.QcPrintDsynT.print( sen );//By Chen Qian
					}
					converter = null;
					
					//attach分支的后续描述
					sentencePlan.add(textToIMConverter.getAttachedEventPostStatement(alternative.getEvents().get(attEvent)));
					qc.QcPrintDsynT.print( textToIMConverter.getAttachedEventPostStatement(alternative.getEvents().get(attEvent)) );//By Chen Qian
				}
			}
		}
		
		
		if (depth > 0) {
			convertToText(node, level);
		}
	}

	//带有上一个活动信息
	private void convertActivities(RPSTNode<ControlFlow, Node> node, int level, int depth , Activity lastActivity) throws JWNLException, JsonSyntaxException, IOException {
		
		Activity activity = (Activity) process.getActivity(Integer.parseInt(node.getEntry().getId()));
		Annotation anno = activity.getAnnotations().get(0);
		ExecutableFragment eFrag = null;
		
		// Start of the path
		//ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
		//modRecord.addAttribute("starting_point", "+");
		
		String bo = anno.getBusinessObjects().get(0);

		eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition() + activity.getAddtion() );
		eFrag.sen_level = level;
		
		//eFrag.addAssociation(activity.getId());
		//eFrag.addMod("the process begins when", modRecord);
		
		String role = getRole(activity, eFrag);
		eFrag.setRole(role);
				
		//After条件
		ConditionFragment cFrag = new ConditionFragment("write", "paper", "Frank", "", ConditionFragment.TYPE_WHEN );
		
		if (anno.getActions().size() == 2) {
			ExecutableFragment eFrag2 = null;
			if (anno.getBusinessObjects().size() == 2) {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
				eFrag2.addAssociation(activity.getId());
			} else {
				eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
				eFrag2.addAssociation(activity.getId());
			}
			
			correctArticleSettings(eFrag2);
			eFrag.addSentence(eFrag2);
		}
		
		if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0,bo.length()-1))) {
			eFrag.bo_hasArticle = true;
		} else {
			eFrag.bo_hasIndefArticle = true;
		}
		
		// If imperative mode
		if (imperative == true && imperativeRole.equals(role) == true) {
			eFrag.verb_isImperative = true;
			eFrag.role_isImperative = true;
		}
		correctArticleSettings(eFrag);
		DSynTSentence dsyntSentence = new DSynTConditionSentence(eFrag, cFrag);
		dsyntSentence.getExecutableFragment().sen_level = level;
		dsyntSentence.getExecutableFragment().sen_hasBullet = true;
		sentencePlan.add(dsyntSentence);
		qc.QcPrintDsynT.print( dsyntSentence );//By Chen Qian
	}
	
	/**
	 * Get ConverterRecord for AND
	 */
	private ConverterRecord getANDConverterRecord(RPSTNode<ControlFlow, Node> node) {

		ArrayList<RPSTNode<ControlFlow, Node>> andNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);	
		
//		// Only General Case, no need for non-bulletin and-branches
		ConverterRecord rec = textToIMConverter.convertANDGeneral(node, andNodes.size(), null);
		return rec;
	}	
					
	/**
	 * Get ConverterRecord for OR
	 */
	private ConverterRecord getORConverterRecord(RPSTNode<ControlFlow, Node> node) {
//		GatewayPropertyRecord orPropRec = new GatewayPropertyRecord(node, rpst, process);
//		
//		// Labeled Case
//		if (orPropRec.isGatewayLabeled() == true)  {
//			return null;
//			
//		// Unlabeled case
//		} else {
//			return textToIMConverter.convertORSimple(node, null, false);
//		}	
		
		//By Chen Qian
		return textToIMConverter.convertORSimple(node, null, false);	
	}
	
	/**
	 * Get ConverterRecord for XOR
	 */
	private ConverterRecord getXORConverterRecord(RPSTNode<ControlFlow, Node> node) {
		GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
		
		boolean qc_control = false;//By Chen Qian
		// Labeled Case with Yes/No - arcs and Max. Depth of 1
		if (qc_control && propRec.isGatewayLabeled()== true && propRec.hasYNArcs() == true && propRec.getMaxPathDepth() == 1) {
			GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);
			
			// Add sentence
			for (DSynTSentence s: textToIMConverter.convertXORSimple(node, gwExtractor)) {
				sentencePlan.add(s);
				qc.QcPrintDsynT.print( s );//By Chen Qian
			}
			return new ConverterRecord(null,null,null,null,null);
		// General case
		} else {
			return textToIMConverter.convertXORGeneral(node);
		}	
	}
	
	//By Chen Qian
	private ConverterRecord getEventSplitConverterRecord(RPSTNode<ControlFlow, Node> node) {

		return textToIMConverter.convertEventSplitGeneral(node);
	}
	
	/**
	 * Get ConverterRecord for Loop
	 */
	private ConverterRecord getLoopConverterRecord(RPSTNode<ControlFlow, Node> node) {
	
		RPSTNode<ControlFlow, Node> firstNodeInLoop = PlanningHelper.getNextNode(node, rpst);
		return textToIMConverter.convertLoop(node,firstNodeInLoop);
	}
	
	/**
	 * Get ConverterRecord for Skip 
	 */
	private ConverterRecord getSkipConverterRecord(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
		GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
		
		// Yes-No Case 
		if (propRec.isGatewayLabeled() == true && propRec.hasYNArcs() == true) {
			
			// Yes-No Case which is directly leading to the end of the process
			if (isToEndSkip(orderedTopNodes, node) == true) {
				return textToIMConverter.convertSkipToEnd(node);

			// General Yes/No-Case	
			} else {
				return textToIMConverter.convertSkipGeneral(node);
			}
		
		// General unlabeled Skip
		} else {
			return textToIMConverter.convertSkipGeneralUnlabeled(node);
		}
	}
			
	/**
	 * Evaluate whether skip leads to an end 
	 */
	private boolean isToEndSkip(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
		int currentPosition = orderedTopNodes.indexOf(node);
		if (currentPosition < orderedTopNodes.size()-1) {
			Node potEndNode = orderedTopNodes.get(currentPosition+1).getExit();
			if (PlanningHelper.isEndEvent(potEndNode,process) == true) {
				return true;
			} 
		}
		return false;
	}
	
	/**
	 * Returns role of a fragment.  
	 */
	private String getRole(Activity a, AbstractFragment frag) {
		if (a.getLane() == null) {
			frag.verb_IsPassive = true;
			frag.bo_isSubject = true;
			if (frag.getBo().equals("")) {
				frag.setBo("it");
				frag.bo_hasArticle = false;
			}
			return "";
		}
		String role = a.getLane().getName();
		if (role.equals("")) {
			role = a.getPool().getName();
		}
		if (role.equals("")) {
			frag.verb_IsPassive = true;
			frag.bo_isSubject = true;
			if (frag.getBo().equals("")) {
				frag.setBo("it");
				frag.bo_hasArticle = false;
			}
		}
		return role;
	}
	
	/**
	 * Checks and corrects the article settings. 
	 */
	public void correctArticleSettings(AbstractFragment frag) {
		String bo = frag.getBo();
		if (bo.endsWith("s") && bo.endsWith("ss") == false && frag.bo_hasArticle == true && lHelper.isNoun(bo.substring(0, bo.length()-1))== true) {
			bo = bo.substring(0, bo.length()-1);
			frag.setBo(bo);
			frag.bo_isPlural = true;
		}
		if (bo.contains("&")) {
			frag.bo_isPlural = true;
		}
		if (frag.bo_hasArticle == true) {
			String[] boSplit = bo.split(" ");
			if (boSplit.length > 1) {
				if (Arrays.asList(quantifiers).contains(boSplit[0].toLowerCase())) {
					 frag.bo_hasArticle = false;
				}
			}
		}
		if (bo.equals("") && frag.bo_hasArticle) {
			frag.bo_hasArticle = false;
		}
		if (bo.startsWith("their") || bo.startsWith("a ") || bo.startsWith("for")) {
			frag.bo_hasArticle = false;
		}
		String[] splitAdd = frag.getAddition().split(" ");
		if (splitAdd.length > 3 && lHelper.isVerb(splitAdd[1]) && splitAdd[0].equals("on") == false) {
			frag.add_hasArticle = false;
		} else {
			frag.add_hasArticle = true;
		}
		
	}

	public ArrayList<Pair<Integer, DSynTSentence>> getActivitiySentenceMap() {
		return activitiySentenceMap;
	}
	
	public ArrayList<DSynTSentence> getSentencePlan() {
		return sentencePlan;
	}

	public void setStart( boolean bool ){
		start = bool;
	}
	
	public void setTagWithBullet( boolean bool ){
		tagWithBullet = bool;
	} 
}
package textPlanning;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import net.didion.jwnl.JWNLException;
import templates.Phrases;
import templates.TemplateLoader;
import textPlanning.recordClasses.ConverterRecord;
import textPlanning.recordClasses.ModifierRecord;
import contentDetermination.extraction.GatewayExtractor;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import dataModel.dsynt.DSynTConditionSentence;
import dataModel.dsynt.DSynTMainSentence;
import dataModel.dsynt.DSynTSentence;
import dataModel.intermediate.AbstractFragment;
import dataModel.intermediate.ConditionFragment;
import dataModel.intermediate.ExecutableFragment;
import dataModel.process.Activity;
import dataModel.process.Annotation;
import dataModel.process.Arc;
import dataModel.process.Event;
import dataModel.process.EventType;
import dataModel.process.Gateway;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;

public class TextToIntermediateConverter {

	private RPST<ControlFlow, Node> rpst;
	private ProcessModel process;
	private EnglishLabelHelper lHelper;
	private boolean imperative;
	private String imperativeRole;
	private TemplateLoader templateLoader;

	public TextToIntermediateConverter(RPST<ControlFlow, Node> rpst,
			ProcessModel process, EnglishLabelHelper lHelper,
			String imperativeRole, boolean imperative)
			throws FileNotFoundException, JWNLException {
		this.rpst = rpst;
		this.process = process;
		this.lHelper = lHelper;
		this.imperative = imperative;
		this.imperativeRole = imperativeRole;
		this.templateLoader = new TemplateLoader();
	}

	// *********************************************************************************************
	// OR - SPLIT
	// *********************************************************************************************

	// The following optional parallel paths are available.

	public ConverterRecord convertORSimple(RPSTNode<ControlFlow, Node> node,
			GatewayExtractor gwExtractor, boolean labeled) {
		ConverterRecord record = null;
		
			templateLoader.loadTemplate(TemplateLoader.OR);
			ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(),templateLoader.getObject(), "", "");
			ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
			modRecord2.addAttribute("adv-type", "sentential");
			eFrag.addMod(templateLoader.getAddition(), modRecord2);
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = false;
			eFrag.verb_IsPassive = true;
			eFrag.bo_isPlural = true;
			eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

			ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
			preStatements.add(new DSynTMainSentence(eFrag));
			
			ConditionFragment post = new ConditionFragment(templateLoader.getAction(),
					templateLoader.getObject(), "", "",
					ConditionFragment.TYPE_ONCE,
					new HashMap<String, ModifierRecord>());
			post.verb_isPast = true;
			post.verb_IsPassive = true;
			post.bo_isSubject = true;
			post.bo_isPlural = true;
			post.bo_hasArticle = false;
			post.setFragmentType(AbstractFragment.TYPE_JOIN);
			post.addAssociation(Integer.valueOf(node.getEntry().getId()));
			
			record = new ConverterRecord(null, post, preStatements, null);
			
		return record;
	}

	// *********************************************************************************************
	// XOR - SPLIT
	// *********************************************************************************************

	public ArrayList<DSynTSentence> convertXORSimple(
			RPSTNode<ControlFlow, Node> node, GatewayExtractor gwExtractor) {

		ExecutableFragment eFragYes = null;
		ExecutableFragment eFragNo = null;
		String role = "";

		ArrayList<RPSTNode<ControlFlow, Node>> pNodeList = new ArrayList<RPSTNode<ControlFlow, Node>>();
		pNodeList.addAll(rpst.getChildren(node));
		for (RPSTNode<ControlFlow, Node> pNode : pNodeList) {
			for (RPSTNode<ControlFlow, Node> tNode : rpst.getChildren(pNode)) {
				if (tNode.getEntry() == node.getEntry()) {
					for (Arc arc : process.getArcs().values()) {
						System.out.println(arc.getLabel());
						if (arc.getSource().getId() == Integer.valueOf(tNode
								.getEntry().getId())
								&& arc.getTarget().getId() == Integer
										.valueOf(tNode.getExit().getId())) {
							if (arc.getLabel().toLowerCase().equals("yes")) {
								Activity a = process.getActivity(Integer
										.valueOf(tNode.getExit().getId()));
								Annotation anno = a.getAnnotations().get(0);
								String action = anno.getActions().get(0);
								String bo = anno.getBusinessObjects().get(0);
								//
								role = a.getLane().getName();
								// role = getRole(tNode);

								String addition = anno.getAddition();
								eFragYes = new ExecutableFragment(action, bo,
										role, addition);
								eFragYes.addAssociation(Integer.valueOf(node
										.getExit().getId()));
							}
							if (arc.getLabel().toLowerCase().equals("no")) {
								Activity a = process.getActivity(Integer
										.valueOf(tNode.getExit().getId()));
								Annotation anno = a.getAnnotations().get(0);
								String action = anno.getActions().get(0);
								String bo = anno.getBusinessObjects().get(0);

								role = a.getLane().getName();
								// role = getRole(tNode);

								String addition = anno.getAddition();
								eFragNo = new ExecutableFragment(action, bo,
										role, addition);

								ModifierRecord modRecord = new ModifierRecord(
										ModifierRecord.TYPE_ADV,
										ModifierRecord.TARGET_VERB);
								modRecord
										.addAttribute("adv-type", "sentential");
								eFragNo.addMod("otherwise", modRecord);
								eFragNo.sen_hasConnective = true;
								eFragNo.addAssociation(Integer.valueOf(node
										.getExit().getId()));
							}
						}
					}
				}
			}
		}

		ConditionFragment cFrag = new ConditionFragment(gwExtractor.getVerb(),
				gwExtractor.getObject(), "", "", ConditionFragment.TYPE_IF,
				gwExtractor.getModList());
		cFrag.bo_replaceWithPronoun = true;
		cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

		// If imperative mode
		if (imperative == true && imperativeRole.equals(role) == true) {
			eFragNo.setRole("");
			eFragNo.verb_isImperative = true;
			eFragYes.setRole("");
			eFragYes.verb_isImperative = true;
		}
		DSynTConditionSentence dsyntSentence1 = new DSynTConditionSentence(
				eFragYes, cFrag);
		DSynTMainSentence dsyntSentence2 = new DSynTMainSentence(eFragNo);
		ArrayList<DSynTSentence> sentences = new ArrayList<DSynTSentence>();
		sentences.add(dsyntSentence1);
		sentences.add(dsyntSentence2);
		return sentences;
	}

	public ConverterRecord convertXORGeneral(RPSTNode<ControlFlow, Node> node) {

		// One of the following branches is executed. (And then use bullet
		// points for structuring)
		
		templateLoader.loadTemplate(TemplateLoader.XOR);
		
		//分裂句(执行Fragment)By Chen Qian
		ExecutableFragment eFrag = null;
		if( node.getEntry().getName().equals("") ){
			eFrag = new ExecutableFragment(templateLoader.getAction(),
					templateLoader.getObject(), "", "without condition");
		}
		else{
			eFrag = new ExecutableFragment(templateLoader.getAction(),
				templateLoader.getObject(), "", "[depends on whether these branches satisfy the condition : \"" + node.getEntry().getName() + "\"]");
		}
		eFrag.bo_isSubject = true;
		eFrag.verb_IsPassive = true;
		eFrag.bo_hasArticle = false;
		eFrag.add_hasArticle = false;//By Chen Qian
		eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
		ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
		preStatements.add(new DSynTMainSentence(eFrag));
		

		//合并句(条件Fragment)
		// Statement about negative case (process is finished)
		ConditionFragment post = new ConditionFragment(templateLoader.getAction(),
				templateLoader.getObject(), "", "",
				ConditionFragment.TYPE_ONCE,
				new HashMap<String, ModifierRecord>());
		post.verb_isPast = true;
		post.verb_IsPassive = true;
		post.bo_isSubject = true;
		post.bo_isPlural = false;
		post.bo_hasArticle = false;
		post.setFragmentType(AbstractFragment.TYPE_JOIN);
		post.addAssociation(Integer.valueOf(node.getEntry().getId()));
		
		//By Qian Chen
		//ArrayList<DSynTSentence> postStatements = new ArrayList<DSynTSentence>();
		//postStatements.add(new DSynTConditionSentence(new ExecutableFragment("","","",""),post));
		//return new ConverterRecord(null, post, preStatements, postStatements, null);
		
		return new ConverterRecord(null, post, preStatements, null, null);

	}
	
	//By Chen Qian
	public ConverterRecord convertEventSplitGeneral(RPSTNode<ControlFlow, Node> node) {
		
		templateLoader.loadTemplate(TemplateLoader.EVENT_SPLIT);
		
		//分裂句(执行Fragment)By Chen Qian
		ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(),
			templateLoader.getObject(), "", "depends on which event occurs");
		eFrag.bo_isSubject = true;
		eFrag.verb_IsPassive = true;
		eFrag.bo_hasArticle = false;
		eFrag.add_hasArticle = false;//By Chen Qian
		eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
		ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
		preStatements.add(new DSynTMainSentence(eFrag));
		

		//合并句(条件Fragment)
		// Statement about negative case (process is finished)
		ConditionFragment post = new ConditionFragment(templateLoader.getAction(),
				templateLoader.getObject(), "", "",
				ConditionFragment.TYPE_ONCE,
				new HashMap<String, ModifierRecord>());
		post.verb_isPast = true;
		post.verb_IsPassive = true;
		post.bo_isSubject = true;
		post.bo_isPlural = false;
		post.bo_hasArticle = false;
		post.setFragmentType(AbstractFragment.TYPE_JOIN);
		post.addAssociation(Integer.valueOf(node.getEntry().getId()));
		
		//By Qian Chen
		//ArrayList<DSynTSentence> postStatements = new ArrayList<DSynTSentence>();
		//postStatements.add(new DSynTConditionSentence(new ExecutableFragment("","","",""),post));
		//return new ConverterRecord(null, post, preStatements, postStatements, null);
		
		return new ConverterRecord(null, post, preStatements, null, null);

	}

	// *********************************************************************************************
	// LOOP - SPLIT
	// *********************************************************************************************

	/**
	 * Converts a loop construct with labeled entry condition into two
	 * sentences.
	 */
	public ConverterRecord convertLoop(RPSTNode<ControlFlow, Node> node,RPSTNode<ControlFlow, Node> firstActivity) {

		// Labeled Case
		if (node.getExit().getName().equals("") == false) {

			// Derive information from the gateway
			GatewayExtractor gwExtractor = new GatewayExtractor(node.getExit(),lHelper);

			// Generate general statement about loop
			templateLoader.loadTemplate(TemplateLoader.LOOP_SPLIT);
			String role = getRole(node);
			ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(), templateLoader.getObject(),role, "");
			eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
			ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
			eFrag.addMod(templateLoader.getAddition(), modRecord);
			eFrag.bo_isPlural = true;

			ExecutableFragment eFrag2 = new ExecutableFragment("continue", "","", "");
			eFrag2.addAssociation(Integer.valueOf(node.getEntry().getId()));
			eFrag.addSentence(eFrag2);
			if (role.equals("")) {
				eFrag.verb_IsPassive = true;
				eFrag.bo_isSubject = true;
				eFrag2.verb_IsPassive = true;
				eFrag2.setBo("it");
				eFrag2.bo_isSubject = true;
				eFrag2.bo_hasArticle = false;
			}

			role = "";
			ConditionFragment cFrag = null;
			Activity a = process.getActivity(Integer.valueOf(firstActivity.getExit().getId()));
			Event e = process.getEvent(Integer.valueOf(firstActivity.getExit().getId()));
			Gateway g = process.getGateway(Integer.valueOf(firstActivity.getExit().getId()));
			if (a != null) {
				role = a.getLane().getName();
				if (role.equals("")) {
					role = a.getPool().getName();
				}
				ExecutableFragment eFrag3 = new ExecutableFragment(a
						.getAnnotations().get(0).getActions().get(0), a
						.getAnnotations().get(0).getBusinessObjects().get(0),
						"", "");
				eFrag3.addAssociation(a.getId());
				eFrag3.sen_isCoord = false;
				eFrag3.verb_isParticiple = true;
				ModifierRecord modRecord2 = new ModifierRecord(
						ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord2.addAttribute("adv-type", "sentential");
				eFrag3.addMod("with", modRecord2);
				eFrag2.addSentence(eFrag3);

				cFrag = new ConditionFragment(gwExtractor.getVerb(),
						gwExtractor.getObject(), "", "",
						ConditionFragment.TYPE_AS_LONG_AS,
						new HashMap<String, ModifierRecord>(
								gwExtractor.getModList()));
				cFrag.verb_IsPassive = true;
				cFrag.bo_isSubject = true;
				cFrag.sen_headPosition = true;
				cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
			} else if (e != null) {
				role = e.getLane().getName();
				if (role.equals("")) {
					role = e.getPool().getName();
				}

				ExecutableFragment eFrag3 = new ExecutableFragment("continue","loop", "", "");
				eFrag3.addAssociation(e.getId());
				eFrag3.sen_isCoord = false;
				eFrag3.verb_isParticiple = true;
				ModifierRecord modRecord2 = new ModifierRecord(
						ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord2.addAttribute("adv-type", "sentential");
				eFrag3.addMod("with", modRecord2);
				eFrag2.addSentence(eFrag3);

				cFrag = new ConditionFragment(gwExtractor.getVerb(),
						gwExtractor.getObject(), "", "",
						ConditionFragment.TYPE_AS_LONG_AS,
						new HashMap<String, ModifierRecord>(
								gwExtractor.getModList()));
				cFrag.verb_IsPassive = true;
				cFrag.bo_isSubject = true;
				cFrag.sen_headPosition = true;
				cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
			} else {
				// Gateway

				role = g.getLane().getName();
				if (role.equals("")) {
					role = g.getPool().getName();
				}

				ExecutableFragment eFrag3 = new ExecutableFragment("repeat","loop", "", "");
				eFrag3.addAssociation(g.getId());
				eFrag3.sen_isCoord = false;
				eFrag3.verb_isParticiple = true;
				eFrag2.addSentence(eFrag3);
				cFrag = new ConditionFragment(gwExtractor.getVerb(),
						gwExtractor.getObject(), "", "",
						ConditionFragment.TYPE_AS_LONG_AS,
						new HashMap<String, ModifierRecord>(
								gwExtractor.getModList()));
				cFrag.verb_IsPassive = true;
				cFrag.bo_isSubject = true;
				cFrag.sen_headPosition = true;
				cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
			}

			// Determine postcondition
			gwExtractor.negateGatewayLabel();
			ConditionFragment post = new ConditionFragment(
					gwExtractor.getVerb(), gwExtractor.getObject(), "", "",
					ConditionFragment.TYPE_ONCE, gwExtractor.getModList());
			post.verb_IsPassive = true;
			post.bo_isSubject = true;
			post.setFragmentType(AbstractFragment.TYPE_JOIN);
			post.addAssociation(Integer.valueOf(node.getEntry().getId()));

			// If imperative mode
			if (imperative == true && imperativeRole.equals(role) == true) {
				eFrag.setRole("");
				eFrag.verb_isImperative = true;
				eFrag2.verb_isImperative = true;
			}

			ArrayList<DSynTSentence> postStatements = new ArrayList<DSynTSentence>();
			postStatements.add(new DSynTConditionSentence(eFrag, cFrag));
			return new ConverterRecord(null, post, null, postStatements);
		}

		// Unlabeled case
		else {
			templateLoader.loadTemplate(TemplateLoader.SKIP);
			ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(), templateLoader.getObject(),"", "");
			ModifierRecord modRecord = new ModifierRecord(
					ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
			eFrag.addMod(templateLoader.getAddition(), modRecord);
			eFrag.bo_isPlural = true;
			eFrag.bo_isSubject = true;
			eFrag.verb_IsPassive = true;

			ConditionFragment cFrag = new ConditionFragment("require", "dummy",
					"", "", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.bo_replaceWithPronoun = true;
			cFrag.verb_IsPassive = true;
			cFrag.bo_isSubject = true;
			cFrag.sen_headPosition = true;

			ExecutableFragment eFrag2 = new ExecutableFragment("continue", "",
					"", "");

			// Determine role
			String role = "";
			Activity a = process.getActivity(Integer.valueOf(firstActivity
					.getExit().getId()));
			if (a != null) {
				role = a.getLane().getName();
				if (role.equals("")) {
					role = a.getPool().getName();
				}
				eFrag2.setRole(role);
				ModifierRecord modRecord3 = new ModifierRecord(
						ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord3.addAttribute("adv-type", "sentential");
				eFrag2.addMod("in that case", modRecord3);
				eFrag2.sen_hasConnective = true;

				ExecutableFragment eFrag3 = new ExecutableFragment(a
						.getAnnotations().get(0).getActions().get(0), a
						.getAnnotations().get(0).getBusinessObjects().get(0),
						"", a.getAnnotations().get(0).getAddition());
				eFrag3.sen_isCoord = false;
				eFrag3.verb_isParticiple = true;
				ModifierRecord modRecord2 = new ModifierRecord(
						ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord2.addAttribute("adv-type", "sentential");
				eFrag3.addMod("with", modRecord2);
				eFrag2.addSentence(eFrag3);

			} else {
				eFrag2 = null;
			}

			// Determine postcondition
			templateLoader.loadTemplate(TemplateLoader.LOOP_JOIN);
			ConditionFragment post = new ConditionFragment(templateLoader.getAction(),templateLoader.getObject(), "", "", ConditionFragment.TYPE_ONCE,
					new HashMap<String, ModifierRecord>());
			post.verb_IsPassive = true;
			post.bo_isSubject = true;
			post.setFragmentType(AbstractFragment.TYPE_JOIN);

			// If imperative mode
			if (imperative == true && imperativeRole.equals(role) == true) {
				eFrag.setRole("");
				eFrag.verb_isImperative = true;
				eFrag2.verb_isImperative = true;
			}

			ArrayList<DSynTSentence> postStatements = new ArrayList<DSynTSentence>();
			postStatements.add(new DSynTConditionSentence(eFrag, cFrag));
			if (eFrag2 != null) {
				postStatements.add(new DSynTMainSentence(eFrag2));
			}
			return new ConverterRecord(null, post, null, postStatements);
		}

	}

	// *********************************************************************************************
	// SKIP - SPLIT
	// *********************************************************************************************

	public ConverterRecord convertSkipGeneralUnlabeled(RPSTNode<ControlFlow, Node> node) {
		
		templateLoader.loadTemplate(TemplateLoader.SKIP);
		ConditionFragment pre = new ConditionFragment(templateLoader.getAction(), templateLoader.getObject(), "", "",
				ConditionFragment.TYPE_IF,new HashMap<String, ModifierRecord>());

		ModifierRecord mod = new ModifierRecord(ModifierRecord.TYPE_ADV,ModifierRecord.TARGET_VERB);
		pre.addMod(templateLoader.getAddition(), mod);
		pre.bo_replaceWithPronoun = true;
		pre.sen_headPosition = true;
		pre.sen_isCoord = true;
		pre.sen_hasComma = true;
		pre.addAssociation(Integer.valueOf(node.getEntry().getId()));
		return new ConverterRecord(pre, null, null, null);

	}

	/**
	 * Converts a standard skip construct with labeled condition gateway into
	 * two sentences.
	 */
	public ConverterRecord convertSkipGeneral(RPSTNode<ControlFlow, Node> node) {
		// Derive information from the gateway
		GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(),
				lHelper);

		// Generate general statement about upcoming decision
		ConditionFragment pre = new ConditionFragment(gwExtractor.getVerb(),
				gwExtractor.getObject(), "", "",
				ConditionFragment.TYPE_IN_CASE, gwExtractor.getModList());
		pre.verb_IsPassive = true;
		if (gwExtractor.hasVerb == false) {
			pre.verb_IsPassive = false;
		}
		pre.bo_isSubject = true;
		pre.sen_headPosition = true;
		pre.bo_isPlural = gwExtractor.bo_isPlural;
		pre.bo_hasArticle = gwExtractor.bo_hasArticle;
		pre.addAssociation(Integer.valueOf(node.getEntry().getId()));
		return new ConverterRecord(pre, null, null, null);
	}

	/**
	 * Converts a standard skip construct with labeled condition gateway,
	 * leading to the end of the process, into two sentences.
	 */
	public ConverterRecord convertSkipToEnd(RPSTNode<ControlFlow, Node> node) {

		// Derive information from the gateway
		GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(),lHelper);
		String role = getRole(node);

		// Generate general statement about upcoming decision
		ExecutableFragment eFrag = new ExecutableFragment("decide", "", role,"");
		ConditionFragment cFrag = new ConditionFragment(gwExtractor.getVerb(),
				gwExtractor.getObject(), "", "",
				ConditionFragment.TYPE_WHETHER, gwExtractor.getModList());
		cFrag.verb_IsPassive = true;
		cFrag.bo_isSubject = true;
		cFrag.sen_headPosition = false;
		cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
		eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

		if (role.equals("")) {
			eFrag.verb_IsPassive = true;
			eFrag.setBo("it");
			eFrag.bo_hasArticle = false;
			eFrag.bo_isSubject = true;
			cFrag.verb_IsPassive = true;
			cFrag.setBo("it");
			cFrag.bo_hasArticle = false;
			cFrag.bo_isSubject = true;
		}

		// Statement about negative case (process is finished)
		ExecutableFragment eFrag2 = new ExecutableFragment("finish","process instance", "", "");
		eFrag2.verb_IsPassive = true;
		eFrag2.bo_isSubject = true;
		ConditionFragment cFrag2 = new ConditionFragment("be", "case", "this","", ConditionFragment.TYPE_IF,
				new HashMap<String, ModifierRecord>());
		cFrag2.verb_isNegated = true;

		// Determine precondition
		ConditionFragment pre = new ConditionFragment(gwExtractor.getVerb(),
				gwExtractor.getObject(), "", "", ConditionFragment.TYPE_IF,
				new HashMap<String, ModifierRecord>());
		pre.verb_IsPassive = true;
		pre.sen_headPosition = true;
		pre.bo_isSubject = true;
		ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_PREP,
				ModifierRecord.TARGET_VERB);
		modRecord.addAttribute("adv-type", "sentential");
		pre.addMod("otherwise", modRecord);
		pre.sen_hasConnective = true;

		// If imperative mode
		if (imperative == true && imperativeRole.equals(role) == true) {
			eFrag.setRole("");
			eFrag.verb_isImperative = true;
		}

		ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
		preStatements.add(new DSynTConditionSentence(eFrag, cFrag));
		preStatements.add(new DSynTConditionSentence(eFrag2, cFrag2));

		return new ConverterRecord(pre, null, preStatements, null);
	}

	// *********************************************************************************************
	// AND - SPLIT
	// *********************************************************************************************

	public ConverterRecord convertANDGeneral(RPSTNode<ControlFlow, Node> node,
			int activities, ArrayList<Node> conditionNodes) {

		// The process is split into three parallel branches. (And then use
		// bullet points for structuring)

		templateLoader.loadTemplate(TemplateLoader.AND_SPLIT);
		ExecutableFragment eFrag = new ExecutableFragment(
				templateLoader.getAction(), templateLoader.getObject(), "",
				templateLoader.getAddition().replace("arg",
						Integer.toString(activities)));
		eFrag.bo_isSubject = true;
		eFrag.verb_IsPassive = true;
		eFrag.add_hasArticle = false;
		eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

		ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
		preStatements.add(new DSynTMainSentence(eFrag));

		// Statement about negative case (process is finished)
		templateLoader.loadTemplate(TemplateLoader.AND_JOIN);
		ConditionFragment post = new ConditionFragment(
				templateLoader.getAction(), 
				templateLoader.getObject().replace("arg", Integer.toString(activities)), "", "",
				ConditionFragment.TYPE_ONCE,
				new HashMap<String, ModifierRecord>());
		post.verb_isPast = true;
		post.verb_IsPassive = true;
		post.bo_isSubject = true;
		post.bo_isPlural = true;
		post.bo_hasArticle = false;
		post.addAssociation(Integer.valueOf(node.getEntry().getId()));
		post.setFragmentType(AbstractFragment.TYPE_JOIN);

		return new ConverterRecord(null, post, preStatements, null, null);
	}

	/**
	 * Converts a simple and construct.
	 */
	public ConverterRecord convertANDSimple(RPSTNode<ControlFlow, Node> node,
			int activities, ArrayList<Node> conditionNodes) {

		// get last element of both branches and combine them to a post condition
		// if one of them is a gateway, include gateway post condition in the and post condition

		ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,ModifierRecord.TARGET_VERB);
		modRecord.addAttribute("adv-type", "sentential");

		if (activities == 1) {
			modRecord.setLemma(Phrases.AND_SIMPLE_SINGLE);
		} else {
			modRecord.setLemma(Phrases.AND_SIMPLE_MULT.replace("arg", Integer.toString(activities)));
		}

		// Determine postcondition
		ConditionFragment post = null;
		String role = "";

		// Check whether postcondition should be passed
		int arcs = 0;
		for (Arc arc : process.getArcs().values()) {
			if (arc.getTarget().getId() == Integer.valueOf(node.getExit()
					.getId())) {
				arcs++;
			}
		}

		// Only if no other arc flows into join gateway, join condition is passed
		if (arcs == 2) {
			templateLoader.loadTemplate(TemplateLoader.AND_JOIN_SIMPLE);
			if (conditionNodes.size() == 1) {
				Activity a = process.getActivity(Integer.valueOf(conditionNodes.get(0).getId()));
				String verb = a.getAnnotations().get(0).getActions().get(0);
				role = getRole(node);
				post = new ConditionFragment(templateLoader.getAction(), lHelper.getNoun(verb),
						role, "", ConditionFragment.TYPE_ONCE,
						new HashMap<String, ModifierRecord>());
				post.sen_headPosition = true;
				post.verb_isPast = true;
				post.setFragmentType(AbstractFragment.TYPE_JOIN);
				post.addAssociation(Integer.valueOf(node.getEntry().getId()));
			} else {
				post = new ConditionFragment(templateLoader.getAction(), templateLoader.getObject(), "", "",
						ConditionFragment.TYPE_ONCE,
						new HashMap<String, ModifierRecord>());
				post.bo_isPlural = true;
				post.sen_headPosition = true;
				post.bo_hasArticle = false;
				post.bo_isSubject = true;
				post.verb_isPast = true;
				post.verb_IsPassive = true;
				post.setFragmentType(AbstractFragment.TYPE_JOIN);
				post.addAssociation(Integer.valueOf(node.getEntry().getId()));
			}
		}

		// If imperative mode
		if (imperative == true && imperativeRole.equals(role) == true) {
			post.role_isImperative = true;
		}

		return new ConverterRecord(null, post, null, null, modRecord);
	}

	public ConverterRecord convertEvent(Event event) {

		ConditionFragment cFrag = null;
		ExecutableFragment eFrag = null;
		ArrayList<DSynTSentence> preSentences;

		String role = event.getLane().getName();
		if (role.equals("")) {
			role = event.getPool().getName();
		}

		switch (event.getType()) {

		// ***************************************************************
		// INTERMEDIATE (CATCHING) EVENTS
		// ***************************************************************

		// ERROR EVENT错误事件，已验证
		case EventType.INTM_ERROR:
			String error = event.getLabel();

			if (error.equals("")) {
				cFrag = new ConditionFragment("occur", "error", "", "",ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasIndefArticle = true;
			} else {
				cFrag = new ConditionFragment("occur", "error '" + error + "'","", "", ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
			}
			cFrag.bo_isSubject = true;
			if (event.isAttached()) {
				cFrag.setAddition("while latter task is executed,");
			}
			break;
			
			//By Qian Chen取消事件，已验证
		case EventType.INTM_CANCEL:
			String cancel_string = event.getLabel();

			if (cancel_string.equals("")) {
				cFrag = new ConditionFragment("cancel", "activity", "", "",ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasIndefArticle = true;
			} else {
				cFrag = new ConditionFragment("cancel", "activity '" + cancel_string + "'","", "", ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
			}
			cFrag.verb_IsPassive = true;
			cFrag.bo_isSubject = true;
			cFrag.bo_hasArticle = true;
			if (event.isAttached()) {
				cFrag.setAddition("while latter task is executed,");
			}
			break;
		
			//By Qian Chen升级事件，，已验证
		case EventType.INTM_ESCALATION:
			String escalation = event.getLabel();

			if (escalation.equals("")) {
				cFrag = new ConditionFragment("occur", "escalation", "", "",ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasIndefArticle = true;
			} else {
				cFrag = new ConditionFragment("occur", "escalation '" + escalation + "'","", "", ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
			}
			cFrag.bo_isSubject = true;
			if (event.isAttached()) {
				cFrag.setAddition("while latter task is executed,");
			}
			break;

		//已验证
		case EventType.INTM_TIMER:
			String limit = event.getLabel();

			if (limit.equals("")) {
				eFrag = new ExecutableFragment("wait", "up to a certain time",role, "", new HashMap<String, ModifierRecord>());
				eFrag.bo_hasArticle = false;
			} else {
				eFrag = new ExecutableFragment("wait","up to the time limit of " + limit, role, "",
						new HashMap<String, ModifierRecord>());
				eFrag.bo_hasArticle = false;
			}
			if (event.isAttached()) {
				cFrag = new ConditionFragment("reach", "time limit", "", "", ConditionFragment.TYPE_IN_CASE,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
				cFrag.bo_isSubject = true;
				cFrag.setAddition("while latter task is executed,");
			}
			break;
			
			//已验证
		case EventType.INTM_CONDITIONAL:
			String condition = event.getLabel();

			if (condition.equals("")) {
				eFrag = new ExecutableFragment("meet", "a certain condition",role, "", new HashMap<String, ModifierRecord>());
				eFrag.bo_hasArticle = false;
			} else {
				eFrag = new ExecutableFragment("meet","the condition '" + condition + "'", role, "",
						new HashMap<String, ModifierRecord>());
				eFrag.bo_hasArticle = false;
			}
			if (event.isAttached()) {
				cFrag = new ConditionFragment("meet", "a certain condition", role, "", ConditionFragment.TYPE_IN_CASE,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = false;
				cFrag.bo_isSubject = false;
				cFrag.setAddition("while latter task is executed,");
			}
			break;

			//已验证
		case EventType.INTM_MSG_CAT:
			String intm_msg_cat = event.getLabel();

			if (intm_msg_cat.equals("")) {
				cFrag = new ConditionFragment("receive", "a message", role, "",ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasIndefArticle = true;
			} else {
				cFrag = new ConditionFragment("receive", "a message '" + intm_msg_cat + "'",role, "", ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
			}
			cFrag.verb_IsPassive = false;
			cFrag.bo_isSubject = false;//....
			if (event.isAttached()) {
				cFrag.setAddition("while latter task is executed,");
			}
			break;
			
			//已验证
		case EventType.INTM_MULTIPLE_CAT:
			String intm_nutp_cat = event.getLabel();

			if (intm_nutp_cat.equals("")) {
				cFrag = new ConditionFragment("meet", "multiple event", role, "",ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasIndefArticle = true;
			} else {
				cFrag = new ConditionFragment("meet", "multiple event '" + intm_nutp_cat + "'",role, "", ConditionFragment.TYPE_IF,
						new HashMap<String, ModifierRecord>());
				cFrag.bo_hasArticle = true;
			}
			cFrag.verb_IsPassive = false;
			cFrag.bo_isSubject = false;//....
			if (event.isAttached()) {
				cFrag.setAddition("while latter task is executed,");
			}
			break;

		//已验证
		case EventType.INTM_SIGNAL_CAT:
			cFrag = new ConditionFragment("", "of receiving a signal", "", "",
					ConditionFragment.TYPE_IN_CASE,
					new HashMap<String, ModifierRecord>());
			cFrag.bo_hasArticle = false;
			cFrag.bo_isSubject = true;
			break;

		// ***************************************************************
		// START / END EVENTS
		// ***************************************************************

		////已验证
		case EventType.END_EVENT:
			if (event.getSubProcessID() > 0) {
				eFrag = new ExecutableFragment("finish", "subprocess", "", "");
			} else {
				eFrag = new ExecutableFragment("finish", "process", "", "");
			}
			eFrag.verb_IsPassive = true;
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			return getEventSentence(eFrag);

			// ERROR EVENT ， 已验证
		case EventType.END_CAT:
			eFrag = new ExecutableFragment("end", "process", "",
					"with an intermediate end event");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			eFrag.add_hasArticle = false;
			return getEventSentence(eFrag);
			
			//已验证
		case EventType.INTM_PMULT_CAT:
			eFrag = new ExecutableFragment("cause", "parallel multiple trigger", event.getLane()
					.getName(), "");
			eFrag.bo_hasArticle = true;
			eFrag.bo_hasIndefArticle = true;
			eFrag.bo_isPlural = true;
			return getEventSentence(eFrag);
			
			//By Qian Chen ， 已验证
		case EventType.END_ERROR:
			eFrag = new ExecutableFragment("end", "process", "",
					"with an error");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			eFrag.add_hasArticle = false;
			return getEventSentence(eFrag);

		case EventType.END_SIGNAL:
			eFrag = new ExecutableFragment("end", "process", "",
					"with a signal");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			eFrag.add_hasArticle = false;
			return getEventSentence(eFrag);

		case EventType.END_MSG:
			eFrag = new ExecutableFragment("end", "process", "",
					"with a message");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			eFrag.add_hasArticle = false;
			return getEventSentence(eFrag);

			//已验证
		case EventType.START_EVENT:
			eFrag = new ExecutableFragment("contain", "subprocess", "",
					"the following steps");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			eFrag.add_hasArticle = false;
			eFrag.sen_hasBullet = true;
			return getEventSentence(eFrag);

			// START EVENT
		case EventType.START_MSG:
			cFrag = new ConditionFragment("receive", "message", "", "",
					ConditionFragment.TYPE_ONCE);
			cFrag.bo_isSubject = true;
			cFrag.verb_IsPassive = true;
			cFrag.bo_hasArticle = true;
			cFrag.bo_hasIndefArticle = true;
			eFrag = new ExecutableFragment("start", "process", "", "");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			return getEventSentence(eFrag, cFrag);

			// TIMER START EVENT
		case EventType.START_TIMER:
			cFrag = new ConditionFragment("fulfill", "time condition", "", "",
					ConditionFragment.TYPE_ONCE);
			cFrag.bo_isSubject = true;
			cFrag.verb_IsPassive = true;
			cFrag.bo_hasArticle = true;
			cFrag.bo_hasIndefArticle = true;
			eFrag = new ExecutableFragment("start", "process", "", "");
			eFrag.bo_isSubject = true;
			eFrag.bo_hasArticle = true;
			return getEventSentence(eFrag, cFrag);

			// ***************************************************************
			// THROWING EVENTS
			// ***************************************************************
			
			//已验证
		case EventType.INTM_MSG_THR:
			eFrag = new ExecutableFragment("send", "message", event.getLane()
					.getName(), "");
			eFrag.bo_hasArticle = true;
			eFrag.bo_hasIndefArticle = true;
			eFrag.bo_isPlural = true;
			return getEventSentence(eFrag);

			//已验证
		case EventType.INTM_ESCALATION_THR:
			eFrag = new ExecutableFragment("trigger", "escalation", event
					.getLane().getName(), "");
			eFrag.bo_hasIndefArticle = true;
			return getEventSentence(eFrag);

			// LINK EVENT
		case EventType.INTM_LINK_THR:
			eFrag = new ExecutableFragment("send", "signal", event.getLane()
					.getName(), "");
			eFrag.bo_hasIndefArticle = true;
			return getEventSentence(eFrag);

			//已验证
		case EventType.INTM_MULTIPLE_THR:
			eFrag = new ExecutableFragment("cause", "multiple trigger", event
					.getLane().getName(), "");
			eFrag.bo_hasArticle = false;
			eFrag.bo_isPlural = true;
			return getEventSentence(eFrag);

			//已验证
		case EventType.INTM_SIGNAL_THR:
			eFrag = new ExecutableFragment("send", "signal", event.getLane()
					.getName(), "");
			eFrag.bo_hasArticle = true;
			eFrag.bo_hasIndefArticle = true;
			eFrag.bo_isPlural = true;
			return getEventSentence(eFrag);
			
		default:
			System.out.println("NON-COVERED EVENT " + event.getType());
			return null;
		}

		// Handling of intermediate Events (up until now only condition is
		// provided)

		// Attached Event
		if (event.isAttached()) {
			preSentences = new ArrayList<DSynTSentence>();
			preSentences.add(getAttachedEventSentence(event, cFrag));
			return new ConverterRecord(null, null, preSentences, null);

			// Non-attached Event
		} else {
			preSentences = new ArrayList<DSynTSentence>();
			if (cFrag != null) {
				preSentences.add(getIntermediateEventSentence(event, cFrag));
			} else {
				preSentences.add(new DSynTMainSentence(eFrag));
			}
			return new ConverterRecord(null, null, preSentences, null);
		}
	}

	/**
	 * Returns Sentence for attached Event.
	 */
	private DSynTConditionSentence getAttachedEventSentence(Event event,
			ConditionFragment cFrag) {
		ExecutableFragment eFrag = new ExecutableFragment("cancel", "it", "",
				"");
		eFrag.verb_IsPassive = true;
		eFrag.bo_isSubject = true;
		eFrag.bo_hasArticle = false;

		// if (event.isLeadsToEnd() == false) {
		ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
				ModifierRecord.TARGET_VERB);
		ExecutableFragment eFrag2 = new ExecutableFragment("continue",
				"process", "", "");
		modRecord.addAttribute("adv-type", "sent-final");
		modRecord.addAttribute("rheme", "+");
		eFrag2.addMod("as follows", modRecord);

		eFrag2.bo_isSubject = true;
		eFrag.addSentence(eFrag2);
		DSynTConditionSentence sen = new DSynTConditionSentence(eFrag, cFrag);
		return sen;
	}
	
	
	

	// For attached events only
	public DSynTConditionSentence getAttachedEventPostStatement(Event event) {
		ModifierRecord modRecord;
		ModifierRecord modRecord2;
		ExecutableFragment eFrag;
		ConditionFragment cFrag;

		switch (event.getType()) {

			//By Chen Qian已验证
		case EventType.INTM_TIMER:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"within the time limit", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);

			//By Chen Qian已验证
		case EventType.INTM_ERROR:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without error", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);

			//By Chen Qian已验证
		case EventType.INTM_CANCEL:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without cancelling", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);					
						
			//By Chen Qian已验证
		case EventType.INTM_SIGNAL_CAT:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without reveiving a signal", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);

			//By Chen Qian
		case EventType.INTM_ESCALATION:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without escalation", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);
			
			//By Chen Qian已验证
		case EventType.INTM_MSG_CAT:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without receiving a message", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);

			//已验证
		case EventType.INTM_MULTIPLE_CAT:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without meeting a multiple event", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);
			
			//已验证
		case EventType.INTM_CONDITIONAL:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without meeting a certain condition", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);
			
			//已验证
		case EventType.INTM_COMPENSATION_CAT:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without compensation", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);

			//已验证
		case EventType.INTM_PMULT_CAT:
			modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
					ModifierRecord.TARGET_VERB);
			eFrag = new ExecutableFragment("continue", "process", "", "");
			eFrag.bo_isSubject = true;
			modRecord.addAttribute("adv-type", "sent-final");
			modRecord.addAttribute("rheme", "+");
			eFrag.addMod("normally", modRecord);

			cFrag = new ConditionFragment("complete", "the task", "",
					"without causing parallel multiple trigger", ConditionFragment.TYPE_IF,
					new HashMap<String, ModifierRecord>());
			cFrag.sen_hasConnective = true;
			cFrag.add_hasArticle = false;
			modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP,
					ModifierRecord.TARGET_VERB);
			modRecord2.addAttribute("adv-type", "sentential");
			cFrag.addMod("otherwise", modRecord2);
			configureFragment(cFrag);
			return new DSynTConditionSentence(eFrag, cFrag);
			
		default:
			System.out.println("NON-COVERED EVENT " + event.getType());
			return null;
		}
	}

	/**
	 * Returns record with sentence for throwing events.
	 */
	private ConverterRecord getEventSentence(ExecutableFragment eFrag) {
		DSynTMainSentence msen = new DSynTMainSentence(eFrag);
		ArrayList<DSynTSentence> preSentences = new ArrayList<DSynTSentence>();
		preSentences.add(msen);
		return new ConverterRecord(null, null, preSentences, null);
	}

	private ConverterRecord getEventSentence(ExecutableFragment eFrag,
			ConditionFragment cFrag) {
		DSynTConditionSentence msen = new DSynTConditionSentence(eFrag, cFrag);
		ArrayList<DSynTSentence> preSentences = new ArrayList<DSynTSentence>();
		preSentences.add(msen);
		return new ConverterRecord(null, null, preSentences, null);
	}

	/**
	 * Returns sentence for intermediate events.
	 */
	private DSynTConditionSentence getIntermediateEventSentence(Event event,
			ConditionFragment cFrag) {
		ExecutableFragment eFrag = new ExecutableFragment("continue",
				"process", "", "");
		eFrag.bo_isSubject = true;
		DSynTConditionSentence sen = new DSynTConditionSentence(eFrag, cFrag);
		return sen;
	}

	/**
	 * Configures condition fragment in a standard fashion.
	 */
	private void configureFragment(ConditionFragment cFrag) {
		cFrag.verb_IsPassive = true;
		cFrag.bo_isSubject = true;
		cFrag.bo_hasArticle = false;
	}

	/**
	 * Returns role executing current RPST node.
	 */
	private String getRole(RPSTNode<ControlFlow, Node> node) {
		String role = process.getGateways()
				.get(Integer.valueOf(node.getExit().getId())).getLane()
				.getName();
		if (role.equals("")) {
			role = process.getGateways()
					.get(Integer.valueOf(node.getExit().getId())).getPool()
					.getName();
		}
		return role;
	}

}

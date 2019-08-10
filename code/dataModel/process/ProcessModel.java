package dataModel.process;

import java.util.ArrayList;
import java.util.HashMap;

import contentDetermination.labelAnalysis.EnglishLabelCategorizer;
import contentDetermination.labelAnalysis.EnglishLabelDeriver;
import contentDetermination.labelAnalysis.EnglishLabelHelper;
import contentDetermination.labelAnalysis.EnglishLabelProperties;
import dataModel.jsonIntermediate.JSONArc;
import dataModel.jsonIntermediate.JSONElem;
import dataModel.jsonIntermediate.JSONEvent;
import dataModel.jsonIntermediate.JSONGateway;
import dataModel.jsonIntermediate.JSONLane;
import dataModel.jsonIntermediate.JSONPool;
import dataModel.jsonIntermediate.JSONTask;
import textPlanning.TextPlanner;


public class ProcessModel {
	
	private int id;
	private String name;
	private HashMap <Integer,Arc> arcs;
	private HashMap <Integer, Activity> activities;
	private HashMap <Integer,Event> events;
	private HashMap <Integer,Gateway> gateways;
	private ArrayList<String> lanes;
	private ArrayList<String> pools;
	private HashMap<Integer,ProcessModel> alternativePaths;
	
	public static final int VOS = 0;
	public static final int AN = 1;
	public static final int INVESTIGATE = 2;
	
	//By Chen Qian
	private HashMap <Integer,Artifact> artifacts;
	private HashMap <Integer,Data> datas;
	
	public void setArtifacts(){
		for( Arc arc : arcs.values() ){
			//System.out.println( arc.getSource().getClass().getName() + "..." + arc.getTarget().getClass().getName() );
			if( arc.getSource().getClass().getName().contains("Activity") && arc.getTarget().getClass().getName().contains("Artifact") ){
				arc.getSource().setArtifact( artifacts.get( arc.getTarget().getId() ) );
			}
		}
	}
	
	public void setDatas(){
		for( Arc arc : arcs.values() ){
			//System.out.println( arc.getSource().getClass().getName() + "..." + arc.getTarget().getClass().getName() );
			if( arc.getSource().getClass().getName().contains("Activity") && arc.getTarget().getClass().getName().contains("Data") ){
				arc.getSource().setData( (Data)arc.getTarget() );
				arc.getSource().setDirection( arc.getTarget().getId() , Direction.OUT );
			}
			else if( arc.getSource().getClass().getName().contains("Data") && arc.getTarget().getClass().getName().contains("Activity") ){
				arc.getTarget().setData( (Data)arc.getSource() );
				arc.getTarget().setDirection( arc.getSource().getId() , Direction.IN );
			}
		}
	}
	
	//得到一个新的ID
	public int getNewId() {
		int base = 0;
		
		for (int i: arcs.keySet()) {
			if (i > base) {
				base = i;
			}
		}
		for (int i: activities.keySet()) {
			if (i > base) {
				base = i;
			}
		}
		for (int i: gateways.keySet()) {
			if (i > base) {
				base = i;
			}
		}
		for (int i: events.keySet()) {
			if (i > base) {
				base = i;
			}
		}
		base++;
		return base;
	}
	
	public HashMap<Integer, ProcessModel> getAlternativePaths() {
		return alternativePaths;
	}

	public void addElem(Element elem) {
		if (elem.getClass().toString().endsWith("Gateway")) {
			gateways.put(elem.getId(), (Gateway) elem);
		}
		if (elem.getClass().toString().endsWith("Activity")) {
			activities.put(elem.getId(), (Activity) elem);
		}
		if (elem.getClass().toString().endsWith("Event")) {
			events.put(elem.getId(), (Event) elem);
		}
	}
	
	public Element getElem(int id) {
		if (events.containsKey(id)) {
			return events.get(id);
		}
		if (gateways.containsKey(id)) {
			return gateways.get(id);
		}
		if (activities.containsKey(id)) {
			return activities.get(id);
		}
		return null;
	}
	
	public Arc getArc(int id) {
		return arcs.get(id);
	}
	
	public void removeArc(int id) {
		if (arcs.containsKey(id) == false ) {
			//System.out.println("NO ARC: " + id);
			return ;
		}
		//System.out.println( "删除了弧:" + arcs.get( id ).getSource().getId() + "→" + arcs.get( id ).getTarget().getId() );
		this.arcs.remove(id);
	}
	
	public void removeElem(int id) {
		if (events.containsKey(id)) {
			events.remove(id);
			//System.out.println( "删除了事件:" + id );
		}
		if (gateways.containsKey(id)) {
			gateways.remove(id);
			//System.out.println( "删除了网关:" + id );
		}
		if (activities.containsKey(id)) {
			activities.remove(id);
			//System.out.println( "删除了活动:" + id );
		}
	}
	
	//合规化活动(多入度多出度)
	public void normalize() {
		
		// Clean arcs
		ArrayList<Integer> toBeDeleted = new ArrayList<Integer>();
		for (int key: arcs.keySet()) {
			if (arcs.get(key).getTarget() == null) {
				toBeDeleted.add(key);
			}
		}
		for (int key: toBeDeleted) {
			arcs.remove(key);
		}
		
		
		for (int activityKey: activities.keySet()) {
			int count = 0;
			
			// Count arcs (incoming)
			//活动的入度
			for (Arc arc: arcs.values()) {
				if (arc.getTarget().getId() == activityKey) {
					count++;
				}
			}
			//入度大于1的活动,增加XOR网关
			if (count > 1) {
				Activity a = activities.get(activityKey);
				int gwId = getNewId();
				Gateway xorGateway =  new Gateway(gwId , "", a.getLane(), a.getPool(), GatewayType.XOR);
				gateways.put(gwId, xorGateway);
				// Modify target of incoming arcs to new gateway
				for (Arc arc: arcs.values()) {
					if (arc.getTarget().getId() == activityKey) {
						arc.setTarget(xorGateway);
					}
				}
				
				// Create new arc from gateway to activity
				int arcId = getNewId();
				Arc arc = new Arc(arcId, "", xorGateway, a);
				arcs.put(arcId, arc);
				//System.out.println("Gateway for incoming arcs inserted (" + activityKey + ") :" + gwId);
			}
			
			
			count = 0;
			// Count arcs (outgoing)
			//活动的出度
			for (Arc arc: arcs.values()) {
				if (arc.getSource().getId() == activityKey) {
					count++;
				}
			}
			if (count > 1) {
				Activity a = activities.get(activityKey);
				int gwId = getNewId();
				boolean isAND = true;
				for (Arc arc: arcs.values()) {
					if (arc.getType().equals("VirtualFlow")) {
						isAND = false;
					}
				}
				
				Gateway gateway = null;
				if (isAND == true) {
					gateway =  new Gateway(gwId , "", a.getLane(), a.getPool(), GatewayType.AND);
				} else {
					gateway =  new Gateway(gwId , "", a.getLane(), a.getPool(), GatewayType.XOR);
				}
				gateways.put(gwId, gateway);
				//System.out.println("Gateway for outgoing arcs inserted: " + gwId);
				
				// Modify target of incoming arcs to new gateway
				for (Arc arc: arcs.values()) {
					if (arc.getSource().getId() == activityKey) {
						arc.setSource(gateway);
					}
				}
				
				// Create new arc from gateway to activity
				int arcId = getNewId();
				Arc arc = new Arc(arcId, "", a, gateway);
				arcs.put(arcId, arc);
			}
		}
	}
	
	//合规化结束事件(多入度)
	public void normalizeEndEvents() {
		int count = 0;
		ArrayList<Integer> endEvents = new ArrayList<Integer>();
		
		for (Event e: events.values()) {
			if (EventType.isEndEvent(e.getType()) && !(e.getSubProcessID() > 0)) {
				count++;
				endEvents.add(e.getId());
			}
		}
		if (count > 1) {
			//System.out.println("Multiple End Events detected");
			int endEventId = getNewId();
			
			Event endEvent = new Event(endEventId, "", events.get(endEvents.get(0)).getLane(), events.get(endEvents.get(0)).getPool(), EventType.END_EVENT);
			events.put(endEventId, endEvent);
			
			Element predecessor = null;
			int removeId = -1;
			String removeString = "";
			
			// For each end event, create an arc to the new end event
			for (int mEndEventId: endEvents) {
				
				// Find predecessor and save arc id
//				for (int id: arcs.keySet()) {
//					Arc arc = arcs.get(id);
//					if (arc.getTarget().getId() ==  mEndEventId) {
//						predecessor = arc.getSource();
//						removeId = id;
//						removeString = arc.getLabel();
//						break;
//					}
//				}
				
//				arcs.remove(removeId);
//				events.remove(mEndEventId);
				
				
				int arcId = getNewId();
//				Arc arc = new Arc(arcId, removeString, predecessor, endEvent);
				Arc arc = new Arc(arcId, "", events.get(mEndEventId), endEvent);
				arcs.put(arcId, arc);
			}
		}
	}
	//...
	public void annotateModel( int option ) {
		
		//System.out.println(activities.size() + "\t" + events.size() + "\t" + gateways.size());
		
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
//					if (lC.getLabelStyle(act).equals("VO")) {
					TextPlanner.lDeriver.deriveFromVOS(a.getLabel(), labelSplit, props);
//					} else {
//						lDeriver.deriveFromActionNounLabels(props, label, labelSplit);
//					}
				
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
	
	public ProcessModel(int id, String name) {
		this.id = id;
		this.name = name;
		arcs = new HashMap<Integer, Arc>();
		activities = new HashMap<Integer, Activity>();
		events = new HashMap<Integer, Event>();
		gateways = new HashMap<Integer, Gateway>();
		lanes = new ArrayList<String>();
		pools = new ArrayList<String>();
		alternativePaths = new HashMap<Integer, ProcessModel>();
		artifacts = new HashMap<Integer, Artifact>();
		datas = new HashMap<Integer, Data>();
	}
	
	public void addAlternativePath(ProcessModel path, int id) {
		alternativePaths.put(id, path);
	}
	
	public int getElemAmount() {
		return activities.size() + gateways.size() + events.size();
	}
	
	public ArrayList<String> getPools() {
		return pools;
	}

	public void addPool(String pool) {
		this.pools.add(pool);
	}

	public void addArc(Arc arc) {
		arcs.put(arc.getId(), arc);
		//System.out.println( "增加了弧:" + arc.getSource().getId() + "→" + arc.getTarget().getId() );
	}
	
	public void addActivity(Activity activity) {
		activities.put(activity.getId(), activity);
		//System.out.println( "增加了活动:" + activity.getId() + "(" + activity.getLabel() + ")" );
	}
	
	public void addEvent(Event event) {
		events.put(event.getId(), event);
	}
	
	public void addGateway(Gateway gateway) {
		gateways.put(gateway.getId(), gateway);
	}
	
	public void addArtifact(Artifact artifact) {
		artifacts.put(artifact.getId(), artifact);
	}
	
	public void addData(Data data) {
		datas.put(data.getId(), data);
	}

	public HashMap<Integer, Arc> getArcs() {
		return arcs;
	}

	public HashMap<Integer, Activity> getActivites() {
		return activities;
	}

	public HashMap<Integer, Event> getEvents() {
		return events;
	}

	public HashMap<Integer, Gateway> getGateways() {
		return gateways;
	}
	
	public HashMap<Integer, Artifact> getArtifact() {
		return artifacts;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public Activity getActivity(int id) {
		return activities.get(id);
	}
	
	public Event getEvent(int id) {
		return events.get(id);
	}
	
	public Gateway getGateway(int id) {
		return gateways.get(id);
	}
	
	public void addLane(String lane) {
		String temp = lane;
		if (temp.contains("glossary://")) {
			temp = lane.replace("glossary://", "");
			temp = temp.substring(temp.indexOf("/")+1,temp.length());
			temp = temp.replace(";;", "");
		}
		lanes.add(temp);
	}
	
	public ArrayList<String> getLanes() {
		return lanes;
	}
	
	//输出过程模型:事件、活动、网关、序列流
	public void print() {

		//System.out.println("Process Model: "  + this.name + " (" + this.getId() + ")");
		
		//System.out.println("\n泳池");
		for (String a: pools ) {
			//System.out.println( a );
		}
		
		//System.out.println("\n泳道");
		for (String a: lanes ) {
			//System.out.println( a );
		}
		
		//System.out.println("\n元素-活动");
		for (Activity a: activities.values()) {
			//System.out.println("Activity (" + a.getId() + ") " + a.getLabel() + " P:" + a.getPool().getId() + " sub: " + a.getSubProcessID());
		}
		
		//System.out.println("\n元素-事件");
		for (Event e: events.values()) {
			//System.out.println("Event (" + e.getId() + ") " + e.getLabel() + " - Type: "  + e.getType() + " P:" + e.getPool().getId() + " sub: " + e.getSubProcessID());
		}
		
		//System.out.println("\n元素-网关");
		for (Gateway g: gateways.values()) {
			//System.out.println("Gatewyay (" + g.getId() + ")" + " " + g.getType() + " P:" + g.getPool().getId() + " sub: " + g.getSubProcessID());
		}
		
		//System.out.println("\n元素-文本注释");
		for (Artifact artifact: artifacts.values()) {
			if( artifact.getType() == ArtifactType.TEXTANNOTATION ){
				//System.out.println("TextAnnotation (" + artifact.getId() + ")" + " " + artifact.getType() + " P:" + artifact.getPool().getId() + " sub: " + artifact.getSubProcessID() + " label: " + artifact.getLabel() );
			}
		}
		
		//System.out.println("\n元素-IT系统");
		for (Artifact artifact: artifacts.values()) {
			if( artifact.getType() == ArtifactType.ITSYSTEM ){
				//System.out.println("ITSystem (" + artifact.getId() + ")" + " " + artifact.getType() + " P:" + artifact.getPool().getId() + " sub: " + artifact.getSubProcessID() + " label: " + artifact.getLabel() );
			}
		}
		
		//System.out.println("\n元素-数据对象");
		for (Data data: datas.values()) {
			if( data.getType() == DataType.DATAOBJECT ){
				//System.out.println("DataObjet (" + data.getId() + ")" + " " + data.getType() + " P:" + data.getPool().getId() + " sub: " + data.getSubProcessID() + " label: " + data.getLabel() );
			}
		}
		
		//System.out.println("\n元素-数据存储");
		for (Data data: datas.values()) {
			if( data.getType() == DataType.DATASTORE ){
				//System.out.println("DataStore (" + data.getId() + ")" + " " + data.getType() + " P:" + data.getPool().getId() + " sub: " + data.getSubProcessID() + " label: " + data.getLabel() );
			}
		}
		
		//System.out.println("\n元素-序列流 " + arcs.size());
		for (Arc arc: arcs.values()) {
			if( arc.getType().equals( "SequenceFlow" ) || arc.getType().equals( "" ) ){
				//System.out.println("SequenceFlow: (s: " + arc.getSource().getId() + " t: " + arc.getTarget().getId() + ")" + "- " + arc.getId() + " " +  arc.getLabel() + " " + arc.getType());
			}
		}
		
		//System.out.println("\n元素-信息流");
		for (Arc arc: arcs.values()) {
			if( arc.getType().equals( "MessageFlow" ) ){
				//System.out.println("MessageFlow: (s: " + arc.getSource().getId() + " t: " + arc.getTarget().getId() + ")" + "- " + arc.getId() + " " +  arc.getLabel() + " " + arc.getType());
			}
		}
		
		//System.out.println("\n元素-无向虚边");
		for (Arc arc: arcs.values()) {
			if( arc.getType().equals( "Association_Undirected" ) ){
				//System.out.println("Association_Undirected: (s: " + arc.getSource().getId() + " t: " + arc.getTarget().getId() + ")" + "- " + arc.getId() + " " +  arc.getLabel() + " " + arc.getType());
			}
		}
		
		//System.out.println("\n元素-有向虚边");
		for (Arc arc: arcs.values()) {
			if( arc.getType().equals( "Association_Unidirectional" ) ){
				//System.out.println("Association_Unidirectional: (s: " + arc.getSource().getId() + " t: " + arc.getTarget().getId() + ")" + "- " + arc.getId() + " " +  arc.getLabel() + " " + arc.getType());
			}
		}
		
		//System.out.println("\n外部路径");
		for (Integer id: alternativePaths.keySet()) {
			ProcessModel pModel = alternativePaths.get( id );
			//System.out.println( id + " -->> " + pModel.getId() );
		}
	}
	
	public int getSequenceFlowCount() {
		int count = 0;
		for (Arc arc: arcs.values()) {
			if (arc.getType().equals("SequenceFlow")) {
				count++;
			}
		}
		return count;
	}
	
	//获取每个泳池pool
	public HashMap<Integer,ProcessModel> getModelForEachPool() {
		
		HashMap<Integer,ProcessModel> newModels = new HashMap<Integer, ProcessModel>(); 

		// Add activities
		for (Activity a: activities.values()) {
			int poolId = a.getPool().getId();
			if (!newModels.containsKey(poolId)) {
				newModels.put(poolId, new ProcessModel(poolId, ""));
				newModels.get(poolId).addPool(a.getPool().getName());
			}
			newModels.get(poolId).addActivity(a);
			for (Arc arc: arcs.values()) {
				if (arc.getSource().getId() == a.getId() || arc.getTarget().getId() == a.getId()) {
					if (arc.getType().equals("SequenceFlow")) {
						newModels.get(poolId).addArc(arc);
						arcs.remove(arc);
					}
				}
			}
		}
		
		// Add events
		for (Event e: events.values()) {
			int poolId = e.getPool().getId();
			if (!newModels.containsKey(poolId)) {
				newModels.put(poolId, new ProcessModel(poolId, ""));
			}
			newModels.get(poolId).addEvent(e);
			for (Arc arc: arcs.values()) {
				if (arc.getSource().getId() == e.getId() || arc.getTarget().getId() == e.getId()) {
					if (arc.getType().equals("SequenceFlow")) {
						newModels.get(poolId).addArc(arc);
						arcs.remove(arc);
					}
				}
			}
		}
		
		// Add gateways
		for (Gateway g: gateways.values()) {
			int poolId = g.getPool().getId();
			if (!newModels.containsKey(poolId)) {
				newModels.put(poolId, new ProcessModel(poolId, ""));
			}
			newModels.get(poolId).addGateway(g);
			for (Arc arc: arcs.values()) {
				if (arc.getSource().getId() == g.getId() || arc.getTarget().getId() == g.getId()) {
					if (arc.getType().equals("SequenceFlow")) {
						newModels.get(poolId).addArc(arc);
						arcs.remove(arc);
					}
				}
			}
		}
		return newModels;
		
	}
}

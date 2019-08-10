package dataModel.jsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xml.serializer.ElemDesc;

import com.google.gson.JsonArray;
import com.itextpdf.text.xml.xmp.DublinCoreSchema;

import dataModel.jsonIntermediate.JSONArc;
import dataModel.jsonIntermediate.JSONArtifact;
import dataModel.jsonIntermediate.JSONData;
import dataModel.jsonIntermediate.JSONElem;
import dataModel.jsonIntermediate.JSONEvent;
import dataModel.jsonIntermediate.JSONGateway;
import dataModel.jsonIntermediate.JSONLane;
import dataModel.jsonIntermediate.JSONPool;
import dataModel.jsonIntermediate.JSONTask;
import dataModel.jsonStructure.Doc;
import dataModel.jsonStructure.ElementLevel;
import dataModel.jsonStructure.LaneLevel;
import dataModel.jsonStructure.PoolLevel;
import dataModel.jsonStructure.Stencil;
import dataModel.process.Activity;
import dataModel.process.ActivityType;
import dataModel.process.Arc;
import dataModel.process.Artifact;
import dataModel.process.ArtifactType;
import dataModel.process.Data;
import dataModel.process.DataType;
import dataModel.process.Element;
import dataModel.process.Event;
import dataModel.process.EventType;
import dataModel.process.Gateway;
import dataModel.process.GatewayType;
import dataModel.process.Lane;
import dataModel.process.Pool;
import dataModel.process.ProcessModel;
import ee.ut.bpstruct.bpmn.test.oulsnam.ModelLBTest;

public class JSONReader {

	private ArrayList<String> fileNames;
	private HashMap<String, Integer> keyMap;
	private HashMap<Integer, Integer> shapeMap;
	private ArrayList<JSONTask> tasks;
	private ArrayList<JSONEvent> events;
	private HashMap<Integer,JSONArc> arcs;
	private HashMap<Integer,JSONElem> elems;
	private ArrayList<JSONGateway> gateways;
	private ArrayList<JSONLane> lanes;
	private ArrayList<JSONPool> pools;
	private ArrayList<JSONPool> subProcesses;
	
	//By Chen Qian
	private HashMap<Integer,JSONArtifact> artifacts;
	private HashMap<Integer,JSONData> datas;
	
	private boolean wasCorrect = true;

	public static int idCounter;

	public JSONReader() {
		init();
	}

	public void init() {
		fileNames = new ArrayList<String>();
		tasks = new ArrayList<JSONTask>();
		arcs = new HashMap<Integer, JSONArc>();
		gateways = new ArrayList<JSONGateway>();
		events = new ArrayList<JSONEvent>();
		lanes = new ArrayList<JSONLane>();
		keyMap = new HashMap<String, Integer>();
		shapeMap = new HashMap<Integer, Integer>();
		pools = new ArrayList<JSONPool>();
		subProcesses = new ArrayList<JSONPool>();
		elems = new HashMap<Integer, JSONElem>();
		idCounter = 0;
		wasCorrect = true;
		
		artifacts = new HashMap<Integer,JSONArtifact>();
		datas = new HashMap<Integer,JSONData>();
	}

	public boolean wasCorrect() {
		return wasCorrect;
	}

	public ProcessModel getProcessModelFromIntermediate() {
		ProcessModel model = new ProcessModel(-1, "Process Model");
		HashMap<Integer, Element> idMap = new HashMap<Integer, Element>();
		idMap = new HashMap<Integer, Element>();
		HashMap<Integer, Lane> laneMap = new HashMap<Integer, Lane>();
		HashMap<Integer, Pool> poolMap = new HashMap<Integer, Pool>();
		
		// Map Pools
		for (JSONPool jPool : pools) {
			Pool pool = new Pool(jPool.getId(), jPool.getLabel());
			model.addPool(jPool.getLabel());
			poolMap.put(jPool.getId(), pool);

			// Map Lanes
			for (JSONLane jLane : lanes) {
				Lane lane = new Lane(jLane.getId(), jLane.getLabel(), pool);
				model.addLane(jLane.getLabel());
				laneMap.put(jLane.getId(), lane);
			}
		}
		
//		////System.out.println( elems.size() );
		// Iterate over all elems to create the according model objects
		for (JSONElem elem: elems.values()) {

//			////System.out.println( elem.getClass().toString() );
			if (elem.getClass().toString().endsWith("JSONTask")) {
				JSONTask jTask = (JSONTask) elem;
				Activity activity = null;

				//多实例入口
				boolean multi = false;
				if( elem.getId() == Integer.MAX_VALUE ){
				//if( multi ){
					activity = new Activity(jTask.getId(), jTask.getLabel().replaceAll("\n", " "),laneMap.get(jTask.getLaneId()), poolMap.get(jTask.getPoolId()), ActivityType.MULTI);
				}else{
					activity = new Activity(jTask.getId(), jTask.getLabel().replaceAll("\n", " "),laneMap.get(jTask.getLaneId()), poolMap.get(jTask.getPoolId()), ActivityType.TYPE_MAP.get(jTask.getType()));
				}

				if (jTask.getSubProcessID() > 0) {
					activity.setSubProcessID(jTask.getSubProcessID());
				}
				model.addActivity(activity);
				idMap.put(jTask.getId(), activity);
			}
			else if (elem.getClass().toString().endsWith("JSONEvent")) {
				JSONEvent jEvent = (JSONEvent) elem;
				Event event = new Event(jEvent.getId(), jEvent.getLabel(), laneMap.get(jEvent.getLaneId()), poolMap.get(jEvent.getPoolId()),getEventType(jEvent));
				if (jEvent.getSubProcessID() > 0) {
					event.setSubProcessID(jEvent.getSubProcessID());
				}
				model.addEvent(event);
				idMap.put(jEvent.getId(), event);
			}
			else if (elem.getClass().toString().endsWith("JSONGateway")) {
				JSONGateway jGateway = (JSONGateway) elem;
				//laneMap.get(jGateway.getLaneId();
				Gateway gateway = new Gateway(jGateway.getId(),jGateway.getLabel(), laneMap.get(jGateway.getLaneId()),poolMap.get(jGateway.getPoolId()), GatewayType.TYPE_MAP.get(jGateway.getType()));
				if (jGateway.getSubProcessID() > 0) {
					gateway.setSubProcessID(jGateway.getSubProcessID());
				}
				model.addGateway(gateway);
				idMap.put(jGateway.getId(), gateway);
			}
			else if (elem.getClass().toString().endsWith("JSONArtifact")) {
				JSONArtifact jArtifact = (JSONArtifact) elem;

				Artifact artifact = null;
				if( jArtifact.getType().contains("TextAnnotation") ){
					artifact = new Artifact(jArtifact.getId(),jArtifact.getLabel(), laneMap.get(jArtifact.getLaneId()),poolMap.get(jArtifact.getPoolId()), ArtifactType.TEXTANNOTATION);
				}else if( jArtifact.getType().contains("ITSystem") ){
					artifact = new Artifact(jArtifact.getId(),jArtifact.getLabel(), laneMap.get(jArtifact.getLaneId()),poolMap.get(jArtifact.getPoolId()), ArtifactType.ITSYSTEM);
				}
				model.addArtifact( artifact );

				idMap.put(jArtifact.getId(), artifact);
			}
			else if (elem.getClass().toString().endsWith("JSONData")) {
				JSONData jData = (JSONData) elem;

				Data data = null;
				if( jData.getType().contains("DataObject") ){
					data = new Data(jData.getId(),jData.getLabel(), laneMap.get(jData.getLaneId()),poolMap.get(jData.getPoolId()), DataType.DATAOBJECT);
				}else if( jData.getType().contains("DataStore") ){
					data = new Data(jData.getId(),jData.getLabel(), laneMap.get(jData.getLaneId()),poolMap.get(jData.getPoolId()), DataType.DATASTORE);
				}
				model.addData( data );

				idMap.put(jData.getId(), data);
			}
			else{
				////System.out.println( "漏JSON元素..." );
			}
		}

		HashMap<Integer,Integer> externalPathInitiators = new HashMap<Integer, Integer>();

		// Iterate over all elems to create the according arcs
		for (JSONElem elem: elems.values()) {
			for (int outId: elem.getArcs()) {

//				////System.out.println( elem.getLabel() + " " + outId );
				// if considered outgoing id does not belong to an arc, create a new one (in order to connect attached event)
				if (elems.containsKey(outId)) {
					Activity activity = ((Activity) idMap.get(elem.getId()));
					activity.addAttachedEvent(outId);

					// Attached event leads to alternative path
					if (elem.getArcs().size() > 1) {
//						////System.out.println("Attached Event with alternative Path detected: "  + elem.getLabel());
						((Event) model.getElem(outId)).setIsAttachedTo(elem.getId());
						((Event) model.getElem(outId)).setAttached(true);
						externalPathInitiators.put(outId, elem.getId());

					// Attached event goes back to standard path
					} else {
						Arc arc = new Arc(getId(), "", idMap.get(elem.getId()) , idMap.get(outId), "VirtualFlow");
						Event attEvent = ((Event) idMap.get(outId));
						attEvent.setAttached(true);
						attEvent.setIsAttachedTo(elem.getId());
						model.addArc(arc);
					}
				// Considered outgoing id exists as arc
				} else if (arcs.keySet().contains(outId)) {
					JSONArc jArc = arcs.get(outId);
					if (jArc.getType().equals("SequenceFlow")) {
						Arc arc = new Arc(outId, jArc.getLabel(), idMap.get(elem.getId()) , idMap.get(jArc.getTarget()), "SequenceFlow");
						model.addArc(arc);
					} else if (jArc.getType().equals("MessageFlow")) {
						Arc arc = new Arc(outId, jArc.getLabel(), idMap.get(elem.getId()) , idMap.get(jArc.getTarget()), "MessageFlow");
						model.addArc(arc);
					} else if (jArc.getType().equals("Association_Undirected")) {
						Arc arc = new Arc(outId, jArc.getLabel(), idMap.get(elem.getId()) , idMap.get(jArc.getTarget()), "Association_Undirected");
						model.addArc(arc);
					} else if (jArc.getType().equals("Association_Unidirectional")) {
						Arc arc = new Arc(outId, jArc.getLabel(), idMap.get(elem.getId()) , idMap.get(jArc.getTarget()), "Association_Unidirectional");
						model.addArc(arc);
					}
					else {
						////System.out.println("No according Arc found: " + outId);
					}
				} else {

					////System.out.println("No according Arc found: " + outId);
				}
			}
		}

		// remove all external path initiators
		for (int exPI: externalPathInitiators.keySet()) {
			ProcessModel alternativePathModel = new ProcessModel(exPI, "");

			// Create start event
			Event startEvent = new Event(getId(), "", model.getElem(exPI).getLane(), model.getElem(exPI).getPool(), EventType.START_EVENT);
			alternativePathModel.addEvent(startEvent);

			// Reallocate elems to alternative path
			//buildAlternativePathModel(exPI, true, model, alternativePathModel, exPI);
			HashSet<Integer> elemIdSet = new HashSet<Integer>();
			HashSet<Integer> arcIdSet = new HashSet<Integer>();
			qc_buildAlternativePathModel(exPI, true, model, alternativePathModel, exPI,elemIdSet,arcIdSet,0);

			// Add arc from artifical start to real start elem
			Event realStart = (Event) alternativePathModel.getElem(exPI);
			alternativePathModel.addArc(new Arc(getId(), "", startEvent, realStart));

			alternativePathModel.addPool( "sub" );

			// Add path to model
			model.addAlternativePath(alternativePathModel, exPI);
		}

		// Connect inner of subproess to process model
		for (Activity a: model.getActivites().values()) {
			if (a.getType() == ActivityType.SUBPROCESS) {
				int subProcesID = a.getId();
				Element out = null;
				int removeout = -1;

				// Remove arcs from subprocess activity
				for (Arc arc: model.getArcs().values()) {
					if (arc.getSource() == a) {
						out = arc.getTarget();
						removeout = arc.getId();
					}
				}
				model.removeArc(removeout);

				// Check all activities belonging to subprocess
				for (Event subE: model.getEvents().values()) {
					if (subE.getSubProcessID() == subProcesID) {
						boolean hasInput = false;
						boolean hasOutput = false;
						for (Arc arc: model.getArcs().values()) {
							if (arc.getSource() == subE) {
								hasOutput = true;
							}
							if (arc.getTarget() == subE) {
								hasInput = true;
							}
						}
						if (!hasInput) {
							model.addArc(new Arc(getId(), "", a, subE, "SequenceFlow"));
						}
						if (!hasOutput) {
							model.addArc(new Arc(getId(), "", subE, out, "SequenceFlow"));
						}
					}
				}
			}
		}

		model.setArtifacts();
		model.setDatas();

		return model;
	}

	private void buildAlternativePathModel(int id, boolean isElem, ProcessModel model, ProcessModel alternative, int exPI) {
		if (isElem) {
			JSONElem elem = elems.get(id);
			if (elem.getArcs().size() > 0) {
				for (int arc: elem.getArcs()) {
					buildAlternativePathModel(arc, false,model, alternative, exPI);
//					alternative.addElem(model.getElem(id));
//					elems.remove(id);
//					model.removeElem(id);
					////System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
				}
			} else {
//				alternative.addElem(model.getElem(id));
//				elems.remove(id);
//				model.removeElem(id);
				////System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
			}
		} else {
			buildAlternativePathModel(arcs.get(id).getTarget(), true, model, alternative, exPI);
//			alternative.addArc(model.getArc(id));
//			arcs.remove(id);
//			model.removeArc(id);
			////System.out.println("Arc reallocated: " + id + " --> " + exPI);
		}
	}

	//qc_buildAlternativePathModel(exPI, true, model, alternativePathModel, exPI,idSet,0);
	private void qc_buildAlternativePathModel(int id, boolean isElem, ProcessModel model, ProcessModel alternative, int exPI , HashSet<Integer> arcIdSet ,HashSet<Integer> elemIdSet , int depth) {
		if (isElem) {
			elemIdSet.add( id );
			JSONElem elem = elems.get(id);
			if (elem.getArcs().size() > 0) {
				for (int arc: elem.getArcs()) {
					////System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
					qc_buildAlternativePathModel(arc, false,model, alternative, exPI, arcIdSet , elemIdSet , depth+1);
				}
			} else {
				////System.out.println("Elem reallocated: " + id + " " + elem.getLabel() + " --> " + exPI);
			}
		} else {
			if( arcs.get(id) == null ){
				////System.out.println("Uhoooo.....");
			}
			else{
				arcIdSet.add( id );
				////System.out.println("Arc reallocated: " + id + " --> " + exPI);
				qc_buildAlternativePathModel(arcs.get(id).getTarget(), true, model, alternative, exPI, arcIdSet , elemIdSet , depth+1);
			}
		}

		if( depth == 0 ){
			Object[] elemObjs =  elemIdSet.toArray();
			////System.out.println( "\n删除的元素ID集:" );
			for( int i=0 ; i<elemObjs.length ; i++ ){
				int elemId = (int)elemObjs[i];
				System.out.print( elemId + "  " );
				if( alternative.getElem( elemId ) == null ){
					alternative.addElem(model.getElem(elemId));
					elems.remove(elemId);
					model.removeElem(elemId);
				}
				else{
					////System.out.println( "!!!!重复" );
				}
			}

			Object[] arcObjs =  arcIdSet.toArray();
			////System.out.println( "\n删除的弧ID集:" );
			for( int i=0 ; i<arcObjs.length ; i++ ){
				int arcId = (int)arcObjs[i];
				System.out.print( arcId + "  " );
				alternative.addArc( model.getArc(arcId) );
				arcs.remove(arcId);
				model.removeArc(arcId);
			}
		}
	}

	private int getEventType(JSONEvent jEvent) {
		try {
			int type = EventType.TYPE_MAP.get(jEvent.getType());
			return type;
		} catch (Exception e) {
			////System.out.println("Error: Event Mapping (" + jEvent.getType()+ ")");
		}
		return 5;
	}

	public String printContent(int id) {
		String s = "";
		for (JSONTask jTask : tasks) {
			s = s + id + "\t" + "Task" + "\t" + jTask.getType() + "\t"+ wasCorrect + "\n";
		}
		for (JSONEvent jEvent : events) {
			s = s + id + "\t" + "Event" + "\t" + jEvent.getType() + "\t"+ wasCorrect + "\n";
		}
		for (JSONGateway jGateway : gateways) {
			s = s + id + "\t" + "Gateway" + "\t" + jGateway.getType() + "\t"+ wasCorrect + "\n";
		}
		for (JSONArc jArc : arcs.values()) {
			s = s + id + "\t" + "Arc" + "\t" + jArc.getType() + "\t"+ wasCorrect + "\n";
		}
		for (JSONPool subP : subProcesses) {
			s = s + id + "\t" + "Subprocess" + "\t" + " " + "\t" + wasCorrect+ "\n";
		}
		return s;
	}

	/**
	 * Read from JSON
	 */
	public void getIntermediateProcessFromFile(Doc doc)
			throws TransformerException, ParserConfigurationException {
		int id = 0;

		int currentLaneId = -1;
		int currentPoolId = -1;

		// Pool level
		for (PoolLevel pool : doc.getChildShapes()) {
			String elemName = pool.getStencil().toString();
			if (elemName.contains(" ")) {
				elemName = elemName.replace(" ", "");
			}

			id = getId(pool.getResourceId());

			// Pool
			if (pool.getStencil().toString().equals("Pool")) {
				currentPoolId = id;
				String temp = cleanString(pool.getProps().getName());
				JSONPool jPool = new JSONPool(id, temp);
				pools.add(jPool);
			}
			// SequenceFlow
			else if (pool.getStencil().toString().equals("SequenceFlow")) {
				int targetId = getId(pool.getTarget().getResourceId());
				JSONArc jArc = new JSONArc(id, targetId, currentLaneId, pool.getProps().getName(), "SequenceFlow");
				arcs.put(id,jArc);
			}
			// MessageFlow
			else if (pool.getStencil().toString().equals("MessageFlow")) {
				int targetId = getId(pool.getTarget().getResourceId());
				JSONArc jArc = new JSONArc(id, targetId, currentLaneId,pool.getProps().getName(), "MessageFlow");
				arcs.put(id,jArc);
			}
			//无向虚边(Artifact)
			else if (pool.getStencil().toString().equals("Association_Undirected")) {
				int targetId = getId(pool.getTarget().getResourceId());
				JSONArc jArc = new JSONArc(id, targetId, currentLaneId,pool.getProps().getName(), "Association_Undirected");
				arcs.put(id,jArc);
			}
			//单向虚边(Data)
			else if (pool.getStencil().toString().equals("Association_Unidirectional")) {
				int targetId = getId(pool.getTarget().getResourceId());
				JSONArc jArc = new JSONArc(id, targetId, currentLaneId,pool.getProps().getName(), "Association_Unidirectional");
				arcs.put(id,jArc);
			}

			// Add lane level
			for (LaneLevel lane : pool.getChildShapes()) {
				id = getId(lane.getResourceId());
				currentLaneId = id;
				String temp = cleanString(lane.getProps().getName());
				JSONLane jlane = new JSONLane(id, temp, currentPoolId);
				lanes.add(jlane);

				// Element level
				for (ElementLevel elem : lane.getChildShapes()) {
					// Add elements
					addElems(elem, currentLaneId, currentPoolId, false, -1);
				}
			}
		}
	}

	private void addElems(ElementLevel elem, Integer currentLaneId,int currentPoolId, boolean inSubProcess, int subProcessId) {
		int id = getId(elem.getResourceId());

		////System.out.println( elem.getStencil().toString() );

 		// Save outgoing elements
		ArrayList<Integer> jArcIDs = new ArrayList<Integer>();
		for (ElementLevel out : elem.getOutgoing()) {
			jArcIDs.add(getId(out.getResourceId()));
		}

		if (elem.getStencil().toString().equals("Task")) {
			JSONTask jTask = new JSONTask(id, elem.getProps().getName(),jArcIDs, currentLaneId, currentPoolId, elem.getProps().getTasktype());
			if (inSubProcess) {
				jTask.setSubProcessID(subProcessId);
			}
			tasks.add(jTask);
			elems.put(id,jTask);
		}
		else if (elem.getStencil().toString().equals("CollapsedSubprocess")) {
			JSONTask jTask = new JSONTask(id, elem.getProps().getName(),jArcIDs, currentLaneId, currentPoolId, "Subprocess");
			if (inSubProcess) {
				jTask.setSubProcessID(subProcessId);
			}
			tasks.add(jTask);
			elems.put(id,jTask);
		}

		else if (elem.getStencil().toString().toLowerCase().contains("gateway")) {
			JSONGateway jGateway = new JSONGateway(id, elem.getProps().getName(), jArcIDs, currentLaneId,currentPoolId, elem.getStencil().toString());
			if (inSubProcess) {
				jGateway.setSubProcessID(subProcessId);
			}
			gateways.add(jGateway);
			elems.put(id,jGateway);
		}
		else if (elem.getStencil().toString().toLowerCase().contains("event")) {
			JSONEvent jEvent = new JSONEvent(id, elem.getProps().getName(),jArcIDs, currentLaneId, currentPoolId,elem.getStencil().toString());
			if (inSubProcess) {
				jEvent.setSubProcessID(subProcessId);
			}
			events.add(jEvent);
			elems.put(id,jEvent);
		}


		else if (elem.getStencil().toString().equals("Subprocess")) {
			JSONTask jTask = new JSONTask(id, "continue with a subprocess",jArcIDs, currentLaneId, currentPoolId, "ExpandedSubprocess");
			tasks.add(jTask);
			elems.put(id,jTask);
//			subProcesses.add(new JSONPool(id, elem.getProps().getName()));
			for (ElementLevel subElem : elem.getChildShapes()) {
				addElems(subElem, currentLaneId, currentPoolId, true, id);
			}
		}
		//By Chen Qian
		else if( elem.getStencil().toString().equals("TextAnnotation") ){
			JSONArtifact jArtifact = new JSONArtifact(id, elem.getProps().getName(), jArcIDs, currentLaneId, currentPoolId, elem.getStencil().toString() );
			artifacts.put( id , jArtifact );
			elems.put(id,jArtifact);
		}
		//By Chen Qian
		else if( elem.getStencil().toString().equals("ITSystem") ){
			JSONArtifact jArtifact = new JSONArtifact(id, elem.getProps().getName(), jArcIDs, currentLaneId, currentPoolId, elem.getStencil().toString() );
			artifacts.put( id , jArtifact );
			elems.put(id,jArtifact);
		}
		//By Chen Qian
		else if( elem.getStencil().toString().equals("DataObject") ){
			JSONData jData = new JSONData(id, elem.getProps().getName(), jArcIDs, currentLaneId, currentPoolId, elem.getStencil().toString() );
			datas.put( id , jData );
			elems.put(id,jData);
		}
		//By Chen Qian
		else if( elem.getStencil().toString().equals("DataStore") ){
			JSONData jData = new JSONData(id, elem.getProps().getName(), jArcIDs, currentLaneId, currentPoolId, elem.getStencil().toString() );
			datas.put( id , jData );
			elems.put(id,jData);
		}
		else{
			////System.out.println( "漏元素!!!" );
		}
	}

	private String cleanString(String s) {
		String temp = s;
		if (temp.contains("glossary://")) {
			temp = temp.replace("glossary://", "");
			temp = temp.substring(temp.indexOf("/") + 1, temp.length());
			temp = temp.replace(";;", "");
		}
		return temp;
	}

	public ArrayList<String> getAllFileNames(String path) {
		getAllJSONFiles(path);
		return fileNames;
	}

	public String getJSONStringFromFile(String file) throws IOException {
		BufferedReader fr = new BufferedReader(new FileReader(file));
		String data;
		String json = "";
		while ((data = fr.readLine()) != null) {
			json = json + data;
		}
		return json;
	}

	private int getId(String rid) {
		int id;
		if (keyMap.containsKey(rid)) {
			id = keyMap.get(rid);
		} else {
			id = idCounter;
			idCounter++;
			keyMap.put(rid, id);
		}
		return id;
	}

	public int getId() {
		int id = idCounter;
		idCounter++;
		keyMap.put("newElem" + id, id);
		return id;
	}

	private void getAllJSONFiles(String dirName) {
		File dir = new File(dirName);
		String[] files = dir.list();
		String temp;
		for (String file : files) {
			if (file.endsWith("json")) {
				temp = dir.getAbsolutePath() + "/" + file;
				fileNames.add(temp);
			}
		}
		Collections.sort(fileNames);
	}

}

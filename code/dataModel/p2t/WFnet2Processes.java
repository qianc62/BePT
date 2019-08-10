package dataModel.p2t;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.structure.PetriNetStructuralChecks;
import org.jbpt.petri.unfolding.OccurrenceNet;
import org.jbpt.petri.untangling.Process;
import org.jbpt.utils.IOUtils;

import dataModel.arborescence.ALEdge;
import dataModel.arborescence.ALNode;
import dataModel.arborescence.AdjacencyList;
import dataModel.arborescence.Edmonds;

public class WFnet2Processes {
	
	private NetSystem sys = null;
	private AdjacencyList sysList = null;
	private AdjacencyList minList = null;
	
	private Map<ALNode,Node> a2p = new HashMap<ALNode, Node>();
	private Map<Node,ALNode> p2a = new HashMap<Node, ALNode>();
	private Map<ALNode,ALNode> tree = new HashMap<ALNode, ALNode>();
	private Node proot = null;
	private ALNode aroot = null;
	private List<List<ALNode>> paths = null;
	private List<Process> processes = null;
	private List<OccurrenceNet> nets = null;
	private List<List<Node>> pnPaths = null;
	
	public List<List<Node>> getPetriNetPaths() {
		return this.pnPaths;
	}
	
	public List<OccurrenceNet> getNets() {
		return this.nets;
	}
	
	
	public WFnet2Processes(NetSystem sys) throws IOException {
		if (sys == null) return;
		
		PetriNetStructuralChecks<Flow,Node,Place,Transition> pnsc = new PetriNetStructuralChecks<Flow, Node, Place, Transition>();
		if (!pnsc.isWorkflowNet(sys)) return;
		
		this.sys = sys;
		this.sys.loadNaturalMarking();
		
		this.constructSystemList();
		this.constructMinList();
		System.out.println(this.getNumberOfNodes(this.minList));
		this.constructTree();
		this.constructPaths();
		System.out.println(this.getNumberOfNodes(this.paths));
		this.constructProcesses();
		this.constructPNpaths();
		this.constructPetriNets();
	}

	private void constructPetriNets() {
		Collections.sort(this.processes, new Comparator<Process>(){
	        public int compare(Process o1, Process o2) {
	            if (o1.getEvents().size() > o2.getEvents().size())
	                return -1;
	            else if (o1.getEvents().size() < o2.getEvents().size())
	            	return 1;
	            else
	            	return 0;
	        }
	    });
		
		Set<Flow> visited = new HashSet<Flow>(); 
		this.nets = new ArrayList<OccurrenceNet>();

		for (Process pi : this.processes) {
			OccurrenceNet net = (OccurrenceNet) pi.getOccurrenceNet();
			
			Set<Flow> toRemove = new HashSet<Flow>();
			for (Flow flow : visited) {
				for (Flow f : net.getFlow()) {
					if (f.getSource().getName().substring(0,f.getSource().getName().lastIndexOf("-")).equals(flow.getSource().getName().substring(0,flow.getSource().getName().lastIndexOf("-"))) &&
							f.getTarget().getName().substring(0,f.getTarget().getName().lastIndexOf("-")).equals(flow.getTarget().getName().substring(0,flow.getTarget().getName().lastIndexOf("-")))) {
						toRemove.add(f);
					}
				}
			}
			net.removeFlow(toRemove);
			net.removeVertices(net.getDisconnectedVertices());
			
			visited.addAll(net.getFlow());
			
			if (!net.getNodes().isEmpty())
				nets.add(net);
		}
	}


	private void constructPNpaths() {
		Collections.sort(this.paths, new Comparator<List<ALNode>>(){
	        public int compare(List<ALNode> o1, List<ALNode> o2) {
	            if (o1.size() > o2.size())
	                return -1;
	            else if (o1.size() < o2.size())
	            	return 1;
	            else
	            	return 0;
	        }
	    });
		
		Set<ALNode> visited = new HashSet<ALNode>();
		pnPaths = new ArrayList<List<Node>>();
		for (List<ALNode> path : this.paths) {
			List<Node> pnPath = new ArrayList<Node>();
			for (ALNode n : path) {
				if (visited.contains(n)) continue;
				visited.add(n);
				pnPath.add(a2p.get(n));
			}
			this.pnPaths.add(pnPath);
		}
	}

	private void serializeProcesses() throws IOException {
		this.sys.loadNaturalMarking();
		
		IOUtils.invokeDOT(".", "sys.png", sys.toDOT());
		
		int c = 1;
		for (Process pi : this.processes) {
			IOUtils.invokeDOT(".", "pi"+(c++)+".png", pi.getOccurrenceNet().toDOT());
		}
	}
	
	private void serializePaths() {
		for (List<ALNode> path : this.paths) {
			for (ALNode n : path) {
				System.out.print(this.a2p.get(n) + " ");
			}
			System.out.println();
		}
	}


	private void constructProcesses() {
		this.processes = new ArrayList<Process>();
		
		for (List<ALNode> path : this.paths) {
			this.sys.loadNaturalMarking();
			Process pi = new Process(sys);
			pi.constructInitialBranchingProcess();
			
			Set<Transition> fired = new HashSet<Transition>();
			
			for (ALNode n : path) {
				Node nn = a2p.get(n);
				
				if (nn instanceof Place) continue;
				
				Transition t = (Transition) nn;
				Set<Place> preT = sys.getPreset(t);
				
				Set<Transition> enabled = sys.getEnabledTransitions();
				
				while (!enabled.contains(t)) {					
					Set<Transition> toRemove = new HashSet<Transition>();
					
					for (Transition tt : enabled) {
						for (Place p : preT) {
							if (this.sys.getPreset(tt).contains(p)) {
								toRemove.add(tt);
								break;
							}
						}
						
						if (fired.contains(tt))
							toRemove.add(tt);
					}
					
					enabled.removeAll(toRemove);
					
					if (enabled.isEmpty()) break;
					
					Transition tt = enabled.iterator().next();
					sys.fire(tt);
					pi.appendTransition(tt);
					fired.add(tt);
					enabled = sys.getEnabledTransitions();
				}
				
				if (enabled.isEmpty()) break;
				
				if (enabled.contains(t)) {
					sys.fire(t);
					pi.appendTransition(t);
					fired.add(t);
				}
			}
			
			this.processes.add(pi);
		}
	}

	private void constructPaths() {
		this.paths = new ArrayList<List<ALNode>>();
		
		Set<ALNode> nodes = new HashSet<ALNode>();
		for (ALEdge edge : this.minList.getAllEdges()) {
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());
		}
		
		//System.out.println(this.minList.getAllEdges());
		nodes.removeAll(this.minList.getSourceNodeSet());
		//System.out.println(this.minList.getSourceNodeSet());
		//System.out.println(nodes);
		//System.out.println(nodes.size());
		
		for (ALNode n : nodes) {
			List<ALNode> path = new ArrayList<ALNode>();
			path.add(n);
			
			while(tree.get(n)!=null) {
				n = tree.get(n);
				path.add(0,n);
			}
			
			paths.add(path);
		}
	}

	private void constructTree() {
		for (ALEdge edge : this.minList.getAllEdges()) {
			this.tree.put(edge.getTarget(), edge.getSource());
		}
	}


	private void constructMinList() {
		Edmonds e = new Edmonds();
		
		this.proot = this.sys.getSourceNodes().iterator().next();
		this.aroot = this.p2a.get(proot);
		
		this.minList = e.getMinBranching(this.aroot, this.sysList);
	}


	private int getNumberOfNodes(AdjacencyList list) {
		Set<ALNode> nodes = new HashSet<ALNode>();
		for (ALEdge edge : list.getAllEdges()) {
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());
		}
		return nodes.size();
	}
	
	private int getNumberOfNodes(List<List<ALNode>> paths) {
		Set<ALNode> nodes = new HashSet<ALNode>();
		for (List<ALNode> path : paths) {
			for (ALNode n : path)
				nodes.add(n);
		}
		
		return nodes.size();
	}

	private void constructSystemList() {
		this.sysList = new AdjacencyList();
		
		int c = 1;
		for (Flow f: this.sys.getFlow()) {
			Node src = f.getSource();
			ALNode asrc = p2a.get(src);
			if (asrc==null) {
				asrc = new ALNode(c++);
				p2a.put(src,asrc);
				a2p.put(asrc,src);
			}
			
			Node tgt = f.getTarget();
			ALNode atgt = p2a.get(tgt);
			if (atgt==null) {
				atgt = new ALNode(c++);
				p2a.put(tgt,atgt);
				a2p.put(atgt,tgt);
			}
			
			this.sysList.addEdge(asrc,atgt,1);
		}
	}
	

}

package dataModel.arborescence;
import java.util.ArrayList;
import java.util.Iterator;

public class Edmonds {

   private ArrayList<ALNode> cycle;

   public AdjacencyList getMinBranching(ALNode root, AdjacencyList list){
       AdjacencyList reverse = list.getReversedList();
       // remove all edges entering the root
       if(reverse.getAdjacent(root) != null){
           reverse.getAdjacent(root).clear();
       }
       AdjacencyList outEdges = new AdjacencyList();
       // for each node, select the edge entering it with smallest weight
       for(ALNode n : reverse.getSourceNodeSet()){
           ArrayList<ALEdge> inEdges = reverse.getAdjacent(n);
           if(inEdges.isEmpty()) continue;
           ALEdge min = inEdges.get(0);
           for(ALEdge e : inEdges){
               if(e.weight < min.weight){
                   min = e;
               }
           }
           outEdges.addEdge(min.to, min.from, min.weight);
       }

       // detect cycles
       ArrayList<ArrayList<ALNode>> cycles = new ArrayList<ArrayList<ALNode>>();
       cycle = new ArrayList<ALNode>();
       getCycle(root, outEdges);
       cycles.add(cycle);
       for(ALNode n : outEdges.getSourceNodeSet()){
           if(!n.visited){
               cycle = new ArrayList<ALNode>();
               getCycle(n, outEdges);
               cycles.add(cycle);
           }
       }

       // for each cycle formed, modify the path to merge it into another part of the graph
       AdjacencyList outEdgesReverse = outEdges.getReversedList();

       for(ArrayList<ALNode> x : cycles){
           if(x.contains(root)) continue;
           mergeCycles(x, list, reverse, outEdges, outEdgesReverse);
       }
       return outEdges;
   }

   private void mergeCycles(ArrayList<ALNode> cycle, AdjacencyList list, AdjacencyList reverse, AdjacencyList outEdges, AdjacencyList outEdgesReverse){
       ArrayList<ALEdge> cycleAllInEdges = new ArrayList<ALEdge>();
       ALEdge minInternalEdge = null;
       // find the minimum internal edge weight
       for(ALNode n : cycle){
           for(ALEdge e : reverse.getAdjacent(n)){
               if(cycle.contains(e.to)){
                   if(minInternalEdge == null || minInternalEdge.weight > e.weight){
                       minInternalEdge = e;
                       continue;
                   }
               }else{
                   cycleAllInEdges.add(e);
               }
           }
       }
       // find the incoming edge with minimum modified cost
       ALEdge minExternalEdge = null;
       int minModifiedWeight = 0;
       for(ALEdge e : cycleAllInEdges){
           int w = e.weight - (outEdgesReverse.getAdjacent(e.from).get(0).weight - minInternalEdge.weight);
           if(minExternalEdge == null || minModifiedWeight > w){
               minExternalEdge = e;
               minModifiedWeight = w;
           }
       }
       // add the incoming edge and remove the inner-circuit incoming edge
       ALEdge removing = outEdgesReverse.getAdjacent(minExternalEdge.from).get(0);
       outEdgesReverse.getAdjacent(minExternalEdge.from).clear();
       outEdgesReverse.addEdge(minExternalEdge.to, minExternalEdge.from, minExternalEdge.weight);
       ArrayList<ALEdge> adj = outEdges.getAdjacent(removing.to);
       for(Iterator<ALEdge> i = adj.iterator(); i.hasNext(); ){
           if(i.next().to == removing.from){
               i.remove();
               break;
           }
       }
       outEdges.addEdge(minExternalEdge.to, minExternalEdge.from, minExternalEdge.weight);
   }

   private void getCycle(ALNode n, AdjacencyList outEdges){
       n.visited = true;
       cycle.add(n);
       if(outEdges.getAdjacent(n) == null) return;
       for(ALEdge e : outEdges.getAdjacent(n)){
           if(!e.to.visited){
               getCycle(e.to, outEdges);
           }
       }
   }
}

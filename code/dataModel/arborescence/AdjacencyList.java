package dataModel.arborescence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdjacencyList {

   private Map<ALNode, ArrayList<ALEdge>> adjacencies = new HashMap<ALNode, ArrayList<ALEdge>>();

   public void addEdge(ALNode source, ALNode target, int weight){
       ArrayList<ALEdge> list;
       if(!adjacencies.containsKey(source)){
           list = new ArrayList<ALEdge>();
           adjacencies.put(source, list);
       }else{
           list = adjacencies.get(source);
       }
       list.add(new ALEdge(source, target, weight));
   }

   public ArrayList<ALEdge> getAdjacent(ALNode source){
       return adjacencies.get(source);
   }

   public void reverseEdge(ALEdge e){
       adjacencies.get(e.from).remove(e);
       addEdge(e.to, e.from, e.weight);
   }

   public void reverseGraph(){
       adjacencies = getReversedList().adjacencies;
   }

   public AdjacencyList getReversedList(){
       AdjacencyList newlist = new AdjacencyList();
       for(ArrayList<ALEdge> edges : adjacencies.values()){
           for(ALEdge e : edges){
               newlist.addEdge(e.to, e.from, e.weight);
           }
       }
       return newlist;
   }

   public Set<ALNode> getSourceNodeSet(){
       return adjacencies.keySet();
   }

   public Collection<ALEdge> getAllEdges(){
       ArrayList<ALEdge> edges = new ArrayList<ALEdge>();
       for(List<ALEdge> e : adjacencies.values()){
           edges.addAll(e);
       }
       return edges;
   }
}

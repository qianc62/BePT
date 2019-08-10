package dataModel.arborescence;

import org.junit.Test;

public class ArboTest {

	@Test
	public void test() {
		ALNode n1 = new ALNode(1);
		ALNode n2 = new ALNode(2);
		ALNode n3 = new ALNode(3);
		ALNode n4 = new ALNode(4);
		ALNode n5 = new ALNode(5);
		
		AdjacencyList list = new AdjacencyList();
		list.addEdge(n1,n2,1);
		list.addEdge(n2,n3,1);
		list.addEdge(n2,n4,1);
		list.addEdge(n3,n4,1);
		list.addEdge(n3,n5,1);
		list.addEdge(n4,n5,1);
		
		Edmonds edmonds = new Edmonds();
		AdjacencyList arb = edmonds.getMinBranching(n1,list);
		System.out.println(arb.getAllEdges());
	}

}

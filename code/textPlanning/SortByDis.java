package textPlanning;

import de.hpi.bpt.process.Node;

public class SortByDis {
	
	public int compare(Object o1, Object o2) {
		
		Node i1 = (Node)o1;
		Node i2 = (Node)o2;
		
		return Integer.valueOf( i1.getId() ) - Integer.valueOf( i2.getId() );
	}
}

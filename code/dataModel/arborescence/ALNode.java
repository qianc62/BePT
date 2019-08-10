package dataModel.arborescence;




public class ALNode implements Comparable<ALNode> {

	final int name;
    boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
    int lowlink = -1;          // used for Tarjan's algorithm
    int index = -1;            // used for Tarjan's algorithm
    
    public ALNode(final int argName) {
        name = argName;
    }
    
    public int compareTo(final ALNode argNode) {
        return argNode == this ? 0 : -1;
    }
    
    public int getName() {
    	return this.name;
    }
    
    @Override
	public String toString() {
		return Integer.toString(this.name);
	}
 }

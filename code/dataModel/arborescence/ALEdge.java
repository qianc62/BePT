package dataModel.arborescence;



public class ALEdge implements Comparable<ALEdge> {
   
	final ALNode from, to;
    final int weight;
    
    public ALEdge(final ALNode argFrom, final ALNode argTo, final int argWeight){
        from = argFrom;
        to = argTo;
        weight = argWeight;
    }
    
    public int compareTo(final ALEdge argEdge){
        return weight - argEdge.weight;
    }
    
    @Override
	public String toString() {
		return "("+from.name + ","+to.name+")";
	}
    
    public ALNode getSource() {
    	return this.from;
    }
    
    public ALNode getTarget() {
    	return this.to;
    }
 }

package qc;

public class QcEdge {
    public String id;
    public String name;
    public QcNode source;
    public QcNode target;

    public QcEdge() {
        id = "";
        this.source = null;
        this.target = null;
    }

    public QcEdge( QcNode node1, QcNode node2 ) {
        this.id = node1.id + "_to_" + node2.id;
        this.name = node1.name + "_to_" + node2.name;
        source = node1;
        target = node2;
    }

    public boolean isSameName( QcEdge edge_ ){
        if ( this.source.isSameName(edge_.source) && this.target.isSameName(edge_.target) ) {
            return true;
        }
        return false;
    }

    public boolean isSamePrefixName( QcEdge edge_ ){

        if ( this.source.isSamePrefixName(edge_.source) && this.target.isSamePrefixName(edge_.target) ) {
            return true;
        }
        return false;
    }
}
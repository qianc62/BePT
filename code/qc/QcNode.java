package qc;

import qc.common.Common;

public class QcNode {
    public String id;
    public String name;
    public String shapeType;
    public String dyeType;

    public QcNode(){
        id = "";
        name = "";
        shapeType = Common.NullType;
        dyeType = Common.NullType;
    }

    public QcNode( String id_, String name_, String shapeType_, String dyeType_ ){
        this.id = id_;
        this.name = name_;
        this.shapeType = shapeType_;
        this.dyeType = dyeType_;
    }

    public QcNode( QcNode node_ ){
        this.id = node_.id;
        this.name = node_.name;
        this.shapeType = node_.shapeType;
        this.dyeType = node_.dyeType;
    }

    public boolean isSameId( QcNode node ){
        String s1 = this.id;
        String s2 = node.id;
        if ( s1.equals(s2) ) {
            return true;
        }
        return false;
    }

    public boolean isSameName( QcNode node ){
        String s1 = this.name;
        String s2 = node.name;
        if ( s1.equals(s2) ) {
            return true;
        }
        return false;
    }

    public boolean isSamePrefixName( QcNode node ){
        String s1 = Common.getPrefix( this.name );
        String s2 = Common.getPrefix( node.name );
        if ( s1.equals(s2) ) {
            return true;
        }
        return false;
    }

    public boolean isSameDyeType( QcNode node ){
        int type1 = this.getDyetype();
        int type2 = node.getDyetype();

        if ( type1 == type2 ) {
            return true;
        }
        return false;
    }

    public int getDyetype(){
        switch( this.dyeType ){
            case Common.StartNode:  return 1;
            case Common.EndNode:    return 1;
            case Common.ShadowNode: return 2;
            default:                return 0;
        }
    }

    public String toString() {
        return this.id;
    }
}

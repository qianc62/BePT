package textPlanning;

import java.util.ArrayList;

public class GGGatewayTypes {

	int attribute = 0;
	int split_join = 0;
	
	static public int AND = 1;
	static public int XOR = 2;
	static public int OR = 3;
	static public int COM = 4;
	static public int EVENT = 5;
	
	static public int SPLIT = 6;
	static public int JOIN = 7;
	
	public void setAttribute( int attribute_ ){
		attribute = attribute_;
	}
	
	public void setSplit_join( int split_join_ ){
		split_join = split_join_;
	}
	
	public void setType( int attribute_, int split_join_ ){
		attribute = attribute_;
		split_join = split_join_;
	}
	
	public int getAttribute( ){
		return attribute;
	}
	
	public int getSplit_join( ){
		return split_join;
	}
}

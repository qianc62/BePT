package dataModel.process;

import java.sql.Date;
import java.util.HashMap;

public abstract class Element {
	
	private int id;
	private String label;
	private String original_label;
	private Lane lane;
	private Pool pool;
	private int subProcessID;
	
	private String descriptionString;
	private int descriptionNum;
	private static int descriptionNUM = 1;
	
	private HashMap<Integer,Artifact> artifacts;
	private HashMap<Integer,Data> datas;
	private HashMap<Integer,Integer> direction;
	
	private int nlgNum = 0;

	public Element(int id, String label, Lane lane, Pool pool) {
		this.id = id;
		this.label = label;
		this.lane = lane;
		this.pool = pool;
		this.subProcessID = -1;
		descriptionString = "";
		artifacts = new HashMap<Integer,Artifact>();
		datas = new HashMap<Integer,Data>();
		direction = new HashMap<Integer,Integer>();
	}
	
	public void setID( int id ){
		this.id = id;
	}
	
	public void setData( Data data ){
		datas.put( data.getId() , data );
	}
	
	public HashMap<Integer,Data> getData( ){
		return datas;
	}
	
	public void setDirection( Integer id , Integer integer ){
		direction.put( id , integer );
	}
	
	public Integer getDirection( Integer id ){
		return direction.get( id );
	}
	
	public void setArtifact( Artifact artifact ){
		artifacts.put( artifact.getId() , artifact );
	}
	
	public HashMap<Integer,Artifact> getArtifact(){
		return artifacts;
	}
	
	public int getTextAnnotationNum(){
		int count = 0;
		for( Artifact artifact : artifacts.values() ){
			if( artifact.getType() == ArtifactType.TEXTANNOTATION ){
				count++;
			}
		}
		return count;
	}
	
	public int getITSystemNum(){
		int count = 0;
		for( Artifact artifact : artifacts.values() ){
			if( artifact.getType() == ArtifactType.ITSYSTEM ){
				count++;
			}
		}
		return count;
	}
	
	public int getDataInNum(){
		int count = 0;
		for( Data data : datas.values() ){
			if( direction.get( data.getId() ) == Direction.IN ){
				count++;
			}
		}
		return count;
	}
	
	public int getDataOutNum(){
		int count = 0;
		for( Data data : datas.values() ){
			if( direction.get( data.getId() ) == Direction.OUT ){
				count++;
			}
		}
		return count;
	}
	
	public void setDescriptionNum( ){
		descriptionNum = descriptionNUM++;
	}
	
	public int getDescriptionNum(){
		return descriptionNum;
	}
	
	public void setDescriptionString( String des ){
		descriptionString = des;
		setDescriptionNum();
	}
	
	public String getDescriptionString(){
		return descriptionString;
	}
	
	public int getSubProcessID() {
		return this.subProcessID;
	}
	
	public void setSubProcessID(int id) {
		this.subProcessID = id;
	}

	public Pool getPool() {
		return pool;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}
	
	public void setLabel( String str ) {
		label = str;
	}
	
	public String getOriginalLabel() {
		return original_label;
	}
	
	public void setOriginalLabel( String str ) {
		original_label = str;
	}

	public Lane getLane() {
		return lane;
	}

	public void setLane(Lane lane) {
		this.lane = lane;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}
	
	//By Chen Qian
	public String getAddtion(){
		String addtion = "";
		
		//文本注释
		if( getTextAnnotationNum() > 0 ){
			addtion += " ( ";
			int count = 0;
			for( Artifact artifact : getArtifact().values() ){
				if( ++count > 1 ){
					addtion += " , ";
				}
				if( artifact.getType() == ArtifactType.TEXTANNOTATION ){
					addtion += " \"" + artifact.getLabel() + "\" ";
				}
			}
			addtion += " ) ";
		}
		
		//IT系统
		if( getITSystemNum() > 0  ){
			addtion += ", this activity needs to be processed by ";
			int count = 0;
			for( Artifact artifact : getArtifact().values() ){
				if( ++count > 1 ){
					addtion += " and ";
				}
				if( artifact.getType() == ArtifactType.ITSYSTEM ){
					addtion += " \"" + artifact.getLabel() + "\" ";
				}
			}
		}
		
		int inNum = getDataInNum();
		int outNum = getDataOutNum();
		
		//数据对象、数据存储输入
		if( inNum > 0  ){
			addtion += ", and this activity needs input data from ";
			int count = 0;
			for( Data data : getData().values() ){
				if( this.getDirection( data.getId() ) != Direction.IN ){
					continue;
				}
				if( ++count > 1 ){
					addtion += " and ";
				}
				if( data.getType() == DataType.DATAOBJECT ){
					addtion += " object \"" + data.getLabel() + "\" ";
				}
				else if( data.getType() == DataType.DATASTORE ){
					addtion += " store \"" + data.getLabel() + "\" ";
				}
			}
		}
		
		//数据对象、数据存储输出
		if( outNum > 0  ){
			if( inNum == 0 ){
				addtion += ", and this activity outputs data into ";
			} else {
				addtion += ", then it outputs datas to ";
			}
			int count = 0;
			for( Data data : getData().values() ){
				if( this.getDirection( data.getId() ) != Direction.OUT ){
					continue;
				}
				if( ++count > 1 ){
					addtion += " and ";
				}
				if( data.getType() == DataType.DATAOBJECT ){
					addtion += " object \"" + data.getLabel() + "\" ";
				}
				else if( data.getType() == DataType.DATASTORE ){
					addtion += " store \"" + data.getLabel() + "\" ";
				}
			}
		}
		
		//多实例
		if( this instanceof Activity ){
			Activity activity = (Activity)this;
			if( activity.getType() == ActivityType.MULTI ){
				addtion += ", and this activity can be simultaneously processed by many instances ";
			}
		}
		
		return addtion;
	}
	
	public void setNlgNum( ){
		nlgNum++;
	}
	
	public int getNlgNum( ){
		return nlgNum;
	}
}

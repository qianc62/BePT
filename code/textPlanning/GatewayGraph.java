package textPlanning;

import java.awt.geom.Arc2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

//import org.apache.xpath.operations.Bool;
import org.junit.experimental.theories.Theories;

import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;

import dataModel.dsynt.DSynTMainSentence;
import dataModel.dsynt.DSynTSentence;
import dataModel.intermediate.ExecutableFragment;
import dataModel.process.Activity;
import dataModel.process.ActivityType;
import dataModel.process.Arc;
import dataModel.process.EPSF;
import dataModel.process.Element;
import dataModel.process.Event;
import dataModel.process.Lane;
import dataModel.process.Pool;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import net.didion.jwnl.JWNLException;
import qc.QcPrintDsynT;
import sentencePlanning.DiscourseMarker;
import sentencePlanning.ReferringExpressionGenerator;
import sentencePlanning.SentenceAggregator;
import sentenceRealization.SurfaceRealizer;
import textPlanning.recordClasses.ModifierRecord;
import de.hpi.bpt.process.Gateway;
import de.hpi.bpt.process.GatewayType;

public class GatewayGraph {

	//模型
	ProcessModel processModel;
	EPSF epsf;

	RPST<ControlFlow, Node> rpst;
	
	public ArrayList<Node> idMap;
	public int[][] GG;
	public int GGNodeNum;
	public GGGatewayTypes[] gatewayTypes;
	
	public int[][] changedGG , saveChangedGG;
	public int changedGGNodeNum;
	
	//矫正
	public ArrayList<ArrayList<Integer>> correctedEdges;
	
	//分裂
	int deltaGatewayNum; 
	int[] corGatewayID;
	
	//匹配
	public boolean[] alive;
	ArrayList<ArrayList<Integer>> goals;
	int[][] floyedRecord;
	
	//转为ProcessModel
	ProcessModel rProcessModel;
	EPSF rEpsf;
	RPST<ControlFlow, Node> rRpst;
	Pool pool;
	Lane lane;
	int modelGatewayNum, modelArcNum, modelActivityNum;
	HashMap<Integer,ArrayList<dataModel.process.Gateway>> modelIdMap;
	
	//两个BPMN2.0模型间的ID映射
	HashMap<Integer, Integer> bpmnGatewayIdMap;
	HashMap<Integer, Integer> bpmnActivityIdMap;
	HashMap<Integer, Integer> bpmnArcIdReverseMap;
	
	//临时变量
	RPSTNode<ControlFlow,Node> savedNode;
		
	//文本
	TextPlanner rConverter;
	
	//描述次数
	int[][] descriptionNum;
	ArrayList<Activity> omissiveActivities;
	ArrayList<Activity> repetitiveActivities;
	ArrayList<Arc> omissiveEdges;
	
	//初始化
	public void init(){
		processModel = null;
		rpst = null;
		epsf = null;
		
		idMap = new ArrayList<Node>();
		GGNodeNum = -1;
		GG = null;
		gatewayTypes = null;
		
		changedGG = null;
		saveChangedGG = null;
		changedGGNodeNum = -1;
		
		correctedEdges = null;
		
		deltaGatewayNum = 0;
		corGatewayID = null;
		
		alive = null;
		
		goals = null;
		floyedRecord = null;
		
		rProcessModel = null;
		modelIdMap = null;
		pool = null;
		lane = null;
		
		rEpsf = null;
		rRpst = null;
		
		bpmnGatewayIdMap = null;
		bpmnActivityIdMap = null;
		bpmnArcIdReverseMap = null;
		
		rConverter = null;
		
		descriptionNum = null;
		omissiveEdges = null;
		omissiveActivities = null;
		repetitiveActivities = null;
	}
	
	//初始矩阵
	public void initMatrix( int[][] mat, int n ){
		for( int i=0 ; i<mat.length ; i++ ){
			Arrays.fill( mat[i] , n );
		}
	}
	
	//打印输出,flag表示是否改变过原模型
	public void print( int[][] map ){
		
		System.out.println( map.length + " " + alive.length );
		
		for( int i=0 ; i<map.length ; i++ ){
			for( int j=0 ; j<map[i].length ; j++ ){
				if( alive[i] == false || alive[j] == false ){
					System.out.print( "×" );
				}
				else if( map[i][j] == Integer.MAX_VALUE ){
					System.out.print( "~" );
				}
				else{
					System.out.print( map[i][j] );
				}
				System.out.print( " " );
			}
			System.out.print( "\n" );
		}
		System.out.println();
	}
	
	//将R区域映射为Gateway Graph，并记录映射ID
	public GatewayGraph(ProcessModel process_, EPSF epsf_, RPST<ControlFlow,Node> rpst_, RPSTNode<ControlFlow,Node> node ) {

		init();
		
		processModel = process_;
		epsf = epsf_;
		rpst = rpst_;
		
		rProcessModel = null;
		rEpsf = null;
		rRpst = null;
		
		idMap = new ArrayList<Node>(); 
		
		//提取所有结点的ID
		int num = 0;
		Object[] childs = rpst.getChildren( node ).toArray();
		for( int i=0 ; i<childs.length ; i++ ){
			RPSTNode<ControlFlow,Node> child = ((RPSTNode<ControlFlow,Node>)childs[i]);
			Node entry = child.getEntry();
			Node exit = child.getExit();
			
			//每次运行结果不一定一样
			System.out.println( "Entry:" + entry.getId() + " Exit:" + exit.getId() );
			
			if( idMap.contains( entry ) == false ){
				idMap.add( entry );
				num++;
			}
			if( idMap.contains( exit ) == false ){
				idMap.add( exit );
				num++;
			}
		}
		
		Collections.sort( idMap, new SortByID() );
		
		for( int i=0 ; i<idMap.size() ; i++ ){
			System.out.println( "Node " + idMap.get( i ).getId() + " : " + i );
		}
		
		GG = new int[num][num];
		
		initMatrix( GG, Integer.MAX_VALUE );
		
		for( int i=0 ; i<childs.length ; i++ ){
			RPSTNode<ControlFlow,Node> child = ((RPSTNode<ControlFlow,Node>)childs[i]);
			Node entry = child.getEntry();
			Node exit = child.getExit();
			
			GG[idMap.indexOf(entry)][idMap.indexOf(exit)] = 1;
		}
		
		for( int i=0 ; i<GG.length ; i++ ){
			GG[i][i] = 0;
		}
		
		gatewayTypes = new GGGatewayTypes[GG.length];
		for( int i=0 ; i<gatewayTypes.length ; i++ ){
			gatewayTypes[i] = new GGGatewayTypes();
		}
		
		for( int i=0 ; i<idMap.size() ; i++ ){
			Node node1 = idMap.get( i );
			GatewayType type = ((Gateway)node1).getGatewayType();
			if( type == GatewayType.AND ){
				gatewayTypes[i].setAttribute( GGGatewayTypes.AND );
			}
			if( type == GatewayType.XOR ){
				gatewayTypes[i].setAttribute( GGGatewayTypes.XOR );		
			}
			if( type == GatewayType.OR ){
				gatewayTypes[i].setAttribute( GGGatewayTypes.OR );
			}
			if( type == GatewayType.CGT ){
				gatewayTypes[i].setAttribute( GGGatewayTypes.COM );
			}
			if( type == GatewayType.EVENT ){
				gatewayTypes[i].setAttribute( GGGatewayTypes.EVENT );
			}
		}
	}

	//创建可变的网关矩阵
	/**
	 * {@p0} input
	 * {@p1} output
	 */
	public void createChangedGG(){
		
		changedGG = new int[GG.length][GG.length];
		changedGGNodeNum = changedGG.length;
		
		for (int i = 0; i < GG.length; i++) {
			for (int j = 0; j < GG[i].length; j++) {
				changedGG[i][j] = GG[i][j];
			}
		}
	}
	
	//找到目前网关图的入口结点集，带有标记
	public ArrayList<Integer> findEntryList( int[][] map ){
		
		ArrayList<Integer> entryList = new ArrayList<Integer>();
		
		for( int u=0 ; u<map.length ; u++ ){
			if( alive[u] == true && getInEdgesList( map, u ).size() == 0 ){
				entryList.add( u );
			}
		}
		
		return entryList;
	}
	
	//找到目前网关图的出口结点集
	public ArrayList<Integer> findExitList( int[][] map ){
		ArrayList<Integer> exitList = new ArrayList<Integer>();
		
		for( int v=0 ; v<map.length ; v++ ){
			if( alive[v] == true && getOutEdgesList( map, v ).size() == 0 ){
				exitList.add( v );
			}
		}

		return exitList;
	}
	
	//找到目前网关图中u结点的出边集
	public ArrayList<Integer> getOutEdgesList( int[][] gg, int u ){
		ArrayList<Integer> outEdgesList = new ArrayList<Integer>();
		for( int v=0 ; v<gg[u].length ; v++ ){
			if( gg[u][v] == 1 ){
				outEdgesList.add( v );
			}
		}
		return outEdgesList;
	}
	
	//找到模型中元素的出边
	public ArrayList<Arc> getOutEdgesList( ProcessModel pm , dataModel.process.Element g ){
		ArrayList<Arc> arcsList = new ArrayList<Arc>();
		Object[] arcs = pm.getArcs().values().toArray();
		
		for( int i=0 ; i<arcs.length ; i++ ){
			Arc arc = ( Arc )arcs[i];
			if( arc.getSource().getId() == g.getId() ){
				arcsList.add( arc );
			}
		}
		
		return arcsList;
	}
	
	//找到模型中元素的入边
	public ArrayList<Arc> getInEdgesList( ProcessModel pm , dataModel.process.Element g ){
		ArrayList<Arc> arcsList = new ArrayList<Arc>();
		Object[] arcs = pm.getArcs().values().toArray();
		
		for( int i=0 ; i<arcs.length ; i++ ){
			Arc arc = ( Arc )arcs[i];
			if( arc.getTarget().getId() == g.getId() ){
				arcsList.add( arc );
			}
		}
		
		return arcsList;
	}
	
	//找到模型中元素的出元素
	public ArrayList<Element> getOutEdgesElements( ProcessModel pm , dataModel.process.Element g ){
		
		ArrayList<Arc> arcsList = getOutEdgesList( pm, g );
		ArrayList<Element> elementsList = new ArrayList<Element>();
		
		for( Arc arc : arcsList ){
			elementsList.add( arc.getTarget() );
		}
		
		return elementsList;
	}
	
	//找到模型中元素的入元素
	public ArrayList<Element> getInEdgesElements( ProcessModel pm , dataModel.process.Element g ){
		
		ArrayList<Arc> arcsList = getInEdgesList( pm, g );
		ArrayList<Element> elementsList = new ArrayList<Element>();
		
		for( Arc arc : arcsList ){
			elementsList.add( arc.getSource() );
		}
		
		return elementsList;
	}	
		
	//找到目前网关图中v结点的入边集
	public ArrayList<Integer> getInEdgesList( int[][] gg, int v ){
		ArrayList<Integer> inEdgesList = new ArrayList<Integer>();
		for( int u=0 ; u<gg.length ; u++ ){
			if( gg[u][v] == 1 ){
				inEdgesList.add( u );
			}
		}
		return inEdgesList;
	}
		
	//BFS矫正可能造成循环的边
	public void correctLoopEdges( ){
		
		alive = new boolean[changedGG.length];
		Arrays.fill( alive , true );
		
		floyedRecord = new int[changedGG.length][changedGG.length];
		initMatrix( floyedRecord , Integer.MAX_VALUE );
		Floyed( changedGG, floyedRecord );
		print( changedGG );
		
		int entry = findEntryList( changedGG ).get( 0 );
		correctedEdges = new ArrayList<ArrayList<Integer>>();
		
		boolean[] colored = new boolean[changedGG.length];
		Arrays.fill( colored, false );
		
		ArrayList<Integer> Q = new ArrayList<Integer>();
		Q.add( entry );
		colored[entry] = true;
		
		while( Q.isEmpty() == false ){
			int v = Q.get( 0 );
			Q.remove( 0 );
			
			ArrayList<Integer> outEdges = getOutEdgesList( changedGG, v );
			
			//可能存在重边
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			for( int i=0 ; i<changedGG.length ; i++ ){
				if( i == v ){
					continue;
				}
				if( reachability( changedGG, v, i ) && reachability( changedGG, i, v ) && colored[i] == false ){
					boolean flag = true;
					for( int k = 0 ; k<candidates.size() ; k++ ){
						if( reachability( changedGG, candidates.get( k ), i ) ){
							flag = false;
							break;
						}
					}
					if( flag == true ){
						candidates.add( i );
					}
				}
			}
			
			for( int k : candidates ){
				ArrayList<ArrayList<Integer>> pathes = getPath( changedGG, k, v );
				
				for( ArrayList<Integer> path : pathes ){
					for( int i=0 ; i<path.size() - 1 ; i++ ){
						int u1 = path.get( i );
						int v1 = path.get( i+1 );
						
						changedGG[u1][v1] = Integer.MAX_VALUE;
						changedGG[v1][u1] = 1;
						
						ArrayList<Integer> correctedEdge = new ArrayList<Integer>();
						correctedEdge.add( v1 );
						correctedEdge.add( u1 );
						System.out.println( "矫正了" + v1 + "→" + u1 );
						correctedEdges.add( correctedEdge );
					}
				}
			}
			
			if( candidates.size() > 0 ){
				floyedRecord = new int[changedGG.length][changedGG.length];
				initMatrix( floyedRecord , Integer.MAX_VALUE );
				recoverToZeroAndOne( changedGG );
				Floyed( changedGG, floyedRecord );
				
				print( changedGG );
			}
			
			//加入队列
			for( int j=0 ; j<outEdges.size() ; j++ ){
				int k = outEdges.get( j );
				if( colored[k] == false ){
					Q.add( k );
					colored[k] = true;
				}
			}
		}
	}

	private void recoverToZeroAndOne(int[][] map) {
		for( int i=0 ; i<map.length ; i++ ){
			for( int j=0 ; j<map[i].length ; j++ ){
				if( map[i][j] > 1 ){
					map[i][j] = Integer.MAX_VALUE;
				}
			}
		}
	}

	//分裂
	public void SplitGateway( ){
		
		recoverToZeroAndOne( changedGG );
		
		deltaGatewayNum = 0;
		corGatewayID = new int[GG.length];
		for( int i=0 ; i<changedGG.length ; i++ ){
			corGatewayID[i] = i;
			if( getOutEdgesList(changedGG, i).size() > 1 && getInEdgesList(changedGG, i).size() > 1 ){
				corGatewayID[i] = GG.length + deltaGatewayNum;
				deltaGatewayNum++;
			}
		}
		
		changedGGNodeNum = changedGG.length+deltaGatewayNum;
		int newgg[][] = new int[changedGGNodeNum][changedGGNodeNum];
		for( int i=0 ; i<newgg.length ; i++ ){
			Arrays.fill( newgg[i], Integer.MAX_VALUE );
			newgg[i][i] = 0;
		}
		
		for( int i=0 ; i<GG.length ; i++ ){
			int v = corGatewayID[i];
			
			for( int j=0 ; j<changedGG[i].length ; j++ ){
				//出边
				if( i!=j && changedGG[i][j] != Integer.MAX_VALUE ){
					newgg[v][j] = 1;
				}
				//入边\无边
				else{
					newgg[i][j] = changedGG[i][j];
				}
			}
			
			//两个网关间相互连接
			newgg[i][v] = ( i == v ? 0 : 1 );
		}
		
		changedGG = newgg;
		
		//数组扩充
		alive = new boolean[changedGG.length];
		Arrays.fill( alive, true );
		
		//网关类型数组扩充
		GGGatewayTypes[] savedGatewayTypes = gatewayTypes;
		gatewayTypes = new GGGatewayTypes[changedGG.length];
		for( int i=0 ; i<gatewayTypes.length ; i++ ){
			gatewayTypes[i] = new GGGatewayTypes();
			if( i < savedGatewayTypes.length ){
				gatewayTypes[i].setAttribute( savedGatewayTypes[i].getAttribute() );
			}
		}
		for( int i=0 ; i<corGatewayID.length ; i++ ){
			if( corGatewayID[i] != i ){
				gatewayTypes[corGatewayID[i]].setAttribute( gatewayTypes[i].getAttribute() );
			}
		}
		
		System.out.println( "多入多出网关分裂后的地图" );
		print( changedGG );
		System.out.println( );
	}

	//求出多源最短路径
	public void Floyed( int[][] map, int[][] floyedRecord ){
		
		for( int k=0 ; k<map.length ; k++ ){
			for( int u=0 ; u<map.length ; u++ ){
				for( int v=0 ; v<map.length ; v++ ){
					if( map[u][v] == 0 || map[u][v] == 1 ){
						floyedRecord[u][v] = map[u][v];
					}
					if( map[u][k] == Integer.MAX_VALUE || map[k][v] == Integer.MAX_VALUE ){
						continue;
					}
					if( map[u][k] + map[k][v] < map[u][v] ){
						map[u][v] = map[u][k] + map[k][v];
						floyedRecord[u][v] = k;
					}
				}
			}
		}
	}
	
	//判断是否可达
	public boolean reachability( int[][] gg, int u, int v ){
		if( gg[u][v] == Integer.MAX_VALUE ){
			return false;
		}
		
		return true;
	}
	
	//判断是否总是可达
	public boolean alwaysReachability( int[][]gg, int u, int v ){
		int num = 0;
		ArrayList<Integer> adjacentEdges = getOutEdgesList( gg, u );
		for( int j=0 ; j<adjacentEdges.size() ; j++ ){
			int k = adjacentEdges.get( j );
			if( reachability( gg, k , v ) ){
				num++;
			}
		}
		if( num == adjacentEdges.size() ){
			return true;
		}
		
		return false;
	}
		
	//判断可达长度
	public int pathLength( int[][] gg, int u, int v ){
		return gg[u][v];
	}
	
	//设置网关类型
	public void recordInformation( ){
				
		int split_join_ = 0;
		for( int i=0 ; i<changedGG.length ; i++ ){
			int n = i;
			
			int ien = getInEdgesList( changedGG, n ).size();
			int oen = getOutEdgesList( changedGG, n ).size();
			if( ien > 1 && oen <= 1 ){
				split_join_ = GGGatewayTypes.JOIN;
			}
			else if( ien <= 1 && oen > 1 ){
				split_join_ = GGGatewayTypes.SPLIT;
			}
			
			gatewayTypes[n].setSplit_join( split_join_ );
		}
	}
	
	//全匹配
	public void matchAll( ){
		
		recordInformation();
		
		goals = new ArrayList<ArrayList<Integer>>(); 
		
		floyedRecord = new int[changedGG.length][changedGG.length];
		initMatrix( floyedRecord , Integer.MAX_VALUE );
		
		for (int i = 0; i < changedGG.length; i++) {
			ArrayList<Integer> goal = new ArrayList<Integer>();
			goals.add( goal );
			alive[i] = true;
		}
		
		//恢复
		Floyed( changedGG, floyedRecord );
		
		System.out.println( "计算了可达路径长度则：" );
		print( changedGG );
		
		saveChangedGG = new int[changedGG.length][changedGG.length];
		copy( saveChangedGG, changedGG );
		
		while( changedGGNodeNum != 0 ){
			
			System.out.println( "当前模型：" );
			print( changedGG );
			
			ArrayList<Integer> entryList = findEntryList( changedGG );
			ArrayList<Integer> exitList = findExitList( changedGG );
			
			int oneEntry = getOneEntey( entryList, exitList );
			
			System.out.print( "匹配：" + oneEntry );
			
			int g = match( oneEntry, exitList );
			goals.get( oneEntry ).add( g );
			System.out.print( " and " + g );
			
			ArrayList<Integer> outEdges = getOutEdgesList( changedGG , oneEntry );
			ArrayList<Integer> inEdges = getInEdgesList( changedGG , g );
			setEdges( oneEntry , outEdges, Integer.MAX_VALUE );
			setEdges( inEdges , g, Integer.MAX_VALUE );
			changedGGNodeNum -= 2;
			alive[oneEntry] = alive[g] = false;
			System.out.println( "\n删除该对结点则：" );
			print( changedGG );
			
			//判断合理性
			boolean leftFlag = unreasonableLeftNode( );
			boolean RightFlag = unreasonableRightNode( );
			
			if( leftFlag == false ){
				setEdges( oneEntry , outEdges, 1 );
				changedGGNodeNum += 1;
				alive[oneEntry] = true;
				System.out.println( "\n还原" + oneEntry +"结点则：" );
				print( changedGG );
			}
			if( RightFlag == false ){
				setEdges( inEdges , g, 1 );
				changedGGNodeNum += 1;
				alive[g] = true;
				System.out.println( "\n还原" + g +"结点则：" );
				print( changedGG );
			}
			
			changedGGNodeNum -= simplyfyModel( );
			processGatewayGraph( );
		}
	}
	
	private int getOneEntey( ArrayList<Integer> entryList, ArrayList<Integer> exitList ) {
		
		int min = Integer.MAX_VALUE;
		int u_ = -1;
		for( int u : entryList ){
			for( int v : exitList ){
				int len = pathLength( changedGG, u, v );
				if( len < min ){
					min = len;
					u_ = u;
				}
			}
		}
		
		return u_;
	}

	private int getOneExit(ArrayList<Integer> entryList, ArrayList<Integer> exitList) {
		
		for( int i=0 ; i<entryList.size() ; i++ ){
			int u = entryList.get( i );
			int uType = gatewayTypes[u].getAttribute();
			if( concurrentBehaviors( u ) ){
				return u;
			} 
			else{
				for( int j=0 ; j<exitList.size() ; j++ ){
					int v = exitList.get( j );
					int vType = gatewayTypes[v].getAttribute();
					if( uType == vType ){
						return u;
					}
				}
			}
		}
		
		return -1;
	}

	//匹配u的目标
	public int match( int u, ArrayList<Integer> exitList ){
		int Min = Integer.MAX_VALUE;
		int goal = -1;
		
		for( int i=0 ; i<exitList.size() ; i++ ){
			int v = exitList.get( i );
			if( alwaysReachability(changedGG, u, v) && pathLength( changedGG, u, v) < Min ){
				Min = pathLength( changedGG, u, v);
				goal = v;
			}
		}
		
		return goal;
	}
	
	//设置边集合（删除、恢复）
	public void setEdges( int u, ArrayList<Integer> edges, int n ){
		
		for( int k : edges ){
			changedGG[u][k] = n;
		}
	}
	
	public void setEdges( ArrayList<Integer> edges, int v, int n ){
		
		for( int k : edges ){
			changedGG[k][v] = n;
			if( n == Integer.MAX_VALUE && 
				concurrentBehaviors( k ) && 
				concurrentBehaviors( v ) && 
				gatewayTypes[k].split_join == GGGatewayTypes.SPLIT && 
				gatewayTypes[v].split_join == GGGatewayTypes.JOIN &&
				goals.get( k ).contains( v ) == false ){
					goals.get( k ).add( v );
					System.out.println( "添加目标：" + k + "→" + v );
			}
		}
	}
	
	//判断模型左边是否合理，返回不合理的结点
	public boolean unreasonableLeftNode( ){
		
		//Block没有入口情况
		for( int v=0 ; v<changedGG.length ; v++ ){
			if( alive[v] == false ){
				continue;
			}
			
			GGGatewayTypes type = gatewayTypes[v];
			if( type.split_join == GGGatewayTypes.JOIN  ){
				System.out.print( "JOIN(" + v + ")合理性：" );
				
				boolean ok = false;

				if( concurrentBehaviors( v ) == false ){
					System.out.println( "行为合理" );
					ok = true;
				}
				
				for( int u=0 ; ok==false && u<changedGG.length ; u++ ){
					if( alive[u] == false ){
						continue;
					}
					if( alwaysReachability( changedGG, u, v ) == true ){
						System.out.println( "存在" + u + "→" + v + "合理" );
						ok = true;
						break;
					}
				}
				
				if( ok == false ){
					return false;
				}
			}
		}
		
		return true;
	}
	
	//判断模型右边是否合理，返回不合理的结点
	public boolean unreasonableRightNode( ){

		//Block没有出口情况
		for( int u=0 ; u<changedGG.length ; u++ ){
			if( alive[u] == false ){
				continue;
			}
			
			GGGatewayTypes type = gatewayTypes[u];
			if( type.split_join == GGGatewayTypes.SPLIT ){
				
				System.out.print( "SPLIT(" + u + ")合理性：" );
				
				boolean ok = false;
				
				if( concurrentBehaviors( u ) == false ){
					System.out.println( "行为合理" );
					ok = true;
				}
				
				for( int v=0 ; ok== false && v<changedGG.length ; v++ ){
					if( alive[v] == false ){
						continue;
					}
					if( alwaysReachability( changedGG, u, v ) == true ){
						System.out.println( "存在" + u + "→" + v + "合理" );
						ok = true;
						break;
					}
				}
				
				if( ok == false ){
					return false;
				}
			}
		}
		
		return true;
	}
	
	//简化模型
	public int simplyfyModel( ){
		int num = 0;
		for( int i=0 ; i<changedGG.length ; i++ ){
			if( alive[i] == false ){
				continue;
			}
			ArrayList<Integer> inEdgesList = getInEdgesList( changedGG, i );
			ArrayList<Integer> outEdgesList = getOutEdgesList( changedGG, i );
			//单入单出
			if( inEdgesList.size() == 1 && outEdgesList.size() == 1 ){
				int u = inEdgesList.get( 0 );
				int k = i;
				int v = outEdgesList.get( 0 );
				if( changedGG[u][v] == 1 ){
					continue;
				}
				changedGG[u][k] = changedGG[k][v] = Integer.MAX_VALUE;
				changedGG[u][v] = 1;
				alive[k] = false;
				num++;
				System.out.println( "删除" + k + " " + u + "连接" + v );
				i = -1;
			}
			//零入零出
			else if( inEdgesList.size() == 0 && outEdgesList.size() == 0 ){
				int k = i;
				alive[k] = false;
				num++;
				System.out.println( "删除" + k );
				i = -1;
			}
			//单出
			else if( inEdgesList.size() == 0 && outEdgesList.size() == 1 ){
				int k = i;
				int v = outEdgesList.get( 0 );
				
				if( concurrentBehaviors( k ) == false ){
					System.out.println( "并发行为，不删除" );
					continue;
				}
				
				changedGG[k][v] = Integer.MAX_VALUE;
				alive[k] = false;
				num++;
				System.out.println( "删除" + k );
				i = -1;
			}
			//单入
			else if( inEdgesList.size() == 1 && outEdgesList.size() == 0 ){
				int u = inEdgesList.get( 0 );
				int k = i;

				if( concurrentBehaviors( k ) ){
					System.out.println( "并发行为，不删除" );
					continue;
				}
				
				changedGG[u][k] = Integer.MAX_VALUE;
				alive[k] = false;
				num++;
				System.out.println( "删除" + i );
				i = -1;
			}
		}
		
		return num;
	}
	
	//从GG中去掉完成的结点
	public void processGatewayGraph( ){
		for( int i=0 ; i<changedGG.length ; i++ ){
			if( alive[i] == false ){
				changedGG[i][i] = Integer.MAX_VALUE;
			}
		}
	}

	//深度复制地图
	public void copy( int[][] map1, int[][] map2 ){
		for( int i=0 ; i<map1.length ; i++ ){
			for( int j=0 ; j<map1[i].length ; j++ ){
				map1[i][j] = map2[i][j];
			}
		}
	}

	//创建单个网关
	public dataModel.process.Gateway addGatewayToModel( int u ){
		//第一次添加
		int type = -1;
		int attri = gatewayTypes[u].getAttribute();
		if( attri == GGGatewayTypes.AND ){
			type = dataModel.process.GatewayType.AND;
		}
		else if( attri == GGGatewayTypes.XOR ){
			type = dataModel.process.GatewayType.XOR;			
		}
		else if( attri == GGGatewayTypes.OR ){
			type = dataModel.process.GatewayType.OR;
		}
		else if( attri == GGGatewayTypes.COM ){
			type = dataModel.process.GatewayType.CGT;
		}
		else if( attri == GGGatewayTypes.EVENT ){
			type = dataModel.process.GatewayType.EVENT;
		}
		
		dataModel.process.Gateway gateway = new dataModel.process.Gateway(modelGatewayNum, "", lane, pool, type);
		rProcessModel.addGateway( gateway );
		
		ArrayList<dataModel.process.Gateway> uid = modelIdMap.get( u );
		if( uid == null ){
			uid = new ArrayList<dataModel.process.Gateway>();
		}
		uid.add( gateway );
		modelIdMap.put( u, uid );
		
		System.out.println( "BPMN2.0模型中添加了" + modelGatewayNum + "网关(" + u + ")" );
		modelGatewayNum++;
		
		return gateway;
	}
	
	//描述边
	public void addEdgeToModel( dataModel.process.Gateway e1 , dataModel.process.Gateway e2 ){
				
		for( Arc arc1 : rProcessModel.getArcs().values() ){
			if( arc1.getSource().getId() == e1.getId() &&
				arc1.getTarget().getId() == e2.getId()){
				System.out.println( "BPMN2.0模型中没添加弧" + e1.getId() + "→" + e2.getId() + "×××" );
				return ;
			}
		}
		
		Arc arc = new Arc( modelArcNum++, "", e1 , e2, "SequenceFlow");
		rProcessModel.addArc( arc );
		System.out.println( "BPMN2.0模型中添加了弧" + e1.getId() + "→" + e2.getId() );
	}

	//展开并描述
	public void unfoldAndDescribe( ){
		changedGG = saveChangedGG;
		changedGGNodeNum = changedGG.length;
		
		Arrays.fill( alive, true );
		
		rProcessModel = new ProcessModel( 0, "R_Process_Modl" );
		modelIdMap = new HashMap<Integer,ArrayList<dataModel.process.Gateway>>();
		
		pool = new Pool( 1, "manager" );
		rProcessModel.addPool( "manager" );
		lane = new Lane( 2, "manager", pool );
		rProcessModel.addLane( "manager" );
		
		int entry = findEntryList( changedGG ).get( 0 );
		int exit = findExitList( changedGG ).get( 0 );
		
		for( int i=0 ; i<goals.size() ; i++ ){
			System.out.print( "\n目标" + i + "：" );
			for( int j = 0 ; j<goals.get( i ).size() ; j++ ){
				System.out.print( goals.get( i ).get( j ) + "  " );
			}
		}
		System.out.println();
		
		modelGatewayNum = 0;
		dataModel.process.Gateway node1 = addGatewayToModel( entry );
		dataModel.process.Gateway node2 = addGatewayToModel( exit );
		createSubModel( changedGG, entry, exit, node1, node2 );
	}
	
	//记录行走路径
	public void record( int u, int v ){
		System.out.println( u + "→" + v );
	}
	
	//展开模型，边进行描述
	public void unfoldGateWayGraph( int[][] map, int u, int sink, int depth ){

		if( u == sink ){
			return ;
		}
		
		//可组合为Bond
		ArrayList<Integer> goal = goals.get( u );
		if( goal.contains( sink ) ){
			ArrayList<Integer> adjacentEdges = getOutEdgesList( map, u );
			for( int i=0 ; i<adjacentEdges.size() ; i++ ){
				int j = adjacentEdges.get( i );
				record( u, j );
				unfoldGateWayGraph( map, j, sink, depth );
			}
			return ;
		}
		
		//直接相连
		if( pathLength( map, u, sink ) == 1 ){
			record( u, sink );
			return ;
		}
		
		//寻找中间结点
		int k = -1;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < goal.size(); i++) {
			int v = goal.get( i );
			int pl = pathLength( map, v, sink );
			if( pl<min ){
				min = pathLength( map, v, sink );
				k = v;
			}
		}
		
		//找到目标结点且可达汇点
		if( k != -1 && reachability( map, k, sink) ){
			//递归描述
			unfoldGateWayGraph( map, u, k, depth+1 );
			unfoldGateWayGraph( map, k, sink, depth );
		}
		//找不到目标结点
		else if( k == -1 ){
			k = floyedRecord[u][sink];
			//递归描述
			unfoldGateWayGraph( map, u, k, depth );
			unfoldGateWayGraph( map, k, sink, depth );
		}
		
		return ;
	}

	//生成子过程模型
	public void createSubModel( int[][] map, int u, int v, dataModel.process.Gateway node1, dataModel.process.Gateway node2 ){

		ArrayList<ArrayList<Integer>> pathes = getPath( map, u, v );

		for( ArrayList<Integer> path : pathes ){
			
			ArrayList<Boolean> flag = new ArrayList<Boolean>(); 
			ArrayList<dataModel.process.Gateway> gatewayPath = new ArrayList<dataModel.process.Gateway>();
			
			//生成结点
			for( int j=0 ; j<path.size() ; j++ ){
				
				if( j == 0 ){
					gatewayPath.add( node1 );
					flag.add( true );
				}
				else if( j == path.size() - 1 ){
					gatewayPath.add( node2 );
					flag.add( true );
				}
				else{
					int goalIndex = pathExistsGoal( path, j );
					//存在目标
					if( goalIndex != -1 ){
						dataModel.process.Gateway node1_ = addGatewayToModel( path.get( j ) );
						gatewayPath.add( node1_ );
						flag.add( false );
						
						for( int h=j+1 ; h<goalIndex ; h++ ){
							gatewayPath.add( null );
							flag.add( false );
						}
						
						dataModel.process.Gateway node2_ = addGatewayToModel( path.get( goalIndex ) );
						gatewayPath.add( node2_ );
						flag.add( false );
						
						createSubModel( map, path.get( j ), path.get( goalIndex ), node1_, node2_ );
						
						if( goalIndex == path.size() - 1 ){
							j = goalIndex - 1;
						}
						else{
							j = goalIndex;
						}
					}
					//不存在目标
					else{
						gatewayPath.add( addGatewayToModel( path.get( j ) ) );
						flag.add( true );
					}
				}
			}
			
			System.out.println( "路径：" + path );
			System.out.print ( "网关：" );
			for( dataModel.process.Gateway gateway : gatewayPath ){
				if( gateway == null ){
					System.out.print( "~  " );	
				}
				else{
					System.out.print( gateway.getId() + "  " );
				}
			}
			System.out.print ( "标记：" );
			for( boolean bool : flag ){
				System.out.print( (bool==true?"√":"×") + "  " );
			}
			System.out.println();
			
			//生成边
			for( int j=0 ; j<gatewayPath.size() - 1 ; j++ ){
				dataModel.process.Gateway node1_ = gatewayPath.get( j );
				dataModel.process.Gateway node2_ = gatewayPath.get( j+1 );
				if( flag.get( j ) != false || flag.get( j+1 ) != false ){
					addEdgeToModel( node1_, node2_ );
				}
			}
			System.out.println();
		}
	}
	
	//path中是否存在目标,是则返回索引值,否则返回-1
	public int pathExistsGoal( ArrayList<Integer> path, int index ){
		
		ArrayList<Integer> goalList = goals.get( path.get( index ) );
		
		for( int g : goalList ){
			if( path.contains( g ) ){
				return path.indexOf( g );
			}
		}
		
		return -1;
	}
	
	//创建uv结点间的路径
	public ArrayList<ArrayList<Integer>> getPath( int[][] map, int u, int v ){
		
		ArrayList<ArrayList<Integer>> Q = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> pathes = new ArrayList<ArrayList<Integer>>();
		boolean[] colored = new boolean[changedGGNodeNum];
		Arrays.fill( colored, false );
		
		ArrayList<Integer> p0 = new ArrayList<Integer>();
		p0.add( u );
		colored[u] = true;
		
		ArrayList<Integer> uOutEdges = getOutEdgesList( changedGG, u );
		for( int j=0 ; j<uOutEdges.size() ; j++ ){
			int k = uOutEdges.get( j );
			if( colored[k] == false ){
				ArrayList<Integer> p1 = new ArrayList<Integer>( p0 );
				p1.add( k );
				Q.add( p1 );
				colored[k] = true;
			}
		}
		
		while( Q.isEmpty() == false ){
			ArrayList<Integer> top = Q.get( 0 );
			Q.remove( 0 );
			
			int last = top.get( top.size() - 1 );
			if( last == v ){
				pathes.add( top );
				continue;
			}
			
			//加入队列
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			if( goals!=null && goals.get( last ) != null ){
				candidates.addAll( goals.get( last ) );
			}
			candidates.addAll( getOutEdgesList( changedGG, last ) );
			
			if( candidates.size() > 0 ){
				int min = Integer.MAX_VALUE;
				int index = 0;
				for( int i=0 ; i<candidates.size() ; i++ ){
					int kk = candidates.get( i );
					int len = pathLength( changedGG, kk, v );
					if( len < min ){
						min = len;
						index = i;
					}
				}
				
				ArrayList<Integer> p1 = new ArrayList<Integer>( top );
				int kk = candidates.get( index );
				p1.add( kk );
				Q.add( p1 );
				colored[kk] = true;
			}
		}
		
		return pathes;
	}
	
	//矫正反边及合并网关
	public void postProcessing( ){
		for( int i=0 ; i<correctedEdges.size() ; i++ ){
			int u = correctedEdges.get( i ).get( 0 );
			int v = correctedEdges.get( i ).get( 1 );
			
			ArrayList<dataModel.process.Gateway> uNodes = modelIdMap.get( u );
			ArrayList<dataModel.process.Gateway> vNodes = modelIdMap.get( v );
			
			for( int j=0 ; j<uNodes.size() ; j++ ){
				returnArcDirection( uNodes.get( j ), vNodes.get( j ) );
			}
		}
		
		for( int i=0 ; i<corGatewayID.length ; i++ ){
			if( i != corGatewayID[i] ){
				aggregateGateway( i , corGatewayID[i] );
			}
		}
	}
	
	//矫正方向
	public void returnArcDirection( dataModel.process.Gateway u, dataModel.process.Gateway v ){
		
		Object[] arcs = rProcessModel.getArcs().values().toArray();
		
		for( int i=0 ; i<arcs.length ; i++ ){
			Arc arc = ( Arc )arcs[i];
			if( arc.getSource().getId() == u.getId() && arc.getTarget().getId() == v.getId() ){
				rProcessModel.removeArc( arc.getId() );
				addEdgeToModel( v, u );
				System.out.println( "矫正为" + v.getId() + "→" + u.getId() );
			}
		}
	}
	
	//合并网关
	public void aggregateGateway( int u, int v ){
		
		System.out.println( "\n映射关系：" );
		for( int i=0 ; i<changedGGNodeNum ; i++ ){
			System.out.print( i + "：" );
			ArrayList<dataModel.process.Gateway> gateways = modelIdMap.get( i );
			for( int j=0 ; gateways!=null && j<gateways.size() ; j++ ){
				System.out.print( gateways.get( j ).getId() + "  " );
			}
			System.out.println();
		}
		System.out.println();
		
		Object[] arcs = rProcessModel.getArcs().values().toArray();
		
		for( int i=0 ; i<arcs.length ; i++ ){
			Arc arc = ( Arc )arcs[i];
			dataModel.process.Gateway g1 = (dataModel.process.Gateway)arc.getSource();
			dataModel.process.Gateway g2 = (dataModel.process.Gateway)arc.getTarget();
			if( modelIdMap.get( u ).contains( g1 ) && modelIdMap.get( v ).contains( g2 ) ){
				ArrayList<Arc> arcs2 = getOutEdgesList( rProcessModel, g2 );
				for( int j=0 ; j<arcs2.size() ; j++ ){
					Arc arc23 = arcs2.get( j );
					dataModel.process.Gateway g3 = (dataModel.process.Gateway)arc23.getTarget();
					addEdgeToModel( g1, g3 );
					System.out.println( "添加了弧：" + g1.getId() + "→" + g3.getId() );
					rProcessModel.removeArc( arc.getId() );
					rProcessModel.removeArc( arc23.getId() );
				}
				rProcessModel.removeElem( g2.getId() );
			}
		}
	}
	
	//生成文本
	public void toText( ) throws FileNotFoundException, JWNLException{
		
		bpmnActivityIdMap = new HashMap<Integer, Integer>(); 
		bpmnArcIdReverseMap = new HashMap<Integer, Integer>(); 
		
		rEpsf = new EPSF( );
		rEpsf.addProcessModels( rProcessModel );
		rEpsf.initialModels( );
		rEpsf.createSubprocessRPSTs( );
		rEpsf.createAlternativeRPSTs( );
		
		Object[] objs = rEpsf.getRPSTs().values().toArray();
		rRpst = (RPST<ControlFlow, Node>)objs[0];
		
		modelActivityNum = 0;
		
		rRpst.print( rRpst.getRoot(), 0);
		
		//直接法
		Object[] objects = rProcessModel.getArcs().values().toArray();
		for( int i=0 ; i<objects.length ; i++ ){
			Arc arc = ( Arc )objects[i];
			
			int ru = arc.getSource().getId();
			int rv = arc.getTarget().getId();
			int ou = bpmnGatewayIdMap.get( ru );
			int ov = bpmnGatewayIdMap.get( rv );
			
			System.out.println( "\n增加活动" + ru + "→" + rv );
			
			savedNode = null;
			RPSTNode<ControlFlow, Node> rNode = null;
			findNewNode( ru, rv, rRpst, rRpst.getRoot(), 0 );
			rNode = savedNode;
			
			savedNode = null;
			RPSTNode<ControlFlow, Node> oNode = null;
			findNewNode( ou, ov, rpst, rpst.getRoot(), Integer.MAX_VALUE );
			oNode = savedNode;
		
			if( rNode != null && oNode != null ){
				addRPSTNodeToModel( arc, rNode, oNode );
			}
			else{
				System.out.println( "相等..." );
			}
		}
		
		rProcessModel.print();
		
		rEpsf = new EPSF( );
		rEpsf.addProcessModels( rProcessModel );
		rEpsf.initialModels( );
		rEpsf.annotateModels( 0  );
		rEpsf.createSubprocessRPSTs( );
		rEpsf.createAlternativeRPSTs( );
		
		Object[] objs2 = rEpsf.getRPSTs().values().toArray();
		rRpst = (RPST<ControlFlow, Node>)objs2[0];
		
		rRpst.print(rRpst.getRoot(), 0);
		
		rConverter = new TextPlanner( rProcessModel , rEpsf, rRpst, "manager" , false , false);
		rConverter.setStart( false );
		
		rRpst.getChildren( rRpst.getRoot() );
		
		Object[] childs = rRpst.getChildren( rRpst.getRoot() ).toArray();
		for( int i=0 ; i<childs.length ; i++ ){
			RPSTNode<ControlFlow, Node> cNode = (RPSTNode<ControlFlow, Node>)childs[i];
			rConverter.setTagWithBullet( true );
			rConverter.convertToText( cNode, 1 );
			rConverter.setTagWithBullet( false );
		}
	}
	
	private void addRPSTNodeToModel( Arc arc, RPSTNode<ControlFlow, Node> rNode, RPSTNode<ControlFlow, Node> oNode ) {
		
		Object[] childs = rpst.getChildren( oNode ).toArray();

		if( childs.length == 2 ){
			rProcessModel.removeArc( arc.getId() );
			
			ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(oNode, oNode.getEntry(), rpst);
			
			for( RPSTNode<ControlFlow, Node> node : orderedTopNodes ){
				Node node1 = node.getEntry();
				int id = Integer.valueOf( node1.getId() );
				
				if( processModel.getActivites().containsKey( id ) ){
					
					Activity originalActivity = processModel.getActivites().get( id );
					Activity activity = new Activity( modelGatewayNum+modelActivityNum++, originalActivity.getLabel(), lane, pool, ActivityType.NONE );
					rProcessModel.addActivity( activity );
					bpmnActivityIdMap.put( activity.getId(), originalActivity.getId() );
					
					int u = Integer.valueOf( rNode.getEntry().getId() );
					int v = Integer.valueOf( rNode.getExit().getId() );
					
					Arc arc1 = new Arc( modelArcNum++, "", rProcessModel.getGateway( u ), activity, "SequenceFlow" );
					rProcessModel.addArc( arc1 );
					Arc arc2 = new Arc( modelArcNum++, "", activity, rProcessModel.getGateway( v ), "SequenceFlow" );
					rProcessModel.addArc( arc2 );
					
					//填写映射关系
					int originalU = Integer.parseInt( orderedTopNodes.get( 0 ).getEntry().getId() );
					int originalK = Integer.parseInt( orderedTopNodes.get( 0 ).getExit().getId() );
					int originalV = Integer.parseInt( orderedTopNodes.get( 1 ).getExit().getId() );
					Arc originalArc1 = getArc( processModel, originalU, originalK );
					Arc originalArc2 = getArc( processModel, originalK, originalV );
					bpmnArcIdReverseMap.put( originalArc1.getId() , arc1.getId() );
					bpmnArcIdReverseMap.put( originalArc2.getId() , arc2.getId() );
				}
			}
		}
		else if( childs.length == 0 ){
			//填写映射关系
			int originalU = Integer.parseInt( oNode.getEntry().getId() );
			int originalV = Integer.parseInt( oNode.getExit().getId() );
			Arc originalArc1 = getArc( processModel, originalU, originalV );
			bpmnArcIdReverseMap.put( originalArc1.getId() , arc.getId() );
		}
		else{
			System.out.println( "活动数不是0或1！！！" );
		}
	}

	//得到两BPMN2.0模型间的ID映射关系
	public void createBPMNIdMap( ){
		
		bpmnGatewayIdMap = new HashMap<Integer, Integer>(); 
		
//		for( int i=0 ; i<modelIdMap.size() ; i++ ){
//			ArrayList<dataModel.process.Gateway> gateways = modelIdMap.get( i );
//			System.out.print( i + ":" );
//			for( dataModel.process.Gateway g : gateways ){
//				if( g== null ){
//					System.out.print( "~  ");
//				}
//				else{
//					System.out.print( g.getId() + " " );
//				}
//			}
//			System.out.println();
//		}
		
		for( int i=0 ; i<changedGGNodeNum ; i++ ){
			ArrayList<dataModel.process.Gateway> gateways = modelIdMap.get( i );
			for( int j=0 ; gateways != null && j<gateways.size() ; j++ ){
				bpmnGatewayIdMap.put( gateways.get( j ).getId(), i );
			}
		}
		
		//output
		for( Integer key : bpmnGatewayIdMap.keySet() ){
			System.out.println( key + ":" + bpmnGatewayIdMap.get( key ) );
		}
		System.out.println();
		
		for( int i=0 ; i<corGatewayID.length ; i++ ){
			if( i == corGatewayID[i] ){
				continue;
			}
			ArrayList<dataModel.process.Gateway> gateways = modelIdMap.get( corGatewayID[i] );
			for( int j=0 ; gateways!=null && j<gateways.size() ; j++ ){
				bpmnGatewayIdMap.remove( gateways.get( j ).getId() );
			}
		}
		
		//output
		for( Integer key : bpmnGatewayIdMap.keySet() ){
			System.out.println( key + ":" + bpmnGatewayIdMap.get( key ) );
		}
		System.out.println();
		
		for( Integer key : bpmnGatewayIdMap.keySet() ){
			int value = bpmnGatewayIdMap.get( key );
			int newValue = Integer.valueOf( idMap.get( value ).getId() );
			bpmnGatewayIdMap.put( key , newValue );
		}
		
		//output
		for( Integer key : bpmnGatewayIdMap.keySet() ){
			System.out.println( key + ":" + bpmnGatewayIdMap.get( key ) );
		}
		System.out.println();
		
	}

	//网关类型
	public GGGatewayTypes getGatewayType( int id ){
		return gatewayTypes[id];
	}
	
	//找到RPST树上的某边
	public void findNewNode( int u, int v, RPST<ControlFlow, Node> tmprpst, RPSTNode<ControlFlow,Node> tmpnode, int control ){
		
		if( savedNode != null ){
			return ;
		}
		
		int u_ = Integer.valueOf( tmpnode.getEntry().getId() );
		int v_ = Integer.valueOf( tmpnode.getExit().getId() );
//		System.out.println( u_ + "→" + v_ );
		
		if( u == u_ && v == v_ && tmprpst.getChildren( tmpnode ).size() <= control ){
			System.out.println( "找到!!!" );
			savedNode = tmpnode;
			return ;
		}

		Object[] childs = tmprpst.getChildren( tmpnode ).toArray();
		for( int i=0 ; i<childs.length ; i++ ){
			RPSTNode<ControlFlow,Node> child = (RPSTNode<ControlFlow,Node>)childs[i];
			findNewNode( u, v, tmprpst, child, control );
		}
	}

	//记录重复和易漏的信息
	public void recordOtherInformation( ArrayList<Arc> arcss ){
		
		System.out.println();
		for( Arc arc : arcss ){
			int u = arc.getSource().getId();
			int v = arc.getTarget().getId();
			System.out.println( "弧：" + u + " -- " + v );
		}
		
		System.out.println();
		for( int v : bpmnGatewayIdMap.keySet() ){
			System.out.println( "网关映射关系：" + v + " -- " + bpmnGatewayIdMap.get( v ) );
		}
		
		System.out.println();
		for( int v : bpmnActivityIdMap.keySet() ){
			int u = bpmnActivityIdMap.get( v );
			processModel.getActivites().get( u ).setDescriptionString(
					rProcessModel.getActivites().get( v ).getDescriptionString() );
			System.out.println( "活动映射关系：" + v + " -- " + bpmnActivityIdMap.get( v ) );
		}
		
		System.out.println();
		for( int v : bpmnArcIdReverseMap.keySet() ){
			if( bpmnArcIdReverseMap.get( v ) != -1 ){
				System.out.println( "弧映射关系：" + v + " -- " + bpmnArcIdReverseMap.get( v ) );
			}
			else{
				System.out.println( "弧映射关系：" + v + " -- " + "没活动的空边" );
			}
		}
		
		//记录遗漏的纯边（两边网关）、重复的活动、遗漏的活动
		repetitiveActivities = new ArrayList<Activity>();
		omissiveActivities = new ArrayList<Activity>();
		omissiveEdges = new ArrayList<Arc>();
		
		HashMap<Integer, Activity> activities = rProcessModel.getActivites();
		HashMap<Activity, Boolean> descriptionNum = new HashMap<Activity, Boolean>(); 
		
		for( Activity activity : activities.values() ){
			
			int originalID = bpmnActivityIdMap.get( activity.getId() );
			Activity originalActivity = processModel.getActivites().get( originalID );
			if( descriptionNum.keySet().contains( originalActivity ) == true ){
				if( repetitiveActivities.contains( originalActivity ) == false ){
					repetitiveActivities.add( originalActivity );
				}
			}
			else{
				descriptionNum.put( originalActivity , true );
			}
		}
		
		for( Arc arc : arcss ){
			Element e1 = arc.getSource();
			Element e2 = arc.getTarget();
			System.out.println( arc.getId() );
			if( bpmnArcIdReverseMap.keySet().contains( arc.getId() ) == false ){
				if( e1.getClass().getName().equals( "dataModel.process.Gateway" ) &&
					e2.getClass().getName().equals( "dataModel.process.Gateway" ) ){
					if( omissiveEdges.contains(  arc ) == false ){
						omissiveEdges.add( arc );
					}
				}
				else{
					if( e1.getClass().getName().equals( "dataModel.process.Activity" ) ){
						if( omissiveActivities.contains( (Activity)e1 ) == false ){
							omissiveActivities.add( (Activity)e1 );
						}
					}
					if( e2.getClass().getName().equals( "dataModel.process.Activity" ) ){
						if( omissiveActivities.contains( (Activity)e2 ) == false ){
							omissiveActivities.add( (Activity)e2 );
						}
					}
				}
			}
		}
	}
		
	//补充漏边
	public ArrayList<DSynTSentence> describeOtherInformation( ){
		
		ArrayList<DSynTSentence> sentences = new ArrayList<DSynTSentence>();
		
		System.out.println( "\n重活动:" );
		for( Activity activity : repetitiveActivities ){
			System.out.println( activity.getId() + "(" + activity.getLabel() + ")" );
		}
		
		System.out.println( "\n漏活动:" );
		for( Activity activity : omissiveActivities ){
			System.out.println( activity.getId() + "(" + activity.getLabel() + ")" );
		}
		
		System.out.println( "\n漏边:" );
		for( Arc arc : omissiveEdges ){
			System.out.println( arc.getId() );
		}
		
		//漏边
		boolean oe = false;
		for( Arc arc : omissiveEdges ){
			dataModel.process.Gateway inElement = ( dataModel.process.Gateway )arc.getSource();
			dataModel.process.Gateway outElement = ( dataModel.process.Gateway )arc.getTarget();
			ArrayList<Element> inActivitiesList = getInEdgesElements( processModel, inElement );
			ArrayList<Element> outActivitiesList = getOutEdgesElements( processModel, outElement );
			
			String actionLeft = "";
			String actionRight = "";
			
			//入活动集
			for( int j=0 ; j<inActivitiesList.size() ; j++ ){
				Element element = inActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionLeft += " or ";
					}
					actionLeft += element.getDescriptionString().toLowerCase().replace("[^a-z]", "");
				}
			}
			//出活动集
			for( int j=0 ; j<outActivitiesList.size() ; j++ ){
				Element element = outActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionRight += " or ";
					}
					actionRight += element.getLabel();
				}
			}
			
			//生成文本				
			if( inActivitiesList.size() > 0 && outActivitiesList.size() > 0 ){
				
				//提示串
				if( oe == false ){
					ExecutableFragment eFrag1 = new ExecutableFragment( "list", "", "omissive edges which can cause other behavior of process model","");
					eFrag1.bo_isSubject = true;
					eFrag1.verb_isPast = true;
					eFrag1.bo_isPlural = true;
					sentences.add( new DSynTMainSentence(eFrag1) );
					QcPrintDsynT.print( new DSynTMainSentence(eFrag1) );
					oe = true;
				}
				
				//预处理文本
				actionLeft = processString( actionLeft );
				actionLeft += ",";
				actionRight = processString( actionRight );
				
				ExecutableFragment eFrag = null;
				String modLemma = "after " + actionLeft + ",";
				ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord.addAttribute("adv-type", "sentential");
						
				eFrag = new ExecutableFragment("may", "also "  + actionRight , "manager", "");
				eFrag.bo_hasArticle = false;
				eFrag.sen_hasBullet = true;
				eFrag.sen_level = 0;
				eFrag.addMod(modLemma, modRecord);
				
				DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
				sentences.add(dsyntSentence);
				QcPrintDsynT.print( dsyntSentence );
			}
		}

		//漏活动
		boolean oa = false;
		for( Activity activity : omissiveActivities ){
			dataModel.process.Gateway inElement = (dataModel.process.Gateway)getInEdgesElements( processModel, activity ).get( 0 );
			dataModel.process.Gateway outElement = (dataModel.process.Gateway)getOutEdgesElements( processModel, activity ).get( 0 );
			ArrayList<Element> inActivitiesList = getInEdgesElements( processModel, inElement );
			ArrayList<Element> outActivitiesList = getOutEdgesElements( processModel, outElement );
			
			String actionLeft = "";
			String actionRight = "";
			
			//入活动集
			for( int j=0 ; j<inActivitiesList.size() ; j++ ){
				Element element = inActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionLeft += " or ";
					}
					actionLeft += element.getDescriptionString().toLowerCase().replace("[^a-z]", "");
				}
			}
			//出活动集
			for( int j=0 ; j<outActivitiesList.size() ; j++ ){
				Element element = outActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionRight += " or ";
					}
					actionRight += element.getLabel();
				}
			}
			
			//生成文本				
			if( inActivitiesList.size() > 0 && outActivitiesList.size() > 0 ){
				
				//提示串
				if( oa == false ){
					ExecutableFragment eFrag1 = new ExecutableFragment( "list", "", "omissive activities which can cause other behavior of process model","");
					eFrag1.bo_isSubject = true;
					eFrag1.verb_isPast = true;
					eFrag1.bo_isPlural = true;
					sentences.add( new DSynTMainSentence(eFrag1) );
					QcPrintDsynT.print( new DSynTMainSentence(eFrag1) );
					oa = true;
				}
				
				//预处理文本
				actionLeft = processString( actionLeft );
				actionLeft += ",";
				actionRight = processString( actionRight );
				
				ExecutableFragment eFrag = null;
				String modLemma = "after " + actionLeft;
				ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord.addAttribute("adv-type", "sentential");
						
				String actionString = activity.getAnnotations().get( 0 ).getActions().get( 0 );
				String objectString = activity.getAnnotations().get( 0 ).getBusinessObjects().get( 0 );
				eFrag = new ExecutableFragment( actionString ,  objectString , "manager", "");
				eFrag.bo_hasArticle = false;
				eFrag.sen_hasBullet = true;
				eFrag.sen_level = 0;
				eFrag.addMod(modLemma, modRecord);
				
				
				ExecutableFragment eFrag2 = null;
				String modLemma2 = "after " + actionString + ",";
				ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord2.addAttribute("adv-type", "sentential");

				eFrag2 = new ExecutableFragment("begin to ", actionRight , "manager", "");
				eFrag2.bo_hasArticle = false;
				eFrag2.sen_level = 0;
				eFrag2.addMod(modLemma2, modRecord2);
				
				DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
				sentences.add(dsyntSentence);
				QcPrintDsynT.print( dsyntSentence );
				
				DSynTMainSentence dsyntSentence2 = new DSynTMainSentence(eFrag2);
				sentences.add(dsyntSentence2);
				QcPrintDsynT.print( dsyntSentence2 );
			}
		}
		
		//重活动
		boolean ra = false;
		for( Activity activity : repetitiveActivities ){
			dataModel.process.Gateway inElement = (dataModel.process.Gateway)getInEdgesElements( processModel, activity ).get( 0 );
			dataModel.process.Gateway outElement = (dataModel.process.Gateway)getOutEdgesElements( processModel, activity ).get( 0 );
			
			boolean sign = false;
			if( ( inElement.getType() == dataModel.process.GatewayType.AND || 
					inElement.getType() == dataModel.process.GatewayType.OR ) &&
				( outElement.getType() == dataModel.process.GatewayType.AND || 
						outElement.getType() == dataModel.process.GatewayType.OR )
				){
				sign = true;
			}
			if( sign == false ){
				continue;
			}
			
			ArrayList<Element> inActivitiesList = getInEdgesElements( processModel, inElement );
			ArrayList<Element> outActivitiesList = getOutEdgesElements( processModel, outElement );
			
			String actionLeft = "";
			String actionRight = "";
			
			//入活动集
			for( int j=0 ; j<inActivitiesList.size() ; j++ ){
				Element element = inActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionLeft += " or ";
					}
					actionLeft += element.getDescriptionString().toLowerCase().replace("[^a-z]", "");
				}
			}
			//出活动集
			for( int j=0 ; j<outActivitiesList.size() ; j++ ){
				Element element = outActivitiesList.get( j );
				if( processModel.getActivites().containsKey( element.getId() ) ){
					if( j != 0 ){
						actionRight += " or ";
					}
					actionRight += element.getLabel();
				}
			}
			
			//生成文本				
			if( actionLeft != "" && actionRight != "" ){
				
				//提示串
				if( ra == false ){
					ExecutableFragment eFrag1 = new ExecutableFragment( "list", "", "omissive activities which can cause other behavior of process model","");
					eFrag1.bo_isSubject = true;
					eFrag1.verb_isPast = true;
					eFrag1.bo_isPlural = true;
					sentences.add( new DSynTMainSentence(eFrag1) );
					QcPrintDsynT.print( new DSynTMainSentence(eFrag1) );
					ra = true;
				}
				
				//预处理文本
				actionLeft = processString( actionLeft );
				actionLeft += ",";
				actionRight = processString( actionRight );
				
				ExecutableFragment eFrag = null;
				String modLemma = "after " + actionLeft + ",";
				ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord.addAttribute("adv-type", "sentential");
						
				eFrag = new ExecutableFragment("will synchronize at ", "\"" + activity.getLabel() + "\"" , "manager", "");
				eFrag.bo_hasArticle = false;
				eFrag.sen_hasBullet = true;
				eFrag.sen_level = 0;
				eFrag.addMod(modLemma, modRecord);
				
				ExecutableFragment eFrag2 = null;
				String modLemma2 = "then " + activity.getLabel();
				ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
				modRecord.addAttribute("adv-type", "sentential");
						
				eFrag2 = new ExecutableFragment("simoutaneously  ", actionRight , "manager", "");
				eFrag2.bo_hasArticle = false;
				eFrag2.sen_level = 0;
				eFrag2.addMod(modLemma2, modRecord2);
				
				DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
				sentences.add(dsyntSentence);
				QcPrintDsynT.print( dsyntSentence );
				
				DSynTMainSentence dsyntSentence2 = new DSynTMainSentence(eFrag2);
				sentences.add(dsyntSentence2);
				QcPrintDsynT.print( dsyntSentence2 );
			}
		}
		
		return sentences;
	}

	//得到某弧
	private Arc getArc(ProcessModel pm, int u, int v) {
		
		for( Arc arc : pm.getArcs().values() ){
			if( arc.getSource().getId() == u && arc.getTarget().getId() == v ){
				return arc;
			}
		}
		
		return null;
	}

	private String processString(String string) {
		
		int begin , end;
		
		char[] str = string.toLowerCase().toCharArray();
		
		begin = 0;
		while( begin<str.length && !(str[begin]>='a' && str[begin] <= 'z') ){
			begin++;
		}
		
		end = str.length-1;
		while( end>=0 && !(str[end]>='a' && str[end] <= 'z') ){
			end--;
		}
		
		return string.substring( begin, end + 1 );
	}

	//并发行为
	public boolean concurrentBehaviors( int u ){
		GGGatewayTypes type = gatewayTypes[u];
		if( type.attribute == GGGatewayTypes.AND || type.attribute == GGGatewayTypes.OR || type.attribute == GGGatewayTypes.COM ){
			return true;
		}
		return false;
	}
}

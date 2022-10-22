package hku.algo;

import hku.Config;
import hku.algo.index.*;
import hku.algo.online.BasicG;
import hku.algo.online.BasicW;
import hku.algo.query1.IncS;
import hku.algo.query1.IncT;
import hku.algo.query2.*;
import hku.algo.query2.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import hku.util.*;
/**
 * @author fangyixiang
 * @date Aug 17, 2015
 */
public class MainTest {

	public static void main1(String[] args) {
		int graph[][] = new int[11][];
		int a1[] = {2, 3, 4, 5};graph[1] = a1;
		int a2[] = {1, 3, 4, 5};graph[2] = a2;
		int a3[] = {1, 2, 3};	graph[3] = a3;
		int a4[] = {1, 2, 3, 7};graph[4] = a4;
		int a5[] = {1, 2, 7};	graph[5] = a5;
		int a6[] = {4};			graph[6] = a6;
		int a7[] = {5};			graph[7] = a7;
		int a8[] = {9};			graph[8] = a8;
		int a9[] = {8};			graph[9] = a9;
		int a10[] = {};			graph[10] = a10;
		
		String nodes[][] = new String[11][];
		String k1[] = {"A", "v", "w", "x", "y"};nodes[1] = k1;
		String k2[] = {"B", "x"};				nodes[2] = k2;
		String k3[] = {"C", "x"};				nodes[3] = k3;
		String k4[] = {"D", "x", "y", "z"};		nodes[4] = k4;
		String k5[] = {"E", "w", "y"};			nodes[5] = k5;
		String k6[] = {"F", "y"};				nodes[6] = k6;
		String k7[] = {"G", "y", "z"};			nodes[7] = k7;
		String k8[] = {"H", "z"};				nodes[8] = k8;
		String k9[] = {"I", "x"};				nodes[9] = k9;
		String k10[] = {"J", "x"};				nodes[10] = k10;
		
		BasicIndex idx = new BasicIndex(graph, nodes);
		TNode root = idx.build();
		int core[] = idx.getCore();
		idx.traverse(root);
		
//		GlobalQuery1 query1 = new GlobalQuery1(graph, nodes, root, core, Config.dblpCCS);
//		query1.query(1);
		
//		LocalQuery1 query1 = new LocalQuery1(graph, nodes, root, core, Config.caseCCS);
//		query1.query(1);
	}
	
	//dblp
	public static void main(String[] args) {
//		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpNode);
//		DataReader dataReader = new DataReader(Config.smallGraph, Config.smallNode);
//		DataReader dataReader = new DataReader(Config.DGraph, Config.DNode);
//		DataReader dataReader = new DataReader("graph_db.txt", "node_db.txt");
//		DataReader dataReader = new DataReader("graph_im.txt", "node_im.txt");
//		DataReader dataReader = new DataReader(Config.IGraph, Config.INode);
		DataReader dataReader = new DataReader(Config.fGraph, Config.fNode);
		int graph[][] = dataReader.readGraph();
		String nodes[][] = dataReader.readNode();
		
		AdvancedIndex index = new AdvancedIndex(graph, nodes);
		TNode root = index.build();
		int core[] = index.getCore();
		
		Config.k = 80;

		String queryfile ="C:\\zxj\\CSHDS\\query\\qfsq.txt";

		int i=0;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
			String line;
			Queue<Integer> querynodes = new LinkedList<>();
			while((line = stdin.readLine()) != null){
				int queryid = Integer.parseInt(line);
				querynodes.add(queryid);
			}
			System.out.println("querynodes size:"+querynodes.size());
			stdin.close();

			long t = 0;
			FileWriter fileWriter = new FileWriter("result_fsq.txt",false);
			for (int queryid:querynodes	) {
				long time1 = System.nanoTime();
				Dec query1 = new Dec(graph, nodes, root, core, null);
				List<Set<Integer>> result = query1.query(queryid);
				long time2 = System.nanoTime();
				t+=time2-time1;
				System.out.println(queryid);
				System.out.println((time2-time1)/1e6);

				for(Set<Integer> set:result){
					StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append(queryid);
					for(int nei:set){
						stringBuffer.append(" ").append(nei);
					}
					stringBuffer.append("\r\n");
					fileWriter.write(stringBuffer.toString());
				}
			}
			fileWriter.close();
			System.out.println(1.0*t/ querynodes.size()/1e6);

		}catch (Exception e){
			System.out.println(e);
		}

	}
}
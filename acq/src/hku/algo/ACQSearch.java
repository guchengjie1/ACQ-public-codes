package hku.algo;

import hku.Config;
import hku.algo.index.AdvancedIndex;
import hku.algo.index.BasicIndex;
import hku.algo.query2.Dec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * @author fangyixiang
 * @date Aug 17, 2015
 */
public class ACQSearch {
	//dblp
	public static void main(String[] args) {
		String Root = args[0] ;
		String Graph = Root + "/graph.txt";
		String Vertex = Root + "/vertex.txt";
		String Edge = Root + "/edge.txt";
		String Attribute = Root + "/attribute.txt";

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
import java.util.Arrays;
import java.util.HashMap;

public class GraphProcessor {
	SQLManager sqlMgr;
	GraphUI graphUI = new GraphUI();
	protected boolean graph[][];
	protected HashMap<TableNode, Integer> map = new HashMap<TableNode, Integer>();
	
	/**
	 * Initialize a graph without relationships
	 * @param size
	 */
	public void initGraph(int size){
		graph = new boolean [size][size];
		for(boolean[] row: graph)
			Arrays.fill(row, false);
	}
	
	/**
	 * Set an edge in graph stream
	 * @param v1
	 * @param v2
	 * @param value
	 */
	protected void setEdge(TableNode v1, TableNode v2, boolean value){
		graphUI.insertEdge(v1.name+v2.name, v1.name,v2.name, "",  true);
		setEdge(map.get(v1), map.get(v2), value);
		v1.outDegree++;
		v2.inDegree++;
	}
	
	/**
	 * Set edge in graph matrix
	 * @param v1
	 * @param v2
	 * @param value
	 */
	protected void setEdge(int v1, int v2, boolean value){
		graph[v1][v2]= value;
	}
	
	/**
	 * Check if two tables are related between them
	 * @param tableNode1
	 * @param tableNode2
	 * @return
	 */
	protected boolean isRelated(TableNode tableNode1, TableNode tableNode2){
		int table1Index = map.get(tableNode1);
		int table2Index = map.get(tableNode2);
		return graph[table1Index][table2Index] || 
			   graph[table2Index][table1Index];
	}

}

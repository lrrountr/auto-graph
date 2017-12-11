import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

public class EntityGraphProcessor extends GraphProcessor{
	private String names[] = null;
	private TableNode nodes[] = null;
	private Vector<String> factTables = new Vector<String>();
	private Vector<TableNode> dimPossibility = new Vector<TableNode>();
	
	public EntityGraphProcessor(SQLManager sqlMgr){
		this.sqlMgr = sqlMgr;
	}
	
	/**
	 * Return a TableNode based on its name
	 * @param tableName
	 * @return
	 */
	private TableNode getNode(String tableName) {
		for(int i = 0; i < nodes.length; i++){
			if(nodes[i].name.equals(tableName))
				return nodes[i];
		}
		return null;
		
	}
	
	/**
	 * Fact table getter
	 * @return
	 */
	public Vector<String> getFactTables() {
		return factTables;
	}
	
	/**
	 * Create a graph entry point
	 * @return
	 */
	public boolean createGraph(){
		setNodeNames();
		createRelationships();
		cathegorizeTables();
		return true;
	}
	
	/**
	 * Set node names based on tables query to SQL manager
	 */
	private void setNodeNames(){
		try {
			Vector <String> tables = sqlMgr.getTablesNames();
			names = tables.toArray(new String[tables.size()]);
			nodes = new TableNode[tables.size()];
			System.out.println("Tables: " + Arrays.toString(names));

			for(int i =0; i < names.length; i++){
				String tableName = names[i];
				TableNode newNode = new TableNode(tableName);
				newNode.columnNames = sqlMgr.getColumnNames(tableName);
				nodes[i] = newNode;
				map.put(newNode, i);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Set table relationships based on foreign keys
	 */
	private void createRelationships() {
		int numNodes = names.length;
		initGraph(numNodes);
		try {
			for(int i =0; i < numNodes; i++){
				//System.out.println(names[i] + ": ");
				Vector<String> relTables = sqlMgr.getForeignKeys(names[i]);
				for(String pkTableName : relTables){
					TableNode pkNode = getNode(pkTableName);
					setEdge(pkNode, nodes[i], true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Determine if table is a fact candidate, dimensional catalog and dimension
	 */
	private void cathegorizeTables() {
		
		for(int i = 0; i < nodes.length; i++){
			TableNode currNode = nodes[i];
			if( 0 == currNode.outDegree && 0 < currNode.inDegree ){
				currNode.type = TableNode.NodeType.CATALOG;
			}else if( 0 < currNode.outDegree && 0 < currNode.inDegree){
				currNode.type = TableNode.NodeType.FACT;
				graphUI.setSelectedNode(currNode.name);
				factTables.add(currNode.name);
			}else if(2 <= currNode.outDegree && 0 == currNode.inDegree){
				currNode.type = TableNode.NodeType.DIMENSION;
				dimPossibility.add(currNode);
			}
			System.out.println(currNode.name+"("+currNode.outDegree+","+currNode.inDegree+")"+currNode.type);
		}
		
	}
	
	/**
	 * Get tables which are related to a given TableNode
	 * @param tableNode
	 * @return
	 */
	private Vector<TableNode> getRelatedTables(TableNode tableNode){
		Vector<TableNode> relatedTables  = new Vector<TableNode>();
		int factIndex = map.get(tableNode);
		for(int i = 0; i < graph[factIndex].length; i++){
			if(graph[i][factIndex] || graph[factIndex][i]){
				//System.out.println(nodes[i].name +" " + nodes[i].type);
				relatedTables.add(nodes[i]);
			}
		}
		return relatedTables;
	}
	
	/**
	 * Get selected fact from user
	 * @param selectedFact
	 */
	public TableNode getFactTableNode(String selectedFact) {
		System.out.println("Selected Fact Table: " + selectedFact);
		TableNode factTableNode = getNode(selectedFact);
		return factTableNode;	
	}
	
	/**
	 * From a fact table get possible dimensions
	 * @param factTableNode
	 * @return
	 */
	public  Vector<Vector<TableNode>> getDimensions(TableNode factTableNode){
		boolean canCreateDimension = false;
		Vector<Vector<TableNode>> dimensions = new Vector<Vector<TableNode>>();
		for(TableNode dim: dimPossibility){
			Vector<TableNode> dimension = new Vector<TableNode>();
			byte conditionState = 0;
			canCreateDimension = false;
			dimension.clear();
			dimension.add(dim);
			Vector<TableNode> relatedTables = getRelatedTables(dim);
			for(TableNode relTable : relatedTables){
				if(relTable.type == TableNode.NodeType.CATALOG){
					conditionState = 1;
					dimension.add(relTable);
					if(isRelated(factTableNode,relTable))
						canCreateDimension = true;
				}
			}
			if(isRelated(factTableNode, dim) && 1 == conditionState)
				canCreateDimension = true;
				
			if(canCreateDimension){
				System.out.println("New dim: " + dimension.toString() + " with fact " + factTableNode.name);
				dimensions.add(dimension);
			}
		}
		return dimensions;
	}

}

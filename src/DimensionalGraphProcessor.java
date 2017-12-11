import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DimensionalGraphProcessor extends GraphProcessor{
	private TableNode factTable;
	private Vector<Vector<TableNode>> dimensions;
	private Vector<TableNode> dimensionTables = new Vector<TableNode>();

	public DimensionalGraphProcessor(TableNode factTable, Vector<Vector<TableNode>> dimensions){
		sqlMgr = new SQLManager();
		this.factTable = factTable;
		this.dimensions = dimensions;
	}
	
	/**
	 * Build dimensional graph
	 */
	public void createGraph(){
		int graphSize = dimensions.size() + 1;// +1 to add fact table
		initGraph(graphSize);
		map.put(factTable, 0);
		for(int i = 0; i < dimensions.size(); i++){
			TableNode dimTable = buildDimensionTableNode(dimensions.get(i), i+1);
			dimensionTables.add(dimTable);
			setEdge(dimTable, factTable, true);
		}
		graphUI.setSelectedNode(factTable.name);
	}
	
	/**
	 * Get dimensional tables names to merge as table name.
	 * @return
	 */
	public Vector<String> getDimensionTablesNames() {
		Vector<String> dimensionTablesNames = new Vector<String>();
		for(TableNode dimTable : dimensionTables)
			dimensionTablesNames.add(dimTable.name);
		return dimensionTablesNames;
	}
	
	/**
	 * Build a dimensional table to send it as a table to database
	 * @param dimension
	 * @param counter
	 * @return
	 */
	private TableNode buildDimensionTableNode(Vector<TableNode> dimension, int counter){
		String tableName = "Dim";
		Vector<String> columnNames = new Vector<String> ();
		for(TableNode tableNode : dimension){
			tableName += tableNode.name;
			columnNames.addAll(tableNode.columnNames);
		}
		TableNode dimensionTable = new TableNode(tableName);
		dimensionTable.columnNames = columnNames;
		map.put(dimensionTable, counter);
		return dimensionTable;
		
	}
	
	/**
	 * Create dimensional database in server
	 */
	public void createDimensionalDatabase(){
		try {
			sqlMgr.connectToDatabase();
			String dbName = "dimensional_"+factTable.name;
			sqlMgr.createDatabase(dbName);
			Map<String,String> columns = new HashMap<String,String>();
			columns.put("id", "int");
			columns.put("id2", "int");
			columns.put("avergae", "float");
			Vector<String> primaryKeys = new Vector<String>();
			primaryKeys.add("id");
			primaryKeys.add("id2");
			Map<String, String> foreignKeys = new HashMap<String, String>();
			sqlMgr.createTable(factTable.name, columns, primaryKeys, foreignKeys);
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Error creating database. Possibly already exists" );
		}
	}

	/**
	 * Merge all possible columns for dimensional database
	 * @param selectedDimension
	 * @return
	 */
	public Vector<String> getPossibleColumns(String selectedDimension) {
		for(TableNode dimension : dimensionTables){
			if(dimension.name.equals(selectedDimension))
				return dimension.columnNames;
		}
		return null;
	}
	
	/**
	 * Create a table in database based on user selection
	 * @param selectedDimension
	 * @param selectedColumns
	 * @param selectedKeys
	 */
	public void createDimensionalTable(String selectedDimension, List<String> selectedColumns, List<String> selectedKeys){
		Map<String,String> columns = new HashMap<String,String>();
		Vector<String> primaryKeys = new Vector<String>();
		Map<String, String> foreignKeys = new HashMap<String, String>();
		
		for(String column: selectedColumns){
			columns.put(column, "int");
		}
		
		for(String key: selectedKeys){
			primaryKeys.add(key);
		}
		
		try {
			sqlMgr.createTable(selectedDimension, columns, primaryKeys, foreignKeys);
		} catch (SQLException e) {
			System.err.println("Error creating table "+ selectedDimension);
		}
		
	}

}

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Vector;


public class SQLManager{
	private String databaseName = null;
	final static String userName = "root";
	final static String password = "mysql@123";
	final static String mySQLPort = "3306";
	final static String hostUrl = "127.0.0.1";
	static Connection conn;
	
	/**
	 * Setup the connection with the database server
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public void connectToDatabase() throws SQLException, ClassNotFoundException{
		String useSSL = "?verifyServerCertificate=false&useSSL=true";
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + hostUrl
				+ ":" + mySQLPort + useSSL, userName, password );

	}
	
	/**
	 * Get catalogs from connection, excludes system catalogs
	 * @param conn
	 */
	public  Vector<String> getCatalogs() throws SQLException{
		ResultSet resultSet;
		Vector<String> cat = new Vector<String>();

		resultSet = conn.getMetaData().getCatalogs();
		String catalog = "";
		while (resultSet.next()) {
			catalog = resultSet.getString("TABLE_CAT");
			if(!(catalog.equals("information_schema") || 
			     catalog.equals("mysql") ||
			     catalog.equals("performance_schema") ||
			     catalog.equals("sys"))){
				cat.add(catalog);
				//System.out.println("Schema Name = " + catalog);
			}
				
		}
		resultSet.close();
		return cat;
	}
	
	/**
	 * Get table names from database
	 * @param conn
	 */
	public Vector<String> getTablesNames() throws SQLException{
		String[] types = { "TABLE" };
		ResultSet resultSet;
		Vector<String> tables = new Vector<String>();

		resultSet = conn.getMetaData().getTables(databaseName, null, "%", types);
		String tableName = "";
		while (resultSet.next()) {
			tableName = resultSet.getString(3);
			tables.add(tableName);
			//System.out.println("Table Name = " + tableName);
		}
		resultSet.close();
		
		
		return tables;

	}
	
	/**
	 * Get table columns
	 * @param conn
	 */
	public Vector<String> getColumnNames(String tableName) throws SQLException{
		DatabaseMetaData meta;
		ResultSet resultSet;
		Vector<String> columnNames = new Vector<String>();
		meta = conn.getMetaData();
		resultSet = meta.getColumns(databaseName, null, tableName, "%");
		while (resultSet.next()) {
			String column = resultSet.getString(4);
			//System.out.println("Column Name of table " + tableName + " = "
			//		+ column);
			columnNames.add(column);
		}
		return columnNames;
	}
	
	/**
	 * Get foreign keys from a table in order to determine related tables
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public Vector<String> getForeignKeys(String tableName) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
	    ResultSet foreignKeys = metaData.getImportedKeys(databaseName, null, tableName);
	    Vector<String> relTables = new Vector<String>();
	    while (foreignKeys.next()) {
	        String fkTableName = foreignKeys.getString("FKTABLE_NAME");
	        String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
	        String pkTableName = foreignKeys.getString("PKTABLE_NAME");
	        String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
	        System.out.println("\t"+ fkTableName + "." + fkColumnName + " -> " + pkTableName + "." + pkColumnName);
	        relTables.add(pkTableName);
	    }
	    
	    return relTables;
	}
	
	/**
	 * Get selected database name
	 * @return
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Set selected database name
	 * @param databaseName
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	/**
	 * Create a database
	 * @param databaseName
	 * @throws SQLException
	 */
	public void createDatabase(String databaseName) throws SQLException {
		setDatabaseName(databaseName);
		Statement statement = conn.createStatement();
		String sql_create_db = "CREATE DATABASE " + this.databaseName;
		statement.executeUpdate(sql_create_db);
	    System.out.println("Database " + databaseName + "created successfully...");
	}
	
	/**
	 * Create a table
	 * @param tableName
	 * @param columns
	 * @param primaryKeys
	 * @param foreignKeys
	 * @throws SQLException
	 */
	public void createTable(String tableName, Map<String,String> columns, Vector<String> primaryKeys, Map<String, String> foreignKeys) throws SQLException{
		Statement statement = conn.createStatement();
		String sql_create_table = "CREATE TABLE " + getDatabaseName() + "." + tableName + " ( ";
		
		int i = 1;
		for(Map.Entry<String, String> column : columns.entrySet()){
			String column_name = column.getKey();
			String data_type = column.getValue();
			sql_create_table += column_name + " " + data_type;
			sql_create_table += ", ";
		
		}
		
		sql_create_table += "PRIMARY KEY (";
		i = 1;
		for(String primaryKey : primaryKeys){
			sql_create_table += primaryKey;
			if(i++ != primaryKeys.size())
				sql_create_table += ", ";
			else
				sql_create_table += ") ";	
		}

		if(!foreignKeys.isEmpty()){
			for(Map.Entry<String, String> foreignKey : foreignKeys.entrySet()){
				String key = foreignKey.getKey();
				String table = foreignKey.getValue();
				sql_create_table += "FOREIGN KEY ( "+ key + " ) REFERENCES " + table;
			}
		}

		sql_create_table += ")";

		System.out.println(sql_create_table);
		statement.executeUpdate(sql_create_table);
		System.out.println("Table " + tableName + "created successfully...");
	}
	
}

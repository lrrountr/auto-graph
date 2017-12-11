import java.util.Vector;



public class TableNode {
	public enum NodeType{
		CATALOG, FACT, DIMENSION
	}
	
	String name;
	int outDegree;
	int inDegree;
	Vector<String> columnNames;
	String primKey;
	Vector<String> foreignKeys;
	NodeType type;
	
	public TableNode(String name){
		this.name = name;
		outDegree = 0;
		inDegree = 0;
	}
}

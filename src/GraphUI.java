import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphUI<T> {
	public Graph graph;
			
	public GraphUI(){
		graph = new SingleGraph("simple");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.setStrict(false);
        graph.setAutoCreate(true);
       
	}
				
	public void insertEdge(String edgeName, String v1, String v2, boolean directed){
		graph.addEdge(edgeName, v1, v2, directed);
		Node node = graph.getNode(v1);
		node.addAttribute("ui.label", v1);
		
		Node node2 = graph.getNode(v2);
		node2.addAttribute("ui.label", v2);
    	
	}
	
	public void insertNode( String nodeName, int id){
		graph.addNode(nodeName);
		Node node = graph.getNode(nodeName);
		node.addAttribute("ui.label", node.getId());
       	node.setAttribute("Id", id);
	}
	
	public void displayGraph(){
		graph.display();
	}
	
	public void hideGraph(){
		graph.display(false);
	}
	
	public void setSelectedNode(String nodeName){
		Node node = graph.getNode(nodeName);
		node.setAttribute("ui.class", "big, important");
	}
	
	public void setSelectedEdge(String edgeName){
		Edge edge = graph.getEdge(edgeName);
		if(edge!=null)
			edge.setAttribute("ui.class", "selected");
	}
	
	public void setUnselectedNode(String nodeName){
		Node node = graph.getNode(nodeName);
		node.setAttribute("ui.class", "none");
	}
	public void setUnSelectedEdge(String edgeName){
		Edge edge = graph.getEdge(edgeName);
		edge.setAttribute("ui.class", "none");
	}
	
	public void insertEdge(String edgeName, String v1, String v2, String edgeLabel, boolean directed){
		Edge e = graph.addEdge(edgeName, v1, v2, directed);
		Node node = graph.getNode(v1);
		node.addAttribute("ui.label", v1);
		
		Node node2 = graph.getNode(v2);
		node2.addAttribute("ui.label", v2);
    	e.addAttribute("ui.label", edgeLabel);
	}
	
	protected String styleSheet =
		        "node {" +
		        "   fill-color: black;" +
		        "	size: 20px;"+
		    	"	stroke-mode: plain;"+
		    	"	stroke-color: black;"+
		    	"	stroke-width: 1px;"+
		        "}" +
		        "node.selected {" +
		        "   fill-color: red;" +
		        "}"+
		        "edge {" +
			        "   fill-color: black;" +
			        "}" +
		        "edge.selected {" +
		        "   fill-color: red;" +
		        "}" +
		        "node.important {" +
		           " fill-color: red;"+
		        "}" +
		        "node.big {" +
		          "  size: 45px;" +
		        "}";
	
}
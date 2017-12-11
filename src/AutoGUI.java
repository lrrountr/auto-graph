import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;

public class AutoGUI {

	private JFrame frame;
	private JPanel bottomPanel = new JPanel();
	private JList<String> factCandidatesList =  new JList<String>();
	private JList<String> dimensionsList =  new JList<String>();
	private JList<String> dimensionColumnsList =  new JList<String>();
	private JComboBox<String> schemasComboBox;
	private SQLManager sqlMgr = new SQLManager();
	private JCheckBox entityGraphCheckBox = new JCheckBox("ER graph");
	private JCheckBox dimensionalGraphCheckBox = new JCheckBox("DIM graph");
	private JButton selectKeysBtn = new JButton("Keys");
	private JButton selectColumnsBtn = new JButton("Columns");
	private JButton createDimensionTableBtn = new JButton("Create Table");
	private String selectedDimension = new String();
	private List<String> selectedColumns = new ArrayList<String>();
	private List<String> selectedKeys = new ArrayList<String>();
	private EntityGraphProcessor entityGraphMaker;
	private DimensionalGraphProcessor dimensionalGraphMaker;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AutoGUI window = new AutoGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AutoGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		Vector<String> dbSchemas = new Vector<String>();
		
	    
		try {
			sqlMgr.connectToDatabase();
			dbSchemas = sqlMgr.getCatalogs();
			
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		

		schemasComboBox = new JComboBox<String>(dbSchemas);
		frame.getContentPane().add(schemasComboBox, BorderLayout.NORTH);
		schemasComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				entityGraphCheckBox.setVisible(false);
				dimensionalGraphCheckBox.setVisible(false);
				selectKeysBtn.setVisible(false);
				selectColumnsBtn.setVisible(false);
				createDimensionTableBtn.setVisible(false);
				dimensionsList.setVisible(false);
				dimensionColumnsList.setVisible(false);
				String databaseName = (String) schemasComboBox.getSelectedItem();
				sqlMgr.setDatabaseName(databaseName);
				Vector<String> factTables = createGraph();
				factCandidatesList.setListData(factTables);
				entityGraphCheckBox.setVisible(true);
			}
		});
		
		frame.getContentPane().add(factCandidatesList, BorderLayout.WEST);
		factCandidatesList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(2 == e.getClickCount()){
					String selectedFact = factCandidatesList.getSelectedValue();
					if(startDimensional(selectedFact)){
						dimensionalGraphCheckBox.setVisible(true);
						Vector<String> dimensionTableNames =  dimensionalGraphMaker.getDimensionTablesNames();
						dimensionsList.setVisible(true);
						dimensionsList.setListData(dimensionTableNames);
					}
					
				}
				
			}
		});
		
		frame.getContentPane().add(dimensionsList, BorderLayout.CENTER);
		dimensionsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(2 == e.getClickCount()){
					selectedDimension = dimensionsList.getSelectedValue();
					Vector<String> dimensionTableNames = dimensionalGraphMaker.getPossibleColumns(selectedDimension);
					dimensionColumnsList.setVisible(true);
					dimensionColumnsList.setListData(dimensionTableNames);
					selectKeysBtn.setVisible(true);
					selectColumnsBtn.setVisible(true);
					createDimensionTableBtn.setVisible(true);
				}
				
			}
		});
		
		frame.getContentPane().add(dimensionColumnsList, BorderLayout.EAST);

		
		entityGraphCheckBox.setVisible(false);
		entityGraphCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = entityGraphCheckBox.isSelected();
				if(selected)
					displayGraph("er", true);
			}
		});
		
		dimensionalGraphCheckBox.setVisible(false);
		selectKeysBtn.setVisible(false);
		selectColumnsBtn.setVisible(false);
		createDimensionTableBtn.setVisible(false);
		dimensionsList.setVisible(false);
		
		
		dimensionalGraphCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = dimensionalGraphCheckBox.isSelected();
				if(selected)
					displayGraph("dim", true);
			}
		});
		
		selectKeysBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedKeys = dimensionColumnsList.getSelectedValuesList();
				System.out.println("Selected Keys(s):" +  selectedKeys.toString());
				
			}
		});
		
		selectColumnsBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedColumns = dimensionColumnsList.getSelectedValuesList();
				System.out.println("Selected Columns(s):" +  selectedColumns.toString());
				
			}
		});
		
		createDimensionTableBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedColumns.addAll(selectedKeys);
				dimensionalGraphMaker.createDimensionalTable(selectedDimension, selectedColumns, selectedKeys);
				
			}
		});
		bottomPanel.add(entityGraphCheckBox);
        bottomPanel.add(dimensionalGraphCheckBox);
        bottomPanel.add(selectKeysBtn);
        bottomPanel.add(selectColumnsBtn);
        bottomPanel.add(createDimensionTableBtn);
        
	}

	/**
	 * Call Entity Relationship graph create and get possible fact tables
	 * @return
	 */
	private Vector<String> createGraph(){
		entityGraphMaker = new EntityGraphProcessor(sqlMgr);
		entityGraphMaker.createGraph();
		return entityGraphMaker.getFactTables();
	}
	
	/**
	 * Start the creation of a dimensional database
	 * @param selectedFact
	 * @return
	 */
	private boolean startDimensional(String selectedFact){
		TableNode factTable = entityGraphMaker.getFactTableNode(selectedFact);
		Vector<Vector<TableNode>> dimensions = entityGraphMaker.getDimensions(factTable);
		if(dimensions.isEmpty()){
			System.err.println("No dimensions available for " + selectedFact);
			return false;
		}
		
		dimensionalGraphMaker = new DimensionalGraphProcessor(factTable, dimensions);
		dimensionalGraphMaker.createGraph();
		dimensionalGraphMaker.createDimensionalDatabase();
		return true;
		
	}
	
	/**
	 * Display graph (ER or Dimensional)
	 * @param type
	 * @param selected
	 */
	private void displayGraph(String type, boolean selected){
		try{
			if(selected && type.equals("er"))
				entityGraphMaker.graphUI.displayGraph();
			if(selected && type.equals("dim"))
				dimensionalGraphMaker.graphUI.displayGraph();

		}catch(NullPointerException e){
			System.out.println("DB Graph not yet created");
		}
	}

}

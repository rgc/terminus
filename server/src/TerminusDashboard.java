import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.HashMap;

public class TerminusDashboard extends JPanel {
	private JFrame frame;
	private JTable table;
	private JPanel picturePanel;
	private JSplitPane splitPane;
	private EventTableModel tableModel;
	private HashMap<String, JLabel> nodeScreens;    	

	public TerminusDashboard() {

		// event table model
		tableModel = new EventTableModel();

		// event table
		JTable table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(300, 100));
		table.setFillsViewportHeight(true);  

		// scrollpane for event table
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setMinimumSize(new Dimension(300, 100));

		// panel to hold all pictures, give it a flow layout so it breaks
		// as new nodes are added
		picturePanel = new JPanel();
		FlowLayout experimentLayout = new FlowLayout();
		picturePanel.setLayout(experimentLayout);

		// split pane with the two panels in it.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tableScrollPane, picturePanel);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);
		splitPane.setPreferredSize(new Dimension(1200, 600));

		// main frame
		frame = new JFrame("Terminus");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(splitPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);

		nodeScreens = new HashMap<String, JLabel>();

	}

	class EventTableModel extends AbstractTableModel {

		private List<String> columnNames = new ArrayList();
		private List<List> data = new ArrayList();

		EventTableModel() {	
			columnNames.add("TimeStamp");
			columnNames.add("Type");
			columnNames.add("Node");
		}

		public void addRow(List rowData)
		{
			data.add(0, rowData);
			fireTableRowsInserted(0,0);
		}

		public int getColumnCount() {
			return columnNames.size();
		}

		public int getRowCount() {
			return data.size();
		}

		public String getColumnName(int col) {
			return columnNames.get(col);
		}

		public Object getValueAt(int row, int col) {
			return data.get(row).get(col);
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

	}

	public void addMessage(String id, String type, Date ts) {
		tableModel.addRow(Arrays.asList(ts, type, id));
	}

	public void addUpdateImage(String nodeId, byte[] img) {

		if(!nodeScreens.containsKey(nodeId)) {
			JLabel picLabel = new JLabel();
			nodeScreens.put(nodeId,picLabel);
			picturePanel.add(nodeScreens.get(nodeId));
		}

		nodeScreens.get(nodeId).setIcon(new ImageIcon(img));
		picturePanel.revalidate();
		picturePanel.repaint();

	}

}

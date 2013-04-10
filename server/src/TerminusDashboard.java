import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.text.SimpleDateFormat;

public class TerminusDashboard extends JPanel {
	private JFrame frame;
	private JTable table;
	private JDesktopPane desktop;
	private JSplitPane splitPane;
	private EventTableModel tableModel;
	private HashMap<String, NodeScreenFrame> nodeScreens;    	

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

		desktop = new JDesktopPane();
		desktop.setMinimumSize(new Dimension(900, 600));
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		
		// split pane with the two panels in it.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tableScrollPane, desktop);

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

		nodeScreens = new HashMap<String, NodeScreenFrame>();
		
	}
	
	public void addMessage(String id, String type, Date ts) {
		
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
		
		tableModel.addRow(Arrays.asList(dateFormat.format(ts), type, id));
				
	}

	public void addUpdateImage(String nodeId, byte[] img) {

		ImageIcon updatedImage = new ImageIcon(img);
		
		if(!nodeScreens.containsKey(nodeId)) {

			NodeScreenFrame tframe = new NodeScreenFrame(nodeId);
			tframe.setCameraScreen(img);
		    desktop.add(tframe);
		    tile(desktop);

			nodeScreens.put(nodeId,tframe);

		} else {
			nodeScreens.get(nodeId).setCameraScreen(img);
			nodeScreens.get(nodeId).setVisible(true);

		}		

	}
	
	public void removeNode(String nodeId) {
		if(nodeScreens.containsKey(nodeId)) {
			nodeScreens.get(nodeId).setVisible(false);
		}
	}
	
	public static void tile( JDesktopPane desktopPane ) {
	    JInternalFrame[] frames = desktopPane.getAllFrames();
	    if ( frames.length == 0) return;
	 
	    Rectangle dBounds = desktopPane.getBounds();
	    
	    int x = 0;
	    int y = 0;
	    
	    for(int i = 0; i < frames.length; i++ ) {
	    	Rectangle fbounds = frames[i].getBounds();
	    	if(x + fbounds.width > dBounds.width) {
	    		x  = 0;
	    		y += fbounds.height;
	    	}
	    	
	    	frames[i].setLocation(x,y);
	    	x += fbounds.width;
	    	
	    }
	    
	    
	}
	
	class NodeScreenFrame extends JInternalFrame {
		
		private JLabel picLabel;
		
		NodeScreenFrame(String nodeId) {
			super(	nodeId,
					false,	// resize
					true,   // close
					false,  // maximize
					true);
			
			picLabel = new JLabel();
			
			this.add(picLabel);
			this.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
			
		}
		
		void setCameraScreen(byte[] i) {
			ImageIcon updatedImage = new ImageIcon(i);
			this.setCameraScreen(updatedImage);
		}
		
		void setCameraScreen(ImageIcon i) {
			picLabel.setIcon(i);
			picLabel.revalidate();
			picLabel.repaint();
			
			this.setSize(	i.getIconWidth()  + 10,
	    			   		i.getIconHeight() + 32);
			
		    this.setVisible(true);
		    
		    try {
	    		this.setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {}
			
		}
		
	}
	

	class EventTableModel extends AbstractTableModel {

		private final int MAX_EVENTS		= 100;
		private List<String> columnNames 	= new ArrayList();
		private List<List> data 			= new Stack();

		EventTableModel() {	
			columnNames.add("TimeStamp");
			columnNames.add("Type");
			columnNames.add("Node");
		}

		public void addRow(List rowData)
		{
			data.add(0, rowData);
			
			if(data.size() > 100) {
				data.remove(data.size()-1);
			}
			
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
	

}

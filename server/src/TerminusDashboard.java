import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
import edu.buffalo.cse.terminus.messages.TerminusMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.text.SimpleDateFormat;

public class TerminusDashboard extends JPanel 
{
	private class NodeInfo
	{
		public String id;
		public String location;
		public String nickname;
	}
	
	private JFrame frame;
	private JTable table;
	private JDesktopPane desktop;
	private JSplitPane splitPane;
	private EventTableModel tableModel;
	private HashMap<String, NodeScreenFrame> nodeScreens;    	
	private HashMap<String, NodeInfo> nodeInfo;
	
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
		nodeInfo = new HashMap<String, TerminusDashboard.NodeInfo>();
	}
	
	public void addMessage(TerminusMessage message) 
	{
        if (message.getMessageType() == TerminusMessage.MSG_EVENT)
        	addEvent((EventMessage)message);
        else
        	addMiscMessage(message);
	}
	
	private void addMiscMessage(TerminusMessage message)
	{
		String type = "Unknown";
		
		switch (message.getMessageType())
		{
		case TerminusMessage.MSG_REGISTER:
			type = "Reg";
			this.addNode((RegisterMessage) message);
			break;
	
		case TerminusMessage.MSG_UNREGISTER:
			type = "Unreg";
			this.removeNode(message.getID());
			break;
	
		case TerminusMessage.MSG_TEST:
			type = "Test";
			break;
		}
		
		tableModel.addRow(Arrays.asList("", type, message.getID(), ""));
	}
	
	private void addEvent(EventMessage event)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        String timestamp = dateFormat.format(event.getTimestamp());
        String priority = String.valueOf(event.getTotalPriority());
        String id = event.getID();
        String type = "";
        
        switch (event.getEventMsgType())
        {
        case EventMessage.EVENT_START:
        	type = "Event Start";
        	break;
        
        case EventMessage.EVENT_UPDATE:
        	type = "Event Update";
        	break;
        	
        case EventMessage.EVENT_END:
        	type = "Event End";
        	break;
        }
        
		tableModel.addRow(Arrays.asList(timestamp, type, id, priority));
		updateNodeEvent(event);
	}
	
	private void updateNodeEvent(EventMessage event)
	{
		String id = event.getID();
		if (!nodeScreens.containsKey(id)) 
		{
			String title = id;
			NodeInfo info = nodeInfo.get(id);
			
			if (info != null)
			{
				title = "Loc: " + info.location + "  Device: " + info.nickname;
			}
			
			NodeScreenFrame tframe = new NodeScreenFrame(title);
			tframe.updateEvent(event);
		    desktop.add(tframe);
		    tile(desktop);
			nodeScreens.put(id, tframe);
		}
		else
		{
			nodeScreens.get(id).updateEvent(event);
		}
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
		
		if (nodeInfo.containsKey(nodeId))
		{
			nodeInfo.remove(nodeId);
		}
	}
	
	public void addNode(RegisterMessage rm)
	{
		if (!nodeInfo.containsKey(rm.getID()))
		{
			NodeInfo info = new NodeInfo();
			info.id = rm.getID();
			info.location = rm.getLocation();
			info.nickname = rm.getNickname();
			
			nodeInfo.put(info.id, info);
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
		private NodeEventTableModel nodeTableModel;
		
		NodeScreenFrame(String title) 
		{
			super(	title,
					false,	// resize
					true,   // close
					false,  // maximize
					true);
			
			// Main panel
			JPanel panel = new JPanel();
			panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
			
			picLabel = new JLabel();
			panel.add(picLabel);
			
			nodeTableModel = new NodeEventTableModel();
			JTable table = new JTable(nodeTableModel);
			JScrollPane tableScrollPane = new JScrollPane(table);
			//tableScrollPane.setMinimumSize(new Dimension(300, 100));
			//table.setPreferredScrollableViewportSize(new Dimension(300, 100));
			//table.setFillsViewportHeight(true);
			panel.add(tableScrollPane);
			
			this.add(panel);
			this.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
			
		}
		
		void updateEvent(EventMessage em)
		{
			nodeTableModel.updateEvent(em);
			
			/*
			 * In case the camera is off...
			 */
			if (!this.isVisible())
			{
				this.setSize(175, 175);
				this.setVisible(true);
			    
			    try {
		    		this.setSelected(true);
			    } catch (java.beans.PropertyVetoException e) {}
			}
		}
		
		void setCameraScreen(byte[] i) {
			ImageIcon updatedImage = new ImageIcon(i);
			this.setCameraScreen(updatedImage);
		}
		
		void setCameraScreen(ImageIcon i) {
			picLabel.setIcon(i);
			picLabel.revalidate();
			picLabel.repaint();
			
			//this.setSize(	i.getIconWidth()  + 10,
	    	//		   		i.getIconHeight() + 32);
			
			this.setSize(i.getIconWidth() + 10, i.getIconHeight() + 175);
			
		    this.setVisible(true);
		    
		    try {
	    		this.setSelected(true);
		    } catch (java.beans.PropertyVetoException e) {}
			
		}
		
	}
	

	class EventTableModel extends AbstractTableModel 
	{
		private final int MAX_EVENTS		= 100;
		private List<String> columnNames 	= new ArrayList();
		private List<List> data 			= new Stack();

		EventTableModel() {	
			columnNames.add("TimeStamp");
			columnNames.add("Type");
			columnNames.add("Node");
			columnNames.add("Pri");
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
	
	/*
	 * Data model for the individual nodes' events
	 */
	class NodeEventTableModel extends AbstractTableModel 
	{
		private List<String> columnNames 	= new ArrayList();
		private List<List> data 			= new ArrayList();
		private Date lastStart;
		
		NodeEventTableModel() 
		{
			columnNames.add("Event Type");
			columnNames.add("Priority");
		}
		
		private String getShortTypeName(int type)
		{
			switch (type) 
			{
				case EventMessage.EVENT_ACCELEROMETER:
					return "Accel";
				case EventMessage.EVENT_MAGNETOMETER:
					return  "Magno";
				case EventMessage.EVENT_LIGHT:
					return "Light";
				case EventMessage.EVENT_CAMERA_MOTION:
					return "Camera";
				case EventMessage.EVENT_SOUND:
					return "Sound";
				default:
					return "";
			}
		}
		
		public void updateEvent(EventMessage em)
		{
			data = new ArrayList();
			
			if (em.getEventMsgType() == EventMessage.EVENT_END)
			{
				data.add(Arrays.asList("The event has ended", ""));
				
				if (lastStart != null)
				{
					long duration =  em.getTimestamp().getTime() - lastStart.getTime();
					long sec = duration / 1000;
					long min = sec / 60;
					long hr = min / 60;
					
					sec -= (60 * min);
					min -= (60 * hr);
					String out = String.valueOf(hr) + " hr " + String.valueOf(min) + " min " + String.valueOf(sec) + " sec"; 
					data.add(Arrays.asList("Duration", out));
				}
			}
			else
			{
				if (em.getEventMsgType() == EventMessage.EVENT_START)
				{
					lastStart = em.getTimestamp();
				}
				
				data.add(Arrays.asList("Total", String.valueOf(em.getTotalPriority())));
				
				int[] priorities = em.getSensorPriorities();
				
				for (int i = 0; i < priorities.length; i++)
					data.add(Arrays.asList(getShortTypeName(i), String.valueOf(priorities[i])));	
			}
			fireTableRowsInserted(0, data.size()-1);
		}
		
		public int getColumnCount() 
		{
			return columnNames.size();
		}

		public int getRowCount() 
		{
			return data.size();
		}

		public String getColumnName(int col) 
		{
			return columnNames.get(col);
		}

		public Object getValueAt(int row, int col) 
		{
			if (row >= data.size())
				return null;
			else
				return data.get(row).get(col);
		}

		public Class getColumnClass(int c) 
		{
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}
	}

}

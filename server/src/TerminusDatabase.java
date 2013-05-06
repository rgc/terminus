

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class TerminusDatabase
{
	private Connection connection;
	private String dbFileName = "terminus.sqlite";

	public TerminusDatabase() throws ClassNotFoundException
	{
	    // load the sqlite JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
		
	    try
	    {
	      // create a database connection
	      connection = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
	    }
	    catch(SQLException e)
	    {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
	}
	
	public void initDB() {
		
		// run only once -- to create db
		
	    try
	    {
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.

		      statement.executeUpdate("create table event (id integer, name string)");
	    }
	    catch(SQLException e)
	    {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
		
	}
	
	public void addEventMessage(TerminusMessage msg)
	{
		  /* 
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        String timestamp = dateFormat.format(msg.getTimestamp());
        String priority = String.valueOf(msg.getTotalPriority());
        String id = msg.getID();
        String type = "";
     
        switch (msg.getEventMsgType())
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
        */
		//tableModel.addRow(Arrays.asList(timestamp, type, id, priority));		
		
	}
	
	public void addEventVideo(String id, String url)
	{
		
	}
     
}   
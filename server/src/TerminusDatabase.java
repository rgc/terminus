

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class TerminusDatabase
{
	private Connection connection;
	private String dbFileName = "www/db/terminus.sqlite";

	public TerminusDatabase() throws ClassNotFoundException
	{
		
		boolean initNeeded = false;
		
		File f = new File(dbFileName);
		if(!f.exists()) {
			initNeeded = true;
		}
		
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
	    
	    if(initNeeded) {
	    	initDB();
	    }
	    
	}
	
	public void initDB() {
		
		// at this point, database needs to be created
		
	    try
	    {
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.

		      statement.executeUpdate("create table event (ts timestamp, priority string, id string, type string, tag string, mediapath string)");
	    }
	    catch(SQLException e)
	    {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
		
	}
	
	public void closeDB() {
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
	
	public void addEventRow(long ts, String priority, String id, String type)
	{
		addEventRow(ts,priority,id,type,"","");
	}
	
	public void addEventRow(long ts, String priority, String id, String type, String tag, String mediapath )
	{
		String insert = "insert into event (ts,priority,id,type,tag,mediapath) VALUES " +
						"(" + 
						"\"" + ts + "\", " +
						"\"" + priority + "\", " +
						"\"" + id + "\", " +
						"\"" + type + "\", " +
						"\"" + tag + "\", " +
						"\"" + mediapath + "\""
						+ ")";
		//System.err.println(insert);
		execQuery(insert);
	}
	
	public void execQuery(String sql) {
				
	    try
	    {
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.

		      statement.executeUpdate(sql);
	    }
	    catch(SQLException e)
	    {
	      // if the error message is "out of memory", 
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
		
	}	
     
}   
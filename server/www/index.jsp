<%@ page language="java" session="true" contentType="text/html; charset=iso-8859-1" %>

<%@ page language="java" import="java.util.*" errorPage="" %>
<%@ page language="java" import="java.sql.*" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>

<%@ page import="org.sqlite.*" %>
<%@include file="header.jsp" %>
            <h2>Recent Video Events</h2>
            <p>These are videos of recent events occurring 
            <%
            	String filter = "";
            	
            	if(request.getParameter("device") != null) {
            		filter = " AND id=\"" + request.getParameter("device") + "\"";
            		out.println(" <b>on device: " + request.getParameter("device") + "</b>");
            	} else if(request.getParameter("location") != null) {
            	filter = " AND location=\"" + request.getParameter("location") + "\"";
            		out.println(" <b>in location: " + request.getParameter("location") + "</b>" );
            	} else {
            		out.println(" <b>at all security monitoring points</b>");
            	}
            
            %>
            
            </p>
            <%
 				//out.println(sql);
 				
                Statement stat = conn.createStatement();
 				String sql = "select ts, id,tag,location,mediapath,strftime('%m/%d/%Y %H:%M:%S', datetime(ts, 'unixepoch','localtime')) as tsf from event where type=\"Video\"" + filter + " order by ts DESC";

                ResultSet rs = stat.executeQuery(sql);
 
                while (rs.next()) {
                	String vid = rs.getString("mediapath");
                	vid = vid.replaceAll("www/", ""); 
                    out.println("<p>");
                    
                    out.println("Date/Time: <b>" + rs.getString("tsf") + "</b><br/>");
                    
                    out.println("Phone: " + rs.getString("id") + "<br/>");
                    out.println("Nickname: " + rs.getString("tag") + "<br/>");
                    out.println("Location: " + rs.getString("location") + "<br/>");
                    
                    out.println("<video width=\"800\" height=\"600\" controls><source src=\"" + vid + "\" type=\"video/mp4\"><object width=\"800\" height=\"600\" data=\"" + vid + "\"><embed width=\"800\" height=\"600\" src=\"" + vid + "\"></object></video>");
                    out.println("</p>");
                    
                }
 
                rs.close();

            %>

<%@include file="footer.jsp" %>


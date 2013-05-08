<%@ page language="java" session="true" contentType="text/html; charset=iso-8859-1" %>

<%@ page language="java" import="java.util.*" errorPage="" %>
<%@ page language="java" import="java.sql.*" %>
<%@ page import="org.sqlite.*" %>

        </div><!-- /article -->
    </div><!-- /content -->
    <div data-role="panel" id="left-panel" data-theme="c">
        <ul data-role="listview" data-theme="d">
            <li data-icon="delete"><a href="#" data-rel="close">Close</a></li>
            <li data-role="list-divider">Menu</li>
            <li data-icon="home"><a href="/" data-ajax="false">Dashboard</a></li>
        </ul>
        <div data-role="collapsible" data-inset="false" data-iconpos="right" data-theme="d" data-content-theme="d">
          <h3>Categories</h3>
          <div data-role="collapsible-set" data-inset="false" data-iconpos="right" data-theme="b" data-content-theme="d">
            <div data-role="collapsible">
              <h3>Devices</h3>
              <ul data-role="listview">
              <%
                stat = conn.createStatement();
                rs = stat.executeQuery("select distinct(id) from event");
 
                while (rs.next()) {
                    out.println("<li><a href=\"/?device=" + rs.getString("id") + "\" data-ajax=\"false\">" + rs.getString("id") + "</a></li>");                    
                }
 
                rs.close();
               %>
              </ul>
            </div><!-- /collapsible -->
            <div data-role="collapsible">
              <h3>Locations</h3>
              <ul data-role="listview">
              <%
                stat = conn.createStatement();
                rs = stat.executeQuery("select distinct(location) from event");
 
                while (rs.next()) {
                    out.println("<li><a href=\"/?location=" + rs.getString("location") + "\" data-ajax=\"false\">" + rs.getString("location") + "</a></li>");                    
                }
 
                rs.close();
               %>
              </ul>
            </div><!-- /collapsible -->
          </div><!-- /collapsible-set -->
        </div><!-- /collapsible -->
    </div><!-- /panel -->
</div>

</body>
</html>

<%
  conn.close();
%>

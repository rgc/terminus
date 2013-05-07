<%@ page language="java" session="true" contentType="text/html; charset=iso-8859-1" %>

<%@ page language="java" import="java.util.*" errorPage="" %>
<%@ page language="java" import="java.sql.*" %>
<%@ page import="org.sqlite.*" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
	"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Terminus</title>
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.3.1/jquery.mobile-1.3.1.min.css" />
<link rel="stylesheet" href="css/terminus.css" />
<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
<script src="http://code.jquery.com/mobile/1.3.1/jquery.mobile-1.3.1.min.js"></script>
</head>

<body>
<div data-role="page" id="dashboard" data-theme="d" data-url="dashboard">
    <div data-role="header" data-theme="c">
        <h1>Dashboard</h1>
        <a href="#left-panel" data-icon="bars" data-iconpos="notext" data-shadow="false" data-iconshadow="false">Menu</a>
    </div><!-- /header -->
    <div data-role="content">
        <div class="article">
            <h2>Recent Video Events</h2>
            <p>These are videos of recent events occurring on all security monitoring points...</p>
            <%
                Class.forName("org.sqlite.JDBC");
                Connection conn =
                     DriverManager.getConnection("jdbc:sqlite:www/db/terminus.sqlite");
                Statement stat = conn.createStatement();
 
                ResultSet rs = stat.executeQuery("select * from event where type=\"Video\";");
 
                while (rs.next()) {
                	String vid = rs.getString("mediapath");
                	vid = vid.replaceAll("www/", ""); 
                    out.println("<p>");
                    out.println("Phone: " + rs.getString("id") + "<br/>");

					out.println("<object data=\"" + vid + "\"></object>");
                    out.println("</p>");
                }
 
                rs.close();
                conn.close();
            %>

        </div><!-- /article -->
    </div><!-- /content -->
    <div data-role="panel" id="left-panel" data-theme="c">
        <ul data-role="listview" data-theme="d">
            <li data-icon="delete"><a href="#" data-rel="close">Close</a></li>
            <li data-role="list-divider">Menu</li>
            <li data-icon="home"><a href="index.jsp">Dashboard</a></li>
        </ul>
        <div data-role="collapsible" data-inset="false" data-iconpos="right" data-theme="d" data-content-theme="d">
          <h3>Categories</h3>
          <div data-role="collapsible-set" data-inset="false" data-iconpos="right" data-theme="b" data-content-theme="d">
            <div data-role="collapsible">
              <h3>Phones</h3>
              <ul data-role="listview">
                <li><a href="#">1</a></li>
                <li><a href="#">2</a></li>
                <li><a href="#">3</a></li>
                <li><a href="#">4</a></li>
              </ul>
            </div><!-- /collapsible -->
            <div data-role="collapsible">
              <h3>Locations</h3>
              <ul data-role="listview">
                <li><a href="#">Room</a></li>
                <li><a href="#">Den</a></li>
                <li><a href="#">Kitchen</a></li>
              </ul>
            </div><!-- /collapsible -->
          </div><!-- /collapsible-set -->
        </div><!-- /collapsible -->
    </div><!-- /panel -->
</div>

</body>
</html>

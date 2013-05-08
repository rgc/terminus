<%@ page language="java" session="true" contentType="text/html; charset=iso-8859-1" %>

<%@ page language="java" import="java.util.*" errorPage="" %>
<%@ page language="java" import="java.sql.*" %>
<%@ page import="org.sqlite.*" %>

<%
    Class.forName("org.sqlite.JDBC");
    
    SQLiteConfig dbConfig = new SQLiteConfig();

	dbConfig.setReadOnly(true);
    
    Connection conn =
           DriverManager.getConnection("jdbc:sqlite:www/db/terminus.sqlite", dbConfig.toProperties());
                     
%>

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
        <h1>Terminus</h1>
        <a href="#left-panel" data-icon="bars" data-iconpos="notext" data-shadow="false" data-iconshadow="false">Menu</a>
    </div><!-- /header -->
    <div data-role="content">
        <div class="article">
        	<a href="javascript:location.reload()" data-ajax="false" style="float:right;">REFRESH</a>
        
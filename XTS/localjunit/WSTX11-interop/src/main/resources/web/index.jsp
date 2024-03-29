<!--
   SPDX short identifier: Apache-2.0
 -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.arjuna.webservices11.ServiceRegistry"%>
<%@page import="com.jboss.transaction.txinterop.test.TestConstants"%>

<%!
private final static String SERVICE_URI = "" ;%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JBoss Transactions WS-TX Interop</title>
</head>
<body>
<h1>JBoss Transactions WS-TX 1.1 Interop</h1>
<h2>Introduction</h2>
<p>This web application implements a set of interoperability tests specified by the <a href="http://www.oasis-open.org/apps/org/workgroup/ws-tx/">Oasis WS-TX Technical Committee</a> site.</p>
<p>Please send any queries to the <a href="mailto:kevin.conner@jboss.com?subject=Interop%20query">interop test contact</a></p>
<h2>Interop tests</h2>
<form action="test" method="post">
<p>Service URI: <input name="<%= TestConstants.PARAM_SERVICE_URI %>" maxlength="2000" size="100" value="<%= SERVICE_URI %>"/></p>
<p>Test timeout: <input name="<%= TestConstants.PARAM_TEST_TIMEOUT %>" maxlength="10" size="10" value="120000"/></p>
<!-- the current JaxWS based interop11 tests only runs synchronous tests for now
<p>Asynchronous Test application: <input name="<%= TestConstants.PARAM_ASYNC_TEST %>" type="checkbox" checked="checked"/></p>
-->
<select name="<%= TestConstants.PARAM_TEST %>">
<!-- the JaxWS interop test code has two separate services, one for AT and one for BA. since we only have
     one participant URL we have to assume it is either the AT server or the BA server and run either AT
     tests or BA tests but not both
<option value="<%= TestConstants.NAME_ALL_TESTS %>">All tests</option>
-->
<option value="<%= TestConstants.NAME_ALL_AT_TESTS %>">All AT tests</option>
<option value="<%= TestConstants.NAME_ALL_BA_TESTS %>">All BA tests</option>
<%

  final Map descriptions = TestConstants.DESCRIPTIONS ;
  final Iterator entryIter = descriptions.entrySet().iterator() ;
  while(entryIter.hasNext())
  {
      final Map.Entry entry = (Map.Entry)entryIter.next() ;
      final String testName = (String)entry.getKey() ;
      final String testDescription = (String)entry.getValue() ;
%>
<option value="<%= testName %>"><%=testName + " - " + testDescription%></option>
<%

}
%>
</select>
<p>
<input type="submit" value="Execute"/>
</p>
</form>
</body>
</html>
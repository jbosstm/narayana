<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.arjuna.webservices11.ServiceRegistry"%>
<%@page import="com.jboss.transaction.wstf.test.TestConstants"%>
<%@page import="com.jboss.transaction.wstf.webservices.InteropConstants"%>

<%!
private final static String SERVICE_URI = ServiceRegistry.getRegistry().getServiceURI(InteropConstants.SERVICE_PARTICIPANT) ;%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JBoss Transactions WSTF Sc007 Interop</title>
</head>
<body>
<h1>JBoss Transactions WSTF Sc007 Interop</h1>
<h2>Introduction</h2>
<p>This web application implements the <a href="http://www.wstf.org/docs/scenarios/sc007/sc007.xml">scenario sc007</a> interoperability tests specified by the <a href="http://www.wstf.org/">Web Services Test Forum</a> Group.</p>
<p>Please send any queries to the <a href="mailto:adinn@redhat.com?subject=Interop%20query">Red Hat Test Forum contact</a></p>
<h2>Sc007 tests</h2>
<p>Enter the URL of the sc007 participant service to be used to run these tests and a timeout for each individual test<br>
 n.b. the JBoss participant has URL http://endpoint.jbossts.org:9090/sc007/ParticipantService</p>
<form action="test" method="post">
<p>Sc007 Participant Service URI: <input name="<%= TestConstants.PARAM_SERVICE_URI %>" maxlength="2000" size="100" value="<%= SERVICE_URI %>"/></p>
<p>Test timeout: <input name="<%= TestConstants.PARAM_TEST_TIMEOUT %>" maxlength="10" size="10" value="120000"/></p>
<!-- the current JaxWS based interop11 tests only runs synchronous tests for now
<p>Asynchronous Test application: <input name="<%= TestConstants.PARAM_ASYNC_TEST %>" type="checkbox" checked="checked"/></p>
-->
<select name="<%= TestConstants.PARAM_TEST %>">
<option value="<%= TestConstants.NAME_ALL_TESTS %>">All AT tests</option>
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

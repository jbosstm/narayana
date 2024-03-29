<!--
   SPDX short identifier: Apache-2.0
 -->

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.jboss.transaction.wstf.test.TestConstants"%>
<%@page import="java.util.Enumeration"%>
<%@page import="junit.framework.TestResult"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="junit.framework.TestFailure"%>
<%@page import="junit.framework.TestCase"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JBoss Transactions WS-TX Interop detail page</title>
</head>
<body>
<h1>Results</h1>
<%
	final TestResult testResult = (TestResult)session.getAttribute(TestConstants.ATTRIBUTE_TEST_RESULT) ;
	if (testResult == null)
	{
%>
No JUnit test results generated.
<%
	}
	else
	{
		final String type = request.getParameter("type") ;
		
		Enumeration detailEnum = null ;
		if (type != null)
		{
		    if ("error".equals(type))
		    {
		        detailEnum = testResult.errors() ;
		    }
		    else if ("failure".equals(type))
		    {
		        detailEnum = testResult.failures() ;
		    }
		}
		
		Integer indexInt = null ;
		if (detailEnum != null)
		{
			final String indexVal = request.getParameter("index") ;
			try
			{
			    indexInt = Integer.valueOf(indexVal) ;
			}
			catch (final NumberFormatException nfe) {}
		}
		
		TestFailure testFailure = null ;
		if (indexInt != null)
		{
		    int index = indexInt.intValue() ;
		    if (index > 0)
		    {
			    while(detailEnum.hasMoreElements())
			    {
		        		final Object current = detailEnum.nextElement() ;
		        		if (--index == 0)
		        		{
		        		    testFailure = (TestFailure)current ;
		        		    break ;
		        		}
			    }
		    }
		}
		
		if (testFailure == null)
		{
%>
<p>Invalid request parameters</p>
<%
		}
		else
		{
		    final TestCase failure = (TestCase)testFailure.failedTest() ;
%>
<p>Test: <%= failure.getName() %></p>
<pre>
<%= testFailure.trace() %>
</pre>
<%
		}
	}
%>
<p>Return to <a href="results.jsp">results page</a></p>
</body>
</html>
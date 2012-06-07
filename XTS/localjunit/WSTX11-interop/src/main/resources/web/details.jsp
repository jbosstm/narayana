<!--
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 -->
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.jboss.transaction.txinterop.test.TestConstants"%>
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
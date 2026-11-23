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
<%@page import="junit.framework.TestCase"%>
<%@page import="junit.framework.TestFailure"%>
<%@page import="junit.framework.TestResult"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>JBoss Transactions WS-TX Interop results page</title>
</head>
<body>
<h1>Results</h1>
<h2>Processed results</h2>
<%
	final String logName = (String)session.getAttribute(TestConstants.ATTRIBUTE_LOG_NAME) ;
    if (logName != null)
    {
%>
<p>View <a href="logs/<%= logName %>">log file</a></p>
<%
    }
%>
<h2>JUnit results</h2>
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
		final int runCount = testResult.runCount() ;
		final int errorCount = testResult.errorCount() ;
		final int failureCount = testResult.failureCount() ;
%>
<p>Run count: <%= runCount %></p>
<p>Error count: <%= errorCount %></p>
<p>Failure count: <%= failureCount %></p>
<%
		if ((errorCount > 0) || (failureCount > 0))
		{
			if (errorCount > 0)
			{
				final Enumeration enumeration = testResult.errors() ;
%>
<H3>Errors</H3>
<%
				int count = 0 ;
				while(enumeration.hasMoreElements())
				{
				    final TestFailure testFailure = (TestFailure)enumeration.nextElement() ;
				    count++ ;
				    final TestCase failedTest = (TestCase)testFailure.failedTest() ;
				    final String name = failedTest.getName() ;
				    final String description = (String)TestConstants.DESCRIPTIONS.get(name) ;
%>
<p><a href="details.jsp?type=error&index=<%= count %>"><%= name %></a> <%= description %></p>
<%
				}
			}
			if (failureCount > 0)
			{
				final Enumeration enumeration = testResult.failures() ;
%>
<H3>Failures</H3>
<%
				int count = 0 ;
				while(enumeration.hasMoreElements())
				{
				    final TestFailure testFailure = (TestFailure)enumeration.nextElement() ;
				    count++ ;
				    final TestCase failedTest = (TestCase)testFailure.failedTest() ;
				    final String name = failedTest.getName() ;
				    final String description = (String)TestConstants.DESCRIPTIONS.get(name) ;
%>
<p><a href="details.jsp?type=failure&index=<%= count %>"><%= name %></a> <%= description %></p>
<%
				}
			}
		}
	}
%>
<p>Return to <a href="index.jsp">main page</a></p>
</body>
</html>
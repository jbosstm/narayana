/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 */
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestRunnerServlet.java
 */

package com.arjuna.qa.junit;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TestRunnerServlet extends HttpServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        _testSuiteClassName = config.getInitParameter("TestSuiteClassName");
    }

   protected String getContentType()
   {
       return "text/html";
   }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try
        {
            PrintWriter writer = response.getWriter();

            response.setContentType(getContentType());
            response.setHeader("Cache-Control", "no-cache");

            if (request.getParameter("failednumber") != null)
                doStatusFailed(writer, request, response);
            else if (request.getParameter("errornumber") != null)
                doStatusError(writer, request, response);
            else
                doStatus(writer, request, response);
        }
        catch (Exception exception)
        {
            log("Test Runner: doGet failed", exception);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.toString());
        }
        catch (Error error)
        {
            log("Test Runner: doGet failed", error);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.toString());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try
        {
            PrintWriter writer = response.getWriter();

            response.setContentType(getContentType());
            response.setHeader("Cache-Control", "no-cache");

	    if ((_runnerThread == null) || (! _runnerThread.isAlive()))
	    {
                _runnerThread = new RunnerThread();
                _runnerThread.start();
            }

            if (request.getParameter("failednumber") != null)
                doStatusFailed(writer, request, response);
            else if (request.getParameter("errornumber") != null)
                doStatusError(writer, request, response);
            else
                doStatus(writer, request, response);
        }
        catch (Exception exception)
        {
            log("Test Runner: doPost failed", exception);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.toString());
        }
        catch (Error error)
        {
            log("Test Runner: doPost failed", error);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.toString());
        }
    }

    public void doStatus(PrintWriter writer, HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        writer.println("<HTML>");
        writer.println("<HEAD>");
        writer.println("<TITLE>Test Runner</TITLE>");
        writer.println("</HEAD>");
        writer.println("<BODY bgcolor=\"white\" style=\"font-family: Arial, Helvetica, sans-serif\">");
        writer.println("<DIV style=\"font-family: Arial, Helvetica, sans-serif; font-size: large\">&nbsp;<BR>Test Runner: Status<BR>&nbsp;</DIV>");

        writer.println("<TABLE width=\"100%\">");

        writer.println("<TR><TD colspan=\"3\" align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Status</TD></TR>");
        writer.println("<TR>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test suite class:</TD>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + _testSuiteClassName + "</TD>");
        writer.println("</TR>");

        if ((_runnerThread == null) || (! _runnerThread.isAlive()))
        {
            writer.println("<TR>");
            writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Action:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.print("<FORM method=\"POST\" action=\"" + request.getRequestURL() + "\">");
            writer.print("<INPUT type=\"button\" value=\"run\" onclick=\"this.form.submit()\">");
            writer.print("</FORM>");
            writer.println("</TD>");
            writer.println("</TR>");
        }
        else
        {
            writer.println("<TR>");
            writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Action:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.print("<FORM method=\"GET\" action=\"" + request.getRequestURL() + "\">");
            writer.print("<INPUT type=\"button\" value=\"reload\" onclick=\"this.form.submit()\">");
            writer.print("</FORM>");
            writer.println("</TD>");
            writer.println("</TR>");

            if (_currentTest != null)
	    {
                writer.println("<TR>");
                writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Current test:</TD>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                encode(writer, _currentTest.toString());
                writer.println("</TD>");
                writer.println("</TR>");
            }
        }
        writer.println("</TR>");
        writer.println("<TR>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Passed count:</TD>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + _passedTests.size() + "</TD>");
        writer.println("</TR>");
        writer.println("<TR>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Failed count:</TD>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + _failedTests.size() + "</TD>");
        writer.println("</TR>");
        writer.println("<TR>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Error count:</TD>");
        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + _errorTests.size() + "</TD>");
        writer.println("</TR>");

        writer.println("</TABLE>");

        if (! _passedTests.isEmpty())
        {
            writer.println("<BR>");

            writer.println("<TABLE width=\"100%\">");

            writer.println("<TR><TD colspan=\"3\" align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Passes</TD></TR>");

            writer.println("<TR>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test</TD>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Duration</TD>");
            writer.println("</TR>");

            Iterator passedTestsIterator = _passedTests.iterator();
            while (passedTestsIterator.hasNext())
            {
                PassedTest passedTest = (PassedTest) passedTestsIterator.next();
                writer.println("<TR>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                encode(writer, passedTest.test.toString());
                writer.println("</TD>");
                writer.print("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif\">" + passedTest.duration + " ms</TD>");
                writer.println("</TR>");
            }

            writer.println("</TABLE>");
        }

        if (! _failedTests.isEmpty())
        {
            writer.println("<BR>");

            writer.println("<TABLE width=\"100%\">");

            writer.println("<TR><TD colspan=\"3\" align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Failures</TD></TR>");
            writer.println("<TR>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test</TD>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Duration</TD>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Message</TD>");
            writer.println("</TR>");

            int failedNumber = 0;
            Iterator failedTestsIterator = _failedTests.iterator();
            while (failedTestsIterator.hasNext())
            {
                FailedTest failedTest = (FailedTest) failedTestsIterator.next();
                writer.println("<TR>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                writer.print("<A href=\"" + request.getRequestURL() + "?failednumber=" + failedNumber + "\">");
                encode(writer, failedTest.test.toString());
                writer.print("</A>");
                writer.println("</TD>");
                writer.print("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif\">" + failedTest.duration + " ms</TD>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                encode(writer, failedTest.assertionFailedError.getMessage());
                writer.println("</TD>");
                writer.println("</TR>");
                failedNumber++;
            }

            writer.println("</TABLE>");

            writer.println("<BR>");
        }

        if (! _errorTests.isEmpty())
        {
            writer.println("<TABLE width=\"100%\">");
            writer.println("<TR>");
            writer.println("<TR><TD colspan=\"3\" align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Errors</TD></TR>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test</TD>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Duration</TD>");
            writer.println("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Exception/Error</TD>");
            writer.println("</TR>");

            int errorNumber = 0;
            Iterator errorTestsIterator = _errorTests.iterator();
            while (errorTestsIterator.hasNext())
            {
                ErrorTest errorTest = (ErrorTest) errorTestsIterator.next();
                writer.println("<TR>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                writer.print("<A href=\"" + request.getRequestURL() + "?errornumber=" + errorNumber + "\">");
                encode(writer, errorTest.test.toString());
                writer.print("</A>");
                writer.println("</TD>");
                writer.print("<TD align=\"center\" style=\"font-family: Arial, Helvetica, sans-serif\">" + errorTest.duration + " ms</TD>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                encode(writer, errorTest.throwable.toString());
                writer.println("</TD>");
                writer.println("</TR>");
                errorNumber++;
            }

            writer.println("</TABLE>");
        }

        writer.println("</BODY>");
        writer.println("</HTML>");
    }

    public void doStatusFailed(PrintWriter writer, HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        int        failedIndex = 0;
        FailedTest failedTest  = null;

        try
        {
            String failedIndexString = (String) request.getParameter("failednumber");

            failedIndex = Integer.parseInt(failedIndexString);
            failedTest  = (FailedTest) _failedTests.get(failedIndex);
        }
        catch (Exception exception)
        {
            failedTest = null;
        }

        if (failedTest != null)
        {
            writer.println("<HTML>");
            writer.println("<HEAD>");
            writer.println("<TITLE>Test Runner</TITLE>");
            writer.println("</HEAD>");
            writer.println("<BODY bgcolor=\"white\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<DIV style=\"font-family: Arial, Helvetica, sans-serif; font-size: large\">&nbsp;<BR>Test Runner: Failed Status<BR>&nbsp;</DIV>");

            writer.println("<TABLE>");

            writer.print("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            encode(writer, failedTest.test.toString());
            writer.println("</TD>");
            writer.println("</TR>");

            writer.println("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Duration:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + failedTest.duration + " ms</TD>");
            writer.println("</TR>");

            writer.println("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Message:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            encode(writer, failedTest.assertionFailedError.getMessage());
            writer.println("</TD>");
            writer.println("</TR>");

            writer.print("<TR>");
            writer.print("<TD colspan=\"2\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Stack trace:</TD>");
            writer.println("</TR>");
            writer.println("<TR>");
            writer.println("<TD colspan=\"2\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<PRE>");
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            PrintWriter     printWriter     = new PrintWriter(charArrayWriter, true);
            failedTest.assertionFailedError.printStackTrace(printWriter);
            printWriter.close();
            charArrayWriter.close();
            encode(writer, charArrayWriter.toString());
            writer.println("</PRE>");
            writer.println("</TD>");
            writer.println("</TR>");

            writer.println("</TABLE>");

            writer.println("<TABLE width=\"100%\">");
            writer.println("<TR>");
            writer.println("<TD align=\"left\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            if (failedIndex > 0)
                writer.println("<A href=\"" + request.getRequestURL() + "?failednumber=" + (failedIndex - 1)+ "\">previous</A>");
            else
                writer.print("&nbsp;");
            writer.println("</TD>");
            writer.println("<TD align=\"center\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<A href=\"" + request.getRequestURL() + "\">all</A>");
            writer.println("</TD>");
            writer.println("<TD align=\"right\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            if (failedIndex < (_failedTests.size() - 1))
                writer.println("<A href=\"" + request.getRequestURL() + "?failednumber=" + (failedIndex + 1)+ "\">next</A>");
            else
                writer.print("&nbsp;");
            writer.println("</TD>");
            writer.println("</TR>");
            writer.println("</TABLE>");

            writer.println("</BODY>");
            writer.println("</HTML>");
        }
        else
            doStatus(writer, request, response);
    }

    public void doStatusError(PrintWriter writer, HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        int       errorIndex = 0;
        ErrorTest errorTest  = null;

        try
        {
            String errorIndexString = (String) request.getParameter("errornumber");

            errorIndex = Integer.parseInt(errorIndexString);
            errorTest  = (ErrorTest) _errorTests.get(errorIndex);
        }
        catch (Exception exception)
        {
            errorTest = null;
        }

        if (errorTest != null)
        {
            writer.println("<HTML>");
            writer.println("<HEAD>");
            writer.println("<TITLE>Test Runner</TITLE>");
            writer.println("</HEAD>");
            writer.println("<BODY bgcolor=\"white\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<DIV style=\"font-family: Arial, Helvetica, sans-serif; font-size: large\">&nbsp;<BR>Test Runner: Error Status<BR>&nbsp;</DIV>");

            writer.println("<TABLE>");

            writer.print("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Test:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            encode(writer, errorTest.test.toString());
            writer.println("</TD>");
            writer.println("</TR>");

            writer.println("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Duration:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">" + errorTest.duration + " ms</TD>");
            writer.println("</TR>");

            writer.println("<TR>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Exception/error:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            encode(writer, errorTest.throwable.toString());
            writer.println("</TD>");
            writer.println("</TR>");

            writer.print("<TR>");
            writer.print("<TD colspan=\"2\" style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Stack trace:</TD>");
            writer.println("</TR>");
            writer.println("<TR>");
            writer.println("<TD colspan=\"2\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<PRE>");
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            PrintWriter     printWriter     = new PrintWriter(charArrayWriter, true);
            errorTest.throwable.printStackTrace(printWriter);
            printWriter.close();
            charArrayWriter.close();
            encode(writer, charArrayWriter.toString());
            writer.println("</PRE>");
            writer.println("</TD>");
            writer.println("</TR>");

            writer.println("</TABLE>");

            writer.println("<TABLE width=\"100%\">");
            writer.println("<TR>");
            writer.println("<TD align=\"left\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            if (errorIndex > 0)
                writer.println("<A href=\"" + request.getRequestURL() + "?errornumber=" + (errorIndex - 1)+ "\">previous</A>");
            else
                writer.print("&nbsp;");
            writer.println("</TD>");
            writer.println("<TD align=\"center\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.println("<A href=\"" + request.getRequestURL() + "\">all</A>");
            writer.println("</TD>");
            writer.println("<TD align=\"right\" width=\"33%\" style=\"font-family: Arial, Helvetica, sans-serif\">");
            if (errorIndex < (_errorTests.size() - 1))
                writer.println("<A href=\"" + request.getRequestURL() + "?errornumber=" + (errorIndex + 1)+ "\">next</A>");
            else
                writer.print("&nbsp;");
            writer.println("</TD>");
            writer.println("</TR>");
            writer.println("</TABLE>");

            writer.println("</BODY>");
            writer.println("</HTML>");
        }
        else
            doStatus(writer, request, response);
    }

    protected class PassedTest
    {
        public Test test;
        public long duration;
    }

    protected class FailedTest
    {
        public Test                 test;
        public long                 duration;
        public AssertionFailedError assertionFailedError;
    }

    protected class ErrorTest
    {
        public Test      test;
        public long      duration;
	public Throwable throwable;
    }

    protected class RunnerThread extends Thread
    {
        public void run()
        {
            try
            {
                _passedTests.clear();
                _failedTests.clear();
                _errorTests.clear();

                Class        testSuiteClass = Class.forName(_testSuiteClassName);
                TestListener testListener   = new BasicTestListener();

                _testResult = new TestResult();
                _testSuite  = (TestSuite) testSuiteClass.newInstance();

                _testResult.addListener(testListener);
                _testSuite.run(_testResult);
                _testResult.removeListener(testListener);
            }
            catch (Exception exception)
            {
                log("Runner Thread: run failed", exception);
            }
            catch (Error error)
            {
                log("Runner Thread: run failed", error);
            }
        }
    }

    protected class BasicTestListener implements TestListener
    {
        public void startTest(Test test)
        {
            _startTime            = System.currentTimeMillis();
            _failed               = false;
            _error                = false;
            _assertionFailedError = null;
            _throwable            = null;
            _currentTest          = test;
        }

        public void addError(Test test, Throwable throwable)
        {
            _error     = true;
            _throwable = throwable;
            throwable.printStackTrace(System.out);
        }

        public void addFailure(Test test, AssertionFailedError assertionFailedError)
        {
            _failed               = true;
            _assertionFailedError = assertionFailedError;
            assertionFailedError.printStackTrace(System.out);
        }

        public void endTest(Test test)
        {
            if (_failed)
            {
                FailedTest failedTest           = new FailedTest();
                failedTest.test                 = test;
                failedTest.duration             = System.currentTimeMillis() - _startTime;
                failedTest.assertionFailedError = _assertionFailedError;
                _failedTests.add(failedTest);
	    }
	    else if (_error)
            {
                ErrorTest errorTest = new ErrorTest();
                errorTest.test      = test;
                errorTest.duration  = System.currentTimeMillis() - _startTime;
                errorTest.throwable = _throwable;
                _errorTests.add(errorTest);
	    }
	    else
            {
                PassedTest passedTest = new PassedTest();
                passedTest.test       = test;
                passedTest.duration   = System.currentTimeMillis() - _startTime;
                _passedTests.add(passedTest);
	    }

            _currentTest = null;
        }

        private long                 _startTime            = 0;
        private boolean              _failed               = false;
        private boolean              _error                = false;
        private AssertionFailedError _assertionFailedError = null;
        private Throwable            _throwable            = null;
    }

    private static void encode(PrintWriter writer, String string)
    {
        if (string != null)
        {
            char[] chars = string.toCharArray();

            for (int index = 0; index < chars.length; index++)
                if (chars[index] == '<')
                    writer.print("&lt;");
                else if (chars[index] == '>')
                    writer.print("&gt;");
                else if (chars[index] == '&')
                    writer.print("&amp;");
                else
                    writer.print(chars[index]);
        }
        else
            writer.print("null");
    }

    protected List         _passedTests        = new LinkedList();
    protected List         _failedTests        = new LinkedList();
    protected List         _errorTests         = new LinkedList();
    protected Test         _currentTest        = null;
    protected String       _testSuiteClassName = null;
    protected RunnerThread _runnerThread       = null;
    protected TestResult   _testResult         = null;
    private TestSuite    _testSuite          = null;

}

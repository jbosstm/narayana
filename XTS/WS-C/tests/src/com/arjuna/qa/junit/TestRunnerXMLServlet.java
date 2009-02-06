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
package com.arjuna.qa.junit;

import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;

/**
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision$
 */
public class TestRunnerXMLServlet extends TestRunnerServlet
{

    @Override
    protected String getContentType()
    {
        return "text/xml";
    }

    @Override
    public void doStatus(PrintWriter writer, HttpServletRequest request, HttpServletResponse response)
            throws ServletException
    {

        DOMDocument report = new DOMDocument();
        DOMElement testsuite = new DOMElement("testsuite");
        report.setRootElement(testsuite);
        testsuite.addAttribute("name", _testSuiteClassName);
        testsuite.addAttribute("errors", Integer.toString(_errorTests.size()));
        testsuite.addAttribute("failures", Integer.toString(_failedTests.size()));
        testsuite.addAttribute("hostname", request.getServerName());
        testsuite.addAttribute("tests", Integer.toString((_testResult != null) ? _testResult.runCount() : 0));
        testsuite.addAttribute("timestamp", new Date().toString());

        DOMElement properties = new DOMElement("properties");
        testsuite.add(properties);
        DOMElement status = newPropertyDOMElement("status");
        properties.add(status);
        if ((_runnerThread == null) || (! _runnerThread.isAlive()))
        {
            if (_passedTests.isEmpty() && _failedTests.isEmpty() && _errorTests.isEmpty())
            {
                status.addAttribute("value", "stopped");
            }
            else
            {
                status.addAttribute("value", "finished");
            }
        }
        else if (_currentTest != null)
        {
            status.addAttribute("value", "running");
            properties.add(newPropertyDOMElement("current-test", _currentTest.toString()));
        }

        long totalDuration = 0;

        if (! _passedTests.isEmpty())
        {
            Iterator passedTestsIterator = _passedTests.iterator();
            while (passedTestsIterator.hasNext())
            {
                PassedTest passedTest = (PassedTest) passedTestsIterator.next();
                totalDuration += passedTest.duration;

                testsuite.add(newTestcase(
                        passedTest.test.getClass().getName(), passedTest.test.toString(), passedTest.duration));
            }
        }

        if (! _failedTests.isEmpty())
        {
            Iterator failedTestsIterator = _failedTests.iterator();
            while (failedTestsIterator.hasNext())
            {
                FailedTest failedTest = (FailedTest) failedTestsIterator.next();
                totalDuration += failedTest.duration;

                CharArrayWriter charArrayWriter = new CharArrayWriter();
                PrintWriter     printWriter     = new PrintWriter(charArrayWriter, true);
                failedTest.assertionFailedError.printStackTrace(printWriter);
                printWriter.close();
                charArrayWriter.close();

                testsuite.add(newFailedTestcase(
                        failedTest.test.getClass().getName(), failedTest.test.toString(), failedTest.duration,
                        failedTest.assertionFailedError.getMessage(), charArrayWriter.toString()));
            }
        }

        if (! _errorTests.isEmpty())
        {
            Iterator errorTestsIterator = _errorTests.iterator();
            while (errorTestsIterator.hasNext())
            {
                ErrorTest errorTest = (ErrorTest) errorTestsIterator.next();
                totalDuration += errorTest.duration;

                CharArrayWriter charArrayWriter = new CharArrayWriter();
                PrintWriter     printWriter     = new PrintWriter(charArrayWriter, true);
                errorTest.throwable.printStackTrace(printWriter);
                printWriter.close();
                charArrayWriter.close();

                System.out.println("charArrayWriter.toString()=" + charArrayWriter.toString());
                testsuite.add(newErrorTestcase(
                        errorTest.test.getClass().getName(), errorTest.test.toString(), errorTest.duration,
                        errorTest.throwable.getMessage(), charArrayWriter.toString()));
            }
        }
        testsuite.add(new DOMElement("system-out").addCDATA(""));
        testsuite.add(new DOMElement("system-err").addCDATA(""));
        // total time of all tests
        testsuite.addAttribute("time", Float.toString(totalDuration / 1000f));

        XMLWriter outputter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        try {
            outputter.write(testsuite);
            outputter.close();
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private DOMElement newPropertyDOMElement(String name)
    {
        return newPropertyDOMElement(name, null);
    }

    private DOMElement newPropertyDOMElement(String name, String value)
    {
        DOMElement property = new DOMElement("property");
        property.addAttribute("name", name);
        if (value != null)
        {
            property.addAttribute("value", value);
        }
        return property;
    }

    private DOMElement newTestcase(String classname, String name, long duration)
    {
        return newTestcase(classname, name, duration, null, null, null);
    }

    private DOMElement newFailedTestcase(String classname, String name, long duration, String failureMessage, String failureDetail)
    {
        return newTestcase(classname, name, duration, "junit.framework.AssertionFailedError", failureMessage, failureDetail);
    }

    private DOMElement newErrorTestcase(String classname, String name, long duration, String failureMessage, String failureDetail)
    {
        return newTestcase(classname, name, duration, "junit.framework.throwable", failureMessage, failureDetail);
    }

    private DOMElement newTestcase(String classname, String name, long duration, String failureMessage, String failureType, String failureDetail)
    {
        DOMElement testcase = new DOMElement("testcase");
        testcase.addAttribute("classname", classname);
        testcase.addAttribute("name", name);
        testcase.addAttribute("time", Float.toString(duration / 1000f));    // converts from miliseconds to seconds
        if (failureMessage != null)
        {
            DOMElement failure = new DOMElement("failure");
            testcase.add(failure);
            failure.addAttribute("message", failureMessage);
            if (failureType != null)
            {
                failure.addAttribute("type", failureType);
            }
            if (failureDetail != null)
            {
                failure.addCDATA(failureDetail);
            }
        }
        return testcase;
    }

}

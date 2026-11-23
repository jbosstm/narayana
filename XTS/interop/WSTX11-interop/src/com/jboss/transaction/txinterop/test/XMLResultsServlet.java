/*
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
 */
package com.jboss.transaction.txinterop.test;

import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Formats test results to the JUnit XML format.
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @version $Revision$
 */
public class XMLResultsServlet extends HttpServlet
{
   public void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
      doStatus(request, response);
   }

   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
      doStatus(request, response);
   }

   public void doStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      response.setContentType("text/xml");
      response.setHeader("Cache-Control", "no-cache");

      HttpSession session = request.getSession();
      final FullTestResult testResult = (FullTestResult) session.getAttribute(TestConstants.ATTRIBUTE_TEST_RESULT);

      DOMDocument report = new DOMDocument();
      DOMElement testsuite = new DOMElement("testsuite");
      report.setRootElement(testsuite);

      if (testResult == null)
      {
         // No JUnit test results generated.
      }
      else
      {
         List passedTests = testResult.getPassedTests();
         List failedTests = testResult.getFailedTests();
         List errorTests  = testResult.getErrorTests();

         final int runCount = testResult.runCount() ;
         final int errorCount = testResult.errorCount() ;
         final int failureCount = testResult.failureCount() ;

         testsuite.addAttribute("name", "com.jboss.transaction.txinterop.interop.InteropTestSuite");
         testsuite.addAttribute("errors", Integer.toString(errorCount));
         testsuite.addAttribute("failures", Integer.toString(failureCount));
         testsuite.addAttribute("hostname", request.getServerName());
         testsuite.addAttribute("tests", Integer.toString(runCount));
         testsuite.addAttribute("timestamp", new Date().toString());

         DOMElement properties = new DOMElement("properties");
         testsuite.add(properties);
         DOMElement status = newPropertyDOMElement("status");
         properties.add(status);
         status.addAttribute("value", "finished");

         long totalDuration = 0;

         if (! passedTests.isEmpty())
         {
             Iterator passedTestsIterator = passedTests.iterator();
             while (passedTestsIterator.hasNext())
             {
                 FullTestResult.PassedTest passedTest = (FullTestResult.PassedTest) passedTestsIterator.next();
                 totalDuration += passedTest.duration;

                 final String name = passedTest.test.toString();
                 final String description = (String)TestConstants.DESCRIPTIONS.get(name) ;

                 testsuite.add(newTestcase(
                         passedTest.test.getClass().getName(), name + ": " + description, passedTest.duration));
             }
         }

         if (! failedTests.isEmpty())
         {
             Iterator failedTestsIterator = failedTests.iterator();
             while (failedTestsIterator.hasNext())
             {
                 FullTestResult.FailedTest failedTest = (FullTestResult.FailedTest) failedTestsIterator.next();
                 totalDuration += failedTest.duration;

                 final String name = failedTest.test.toString();
                 final String description = (String)TestConstants.DESCRIPTIONS.get(name) ;
                 CharArrayWriter charArrayWriter = new CharArrayWriter();
                 PrintWriter printWriter     = new PrintWriter(charArrayWriter, true);
                 failedTest.assertionFailedError.printStackTrace(printWriter);
                 printWriter.close();
                 charArrayWriter.close();

                 testsuite.add(newFailedTestcase(
                         failedTest.test.getClass().getName(), name + ": " + description, failedTest.duration,
                         failedTest.assertionFailedError.getMessage(), charArrayWriter.toString()));
             }
         }

         if (! errorTests.isEmpty())
         {
             Iterator errorTestsIterator = errorTests.iterator();
             while (errorTestsIterator.hasNext())
             {
                 FullTestResult.ErrorTest errorTest = (FullTestResult.ErrorTest) errorTestsIterator.next();
                 totalDuration += errorTest.duration;

                 final String name = errorTest.test.toString();
                 final String description = (String)TestConstants.DESCRIPTIONS.get(name) ;
                 CharArrayWriter charArrayWriter = new CharArrayWriter();
                 PrintWriter     printWriter     = new PrintWriter(charArrayWriter, true);
                 errorTest.throwable.printStackTrace(printWriter);
                 printWriter.close();
                 charArrayWriter.close();

                 System.out.println("charArrayWriter.toString()=" + charArrayWriter.toString());
                 testsuite.add(newErrorTestcase(
                         errorTest.test.getClass().getName(), name + ": " + description, errorTest.duration,
                         errorTest.throwable.getMessage(), charArrayWriter.toString()));
             }
         }

         // total time of all tests
         testsuite.addAttribute("time", Float.toString(totalDuration / 1000f));
      }

      String logContent = null;
      final String logName = (String)session.getAttribute(TestConstants.ATTRIBUTE_LOG_NAME) ;
      if (logName != null)
      {
         try
         {
            logContent = TestLogController.readLog(logName) ;
         }
         catch (final Throwable th)
         {
            log("Error reading log file", th) ;
         }
      }

      testsuite.add(new DOMElement("system-out").addCDATA((logContent != null) ? logContent : ""));
      testsuite.add(new DOMElement("system-err").addCDATA(""));

      XMLWriter outputter = new XMLWriter(response.getWriter(), OutputFormat.createPrettyPrint());
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

package org.jboss.jbossts.xts.servicetests.webbean;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

import org.jboss.jbossts.xts.servicetests.test.XTSServiceTest;

/**
 * a servlet which allows XTS Servcie tests to be run via a web form
 *
 * this is provided for use during testing. Service tests are normally expected to be run at AS boot
 * via XTSServiceTestRunnerBean
 */
@SuppressWarnings("serial")
public class XTSHTTPServiceTestRunner extends HttpServlet
{
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
    }

    protected String getContentType()
    {
        return "text/html";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter writer = response.getWriter();

        response.setContentType(getContentType());
        response.setHeader("Cache-Control", "no-cache");

        doStatus(writer, request, response);
    }

    @SuppressWarnings("rawtypes")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        PrintWriter writer = response.getWriter();

        response.setContentType(getContentType());
        response.setHeader("Cache-Control", "no-cache");

        if ((_runnerThread == null) || (! _runnerThread.isAlive()))
        {
            _testClassName = request.getParameter("TestClassName");

            if (_testClassName != null) {
                Class testClass;
                ClassLoader cl = XTSHTTPServiceTestRunner.class.getClassLoader();

                try {
                    testClass = cl.loadClass(_testClassName);
                } catch (ClassNotFoundException cnfe) {
                    throw new ServletException("XTSHTTPServicetestRunner : cannot find test class " + _testClassName, cnfe);
                }

                try {
                    _currentTest = (XTSServiceTest)testClass.newInstance();
                } catch (InstantiationException ie) {
                    throw new ServletException("XTSHTTPServicetestRunner : cannot instantiate test class " + _testClassName, ie);
                } catch (IllegalAccessException iae) {
                    throw new ServletException("XTSHTTPServicetestRunner : cannot access constructor for test class " + _testClassName, iae);
                }

                // since we are running in the AS startup thread we need a separate thread for the test

                _runnerThread = new Thread() {
                    public void run()
                    {
                        _currentTest.run();
                    }
                };

                _runnerThread.start();
            }
        }

        doStatus(writer, request, response);
    }

    public void doStatus(PrintWriter writer, HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        writer.println("<HTML>");
        writer.println("<HEAD>");
        writer.println("<TITLE>Test Runner</TITLE>");
        writer.println("</HEAD>");
        writer.println("<BODY bgcolor=\"white\" style=\"font-family: Arial, Helvetica, sans-serif\">");
        writer.println("<DIV style=\"font-family: Arial, Helvetica, sans-serif; font-size: large\">&nbsp;<BR>Test Runner: Status<BR>&nbsp;</DIV>");

        writer.println("<TABLE width=\"100%\">");

        if ((_runnerThread == null) || (! _runnerThread.isAlive()))
        {
            writer.println("<TR>");
            writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Action:</TD>");
            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
            writer.print("<FORM method=\"POST\" action=\"" + request.getRequestURL() + "\">");
            writer.print("<INPUT type=\"button\" value=\"run\" onclick=\"this.form.submit()\">");
            writer.print(" : <INPUT type=\"text\" name=\"TestClassName\"maxlength=\"2000\" size=\"60\">");
            writer.print("</FORM>");
            writer.println("</TD>");
            writer.println("</TR>");
            if (_runnerThread != null && !_runnerThread.isAlive()) {
                if (_currentTest != null)
                {
                    writer.println("<TR>");
                    writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Current test:</TD>");
                    writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                    encode(writer, _testClassName.toString());
                    writer.println("</TD>");
                    writer.println("</TR>");
                    if (_currentTest.isSuccessful()) {
                        writer.println("<TR>");
                        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Status:</TD>");
                        writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                        writer.print("success!");
                        writer.println("</TD>");
                        writer.println("</TR>");
                    } else {
                        writer.println("<TR>");
                        writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Status:</TD>");
                        writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                        writer.print("fail!");
                        writer.println("</TD>");
                        writer.println("</TR>");
                        if (_currentTest.getException() != null) {
                            writer.println("<TR>");
                            writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Exception:</TD>");
                            writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                            writer.println("Exception:<BR/>");
                            encode(writer, _currentTest.getException().toString());
                            writer.println("</TD>");
                            writer.println("</TR>");
                        }
                    }
                }
            }
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
            if (_runnerThread != null && _currentTest != null)
            {
                writer.println("<TR>");
                writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Current test:</TD>");
                writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                encode(writer, _testClassName.toString());
                writer.println("</TD>");
                writer.println("</TR>");
                    writer.println("<TR>");
                    writer.println("<TD style=\"font-family: Arial, Helvetica, sans-serif; font-weight: bold\">Status:</TD>");
                    writer.print("<TD style=\"font-family: Arial, Helvetica, sans-serif\">");
                    writer.print("running");
                    writer.println("</TD>");
                    writer.println("</TR>");
            }
        }
        writer.println("</TABLE>");


        writer.println("</BODY>");
        writer.println("</HTML>");
    }


    protected static void encode(PrintWriter writer, String string)
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

    protected XTSServiceTest         _currentTest        = null;
    protected String _testClassName = null;
    protected Thread _runnerThread       = null;
}

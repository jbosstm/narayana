package com.arjuna.qa.junit;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.Header;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Runs XTS Interop Testsuite at a serveruURL and writes the test result into an outfile.
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @version <tt>$Revision$</tt>
 */
public class LocalJUnitInteropRunner extends TestCase
{
    private String serverUrl    = null;
    private String outfile      = null;

    protected void setUp() throws Exception
    {
        serverUrl   = System.getProperty("serverUrl");
        outfile     = System.getProperty("outfile");
        System.err.println("serverUrl=" + serverUrl);
        System.err.println("outfile=" + outfile);
    }

    public void testCallServlet()
    {
        boolean result = true;
        try
        {
            // run tests by calling a servlet
            Header runParam = new Header("Execute", "run");
            HttpMethodBase request = HttpUtils.accessURL(
                    new URL(serverUrl), null,
                    HttpURLConnection.HTTP_OK,
                    new Header[] {runParam},
                    HttpUtils.POST);

           String response = request.getResponseBodyAsString();

            System.err.println("======================================================");
            System.err.println("====================   RESULT    =====================");
            System.err.println("======================================================");
            System.err.println(response);

            // writes response to the outfile
            BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
            writer.write(response);
            writer.close();
        }
        catch (Exception e)
        {
            System.err.println("======================================================");
            System.err.println("====================  EXCEPTION  =====================");
            System.err.println("======================================================");
            e.printStackTrace();
            result = false;
        }

        assertTrue(result);
    }

}
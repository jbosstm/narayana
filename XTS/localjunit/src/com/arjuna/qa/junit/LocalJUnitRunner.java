package com.arjuna.qa.junit;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.Header;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Created by IntelliJ IDEA.
 * User: studensky
 * Date: 30.1.2009
 * Time: 14:09:23
 * To change this template use File | Settings | File Templates.
 */
public class LocalJUnitRunner extends TestCase
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
            Header runParam = new Header("run", "run");
            HttpMethodBase request = HttpUtils.accessURL(
                    new URL(serverUrl), null,
                    HttpURLConnection.HTTP_OK,
                    new Header[] {runParam},
                    HttpUtils.POST);

            String response = null;
            int index = 0;
            do
            {
                System.err.println("_____________ " +( index++) + "th round");
                // we have to give some time to the tests to finish
                Thread.sleep(10000);     // 10 secs

                // tries to get results
                request = HttpUtils.accessURL(
                        new URL(serverUrl), null,
                        HttpURLConnection.HTTP_OK,
                        HttpUtils.GET);

                response = request.getResponseBodyAsString();
            }
            while (response != null && response.indexOf("finished") == -1);

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
            e.printStackTrace();
            result = false;
        }
        
        assertTrue(result);
    }
    
}

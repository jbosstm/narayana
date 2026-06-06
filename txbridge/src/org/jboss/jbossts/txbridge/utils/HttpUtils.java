/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jbossts.txbridge.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.TraceMethod;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** Utilities for client http requests
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 82338 $
 */
public class HttpUtils
{
//   private static Logger log = Logger.getLogger(HttpUtils.class);

   public static final int GET = 1;
   public static final int POST = 2;
   public static final int HEAD = 3;
   public static final int OPTIONS = 4;
   public static final int PUT = 5;
   public static final int DELETE = 6;
   public static final int TRACE = 7;

   /** Perform a get on the indicated URL and assert an HTTP_OK response code
    *
    * @param url
    * @return The commons HttpClient used to perform the get
    * @throws Exception on any failure
    */
   public static HttpMethodBase accessURL(URL url) throws Exception
   {
      return accessURL(url, null, HttpURLConnection.HTTP_OK);
   }
   /** Perform a get on the indicated URL and assert that the response code
    * matches the expectedHttpCode argument.
    *
    * @param url
    * @param expectedHttpCode the http response code expected
    * @return The commons HttpClient used to perform the get
    * @throws Exception on any failure
    */
   public static HttpMethodBase accessURL(URL url, String realm,
      int expectedHttpCode)
      throws Exception
   {
      return accessURL(url, realm, expectedHttpCode, null);
   }
   public static HttpMethodBase accessURL(URL url, String realm,
      int expectedHttpCode, int type)
      throws Exception
   {
      return accessURL(url, realm, expectedHttpCode, null, type);
   }
   public static HttpMethodBase accessURL(URL url, String realm,
      int expectedHttpCode, Header[] hdrs)
      throws Exception
   {
      return accessURL(url, realm, expectedHttpCode, hdrs, GET);
   }
   public static HttpMethodBase accessURL(URL url, String realm,
      int expectedHttpCode, Header[] hdrs, int type)
      throws Exception
   {
      HttpClient httpConn = new HttpClient();
      HttpMethodBase request = createMethod(url, type);

      int hdrCount = hdrs != null ? hdrs.length : 0;
      for(int n = 0; n < hdrCount; n ++)
         request.addRequestHeader(hdrs[n]);
      try
      {
         System.err.println("Connecting to: "+url);
         String userInfo = url.getUserInfo();

         if( userInfo != null )
         {
            UsernamePasswordCredentials auth = new UsernamePasswordCredentials(userInfo);
            httpConn.getState().setCredentials(realm, url.getHost(), auth);
         }
         System.err.println("RequestURI: "+request.getURI());
         int responseCode = httpConn.executeMethod(request);
         String response = request.getStatusText();
         System.err.println("responseCode="+responseCode+", response="+response);
         String content = request.getResponseBodyAsString();
         System.err.println(content);
         // Validate that we are seeing the requested response code
         if( responseCode != expectedHttpCode )
         {
            throw new IOException("Expected reply code:"+expectedHttpCode
               +", actual="+responseCode);
         }
      }
      catch(IOException e)
      {
         throw e;
      }
      return request;
   }

   public static HttpMethodBase createMethod(URL url, int type)
   {
      HttpMethodBase request = null;
      switch( type )
      {
         case GET:
            request = new GetMethod(url.toString());
            break;
         case POST:
            request = new PostMethod(url.toString());
            break;
         case HEAD:
            request = new HeadMethod(url.toString());
            break;
         case OPTIONS:
            request = new OptionsMethod(url.toString());
            break;
         case PUT:
            request = new PutMethod(url.toString());
            break;
         case DELETE:
            request = new DeleteMethod(url.toString());
            break;
         case TRACE:
            request = new TraceMethod(url.toString());
            break;
      }
      return request;
   }
}

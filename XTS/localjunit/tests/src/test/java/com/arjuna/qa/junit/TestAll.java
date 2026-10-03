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
package com.arjuna.qa.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.Header;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestAll {
	private String baseUrl = "http://localhost:8080";
    private String reportDir = "target/surefire-reports";

	private int timeout = 10 * 1000;
    private final static int LOOP_RETRY_MAX = 60;

	private final static String wsastestsWar = "../../WSAS/tests/target/wsas-tests.war";
	private final static String wsc11testsWar = "../../WS-C/tests/target/ws-c11-tests.war";
	private final static String wst11testsWar = "../../WS-T/tests/target/ws-t11-tests.war";
	private final static String wscf11testsWar = "../../WSCF/tests/target/wscf11-tests.war";
	private final static String wstx11testsWar = "../../WSTX/tests/target/wstx11-tests.war";

	@Deployment(name = "wsas-tests", testable = false)
	public static Archive<?> createWSASTestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(wsastestsWar));
		return archive;	
	}

	@Deployment(name = "ws-c11-tests", testable = false)
	public static Archive<?> createWSC11TestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(wsc11testsWar));
		return archive;	
	}

	@Deployment(name = "ws-t11-tests", testable = false)
	public static Archive<?> createWST11TestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(wst11testsWar));
		return archive;	
	}

	@Deployment(name = "wscf11-tests", testable = false)
	public static Archive<?> createWSCF11TestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(wscf11testsWar));
		return archive;	
	}

	@Deployment(name = "wstx11-tests", testable = false)
	public static Archive<?> createWSTX11TestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(wstx11testsWar));
		return archive;	
	}

	@Test @OperateOnDeployment("wsas-tests")
	public void test_wsas() {
		testCallServlet(baseUrl + "/wsas-tests/index.xml", reportDir + "/Test-wsas-tests.xml");
	}

	@Test @OperateOnDeployment("ws-c11-tests")
	public void test_wsc11() {
		testCallServlet(baseUrl + "/ws-c11-tests/index.xml", reportDir + "/Test-ws-c11-tests.xml");
	}

	@Test @OperateOnDeployment("ws-t11-tests")
	public void test_wst11() {
		testCallServlet(baseUrl + "/ws-t11-tests/index.xml", reportDir + "/Test-ws-t11-tests.xml");
	}

	@Test @OperateOnDeployment("wscf11-tests")
	public void test_wscf11() {
		testCallServlet(baseUrl + "/wscf11-tests/index.xml", reportDir + "/Test-wscf11-tests.xml");
	}

	@Test @OperateOnDeployment("wstx11-tests")
	public void test_wstx11() {
		testCallServlet(baseUrl + "/wstx11-tests/index.xml", reportDir + "/Test-wstx11-tests.xml");
	}

	private void testCallServlet(String serverUrl, String outfile) {
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
				Thread.sleep(timeout);

				// tries to get results
				request = HttpUtils.accessURL(
						new URL(serverUrl), null,
						HttpURLConnection.HTTP_OK,
						HttpUtils.GET);

				response = request.getResponseBodyAsString();
			}
			while (response != null && response.indexOf("finished") == -1 && index < LOOP_RETRY_MAX);

			if (response != null && response.indexOf("finished") == -1)
			{
				System.err.println("======================================================");
				System.err.println("====================  TIMED OUT  =====================");
				System.err.println("======================================================");
				result = false;
			} else {
				System.err.println("======================================================");
				System.err.println("====================   RESULT    =====================");
				System.err.println("======================================================");
				System.err.println(response);
				// writes response to the outfile
				BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
				writer.write(response);
				writer.close();
			}
		}
		catch (Exception e) {
			System.err.println("======================================================");
			System.err.println("====================  EXCEPTION  =====================");
			System.err.println("======================================================");
			e.printStackTrace();
			result = false;
		}
		assertTrue(result);
	}
}

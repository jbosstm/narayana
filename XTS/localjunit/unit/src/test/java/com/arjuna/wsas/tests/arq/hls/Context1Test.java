/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsas.tests.arq.hls;

import static org.junit.Assert.fail;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.ActivityManagerFactory;
import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.DemoSOAPContextImple;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class Context1Test {
    
    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoHLS.class,
                DemoSOAPContextImple.class,
                WSASTestUtils.class);
    }

    @Test
    public void testContext1()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
        HLS[] currentHLS = ActivityManagerFactory.activityManager().allHighLevelServices();

        for (HLS hls : currentHLS) {
            ActivityManagerFactory.activityManager().removeHLS(hls);
        }
        try
        {
            ActivityManagerFactory.activityManager().addHLS(demoHLS);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            org.w3c.dom.Element root = doc.createElement("Context1-test");
            doc.appendChild(root);
            String coordinationType = demoHLS.identity();

            ua.start(coordinationType);

            System.out.println("Started: "+ua.activityName());

            ua.start(coordinationType);

            System.out.println("Started: "+ua.activityName()+"\n");

            ContextManager contextManager = new ContextManager();
            Context theContext = contextManager.context(coordinationType);

            if (theContext == null) {
                fail("Demo context not found");
            }

            if (!(theContext instanceof DemoSOAPContextImple)) {
                fail("Demo context not found");
            }

            ((SOAPContext)theContext).serialiseToElement(root);

            System.out.println("Context is " + root.getTextContent());

            ua.end();

            System.out.println("\nFinished child activity.\n");

            theContext = contextManager.context(coordinationType);

            if (theContext == null) {
                fail("Demo context not found");
            }

            if (!(theContext instanceof DemoSOAPContextImple)) {
                fail("Demo context not found");
            }

            doc = docBuilder.newDocument();
            root = doc.createElement("Context1-test");
            doc.appendChild(root);


            ((SOAPContext)theContext).serialiseToElement(root);

            System.out.println("Context is " + root.getTextContent());

            ua.end();

            System.out.println("\nFinished parent activity.\n");

            theContext = contextManager.context(coordinationType);

            if (theContext != null) {
                fail("Demo context not removed");
            }
        }
        catch (Exception ex)
        {
            WSASTestUtils.cleanup(ua);
            throw ex;
        } finally {
            try {
                for (HLS hls : currentHLS) {
                    ActivityManagerFactory.activityManager().addHLS(hls);
                }
            } catch (Exception ex) {
                // ignore this
            }
            try {
                if (demoHLS != null) {
                    ActivityManagerFactory.activityManager().removeHLS(demoHLS);
                }
            } catch (Exception ex) {
                // ignore this
            }
        }
            }
}

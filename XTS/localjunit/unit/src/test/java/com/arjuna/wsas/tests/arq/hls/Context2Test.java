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
import com.arjuna.wsas.tests.FailureHLS;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class Context2Test {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoHLS.class,
                FailureHLS.class,
                DemoSOAPContextImple.class,
                WSASTestUtils.class);
    }

    @Test
    public void testContext2()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
        FailureHLS failureHLS = new FailureHLS(); // this constructor means it will not fail
        HLS[] currentHLS = ActivityManagerFactory.activityManager().allHighLevelServices();

        for (HLS hls : currentHLS) {
            ActivityManagerFactory.activityManager().removeHLS(hls);
        }
        try
        {
            ActivityManagerFactory.activityManager().addHLS(demoHLS);
            ActivityManagerFactory.activityManager().addHLS(failureHLS);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            org.w3c.dom.Element root = doc.createElement("Context2-test");
            doc.appendChild(root);
            String demoServiceType = demoHLS.identity();
            String failureServiceType = failureHLS.identity();

            ua.start(demoServiceType);

            System.out.println("Started: "+ua.activityName());

            ua.start(failureServiceType);

            System.out.println("Started: "+ua.activityName()+"\n");

            String currentServiceType = ua.serviceType();

            if (currentServiceType != failureServiceType) {
                fail("invalid service type for current activity");
            }

            ContextManager contextManager = new ContextManager();

            Context demoServiceContext;
            Context failureServiceContext;

            demoServiceContext = contextManager.context(demoServiceType);
            failureServiceContext = contextManager.context(failureServiceType);

            // we should find a context for each service

            if (failureServiceContext == null) {
                fail("Failure context not found");
            } else if (demoServiceContext != null) {
                fail("Found multiple contexts");
            }

            if (!(failureServiceContext instanceof DemoSOAPContextImple)) {
                fail("Failure context not found");
            }

            ((SOAPContext)failureServiceContext).serialiseToElement(root);

            System.out.println("Faiure Context is " + root.getTextContent());

            ua.end();

            System.out.println("\nFinished child activity.\n");

            currentServiceType = ua.serviceType();

            if (currentServiceType != demoServiceType) {
                fail("invalid service type for current activity");
            }

            demoServiceContext = contextManager.context(demoServiceType);
            failureServiceContext = contextManager.context(failureServiceType);

            // we should only find one context for the demo service

            if (demoServiceContext == null) {
                fail("Demo context not found");
            } else if (failureServiceContext != null) {
                fail("Found multiple contexts");
            }

            if (!(demoServiceContext instanceof DemoSOAPContextImple)) {
                fail("Demo context not found");
            }

            ((SOAPContext)demoServiceContext).serialiseToElement(root);

            System.out.println("Demo Context is " + root.getTextContent());

            ua.end();

            System.out.println("\nFinished parent activity.\n");
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
            try {
                if (failureHLS != null) {
                    ActivityManagerFactory.activityManager().removeHLS(failureHLS);
                }
            } catch (Exception ex) {
                // ignore this
            }
        }
            }
}


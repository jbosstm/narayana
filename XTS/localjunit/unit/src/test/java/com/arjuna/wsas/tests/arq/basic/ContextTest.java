/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsas.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.ActivityManagerFactory;
import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ContextTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testContext()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        HLS[] currentHLS = ActivityManagerFactory.activityManager().allHighLevelServices();

        for (HLS hls : currentHLS) {
            ActivityManagerFactory.activityManager().removeHLS(hls);
        }

        try
        {
            ua.start("dummy");

            System.out.println("Started: "+ua.activityName());

            ua.start("dummy");

            System.out.println("Started: "+ua.activityName());

            ContextManager manager = new ContextManager();
            com.arjuna.mw.wsas.context.Context context = manager.context("dummy");

            if (context != null) {
                fail("Context not null: "+ context);
            }
        } finally {
            try {
                for (HLS hls : currentHLS) {
                    ActivityManagerFactory.activityManager().addHLS(hls);
                }
            } catch (Exception e) {

            }
            WSASTestUtils.cleanup(ua);
        }
            }
}

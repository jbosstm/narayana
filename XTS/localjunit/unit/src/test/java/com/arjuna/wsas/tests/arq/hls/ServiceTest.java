/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsas.tests.arq.hls;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.ActivityManagerFactory;
import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ServiceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoHLS.class,
                WSASTestUtils.class);
    }

    @Test
    public void testService()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
        try
        {
            ActivityManagerFactory.activityManager().addHLS(demoHLS);
            String coordinationType = demoHLS.identity();

            ua.start(coordinationType);

            System.out.println("Started: "+ua.activityName());

            ua.start(coordinationType);

            System.out.println("Started: "+ua.activityName());

            ua.end();

            ua.end();
        }
        catch (Exception ex)
        {
            WSASTestUtils.cleanup(ua);
            throw ex;
        } finally {
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


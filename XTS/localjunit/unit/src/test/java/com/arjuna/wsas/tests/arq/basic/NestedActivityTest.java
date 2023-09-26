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

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class NestedActivityTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testNestedActivity()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();

        try
        {
            ua.start("dummy");

            System.out.println("Started: "+ua.activityName());

            ua.start("dummy");

            String nested = ua.activityName();

            System.out.println("Started: "+nested);

            System.out.println("\nEnding: "+nested);

            ua.end();

            String parent = ua.activityName();

            System.out.println("\nCurrent: "+parent);

            System.out.println("\nEnding: "+parent);

            ua.end();

            try {
                if (ua.activityName() != null) {
                    fail("activity name should be null but is " + ua.activityName());
                }
            } catch (NoActivityException ex) {
                // ok if we get here
            }

            System.out.println("\nEnded.");

        }
        catch (Exception ex)
        {
            WSASTestUtils.cleanup(ua);
            throw ex;
        }
            }
}

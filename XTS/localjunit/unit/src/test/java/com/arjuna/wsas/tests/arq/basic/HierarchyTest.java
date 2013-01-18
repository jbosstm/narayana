package com.arjuna.wsas.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class HierarchyTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testHierarchy()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();

        try
        {
            ua.start("dummy");

            System.out.println("Started: "+ua.activityName());

            ua.start("dummy");

            System.out.println("Started: "+ua.activityName());

            ActivityHierarchy ctx = ua.currentActivity();

            System.out.println("\nHierarchy: "+ctx);

            if (ctx == null) {
                fail("current activity should not be null");
            } else {
                ua.end();

                System.out.println("\nCurrent: "+ua.activityName());

                ua.end();

                try {
                    if (ua.activityName() != null) {
                        fail("activity name should be null but is " + ua.activityName());
                    }
                } catch (NoActivityException ex) {
                    // ok if we get here
                }
            }
        } catch (Exception ex) {
            WSASTestUtils.cleanup(ua);
            throw ex;
        }
            }
}

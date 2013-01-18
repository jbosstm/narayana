package com.arjuna.wsas.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.completionstatus.Failure;
import com.arjuna.mw.wsas.status.Completed;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class TimeoutTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testTimeout()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        int timeout = ua.getTimeout();
        try {
            ua.setTimeout(1);

            ua.start("dummy");

            Thread.currentThread();
            Thread.sleep(2000);

            if (!(ua.status() instanceof Completed)) {
                ua.end();
                fail("Activity status should be Completed " + ua.status());
            }
            if (!(ua.getCompletionStatus() instanceof Failure)) {
                fail("Activity completion status should be Failure " + ua.getCompletionStatus());
            }
            System.out.println("Activity status: "+ua.status());
        } finally {
            ua.setTimeout(timeout);
            WSASTestUtils.cleanup(ua);
        }
            }
}

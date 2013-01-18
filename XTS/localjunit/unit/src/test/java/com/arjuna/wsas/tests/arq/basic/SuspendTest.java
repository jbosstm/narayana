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
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendTest {
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				WSASTestUtils.class);
	}
	
	@Test
	public void testSuspend()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();

    try
    {
        ua.start("dummy");
        
        System.out.println("Started: "+ua.activityName());
        
        ActivityHierarchy ctx = ua.suspend();
        
        System.out.println("\nSuspended: "+ctx);
        
        if (ua.currentActivity() != null) {
            fail("Current activity should be null " + ua.currentActivity());
        }
    }
    catch (Exception ex)
    {
        WSASTestUtils.cleanup(ua);
        throw ex;
    }
    }
}

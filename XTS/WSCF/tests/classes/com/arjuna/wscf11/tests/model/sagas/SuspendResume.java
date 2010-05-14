package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf11.model.sagas.UserCoordinatorFactory;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 */

public class SuspendResume
{
    @Test
    public void testSuspendResume()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin();

	    System.out.println("Started: "+ua.identifier()+"\n");

	    ActivityHierarchy hier = ua.suspend();

	    System.out.println("Suspended: "+hier+"\n");

	    if (ua.currentActivity() != null)
	    {
            WSCF11TestUtils.cleanup(ua);
            fail("Hierarchy still active");
        }
	    else
	    {
            ua.resume(hier);

            System.out.println("Resumed: "+hier+"\n");

            ua.cancel();

            System.out.println("Cancelled");
	    }
	}
	catch (Exception ex)
	{
        WSCF11TestUtils.cleanup(ua);
        throw ex;
    }
    }
}
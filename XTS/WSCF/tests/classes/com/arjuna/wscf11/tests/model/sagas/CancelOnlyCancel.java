package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf11.model.sagas.UserCoordinatorFactory;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

/**
 */

public class CancelOnlyCancel
{
    @Test
    public void testCancelOnlyCancel()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin();

	    System.out.println("Started: "+ua.identifier()+"\n");

	    ua.setCancelOnly();

	    ua.cancel();
	}
	catch (CoordinatorCancelledException ex)
	{
        // why is it ok to get here?
        WSCF11TestUtils.cleanup(ua);
    }
	catch (Exception ex)
	{
        WSCF11TestUtils.cleanup(ua);
        throw ex;
	}
    }
}
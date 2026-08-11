package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf11.model.sagas.UserCoordinatorFactory;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

/**
 */

public class BeginClose
{
    @Test
    public void testBeginClose()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin("Sagas11HLS");

	    System.out.println("Started: "+ua.identifier()+"\n");

	    ua.close();
	}
	catch (Exception ex)
	{
	    WSCF11TestUtils.cleanup(ua);
        throw ex;
    }
    }
}
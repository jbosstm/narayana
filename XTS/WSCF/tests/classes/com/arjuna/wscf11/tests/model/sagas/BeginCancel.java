package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf11.model.sagas.UserCoordinatorFactory;
import org.junit.Test;

/**
 */

public class BeginCancel
{
    @Test
    public void testBeginCancel()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	    ua.begin();

	    System.out.println("Started: "+ua.identifier()+"\n");

	    ua.cancel();

        System.out.println("Cancelled");
    }
}
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wscf.tests.model.sagas;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf11.model.sagas.UserCoordinatorFactory;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class CancelOnlyCancelTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testCancelOnlyCancel()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

        try
        {
            ua.begin("Sagas11HLS");

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

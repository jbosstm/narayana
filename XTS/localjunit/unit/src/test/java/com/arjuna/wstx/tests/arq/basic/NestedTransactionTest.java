/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wstx.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wstx.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class NestedTransactionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testNestedTransaction()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        try
        {

            // nesting not supported, so each is a separate top-level tx.

            ut.begin();

            ut.begin();

            ut.commit();

            ut.commit();

            fail("expected WrongStateException");
        }
        catch (com.arjuna.wst.WrongStateException ex)
        {
            // original test was expecting UnknownTransactionException
            // we should get here;
            try {
                ut.rollback();
            } catch(Exception einner) {
            }
        }
        catch (Exception eouter)
        {
            try {
                ut.rollback();
                ut.rollback();
            } catch(Exception einner) {
            }
            throw eouter;
        }
            }
}

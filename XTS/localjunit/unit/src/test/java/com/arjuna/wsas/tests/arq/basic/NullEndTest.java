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
public class NullEndTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testNullEnd()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();
        try
        {
            ua.end();
            fail("should have thrown NoActivityException");
        }
        catch (NoActivityException ex)
        {
            // it's ok if we arrive here?
        }
        catch (Exception ex) {
            WSASTestUtils.cleanup(ua);
            throw ex;
        }
            }
}

/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wsc.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.tests.TestUtil11;
import com.arjuna.wsc.tests.WarDeployment;
import com.arjuna.wsc11.ActivationCoordinator;

@RunWith(Arquillian.class)
public class ActivationServiceTest extends BaseWSCTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testKnownCoordinationType()
            throws Exception
            {
        final String messageID = "testKnownCoordinationType" ;
        final String coordinationTypeURI = TestUtil.COORDINATION_TYPE ;

        try
        {
            final CoordinationContextType coordinationContext = ActivationCoordinator.createCoordinationContext(TestUtil11.activationCoordinatorService, messageID, coordinationTypeURI, null, null) ;

            assertNotNull(coordinationContext);
            assertNotNull(coordinationContext.getCoordinationType()) ;
            assertEquals(TestUtil.COORDINATION_TYPE, coordinationContext.getCoordinationType());
            assertNotNull(coordinationContext.getRegistrationService()) ;
        }
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th);
        }
            }

    @Test
    public void testUnknownCoordinationType()
            throws Exception
            {
        final String messageID = "testUnknownCoordinationType" ;
        final String coordinationTypeURI = TestUtil.UNKNOWN_COORDINATION_TYPE ;
        try
        {
            ActivationCoordinator.createCoordinationContext(TestUtil11.activationCoordinatorService, messageID, coordinationTypeURI, null, null) ;
            fail("Expected exception: InvalidCreateParametersException");
        }
        catch (final InvalidCreateParametersException icpe) {} // Ignore, expected
        catch (final Throwable th)
        {
            fail("Expected exception: InvalidCreateParametersException");
        }
            }
}
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wsc.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
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
    @Ignore //JBTM-1637
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

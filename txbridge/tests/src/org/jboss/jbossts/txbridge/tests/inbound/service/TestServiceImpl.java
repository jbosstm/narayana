/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.inbound.service;

import org.apache.log4j.Logger;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;


/**
 * Implementation of a web service used by txbridge test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
@Stateless
@Remote(TestService.class)
@WebService()
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "jaxws-handlers-server.xml") // relative path from the class file
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class TestServiceImpl implements TestService
{
    private static Logger log = Logger.getLogger(TestServiceImpl.class);

    private boolean arrangeBeforeCompletionFailure = false;
    private int xaErrorCode = 0;

    @Override
    @WebMethod
    public void doTestResourceEnlistment()
    {
        log.trace("doTestResourceEnlistment()");

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        try
        {
            TestSynchronization testSynchronization = new TestSynchronization();
            if(arrangeBeforeCompletionFailure) {
                testSynchronization.setFailInBeforeCompletion(true);
            }
            tm.getTransaction().registerSynchronization(testSynchronization);

            TestXAResource testXAResource = new TestXAResource();
            if(xaErrorCode != 0) {
                testXAResource.setPrepareException(new XAException(xaErrorCode));
            }
            tm.getTransaction().enlistResource(testXAResource);

        } catch(Exception e) {
            log.error("could not enlist", e);
        }
    }

    @Override
    @WebMethod
    public void doNothing() {
        log.trace("doNothing()");
    }

    @Override
    @WebMethod
    public void arrangeBeforeCompletionFailure() {
        log.trace("arrangeBeforeCompletionFailure()");
        arrangeBeforeCompletionFailure = true;
    }

    @Override
    @WebMethod
    public void arrangeXAResourcePrepareXAException(int xaErrorCode) {
        log.trace("arrangeXAResourcePrepareXAException("+xaErrorCode+")");
        this.xaErrorCode = xaErrorCode;
    }

}
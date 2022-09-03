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

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.transaction.TransactionManager;

import org.jboss.jbossts.txbridge.tests.inbound.utility.TestSynchronization;
import org.jboss.jbossts.txbridge.tests.inbound.utility.TestXAResource;
import org.jboss.logging.Logger;


/**
 * Implementation of a web service used by txbridge test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
@Stateless
@WebService(targetNamespace = "http://client.inbound.tests.txbridge.jbossts.jboss.org/", portName = "TestServiceImpl")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class TestServiceImpl {
    private static Logger log = Logger.getLogger(TestServiceImpl.class);

    @Resource(lookup = "java:/TransactionManager")
    private TransactionManager tm;

    @WebMethod
    public void doNothing() {
        log.trace("doNothing()");
    }

    @WebMethod(exclude = true)
    public void enlistSynchronization(int count) {
        try {
            for (int i = 0; i < count; i++) {
                TestSynchronization testSynchronization = new TestSynchronization();
                tm.getTransaction().registerSynchronization(testSynchronization);
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }

    @WebMethod(exclude = true)
    public void enlistXAResource(int count) {
        try {
            for (int i = 0; i < count; i++) {
                TestXAResource testXAResource = new TestXAResource();
                tm.getTransaction().enlistResource(testXAResource);
            }
        } catch (Exception e) {
            log.error("could not enlist", e);
        }
    }
}

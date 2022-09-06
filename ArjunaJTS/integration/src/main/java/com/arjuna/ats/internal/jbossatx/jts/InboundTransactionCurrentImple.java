/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 * (C) 2008,
 * @author Redhat Middleware LLC.
 */
package com.arjuna.ats.internal.jbossatx.jts;

import org.jboss.iiop.tm.InboundTransactionCurrent;
import org.jboss.tm.TransactionManagerLocator;
import org.omg.CORBA.LocalObject;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.jbossatx.logging.jbossatxLogger;




/**
 * Implementation of the InboundTransactionCurrent interface of the app server's
 * transaction integration SPI. Provides a way for app server code to obtain the
 * transaction that was on an inbound CORBA call. The context processing on the
 * wire is handled by the JTS's RequestInterceptors, here we just provide a way
 * to expose that context to the app server.
 *
 * @see org.jboss.iiop.tm.InboundTransactionCurrent
 * @see com.arjuna.ats.jbossatx.jts.InboundTransactionCurrentInitializer
 *
 * @author jonathan.halliday@redhat.com
 * @version $Id$
 */
public class InboundTransactionCurrentImple extends LocalObject implements InboundTransactionCurrent
{
    /* jakarta TODO
    Returns:
    the javax.transaction.Transaction instance associated with the current incoming request, or null if that request was not issued within the scope of some transaction.
     */
    public javax.transaction.Transaction getCurrentTransaction()
    {
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("InboundTransactionCurrentImple.getCurrentTransaction() called");
        }

        throw new RuntimeException("InboundTransactionCurrentImple inbound transaction context is not implemented");
    }
}

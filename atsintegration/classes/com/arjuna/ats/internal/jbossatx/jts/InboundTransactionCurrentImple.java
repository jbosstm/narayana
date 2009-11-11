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
import org.jboss.logging.Logger;
import org.omg.CORBA.LocalObject;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.FacilityCode;

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
    private Logger log = org.jboss.logging.Logger.getLogger(InboundTransactionCurrentImple.class);

    public Transaction getCurrentTransaction()
    {
        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ALL,
                    "InboundTransactionCurrentImple.getCurrentTransaction() called");
        }

        TransactionManager transactionManager = null;
        Transaction transaction = null;

        try
        {
            // We need to get a Transaction representation of the tx context that came in on the CORBA call.
            // The easiest way to do this is to have the JTS transaction manager impl give us the current
            // transaction. That will cause it to create a Transaction to wrap the context, which saves us
            // doing it ourselves. Less code duplication is a good thing.
            transactionManager = TransactionManagerLocator.getInstance().getTransactionManager();
            transaction = transactionManager.getTransaction();
            if(transaction != null)
            {
                // only problem is, the transaction manager assumes we want the inbound context bound to the Thread.
                // normally that is user friendly, but in this case the downstream code seems to expect to do the
                // Thread association itself through a resume() and will be upset if we don't let it. Therefore,
                // disassociate the tx from the Thread before returning it. Inefficient and a little kludgy.
                transactionManager.suspend();
            }
        } catch(Exception e)
        {
            log.error("InboundTransactionCurrentImple.getCurrentTransaction() failed", e);
            // this is a problem, because we may actually have a valid tx context on the thread
            // which could cause weird behaviour in downstream code. We need to ensure that code is not called
            // but the API does not allow for checked excpetion to be thrown, so...
            throw new RuntimeException("InboundTransactionCurrentImple unable to determine inbound transaction context", e);
        }

        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ALL,
                    "InboundTransactionCurrentImple.getCurrentTransaction() returning tx="+transaction);
        }

        return transaction;
    }
}

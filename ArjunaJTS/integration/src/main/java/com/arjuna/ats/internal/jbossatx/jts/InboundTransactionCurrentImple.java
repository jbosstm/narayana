/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jbossatx.jts;

import org.jboss.iiop.tm.InboundTransactionCurrent;
import org.omg.CORBA.LocalObject;

import jakarta.transaction.Transaction;

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
    public Transaction getCurrentTransaction()
    {
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("InboundTransactionCurrentImple.getCurrentTransaction() called");
        }

        throw new RuntimeException("InboundTransactionCurrentImple inbound transaction context is not implemented");
    }
}
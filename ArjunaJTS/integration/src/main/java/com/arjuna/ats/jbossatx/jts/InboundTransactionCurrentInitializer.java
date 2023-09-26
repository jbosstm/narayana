/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jbossatx.jts;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.jboss.iiop.tm.InboundTransactionCurrent;
import com.arjuna.ats.internal.jbossatx.jts.InboundTransactionCurrentImple;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;




/**
 * This Initializer is used to register our InboundTransactionCurrent implementation
 * so that the app server can find it. Used together with the InterpositionORBInitializerImpl
 * this sets up processing for inbound distributed transaction contexts on the server,
 * replacing the app server's TxServerInterceptorInitializer.
 *
 * @see org.jboss.iiop.tm.InboundTransactionCurrent
 * @see com.arjuna.ats.internal.jbossatx.jts.InboundTransactionCurrentImple
 *
 * @author jonathan.halliday@redhat.com
 * @version $Id$
 */
public class InboundTransactionCurrentInitializer extends LocalObject implements ORBInitializer
{
    public void pre_init(ORBInitInfo info)
    {
        if(jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("InboundTransactionCurrentInitializer.pre_init()");
        }

        try
        {
            // Create and register the InboundTransactionCurrent implementation class
            InboundTransactionCurrentImple inboundTxCurrent = new InboundTransactionCurrentImple();
            info.register_initial_reference(InboundTransactionCurrent.NAME, inboundTxCurrent);
        }
        catch(InvalidName e)
        {
            throw new RuntimeException("Could not register initial " +
                    "reference for InboundTransactionCurrent implementation: " + e, e);
        }
    }

    public void post_init(ORBInitInfo info) {}
}
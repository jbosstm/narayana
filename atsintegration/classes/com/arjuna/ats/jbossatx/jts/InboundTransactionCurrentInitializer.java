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
        if(jbossatxLogger.logger.isDebugEnabled()) {
            jbossatxLogger.logger.debug("InboundTransactionCurrentInitializer.pre_init()");
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

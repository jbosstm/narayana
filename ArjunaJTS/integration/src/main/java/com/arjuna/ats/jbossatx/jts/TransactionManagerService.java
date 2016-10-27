/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerService.java,v 1.17 2005/06/24 15:24:14 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jts;

import com.arjuna.ats.internal.jbossatx.jts.PropagationContextWrapper;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.orbportability.Services;
import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBean;

/**
 * JBoss Transaction Manager Service.
 *
 * Should be configured via deploy/transaction-jboss-beans.xml
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerService.java,v 1.17 2005/06/24 15:24:14 kconner Exp $
 */
public class TransactionManagerService extends com.arjuna.ats.jbossatx.jta.TransactionManagerService implements TransactionManagerServiceMBean
{
    final static String ORB_NAME = "jboss-atx";

    public TransactionManagerService() {
        mode = "JTS";
    }

    public void start()
    {
        throw new IllegalArgumentException("JTS mode startup requires an ORB to be provided");
    }

    /**
     *
     * @param theCorbaORB an orb that the TM should use for JTS operations. The caller is responsible for
     *                    shutting down and destroying this orb
     * @throws Exception
     */
    public void start(org.omg.CORBA.ORB theCorbaORB) throws Exception
    {
        jbossatxLogger.i18NLogger.info_jts_TransactionManagerService_start();

        // Create an ORB portability wrapper around the CORBA ORB services orb
        ORB orb = ORB.getInstance(ORB_NAME);

        org.omg.PortableServer.POA rootPOA = org.omg.PortableServer.POAHelper.narrow(theCorbaORB.resolve_initial_references("RootPOA"));

        orb.setOrb(theCorbaORB);
        OA oa = OA.getRootOA(orb);
        oa.setPOA(rootPOA);

        try
        {
            // OTSManager won't play nice unless we explicity bootstrap the portability layer:
            ORBManager.setORB(orb);
            ORBManager.setPOA(oa);
            OrbPortabilityEnvironmentBean env = BeanPopulator.getDefaultInstance(OrbPortabilityEnvironmentBean.class);
            env.setShutdownWrappedOrb(false);

            org.omg.CosTransactions.TransactionFactory factory = com.arjuna.ats.jts.OTSManager.get_factory();
            final int resolver = Services.getResolver();

            com.arjuna.ats.jts.TransactionServer.registerTransactionManager(resolver, orb, factory);
        }
        catch (final Exception ex)
        {
            throw new Exception(jbossatxLogger.i18NLogger.get_jts_TransactionManagerService_failed(), ex);
        }
    }

    /**
     * Set whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     *
     * @param propagateFullContext
     */
    public void setPropagateFullContext(boolean propagateFullContext)
    {
        PropagationContextWrapper.setPropagateFullContext(propagateFullContext);
    }

    /**
     * Retrieve whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     */
    public boolean getPropagateFullContext()
    {
        return PropagationContextWrapper.getPropagateFullContext();
    }
}

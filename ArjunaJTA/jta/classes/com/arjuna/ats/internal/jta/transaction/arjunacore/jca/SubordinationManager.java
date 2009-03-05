/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import com.arjuna.ats.jta.logging.jtaLogger;

import javax.transaction.TransactionManager;
import javax.resource.spi.XATerminator;

/**
 * Utility factory class to return appropriate implementations of the TransactionImporter and
 * XATerminator interfaces. This will automatically instantiate the correct implementations
 * based on the use of the JTA or JTAX transaction manager configuration and is therefore
 * preferred to instantiating a specific implementation class directly.
 */
public class SubordinationManager
{
    private static TransactionImporter transactionImporter = null;
    private static XATerminator xaTerminator = null;

    public static TransactionImporter getTransactionImporter()
    {
        if(transactionImporter == null)
        {
            initTransactionImporter();
        }
        
        return transactionImporter;
    }

    public static XATerminator getXATerminator()
    {
        if(xaTerminator == null)
        {
            initXATerminator();
        }
        
        return xaTerminator;
    }

    /**
     * @message com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager.importerfailure Failed to create instance of TransactionImporter
     */
    private static void initTransactionImporter()
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        
        if(tm instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple)
        {
            // we are running in JTA mode
            transactionImporter = new TransactionImporterImple();
        }
        else
        {
            // it's not JTA, so it must be JTAX. However, we are in the JTA module and
            // can't link against the JTS code so we need to do it the hard way...
            try
            {
                Class clazz = Class.forName("com.arjuna.ats.internal.jta.transaction.jts.jca.TransactionImporterImple");
                transactionImporter = (TransactionImporter)clazz.newInstance();
            }
            catch(Exception e)
            {
                jtaLogger.loggerI18N.error("com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager.importerfailure", e);
            }
        }
    }
    
    /**
     * @message com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager.terminatorfailure Failed to create instance of XATerminator
     */
    private static void initXATerminator()
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        
        if(tm instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple)
        {
            // we are running in JTA mode
            xaTerminator = new XATerminatorImple();
        }
        else
        {
            // it's not JTA, so it must be JTAX. However, we are in the JTA module and
            // can't link against the JTS code so we need to do it the hard way...
            try
            {
                Class clazz = Class.forName("com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple");
                xaTerminator = (XATerminator)clazz.newInstance();
            }
            catch(Exception e)
            {
                jtaLogger.loggerI18N.error("com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager.terminatorfailure", e);
            }
        }
    }
}

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
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

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
    public enum TxType { JTA, JTS }    
    
    private static TransactionImporter transactionImporter = null;
    private static XATerminator xaTerminator = null;
    private static TxType txType;

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

    public static TxType getTxType()
    {
        return txType;
    }

    public static void setTxType(TxType txType)
    {
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "SubordinationManager.setTxType("+txType+")");
		}
        
        if(SubordinationManager.txType != null && SubordinationManager.txType != txType)
        {
            throw new IllegalStateException("SubordinationManager can't change txType once it has been set.");
        }
        
        SubordinationManager.txType = txType;
    }

    /**
     * @message com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager.importerfailure Failed to create instance of TransactionImporter
     */
    private static void initTransactionImporter()
    {
        if(txType == null) {
            setTxType( guessTxType() );
        }
        
        if(txType == TxType.JTA)
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
        if(txType == null) {
            setTxType( guessTxType() );
        }

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        
        if(txType == TxType.JTA)
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

    /**
     * Its rather tricky to figure out if we are running in JTA or JTAX(JTS) mode. We can make a reasonable guess
     * based on the transaction manager implementation that is running, but it's going to break if some unknown
     * or derived impl comes along. It's therefore safer to use setTxType explicitly in such cases.
     * 
     * @return best guess at the currently configured TxType.
     */
    private static TxType guessTxType() {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        
        if(tm instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple)
        {
            return TxType.JTA;
        } else if (tm.getClass().getName().contains(".jts.") || tm.getClass().getName().contains(".jtax.")) {
            return TxType.JTS;
        } else {
            return TxType.JTA;
        }
    }
}

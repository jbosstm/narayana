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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxControl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.nio.charset.StandardCharsets;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.TransactionStatusManager;

/**
 * Transaction configuration object. We have a separate object for this so that
 * other classes can enquire of (and use) this information.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxControl.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.2.
 */

public class TxControl
{
    public static final int NODE_NAME_SIZE = 28;
    public static final String DEFAULT_NODE_NAME = "Arjuna:";
    
    public static class Shutdown extends Thread
    {
        public void run()
        {
            // guard against simultaneous user-initiated shutdown
            // synchronize on the class since the shutdown method on TxControl is static synchronized
            synchronized (TxControl.class) {
            // check that this hook is still active
            if (_shutdownHook == this && transactionStatusManager != null)
            {
                transactionStatusManager.shutdown();
                transactionStatusManager = null;
            }
            }
        }
    };
    
    /**
     * If a timeout is not associated with a transaction when it is created then
     * this value will be used. A value of 0 means that the transaction will
     * never time out.
     */
	public static final int getDefaultTimeout()
	{
		return _defaultTimeout;
	}

	/**
	 * Set the timeout to be associated with a newly created transaction if there is no
	 * other timeout to be used.
	 * 
	 * @param timeout
	 */
	public static final void setDefaultTimeout(int timeout)
	{
		_defaultTimeout = timeout;
	}

	/**
	 * Start the transaction system. This allows new transactions to be created
	 * and for recovery to execute.
	 */
	
	public static final synchronized void enable()
	{
	    createTransactionStatusManager();
	    
	    TxControl.enable = true;
	}

	/**
	 * Stop the transaction system. New transactions will be prevented but
	 * recovery will be allowed to continue.
	 */
	
	public static final synchronized void disable()
	{
	    disable(false);
	}
	
	/**
         * Stop the transaction system. New transactions will be prevented and
         * recovery will cease.
         * 
         * WARNING: make sure you know what you are doing when you call this
         * routine!
         */
	
	public static final synchronized void disable (boolean disableRecovery)
        {
            /*
             * We could have an implementation that did not return until all
             * transactions had finished. However, this could take an arbitrary
             * time, especially if participants could fail. Since this information
             * is available anyway to the application, let it handle it.
             */

	    if (disableRecovery)
	        removeTransactionStatusManager();

            TxControl.enable = false;
        }


	public static final boolean isEnabled()
	{
		return TxControl.enable;
	}
        
	/**
	 * @return the <code>ObjectStore</code> implementation which the
	 *         transaction coordinator will use.
	 * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
	 */

	public static final boolean getAsyncPrepare()
	{
		return asyncPrepare;
	}

	public static final boolean getMaintainHeuristics()
	{
		return maintainHeuristics;
	}

    public static boolean isReadonlyOptimisation()
    {
        return readonlyOptimisation;
    }

    public static final String getXANodeName()
	{
		return xaNodeName;
	}

	public static final byte[] getXaNodeNameBytes() {
		return xaNodeNameBytes;
	}

	public static void setXANodeName(String name)
	{
		byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
	    if (bytes.length > NODE_NAME_SIZE) {
            tsLogger.i18NLogger.warn_coordinator_toolong(NODE_NAME_SIZE);

            throw new IllegalArgumentException();
        }
	    
		xaNodeName = name;
		xaNodeNameBytes = bytes;
	}



    public static boolean isBeforeCompletionWhenRollbackOnly()
    {
        return beforeCompletionWhenRollbackOnly;
    }

    private final static synchronized void createTransactionStatusManager ()
	{
	    if (transactionStatusManager == null && _enableTSM)
	    {
	        transactionStatusManager = new TransactionStatusManager();

	        _shutdownHook = new Shutdown();
	        
	        // add hook to ensure finalize gets called.
	        Runtime.getRuntime().addShutdownHook(_shutdownHook);
	    }
	}
	
	private final static synchronized void removeTransactionStatusManager ()
	{
	    if (_shutdownHook != null)
	    {
	        Runtime.getRuntime().removeShutdownHook(_shutdownHook);
	        
            _shutdownHook = null;

	        if (transactionStatusManager != null)
	        {
	            transactionStatusManager.shutdown();
	            transactionStatusManager = null;
	        }
	    }
	}
	
	static final boolean maintainHeuristics = arjPropertyManager.getCoordinatorEnvironmentBean().isMaintainHeuristics();

	static final boolean asyncCommit = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncCommit();

	static final boolean asyncPrepare = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncPrepare();

	static final boolean asyncRollback = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncRollback();

    static final boolean asyncBeforeSynch = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncBeforeSynchronization();

    static final boolean asyncAfterSynch = arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncAfterSynchronization();

	static final boolean onePhase = arjPropertyManager.getCoordinatorEnvironmentBean().isCommitOnePhase();

	static final boolean readonlyOptimisation = arjPropertyManager.getCoordinatorEnvironmentBean().isReadonlyOptimisation();

	static final boolean dynamic1PC = arjPropertyManager.getCoordinatorEnvironmentBean().getDynamic1PC();
	
    /**
     * flag which is true if transaction service is enabled and false if it is disabled
     */
	static volatile boolean enable = !arjPropertyManager.getCoordinatorEnvironmentBean().isStartDisabled();

	private static TransactionStatusManager transactionStatusManager = null;

	static String xaNodeName = arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier();
	static byte[] xaNodeNameBytes = (xaNodeName == null ? null : xaNodeName.getBytes(StandardCharsets.UTF_8));

	static int _defaultTimeout = arjPropertyManager.getCoordinatorEnvironmentBean().getDefaultTimeout();

    /**
     * flag which is true if enable and disable operations, respectively, start and stop the transaction status
     * manager and false if they do not perform a start and stop. this flag is true by default and can only be
     * set to false by setting property @see#com.arjuna.ats.arjuna.common.TRANSACTION_STATUS_MANAGER_ENABLE
     * to value "NO"
     */
	static final boolean _enableTSM = arjPropertyManager.getCoordinatorEnvironmentBean().isTransactionStatusManagerEnable();
    
    static final boolean beforeCompletionWhenRollbackOnly = arjPropertyManager.getCoordinatorEnvironmentBean().isBeforeCompletionWhenRollbackOnly();
	
	static Thread _shutdownHook = null;
	
	static Object _lock = new Object();

    /**
     * Creates transaction status manager.
     */
	static
	{
        // TODO -- add this check to respect the environment setting for Environment.START_DISABLED?
        // TODO -- is this feature actually needed (it appears not to be used internally)
        // if (enable) {
		createTransactionStatusManager();
        // }
	}

}

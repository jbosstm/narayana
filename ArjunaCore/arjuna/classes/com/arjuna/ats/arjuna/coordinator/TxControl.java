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

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.recovery.TransactionStatusManager;

/**
 * Transaction configuration object. We have a separate object for
 * this so that other classes can enquire of (and use) this information.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxControl.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.2.
 *
 * @message com.arjuna.ats.arjuna.coordinator.TxControl_1 [com.arjuna.ats.arjuna.coordinator.TxControl_1] - Name of XA node not defined. Using {0}
 * @message com.arjuna.ats.arjuna.coordinator.TxControl_2 [com.arjuna.ats.arjuna.coordinator.TxControl_2] - Supplied name of node is too long. Using {0}
 * @message com.arjuna.ats.arjuna.coordinator.TxControl_3 [com.arjuna.ats.arjuna.coordinator.TxControl_3] - Supplied name of node contains reserved character '-'. Using {0}
 */

public class TxControl
{

	public static final int getDefaultTimeout ()
	{
		return _defaultTimeout;
	}
	
	public static final void setDefaultTimeout (int timeout)
	{
		_defaultTimeout = timeout;
	}
	
    public static final void enable ()
    {
	TxControl.enable = true;
    }
    
    public static final void disable ()
    {
	/*
	 * We could have an implementation that did not return until
	 * all transactions had finished. However, this could take
	 * an arbitrary time, especially if participants could fail.
	 * Since this information is available anyway to the application,
	 * let it handle it.
	 */

	TxControl.enable = false;
    }

    public static final boolean isEnabled ()
    {
	return TxControl.enable;
    }

    public static final ClassName getActionStoreType ()
    {
	return actionStoreType;
    }
    
    /**
     * @return the <code>ObjectStore</code> implementation which the
     * transaction coordinator will use.
     * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
     */

    public static final ObjectStore getStore ()
    {
	/*
	 * Check for action store once per application. The second
	 * parameter is the default value, which is returned if no
	 * other value is specified.
	 */
	    
	if (TxControl.actionStoreType == null)
	{
	    String useLog = arjPropertyManager.propertyManager.getProperty(Environment.TRANSACTION_LOG, "OFF");

	    if (useLog.equals("ON"))
		TxControl.actionStoreType = new ClassName(ArjunaNames.Implementation_ObjectStore_ActionLogStore());
	    else
		TxControl.actionStoreType = new ClassName(arjPropertyManager.propertyManager.getProperty(Environment.ACTION_STORE, ArjunaNames.Implementation_ObjectStore_defaultActionStore().stringForm()));

	    String sharedLog = arjPropertyManager.propertyManager.getProperty(Environment.SHARED_TRANSACTION_LOG, "NO");
	    
	    if (sharedLog.equals("YES"))
		sharedTransactionLog = true;
	}

	/*
	 * Defaults to ObjectStore.OS_UNSHARED
	 */
	
	if (sharedTransactionLog)
	    return new ObjectStore(actionStoreType, ObjectStore.OS_SHARED);
	else
	    return new ObjectStore(actionStoreType);
    }

    public static final boolean getAsyncPrepare ()
    {
	return asyncPrepare;
    }

    public static final boolean getMaintainHeuristics ()
    {
	return maintainHeuristics;
    }

    public static final byte[] getXANodeName ()
    {
	return xaNodeName;
    }

    public static void setXANodeName (byte[] name)
    {
	xaNodeName = name;
    }
    
    static boolean maintainHeuristics = true;
    static boolean asyncCommit = false;
    static boolean asyncPrepare = false;
    static boolean asyncRollback = false;
    static boolean onePhase = true;
    static boolean readonlyOptimisation = true;
    static boolean enableStatistics = false;
    static boolean sharedTransactionLog = false;
    static int     numberOfTransactions = 100;
    static boolean enable = true;
    static TransactionStatusManager transactionStatusManager = null;
    static ClassName actionStoreType = null;
    static byte[]  xaNodeName = null;
    static int _defaultTimeout = 60; // 60 seconds

    static
    {
    	String env = arjPropertyManager.propertyManager.getProperty(Environment.DEFAULT_TIMEOUT);
    	
    	if (env != null)
    	{
    		try
    		{
    			Integer in = new Integer(env);
    			
    			_defaultTimeout = in.intValue();
    		}
    		catch (Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	
	env = arjPropertyManager.propertyManager.getProperty(Environment.MAINTAIN_HEURISTICS);
	
	if (env != null)
	{
	    if (env.compareTo("NO") == 0)
		TxControl.maintainHeuristics = false;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.ASYNC_COMMIT);
	    
	if (env != null)
	{
	    if (env.compareTo("YES") == 0)
		TxControl.asyncCommit = true;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.ASYNC_PREPARE);
	    
	if (env != null)
	{
	    if (env.compareTo("YES") == 0)
		TxControl.asyncPrepare = true;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.COMMIT_ONE_PHASE);
	    
	if (env != null)
	{
	    if (env.compareTo("NO") == 0)
		TxControl.onePhase = false;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.ASYNC_ROLLBACK);
	    
	if (env != null)
	{
	    if (env.compareTo("YES") == 0)
		TxControl.asyncRollback = true;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.READONLY_OPTIMISATION);
	    
	if (env != null)
	{
	    if (env.compareTo("NO") == 0)
		TxControl.readonlyOptimisation = false;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.ENABLE_STATISTICS);
	    
	if (env != null)
	{
	    if (env.compareTo("YES") == 0)
		TxControl.enableStatistics = true;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.START_DISABLED);
	    
	if (env != null)
	{
	    if (env.compareTo("YES") == 0)
		TxControl.enable = false;
	}

	env = arjPropertyManager.propertyManager.getProperty(Environment.XA_NODE_IDENTIFIER);
	boolean writeNodeName = false;
	
	if (env != null)
	{
	    xaNodeName = env.getBytes();
	}
	else
	{
	    Uid nodeName = new Uid();
	    
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TxControl_1",
					    new Object[]{nodeName.stringForm()});
	    }

	    xaNodeName = nodeName.stringForm().getBytes();
	    
	    writeNodeName = true;
	}

	if (xaNodeName.length > 30)
	{
	    Uid nodeName = new Uid();

	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TxControl_2",
					    new Object[]{nodeName.stringForm()});
	    }

	    xaNodeName = nodeName.stringForm().getBytes();

	    writeNodeName = true;
	}

	if ((env != null) && (env.indexOf('-') != -1))
	{
	    Uid nodeName = new Uid();

	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.TxControl_3",
					    new Object[]{nodeName.stringForm()});
	    }

	    xaNodeName = nodeName.stringForm().getBytes();

	    writeNodeName = true;
	}

	if (writeNodeName)
	{
	    arjPropertyManager.propertyManager.setProperty(Environment.XA_NODE_IDENTIFIER, new String(xaNodeName));
	}
        
	if ( transactionStatusManager == null )
	{
	    transactionStatusManager = new TransactionStatusManager();
            
            // add hook to ensure finalize gets called.
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    if ( transactionStatusManager != null )
                    {
                       transactionStatusManager.finalize() ;
                    }
                }
            }) ;
        }
    }

}

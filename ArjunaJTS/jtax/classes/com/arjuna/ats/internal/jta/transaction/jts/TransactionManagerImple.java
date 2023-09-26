/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transaction;

import org.omg.CORBA.TRANSACTION_UNAVAILABLE;
import org.omg.CosTransactions.Control;
import org.omg.PortableInterceptor.InvalidSlot;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.OTSManager;

/**
 * An implementation of jakarta.transaction.TransactionManager.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TransactionManagerImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class TransactionManagerImple extends BaseTransaction implements
		jakarta.transaction.TransactionManager, javax.naming.spi.ObjectFactory
{

	public TransactionManagerImple ()
	{
	}

	public Transaction getTransaction ()
			throws jakarta.transaction.SystemException
	{
		try
		{
			return TransactionImple.getTransaction();
		}
		catch (NullPointerException ex)
		{
			return null;
		}
		catch (TRANSACTION_UNAVAILABLE e)
		{
			try {
				Uid uid = OTSImpleManager.systemCurrent().contextManager().getReceivedCoordinatorUid();
				if (uid != null) {
					return TransactionImple.getTransactions().get(uid);
				} else {
					return null;
				}
			} catch (InvalidSlot e1) {
	            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
	            systemException.initCause(e);
	            throw systemException;
			}	
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
		    throw systemException;
		}
	}

	/**
	 * @return the suspended transaction.
	 */

	public Transaction suspend () throws jakarta.transaction.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("TransactionManagerImple.suspend");
        }

		try
		{
			TransactionImple tx = TransactionImple.getTransaction();
			Control theControl = OTSManager.get_current().suspend();

			return tx;
		}
		catch (org.omg.CORBA.TRANSACTION_UNAVAILABLE e)
		{
			try {
				Uid uid = OTSImpleManager.systemCurrent().contextManager().getReceivedCoordinatorUid();
				if (uid != null) {
					OTSImpleManager.systemCurrent().contextManager().disassociateContext(OTSManager.getReceivedSlotId());
					return TransactionImple.getTransactions().get(uid);
				} else {
					return null;
				}
			} catch (InvalidSlot e1) {
	            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
	            systemException.initCause(e);
	            throw systemException;
			}
		}
		catch (Exception e)
		{
            jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
        }
	}

	/**
	 * Unlike the OTS, if we are already associated with a transaction then we
	 * cannot call resume.
	 */

	public void resume (Transaction which) throws InvalidTransactionException,
			java.lang.IllegalStateException, jakarta.transaction.SystemException
	{
	    if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("TransactionManagerImple.resume");
        }

	    super.checkTransactionState();

	    /*
	     * Need to resume null as the standard says this is the
	     * same as suspend (breaking thread-to-transaction association).
	     */

	    if ((which == null) || (which instanceof TransactionImple))
	    {
	        TransactionImple theTransaction = (TransactionImple) which;

	        try
	        {
	            ControlWrapper cont = ((theTransaction == null) ? null : theTransaction.getControlWrapper());

	            OTSImpleManager.current().resumeWrapper(cont);

	            cont = null;
	            theTransaction = null;
	        }
	        catch (org.omg.CosTransactions.InvalidControl e1)
	        {
                InvalidTransactionException invalidTransactionException = new InvalidTransactionException();
                invalidTransactionException.initCause(e1);
                throw invalidTransactionException;
	        }
	        catch (org.omg.CORBA.SystemException e2)
	        {
                jakarta.transaction.SystemException systemException = new jakarta.transaction.SystemException(e2.toString());
                systemException.initCause(e2);
                throw systemException;
	        }
	    }
	    else
	    {
	        throw new InvalidTransactionException();
	    }
	}

	/**
	 * Creates a TransactionManageImple from the given information.
	 *
	 * @param obj
	 * @param name
	 * @param nameCtx
	 * @param environment
	 * @return Object
	 * @throws Exception
	 */
	public Object getObjectInstance (Object obj, Name name, Context nameCtx, Hashtable environment)
			throws Exception
	{
		return this;
	}
}
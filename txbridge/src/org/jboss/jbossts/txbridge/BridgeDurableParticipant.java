/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * (C) 2007, 2009 @author JBoss Inc
 */
package org.jboss.jbossts.txbridge;

import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import org.apache.log4j.Logger;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import javax.resource.spi.XATerminator;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Provides method call mapping between WS-AT Durable Participant interface
 * and an underlying JTA subtransaction coordinator.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class BridgeDurableParticipant implements Durable2PCParticipant, Serializable
{
	private static Logger log = Logger.getLogger(BridgeDurableParticipant.class);

    /*
     * Uniq String used to prefix ids at participant registration,
     * so that the recovery module can identify relevant instances.
     */
    public static String TYPE_IDENTIFIER = "BridgeDurableParticipant_";

	private transient XATerminator xaTerminator;

    private transient String externalTxId;

    static final long serialVersionUID = -5739871936627778072L;

    // Xid not guarateed Serializable by spec, but our XidImple happens to be
	private Xid xid;

    // Id needed for recovery of the subordinate tx. Uids are likewise Serializable.
    private Uid subordinateTransactionId;

    /**
     * Create a new WS-AT Durable Participant which wraps the subordinate XA tx terminator.
     *
     * @param externalTxId the WS-AT Tx identifier
     * @param xid the Xid to use when driving the subordinate XA transaction.
     */
	BridgeDurableParticipant(String externalTxId, Xid xid)
    {
		log.trace("BridgeDurableParticipant(TxId="+externalTxId+", Xid="+xid+")");

        this.xid = xid;
        this.externalTxId = externalTxId;
		xaTerminator = SubordinationManager.getXATerminator();
	}

    /**
     * Serialization hook. Gathers and writes information needed for transaction recovery.
     *
     * @param out the strean to which the object state is serialized.
     * @throws IOException if serialization fails.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        log.trace("writeObject() for Xid="+xid);

        // we need to preserve the Uid of the underlying SubordinateTx, as it's required
        // to get a handle on it again during recovery, Using the xid wont work,
        // although we do need to serialize that too for use after recovery.
        try
        {
            subordinateTransactionId = SubordinationManager.getTransactionImporter().getImportedTransaction(xid).get_uid();
        }
        catch(XAException xaException)
        {
            log.error("Unable to get subordinate transaction id", xaException);
            IOException ioException = new IOException("Unable to serialize");
            ioException.initCause(xaException);
            throw ioException;
        }

        out.defaultWriteObject();
    }

    /**
     * Deserialization hook. Unpacks transaction recovery information and uses it to
     * recover the subordinate transaction.
     *
     * @param in the strean from which to unpack the object state.
     * @throws IOException if deserialzation and recovery fail.
     * @throws ClassNotFoundException if deserialzation fails.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        log.trace("readObject()");

        in.defaultReadObject();
        xaTerminator = SubordinationManager.getXATerminator();

        try
        {
            SubordinationManager.getTransactionImporter().recoverTransaction(subordinateTransactionId);
        }
        catch(XAException xaException)
        {
            log.error("Unable to recover subordinate transaction id="+subordinateTransactionId, xaException);
            IOException ioException = new IOException("unable to deserialize");
            ioException.initCause(xaException);
            throw ioException;
        }
    }

    /**
     * Perform any work necessary to allow it to either commit or rollback
     * the work performed by the Web service under the scope of the
     * transaction. The implementation is free to do whatever it needs to in
     * order to fulfill the implicit contract between it and the coordinator.
     *
     * @return an indication of whether it can prepare or not.
     * @see com.arjuna.wst.Vote
     */
	public Vote prepare() throws WrongStateException, SystemException
	{
		log.trace("prepare(Xid="+xid+")");

		try
        {
			// XAResource.XA_OK, XAResource.XA_RDONLY or exception.  if RDONLY, don't call commit
			int result = xaTerminator.prepare(xid);
			if(result == XAResource.XA_OK)
            {
				log.debug("prepare on Xid="+xid+" returning Prepared");
				return new Prepared();
			}
            else
            {
                InboundBridgeManager.removeMapping(externalTxId);
				log.debug("prepare on Xid="+xid+" returning ReadOnly");
				return new ReadOnly();
			}

		}
        catch(XAException e)
        {
            InboundBridgeManager.removeMapping(externalTxId);
			log.warn("prepare on Xid="+xid+" returning Aborted", e);
			return new Aborted();
		}
    }

    /**
     * The participant should make permanent the work that it controls.
	 *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void commit() throws WrongStateException, SystemException
    {
		log.trace("commit(Xid="+xid+")");

		try
		{
			xaTerminator.commit(xid, false);
			log.debug("commit on Xid="+xid+" OK");
		}
		catch (XAException e)
		{
			log.error("commit on Xid="+xid+" failed", e);
		}
        finally
        {
            InboundBridgeManager.removeMapping(externalTxId);
        }
    }

    /**
     * The participant should undo the work that it controls. The participant
     * will then return an indication of whether or not it succeeded..
     *
     * @throws WrongStateException
     * @throws SystemException
     */
    public void rollback() throws WrongStateException, SystemException
    {
		log.trace("rollback(Xid="+xid+")");

		try
		{
			xaTerminator.rollback(xid);
			log.debug("rollback on Xid="+xid+" OK");
		}
		catch (XAException e)
		{
			log.error("rollback on Xid="+xid+" failed", e);
		}
        finally
        {
            InboundBridgeManager.removeMapping(externalTxId);
        }
    }

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If that transaction is no longer
     * available (has rolled back) then this operation will be invoked by the
     * coordination service.
     */
	public void unknown() throws SystemException
    {
		log.trace("unknown(Xid="+xid+"): NOT IMPLEMENTED");
    }

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If an error occurs (e.g., the
     * transaction service is unavailable) then this operation will be invoked.
     */
	public void error() throws SystemException
    {
		log.trace("error(Xid="+xid+"): NOT IMPLEMENTED");
    }
}


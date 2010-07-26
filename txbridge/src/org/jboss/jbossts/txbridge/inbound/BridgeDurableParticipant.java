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
package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.wst.*;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;
import org.omg.XA.XIDsHelper;

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
    /*
     * Uniq String used to prefix ids at participant registration,
     * so that the recovery module can identify relevant instances.
     */
    public static final String TYPE_IDENTIFIER = "BridgeDurableParticipant_";

    /*
     * Uniq (well, hopefully) formatId so we can distinguish our own Xids.
     */
    public static final int XARESOURCE_FORMAT_ID = 131080;

    private transient volatile XATerminator xaTerminator;

    private transient volatile String externalTxId;

    private transient volatile boolean isAwaitingRecovery = false;

    static final long serialVersionUID = -5739871936627778072L;

    // Xid not guaranteed Serializable by spec, but our XidImple happens to be
    private volatile Xid xid;

    // Id needed for recovery of the subordinate tx. Uids are likewise Serializable.
    private volatile Uid subordinateTransactionId;

    /**
     * Create a new WS-AT Durable Participant which wraps the subordinate XA tx terminator.
     *
     * @param externalTxId the WS-AT Tx identifier
     * @param xid the Xid to use when driving the subordinate XA transaction.
     */
    BridgeDurableParticipant(String externalTxId, Xid xid)
    {
        txbridgeLogger.logger.trace("BridgeDurableParticipant.<ctor>(TxId="+externalTxId+", Xid="+xid+")");

        this.xid = xid;
        this.externalTxId = externalTxId;
        xaTerminator = SubordinationManager.getXATerminator();
    }

    /**
     * Serialization hook. Gathers and writes information needed for transaction recovery.
     *
     * @param out the stream to which the object state is serialized.
     * @throws IOException if serialization fails.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        txbridgeLogger.logger.trace("BridgeDurableParticipant.writeObject() for Xid="+xid);

        // we need to preserve the Uid of the underlying SubordinateTx, as it's required
        // to get a handle on it again during recovery, Using the xid wont work,
        // although we do need to serialize that too for use after recovery.
        try
        {
            subordinateTransactionId = SubordinationManager.getTransactionImporter().getImportedTransaction(xid).get_uid();
        }
        catch(XAException xaException)
        {
            txbridgeLogger.i18NLogger.error_ibdp_nosub(xaException);
            throw new IOException(xaException);
        }

        //out.defaultWriteObject();
        out.writeObject(xid);
        out.writeObject(subordinateTransactionId);
    }

    /**
     * Deserialization hook. Unpacks transaction recovery information and uses it to
     * recover the subordinate transaction.
     *
     * @param in the stream from which to unpack the object state.
     * @throws IOException if deserialzation and recovery fail.
     * @throws ClassNotFoundException if deserialzation fails.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        txbridgeLogger.logger.trace("BridgeDurableParticipant.readObject()");

        //in.defaultReadObject();
        xid = (Xid)in.readObject();
        subordinateTransactionId = (Uid)in.readObject();

        // this readObject method executes only when a log is being read at recovery time:
        isAwaitingRecovery = true;

        xaTerminator = SubordinationManager.getXATerminator();

        try
        {
            SubordinationManager.getTransactionImporter().recoverTransaction(subordinateTransactionId);
        }
        catch(XAException xaException)
        {
            txbridgeLogger.i18NLogger.error_ibdp_norecovery(subordinateTransactionId, xaException);
            throw new IOException(xaException);
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
        txbridgeLogger.logger.trace("BridgeDurableParticipant.prepare(Xid="+xid+")");

        try
        {
            // XAResource.XA_OK, XAResource.XA_RDONLY or exception.  if RDONLY, don't call commit
            int result = xaTerminator.prepare(xid);
            if(result == XAResource.XA_OK)
            {
                txbridgeLogger.logger.trace("prepare on Xid="+xid+" returning Prepared");
                return new Prepared();
            }
            else
            {
                cleanupRefs();
                txbridgeLogger.logger.trace("prepare on Xid="+xid+" returning ReadOnly");
                return new ReadOnly();
            }

        }
        catch(XAException e)
        {
            // TODO: this is not necessarily an error. If the subordinate tx is setRollbackOnly
            // e.g. due to failure of VolatileParticipant.prepare, then it's expected the prepare will fail.
            // we really need to use XATerminatorExtensions to expose a isSetRollbackOnly...
            cleanupRefs();
            txbridgeLogger.i18NLogger.warn_ibdp_aborted(XAHelper.xidToString(xid), e);
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
        txbridgeLogger.logger.trace("BridgeDurableParticipant.commit(Xid="+xid+")");

        try
        {
            xaTerminator.commit(xid, false);
            txbridgeLogger.logger.trace("commit on Xid="+xid+" OK");
        }
        catch (XAException e)
        {
            txbridgeLogger.i18NLogger.error_ibdp_commitfailed(XAHelper.xidToString(xid), e);
        }
        finally
        {
            cleanupRefs();
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
        txbridgeLogger.logger.trace("BridgeDurableParticipant.rollback(Xid="+xid+")");

        try
        {
            xaTerminator.rollback(xid);
            txbridgeLogger.logger.trace("rollback on Xid="+xid+" OK");
        }
        catch (XAException e)
        {
            txbridgeLogger.i18NLogger.error_ibdp_rollbackfailed(XAHelper.xidToString(xid), e);
        }
        finally
        {
            cleanupRefs();
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
        txbridgeLogger.logger.trace("BridgeDurableParticipant.unknown(Xid="+xid+"): NOT IMPLEMENTED");
    }

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If an error occurs (e.g., the
     * transaction service is unavailable) then this operation will be invoked.
     */
    public void error() throws SystemException
    {
        txbridgeLogger.logger.trace("BridgeDurableParticipant.error(Xid="+xid+"): NOT IMPLEMENTED");
    }

    public boolean isAwaitingRecovery() {
        return isAwaitingRecovery;
    }

    public Xid getXid()
    {
        return xid;
    }

    private void cleanupRefs()
    {
        txbridgeLogger.logger.trace("BridgeDurableParticipant.cleanupRefs()");

        org.jboss.jbossts.txbridge.inbound.InboundBridgeManager.removeMapping(externalTxId);
        isAwaitingRecovery = false;
    }
}


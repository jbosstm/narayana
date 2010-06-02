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
 * (C) 2009 @author Red Hat Middleware LLC
 */
package org.jboss.jbossts.txbridge.outbound;

import com.arjuna.wst.UnknownTransactionException;
import org.apache.log4j.Logger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides method call mapping between JTA parent coordinator and WS-AT subordinate transaction.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class BridgeXAResource implements XAResource, Serializable
{
    // Design note: Given the way JBossTS is designed, we could subclass AbstractRecord rather than
    // implementing XAResource, but this design is more standards friendly and thus portable.

    private static final Logger log = Logger.getLogger(BridgeXAResource.class);

    private static final List<BridgeXAResource> xaResourcesAwaitingRecovery =
            Collections.synchronizedList(new LinkedList<BridgeXAResource>());

    private transient volatile BridgeWrapper bridgeWrapper;

    private volatile Uid externalTxId; // JTA i.e. parent id

    private volatile String bridgeWrapperId; // XTS i.e. subordinate.

    private transient volatile boolean isAwaitingRecovery = false;

    /**
     * Create a new XAResource which wraps the subordinate WS-AT transaction.
     *
     * @param externalTxId the parent JTA transaction identifier.
     * @param bridgeWrapper the control for the subordinate WS-AT transaction.
     */
    public BridgeXAResource(Uid externalTxId, BridgeWrapper bridgeWrapper)
    {
        log.trace("BridgeXARresource(TxId="+externalTxId+", BridgeWrapper="+bridgeWrapper+")");

        this.externalTxId = externalTxId;
        this.bridgeWrapper = bridgeWrapper;
        bridgeWrapperId = bridgeWrapper.getIdentifier();
    }

    /**
     * Serialization hook. Gathers and writes information needed for transaction recovery.
     *
     * @param out the stream to which the object state is serialized.
     * @throws java.io.IOException if serialization fails.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        log.trace("writeObject() for tx id="+externalTxId);

        //out.defaultWriteObject();
        out.writeObject(externalTxId);
        out.writeObject(bridgeWrapperId);
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
        log.trace("readObject()");

        //in.defaultReadObject();
        externalTxId = (Uid)in.readObject();
        bridgeWrapperId = (String)in.readObject();

        // this readObject method executes only when a log is being read at recovery time:
        isAwaitingRecovery = true;
        synchronized(xaResourcesAwaitingRecovery) {
            xaResourcesAwaitingRecovery.add(this);
        }

        try
        {
            bridgeWrapper = BridgeWrapper.recover(bridgeWrapperId);
            // rollback and commit will deal with bridgeWrapper == null cases via ensureRecoveryIsDoneIfNeeded
        }
        catch(UnknownTransactionException unknownTransactionException)
        {
            log.error("Unable to recover subordinate transaction id="+bridgeWrapperId, unknownTransactionException);
            IOException ioException = new IOException("unable to deserialize");
            ioException.initCause(unknownTransactionException);
            throw ioException;
        }
    }

    /**
     * Attempts to reconnect the BridgeWrapper if it's not already connected.
     * If XTS is not ready yet, throws the specified Exception.
     *
     * @param exceptionCode a XAException status code.
     * @throws XAException if XTS recovery is not complete.
     */
    private void ensureRecoveryIsDoneIfNeeded(int exceptionCode) throws XAException
    {
        if(bridgeWrapper == null) {
            // XTS recovery was not ready when deserialization ran. maybe it's ready now, so try again...
            try
            {
                bridgeWrapper = BridgeWrapper.recover(bridgeWrapperId);
                if(bridgeWrapper == null) {
                    // nope, still not ready...
                    throw new XAException(exceptionCode);
                }
            }
            catch(UnknownTransactionException unknownTransactionException)
            {
                log.error("Unable to recover subordinate transaction id="+bridgeWrapperId, unknownTransactionException);
                XAException xaException = new XAException(XAException.XAER_NOTA);
                xaException.initCause(unknownTransactionException);
                throw xaException;
            }
        }
    }


    /**
     * Ask the resource manager to prepare for a transaction commit of the transaction specified in xid.
     *
     * @param xid A global transaction identifier
     * @return A value indicating the resource manager's vote on the outcome of the transaction
     * @throws XAException
     */
    public int prepare(Xid xid) throws XAException
    {
        log.trace("prepare(Xid="+xid+")");

        // TwoPhaseOutcome needs converting to XAResource rtn type.
        int twoPhaseOutcome = bridgeWrapper.prepare();

        log.trace("prepare TwoPhaseOutcome is "+twoPhaseOutcome+"/"+TwoPhaseOutcome.stringForm(twoPhaseOutcome));

        switch(twoPhaseOutcome)
        {
            case TwoPhaseOutcome.PREPARE_OK:
                log.trace("prepare returning XAResource.XA_OK");
                return XAResource.XA_OK;
            case TwoPhaseOutcome.PREPARE_READONLY:
                cleanupRefs();
                log.trace("prepare returning XAResource.XA_RDONLY");
                return XAResource.XA_RDONLY;
            default:
                // TODO more find-grained error type handling
                log.trace("prepare TwoPhaseOutcome is "+twoPhaseOutcome+"/"+
                        TwoPhaseOutcome.stringForm(twoPhaseOutcome)+", throwing XAException...");
                XAException xaException = new XAException("unexpected oucome: "+TwoPhaseOutcome.stringForm(twoPhaseOutcome));
                xaException.errorCode = XAException.XA_RBROLLBACK;
                throw xaException;
        }
    }

    /**
     * Release any BridgeXAResource instances that have been driven
     * through to completion by their parent JTA transaction.
     */
    public static void cleanupRecoveredXAResources()
    {
        log.trace("cleanupRecoveredXAResources()");

        synchronized(xaResourcesAwaitingRecovery) {
            Iterator<BridgeXAResource> iter = xaResourcesAwaitingRecovery.iterator();
            while(iter.hasNext()) {
                BridgeXAResource xaResource = iter.next();
                if(!xaResource.isAwaitingRecovery()) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Determine if the specified BridgeWrapper is awaiting recovery or not.
     *
     * @param bridgeWrapperId the Id to search for.
     * @return true if the BridgeWrapper is known to be awaiting recovery, false otherwise.
     */
    public static boolean isAwaitingRecovery(String bridgeWrapperId)
    {
        synchronized(xaResourcesAwaitingRecovery) {
            for(BridgeXAResource bridgeXAResource : xaResourcesAwaitingRecovery) {
                if(bridgeXAResource.bridgeWrapperId.equals(bridgeWrapperId)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Informs the resource manager to roll back work done on behalf of a transaction branch.
     *
     * @param xid A global transaction identifier
     * @throws XAException
     */
    public void rollback(Xid xid) throws XAException
    {
        log.trace("rollback(Xid="+xid+")");

        // if XTS has not finished recovery yet we can't throw XA_RETRY here.
        // We'll settle for RMFAIL which has transient semantics.
        ensureRecoveryIsDoneIfNeeded(XAException.XAER_RMFAIL);

        try
        {
            bridgeWrapper.rollback();
        }
        finally
        {
            cleanupRefs();
        }
    }

    /**
     * Commits the global transaction specified by xid.
     *
     * @param xid A global transaction identifier
     * @param onePhase
     * @throws XAException
     */
    public void commit(Xid xid, boolean onePhase) throws XAException
    {
        log.trace("commit(Xid="+xid+", onePhase="+onePhase+")");

        // if XTS has not finished recovery yet we wont be able to complete until it does.
        ensureRecoveryIsDoneIfNeeded(XAException.XA_RETRY);

        try
        {
            if(onePhase)
            {
                // no shortcuts, we have to do prepare anyhow
                if(prepare(xid) == XAResource.XA_RDONLY)
                {
                    return;
                }
            }

            bridgeWrapper.commit();
        }
        finally
        {
            cleanupRefs();
        }
    }

    /**
     * Starts work on behalf of a transaction branch specified in xid.
     *
     * @param xid A global transaction identifier
     * @param flags
     * @throws XAException
     */
    public void start(Xid xid, int flags) throws XAException
    {
        log.trace("start(Xid="+xid+", flags="+flags+")");

        // do nothing
    }

    /**
     * Ends the work performed on behalf of a transaction branch.
     *
     * @param xid A global transaction identifier
     * @param flags
     * @throws XAException
     */
    public void end(Xid xid, int flags) throws XAException
    {
        log.trace("end(Xid="+xid+", flags="+flags+")");

        // do nothing
    }

    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        log.trace("isSameRM(XAResource="+xaResource+")");

        return false;  // TODO
    }

    public void forget(Xid xid) throws XAException
    {
        log.trace("forget(Xid="+xid+")");

        // TODO
    }

    /**
     * Obtains a list of prepared transaction branches from a resource manager.
     *
     * @param flag
     * @return
     * @throws XAException
     */
    public Xid[] recover(int flag) throws XAException
    {
        log.trace("recover(flag="+flag+")");

        return new Xid[0];  // TODO
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     *
     * @param seconds - The transaction timeout value in seconds.
     * @return true if the transaction timeout value is set successfully; otherwise false.
     * @throws XAException
     */
    public boolean setTransactionTimeout(int seconds) throws XAException
    {
        log.trace("setTransactionTimeout(seconds="+seconds+")");

        return false;  // TODO
    }

    /**
     * Obtains the current transaction timeout value set for this XAResource instance.
     *
     * @return the transaction timeout value in seconds.
     * @throws XAException
     */
    public int getTransactionTimeout() throws XAException
    {
        log.trace("getTransactionTimeout()");

        return 0;  // TODO
    }

    public boolean isAwaitingRecovery() {
        return isAwaitingRecovery;
    }

    private void cleanupRefs()
    {
        log.trace("cleanupRefs()");

        OutboundBridgeManager.removeMapping(externalTxId);
        isAwaitingRecovery = false;
    }
}

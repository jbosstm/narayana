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
 * (C) 2009 @author JBoss Inc
 */
package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.wst.*;
import com.arjuna.ats.internal.jta.resources.spi.XATerminatorExtensions;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.utils.JTAHelper;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

import javax.transaction.xa.Xid;
import javax.transaction.Status;

/**
 * Provides method call mapping between WS-AT Volatile Participant interface
 * and an underlying JTA subtransaction coordinator.
 *
 * @author jonathan.halliday@redhat.com, 2009-06-01
 */
public class BridgeVolatileParticipant implements Volatile2PCParticipant
{
    // no standard interface for driving Synchronization phases separately
    // in JCA, so we have to use proprietary API.
    private final XATerminatorExtensions xaTerminatorExtensions;

    private final String externalTxId;

    private final Xid xid;

    /**
     * Create a new WS-AT Volatile Participant which wraps the subordinate XA tx terminator.
     *
     * @param externalTxId the WS-AT Tx identifier
     * @param xid the Xid to use when driving the subordinate XA transaction.
     */
    BridgeVolatileParticipant(String externalTxId, Xid xid)
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.<ctor>(TxId="+externalTxId+", Xid="+xid+")");

        this.xid = xid;
        this.externalTxId = externalTxId;
        this.xaTerminatorExtensions = (XATerminatorExtensions)SubordinationManager.getXATerminator();
    }

    /**
     * Perform beforeCompletion activities such as flushing cached state to stable store.
     *
     * @return an indication of whether it can prepare or not.
     * @see com.arjuna.wst.Vote
     */
    public Vote prepare() throws WrongStateException, SystemException
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.prepare(Xid="+xid+")");

        // Usually a VolatileParticipant would return Aborted to stop the tx in error cases. However, that
        // would mean rollback() would not be called on the instance returning Aborted, which would make it
        // hard to invoke afterCompletion on the subordinate. So we cheat a bit by using setRollbackOnly instead.
        // A slightly more efficient but less clear impl would be to have the same object implement both the Volatile
        // and Durable Participants and keep count of the number of prepare/rollback invocations to know
        // if being invoked as Volatile or Durable.

        InboundBridge inboundBridge = InboundBridgeManager.getInboundBridge(externalTxId);

        try
        {
            // TODO: check for rollbackOnly before bothering to invoke?
            // beforeCompletion should run in tx context.
            inboundBridge.start();

            if(!xaTerminatorExtensions.beforeCompletion(xid))
            {
                txbridgeLogger.i18NLogger.warn_ibvp_preparefailed(XAHelper.xidToString(xid), null);
                inboundBridge.setRollbackOnly();
            }

            return new Prepared();
        }
        catch(Exception e)
        {
            txbridgeLogger.i18NLogger.warn_ibvp_preparefailed(XAHelper.xidToString(xid), e);
            try
            {
                inboundBridge.setRollbackOnly();
            }
            catch(Exception e2)
            {
                txbridgeLogger.i18NLogger.warn_ibvp_setrollbackfailed(e2);
            }

            return new Prepared();
        }
        finally
        {
            try
            {
                inboundBridge.stop();
            }
            catch(Exception e)
            {
                txbridgeLogger.i18NLogger.warn_ibvp_stopfailed(XAHelper.xidToString(xid), e);
            }
        }
    }

    /**
     * Perform afterCompletion cleanup activities such as releasing resources.
     *
     * Caution: may not be invoked in crash recovery situations.
     */
    public void commit() throws WrongStateException, SystemException
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.commit(Xid="+xid+")");

        afterCompletion(Status.STATUS_COMMITTED);
    }

    /**
     * Perform afterCompletion cleanup activities such as releasing resources.
     *
     * Caution: may not be invoked in crash recovery situations.
     */
    public void rollback() throws WrongStateException, SystemException
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.rollback(Xid="+xid+")");

        afterCompletion(Status.STATUS_ROLLEDBACK);
    }

    /**
     * Invoke afterCompletion on the subordinate JTA tx.
     *
     * @param status a javax.transaction.Status value, normally STATUS_COMMITTED or STATUS_ROLLEDBACK
     */
    private void afterCompletion(int status)
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.afterCompletion(Xid="+xid+", status="+status+"/"+JTAHelper.stringForm(status)+")");

        // this is a null-op, the afterCompletion is done implicitly at the XAResource commit/rollback stage.
    }

    /**
     * Deprecated, should never be called.
     */
    public void unknown() throws SystemException
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.unknown(Xid="+xid+"): NOT IMPLEMENTED");
    }

    /**
     * VolatileParticipants don't support recovery, so this should never be called.
     */
    public void error() throws SystemException
    {
        txbridgeLogger.logger.trace("BridgeVolatileParticipant.unknown(Xid="+xid+"): NOT IMPLEMENTED");
    }
}

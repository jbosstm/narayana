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
package org.jboss.jbossts.txbridge;

import org.apache.log4j.Logger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

import javax.transaction.Synchronization;
import javax.transaction.Status;

import com.arjuna.ats.jta.utils.JTAHelper;

/**
 * Provides method call mapping between JTA parent coordinator and WS-AT subordinate transaction.
 *
 * @author jonathan.halliday@redhat.com, 2009-06-01
 */
public class BridgeSynchronization implements Synchronization
{
    private static Logger log = Logger.getLogger(BridgeSynchronization.class);

    private BridgeWrapper bridgeWrapper;

    public BridgeSynchronization(BridgeWrapper bridgeWrapper)
    {
        log.trace("BridgeSynchronization(BridgeWrapper="+bridgeWrapper+")");

        this.bridgeWrapper = bridgeWrapper;
    }

    /**
     * The beforeCompletion method is called by the transaction manager prior to the start of the two-phase transaction commit process.
     */
    public void beforeCompletion()
    {
        log.trace("beforeCompletion()");

        if(!bridgeWrapper.prepareVolatile())
        {
            // JTA does not explicitly provide for beforeCompletion signalling problems, but in
            // our impl the engine will set the tx rollbackOnly if beforeCompletion throw an exception
            // Note com.arjuna.ats.jta.TransactionManager.getTransaction().setRollbackOnly may also work.
            throw new RuntimeException("BridgeWrapper.prepareVolatile() returned false");
        }
    }

    /**
     * This method is called by the transaction manager after the transaction is committed or rolled back.
     *
     * @param status the javax.transaction.Status representing the tx outcome.
     */
    public void afterCompletion(int status)
    {
        log.trace("afterCompletion(status="+status+"/"+ JTAHelper.stringForm(status)+")");

        switch(status)
        {
            case Status.STATUS_COMMITTED:
                bridgeWrapper.commitVolatile();
                break;
            case Status.STATUS_ROLLEDBACK:
                bridgeWrapper.rollbackVolatile();
                break;
            default:
                log.warn("unexpected Status "+status+", treating as ROLLEDBACK");
                bridgeWrapper.rollbackVolatile();
        }
    }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package com.jboss.transaction.txinterop.webservices.atinterop.participant;

import java.util.TimerTask;

import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Prepared;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.messaging.engines.ParticipantEngine;

/**
 * The durable 2PC participant which fails the first call to commit and recovers.
 */
public class CommitFailureRecoveryDurable2PCParticipant extends ParticipantAdapter implements Durable2PCParticipant
{
    /**
     * The participant engine.
     */
    private ParticipantEngine engine ;
    /**
     * The set recovery flag.
     */
    private boolean setRecovery ;
    /**
     * The recovering flag.
     */
    private boolean recovering ;
    
    /**
     * Set the participant engine.
     * @param engine The participant engine.
     */
    public void setEngine(final ParticipantEngine engine)
    {
        this.engine = engine ;
    }
    
    /**
     * Vote to prepare.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new Prepared() ;
    }
    
    public void commit()
        throws WrongStateException, SystemException
    {
        if (!setRecovery)
        {
            setRecovery = true ;
            final TimerTask timerTask = new TimerTask() {
                public void run() {
                    recovering = true ;
                    engine.recovery() ;
                }
            } ;
            TransportTimer.getTimer().schedule(timerTask, 2000) ;
        }
        
        if (!recovering)
        {
            throw new IllegalStateException("Forced failure of commit") ;
        }
    }
}
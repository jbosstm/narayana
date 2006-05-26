/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTAParticipant.java,v 1.3.4.1 2005/11/22 10:34:13 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta.participants;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.webservices.wsaddr.EndpointReferenceType;

import javax.transaction.xa.*;

/**
 * @message com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1 [com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1] - {0} {1}
 * @message com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_2 [com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_2] - {0}
 * @message com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_3 [com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_3] - {0} {1} {2}
 */

public class JTAParticipant implements XAResource
{
    
    public JTAParticipant (final EndpointReferenceType address)
    {
	_address = address;
    }
    
    public void commit (Xid xid, boolean onePhase) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.commit", xid});
    }

    public void end (Xid xid, int flags) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_3",
				      new Object[]{"JTAParticipant.end", xid, new String(""+flags)});
    }

    public void forget (Xid xid) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.forget", xid});
    }
    
    public int getTransactionTimeout () throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_2",
				      new Object[]{"JTAParticipant.getTransactionTimeout"});

	return 0;
    }
    
    public int prepare (Xid xid) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.prepare", xid});

	return XAResource.XA_OK;
    }

    public Xid[] recover (int flag) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.recover", new String(""+flag)});

	return null;
    }

    public void rollback (Xid xid) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.rollback", xid});
    }

    public boolean setTransactionTimeout (int seconds) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_2",
				      new Object[]{"JTAParticipant.setTransactionTimeout", new String(""+seconds)});

	return true;
    }

    public void start (Xid xid, int flags) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_3",
				      new Object[]{"JTAParticipant.start", xid, new String(""+flags)});
    }

    public boolean isSameRM (XAResource xares) throws XAException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant_1",
				      new Object[]{"JTAParticipant.isSameRM", xares});
       
	return (xares == this);
    }

    private EndpointReferenceType _address;
    
}

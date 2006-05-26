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
 * $Id: JTASynchronization.java,v 1.4.4.1 2005/11/22 10:34:13 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta.participants;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/**
 * @message com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_1 [com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_1] - {0}
 * @message com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_2 [com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_2] - {0} : {1}
 */

public class JTASynchronization implements javax.transaction.Synchronization
{

    public JTASynchronization (EndpointReferenceType address)
    {
	_address = address;
    }
    
    public void beforeCompletion ()
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_1",
				      new Object[]{"beforeCompletion"});
    }

    public void afterCompletion (int status)
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization_2",
				      new Object[]{"afterCompletion", new String(""+status)});
    }

    private EndpointReferenceType _address;
    
}

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
 * $Id: TwoPhaseSynchronization.java,v 1.4.4.1 2005/11/22 10:34:11 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.twophase.participants;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.model.twophase.common.CoordinationResult;
import com.arjuna.mw.wscf.model.twophase.participants.Synchronization;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TwoPhaseSynchronization.java,v 1.4.4.1 2005/11/22 10:34:11 kconner Exp $
 * @since 1.0.
 *
 * @message com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_1 [com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_1] - {0}
 * @message com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_2 [com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_2] - {0} ( {1} )
 */

public class TwoPhaseSynchronization implements Synchronization
{

    public TwoPhaseSynchronization (EndpointReferenceType address)
    {
	_address = address;
    }

    public void beforeCompletion () throws SystemException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_1",
				      new Object[]{"TwoPhaseSynchronization.beforeCompletion"});
    }

    public void afterCompletion (int status) throws SystemException
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.twophase.participants.TwoPhaseSynchronization_2",
				      new Object[]{"TwoPhaseSynchronization.afterCompletion", CoordinationResult.stringForm(status)});
    }

    private EndpointReferenceType _address;
    
}

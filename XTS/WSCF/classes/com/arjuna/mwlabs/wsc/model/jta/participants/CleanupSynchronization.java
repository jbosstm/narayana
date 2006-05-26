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
 * $Id: CleanupSynchronization.java,v 1.4 2005/05/19 12:13:30 nmcl Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta.participants;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mwlabs.wsc.model.jta.RegistrarImple;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CleanupSynchronization.java,v 1.4 2005/05/19 12:13:30 nmcl Exp $
 * @since 1.0.
 */

public class CleanupSynchronization implements javax.transaction.Synchronization
{

    public CleanupSynchronization (String cleanupId, RegistrarImple theRegistrar)
    {
	_cleanupId = cleanupId;
	_theRegistrar = theRegistrar;
    }

    /**
     * @message com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_1 [com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_1] - CleanupSynchronization.beforeCompletion
     */

    public void beforeCompletion ()
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_1");
    }

    /**
     * @message com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_2 [com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_2] - CleanupSynchronization.afterCompletion ( {0} )
     */

    public void afterCompletion (int status)
    {
	wscfLogger.arjLoggerI18N.info("com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization_2",
				      new Object[]{new String(""+status)});

	try
	{
	    _theRegistrar.disassociate(_cleanupId);
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    private String         _cleanupId;
    private RegistrarImple _theRegistrar;
    
}

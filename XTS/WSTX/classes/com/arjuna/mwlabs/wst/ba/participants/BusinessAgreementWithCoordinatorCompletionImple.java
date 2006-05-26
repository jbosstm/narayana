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
 * $Id: BusinessAgreementWithCoordinatorCompletionImple.java,v 1.1.2.2 2004/08/09 12:34:26 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.ba.participants;

import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BusinessAgreementWithCoordinatorCompletionImple.java,v 1.1.2.2 2004/08/09 12:34:26 nmcl Exp $
 * @since 1.0.
 */

public class BusinessAgreementWithCoordinatorCompletionImple extends BusinessAgreementWithParticipantCompletionImple implements com.arjuna.mw.wscf.model.sagas.participants.ParticipantWithComplete
{
    
    public BusinessAgreementWithCoordinatorCompletionImple (BusinessAgreementWithCoordinatorCompletionParticipant participant, String identifier)
    {
	super(participant, identifier);
    }
    
    public void complete () throws InvalidParticipantException, WrongStateException, SystemException
    {
	if (_resource != null)
	{
	    try
	    {
		((BusinessAgreementWithCoordinatorCompletionParticipant) _resource).complete();
	    }
	    catch (com.arjuna.wst.WrongStateException ex)
	    {
		throw new WrongStateException(ex.toString());
	    }
	    catch (com.arjuna.wst.SystemException ex)
	    {
		throw new SystemException(ex.toString());
	    }
	}
	else
	    throw new InvalidParticipantException();
    }

    protected void deactivateParticipantManager(final BAParticipantManager participantManager)
        throws SystemException
    {
        try
        {
            CoordinatorCompletionCoordinatorProcessor.getCoordinator().deactivateParticipant(participantManager) ;
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            
            throw new SystemException(ex.toString());
        }
    }
    
}


/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.ba.participants;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wscf.exceptions.InvalidParticipantException;
import com.arjuna.mw.wscf.model.sagas.participants.ParticipantWithComplete;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst11.BAParticipantManager;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: BusinessAgreementWithCoordinatorCompletionImple.java,v 1.1.2.2
 *          2004/08/09 12:34:26 nmcl Exp $
 * @since 1.0.
 */

public class BusinessAgreementWithCoordinatorCompletionImple extends
        BusinessAgreementWithParticipantCompletionImple implements ParticipantWithComplete
{

    public BusinessAgreementWithCoordinatorCompletionImple(
            BusinessAgreementWithCoordinatorCompletionParticipant participant,
            String identifier)
    {
        super(participant, identifier);
    }

    public BusinessAgreementWithCoordinatorCompletionImple(
            BAParticipantManager manager,
            BusinessAgreementWithCoordinatorCompletionParticipant participant,
            String identifier)
    {
        super(manager, participant, identifier);
    }

    public BusinessAgreementWithCoordinatorCompletionImple()
    {
        super();
    }

	public void complete () throws InvalidParticipantException,
			WrongStateException, SystemException
	{
		if (_resource != null)
		{
			try
			{
				((BusinessAgreementWithCoordinatorCompletionParticipant) _resource)
						.complete();
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
}
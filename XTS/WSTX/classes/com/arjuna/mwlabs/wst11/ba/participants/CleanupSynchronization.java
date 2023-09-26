/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.ba.participants;

import com.arjuna.mwlabs.wst11.ba.RegistrarImple;

import com.arjuna.mw.wscf.model.sagas.participants.*;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CleanupSynchronization.java,v 1.3 2005/05/19 12:13:42 nmcl Exp $
 * @since 1.0.
 */

public class CleanupSynchronization implements Synchronization
{

	public CleanupSynchronization(String cleanupId, RegistrarImple theRegistrar)
	{
		_cleanupId = cleanupId;
		_theRegistrar = theRegistrar;
	}

	public void afterCompletion (int status) throws SystemException
	{
		try
		{
			_theRegistrar.disassociate(_cleanupId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			throw new SystemException(ex.toString());
		}
	}

	private String _cleanupId;

	private RegistrarImple _theRegistrar;

}
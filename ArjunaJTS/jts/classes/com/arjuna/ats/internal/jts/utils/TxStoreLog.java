/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.utils;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;

public class TxStoreLog
{

    public static boolean getTransactions (InputObjectState os)
    {
	return getTransactions(os, StateStatus.OS_UNKNOWN);
    }
 
    public static boolean getTransactions (InputObjectState os, int status)
    {
	RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

	try
	{
	    return recoveryStore.allObjUids(com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple.typeName(), os, status);
	}
	catch (NullPointerException ex)
	{
	}
	catch (ObjectStoreException e)
	{
	    e.printStackTrace();
	}

	return false;
    }

}
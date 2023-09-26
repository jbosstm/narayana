/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class StateManagerWrapper extends StateManager {
	public static final DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");

	String state;
	Uid txId = Uid.nullUid();
	Uid processUid = Uid.nullUid();
	long birthDate = -1;

	public StateManagerWrapper(RecoveryStore os, Uid uid, String type) {
		super(uid);

        try {
			HeaderStateReader headerStateReader = ObjStoreBrowser.getHeaderStateUnpacker(type);

			if (headerStateReader != null) {
				HeaderState hs = headerStateReader.unpackHeader(os.read_committed(uid, type));

				if (hs != null)  {
					state = hs.getState();
					txId = hs.getTxUid();
					processUid = hs.getProcessUid();
					birthDate = hs.getBirthDate();
				}
			}
        } catch (IOException e) {
            tsLogger.i18NLogger.info_osb_StateManagerWrapperFail(e);
        } catch (ObjectStoreException e) {
            tsLogger.i18NLogger.info_osb_StateManagerWrapperFail(e);
        }
	}

	public String getCreationTime()
	{
		return birthDate < 0 ? "" : formatter.format(new Date(birthDate));
	}

	public long getAgeInSeconds()
	{
		return (birthDate < 0 ? -1 : ((System.currentTimeMillis()) - birthDate) / 1000L);
	}
}

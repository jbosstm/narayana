package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StateManagerWrapper extends StateManager {
	public static final DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");

	String state;
	Uid txId = Uid.nullUid();
	Uid processUid = Uid.nullUid();
	long birthDate = -1;

	public StateManagerWrapper(ObjectStore os, Uid uid, String type) {
		super(uid);

        try {

            unpackHeader(os.read_committed(uid, type));
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

	void unpackHeader(InputObjectState os) throws IOException
	{
		if (os != null) {
			state = os.unpackString();
			byte[] txIdBytes = os.unpackBytes();
			txId = new Uid(txIdBytes);

			if (state.equals("#ARJUNA#")) {
				if (!txId.equals(Uid.nullUid())) {
					byte[] pUidBytes = os.unpackBytes();
					processUid = new Uid(pUidBytes);
				}

				birthDate = os.unpackLong();
			}
		}
	}

}

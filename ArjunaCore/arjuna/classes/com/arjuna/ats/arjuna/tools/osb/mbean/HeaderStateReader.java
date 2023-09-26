/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.IOException;

/**
 * Per record header reader. The majority of records pack the header state {@link HeaderState} first.
 * This class unpacks that state. If a record type packs information before the standard header
 * then a subclass of this type should be provided to
 * {@link com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser#registerHandler}. For example
 * see {@link com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ServerTransactionHeaderReader}
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class HeaderStateReader {
    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        HeaderState hs = null;

        if (os != null) {

            String state = os.unpackString();
            byte[] txIdBytes = os.unpackBytes();
            Uid txUid = new Uid(txIdBytes);
            Uid processUid = null;
            long birthDate = 0;

            if (state.equals("#ARJUNA#")) {
                if (!txUid.equals(Uid.nullUid())) {
                    byte[] pUidBytes = os.unpackBytes();
                    processUid = new Uid(pUidBytes);
                }

                birthDate = os.unpackLong();
            }

            hs = new HeaderState(state, txUid, processUid, birthDate);
        }

        return hs;
    }
}

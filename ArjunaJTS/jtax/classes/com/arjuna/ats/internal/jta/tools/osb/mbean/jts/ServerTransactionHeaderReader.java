/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.HeaderState;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.HeaderStateReader;

import java.io.IOException;

/**
 * ObjectStore record header reader for ServerTransaction
 *
 * @author Mike Musgrove
 */
public class ServerTransactionHeaderReader extends HeaderStateReader {
    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        boolean haveRecCoord = os.unpackBoolean();

        if (haveRecCoord)
            os.unpackString(); // read ior

        return super.unpackHeader(os);
    }
}

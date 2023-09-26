/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeaderState;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeaderStateReader;

import java.io.IOException;

/**
 * ObjectStore record header reader for ServerTransaction
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class ServerTransactionHeaderReader extends HeaderStateReader {
    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        boolean haveRecCoord = os.unpackBoolean();

        if (haveRecCoord)
            os.unpackString(); // read ior

        return super.unpackHeader(os);
    }
}
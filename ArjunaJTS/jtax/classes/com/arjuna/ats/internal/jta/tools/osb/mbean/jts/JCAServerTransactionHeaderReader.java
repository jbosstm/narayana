/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.HeaderState;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * Header reader for {@link com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction}
 * records.
 *
 * @author Mike Musgrove
 */
public class JCAServerTransactionHeaderReader extends ServerTransactionHeaderReader {
    private boolean wasInvoked = false;

    public JCAServerTransactionHeaderReader() {
        this.wasInvoked = false;
    }

    protected HeaderState unpackHeader(InputObjectState os) throws IOException {
        wasInvoked = true;

        if (os.unpackBoolean())
            new XidImple().unpackFrom(os);

        return super.unpackHeader(os);
    }

    public boolean isWasInvoked() {
        return wasInvoked;
    }
}

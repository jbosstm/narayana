/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * All log store records contain the state held in this object.
 *
 * @author Mike Musgrove
 */
public class HeaderState {
    String state;
    Uid txUid;
    Uid processUid;
    long birthDate;

    public HeaderState(String state, Uid txUid, Uid processUid, long birthDate) {
        this.state = state;
        this.txUid = txUid;
        this.processUid = processUid;
        this.birthDate = birthDate;
    }

    public String getState() {
        return state;
    }

    public Uid getTxUid() {
        return txUid;
    }

    public Uid getProcessUid() {
        return processUid;
    }

    public long getBirthDate() {
        return birthDate;
    }
}

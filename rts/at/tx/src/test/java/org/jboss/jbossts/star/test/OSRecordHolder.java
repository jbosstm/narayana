/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class OSRecordHolder {
    Uid uid;
    String type;
    InputObjectState ios;
    OutputObjectState oos;

    public OSRecordHolder(Uid uid, String type, InputObjectState ios) {
        this.uid = uid;
        this.type = type;
        this.ios = ios;
        this.oos = new OutputObjectState(ios);
    }
}
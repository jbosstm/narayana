/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.common.Uid;
import io.narayana.lra.coordinator.domain.service.LRAService;

public class FailedLongRunningAction extends LongRunningAction {

    public static final String FAILED_LRA_TYPE = "/StateManager/BasicAction/LongRunningAction/Failed";


    public FailedLongRunningAction(LRAService lraService, Uid rcvUid) {
        super(lraService, rcvUid);
    }

    // used for MBean LRA listing, see com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser
    public FailedLongRunningAction(Uid rcvUid) {
        this(new LRAService(), rcvUid);
    }

    @Override
    public String type() {
        return FAILED_LRA_TYPE;
    }

    public static String getType() {
        return FAILED_LRA_TYPE;
    }
}
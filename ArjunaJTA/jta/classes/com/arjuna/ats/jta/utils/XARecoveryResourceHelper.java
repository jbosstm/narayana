/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.utils;

import com.arjuna.ats.jta.recovery.XARecoveryResource;

public class XARecoveryResourceHelper
{

    public static String stringForm(int status)
    {
        switch (status)
        {
            case XARecoveryResource.RECOVERED_OK:
                return "XARecoveryResource.RECOVERED_OK";
            case XARecoveryResource.FAILED_TO_RECOVER:
                return "XARecoveryResource.FAILED_TO_RECOVER";
            case XARecoveryResource.WAITING_FOR_RECOVERY:
                return "XARecoveryResource.WAITING_FOR_RECOVERY";
            case XARecoveryResource.TRANSACTION_NOT_PREPARED:
                return "XARecoveryResource.TRANSACTION_NOT_PREPARED";
            case XARecoveryResource.INCOMPLETE_STATE:
                return "XARecoveryResource.INCOMPLETE_STATE";
            case XARecoveryResource.INFLIGHT_TRANSACTION:
                return "XARecoveryResource.INFLIGHT_TRANSACTION";
            case XARecoveryResource.RECOVERY_REQUIRED:
                return "XARecoveryResource.RECOVERY_REQUIRED";
            default:
                return "[XARecoveryResource unknown status]";
        }
    }

}
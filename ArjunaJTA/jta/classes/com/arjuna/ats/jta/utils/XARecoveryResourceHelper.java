/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * @author JBoss Inc.
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

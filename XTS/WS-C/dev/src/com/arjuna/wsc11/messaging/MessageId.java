/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created on 15-Jan-2005
 */
package com.arjuna.wsc11.messaging;

import java.rmi.dgc.VMID;

/**
 * A simple message id generator, used when UID is inaccessible.
 * @author kevin
 */
public class MessageId
{
    /**
     * Prevent instantiation.
     */
    private MessageId()
    {
    }

    /**
     * Get the next message identifier.
     * @return The next message identifier.
     */
    public static String getMessageId()
    {
        return "urn:" + new VMID().toString() ;
    }
}
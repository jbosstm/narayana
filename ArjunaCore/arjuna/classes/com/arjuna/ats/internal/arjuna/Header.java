/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * @author Mark Little (mark@arjuna.com)
 * @since JBTM 4.9.0.
 */

public class Header
{
    public Header ()
    {
        _txId = null;
        _processId = null;
    }
    
    public Header (Uid txId, Uid processId)
    {
        _txId = txId;
        _processId = processId;
    }
    
    public Uid getTxId ()
    {
        return _txId;
    }
    
    public Uid getProcessId ()
    {
        return _processId;
    }
    
    public void setTxId (Uid txId)
    {
        _txId = txId;
    }
    
    public void setProcessId (Uid processId)
    {
        _processId = processId;
    }
    
    private Uid _txId;
    private Uid _processId;
}
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  	    
 *
 * $Id$
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

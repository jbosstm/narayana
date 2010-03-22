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

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

public class SampleOnePhaseResource extends TestResource
{
    public enum ErrorType { none, heurcom, heurrb, heurmix, rmerr, nota, inval, proto };
    
    public SampleOnePhaseResource ()
    {
        this(ErrorType.none);
    }
    
    public SampleOnePhaseResource (ErrorType type)
    {
        _heuristic = type;
    }
    
    public boolean onePhaseCalled ()
    {
        return _onePhase;
    }

    public boolean forgetCalled ()
    {
        return _forgot;
    }
    
    public void commit (Xid id, boolean onePhase) throws XAException
    {
        System.out.println("XA_COMMIT[" + id + "]");
        
        _onePhase = onePhase;
        
        if (_heuristic == ErrorType.heurcom)
            throw new XAException(XAException.XA_HEURCOM);
        else
        {
            if (_heuristic == ErrorType.heurrb)
                throw new XAException(XAException.XA_HEURRB);
            else
            {
                if (_heuristic == ErrorType.heurmix)
                    throw new XAException(XAException.XA_HEURMIX);
                else
                {
                    if (_heuristic == ErrorType.rmerr)
                        throw new XAException(XAException.XAER_RMERR);
                    else
                    {
                        if (_heuristic == ErrorType.nota)
                            throw new XAException(XAException.XAER_NOTA);
                        else
                        {
                            if (_heuristic == ErrorType.inval)
                                throw new XAException(XAException.XAER_INVAL);
                            else
                            {
                                if (_heuristic == ErrorType.proto)
                                    throw new XAException(XAException.XAER_PROTO);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void forget (Xid xid) throws XAException
    {
            System.out.println("XA_FORGET[" + xid + "]");
            
            _forgot = true;
    }

    private boolean _onePhase = false;
    private ErrorType _heuristic = ErrorType.none;
    private boolean _forgot = false;
}

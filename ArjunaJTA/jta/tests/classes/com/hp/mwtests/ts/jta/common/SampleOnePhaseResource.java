/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
        this(type, true);
    }
    
    public SampleOnePhaseResource (ErrorType type, boolean print)
    {
        super(false, print);
        
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
        if (_doPrint)
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
        if (_doPrint)
            System.out.println("XA_FORGET[" + xid + "]");
            
        _forgot = true;
    }

    private boolean _onePhase = false;
    private ErrorType _heuristic = ErrorType.none;
    private boolean _forgot = false;
}
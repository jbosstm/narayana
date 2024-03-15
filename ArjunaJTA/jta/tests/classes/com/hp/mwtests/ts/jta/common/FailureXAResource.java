/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class FailureXAResource implements XAResource {

    public enum FailLocation {none, prepare, commit, rollback, end, prepare_and_rollback}

    public enum FailType {normal, timeout, heurcom, nota, inval, proto, rmfail, rollback, XA_RBCOMMFAIL, XA_HEURHAZ, message, XA_RBINTEGRITY}

    private final FailLocation _locale;
    private final FailType _type;

    private boolean _rolledBack = false;
    private boolean _committed = false;

    public FailureXAResource() {
        this(FailLocation.none, FailType.normal);
    }

    public FailureXAResource(FailLocation loc) {
        this(loc, FailType.normal);
    }

    public FailureXAResource(FailLocation loc, FailType type) {
        _locale = loc;
        _type = type;
    }

    public void commit(Xid id, boolean onePhase) throws XAException {
        if (_locale == FailLocation.commit) {
            if (_type == FailType.normal)
                throw new XAException(XAException.XA_HEURMIX);

            if (_type == FailType.heurcom)
                throw new XAException(XAException.XA_HEURCOM);

            if (_type == FailType.rollback)
                throw new XAException(XAException.XA_HEURRB);

            if (_type == FailType.nota)
                throw new XAException(XAException.XAER_NOTA);

            if (_type == FailType.inval)
                throw new XAException(XAException.XAER_INVAL);

            if (_type == FailType.proto)
                throw new XAException(XAException.XAER_PROTO);

            if (_type == FailType.rmfail)
                throw new XAException(XAException.XAER_RMFAIL);

            if (_type == FailType.XA_RBINTEGRITY)
                throw new XAException(XAException.XA_RBINTEGRITY);

            throw new XAException(XAException.XA_RBTIMEOUT);
        }
        // successful
        this._committed = true;
    }

    public void end(Xid xid, int flags) throws XAException {
        if (_locale == FailLocation.end) {
            if (_type == FailType.normal)
                throw new XAException(XAException.XA_HEURRB);

            if (_type == FailType.timeout)
                throw new XAException(XAException.XA_RBTIMEOUT);

            if (_type == FailType.XA_RBCOMMFAIL)
                throw new XAException(XAException.XA_RBCOMMFAIL);
        }
    }

    public void forget(Xid xid) throws XAException {
    }

    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    public boolean isSameRM(XAResource xares) throws XAException {
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        if ((_locale == FailLocation.prepare) || (_locale == FailLocation.prepare_and_rollback)) {
            if (_type == FailType.message) {
                XAException xae = new XAException(XAException.XA_RBROLLBACK);
                xae.initCause(new Throwable("test message"));
                throw xae;
            } else if (_type == FailType.XA_HEURHAZ) {// XA spec invalid error code
                throw new XAException(XAException.XA_HEURHAZ);
            } else if (_type == FailType.XA_RBINTEGRITY) {
                throw new XAException(XAException.XA_RBINTEGRITY);
            } else {
                throw new XAException(XAException.XAER_INVAL);
            }
        }

        return XA_OK;
    }

    public Xid[] recover(int flag) throws XAException {
        return null;
    }

    public void rollback(Xid xid) throws XAException {
        if ((_locale == FailLocation.rollback) || (_locale == FailLocation.prepare_and_rollback)) {
            if (_type == FailType.normal)
                throw new XAException(XAException.XA_HEURMIX);

            if (_type == FailType.heurcom)
                throw new XAException(XAException.XA_HEURCOM);

            if (_type == FailType.rollback)
                throw new XAException(XAException.XA_HEURRB);

            if (_type == FailType.nota)
                throw new XAException(XAException.XAER_NOTA);

            if (_type == FailType.inval)
                throw new XAException(XAException.XAER_INVAL);

            if (_type == FailType.proto)
                throw new XAException(XAException.XAER_PROTO);

            if (_type == FailType.rmfail)
                throw new XAException(XAException.XAER_RMFAIL);

            if (_type == FailType.XA_RBINTEGRITY)
                throw new XAException(XAException.XAER_NOTA);

            throw new XAException(XAException.XA_HEURHAZ);
        }

        // Successful
        this._rolledBack = true;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return true;
    }

    public void start(Xid xid, int flags) throws XAException {
    }

    public boolean isRolledBack() {
        return _rolledBack;
    }

    public boolean isCommitted() {
        return _committed;
    }

}
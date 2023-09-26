/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server;

import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

public interface RemoteServer {

	public int prepare(Xid xid, boolean recover) throws XAException;

	public void commit(Xid xid, boolean onePhase, boolean recover) throws XAException;

	public void rollback(Xid xid, boolean recover) throws XAException;

	public void forget(Xid xid, boolean recover) throws XAException;

	public void beforeCompletion(Xid xid) throws SystemException;

	public Xid[] recoverFor(String localServerName) throws XAException;

	public int getTransactionCount();
}
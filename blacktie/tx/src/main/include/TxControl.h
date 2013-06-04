/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
#ifndef _TX_CONTROL_H
#define _TX_CONTROL_H

#include <map>
#include "txi.h"
#include <apr_portable.h>

namespace atmibroker {
	namespace tx {

/**
 * An object that gets associated with a thread when there
 * is an active transaction.
 */
class BLACKTIE_TX_DLL TxControl {
public:
	TxControl(long timeout, apr_os_thread_t tid);
	virtual ~TxControl();

	virtual int rollback_only() = 0;
	virtual int get_status() = 0;
	virtual bool isActive(const char *, bool) = 0;
	virtual bool get_xid(XID& xid) = 0;
	virtual char * enlist(const char *host, int port, const char *xid) {return NULL;};
	virtual bool isOTS() {return false;}

	void* get_control(long* ttlp);

	int commit(bool reportHeuristics);
	int rollback();
	/**
	 * Return the amount of time in seconds remaining before the txn is subject to rollback,
	 * a value -1 means the txn is not subject to timeouts
	 */
	long ttl();
	bool isOriginator();	
	apr_os_thread_t thr_id() {return _tid;}

	// return a list of outstanding xatmi call descriptors associated with this tx
	std::map<int, int (*)(int)> &get_cds() {return _cds;}
	// Note this op disassociates the tx and releases _ctrl:
	// perhaps we should make it private and use friend TxManager;
	virtual void suspend() = 0;

protected:
	virtual int do_end(bool commit, bool reportHeuristics) = 0;
	virtual void* get_control() = 0;

	bool _rbonly;	// txn marked rollback only after txn timeout
	long _ttl;	// time left until the tx is subject to being rolled back

private:
	int end(bool commit, bool reportHeuristics);

	long _ctime;	// creation time in seconds (since 1970)
	apr_os_thread_t _tid;	// apr thread id
	std::map<int, int (*)(int)> _cds;  // xatmi outstanding tpacall descriptors
};
} //	namespace tx
} //namespace atmibroker
#endif	// _TX_CONTROL_H

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
#ifndef _XAWRAPPER_H
#define _XAWRAPPER_H

#include <map>

#include "SynchronizableObject.h"
#include "XARecoveryLog.h"
#include "XAStateModel.h"
#include "txi.h"
#include "XABranchNotification.h"

class BLACKTIE_TX_DLL XAWrapper
{
public:
	XAWrapper(XABranchNotification* rm, XID& bid,
		long rmid, struct xa_switch_t *xa_switch, XARecoveryLog& log, const char* rc = NULL);
	virtual ~XAWrapper();

	// REST-AT resource methods
	int do_prepare();
	int do_rollback();
	int do_commit();
	int do_commit_one_phase();
	int do_forget();

	bool is_complete();	// has this resource finished 2PC - need for testing
	void set_recovery_coordinator(char *rc) {_rc = rc;}
	const char* get_recovery_coordinator() {return _rc;}
	void notify_error(int reason, bool forget);
	virtual bool isOTS() {return false;}

	int xa_start (long);
	int xa_end (long);

	const char* get_name() { return _name;}

protected:
	atmibroker::xa::XAStateModel sm_;
	XABranchNotification * rm_;
	XID bid_;
	bool complete_;
	long rmid_;
	struct xa_switch_t * xa_switch_;
	char *_rc;
	int flags_;
	long eflags_;
	int tightly_coupled_;
	XARecoveryLog& rclog_;
	bool prepared_;
	char *_name;

	int do_terminate(int, bool commit);

	int set_flags(int flags);
	void set_complete();

	// XA methods
	int xa_rollback (long);
	int xa_prepare (long);
	int xa_commit (long);
	int xa_recover (long, long);
	int xa_forget (long);
	int xa_complete (int *, int *, long);
};

#endif // _XAWRAPPER_H

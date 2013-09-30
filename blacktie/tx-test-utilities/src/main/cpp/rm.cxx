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
#include <vector>
#include <algorithm>
#include <string.h>

#include "xa.h"
#include "testrm.h"
#include "btlogger.h"
#include "SynchronizableObject.h"

#include <stdlib.h>
#include <stdio.h>

using namespace std;

static SynchronizableObject _lock;
static long counter = 0;
static vector<fault_t> faults;

static XID gen_xid(long id, long sid, XID &gid)
{
	XID xid = {gid.formatID, gid.gtrid_length};
	int i;

	for (i = 0; i < gid.gtrid_length; i++)
		xid.data[i] = gid.data[i];

	apr_time_exp_t now;
	apr_time_exp_gmt(&now, apr_time_now());
	// the first long in the XID data must contain the RM id
	(void) sprintf(xid.data + i, "%ld:%ld:%ld:%d:%d", id, sid, ++counter, now.tm_sec, now.tm_usec);
	xid.bqual_length = strlen(xid.data + i);

	return xid;
}

void dummy_rm_dump() {
    vector<fault_t>::iterator i;

	btlogger_debug("dummy_rm: dump %d faults", faults.size());
    for (i = faults.begin() ; i < faults.end(); i++ ) {
		btlogger_debug("dummy_rm: fault: id=%d rmid=%d op %d xarc=%d fault %d arg %p",
			i->id, i->rmid, i->op, i->rc, i->xf, i->arg);
	}
}

int dummy_rm_add_fault(fault_t& fault) {
	btlogger_debug("dummy_rm: add_fault:");
	fault.orig = &fault;
	(fault.orig)->res = 0;
	(fault.orig)->res2 = 0;

	if (fault.xf == F_ADD_XIDS) {
		int i;
		long larg = *((long*) fault.arg);
		XID gid = {1L, 1L, 0L};

		if (larg >= 10 || larg <= 0)
			return 1;

		fault.rmstate.cursor = 0;
		fault.rmstate.count = larg;

		for (i = 0; i < larg; i++) {
			XID xid = gen_xid(fault.rmid, 0L, gid);
			memcpy(&(fault.rmstate.xids[i]), &xid, sizeof (XID));
		}
	}

	faults.push_back(fault);

	return 0;
}

int dummy_rm_del_fault(fault_t& f)
{
	if (f.id == -1)
		faults.clear();
	else
		faults.erase(std::remove(faults.begin(), faults.end(), f), faults.end());

	return 0;
}

static int apply_faults(XID *xid, enum XA_OP op, int rmid)
{
	long *larg;
	long fc = 0L;
	int rc = 0;
	vector<fault_t>::iterator f;

	btlogger_debug("dummy_rm: apply_faults: op=%d rmid=%d\n", op, rmid);

	_lock.lock();

	for (f = faults.begin() ; f < faults.end(); f++) {
		fc += 1;

		btlogger_debug("dummy_rm: cf op=%d rmid=%d\n", f->op, f->rmid);

		if (fc == 100)
			btlogger_debug("dummy_rm: too many fault specifications\n");
		if (f->rmid == rmid && f->op == op) {
			if (f->xf != F_NONE) {
				btlogger_debug("dummy_rm: applying fault %d to op %d rc %d\n", f->xf, op, f->rc);
			}

			switch (f->xf) {
			default:
				printf("dummy_rm: default\n");
				break;
			case F_CB:
				btlogger_debug("dummy_rm: calling callback\n");
				/* cast arg to a function pointer of type (void(*)(void) and call it */
				((void(*)(void))(f->arg))();
				break;
			case F_HALT:
				/* generate a SEGV fault */
				btlogger_debug("dummy_rm: generating a SEGV fault\n");
				larg = 0;
				*larg = 0;
				break;
			case F_DELAY:
				larg = (long*) f->arg;

				if (larg == 0 || *larg < 0L || *larg > 3600L) {
					btlogger_debug("dummy_rm: sleep period is invalid arg=%ld", larg == 0 ? 0 : *larg);
				} else {
					btlogger_debug("dummy_rm: delaying for %ld seconds\n", *larg);
					(void) apr_sleep(apr_time_from_sec(*larg));
				}
				break;
			}
			rc = f->rc;
			_lock.unlock();
			btlogger_debug("dummy_rm: fault return: %d\n", rc);

			return rc;
		}
	}
	_lock.unlock();

	btlogger_debug("dummy_rm: fault return: XA_OK\n");

	return XA_OK;
}

// remove xid from any fault specifications used to support recovery testing
static void end_check(XID *xid, int rmid) {
	vector<fault_t>::iterator f;

	// TODO move this into apply_faults (ie generalise apply_faults)
	_lock.lock();
	for (f = faults.begin() ; f < faults.end(); f++) {
		if (f->rmid == rmid && f->op == O_XA_RECOVER && f->xf == F_ADD_XIDS) {
			struct xid_array *xids = &(f->rmstate);
			int i;

			for (i = 0; i < xids->count; i++) {
				if (memcmp(xid, &(xids->xids[i]), sizeof (XID)) == 0) { // bytewise compare
					for (i = i + 1; i < xids->count; i++)
						memcpy(&(xids->xids[i - 1]), &(xids->xids[i]), sizeof (XID));

					xids->count -= 1;
//				  f->orig->res = xids->count;
					f->orig->res += 1;
				}
			}
		}
	}
	_lock.unlock();
}

static int recover(XID *xid, long l1, int rmid, long flags) {
	vector<fault_t>::iterator f;
	int rv = apply_faults(NULL, O_XA_RECOVER, rmid);

	_lock.lock();
	for (f = faults.begin() ; f < faults.end(); f++) {
		if (f->rmid == rmid && f->op == O_XA_RECOVER && f->xf == F_ADD_XIDS) {
			struct xid_array *xids = &(f->rmstate);
			int i;
	
			// note a recovery scan than spans multiple calls must be done in the same thread
			// - we don't check for this since this is only a dummy RM for testing particular
			// behaviour. Likewise we don't validate the TMNOFLAGS flag
			if ((xids == NULL && l1 > 0) || l1 < 0) {
				rv = XAER_INVAL;
				break;
			}

			if (flags & TMSTARTRSCAN)
				xids->cursor = 0;

			for (i = 0; i < l1 && xids->cursor <= xids->count; xids->cursor++, i++)
				xid[i] = xids->xids[xids->cursor];

			if (flags & TMENDRSCAN)
				xids->cursor = 0;

			// record the number of recovered XIDs in res and the number needing recovery in res2
			f->orig->res = 0;
			f->orig->res2 = xids->count;

			rv = (i > 0 ? i - 1 : i);
			break;
		}
	}
	_lock.unlock();
			
	return rv;
}

static int open(char *a, int rmid, long l) {
	return apply_faults(NULL, O_XA_OPEN, rmid);
}
static int close(char *a, int rmid, long l) {
	return apply_faults(NULL, O_XA_CLOSE, rmid);
}

static int start(XID *x, int rmid, long l) {
	return apply_faults(x, O_XA_START, rmid);
}
static int end(XID *x, int rmid, long l) {
	return apply_faults(x, O_XA_END, rmid);
}
static int prepare(XID *x, int rmid, long l) {
	return apply_faults(x, O_XA_PREPARE, rmid);
}

static int commit(XID *x, int rmid, long l) {
	int rv = apply_faults(x, O_XA_COMMIT, rmid);
	end_check(x, rmid);
	return rv;
}
static int rollback(XID *x, int rmid, long l) {
	int rv = apply_faults(x, O_XA_ROLLBACK, rmid);
	end_check(x, rmid);
	return rv;
}
static int forget(XID *x, int rmid, long l) {
	int rv =  apply_faults(x, O_XA_FORGET, rmid);
	end_check(x, rmid);
	return rv;
}

static int complete(int *ip1, int *ip2, int rmid, long l) {
	return apply_faults(NULL, O_XA_COMPLETE, rmid);
}

struct xa_switch_t testxasw = {
	"DummyRM", 0L, 0,
	open, close,
	start, end, rollback, prepare, commit,
	recover,
	forget,
	complete
};


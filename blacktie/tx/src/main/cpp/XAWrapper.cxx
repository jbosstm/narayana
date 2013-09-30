/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and others contributors as indicated
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

#include "XAWrapper.h"
#include "HttpClient.h"
#include <string.h>
#include <stdlib.h>

using namespace atmibroker::xa;

log4cxx::LoggerPtr xarwlogger(log4cxx::Logger::getLogger("TxXAWrapper"));

extern std::ostream& operator<<(std::ostream &os, const XID& xid);

static char * encode_xid(XID& xid) {
	HttpClient wc;
	char data[1024];
	char *buf = (char *) malloc(1024);

	memset(buf, 0, 1024);

	sprintf(data, "%ld:%s", xid.formatID, xid.data + xid.gtrid_length);

	wc.url_encode(data, buf, 1024);

	return buf;
}

XAWrapper::XAWrapper(XABranchNotification* rm, XID& bid,
		long rmid, struct xa_switch_t *xa_switch, XARecoveryLog& log, const char* rc) :
			rm_(rm), bid_(bid), complete_(false), rmid_(rmid), xa_switch_(xa_switch),
			_rc(NULL), eflags_(0l), tightly_coupled_(0), rclog_(log), prepared_(false), _name(NULL)
{
	FTRACE(xarwlogger, "ENTER address=" << this << (char*) " new XA resource rmid:" << rmid_ <<
		" branch id: " << bid_);

	if (rc != NULL) {
		_rc = strdup(rc);
		prepared_ = true;
	}

	_name = encode_xid(bid_);

	LOG4CXX_DEBUG(xarwlogger, (char*) "encoded name: " << _name);
}

XAWrapper::~XAWrapper()
{
	FTRACE(xarwlogger, "ENTER address=" << this);
	LOG4CXX_TRACE(xarwlogger, (char *) "Deleting branch " << bid_);

	if (_rc != NULL)
		free(_rc);

	if (_name)
		free(_name);
}

void XAWrapper::notify_error(int reason, bool forget)
{
	FTRACE(xarwlogger, "ENTER");
	if (rm_)
		rm_->notify_error(this, reason, forget);
		//rm_->notify_error(&xid_, reason, forget);
}

void XAWrapper::set_complete()
{
	FTRACE(xarwlogger, "ENTER");
	complete_ = true;

	if (rm_)
		rm_->set_complete(this);
		//rm_->set_complete(xid_);
}

int XAWrapper::do_prepare()
{
	FTRACE(xarwlogger, "ENTER astate=" << sm_.astate() << " bstate=" << sm_.bstate());
	int rv1, rv2;

	// This resource is joining an existing branch. In this case the thread that
	// originally started the branch is responsible for all updates the RM.
	// Disable since we have introduced bid_ for unique branches for work
	// performed on the RM from different processes
	if (tightly_coupled_)
		return XA_OK;

	rv1 = xa_end(TMSUCCESS);
	rv2 = xa_prepare(TMNOFLAGS);

	if (rv1 != XA_OK && rv2 == XA_OK) {
		LOG4CXX_DEBUG(xarwlogger, (char*) "resource: end TMSUCCESS was already set");
	}

	if (rv2 != XA_OK && rv2 != XA_RDONLY) {
		LOG4CXX_WARN(xarwlogger, (char *) xa_switch_->name <<
			(char*) ": prepare resource error: " << rv2 << " rid=" << rmid_ << (char*) " rv1=" << rv1);
	} else {
		LOG4CXX_DEBUG(xarwlogger, (char*) "prepare resource end ok: rid=" << rmid_
			<< (char*) " rv1=" << rv1 << " rv2=" << rv2 << " bstate=" << sm_.bstate());
	}

	if (_rc == NULL) {
		rv2 = XA_RDONLY;
		LOG4CXX_ERROR(xarwlogger, (char *) "prepare called but no recovery coordinator has been set - assuming RDONLY");
	}

	if (rv2 == XA_OK) {
#if TX_RC == 3
		if (getenv("TEST_BLACKTIE_209")) {
			LOG4CXX_INFO(xarwlogger, (char *) "Test BLACKTIE_209:- SEGV after prepare but before writing log");
			char *s = 0;
			*s = 0;
		}
#endif
		LOG4CXX_DEBUG(xarwlogger, (char *) "Writing recovery record for branch " << bid_);
		// about to vote commit - remember the descision
		rclog_.add_rec(bid_, _rc);
		prepared_ = true;
	}

	switch (rv2) {
	case XA_OK:
		return XA_OK;	// VoteCommit
	case XA_RDONLY:
		set_complete();
		return XA_RDONLY;	// VoteReadOnly
	case XA_RBROLLBACK:
	case XA_RBCOMMFAIL:
	case XA_RBDEADLOCK:
	case XA_RBINTEGRITY:
	case XA_RBOTHER:
	case XA_RBPROTO:
	case XA_RBTIMEOUT:
	case XA_RBTRANSIENT:
	case XAER_ASYNC:
	case XAER_RMERR:
	case XAER_RMFAIL:
	case XAER_NOTA:
	case XAER_INVAL:
	case XAER_PROTO:
		return XA_RBROLLBACK;	//VoteRollback
	default:	 // shouldn't happen
		return XA_RBROLLBACK;	//VoteRollback
	}
}

int XAWrapper::do_terminate(int rv, bool commit)
{
	FTRACE(xarwlogger, "ENTER");

	switch (rv) {
	default:
		break;
	case XA_HEURHAZ:
		rv = XA_HEURHAZ;
		break;
	case XA_HEURCOM:
		// a heuristic descision to commit was made (we may have been lucky) 
		rv = (commit ? XA_OK : XA_HEURRB);
		break;
	case XA_HEURRB:
	case XA_RBROLLBACK:	// these codes may be returned only if the TMONEPHASE flag was set
	case XA_RBCOMMFAIL:
	case XA_RBDEADLOCK:
	case XA_RBINTEGRITY:
	case XA_RBOTHER:
	case XA_RBPROTO:
	case XA_RBTIMEOUT:
	case XA_RBTRANSIENT:
		rv = (commit ? XA_HEURRB : XA_OK);
		break;
	case XA_HEURMIX:
		rv = XA_HEURMIX;
		break;
	}

	if (rv == XA_OK) {
		// remove the entry for this branch from the recovery log
		LOG4CXX_DEBUG(xarwlogger, (char *) "Removing recovery record for branch " <<
			bid_ << " prepared=" << prepared_);
		if (prepared_ && rclog_.del_rec(bid_) != 0) {
			LOG4CXX_DEBUG(xarwlogger, (char *) xa_switch_->name <<
				": do_terminate - entry not found in recovery log rid=" << rmid_);
		}

		set_complete();
	}

	LOG4CXX_TRACE(xarwlogger, (char*) "resource XAWrapper::do_terminate rv=" << rv);

	return rv;
}

int XAWrapper::do_commit()
{
	FTRACE(xarwlogger, "ENTER");
	int rv;

	if (!prepared_) {
		LOG4CXX_WARN(xarwlogger, (char*) "commit called but resource hasn't been prepared");
		rv = XAER_PROTO;	// SPEC doesn't say what happens to the txn but ORACLE rolls it back
	} else if (tightly_coupled_) {
		set_complete();
		rv = XA_OK;
	} else {
		rv = xa_commit (TMNOFLAGS);	// no need for xa_end since prepare must have been called

		LOG4CXX_TRACE(xarwlogger, (char*) "resource commit rv=" << rv);

		rv = do_terminate(rv, true);
	}

	return rv;
}

int XAWrapper::do_rollback()
{
	long eflags = eflags_;

	FTRACE(xarwlogger, "ENTER");
	if (tightly_coupled_) {
		set_complete();
		return XA_OK;
	}

	int rv = xa_end (TMSUCCESS);

	if (rv != XA_OK && eflags != TMSUCCESS) {
		LOG4CXX_WARN(xarwlogger, (char *) xa_switch_->name <<
			(char*) ": rollback resource end error " << rv <<
				" for rid " << rmid_ << " - flags=" << std::hex << eflags);
	} else {
		// if rv != XA_OK and the branch was already idle then log at debug only - see ch 6 of the XA spec
		LOG4CXX_DEBUG(xarwlogger, (char *) xa_switch_->name <<
			(char*) ": rollback resource end result " << rv <<
				" for rid " << rmid_ << " - flags=" << std::hex << eflags);
	}

	rv = xa_rollback (TMNOFLAGS);
	LOG4CXX_DEBUG(xarwlogger, (char*) "resource rollback rv=" << rv);

	return do_terminate(rv, false);
}

int XAWrapper::do_commit_one_phase()
{
	FTRACE(xarwlogger, "ENTER");

	if (tightly_coupled_) {
		set_complete();
		return XA_OK;
	}

	int rv = xa_end (TMSUCCESS);

	if (rv != XA_OK) {
		LOG4CXX_WARN(xarwlogger, (char *) xa_switch_->name <<
			(char*) ": commit 1PC resource end failed: error=" << rv << " rid=" << rmid_);
	} else {
		LOG4CXX_DEBUG(xarwlogger, (char*) "1PC resource end ok, rid=" << rmid_);
	}

	rv = xa_commit (TMONEPHASE);
	LOG4CXX_DEBUG(xarwlogger, (char*) "1PC resource commit rv=" << rv);

	return do_terminate(rv, true);
}

int XAWrapper::do_forget()
{
	FTRACE(xarwlogger, "ENTER");
	int rv = xa_forget (TMNOFLAGS);

	LOG4CXX_TRACE(xarwlogger, (char*) "resource forget rv=" << rv);
	set_complete();

	return rv;
}

// accessors
bool XAWrapper::is_complete()
{
	FTRACE(xarwlogger, "ENTER");
	return complete_;
}

// XA methods
int XAWrapper::xa_start (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER astate=" << sm_.astate() << " bstate=" << sm_.bstate());

	LOG4CXX_TRACE(xarwlogger, (char*) "xaostart: flags=" << flags << " XID format: " << bid_.formatID <<
		" gtrid length="<< bid_.gtrid_length << ", bqual length=" << bid_.bqual_length <<
		" data=" << bid_.data);
 
	int rv = xa_switch_->xa_start_entry(&bid_, rmid_, flags);
	return sm_.transition(bid_, XACALL_START, flags, rv);
}
int XAWrapper::xa_end (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << std::hex << sm_.bstate() << " flags=" << flags);
 
	LOG4CXX_DEBUG(xarwlogger, (char*) "XA_END rmid: " << rmid_ << std::hex << " flags: " << eflags_ << " -> " << flags);
	eflags_ = flags;

	// if the branch is already idle just return OK - see ch 6 of the XA specification
//	if (sm_.bstate() == S2)
//		return XA_OK;

	int rv = xa_switch_->xa_end_entry(&bid_, rmid_, flags);
	LOG4CXX_DEBUG(xarwlogger, (char*) "XA_END res: " << rv);
	return sm_.transition(bid_, XACALL_END, flags, rv);
}
int XAWrapper::xa_rollback (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << sm_.bstate());

	int rv = xa_switch_->xa_rollback_entry(&bid_, rmid_, flags);
	return sm_.transition(bid_, XACALL_ROLLBACK, flags, rv);
}
int XAWrapper::xa_prepare (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << sm_.bstate());
	int rv = xa_switch_->xa_prepare_entry(&bid_, rmid_, flags);
	return sm_.transition(bid_, XACALL_PREPARE, flags, rv);
}
int XAWrapper::xa_commit (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << sm_.bstate());
	LOG4CXX_DEBUG(xarwlogger, (char*) "Commiting resource with branch id: " << bid_);
	int rv = xa_switch_->xa_commit_entry(&bid_, rmid_, flags);
	return sm_.transition(bid_, XACALL_COMMIT, flags, rv);
}
int XAWrapper::xa_recover (long xxx, long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << sm_.bstate());
	int rv = xa_switch_->xa_recover_entry(&bid_, xxx, rmid_, flags);
	return sm_.transition(bid_, XACALL_RECOVER, flags, rv);
}
int XAWrapper::xa_forget (long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER bstate=" << sm_.bstate());
	int rv = xa_switch_->xa_forget_entry(&bid_, rmid_, flags);
	return sm_.transition(bid_, XACALL_FORGET, flags, rv);
}
int XAWrapper::xa_complete (int * handle, int * retvalue, long flags)
{
	FTRACE(xarwlogger, (char*) "ENTER");
	int rv = xa_switch_->xa_complete_entry(handle, retvalue, rmid_, flags);
	return rv;
}

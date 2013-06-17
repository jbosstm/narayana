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
#include "XAStateModel.h"
#include "log4cxx/logger.h"

log4cxx::LoggerPtr xasmlogger(log4cxx::Logger::getLogger("TxXAStateModel"));

static std::string flag_dbg_str;

extern std::ostream& operator<<(std::ostream &os, const XID& xid);

namespace atmibroker {
	namespace xa {

XAStateModel::XAStateModel() : astate_(T0), bstate_(S0)
{
}

// enable XA state model checks - can be disabled to improve performance overhead
// provided XAResourceAdaptorImpl remembers when it goes into state Idle (via calls to xa_end).
// XAResourceAdaptorImpl::xa_end is the only place where the current state is consulted
//#define XASM
int XAStateModel::transition(XID& xid, enum XAEVENT method, long flags, int rv)
{
	LOG4CXX_TRACE(xasmlogger, "transition:ENTER xid="
		<< xid.formatID << ':' << xid.gtrid_length << ':' << xid.bqual_length << ':' << xid.data
		<< " rv=" << rv << " flags=" << std::hex << flags << show_flags(flags)
		<< " method=" << method << " astate=" << astate_ << " bstate=" << bstate_);

#ifdef XASM
	int rv1 = XA_OK, rv2;

	if ((method < XACALL_PREPARE ))
		rv1 =  atransition(&astate_,  method, flags, rv);

	rv2 =  btransition(&bstate_,  method, flags, rv);

	if (rv != XA_OK && rv != XA_RDONLY) {
		// output at DEBUG rather than WARN since maintaining a distributed view of XA state model
		// is problematic
		LOG4CXX_DEBUG(xasmlogger, (char*) "XA error xa_ method: " << method << " error: " << rv);
	}

	LOG4CXX_TRACE(xasmlogger, (char*) "transition: rv1=" << rv1 << " rv2=" << rv2);
#endif

	return rv;
}

int XAStateModel::transition(int allowable[], int *initial_state, int next_state)
{
	LOG4CXX_TRACE(xasmlogger, (char *) "transition:ENTER");
	int *as = allowable;

	while (*as != -1) {
		if (*initial_state == *as++) {
			*initial_state = next_state;
			return XA_OK;
		}
	}

	// output at DEBUG rather than WARN since maintaining a distributed view of XA state model
	// is problematic
	if (*initial_state == next_state)
		return XA_OK;	// TODO when do we get called without a change in state (recovery is one such quickstart)

	LOG4CXX_DEBUG(xasmlogger, (char*) "Unexpected XA transition 0x" << std::hex << *initial_state
		<< " -> 0x" << std::hex << next_state);

	return XAER_PROTO;
}

int XAStateModel::btransition(int *ini_state, enum XAEVENT method, long flag, int rval)
{
	LOG4CXX_TRACE(xasmlogger, (char *) "btransition:ENTER");
	int ct[] = {*ini_state, -1};
//	int s0t[] = {S0, -1};
	int s1t[] = {S1, -1};
	int s2t[] = {S2, -1};
	int s3t[] = {S3, -1};
//	int s4t[] = {S4, -1};
	int s5t[] = {S5, -1};
	int s02t[] = {S0, S2, -1};
	int s23t[] = {S2, S3, -1};
	int s235t[] = {S2, S3, S5, -1};
	int s234t[] = {S2, S3, S4, -1};
	int s345t[] = {S3, S4, S5, -1};
	int s02345t[] = {S0, S2, S3, S4, S5, -1};

	if (rval == XAER_RMFAIL) {
		return transition(ct, ini_state, R0);
	} else if (method == XACALL_START) {
		if (rval >= XA_RBBASE && rval <= XA_RBEND)
			return transition(s2t, ini_state, S4);
		else
			return transition(s02t, ini_state, S1);
	} else if (method == XACALL_END) {
		if (rval >= XA_RBBASE && rval <= XA_RBEND)
			return transition(s1t, ini_state, S4);
		else
			return transition(s1t, ini_state, S2);
	} else if (method == XACALL_PREPARE) {
		if (rval == XAER_RMERR)
			return transition(s2t, ini_state, S2);
		else if (rval == XA_RDONLY || (rval >= XA_RBBASE && rval <= XA_RBEND))
			return transition(s2t, ini_state, S0);
		else
			return transition(s2t, ini_state, S3);
	} else if (method == XACALL_COMMIT) {
		if (rval == XA_OK || rval == XAER_RMERR)
			return transition(s23t, ini_state, S0);
		else if (rval >= XA_RBBASE && rval <= XA_RBEND)
			return transition(s2t, ini_state, S0);
		else if (rval == XA_RETRY)
			return transition(s3t, ini_state, S3);
		else if (rval == XA_HEURHAZ || rval == XA_HEURCOM || rval == XA_HEURRB || XA_HEURMIX)
			return transition(s235t, ini_state, S5);
		else
			LOG4CXX_TRACE(xasmlogger, "xa_commit: no transition for rval " << rval);
	} else if (method == XACALL_ROLLBACK) {
		if (rval == XA_OK || rval == XAER_RMERR || (rval >= XA_RBBASE && rval <= XA_RBEND))
			return transition(s234t, ini_state, S0);
		else if (rval == XA_HEURHAZ || rval == XA_HEURCOM || rval == XA_HEURRB || XA_HEURMIX)
			return transition(s345t, ini_state, S5);
		else
			LOG4CXX_TRACE(xasmlogger, "xa_rollback: no transition for rval " << rval);
	} else if (method == XACALL_FORGET) {
		if (rval == XAER_RMERR)
			return transition(s5t, ini_state, S5);
		else
			return transition(s5t, ini_state, S0);
	} else if (method == XACALL_OPEN) {
		return transition(ct, ini_state, *ini_state);
	} else if (method == XACALL_RECOVER) {
		return transition(ct, ini_state, *ini_state);
	} else if (method == XACALL_CLOSE) {
		return transition(s02345t, ini_state, R0);
	} else {
		LOG4CXX_TRACE(xasmlogger, "Unexpected branch state call " << method);
		*ini_state = R0;
		return XAER_PROTO;
	}
}

int XAStateModel::atransition(int *ini_state, enum XAEVENT method, long flag, int rval)
{
	LOG4CXX_TRACE(xasmlogger, (char *) "atransition:ENTER");
	int ct[] = {*ini_state, -1};
	int t0t[] = {T0, -1};
	int t1t[] = {T1, -1};
	int xxxt1t[] = {T0, T1, -1};
	int t2t[] = {T2, -1};
	int xxxt2t[] = {T0, T2, -1};
	int t12t[] = {T1, T2, -1};
	int t02t[] = {T0, T2, -1};

	if (rval == XAER_RMFAIL) {
		return transition(ct, ini_state, R0);
	} else if (method == XACALL_START) {
		if (flag & TMRESUME) {
			if (rval >= XA_RBBASE && rval <= XA_RBEND)
				return transition(t2t, ini_state, T0);
			else
				return transition(xxxt2t, ini_state, T1);
		} else {
			return transition(t0t, ini_state, T1);
		}
	} else if (method == XACALL_END) {
		if (flag & TMSUSPEND) {
			LOG4CXX_TRACE(xasmlogger, (char*) "transition: XACALL_END TMSUSPEND " << rval);
			if (rval >= XA_RBBASE && rval <= XA_RBEND)
				return transition(t1t, ini_state, T0);
			else
				return transition(xxxt1t, ini_state, T2);
		} else if (flag & TMSUCCESS) {
			return transition(t12t, ini_state, T0);
		} else if (flag & TMFAIL) {
			return transition(t12t, ini_state, T0);
		} else {
			LOG4CXX_TRACE(xasmlogger, "Unexpected association end call - no matching transition: flag " << flag);
			*ini_state = R0;
			return XAER_PROTO;
		}
	} else if (method == XACALL_OPEN) {
		return transition(ct, ini_state, *ini_state);
	} else if (method == XACALL_RECOVER) {
		return transition(ct, ini_state, *ini_state);
	} else if (method == XACALL_CLOSE) {
		return transition(t02t, ini_state, R0);
	} else {
		LOG4CXX_TRACE(xasmlogger, "Unexpected association state call " << method);
		*ini_state = R0;
		return XAER_PROTO;
	}
}

std::string XAStateModel::show_flags(long flags)
{
	flag_dbg_str = (char *) " (";

	if (flags & TMASYNC)
		flag_dbg_str += (char *) "TMASYNC|";
	if (flags & TMONEPHASE)
		flag_dbg_str += (char *) "TMONEPHASE|";
	if (flags & TMFAIL)
		flag_dbg_str += (char *) "TMFAIL|";
	if (flags & TMNOWAIT)
		flag_dbg_str += (char *) "TMNOWAIT|";
	if (flags & TMRESUME)
		flag_dbg_str += (char *) "TMRESUME|";
	if (flags & TMSUCCESS)
		flag_dbg_str += (char *) "TMSUCCESS|";
	if (flags & TMSUSPEND)
		flag_dbg_str += (char *) "TMSUSPEND|";
	if (flags & TMSTARTRSCAN)
		flag_dbg_str += (char *) "TMSTARTRSCAN|";
	if (flags & TMENDRSCAN)
		flag_dbg_str += (char *) "TMENDRSCAN|";
	if (flags & TMMULTIPLE)
		flag_dbg_str += (char *) "TMMULTIPLE|";
	if (flags & TMJOIN)
		flag_dbg_str += (char *) "TMJOIN|";
	if (flags & TMMIGRATE)
		flag_dbg_str += (char *) "TMMIGRATE|";

	flag_dbg_str += ')';

	return flag_dbg_str; //XX.c_str;
}

	} //	namespace xa {
}   // namespace atmibroker {

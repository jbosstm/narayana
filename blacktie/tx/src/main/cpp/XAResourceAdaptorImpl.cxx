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

#include "XAResourceAdaptorImpl.h"

using namespace atmibroker::xa;

log4cxx::LoggerPtr xaralogger(log4cxx::Logger::getLogger("TxXAResourceAdaptorImpl"));

extern std::ostream& operator<<(std::ostream &os, const XID& xid);

XAResourceAdaptorImpl::XAResourceAdaptorImpl(
	XABranchNotification * rm, XID& bid, long rmid,
	struct xa_switch_t * xa_switch, XARecoveryLog& log, const char *rc) :
	XAWrapper(rm, bid, rmid, xa_switch, log, rc)
{
	FTRACE(xaralogger, "ENTER address=" << this);
}

XAResourceAdaptorImpl::~XAResourceAdaptorImpl()
{
	FTRACE(xaralogger, "ENTER address=" << this);
}

Vote XAResourceAdaptorImpl::prepare()
	throw (HeuristicMixed,HeuristicHazard)
{
	FTRACE(xaralogger, "ENTER");

	switch(do_prepare()) {
	case XA_OK:
		return VoteCommit;
	case XA_RDONLY:
		return VoteReadOnly;
	case XA_RBROLLBACK: 
		//FALLTHRU
	default:
		return VoteRollback;
	}
}

void XAResourceAdaptorImpl::terminate(int rv)
	throw(
		HeuristicRollback,
		HeuristicMixed,
		HeuristicHazard)
{
	FTRACE(xaralogger, "ENTER");

	switch (rv) {
	case XA_HEURHAZ: {
		HeuristicHazard e;
		notify_error(rv, true);
		throw e;
		break;
	}
	case XA_HEURRB: {
		HeuristicRollback e;
		notify_error(rv, true);
		throw e;
		break;
	}
	case XA_HEURCOM:
	case XA_HEURMIX: {
		HeuristicMixed e;
		notify_error(rv, true);
		throw e;
		break;
	}
	default:
		break;
	}
}

void XAResourceAdaptorImpl::commit()
	throw(
		NotPrepared,
		HeuristicRollback,
		HeuristicMixed,
		HeuristicHazard)
{
	FTRACE(xaralogger, "ENTER");
	terminate(do_commit());
}

void XAResourceAdaptorImpl::rollback()
	throw(HeuristicCommit,HeuristicMixed,HeuristicHazard)
{
	FTRACE(xaralogger, "ENTER");
	terminate(do_rollback());
}

void XAResourceAdaptorImpl::commit_one_phase() throw(HeuristicHazard)
{
	FTRACE(xaralogger, "ENTER");
	terminate(do_commit_one_phase());
}

void XAResourceAdaptorImpl::forget()
{
	FTRACE(xaralogger, "ENTER");
	(void) do_forget();
}

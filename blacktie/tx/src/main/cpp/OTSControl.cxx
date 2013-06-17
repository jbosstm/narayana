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
#include "log4cxx/logger.h"
#include "ThreadLocalStorage.h"
#include "OTSControl.h"
#include "TxManager.h"

#include "ace/OS_NS_time.h"
#include "ace/OS_NS_sys_time.h"

#define TX_GUARD(msg, expect) { \
	FTRACE(otsclogger, "ENTER"); \
	if (!isActive(msg, expect)) {   \
		return TX_PROTOCOL_ERROR;   \
	}}

namespace atmibroker {
	namespace tx {

log4cxx::LoggerPtr otsclogger(log4cxx::Logger::getLogger("OTSControl"));

OTSControl::OTSControl(CosTransactions::Control_ptr ctrl, long timeout, int tid) : TxControl(timeout, tid),
	_ctrl(ctrl) {
	FTRACE(otsclogger, "ENTER new OTSCONTROL: " << this);
}

OTSControl::~OTSControl()
{
	FTRACE(otsclogger, "ENTER delete OTSCONTROL: " << this);
	suspend();
}

int OTSControl::do_end(bool commit, bool reportHeuristics)
{
	TX_GUARD("end", true);
	int outcome = TX_OK;
	CosTransactions::Terminator_var term;

	try {
		term = _ctrl->get_terminator();

		if (CORBA::is_nil(term)) {
			LOG4CXX_WARN(otsclogger, (char*) "end: no terminator");
			outcome = TX_FAIL;
		}
	} catch (CosTransactions::Unavailable & e) {
		LOG4CXX_WARN(otsclogger, (char*) "end: term unavailable: " << e._name());
		outcome = TX_FAIL;
	} catch (CORBA::OBJECT_NOT_EXIST & e) {
		LOG4CXX_DEBUG(otsclogger, (char*) "end: term ex " << e._name() << " minor: " << e.minor());
		// transaction no longer exists (presumed abort)
		outcome = TX_ROLLBACK;
	} catch (CORBA::INVALID_TRANSACTION & e) {
		LOG4CXX_WARN(otsclogger, (char*) "end get terminater: ex: wrong exception type (see JBTM-748)"
			<< e._name() << " minor: " << e.minor());
		// cannot assume TX_ROLLBACK since the RMs will not have been told (must return TX_FAIL to indicate
		// that the caller’s state with respect to the transaction is unknown)
		outcome = TX_FAIL;
	} catch (...) {
		LOG4CXX_WARN(otsclogger, (char*) "end: unknown error looking up terminator");
		outcome = TX_FAIL; // TM failed temporarily
	}

	if (outcome == TX_OK) {
		try {
			// ask the transaction service to end the tansaction
			(commit ? term->commit(reportHeuristics) : term->rollback());

		} catch (CORBA::TRANSACTION_ROLLEDBACK &e) {
			LOG4CXX_INFO(otsclogger, (char*) "end: rolled back: " << e._name());
			outcome = TX_ROLLBACK;
		} catch (CosTransactions::Unavailable & e) {
			LOG4CXX_INFO(otsclogger, (char*) "end: unavailable: " << e._name());
			outcome = TX_FAIL; // TM failed temporarily
		} catch (CosTransactions::HeuristicMixed &e) {
			LOG4CXX_INFO(otsclogger, (char*) "end: HeuristicMixed: " << e._name());
			// can be thrown if commit_return characteristic is TX_COMMIT_COMPLETED
			outcome = TX_MIXED;
		} catch (CosTransactions::HeuristicHazard &e) {
			LOG4CXX_ERROR(otsclogger, (char*) "end: HeuristicHazard: " << e._name());
			// can be thrown if commit_return characteristic is TX_COMMIT_COMPLETED
			outcome = TX_HAZARD;
		} catch (CORBA::INVALID_TRANSACTION & e) {
			LOG4CXX_WARN(otsclogger, (char*) "end: terminate ex: wrong exception type (see JBTM-748)"
			<< e._name() << " minor: " << e.minor());
			// cannot assume TX_ROLLBACK since the RMs will not have been told (must return TX_FAIL to indicate
			// that the caller’s state with respect to the transaction is unknown)
			outcome = TX_FAIL;
		} catch (CORBA::OBJECT_NOT_EXIST & e) {
			LOG4CXX_DEBUG(otsclogger, (char*) "end: term ex " << e._name() <<
			" minor: " << e.minor());
			// transaction no longer exists (presumed abort) assume that in between the
			// time we obtain the terminator and the time we called commit/rollback on it
			// the transaction timed out and the terminator corba object was destroyed
			outcome = TX_ROLLBACK;
		} catch (CORBA::SystemException & e) {
			LOG4CXX_WARN(otsclogger, (char*) "end: " << e._name() << " minor: " << e.minor());
			outcome = TX_FAIL;
		} catch (...) {
			LOG4CXX_WARN(otsclogger, (char*) "end: unknown error");
			outcome = TX_FAIL; // TM failed temporarily
		}
	}

	LOG4CXX_DEBUG(otsclogger, (char*) "end: outcome: " << outcome);

	return outcome;
}

int OTSControl::rollback_only()
{
	TX_GUARD("rollback_only", true);

	try {
		CosTransactions::Coordinator_var coord = _ctrl->get_coordinator();

		if (!CORBA::is_nil(coord.in())) {
			coord->rollback_only();
			return TX_OK;
		}
	} catch (CosTransactions::Unavailable & e) {
		// no coordinator
		LOG4CXX_WARN(otsclogger, (char*) "rollback_only: unavailable: " << e._name());
	} catch (CORBA::OBJECT_NOT_EXIST & e) {
		// ought to be due to the txn timing out
		LOG4CXX_DEBUG(otsclogger, (char*) "rollback_only: " << e._name() << " minor: " << e.minor());
		_rbonly = true;
		return TX_OK;
	} catch (CORBA::INVALID_TRANSACTION & e) {	// TODO this is wrong (its a workaround for JBTM-748
		LOG4CXX_WARN(otsclogger, (char*) "rollback_only: wrong exception type (see JBTM-748)"
			<< e._name() << " minor: " << e.minor());
//		_rbonly = true;
		return TX_FAIL;	// should not happen (see comments in OTSControl::end)
	} catch (CORBA::SystemException & e) {
		LOG4CXX_WARN(otsclogger, (char*) "rollback_only: " << e._name() << " minor: " << e.minor());
	}

	return TX_FAIL;
}

CosTransactions::Status OTSControl::get_ots_status()
{
	FTRACE(otsclogger, "ENTER");
	if (!isActive(NULL, false)) {
		return CosTransactions::StatusNoTransaction;
	}

	try {
		CosTransactions::Coordinator_var coord = _ctrl->get_coordinator();

		if (!CORBA::is_nil(coord.in()))
			return (coord->get_status());
	} catch (CosTransactions::Unavailable & e) {
		// no coordinator
		LOG4CXX_TRACE(otsclogger, (char*) "unavailable: " << e._name());
	} catch (CORBA::OBJECT_NOT_EXIST & e) {
		LOG4CXX_DEBUG(otsclogger, (char*) "status: " << e._name() << " minor: " << e.minor());
		// transaction no longer exists (presumed abort)
		return CosTransactions::StatusNoTransaction;
	} catch (CORBA::INVALID_TRANSACTION & e) {	// TODO this is wrong (its a workaround for JBTM-748
		LOG4CXX_WARN(otsclogger, (char*) "status: wrong exception type (see JBTM-748)"
			<< e._name() << " minor: " << e.minor());
		return CosTransactions::StatusNoTransaction;
	} catch (CORBA::SystemException & e) {
		LOG4CXX_WARN(otsclogger, (char*) "status: " << e._name() << " minor: " << e.minor());
	}

	return CosTransactions::StatusUnknown;
}

int OTSControl::get_status()
{
	FTRACE(otsclogger, "ENTER");

	if (_rbonly)
		return TX_ROLLBACK_ONLY;

	CosTransactions::Status status = get_ots_status();

	switch (status) {
	case CosTransactions::StatusActive:
	case CosTransactions::StatusPreparing:
	case CosTransactions::StatusPrepared:
	case CosTransactions::StatusCommitting:
	case CosTransactions::StatusCommitted:
		return TX_ACTIVE;

	case CosTransactions::StatusRollingBack:
	case CosTransactions::StatusRolledBack:
		return TX_ACTIVE;

	case CosTransactions::StatusMarkedRollback:
		return TX_ROLLBACK_ONLY;
		// there is no way to detect TX_TIMEOUT_ROLLBACK_ONLY

	case CosTransactions::StatusUnknown:
		// only option is to assume its active
		return TX_ACTIVE;

	case CosTransactions::StatusNoTransaction:
		// Since XATMI thinks the txn exists but OTS says it doesn't then
		// it must have been due to a timeout (a rollback would have
		// deleted this OTSControl object)
		return TX_TIMEOUT_ROLLBACK_ONLY;
	default:
		LOG4CXX_DEBUG(otsclogger, (char*) "get_status default: " << status);
		return -1;	// there is no #define for NO TX
	}
}

int OTSControl::get_timeout(CORBA::ULong *timeout)
{
	TX_GUARD(NULL, false);

	try {
		CosTransactions::PropagationContext* context = _ctrl->get_coordinator()->get_txcontext();

		*timeout = context->timeout;
		return TX_OK;
	} catch (CORBA::SystemException & e) {
		LOG4CXX_WARN(otsclogger, (char*) "get timeout: " << e._name() << " minor: " << e.minor());
		return TX_FAIL;
	}
}

/**
 * Return the OTS control for the current transaction.
 * The caller is responsible for decrementing the ref count
 * of the returned pointer.
 */
void* OTSControl::get_control()
{
	FTRACE(otsclogger, "ENTER");
	return (CORBA::is_nil(_ctrl) ? NULL : CosTransactions::Control::_duplicate(_ctrl));
}

bool OTSControl::get_xid(XID& xid)
{
	FTRACE(otsclogger, "ENTER");
//	CosTransactions::Control_ptr cp = (CosTransactions::Control_ptr) txx_get_control();
	bool ok = false;

	if (CORBA::is_nil(_ctrl)) {
		LOG4CXX_WARN(otsclogger,  (char *) "getXID: no tx associated with the callers thread");
		return false;
	}

	LOG4CXX_TRACE(otsclogger,  (char *) "getXID: control: " << _ctrl);

	try {
		CosTransactions::Coordinator_var cv = _ctrl->get_coordinator();
		CosTransactions::PropagationContext_var pcv = cv->get_txcontext();
		CosTransactions::otid_t otid = pcv->current.otid;

		int otidlen = (int) otid.tid.length();
		//char JBOSSTS_NODE_SEPARATOR = '-';
		char *tid, *p; // copy of the ots tid

		p = tid = (char*) malloc(otidlen);

		if (tid == 0) {
			LOG4CXX_WARN(otsclogger, (char*) "Out of memory whilst converting OTS tid");
			return false;
		}

		memset(&xid, 0, sizeof (XID));
		xid.formatID = otid.formatID;

		for (int k = 0; k < otidlen; p++, k++)
			*p = otid.tid[k];

		LOG4CXX_TRACE(otsclogger,  (char *) "converting OTS tid");

		// fingers crossed JBTM-577 has been fixed - do it the OTS way
		LOG4CXX_DEBUG(otsclogger, (char*) "no JBOSS separator in otid - assuming JBTM-577 is fixed");
		xid.bqual_length = otid.bqual_length;
		xid.gtrid_length = otidlen - otid.bqual_length;
		memcpy(xid.data, tid, otidlen);

		free(tid);
		LOG4CXX_TRACE(otsclogger,  (char *) "converted OTS tid len:" << otidlen << (char *) " XID: "
			<< xid.formatID << ':' << xid.gtrid_length << ':' << xid.bqual_length << ':' << xid.data);

		ok = true;
	} catch (CosTransactions::Unavailable & e) {
		LOG4CXX_ERROR(otsclogger,  (char *) "XA-compatible Transaction Service raised unavailable: " << e._name());
	} catch (const CORBA::OBJECT_NOT_EXIST &e) {
		// transaction has most likely timed out
		LOG4CXX_DEBUG(otsclogger,  (char *) "Unexpected exception converting xid: " << e._name());
	} catch  (CORBA::Exception& e) {
		LOG4CXX_ERROR(otsclogger,  (char *) "Unexpected exception converting xid: " << e._name());
	} catch  (...) {
		LOG4CXX_ERROR(otsclogger,  (char *) "Unexpected generic exception converting xid");
	}

//	txx_release_control(cp);

	return ok;
}

/**
 * release the control and remove the tx to thread association
 */
void OTSControl::suspend()
{
	FTRACE(otsclogger, "ENTER");

	if (!CORBA::is_nil(_ctrl)) {
		try {	// c.f. TxManager::tx_suspend
			(void) CORBA::release(_ctrl);
		} catch (CORBA::SystemException & e) {
			LOG4CXX_WARN(otsclogger, (char*) "end: error unref control: " << e._name() << " minor: " << e.minor());
		}
	}

	destroySpecific(TSS_KEY);
	_ctrl = NULL;
}

/**
 * Test whether the OTS control represents a valid transaction
 * If the transaction is expected to be active and its not
 * or vica-versa then log the supplied msg (if its not NULL)
 */
bool OTSControl::isActive(const char *msg, bool expect)
{
	FTRACE(otsclogger, "ENTER");
	bool c = (!CORBA::is_nil(_ctrl));

	if (c != expect && msg) {
		LOG4CXX_WARN(otsclogger, (char*) "protocol violation: " << msg);
	}

	return c;
}

} //	namespace tx
} //namespace atmibroker

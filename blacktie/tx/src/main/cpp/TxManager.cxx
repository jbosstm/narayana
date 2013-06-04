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
#include "ThreadLocalStorage.h"
#include "XAResourceManagerFactory.h"
#include "HttpTxManager.h"
#include "AtmiBrokerEnv.h"
#include "txAvoid.h"
#include "SynchronizableObject.h"

#define TX_GUARD(cond) {	\
	FTRACE(txmlogger, "ENTER"); \
	if (!cond) {  \
		LOG4CXX_WARN(txmlogger, (char*) "protocol error: open: " << _isOpen << " transaction: " << getSpecific(TSS_KEY));   \
		return TX_PROTOCOL_ERROR;   \
	}}

namespace atmibroker {
	namespace tx {

log4cxx::LoggerPtr txmlogger(log4cxx::Logger::getLogger("TxManager"));

TxManager *TxManager::_instance = NULL;
SynchronizableObject globLock;

TxManager *TxManager::get_instance()
{
	FTRACE(txmlogger, "ENTER");

	globLock.lock();
	if (_instance == NULL) {
		AtmiBrokerEnv::get_instance();

		if (txnConfig.mgrEP != NULL) {
			_instance = HttpTxManager::create(txnConfig.mgrEP, txnConfig.resourceEP);
			LOG4CXX_DEBUG(txmlogger,
				(char*) "Using RTS for transaction support with manager endpoint: " <<
				txnConfig.mgrEP << " and participant endpoint: " << txnConfig.resourceEP);
		} else {
			LOG4CXX_FATAL(txmlogger, (char*) "Please make sure TXN_CFG is set in btconfig,xml");
		}
		AtmiBrokerEnv::discard_instance();
	}
	globLock.unlock();

	return _instance;
}

void TxManager::discard_instance()
{
	FTRACE(txmlogger, "ENTER");
	globLock.lock();
	if (_instance != NULL) {
		_instance->dispose();
		delete _instance;
		_instance = NULL;
	}
	globLock.unlock();

}

TxManager::TxManager() : _isOpen(false), _whenReturn(TX_COMMIT_DECISION_LOGGED),
	_controlMode(TX_UNCHAINED), _timeout (0l), _lock(NULL)
{
	FTRACE(txmlogger, "ENTER: " << this);
//	AtmiBrokerEnv::get_instance();
	_lock = new SynchronizableObject();
}

TxManager::~TxManager()
{
	FTRACE(txmlogger, "ENTER");
	dispose();
}

void TxManager::dispose()
{
	FTRACE(txmlogger, "ENTER");
	if (_lock != NULL) {
		(void) close();
//		AtmiBrokerEnv::discard_instance();
		delete _lock;
		_lock = NULL;
	}
}

atmibroker::tx::TxControl *TxManager::currentTx(const char *msg)
{
	FTRACE(txmlogger, "ENTER");
	atmibroker::tx::TxControl *tx = NULL;

	if ((!_isOpen || (tx = (TxControl *) getSpecific(TSS_KEY)) == NULL || !tx->isActive(NULL, true)) && msg) {
		LOG4CXX_INFO(txmlogger, (char*) "protocol violation (" << msg << ") open="
			<< _isOpen << " TSS_KEY=" << getSpecific(TSS_KEY));
	}

	return tx;
}

int TxManager::begin(void)
{
	TX_GUARD((_isOpen && !getSpecific(TSS_KEY)));

	// start a new transaction
	TRANSACTION_TIMEOUT timeout = _timeout;	// take a copy since _timeout can change at any time
	TxControl* tx = create_tx(timeout);

	if (tx == NULL)
		return TX_ERROR;

	// associate the tx with the callers thread and enlist all open RMs with the tx
	int rc = tx_resume(tx, TMNOFLAGS);

	if (rc != XA_OK) {
		tx->rollback();

		delete tx;

		return TX_ERROR;
	}

	LOG4CXX_DEBUG(txmlogger, (char*) "begin: ok");

	return TX_OK;
}

int TxManager::commit(void)
{
	FTRACE(txmlogger, "ENTER");
	return complete(true);
}

int TxManager::rollback(void)
{
	FTRACE(txmlogger, "ENTER");
	return complete(false);
}

int TxManager::rollback_only(void)
{
	TxControl *tx;

	TX_GUARD(((tx = currentTx("rollback_only")) != NULL));

	// inform the local resource managers
	rm_end(TMFAIL);
	return tx->rollback_only();
}

int TxManager::complete(bool commit)
{
	TxControl *tx;
	int outcome;

	TX_GUARD(((tx = currentTx("complete")) != NULL));

	std::map<int, int (*)(int)> &cds = tx->get_cds();

	if (cds.size() != 0) {
		LOG4CXX_WARN(txmlogger,
			(char*) "Ending a tx with outstanding xatmi descriptors is not allowed - rolling back");

		// invalidate the descriptors
		for (std::map<int,  int (*)(int)>::iterator i = cds.begin() ; i != cds.end(); i++) {
			int cd = (*i).first;
			int (*func)(int) = (*i).second;

			 LOG4CXX_DEBUG(txmlogger, (char*) "Invalidating descriptor " << cd);

			if (func != 0) {
				int rv = func(cd);
			 	LOG4CXX_DEBUG(txmlogger, (char*) "Invalidate returned " << rv);
				// NOTE the invalidate function may mark the tx as rollback only
			}
		}

		commit = false;
	}

	// no need to call rm_end since each RM wrapper calls xa_end during the prepare call
	outcome = (commit ? tx->commit(reportHeuristics()) : tx->rollback());

	delete tx;

 	LOG4CXX_DEBUG(txmlogger, (char*) "complete: outcome=" << outcome << " isChained=" << isChained());

	return (isChained() ? chainTransaction(outcome) : outcome);
}

int TxManager::chainTransaction(int outcome)
{
	FTRACE(txmlogger, "ENTER");
	/*
	 * NOTE: outcome will only truly represent the outcome of commit if the commit_return
	 * characteristic is TX_COMMIT_COMPLETED (see method reportHeuristics()).
	 * Using get Coordinator::get_status is ambiguous since NoTransaction can mean the
	 * transaction committed or rolled back and has been forgotten.
	 * TODO in begin register a participant in the transaction so we can definitively know
	 * the transaction outcome.
	 */
	switch(begin()) {
	case TX_OK:
		return TX_OK;
	default:
		switch (outcome) {
		case TX_OK:
			return TX_NO_BEGIN;
		case TX_ROLLBACK:
			return TX_ROLLBACK_NO_BEGIN;
		case TX_MIXED:
			return TX_MIXED_NO_BEGIN;
		case TX_HAZARD:
			return TX_HAZARD_NO_BEGIN;
		default:
			return outcome;
		}
	}
}

int TxManager::set_commit_return(COMMIT_RETURN when_return)
{
	TX_GUARD(_isOpen);

	if (when_return != TX_COMMIT_DECISION_LOGGED &&
		when_return != TX_COMMIT_COMPLETED) {
		LOG4CXX_WARN(txmlogger, (char *) "set_commit_return: invalid arg: " <<
			when_return);
		return TX_EINVAL;
	}

	_whenReturn = when_return;

	return TX_OK;
}

int TxManager::set_transaction_control(TRANSACTION_CONTROL mode)
{
	TX_GUARD(_isOpen);

 	LOG4CXX_TRACE(txmlogger, (char*) "set_transaction_control " << mode);

	if (mode != TX_UNCHAINED && mode != TX_CHAINED) {
		LOG4CXX_WARN(txmlogger, (char *) "set_transaction_control: invalid mode: " << mode);
		return TX_EINVAL;
	}

	_controlMode = mode;

	return TX_OK;
}

int TxManager::set_transaction_timeout(TRANSACTION_TIMEOUT timeout)
{
	TX_GUARD(_isOpen);

	if (timeout < 0l) {
		LOG4CXX_WARN(txmlogger, (char *) "set_transaction_timeout: invalid timeout: " << timeout);
		return TX_EINVAL;
	}

	_timeout = timeout;

	return TX_OK;
}

int TxManager::info(void *info)
{
	TX_GUARD(_isOpen);

	atmibroker::tx::TxControl *tx = currentTx(NULL);

	if (info != 0) {
		long whenReturn = _whenReturn;
		long controlMode = _controlMode;
		long status = -1l;
		long timeout = _timeout;
		if (tx != NULL) {
			XAResourceManagerFactory::getXID(::getXid(info));
			status = tx->get_status();
		}
		::updateInfo(info, whenReturn, controlMode, timeout, status);
	}
	LOG4CXX_DEBUG(txmlogger, (char*) "info tx=" << tx);
	return (tx != NULL ? 1 : 0);
}

int TxManager::open(void)
{
	TX_GUARD((true));
	_lock->lock();
	if (_isOpen) {
		_lock->unlock();
		return TX_OK;
	}

	if (do_open() != TX_OK) {
		_lock->unlock();
		return TX_ERROR;
	}

	if (rm_open() != 0) {
		LOG4CXX_ERROR(txmlogger, (char*) "At least one resource manager failed");
		(void) rm_close();

		_lock->unlock();
		return TX_ERROR;
	}

	// re-initialize values
	_controlMode = TX_UNCHAINED;
	_timeout = 0l;

	_isOpen = true;

	_lock->unlock();
	return TX_OK;
}

int TxManager::close(void)
{
	FTRACE(txmlogger, "ENTER");
	_lock->lock();
	if (!_isOpen) {
		_lock->unlock();
		return TX_OK;
	}

	if (getSpecific(TSS_KEY) != NULL) {
		LOG4CXX_WARN(txmlogger, (char*) "protocol error: open: " << _isOpen << " transaction: " << getSpecific(TSS_KEY));
		_lock->unlock();
		return TX_PROTOCOL_ERROR;
	}

	do_close();
	LOG4CXX_TRACE(txmlogger, (char *) "do_close finished");
	_isOpen = false;
	rm_close();

	LOG4CXX_TRACE(txmlogger, (char *) "rm_close finished");
	_lock->unlock();
	LOG4CXX_TRACE(txmlogger, (char *) "close finished");
	return TX_OK;
}

int TxManager::rm_open(void)
{
	FTRACE(txmlogger, "ENTER");
	try {
		_xaRMFac.createRMs();
		return 0;
	} catch (RMException& ex) {
		LOG4CXX_WARN(txmlogger, (char*) "failed to load RMs: " << ex.what());
		return -1;
	}
}

// private methods
void TxManager::rm_close(void)
{
	FTRACE(txmlogger, "ENTER");
	_xaRMFac.destroyRMs();
}

// pre-requisite:- there is an active transaction
int TxManager::rm_end(int flags, int altflags)
{
	FTRACE(txmlogger, "ENTER: " << std::hex << flags);
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);
	return (tx ? _xaRMFac.endRMs(tx->isOriginator(), flags, altflags) : XA_OK);
}

// pre-requisite:- there is an active transaction
int TxManager::rm_start(int flags, int altflags)
{
	FTRACE(txmlogger, "ENTER: " << std::hex << flags);
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);
	return (tx ? _xaRMFac.startRMs(tx->isOriginator(), flags, altflags) : XA_OK);
}

void* TxManager::get_control(long* ttl)
{
	FTRACE(txmlogger, "ENTER");
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	return (tx ? tx->get_control(ttl) : 0);
}

int TxManager::tx_resume(TxControl *tx, int flags, int altflags)
{
	FTRACE(txmlogger, "ENTER " << tx << " - flags=" << std::hex << flags);
	int rc = XAER_NOTA;
	TxControl *pt = (TxControl *) getSpecific(TSS_KEY);

	if (pt) {
		LOG4CXX_WARN(txmlogger, (char *) "Thread already bound to " << pt << " (deleting it)");
		delete pt;
	}

	try {
		// TMJOIN TMRESUME TMNOFLAGS
		// must associate the tx with the thread before calling start on each open RM
		   setSpecific(TSS_KEY, tx);
		if ((rc = rm_start(flags, altflags)) == XA_OK) {
			LOG4CXX_DEBUG(txmlogger, (char *) "Resume tx: ok");

			return XA_OK;
		} else {
			LOG4CXX_WARN(txmlogger, (char *) "Resume tx: error: " << rc);
		}
	
	} catch (...) {
		LOG4CXX_WARN(txmlogger, (char *) "Resume tx: generic exception");
	}

	destroySpecific(TSS_KEY);

	return rc;
}

/**
 * Suspend the transaction and return the control.
 * The caller is responsible for releasing the returned control
 */
void* TxManager::tx_suspend(int flags, int altflags)
{
	FTRACE(txmlogger, "ENTER");
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx && tx->isActive(NULL, true)) {
		// increment the control reference count
		void* ctrl = tx->get_control(NULL);

		// suspend all open Resource Managers (TMSUSPEND TMMIGRATE TMSUCCESS TMFAIL)
		(void) rm_end(flags, altflags);
		// disassociate the transaction from the callers thread
		tx->suspend();
		delete tx;

		return ctrl;
	}

	FTRACE(txmlogger, "< ctrl: 0x0");
	return NULL;
}

int TxManager::resume()
{
	FTRACE(txmlogger, "ENTER");
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx) {
		return rm_start(TMRESUME);
	}

	return XA_OK;
}

int TxManager::suspend()
{
	FTRACE(txmlogger, "ENTER");
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx) {
		return rm_end(TMSUSPEND | TMMIGRATE);
	}

	return XA_OK;
}

int TxManager::resume(int cd)
{
	FTRACE(txmlogger, "ENTER");
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx) {
		std::map<int, int (*)(int)> &cds = tx->get_cds();
		std::map<int,  int (*)(int)>::iterator i = cds.find(cd);

		if (i != cds.end()) {
			LOG4CXX_DEBUG(txmlogger, (char*) "Removing tp call " << cd << " from tx "
				<< tx << " remaining tpcalls: " << cds.size());
			cds.erase(i);

			LOG4CXX_DEBUG(txmlogger, (char*) "Deleted cd - remaining tpcalls: " << cds.size());
			if (cds.size() == 0) {
				LOG4CXX_DEBUG(txmlogger, (char*) "No more outstanding calls - resume RMs");
				return rm_start(TMRESUME);
			}
		}
	}

	return XA_OK;
}

int TxManager::suspend(int cd, int (*invalidate)(int cd))
{
	FTRACE(txmlogger, "ENTER: " << cd);
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx) {
		std::map<int, int (*)(int)> &cds = tx->get_cds();
		std::map<int, int (*)(int)>::iterator i = cds.find(cd);

		if (i == cds.end()) {
			LOG4CXX_DEBUG(txmlogger, (char*) "Adding tp call " << cd << " to tx " << tx);
			cds[cd] = invalidate;

			if (cds.size() == 1) {
				LOG4CXX_DEBUG(txmlogger, (char*) "First outstanding call - suspending RMs");
				return rm_end(TMSUSPEND | TMMIGRATE);
			}
		}
	}

	return XA_OK;
}

bool TxManager::isCdTransactional(int cd)
{
	FTRACE(txmlogger, "ENTER: " << cd);
	TxControl *tx = (TxControl *) getSpecific(TSS_KEY);

	if (tx) {
		std::map<int, int (*)(int)> &cds = tx->get_cds();
		std::map<int, int (*)(int)>::iterator i = cds.find(cd);
		LOG4CXX_TRACE(txmlogger, (char*) "found=" << (i != cds.end()) << " tx=" << tx << " calls=" << cds.size());
		return (i != cds.end());
	}

	return false;
}

char * TxManager::current_to_string(long* ttl) {
	return TxManager::get_instance()->get_current(ttl);
}

int TxManager::guard(bool cond) {
	TX_GUARD(cond);
	return TX_OK;
}

} //	namespace tx
} //namespace atmibroker

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
#include "log4cxx/logger.h"
#include "ThreadLocalStorage.h"
#include "TxControl.h"
#include "TxManager.h"

#include "ace/OS_NS_time.h"
#include "ace/OS_NS_sys_time.h"

#define TX_GUARD(msg, expect) { \
	FTRACE(txclogger, "ENTER"); \
	if (!isActive(msg, expect)) {   \
		return TX_PROTOCOL_ERROR;   \
	}}

namespace atmibroker {
	namespace tx {

log4cxx::LoggerPtr txclogger(log4cxx::Logger::getLogger("TxControl"));

TxControl::TxControl(long timeout, int tid) : _rbonly(false), _ttl(timeout), _tid(tid) {
	FTRACE(txclogger, "ENTER new TXCONTROL: " << this);

	_ctime = (long) (ACE_OS::gettimeofday().sec());

	if (timeout <= 0l)
		_ttl = -1l;
}

TxControl::~TxControl()
{
	FTRACE(txclogger, "ENTER delete TXCONTROL: " << this);

	if (_cds.size() != 0) {
		LOG4CXX_ERROR(txclogger, (char*) "delete called with outstanding tp calls");
	}
}

int TxControl::end(bool commit, bool reportHeuristics)
{
	TX_GUARD("end", true);

	if (_cds.size() != 0) {
		LOG4CXX_WARN(txclogger, (char*) "protocol error: there are outstanding tp calls");
		return TX_PROTOCOL_ERROR;
	}

	int outcome = do_end(commit, reportHeuristics);

	LOG4CXX_DEBUG(txclogger, (char*) "end: outcome: " << outcome);
	suspend();

	return outcome;
}

int TxControl::commit(bool reportHeuristics)
{
	FTRACE(txclogger, "ENTER report " << reportHeuristics);
	return end(true, reportHeuristics);
}

int TxControl::rollback()
{
	FTRACE(txclogger, "ENTER");
	return end(false, false);
}

/**
 * Return -1 if the txn is subject to timeouts
 * otherwise return the remaining time to live
 */
long TxControl::ttl()
{
	FTRACE(txclogger, "ENTER ttl=" << _ttl << " ctime=" << _ctime << " now=" << ACE_OS::gettimeofday().sec());

	if (_ttl == -1l)
		return -1l;

	long ttl = _ttl - (long) (ACE_OS::gettimeofday().sec()) + _ctime;

	LOG4CXX_TRACE(txclogger, (char*) "> ttl=" << ttl);

	return (ttl <= 0l ? 0l : ttl);
}

void* TxControl::get_control(long* ttlp)
{
	FTRACE(txclogger, "ENTER");

	if (ttlp != NULL)
		*ttlp = ttl();

	return get_control();
}

bool TxControl::isOriginator()
{
	return (_tid != 0);
}

} //	namespace tx
} //namespace atmibroker

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

#include "TxManager.h"
#include "ThreadLocalStorage.h"

using namespace atmibroker::tx;

log4cxx::LoggerPtr txmclogger(log4cxx::Logger::getLogger("TxManagerc"));

/* Blacktie tx interface additions */
int txx_rollback_only() {
	FTRACE(txmclogger, "ENTER");
	return (getSpecific(TSS_KEY) == NULL ? TX_OK : TxManager::get_instance()->rollback_only());
}

void txx_stop(void) {
	FTRACE(txmclogger, "ENTER");
	TxManager::discard_instance();
	FTRACE(txmclogger, "<");
}

int txx_associate_serialized(char* ctrlIOR, long ttl) {
	FTRACE(txmclogger, "ENTER" << ctrlIOR);
	return TxManager::get_instance()->associate_transaction(ctrlIOR, ttl);
}

void *txx_unbind(bool rollback) {
	FTRACE(txmclogger, "ENTER rollback=" << rollback);
	if (getSpecific(TSS_KEY) == NULL)
		return NULL;

	if (rollback)
		(void) TxManager::get_instance()->rollback_only();

	return (void *) TxManager::get_instance()->tx_suspend((TMSUSPEND | TMMIGRATE), TMSUCCESS);
}

void *txx_get_control() {
	FTRACE(txmclogger, "ENTER");
	void *ctrl = TxManager::get_instance()->get_control(NULL);
	FTRACE(txmclogger, "< with control " << ctrl);
	return ctrl;
}

void txx_release_control(void *control) {
	FTRACE(txmclogger, "ENTER");
	TxManager::get_instance()->release_control(control);
}

char* txx_serialize(long* ttl) {
	return TxManager::get_instance()->current_to_string(ttl);
}

int txxx_suspend() {
	FTRACE(txmclogger, "ENTER");
	return TxManager::get_instance()->suspend();
}

int txxx_resume() {
	FTRACE(txmclogger, "ENTER");
	return TxManager::get_instance()->resume();
}

int txx_suspend(int cd, int (*invalidate)(int cd)) {
	FTRACE(txmclogger, "ENTER");
	return TxManager::get_instance()->suspend(cd, invalidate);
}

int txx_resume(int cd) {
	FTRACE(txmclogger, "ENTER");
	return TxManager::get_instance()->resume(cd);
}

bool txx_isCdTransactional(int cd) {
	return TxManager::get_instance()->isCdTransactional(cd);
}

int txx_ttl(long* ttl) {
	TxControl *tx = (TxControl*) getSpecific(TSS_KEY);

	LOG4CXX_TRACE(txmclogger, (char*) "txx_ttl tx=" << tx);

	if (tx == NULL)
		return -1;	/* indicates no txn is bound to the callers thread */

	*ttl = tx->ttl();

	LOG4CXX_TRACE(txmclogger, (char*) "tx->ttl()=" << *ttl);

	if (*ttl < 0l)
		return 1;	/* indicates the txn is not subject to timeouts */

	return 0;	/* indicates that *ttl corresponds to the time left for the txn to complete */
}

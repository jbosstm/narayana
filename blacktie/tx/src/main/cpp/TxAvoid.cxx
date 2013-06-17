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

#include "txAvoid.h"
#include "txManagerAvoid.h"

#include "tx.h"
#include "log4cxx/logger.h"

// Logger for txAvoid
log4cxx::LoggerPtr loggerTxAvoid(log4cxx::Logger::getLogger("TxAvoid"));

void updateInfo(void *infoVoid, long whenReturn, long controlMode, long timeout, long status)
{
	TXINFO* info = (TXINFO*) infoVoid;
	(info->xid).formatID = -1L; // means the XID is null
	info->when_return = whenReturn;
	info->transaction_control = controlMode;
	/*
	 * the timeout that will be used when this process begins the next transaction
	 * (it is not neccessarily the timeout for the current transaction since
	 * this may have been set in another process or it may have been changed by
	 * this process after the current transaction was started).
	 */
	info->transaction_timeout = timeout;
	info->transaction_state = status;
	LOG4CXX_DEBUG(loggerTxAvoid, (char*) "info status=" << info->transaction_state);
}

extern XID& getXid(void *infoVoid) {
	TXINFO* info = (TXINFO*) infoVoid;
	return info->xid;
}

/* X/Open tx interface */
int tx_open(void) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_open: ENTER");
	return txManager_open();
}

int tx_begin(void) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_begin: ENTER");
	return txManager_begin();
}

int tx_commit(void) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_commit: ENTER");
	return txManager_commit();
}

int tx_rollback(void) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_rollback: ENTER");
	return txManager_rollback();
}

int tx_close(void) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_close: ENTER");
	return txManager_close();
}

int tx_set_commit_return(COMMIT_RETURN when_return) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_set_commit_return: ENTER");
	return txManager_set_commit_return(when_return);
}
int tx_set_transaction_control(TRANSACTION_CONTROL control) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_set_transaction_control: ENTER");
	return txManager_set_transaction_control(control);
}
int tx_set_transaction_timeout(TRANSACTION_TIMEOUT timeout) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_set_transaction_timeout: ENTER");
	return txManager_set_transaction_timeout(timeout);
}
int tx_info(TXINFO *info) {
	LOG4CXX_DEBUG(loggerTxAvoid, "tx_info: ENTER");
	return txManager_info(info);
}

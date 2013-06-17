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
#include "txManagerAvoid.h"

int txManager_open(void) {
	return atmibroker::tx::TxManager::get_instance()->open();
}

int txManager_begin(void) {
	return atmibroker::tx::TxManager::get_instance()->begin();
}

int txManager_commit(void) {
	return atmibroker::tx::TxManager::get_instance()->commit();
}

int txManager_rollback(void) {
	return atmibroker::tx::TxManager::get_instance()->rollback();
}

int txManager_close(void) {
	return atmibroker::tx::TxManager::get_instance()->close();
}

int txManager_set_commit_return(long when_return) {
	return atmibroker::tx::TxManager::get_instance()->set_commit_return(when_return);
}
int txManager_set_transaction_control(long control) {
	return atmibroker::tx::TxManager::get_instance()->set_transaction_control(control);
}
int txManager_set_transaction_timeout(long timeout) {
	return atmibroker::tx::TxManager::get_instance()->set_transaction_timeout(timeout);
}
int txManager_info(void *info) {
	return atmibroker::tx::TxManager::get_instance()->info(info);
}

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

#ifndef _OTSTXMANAGER_H
#define _OTSTXMANAGER_H

#include "TxManager.h"
#include "CosTransactionsS.h"

namespace atmibroker {
	namespace tx {

class BLACKTIE_TX_DLL OTSTxManager: public virtual TxManager {
public:
	static TxManager* create(char *transFactoryId);

	int associate_transaction(char* txn, long ttl);
	void release_control(void *);
	bool recover(XAWrapper*);
	virtual bool isOTS() {return true;}

protected:
	OTSTxManager(char *transFactoryId);
	virtual ~OTSTxManager();

	char *get_current(long* ttl);
	TxControl* create_tx(TRANSACTION_TIMEOUT timeout);
	int do_open(void);
	int do_close(void);

private:
	int tx_resume(CosTransactions::Control_ptr control, long ttl, int flags, int altflag = -1);
//    CosTransactions::Control_ptr tx_suspend(TxControl *, int flags, int altflags = -1);

	CosTransactions::TransactionFactory_var _txfac;

	int open_trans_factory(void);

	char *_transFactoryId;
};
}	//	namespace tx
}	//namespace atmibroker

#endif // _TXMANAGER_H

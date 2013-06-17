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
#ifndef _OTS_CONTROL_H
#define _OTS_CONTROL_H

#include "TxControl.h"
#include "CosTransactionsS.h"

namespace atmibroker {
	namespace tx {

/**
 * An object that gets associated with a thread when there
 * is an active transaction.
 */
class BLACKTIE_TX_DLL OTSControl : public virtual TxControl {
public:
	OTSControl(CosTransactions::Control_ptr ctrl, long timeout, int tid);
	virtual ~OTSControl();

	int rollback_only();
    int get_status();
	bool get_xid(XID& xid);
	bool isOTS() {return true;}

protected:
	void suspend();
    int do_end(bool commit, bool reportHeuristics);
    void* get_control();
	bool isActive(const char *msg, bool expect);

private:
	CosTransactions::Status get_ots_status();
	int get_timeout(CORBA::ULong *timeout);

	CosTransactions::Control_ptr _ctrl;
};
} //	namespace tx
} //namespace atmibroker
#endif	// _OTS_CONTROL_H

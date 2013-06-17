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
#ifndef _XARESOURCEADAPTORIMPL_H
#define _XARESOURCEADAPTORIMPL_H

#include <tao/PortableServer/PortableServer.h>
#include "CosTransactionsS.h"
#include "XAWrapper.h"

using namespace CosTransactions;

class BLACKTIE_TX_DLL XAResourceAdaptorImpl :
	public XAWrapper,
	public virtual POA_CosTransactions::Resource,
	public virtual PortableServer::RefCountServantBase
{
public:
	XAResourceAdaptorImpl(XABranchNotification* rm, XID& bid, long rmid,
		struct xa_switch_t * xa_switch, XARecoveryLog& log, const char *rc = NULL);
	virtual ~XAResourceAdaptorImpl();

	// OTS resource methods
	Vote prepare() throw (HeuristicMixed,HeuristicHazard);
	void rollback() throw(HeuristicCommit,HeuristicMixed,HeuristicHazard);
	void commit() throw(NotPrepared,HeuristicRollback,HeuristicMixed,HeuristicHazard);
	void commit_one_phase() throw(HeuristicHazard);
	void forget();
	bool isOTS() {return true;}

private:
	void terminate(int) throw(
		HeuristicRollback,
		HeuristicMixed,
		HeuristicHazard);
};
#endif // _XARESOURCEADAPTORIMPL_H

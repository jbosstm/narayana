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
#ifndef _XARESOURCEMANAGER_H
#define _XARESOURCEMANAGER_H

#include "XAWrapper.h"
#include "SynchronizableObject.h"

class XAResourceAdaptorImpl;

class xid_cmp
{   
public: 
	bool operator()(XID const& xid1, XID const& xid2) const;
};

bool operator< (XID const& xid1, XID const& xid2);

class BLACKTIE_TX_DLL XAResourceManager : public virtual XABranchNotification
{
public:
	XAResourceManager(const char *, const char *, const char *,
		long, long, struct xa_switch_t *, XARecoveryLog& log) throw (RMException);
	virtual ~XAResourceManager();

	int xa_start (XID *, long);
	int xa_end (XID *, long);
	bool recover(XID& xid, const char* rc);	// recover a single XID (on a potentially remote RM)
	int recover();	// initiate a recovery scan on this RM

	void deactivate_objects(bool deactivate); //TODO #ifdef TEST

	// return the resource id
	long rmid(void) {return rmid_;};
	//void notify_error(XID *, int, bool);
	//void set_complete(XID*);
	void notify_error(XAWrapper*, int, bool);
	void set_complete(XAWrapper*);
	const char * name() {return name_;}
	int xa_flags();

	struct xa_switch_t * get_xa_switch() { return xa_switch_;}
	static XID gen_xid(long rmid, long sid, XID &gid);

private:
	typedef std::map<XID, XAWrapper *> XABranchMap;
	XABranchMap branches_;
	SynchronizableObject* branchLock;

	const char *name_;
	const char *openString_;
	const char *closeString_;
	long rmid_;
	long sid_;
	struct xa_switch_t * xa_switch_;
	XARecoveryLog& rclog_;

	void createPOA();
	int createServant(XID &);
	int createResourceAdapter(XID &);
	XAWrapper * locateBranch(XID *);

	void show_branches(const char *, XID *);
	bool isRecoverable(XID &xid);

	int  getRRType(const char* rc);
	bool recoverRTS(XID& bid, const char* rc);
	bool recoverIOR(XID& bid, const char* rc);


	static SynchronizableObject* lock;
	static long counter;
};
#endif // _XARESOURCEMANAGER_H

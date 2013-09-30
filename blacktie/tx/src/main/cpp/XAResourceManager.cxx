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
#include <string.h>
#include <ostream>
#include <stdlib.h>

#include "XAResourceManager.h"
#include "XAWrapper.h"
#include "TxManager.h"
#include "ThreadLocalStorage.h"
#include "AtmiBrokerEnv.h"
#include "xa.h"

log4cxx::LoggerPtr xarmlogger(log4cxx::Logger::getLogger("XAResourceManager"));

static const int RR_TYPE_UNK = 0;
static const int RR_TYPE_IOR = 1;
static const int RR_TYPE_RTS = 2;
static const char HTTP_PREFIX[] = "http://";

SynchronizableObject* XAResourceManager::lock = new SynchronizableObject();
long XAResourceManager::counter = 0l;

std::ostream& operator<<(std::ostream &os, const XID& xid)
{
	os << xid.formatID << ':' << xid.gtrid_length << ':' << xid.bqual_length << ':' << xid.data;
	return os;
}

void XAResourceManager::show_branches(const char *msg, XID * xid)
{
	FTRACE(xarmlogger, "ENTER " << *xid);

	branchLock->lock();
	for (XABranchMap::iterator i = branches_.begin(); i != branches_.end(); ++i) {
		LOG4CXX_TRACE(xarmlogger, (char *) "XID: " << (i->first)); 
	}
	branchLock->unlock();
}

static int compareXids(const XID& xid1, const XID& xid2)
{
	char *x1 = (char *) &xid1;
	char *x2 = (char *) &xid2;
	char *e = (char *) (x1 + sizeof (XID));

	while (x1 < e)
		if (*x1 < *x2)
			return -1;
		else if (*x1++ > *x2++)
			return 1;

	return 0;
}

bool xid_cmp::operator()(XID const& xid1, XID const& xid2) const {
	return (compareXids(xid1, xid2) < 0);
}

bool operator<(XID const& xid1, XID const& xid2) {
	return (compareXids(xid1, xid2) < 0);
}

XAResourceManager::XAResourceManager(
	const char * name,
	const char * openString,
	const char * closeString,
	long rmid,
	long sid,
	struct xa_switch_t * xa_switch,
	XARecoveryLog& log) throw (RMException) :

	name_(name), openString_(openString), closeString_(closeString),
	rmid_(rmid), sid_(sid), xa_switch_(xa_switch), rclog_(log) {

	FTRACE(xarmlogger, "ENTER " << (char *) "new RM name: " << name << (char *) " openinfo: " <<
		openString << (char *) " rmid: " << rmid_);

	if (name == NULL) {
		RMException ex("Invalid RM name", EINVAL);
		throw ex;
	}

	int rv = xa_switch_->xa_open_entry((char *) openString, rmid_, TMNOFLAGS);

	LOG4CXX_TRACE(xarmlogger,  (char *) "xa_open: " << rv);

	if (rv != XA_OK) {
		LOG4CXX_ERROR(xarmlogger,  (char *) "xa_open error: " << rv);

		RMException ex("xa_open", rv);
		throw ex;
	}

	branchLock = new SynchronizableObject();
	// each RM has its own POA
//	createPOA();
}

XAResourceManager::~XAResourceManager() {
	FTRACE(xarmlogger, "ENTER");

	XABranchMap::iterator iter;
	for (iter = branches_.begin(); iter != branches_.end(); ++iter) {
		XAWrapper* xaw = (*iter).second;

		// When a txn completes each resource calls XAResourceManager::set_complete
		// which should remove (and delete) the resource from branches_
		LOG4CXX_WARN(xarmlogger, (char *) "There are still incomplete branches");

		LOG4CXX_WARN(xarmlogger, (char *) "Deleting branch " << (*iter).first <<
			" address: " << xaw);
		delete xaw;
	}

	int rv = xa_switch_->xa_close_entry((char *) closeString_, rmid_, TMNOFLAGS);

	LOG4CXX_TRACE(xarmlogger, (char *) "xa_close: " << rv);

	if (rv != XA_OK)
		LOG4CXX_WARN(xarmlogger, (char *) " close RM " << name_ << " failed: " << rv);

	delete branchLock;
}

int  XAResourceManager::getRRType(const char* rc)
{
	LOG4CXX_TRACE(xarmlogger, (char *) "getRRType: " << rc);

	if (strncmp(HTTP_PREFIX, rc, sizeof (HTTP_PREFIX) - 1) == 0)
		return RR_TYPE_RTS;

	return RR_TYPE_IOR;
}

/**
 * replay branch completion on an XID that needs recovering.
 * The rc parameter is the CORBA object reference of the
 * Recovery Coordinator for the the branch represented by the XID
 *
 * return bool true if it is OK for the caller to delete the associated recovery record
 */
bool XAResourceManager::recover(XID& bid, const char* rc)
{
	switch (getRRType(rc)) {
	case RR_TYPE_RTS:
		return recoverRTS(bid, rc);
	default:
		return false;
	}
}

bool XAResourceManager::recoverRTS(XID& bid, const char* rc)
{
	XAWrapper *ra = new XAWrapper(this, bid, rmid_, xa_switch_, rclog_, rc);

	LOG4CXX_TRACE(xarmlogger, (char *) "trying to recover branch: " << bid);

	branchLock->lock();
	branches_[bid] = ra;
	branchLock->unlock();


	// TODO have we caught all race conditions - recover can cause ra to call back
	// here via XAResourceManager::set_complete
	// Note if the TM does not know about this participant then
	// the next recover scan (XAResourceManager::recover()) will roll it back.
	if (atmibroker::tx::TxManager::get_instance()->recover(ra)) {
		LOG4CXX_TRACE(xarmlogger, (char *) "branch recovered: " << bid);

		branchLock->lock();
		branches_.erase(bid);
		branchLock->unlock();
		delete ra;

		return true;
	}

	return false;
}

/**
 * check whether it is OK to recover a given XID
 */
bool XAResourceManager::isRecoverable(XID &xid)
{
	/*
	 * if the XID is one of ours it will encode the RM id and the server id
	 * in the first two longs of the XID data (see XAResourceManager::gen_xid)
	 */
	char *bdata = (char *) (xid.data + xid.gtrid_length);
	char *sp = strchr(bdata, ':');
	long rmid = atol(bdata);	// the RM id
	long sid = (sp == 0 || ++sp == 0 ? 0l : atol(sp));	// the server id

	/*
	 * Only recover our own XIDs - the reason we need to check the server id is to
	 * avoid the possibility of a server rolling back another servers active XID
	 *
	 * Note that this means that a recovery log can only be processed by server
	 * with the correct id.
	 *
	 * The user can override this behaviour, so that any server or client can recover any log,
	 * via an environment variable:
	 */

	if (AtmiBrokerEnv::get_instance()->getenv((char*) "BLACKTIE_RC_LOG_NAME", NULL) != NULL)
		sid = sid_;

	AtmiBrokerEnv::discard_instance();

	if (rmid == rmid_ && sid == sid_) {
		/*
		 * If this XID does not appear in the recovery log then the server must have failed
		 * after calling prepare on the RM but before writing the recovery log in which case
		 * it is OK to recover the XID
		 */
		if (rclog_.find_ior(xid) == 0)
			return true;
	}

	return false;
}

/**
 * Initiate a recovery scan on the RM looking for prepared or heuristically completed branches
 * This functionality covers the following failure scenario:
 * - server calls prepare on a RM
 * - RM prepares but the the server fails before it can write to its transaction recovery log
 * In this case the RM will have a pending transaction branch which does not appear in
 * the recovery log. Calling xa_recover on the RM will return the 'missing' XID which the
 * recovery scan can rollback.
 */
int XAResourceManager::recover()
{
	XID xids[10];
	long count = sizeof (xids) / sizeof (XID);
	long flags = TMSTARTRSCAN;	// position the cursor at the start of the list
	int i, nxids;

	do {
		// ask the RM for all XIDs that need recovering
		nxids = xa_switch_->xa_recover_entry(xids, count, rmid_, flags);

		flags = TMNOFLAGS;	// on the next call continue the scan from the current cursor position

		for (i = 0; i < nxids; i++) {
			// check whether this id needs rolling back
			if (isRecoverable(xids[i])) {
				int rv = xa_switch_->xa_rollback_entry((XID *) (xids + i), rmid_, TMNOFLAGS);

				LOG4CXX_INFO(xarmlogger, (char*) "Recovery of xid " << xids[i] << " for RM " << rmid_ <<
					 " returned XA status " << rv);
			}
		}
	} while (count == nxids);

	return 0;
}

int XAResourceManager::createResourceAdapter(XID& xid)
{
	atmibroker::tx::TxControl *tx = (atmibroker::tx::TxControl *) getSpecific(TSS_KEY);

	if (tx == NULL)
		return XAER_PROTO;

	XID bid = gen_xid(rmid_, sid_, xid);
	XAWrapper *ra = new XAWrapper(this, bid, rmid_, xa_switch_, rclog_);

	char * recUrl = atmibroker::tx::TxManager::get_instance()->enlist(ra, tx, ra->get_name());

	if (recUrl == NULL) {
		LOG4CXX_WARN(xarmlogger, (char*) "resource enlistment failed: no recovery URL");
		delete ra;
		return XAER_PROTO;
	} else {
		ra->set_recovery_coordinator(recUrl);

		LOG4CXX_TRACE(xarmlogger, (char*) "adding branch: " << xid);

		branchLock->lock();
		branches_[xid] = ra;
		branchLock->unlock();

		return XA_OK;
	}
}

void XAResourceManager::notify_error(XAWrapper* resource, int xa_error, bool forget)
{
	FTRACE(xarmlogger, "ENTER: reason: " << xa_error);

	if (forget)
		set_complete(resource);
}

void XAResourceManager::set_complete(XAWrapper* resource)
{
	FTRACE(xarmlogger, "ENTER");

	LOG4CXX_TRACE(xarmlogger, (char*) "looking for branch: " << resource);

	branchLock->lock();
	for (XABranchMap::iterator i = branches_.begin(); i != branches_.end(); ++i)
	{
		XAWrapper *ra = i->second;

		LOG4CXX_TRACE(xarmlogger, (char*) "comparing with " << ra);

		if (ra == resource) {
			LOG4CXX_TRACE(xarmlogger, (char*) "removing branch");
			delete ra;

			XID xid = i->first;
			if (rclog_.del_rec(xid) != 0) {
				// probably a readonly resource
				LOG4CXX_TRACE(xarmlogger,
					(char*) "branch completion notification without a corresponding log entry: " <<
					i->first);
			}

			branches_.erase(i);
			break;
		}
	}
	branchLock->unlock();
	LOG4CXX_TRACE(xarmlogger, (char*) "... unknown branch");
}

int XAResourceManager::xa_start (XID * xid, long flags)
{
	FTRACE(xarmlogger, "ENTER " << rmid_ << (char *) ": flags=" << std::hex << flags << " lookup XID: " << *xid);
	XAWrapper * resource = locateBranch(xid);
	int rv;

	if (resource == NULL) {
		FTRACE(xarmlogger, "creating branch " << *xid);
		if ((rv = createResourceAdapter(*xid)) != XA_OK)
			return rv;

		if ((resource = locateBranch(xid)) == NULL)	// cannot be NULL
			return XAER_RMERR;

		FTRACE(xarmlogger, "starting branch " << *xid);
		return resource->xa_start(TMNOFLAGS);
	}

	FTRACE(xarmlogger, "existing branch " << *xid);
	return resource->xa_start(TMRESUME);
}

int XAResourceManager::xa_end (XID * xid, long flags)
{
	FTRACE(xarmlogger, "ENTER end branch " << *xid << " rmid=" << rmid_ << " flags=" << std::hex << flags);
	XAWrapper * resource = locateBranch(xid);

	if (resource == NULL) {
		LOG4CXX_WARN(xarmlogger, (char *) " no such branch " << *xid);
		return XAER_NOTA;
	}

	return resource->xa_end(flags);
}

XAWrapper * XAResourceManager::locateBranch(XID * xid)
{
	FTRACE(xarmlogger, "ENTER");
	XABranchMap::iterator iter;

	branchLock->lock();
	for (iter = branches_.begin(); iter != branches_.end(); ++iter) {
		LOG4CXX_TRACE(xarmlogger, (char *) "compare: " << *xid << " with " << (iter->first));

		if (compareXids(iter->first, (const XID&) (*xid)) == 0) {
			branchLock->unlock();
			return (*iter).second;
		}
	}
	branchLock->unlock();

	LOG4CXX_DEBUG(xarmlogger, (char *) " branch not found");
	return NULL;
}

void XAResourceManager::deactivate_objects(bool deactivate)
{
	FTRACE(xarmlogger, "ENTER");

	branchLock->lock();
	for (XABranchMap::iterator i = branches_.begin(); i != branches_.end(); ++i)
	{
		XAWrapper *ra = i->second;

		LOG4CXX_TRACE(xarmlogger, (char*) "removing branch " << ra->get_name());

//		branches_.erase(i);
	}
	branchLock->unlock();
}


int XAResourceManager::xa_flags()
{
	return xa_switch_->flags;
}

/**
 * Generate a new XID. The xid should be unique within the currently
 * running process. Uniqueness is assured by including
 *
 * - the global transaction identifier (gid)
 * - the server id (sid)
 * - a counter
 * - the current time
 */
XID XAResourceManager::gen_xid(long id, long sid, XID &gid)
{
	FTRACE(xarmlogger, "ENTER");
	XID xid = {gid.formatID, gid.gtrid_length};
	int i;
	long myCounter = -1l;

	lock->lock();
	myCounter = ++counter;
	lock->unlock();

	for (i = 0; i < gid.gtrid_length; i++)
		xid.data[i] = gid.data[i];

        apr_time_exp_t now;
        apr_time_exp_gmt(&now, apr_time_now());

	/*
	 * The first two longs in the XID data should contain the RM id and server id respectively.
	 */
	(void) sprintf(xid.data + i, "%ld:%ld:%ld:%ld:%ld", id, sid, myCounter, (long) now.tm_sec, (long) now.tm_usec);
	xid.bqual_length = strlen(xid.data + i);

	FTRACE(xarmlogger, "Leaving with XID: " << xid);
	return xid;
}

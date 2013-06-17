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
#include "XAResourceManagerFactory.h"
#include "ThreadLocalStorage.h"
#include "SymbolLoader.h"
#include "AtmiBrokerEnv.h"
#include "TxControl.h"

#include "ace/DLL.h"
#include "ace/ACE.h"
#ifdef ACE_HAS_POSITION_INDEPENDENT_POINTERS
#include "ace/Based_Pointer_Repository.h"
#endif /* ACE_HAS_POSITION_INDEPENDENT_POINTERS */
#include "ace/Malloc_T.h"
#include "ace/MMAP_Memory_Pool.h"
#include "ace/PI_Malloc.h"
#include "ace/Null_Mutex.h"
#include "ace/Based_Pointer_T.h"

log4cxx::LoggerPtr xarflogger(log4cxx::Logger::getLogger("TxXAResourceManagerFactory"));

extern std::ostream& operator<<(std::ostream &os, const XID& xid);

/**
 * Convert an OTS tid (using the currently associated OTS transaction) into an XA XID:
 * - the gtrid (global transaction id) is provided by the first bytes in tid
 *   and the following bqual_length bytes correspond to the bqual (branch qualifier)
 *   part of the XID
 *
 * Only the gtrid portion is of interest since BlackTie creates its own XIDs for
 * driving RMs (but the gtrid must match the one the transaction manager is using).
 * Refer to the method XAResourceManager::gen_xid for how the branch qualifier
 * portion of the XID is generated.
 *
 * Refer to sections 4.2 and 7.3 of the XA spec and appendix B.2.2 of the OTS spec
 * for details
 */
bool XAResourceManagerFactory::getXID(XID& xid)
{
	FTRACE(xarflogger, "ENTER");
	atmibroker::tx::TxControl *tx = (atmibroker::tx::TxControl *) getSpecific(TSS_KEY);

	if (tx == NULL) {
		LOG4CXX_WARN(xarflogger,  (char *) "getXID: no tx associated with the callers thread");
		return false;
	}

	return tx->get_xid(xid);
}

static int _rm_start(XAResourceManager* rm, XID& xid, long flags)
{
	FTRACE(xarflogger, "ENTER");
	return rm->xa_start(&xid, flags);
}
static int _rm_end(XAResourceManager* rm, XID& xid, long flags)
{
	FTRACE(xarflogger, "ENTER");
	return rm->xa_end(&xid, flags);
}

static int _rmiter(ResourceManagerMap& rms, int (*func)(XAResourceManager *, XID&, long),
	bool isOriginator, int flags, int altflags)
{
	FTRACE(xarflogger, "ENTER: flags=0x" << std::hex << flags << " tx owner=" << isOriginator);
	XID xid;

	if (!XAResourceManagerFactory::getXID(xid)) {
		LOG4CXX_TRACE(xarflogger,  (char *) "No tx ... returning");
		return XAER_NOTA;
	}

	for (ResourceManagerMap::iterator i = rms.begin(); i != rms.end(); ++i) {
		XAResourceManager * rm = i->second;
		int rc;

		LOG4CXX_TRACE(xarflogger,  (char *) rm->name() << ": xa flags=0x" << std::hex << rm->xa_flags());
		rc = func(rm, xid, (rm->xa_flags() & TMNOMIGRATE) && altflags != -1 ? altflags : flags);

		if (rc != XA_OK) {
			LOG4CXX_DEBUG(xarflogger,  (char *) rm->name() << ": rm operation failed");
			return rc;
		}
		LOG4CXX_TRACE(xarflogger,  rm->name() << ": rm operation ok");
	}

	return XA_OK;
}

XAResourceManagerFactory::XAResourceManagerFactory() : poa_(0)
{
	FTRACE(xarflogger, "ENTER");
/*
	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
	const char* serverId = env->getenv("BLACKTIE_SERVER_ID", "0");
	const char* poaname = env->getenv("BLACKTIE_SERVER_NAME", "ATMI_RM_POA");
	xarmp
	AtmiBrokerEnv::discard_instance();
*/
}

XAResourceManagerFactory::~XAResourceManagerFactory()
{
	FTRACE(xarflogger, "ENTER");
	destroyRMs();

	if (!CORBA::is_nil(poa_)) {
		CORBA::release(poa_);
		poa_ = NULL;
	}
}

XAResourceManager * XAResourceManagerFactory::findRM(long id)
{
	FTRACE(xarflogger, "ENTER");
	ResourceManagerMap::iterator i = rms_.find(id);

	return (i == rms_.end() ? NULL : i->second);
}

void XAResourceManagerFactory::destroyRMs()
{
	FTRACE(xarflogger, "ENTER");
	for (ResourceManagerMap::iterator i = rms_.begin(); i != rms_.end(); ++i)
		delete i->second;

	rms_.clear();
}

int XAResourceManagerFactory::startRMs(bool isOriginator, int flags, int altflags)
{
	FTRACE(xarflogger, "ENTER");
	LOG4CXX_DEBUG(xarflogger, (char *) " starting RMs flags=0x" << std::hex << flags);
	// there is a current transaction (otherwise the call doesn't need to start the RMs
	return _rmiter(rms_, _rm_start, isOriginator, flags, altflags);
}
int XAResourceManagerFactory::endRMs(bool isOriginator, int flags, int altflags)
{
	FTRACE(xarflogger, "ENTER");
	LOG4CXX_DEBUG(xarflogger,  (char *) "end RMs flags=0x" << std::hex << flags);
	return _rmiter(rms_, _rm_end, isOriginator, flags, altflags);
}

/**
 * See if there are any transaction branches in need of revovery. This call is performed
 * once at startup so there should be no transactions created during the recovery scan.
 */
void XAResourceManagerFactory::run_recovery()
{
	FTRACE(xarflogger, "ENTER");

	/*
	 * If the TM failed before updating its recovery log then there may RMs with pending
	 * branches. Ask each registered RM to return all pending XIDs and if any belong to
	 * the TM but weren't in the recovery log then rollback the branch:
	 */
	for (ResourceManagerMap::iterator i = rms_.begin(); i != rms_.end(); ++i)
		i->second->recover();

	/*
	 * iterate through the recovery log and try to recover each branch
	 */
	for (rrec_t* rrp = rclog_.find_next(0); rrp; rrp = rclog_.find_next(rrp)) {
		// the first long in the XID data contains the RM id
		long rmid = ACE_OS::atol((char *) ((rrp->xid).data + (rrp->xid).gtrid_length));
		XAResourceManager *rm = findRM(rmid);

		if (rm != NULL) {
			if (rm->recover(rrp->xid, rclog_.get_ior(*rrp)))
				rclog_.del_rec(rrp->xid);
		} else {
			LOG4CXX_DEBUG(xarflogger,  (char *) "recover_branches rm " << rmid << " not found");
		}
	}
}

void XAResourceManagerFactory::createRMs(CORBA_CONNECTION * connection) throw (RMException)
{
	FTRACE(xarflogger, "ENTER rmsize: " << rms_.size());

	if (!rclog_.isOpen())
		throw new RMException("Could not load recovery log", EINVAL);

	if (connection != NULL && CORBA::is_nil(poa_))
		create_poa(connection);

	if (rms_.size() == 0) {
		AtmiBrokerEnv::get_instance();

		FTRACE(xarflogger, "ENTER rmsize: " << rms_.size() << "xarmp: " << xarmp);
		xarm_config_t * rmp = (xarmp == 0 ? 0 : xarmp->head);

		LOG4CXX_DEBUG(xarflogger, (char *) "rmp: " << rmp);

		while (rmp != 0) {
			LOG4CXX_TRACE(xarflogger,  (char*) "createRM:"
				<< (char *) " xaResourceMgrId: " << rmp->resourceMgrId
				<< (char *) " xaResourceName: " << rmp->resourceName
				<< (char *) " xaOpenString: " << rmp->openString
				<< (char *) " xaCloseString: " << rmp->closeString
				<< (char *) " xaSwitch: " << rmp->xasw
				<< (char *) " xaLibName: " << rmp->xalib
			);

			(void) createRM(connection, rmp);

			rmp = rmp->next;
		}

		AtmiBrokerEnv::discard_instance();
	}

	run_recovery();
}

/**
 * Create a Resource Manager proxy for a XA compliant RM.
 * RMs must have a unique rmid.
 * A separate POA is created for each RM whose name is
 * derived from the unique rmid. The POA is responsible for
 * generating servants that correspond to each transaction branch
 * (a branch is created when start on the RM is called).
 */
XAResourceManager * XAResourceManagerFactory::createRM(
	CORBA_CONNECTION * connection,
	xarm_config_t *rmp)
	throw (RMException)
{
	FTRACE(xarflogger, "ENTER");
	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
	const char* serverId = env->getenv("BLACKTIE_SERVER_ID", "0");
	AtmiBrokerEnv::discard_instance();

	// make sure the XA_RESOURCE XML config is valid
	if (rmp->resourceMgrId == 0 || rmp->xasw == NULL || rmp->xalib == NULL) {
		LOG4CXX_DEBUG(xarflogger, 
			(char *) "Bad XA_RESOURCE config: "
			<< " rmid: " << rmp->resourceMgrId
			<< " xaswitch symbol: " << rmp->xasw
			<< " xa lib name: " << rmp->xalib);

		//destroyRMs(NULL);
		RMException ex = RMException("Invalid XA_RESOURCE XML config", EINVAL);
		throw ex;
	}

	// Check that rmid is unique
	XAResourceManager * id = findRM(rmp->resourceMgrId);

	if (id != 0) {
		LOG4CXX_INFO(xarflogger, 
			(char *) "Duplicate RM with id " << rmp->resourceMgrId);

		RMException ex("RMs must have unique ids", EINVAL);
		throw ex;
	}

	void * symbol = lookup_symbol(rmp->xalib, rmp->xasw);
	ptrdiff_t tmp = reinterpret_cast<ptrdiff_t> (symbol);
	struct xa_switch_t * xa_switch = reinterpret_cast<struct xa_switch_t *>(tmp);

	if (xa_switch == NULL) {
		LOG4CXX_ERROR(xarflogger, 
			(char *) " xa_switch " << rmp->xasw << (char *) " not found in library " << rmp->xalib);
		RMException ex("Could not find xa_switch in library", 0);
		throw ex;
	}

	LOG4CXX_TRACE(xarflogger,  (char *) "creating xa rm: " << xa_switch->name);
	XAResourceManager * a = new XAResourceManager(
		connection, rmp->resourceName, rmp->openString, rmp->closeString, rmp->resourceMgrId, ACE_OS::atol((char *) serverId),
		xa_switch, rclog_, poa_);

	LOG4CXX_TRACE(xarflogger,  (char *) "created xarm");

	if (a != NULL)
		rms_[rmp->resourceMgrId] = a;
	
	return a;
}

// All resource managers share the same POA.
void XAResourceManagerFactory::create_poa(CORBA_CONNECTION * connection) throw (RMException) {
	FTRACE(xarflogger, "ENTER");

	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
	const char* poaname = env->getenv("BLACKTIE_SERVER_NAME", "ATMI_RM_POA");
	AtmiBrokerEnv::discard_instance();
	PortableServer::POAManager_ptr poa_manager = (PortableServer::POAManager_ptr) connection->root_poa_manager;
	PortableServer::POA_ptr parent_poa = (PortableServer::POA_ptr) connection->root_poa;
	PortableServer::LifespanPolicy_var p1 = parent_poa->create_lifespan_policy(PortableServer::PERSISTENT);
	PortableServer::IdAssignmentPolicy_var p2 = parent_poa->create_id_assignment_policy(PortableServer::USER_ID);

	CORBA::PolicyList policies;
	policies.length(2); // set number of policies to 1 to disable USER_ID policy

	// the servant object references must survive failure of the ORB in order to support recover of 
	// transaction branches (the default orb policy for servants is transient)
	policies[0] = PortableServer::LifespanPolicy::_duplicate(p1);
	policies[1] = PortableServer::IdAssignmentPolicy::_duplicate(p2);

	// create a the POA
	try {
		this->poa_ = parent_poa->create_POA(poaname, poa_manager, policies);
		p1->destroy(); p2->destroy();
	} catch (PortableServer::POA::AdapterAlreadyExists &) {
		p1->destroy(); p2->destroy();
		try {
			this->poa_ = parent_poa->find_POA(poaname, false);
		} catch (const PortableServer::POA::AdapterNonExistent &) {
			LOG4CXX_ERROR(xarflogger, (char *) "Duplicate RM POA with name " << poaname <<
				" (check that the server was started with a unique name using the -n <name> flag)");
			RMException ex("Duplicate RM POA", EINVAL);
			throw ex;
		}
	} catch (PortableServer::POA::InvalidPolicy &) {
		p1->destroy(); p2->destroy();
		LOG4CXX_WARN(xarflogger, (char *) "Invalid RM POA policy");
		RMException ex("Invalid RM POA policy", EINVAL);
		throw ex;
	}

	// take the POA out of its holding state
	LOG4CXX_TRACE(xarflogger,  (char *) "activating RM POA");

	PortableServer::POAManager_var mgr = this->poa_->the_POAManager();
	mgr->activate();

	FTRACE(xarflogger, ">");
}

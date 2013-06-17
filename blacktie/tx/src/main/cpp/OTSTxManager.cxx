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
#include "OTSTxManager.h"

#include "ThreadLocalStorage.h"
#include "XAResourceManagerFactory.h"
#include "OrbManagement.h"
#include "OTSControl.h"
#include "OTSTxManager.h"
#include "HttpTxManager.h"
#include "AtmiBrokerEnv.h"
#include "ace/Thread.h"
#include "txAvoid.h"
#include "SynchronizableObject.h"


namespace atmibroker {
	namespace tx {

log4cxx::LoggerPtr otstxlogger(log4cxx::Logger::getLogger("TxOTSTxManager"));

OTSTxManager::OTSTxManager(char *transFactoryId) : TxManager() {
	try {
		_connection = ::initOrb((char*) "ots");
		LOG4CXX_DEBUG(otstxlogger, (char*) "new CONNECTION: " << _connection);
	} catch (CORBA::SystemException & e) {
		LOG4CXX_WARN(otstxlogger, (char*) "Failed to connect to ORB for TM: " << e._name() << " minor code: " << e.minor());
	} catch (...) {
		LOG4CXX_WARN(otstxlogger, (char*) "Unknown error looking up ORB for TM");
	}
	_transFactoryId = strdup(transFactoryId);
}

OTSTxManager::~OTSTxManager() {
	if (_transFactoryId)
		free(_transFactoryId);
	LOG4CXX_DEBUG(otstxlogger, (char*) "deleting CONNECTION: " << _connection);
	shutdownBindings(_connection);
}

TxManager* OTSTxManager::create(char *transFactoryId) {
	return (_instance ? _instance : new OTSTxManager(transFactoryId));
}

int OTSTxManager::associate_transaction(char* txn, long ttl) {
	FTRACE(otstxlogger, "ENTER" << txn);

	CORBA::Object_ptr p = _connection->orbRef->string_to_object(txn);

	LOG4CXX_DEBUG(otstxlogger, (char*) "tx_resume IOR=" << txn << " ptr=" << p);

	if (!CORBA::is_nil(p)) {
		CosTransactions::Control_ptr cptr =
				CosTransactions::Control::_narrow(p);
		CORBA::release(p); // dispose of it now that we have narrowed the object reference

		return tx_resume(cptr, ttl, TMJOIN);
	} else {
		LOG4CXX_WARN(otstxlogger, (char*) "tx_resume: invalid control IOR: " << txn);
	}

	return TMER_INVAL;
}

bool OTSTxManager::recover(XAWrapper* resource)
{
	return true;
}

int OTSTxManager::tx_resume(CosTransactions::Control_ptr control, long ttl, int flags, int altflag)
{
	FTRACE(otstxlogger, "ENTER");
	TxControl *tx = new OTSControl(control, ttl, 0);
	int rc = TxManager::tx_resume(tx, flags);
	if (rc != XA_OK) {
		delete tx;
	}

	return rc;
}

char *OTSTxManager::get_current(long* ttl) {
	FTRACE(otstxlogger, "ENTER");
	char* toReturn = NULL;
//	CosTransactions::Control_ptr ctrl = get_control(ttl);
	CosTransactions::Control_ptr ctrl = static_cast<CosTransactions::Control_ptr>(get_control(ttl));

	if (!CORBA::is_nil(ctrl)) {
		CORBA::ORB_ptr orb = _connection->orbRef;
		CORBA::String_var cs = orb->object_to_string(ctrl);
		toReturn = strdup(cs);
	}

	CORBA::release(ctrl);
	FTRACE(otstxlogger, "< No tx ior");

	return toReturn;
}

TxControl* OTSTxManager::create_tx(TRANSACTION_TIMEOUT timeout)
{
	CosTransactions::Control_ptr ctrl = NULL;

	if (!CORBA::is_nil(_txfac)) {
		try {
			LOG4CXX_TRACE(otstxlogger, (char*) "Creating an OTS transaction");
			ctrl = _txfac->create((long) timeout);
			LOG4CXX_TRACE(otstxlogger, (char*) "Created OTS transaction - ctrl: " << ctrl);
		} catch (CORBA::SystemException & e) {
			LOG4CXX_DEBUG(otstxlogger, (char*) "Could not create OTS transaction: "
				<< e._name() << " minor code: " << e.minor());
		} catch (...) {
			LOG4CXX_WARN(otstxlogger, (char*) "Could not new OTS transaction (generic exception)");
		}
	} else {
		LOG4CXX_INFO(otstxlogger, (char*) "Unable to create OTS transaction - factory is nill");
	}

	if (CORBA::is_nil(ctrl)) {
		if (open_trans_factory() == TX_OK) {
			try {
				ctrl = _txfac->create((long) timeout);
			} catch (...) {
				LOG4CXX_WARN(otstxlogger, (char*) "Unable to start a new transaction (nil control)");
			}
		}
	}

	return (CORBA::is_nil(ctrl) ? NULL : new OTSControl(ctrl, (long) timeout, ACE_OS::thr_self()));
}

int OTSTxManager::do_open(void) {
	return open_trans_factory();
}

int OTSTxManager::open_trans_factory(void)
{
	if (_transFactoryId == NULL || strlen(_transFactoryId) == 0) {
		LOG4CXX_ERROR(otstxlogger, (char*) "Please set the TRANS_FACTORY_ID env variable");
		return TX_ERROR;
	}

	try {
		CosNaming::Name *name = _connection->default_ctx->to_name(_transFactoryId);
		LOG4CXX_DEBUG(otstxlogger, (char*) "resolving Tx Fac Id: " << _transFactoryId);
		CORBA::Object_var obj = _connection->default_ctx->resolve(*name);
		delete name;
		LOG4CXX_DEBUG(otstxlogger, (char*) "resolved OK: " << (void*) obj);
		_txfac = CosTransactions::TransactionFactory::_narrow(obj);
		LOG4CXX_DEBUG(otstxlogger, (char*) "narrowed OK: " << (void*) _txfac);
	} catch (CORBA::SystemException & e) {
		LOG4CXX_ERROR(otstxlogger, 
			(char*) "Error resolving Tx Service: " << e._name() << " minor code: " << e.minor());
		return TX_ERROR;
	} catch (...) {
		LOG4CXX_ERROR(otstxlogger, 
			(char*) "Unknown error resolving Tx Service did you run ant jts in the JBoss distribution and edit the jbossts properties to bind the service in the CORBA naming service: " << _transFactoryId);
		return TX_ERROR;
	}

	return TX_OK;
}

int OTSTxManager::do_close(void) {
	return TX_OK;
}

void OTSTxManager::release_control(void *ctrl) {
	FTRACE(otstxlogger, "ENTER");
	CosTransactions::Control_ptr cp = (CosTransactions::Control_ptr) ctrl;

	try {
		if (!CORBA::is_nil(cp))
			CORBA::release(cp);
		else
			FTRACE(otstxlogger, "< nothing to release");
	} catch (...) {
	}
}

}
}

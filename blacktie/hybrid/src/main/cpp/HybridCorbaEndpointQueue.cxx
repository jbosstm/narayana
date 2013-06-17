/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
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
//
// Servant which implements the AtmiBroker::ClientCallback interface.
//

#ifdef TAO_COMP
#include <orbsvcs/CosNamingS.h>
#endif

#include "ThreadLocalStorage.h"
#include "txx.h"
#include "HybridCorbaEndpointQueue.h"
#include "HybridSessionImpl.h"
#include "Codec.h"
#include "CodecFactory.h"

log4cxx::LoggerPtr HybridCorbaEndpointQueue::logger(log4cxx::Logger::getLogger(
		"HybridCorbaEndpointQueue"));

long TPFAIL = 0x00000001;
long COE_DISCON = 0x00000003;

int TPESVCERR = 10;
int TPESVCFAIL = 11;

long TPEV_DISCONIMM = 0x0001;
long TPEV_SVCERR = 0x0002;
long TPEV_SVCFAIL = 0x0004;

// EndpointQueue constructor
//
// Note: since we use virtual inheritance, we must include an
// initialiser for all the virtual base class constructors that
// require arguments, even those that we inherit indirectly.
//
HybridCorbaEndpointQueue::HybridCorbaEndpointQueue(HybridSessionImpl* session,
		CORBA_CONNECTION* connection, char* poaName, int id,
		void(*messagesAvailableCallback)(int, bool)) {
	LOG4CXX_DEBUG(logger, (char*) "Creating corba endpoint queue");
	shutdown = false;
	lock = new SynchronizableObject();
	LOG4CXX_DEBUG(logger, "Created lock: " << lock);

	CORBA::PolicyList policies;
	policies.length(0);
	thePoa = connection->callback_poa->create_POA(poaName,
			connection->root_poa_manager, policies);
	LOG4CXX_DEBUG(logger, (char*) "created thePoa: " << thePoa << " with name " << poaName);

	LOG4CXX_DEBUG(logger, (char*) "tmp_servant " << this);
	oid = thePoa->activate_object(this);
	LOG4CXX_DEBUG(logger, (char*) "activated tmp_servant " << this);
	CORBA::Object_var tmp_ref = thePoa->servant_to_reference(this);
	AtmiBroker::EndpointQueue_var queue = AtmiBroker::EndpointQueue::_narrow(
			tmp_ref);

	this->name = connection->orbRef->object_to_string(queue);
	this->connection = connection;
	this->session = session;
	this->poaName = poaName;
	this->id = id;
	this->messagesAvailableCallback = messagesAvailableCallback;
	LOG4CXX_DEBUG(logger, (char*) "Created corba endpoint queue: " << this);
}

// ~EndpointQueue destructor.
//
HybridCorbaEndpointQueue::~HybridCorbaEndpointQueue() {
	LOG4CXX_DEBUG(logger, (char*) "destroy called: " << this);

	LOG4CXX_DEBUG(logger, (char*) "destroying thePoa: " << thePoa);
	thePoa->destroy(true, true);
	thePoa = NULL;
	LOG4CXX_DEBUG(logger, (char*) "destroyed thePoa: " << thePoa);

	lock->lock();
	while (returnData.size() > 0) {
		MESSAGE message = returnData.front();
		returnData.pop();
		this->messagesAvailableCallback(id, true);
		LOG4CXX_DEBUG(logger, (char*) "updated listener");
		LOG4CXX_DEBUG(logger, (char*) "Freeing data message");
		if (message.data != NULL) {
			free(message.data);
		}
		if (message.type != NULL) {
			free(message.type);
		}
		if (message.subtype != NULL) {
			free(message.subtype);
		}
		if (message.replyto != NULL) {
			free((char*) message.replyto);
		}
	}
	if (!shutdown) {
		shutdown = true;
		lock->notifyAll();
	}
	lock->unlock();

	delete[] name;
	delete lock;
	free( poaName);
	LOG4CXX_DEBUG(logger, (char*) "destroyed: " << this);
}

void HybridCorbaEndpointQueue::send(const char* replyto_ior, CORBA::Short rval,
		CORBA::Long rcode, const AtmiBroker::octetSeq& idata, CORBA::Long ilen,
		CORBA::Long correlationId, CORBA::Long flags, const char* type,
		const char* subtype) throw (CORBA::SystemException) {
	lock->lock();
	if (!shutdown) {
		LOG4CXX_DEBUG(logger, (char*) "send called" << this);
		if (replyto_ior != NULL) {
			LOG4CXX_DEBUG(logger, (char*) "send reply to = " << replyto_ior);
		}
		LOG4CXX_DEBUG(logger, (char*) "send ilen = " << ilen);
		LOG4CXX_DEBUG(logger, (char*) "send flags = " << flags);

		MESSAGE message;
		message.type = NULL;
		message.subtype = NULL;
		message.len = 0;
		message.priority = 0;
		message.data = NULL;
		message.correlationId = correlationId;
		message.flags = flags;
		message.rcode = rcode;
		message.replyto = NULL;
		message.rval = rval;
		if (message.rval != COE_DISCON) {
			message.type = strdup(type);
			message.subtype = strdup(subtype);
			message.len = ilen;

			CodecFactory factory;
			Codec* codec = this->session->getCodec();
			if(codec == NULL) {
				codec = factory.getCodec(NULL);
			}
			message.data = codec->decode(message.type, message.subtype,
					(char*) idata.get_buffer(), &message.len);
			// if codec not from session, delete it
			if(codec != this->session->getCodec()) {
				factory.release(codec);
			}

			if (replyto_ior != NULL) {
				LOG4CXX_TRACE(logger, (char*) "Duplicating the replyto");
				message.replyto = strdup(replyto_ior);
				LOG4CXX_TRACE(logger, (char*) "Duplicated");
			}
		}

		message.received = true;
		// For remote comms this thread (comes from a pool) is different from the thread that will
		// eventually consume the message. For local comms this is not the case.
		// Thus we cannot dissassociate any transaction from the thread here (using destroySpecific)

		if (message.rval == COE_DISCON) {
			session->setLastEvent(TPEV_DISCONIMM);
		} else if (message.rcode == TPESVCERR) {
			session->setLastEvent(TPEV_SVCERR);
		} else if (message.rval == TPFAIL) {
			session->setLastEvent(TPEV_SVCFAIL);
			session->setLastRCode(message.rcode);
		}
		returnData.push(message);
		this->messagesAvailableCallback(id, false);
		LOG4CXX_DEBUG(logger, (char*) "informed watchers");
		LOG4CXX_DEBUG(logger, (char*) "notifying");
		lock->notify();
		LOG4CXX_DEBUG(logger, (char*) "notified");
	} else {
		LOG4CXX_WARN(logger, (char*) "MESSAGE DISCARDED - queue shutdown");
	}
	lock->unlock();
}

MESSAGE HybridCorbaEndpointQueue::receive(long time) {
	LOG4CXX_DEBUG(logger, (char*) "HybridCorbaEndpointQueue::receive: " << time << " : " << this);

	MESSAGE message = { NULL, -1, 0, NULL, NULL, NULL, -1, -1, -1, -1, -1, NULL, NULL,
			false, NULL, NULL, false };

	lock->lock();
	if (!shutdown) {
		if (time == -1) {
			LOG4CXX_DEBUG(logger, (char*) "TPNOBLOCK detected");
		} else if (returnData.size() == 0) {
			LOG4CXX_DEBUG(logger, (char*) "waiting for %d" << time);
			lock->wait(time);
			LOG4CXX_DEBUG(logger, (char*) "out of wait");
		}
		if (returnData.size() > 0) {
			message = returnData.front();
			returnData.pop();
			LOG4CXX_DEBUG(logger, (char*) "returning message");
			this->messagesAvailableCallback(id, true);
			LOG4CXX_DEBUG(logger, (char*) "updated listener");
		} else {
			LOG4CXX_DEBUG(logger, (char*) "no message");
		}
	}
	lock->unlock();
	return message;
}

void HybridCorbaEndpointQueue::ack(MESSAGE message) {
	// NO-OP
}

bool HybridCorbaEndpointQueue::connected() {
	LOG4CXX_ERROR(logger, (char*) "connected NO-OP");
	return false;
}

bool HybridCorbaEndpointQueue::connect() {
	LOG4CXX_ERROR(logger, (char*) "connect NO-OP");
	return false;
}

void HybridCorbaEndpointQueue::disconnect() throw (CORBA::SystemException) {
	LOG4CXX_DEBUG(logger, (char*) "disconnect");
	lock->lock();
	if (!shutdown) {
		shutdown = true;
		lock->notifyAll();
	}
	lock->unlock();
}

const char * HybridCorbaEndpointQueue::getName() {
	return name;
}

PortableServer::POA_ptr HybridCorbaEndpointQueue::getPoa() {
	return thePoa;
}

bool HybridCorbaEndpointQueue::isShutdown() {
	return this->shutdown;
}

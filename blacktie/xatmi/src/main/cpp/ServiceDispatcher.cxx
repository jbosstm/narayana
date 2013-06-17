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
#include "ServiceDispatcher.h"
#include "txx.h"
#include "ThreadLocalStorage.h"
#include "AtmiBrokerEnv.h"
#include "AtmiBrokerMem.h"
#include "txx.h"

#include "tao/ORB.h"
#include "ace/OS_NS_time.h"
#include "ace/OS_NS_sys_time.h"

log4cxx::LoggerPtr ServiceDispatcher::logger(log4cxx::Logger::getLogger(
		"ServiceDispatcher"));

extern void setTpurcode(long rcode);

ServiceDispatcher::ServiceDispatcher(AtmiBrokerServer* server,
		Destination* destination, Connection* connection,
		const char *serviceName, void(*func)(TPSVCINFO *), bool isPause,
		SynchronizableObject* reconnect, bool isConversational) {

	if (strncmp(serviceName, ".", 1) == 0) {
		this->isadm = true;
		LOG4CXX_DEBUG(logger, (char*) "servicedispatcher is an admin one: "
				<< this);
	} else {
		this->isadm = false;
	}

	this->reconnect = reconnect;
	this->destination = destination;
	this->connection = connection;
	this->serviceName = strdup(serviceName);
	this->func = func;
	this->isPause = isPause;
	session = NULL;
	stop = false;
	this->timeout = (long) (mqConfig.destinationTimeout * 1000000);
	this->counter = 0;
	this->error_counter = 0;
	this->server = server;
	this->minResponseTime = 0;
	this->avgResponseTime = 0;
	this->maxResponseTime = 0;
	this->isConversational = isConversational;
	pauseLock = new SynchronizableObject();
	LOG4CXX_DEBUG(logger, "Created lock: " << pauseLock);
	stopLock = new SynchronizableObject();
	LOG4CXX_DEBUG(logger, "Created lock: " << stopLock);
	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher created: " << this
			<< " isPause " << this->isPause << " isadm: " << this->isadm);
}

ServiceDispatcher::~ServiceDispatcher() {
	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher destroyed: " << this);
	free(this->serviceName);
	delete pauseLock;
	delete stopLock;
}

int ServiceDispatcher::pause(void) {
	LOG4CXX_TRACE(logger, "ServiceDispatcher pause: " << this);
	pauseLock->lock();
	isPause = true;
	pauseLock->unlock();
	return 0;
}

int ServiceDispatcher::resume(void) {
	LOG4CXX_TRACE(logger, "ServiceDispatcher resume: " << this);
	pauseLock->lock();
	isPause = false;
	pauseLock->notify();
	pauseLock->unlock();
	return 0;
}

int ServiceDispatcher::svc(void) {
	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher started: " << this
			<< " isPause " << isPause);
	while (!stop) {
		MESSAGE message;
		message.replyto = NULL;
		message.correlationId = -1;
		message.data = NULL;
		message.len = -1;
		message.priority = 0;
		message.flags = -1;
		message.control = NULL;
		message.rval = -1;
		message.rcode = -1;
		message.type = NULL;
		message.subtype = NULL;
		message.received = false;
		message.ttl = -1;
		message.serviceName = NULL;
		message.messageId = NULL;

		// Make sure we connect anyway to the subscriber is registered (although not when we are stopped) in the case where the server is paused
		stopLock->lock();
		if (!stop) {
			destination->connect();
		}
		stopLock->unlock();

		// This will wait while the server is paused
		if (!isadm) {
			pauseLock->lock();
			while (isPause == true) {
				LOG4CXX_DEBUG(logger, (char*) "pausing: " << this);
				pauseLock->wait(0);
				LOG4CXX_DEBUG(logger, (char*) "paused: " << this);
			}
			LOG4CXX_DEBUG(logger, (char*) "not paused: " << this
					<< " isPause: " << isPause);
			pauseLock->unlock();
		}

		bool stopped = false;
		stopLock->lock();
		if (!stop) {
			message = destination->receive(this->timeout);
			stopped = destination->isShutdown();
		}
		stopLock->unlock();

		if (message.received) {
			// Always ack the message as this is what closes the socket
			destination->ack(message);
			try {
				counter += 1;
				ACE_Time_Value start = ACE_OS::gettimeofday();
				LOG4CXX_DEBUG(logger, (char*) "calling onmessage");
				onMessage(message);
				LOG4CXX_DEBUG(logger, (char*) "called onmessage");
				ACE_Time_Value end = ACE_OS::gettimeofday();
				ACE_Time_Value tv = end - start;
				unsigned long responseTime = tv.msec();

				LOG4CXX_DEBUG(logger, (char*) "response time is "
						<< responseTime);

				if (minResponseTime == 0 || responseTime < minResponseTime) {
					minResponseTime = responseTime;
				}

				avgResponseTime = ((avgResponseTime * (counter - 1))
						+ responseTime) / counter;

				if (responseTime > maxResponseTime) {
					maxResponseTime = responseTime;
				}

				LOG4CXX_DEBUG(logger, (char*) "min:" << minResponseTime
						<< (char*) " avg:" << avgResponseTime
						<< (char*) " max:" << maxResponseTime);
			} catch (const CORBA::BAD_PARAM& ex) {
				LOG4CXX_WARN(logger, (char*) "Service dispatcher BAD_PARAM: "
						<< ex._name());
			} catch (const CORBA::SystemException& ex) {
				LOG4CXX_WARN(logger,
						(char*) "Service dispatcher SystemException: "
								<< ex._name());
			} catch (...) {
				LOG4CXX_ERROR(
						logger,
						(char*) "Service Dispatcher caught error running during onMessage");
			}

			if (message.data != NULL) {
				free(message.data);
			}
			setTpurcode(0);
		} else {
			if (stopped) {
				LOG4CXX_DEBUG(logger,
						(char*) "Service dispatcher detected closure");
				shutdown();
			}

			if (!stop && tperrno == TPESYSTEM) {
				LOG4CXX_WARN(
						logger,
						(char*) "Service dispatcher detected dead connection will reconnect after sleep");
				reconnect->lock();
				int timeout = 10;
				while (!stop && !destination->connected()) {
					LOG4CXX_DEBUG(logger, (char*) "sleeper, sleeping for "
							<< timeout << " seconds");
					ACE_OS::sleep(timeout);
					LOG4CXX_DEBUG(logger, (char*) "sleeper, slept for "
							<< timeout << " seconds");
					stopLock->lock();
					if (!stop && this->server->createAdminDestination(
							serviceName)) {
						LOG4CXX_INFO(logger,
								(char*) "Service dispatcher recreated: "
										<< serviceName);
						destination->connect(); // Attempt to reconnect
					}
					stopLock->unlock();
				}
				reconnect->unlock();
			}
		}
	}
	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher completed: " << this);
	return 0;
}

void ServiceDispatcher::onMessage(MESSAGE message) {
	setSpecific(TPE_KEY, TSS_TPERESET);

	LOG4CXX_DEBUG(logger, (char*) "svc()");

	// INITIALISE THE SENDER AND RECEIVER FOR THIS CONVERSATION
	if (message.replyto) {
		LOG4CXX_DEBUG(logger, (char*) "replyTo: " << message.replyto);
		LOG4CXX_TRACE(logger, (char*) "Creating session: "
				<< message.correlationId);
		session = connection->createSession(
				((message.flags & TPCONV) == TPCONV), message.correlationId,
				message.replyto);
		LOG4CXX_TRACE(logger, (char*) "Created session: "
				<< message.correlationId);
	} else {
		LOG4CXX_DEBUG(logger, (char*) "replyTo: NULL");
	}

	LOG4CXX_DEBUG(logger, (char*) "message.len: " << message.len
			<< " message.flags: " << message.flags << "cd: "
			<< message.correlationId << " message.control=" << message.control
            << " message.serviceName=" << message.serviceName);

	// PREPARE THE STRUCT FOR SENDING TO THE CLIENT
	TPSVCINFO tpsvcinfo;
	memset(&tpsvcinfo, '\0', sizeof(tpsvcinfo));


	LOG4CXX_DEBUG(logger, (char*) "serviceName Allocated the TPSVCINFO: " << sizeof(tpsvcinfo));
	LOG4CXX_DEBUG(logger, (char*) "serviceName Will copy up to: " << XATMI_SERVICE_NAME_LENGTH);
	LOG4CXX_DEBUG(logger, (char*) "serviceName Will copy: " << message.serviceName);
	LOG4CXX_DEBUG(logger, (char*) "serviceName Length of name is: " << strlen(message.serviceName));
	LOG4CXX_DEBUG(logger, (char*) "serviceName Length of existing name is: " << strlen(tpsvcinfo.name));
    int copyUpTo = (strlen(message.serviceName) < XATMI_SERVICE_NAME_LENGTH) ? strlen(message.serviceName) : XATMI_SERVICE_NAME_LENGTH;
	LOG4CXX_DEBUG(logger, (char*) "Will copy up to: " << copyUpTo);
	strncpy(tpsvcinfo.name, message.serviceName, copyUpTo);
	LOG4CXX_DEBUG(logger, (char*) "serviceName Length of new name is: " << strlen(tpsvcinfo.name));
	//strdup(tpsvcinfo.name, this->serviceName);
	tpsvcinfo.flags = message.flags;
	tpsvcinfo.len = message.len;

	if (message.data != NULL) {
		tpsvcinfo.data = AtmiBrokerMem::get_instance()->tpalloc(message.type,
				message.subtype, message.len, true);
		if (message.len > 0) {
			memcpy(tpsvcinfo.data, message.data, message.len);
		}
	} else {
		tpsvcinfo.data = NULL;
	}

	setSpecific(SVC_KEY, this);
	setSpecific(SVC_SES, session);

	bool hasTPCONV = tpsvcinfo.flags & TPCONV;
	if (hasTPCONV && isConversational) {
		long olen = 4;
		char* odata = (char*) tpalloc((char*) "X_OCTET", NULL, olen);
		strcpy(odata, "ACK");
		long revent = 0;
		long result = tpsend(message.correlationId, odata, olen, 0, &revent);
		if (result == -1) {
			connection->closeSession(message.correlationId);
			destroySpecific( SVC_SES);
			destroySpecific( SVC_KEY);
			return;
		}
		tpsvcinfo.cd = message.correlationId;
	} else if (!hasTPCONV && !isConversational) {
		LOG4CXX_DEBUG(logger, (char*) "cd not being set");
	} else {
		LOG4CXX_DEBUG(logger,
				(char*) "Session was invoked in an improper manner");

		long olen = 4;
		char* odata = (char*) tpalloc((char*) "X_OCTET", NULL, olen);
		strcpy(odata, "ERR");
		::tpreturn(TPFAIL, TPESVCFAIL, odata, olen, 0);
		connection->closeSession(message.correlationId);
		destroySpecific( SVC_SES);
		destroySpecific( SVC_KEY);
		LOG4CXX_DEBUG(logger, (char*) "Error reported");
		return;
	}

	if (tpsvcinfo.flags & TPRECVONLY) {
		session->setCanRecv(false);
		LOG4CXX_DEBUG(logger, (char*) "onMessage set constraints session: "
				<< session->getId() << " send(not changed): "
				<< session->getCanSend() << " recv: " << session->getCanRecv());
	} else if (tpsvcinfo.flags & TPSENDONLY) {
		session->setCanSend(false);
		LOG4CXX_DEBUG(logger, (char*) "onMessage set constraints session: "
				<< session->getId() << " send: " << session->getCanSend()
				<< " recv (not changed): " << session->getCanRecv());
	}

	// HANDLE THE CLIENT INVOCATION
	if (message.control != NULL && strcmp((char*) message.control, "null") != 0) {
		try {
			if (txx_associate_serialized((char*) message.control, message.ttl)
					!= XA_OK) {
				LOG4CXX_ERROR(logger, "Unable to handle control");
				setSpecific(TPE_KEY, TSS_TPESYSTEM);
			} else {
				tpsvcinfo.flags = (tpsvcinfo.flags | TPTRAN);
			}
		} catch (const CORBA::SystemException& ex) {
			LOG4CXX_ERROR(logger, "Unable to handle control: " << ex._name());
			setSpecific(TPE_KEY, TSS_TPESYSTEM);
		}
	}

	if (tperrno == 0) {
		try {
			LOG4CXX_TRACE(logger, (char*) "Calling function");
			this->func(&tpsvcinfo);
			LOG4CXX_TRACE(logger, (char*) "Called function");
		} catch (...) {
			LOG4CXX_ERROR(
					logger,
					(char*) "ServiceDispatcher caught error running during onMessage");
		}
	} else {
		LOG4CXX_ERROR(logger,
				(char*) "Not invoking tpservice as tpernno was not 0");
	}

	AtmiBrokerMem::get_instance()->tpfree(tpsvcinfo.data, true);

	// CLEAN UP THE SENDER AND RECEIVER FOR THIS CLIENT
	if (message.replyto && session->getCanSend()) {
		LOG4CXX_TRACE(logger,
				(char*) "Returning error - marking tx as rollback only if "
						<< getSpecific(TSS_KEY));
		::tpreturn(TPFAIL, TPESVCERR, NULL, 0, 0);
		LOG4CXX_TRACE(logger, (char*) "Returned error");
	}

	if (getSpecific(TSS_KEY) != NULL) {
		txx_release_control(txx_unbind(true));
	}

	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher closing session: "
			<< message.correlationId);
	connection->closeSession(message.correlationId);
	LOG4CXX_TRACE(logger, (char*) "ServiceDispatcher session closed: "
			<< message.correlationId);

	session = NULL;
	destroySpecific( SVC_SES);
	destroySpecific( SVC_KEY);

	LOG4CXX_TRACE(logger,
			(char*) "Freeing the data that was passed to the service");
	LOG4CXX_TRACE(logger, (char*) "Freed the data");
}

void ServiceDispatcher::shutdown() {
	LOG4CXX_TRACE(logger, (char*) "Service dispatcher shutting down: " << this);

	if (!stop) {
		// Stop the server
		stopLock->lock();
		stop = true;
		stopLock->unlock();

		// Unpause the server if it was paused
		resume();
	}

	LOG4CXX_TRACE(logger, (char*) "Service dispatcher shutdown: " << this);
}

long ServiceDispatcher::getCounter() {
	return counter;
}

long ServiceDispatcher::getErrorCounter() {
	return error_counter;
}

void ServiceDispatcher::updateErrorCounter() {
	error_counter++;
}

void ServiceDispatcher::getResponseTime(unsigned long* min, unsigned long* avg,
		unsigned long* max) {
	*min = minResponseTime;
	*avg = avgResponseTime;
	*max = maxResponseTime;
}

SynchronizableObject* ServiceDispatcher::getReconnect() {
	return reconnect;
}

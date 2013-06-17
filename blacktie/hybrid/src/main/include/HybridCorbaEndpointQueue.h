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

// Class: EndpointQueue
// A POA servant which implements of the AtmiBroker::ClientCallback interface
//

#ifndef HybridCorbaEndpointQueue_H_
#define HybridCorbaEndpointQueue_H_

#include "atmiBrokerHybridMacro.h"

#ifdef TAO_COMP
#include "AtmiBrokerS.h"
#endif

#include <queue>
#include "log4cxx/logger.h"
#include "CorbaConnection.h"
#include "Destination.h"
#include "SynchronizableObject.h"

class HybridSessionImpl;

class BLACKTIE_HYBRID_DLL HybridCorbaEndpointQueue: public virtual Destination, public virtual POA_AtmiBroker::EndpointQueue {
public:
	HybridCorbaEndpointQueue(HybridSessionImpl* session, CORBA_CONNECTION* connection, char* poaName, int id, void(*messagesAvailableCallback)(int, bool));
	virtual ~HybridCorbaEndpointQueue();

	virtual void send(const char* replyto_ior, CORBA::Short rval, CORBA::Long rcode, const AtmiBroker::octetSeq& idata, CORBA::Long ilen, CORBA::Long correlationId, CORBA::Long flags, const char* type, const char* subtype) throw (CORBA::SystemException );

	virtual bool connected();

	virtual bool connect();

	virtual void disconnect() throw (CORBA::SystemException );

	virtual MESSAGE receive(long time);
	virtual void ack(MESSAGE message);

	virtual const char* getName();

	PortableServer::POA_ptr getPoa();

	virtual bool isShutdown();

private:
	HybridSessionImpl* session;
	static log4cxx::LoggerPtr logger;
	std::queue<MESSAGE> returnData;
	SynchronizableObject* lock;
	bool shutdown;
	const char* name;
	PortableServer::POA_var thePoa;
	CORBA_CONNECTION* connection;
	PortableServer::ObjectId_var oid;
	char* poaName;
	int id;
	void(*messagesAvailableCallback)(int, bool);

	// The following are not implemented
	HybridCorbaEndpointQueue(const HybridCorbaEndpointQueue &);
	HybridCorbaEndpointQueue& operator=(const HybridCorbaEndpointQueue &);
};

#endif

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
#ifndef HybridSessionImpl_H_
#define HybridSessionImpl_H_

#include "atmiBrokerHybridMacro.h"

#include "log4cxx/logger.h"
#include "Session.h"
#include "CorbaConnection.h"
#include "HybridConnectionImpl.h"
#include "HybridCorbaEndpointQueue.h"
#include "HybridStompEndpointQueue.h"
#include "AtmiBrokerEnv.h"
#include "CodecFactory.h"

class HybridConnectionImpl;

class BLACKTIE_HYBRID_DLL HybridSessionImpl: public virtual Session {
public:
	HybridSessionImpl(bool isConv, char* connectionName, CORBA_CONNECTION* connection, apr_pool_t* pool, int id, const char* temporaryQueueName, void(*messagesAvailableCallback)(int, bool));
	HybridSessionImpl(bool isConv, char* connectionName, CORBA_CONNECTION* connection, apr_pool_t* pool, int id, char* service, void(*messagesAvailableCallback)(int, bool));
	HybridSessionImpl(apr_pool_t* pool);

	virtual ~HybridSessionImpl();

	void setSendTo(const char* replyTo);

	const char* getReplyTo();

	MESSAGE receive(long time);

	bool send(char* destinationName, MESSAGE &message);
	bool send(MESSAGE &message);
	void disconnect();

	int getId();
private:
	static log4cxx::LoggerPtr logger;
	CORBA_CONNECTION* corbaConnection;
	HybridCorbaEndpointQueue* temporaryQueue;
	AtmiBroker::EndpointQueue_var remoteEndpoint;
	stomp_connection* stompConnection;
	apr_pool_t* pool;
	const char* replyTo;
	char* sendTo;
	bool serviceInvokation;
	char* serviceName;
	const char* temporaryQueueName;
	AtmiBrokerEnv* env;
	CodecFactory factory;
};

#endif

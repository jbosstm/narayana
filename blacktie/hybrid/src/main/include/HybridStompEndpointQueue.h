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

#ifndef HybridStompEndpointQueue_H_
#define HybridStompEndpointQueue_H_

#include "atmiBrokerHybridMacro.h"

#include <queue>

#ifdef __cplusplus
extern "C" {
#endif
#include "stomp.h"
#ifdef __cplusplus
}
#endif

#include "log4cxx/logger.h"
#include "Destination.h"
#include "Codec.h"
#include "CodecFactory.h"
#include "SynchronizableObject.h"

class BLACKTIE_HYBRID_DLL HybridStompEndpointQueue: public virtual Destination {
public:
	HybridStompEndpointQueue(apr_pool_t* pool, char* serviceName, bool conversational, char* type);
	virtual ~HybridStompEndpointQueue();

	virtual bool connected();

	virtual bool connect();

	virtual void disconnect();

	virtual MESSAGE receive(long time);
	virtual void ack(MESSAGE message);

	virtual const char* getName();

	virtual bool isShutdown();

	const char* getFullName();
private:
	void disconnectImpl();

	static log4cxx::LoggerPtr logger;
	stomp_connection* connection;
	apr_pool_t* pool;
	stomp_frame* message;
	char* receipt;
	SynchronizableObject* shutdownLock;
	SynchronizableObject* readLock;
	bool shutdown;
	char* name;
	char* fullName;
	bool transactional;
	bool _connected;
	int unackedMessages;
    bool readDisconnected;
	CodecFactory factory;
	Codec* codec;
};

#endif

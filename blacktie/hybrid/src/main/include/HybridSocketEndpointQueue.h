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

#ifndef HybridSocketEndpointQueue_H_
#define HybridSocketEndpointQueue_H_

#include "atmiBrokerHybridMacro.h"

#include <queue>
#include "log4cxx/logger.h"
#include "Destination.h"
#include "Codec.h"
#include "CodecFactory.h"

#include "apr_network_io.h"
#include "apr_errno.h"
#include "apr_poll.h"
#include "apr_thread_cond.h"
#include "apr_version.h"
#include "apr.h"

#include "SocketServer.h"
#include "SynchronizableObject.h"

class HybridSocketSessionImpl;
#define DEF_POLL_TIMEOUT    (APR_USEC_PER_SEC * 3)

/**
 * JBTM-2743 we're on WIN32, and APR is version 1.4.0+,
 * then we have a broken WSAPoll() implementation.
 */
#if defined(APR_VERSION_AT_LEAST) && defined(WIN32)
#if APR_VERSION_AT_LEAST(1,4,0)
#define BROKEN_WSAPOLL
#endif
#endif

class BLACKTIE_HYBRID_DLL HybridSocketEndpointQueue: public virtual Destination {
public:
	HybridSocketEndpointQueue(HybridSocketSessionImpl* session, apr_pool_t* pool, int id, const char* addr, int port, void(*messagesAvailableCallback)(int, bool));
	HybridSocketEndpointQueue(HybridSocketSessionImpl* session, apr_pool_t* pool, client_ctx_t* ctx, void(*messagesAvailableCallback)(int, bool));
	virtual ~HybridSocketEndpointQueue();

	virtual bool connected();
	virtual bool connect();
	virtual void disconnect();

	virtual MESSAGE receive(long time);
	virtual void ack(MESSAGE message);

	virtual const char* getName();
	virtual bool isShutdown();
	void run();
	bool send(const char* replyto, long rval, long rcode, char* data, int len, long correlationId, long flags, const char* type, const char* subtype);

private:
	static log4cxx::LoggerPtr logger;
	apr_pool_t* pool;
	apr_socket_t* socket;
	apr_pollset_t *pollset;
	client_ctx_t* ctx;
	bool _connected;
	bool shutdown;
	char* addr;
	int port;
	CodecFactory factory;
	Codec* codec;
	std::queue<MESSAGE> send_queue;
	SynchronizableObject send_lock;
	SynchronizableObject queue_lock;
	client_ctx_t queue_ctx;
	int id;
	HybridSocketSessionImpl* session;
	void(*messagesAvailableCallback)(int, bool);

	int do_recv(serv_buffer_t *buffer, apr_pollset_t *pollset, apr_socket_t *sock);
	bool _send(const char* replyto, long rval, long rcode, char* data, int len, long correlationId, long flags, const char* type, const char* subtype);
	bool socketIsClose();
};

#endif

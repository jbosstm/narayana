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
#include "TestAssert.h"
#include "TestSocketSession.h"
#include "btlogger.h"
#include "AtmiBrokerEnv.h"
#include "apr_strings.h"
#include "apr.h"
#include <stdlib.h>

extern void messagesAvailableCallback(int bar, bool remove);

static void* APR_THREAD_FUNC run_server(apr_thread_t *thd, void *data) {
	SocketServer* server = (SocketServer*)data;
	btlogger("start server");
	server->run();
	apr_thread_exit(thd, APR_SUCCESS);
	return NULL;
}

static void* APR_THREAD_FUNC run_queue(apr_thread_t *thd, void *data) {
	HybridSocketEndpointQueue* queue = (HybridSocketEndpointQueue*)data;
	btlogger("start queue");
	queue->run();
	apr_thread_exit(thd, APR_SUCCESS);
	return NULL;
}

void TestSocketSession::setUp() {
	btlogger("TestSocketSession::setUp");
	apr_status_t rv;
	apr_pool_create(&mp, NULL);
	server = new SocketServer(port, mp, messagesAvailableCallback);
	apr_threadattr_create(&thd_attr, mp);
	rv = apr_thread_create(&thead, thd_attr, run_server, (void*)server, mp);
	BT_ASSERT(rv == APR_SUCCESS);

}

void TestSocketSession::tearDown() {
	btlogger("TestSocketSession::tearDown");
	apr_status_t rv;
	server->shutdown();
	btlogger("stop server");
	rv = apr_thread_join(&rv, thead);
	BT_ASSERT(rv == APR_SUCCESS);
	if(server) delete server;
}

void TestSocketSession::test_queue() {
	btlogger("TestSocketSession::test_queue");
	apr_status_t rv;
	client_ctx_t* ctx = server->register_client(1, NULL);
	HybridSocketSessionImpl* session = new HybridSocketSessionImpl(mp);
	HybridSocketEndpointQueue* client_queue = new HybridSocketEndpointQueue(session, mp, ctx, messagesAvailableCallback);
	HybridSocketEndpointQueue* svc_queue = new HybridSocketEndpointQueue(session, mp, 1, "localhost", port, messagesAvailableCallback);
	BT_ASSERT(svc_queue->connect());
	apr_thread_t     *queue_thead;
	rv = apr_thread_create(&queue_thead, thd_attr, run_queue, (void*)svc_queue, mp);
	BT_ASSERT(rv == APR_SUCCESS);

	for(int i = 0; i < 10; i++) {
		MESSAGE* msg = (MESSAGE*)apr_pcalloc(mp, sizeof(MESSAGE));
		msg->correlationId = 1;
		msg->replyto = NULL;
		msg->serviceName = NULL;
		msg->type = (char*) "X_OCTET";
		msg->subtype = NULL;
		msg->flags = 0;
		msg->rval = 0;
		msg->rcode = 1;
                msg->schedtime = -1;
		msg->data = new char[8];
		memcpy(msg->data, (char*)"test1234", 8);;
		msg->len = 8;
		BT_ASSERT(svc_queue->send(msg->replyto, msg->rval,
						msg->rcode, msg->data, msg->len,
						msg->correlationId, msg->flags, msg->type,
						msg->subtype));

		MESSAGE message = client_queue->receive(0);
		/*
		btlogger("message.data: %s  message.len: %ld message.rcode: %ld",
				message.data,  message.len, message.rcode);
		*/
		BT_ASSERT(memcmp(message.data, "test1234", message.len) == 0);
		BT_ASSERT(message.correlationId == 1);
		BT_ASSERT(message.len == 8);
		BT_ASSERT(message.rcode == 1);
		BT_ASSERT(message.flags == 0);
		BT_ASSERT(message.rval == 0);
		free(message.data);
		free((char*)message.replyto);
		free(message.type);
		free(message.subtype);

		msg->correlationId = 2;
		msg->rcode = 2;
		msg->data = new char[8];
		memcpy(msg->data, (char*)"test1235", 8);;
		BT_ASSERT(client_queue->send(msg->replyto, msg->rval,
						msg->rcode, msg->data, msg->len,
						msg->correlationId, msg->flags, msg->type,
						msg->subtype));

		message = svc_queue->receive(0);
		/*
		btlogger("message.data: %s message.len: %ld message.rcode: %ld",
				message.data, message.len, message.rcode);
		*/
		BT_ASSERT(memcmp(message.data, "test1235", message.len) == 0);
		BT_ASSERT(message.correlationId == 2);
		BT_ASSERT(message.rcode == 2);
		free(message.data);
		free((char*)message.replyto);
		free(message.type);
		free(message.subtype);
	}

	svc_queue->disconnect();
	server->unregister_client(1);

	rv = apr_thread_join(&rv, queue_thead);
	BT_ASSERT(rv == APR_SUCCESS);
	delete svc_queue;
	delete client_queue;
	delete session;
}

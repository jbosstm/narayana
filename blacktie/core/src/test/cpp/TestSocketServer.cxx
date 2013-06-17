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
#include "apr.h"
#include "TestSocketServer.h"
#include "TestAssert.h"
#include "btlogger.h"
#include "apr_strings.h"
#include <stdlib.h>

#define DEF_SOCK_TIMEOUT	(APR_USEC_PER_SEC * 30)

static void messagesAvailableCallback(int bar, bool remove) { }

static void* APR_THREAD_FUNC wait_cb(apr_thread_t *thd, void* data) {
	client_ctx_t* ctx = (client_ctx_t*)data;
	MESSAGE message = { NULL, -1, 0, NULL, NULL, NULL, -1, -1, -1, -1, -1, NULL, NULL,
			false, NULL, NULL, false };

	apr_thread_mutex_lock(ctx->mutex);
	while (ctx->data.empty()) {
		apr_thread_cond_wait(ctx->cond, ctx->mutex);
	}

	if(ctx->data.size() > 0){
		message = ctx->data.front();
		ctx->data.pop();
	}
	apr_thread_mutex_unlock(ctx->mutex);

	btlogger("message.type: %s message.len: %ld message.rcode: %ld",
			  message.type, message.len, message.rcode);

	BT_ASSERT(strcmp(message.type, "X_OCTET") == 0);
	BT_ASSERT(strcmp(message.subtype, "") == 0);
	BT_ASSERT(memcmp(message.data, "test1234", message.len) == 0);
	BT_ASSERT(message.len == 8);
	BT_ASSERT(message.flags == 0);
	BT_ASSERT(message.rcode == 1);
	BT_ASSERT(message.rval == 0);

	free((char*)message.replyto);
	free(message.data);
	free(message.type);
	free(message.subtype);

	apr_thread_exit(thd, APR_SUCCESS);
	return NULL;
}

void TestSocketServer::setUp() {
#ifdef WIN32
	apr_status_t rv = apr_initialize();
	BT_ASSERT(rv == APR_SUCCESS);
#endif
	apr_pool_create(&mp, NULL);
	apr_threadattr_create(&thd_attr, mp);
	server = SocketServer::get_instance(port, messagesAvailableCallback);
	btlogger("start server");
}

void TestSocketServer::tearDown() {
	btlogger("stop server");
	SocketServer::discard_instance();
}

void TestSocketServer::testServer() {
	apr_sockaddr_t *sa;
	apr_socket_t *s;
	apr_status_t rv;
	apr_thread_t *client_thead;

	client_ctx_t* ctx = server->register_client(1, NULL);
	BT_ASSERT(ctx != NULL);

	rv = apr_thread_create(&client_thead, thd_attr, wait_cb, (void*)ctx, mp);
	BT_ASSERT(rv == APR_SUCCESS);

	rv = apr_sockaddr_info_get(&sa, "localhost", APR_INET, port, 0, mp);
	BT_ASSERT(rv == APR_SUCCESS);

	rv = apr_socket_create(&s, sa->family, SOCK_STREAM, APR_PROTO_TCP, mp);
	BT_ASSERT(rv == APR_SUCCESS);

	apr_socket_opt_set(s, APR_SO_NONBLOCK, 1);
	apr_socket_timeout_set(s, DEF_SOCK_TIMEOUT);

	rv = apr_socket_connect(s, sa);
	BT_ASSERT(rv == APR_SUCCESS);

	MESSAGE* msg = (MESSAGE*)apr_pcalloc(mp, sizeof(MESSAGE));
	msg->correlationId = 1;
	msg->flags = 0;
	msg->rcode = 1;
	msg->rval  = 0;
	msg->replyto = NULL;
	msg->type = (char*)"X_OCTET";
	msg->subtype = (char*)"";
	msg->data = (char*)"test1234";
	msg->len = strlen(msg->data);

	char *buf = apr_psprintf(mp, "1\n%ld\n%ld\n%ld\n%ld\n%ld\n%s\n%s\n%s\n",
		(long)msg->correlationId, (long)msg->rcode, (long)msg->len, (long)msg->flags, (long)msg->rval,
		msg->replyto, msg->type, 
		(msg->subtype == NULL || strlen(msg->subtype) == 0) ? "(null)" : msg->subtype);

	apr_size_t len = strlen(buf) + msg->len;
	int sendlen = htonl(len);

	len = sizeof(sendlen);
	apr_socket_send(s, (char*)&sendlen, &len);

	len = strlen(buf);
	apr_socket_send(s, buf, &len);

	len = msg->len;
	apr_socket_send(s, msg->data, &len);

	apr_socket_close(s);

	rv = apr_thread_join(&rv, client_thead);
	BT_ASSERT(rv == APR_SUCCESS);

	server->unregister_client(1);
}

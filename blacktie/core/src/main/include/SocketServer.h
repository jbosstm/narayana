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
#ifndef SOCKET_SERVER_H
#define SOCKET_SERVER_H

#include "apr_network_io.h"
#include "apr_errno.h"
#include "apr_poll.h"
#include "apr_thread_cond.h"

#include "Message.h"
#include "Session.h"
#include <queue>

#define DEF_POLLSET_NUM     32
#define DEF_POLL_TIMEOUT    (APR_USEC_PER_SEC * 3)
#define DEF_BUF_SIZE        4096
#define DEF_MSG_QUEUE_SIZE  32
#define MSG_HEADER_SIZE     4
#define MAX_CLIENT_SIZE     128

typedef BLACKTIE_CORE_DLL struct _serv_buffer {
	apr_socket_t *sock;
	apr_pool_t   *context;
	char         *buf;
	apr_size_t   len;
	apr_size_t   rcvlen;
} serv_buffer_t;

typedef BLACKTIE_CORE_DLL struct _client_ctx {
	apr_thread_mutex_t  *mutex;
	apr_thread_cond_t   *cond;
	std::queue<MESSAGE> data;
	apr_socket_t        *sock;
	int                 sid;
	bool                used;
	Session             *session;
	apr_thread_mutex_t  *socket_close_mutex;
	bool                socket_is_close;
} client_ctx_t;

class BLACKTIE_CORE_DLL SocketServer {
	public:
		SocketServer(int port, apr_pool_t* context, void(*messagesAvailableCallback)(int, bool));
		~SocketServer();

		int  getPort()  {return port;};
		void shutdown() {this->finish = true;};
		int  run();
		client_ctx_t* register_client(int sid, Session* session);
		void unregister_client(int sid);

		static SocketServer* get_instance(int port, void(*messagesAvailableCallback)(int, bool));
		static void discard_instance();

	private:
		int  client_num;
		int  port;
		bool finish;
		bool running;
		apr_pool_t     *context;
		apr_socket_t   *sock;
		apr_sockaddr_t *localsa;
		apr_thread_mutex_t *mutex;
		apr_thread_mutex_t *startup;
		apr_thread_cond_t  *startup_cond;
		client_ctx_t   client_list[MAX_CLIENT_SIZE];
		void(*messagesAvailableCallback)(int, bool);
		static SocketServer* ptrSocketServer;

		int do_accept(apr_pollset_t *pollset, apr_socket_t *sock, apr_pool_t *context);
		int do_recv(serv_buffer_t *buffer, apr_pollset_t *pollset, apr_socket_t *sock);
		int do_dispatch(serv_buffer_t *buffer);
};

BLACKTIE_CORE_DLL bool unpack_message(MESSAGE* msg, int* sid, void* buf, size_t sz);

#endif

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
#ifndef HTTP_SERVER_H
#define HTTP_SERVER_H

#include "apr_network_io.h"
#include "apr_errno.h"
#include "apr_poll.h"
#include "apr_thread_cond.h"

#include "Http.h"
#include "httpTransportMacro.h"

#define DEF_POLLSET_NUM     32
#define DEF_POLL_TIMEOUT    (APR_USEC_PER_SEC * 3)
#define DEF_BUF_SIZE        512
#define MAX_CLIENT_SIZE     128

class HttpRequestHandler;

class BLACKTIE_HTTP_TRANSPORT_DLL HttpServer {
	public:
		HttpServer(char* host, int port, apr_pool_t* pool);
		~HttpServer();

		const char* get_host() {return host;};
		int get_port() {return port;};
		void shutdown() {this->finish = true;};
		int  run();
		void add_request_handler(HttpRequestHandler* handler);
		bool wait_for_server_startup();

	private:
		char* host;
		int  port;
		bool finish;
		bool running;
		apr_pool_t     *pool;
		apr_socket_t   *sock;
		apr_sockaddr_t *localsa;
		apr_thread_mutex_t *mutex;
		apr_thread_mutex_t *startup;
		apr_thread_cond_t  *startup_cond;
		HttpRequestHandler* _handler;
		apr_socket_t*   clients[MAX_CLIENT_SIZE];

		int do_accept(apr_pollset_t *pollset, apr_socket_t *sock, apr_pool_t *pool);
		int do_recv(http_conn_ctx *ctx, apr_pollset_t *pollset, apr_socket_t *sock);
		int do_dispatch(http_conn_ctx *ctx, apr_pollset_t *pollset, int clen);
};
#endif

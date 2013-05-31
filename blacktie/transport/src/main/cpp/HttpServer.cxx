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
#include <stdio.h>
#include <stdlib.h>
#include "apr_strings.h"
#include "apr_thread_proc.h"
#include "log4cxx/logger.h"
#include "HttpServer.h"
#include "HttpRequestHandler.h"

#ifdef WIN32
#define strtok_r(s,d,p) strtok_s(s,d,p)
#endif

static log4cxx::LoggerPtr loggerHttpServer(log4cxx::Logger::getLogger(
			"HttpServer"));

HttpServer::HttpServer(char* host, int port, apr_pool_t* pool) {
	LOG4CXX_DEBUG(loggerHttpServer, (char*) "http server constructor");
	this->host    = strdup(host);
	this->port    = port;
	this->pool    = pool;
	this->sock    = NULL;
	this->localsa = NULL;
	this->finish  = false;
	this->running = false;
	this->_handler = NULL;
	apr_thread_mutex_create(&this->startup, APR_THREAD_MUTEX_UNNESTED, pool);
	apr_thread_cond_create(&this->startup_cond, pool);
	for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
		clients[i] = NULL;
	}
}

HttpServer::~HttpServer() {
	LOG4CXX_DEBUG(loggerHttpServer, (char*) "http server deconstructor");
	if(host) free(host);
	for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
		if(clients[i] != NULL) {
			LOG4CXX_DEBUG(loggerHttpServer, (char*) "close clients[" << i << "] " << clients[i]);
			apr_socket_close(clients[i]);
		}
	}
}

void HttpServer::add_request_handler(HttpRequestHandler* handler) {
	_handler = handler;
}

bool HttpServer::wait_for_server_startup() {
	apr_thread_mutex_lock(this->startup);
	while(!running) {
		apr_thread_cond_wait(this->startup_cond, this->startup);
	}
	apr_thread_mutex_unlock(this->startup);

	//double check the server is running
	if (!running) {
		LOG4CXX_ERROR(loggerHttpServer, "server on port " << port << " is not running");
		return false;
	}

	return true;
}

int HttpServer::run() {
	apr_status_t stat;
	apr_pollset_t *pollset;
	apr_int32_t num;
	const apr_pollfd_t *ret_pfd;
	apr_int32_t  rv;
	char buf[128];

	if (apr_sockaddr_info_get(&localsa, host, APR_INET, port, 0, pool) != APR_SUCCESS) {
		LOG4CXX_WARN(loggerHttpServer, (char*) "could not get sockaddr");
		return -1;
	}

	if (apr_socket_create(&sock, localsa->family, SOCK_STREAM, APR_PROTO_TCP, pool) != APR_SUCCESS) {
		LOG4CXX_WARN(loggerHttpServer, (char*) "could not create socket");
		return -1;
	}

	apr_socket_opt_set(sock, APR_SO_NONBLOCK, 1);
	apr_socket_timeout_set(sock, 0);
	apr_socket_opt_set(sock, APR_SO_REUSEADDR, 1);

	if ((stat = apr_socket_bind(sock, localsa)) != APR_SUCCESS) {
		apr_socket_close(sock);
		apr_strerror(stat, buf, sizeof buf);
		LOG4CXX_WARN(loggerHttpServer, "could not bind on port " << port << ": " << buf);
		return -1;
	}

	if (apr_socket_listen(sock, 5) != APR_SUCCESS) {
		apr_socket_close(sock);
		LOG4CXX_WARN(loggerHttpServer, "could not listen");
		return -1;
	}

        if(apr_socket_addr_get(&localsa, APR_LOCAL, sock) != APR_SUCCESS) {
                apr_socket_close(sock);
                LOG4CXX_WARN(loggerHttpServer, "could not get address");
                return -1;
        }

        port = localsa->port;

	apr_pollset_create(&pollset, DEF_POLLSET_NUM, pool, 0);
	apr_pollfd_t pfd = { pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, NULL };
	pfd.desc.s = sock;
	apr_pollset_add(pollset, &pfd);

	LOG4CXX_DEBUG(loggerHttpServer, "running http server on port " << port);
	apr_thread_mutex_lock(this->startup);
	this->running = true;
	apr_thread_cond_signal(this->startup_cond);
	apr_thread_mutex_unlock(this->startup);

	while (!finish) {
		rv = apr_pollset_poll(pollset, DEF_POLL_TIMEOUT, &num, &ret_pfd);
		if (rv == APR_SUCCESS) {
			for (int i = 0; i < num; i++) {
				if ((ret_pfd[i].rtnevents & APR_POLLIN) && ret_pfd[i].desc.s == sock) {
					do_accept(pollset, sock, pool);
				} else {
					if (ret_pfd[i].rtnevents & APR_POLLIN) {
						LOG4CXX_DEBUG(loggerHttpServer, (char*) "read on " << ret_pfd[i].desc.s);
						http_conn_ctx* ctx = (http_conn_ctx*)ret_pfd[i].client_data;
						do_recv(ctx, pollset, ret_pfd[i].desc.s);
					}
				}
			}
		}
	}

	apr_socket_close(sock);
	return 0;
}

int HttpServer::do_accept(apr_pollset_t *pollset, apr_socket_t *sock, apr_pool_t *pool) {
	apr_socket_t *client;
	apr_sockaddr_t *remotesa;
	char* remote_addr;
	apr_status_t rv;

	rv = apr_socket_accept(&client, sock, pool);
	if (rv == APR_SUCCESS) {
		apr_socket_addr_get(&remotesa, APR_REMOTE, client);
		apr_sockaddr_ip_get(&remote_addr, remotesa);
		LOG4CXX_DEBUG(loggerHttpServer, (char*) "connection from " << remote_addr << ":" << remotesa->port);
		http_conn_ctx* ctx = (http_conn_ctx*)apr_palloc(pool, sizeof(http_conn_ctx));
		ctx->sock = client;
		ctx->pool = pool;
		ctx->len = 0;
		ctx->rcvlen = 0;
		ctx->data = NULL;

		apr_pollfd_t pfd = { pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, ctx};
		pfd.desc.s = client;

		apr_socket_opt_set(client, APR_SO_NONBLOCK, 1);
		apr_socket_timeout_set(client, 0);

		apr_pollset_add(pollset, &pfd);
		for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
			if(clients[i] == NULL) {
				LOG4CXX_DEBUG(loggerHttpServer, (char*) "add clients[" << i << "] " << client);
				clients[i] = client;
				break;
			}
		}
	} else {
		LOG4CXX_DEBUG(loggerHttpServer, (char*) "accept failed");
	}

	return 0;
}

int HttpServer::do_recv(http_conn_ctx* ctx, apr_pollset_t* pollset, apr_socket_t* sock) {
	apr_size_t   len;
	apr_status_t rv;
	int clen = 0;

	if(ctx->len == 0) {
		char* buf = (char*) apr_palloc(pool, ctx->rcvlen + DEF_BUF_SIZE);
		memset(buf, 0, ctx->rcvlen + DEF_BUF_SIZE);
		if(buf == NULL) {
			LOG4CXX_ERROR(loggerHttpServer, (char*) "can not alloc " << ctx->rcvlen + DEF_BUF_SIZE << " bytes");
			return -1;
		}

		if(ctx->rcvlen > 0) {
			memcpy(buf, ctx->data, ctx->rcvlen);
		}
		ctx->data = buf;
		ctx->len = DEF_BUF_SIZE;
	}

	len = ctx->len;
	rv = apr_socket_recv(sock, ctx->data + ctx->rcvlen, &len);
	if(rv == APR_SUCCESS) {
		LOG4CXX_DEBUG(loggerHttpServer, (char*) "receive " << len << " bytes");
		ctx->rcvlen += len;
		ctx->len    -= len;
	}

	if(len > 0 && check_http_end(NULL, ctx->data, ctx->rcvlen, &clen) == 0) {
		do_dispatch(ctx, pollset, clen);

		// clear for the next http request
		ctx->len = 0;
		ctx->rcvlen = 0;
	}

	if(rv == APR_EOF || len == 0) {
		LOG4CXX_DEBUG(loggerHttpServer, (char*) "rv: " << rv << ", len:" << len);
		LOG4CXX_DEBUG(loggerHttpServer, (char*) "client receive closed and remove from pollset");
		apr_pollfd_t pfd = { ctx->pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, ctx};
		pfd.desc.s = sock;
		apr_pollset_remove(pollset, &pfd);
		apr_socket_close(sock);
		for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
			if(clients[i] == sock) {
				LOG4CXX_DEBUG(loggerHttpServer, (char*) "remove clients[" << i << "] " << sock);
				clients[i] = NULL;
				break;
			}
		}
	}

	return rv;
}

int HttpServer::do_dispatch(http_conn_ctx* ctx, apr_pollset_t* pollset, int clen) {
	int  i;
	char *tok;
	char *saveptr;
	const char* sep = "\r\n";
	char* content = NULL;
	char* buf = ctx->data;

	http_request_info* request = (http_request_info*)apr_palloc(ctx->pool, sizeof(http_request_info));
	if(request == NULL) {
		LOG4CXX_ERROR(loggerHttpServer, "can not alloc http request");
		return -1;
	}

	request->pool = ctx->pool;
	request->num_headers = 0;
	request->query_strings = NULL;

	buf[ctx->rcvlen - clen - 1] = '\0';
	if(clen > 0) content = apr_pstrcat(request->pool, &buf[ctx->rcvlen - clen], NULL);

	for (i = 0, tok = strtok_r(buf, sep, &saveptr);
			tok;
			i++, tok = strtok_r(NULL, sep, &saveptr)) {
		LOG4CXX_DEBUG(loggerHttpServer, tok);
		switch(i) {
		case 0:
			parse_http_request(tok, request);
			LOG4CXX_DEBUG(loggerHttpServer, "method:" << request->method << " uri:" << request->uri);
			break;
		default:
			parse_http_headers(tok, request);
			break;
		}
	}

	for(i = 0; i < request->num_headers; i++) {
		LOG4CXX_DEBUG(loggerHttpServer, "name:" << request->http_headers[i].name << " value:" << request->http_headers[i].value);
	}
	if(content != NULL) {
		LOG4CXX_DEBUG(loggerHttpServer, content);
	}

	if(_handler != NULL) {
		LOG4CXX_DEBUG(loggerHttpServer, "dipatch request to handler " << _handler);
		if(_handler->handle_request(ctx, request, content, strlen(content)) == false) {
			LOG4CXX_DEBUG(loggerHttpServer, "closing connection " << ctx);
			//close ctx connection
			apr_pollfd_t pfd = { ctx->pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, ctx};
			pfd.desc.s = ctx->sock;
			apr_pollset_remove(pollset, &pfd);
			apr_socket_close(ctx->sock);
			for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
				if(clients[i] == ctx->sock) {
					LOG4CXX_DEBUG(loggerHttpServer, (char*) "remove clients[" << i << "] " << ctx->sock);
					clients[i] = NULL;
					break;
				}
			}
			LOG4CXX_DEBUG(loggerHttpServer, "closed connection " << ctx);
		}
	}

	return 0;
}

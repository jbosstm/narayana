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
#include "SynchronizableObject.h"
#include "SocketServer.h"

#ifdef WIN32
#define strtok_r(s,d,p) strtok_s(s,d,p)
#endif

log4cxx::LoggerPtr loggerSocketServer(log4cxx::Logger::getLogger(
			"SocketServer"));

SocketServer* SocketServer::ptrSocketServer = NULL;
SynchronizableObject server_lock;
int referencesSocketServer = 0;

static long TPFAIL = 0x00000001;
static long DISCON = 0x00000003;

static int TPESVCERR = 10;

static long TPEV_DISCONIMM = 0x0001;
static long TPEV_SVCERR = 0x0002;
static long TPEV_SVCFAIL = 0x0004;

static apr_thread_t     *thead;
static apr_threadattr_t *thd_attr;
static apr_pool_t       *pool;

static void* APR_THREAD_FUNC run_server(apr_thread_t *thd, void *data) {
	SocketServer* server = (SocketServer*)data;
	server->run();
	return NULL;
}

SocketServer* SocketServer::get_instance(int port, void(*messagesAvailableCallback)(int, bool)) {
	server_lock.lock();
	if (referencesSocketServer == 0) {
		if(ptrSocketServer == NULL) {
			apr_pool_create(&pool, NULL);
			ptrSocketServer = new SocketServer(port, pool, messagesAvailableCallback);
			apr_threadattr_create(&thd_attr, pool);
			apr_thread_create(&thead, thd_attr, run_server, (void*)ptrSocketServer, pool);
			LOG4CXX_DEBUG(loggerSocketServer, (char*) "create socket server");
		} else {
			LOG4CXX_WARN(loggerSocketServer, (char*) "can not create socket server");
		}
	}
	referencesSocketServer++;
	LOG4CXX_DEBUG(loggerSocketServer, (char*) "get_instance reference socket server " << referencesSocketServer);
	server_lock.unlock();
	return ptrSocketServer;
}

void SocketServer::discard_instance() {
	server_lock.lock();
	if (referencesSocketServer > 0) {
		referencesSocketServer --;
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "discard_instance reference socket server " << referencesSocketServer);
		if(referencesSocketServer == 0) {
			if(ptrSocketServer != NULL) {
				ptrSocketServer->shutdown();
				apr_status_t rv;
				if(thead) apr_thread_join(&rv, thead);
				apr_pool_destroy(pool);
				delete ptrSocketServer;
				ptrSocketServer = NULL;
			}
		}
	}
	server_lock.unlock();
}

SocketServer::SocketServer(int port, apr_pool_t* context, void(*messagesAvailableCallback)(int, bool)) {
	LOG4CXX_DEBUG(loggerSocketServer, (char*) "constructor");
	this->client_num = 0;
	this->port    = port;
	this->context = context;
	this->sock    = NULL;
	this->localsa = NULL;
	this->finish  = false;
	this->running = false;
	this->messagesAvailableCallback = messagesAvailableCallback;

	apr_thread_mutex_create(&this->mutex, APR_THREAD_MUTEX_UNNESTED, context);
	apr_thread_mutex_create(&this->startup, APR_THREAD_MUTEX_UNNESTED, context);
	apr_thread_cond_create(&this->startup_cond, context);

	for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
		client_list[i].used = false;
	}
}

SocketServer::~SocketServer() {
	LOG4CXX_DEBUG(loggerSocketServer, (char*) "deconstructor");
}

int SocketServer::run() {
	apr_status_t stat;
	apr_pollset_t *pollset;
	apr_int32_t num;
	const apr_pollfd_t *ret_pfd;
	apr_int32_t  rv;
	char buf[128];

	if (apr_sockaddr_info_get(&localsa, NULL, APR_INET, port, 0, context) != APR_SUCCESS) {
		LOG4CXX_WARN(loggerSocketServer, (char*) "could not get sockaddr");
		apr_thread_cond_signal(this->startup_cond);
		return -1;
	}

	if (apr_socket_create(&sock, localsa->family, SOCK_STREAM, APR_PROTO_TCP, context) != APR_SUCCESS) {
		LOG4CXX_WARN(loggerSocketServer, (char*) "could not create socket");
		apr_thread_cond_signal(this->startup_cond);
		return -1;
	}

	apr_socket_opt_set(sock, APR_SO_NONBLOCK, 1);
	apr_socket_timeout_set(sock, 0);
	apr_socket_opt_set(sock, APR_SO_REUSEADDR, 1);

	if ((stat = apr_socket_bind(sock, localsa)) != APR_SUCCESS) {
		apr_socket_close(sock);
		apr_strerror(stat, buf, sizeof buf);
		LOG4CXX_WARN(loggerSocketServer, "could not bind on port " << port << ": " << buf);
		apr_thread_cond_signal(this->startup_cond);
		return -1;
	}

	if (apr_socket_listen(sock, 5) != APR_SUCCESS) {
		apr_socket_close(sock);
		LOG4CXX_WARN(loggerSocketServer, "could not listen");
		apr_thread_cond_signal(this->startup_cond);
		return -1;
	}

        if(apr_socket_addr_get(&localsa, APR_LOCAL, sock) != APR_SUCCESS) {
                apr_socket_close(sock);
                LOG4CXX_WARN(loggerSocketServer, "could not get address");
                apr_thread_cond_signal(this->startup_cond);
                return -1; 
        };

        port = localsa->port;
       
	apr_pollset_create(&pollset, DEF_POLLSET_NUM, context, 0);
	apr_pollfd_t pfd = { context, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, NULL };
	pfd.desc.s = sock;
	apr_pollset_add(pollset, &pfd);

	LOG4CXX_DEBUG(loggerSocketServer, "running callback server on port " << port);

	apr_thread_mutex_lock(this->startup);
	this->running = true;
	apr_thread_cond_signal(this->startup_cond);
	apr_thread_mutex_unlock(this->startup);

	while (!finish) {
		rv = apr_pollset_poll(pollset, DEF_POLL_TIMEOUT, &num, &ret_pfd);
		if (rv == APR_SUCCESS) {
			for (int i = 0; i < num; i++) {
				if ((ret_pfd[i].rtnevents & APR_POLLIN) && ret_pfd[i].desc.s == sock) {
					do_accept(pollset, sock, context);
				} else {
					if (ret_pfd[i].rtnevents & APR_POLLIN) {
						LOG4CXX_DEBUG(loggerSocketServer, (char*) "read on " << ret_pfd[i].desc.s);
						serv_buffer_t *buffer = (serv_buffer_t*)ret_pfd[i].client_data;
						do_recv(buffer, pollset, ret_pfd[i].desc.s);
					}
				}
			}
		}
	}

	apr_socket_close(sock);
	return 0;
}

client_ctx_t* SocketServer::register_client(int sid, Session* session) {
	int i;
	client_ctx_t* result = NULL;

	apr_thread_mutex_lock(this->startup);
	if(!running) {
		apr_thread_cond_wait(this->startup_cond, this->startup);
	}
	apr_thread_mutex_unlock(this->startup);

	//double check the server is running
	if (!running) {
		LOG4CXX_ERROR(loggerSocketServer, "server on port " << port << " is not running");
		return NULL;
	}

	LOG4CXX_DEBUG(loggerSocketServer, "register_client sid: " << sid);
	if(client_list != NULL && client_num < MAX_CLIENT_SIZE) {
		apr_thread_mutex_lock(this->mutex);
		for(i = 0; i < MAX_CLIENT_SIZE; i++) {
			if(client_list[i].used == false) {
				apr_thread_mutex_create(&client_list[i].mutex, APR_THREAD_MUTEX_UNNESTED, context);
				apr_thread_mutex_create(&client_list[i].socket_close_mutex, APR_THREAD_MUTEX_UNNESTED, context);
				apr_thread_cond_create(&client_list[i].cond, context);
				client_list[i].sid = sid;
				client_list[i].session = session;
				client_list[i].used = true;
				client_list[i].sock = NULL;
				client_list[i].socket_is_close = true;
				result = &client_list[i];
				client_num++;
				break;
			}
		}
		apr_thread_mutex_unlock(this->mutex);
	} else {
		if(client_list == NULL) {
			LOG4CXX_WARN(loggerSocketServer, "client_list is null");
		} else {
			LOG4CXX_WARN(loggerSocketServer, "client_num reach maxsize " << client_num);
		}
	}

	return result;
}

void SocketServer::unregister_client(int sid) {
	int i;

	LOG4CXX_DEBUG(loggerSocketServer, "unregister_client sid: " << sid);
	apr_thread_mutex_lock(this->mutex);
	for(i = 0; i < MAX_CLIENT_SIZE; i++) {
		if(client_list[i].used && client_list[i].sid == sid) {
			client_list[i].sid = -1;
			client_list[i].session = NULL;
			client_list[i].used = false;
			client_list[i].sock = NULL;
			client_list[i].socket_is_close = true;
			client_num --;
			break;
		}
	}
	apr_thread_mutex_unlock(this->mutex);
}

int SocketServer::do_accept(apr_pollset_t *pollset, apr_socket_t *sock, apr_pool_t *context) {
	apr_socket_t *client;
	apr_sockaddr_t *remotesa;
	char* remote_addr;
	apr_status_t rv;

	rv = apr_socket_accept(&client, sock, context);
	if (rv == APR_SUCCESS) {
		apr_socket_addr_get(&remotesa, APR_REMOTE, client);
		apr_sockaddr_ip_get(&remote_addr, remotesa);
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "connection from " << remote_addr << ":" << remotesa->port);
		serv_buffer_t *buf = (serv_buffer_t*)apr_palloc(context, sizeof(serv_buffer_t));
		buf->sock = client;
		buf->context = context;
		buf->len = 0;
		buf->rcvlen = 0;

		apr_pollfd_t pfd = { context, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, buf};
		pfd.desc.s = client;

		apr_socket_opt_set(client, APR_SO_NONBLOCK, 1);
		apr_socket_timeout_set(client, 0);

		apr_pollset_add(pollset, &pfd);
	} else {
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "accept failed");
	}

	return 0;
}

int SocketServer::do_recv(serv_buffer_t *buffer, apr_pollset_t *pollset, apr_socket_t *sock) {
	apr_size_t   len;
	apr_status_t rv;
	static char headbuf[MSG_HEADER_SIZE];

	if(buffer->len == 0) {
		//receive header
		len = MSG_HEADER_SIZE - buffer->rcvlen;

		rv = apr_socket_recv(sock, headbuf + buffer->rcvlen, &len);
		buffer->rcvlen += len;

		if(rv == APR_SUCCESS) {
			if(buffer->rcvlen == MSG_HEADER_SIZE) {
				buffer->len = ntohl(*(int *)headbuf);	
				LOG4CXX_DEBUG(loggerSocketServer, (char*) "alloc " << buffer->len << " bytes");
				buffer->buf = (char*) apr_palloc(context, buffer->len);
			} else {
				LOG4CXX_DEBUG(loggerSocketServer, (char*) "receive header " << len << " bytes");
			}
		} else {
			if(rv != APR_EOF && rv != APR_ECONNRESET) {
				LOG4CXX_WARN(loggerSocketServer, (char*) "receive header failed with " << rv);
			}
		}
	} else {
		len = buffer->len + MSG_HEADER_SIZE - buffer->rcvlen;
		if(len > 0) {
			LOG4CXX_DEBUG(loggerSocketServer, (char*) "expect " << len << " bytes");
			rv = apr_socket_recv(sock, buffer->buf + buffer->rcvlen - MSG_HEADER_SIZE, &len);
			if(rv == APR_SUCCESS) {
				LOG4CXX_DEBUG(loggerSocketServer, (char*) "receive " << len << " bytes");
				buffer->rcvlen += len;
			} else {
				LOG4CXX_WARN(loggerSocketServer, (char*) "receive data failed with " << rv);
			}
		}
	}

	if(len > 0 && buffer->rcvlen == buffer->len + MSG_HEADER_SIZE) {
		do_dispatch(buffer);
	}

	if(rv == APR_EOF || len == 0) {
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "rv: " << rv << ", len:" << len);
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "client receive closed and remove from pollset");
		apr_pollfd_t pfd = { buffer->context, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, buffer};
		pfd.desc.s = sock;
		apr_pollset_remove(pollset, &pfd);
		bool socketClose = false;
		for(int i = 0; i < MAX_CLIENT_SIZE; i++) {
			if(client_list[i].used && sock == client_list[i].sock) {
				apr_thread_mutex_lock(client_list[i].socket_close_mutex);
				LOG4CXX_DEBUG(loggerSocketServer, (char*) "client[" << i << "] socket " << sock << " close");
				client_list[i].socket_is_close = true;
				apr_socket_shutdown(sock,APR_SHUTDOWN_READ);
				socketClose = true;
				apr_thread_mutex_unlock(client_list[i].socket_close_mutex);
				break;
			}
		}

		if (!socketClose) {
			LOG4CXX_DEBUG(loggerSocketServer, (char*) "socket " << sock << " is not register and close now");
			apr_socket_shutdown(sock,APR_SHUTDOWN_READ);
		}
	}

	return rv;
}

int SocketServer::do_dispatch(serv_buffer_t* buffer) {
	MESSAGE* msg;
	int sid;
	int i;

	LOG4CXX_DEBUG(loggerSocketServer, (char*) "dispatch message from buffer");
	msg = (MESSAGE*)apr_palloc(context, sizeof(MESSAGE));
	unpack_message(msg, &sid, buffer->buf, buffer->len);

	LOG4CXX_DEBUG(loggerSocketServer, (char*) "sid is " << sid);

	for(i = 0; i < MAX_CLIENT_SIZE; i++) {
		if(client_list[i].used && sid == client_list[i].sid) {
			LOG4CXX_DEBUG(loggerSocketServer, (char*) "push message to session id " << sid);
			apr_thread_mutex_lock(client_list[i].mutex);
			Session* session = client_list[i].session;

			if(session != NULL) {
				if (msg->rval == DISCON) {
					session->setLastEvent(TPEV_DISCONIMM);
				} else if (msg->rcode == TPESVCERR) {
					session->setLastEvent(TPEV_SVCERR);
				} else if (msg->rval == TPFAIL) {
					session->setLastEvent(TPEV_SVCFAIL);
					session->setLastRCode(msg->rcode);
				}
			}
			client_list[i].data.push(*msg);
			client_list[i].sock = buffer->sock;
			client_list[i].socket_is_close = false;
			this->messagesAvailableCallback(sid, false);
			apr_thread_cond_signal(client_list[i].cond);
			apr_thread_mutex_unlock(client_list[i].mutex);
			break;
		}
	}

	if(i == MAX_CLIENT_SIZE) {
		LOG4CXX_DEBUG(loggerSocketServer, (char*) "no client wait message for sid " << sid);
		if(msg->data != NULL) {
			free(msg->data);
		}
		if(msg->replyto != NULL) {
			free((char*)msg->replyto);
		}
		if(msg->type != NULL) {
			free(msg->type);
		}
		if(msg->subtype != NULL) {
			free(msg->subtype);
		}
	}

	LOG4CXX_DEBUG(loggerSocketServer, (char*) "reset buffer len for next receive");
	buffer->len = 0;
	buffer->rcvlen = 0;
	return 0;
}

static char *dup_string(char *s) {
	return s == NULL || strcmp(s, "(null)") == 0 ? strdup("") : strdup(s);
}

bool unpack_message(MESSAGE* msg, int* sid, void* buf, size_t sz) {
	int i;
	char *data = (char *) buf;
	char *tok;
	char *saveptr;
	const char* sep = "\n";

	//LOG4CXX_DEBUG(loggerSocketServer, (char*) "unpack buf: " << data);
	for (i = 0, tok = strtok_r((char *) data, sep, &saveptr);
			tok;
			i++, tok = strtok_r(NULL, sep, &saveptr)) {
		switch (i) {
			case 0: *sid = (int) atol(tok); break;
			case 1: msg->correlationId = (int) atol(tok); break;
			case 2: msg->rcode = atol(tok); break;
			case 3: msg->len = atol(tok); break;
			case 4: msg->flags = atol(tok); break;
			case 5: msg->rval = (int) atol(tok); break;

			case 6: msg->replyto = msg->rval == DISCON ? NULL:dup_string(tok); break;
			case 7: msg->type = msg->rval == DISCON ? NULL:dup_string(tok); break;
			case 8: msg->subtype = msg->rval == DISCON ? NULL:dup_string(tok); break;
			default: break;
		};

		if(i == 8) break;
	}

	if(msg->rval != DISCON && msg->len > 0) {
		msg->data = (char*)malloc(msg->len);
		memcpy(msg->data, saveptr, msg->len);
		//LOG4CXX_DEBUG(loggerSocketServer, (char*) "memcpy saveptr: " << saveptr << " msg->data:" << msg->data);
	} else {
		msg->data = NULL;
	}

	return true;
}

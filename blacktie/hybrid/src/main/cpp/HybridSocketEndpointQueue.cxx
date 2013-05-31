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
#include "apr_strings.h"
#include "ThreadLocalStorage.h"
#include "txx.h"
#include "HybridSocketEndpointQueue.h"
#include "HybridSocketSessionImpl.h"
#include "Codec.h"
#include "CodecFactory.h"
#include <stdlib.h>

log4cxx::LoggerPtr HybridSocketEndpointQueue::logger(log4cxx::Logger::getLogger(
		"HybridSocketEndpointQueue"));

static long TPFAIL = 0x00000001;
static long COE_DISCON = 0x00000003;

static int TPESVCERR = 10;
//static int TPESVCFAIL = 11;

static long TPEV_DISCONIMM = 0x0001;
static long TPEV_SVCERR = 0x0002;
static long TPEV_SVCFAIL = 0x0004;

// EndpointQueue constructor
//
// Note: since we use virtual inheritance, we must include an
// initialiser for all the virtual base class constructors that
// require arguments, even those that we inherit indirectly.
//
HybridSocketEndpointQueue::HybridSocketEndpointQueue(HybridSocketSessionImpl* session, apr_pool_t* pool, int id, const char* addr, int port,
		void(*messagesAvailableCallback)(int, bool)) {
	LOG4CXX_DEBUG(logger, (char*) "Creating socket endpoint queue with " << addr << ":" << port);
	shutdown = false;
	_connected = false;
	this->pool = pool;
	this->addr = strdup(addr);
	this->port = port;
	this->socket = NULL;
	apr_pollset_create(&this->pollset, 1, pool, 0);
	this->ctx = &this->queue_ctx;
	ctx->sock = NULL;
	ctx->sid = id;
	ctx->socket_is_close = true;
	this->id = id;
	LOG4CXX_TRACE(logger, (char*) "create id is " << id);
	this->session = session;
	this->messagesAvailableCallback = messagesAvailableCallback;
	apr_thread_mutex_create(&ctx->mutex, APR_THREAD_MUTEX_UNNESTED, pool);
	apr_thread_mutex_create(&ctx->socket_close_mutex, APR_THREAD_MUTEX_UNNESTED, pool);
	apr_thread_cond_create(&ctx->cond, pool);
}

HybridSocketEndpointQueue::HybridSocketEndpointQueue(HybridSocketSessionImpl* session, apr_pool_t* pool, client_ctx_t* ctx,
		void(*messagesAvailableCallback)(int, bool)) {
	LOG4CXX_DEBUG(logger, (char*) "Creating socket endpoint queue with client_ctx_t");
	shutdown = false;
	_connected = true;
	this->pool = pool;
	this->ctx = ctx;
	this->socket = ctx->sock;
	this->addr = NULL;
	this->id = ctx->sid;
	LOG4CXX_TRACE(logger, (char*) "create id from ctx->sid is " << id);
	this->session = session;
	this->messagesAvailableCallback = messagesAvailableCallback;
}
// ~EndpointQueue destructor.
//
HybridSocketEndpointQueue::~HybridSocketEndpointQueue() {
	LOG4CXX_DEBUG(logger, (char*) "destroy called: " << this);
	disconnect();
	if(addr) free(addr);

	apr_thread_mutex_lock(ctx->mutex);
	while (ctx->data.size() > 0) {
		MESSAGE message = ctx->data.front();
		ctx->data.pop();
		this->messagesAvailableCallback(id, true);
		LOG4CXX_DEBUG(logger, (char*) "updated listener");
		LOG4CXX_DEBUG(logger, (char*) "Freeing data message");
		if (message.data != NULL) {
			free(message.data);
		}
		if (message.type != NULL) {
			free(message.type);
		}
		if (message.subtype != NULL) {
			free(message.subtype);
		}
		if (message.replyto != NULL) {
			free((char*) message.replyto);
		}
	}
	if (!shutdown) {
		shutdown = true;
		apr_thread_cond_broadcast(ctx->cond);
	}
	apr_thread_mutex_unlock(ctx->mutex);
	LOG4CXX_DEBUG(logger, (char*) "destroyed: " << this);
}

void HybridSocketEndpointQueue::ack(MESSAGE message) {
	// NO-OP
}

bool HybridSocketEndpointQueue::connected() {
	if(ctx != NULL && ctx->sock != this->socket) {
		this->socket = ctx->sock;
	}
	return _connected;
}

bool HybridSocketEndpointQueue::connect() {
	if(_connected) {
		LOG4CXX_WARN(logger, (char*) "already connected");
		return true;
	}

	if(addr != NULL) {
		apr_status_t rv;
		apr_sockaddr_t *sa;

		rv = apr_sockaddr_info_get(&sa, addr, APR_INET, port, 0, pool);
		if(rv != APR_SUCCESS) {
			LOG4CXX_ERROR(logger, (char*) "connect get sockaddr info failed");
			return false;
		}

		rv = apr_socket_create(&socket, sa->family, SOCK_STREAM, APR_PROTO_TCP, pool);
		if(rv != APR_SUCCESS) {
			LOG4CXX_ERROR(logger, (char*) "connect create socket failed");
			return false;
		}

		ctx->sock = socket;
		apr_socket_opt_set(socket, APR_SO_NONBLOCK, 1);
		apr_socket_timeout_set(socket, 0);

		rv = apr_socket_connect(socket, sa);
		if(rv == APR_SUCCESS){
			LOG4CXX_DEBUG(logger, (char*) "connect to " << addr << ":" << port << " ok");
			_connected = true;
			ctx->socket_is_close = false;

			serv_buffer_t *buf = (serv_buffer_t*)apr_palloc(pool, sizeof(serv_buffer_t));
			buf->sock = socket;
			buf->context = pool;
			buf->len = 0;
			buf->rcvlen = 0;

			apr_pollfd_t pfd = { pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, buf};
			pfd.desc.s = socket;
			apr_pollset_add(pollset, &pfd);
			LOG4CXX_DEBUG(logger, (char*) "connected and wait for message");
			return true;
		} else if (APR_STATUS_IS_EINPROGRESS(rv)) {
			apr_pollfd_t pfd = { pool, APR_POLL_SOCKET, APR_POLLOUT|APR_POLLERR, 0, { NULL }, NULL };
			pfd.desc.s = socket;
			apr_pollset_add(pollset, &pfd);
			LOG4CXX_DEBUG(logger, (char*) "connecting and wait for connected");
			return true;
		} else {
			LOG4CXX_DEBUG(logger, (char*) "connect to " << addr << ":" << port << " failed");
		}
	} else {
		LOG4CXX_ERROR(logger, (char*) "connect addr is null");
	}
	return false;
}

void HybridSocketEndpointQueue::disconnect() {
	LOG4CXX_DEBUG(logger, (char*) "disconnect");
	//_connected = false;
	shutdown = true;
}

const char * HybridSocketEndpointQueue::getName() {
	return addr;
}

bool HybridSocketEndpointQueue::isShutdown() {
	return this->shutdown;
}

MESSAGE HybridSocketEndpointQueue::receive(long time) {
	MESSAGE message = { NULL, -1, 0, NULL, NULL, NULL, -1, -1, -1, -1, -1, NULL, NULL,
		false, NULL, NULL, false };
	apr_status_t rv;

	apr_thread_mutex_lock(ctx->mutex);
	while (ctx->data.empty()) {
		if(time > 0) {
			LOG4CXX_DEBUG(logger, (char*) "waiting message with time " << time);
			rv = apr_thread_cond_timedwait(ctx->cond, ctx->mutex, APR_USEC_PER_SEC * time);
			if(rv == APR_TIMEUP) {
				LOG4CXX_WARN(logger, (char*) "no message receive session " << ctx->sid << " for " << time << " seconds");
				break;
			} else if(rv != APR_SUCCESS){
				LOG4CXX_ERROR(logger, (char*) "apr_thread_cond_timedwait failed with " << rv);
				break;
			} else {
				LOG4CXX_DEBUG(logger, (char*) "double check ctx->data size after wait");
				int size = ctx->data.size();
				LOG4CXX_DEBUG(logger, (char*) "ctx->data size is " << size);
				if(size > 0) {
					LOG4CXX_DEBUG(logger, (char*) "ctx->data has message to return");
					break;
				} else {
					LOG4CXX_DEBUG(logger, (char*) "ctx->data has no message and should wait again");
				}
			}
		} else if(time == 0) {
			LOG4CXX_DEBUG(logger, (char*) "blocking waiting with time 0");
			rv = apr_thread_cond_wait(ctx->cond, ctx->mutex);
		} else {
			LOG4CXX_DEBUG(logger, (char*) "no waiting for time " << time);
			break;
		}
	}

	if(ctx->data.size() > 0){
		message = ctx->data.front();
		message.received = true;

		CodecFactory factory;
		Codec* codec = this->session->getCodec();
		if(codec == NULL) {
			codec = factory.getCodec(NULL);
		}

		char* pdata = message.data;
		message.data = codec->decode(message.type, message.subtype,
				(char*) message.data, &message.len);
		if(pdata) free(pdata);
		// if codec not from session, delete it
		if(codec != this->session->getCodec()) {
			factory.release(codec);
		}
		
		ctx->data.pop();
		LOG4CXX_DEBUG(logger, (char*) "returning message");
		this->messagesAvailableCallback(id, true);
		LOG4CXX_DEBUG(logger, (char*) "updated listener");
	}
	apr_thread_mutex_unlock(ctx->mutex);
	return message;
}

bool HybridSocketEndpointQueue::socketIsClose() {
	return ctx != NULL && ctx->socket_is_close;
}

bool HybridSocketEndpointQueue::_send(const char* replyto, long rval, long rcode, char* data, int msglen, long correlationId, long flags, const char* type, const char* subtype) {
	bool toReturn = false;

	apr_thread_mutex_lock(ctx->socket_close_mutex);
	if (socketIsClose()) {
		LOG4CXX_DEBUG(logger, (char*) "socket is close and can not send message");
	} else {
		int rc = APR_SUCCESS;
		char *buf = apr_psprintf(pool, "%ld\n%ld\n%ld\n%ld\n%ld\n%ld\n%s\n%s\n%s\n",
				(long)id, (long)correlationId, (long)rcode, (long)msglen, (long)flags, 
				(long)rval, 
				(replyto == NULL || strlen(replyto) == 0) ? "(null)" : replyto, 
				(type == NULL || strlen(type) == 0) ? "(null)": type, 
				(subtype == NULL || strlen(subtype) == 0) ? "(null)" : subtype);

		//LOG4CXX_DEBUG(logger, (char*)"buf: " << buf);

		apr_size_t len = strlen(buf) + msglen;
		int sendlen = htonl(len);

		if(rc == APR_SUCCESS) {
			len = sizeof(sendlen);
			rc = apr_socket_send(socket, (char*)&sendlen, &len);
		}

		if(rc == APR_SUCCESS) {
			len = strlen(buf);
			rc = apr_socket_send(socket, buf, &len);
		}

		if(rc == APR_SUCCESS && msglen > 0) {
			len = msglen;
			rc = apr_socket_send(socket, data, &len);
		}

		if(data != NULL) delete[] data;

		if(rc == APR_SUCCESS) {
			toReturn = true;
		}

		if(rval == COE_DISCON && socket != NULL) {
			apr_socket_shutdown(socket, APR_SHUTDOWN_WRITE);
			ctx->socket_is_close = true;
		}
	}
	apr_thread_mutex_unlock(ctx->socket_close_mutex);

	return toReturn;
}

bool HybridSocketEndpointQueue::send(const char* replyto, long rval, long rcode, char* data, int msglen, long correlationId, long flags, const char* type, const char* subtype) {
	bool is_connected = false;
	bool toReturn = false;

	send_lock.lock();
	is_connected = connected();

	if(is_connected) {
		send_lock.unlock();

		//wait for send_queue to send all messages first
		if(!send_queue.empty()) {
			queue_lock.lock();
			while(!send_queue.empty()) {
				queue_lock.wait(0);
			}
			queue_lock.unlock();
		}
		toReturn = _send(replyto, rval, rcode, data, msglen, correlationId, flags, type, subtype);
	} else {
		LOG4CXX_DEBUG(logger, (char*) "socket endpoint has not connected and push message to send_queue");
		MESSAGE message = { NULL, -1, 0, NULL, NULL, NULL, -1, -1, -1, -1, -1, -1, NULL, NULL, false, NULL, NULL, false };
		message.replyto = (char*)replyto;
		message.rval = rval;
		message.rcode = rcode;
		message.data = data;
		message.len = msglen;
		message.correlationId = correlationId;
		message.flags = flags;
		message.type = type == NULL ? NULL:strdup(type);
		message.subtype = subtype == NULL ? NULL:strdup(subtype);
		send_queue.push(message);
		send_lock.unlock();
		toReturn = true;
	}
	return toReturn;
}

void HybridSocketEndpointQueue::run() {
	apr_status_t rv;
	apr_int32_t num;
	const apr_pollfd_t *ret_pfd;

	while(!shutdown || !send_queue.empty()) {
		rv = apr_pollset_poll(pollset, DEF_POLL_TIMEOUT, &num, &ret_pfd);

		if (rv == APR_SUCCESS) {
			for (int i = 0; i < num; i++) {
				if (ret_pfd[i].rtnevents & APR_POLLERR) {
					LOG4CXX_WARN(logger, (char*) "connect to " << addr << ":" << port << " failed");
					return;
				} else if (ret_pfd[i].rtnevents & APR_POLLOUT) {
					if(ret_pfd[i].desc.s == socket && _connected == false) {
						LOG4CXX_DEBUG(logger, (char*) "connected");
						send_lock.lock();
						_connected = true;
						ctx->socket_is_close = false;
						send_lock.unlock();

						queue_lock.lock();
						while(!send_queue.empty()) {
							LOG4CXX_DEBUG(logger, (char*) "send message in queue");
							MESSAGE message = { NULL, -1, 0, NULL, NULL, NULL, -1, -1, -1, -1, -1, -1, NULL, NULL, false, NULL, NULL, false };
							message = send_queue.front();
							_send(message.replyto, message.rval,
								 message.rcode, message.data, message.len,
								 message.correlationId, message.flags, message.type,
								 message.subtype);
							if(message.type != NULL) free(message.type);
							if(message.subtype != NULL) free(message.subtype);
							send_queue.pop();
						}
						queue_lock.notify();
						queue_lock.unlock();

						apr_pollfd_t pfd = { pool, APR_POLL_SOCKET, APR_POLLOUT|APR_POLLERR, 0, { NULL }, NULL };
						pfd.desc.s = socket;
						apr_pollset_remove(pollset, &pfd);
						LOG4CXX_DEBUG(logger, (char*) "remove socket for connected");

						serv_buffer_t *buf = (serv_buffer_t*)apr_palloc(pool, sizeof(serv_buffer_t));
						buf->sock = socket;
						buf->context = pool;
						buf->len = 0;
						buf->rcvlen = 0;

						apr_pollfd_t pfd_s = { pool, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, buf};
						pfd_s.desc.s = socket;
						apr_pollset_add(pollset, &pfd_s);
						LOG4CXX_DEBUG(logger, (char*) "add socket in pollset");
					}
				} else if (ret_pfd[i].rtnevents & APR_POLLIN) {
					if(ret_pfd[i].desc.s == socket) {
						serv_buffer_t *buffer = (serv_buffer_t*)ret_pfd[i].client_data;
						do_recv(buffer, pollset, ret_pfd[i].desc.s);
					}
				}
			}
		}
	}
	if(socket != NULL) apr_socket_close(socket);
}

int HybridSocketEndpointQueue::do_recv(serv_buffer_t *buffer, apr_pollset_t *pollset, apr_socket_t *sock) {
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
				LOG4CXX_DEBUG(logger, (char*) "alloc " << buffer->len << " bytes");
				buffer->buf = (char*) apr_palloc(buffer->context, buffer->len);
			} else {
				LOG4CXX_DEBUG(logger, (char*) "receive header " << len << " bytes");
			}
		} else {
			if(rv != APR_EOF) {
				LOG4CXX_WARN(logger, (char*) "receive header failed with " << rv);
			}
		}
	} else {
		len = buffer->len + MSG_HEADER_SIZE - buffer->rcvlen;
		if(len > 0) {
			LOG4CXX_DEBUG(logger, (char*) "expect " << len << " bytes");
			rv = apr_socket_recv(sock, buffer->buf + buffer->rcvlen - MSG_HEADER_SIZE, &len);
			if(rv == APR_SUCCESS) {
				LOG4CXX_DEBUG(logger, (char*) "receive " << len << " bytes");
				buffer->rcvlen += len;
			} else {
				LOG4CXX_WARN(logger, (char*) "receive data failed with " << rv);
			}
		}
	}

	if(len > 0 && buffer->rcvlen == buffer->len + MSG_HEADER_SIZE) {
		MESSAGE* msg;
		int sid;
		msg = (MESSAGE*)apr_palloc(buffer->context, sizeof(MESSAGE));
		unpack_message(msg, &sid, buffer->buf, buffer->len);

		apr_thread_mutex_lock(ctx->mutex);
		if(sid != ctx->sid) {
			LOG4CXX_WARN(logger, (char*) "message sid is " << sid << " but ctx->sid is " << ctx->sid);
		}
		LOG4CXX_DEBUG(logger, (char*) "push message to ctx->sid " << ctx->sid);
		if (msg->rval == COE_DISCON) {
			session->setLastEvent(TPEV_DISCONIMM);
		} else if (msg->rcode == TPESVCERR) {
			session->setLastEvent(TPEV_SVCERR);
		} else if (msg->rval == TPFAIL) {
			session->setLastEvent(TPEV_SVCFAIL);
			session->setLastRCode(msg->rcode);
		}
		ctx->data.push(*msg);
		this->messagesAvailableCallback(ctx->sid, false);
		apr_thread_cond_signal(ctx->cond);
		apr_thread_mutex_unlock(ctx->mutex);
		LOG4CXX_DEBUG(logger, (char*) "reset buffer len");
		buffer->len = 0;
		buffer->rcvlen = 0;
	}

	if(rv == APR_EOF || len == 0) {
		LOG4CXX_DEBUG(logger, (char*) "queue receive closed and remove from pollset");
		apr_pollfd_t pfd = { buffer->context, APR_POLL_SOCKET, APR_POLLIN, 0, { NULL }, buffer};
		pfd.desc.s = sock;
		apr_pollset_remove(pollset, &pfd);
		apr_socket_shutdown(sock,APR_SHUTDOWN_READ);
		ctx->socket_is_close = true;
	}

	return rv;
}

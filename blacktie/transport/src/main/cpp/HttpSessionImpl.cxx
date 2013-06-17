/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and others contributors as indicated
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
#include <string.h>

#include "apr_strings.h"

#include <string>
#include <map>
#include <exception>

#include "btlogger.h"

#include "HttpSessionImpl.h"

#ifdef WIN32
#define strtok_r(s,d,p) strtok_s(s,d,p)
#endif

static const char* PULL_CONSUMERS = "msg-pull-consumers";
static const char* CREATE_WITH_ID = "msg-create-with-id";
static const char* CREATE_HDR = "msg-create";
static const char* PUSH_CONSUMERS = "msg-push-consumers";

static const char* CREATE_NEXT = "msg-create-next";
static const char* PULL_SUBSCRIPTIONS = "msg-pull-subscriptions";
static const char* CONSUME_NEXT = "msg-consume-next";

static const char* CONSUMER  = "msg-consumer";

//log4cxx::LoggerPtr httplogger(log4cxx::Logger::getLogger("TxHttpServer"));

/*
g++ -g HttpClient.cxx  HttpSessionImpl.cxx mongoose.c -I/home/mmusgrov/source/blacktie/trunk/atmibroker-transport/target/cxx/compile/include/apr-1 -l dl -L/home/mmusgrov/source/blacktie/trunk/atmibroker-transport/target/cxx/compile/lib -l apr-1 
 */

typedef std::map<std::string, std::string> HeaderMap;

const HeaderMap::value_type msg_headers[] = {
	HeaderMap::value_type(PULL_CONSUMERS, ""),
	HeaderMap::value_type(CREATE_WITH_ID, ""),
	HeaderMap::value_type(CREATE_HDR, ""),
	HeaderMap::value_type(PUSH_CONSUMERS, ""),

	HeaderMap::value_type(CREATE_NEXT, ""),
	HeaderMap::value_type(PULL_SUBSCRIPTIONS, ""),
	HeaderMap::value_type(CONSUME_NEXT, ""),
};

static const int nelems = sizeof msg_headers / sizeof msg_headers[0];

#define STR_SEP	'\n'
#define FIELD_LEN(str)  ((str) != NULL ? strlen ((char *) (str)) + 1 : 1)
#define FIELD_COPY(field, p, len)	\
	if ((field) == NULL) {*p++ = STR_SEP;}	\
	else {memcpy(p, (field), (len)); p += (len); *(p - 1) = STR_SEP;}
#define COPY_FROM_LONG(p, v)	memcpy(p, &(v), sizeof (apr_int64_t));	\
	p += sizeof (apr_int64_t)
#define COPY_TO_LONG(v, p)  memcpy(&(v), p, sizeof (apr_int64_t));	\
	p += sizeof (apr_int64_t)

template <typename T>
T swap_endian(T u)
{
	union {
		T u;
		unsigned char u8[sizeof(T)];
	} source, dest;

	source.u = u;

	for (size_t k = 0; k < sizeof(T); k++)
		dest.u8[k] = source.u8[sizeof(T) - k - 1];

	return dest.u;
}

static void* pack_message(apr_pool_t* pool, MESSAGE* msg, size_t* sz) {
#if 0
	size_t len_replyto = FIELD_LEN(msg->replyto);
	size_t len_data = FIELD_LEN(msg->data);
	size_t len_control = FIELD_LEN(msg->control);
	size_t len_xid = FIELD_LEN(msg->xid);
	size_t len_type = FIELD_LEN(msg->type);
	size_t len_subtype = FIELD_LEN(msg->subtype);
	size_t len_serviceName = FIELD_LEN(msg->serviceName);
	size_t len_messageId = FIELD_LEN(msg->messageId);

#if APR_IS_BIGENDIAN
	// tranfer over the wire in litle endian format
	apr_int64_t rcode = (apr_int64_t) swap_endian(msg->rcode);
	apr_int64_t len = (apr_int64_t) swap_endian(msg->len);
	apr_int64_t flags = (apr_int64_t) swap_endian(msg->flags);
	apr_int64_t ttl = (apr_int64_t) swap_endian(msg->ttl);
	apr_int64_t correlationId = ((apr_int64_t)) swap_endian(msg->correlationId);
	apr_int64_t priority = ((apr_int64_t)) swap_endian(msg->priority);
	apr_int64_t rval = ((apr_int64_t)) swap_endian(msg->rval);
#else
	apr_int64_t rcode = (apr_int64_t) msg->rcode;
	apr_int64_t len = (apr_int64_t) msg->len;
	apr_int64_t flags = (apr_int64_t) msg->flags;
	apr_int64_t ttl = (apr_int64_t) msg->ttl;
	apr_int64_t correlationId = (apr_int64_t) msg->correlationId;
	apr_int64_t priority = (apr_int64_t) msg->priority;
	apr_int64_t rval = (apr_int64_t) msg->rval;
#endif

	*sz = len_replyto + len_data + len_control + len_xid +
		len_type + len_subtype + len_serviceName + len_messageId +
		(7 * sizeof (apr_int64_t)); // pack ints as 64 bit types
	void* dp = apr_palloc(pool, *sz);
	char* p = (char *) dp;

	COPY_FROM_LONG(p, correlationId);
	COPY_FROM_LONG(p, priority);
	COPY_FROM_LONG(p, rcode);
	COPY_FROM_LONG(p, len);
	COPY_FROM_LONG(p, flags);
	COPY_FROM_LONG(p, ttl);
	COPY_FROM_LONG(p, rval);

	FIELD_COPY(msg->replyto, p, len_replyto);
	FIELD_COPY(msg->data, p, len_data);
	FIELD_COPY(msg->control, p, len_control);
	FIELD_COPY(msg->xid, p, len_xid);
	FIELD_COPY(msg->type, p, len_type);
	FIELD_COPY(msg->subtype, p, len_subtype);
	FIELD_COPY(msg->serviceName, p, len_serviceName);
	FIELD_COPY(msg->messageId, p, len_messageId);

	return dp;
#endif

	char *s = apr_psprintf(pool, "%ld\n%ld\n%ld\n%ld\n%ld\n%ld\n%ld\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
		(long) msg->correlationId, (long) msg->priority, (long) msg->rcode, (long) msg->len,
		(long) msg->flags, (long) msg->ttl, (long) msg->rval,
		msg->replyto, msg->data, (char *) msg->control, (char *) msg->xid,
		msg->type, msg->subtype, msg->serviceName, msg->messageId);

	*sz = strlen (s);

	return s;
}

#if 0
static char *copy_string(char **dst, char *src) {
	if (*src == STR_SEP) {
		*dst = NULL;
		return src + 1;
	} else {
		size_t sz;
		char *p;

		for (p = src; *p != STR_SEP; p++)
			;

		sz = p - src + 1;
		*p = 0;
		*dst = (char *) malloc(sz);
		memcpy(*dst, src, sz);

		return src + sz;
	}
}
#endif

static char *dup_string(char *s) {
	return s == NULL || strcmp(s, "(null)") == 0 ? NULL : strdup(s);
}

static bool unpack_message(MESSAGE* msg, void* buf, size_t sz) {
	int i;
	char *data = (char *) buf;

#if 0
	apr_pool_t *pool = NULL;
	int rc = apr_pool_create(&pool, NULL);
	void *p;

	if (rc != APR_SUCCESS) {
		btlogger_debug("OOM: unpack_message\n");
		return false;
	}

	p = apr_palloc(pool, sz);
	data[sz - 1] = '\010';

	sscanf(data, "%ld\n%ld\n%ld\n%ld\n%ld\n%ld\n%ld\n%200[^\010]",
		&(msg->correlationId), &(msg->priority), &(msg->rcode), &(msg->len),
		&(msg->flags), &(msg->ttl), &(msg->rval), (char *) p);
#endif
	char *tok;
	char *saveptr;
	const char* sep = "\n";

	for (i = 0, tok = strtok_r((char *) data, sep, &saveptr);
		tok;
		i++, tok = strtok_r(NULL, sep, &saveptr)) {
		switch (i) {
		case 0: msg->correlationId = (int) atol(tok); break;
		case 1: msg->priority = (int) atol(tok); break;
		case 2: msg->rcode = atol(tok); break;
		case 3: msg->len = atol(tok); break;
		case 4: msg->flags = atol(tok); break;
		case 5: msg->ttl = atol(tok); break;
		case 6: msg->rval = (int) atol(tok); break;

		case 7: msg->replyto = dup_string(tok); break;
		case 8: msg->data = dup_string(tok); break;
		case 9: msg->control = dup_string(tok); break;
		case 10: msg->xid = dup_string(tok); break;
		case 11: msg->type = dup_string(tok); break;
		case 12: msg->subtype = dup_string(tok); break;
		case 13: msg->serviceName = dup_string(tok); break;
		case 14: msg->messageId = dup_string(tok); break;
		default: break;
		};
	}

#if 0
	apr_pool_destroy(pool);
#endif
	btlogger_debug("unpack_message: rcode=%ld (%s)\n", msg->rcode, msg->data);

	return true;

#if 0
	char *p = (char*) buf;
	bool ok = false;

	COPY_TO_LONG(msg->correlationId, p);
	COPY_TO_LONG(msg->priority, p);
	COPY_TO_LONG(msg->rcode, p);
	COPY_TO_LONG(msg->len, p);
	COPY_TO_LONG(msg->flags, p);
	COPY_TO_LONG(msg->ttl, p);
	COPY_TO_LONG(msg->rval, p);

	p = copy_string((char**) &msg->replyto, p);
	p = copy_string(&msg->data, p);
	p = copy_string((char**) &msg->control, p);
	p = copy_string((char**) &msg->xid, p);
	p = copy_string(&msg->type, p);
	p = copy_string(&msg->subtype, p);
	p = copy_string(&msg->serviceName, p);
	p = copy_string(&msg->messageId, p);

	return true;
#endif
}

HttpSessionImpl::HttpSessionImpl(const char *qname) : _HEADERS(msg_headers, msg_headers + nelems), _status(-1) {
	int rc = apr_pool_create(&pool, NULL);
	if (rc != APR_SUCCESS) {
		btlogger_warn("OOM: pool create\n");
		apr_pool_destroy(pool);
		throw new std::exception();
	}

	(void) qinfo(qname);

	if (qname)
		_sqname = strdup(qname);
}

HttpSessionImpl::~HttpSessionImpl() {
	disconnect();
	if (_sqname)
		free(_sqname);
	if(pool)
		apr_pool_destroy(pool);
}


void HttpSessionImpl::setSendTo(const char* replyTo) {
	if (_sqname && (replyTo == NULL || strcmp(replyTo, _sqname) != 0)) {
		free(_sqname);
		_sqname = NULL;
//		remoteEndpoint = NULL;
	}

	if (replyTo && *replyTo != 0 && _sqname == NULL) {
//		CORBA::Object_var ref = corbaConnection->orbRef->string_to_object(replyTo);
//		remoteEndpoint = AtmiBroker::EndpointQueue::_narrow(ref);
//		LOG4CXX_DEBUG(logger, (char*) "RemoteEndpoint: " << remoteEndpoint);
		_sqname = strdup(replyTo);
	}
}

const char* HttpSessionImpl::getReplyTo() {
	return _rqname;
}

bool HttpSessionImpl::startConsumer() {
	std::string consumer = _HEADERS[CONSUMER];
	int sc;

	// create a consumer 
	if (consumer.size() == 0 && (sc = create_consumer(true)) != 201) {
		btlogger_debug("Could not create consumer. Status: %d\n", sc);
		return false;
	}

	return true;
}

bool HttpSessionImpl::stopConsumer() {
	remove_consumer();

	return true;
}

MESSAGE HttpSessionImpl::receive(long time) {
	MESSAGE msg;

	get(msg, time);

	return msg;
}

bool HttpSessionImpl::send(char* destinationName, MESSAGE &message) {
	return send(message);
}

bool HttpSessionImpl::send(MESSAGE &message) {
	http_request_info ri;
	std::string nm = _HEADERS[CREATE_HDR];
	size_t sz;


	void *msg = pack_message(pool, &message, &sz);
//btlogger_debug("XXX packed: %s\n", (char *) msg);

	int rc = _wc.send(pool, &ri, "POST", nm.c_str(), "*/*", NULL, (char *) msg, sz, NULL, NULL);


	if (rc != 0 || ri.status_code != 201) {
		btlogger_warn("Error %d sending message. Status: %d\n", rc, ri.status_code);
		_wc.dispose(&ri);

		return false;
	}

	decode_headers(&ri);

	return true;
}

void HttpSessionImpl::disconnect() {
	remove_consumer();
}

void HttpSessionImpl::dump_headers() {
	for (HeaderMap::iterator i = _HEADERS.begin(); i != _HEADERS.end(); ++i)
		btlogger_debug("Header: %s=%s\n", i->first.c_str(), i->second.c_str());
}

void HttpSessionImpl::decode_headers(http_request_info* ri) {
	for (int i = 0; i < ri->num_headers; i++) {
		_HEADERS[ri->http_headers[i].name] = ri->http_headers[i].value;
//		btlogger_debug("decoded %s=%s\n", ri->http_headers[i].name, ri->http_headers[i].value);
	}
//	btlogger_debug("==================\n");

	_wc.dispose(ri);
}

int HttpSessionImpl::qinfo(const char *qname) {
	http_request_info ri;

	if ((_status = _wc.send(pool, &ri, "HEAD", qname, "*/*", NULL, NULL, 0, NULL, NULL)) != 0) {
		btlogger_warn("send failure %d\n", errno);
		_wc.dispose(&ri);
		return -1;
	}

	decode_headers(&ri);

	return ri.status_code;
}

int HttpSessionImpl::remove_consumer() {
	http_request_info ri;
	std::string consumer = _HEADERS[CONSUMER];

	if (consumer.size() > 0) {
		_wc.send(pool, &ri, "DELETE", consumer.c_str(), "*/*", NULL, NULL, 0, NULL, NULL);
		_wc.dispose(&ri);

		switch (ri.status_code) {
		case -1:
			break;
		case 200:
		case 202:
		case 204:
			_HEADERS[CONSUMER] = "";
			break;
		default:
			_HEADERS[CONSUMER] = "";
		}

		return ri.status_code;
	}

	return 200;
}

int HttpSessionImpl::create_consumer(bool autoack) {
	http_request_info ri;
	const char *body = autoack ? NULL : "autoAck=false";
	size_t bodysz = autoack ? 0 : strlen(body);
	const char* mt = "application/x-www-form-urlencoded";
	std::string consumer = _HEADERS[CONSUMER];

	if (consumer.size() > 0)
		remove_consumer();

	consumer = _HEADERS[PULL_CONSUMERS];
	if (_wc.send(pool, &ri, "POST", consumer.c_str(), mt, NULL, body, bodysz, NULL, NULL) == 0)
		decode_headers(&ri);
	else
		_wc.dispose(&ri);

	return ri.status_code;
}

char* HttpSessionImpl::try_get_message(http_request_info* ri, long time, size_t *sz) {
	std::string nm = _HEADERS[CONSUME_NEXT];
	const char *mt = "application/x-www-form-urlencoded";
	char hdr[32];
	char* headers[] = {hdr, NULL};
	char *resp;

	sprintf(hdr, "Accept-Wait: %ld", time);

	btlogger_debug("consuming via resource: %s\n", nm.c_str());
	if (_wc.send(pool, ri, "POST", nm.c_str(), mt, (const char**) headers, NULL, 0, &resp, sz) == 0) {
		if (ri->status_code == 200 || ri->status_code == 412)
			decode_headers(ri);
		else
			_wc.dispose(ri);
	}

	return resp;
}

bool HttpSessionImpl::get(MESSAGE& msg, long time) {
	http_request_info ri;
	size_t sz;
	char * resp = try_get_message(&ri, time, &sz);

	btlogger_debug("%d ", ri.status_code);
	if (ri.status_code == 412) {
		if (resp)
			free(resp);

		// the server has crashed and is passing us a new mesg-consume-next header
		resp = try_get_message(&ri, time, &sz);

		// or the consumer session may have timed out
		if (ri.status_code == 412) {
			btlogger_debug("recreating consumer and retrying get\n");
			(void) remove_consumer();

			(void) create_consumer(true);
			if (resp)
				free(resp);

			resp = try_get_message(&ri, time, &sz);
		} else {
			btlogger_debug("retry status: %d\n", ri.status_code);
		}
	} else if (ri.status_code == 503) {
		btlogger("No messages, try again later\n");
	} else if (ri.status_code != 200) {
		btlogger("get status: %d\n", ri.status_code);
	}

	if (ri.status_code == 200) {
//		btlogger_debug("Message: %s\n", resp);
		if (resp == NULL) {
			btlogger_debug("empty response\n");
			memset(&msg, 0, sizeof (msg));
			msg.rcode = -1; //TPESVCERR;
		} else {
			unpack_message(&msg, resp, sz);
		}
	} else {
		memset(&msg, 0, sizeof (msg));
		msg.rcode = -1; //TPESVCERR;
	}

	if (resp)
		free(resp);

	return (ri.status_code == 200);
}

// TODO delete this method
void HttpSessionImpl::put_message(const char *msg) {
	http_request_info ri;
	std::string nm = _HEADERS[CREATE_HDR];

	int cnt = 0, i;
	(void) sscanf(msg + 1, "%d %d", &cnt, &i);

	if (cnt <= 0) {
		if (_wc.send(pool, &ri, "POST", nm.c_str(), "*/*", NULL, msg, strlen(msg), NULL, NULL) == 0 &&
			ri.status_code == 201)
			decode_headers(&ri);
	} else {
		while (cnt--) {
			char m[32];
			sprintf(m, "MSG %d", i++);
			btlogger_debug("%s\n", m);
			if (_wc.send(pool, &ri, "POST", nm.c_str(), "*/*", NULL, m, strlen(msg), NULL, NULL) != 0)
				btlogger_warn("message send error:  %d\n", ri.status_code);
			else if (ri.status_code == 201)
				decode_headers(&ri);
			else
				btlogger_debug("POST status: %d\n", ri.status_code);
		}
	}

	_wc.dispose(&ri);
}

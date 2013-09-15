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
#include <ctype.h>
#include <errno.h>

#include "ace/ACE.h"
#include "log4cxx/logger.h"
#include "apr_strings.h"

#include "HttpClient.h"

#ifdef WIN32
#define strtok_r(s,d,p) strtok_s(s,d,p)
#endif

static log4cxx::LoggerPtr logger(log4cxx::Logger::getLogger("HttpClient"));

void HttpClient::dup_headers(http_request_info* ri) {
    for (int i = 0; i < ri->num_headers; i++) {
        ri->http_headers[i].name = strdup(ri->http_headers[i].name);
        ri->http_headers[i].value = strdup(ri->http_headers[i].value);
    }
}

void HttpClient::dispose(http_request_info* ri) {
#if 0
// no longer calling HttpClient::dup_headers
    for (int i = 0; i < ri->num_headers; i++) {
        free(ri->http_headers[i].name);
        free(ri->http_headers[i].value);
    }
#endif
	ri->num_headers = 0;
}

http_conn_ctx* HttpClient::get_connection(apr_pool_t* pool, char* addr, int port) {
	apr_status_t rv;
	apr_sockaddr_t *sa;
	apr_socket_t *socket;

	if(addr != NULL && strlen(addr) > 0) {
		rv = apr_sockaddr_info_get(&sa, addr, APR_INET, port, 0, pool);
		if(rv != APR_SUCCESS) {
			LOG4CXX_ERROR(logger, (char*) "connect get sockaddr info failed");
			return NULL;
		}

		rv = apr_socket_create(&socket, sa->family, SOCK_STREAM, APR_PROTO_TCP, pool);
		if(rv != APR_SUCCESS) {
			LOG4CXX_ERROR(logger, (char*) "connect create socket failed");
			return NULL;
		}

		rv = apr_socket_connect(socket, sa);
		if(rv == APR_SUCCESS){
			LOG4CXX_DEBUG(logger, (char*) "connect to " << addr << ":" << port << " ok");
			http_conn_ctx* conn = (http_conn_ctx*)apr_palloc(pool, sizeof(http_conn_ctx));
			conn->pool = pool;
			conn->sock = socket;
			conn->data = NULL;
			conn->len  = 0;
			conn->rcvlen = 0;
			return conn;
		} else {
			LOG4CXX_DEBUG(logger, (char*) "connect to " << addr << ":" << port << " failed");
		}
	} else {
		LOG4CXX_ERROR(logger, (char*) "connect addr is null and port is " << port);
	}
	return NULL;
}

int HttpClient::http_print(http_conn_ctx *conn, const char* fmt, ...) {
	char* buf;
    va_list ap;

    va_start(ap, fmt);
	buf = apr_pvsprintf(conn->pool, fmt, ap);
    va_end(ap);

	LOG4CXX_DEBUG(logger, buf);
	write(conn, buf, strlen(buf));
	return 0;
}

int HttpClient::send(apr_pool_t* pool, http_request_info* ri, const char* method, const char* uri,
	const char* mediaType, const char* headers[], const char *body, size_t blen, char **resp, size_t *rcnt) {
//	size_t blen = (body == NULL ? 0 : strlen(body));
	char host[1025], buf[4096];
	int port = 0;

	ri->num_headers = 0;
	memset(host, 0, 1025);

	int ilen = parse_url(uri, host, &port);
	LOG4CXX_DEBUG(logger, "connected to TM on " << host << ":" << port << " URI=" << uri + ilen);

	memset(buf, 0, 4096);
	http_conn_ctx* conn = get_connection(pool, host, port);

	if (conn == NULL) {
		//perror("could not connect");
//		LOG4CXX_WARN(httpclientlog, "Unable to connect to TM on " << host << ":" << port);
		if (resp)
			*resp = NULL;

		ri->status_code = -1;
		return errno;
	}

//	LOG4CXX_DEBUG(httpclientlog, "connected to TM on " << host << ":" << port << " " << method << " " << uri + ilen);
	http_print(conn, "%s %s HTTP/%s\r\n", method, uri + ilen, HTTP_PROTO_VERSION);
	http_print(conn, "Host: %s:%d\r\n", host, port);
	http_print(conn, "%s: %d\r\n", "Content-Length", blen);
	http_print(conn, "%s: %s\r\n", "Content-Type", mediaType);

    if (headers != NULL) {
        int i = 0;

		for (; headers[i]; i++) {
			LOG4CXX_DEBUG(logger, "Header: " << headers[i]);
			http_print(conn, "%s\r\n", headers[i]);
		}
	}

	// End of headers, final newline
	write(conn, "\r\n", 2);

	// write any body data
	if (blen > 0) {
		LOG4CXX_DEBUG(logger, body);
		write(conn, body, blen);
	}

	// read the response
	int nread = 0;
	int clen = 0;
	int len = sizeof(buf);
	apr_size_t size = len;

	LOG4CXX_DEBUG(logger, "buf size is " << size);
	do {
		int rv = apr_socket_recv(conn->sock, buf + nread, &size);
		if(rv == APR_SUCCESS) {
			LOG4CXX_DEBUG(logger, (char*) "receive " << size << " bytes");
			nread += size;
			len   -= size;
			LOG4CXX_DEBUG(logger, buf);
		}
	} while(size > 0 && check_http_end(method, buf, nread, &clen) != 0);


	int i;
	char *tok;
	char *saveptr;
	const char* sep = "\r\n";
	char *content = NULL;

	char *b = & buf[0];
	char *scode = NULL;
	char *data = NULL;

	buf[nread - clen - 1] ='\0';
	if(clen > 0) {
		data = apr_pstrcat(pool, &buf[nread - clen], NULL);
	}

	// parse response
	// RFC says that all initial whitespaces should be ingored
	while (*b != '\0' && isspace(* (unsigned char *) b))
		b++;

	ri->pool = pool;
	for (i = 0, tok = strtok_r(buf, sep, &saveptr);
			tok;
			i++, tok = strtok_r(NULL, sep, &saveptr)) {
		LOG4CXX_DEBUG(logger, tok);
		switch(i) {
		case 0:
			ri->version = (char*) apr_palloc(pool, 4);
			scode = (char*) apr_palloc(pool, 4);
			if (sscanf(tok, "HTTP/%s%s", ri->version, scode) != 2) {
				LOG4CXX_WARN(logger, "parse " << tok << " : can not read http version and status code");
			}
			break;
		default:
			parse_http_headers(tok, ri);
		}
	}

	if (strncmp(ri->version, "1.1", 3) == 0) {
		ri->status_code = (scode != NULL ? atoi(scode) : -1);
		if(data != NULL) {
			content = strdup(data);
		}
	} else {
		ri->status_code = 500;
	}

	// done with the connection
	close_connection(conn);

	LOG4CXX_DEBUG(logger, (char*) "status_code:" << ri->status_code);
	if(content != NULL) {
		LOG4CXX_DEBUG(logger, (char*) "content: " << content);
	}

	if (resp == NULL) {
		if(content) {
			free(content);
			content = NULL;
		}
	} else {
		*resp = content;
	}

	if (rcnt != NULL) {
		*rcnt = content == NULL ? 0 : strlen(content);
	}
	return 0;
}

const char *HttpClient::get_header(http_request_info *ri, const char *name) {
	int i;

	for (i = 0; i < ri->num_headers; i++)
		if (!strcasecmp(name, ri->http_headers[i].name))
			return ri->http_headers[i].value;

	return NULL;
}

int HttpClient::parse_url(const char *url, char *host, int *port) {
	int len = 0;

	if (sscanf(url, "%*[htps]://%1024[^:]:%d%n", host, port, &len) == 2 ||
			sscanf(url, "%1024[^:]:%d%n", host, port, &len) == 2) {
	} else if (sscanf(url, "%*[htps]://%1024[^/]%n", host, &len) == 1) {
		*port = 80;
	} else {
		sscanf(url, "%1024[^/]%n", host, &len);
		*port = 80;
	}

	LOG4CXX_DEBUG(logger, (char*) "len: " << len);
	LOG4CXX_DEBUG(logger, (char*) "url: " << url + len);
	return len;
}

void HttpClient::url_encode(const char *src, char *dst, size_t dst_len) {
	static const char *dont_escape = "._-$,;~()";
	static const char *hex = "0123456789abcdef";
	const char *end = dst + dst_len - 1;

	for (; *src != '\0' && dst < end; src++, dst++) {
		if (isalnum(*(const unsigned char *) src) ||
				strchr(dont_escape, * (const unsigned char *) src) != NULL) {
			*dst = *src;
		} else if (dst + 2 < end) {
			dst[0] = '%';
			dst[1] = hex[(* (const unsigned char *) src) >> 4];
			dst[2] = hex[(* (const unsigned char *) src) & 0xf];
			dst += 2;
		}
	}

	*dst = '\0';
}

int HttpClient::write(http_conn_ctx *conn, const void *buf, size_t len) {
	apr_size_t sndlen = len;
	apr_socket_send(conn->sock, (const char*)buf, &sndlen);
	//LOG4CXX_DEBUG(logger, "len = " << len << " sndlen = " << sndlen);
	return sndlen;
}

void HttpClient::close_connection(http_conn_ctx *conn) {
	apr_socket_close(conn->sock);
}



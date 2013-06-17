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
#ifndef HTTP_H
#define HTTP_H

#include "apr_network_io.h"

#include "httpTransportMacro.h"

typedef BLACKTIE_HTTP_TRANSPORT_DLL struct _http_conn_ctx {
	apr_pool_t*   pool;
	apr_socket_t* sock;
	char*         data;
	apr_size_t    len;
	apr_size_t    rcvlen;
} http_conn_ctx;

typedef BLACKTIE_HTTP_TRANSPORT_DLL struct _http_request_info {
	apr_pool_t*   pool;
	char*         method;
	char*         uri;
	char*         query_strings;
	char*         version;
	int           num_headers;
	int           status_code;
	struct http_header {
		char* name;
		char* value;
	} http_headers[64];
} http_request_info;

int  check_http_end(const char* method, char* s, int i, int* clen);
void parse_http_request(char* data, http_request_info* request);
int  parse_http_headers(char* data, http_request_info* ri);

#endif

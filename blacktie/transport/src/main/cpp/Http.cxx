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
#include "Http.h"

#ifdef WIN32
#define strtok_r(s,d,p) strtok_s(s,d,p)
#endif

void parse_http_request(char* data, http_request_info* request) {
	int  i;
	char *tok;
	char *saveptr;
	const char* sep = " ";

	for (i = 0, tok = strtok_r(data, sep, &saveptr);
			tok;
			i++, tok = strtok_r(NULL, sep, &saveptr)) {
		switch(i) {
			case 0:
				request->method = (char*)apr_palloc(request->pool, strlen(tok) + 1);
				strcpy(request->method, tok);
				break;
			case 1:
				request->uri = (char*)apr_palloc(request->pool, strlen(tok) + 1);
				strcpy(request->uri, tok);
				break;
			case 2:
				request->version = (char*)apr_palloc(request->pool, strlen(tok) + 1);
				strcpy(request->version, tok);
				break;
			default:
				break;
		}
	}
}

int parse_http_headers(char* data, http_request_info* request) {
	char* value = strchr(data, ':');
	if(value != NULL) {
		request->http_headers[request->num_headers].value = (char*)apr_palloc(request->pool, strlen(value) - 1);
		strcpy(request->http_headers[request->num_headers].value, value + 2);

		int len = value - data;
		request->http_headers[request->num_headers].name = (char*)apr_palloc(request->pool, len + 1);
		strncpy(request->http_headers[request->num_headers].name, data, len);
		request->http_headers[request->num_headers].name[len] = '\0';
		request->num_headers ++;
		return 0;
	}
	return -1;
}

int check_http_end(const char* method, char* s, int i, int* clen) {
	int length = 0;

	if(method == NULL || strcmp(method, "HEAD") != 0) {
		char* p = strstr(s, "Content-Length");

		if (p != NULL && sscanf(p, "Content-Length: %d", &length) != 1) {
			length = 0;
		}
	}

	if(clen) *clen = length;

	if (s[i-4-length] == '\r' && s[i-3-length] == '\n') {
		return 0;
	}

	return -1;
}

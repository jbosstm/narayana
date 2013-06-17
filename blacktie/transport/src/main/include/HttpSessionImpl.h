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
#ifndef	_HTTPSESSIONIMPL_H_
#define _HTTPSESSIONIMPL_H_

#include "httpTransportMacro.h"
#include "log4cxx/logger.h"
#include "Session.h"
#include "HttpClient.h"

class BLACKTIE_HTTP_TRANSPORT_DLL HttpSessionImpl : public virtual Session {
public:
    HttpSessionImpl(const char* qname);
    virtual ~HttpSessionImpl();

    void setSendTo(const char* replyTo);
    const char* getReplyTo();

    MESSAGE receive(long time);
    bool send(char* destinationName, MESSAGE &message);
    bool send(MESSAGE &message);
    void disconnect();

	bool startConsumer();
	bool stopConsumer();
	void dump_headers();

	int status() {return _status;}

private:
	typedef std::map<std::string, std::string> HeaderMap;

	HeaderMap _HEADERS;
	int _status;
	char *_sqname;
	char *_rqname;
	HttpClient _wc;
	apr_pool_t     *pool;

	void decode_headers(http_request_info* ri);
	int qinfo(const char *qname);
	int remove_consumer();
	int create_consumer(bool autoack);
	char* try_get_message(http_request_info* ri, long time, size_t *sz);
	bool get(MESSAGE&, long time);
	void put_message(const char *msg);
};

#endif // _HTTPSESSIONIMPL_H_

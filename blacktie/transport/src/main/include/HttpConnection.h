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
#ifndef _HTTP_CONNECTION_H_
#define _HTTP_CONNECTION_H_

#include "httpTransportMacro.h"
#include "Connection.h"
#include "Destination.h"
#include "HttpSessionImpl.h"
#include "SynchronizableObject.h"

class BLACKTIE_HTTP_TRANSPORT_DLL HttpConnection: public virtual Connection {
public:
    HttpConnection();
	Session* createSessionImpl(bool isConv, int id, const char* temporaryQueueName);

    virtual ~HttpConnection();
    Session* createSession(bool isConv, char* serviceName) = 0;
    Session* createSession(bool isConv, int id, const char* temporaryQueueName) = 0;
    Session* getQueueSession() = 0;
    Session* getSession(int id) = 0;
    void closeSession(int id) = 0;
    void disconnectSession(int id) = 0;
    void cleanupThread() = 0;

    Destination* createDestination(char* serviceName, bool conversational, char* type) = 0;
    void destroyDestination(Destination* destination) = 0;

private:
	std::map<int, HttpSessionImpl*> _sessions;
	SynchronizableObject _lock;
	long _sid;

	bool lock() {return _lock.lock();}
	bool unlock() {return _lock.unlock();}
	HttpSessionImpl *addSession(int id, HttpSessionImpl *s);
};

#endif	// _HTTP_CONNECTION_H_

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

#include <string>
#include <map>
#include <exception>

#include "ThreadLocalStorage.h"
#include "HttpConnection.h"

// ConnectionManager::getConnection needs to be configurable
HttpConnection::HttpConnection() : Connection(), _sid(0) {}

HttpConnection::~HttpConnection() {}

Session* HttpConnection::createSessionImpl(bool isConv, int id, const char* temporaryQueueName) {
	Session *s = getSession(id);

	if (s == NULL)
		return addSession(id, new HttpSessionImpl("XXX qname"));

	return s;
}

Session* HttpConnection::createSession(bool isConv, char* serviceName) {
	return addSession(-1, new HttpSessionImpl("XXX qname"));
}

HttpSessionImpl *HttpConnection::addSession(int id, HttpSessionImpl *s) {
	lock();
	if (id < 0)
		id = _sid++;
    _sessions[id] = s;
    unlock();

    return s;
}

Session* HttpConnection::getQueueSession() {
    Session* qs = (Session*) getSpecific(QCN_KEY);

    if (qs == NULL) {
        qs = new HttpSessionImpl("XXX qname");
        setSpecific(QCN_KEY, qs);
    }

    return qs;
}

Session* HttpConnection::getSession(int id) {
	lock();
	std::map<int, HttpSessionImpl*>::iterator e = _sessions.find(id);

	return (e == _sessions.end() ? NULL : e->second);
	unlock();
}

void HttpConnection::closeSession(int id) {
	lock();
	std::map<int, HttpSessionImpl*>::iterator e = _sessions.find(id);

	if (e != _sessions.end()) {
		delete e->second;
		_sessions.erase(e);
	}

	unlock();
}

void HttpConnection::disconnectSession(int id) {
	lock();

	if (id < 0) {
		for (std::map<int, HttpSessionImpl*>::iterator e = _sessions.begin(); e != _sessions.end(); ++e)
			e->second->disconnect();
	} else {
		std::map<int, HttpSessionImpl*>::iterator e = _sessions.find(id);

		if (e != _sessions.end())
			e->second->disconnect();
	}

	unlock();
}

void HttpConnection::cleanupThread() {
	Session *qs = (Session*) getSpecific(QCN_KEY);

    if (qs != NULL) {
        delete qs;
        setSpecific(QCN_KEY, NULL);
    }
}


Destination* HttpConnection::createDestination(char* serviceName, bool conversational, char* type) {
	throw new std::exception();
}

void HttpConnection::destroyDestination(Destination* destination) {
	throw new std::exception();
}

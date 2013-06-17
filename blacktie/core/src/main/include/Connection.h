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

#ifndef CONNECTION_H
#define CONNECTION_H

#include "atmiBrokerCoreMacro.h"

#include "Destination.h"
#include "Session.h"

class Connection {
public:
	virtual ~Connection() {
	}
	virtual Session* createSession(bool isConv, char* serviceName) = 0;
	virtual Session* createSession(bool isConv, int id, const char* temporaryQueueName) = 0;
	virtual Session* getQueueSession() = 0;
	virtual Session* getSession(int id) = 0;
	virtual void closeSession(int id) = 0;
	virtual void disconnectSession(int id) = 0;
	virtual void cleanupThread() = 0;

	virtual Destination* createDestination(char* serviceName, bool conversational, char* type) = 0;
	virtual void destroyDestination(Destination* destination) = 0;
};

struct connection_factory_t {
	Connection* (*create_connection)(char * connectionName, void(*messagesAvailableCallback)(int, bool));
};

#endif

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
#ifndef CONNECTION_MANAGER_H_
#define CONNECTION_MANAGER_H_

#include <string>
#include <map>
#include "Connection.h"
#include "SynchronizableObject.h"

typedef std::map<std::string, Connection*> ConnectionMap;

class ConnectionManager {
public:
	ConnectionManager();
	~ConnectionManager();

	Connection* getClientConnection();
	Connection* getServerConnection();

	void closeConnections();

private:
	ConnectionMap manager;
	SynchronizableObject* lock;
	Connection* getConnection(char* side);
};

#endif

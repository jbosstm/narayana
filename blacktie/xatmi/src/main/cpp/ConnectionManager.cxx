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

#include "log4cxx/basicconfigurator.h"
#include "log4cxx/propertyconfigurator.h"
#include "log4cxx/logger.h"
#include "log4cxx/logmanager.h"

#include "AtmiBrokerServer.h"
#include "ConnectionManager.h"
#include "AtmiBrokerEnv.h"
#include "SymbolLoader.h"
#include "xatmi.h"
#include "HybridConnectionImpl.h"

log4cxx::LoggerPtr loggerConnectionManager(log4cxx::Logger::getLogger(
		"ConnectionManager"));
extern char server[30];
extern int serverid;

extern void tpgetanyCallback(int sessionId, bool remove);

ConnectionManager::ConnectionManager() {
	LOG4CXX_TRACE(loggerConnectionManager, (char*) "constructor");
	lock = new SynchronizableObject();
}

ConnectionManager::~ConnectionManager() {
	LOG4CXX_TRACE(loggerConnectionManager, (char*) "destructor");

	this->closeConnections();
	delete lock;
}

void ConnectionManager::closeConnections() {
	LOG4CXX_TRACE(loggerConnectionManager, (char*) "closeConnections");

	lock->lock();
	ConnectionMap::iterator it;
	for (it = manager.begin(); it != manager.end(); it++) {
		delete (*it).second;
	}
	manager.clear();
	lock->unlock();
}

Connection*
ConnectionManager::getConnection(char* side) {
	char adm[XATMI_SERVICE_NAME_LENGTH + 1];
	ACE_OS::snprintf(adm, XATMI_SERVICE_NAME_LENGTH + 1, ".%s%d", server,
			serverid);
	std::string key = side;
	key.append("/hybrid");

	lock->lock();
	ConnectionMap::iterator it;
	it = manager.find(key);

	if (it != manager.end()) {
		LOG4CXX_DEBUG(loggerConnectionManager, (char*) "Found Connection in map " << (*it).second);
		lock->unlock();
		return (*it).second;
	} else {
		Connection* connection = new HybridConnectionImpl(side, tpgetanyCallback);
		manager.insert(ConnectionMap::value_type(key, connection));
		LOG4CXX_DEBUG(loggerConnectionManager, (char*) "insert service " << key << " connection " << connection);
		lock->unlock();
		return connection;
	}

	LOG4CXX_WARN(loggerConnectionManager,
			(char*) "can not create connection for service " << side);
	lock->unlock();
	return NULL;
}

Connection*
ConnectionManager::getClientConnection() {
	return getConnection((char*) "client");
}

Connection*
ConnectionManager::getServerConnection() {
	return getConnection((char*) "server");
}

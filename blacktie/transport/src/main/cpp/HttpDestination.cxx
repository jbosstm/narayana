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
#include <stdlib.h>
#include <string.h>
#include "HttpDestination.h"

HttpDestination::HttpDestination(char* serviceName, bool conversational, char* type) : _shutdown(false) {
	_name = serviceName ? strdup(serviceName) : NULL;
}

HttpDestination::~HttpDestination() {
	if (_name)
		free(_name);
}

MESSAGE HttpDestination::receive(long timeout) {
	return MESSAGE();
}

void HttpDestination::ack(MESSAGE message) {
}

bool HttpDestination::connected() {
#if 0
    std::string consumer = _HEADERS[CONSUMER];

	return (consumer.size() == 0);
#endif
	return true;
}

bool HttpDestination::isShutdown() {
	return _shutdown;
}

bool HttpDestination::connect() {
#if 0
    std::string consumer = _HEADERS[CONSUMER];
    int sc;

    // create a consumer
    if (consumer.size() == 0 && (sc = create_consumer(true)) != 201) {
    	printf("Could not create consumer. Status: %d\n", sc);
    	return false;
    }
    
    return true;
#endif
	return true;
}

void HttpDestination::disconnect() {
#if 0
    remove_consumer();
#endif
}

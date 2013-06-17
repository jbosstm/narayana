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

#ifndef _HTTP_DESTINATION_H_
#define _HTTP_DESTINATION_H_

#include "httpTransportMacro.h"
#include "Destination.h"

class BLACKTIE_HTTP_TRANSPORT_DLL HttpDestination: public virtual Destination {
public:
	HttpDestination(char* serviceName, bool conversational, char* type);
    virtual ~HttpDestination();

    MESSAGE receive(long timeout);
    void ack(MESSAGE message);
	const char* getName() {return _name;}
    bool connected();
    bool connect();
    void disconnect();
    bool isShutdown();

private:
	char *_name;
	bool _shutdown;
};

#endif	// _HTTP_DESTINATION_H_

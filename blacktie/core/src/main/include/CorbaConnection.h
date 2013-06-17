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

#ifndef CORBA_CONNECTION_H
#define CORBA_CONNECTION_H

#include "atmiBrokerCoreMacro.h"

#include <tao/ORB.h>
#include <tao/Object.h>
#include <orbsvcs/CosNamingS.h>

#include "Worker.h"
#include "AtmiBrokerPoaFac.h"

struct BLACKTIE_CORE_DLL corba_connection_t {
    ~corba_connection_t() {if (worker) delete worker;}
	CORBA::ORB_var orbRef;
	PortableServer::POA_var root_poa;
	PortableServer::POAManager_var root_poa_manager;
	CosNaming::NamingContextExt_ptr default_ctx;
	PortableServer::POA_var callback_poa;
	Worker* worker;
	AtmiBrokerPoaFac* poaFactory;
	char * connectionName;
};
typedef struct BLACKTIE_CORE_DLL corba_connection_t CORBA_CONNECTION;

#endif

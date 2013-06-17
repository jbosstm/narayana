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

#ifndef AtmiBroker_POA_FAC_H_
#define AtmiBroker_POA_FAC_H_

#include "atmiBrokerCoreMacro.h"

#ifdef TAO_COMP
#include <tao/PortableServer/PortableServer.h>
#elif ORBIX_COMP
#include <omg/PortableServer.hh>
#endif
#ifdef VBC_COMP
#include <PortableServerExt_c.hh>
#endif

class BLACKTIE_CORE_DLL AtmiBrokerPoaFac {
public:

	AtmiBrokerPoaFac();

	~AtmiBrokerPoaFac();

	PortableServer::POA_ptr createCallbackPoa(CORBA::ORB_ptr, const char* poa_name, PortableServer::POA_ptr parent_poa, PortableServer::POAManager_ptr poa_manager);

	PortableServer::POA_ptr createServicePoa(CORBA::ORB_ptr, const char* poa_name, PortableServer::POA_ptr parent_poa, PortableServer::POAManager_ptr poa_manager);

	PortableServer::POA_ptr createServerPoa(CORBA::ORB_ptr, const char* poa_name, PortableServer::POA_ptr parent_poa, PortableServer::POAManager_ptr poa_manager);
};

#endif

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and others contributors as indicated
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

#include "ace/ACE.h"
#include "AtmiBrokerInit.h"

#include <log4cxx/propertyconfigurator.h>

#include "btlogger.h"

static log4cxx::LoggerPtr logger;

#ifdef __cplusplus
extern "C" {
#endif
BLACKTIE_CORE_DLL void init_ace() {
	(void) AtmiBrokerInitSingleton::instance();
}
#ifdef __cplusplus
}
#endif

AtmiBrokerInit::AtmiBrokerInit() {
    btlogger_init();

	logger = log4cxx::Logger::getLogger("AtmiBrokerInit");
	LOG4CXX_DEBUG(logger, (char*) "Constructed");
	ACE::init();
}

AtmiBrokerInit::~AtmiBrokerInit() {
	// NB cannot log from ACE singleton destructors since the ACE_Object_Manager calls
	// finalizers after log4cxx has been finalized.
	ACE::fini();
}

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

#include "AtmiBrokerInit.h"

#include <log4cxx/propertyconfigurator.h>

#include "btlogger.h"

#include "apr_general.h"

static log4cxx::LoggerPtr logger;
AtmiBrokerInit* AtmiBrokerInit::mpinstance;

#ifdef __cplusplus
extern "C" {
#endif
BLACKTIE_CORE_DLL void init_ace() {
	(void) AtmiBrokerInitSingleton::instance();
}
#ifdef __cplusplus
}
#endif

AtmiBrokerInit* AtmiBrokerInit::instance() {
	if(mpinstance == NULL)
		mpinstance = new AtmiBrokerInit();

	return mpinstance;
}

AtmiBrokerInit::AtmiBrokerInit() {
    btlogger_init();

	logger = log4cxx::Logger::getLogger("AtmiBrokerInit");

        apr_status_t rc = apr_initialize();
        if (rc != APR_SUCCESS) {
                LOG4CXX_ERROR(logger, (char*) "Could not initialize: " << rc);
                throw new std::exception();
        }
        LOG4CXX_TRACE(logger, (char*) "Initialized apr");

        LOG4CXX_DEBUG(logger, (char*) "Constructed");
}

AtmiBrokerInit::~AtmiBrokerInit() {
	apr_terminate();
}

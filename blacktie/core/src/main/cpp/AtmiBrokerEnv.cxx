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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <iostream>

#include "btlogger.h"
#include "AtmiBrokerEnv.h"
#include "AtmiBrokerEnvParser.h"
#include "AtmiBrokerEnvHandlers.h"
#include "SynchronizableObject.h"
#include "log4cxx/logger.h"
#include "ace/ACE.h"
#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_string.h"
#include "ace/Default_Constants.h"

log4cxx::LoggerPtr loggerAtmiBrokerEnv(log4cxx::Logger::getLogger(
		"AtmiBrokerEnv"));

int AtmiBrokerEnv::ENV_VARIABLE_SIZE = 30;

char *AtmiBrokerEnv::ENVIRONMENT_DIR = NULL;
AtmiBrokerEnv *AtmiBrokerEnv::ptrAtmiBrokerEnv = NULL;

char* configuration = NULL;
SynchronizableObject instance_lock;
int referencesAtmiBrokerEnv = 0;

AtmiBrokerEnv * AtmiBrokerEnv::get_instance() {
	btlogger_init();
	instance_lock.lock();
	if (referencesAtmiBrokerEnv == 0) {
		if (ptrAtmiBrokerEnv == NULL) {
			try {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
						(char*) "Creating AtmiBrokerEnv");
				ptrAtmiBrokerEnv = new AtmiBrokerEnv();
			} catch (...) {
				instance_lock.unlock();
				throw ;
			}
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerEnv,
					(char*) "Did not create AtmiBrokerEnv");
		}
	}
	referencesAtmiBrokerEnv++;
	LOG4CXX_TRACE(loggerAtmiBrokerEnv,
			(char*) "Reference count: " << referencesAtmiBrokerEnv);
	instance_lock.unlock();
	return ptrAtmiBrokerEnv;
}

void AtmiBrokerEnv::discard_instance() {
	instance_lock.lock();
    if (referencesAtmiBrokerEnv > 0) {
	referencesAtmiBrokerEnv--;
	LOG4CXX_TRACE(loggerAtmiBrokerEnv, (char*) "Reference count: "
			<< referencesAtmiBrokerEnv);
	if (referencesAtmiBrokerEnv == 0) {
		if (ptrAtmiBrokerEnv != NULL) {
			try {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
						(char*) "Deleting AtmiBrokerEnv");
				delete ptrAtmiBrokerEnv;
			} catch (...) {
				instance_lock.unlock();
				ptrAtmiBrokerEnv = NULL;
				throw ;
			}
			ptrAtmiBrokerEnv = NULL;
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerEnv,
					(char*) "Did not delete AtmiBrokerEnv");
		}
	}
    } else {
			LOG4CXX_WARN(loggerAtmiBrokerEnv,
					(char*) "Reference count already zero");
    }
	instance_lock.unlock();
}

void AtmiBrokerEnv::set_environment_dir(const char* dir) {
	if (ENVIRONMENT_DIR != NULL) {
		free( ENVIRONMENT_DIR);
		ENVIRONMENT_DIR = NULL;
	}
	if (dir != NULL) {
		LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
				(char*) "setting configuration dir: " << dir);
		ENVIRONMENT_DIR = strdup(dir);
	} else {
		LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
				(char*) "setting configuration to null");
	}
}

void AtmiBrokerEnv::set_configuration(const char* dir) {
	if (configuration == NULL) {
		btlogger_init();
		if (dir != NULL) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
					(char*) "setting configuration type: " << dir);

			configuration = strdup(dir);
		} else {
			configuration = strdup("");
			LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
					(char*) "setting configuration to null");
		}
	}
}

AtmiBrokerEnv::AtmiBrokerEnv() {
	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "constructor");
	readEnvironment = false;

	set_environment_dir(ACE_OS::getenv("BLACKTIE_CONFIGURATION_DIR"));
	set_configuration(ACE_OS::getenv("BLACKTIE_CONFIGURATION"));

	try {
		readenv();
	} catch (...) {
		destroy();
		throw ;
	}
}

AtmiBrokerEnv::~AtmiBrokerEnv() {
	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "destructor");
	destroy();
}

void AtmiBrokerEnv::destroy() {
	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "destroy");
	for (std::vector<envVar_t>::iterator i = envVariableInfoSeq.begin(); i
			!= envVariableInfoSeq.end(); i++) {
		free((*i).name);
		free((*i).value);
	}

	set_environment_dir( NULL);
	if (configuration != NULL) {
		free( configuration);
		configuration = NULL;
	}
	envVariableInfoSeq.clear();

	//free(namingServiceId);
	//free(transFactoryId);

	if (xarmp) {
		xarmp = xarmp->head;

		while (xarmp) {
			xarm_config_t * next = xarmp->next;

			free(xarmp->resourceName);
			free(xarmp->openString);
			free(xarmp->closeString);
			free(xarmp->xasw);
			free(xarmp->xalib);

			free( xarmp);

			xarmp = next;
		}
		xarmp = 0;
	}

	if (servers.size() != 0) {
		for (ServersInfo::iterator server = servers.begin(); server
				!= servers.end(); server++) {
			free((*server)->serverName);
            if ((*server)->function_name != NULL) {
    			free((*server)->function_name);
            }
            if ((*server)->done_function_name != NULL) {
    			free((*server)->done_function_name);
            }

			std::vector<ServiceInfo>* services = &(*server)->serviceVector;
			for (std::vector<ServiceInfo>::iterator i = services->begin(); i
					!= services->end(); i++) {
				free((*i).serviceName);
				free((*i).serviceType);
				free((*i).transportLib);
                if ((*i).function_name != NULL) {
    				free((*i).function_name);
                }
        		if ((*i).library_name != NULL) {
				    free((*i).library_name);
        		}
				if ((*i).coding_type != NULL) {
					free((*i).coding_type);
				}
			}
			services->clear();

			delete (*server);
		}
		servers.clear();
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "free orbConfig");
	free(orbConfig.opt);
	free(orbConfig.transactionFactoryName);
	if (orbConfig.interface != NULL) {
		free(orbConfig.interface);
	}
	orbConfig.opt = NULL;
	orbConfig.transactionFactoryName = NULL;

	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "free txnConfig");
	if (txnConfig.mgrEP != NULL) {
		free(txnConfig.mgrEP);
		free(txnConfig.resourceEP);
		txnConfig.mgrEP = NULL;
		txnConfig.resourceEP = NULL;
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "free mqConfig");
	free(mqConfig.host);
	free(mqConfig.user);
	free(mqConfig.pwd);
	mqConfig.host = NULL;
	mqConfig.user = NULL;
	mqConfig.pwd = NULL;

	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "free cbConfig");
	if(cbConfig.host != NULL) {
		free(cbConfig.host);
		cbConfig.host = NULL;
	}

	Buffers::iterator it;
	for (it = buffers.begin(); it != buffers.end(); ++it) {
		Buffer* buffer = it->second;
		if (buffer != NULL) {
			free(buffer->name);
			buffer->name = NULL;
			buffer->memSize = -1;
			buffer->wireSize = -1;

			//std::vector<ServiceInfo>* services = &(*server)->serviceVector;
			::Attributes::iterator i;
			for (i = buffer->attributes.begin(); i != buffer->attributes.end(); ++i) {
				Attribute* attribute = i->second;
				free(attribute->id);
				attribute->id = NULL;
				free(attribute->type);
				delete attribute;
			}
			buffer->attributes.clear();

			delete buffer;
		}
	}
	buffers.clear();

	readEnvironment = false;
}

char*
AtmiBrokerEnv::getTransportLibrary(char* serviceName) {
	if (servers.size() != 0 && serviceName != NULL) {
		for (ServersInfo::iterator server = servers.begin(); server
				!= servers.end(); server++) {
			std::vector<ServiceInfo>* services = &(*server)->serviceVector;
			for (std::vector<ServiceInfo>::iterator i = services->begin(); i
					!= services->end(); i++) {
				if (ACE_OS::strncmp((*i).serviceName, serviceName, 128) == 0) {
					return (*i).transportLib;
				}
			}
		}
	}

	return NULL;
}

char*
AtmiBrokerEnv::getServiceType(char* serviceName) {
	if (servers.size() != 0 && serviceName != NULL) {
		for (ServersInfo::iterator server = servers.begin(); server
				!= servers.end(); server++) {
			std::vector<ServiceInfo>* services = &(*server)->serviceVector;
			for (std::vector<ServiceInfo>::iterator i = services->begin(); i
					!= services->end(); i++) {
				if (ACE_OS::strncmp((*i).serviceName, serviceName, 128) == 0) {
					return (*i).serviceType;
				}
			}
		}
	}

	return NULL;
}

char*
AtmiBrokerEnv::getCodingType(char* serviceName) {
	if (servers.size() != 0 && serviceName != NULL) {
		for (ServersInfo::iterator server = servers.begin(); server
				!= servers.end(); server++) {
			std::vector<ServiceInfo>* services = &(*server)->serviceVector;
			for (std::vector<ServiceInfo>::iterator i = services->begin(); i
					!= services->end(); i++) {
				if (ACE_OS::strncmp((*i).serviceName, serviceName, 128) == 0) {
					return (*i).coding_type;
				}
			}
		}
	}

	return NULL;
}
const char* AtmiBrokerEnv::getenv(const char* anEnvName, const char* defValue) {
	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "getenv %s" << anEnvName);

	char *envValue = ::getenv(anEnvName);
	if (envValue != NULL) {
		LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "getenv env is %s"
				<< envValue);
		return envValue;
	}

	for (std::vector<envVar_t>::iterator i = envVariableInfoSeq.begin(); i
			!= envVariableInfoSeq.end(); i++) {
		if (strcmp(anEnvName, (*i).name) == 0) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
					(char*) "getenv found env name '%s'" << (*i).value);
			return (*i).value;
		}
	}

	return defValue;
}

char*
AtmiBrokerEnv::getenv(char* anEnvName) {
	const char* val = getenv(anEnvName, NULL);

	if (val != NULL)
		return (char *) val;

	LOG4CXX_ERROR(loggerAtmiBrokerEnv, (char*) "Could not locate: "
			<< anEnvName);
	throw new std::exception();
}

int AtmiBrokerEnv::putenv(char* anEnvNameValue) {
	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "putenv %s" << anEnvNameValue);

	char *p = strchr(anEnvNameValue, '=');
	envVar_t envVar;

	envVar.name = ACE::strndup(anEnvNameValue, (size_t)(p - anEnvNameValue));
	envVar.value = ACE::strndup(p + 1, (int) (strlen(anEnvNameValue)
			- strlen(p)));
	envVariableInfoSeq.push_back(envVar);

	LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "putenv name '" << envVar.name
			<< "' value '" << envVar.value);

	return 1;
}

int AtmiBrokerEnv::readenv() {
	if (!readEnvironment) {
		LOG4CXX_DEBUG(loggerAtmiBrokerEnv, (char*) "readenv");
		if (ENVIRONMENT_DIR != NULL) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
					(char*) "readenv configuration dir: " << ENVIRONMENT_DIR);
		} else {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnv,
					(char*) "readenv default configuration");
		}

		AtmiBrokerEnvParser aAtmiBrokerEnvParser;
		AtmiBrokerEnvHandlers handler(envVariableInfoSeq, configuration);
		
		if (aAtmiBrokerEnvParser.parse(ENVIRONMENT_DIR, &handler)) {
			readEnvironment = true;
		} else {
			if (ENVIRONMENT_DIR != NULL) {
				LOG4CXX_ERROR(loggerAtmiBrokerEnv, (char*) "can not parse "
						<< ENVIRONMENT_DIR << "/btconfig.xml");
			} else {
				LOG4CXX_ERROR(loggerAtmiBrokerEnv,
						(char*) "can not parse btconfig.xml");
			}
			throw std::exception();
		}
	}
	return 1;
}

std::vector<envVar_t>& AtmiBrokerEnv::getEnvVariableInfoSeq() {
	return envVariableInfoSeq;
}


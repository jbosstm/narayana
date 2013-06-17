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

#include <sys/stat.h>
#include <stdio.h>
#include <string.h>
#include <iostream>
#include <stdexcept>

#include "expat.h"

#include "AtmiBrokerEnv.h"
#include "AtmiBrokerEnvXml.h"
#include "XsdValidator.h"

#include "log4cxx/logger.h"
#include "ace/ACE.h"
#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_string.h"
#include "ace/Default_Constants.h"

log4cxx::LoggerPtr loggerAtmiBrokerEnvXml(log4cxx::Logger::getLogger(
		"AtmiBrokerEnvXml"));
xarm_config_t * xarmp = 0;
ServersInfo servers;
ServiceInfo service;
Buffers buffers;

OrbConfig orbConfig = {NULL, NULL, NULL};
TxnConfig txnConfig = {NULL, NULL};
MqConfig mqConfig = {
    NULL,	// host
    0,	// port; 
    NULL,	// user;
    NULL,	// pwd;
    -1,	// destinationTimeout;
    1,	// requestTimeout;
    0,	// timeToLive;
    0,	// noReplyTimeToLive;
};

CallbackServerConfig cbConfig = {
	NULL,  // host
	0,     // port
};

char domain[30];
char* queue_name;
char* transFactoryId;

static char last_element[50];
static char last_value[1024];

static char element[50];
static char value[1024];

static int depth = 0;

static int envVariableCount = 0;

static bool processingXaResource = false;
static bool processingEnvVariable = false;
static char* configuration = NULL;
static char* currentBufferName = NULL;

static int MEM_CHAR_SIZE = sizeof(char);//1;
static int MEM_LONG_SIZE = sizeof(long);//8;
static int MEM_INT_SIZE = sizeof(int);//4;
static int MEM_SHORT_SIZE = sizeof(short);//2;
static int MEM_FLOAT_SIZE = sizeof(float);//INT_SIZE;
static int MEM_DOUBLE_SIZE = sizeof(double);//LONG_SIZE;

static int WIRE_CHAR_SIZE = 1;
static int WIRE_LONG_SIZE = 8;
static int WIRE_INT_SIZE = 4;
static int WIRE_SHORT_SIZE = 2;
static int WIRE_FLOAT_SIZE = 4;
static int WIRE_DOUBLE_SIZE = 8;

static XML_Parser parser;
static enum XML_Error parseErr = XML_ERROR_NONE;

static void abortParser() {
	// disable further parsing
	XML_SetElementHandler (parser, NULL, NULL);
	XML_SetCharacterDataHandler(parser, NULL);
	parseErr = XML_ERROR_ABORTED;
}

AtmiBrokerEnvXml::AtmiBrokerEnvXml() {
	depth = 0;
	envVariableCount = 0;

	processingXaResource = false;
	processingEnvVariable = false;
	currentBufferName = NULL;
	configuration = NULL;
}

AtmiBrokerEnvXml::~AtmiBrokerEnvXml() {
}

static int warnCnt = 0;
static void warn(const char * reason) {
	if (warnCnt++ == 0)
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) reason);
}

/**
 * Duplicate a value. If the value contains an expression of the for ${ENV}
 * then ENV is interpreted as an environment variable and ${ENV} is replaced
 * by its value (if ENV is not set it is replaced by null string).
 *
 * WARNING: only the first such occurence is expanded. TODO generalise the function
 */
static char * copy_value_impl(char *value);

static char * XMLCALL copy_value(const char *value) {
    LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, "copy_value" << value);
    return copy_value_impl(strdup(value));
}

static char * copy_value_impl(char *value) {
    LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, "copy_value_impl" << value);
	char *s = (char *) strchr(value, '$');
	char *e;

	if (s && *(s + 1) == '{' && (e = (char *) strchr(s, '}'))) {
		size_t esz = e - s - 2;
		char *en = ACE::strndup(s + 2, esz);
		char *ev = ACE_OS::getenv(en); /* ACE_OS::getenv(en);*/
		char *pr = ACE::strndup(value, (s - value));
		size_t rsz;
		char *v;

		if (ev == NULL) {
			LOG4CXX_WARN(loggerAtmiBrokerEnvXml, (char*) "env variable is unset: " << en);
			ev = (char *) "";
		}

		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char *) "expanding env: "
				<< (s + 2) << (char *) " and e=" << e << (char *) " and en="
				<< en << (char *) " and pr=" << pr << (char *) " and ev=" << ev);
		e += 1;
		rsz = ACE_OS::strlen(pr) + ACE_OS::strlen(e) + ACE_OS::strlen(ev) + 1; /* add 1 for null terminator */
		v = (char *) malloc(rsz);
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, "copy_value_impl malloc");

		ACE_OS::snprintf(v, rsz, "%s%s%s", pr, ev, e);
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, value << (char*) " -> " << v);

		free(en);
		free(pr);

		LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, "Freeing previous value" << value);
		free(value);
		return copy_value_impl(v);
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, "Returning value" << value);
	return value;
}



static bool applicable_config(char *config, const char *attribute) {
	if (config == NULL || ACE_OS::strlen(config) == 0) {
		// see if it is set in the environment
		if ((config = ACE_OS::getenv("BLACKTIE_CONFIGURATION")) == 0)
			return false;
	}

	char * conf = copy_value(attribute);
	bool rtn = strcmp(conf, config);

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "comparing " << conf
			<< " with " << config);
	free(conf);

	return (rtn == 0);
}

static bool checkService(char* serverName, const char* serviceName) {
	for(unsigned int i = 0; i < servers.size(); i ++) {
		if(ACE_OS::strcmp(serverName, servers[i]->serverName) != 0) {
			for(unsigned int j = 0; j < servers[i]->serviceVector.size(); j ++) {
				if(ACE_OS::strcmp(serviceName, servers[i]->serviceVector[j].serviceName) == 0)
					return true;
			}
		}
	}
	return false;
}

static void XMLCALL startElement
(void *userData, const char *name, const char **atts) {
	std::vector<envVar_t>* aEnvironmentStructPtr = (std::vector<envVar_t>*) userData;

	LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processing element " << name);
	if (strcmp(name, "ENVIRONMENT xmnls") == 0 || strcmp(name, "ENVIRONMENT") == 0) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "starting to read");
	} else if (strcmp(name, "ORB") == 0) {
		for(int i = 0; atts[i]; i += 2) {
			if(strcmp(atts[i], "OPT") == 0) {
				orbConfig.opt = copy_value(atts[i+1]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set opt: " << orbConfig.opt);
			} else if(strcmp(atts[i], "TRANS_FACTORY_ID") == 0) {
				orbConfig.transactionFactoryName = copy_value(atts[i+1]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set tFN: " << orbConfig.transactionFactoryName);
			} else if(strcmp(atts[i], "INTERFACE") == 0) {
				orbConfig.interface = copy_value(atts[i+1]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set tFN: " << orbConfig.interface);
			}
		}
	} else if (strcmp(name, "TXN_CFG") == 0) {
		for(int i = 0; atts[i]; i += 2) {
			if(strcmp(atts[i], "MGR_URL") == 0) {
				txnConfig.mgrEP = copy_value(atts[i+1]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "txn manager URL: " << txnConfig.mgrEP);
			} else if(strcmp(atts[i], "RES_EP") == 0) {
				txnConfig.resourceEP = copy_value(atts[i+1]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "resource host:port: " << txnConfig.resourceEP);
			}
		}
	} else if (strcmp(name, "MQ") == 0) {
		for(int i = 0; atts[i]; i += 2) {
			if(strcmp(atts[i], "HOST") == 0) {
				mqConfig.host = copy_value(atts[i+1]);
			} else if(strcmp(atts[i], "PORT") == 0) {
				mqConfig.port = atoi(atts[i+1]);
			} else if(strcmp(atts[i], "USER") == 0) {
				mqConfig.user = copy_value(atts[i+1]);
			} else if(strcmp(atts[i], "PASSWORD") == 0) {
				mqConfig.pwd = copy_value(atts[i+1]);
			} else if(strcmp(atts[i], "DESTINATION_TIMEOUT") == 0) {
				mqConfig.destinationTimeout = atoi(atts[i+1]);
			} else if(strcmp(atts[i], "RECEIVE_TIMEOUT") == 0) {
				mqConfig.requestTimeout = atoi(atts[i+1]);
			} else if(strcmp(atts[i], "TIME_TO_LIVE") == 0) {
				mqConfig.timeToLive = atoi(atts[i+1]);
			} else if(strcmp(atts[i], "NOREPLY_TIME_TO_LIVE") == 0) {
				mqConfig.noReplyTimeToLive = atoi(atts[i+1]);
			}
		}
	} else if (strcmp(name, "SOCKETSERVER") == 0) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processing SOCKETSERVER");
		for(int i = 0; atts[i]; i += 2) {
			if(strcmp(atts[i], "HOST") == 0) {
				cbConfig.host = copy_value(atts[i+1]);
			} else if(strcmp(atts[i], "PORT") == 0) {
				cbConfig.port = atoi(atts[i+1]);
			}
		}
	} else if (strcmp(name, "SERVER") == 0) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processing SERVER");

		ServerInfo* server = new ServerInfo;
		server->function_name = NULL;
		server->done_function_name = NULL;
		server->library_name = NULL;
		for(int i = 0; atts[i]; i += 2) {
			if(atts[i] && strcmp(atts[i], "name") == 0) {
				server->serverName = copy_value(atts[i+1]);
			} else if(atts[i] && strcmp(atts[i], "init_function") == 0) {
				server->function_name = copy_value(atts[i+1]);
			} else if(atts[i] && strcmp(atts[i], "done_function") == 0) {
				server->done_function_name = copy_value(atts[i+1]);
			}
		}

		servers.push_back(server);
	} else if (strcmp(name, "INIT_FUNCTION_LIBRARY_NAME") == 0) {
		if(atts != 0 && atts[0] && strcmp(atts[0], "configuration") == 0) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "comparing" << atts[1] << " with " << configuration);
			if (strcmp(atts[1], configuration) == 0) {
				servers.back()->library_name = copy_value(atts[3]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processed INIT_FUNCTION_LIBRARY_NAME: " << servers.back()->library_name);
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "CONFIGURATION NOT APPLICABLE FOR LIBRARY_NAME: " << atts[1]);
			}
		}
	} else if (strcmp(name, "XA_RESOURCE") == 0) {
		if(strcmp(atts[0], "configuration") == 0 && applicable_config(configuration, atts[1])) {

			LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processing xaresource");
			processingXaResource = true;
			xarm_config_t *p;
			if ((p = (xarm_config_t *) malloc(sizeof(xarm_config_t))) == 0) {
				warnCnt = 0;
				warn("out of memory");
			} else {
				(void *) memset(p, 0, sizeof(xarm_config_t));

				if (xarmp == 0) {
					p->head = p;
				} else {
					xarmp->next = p;
					p->head = xarmp->head;
				}
				xarmp = p;
			}
		} else {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "CONFIGURATION NOT APPLICABLE FOR XA_RESOURCE: " << atts[1]);
		}
	} else if (strcmp(name, "ENV_VARIABLE") == 0) {
		if(atts != 0 && atts[0] && strcmp(atts[0], "configuration") == 0) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "comparing" << atts[1] << " with " << configuration);
			if (strcmp(atts[1], configuration) == 0) {
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processing ENV_VARIABLE");
				processingEnvVariable = true;
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "CONFIGURATION NOT APPLICABLE FOR ENV_VARIABLE: " << atts[1]);
			}
		} else {
			processingEnvVariable = true;
		}

		if (processingEnvVariable) {
			envVariableCount++;
			envVar_t envVar;
			(*aEnvironmentStructPtr).push_back(envVar);
		}
	} else if (strcmp(name, "BUFFER") == 0) {
		char * bufferName = copy_value(atts[1]);
		Buffer* buffer = buffers[bufferName];
		if (buffer == NULL) {
			currentBufferName = bufferName;
			Buffer* buffer = new Buffer();
			buffer->name = currentBufferName;
			buffer->wireSize = 0;
			buffer->memSize = 0;
			buffer->lastPad = 0;
			buffers[buffer->name] = buffer;
		} else {
			LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "Duplicate buffer detected: " << currentBufferName);
			free (bufferName);
			currentBufferName = NULL;
		}
	} else if (strcmp(name, "ATTRIBUTE") == 0) {
		if (currentBufferName != NULL) {
			Buffer* buffer = buffers[currentBufferName];
			Attribute* attribute = new Attribute();
			attribute->id = NULL;
			attribute->type = NULL;
			attribute->count = 0;
			attribute->length = 0;
			attribute->wirePosition = 0;
			attribute->memPosition = 0;
			for(int i = 0; atts[i]; i += 2) {
				if(strcmp(atts[i], "id") == 0) {
					attribute->id = copy_value(atts[i+1]);
				} else if(strcmp(atts[i], "type") == 0) {
					attribute->type = copy_value(atts[i+1]);
				} else if(strcmp(atts[i], "arrayCount") == 0) {
					attribute->count = atoi(atts[i+1]);
				} else if(strcmp(atts[i], "arrayLength") == 0) {
					attribute->length = atoi(atts[i+1]);
				}
			}

			int memTypeSize = -1;
			int wireTypeSize = -1;
			Attribute* toCheck = buffer->attributes[attribute->id];
			bool fail = false;
			bool isDbl = false;
			if (toCheck == NULL) {
				// short, int, long, float, double, char
				if (strcmp(attribute->type, "short") == 0) {
					memTypeSize = MEM_SHORT_SIZE;
					wireTypeSize = WIRE_SHORT_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "int") == 0) {
					memTypeSize = MEM_INT_SIZE;
					wireTypeSize = WIRE_INT_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "long") == 0) {
					memTypeSize = MEM_LONG_SIZE;
					wireTypeSize = WIRE_LONG_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "float") == 0) {
					memTypeSize = MEM_FLOAT_SIZE;
					wireTypeSize = WIRE_FLOAT_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "double") == 0) {
					isDbl = true;
					memTypeSize = MEM_DOUBLE_SIZE;
					wireTypeSize = WIRE_DOUBLE_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "char") == 0) {
					memTypeSize = MEM_CHAR_SIZE;
					wireTypeSize = WIRE_CHAR_SIZE;
					attribute->memSize = memTypeSize;
					attribute->wireSize = wireTypeSize;
				} else if (strcmp(attribute->type, "char[]") == 0) {
					memTypeSize = MEM_CHAR_SIZE;
					wireTypeSize = WIRE_CHAR_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "short[]") == 0) {
					memTypeSize = MEM_SHORT_SIZE;
					wireTypeSize = WIRE_SHORT_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "int[]") == 0) {
					memTypeSize = MEM_INT_SIZE;
					wireTypeSize = WIRE_INT_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "long[]") == 0) {
					memTypeSize = MEM_LONG_SIZE;
					wireTypeSize = WIRE_LONG_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "float[]") == 0) {
					memTypeSize = MEM_FLOAT_SIZE;
					wireTypeSize = WIRE_FLOAT_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "double[]") == 0) {
					isDbl = true;
					memTypeSize = MEM_DOUBLE_SIZE;
					wireTypeSize = WIRE_DOUBLE_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					attribute->memSize = memTypeSize * attribute->length;
					attribute->wireSize = wireTypeSize * attribute->length;
				} else if (strcmp(attribute->type, "char[][]") == 0) {
					memTypeSize = MEM_CHAR_SIZE;
					wireTypeSize = WIRE_CHAR_SIZE;
					if (attribute->length == 0) {
						attribute->length = 1;
					}
					if (attribute->count == 0) {
						attribute->count = 1;
					}
					attribute->memSize = memTypeSize * attribute->length * attribute->count;
					attribute->wireSize = wireTypeSize * attribute->length * attribute->count;
				} else {
					LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "Unknown attribute type: " << attribute->type);
					fail = true;
				}

				if (!fail) {
					buffer->attributes[attribute->id] = attribute;

					// doubles are aligned on a (long) word boundary
#ifndef WIN32
					if (isDbl) {
						memTypeSize = MEM_LONG_SIZE;
					}
#endif

					// Extend the buffer by the required extra buffer size
					if (buffer->lastPad < memTypeSize) {
							buffer->lastPad = memTypeSize;
					}

					// advance to then next alignment boundary
					int memAlign = buffer->memSize % memTypeSize;

					buffer->memSize = buffer->memSize + (memAlign == 0 ? 0 : (memTypeSize - memAlign));
					attribute->memPosition = buffer->memSize;
					attribute->wirePosition = buffer->wireSize;
					buffer->wireSize = buffer->wireSize + attribute->wireSize;
					buffer->memSize = buffer->memSize + attribute->memSize;
				} else {
					LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "Cleaning attribute: " << attribute->id);
					free(attribute->id);
					free(attribute->type);
					delete attribute;
				}
			} else {
				LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "Duplicate attribute detected: " << attribute->id);
				free(attribute->id);
				free(attribute->type);
				delete attribute;
			}
		} else {
			LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "No buffer is being processed");
		}
	} else if(strcmp(name, "SERVICE") == 0) {
		if(atts != 0) {
			char  adm[16];
			char* server;

			server = servers.back()->serverName;
			memset(&service, 0, sizeof(ServiceInfo));
			ACE_OS::strcpy(adm, ".");

			service.serviceType = NULL;
			service.coding_type = NULL;
			service.transportLib = NULL;
			service.advertised = false;
			service.conversational = false;
			service.externally_managed_destination = false;
			service.poolSize = 1;

			for(int i = 0; atts[i]; i += 2) {
				if(strcmp(atts[i], "name") == 0) {
					if(ACE_OS::strstr(atts[i+1], adm) || 
					   ACE_OS::strcmp(atts[i+1], "BTStompAdmin") == 0 ||
					   ACE_OS::strcmp(atts[i+1], "BTDomainAdmin") == 0) {
						LOG4CXX_WARN(loggerAtmiBrokerEnvXml, (char*) "Can not define " << atts[i+1]);
						// disable further parsing
						abortParser();

						return;
					}

					if(checkService(server, atts[i+1])) {
						LOG4CXX_WARN(loggerAtmiBrokerEnvXml, (char*) "Can not define Same Service " << atts[i+1]);
						// disable further parsing
						abortParser();

						return;
					}

					service.serviceName = copy_value(atts[i+1]);
					LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set name: " << service.serviceName);
				} else if(strcmp(atts[i], "function_name") == 0) {
					service.function_name = strdup(atts[i+1]);
					LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set function_name: " << service.function_name);
				} else if(strcmp(atts[i], "advertised") == 0) {
					if(strcmp(atts[i+1], "true") == 0) {
						service.advertised = true;
					} else {
						service.advertised = false;
					}
				} else if(strcmp(atts[i], "conversational") == 0) {
					if(strcmp(atts[i+1], "true") == 0) {
						service.conversational = true;
					} else {
						service.conversational = false;
					}
				} else if(strcmp(atts[i], "externally-managed-destination") == 0) {
					if(strcmp(atts[i+1], "true") == 0) {
						service.externally_managed_destination = true;
					} else {
						service.externally_managed_destination = false;
					}
				} else if (strcmp(atts[i], "size") == 0) {
					service.poolSize = (short) atol(atts[i+1]);
					LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "storing size " << service.poolSize);
				} else if(strcmp(atts[i], "type") == 0) {
					service.serviceType = copy_value(atts[i+1]);
					LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set type: " << service.serviceType);
				} else if(strcmp(atts[i], "coding_type") == 0) {
					service.coding_type = copy_value(atts[i+1]);
					LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "set coding type: " << service.coding_type);
				}
			}


			if(service.advertised && service.function_name == NULL) {
				LOG4CXX_WARN(loggerAtmiBrokerEnvXml, (char*) "Can not mark a service as advertised if it does not define a function_name" << service.serviceName);
				// disable further parsing
				abortParser();

				return;
			}

			if(service.serviceType == NULL) {
				service.serviceType = strdup("queue");
			}

			LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "setting transportlib");
#ifdef WIN32
			service.transportLib = strdup("atmibroker-hybrid.dll");
#else
			service.transportLib = strdup("libatmibroker-hybrid.so");
#endif
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "set transportlib: " << service.transportLib);
		}
	} else if (strcmp(name, "LIBRARY_NAME") == 0) {
		if(atts != 0 && atts[0] && strcmp(atts[0], "configuration") == 0) {
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "comparing" << atts[1] << " with " << configuration);
			if (strcmp(atts[1], configuration) == 0) {
				service.library_name = copy_value(atts[3]);
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "processed LIBRARY_NAME: " << service.library_name);
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "CONFIGURATION NOT APPLICABLE FOR LIBRARY_NAME: " << atts[1]);
			}
		}
	}
	strcpy(element, name);
	strcpy(value, "");

	depth += 1;
}

static void XMLCALL endElement
(void *userData, const char *name) {
	std::vector<envVar_t>* aEnvironmentStructPtr = (std::vector<envVar_t>*) userData;

	strcpy(last_element, name);
	strcpy(last_value, value);

	LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "storing element: " << last_element);

	if (strcmp(last_element, "DOMAIN") == 0) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "storing domain value: " << last_value);
		strcpy(domain, last_value);
	} else if (strcmp(last_element, "XA_RESOURCE") == 0) {
		processingXaResource = false;
	} else if (strcmp(last_element, "XA_RESOURCE_MGR_ID") == 0) {
		if (processingXaResource) {
			xarmp->resourceMgrId = atol(last_value);
		}
	} else if (strcmp(last_element, "XA_RESOURCE_NAME") == 0) {
		if (processingXaResource) {
			xarmp->resourceName = copy_value(last_value);
		}
	} else if (strcmp(last_element, "XA_OPEN_STRING") == 0) {
		if (processingXaResource) {
			xarmp->openString = copy_value(last_value);
		}
	} else if (strcmp(last_element, "XA_CLOSE_STRING") == 0) {
		if (processingXaResource) {
			xarmp->closeString = copy_value(last_value);
		}
	} else if (strcmp(last_element, "XA_SWITCH") == 0) {
		if (processingXaResource) {
			xarmp->xasw = copy_value(last_value);
		}
	} else if (strcmp(last_element, "XA_LIB_NAME") == 0) {
		if (processingXaResource) {
			xarmp->xalib = copy_value(last_value);
		}
	} else if (strcmp(last_element, "ENV_VARIABLE") == 0) {
		if (processingEnvVariable) {
			int index = envVariableCount - 1;
			LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "stored EnvVariable at index %d" << index);
		}
		processingEnvVariable = false;
	} else if (strcmp(last_element, "NAME") == 0) {
		if (processingEnvVariable) {
			int index = envVariableCount - 1;
			(*aEnvironmentStructPtr)[index].name = copy_value(last_value);
			LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "stored EnvName %s at index %d" << last_value << index);
		}
	} else if (strcmp(last_element, "VALUE") == 0) {
		if (processingEnvVariable) {
			int index = envVariableCount - 1;
			(*aEnvironmentStructPtr)[index].value = copy_value(last_value);
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "stored Env Value %s at index %d" << last_value << index);
		}
	} else if (strcmp(last_element, "BUFFER") == 0) {
		if (currentBufferName != NULL) {
			Buffer* buffer = buffers[currentBufferName];
			int currentSize = buffer->memSize;
			if (currentSize != 0) {
				if (currentSize % buffer->lastPad != 0) {
					buffer->lastPad = buffer->lastPad - (currentSize % buffer->lastPad);
					buffer->memSize = currentSize + buffer->lastPad;
				} else {
					buffer->lastPad = 0;
				}
			} else {
				buffer->lastPad = 1;
				buffer->memSize = 1;
			}
			currentBufferName = NULL;
		}
	} else if (strcmp(last_element, "SERVICE") == 0) {
		servers.back()->serviceVector.push_back(service);
	}
	depth -= 1;
}

static void XMLCALL characterData
(void *userData, const char *cdata, int len) {
	int i = 0;
	int j = 0;
	int priorLength = strlen(value);

	i = priorLength;
	for (; i < len + priorLength; i++, j++) {
		value[i] = cdata[j];
	}
	value[i] = '\0';
	if (value[0] == '\n') {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "value starts with newline (may be other character data)");
	} else {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "value is :" << value);
	}
}

bool AtmiBrokerEnvXml::parseXmlDescriptor(
		std::vector<envVar_t>* aEnvironmentStructPtr,
		const char * configurationDir, char * conf) {

	char aDescriptorFileName[256];

	if (configurationDir != NULL) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "read env from dir: "
				<< configurationDir);
		ACE_OS::snprintf(aDescriptorFileName, 256, "%s"ACE_DIRECTORY_SEPARATOR_STR_A"btconfig.xml",
				configurationDir);
		LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml,
				(char*) "in parseXmlDescriptor() " << aDescriptorFileName);
	} else {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvXml,
				(char*) "read env from default file");
		ACE_OS::strcpy(aDescriptorFileName, "btconfig.xml");
	}
	configuration = conf;

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, "BLACKTIE_CONFIGURATION: " << configuration);

	bool toReturn = true;
	char schemaPath[256];
	char* schemaDir;

	schemaDir = ACE_OS::getenv("BLACKTIE_SCHEMA_DIR");
	if (schemaDir) {
		ACE_OS::snprintf(schemaPath, 256, "%s"ACE_DIRECTORY_SEPARATOR_STR_A"btconfig.xsd", schemaDir);
	} else {
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml,
				(char*) "BLACKTIE_SCHEMA_DIR is not set, cannot validate configuration");
		return false;
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "schemaPath is "
			<< schemaPath);

	XsdValidator validator;
	if (validator.validate(schemaPath, aDescriptorFileName) == false) {
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml,
				(char*) "btconfig.xml did not validate against btconfig.xsd");
		return false;
	}
	struct stat s; /* file stats */
	FILE *aDescriptorFile = fopen(aDescriptorFileName, "r");

	if (!aDescriptorFile) {
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml,
				(char*) "loadfile: fopen failed on %s" << aDescriptorFileName);
		return false;
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml, (char*) "read file %p"
			<< aDescriptorFile);

	/* Use fstat to obtain the file size */
	if (fstat(fileno(aDescriptorFile), &s) != 0) {
		/* fstat failed */
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml,
				(char*) "loadfile: fstat failed on %s" << aDescriptorFileName);
		return false;
	}
	if (s.st_size == 0) {
		LOG4CXX_ERROR(loggerAtmiBrokerEnvXml,
				(char*) "loadfile: file %s is empty" << aDescriptorFileName);
	}
	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml,
			(char*) "loadfile: file %s is %d long" << aDescriptorFileName
					<< s.st_size);

	char *buf = (char *) malloc(sizeof(char) * s.st_size + 1);
	if (!buf) {
		/* malloc failed */
		LOG4CXX_ERROR(
				loggerAtmiBrokerEnvXml,
				(char*) "loadfile: Could not allocate enough memory to load file %s"
						<< aDescriptorFileName);
		return false;
	}
	memset(buf, '\0', s.st_size + 1);
	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml,
			(char*) "loadfile: Allocated enough memory to load file %d"
					<< s.st_size);

	parser = XML_ParserCreate(NULL);
	parseErr = XML_ERROR_NONE;

	int done;
	strcpy(element, "");
	strcpy(value, "");
	XML_SetUserData(parser, aEnvironmentStructPtr);
	XML_SetElementHandler(parser, startElement, endElement);
	XML_SetCharacterDataHandler(parser, characterData);
	try {
		do {
			LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "reading file");
			size_t len = fread(buf, 1, s.st_size, aDescriptorFile);
			done = len < sizeof(buf);
			if (len > 0) {
				LOG4CXX_TRACE(loggerAtmiBrokerEnvXml, (char*) "buf is " << buf);

				if (XML_Parse(parser, buf, len, done) == XML_STATUS_ERROR ||
					parseErr != XML_ERROR_NONE) {
					LOG4CXX_ERROR(loggerAtmiBrokerEnvXml, (char*) "%d at line %d"
							<< XML_ErrorString(XML_GetErrorCode(parser))
							<< XML_GetCurrentLineNumber(parser));
					toReturn = false;
					break;
				}
			}
		} while (!done);
	} catch (...) {
		free(buf);
		XML_ParserFree(parser);
		fflush(aDescriptorFile);
		fclose(aDescriptorFile);
		throw;
	}

	free(buf);
	XML_ParserFree(parser);

	fflush(aDescriptorFile);
	fclose(aDescriptorFile);

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvXml,
			(char*) "leaving parseXmlDescriptor() %s" << aDescriptorFileName);

	if (warnCnt) {
		warnCnt = 0;
		return false;
	}

	return toReturn;
}

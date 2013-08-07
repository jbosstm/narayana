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

#include <xercesc/sax2/Attributes.hpp>
#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/sax/SAXException.hpp>


#include "AtmiBrokerEnv.h"
#include "AtmiBrokerEnvXml.h"
#include "AtmiBrokerEnvHandlers.h"


#include "log4cxx/logger.h"
#include "ace/ACE.h"
#include "ace/OS_NS_stdlib.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_string.h"
#include "ace/Default_Constants.h"

#include <string>
using namespace std;

log4cxx::LoggerPtr loggerAtmiBrokerEnvHandlers(log4cxx::Logger::getLogger(
		"AtmiBrokerEnvHandlers"));
log4cxx::LoggerPtr AtmiBrokerEnvHandlers::logger(log4cxx::Logger::getLogger("AtmiBrokerEnvHandlers"));


static int warnCnt = 0;
static void warn(const char * reason) {
	if (warnCnt++ == 0)
		LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) reason);
}

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
static char* currentBufferName = NULL;

/**
 * Duplicate a value. If the value contains an expression of the for ${ENV}
 * then ENV is interpreted as an environment variable and ${ENV} is replaced
 * by its value (if ENV is not set it is replaced by null string).
 *
 * WARNING: only the first such occurence is expanded. TODO generalise the function
 */
static char * copy_value_impl(char *value);

static char * copy_value(const XMLCh *value) {
    LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, "copy_value: " << value);
    return copy_value_impl(strdup(StrX(value).localForm()));
}

static char * copy_value(const char *value) {
    LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, "copy_value: " << value);
    return copy_value_impl(strdup(value));
}

static char * copy_value_impl(char *value) {
    LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, "copy_value_impl: " << value);
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
			LOG4CXX_WARN(loggerAtmiBrokerEnvHandlers, (char*) "env variable is unset: " << en);
			ev = (char *) "";
		}

		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char *) "expanding env: "
				<< (s + 2) << (char *) " and e=" << e << (char *) " and en="
				<< en << (char *) " and pr=" << pr << (char *) " and ev=" << ev);
		e += 1;
		rsz = ACE_OS::strlen(pr) + ACE_OS::strlen(e) + ACE_OS::strlen(ev) + 1; /* add 1 for null terminator */
		v = (char *) malloc(rsz);
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, "copy_value_impl malloc");

		ACE_OS::snprintf(v, rsz, "%s%s%s", pr, ev, e);
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, value << (char*) " -> " << v);

		free(en);
		free(pr);

		LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, "Freeing previous value" << value);
		free(value);
		return copy_value_impl(v);
	}

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, "Returning value" << value);
	return value;
}



static bool applicable_config(const char *config, const char *attribute) {
       
	if (config == NULL || ACE_OS::strlen(config) == 0) {
		// see if it is set in the environment
		if ((config = ACE_OS::getenv("BLACKTIE_CONFIGURATION")) == 0)
			return false;
	}

	char * conf = copy_value(attribute);
	bool rtn = strcmp(conf, config);

	LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "comparing " << conf
			<< " with " << config);
	free(conf);

	return (rtn == 0);
}

static bool checkService(char* serverName, const char* serviceName) {
	for(unsigned int i = 0; i < servers.size(); i ++) {
		if(ACE_OS::strcmp(serverName, servers[i]->serverName) != 0) {
			for(unsigned int j = 0; j < servers[i]->serviceVector.size(); j ++) {
				if(ACE_OS::strcmp(serviceName, servers[i]->serviceVector[j].serviceName) == 0 && ACE_OS::strcmp("topic", servers[i]->serviceVector[j].serviceType) != 0)
					return true;
			}
		}
	}
	return false;
}

AtmiBrokerEnvHandlers::AtmiBrokerEnvHandlers(std::vector<envVar_t>& iEnv, const char* conf) {
	depth = 0;
	envVariableCount = 0;

	processingXaResource = false;
	processingEnvVariable = false;
	currentBufferName = NULL;
	configuration = conf;
	aEnvironmentStructPtr = &iEnv;
}

AtmiBrokerEnvHandlers::~AtmiBrokerEnvHandlers() {
}

void AtmiBrokerEnvHandlers::startElement(const XMLCh* const uri, const XMLCh* const localName, const XMLCh* const qname, const xercesc::Attributes& attributes) {

	StrX name = localName;
		
	LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processing element " << name);
	if (name == "ENVIRONMENT xmnls" || name == "ENVIRONMENT") {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "starting to read");
	} else if (name == "ORB") {
		for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
		        StrX attvalue = attributes.getLocalName(i);
			if(attvalue == "OPT") {
				orbConfig.opt = copy_value(attributes.getValue(i));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set opt: " << orbConfig.opt);
			} else if(attvalue == "TRANS_FACTORY_ID") {
				orbConfig.transactionFactoryName = copy_value(attributes.getValue(i));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set tFN: " << orbConfig.transactionFactoryName);
			} else if(attvalue == "INTERFACE") {
				orbConfig.interface = copy_value(attributes.getValue(i));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set tFN: " << orbConfig.interface);
			}
		}
	} else if (name == "TXN_CFG") {
		for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
		        StrX attvalue = attributes.getLocalName(i);
			if(attvalue == "MGR_URL") {
				txnConfig.mgrEP = copy_value(attributes.getValue(i));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "txn manager URL: " << txnConfig.mgrEP);
			} else if(attvalue == "RES_EP") {
				txnConfig.resourceEP = copy_value(attributes.getValue(i));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "resource host:port: " << txnConfig.resourceEP);
			}
		}
	} else if (name == "MQ") {
		for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
		        StrX attvalue = attributes.getLocalName(i);
			if(attvalue == "HOST") {
				mqConfig.host = copy_value(attributes.getValue(i));
			} else if(attvalue == "PORT") {
				mqConfig.port = atoi(StrX(attributes.getValue(i)).localForm());
			} else if(attvalue == "USER") {
				mqConfig.user = copy_value(attributes.getValue(i));
			} else if(attvalue == "PASSWORD") {
				mqConfig.pwd = copy_value(attributes.getValue(i));
			} else if(attvalue == "DESTINATION_TIMEOUT") {
				mqConfig.destinationTimeout = atoi(StrX(attributes.getValue(i)).localForm());
			} else if(attvalue == "RECEIVE_TIMEOUT") {
				mqConfig.requestTimeout = atoi(StrX(attributes.getValue(i)).localForm());
			} else if(attvalue == "TIME_TO_LIVE") {
				mqConfig.timeToLive = atoi(StrX(attributes.getValue(i)).localForm());
			} else if(attvalue == "NOREPLY_TIME_TO_LIVE") {
				mqConfig.noReplyTimeToLive = atoi(StrX(attributes.getValue(i)).localForm());
			}
		}
	} else if (name == "SOCKETSERVER") {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processing SOCKETSERVER");
		for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
			StrX attvalue = attributes.getLocalName(i);
			if(attvalue == "HOST") {
				cbConfig.host = copy_value(attributes.getValue(i));
			} else if(attvalue == "PORT") {
				cbConfig.port = atoi(StrX(attributes.getValue(i)).localForm());
			}
		}
	} else if (name == "SERVER") {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processing SERVER");

		ServerInfo* server = new ServerInfo;
		server->function_name = NULL;
		server->done_function_name = NULL;
		server->library_name = NULL;
		server->xa = true;
		for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
		        StrX attvalue = attributes.getLocalName(i);
			if(attvalue == "name") {
				server->serverName = copy_value(attributes.getValue(i));
			} else if(attvalue == "init_function") {
				server->function_name = copy_value(attributes.getValue(i));
			} else if(attvalue == "done_function") {
				server->done_function_name = copy_value(attributes.getValue(i));
			} else if(attvalue == "xa") {
                                if(StrX(attributes.getValue(i)) == "true") {
                                        server->xa = true;
                                } else {
                                        server->xa = false;
                                }
			}
		}

		servers.push_back(server);
	} else if (name == "INIT_FUNCTION_LIBRARY_NAME") {
		if(attributes.getLength() > 0 && StrX(attributes.getLocalName(0)) == "configuration") {
		        StrX attvalue = attributes.getValue((XMLSize_t)0);
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "comparing" << attvalue << " with " << configuration);
			if (attvalue == configuration) {
				servers.back()->library_name = copy_value(attributes.getValue(1));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processed INIT_FUNCTION_LIBRARY_NAME: " << servers.back()->library_name);
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "CONFIGURATION NOT APPLICABLE FOR LIBRARY_NAME: " << attributes.getValue(1));
			}
		}
	} else if (name == "XA_RESOURCE") {
	        StrX attname = attributes.getLocalName(0);
		StrX attvalue = attributes.getValue((XMLSize_t)0);
		if(attname == "configuration" && applicable_config(configuration, attvalue.localForm())) {

			LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processing xaresource");
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
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "CONFIGURATION NOT APPLICABLE FOR XA_RESOURCE: " << attvalue);
		}
	} else if (name == "ENV_VARIABLE") {
		if(attributes.getLength() > 0 && StrX(attributes.getLocalName(0)) == "configuration") {
		        StrX attname = attributes.getValue((XMLSize_t)0);
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "comparing " << attname << " with " << configuration);
			if (attname == configuration) {
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processing ENV_VARIABLE");
				processingEnvVariable = true;
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "CONFIGURATION NOT APPLICABLE FOR ENV_VARIABLE: " << attname);
			}
		} else {
			processingEnvVariable = true;
		}

		if (processingEnvVariable) {
			envVariableCount++;
			envVar_t envVar;
			(*aEnvironmentStructPtr).push_back(envVar);
		}
	} else if (name == "BUFFER") {
		char * bufferName = copy_value(attributes.getValue((XMLSize_t)0));
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
			LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) "Duplicate buffer detected: " << currentBufferName);
			free (bufferName);
			currentBufferName = NULL;
		}
	} else if (name == "ATTRIBUTE") {
		if (currentBufferName != NULL) {
			Buffer* buffer = buffers[currentBufferName];
			Attribute* attribute = new Attribute();
			attribute->id = NULL;
			attribute->type = NULL;
			attribute->count = 0;
			attribute->length = 0;
			attribute->wirePosition = 0;
			attribute->memPosition = 0;
			for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
			        StrX attvalue = attributes.getLocalName(i);
				if(attvalue == "id") {
					attribute->id = copy_value(attributes.getValue(i));
				} else if(attvalue == "type") {
					attribute->type = copy_value(attributes.getValue(i));
				} else if(attvalue == "arrayCount") {
					attribute->count = atoi(StrX(attributes.getValue(i)).localForm());
				} else if(attvalue == "arrayLength") {
					attribute->length = atoi(StrX(attributes.getValue(i)).localForm());
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
					LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) "Unknown attribute type: " << attribute->type);
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
					LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) "Cleaning attribute: " << attribute->id);
					free(attribute->id);
					free(attribute->type);
					delete attribute;
				}
			} else {
				LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) "Duplicate attribute detected: " << attribute->id);
				free(attribute->id);
				free(attribute->type);
				delete attribute;
			}
		} else {
			LOG4CXX_ERROR(loggerAtmiBrokerEnvHandlers, (char*) "No buffer is being processed");
		}
	} else if(name == "SERVICE") {
		if(attributes.getLength() != 0) {
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

			for(XMLSize_t i = 0; i < attributes.getLength(); i++) {
			        StrX attvalue = attributes.getLocalName(i);
				StrX att = attributes.getValue(i);
				if(attvalue == "name") {
					if(strstr(att.localForm(), adm) || 
					   att == "BTStompAdmin" ||
					   att == "BTDomainAdmin") {
						LOG4CXX_WARN(loggerAtmiBrokerEnvHandlers, (char*) "Cannot define " << att);
						// disable further parsing
						XMLCh* etxt = XMLString::transcode("Cannot define service");
						SAXParseException exc(etxt, *locator);
						XMLString::release(&etxt);
						fatalError(exc);
					}

					if(checkService(server, att.localForm())) {
						LOG4CXX_WARN(loggerAtmiBrokerEnvHandlers, (char*) "Cannot define Same Service " << att);
						// disable further parsing
						XMLCh* etxt = XMLString::transcode("Cannot define same service");
						SAXParseException exc(etxt, *locator);
						XMLString::release(&etxt);
						fatalError(exc);
					}

					service.serviceName = copy_value(att.localForm());
					LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set name: " << service.serviceName);
				} else if(attvalue == "function_name") {
					service.function_name = strdup(att.localForm());
					LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set function_name: " << service.function_name);
				} else if(attvalue == "advertised") {
					if(att == "true") {
						service.advertised = true;
					} else {
						service.advertised = false;
					}
				} else if(attvalue == "conversational") {
					if(att == "true") {
						service.conversational = true;
					} else {
						service.conversational = false;
					}
				} else if(attvalue == "externally-managed-destination") {
					if(att == "true") {
						service.externally_managed_destination = true;
					} else {
						service.externally_managed_destination = false;
					}
				} else if (attvalue == "size") {
					service.poolSize = (short) atol(att.localForm());
					LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "storing size " << service.poolSize);
				} else if(attvalue == "type") {
					service.serviceType = copy_value(attributes.getValue(i));
					LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set type: " << service.serviceType);
				} else if(attvalue == "coding_type") {
					service.coding_type = copy_value(attributes.getValue(i));
					LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "set coding type: " << service.coding_type);
				}
			}


			if(service.advertised && service.function_name == NULL) {
				LOG4CXX_WARN(loggerAtmiBrokerEnvHandlers, (char*) "Can not mark a service as advertised if it does not define a function_name" << service.serviceName);
				// disable further parsing
				XMLCh* etxt = XMLString::transcode("Can not mark a service as advertised if it does not define a function_name");
				SAXParseException exc(etxt, *locator);
				XMLString::release(&etxt);
				fatalError(exc);
			}

			if(service.serviceType == NULL) {
				service.serviceType = strdup("queue");
			}

			LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "setting transportlib");
#ifdef WIN32
			service.transportLib = strdup("atmibroker-hybrid.dll");
#else
			service.transportLib = strdup("libatmibroker-hybrid.so");
#endif
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "set transportlib: " << service.transportLib);
		}
	} else if (name == "LIBRARY_NAME") {
		if(attributes.getLength() > 0 && StrX(attributes.getLocalName(0)) == "configuration") {
		        StrX attvalue = attributes.getValue((XMLSize_t)0);
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "comparing" << attvalue << " with " << configuration);
			if (attvalue == configuration) {
				service.library_name = copy_value(attributes.getValue(1));
				LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "processed LIBRARY_NAME: " << service.library_name);
			} else {
				LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "CONFIGURATION NOT APPLICABLE FOR LIBRARY_NAME: " << attvalue);
			}
		}
	}
	strcpy(element, name.localForm());
	strcpy(value, "");

	depth += 1;
}

void AtmiBrokerEnvHandlers::endElement( const XMLCh* const uri, const XMLCh* const name, const XMLCh* const qname) {

	strcpy(last_element, StrX(name).localForm());
	strcpy(last_value, value);

	LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "storing element: " << last_element);

	if (strcmp(last_element, "DOMAIN") == 0) {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "storing domain value: " << last_value);
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
			LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "stored EnvVariable at index %d" << index);
		}
		processingEnvVariable = false;
	} else if (strcmp(last_element, "NAME") == 0) {
		if (processingEnvVariable) {
			int index = envVariableCount - 1;
			(*aEnvironmentStructPtr)[index].name = copy_value(last_value);
			LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "stored EnvName %s at index %d" << last_value << index);
		}
	} else if (strcmp(last_element, "VALUE") == 0) {
		if (processingEnvVariable) {
			int index = envVariableCount - 1;
			(*aEnvironmentStructPtr)[index].value = copy_value(last_value);
			LOG4CXX_DEBUG(loggerAtmiBrokerEnvHandlers, (char*) "stored Env Value %s at index %d" << last_value << index);
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


void AtmiBrokerEnvHandlers::characters(const XMLCh* const name,
								  const XMLSize_t length) {
	XMLSize_t i = 0;
	XMLSize_t j = 0;
	XMLSize_t priorLength = strlen(value);

	i = priorLength;
	for (; i < length + priorLength; i++, j++) {
		value[i] = name[j];
	}
	value[i] = '\0';
	if (value[0] == '\n') {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "value starts with newline (may be other character data)");
	} else {
		LOG4CXX_TRACE(loggerAtmiBrokerEnvHandlers, (char*) "value is :" << value);
	}
}

void AtmiBrokerEnvHandlers::resetDocument() {
}

void AtmiBrokerEnvHandlers::error(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void AtmiBrokerEnvHandlers::fatalError(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Fatal Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
	throw e;
}

void AtmiBrokerEnvHandlers::warning(const SAXParseException& e) {
	LOG4CXX_WARN(logger, "Warning at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}


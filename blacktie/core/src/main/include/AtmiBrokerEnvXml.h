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

#ifndef AtmiBroker_ENV_XML_H_
#define AtmiBroker_ENV_XML_H_

#include "atmiBrokerCoreMacro.h"

#include <map>
#include <string.h>
#include <vector>

typedef struct {
	char * name;
	char * value;
} envVar_t;

class BLACKTIE_CORE_DLL AtmiBrokerEnvXml {
public:

	AtmiBrokerEnvXml();

	~AtmiBrokerEnvXml();

	bool parseXmlDescriptor(std::vector<envVar_t>*,
			const char * ConfigurationDir,
			char * configuration);
};

typedef struct BLACKTIE_CORE_DLL xarm_config {
	long resourceMgrId;
	char * resourceName;
	char * openString;
	char * closeString;
	char * xasw;
	char * xalib;
	struct xarm_config * next;
	struct xarm_config * head;
}xarm_config_t;

typedef BLACKTIE_CORE_DLL struct _mq_config {
	char* host;
	int port;
	char* user;
	char* pwd;
	int destinationTimeout;
	int requestTimeout;
	int timeToLive;
	int noReplyTimeToLive;
}MqConfig;

typedef BLACKTIE_CORE_DLL struct _callback_server_config {
	char* host;
	int port;
}CallbackServerConfig;

typedef BLACKTIE_CORE_DLL struct _orb_config {
	char* opt;
	char* transactionFactoryName;
	char* interface;
}OrbConfig;

typedef BLACKTIE_CORE_DLL struct _txn_config {
	char* mgrEP;
	char* resourceEP;
}TxnConfig;

typedef BLACKTIE_CORE_DLL struct _service_info {
	char* serviceName;
	char* serviceType;
	char* transportLib;
	char* function_name;
	char* library_name;
	char* coding_type;
	int poolSize;
	bool advertised;
	bool conversational;
	bool externally_managed_destination;
}ServiceInfo;

typedef BLACKTIE_CORE_DLL struct _server_info {
	char* serverName;
	short maxChannels;
	short maxSuppliers;
	short maxConsumers;
	short maxReplicas;
	short logLevel;
	char* function_name;
	char* done_function_name;
	char* library_name;
	bool xa;

	std::vector<ServiceInfo> serviceVector;
}ServerInfo;

struct ltstr {
	bool operator()(const char* s1, const char* s2) const {
		return strcmp(s1, s2) < 0;
	}
};

typedef BLACKTIE_CORE_DLL struct _attribute {
	char* id;
	char* type;
	int count;
	int length;
	int wirePosition;
	int memPosition;
	int wireSize;
	int memSize;
}Attribute;

typedef BLACKTIE_CORE_DLL struct _buffer {
	char* name;
	std::map<const char*, Attribute*, ltstr> attributes;
	long wireSize;
	long memSize;
	int lastPad;
}Buffer;

typedef BLACKTIE_CORE_DLL std::map<const char*, Attribute*, ltstr> Attributes;

typedef BLACKTIE_CORE_DLL std::map<const char*, Buffer*, ltstr> Buffers;

typedef std::vector<ServerInfo*> ServersInfo;

extern BLACKTIE_CORE_DLL Buffers buffers;
extern BLACKTIE_CORE_DLL xarm_config_t * xarmp;
extern BLACKTIE_CORE_DLL ServersInfo servers;
extern BLACKTIE_CORE_DLL char domain[30];
extern BLACKTIE_CORE_DLL OrbConfig orbConfig;
extern BLACKTIE_CORE_DLL TxnConfig txnConfig;
extern BLACKTIE_CORE_DLL MqConfig mqConfig;
extern BLACKTIE_CORE_DLL CallbackServerConfig cbConfig;
#endif

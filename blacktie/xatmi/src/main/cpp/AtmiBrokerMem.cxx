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

#include "xatmi.h"
#include "AtmiBrokerMem.h"
#include "AtmiBrokerClient.h"
#include "log4cxx/logger.h"
#include "ThreadLocalStorage.h"
#include "AtmiBrokerEnvXml.h"

log4cxx::LoggerPtr AtmiBrokerMem::logger(log4cxx::Logger::getLogger(
		"AtmiBrokerMem"));
SynchronizableObject* AtmiBrokerMem::lock = new SynchronizableObject();

AtmiBrokerMem * AtmiBrokerMem::ptrAtmiBrokerMem = NULL;

AtmiBrokerMem *
AtmiBrokerMem::get_instance() {
	LOG4CXX_TRACE(logger, (char*) "get_instance locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "get_instance locked");
	if (ptrAtmiBrokerMem == NULL)
		ptrAtmiBrokerMem = new AtmiBrokerMem();
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "get_instance unlocked");
	return ptrAtmiBrokerMem;
}

void AtmiBrokerMem::discard_instance() {
	LOG4CXX_TRACE(logger, (char*) "discard_instance locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "discard_instance locked");
	if (ptrAtmiBrokerMem != NULL) {
		delete ptrAtmiBrokerMem;
		ptrAtmiBrokerMem = NULL;
	}
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "discard_instance unlocked");
}

AtmiBrokerMem::AtmiBrokerMem() {
	LOG4CXX_DEBUG(logger, (char*) "constructor");
}

AtmiBrokerMem::~AtmiBrokerMem() {
	LOG4CXX_DEBUG(logger, (char*) "destructor assumes you have the lock....");
	LOG4CXX_DEBUG(logger, (char*) "memoryInfoVector.size "
			<< memoryInfoVector.size());
	std::vector<MemoryInfo>::iterator it = memoryInfoVector.begin();
	while (it != memoryInfoVector.end()) {
		MemoryInfo memoryInfo = (*it);
		LOG4CXX_DEBUG(logger, (char*) "freeing memoryPtr");
		if (memoryInfo.memoryPtr != NULL) {
			LOG4CXX_DEBUG(logger, (char*) "freeing memoryPtr");
			free(memoryInfo.memoryPtr);
		}
		if (memoryInfo.type != NULL) {
			LOG4CXX_DEBUG(logger, (char*) "freeing type");
			free(memoryInfo.type);
		}
		if (memoryInfo.subtype != NULL) {
			LOG4CXX_DEBUG(logger, (char*) "freeing subtype");
			free(memoryInfo.subtype);
		}
		LOG4CXX_DEBUG(logger, (char*) "freed memory");

		LOG4CXX_DEBUG(logger, (char*) "removing  from vector");
		it = memoryInfoVector.erase(it);
		LOG4CXX_DEBUG(logger, (char*) "removed from vector ");
	}
	memoryInfoVector.clear();
	LOG4CXX_TRACE(logger, (char*) "freeAllMemory unlocked");
}

char*
AtmiBrokerMem::tpalloc(char* type, char* subtype, long size,
		bool serviceAllocated) {
	char* toReturn = NULL;
	LOG4CXX_TRACE(logger, (char*) "tpalloc locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "tpalloc locked");
	if (!type) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc - no type");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else if ((strncmp(type, "X_COMMON", MAX_TYPE_SIZE) == 0 || strncmp(type,
			"X_C_TYPE", MAX_TYPE_SIZE) == 0 || strncmp(type, "BT_NBF", MAX_TYPE_SIZE) == 0) && !subtype) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc - no subtype");
		setSpecific(TPE_KEY, TSS_TPEOS);
	} else if ((strncmp(type, "X_COMMON", MAX_TYPE_SIZE) == 0 || strncmp(type,
			"X_C_TYPE", MAX_TYPE_SIZE) == 0) && buffers[subtype] == NULL) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc - unknown buffer subtype: "
				<< subtype);
		setSpecific(TPE_KEY, TSS_TPEOS);
	} else if (size < 0) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc - negative size");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else if (strncmp(type, "X_OCTET", 8) == 0 && size == 0) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc - buffer type requires size");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else if (strncmp(type, "X_OCTET", MAX_TYPE_SIZE) != 0 && strncmp(type,
			"X_COMMON", MAX_TYPE_SIZE) != 0 && strncmp(type, "X_C_TYPE",
			MAX_TYPE_SIZE) != 0 && strncmp(type, "BT_NBF", MAX_TYPE_SIZE) != 0) {
		LOG4CXX_ERROR(logger, (char*) "tpalloc DONT YET know type: " << type);
		setSpecific(TPE_KEY, TSS_TPENOENT);
	} else {
		if (strcmp(type, "X_OCTET") == 0) {
			LOG4CXX_DEBUG(logger, (char*) "tpalloc character array ");
			subtype = (char*) "";
		} else if (strcmp(type, "X_COMMON") == 0 || strcmp(type, "X_C_TYPE")
				== 0) {
			if (!serviceAllocated && size != 0) {
				LOG4CXX_WARN(logger,
						(char*) "tpalloc - X_C_TYPE/X_COMMON size should be 0");
			}
			LOG4CXX_DEBUG(logger, (char*) "tpalloc X_C_TYPE/X_COMMON");
			size = buffers[subtype]->memSize;
		} else if(strcmp(type, "BT_NBF") == 0) {
			LOG4CXX_DEBUG(logger, (char*) "tpalloc BT_NBF");
			size = 512;
		}

		LOG4CXX_DEBUG(logger, (char*) "tpalloc - type: subtype: size:" << type
				<< ":" << subtype << ":" << size);
		MemoryInfo memoryInfo;
		LOG4CXX_TRACE(logger, (char*) "tpalloc - created memoryInfo");
		memoryInfo.memoryPtr = (char*) malloc(size);
		memoryInfo.size = size;
		memset(memoryInfo.memoryPtr, '\0', memoryInfo.size);
		LOG4CXX_TRACE(logger, (char*) "tpalloc - sized: " << size);

		memoryInfo.type = (char*) malloc(MAX_TYPE_SIZE + 1);
		memset(memoryInfo.type, '\0', MAX_TYPE_SIZE + 1);
		LOG4CXX_TRACE(logger, (char*) "type prep");
		strncpy(memoryInfo.type, type, MAX_TYPE_SIZE);
		LOG4CXX_TRACE(logger, (char*) "tpalloc - copied type/"
				<< memoryInfo.type << "/");

		memoryInfo.subtype = (char*) malloc(MAX_SUBTYPE_SIZE + 1);
		memset(memoryInfo.subtype, '\0', MAX_SUBTYPE_SIZE + 1);
		strncpy(memoryInfo.subtype, subtype, MAX_SUBTYPE_SIZE);
		LOG4CXX_TRACE(logger, (char*) "tpalloc - copied subtype/"
				<< memoryInfo.subtype << "/");

		memoryInfo.forcedDelete = serviceAllocated;

		LOG4CXX_DEBUG(
				logger,
				(char*) "adding MemoryInfo: with type: with subtype: with size: to vector"
						<< memoryInfo.type << ":" << memoryInfo.subtype << ":"
						<< memoryInfo.size);
		memoryInfoVector.push_back(memoryInfo);
		LOG4CXX_DEBUG(logger, (char*) "added MemoryInfo to vector: "
				<< memoryInfoVector.size());
		toReturn = (char*) memoryInfo.memoryPtr;
		if(strcmp(type, "BT_NBF") == 0) {
			strcpy(toReturn, "<?xml version='1.0'?>");
			strcat(toReturn, "<");
			strcat(toReturn, subtype);
			strcat(toReturn, " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			strcat(toReturn, " xmlns=\"http://www.jboss.org/blacktie\"");
			strcat(toReturn, " xsi:schemaLocation=\"http://www.jboss.org/blacktie buffers/");
			strcat(toReturn, subtype);
			strcat(toReturn, ".xsd\">");
			strcat(toReturn, "</");
			strcat(toReturn, subtype);
			strcat(toReturn, ">");
		}
	}
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "tpalloc unlocked");
	return toReturn;
}

char* AtmiBrokerMem::tprealloc(char * addr, long size, char* type,
		char* subtype, bool force) {
	char* toReturn = NULL;
	LOG4CXX_TRACE(logger, (char*) "tprealloc locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "tprealloc locked");
	if (!addr) {
		LOG4CXX_ERROR(logger, (char*) "tprealloc - no buffer");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		LOG4CXX_DEBUG(logger, (char*) "tprealloc hunting " << size);
		for (std::vector<MemoryInfo>::iterator it = memoryInfoVector.begin(); it
				!= memoryInfoVector.end(); it++) {
			LOG4CXX_TRACE(logger, (char*) "next memoryInfo id is with size: "
					<< (*it).size);
			if ((*it).memoryPtr == addr) {
				LOG4CXX_DEBUG(logger, (char*) "found matching memory with size"
						<< (*it).size);
				if (!force && (strncmp((*it).type, "X_COMMON",
						MAX_TYPE_SIZE) == 0 || strncmp((*it).type, "X_C_TYPE",
						MAX_TYPE_SIZE) == 0)) {
					LOG4CXX_INFO(
							logger,
							(char*) "tprealloc - cannot resize a X_C_TYPE/X_COMMON buffer - leaving buffer unmodified");
					// it does not make sense to reallocate these buffer types - treat it as a NOOP
					toReturn = addr;
				} else if (size <= 0) {
					// can't have buffers of size less than or equal to zero - treat it as a NOOP
					LOG4CXX_ERROR(logger, (char*) "tprealloc - zero or negative size");
					setSpecific(TPE_KEY, TSS_TPEINVAL);
				} else {
					char* memPtr = (char*) realloc((void*) addr, size);
					(*it).memoryPtr = memPtr;
					(*it).size = size;
					if(force) {
						memset((*it).memoryPtr, '\0', (*it).size);
					}
					toReturn = memPtr;

					if (type != NULL) {
						free((*it).type);
						(*it).type = (char*) malloc(MAX_TYPE_SIZE + 1);
						memset((*it).type, '\0', MAX_TYPE_SIZE + 1);
						LOG4CXX_TRACE(logger, (char*) "type prep");
						strncpy((*it).type, type, MAX_TYPE_SIZE);
						LOG4CXX_TRACE(logger, (char*) "tpalloc - copied type/"
								<< (*it).type << "/");
					}

					if (subtype != NULL) {
						free((*it).subtype);
						(*it).subtype = (char*) malloc(MAX_SUBTYPE_SIZE + 1);
						memset((*it).subtype, '\0', MAX_SUBTYPE_SIZE + 1);
						strncpy((*it).subtype, subtype, MAX_SUBTYPE_SIZE);
						LOG4CXX_TRACE(logger, (char*) "tpalloc - copied subtype/"
								<< (*it).subtype << "/");
					}

					LOG4CXX_DEBUG(logger, (char*) "updated - size: " << size);
				}
				break;
			}
		}

		if (toReturn == NULL) {
			LOG4CXX_TRACE(logger, (char*) "tprealloc - not found addr");
			setSpecific(TPE_KEY, TSS_TPEINVAL);
			LOG4CXX_TRACE(logger, (char*) "tprealloc - failure advised");
		}
	}
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "tprealloc unlocked");
	return toReturn;
}

void AtmiBrokerMem::tpfree(char* ptr, bool force) {
	LOG4CXX_TRACE(logger, (char*) "tpfree locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "tpfree locked");
	if (ptr && ptr != NULL) {
		LOG4CXX_DEBUG(logger, (char*) "tpfree: " << memoryInfoVector.size());
		for (std::vector<MemoryInfo>::iterator it = memoryInfoVector.begin(); it
				!= memoryInfoVector.end(); it++) {
			//			LOG4CXX_TRACE(logger, (char*) "next memoryInfo id is: "
			//					<< (char*) (*it).memoryPtr);
			if ((*it).memoryPtr == NULL) {
				LOG4CXX_ERROR(logger, (char*) "found a null in the vector");
				break;
			}
			MemoryInfo memoryInfo = (*it);
			if (memoryInfo.memoryPtr == ptr) {
				if (!memoryInfo.forcedDelete || (memoryInfo.forcedDelete
						&& force)) {
					LOG4CXX_DEBUG(logger,
							(char*) "freeing memoryPtr to reclaim: "
									<< memoryInfo.size);
					free(memoryInfo.memoryPtr);
					if (memoryInfo.type != NULL) {
						LOG4CXX_DEBUG(logger, (char*) "freeing type");
						free(memoryInfo.type);
					}
					if (memoryInfo.subtype != NULL) {
						LOG4CXX_DEBUG(logger, (char*) "freeing subtype");
						free(memoryInfo.subtype);
					}
					LOG4CXX_DEBUG(logger, (char*) "freed memory");

					LOG4CXX_DEBUG(logger, (char*) "removing  from vector");
					memoryInfoVector.erase(it);
					LOG4CXX_DEBUG(logger, (char*) "removed from vector ");

					break;
				} else {
					LOG4CXX_DEBUG(logger,
							(char*) "tpfree without force ignored");
				}
			}
		}
		LOG4CXX_DEBUG(logger, (char*) "tpfreed: " << memoryInfoVector.size());
	}
	LOG4CXX_TRACE(logger, (char*) "tpfree unlocking");
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "tpfree unlocked");
	return;
}

long AtmiBrokerMem::tptypes(char* ptr, char* type, char* subtype) {
	LOG4CXX_TRACE(logger, (char*) "tptypes locking");
	lock->lock();
	LOG4CXX_TRACE(logger, (char*) "tptypes locked");
	long toReturn = -1;
	if (ptr && ptr != NULL) {
		LOG4CXX_DEBUG(logger, (char*) "ptr appeared valid");
		for (std::vector<MemoryInfo>::iterator it = memoryInfoVector.begin(); it
				!= memoryInfoVector.end(); it++) {
			LOG4CXX_TRACE(logger, (char*) "next memoryInfo is with size: "
					<< (*it).size);
			if ((*it).memoryPtr == ptr) {
				MemoryInfo memoryInfo = (*it);
				LOG4CXX_DEBUG(logger, (char*) "found matching memory");

				if (type && type != NULL) {
					strncpy(type, memoryInfo.type, MAX_TYPE_SIZE);
				}
				if (subtype && subtype != NULL) {
					strncpy(subtype, memoryInfo.subtype, MAX_SUBTYPE_SIZE);
				}

				toReturn = memoryInfo.size;
				break;
			}
		}
	}
	if (toReturn == -1) {
		// WAS NOT FOUND
		LOG4CXX_TRACE(logger, (char*) "notifying could not found");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	}
	LOG4CXX_TRACE(logger, (char*) "tptypes unlocking");
	lock->unlock();
	LOG4CXX_TRACE(logger, (char*) "tptypes unlocked");
	return toReturn;
}

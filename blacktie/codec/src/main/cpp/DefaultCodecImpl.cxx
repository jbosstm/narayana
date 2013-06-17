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

#include "DefaultCodecImpl.h"

#include <exception>
#include "malloc.h"

#include "ace/os_include/netinet/os_in.h"
#include "ace/Basic_Types.h"

#include "AtmiBrokerEnvXml.h"

log4cxx::LoggerPtr DefaultCodecImpl::logger(log4cxx::Logger::getLogger(
		"DefaultCodecImpl"));

ACE_UINT64 htonll(ACE_UINT64 value) {
	static const int num = 42;

	if (*reinterpret_cast<const char*>(&num) == num) {
		const ACE_UINT32 high_part = htonl(static_cast<ACE_UINT32>(value >> 32));
		const ACE_UINT32 low_part  = htonl(static_cast<ACE_UINT32>(value & 0xFFFFFFFFLL));
		return (static_cast<ACE_UINT64>(low_part) << 32) | high_part;
	} else {
		return value;
	}
}

ACE_UINT64 ntohll(ACE_UINT64 value) {
	static const int num = 42;

	if (*reinterpret_cast<const char*>(&num) == num) {
		const ACE_UINT32 high_part = ntohl(static_cast<ACE_UINT32>(value >> 32));
		const ACE_UINT32 low_part  = ntohl(static_cast<ACE_UINT32>(value & 0xFFFFFFFFLL));
		return (static_cast<ACE_UINT64>(low_part) << 32) | high_part;
	} else {
		return value;
	}
}

char* DefaultCodecImpl::encode(char* type,
		char* subtype, char* membuffer,
		long* length) {
	LOG4CXX_DEBUG(logger, (char*) "convertToWireFormat");
	char* data_togo = NULL;

	if (strlen(type) == 0) {
		LOG4CXX_TRACE(logger, (char*) "Sending NULL buffer");
		*length = 1;
		data_togo = new char[*length];
		data_togo[0] = (char) NULL;
	} else if (strncmp(type, "BT_NBF", 6) == 0) {
		data_togo = new char[*length];
		
		LOG4CXX_TRACE(logger, (char*) "allocated: " << *length);
		if (*length != 0) {
			memcpy(data_togo, membuffer, *length);
			LOG4CXX_TRACE(logger, (char*) "copied: idata into: data_togo");
		}
	} else if (strncmp(type, "X_OCTET", 8) == 0) {
		data_togo = new char[*length];

		LOG4CXX_TRACE(logger, (char*) "allocated: " << *length);
		if (*length != 0) {
			memcpy(data_togo, membuffer, *length);
			LOG4CXX_TRACE(logger, (char*) "copied: idata into: data_togo");
		}
	} else {
		Buffer* buffer = buffers[subtype];
		data_togo = new char[buffer->wireSize];
		memset(data_togo, '\0', buffer->wireSize);
		LOG4CXX_TRACE(logger, (char*) "allocated: " << buffer->wireSize);

		// Copy the attributes in
		int copiedAmount = 0;
		// TODO ASSUMES ATMIBROKERMEM HAS INITED THE MEMORY WITH DETAILS
		Attributes::iterator i;
		for (i = buffer->attributes.begin(); i != buffer->attributes.end(); ++i) {
			Attribute* attribute = i->second;
			int position = attribute->memPosition;
			int length = attribute->length > 0 ? attribute->length : 1;
			int memTypeSize = attribute->memSize / length;
			int wireTypeSize = attribute->wireSize / length;
			ACE_UINT16 svalue;
			ACE_UINT32 lvalue;
			ACE_UINT64 llvalue;
			void* buf;

			if (strcmp(attribute->type, "short") == 0 || strcmp(attribute->type, "short[]") == 0) {
				for(int i = 0; i < length; i++) {
					svalue = *(ACE_UINT16*)(&membuffer[position + i * memTypeSize]);
					svalue = htons(svalue);
					LOG4CXX_DEBUG(logger, (char*) "htons short " << i << " value:" << svalue);
					buf = (char*)&svalue;
					memcpy(&data_togo[attribute->wirePosition + i * wireTypeSize], buf, wireTypeSize);
				}
			} else if(strcmp(attribute->type, "int") == 0 || strcmp(attribute->type, "int[]") == 0 || strcmp(attribute->type, "float") == 0 || strcmp(attribute->type, "float[]") == 0 || strcmp(attribute->type, "long") == 0 || strcmp(attribute->type, "long[]") == 0) {
				for(int i = 0; i < length; i++) {
					lvalue = *(ACE_UINT32*)(&membuffer[position + i * memTypeSize]);
					lvalue = htonl(lvalue);
					LOG4CXX_DEBUG(logger, (char*) "htonl " << attribute->type << i << " value:" << lvalue);
					buf = (char*)&lvalue;
					memcpy(&data_togo[attribute->wirePosition + i * wireTypeSize], buf, 4);
				}
			} else if(strcmp(attribute->type, "double") == 0 || strcmp(attribute->type, "double[]") == 0) {
				for(int i = 0; i < length; i++) {
					llvalue = *(ACE_UINT64*)(&membuffer[position + i * memTypeSize]);
					llvalue = htonll(llvalue);
					LOG4CXX_DEBUG(logger, (char*) "htonll " << attribute->type << i << " value:" << llvalue);
					buf = (char*)&llvalue;
					memcpy(&data_togo[attribute->wirePosition + i * wireTypeSize], buf, wireTypeSize);
				}
			} else {
				buf = &membuffer[attribute->memPosition];
				memcpy(&data_togo[attribute->wirePosition], buf, attribute->wireSize);
			}
			copiedAmount = copiedAmount + attribute->wireSize;
			LOG4CXX_TRACE(logger, (char*) "copied: idata into: data_togo: "
					<< attribute->wireSize);
		}
		if (copiedAmount != buffer->wireSize) {
			LOG4CXX_TRACE(logger, (char*) "DID NOT FILL THE BUFFER Amount: "
					<< copiedAmount << " Expected: " << buffer->wireSize);
		}
		*length = buffer->wireSize;
	}
	
	return data_togo;
}

char* DefaultCodecImpl::decode(char* type,
		char* subtype, char* membuffer,
		long* length) {
	LOG4CXX_DEBUG(logger, (char*) "convertToMemoryFormat");
	char* data_tostay = NULL;

	if (type == NULL || strlen(type) == 0) {
		LOG4CXX_TRACE(logger, (char*) "Received NULL buffer");
		*length = 0;
	} else if (strncmp(type, "BT_NBF", 6) == 0) {
		LOG4CXX_TRACE(logger, (char*) "Received an BT_NBF buffer");
		*length = *length;
		LOG4CXX_TRACE(logger, (char*) "Allocating DATA");
		data_tostay = (char*) malloc(*length);
		LOG4CXX_TRACE(logger, (char*) "Allocated");
		if (*length > 0) {
			memcpy(data_tostay, membuffer, *length);
			LOG4CXX_TRACE(logger, (char*) "Copied");
		}
	} else if (strncmp(type, "X_OCTET", 8) == 0) {
		LOG4CXX_TRACE(logger, (char*) "Received an X_OCTET buffer");
		*length = *length;
		LOG4CXX_TRACE(logger, (char*) "Allocating DATA");
		data_tostay = (char*) malloc(*length);
		LOG4CXX_TRACE(logger, (char*) "Allocated");
		if (*length > 0) {
			memcpy(data_tostay, membuffer, *length);
			LOG4CXX_TRACE(logger, (char*) "Copied");
		}
	} else {
		LOG4CXX_TRACE(logger, (char*) "Received a non X_OCTET buffer: "
				<< subtype);
		Buffer* buffer = buffers[subtype];
		if (buffer == NULL) {
			LOG4CXX_FATAL(
					logger,
					(char*) "Unknown buffer type: "
							<< subtype);
		}


		if (*length != buffer->wireSize) {
			LOG4CXX_ERROR(
					logger,
					(char*) "DID NOT Receive the expected amount of wire data: "
							<< *length << " Expected: "
							<< buffer->wireSize);
		}
		data_tostay = (char*) malloc(buffer->memSize);

		memset(data_tostay, '\0', buffer->memSize);
		LOG4CXX_TRACE(logger, (char*) "allocated: " << buffer->memSize);


		// TODO ASSUMES ATMIBROKERMEM HAS INITED THE MEMORY WITH DETAILS
		Attributes::iterator i;
		for (i = buffer->attributes.begin(); i != buffer->attributes.end(); ++i) {
			Attribute* attribute = i->second;
			if(strcmp(attribute->type, "short") == 0 || strcmp(attribute->type, "short[]") == 0) {
				int position = attribute->wirePosition;
				int length = attribute->length > 0 ? attribute->length : 1;
				int memTypeSize = attribute->memSize / length;
				int wireTypeSize = attribute->wireSize / length;

				for(int i = 0; i < length; i++) {
					ACE_UINT16 value = *((ACE_UINT16*)&membuffer[position + i * wireTypeSize]);
					value = ntohs(value);
					LOG4CXX_DEBUG(logger, (char*) "ntohs short " << i << " value:" << value);
					memcpy(&membuffer[position + i * memTypeSize], &value, memTypeSize);
				}
			} else if(strcmp(attribute->type, "int") == 0 || strcmp(attribute->type, "int[]") == 0 || strcmp(attribute->type, "float") == 0 || strcmp(attribute->type, "float[]") == 0 || strcmp(attribute->type, "long") == 0 || strcmp(attribute->type, "long[]") == 0){
				int position = attribute->wirePosition;
				int length = attribute->length > 0 ? attribute->length : 1;
				int memTypeSize = attribute->memSize / length;
				int wireTypeSize = attribute->wireSize / length;

				for(int i = 0; i < length; i++) {
					LOG4CXX_DEBUG(logger, (char*) "position: " << position << " i: " << i << " wireTypeSize: " << wireTypeSize);
					ACE_UINT32 value = *((ACE_UINT32*)&membuffer[position + i * wireTypeSize]);
					value = ntohl(value);
					LOG4CXX_DEBUG(logger, (char*) "ntohl " << i << attribute->type << " value:" << value);
					memset(&membuffer[position + i * memTypeSize], 0, memTypeSize);
					memcpy(&membuffer[position + i * memTypeSize], &value, 4);
				}
			} else if(strcmp(attribute->type, "double") == 0 || strcmp(attribute->type, "double[]") == 0){
				int position = attribute->wirePosition;
				int length = attribute->length > 0 ? attribute->length : 1;
				int memTypeSize = attribute->memSize / length;
				int wireTypeSize = attribute->wireSize / length;

				for(int i = 0; i < length; i++) {
					LOG4CXX_DEBUG(logger, (char*) "position: " << position << " i: " << i << " wireTypeSize: " << wireTypeSize);
					ACE_UINT64 value = *((ACE_UINT64*)&membuffer[position + i * wireTypeSize]);
					value = ntohll(value);
					LOG4CXX_DEBUG(logger, (char*) "ntohll " << i << attribute->type << " value:" << value);
					memcpy(&membuffer[position + i * memTypeSize], &value, memTypeSize);
				}
			}
			memcpy(&data_tostay[attribute->memPosition],
					&membuffer[attribute->wirePosition],
					attribute->memSize);
			LOG4CXX_TRACE(logger, (char*) "copied: idata into: data_togo: "
					<< attribute->memSize);
		}
		*length = buffer->memSize;
	}
	return data_tostay;
}

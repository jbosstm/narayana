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
#include <XMLCodecHandlers.h>
#include <Base64.h>
#include <log4cxx/logger.h>
#include <xercesc/sax/AttributeList.hpp>
#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/sax/SAXException.hpp>
#include <xercesc/framework/psvi/XSTypeDefinition.hpp>
#include <xercesc/framework/psvi/PSVIElement.hpp>
#include <xercesc/framework/psvi/XSConstants.hpp>

log4cxx::LoggerPtr XMLCodecHandlers::logger(
		log4cxx::Logger::getLogger("XMLCodecHandlers"));

XMLCodecHandlers::XMLCodecHandlers(char* type, char* subtype) {
	this->type = strdup(type);
	this->subtype = strdup(subtype);
	membuffer = NULL;
	buffer = NULL;
	length = 0;
	foundOctet = false;
	foundXCType = false;
	attrName = NULL;
}

XMLCodecHandlers::~XMLCodecHandlers() {
	if(type) {
		free(type);
	}

	if(subtype) {
		free(subtype);
	}

	if(attrType) {
		free(attrType);
	}

	if(attrName) {
		free(attrName);
	}
}

char* XMLCodecHandlers::getBuffer() {
	return membuffer;
}

long XMLCodecHandlers::getLength() {
	return length;
}

void XMLCodecHandlers::startElement(const XMLCh* const name, AttributeList& attributes) {
	StrX str(name);
	const char* qname = str.localForm();

	LOG4CXX_DEBUG(logger, "start name:" << qname);
	if(qname && strcmp(qname, "bt:X_OCTET") == 0) {
		foundOctet = true;
	} else if(qname && strcmp(qname, "bt:X_C_TYPE") == 0) {
		buffer = buffers[subtype];
		if(buffer == NULL) {
			LOG4CXX_FATAL(
					logger,
					(char*) "Unknown buffer type: "
							<< subtype);	
		} else {
			foundXCType = true;
			membuffer = (char*) malloc(buffer->memSize);
			memset(membuffer, '\0', buffer->memSize);
			length = buffer->memSize;
		}
	} else {
		attrName = strdup(&qname[3]);
		LOG4CXX_DEBUG(logger, "strdup " << attrName);
	}
}

void XMLCodecHandlers::endElement( const XMLCh* const name) {
	StrX str(name);
	const char* qname = str.localForm();
	LOG4CXX_DEBUG(logger, "end name:" << qname);
	if(qname && strcmp(qname, "bt:X_OCTET") == 0) {
		foundOctet = false;
	} else if(qname && strcmp(qname, "bt:X_C_TYPE") == 0) {
		foundXCType = false;
	}

	if(attrName) {
		LOG4CXX_DEBUG(logger, "free " << attrName);
		free(attrName);
		attrName = NULL;
	}
}

void XMLCodecHandlers::characters(const XMLCh* const name,
								  const XMLSize_t length) {
	StrX str(name);
	const char* value = str.localForm();
	LOG4CXX_DEBUG(logger, "value:" << value);
	if(foundOctet) {
		char* tmp = base64_decode((char*)value, &this->length);
		membuffer = (char*) malloc (this->length);
		memcpy(membuffer, tmp, this->length);
		delete tmp;
	} else if(foundXCType) {
		if(attrType != NULL) {
			LOG4CXX_DEBUG(logger, attrType << (char*)":" << value);

			Attributes::iterator it = buffer->attributes.find(attrName);
			if(it != buffer->attributes.end()) {
				Attribute* attribute = it->second;

				if(strcmp(attrType, "string") != 0 &&
				   strcmp(attrType, "base64Binary") != 0 && 
				   !(strcmp(attrType, "integer") == 0 && strcmp(attribute->type, "int") == 0) &&
				   strcmp(attribute->type, attrType) != 0) {
					LOG4CXX_WARN(logger, attrName << " buffer type:" << attribute->type << " and schema type:" << attrType);
				} else {
					if(strcmp(attrType, "short") == 0) {
						short svalue = atoi(value);
						memcpy(&membuffer[attribute->memPosition], &svalue, attribute->memSize);
					} else if(strcmp(attrType, "integer") == 0) {
						int ivalue = atoi(value);
						memcpy(&membuffer[attribute->memPosition], &ivalue, attribute->memSize);
					} else if(strcmp(attrType, "long") == 0) {
						long lvalue = atol(value);
						memcpy(&membuffer[attribute->memPosition], &lvalue, attribute->memSize);
					} else if(strcmp(attrType, "string") == 0) {
						char cvalue = *value;
						memcpy(&membuffer[attribute->memPosition], &cvalue, attribute->memSize);
					} else if(strcmp(attrType, "float") == 0) {
						float fvalue = atof(value);
						memcpy(&membuffer[attribute->memPosition], &fvalue, attribute->memSize);
					} else if(strcmp(attrType, "double") == 0) {
						double dvalue = atof(value);	
						memcpy(&membuffer[attribute->memPosition], &dvalue, attribute->memSize);
					} else if(strcmp(attrType, "base64Binary") == 0) {
						char* content;
						long size = 0;
						content = base64_decode((char*)value, &size);
						memcpy(&membuffer[attribute->memPosition], content, size);
						delete content;
					} else {
						LOG4CXX_WARN(logger, "can not decode with type " << attrType);
					}
				}
			} else {
				LOG4CXX_WARN(logger, "can not find " << attrName);
			}
			LOG4CXX_DEBUG(logger, "free " << attrType);
			free(attrType);
			attrType = NULL;
		}
	}
}

void XMLCodecHandlers::resetDocument() {
}

void XMLCodecHandlers::error(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void XMLCodecHandlers::fatalError(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Fatal Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void XMLCodecHandlers::warning(const SAXParseException& e) {
	LOG4CXX_WARN(logger, "Warning at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void XMLCodecHandlers::handleElementPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,
		PSVIElement* elementInfo) {
	LOG4CXX_DEBUG(logger, "handleElementPSVI " << StrX(localName));
	
}

void XMLCodecHandlers::handlePartialElementPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,
		PSVIElement* elementInfo) {
	LOG4CXX_DEBUG(logger, "handlePartialElementPSVI " << StrX(localName));
	XSTypeDefinition* typeInfo = elementInfo->getTypeDefinition();
	const char* typeStr;

	while(typeInfo) {
		StrX type(typeInfo->getName());
		LOG4CXX_DEBUG(logger, "typeInfo type of " << type);
		typeStr = type.localForm();
		if (strcmp(typeStr, "string") == 0 ||
				strcmp(typeStr, "long") == 0 ||
				strcmp(typeStr, "integer") == 0 ||
				strcmp(typeStr, "short") == 0 ||
				strcmp(typeStr, "float") == 0 ||
				strcmp(typeStr, "double") == 0 ||
				strcmp(typeStr, "base64Binary") == 0 ||
				strcmp(typeStr, "anyType") == 0) {
			if(strcmp(typeStr, "anyType") != 0) {
				attrType = strdup(typeStr);
				LOG4CXX_DEBUG(logger, "stdup " << attrType);
			}
			break;
		}
		typeInfo = typeInfo->getBaseType();
	}
}

void XMLCodecHandlers::handleAttributesPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,  
		PSVIAttributeList* elementInfo) {
	LOG4CXX_DEBUG(logger, "handleAttributesPSVI " << StrX(localName));
}

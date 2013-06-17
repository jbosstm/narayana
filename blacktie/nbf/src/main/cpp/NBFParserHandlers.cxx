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
#include <NBFParserHandlers.h>
#include <log4cxx/logger.h>
#include <xercesc/sax/AttributeList.hpp>
#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/sax/SAXException.hpp>
#include <xercesc/framework/psvi/XSTypeDefinition.hpp>
#include <xercesc/framework/psvi/PSVIElement.hpp>
#include <xercesc/framework/psvi/XSConstants.hpp>

log4cxx::LoggerPtr NBFParserHandlers::logger(
		log4cxx::Logger::getLogger("NBFParserHandlers"));

NBFParserHandlers::NBFParserHandlers(const char* attrName, int index) {
	if(attrName != NULL) {
		this->attrName = strdup(attrName);
	} else {
		this->attrName = NULL;
	}
	this->qName = NULL;
	this->attrValue = NULL;
	this->attrType = NULL;
	this->index = index;
	this->curIndex = -1;
	this->found = false;
}

NBFParserHandlers::~NBFParserHandlers() {
	if(qName != NULL) {
		free(qName);
	}

	if(attrName != NULL) {
		free(attrName);
	}

	if(attrValue != NULL) {
		free(attrValue);
	}

	if(attrType != NULL) {
		free(attrType);
	}
}

char* NBFParserHandlers::getValue() {
	return attrValue;
}

char* NBFParserHandlers::getType() {
	return attrType;
}

int NBFParserHandlers::getOccurs() {
	return curIndex+1;
}

void NBFParserHandlers::startElement(const XMLCh* const name, AttributeList& attributes) {
	StrX str(name);
	const char* qname = str.localForm();
	LOG4CXX_DEBUG(logger, "name:" << qname);
	if(strcmp(qname, attrName) == 0) {
		found = true;
		curIndex ++;
	}

	if(qName != NULL) {
		free(qName);
	}

	qName = strdup(qname);
}

void NBFParserHandlers::endElement( const XMLCh* const name) {
	StrX str(name);
	const char* qname = str.localForm();
	LOG4CXX_DEBUG(logger, "name:" << qname);
	if(strcmp(qname, attrName) == 0) {
		found = false;
	}
}

void NBFParserHandlers::characters(const XMLCh* const name,
								  const XMLSize_t length) {
	StrX str(name);
	const char* value = str.localForm();

	if(found && curIndex == index) {
		LOG4CXX_DEBUG(logger, "find value " << value << " at index " << index);
		if (attrType != NULL && strstr(attrType, "_type") != NULL) {
			int size = strlen(qName) * 2 + strlen(value) + 5 + 1;
			char* tmp = (char*) malloc (sizeof(char) * size);
			int len = 0;
			memset(tmp, 0, size);
			sprintf(tmp, "<%s>%s</%s>", qName, value, qName);
			if(attrValue != NULL) {
				len = strlen(attrValue);
				size += len;
			}
			attrValue = (char*) realloc(attrValue, size);
			memset(attrValue + len, 0, size - len);	
			strcat(attrValue, tmp);
			free(tmp);
		} else {
			attrValue = strdup(value);
		}
	}
}

void NBFParserHandlers::resetDocument() {
}

void NBFParserHandlers::error(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void NBFParserHandlers::fatalError(const SAXParseException& e) {
	LOG4CXX_ERROR(logger, "Fatal Error at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void NBFParserHandlers::warning(const SAXParseException& e) {
	LOG4CXX_WARN(logger, "Warning at (file " << StrX(e.getSystemId())
			<< ", line " << e.getLineNumber()
			<< ", char " << e.getColumnNumber()
			<< "): " << StrX(e.getMessage()));
}

void NBFParserHandlers::handleElementPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,
		PSVIElement* elementInfo) {
	LOG4CXX_DEBUG(logger, "handleElementPSVI " << StrX(localName));
	
}

void NBFParserHandlers::handlePartialElementPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,
		PSVIElement* elementInfo) {
	LOG4CXX_DEBUG(logger, "handlePartialElementPSVI " << StrX(localName));

	StrX str(localName);
	const char* qname = str.localForm();
	if(strcmp(qname, attrName) == 0) {
		XSTypeDefinition* typeInfo = elementInfo->getTypeDefinition();
		const char* typeStr;

		while(typeInfo) {
			StrX type(typeInfo->getName());
			LOG4CXX_DEBUG(logger, "typeInfo type of " << type);
			typeStr = type.localForm();

			if (strcmp(typeStr, "string") == 0 ||
					strcmp(typeStr, "long") == 0 ||
					strcmp(typeStr, "integer") == 0 ||
					strcmp(typeStr, "float") == 0 ||
					strstr(typeStr, "_type") != NULL) {
				if(attrType == NULL) {
					attrType = strdup(typeStr);
					LOG4CXX_DEBUG(logger, attrName << " has type of " << attrType);
				}
				break;
			}
			typeInfo = typeInfo->getBaseType();
		}
	}
}

void NBFParserHandlers::handleAttributesPSVI(const XMLCh* const localName, 
		const XMLCh* const uri,  
		PSVIAttributeList* elementInfo) {
	LOG4CXX_DEBUG(logger, "handleAttributesPSVI " << StrX(localName));
}

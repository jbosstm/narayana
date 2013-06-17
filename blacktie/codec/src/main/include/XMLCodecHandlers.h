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
#ifndef XMLCODEC_HANDLERS_H
#define XMLCODEC_HANDLERS_H
#include <log4cxx/logger.h>
#include <xercesc/sax/HandlerBase.hpp>
#include <xercesc/framework/psvi/PSVIHandler.hpp>

#include "AtmiBrokerEnvXml.h"

XERCES_CPP_NAMESPACE_USE

class StrX {
public :
	StrX(const XMLCh* const toTranscode) {
		fLocalForm = XMLString::transcode(toTranscode);
	}

	~StrX() {
		XMLString::release(&fLocalForm);
	}

	const char* localForm() const {
		return fLocalForm;
	}

private:
	char*   fLocalForm;
};

inline std::ostream& operator<< (std::ostream& out, const StrX& toDump) {
	out << toDump.localForm();
	return out;
}

XERCES_CPP_NAMESPACE_BEGIN
class AttributeList;
XERCES_CPP_NAMESPACE_END

class XMLCodecHandlers : public HandlerBase, public PSVIHandler {
public:
	XMLCodecHandlers(char*, char*);
	~XMLCodecHandlers();

	// Handlers for the SAX DocumentHandler interface
	void startElement(const XMLCh* const name, AttributeList& attributes);
	void endElement(const XMLCh* const name);
	void characters(const XMLCh* const chars, const XMLSize_t length);
	void resetDocument();
	
	// Handlers for the SAX ErrorHandler interface
	void warning(const SAXParseException& exc);
	void error(const SAXParseException& exc);
	void fatalError(const SAXParseException& exc);

	// Handlers for the PSVI interface
	void handleElementPSVI(const XMLCh* const localName, const XMLCh* const uri,  PSVIElement* elementInfo);
	void handlePartialElementPSVI(const XMLCh* const localName, const XMLCh* const uri,  PSVIElement* elementInfo);
	void handleAttributesPSVI(const XMLCh* const localName, const XMLCh* const uri,  PSVIAttributeList* elementInfo);

	// Public Function
	char* getBuffer();
	long  getLength();

private:
	static log4cxx::LoggerPtr logger;
	char* type;
	char* subtype;
	char* membuffer;
	Buffer* buffer;
	char* attrType;
	char* attrName;
	long  length;
	bool  foundOctet;
	bool  foundXCType;
};
#endif

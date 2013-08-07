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
#ifndef AtmiBroker_ENVHandlers_H
#define AtmiBroker_ENVHandlers_H
#include <log4cxx/logger.h>
#include <xercesc/sax2/DefaultHandler.hpp>

#include <AtmiBrokerEnvXml.h>

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

inline bool operator==( const StrX& d1,const char* d2){
   return !strcmp(d1.localForm(), d2); 
}

XERCES_CPP_NAMESPACE_BEGIN
class Attributes;
XERCES_CPP_NAMESPACE_END

class AtmiBrokerEnvHandlers : public DefaultHandler {
public:
	AtmiBrokerEnvHandlers(std::vector<envVar_t>&, const char*);
	~AtmiBrokerEnvHandlers();

	// Handlers for the SAX DocumentHandler interface
	void startElement(const XMLCh* const uri, const XMLCh* const name, const XMLCh* const qname, const xercesc::Attributes& attributes);
	void endElement(const XMLCh* const uri, const XMLCh* const name, const XMLCh* const qname);
	void characters(const XMLCh* const chars, const XMLSize_t length);
	void resetDocument();
	
	// Handlers for the SAX ErrorHandler interface
	void warning(const SAXParseException& exc);
	void error(const SAXParseException& exc);
	void fatalError(const SAXParseException& exc);

	void setDocumentLocator(const Locator* const i_locator) { locator = i_locator; }


private:
	static log4cxx::LoggerPtr logger;
	std::vector<envVar_t>* aEnvironmentStructPtr;
	const char* configuration;
	const Locator* locator;
};
#endif

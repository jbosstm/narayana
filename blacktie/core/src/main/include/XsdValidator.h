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
#ifndef XSDVALIDATOR_H
#define XSDVALIDATOR_H

#include "atmiBrokerCoreMacro.h"
#include "log4cxx/logger.h"
#include "log4cxx/stream.h"

#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/sax2/SAX2XMLReader.hpp>
#include <xercesc/sax2/XMLReaderFactory.hpp>
#include <xercesc/util/OutOfMemoryException.hpp>
#include <xercesc/sax2/DefaultHandler.hpp>
#include <xercesc/util/XMLUni.hpp>

#if defined(XERCES_NEW_IOSTREAMS)
#include <iostream>
#include <fstream>
#else
#include <iostream.h>
#include <fstream.h>
#endif

XERCES_CPP_NAMESPACE_USE

// ---------------------------------------------------------------------------
// This is a simple class that lets us do easy (though not terribly efficient)
// trancoding of XMLCh data to local code page for display.
// ---------------------------------------------------------------------------
class StrX {
public :
	StrX(const XMLCh* const toTranscode)
        {
		//Call the private transcoding method
                fLocalForm = XMLString::transcode(toTranscode);
        }

        ~StrX()
        {
                XMLString::release(&fLocalForm);
        }

	const char* localForm() const
	{
    		return fLocalForm;
	}

private:
	char*   fLocalForm;
};

inline std::ostream& operator<< (std::ostream& out, const StrX& toDump)
{
	out << toDump.localForm();
	return out;
}

class MYHandler : public DefaultHandler {
public:
	MYHandler();
	~MYHandler();

	bool getSawErrors() const
    	{
        	return fSawErrors;
    	}

	void warning(const SAXParseException& exc);
	void error(const SAXParseException& exc);
	void fatalError(const SAXParseException& exc);

	void resetErrors()
	{
		fSawErrors = false;
	}

private:
	bool fSawErrors;
};

class BLACKTIE_CORE_DLL XsdValidator {
public:
	XsdValidator();
	~XsdValidator();

	//Validate XML file with XSD Schema
	bool validate(const char* aXSDFileName, const char* aXMLFileName);

	//Get logger
	static log4cxx::LoggerPtr getLogger()
	{
		return logger;
	}

private:
	//Load XSD Schema and build a parser
	bool buildXSDParser(const char* aXSDFileName);

	//Check File
	bool checkFile(const char* fname, const char* suffix);

	static log4cxx::LoggerPtr logger;
	bool   isInitial;

	SAX2XMLReader*  parser;
};
#endif

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
#include "XsdValidator.h"

log4cxx::LoggerPtr XsdValidator::logger(
		log4cxx::Logger::getLogger("XsdValidator"));

MYHandler::MYHandler()
	: fSawErrors(false)
{
	LOG4CXX_TRACE(XsdValidator::getLogger(), "Created MYHandler");
}

MYHandler::~MYHandler()
{
	LOG4CXX_TRACE(XsdValidator::getLogger(), "Destroyed MYHandler");
}

void MYHandler::error(const SAXParseException& e)
{
	LOG4CXX_ERROR(XsdValidator::getLogger(), "Error at file " << StrX(e.getSystemId())
						<< ", line " << e.getLineNumber()
						<< ", char " << e.getColumnNumber()
						<< " Message: " << StrX(e.getMessage()));
}

void MYHandler::fatalError(const SAXParseException& e)
{
	LOG4CXX_FATAL(XsdValidator::getLogger(), "Fatal Error at file " << StrX(e.getSystemId())
						<< ", line " << e.getLineNumber()
						<< ", char " << e.getColumnNumber()
						<< " Message: " << StrX(e.getMessage()));
}

void MYHandler::warning(const SAXParseException& e)
{
	LOG4CXX_WARN(XsdValidator::getLogger(), "Warning at file " << StrX(e.getSystemId())
						<< ", line " << e.getLineNumber()
						<< ", char " << e.getColumnNumber()
						<< " Message: " << StrX(e.getMessage()));
}

XsdValidator::XsdValidator()
{
	try {
		XMLPlatformUtils::Initialize();
		isInitial = true;
  	} catch (const XMLException& toCatch){
		LOG4CXX_ERROR(logger, "Error during initialization! Message:"
					<< StrX(toCatch.getMessage()));	
		isInitial = false;
	}
}

XsdValidator::~XsdValidator()
{
	XMLPlatformUtils::Terminate();
}

bool XsdValidator::checkFile(const char* fname, const char* suffix)
{
	int flen, slen, len;
	FILE* fp;

	if(fname == NULL || suffix == NULL) {
		return false;
	}

	flen = strlen(fname);
	slen = strlen(suffix);
	len  = flen - slen;

	if(len > 0 && slen > 0 && fname[len-1] == '.' && strncmp(fname+len, suffix, len) == 0) {
		fp = fopen(fname, "r");
		if(fp == NULL) {
			LOG4CXX_DEBUG(logger, fname << " does not exist");
			return false;
		}	
		fclose(fp);
		return true;
	}
	
	LOG4CXX_ERROR(logger, fname << " does not suffix " << suffix);
	return false;
}

bool XsdValidator::buildXSDParser(const char* aXSDFileName)
{
	LOG4CXX_DEBUG(logger, "checkFile " << aXSDFileName << ": start");
	if(checkFile(aXSDFileName, "xsd") == false) {
		LOG4CXX_DEBUG(logger, "checkFile " << aXSDFileName << ": fail");
		return false;
	}
	LOG4CXX_DEBUG(logger, "checkFile " << aXSDFileName << ": ok");

	try {
		// Create a SAX2 parser object.
		parser = XMLReaderFactory::createXMLReader();
		LOG4CXX_DEBUG(logger, "create schema parser");

		// Set the appropriate features on the parser.
		// Enable namespaces, schema validation, and the checking 
		// of all Schema constraint
		parser->setFeature(XMLUni::fgSAX2CoreNameSpaces, true);
		parser->setFeature(XMLUni::fgSAX2CoreValidation, true);
		parser->setFeature(XMLUni::fgXercesDynamic, false);
		parser->setFeature(XMLUni::fgXercesSchema, true);
		parser->setFeature(XMLUni::fgXercesSchemaFullChecking, true);

		// Preprocess the XML Schema and cache it.
		parser->loadGrammar(aXSDFileName, Grammar::SchemaGrammarType, true);

		// Instruct the parser to use the cached schema 
		// when processing XML documents.
		parser->setFeature(XMLUni::fgXercesUseCachedGrammarInParse, true);
		return true;
	} catch (const OutOfMemoryException&) {
		LOG4CXX_ERROR(logger, "OutOfMemoryException during parsing: " << aXSDFileName);
	} catch (const XMLException& e) {
		LOG4CXX_ERROR(logger, "Error during parsing: " << aXSDFileName
					 << " Exception message is: " << StrX(e.getMessage()));
	} catch (...) {
		LOG4CXX_ERROR(logger, "Unexpected exception during parsing: " << aXSDFileName);
	} 

	delete parser;
	LOG4CXX_DEBUG(logger, "destory schema parser");
	return false;
}

bool XsdValidator::validate(const char* aXSDFileName, const char* aXMLFileName)
{
	int  errorCount = 0;
	bool result     = false;

	if(isInitial == false) {
		LOG4CXX_ERROR(logger, "XERCES Initailization failed!");
		return false;
	}

	if(aXSDFileName == NULL || aXMLFileName == NULL) {
		LOG4CXX_ERROR(logger, "parameters violation");
		return false;
	}

	LOG4CXX_DEBUG(logger, "checkFile " << aXMLFileName << ": start");
	if(checkFile(aXMLFileName, "xml") == false) {
		LOG4CXX_DEBUG(logger, "checkFile " << aXMLFileName << ": fail");
		return false;
	}
	LOG4CXX_DEBUG(logger, "checkFile " << aXMLFileName << ": ok");

	if(buildXSDParser(aXSDFileName) == true) {
		try{
			MYHandler handler;

			parser->setContentHandler(&handler);
			parser->setErrorHandler(&handler);
			parser->setEntityResolver(&handler);

			parser->parse(aXMLFileName);
			errorCount = parser->getErrorCount();
			LOG4CXX_DEBUG(logger, "errorCount is " << errorCount);

			if(errorCount != 0) {
				LOG4CXX_ERROR(logger, aXMLFileName << " is not validate with "
							<< aXSDFileName);
				result = false;
			} else {
				LOG4CXX_DEBUG(logger, aXMLFileName << " is validate with "
							<< aXSDFileName);
				result = true;
			}
		} catch (const OutOfMemoryException&) {
			LOG4CXX_ERROR(logger, "OutOfMemoryException during parsing: " << aXMLFileName);
		} catch (const XMLException& e) {
			LOG4CXX_ERROR(logger, "Error during parsing: " << aXMLFileName
						 << " Exception message is: " << StrX(e.getMessage()));
		} catch (...) {
			LOG4CXX_ERROR(logger, "Unexpected exception during parsing: " << aXMLFileName);
		} 

		LOG4CXX_DEBUG(logger, "deleting parser");
		delete parser;
		LOG4CXX_DEBUG(logger, "deleted parser");
	}

	return result;
}

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
#include <log4cxx/logger.h>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/util/OutOfMemoryException.hpp>
#include <AtmiBrokerEnvParser.h>

#include <apr_strings.h>
#include <string>
using namespace std;

log4cxx::LoggerPtr AtmiBrokerEnvParser::logger(log4cxx::Logger::getLogger("AtmiBrokerEnvParser"));

AtmiBrokerEnvParser::AtmiBrokerEnvParser() : parser(NULL) {
  
  	char schemaPath[256];
	char* schemaDir;
		
	schemaDir = getenv("BLACKTIE_SCHEMA_DIR");
	if (schemaDir) {
		apr_snprintf(schemaPath, 256, "%s/btconfig.xsd", schemaDir);
		LOG4CXX_TRACE(logger, (char*) "BLACKTIE_SCHEMA_DIR="<<schemaDir);
	} else {
		LOG4CXX_ERROR(logger,
				(char*) "BLACKTIE_SCHEMA_DIR is not set, cannot validate configuration");

		isInitial = false;
		return;
	}

	LOG4CXX_DEBUG(logger, "checkFile " << schemaPath << ": start");
	if(checkFile(schemaPath, "xsd") == false) {
		LOG4CXX_ERROR(logger, "Failed to open blacktie schema: " << schemaPath);
		isInitial = false;
		return;
	}
	
	LOG4CXX_DEBUG(logger, "checkFile " << schemaPath << ": ok");
	
	try {
		XMLPlatformUtils::Initialize();
		isInitial = true;

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
		parser->loadGrammar(schemaPath, Grammar::SchemaGrammarType, true);
		
		// Instruct the parser to use the cached schema 
		// when processing XML documents.
		parser->setFeature(XMLUni::fgXercesUseCachedGrammarInParse, true);
		
		LOG4CXX_DEBUG(logger, "create SAXParser");
	} catch (const XMLException& toCatch){
		LOG4CXX_ERROR(logger, "Error during initialization! Message:"
				<< StrX(toCatch.getMessage()));
		isInitial = false;
		parser = NULL;
	}
}

AtmiBrokerEnvParser::~AtmiBrokerEnvParser() {
	if(parser != NULL) {
		delete parser;
		LOG4CXX_DEBUG(logger, "release parser");
	}
	XMLPlatformUtils::Terminate();
}

bool AtmiBrokerEnvParser::checkFile(const char* fname, const char* suffix)
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


bool AtmiBrokerEnvParser::parse(const char* configurationDir, AtmiBrokerEnvHandlers* handler) {
	bool result = false;

	char aDescriptorFileName[256];

	if (configurationDir != NULL) {
		LOG4CXX_TRACE(logger, (char*) "read env from dir: "
				<< configurationDir);
		apr_snprintf(aDescriptorFileName, 256, "%s/btconfig.xml",
				configurationDir);
		LOG4CXX_DEBUG(logger,
				(char*) "in parse() " << aDescriptorFileName);
	} else {
		LOG4CXX_TRACE(logger,
				(char*) "read env from default file");
		strcpy(aDescriptorFileName, "btconfig.xml");
	}

	LOG4CXX_DEBUG(logger, "checkFile " << aDescriptorFileName << ": start");
	if(checkFile(aDescriptorFileName, "xml") == false) {
		LOG4CXX_DEBUG(logger, "checkFile " << aDescriptorFileName << ": fail");
		return false;
	}
	LOG4CXX_DEBUG(logger, "checkFile " << aDescriptorFileName << ": ok");
	
	if(isInitial && parser != NULL) {
		parser->setContentHandler(handler);
		parser->setEntityResolver(handler);
		parser->setErrorHandler(handler);

		int errorCount = 0;
		try {
			parser->parse(aDescriptorFileName);
			errorCount = parser->getErrorCount();
			if (!errorCount) {
				LOG4CXX_DEBUG(logger, "parse buf OK");
				result = true;
			}
		} catch (const OutOfMemoryException&) {
			LOG4CXX_ERROR(logger, (char*) "OutOfMemoryException");
		} catch (const XMLException& e) {
			LOG4CXX_ERROR(logger, (char*) "parsing exception message is "
					<< StrX(e.getMessage()));
		} catch (...) {
			LOG4CXX_ERROR(logger, "Unexpected exception during parsing: " << aDescriptorFileName);
		} 


	}

	return result;
}

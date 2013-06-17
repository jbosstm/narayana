/*
 * SymbolLoader.cpp
 *
 *  Created on: Mar 11, 2009
 *      Author: tom
 */

#include "SymbolLoader.h"

#include "ace/DLL.h"

#include "log4cxx/logger.h"

log4cxx::LoggerPtr symbolLoaderLogger(log4cxx::Logger::getLogger(
		"symbolLoaderLogger"));


#include "ace/OS_NS_dlfcn.h"
#include "ace/Lib_Find.h"

void* lookup_symbol(const char *lib, const char *symbol) {
	if (lib != NULL) {
		LOG4CXX_LOGLS(symbolLoaderLogger, log4cxx::Level::getTrace(),
				(char *) "lookup_symbol " << symbol << (char *) " in library "
						<< lib);
	} else {
		LOG4CXX_LOGLS(symbolLoaderLogger, log4cxx::Level::getTrace(),
				(char *) "lookup_symbol " << symbol << (char *) " in main executable");
	}

//	if (symbol == NULL || lib == NULL)
//		return 0;

	//void* dll = ::dlopen (NULL, RTLD_NOW);
	void* dll = ACE_OS::dlopen(lib, ACE_DEFAULT_SHLIB_MODE);

	if (dll == 0) {
		LOG4CXX_ERROR(symbolLoaderLogger, (char*) "lookup_symbol: " << symbol
				<< (char *) " dll.open error");
		return NULL;
	}

	void * sym = NULL;

	try {
		//sym = ::dlsym (dll, symbol);//
		sym = (void*) ACE_OS::dlsym((ACE_SHLIB_HANDLE)dll, symbol);

		if (sym == NULL) {
			LOG4CXX_ERROR(symbolLoaderLogger, (char*) "lookup_symbol: "
					<< symbol << (char *) " dlsym error");
			//dll.close();
			return NULL;
		}

		LOG4CXX_TRACE(symbolLoaderLogger, (char *) "symbol addr=" << sym);

		return sym;
	} catch (std::exception& e) {
		LOG4CXX_ERROR(symbolLoaderLogger, (char *) "symbol addr=" << sym
				<< e.what());
		return NULL;
	}
}


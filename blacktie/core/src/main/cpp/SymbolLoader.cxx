/*
 * SymbolLoader.cpp
 *
 *  Created on: Mar 11, 2009
 *      Author: tom
 */

#include "SymbolLoader.h"

#include "log4cxx/logger.h"

#include "apr_dso.h"

log4cxx::LoggerPtr symbolLoaderLogger(log4cxx::Logger::getLogger(
		"symbolLoaderLogger"));

apr_pool_t* sym_pool = NULL;

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
	
	if(sym_pool == NULL)
	{
	  apr_pool_create(&sym_pool,NULL);
	}

	apr_dso_handle_t* handle = NULL;
	
	apr_status_t res = apr_dso_load(&handle, lib, sym_pool);

        apr_dso_handle_sym_t sym;

	if (res != APR_SUCCESS) {
		LOG4CXX_ERROR(symbolLoaderLogger, (char*) "lookup_symbol: " << symbol
				<< (char *) " dll.open error");
		return NULL;
	}

	try {
		//sym = ::dlsym (dll, symbol);//
		res = apr_dso_sym(&sym, handle, symbol);

		if (res != APR_SUCCESS) {
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


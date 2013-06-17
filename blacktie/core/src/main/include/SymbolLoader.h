/*
 * SymbolLoader.h
 *
 *  Created on: Mar 11, 2009
 *      Author: tom
 */

#ifndef SYMBOLLOADER_H_
#define SYMBOLLOADER_H_

#include "atmiBrokerCoreMacro.h"

extern BLACKTIE_CORE_DLL void* lookup_symbol(const char *lib, const char *symbol);

#endif /* SYMBOLLOADER_H_ */

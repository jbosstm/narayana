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
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <iostream>

#include "log4cxx/logger.h"
#include "log4cxx/basicconfigurator.h"
#include "log4cxx/propertyconfigurator.h"
#include "log4cxx/logmanager.h"

extern "C" {
#include "btlogger.h"
}
#include "AtmiBrokerEnv.h"

#define MAXLOGSIZE 2048

log4cxx::LoggerPtr loggerAtmiBrokerLogc(log4cxx::Logger::getLogger(
		"AtmiBrokerLogc"));

static bool loggerInitialized = false;

extern "C"BLACKTIE_CORE_DLL
int btlogger_snprintf(char *str, size_t size, const char * format, ...) {
	va_list args;
	int ret;
	va_start(args, format);
	ret = vsnprintf(str, size, format, args);
	va_end(args);
	return ret;
}

extern "C"BLACKTIE_CORE_DLL
void btlogger(const char * format, ...) {
	if (loggerAtmiBrokerLogc->isEnabledFor(log4cxx::Level::getInfo())) {
		char str[MAXLOGSIZE];
		va_list args;
		va_start(args, format);
		vsnprintf(str, MAXLOGSIZE, format, args);
		va_end(args);
		LOG4CXX_LOGLS(loggerAtmiBrokerLogc, log4cxx::Level::getInfo(), str);
	}
}

extern "C"BLACKTIE_CORE_DLL
void btlogger_debug(const char * format, ...) {
	if (loggerAtmiBrokerLogc->isEnabledFor(log4cxx::Level::getDebug())) {
		char str[MAXLOGSIZE];
		va_list args;
		va_start(args, format);
		vsnprintf(str, MAXLOGSIZE, format, args);
		va_end(args);
		LOG4CXX_LOGLS(loggerAtmiBrokerLogc, log4cxx::Level::getDebug(), str);
	}
}

extern "C"BLACKTIE_CORE_DLL
void btlogger_trace(const char * format, ...) {
	if (loggerAtmiBrokerLogc->isEnabledFor(log4cxx::Level::getTrace())) {
		char str[MAXLOGSIZE];
		va_list args;
		va_start(args, format);
		vsnprintf(str, MAXLOGSIZE, format, args);
		va_end(args);
		LOG4CXX_LOGLS(loggerAtmiBrokerLogc, log4cxx::Level::getTrace(), str);
	}
}

extern "C"BLACKTIE_CORE_DLL
void btlogger_warn(const char * format, ...) {
	if (loggerAtmiBrokerLogc->isEnabledFor(log4cxx::Level::getWarn())) {
		char str[MAXLOGSIZE];
		va_list args;
		va_start(args, format);
		vsnprintf(str, MAXLOGSIZE, format, args);
		va_end(args);
		LOG4CXX_LOGLS(loggerAtmiBrokerLogc, log4cxx::Level::getWarn(), str);
	}
}

extern void btlogger_init() {
	if (!loggerInitialized) {
		char* config = getenv("LOG4CXXCONFIG");
		if (config != NULL) {
			log4cxx::PropertyConfigurator::configure(config);
		} else {
			log4cxx::PropertyConfigurator::configure("log4cxx.properties");
		}
		loggerInitialized = true;
	}
}

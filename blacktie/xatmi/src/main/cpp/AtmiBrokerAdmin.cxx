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

#include "log4cxx/logger.h"
#include "btserver.h"
#include "xatmi.h"
#include "string.h"
#include <stdio.h>
#include <stdlib.h>

extern const char* version;
log4cxx::LoggerPtr loggerAtmiBrokerAdmin(log4cxx::Logger::getLogger(
		"AtmiBrokerAdmin"));

void ADMIN(TPSVCINFO* svcinfo) {
	char* toReturn = NULL;
	long len = 1;
	char* req = NULL;

	req = tpalloc((char*) "X_OCTET", NULL, svcinfo->len + 1);
	memcpy(req, svcinfo->data, svcinfo->len);

	toReturn = tpalloc((char*) "X_OCTET", NULL, len);
	toReturn[0] = '0';

	strtok(req, ",");
	char* svc = strtok(NULL, ",");
	LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get request is " << req);

	if (strncmp(req, "serverdone", 10) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get serverdone command");
		toReturn[0] = '1';
		server_sigint_handler_callback(0);
	} else if (strncmp(req, "advertise", 9) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get advertise command, " << svc << " " << svcinfo->name);
		if (svc != NULL && strncmp(svc, ".", 1) != 0 && advertiseByAdmin(
				svc) == 0) {
			LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "advertise service "
					<< svc << " OK");
			toReturn[0] = '1';
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin, (char*) "advertise service "
					<< svc << " FAIL");
		}
	} else if (strncmp(req, "unadvertise", 11) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get unadvertise command");
		if (svc != NULL && strncmp(svc, ".", 1) != 0
				&& unadvertiseByAdmin(svc) == 0) {
			toReturn[0] = '1';
			LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "unadvertise service "
					<< svc << " OK");
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin, (char*) "unadvertise service "
					<< svc << " FAIL");
		}
	} else if (strncmp(req, "status", 6) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get status command");
		char* status = NULL;

		len += getServiceStatus(&status, svc) + 1;
		if (len > 1 && status != NULL) {
			toReturn = tprealloc(toReturn, len + 1);
			memcpy(&toReturn[1], status, len);
			free(status);
			toReturn[0] = '1';
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin,
					(char*) "get server status FAIL");
		}
	} else if (strncmp(req, "version", 7) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get version command");
		toReturn = tprealloc(toReturn, strlen(version) + 1 + 1);
		len += sprintf(&toReturn[1], "%s", version);
		toReturn[0] = '1';
	} else if (strncmp(req, "counter", 7) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get counter command");
		long counter = 0;

		if (svc != NULL) {
			toReturn = tprealloc(toReturn, 16);
			counter = getServiceMessageCounter(svc);
			len += sprintf(&toReturn[1], "%ld", counter);
			toReturn[0] = '1';
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin,
					(char*) "get counter failed with no service");
		}
	} else if (strncmp(req, "error_counter", 13) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin,
				(char*) "get error_counter command");
		long counter = 0;

		if (svc != NULL) {
			toReturn = tprealloc(toReturn, 16);
			counter = getServiceErrorCounter(svc);
			len += sprintf(&toReturn[1], "%ld", counter);
			toReturn[0] = '1';
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin,
					(char*) "get error counter failed with no service");
		}
	} else if (strncmp(req, "pause", 5) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get pause command");
		if (pauseServerByAdmin() == 0) {
			toReturn[0] = '1';
		}
	} else if (strncmp(req, "resume", 6) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get resume command");
		if (resumeServerByAdmin() == 0) {
			toReturn[0] = '1';
		}
	} else if (strncmp(req, "responsetime", 12) == 0) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "get responsetime command");
		unsigned long min;
		unsigned long avg;
		unsigned long max;

		if (svc != NULL) {
			toReturn = tprealloc(toReturn, 256);
			getResponseTime(svc, &min, &avg, &max);
			LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "min = " << min
					<< (char*) " avg=" << avg << (char*) " max=" << max);
			len += sprintf(&toReturn[1], "%ld,%ld,%ld", min, avg, max);
			toReturn[0] = '1';
		} else {
			LOG4CXX_WARN(loggerAtmiBrokerAdmin,
					(char*) "get response time failed with no service");
		}
	}

	if(req != NULL) {
		LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "tpfree req");
		tpfree(req);
	}
	LOG4CXX_DEBUG(loggerAtmiBrokerAdmin, (char*) "service done");
	tpreturn(TPSUCCESS, 0, toReturn, len, 0);
}

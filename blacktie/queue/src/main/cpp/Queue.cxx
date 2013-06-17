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

#include "ThreadLocalStorage.h"
#include "txx.h"
#include "xatmi.h"
#include "btqueue.h"
#include "Session.h"
#include "AtmiBrokerClient.h"
#include "btclient.h"
#include "btserver.h"
#include "AtmiBrokerEnv.h"

// Logger for XATMIc
log4cxx::LoggerPtr loggerQueue(log4cxx::Logger::getLogger("XATMIc"));

int btenqueue(char * svc, msg_opts_t* headers, char* idata, long ilen, long flags) {
	LOG4CXX_TRACE(loggerQueue, (char*) "tpacall: " << svc << " ilen: " << ilen
			<< " flags: 0x" << std::hex << flags);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);
	flags = flags | TPNOREPLY | TPNOTIME;

	long toCheck = flags & ~(TPNOTRAN | TPNOREPLY | TPNOTIME | TPSIGRSTRT
			| TPNOBLOCK);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerQueue, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		bool transactional = !(flags & TPNOTRAN);
		if (transactional) {
			void *ctrl = txx_get_control();
			if (ctrl == NULL) {
				transactional = false;
			}
			txx_release_control(ctrl);
		}

		int len = ::bufferSize(idata, ilen);
		if (len != -1) {
			if (clientinit() != -1) {
				Session* session = NULL;
				try {
					session = ptrAtmiBrokerClient->getQueueSession();
					LOG4CXX_TRACE(loggerQueue, (char*) "new session: "
							<< session << " transactional: " << transactional);

					if (tperrno != -1) {
						if (transactional)
							txxx_suspend();

						MESSAGE message;
						message.syncRcv = 0;

						int priority = 0;
						long timeToLive = 0;
						if (headers != NULL) {
							priority = headers->priority;
							timeToLive = headers->ttl;
						}

						LOG4CXX_TRACE(loggerQueue, (char*) "TPNOREPLY send");
						toReturn = ::send(session, "", idata, len, 0, flags, 0,
								message, 0, priority, timeToLive, true, svc);

						LOG4CXX_TRACE(loggerQueue, (char*) "TPNOREPLY sent");

						if (toReturn == -1) {
							LOG4CXX_WARN(loggerQueue,
									(char*) "Queue Session failure");
						}
					} else {
						LOG4CXX_TRACE(loggerQueue,
								(char*) "tpacall unknown error");
						setSpecific(TPE_KEY, TSS_TPESYSTEM);
					}
				} catch (...) {
					LOG4CXX_ERROR(
							loggerQueue,
							(char*) "tpacall failed to connect to service queue: "
									<< svc);
					setSpecific(TPE_KEY, TSS_TPENOENT);
				}

				if (transactional) {
					// txx_suspend was called but there was an error so
					// resume (note we didn't check for TPNOREPLY since we are in
					// the else arm of if (transactional && (flags & TPNOREPLY))
					LOG4CXX_DEBUG(loggerQueue, (char*) "tpacall resume rv="
							<< toReturn);
					txxx_resume();
				}

			}
		}
	}
	LOG4CXX_TRACE(loggerQueue, (char*) "tpacall return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}



int btdequeue(char * svc, msg_opts_t* headers , char ** odata, long *olen, long flags) {
	LOG4CXX_TRACE(loggerQueue, (char*) "btdequeue: " << svc <<
		" flags: 0x" << std::hex << flags);
	int toReturn = -1;

	setSpecific(TPE_KEY, TSS_TPERESET);
	//flags = flags | TPNOREPLY | TPNOTIME;
	flags = flags | TPNOREPLY;

	//long toCheck = flags & ~(TPNOTRAN | TPNOREPLY | TPNOTIME | TPSIGRSTRT | TPNOBLOCK);
	long toCheck = flags & ~(TPNOREPLY | TPSIGRSTRT);
	int len, suspended = 0;

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerQueue, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else if ((len = ::bufferSize(*odata, *olen) != -1) && clientinit() != -1) {
		bool transactional = !(flags & TPNOTRAN);

		if (transactional) {
			void *ctrl = txx_get_control();

			if (ctrl == NULL)
				transactional = false;
			else
				txx_release_control(ctrl);
		}

		try {
			Session* session = ptrAtmiBrokerClient->getQueueSession();
			LOG4CXX_TRACE(loggerQueue, (char*) "new session: " << session <<
				" transactional: " << transactional);

			if (session != NULL) {
				MESSAGE message = {0, 0, 0, 0, 0, 0, 0};
				message.syncRcv = 1;
				char* tperr;

				int priority = 0;
				long timeToLive = 0;
				if (headers != NULL) {
					priority = headers->priority;
					timeToLive = headers->ttl;
				}

				if (transactional) {
					txxx_suspend();
					suspended = 1;
				}

				message.data = NULL;
				toReturn = ::send(session, "", NULL, 0, 0, flags, 0, message, 0, priority, timeToLive, true, svc);
				// save tperrno (convertMessage may mask it)
				tperr = (char*) getSpecific(TPE_KEY);

				(void) ::convertMessage(message, len, odata, olen, 0L);

				if (tperr != NULL)
					setSpecific(TPE_KEY, tperr);
			} else {
				LOG4CXX_TRACE(loggerQueue, (char*) "btdequeue session error: " << tperrno);
				setSpecific(TPE_KEY, TSS_TPESYSTEM);
			}
		} catch (...) {
			LOG4CXX_ERROR( loggerQueue, (char*) "btdequeue failed to connect to service queue: " << svc);
			setSpecific(TPE_KEY, TSS_TPENOENT);
		}

		if (suspended)
			txxx_resume();
	}

	LOG4CXX_TRACE(loggerQueue, (char*) "btdequeue return: " << toReturn
			<< " tperrno: " << tperrno);

	return toReturn;
}

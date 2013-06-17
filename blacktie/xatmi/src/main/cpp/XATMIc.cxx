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
#include "Session.h"
#include "btclient.h"
#include "btserver.h"
#include "AtmiBrokerClient.h"
#include "AtmiBrokerServer.h"
#include "AtmiBrokerMem.h"
#include "AtmiBrokerEnv.h"
#include "ServiceDispatcher.h"

std::vector<int> sessionIds;
SynchronizableObject lock;

long DISCON = 0x00000003;

// Logger for XATMIc
log4cxx::LoggerPtr loggerXATMI(log4cxx::Logger::getLogger("XATMIc"));

int bufferSize(char* data, int suggestedSize) {
	if (data == NULL) {
		return 0;
	}
	int data_size = ::tptypes(data, NULL, NULL);
	if (data_size >= 0) {
		if (suggestedSize <= 0 || suggestedSize > data_size) {
			return data_size;
		} else {
			return suggestedSize;
		}
	} else {
		LOG4CXX_DEBUG(loggerXATMI,
				(char*) "A NON-BUFFER WAS ATTEMPTED TO BE SENT");
		setSpecific(TPE_KEY, TSS_TPEINVAL);
		return -1;
	}

}

void setTpurcode(long rcode) {
	char* retrieved = (char*) getSpecific(TPR_KEY);
	if (retrieved != NULL) {
		destroySpecific( TPR_KEY);
		free(retrieved);
	}
	if (rcode > 0) {
		char* toStore = (char*) malloc(8 * sizeof(long));
		sprintf(toStore, "%ld", rcode);
		setSpecific(TPR_KEY, toStore);
	}
}

int send(Session* session, const char* replyTo, char* idata, long ilen,
		int correlationId, long flags, long rval, MESSAGE& message, long rcode,
		int priority, long timeToLive, bool queue, char* queueName) {
	LOG4CXX_DEBUG(loggerXATMI, (char*) "send - ilen: " << ilen << ": "
			<< "cd: " << correlationId << "flags: " << flags);
	int toReturn = -1;

	if (session->getCanSend() || rval == DISCON) {
		try {
			LOG4CXX_TRACE(loggerXATMI, (char*) "allocating data to go: "
					<< ilen);

			message.replyto = replyTo;
			message.data = idata;
			message.len = ilen;
			message.correlationId = correlationId;
			message.priority = 0;
			message.flags = flags;
			message.rcode = rcode;
			message.rval = rval;
			message.type = (char *) "";
			message.subtype = (char *) "";
			if (message.data != NULL) {
				message.type = (char*) malloc(MAX_TYPE_SIZE + 1);
				memset(message.type, '\0', MAX_TYPE_SIZE + 1);
				message.subtype = (char*) malloc(MAX_SUBTYPE_SIZE + 1);
				memset(message.subtype, '\0', MAX_SUBTYPE_SIZE + 1);
				tptypes(idata, message.type, message.subtype);
			}

			session->blockSignals((flags & TPSIGRSTRT));

			if (queue) {
				long discardTTL = -1;
				message.xid = (TPNOTRAN & flags) ? NULL : txx_serialize(
						&discardTTL);
				message.control = NULL;

				// see if there are any extra headers
				if (priority > -1) {
					message.priority = priority;
                }

                if (timeToLive > -1) {
					message.ttl = timeToLive;
				}

				if (session->send(queueName, message))
					toReturn = 0;

				if (message.xid)
					free(message.xid);
			} else {
				message.xid = NULL;
				message.control = (TPNOTRAN & flags) ? NULL : txx_serialize(
						&(message.ttl));

				if (message.control == NULL) {
					// tapcalls with the TPNOREPLY flag set should live forever
					if (TPNOREPLY & flags) {
						message.ttl = mqConfig.noReplyTimeToLive;
					} else {
						message.ttl = mqConfig.timeToLive * 1000;
					}
				}


				if (session->send(message))
					toReturn = 0;
			}

			if (session->unblockSignals() != 0 && (flags & TPSIGRSTRT) == 0)
				toReturn = -1;

			if (message.control)
				free(message.control);

			if (message.data != NULL && !message.syncRcv) {
				free(message.type);
				free(message.subtype);
			}
		} catch (...) {
			LOG4CXX_ERROR(loggerXATMI, (char*) "send: call failed");
			setSpecific(TPE_KEY, TSS_TPESYSTEM);
		}
	} else {
		LOG4CXX_ERROR(loggerXATMI, (char*) "Session " << correlationId
				<< "can't send");
		setSpecific(TPE_KEY, TSS_TPEPROTO);
	}

	return toReturn;
}


int send(Session* session, const char* replyTo, char* idata, long ilen,
		int correlationId, long flags, long rval, long rcode,
		int priority, long timeToLive, bool queue, char* queueName) {
	MESSAGE message;
	message.syncRcv = 0;
	return ::send(session, replyTo, idata, ilen,
		correlationId, flags, rval, message, rcode,
		priority, timeToLive, queue, queueName);
}

static long determineTimeout(long flags) {
	long time = 0;
	if (TPNOBLOCK & flags) { // NB flags override any XATMI or transaction timeouts
		time = -1;
		LOG4CXX_DEBUG(loggerXATMI,
				(char*) "Setting timeout to -1 for TPNOBLOCK");
	} else if (TPNOTIME & flags) {
		time = 0;
		LOG4CXX_DEBUG(loggerXATMI, (char*) "TPNOTIME = BLOCKING CALL");
	} else {
		switch (txx_ttl(&time)) {
		case -1: // No transaction so use XATMI timeouts
			time = (long) mqConfig.requestTimeout + (long) mqConfig.timeToLive;
			break;
		case 1: // txn not subject to a timeout so block
			time = 0;
			break;
		case 0: // time has already been updated
		default: /*FALLTHRU txx_ttl will only returns -1, 0 or 1*/
			break;
		}
		LOG4CXX_TRACE(loggerXATMI, (char*) "receive txx_ttl time=" << time);
	}
	return time;
}

int convertMessage(MESSAGE &message, int len, char** odata, long* olen,
		long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "convertMessage");
	char* type = message.type;
	if (type == NULL) {
		type = (char*) "";
	}
	char* subtype = message.subtype;
	if (subtype == NULL) {
		subtype = (char*) "";
	}
	char* messageType = (char*) malloc(MAX_TYPE_SIZE);
	char* messageSubtype = (char*) malloc(MAX_SUBTYPE_SIZE);
	tptypes(*odata, messageType, messageSubtype);
	bool typesChanged = strncmp(type, messageType, MAX_TYPE_SIZE) != 0
			|| strncmp(subtype, messageSubtype, MAX_SUBTYPE_SIZE) != 0;
	free(messageType);
	free(messageSubtype);

	if (flags & TPNOCHANGE && typesChanged) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "convertMessage TPNOCHANGE");
		setSpecific(TPE_KEY, TSS_TPEOTYPE);
		setTpurcode(message.rcode);
		txx_rollback_only();
		free(message.data);
		return -1;
	}

	if (len < message.len && !typesChanged) {
		*odata = AtmiBrokerMem::get_instance()->tprealloc(*odata, message.len,
				NULL, NULL, true);
	} else if (len < message.len && typesChanged) {
		*odata = AtmiBrokerMem::get_instance()->tprealloc(*odata, message.len,
				message.type, message.subtype, true);
	} else if (typesChanged && message.len > 0) {
		*odata = AtmiBrokerMem::get_instance()->tprealloc(*odata, message.len,
				message.type, message.subtype, true);
	}

	*olen = message.len;
	if (message.len > 0) {
		memcpy(*odata, (char*) message.data, *olen);
	} else if (message.data == NULL) {
		*odata = NULL;
	}
	free(message.data);
	LOG4CXX_TRACE(loggerXATMI, (char*) "convertMessage ok");
	return 0;
}

int receive(int id, Session* session, char ** odata, long *olen, long flags,
		long* event, bool closeSession) {
	LOG4CXX_DEBUG(loggerXATMI, (char*) "Receive invoked");
	setTpurcode(0);
	int toReturn = -1;
	int len = ::bufferSize(*odata, *olen);
	if (len != -1) {
		LOG4CXX_DEBUG(loggerXATMI, (char*) "receive session: "
				<< session->getId() << " olen: " << olen << " flags: " << flags);
		if (session->getCanRecv()) {
			// TODO Make configurable Default wait time is blocking (x1000 in SynchronizableObject)
			long time = determineTimeout(flags);

			LOG4CXX_DEBUG(loggerXATMI, (char*) "Setting timeout to: " << time);
			try {
				session->blockSignals((flags & TPSIGRSTRT));

				MESSAGE message = session->receive(time);

				if (session->unblockSignals() != 0 && (flags & TPSIGRSTRT) == 0) {
					// signalled during the receive call TPSIGRSTRT wasn't requested
					txx_rollback_only();
				} else if (message.received) {
					if (message.rval == DISCON) {
						*event = TPEV_DISCONIMM;
						txx_rollback_only();
					} else {
						int converted = convertMessage(message, len, odata,
								olen, flags);
						free(message.type);
						free(message.subtype);
						free((char*) message.replyto);
						if (converted == -1) {
							if (closeSession) {
								// Remove the child session
								Session* svcSession = (Session*) getSpecific(
										SVC_SES);
								if (svcSession != NULL) {
									svcSession->removeChildSession(session);
									LOG4CXX_TRACE(
											loggerXATMI,
											(char*) "receive closed child session");
								}
								ptrAtmiBrokerClient->closeSession(id);
								LOG4CXX_TRACE(loggerXATMI,
										(char*) "receive session closed: "
												<< id);
							}
							return toReturn;
						}

						if (message.rcode == TPESVCERR) {
							*event = TPEV_SVCERR;
							setSpecific(TPE_KEY, TSS_TPESVCERR);
							txx_rollback_only();
							// Remove the child session
							Session* svcSession = (Session*) getSpecific(
									SVC_SES);
							if (svcSession != NULL) {
								svcSession->removeChildSession(session);
								LOG4CXX_TRACE(loggerXATMI,
										(char*) "receive triggered close");
							}
							ptrAtmiBrokerClient->closeSession(id);
							closeSession = false;
						} else if (message.rval == TPFAIL) {
							setTpurcode(message.rcode);
							*event = TPEV_SVCFAIL;
							setSpecific(TPE_KEY, TSS_TPESVCFAIL);
							txx_rollback_only();
							// Remove the child session
							Session* svcSession = (Session*) getSpecific(
									SVC_SES);
							if (svcSession != NULL) {
								svcSession->removeChildSession(session);
								LOG4CXX_TRACE(loggerXATMI,
										(char*) "receive triggered close");
							}
							ptrAtmiBrokerClient->closeSession(id);
							closeSession = false;
						} else if (message.rval == TPSUCCESS) {
							toReturn = 0;
							setTpurcode(message.rcode);
							*event = TPEV_SVCSUCC;
							// Remove the child session
							Session* svcSession = (Session*) getSpecific(
									SVC_SES);
							if (svcSession != NULL) {
								svcSession->removeChildSession(session);
								LOG4CXX_TRACE(loggerXATMI,
										(char*) "receive triggered close");
							}
							ptrAtmiBrokerClient->closeSession(id);
							closeSession = false;
						} else if (message.flags & TPRECVONLY) {
							toReturn = 0;
							*event = TPEV_SENDONLY;
							session->setCanSend(true);
							session->setCanRecv(false);
							LOG4CXX_DEBUG(
									loggerXATMI,
									(char*) "receive TPRECVONLY set constraints session: "
											<< session->getId() << " send: "
											<< session->getCanSend()
											<< " recv: "
											<< session->getCanRecv());
						} else if (message.correlationId >= 0) {
							toReturn = 0;
						} else {
							LOG4CXX_ERROR(loggerXATMI,
									(char*) "COULD NOT PARSE RECEIVED MESSAGE");
							setSpecific(TPE_KEY, TSS_TPESYSTEM);
						}
					}
				} else if (TPNOBLOCK & flags) {
					LOG4CXX_DEBUG(loggerXATMI,
							(char*) "Message not immediately available");
					setSpecific(TPE_KEY, TSS_TPEBLOCK);
					closeSession = false;
				} else {
					setSpecific(TPE_KEY, TSS_TPETIME);
					txx_rollback_only();
				}
			} catch (...) {
				LOG4CXX_ERROR(
						loggerXATMI,
						(char*) "Could not set the receive from the destination");
			}
		} else {
			LOG4CXX_DEBUG(loggerXATMI, (char*) "Session can't receive");
			setSpecific(TPE_KEY, TSS_TPEPROTO);
		}
	}

	if (closeSession) {
		// Remove the child session
		Session* svcSession = (Session*) getSpecific(SVC_SES);
		if (svcSession != NULL) {
			svcSession->removeChildSession(session);
			LOG4CXX_TRACE(loggerXATMI, (char*) "receive child session closed");
		}
		ptrAtmiBrokerClient->closeSession(id);
		LOG4CXX_TRACE(loggerXATMI, (char*) "receive session closed: " << id);
	}

	return toReturn;
}

int _get_tperrno(void) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "_get_tperrno");
	char* err = (char*) getSpecific(TPE_KEY);
	int toReturn = 0;
	if (err != NULL) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "found _get_tperrno" << err);
		toReturn = atoi(err);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "returning _get_tperrno" << toReturn);
	return toReturn;
}

long _get_tpurcode(void) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "_get_tpurcode");
	char* rcode = (char*) getSpecific(TPR_KEY);
	long toReturn = 0;
	if (rcode != NULL) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "found _get_tpurcode" << rcode);
		toReturn = atol(rcode);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "returning _get_tpurcode" << toReturn);
	return toReturn;
}

int tpadvertise(char * svcname, void(*func)(TPSVCINFO *)) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpadvertise: " << svcname);
	setSpecific(TPE_KEY, TSS_TPERESET);
	int toReturn = -1;
	if (serverinit(0, NULL) != -1) {
		if (ptrServer->advertiseService(svcname, func)) {
			toReturn = 1;
		}
	} else {
		LOG4CXX_ERROR(loggerXATMI, (char*) "server not initialized");
		setSpecific(TPE_KEY, TSS_TPESYSTEM);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpadvertise return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpunadvertise(char * svcname) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpunadvertise: " << svcname);
	setSpecific(TPE_KEY, TSS_TPERESET);
	int toReturn = -1;
	if (serverinit(0, NULL) != -1) {
		if (svcname && strcmp(svcname, "") != 0) {
			if (ptrServer->isAdvertised(svcname)) {
				ptrServer->removeAdminDestination(svcname, false);
				toReturn = 0;
			} else {
				setSpecific(TPE_KEY, TSS_TPENOENT);
			}
		} else {
			setSpecific(TPE_KEY, TSS_TPEINVAL);
		}
	} else {
		LOG4CXX_ERROR(loggerXATMI, (char*) "server not initialized");
		setSpecific(TPE_KEY, TSS_TPESYSTEM);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpunadvertise return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

char* tpalloc(char* type, char* subtype, long size) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpalloc type: " << type << " size: "
			<< size);
	if (subtype) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "tpalloc subtype: " << type);
	}
	setSpecific(TPE_KEY, TSS_TPERESET);
	char* toReturn = NULL;
	if (clientinit() != -1) {
		toReturn = AtmiBrokerMem::get_instance()->tpalloc(type, subtype, size,
				false);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpalloc returning" << " tperrno: "
			<< tperrno);
	return toReturn;
}

char* tprealloc(char * addr, long size) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tprealloc: " << size);
	setSpecific(TPE_KEY, TSS_TPERESET);
	char* toReturn = NULL;
	if (clientinit() != -1) {
		toReturn = AtmiBrokerMem::get_instance()->tprealloc(addr, size, NULL,
				NULL, false);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tprealloc returning" << " tperrno: "
			<< tperrno);
	return toReturn;
}

void tpfree(char* ptr) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpfree");
	setSpecific(TPE_KEY, TSS_TPERESET);
	if (clientinit() != -1) {
		AtmiBrokerMem::get_instance()->tpfree(ptr, false);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpfree returning" << " tperrno: "
			<< tperrno);
}

long tptypes(char* ptr, char* type, char* subtype) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tptypes called");
	setSpecific(TPE_KEY, TSS_TPERESET);
	LOG4CXX_TRACE(loggerXATMI, (char*) "set the specific");
	long toReturn = -1;
	if (clientinit() != -1) {
		toReturn = AtmiBrokerMem::get_instance()->tptypes(ptr, type, subtype);
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tptypes return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpcall(char * svc, char* idata, long ilen, char ** odata, long *olen,
		long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpcall: " << svc << " ilen: " << ilen
			<< " flags: " << flags);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);

	long toCheck = flags & ~(TPNOTRAN | TPNOCHANGE | TPNOTIME | TPSIGRSTRT
			| TPNOBLOCK);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		if (clientinit() != -1) {
			long tpacallFlags = flags;
			tpacallFlags &= ~TPNOCHANGE;
			int cd = tpacall(svc, idata, ilen, tpacallFlags);

			if (cd != -1) {
				long tpgetrplyFlags = flags;
				tpgetrplyFlags &= ~TPNOTRAN;
				tpgetrplyFlags &= ~TPNOBLOCK;
				toReturn = tpgetrply(&cd, odata, olen, tpgetrplyFlags);
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpcall return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpacall(char * svc, char* idata, long ilen, long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpacall: " << svc << " ilen: " << ilen
			<< " flags: 0x" << std::hex << flags);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);

	long toCheck = flags & ~(TPNOTRAN | TPNOREPLY | TPNOTIME | TPSIGRSTRT
			| TPNOBLOCK);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
		char* qtype = env->getServiceType(svc);
		AtmiBrokerEnv::discard_instance();

		if(qtype != NULL && strcmp(qtype, "topic") == 0 && !(flags & TPNOREPLY)) {
			LOG4CXX_WARN(
					loggerXATMI,
					(char*) "CALL TOPIC SERVICE MUST HAVE TPNOREPLY SET");
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

			if (transactional && (flags & TPNOREPLY)) {
				LOG4CXX_ERROR(
						loggerXATMI,
						(char*) "TPNOREPLY CALLED WITHOUT TPNOTRAN DURING TRANSACTION");
				setSpecific(TPE_KEY, TSS_TPEINVAL);
			} else {
				int len = ::bufferSize(idata, ilen);
				if (len != -1) {
					if (clientinit() != -1) {
						Session* session = NULL;
						int cd = -1;
						try {
							session = ptrAtmiBrokerClient->createSession(false, cd,
									svc);
							LOG4CXX_TRACE(loggerXATMI, (char*) "new session: "
									<< session << " cd: " << cd
									<< " transactional: " << transactional);

							if (cd != -1) {
								if (transactional)
									txx_suspend(cd, tpdiscon);

								if (TPNOREPLY & flags) {
									LOG4CXX_TRACE(loggerXATMI,
											(char*) "TPNOREPLY send");
									toReturn = ::send(session, "", idata, len, cd,
											flags, 0, 0, -1, -1, false, NULL);
									LOG4CXX_TRACE(loggerXATMI,
											(char*) "TPNOREPLY sent");
								} else {
									LOG4CXX_TRACE(loggerXATMI,
											(char*) "expect reply send");
									toReturn = ::send(session,
											session->getReplyTo(), idata, len, cd,
											flags, 0, 0, -1, -1, false, NULL);
									LOG4CXX_TRACE(loggerXATMI,
											(char*) "expect reply sent");
								}

								if (toReturn >= 0) {
									if (TPNOREPLY & flags) {
										toReturn = 0;
										ptrAtmiBrokerClient->closeSession(cd);
										// Remove the child session not required as not added
									} else {
										toReturn = cd;
										Session* svcSession =
											(Session*) getSpecific(SVC_SES);
										if (svcSession != NULL) {
											svcSession->addChildSession(session);
											LOG4CXX_TRACE(
													loggerXATMI,
													(char*) "tpacall child session added");
										}
									}
								} else {
									LOG4CXX_DEBUG(loggerXATMI,
											(char*) "Session got dudded: " << cd);
									ptrAtmiBrokerClient->closeSession(cd);
									// Remove the child session not required as not added
								}
							} else if (tperrno == 0) {
								LOG4CXX_TRACE(loggerXATMI,
										(char*) "tpacall unknown error");
								setSpecific(TPE_KEY, TSS_TPESYSTEM);
								ptrAtmiBrokerClient->closeSession(cd);
								// Remove the child session not required as not added
							}
						} catch (...) {
							LOG4CXX_ERROR(
									loggerXATMI,
									(char*) "tpacall failed to connect to service queue:"
									<< svc);
							setSpecific(TPE_KEY, TSS_TPENOENT);
							if (cd != -1) {
								// Remove the child session
								Session* svcSession = (Session*) getSpecific(
										SVC_SES);
								if (svcSession != NULL) {
									svcSession->removeChildSession(session);
									LOG4CXX_TRACE(
											loggerXATMI,
											(char*) "tpacall failed child session closed");
								}
								ptrAtmiBrokerClient->closeSession(cd);
							}
						}

						if (transactional && toReturn < 0) {
							// txx_suspend was called but there was an error so
							// resume (note we didn't check for TPNOREPLY since we are in
							// the else arm of if (transactional && (flags & TPNOREPLY))
							LOG4CXX_DEBUG(loggerXATMI, (char*) "tpacall resume cd="
									<< cd << " rv=" << toReturn);
							txx_resume(cd);
						}
					}
				}
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpacall return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpconnect(char * svc, char* idata, long ilen, long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpconnect: " << svc << " ilen: "
			<< ilen << " flags: " << flags);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);

	long toCheck = flags & ~(TPNOTRAN | TPSENDONLY | TPRECVONLY | TPNOTIME
			| TPSIGRSTRT | TPNOBLOCK);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		if (!(flags & TPSENDONLY || flags & TPRECVONLY)) {
			setSpecific(TPE_KEY, TSS_TPEINVAL);
		} else {
			int len = 0;
			if (idata != NULL) {
				len = ::bufferSize(idata, ilen);
			}
			if (len != -1) {
				if (clientinit() != -1) {
					int cd = -1;
					Session* session = NULL;
					try {
						session = ptrAtmiBrokerClient->createSession(true, cd,
								svc);
						if (cd != -1) {
							int sendOk = ::send(session, session->getReplyTo(),
									idata, len, cd, flags | TPCONV, 0, 0, -1, -1,
									false, NULL);
							if (sendOk != -1) {
								long olen = 4;
								char* odata = (char*) tpalloc(
										(char*) "X_OCTET", NULL, olen);
								long event = 0;
								::tprecv(cd, &odata, &olen, 0, &event);
								bool connected = strcmp(odata, "ACK") == 0;
								bool err = strcmp(odata, "ERR") == 0;
								tpfree(odata);
								if (connected) {
									toReturn = cd;
									if (flags & TPRECVONLY) {
										session->setCanSend(false);
										LOG4CXX_DEBUG(
												loggerXATMI,
												(char*) "tpconnect set constraints session: "
														<< session->getId()
														<< " send: "
														<< session->getCanSend()
														<< " recv (not changed): "
														<< session->getCanRecv());
									} else {
										session->setCanRecv(false);
										LOG4CXX_DEBUG(
												loggerXATMI,
												(char*) "tpconnect set constraints session: "
														<< session->getId()
														<< " send (not changed): "
														<< session->getCanSend()
														<< " recv: "
														<< session->getCanRecv());
									}

									// Add the child session if this is a service invocation
									Session* svcSession =
											(Session*) getSpecific(SVC_SES);
									if (svcSession != NULL) {
										svcSession->addChildSession(session);
										LOG4CXX_TRACE(
												loggerXATMI,
												(char*) "tpconnect child session opened");
									}
								} else if (err) {
									LOG4CXX_DEBUG(loggerXATMI,
											(char*) "COULD NOT CONNECT: " << cd);
									// Remove the child session
									Session* svcSession =
											(Session*) getSpecific(SVC_SES);
									if (svcSession != NULL) {
										svcSession->removeChildSession(session);
										LOG4CXX_TRACE(
												loggerXATMI,
												(char*) "tpconnect failed connect session closed");
									}
									ptrAtmiBrokerClient->closeSession(cd);
									setSpecific(TPE_KEY, TSS_TPENOENT);
								} else {
									LOG4CXX_DEBUG(loggerXATMI,
											(char*) "COULD NOT CONNECT: " << cd);
									// Remove the child session
									Session* svcSession =
											(Session*) getSpecific(SVC_SES);
									if (svcSession != NULL) {
										svcSession->removeChildSession(session);
										LOG4CXX_TRACE(
												loggerXATMI,
												(char*) "tpconnect failed connect session closed");
									}
									ptrAtmiBrokerClient->closeSession(cd);
									setSpecific(TPE_KEY, TSS_TPESYSTEM);
								}
							} else {
								LOG4CXX_DEBUG(loggerXATMI,
										(char*) "Session got dudded: " << cd);
								// Remove the child session
								Session* svcSession = (Session*) getSpecific(
										SVC_SES);
								if (svcSession != NULL) {
									svcSession->removeChildSession(session);
									LOG4CXX_TRACE(
											loggerXATMI,
											(char*) "tpconnect dudded child session closed");
								}
								ptrAtmiBrokerClient->closeSession(cd);
							}
						} else if (tperrno == 0) {
							setSpecific(TPE_KEY, TSS_TPESYSTEM);
						}
					} catch (...) {
						LOG4CXX_ERROR(
								loggerXATMI,
								(char*) "tpconnect failed to connect to service queue");
						setSpecific(TPE_KEY, TSS_TPENOENT);
						if (cd != -1) {
							// Remove the child session
							Session* svcSession = (Session*) getSpecific(
									SVC_SES);
							if (svcSession != NULL) {
								svcSession->removeChildSession(session);
								LOG4CXX_TRACE(
										loggerXATMI,
										(char*) "tpconnect fail child session closed");
							}
							ptrAtmiBrokerClient->closeSession(cd);
						}
					}
				}
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpconnect return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

void tpgetanyCallback(int sessionId, bool remove) {
	lock.lock();
	if (!remove) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "tpgetanyCallback adding: "
				<< sessionId);
		sessionIds.push_back(sessionId);
		lock.notify();
	} else {
		LOG4CXX_TRACE(loggerXATMI, (char*) "tpgetanyCallback removing: "
				<< sessionId);
		for (std::vector<int>::iterator it = sessionIds.begin(); it
				!= sessionIds.end(); it++) {
			int id = (*it);
			if (id == sessionId) {
				sessionIds.erase(it);
				LOG4CXX_TRACE(loggerXATMI, (char*) "tpgetanyCallback removed: "
						<< sessionId);
				break;
			}
		}
	}
	lock.unlock();
}

int tpgetrply(int *id, char ** odata, long *olen, long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpgetrply " << id);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);

	if (odata == NULL || *odata == NULL) {
		setSpecific(TPE_KEY, TSS_TPEINVAL);
		return toReturn;
	}

	long toCheck = flags & ~(TPGETANY | TPNOCHANGE | TPNOBLOCK | TPNOTIME
			| TPSIGRSTRT);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		if (clientinit() != -1) {
			if (flags & TPGETANY) {
				lock.lock();
				if (sessionIds.size() == 0) {
					long timeout = determineTimeout(flags);
					if (timeout >= 0) {
						lock.wait(timeout);
					}
				}
				if (sessionIds.size() == 0) {
					LOG4CXX_TRACE(loggerXATMI,
							(char*) "tpgetany no message available");
					setSpecific(TPE_KEY, TSS_TPETIME);
				} else {
					*id = sessionIds.front();
					sessionIds.erase(sessionIds.begin());
				}
			}
			if (tperrno == 0 && id && olen) {
				Session* session = ptrAtmiBrokerClient->getSession(*id);
				if (session == NULL) {
					setSpecific(TPE_KEY, TSS_TPEBADDESC);
				} else if (session->getIsConv()) {
					setSpecific(TPE_KEY, TSS_TPEBADDESC);
					LOG4CXX_WARN(loggerXATMI,
							(char*) "Session was conversational: " << id);
				} else {
					long event = 0;
					toReturn = ::receive(*id, session, odata, olen, flags,
							&event, true);
					txx_resume(*id);
				}
			} else {
				setSpecific(TPE_KEY, TSS_TPEINVAL);
			}
			if (flags & TPGETANY) {
				lock.unlock();
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpgetrply return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpcancel(int id) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpcancel " << id);
	int toReturn = -1;

	setSpecific(TPE_KEY, TSS_TPERESET);
	if (::txx_isCdTransactional(id)) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "tpcancel not allowed (TSS_TPETRAN)");
		setSpecific(TPE_KEY, TSS_TPETRAN);
	} else if (clientinit() != -1) {
		if (getSpecific(TSS_KEY)) {
			setSpecific(TPE_KEY, TSS_TPETRAN);
		}
		Session* session = ptrAtmiBrokerClient->getSession(id);
		if (session != NULL) {
			// Remove the child session
			Session* svcSession = (Session*) getSpecific(SVC_SES);
			if (svcSession != NULL) {
				svcSession->removeChildSession(session);
				LOG4CXX_TRACE(loggerXATMI,
						(char*) "tpcancel child session closed");
			}
			ptrAtmiBrokerClient->closeSession(id);
			LOG4CXX_TRACE(loggerXATMI, (char*) "tpcancel session closed");
			toReturn = 0;
		} else {
			setSpecific(TPE_KEY, TSS_TPEBADDESC);
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpcancel return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tpsend(int id, char* idata, long ilen, long flags, long *revent) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpsend " << id);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);

	long toCheck = flags & ~(TPRECVONLY | TPNOBLOCK | TPNOTIME | TPSIGRSTRT
			| TPNOBLOCK);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		int len = ::bufferSize(idata, ilen);
		if (len != -1) {
			if (clientinit() != -1) {
				Session* session = (Session*) getSpecific(SVC_SES);
				if (session != NULL) {
					if (session->getId() != id) {
						session = NULL;
					}
				}
				if (session == NULL) {
					if (clientinit() != -1) {
						session = ptrAtmiBrokerClient->getSession(id);
						if (session == NULL) {
							setSpecific(TPE_KEY, TSS_TPEBADDESC);
							len = -1;
						}
					}
				}
				if (len != -1) {
					if (session->getLastEvent() != 0) {
						if (revent != 0) {
							*revent = session->getLastEvent();
							LOG4CXX_DEBUG(
									loggerXATMI,
									(char*) "Session has event, will be closed: "
											<< *revent);
						} else {
							LOG4CXX_ERROR(
									loggerXATMI,
									(char*) "Session has event, will be closed: "
											<< session->getLastEvent());
						}

						if (session->getLastEvent() == TPEV_SVCFAIL) {
							setTpurcode(session->getLastRCode());
						} else if (session->getLastEvent() == TPEV_SVCSUCC
								|| session->getLastEvent() == TPEV_DISCONIMM) {
							setSpecific(TPE_KEY, TSS_TPEEVENT);
							toReturn = -1;
						}
						// Remove the child session
						Session* svcSession = (Session*) getSpecific(SVC_SES);
						if (svcSession != NULL) {
							svcSession->removeChildSession(session);
							LOG4CXX_TRACE(loggerXATMI,
									(char*) "tpsend child session closed");
						}
						ptrAtmiBrokerClient->closeSession(id);
						LOG4CXX_TRACE(loggerXATMI,
								(char*) "tpsend closed session");
					} else {
						toReturn = ::send(session, session->getReplyTo(),
								idata, len, id, flags, 0, 0, -1, -1, false, NULL);
						if (toReturn != -1 && flags & TPRECVONLY) {
							session->setCanSend(false);
							session->setCanRecv(true);
							LOG4CXX_DEBUG(loggerXATMI,
									(char*) "tpsend set constraints session: "
											<< session->getId() << " send: "
											<< session->getCanSend()
											<< " recv: "
											<< session->getCanRecv());
						}
					}
				}
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpsend return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

int tprecv(int id, char ** odata, long *olen, long flags, long* event) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tprecv " << id);
	int toReturn = -1;
	setSpecific(TPE_KEY, TSS_TPERESET);
	*event = 0;

	long toCheck = flags & ~(TPNOCHANGE | TPNOBLOCK | TPNOTIME | TPSIGRSTRT);

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		if (clientinit() != -1) {
			Session* session = (Session*) getSpecific(SVC_SES);
			if (session != NULL && session->getId() != id) {
				session = NULL;
			}
			if (session == NULL) {
				if (clientinit() != -1) {
					session = ptrAtmiBrokerClient->getSession(id);
				}
			}
			if (session == NULL) {
				setSpecific(TPE_KEY, TSS_TPEBADDESC);
			} else if (!session->getIsConv()) {
				setSpecific(TPE_KEY, TSS_TPEBADDESC);
				LOG4CXX_WARN(loggerXATMI,
						(char*) "Session was not conversational: " << id);
			} else {
				toReturn = ::receive(id, session, odata, olen, flags, event,
						false);
				if (*event == TPEV_SVCSUCC || *event == TPEV_DISCONIMM
						|| *event == TPEV_SENDONLY || *event == TPEV_SVCFAIL
						|| *event == TPEV_SVCERR) {
					setSpecific(TPE_KEY, TSS_TPEEVENT);
					toReturn = -1;
				}
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tprecv return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

void tpreturn(int rval, long rcode, char* idata, long ilen, long flags) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpreturn " << rval);
	setSpecific(TPE_KEY, TSS_TPERESET);

	long toCheck = flags;

	if (toCheck != 0) {
		LOG4CXX_TRACE(loggerXATMI, (char*) "invalid flags remain: " << toCheck);
		setSpecific(TPE_KEY, TSS_TPEINVAL);
	} else {
		if (clientinit() != -1) {
			int len = 0;
			if (idata != NULL) {
				len = ::bufferSize(idata, ilen);
			}
			Session* session = (Session*) getSpecific(SVC_SES);
			ServiceDispatcher* dispatcher = (ServiceDispatcher*) getSpecific(
					SVC_KEY);
			if (session != NULL) {
				if (!session->getCanSend()
						&& !(rval == TPFAIL && idata == NULL)) {
					LOG4CXX_TRACE(loggerXATMI, (char*) "generating TPESVCERR");
					rcode = TPESVCERR;
					rval = TPFAIL;
				}
				if (rcode == TPESVCERR || len == -1) {
					rval = TPFAIL;
				}
				if (!session->getCanSend()) {
					rval = DISCON;
				}
				session->setCanRecv(false);

				std::vector<Session*> childSessions =
						session->getChildSessions();
				if (childSessions.size() > 0) {
					LOG4CXX_ERROR(loggerXATMI,
							(char*) "Open child sessions detected");
					for (std::vector<Session*>::iterator it =
							childSessions.begin(); it != childSessions.end(); it++) {
						Session* session = (*it);
						if (session->getIsConv()) {
							::tpdiscon(session->getId());
						} else {
							::tpcancel(session->getId());
						}
					}
					rcode = TPESVCERR;
					rval = TPFAIL;
				}

				if (rcode == TPESVCERR || len == -1) {
					// mark rollback only and disassociate tx if present
					txx_rollback_only();
					if (getSpecific(TSS_KEY) != NULL)
						txx_release_control(txx_unbind(true));
					if (idata != NULL) {
						::tpfree(idata);
					}
					::send(session, "", NULL, 0, 0, flags, rval, TPESVCERR, -1, -1,
							false, NULL);
					LOG4CXX_TRACE(loggerXATMI, (char*) "sent TPESVCERR");
					if (dispatcher != NULL) {
						LOG4CXX_TRACE(loggerXATMI,
								(char*) "update error counter");
						dispatcher->updateErrorCounter();
					}
				} else {
					if (rval != TPSUCCESS && rval != TPFAIL) {
						rval = TPFAIL;
						LOG4CXX_TRACE(loggerXATMI, (char*) "generating TPFAIL");
					}
					if (rval == TPFAIL) {
						txx_rollback_only();
						LOG4CXX_TRACE(loggerXATMI, (char*) "will send TPFAIL");
						if (dispatcher != NULL) {
							LOG4CXX_TRACE(loggerXATMI,
									(char*) "update error counter");
							dispatcher->updateErrorCounter();
						}
					}

					// TODO send a fail if any work done within the service
					// caused its transaction to be marked rollback-only


					// mark rollback only and disassociate tx if present
					if (getSpecific(TSS_KEY) != NULL)
						txx_release_control(txx_unbind((rval == TPFAIL)));

					::send(session, "", idata, len, 0, flags, rval, rcode, -1, -1,
							false, NULL);
					LOG4CXX_TRACE(loggerXATMI, (char*) "sent response");
				}

				::tpfree(idata);
				session->setSendTo(NULL);
				session->setCanSend(false);
				LOG4CXX_DEBUG(loggerXATMI,
						(char*) "tpreturn set constraints session: "
								<< session->getId() << " send: "
								<< session->getCanSend() << " recv: "
								<< session->getCanRecv());

			} else {
				LOG4CXX_DEBUG(loggerXATMI, (char*) "Session is NULL");
				setSpecific(TPE_KEY, TSS_TPEPROTO);
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpreturn returning" << " tperrno: "
			<< tperrno);
}

int tpdiscon(int id) {
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpdiscon " << id);
	setSpecific(TPE_KEY, TSS_TPERESET);
	int toReturn = -1;
	if (clientinit() != -1) {
		LOG4CXX_DEBUG(loggerXATMI, (char*) "end - id: " << id);
		Session* session = ptrAtmiBrokerClient->getSession(id);
		if (session == NULL) {
			setSpecific(TPE_KEY, TSS_TPEBADDESC);
		} else {
			// CHECK TO MAKE SURE THE REMOTE SIDE IS "EXPECTING" DISCONNECTS STILL
			if (session->getLastEvent() == 0) {
				// SEND THE DISCONNECT TO THE REMOTE SIDE
				::send(session, "", NULL, 0, id, TPNOTRAN, DISCON, 0, -1, -1, false, NULL);
			}
			try {
				if (getSpecific(TSS_KEY)) {
					toReturn = txx_rollback_only();
				}

				// Remove the child session
				Session* svcSession = (Session*) getSpecific(SVC_SES);
				if (svcSession != NULL) {
					svcSession->removeChildSession(session);
					LOG4CXX_TRACE(loggerXATMI,
							(char*) "tpdiscon child session closed");
				}
				ptrAtmiBrokerClient->closeSession(id);
				LOG4CXX_TRACE(loggerXATMI, (char*) "tpdiscon session closed");
			} catch (...) {
				LOG4CXX_ERROR(loggerXATMI, (char*) "tpdiscon: call failed");
				setSpecific(TPE_KEY, TSS_TPESYSTEM);
			}
		}
	}
	LOG4CXX_TRACE(loggerXATMI, (char*) "tpdiscon return: " << toReturn
			<< " tperrno: " << tperrno);
	return toReturn;
}

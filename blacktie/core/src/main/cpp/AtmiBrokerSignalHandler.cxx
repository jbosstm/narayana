/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and others contributors as indicated
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
#include "AtmiBrokerSignalHandler.h"
#include "ThreadLocalStorage.h"
#include "ace/Thread.h"
#include "ace/OS_NS_Thread.h"

#define SIGCHK(err, msg)	{if ((err) != 0) LOG4CXX_WARN(logger_, (char*) (msg) << " error: " << err);}

int default_handlesigs[] = {SIGINT, SIGTERM, 0};
int default_blocksigs[] = {SIGQUIT, SIGABRT, SIGHUP, SIGALRM, SIGUSR1, SIGUSR2, 0};

log4cxx::LoggerPtr AtmiBrokerSignalHandler::logger_(log4cxx::Logger::getLogger("AtmiBrokerSignalHandler"));

ACE_Sig_Handler AtmiBrokerSignalHandler::handler_;

AtmiBrokerSignalHandler::AtmiBrokerSignalHandler(int* hsignals, int* bsignals)
{
	int* sigp;

	SIGCHK(ACE_OS::sigemptyset(&hss_), "sigemptyset");
	SIGCHK(ACE_OS::sigemptyset(&bss_), "sigemptyset");

	for (sigp = hsignals; *sigp != 0; sigp++) {
		SIGCHK(ACE_OS::sigaddset(&hss_, *sigp), "sigaddset");
		SIGCHK(ACE_OS::sigaddset(&bss_, *sigp), "sigaddset");
	}
	for (sigp = bsignals; *sigp != 0; sigp++) {
		SIGCHK(ACE_OS::sigaddset(&bss_, *sigp), "sigaddset");
	}
#ifndef WIN32
	for (int i = 1; i < ACE_NSIG; i++)
		if (ACE_OS::sigismember(&bss_, i))
			handler_.register_handler(i, this);
#endif
}

AtmiBrokerSignalHandler::~AtmiBrokerSignalHandler()
{
#ifndef WIN32
	for (int i = 1; i < ACE_NSIG; i++)
		if (ACE_OS::sigismember(&bss_, i))
			handler_.remove_handler(i);
#endif
}

void AtmiBrokerSignalHandler::addSignalHandler(int (*handler)(int signum), bool front)
{
    std::vector<int (*)(int)>::iterator it;

	LOG4CXX_DEBUG(logger_, (char*) "adding signal handler " << &handler << " size " << handlers_.size());

    if (front)
        handlers_.insert(handlers_.begin(), handler);
    else
        handlers_.insert(handlers_.end(), handler);
}

int AtmiBrokerSignalHandler::handle_signal(int sig, siginfo_t *, ucontext_t *)
{
	sigset_t* pending = (sigset_t*) getSpecific(TSS_SIG_KEY);
	std::vector<int (*)(int)>::iterator it;
	bool doexit = false;

	if (pending)
		SIGCHK(ACE_OS::sigaddset(pending, sig), "sigaddset");

	if (ACE_OS::sigismember(&hss_, sig)) {
		LOG4CXX_DEBUG(logger_, (char*) "handling signal " << sig);

		for (it = handlers_.begin(); it < handlers_.end(); it++) {
			LOG4CXX_DEBUG(logger_, (char*) "running handler " << (*it));
			if ((*it)(sig) == -1)
				doexit = true;
		}
	} else {
		LOG4CXX_DEBUG(logger_, (char*) "ignoring signal " << sig);
	}

	if (doexit) {
		LOG4CXX_INFO(logger_, (char*) "Unregistering signal handler after signal " << sig);
	}

	return 0;	// -1 unregisters this handler
}

int AtmiBrokerSignalHandler::blockSignals(bool sigRestart) {
	return block_sigs(&bss_, NULL, true, !sigRestart);
}

int AtmiBrokerSignalHandler::unblockSignals() {
	sigset_t pending;
	int nsigs = block_sigs(&bss_, &pending, false, false);

	LOG4CXX_DEBUG(logger_, (char*) " unblockSignals returned " << nsigs);
	for (int i = 1; i <= NSIG; i++) {
		if (ACE_OS::sigismember(&pending, i) == 1) {
			LOG4CXX_DEBUG(logger_, (char*) " \treceived sig during syscall " <<  i);
		}
	}

	return nsigs;
}

/**
 * block or unblock signals.
 * @param mask
 * 	the set of signals to block/unblock
 * @param pending
 * 	out parameter for holding any signals that were received since the last blocking call
 * @param block
 * 	if true then block signals otherwise unblock them
 * @param informational
 *  if true don't actually block or unblock signals but still return any signals that were
 *  received since the previous call
 *
 * @return
 * 	the number of signals received since the last call
 */
int AtmiBrokerSignalHandler::block_sigs(sigset_t* mask, sigset_t* pending, bool block, bool informational)
{
	int err = 0;
	int i;
	int sigcnt = 0;
	sigset_t* tsspending = (sigset_t*) getSpecific(TSS_SIG_KEY);

	if (block) {
		if (tsspending == NULL) {
			// create a new TSS holder for the signal set that may be received before
			// the next matching call
			if ((tsspending = (sigset_t*) malloc(sizeof (sigset_t))) == NULL)
				err = -1;
			else if (ACE_OS::sigemptyset(tsspending) != 0)
				err = -1;
			else
				setSpecific(TSS_SIG_KEY, tsspending);
		} else {
			LOG4CXX_WARN(logger_, (char*) "sigblock called but already blocking signals");
		}
	} else {
		// about to unblock signals - see if there was signal since they were blocked

		if (tsspending == NULL) {
			LOG4CXX_WARN(logger_, (char*) "sigunblock called but signals are not blocked");
		} else {
			destroySpecific(TSS_SIG_KEY);

			if (pending != NULL) {
				// see if there are any pending signals
				sigset_t pmask;

				if (ACE_OS::sigemptyset(pending) == -1 || ACE_OS::sigemptyset(&pmask) == -1) {
					LOG4CXX_WARN(logger_, (char*) " sigemptyset error");
					err = -1;
				} else {
					// no ACE_OS equivalent for sigpending so ifdef the call (WIN32 does not support signals)
#ifndef WIN32
					if (sigpending(&pmask) != 0)
						err = -1;
#endif
					for (i = 1; i <= NSIG; i++)
						if (ACE_OS::sigismember(tsspending, i) == 1 ||
							ACE_OS::sigismember(&pmask, i) == 1) {

							setSpecific(TPE_KEY, TSS_TPGOTSIG);
    						(void) ACE_OS::sigaddset(pending, i);
							sigcnt += 1;
						}
				}
			}

			free(tsspending);
		}
	}

	LOG4CXX_DEBUG(logger_, (char*) "blocksigs=" << block << " informational=" << informational << " rval=" << sigcnt);
	if (!informational) {
#ifndef WIN32
		// TODO figure out how to handle signals on windows
    	if (ACE_OS::pthread_sigmask((block ? SIG_BLOCK : SIG_UNBLOCK), mask, NULL) != 0)
			err = -1;
#endif
	}

	return (err != 0 ? err : sigcnt);
}

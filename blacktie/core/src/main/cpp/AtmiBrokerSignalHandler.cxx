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
#include "apr_signal.h"
#include <stdlib.h>

#define SIGCHK(err, msg)	{if ((err) != 0) LOG4CXX_WARN(logger_, (char*) (msg) << " error: " << err);}

#ifndef WIN32
int default_handlesigs[] = {SIGINT, SIGTERM, 0};
int default_blocksigs[] = {SIGQUIT, SIGABRT, SIGHUP, SIGALRM, SIGUSR1, SIGUSR2, 0};
#else
int default_handlesigs[] = {0};
int default_blocksigs[] = {0};
#endif

log4cxx::LoggerPtr AtmiBrokerSignalHandler::logger_(log4cxx::Logger::getLogger("AtmiBrokerSignalHandler"));

std::multimap<int, AtmiBrokerSignalHandler*> AtmiBrokerSignalHandler::handler_;

void static_handler(int sig)
{
     std::pair< 
	std::multimap<int, AtmiBrokerSignalHandler*>::iterator,
        std::multimap<int, AtmiBrokerSignalHandler*>::iterator> ret;

     ret = AtmiBrokerSignalHandler::handler_.equal_range(sig);

     for(std::multimap<int, AtmiBrokerSignalHandler*>::iterator it = ret.first; it != ret.second; ++it)
     {
	it->second->handle_signal(sig);
     }

}

AtmiBrokerSignalHandler::AtmiBrokerSignalHandler(int* hsignals, int* bsignals)
{
	int* sigp;

#ifndef WIN32
	SIGCHK(sigemptyset(&hss_), "sigemptyset");
	SIGCHK(sigemptyset(&bss_), "sigemptyset");

	for (sigp = hsignals; *sigp != 0; sigp++) {
		SIGCHK(sigaddset(&hss_, *sigp), "sigaddset");
		SIGCHK(sigaddset(&bss_, *sigp), "sigaddset");
	}
	for (sigp = bsignals; *sigp != 0; sigp++) {
		SIGCHK(sigaddset(&bss_, *sigp), "sigaddset");
	}
	for (int i = 1; i < NSIG; i++)
		if (sigismember(&bss_, i))
		{
			if(handler_.find(i) == handler_.end())
			   apr_signal(i, static_handler);
			handler_.insert(std::pair<int, AtmiBrokerSignalHandler*>(i, (AtmiBrokerSignalHandler*)this));
		}
#endif
}

AtmiBrokerSignalHandler::~AtmiBrokerSignalHandler()
{
#ifndef WIN32
     std::pair<
        std::multimap<int, AtmiBrokerSignalHandler*>::iterator,
        std::multimap<int, AtmiBrokerSignalHandler*>::iterator> ret;

	for (int i = 1; i < NSIG; i++)
        {
		if (sigismember(&bss_, i))
                {
		  ret = handler_.equal_range(i);
                  std::multimap<int, AtmiBrokerSignalHandler*>::iterator found = handler_.end();
                  for(std::multimap<int, AtmiBrokerSignalHandler*>::iterator it = ret.first; it != ret.second && found == handler_.end(); ++it)
                  {
			if(it->second == this)
			  found = it;
                  }
                  if(found != handler_.end())
			handler_.erase(found);
		  if(handler_.find(i) == handler_.end())
			apr_signal(i,NULL);
                }

       }

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

int AtmiBrokerSignalHandler::handle_signal(int sig)
{
#ifndef WIN32
	sigset_t* pending = (sigset_t*) getSpecific(TSS_SIG_KEY);
	std::vector<int (*)(int)>::iterator it;
	bool doexit = false;

	if (pending)
		SIGCHK(sigaddset(pending, sig), "sigaddset");

	if (sigismember(&hss_, sig)) {
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
#endif
	return 0;	// -1 unregisters this handler
}

int AtmiBrokerSignalHandler::blockSignals(bool sigRestart) {
#ifndef WIN32
	return block_sigs(&bss_, NULL, true, !sigRestart);
#else
	return 0;
#endif
}

int AtmiBrokerSignalHandler::unblockSignals() {
#ifndef WIN32
	sigset_t pending;
	int nsigs = block_sigs(&bss_, &pending, false, false);

	LOG4CXX_DEBUG(logger_, (char*) " unblockSignals returned " << nsigs);
	for (int i = 1; i <= NSIG; i++) {
		if (sigismember(&pending, i) == 1) {
			LOG4CXX_DEBUG(logger_, (char*) " \treceived sig during syscall " <<  i);
		}
	}

	return nsigs;
#else
	return 0;
#endif
}

#ifndef WIN32
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
			else if (sigemptyset(tsspending) != 0)
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

				if (sigemptyset(pending) == -1 || sigemptyset(&pmask) == -1) {
					LOG4CXX_WARN(logger_, (char*) " sigemptyset error");
					err = -1;
				} else {
					// no ACE_OS equivalent for sigpending so ifdef the call (WIN32 does not support signals)
#ifndef WIN32
					if (sigpending(&pmask) != 0)
						err = -1;
#endif
					for (i = 1; i <= NSIG; i++)
						if (sigismember(tsspending, i) == 1 ||
							sigismember(&pmask, i) == 1) {

							setSpecific(TPE_KEY, TSS_TPGOTSIG);
    						(void) sigaddset(pending, i);
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
    	if (pthread_sigmask((block ? SIG_BLOCK : SIG_UNBLOCK), mask, NULL) != 0)
			err = -1;
#endif
	}

	return (err != 0 ? err : sigcnt);
}
#endif

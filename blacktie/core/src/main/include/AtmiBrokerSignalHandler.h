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
#ifndef _ATMIBROKERSIGNALHANDLER_H
#define _ATMIBROKERSIGNALHANDLER_H

#include <vector>
#include "atmiBrokerCoreMacro.h"
#include "log4cxx/logger.h"

#include <signal.h>

extern int default_handlesigs[];
extern int default_blocksigs[];

class BLACKTIE_CORE_DLL AtmiBrokerSignalHandler 
{
public:
	AtmiBrokerSignalHandler(int* hsignals = default_handlesigs, int* bsignals = default_blocksigs);
	virtual ~AtmiBrokerSignalHandler();

	virtual int handle_signal(int signum);

	/**
	 * Add a handler to be called when one of the signals in the set of handleable signals
	 * is raised.
	 * @param sigHandler
	 * 	the function to be called when the signal is raised. If any handler returns -1 then
	 * 	the process will exit after all handlers have finished
	 * @param front
	 *  if true the handler is placed at the front of the handler list
	 */
	void addSignalHandler(int (*sigHandler)(int signum), bool front = false);

    /**
     * If sigRestart is true start blocking signals (ie system calls will not be interrupted
     * by the receipt of a signal). Signals will be blocked until the next call to
     * unblockSignals is made.
     *
     * If sigRestart is false and a signal is received then any system call is interrupted
     * and the condition is notified when the matching call to unblockSignals is made.
     * For XATMI users this translates into tperrno being set to TPEGOTSIG.
     * 
     * @return 0 on success
     */
	int blockSignals(bool sigRestart);

    /**
     * Reverses the effect of @see AtmiBrokerSignalHandler::blockSignals.
     * @return
     *  the number of signals delivered since the protected code block was entered or
     *  a negative value if there was an error
     */
	int unblockSignals();

private:
#ifndef WIN32
	sigset_t bss_;
	sigset_t hss_;
#endif
	std::vector<int (*)(int)> handlers_;	// the actual signal handlers

#ifndef WIN32
	int block_sigs(sigset_t*, sigset_t*, bool, bool);
#endif

private:
	static log4cxx::LoggerPtr logger_;

public:
        static std::multimap<int, AtmiBrokerSignalHandler*> handler_;
};
#endif // _ATMIBROKERSIGNALHANDLER_H

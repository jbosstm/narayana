/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#ifndef _TXX_H
#define _TXX_H

#include "xa.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Warning this interface is for use by blacktie modules only. Calling methods in this
 * interface will invalidate your warranty
 */

/**
 * suspend/resume Resource Managers whilst there are outstanding xatmi calls
 * @param cd
 * 	xatmi call descriptor
 * @param invalidate
 * 	callback for invalidating the passed in call descriptor (will only be called
 * 	if the client tries to end a transaction whilst the descriptor is still active)
 */
extern BLACKTIE_TX_DLL int txx_resume(int cd);
extern BLACKTIE_TX_DLL int txx_suspend(int cd, int (*invalidate)(int cd));
extern BLACKTIE_TX_DLL int txxx_resume();
extern BLACKTIE_TX_DLL int txxx_suspend();
/**
 * test wether the supplied xatmi call descriptor is transactional
 */
extern BLACKTIE_TX_DLL bool txx_isCdTransactional(int cd);

/**
 * Modify the transaction associated with the target thread such that the only
 * possible outcome of the transaction is to roll back the transaction
 */
extern BLACKTIE_TX_DLL int txx_rollback_only();

/**
 * stop the transaction manager proxy
 */
extern BLACKTIE_TX_DLL void txx_stop(void);

/**
 * Associate a transaction with the current thread:
 * - input parameter 1 is a serialized transaction (ie an IOR)
 * - input parameter 2 is a the time in seconds during which the txn remains alive
 *
 * Return a non-negative value on success
 */
extern BLACKTIE_TX_DLL int txx_associate_serialized(char *, long);

/**
 * Convert the transaction associated with the calling thread into a string.
 * @param ttl
 * 	output param holds the remaining time before which the txn is subject to rollback,
 * 	a value of -1 indicates that the txn is not subject to timeouts
 *
 * Return a string representation of the txn
 */
extern BLACKTIE_TX_DLL char* txx_serialize(long* ttl);

/**
 * disassociate a transaction from the current thread
 * (also suspends all Resource Managers linked into the running applications)
 * returns the transaction that was previously associated
 *
 * If the request argument rollback is set to true then the transaction is
 * marked rollback only prior to disassociation from the thread
 *
 * Returns the OTS control associated with the current thread. The caller
 * is responsible for calling release_control on the returned value.
 */
extern BLACKTIE_TX_DLL void * txx_unbind(bool rollback);

/**
 * Return the OTS control associated with the current thread
 * The caller is responsible for calling release_control on the
 * returned control.
 */
extern BLACKTIE_TX_DLL void * txx_get_control();

/**
 * Release an OTS control returned by:
 * get_control
 * txx_unbind
 */
extern BLACKTIE_TX_DLL void txx_release_control(void *);

/**
 * Return the time left in seconds before any txn bound to the callers
 * thread becomes eligible for rollback.
 * @param
 * 	the time left in seconds. A negative value means it is eligible for
 * 	rollback. A value of 0 means 
 * @return
 * 	-1 if there in no txn
 * 	0 if the ttl param was updated with a valid time to live value
 * 	1 if the current txn is not subject to a timeout
 */
extern BLACKTIE_TX_DLL int txx_ttl(long* ttl);

#ifdef __cplusplus
}
#endif

#endif //_TXX_H

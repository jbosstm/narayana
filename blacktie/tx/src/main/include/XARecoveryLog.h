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
#ifndef _XARECOVERYLOG_H
#define _XARECOVERYLOG_H

#include "xa.h"
#include <fstream>
#include "atmiBrokerTxMacro.h"
#include "RMException.h"
//#include "SynchronizableObject.h"

/*
 * Simple log for recording branch recovery coordinators at
 * prepare time, for deleting them after commit and for
 * reading outstanding branches after a crash.
 *
 * As branch records are created and deleted the backing store for
 * the log is kept in sync with the in memory copy thus ensuring
 * that the log can be recreated correctly after a crash.
 *
 * The algorithm manages a chunk of memory from allocations are
 * a created and freed (cf malloc and free). Unfortunately, the
 * standard allocator algorithms cannot be used since they
 * generally rely on multiple memory locations for managing the
 * the storage area:
 * 
 * - a recovery record needs to be created or deleted in a single
 *   atomic sync operations.
 *
 */

/*
 * A recovery record consists of an XID and a CosTransactions RecoveryCoordinator IOR
 * The string form of the IOR follows directly after the recovery record).
 */
typedef struct rrec {
	size_t next;	// offset to the next free block
	long magic;		// mark to indicate the block is allocated
	XID xid;
} rrec_t;

class BLACKTIE_TX_DLL XARecoveryLog {
public:
	XARecoveryLog(const char *logfile = 0) throw (RMException);
	~XARecoveryLog();

	// add, lookup and delete log records
	int add_rec(XID& xid, char *ior);
	char* find_ior(XID& xid);
	int del_rec(XID& xid);

	// mechanism for iterating through the log
	rrec_t* find_next(rrec_t* from);
	const char *get_ior(rrec_t&);
	bool isOpen() {return log_.is_open();}

private:
	std::fstream log_;
	rrec_t* arena_;
	size_t nblocks_;
	size_t maxblocks_;	// limit the arena to this many blocks (configurable)
//TODO	SynchronizableObject lock_;

	void sync_rec(void* p, size_t sz);
//	void check_log();
	int find(XID& xid, rrec_t** prev, rrec_t** next);
	bool load_log(const char* logname);
	rrec_t* get_blocks(size_t nblocks, bool dosync);

	bool lock();
	bool unlock();
};

#endif //_XARECOVERYLOG_H

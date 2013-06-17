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
#include <iostream>
#include <string>
#include <sstream>
#include <stdio.h>
#include <errno.h>

#include "xa.h"

#if 1
#include "log4cxx/logger.h"
#include "ace/OS_NS_string.h"
#include "ace/OS_NS_stdio.h"
#include "ace/OS_NS_stdlib.h"
#include "AtmiBrokerEnv.h"
#include "SynchronizableObject.h"
#else
#include <stdlib.h>
#include <string.h>
#endif

#include "XARecoveryLog.h"

#define INUSE 0xaf12L   // marker to indicate that the block is allocated
#define NBLOCKS 0x100   // the minimum number of blocks to allocate when expanding the arena
#define MAXBLOCKS   "0x1000"	// limit the size of the arena to this many blocks
#define IOR(rr) ((char *) (rr + 1)) // extract the IOR from a recovery record

#define BSIZE	(sizeof (rrec_t))
#define NEXT_REC(p)	(p + p->next)

// the persistent store for recovery records
static const char* DEF_LOG = "rclog";
static char RCLOGPATH[1024];

static SynchronizableObject lock_;

extern std::ostream& operator<<(std::ostream &os, const XID& xid);

log4cxx::LoggerPtr xarcllogger(log4cxx::Logger::getLogger("TxXARecoveryLog"));

using namespace std;

static int compareXids(const XID& xid1, const XID& xid2)
{
	char *x1 = (char *) &xid1;
	char *x2 = (char *) &xid2;
	char *e = (char *) (x1 + sizeof (XID));

	while (x1 < e)
		if (*x1 < *x2)
			return -1;
		else if (*x1++ > *x2++)
			return 1;

	return 0;
}

/**
 * locate the path to the backing store for the recovery log
 */
static void init_logpath(const char *fname)
{
	if (fname) {
		ACE_OS::snprintf(RCLOGPATH, sizeof (RCLOGPATH), "%s", fname);
	} else {
		// if fname is not passed see if the log name is set in the environent
#if 1
		AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
		const char *rcLog   = env->getenv((char*) "BLACKTIE_RC_LOG_NAME", DEF_LOG);
		const char *servName = env->getenv((char*) "BLACKTIE_SERVER_NAME", rcLog);
		ACE_OS::snprintf(RCLOGPATH, sizeof (RCLOGPATH), "%s", servName);
		AtmiBrokerEnv::discard_instance();
#else
		const char *rcLog   = DEF_LOG;
		const char *servName = rcLog;
		ACE_OS::snprintf(RCLOGPATH, sizeof (RCLOGPATH), "%s", servName);

#endif
	}

	LOG4CXX_TRACE(xarcllogger, (char *) "Using log file " << RCLOGPATH);
}

/*
 * Construct a recovery log for storing XID's and their associated OTS Recovery Records.
 * The log is backed by a file.
 */
XARecoveryLog::XARecoveryLog(const char* logfile) throw (RMException) :
	arena_(0), nblocks_((size_t) 0), maxblocks_((size_t) 0)
{
	init_logpath(logfile);

	if (!load_log(RCLOGPATH)) {
		LOG4CXX_ERROR(xarcllogger, (char *) "ERROR: creating recovery log");
	}
}

/**
 * free the arena and close the backing file
 */
XARecoveryLog::~XARecoveryLog()
{
	LOG4CXX_TRACE(xarcllogger, (char *) "destructor");
	if (arena_)
		free(arena_);

	if (log_.is_open())
		log_.close();
}
/**
 * Read a collection of recovery records from file and load them into memory
 */
bool XARecoveryLog::load_log(const char* logname)
{
	LOG4CXX_TRACE(xarcllogger, (char *) "Loading log file: " << logname);
#if 1
	AtmiBrokerEnv* env = AtmiBrokerEnv::get_instance();
	const char* maxblk = env->getenv("BLACKTIE_MAX_RCLOG_SIZE", MAXBLOCKS);
	AtmiBrokerEnv::discard_instance();
#else
	const char* maxblk = MAXBLOCKS;
#endif

	ios_base::openmode mode = ios::out | ios::in | ios::binary;

	// make sure the log file exists
	FILE *fp = fopen(logname, "a+");

	if (fp == NULL) {
		LOG4CXX_ERROR(xarcllogger, (char *) "ERROR: log open failed: " << errno);
		return false;
	}

	(void) fclose(fp);

	log_.open (logname, mode);

	if (!log_.is_open()) {
		LOG4CXX_ERROR(xarcllogger, (char *) "ERROR: log open failed for: " << logname);
		return false;
	}

	// calculate the number of bytes in the file
	size_t sz;
	fstream::pos_type s = log_.tellg();

	log_.seekg (0, ios::end);
	sz = (size_t) (log_.tellg() - s);

	// allocate enough space into the arena for sz bytes
	if ((maxblocks_ = strtol(maxblk, NULL, 0)) == 0) {
		LOG4CXX_TRACE(xarcllogger, (char *)
			"ERROR: the env variable BLACKTIE_MAX_RCLOG_SIZE is invalid: " <<
			(char *) maxblk);
	} else if (get_blocks(sz / BSIZE + 1, false) != NULL) {
		LOG4CXX_DEBUG(xarcllogger, (char *) "initialized recovery log size: " << (sz / BSIZE + 1));

		// read the bytes from the file into the arena
		log_.seekg(0, ios::beg);
		log_.read((char *) arena_, sz);

		return true;
	}

	log_.close();

	return false;
}

void XARecoveryLog::sync_rec(void* p, size_t sz) {
	fstream::pos_type pos = (fstream::pos_type) ((char *) p - (char *) arena_);

	log_.seekg(pos);
	log_.write((char *) p, sz);
	log_.sync();
}

rrec_t* XARecoveryLog::get_blocks(size_t nblocks, bool dosync) {
	size_t sz = 0;
	rrec_t* e = arena_ + nblocks_;
	rrec_t* p = arena_;

	if (p) {
		for ( ; p < e; ) {
			if (p->magic == INUSE) {
				sz = 0;
				p = NEXT_REC(p);
			} else if (++sz >= nblocks) {
				return p - sz + 1;
			} else {
				p += 1;
			}
		}
	}

	// more blocks are required
	if (nblocks < NBLOCKS)
		nblocks = NBLOCKS;

	size_t nsz = nblocks_ + nblocks;
	rrec_t* ar;

	LOG4CXX_DEBUG(xarcllogger, (char *) "alocating blocks: " << nsz);

	if (nsz > maxblocks_) {
		LOG4CXX_ERROR(xarcllogger, (char *)
			"ERROR: recovery log has grown beyond its configurable limit");
	} else if ((ar = (rrec_t*) calloc(BSIZE, nsz)) == 0) {
		LOG4CXX_ERROR(xarcllogger, (char *) "ERROR: recovery log: out of memory");
	} else {
		LOG4CXX_DEBUG(xarcllogger, (char *)
			"increasing rc log blocks from " << nblocks_ << " to " << nsz);

		if (arena_)
			memcpy(ar, arena_, nblocks_);

		if (dosync)
			sync_rec(ar + nblocks_, nblocks * BSIZE);

		p = ar + nblocks_;

		nblocks_ = nsz;

		if (arena_)
			free(arena_);

		arena_ = ar;

		LOG4CXX_DEBUG(xarcllogger, (char *) "ARENA: [" << arena_ << ", " <<  arena_ + (BSIZE * nsz));

		return arena_;
	}

	return NULL;
}

/**
 * Insert a recovery record into persistent storage.
 */
int XARecoveryLog::add_rec(XID& xid, char* ior) {
	size_t len;

	if (ior == NULL || (len = strlen(ior) + 1) == 1) {
		LOG4CXX_WARN(xarcllogger, (char *) "Attempt to create a log record with no value. Xid: " << xid);

		return -1;
	}

	size_t nblocks = len / BSIZE + 2;	// add 1 for header (and 1 to round up for ior))
	rrec_t* fb; // next free block of the correct size
	int rv = -1;

	LOG4CXX_DEBUG(xarcllogger, (char *) "Adding log record key=" << xid << " value=" << ior);

	lock();
	if ((fb = get_blocks(nblocks, true)) != NULL) {
		fb->xid = xid;
		fb->next = nblocks;
		fb->magic = INUSE;
		memcpy(fb + 1, ior, len);
		sync_rec(fb, nblocks * BSIZE);
		rv = 0;
	}
	unlock();

	return rv;
}

int XARecoveryLog::del_rec(XID& xid) {
	rrec_t* prev;
	rrec_t* next;
	int rv;

	LOG4CXX_DEBUG(xarcllogger, (char *) "Deleting log record key=" << xid);
	lock();

	if ((rv = find(xid, &prev, &next)) == 0) {
		next->magic = 0l;
		sync_rec(&(next->magic), sizeof (next->magic));

		// TODO if we fail here remember to fix up the offset next time the log is loaded
		prev->next = next->next;
		sync_rec(&(prev->next), sizeof (prev->next));
	}
	unlock();

	return rv;
}

/**
 * Locate the next record following the passed in record.
 * If from is NULL the first record is returned - thus
 * the returned record also serves as a handle for finding
 * the next record - including the case where the record is
 * deleted. Note that it will find records inserted after
 * the handle but not ones inserted before it.
 */
rrec_t* XARecoveryLog::find_next(rrec_t* from) {
	if (from == 0) {
		if (arena_->magic == INUSE)
			return arena_;
		from = arena_;
	} else {
		from = NEXT_REC(from);
	}

	rrec_t* e = arena_ + nblocks_;

	while (from < e && from->magic != INUSE)
		from += 1;

	return from < e ? from : NULL;
}

char* XARecoveryLog::find_ior(XID& xid) {
	rrec_t* prev;
	rrec_t* next;

	if (find(xid, &prev, &next) == 0)
		return IOR(next);

	return 0;

}

const char *XARecoveryLog::get_ior(rrec_t& rr) {
	return IOR(&rr);
}

int XARecoveryLog::find(XID& xid, rrec_t** prev, rrec_t** next) {
	*prev = *next = arena_;

	while (*next) {
//		printf("comparing %d with %d\n", xid.formatID, ((*next)->xid).formatID);
		if (compareXids(xid, (*next)->xid) == 0)
			return 0;

		*prev = *next;
		*next = find_next(*next);
	}

	return -1;
}

bool XARecoveryLog::lock() {
	return lock_.lock();
}

bool XARecoveryLog::unlock() {
	return lock_.unlock();
}

#if 0
int
main(int argc, char* argv[])
{
	int nrecs = 0;
	bool verbose = false;

	if (argc <= 2) {
		fprintf(stderr, "syntax %s <-i|-v> <recovery log file path>\n", argv[0]);
		return -1;
	}

	XARecoveryLog log(argv[2]);
	bool prompt = (strcmp(argv[1], "-i") == 0 ? true : false);

	if (strcmp(argv[1], "-a") == 0) {
		long fid;
		XID xid = {131072, 0, 26, "102:0:42:1317213340:392306"};
		const char *ior = "010000003400000049444c3a6f6d672e6f72672f436f735472616e73616374696f6e732f5265636f76657279436f6f7264696e61746f723a312e3000010000000000000064010000010102000a0000003132372e302e302e3100c80d860000004a426f73732f526376436f2d526563436f536572766963655f646576317265636f766572795f636f6f7264696e61746f722f37663030303030313a336564323a34653832666333633a3766302a37663030303030313a336564323a34653832666333633a3765632a37663030303030313a336564323a34653832666333633a302a66616c73650000050000000000000008000000000000004a414300010000001c00000000000000050100010000000105010001000101090000000105010001210000006c000000000000000000000100000000000000240000001c0000007e00000000000000010000000a3132372e302e302e31000dc900400000000000080606678102010101000000170401000806066781020101010000000764656661756c7400000000000000000000000000000000002000000004000000000000001f0000000400000000000003";

		nrecs = argc > 3 ? atoi(argv[3]) : 1;
		fid = argc > 4 ? atol(argv[4]) : 131072L;
		fprintf(stdout, "adding %d iors\n", nrecs);

		for (int i = 0; i < nrecs; i++, fid++) {
			xid.formatID = fid;
			if (log.add_rec(xid, (char*) ior) != 0)
				fprintf(stderr, "error adding ior number %d\n", i);
			else
				fprintf(stdout, "added ior %d\n", i);
		}
	} else if (strcmp(argv[1], "-d") == 0) {
		long fid = argc > 2 ? atol(argv[3]) : 131072L;
		XID xid = {fid, 0, 26, "102:0:42:1317213340:392306"};

		fprintf(stdout, "deleting xid %d\n", xid.formatID);
	   	if (log.del_rec(xid) != 0)
			fprintf(stderr, "xid %d not found\n", xid.formatID);
	} else if (strcmp(argv[1], "-f") == 0) {
		long fid = argc > 2 ? atol(argv[3]) : 131072L;
		XID xid = {fid, 0, 26, "102:0:42:1317213340:392306"};
		char* ior = log.find_ior(xid);
		fprintf(stdout, "%s\n", ior);
	} else if (strcmp(argv[1], "-z") == 0) {
		for (rrec_t* rrp = log.find_next(0); rrp; rrp = log.find_next(rrp))
			printf("xid: %d ior: %s\n", (rrp->xid).formatID, log.get_ior(*rrp));
	} else {
		verbose = true;
	}

	nrecs = 0;
	for (rrec_t* rr = log.find_next(0); rr; nrecs++, rr = log.find_next(rr)) {
		XID &xid = rr->xid;

		if (verbose) {
			fprintf(stdout, "XID=%ld:%ld:%ld:%s IOR=%s\n",
				xid.formatID, xid.gtrid_length, xid.bqual_length,
				(char *) (xid.data + xid.gtrid_length), IOR(rr));

			if (prompt) {
				char ans[64];

				fprintf(stdout, "Do you wish to delete this record [y/n]? ");

				if (fgets(ans, sizeof (ans), stdin) != NULL && ans[0] == 'y') {
					log.del_rec(rr->xid);
				}
			}
		}
	}

	fprintf(stdout, "%d records\n", nrecs);

	return 0;
}
#endif

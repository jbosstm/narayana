/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and others contributors as indicated
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
#ifndef _HTTP_CONTROL_H
#define _HTTP_CONTROL_H

#include <map>
#include "TxControl.h"
#include "HttpClient.h"

namespace atmibroker {
	namespace tx {

class BLACKTIE_TX_DLL HttpControl :public virtual TxControl {
public:
	HttpControl(long timeout, apr_os_thread_t tid);
	HttpControl(char* txn, long ttl, apr_os_thread_t tid);
	virtual ~HttpControl();

	int start(const char* txnMgrUrl);
    int rollback_only();
    int get_status();
	bool get_xid(XID& xid);

	const char* txnUrl() {return _txnUrl;}
	const char* enlistUrl() {return _enlistUrl;}

protected:
    void suspend();
    int do_end(bool commit, bool reportHeuristics);
    void* get_control();
	bool isActive(const char *msg, bool expect);

private:
	apr_pool_t *_pool;
	HttpClient _wc;
	char *_txnUrl;
	char *_endUrl;
	char *_enlistUrl;
	char *_xid;

	bool headRequest();
	int decode_headers(http_request_info *ri);
	int do_end(int how);

public:
	static int http_to_tx_status(const char *content);

	static const char * TXSTATUS;

	static const char ABORT_ONLY[];
	static const char ABORTING[];
	static const char ABORTED[];
	static const char COMMITTING[];
	static const char COMMITTED[];
	static const char COMMITTED_ONE_PHASE[];
	static const char H_ROLLBACK[];
	static const char H_COMMIT[];
	static const char H_HAZARD[];
	static const char H_MIXED[];
	static const char PREPARING[];
	static const char PREPARED[];
	static const char RUNNING[];
	static const char READONLY[];

	static const int ABORT_ONLY_STATUS;
	static const int ABORTING_STATUS;
	static const int ABORTED_STATUS;
	static const int COMMITTING_STATUS;
	static const int COMMITTED_STATUS;
	static const int COMMITTED_ONE_PHASE_STATUS;
	static const int H_ROLLBACK_STATUS;
	static const int H_COMMIT_STATUS;
	static const int H_HAZARD_STATUS;
	static const int H_MIXED_STATUS;
	static const int PREPARING_STATUS;
	static const int PREPARED_STATUS;
	static const int RUNNING_STATUS;
	static const int READONLY_STATUS;

	static const char * TX_STATUS_MEDIA_TYPE;
	static const char * POST_MEDIA_TYPE;
	static const char * PLAIN_MEDIA_TYPE;
	static const char * TX_LIST_MEDIA_TYPE;
	static const char * TX_STATUS_EXT_MEDIA_TYPE;

	static const char * TIMEOUT_PROPERTY;

	// HTTP Header names
	static const char * LOCATION;
	// Transaction links
	static const char * TXN_TERMINATOR;// transaction-terminator URI
	static const char * TXN_PARTICIPANT;// transaction-enlistment URI
	static const char * VOLATILE_PARTICIPANT;// transaction-enlistment URI
	// Transaction statistics
	static const char * TXN_STATISTICS;// transaction-statistics URI
	// Two phase aware participants
	static const char * PARTICIPANT_RESOURCE;// participant-resource URI
	static const char * PARTICIPANT_TERMINATOR;// participant-terminator URI
	// Two phase unaware participants
	static const char * PARTICIPANT_PREPARE;// participant-prepare URI
	static const char * PARTICIPANT_COMMIT;// participant-commit URI
	static const char * PARTICIPANT_ROLLBACK;// participant-rollback URI
	static const char * PARTICIPANT_COMMIT_ONE_PHASE;// participant-commit-one-phase URI
};
} //	namespace tx
} //namespace atmibroker
#endif	// _HTTP_CONTROL_H

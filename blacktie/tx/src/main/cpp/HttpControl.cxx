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
#include <string>
#include <string.h>
#include <stdlib.h>

#include <apr_strings.h>

#include "log4cxx/logger.h"
#include "ThreadLocalStorage.h"
#include "HttpControl.h"
#include "TxManager.h"
#include "Http.h"
#include "AtmiBrokerEnv.h"

#define TX_GUARD(msg, expect) { \
	FTRACE(httpclogger, "ENTER"); \
	if (!isActive(msg, expect)) {   \
		return TX_PROTOCOL_ERROR;   \
	}}

namespace atmibroker {
	namespace tx {

static long BT_XID_FORMAT = 131077;

const char * HttpControl::LOCATION = "location";

// Transaction links
const char * HttpControl::TXN_TERMINATOR = "terminator"; // transaction-terminator URI
const char * HttpControl::TXN_PARTICIPANT = "durable-participant"; // transaction-enlistment URI
const char * HttpControl::VOLATILE_PARTICIPANT = "volatile-participant"; // transaction-enlistment URI

const char * HttpControl::TXN_STATISTICS = "statistics"; // transaction-statistics URI

// Two phase aware participants
const char * HttpControl::PARTICIPANT_RESOURCE = "participant"; // participant-resource URI
const char * HttpControl::PARTICIPANT_TERMINATOR = "terminator"; // participant-terminator URI
// Two phase unaware participants
const char * HttpControl::PARTICIPANT_PREPARE = "prepare"; // participant-prepare URI
const char * HttpControl::PARTICIPANT_COMMIT = "commit"; // participant-commit URI
const char * HttpControl::PARTICIPANT_ROLLBACK = "rollback"; // participant-rollback URI
const char * HttpControl::PARTICIPANT_COMMIT_ONE_PHASE = "commit-one-phase"; // participant-commit-one-phase

const char * HttpControl::TX_STATUS_MEDIA_TYPE = "application/txstatus";
const char * HttpControl::POST_MEDIA_TYPE = "application/x-www-form-urlencoded";
const char * HttpControl::PLAIN_MEDIA_TYPE = "text/plain";
const char * HttpControl::TX_LIST_MEDIA_TYPE = "application/txlist";
const char * HttpControl::TX_STATUS_EXT_MEDIA_TYPE = "application/txstatusext+xml";

const char * HttpControl::TIMEOUT_PROPERTY = "timeout";

const char * HttpControl::TXSTATUS = "txstatus=";

const char HttpControl::ABORT_ONLY[] = "TransactionRollbackOnly";
const char HttpControl::ABORTING[] = "TransactionRollingBack";
const char HttpControl::ABORTED[] = "TransactionRolledBack";
const char HttpControl::COMMITTING[] = "TransactionCommitting";
const char HttpControl::COMMITTED[] = "TransactionCommitted";
const char HttpControl::COMMITTED_ONE_PHASE[] = "TransactionCommittedOnePhase";
const char HttpControl::H_ROLLBACK[] = "TransactionHeuristicRollback";
const char HttpControl::H_COMMIT[] = "TransactionHeuristicCommit";
const char HttpControl::H_HAZARD[] = "TransactionHeuristicHazard";
const char HttpControl::H_MIXED[] = "TransactionHeuristicMixed";
const char HttpControl::PREPARING[] = "TransactionPreparing";
const char HttpControl::PREPARED[] = "TransactionPrepared";
const char HttpControl::RUNNING[] = "TransactionActive";
const char HttpControl::READONLY[] = "TransactionReadOnly";

// what about TransactionCommitOnePhase

log4cxx::LoggerPtr httpclogger(log4cxx::Logger::getLogger("TxHttpControl"));

const int HttpControl::ABORT_ONLY_STATUS = 0;
const int HttpControl::ABORTING_STATUS = 1;
const int HttpControl::ABORTED_STATUS = 2;
const int HttpControl::COMMITTING_STATUS = 3;
const int HttpControl::COMMITTED_STATUS = 4;
const int HttpControl::COMMITTED_ONE_PHASE_STATUS = 5;
const int HttpControl::H_ROLLBACK_STATUS = 6;
const int HttpControl::H_COMMIT_STATUS = 7;
const int HttpControl::H_HAZARD_STATUS = 8;
const int HttpControl::H_MIXED_STATUS = 9;
const int HttpControl::PREPARING_STATUS = 10;
const int HttpControl::PREPARED_STATUS = 11;
const int HttpControl::RUNNING_STATUS = 12;
const int HttpControl::READONLY_STATUS = 13;

static const char * DUR_PARTICIPANT = "rel=\"durable-participant";
static const char * TERMINATOR = "rel=\"terminator";

typedef std::map<std::string, int> StatusMap;

const StatusMap::value_type status_entries[] = {
	StatusMap::value_type(HttpControl::ABORT_ONLY, HttpControl::ABORT_ONLY_STATUS),
	StatusMap::value_type(HttpControl::ABORTING, HttpControl::ABORTING_STATUS),
	StatusMap::value_type(HttpControl::ABORTED, HttpControl::ABORTED_STATUS),
	StatusMap::value_type(HttpControl::COMMITTING, HttpControl::COMMITTING_STATUS),
	StatusMap::value_type(HttpControl::COMMITTED, HttpControl::COMMITTED_STATUS),
	StatusMap::value_type(HttpControl::COMMITTED_ONE_PHASE, HttpControl::COMMITTED_ONE_PHASE_STATUS),
	StatusMap::value_type(HttpControl::H_ROLLBACK, HttpControl::H_ROLLBACK_STATUS),
	StatusMap::value_type(HttpControl::H_COMMIT, HttpControl::H_COMMIT_STATUS),
	StatusMap::value_type(HttpControl::H_HAZARD, HttpControl::H_HAZARD_STATUS),
	StatusMap::value_type(HttpControl::H_MIXED, HttpControl::H_MIXED_STATUS),
	StatusMap::value_type(HttpControl::PREPARING, HttpControl::PREPARING_STATUS),
	StatusMap::value_type(HttpControl::PREPARED, HttpControl::PREPARED_STATUS),
	StatusMap::value_type(HttpControl::RUNNING, HttpControl::RUNNING_STATUS),
	StatusMap::value_type(HttpControl::READONLY, HttpControl::READONLY_STATUS),
};

static const int nelems = sizeof status_entries / sizeof status_entries[0];
static StatusMap STATUS_CODES(status_entries, status_entries + nelems);

// find the last path component of URL
static char *last_path(char *url) {
	// strip off any trailing /'s
	char* p = url + strlen(url);

	while((*--p) == '/');
		*(p + 1) = 0;

	// find the last occurence of /
	p = strrchr(url,'/');

	if(p != NULL)
		p++;	// the last path is just after the /

	return p;
}

HttpControl::HttpControl(long timeout, apr_os_thread_t tid) : TxControl(timeout, tid),
	_txnUrl(NULL), _endUrl(NULL), _enlistUrl(NULL), _xid(NULL)
{
	FTRACE(httpclogger, "ENTER new HTTPTXCONTROL: " << this);

	int rc = apr_pool_create(&_pool, NULL);
	if (rc != APR_SUCCESS) {
		LOG4CXX_WARN(httpclogger, "can not create pool");
		apr_pool_destroy(_pool);
		throw new std::exception();
	}
}

HttpControl::HttpControl(char* txn, long ttl, apr_os_thread_t tid) : TxControl(ttl, tid),
	_txnUrl(NULL), _endUrl(NULL), _enlistUrl(NULL)
{
	FTRACE(httpclogger, "ENTER reassociate HTTPTXCONTROL: " << this << " txn=" << txn);
	_txnUrl = strdup(txn);
	_xid = last_path(_txnUrl);
	LOG4CXX_TRACE(httpclogger, "_txnUrl=" << _txnUrl);

	int rc = apr_pool_create(&_pool, NULL);
	if (rc != APR_SUCCESS) {
		LOG4CXX_WARN(httpclogger, "can not create pool");
		apr_pool_destroy(_pool);
		throw new std::exception();
	}
}

HttpControl::~HttpControl()
{
	FTRACE(httpclogger, "ENTER delete HTTPTXCONTROL: " << this);
	suspend();
	LOG4CXX_TRACE(httpclogger, "freeing _txnUrl=" << _txnUrl);
	if (_pool) apr_pool_destroy(_pool); 
	if (_txnUrl) free (_txnUrl);
	if (_endUrl) free (_endUrl);
	if (_enlistUrl) free (_enlistUrl);
}

int HttpControl::decode_headers(http_request_info *ri) {
	for (int i = 0; i < ri->num_headers; i++) {
		char *n = ri->http_headers[i].name;
		char *v = ri->http_headers[i].value;

		LOG4CXX_DEBUG(httpclogger, "check header: " << n << " = " << v);
		if (strcmp(n, "Location") == 0) {
			LOG4CXX_TRACE(httpclogger, "_txnUrl=" << _txnUrl);
			if (_txnUrl != NULL)
				free (_txnUrl);
			_txnUrl = strdup(v);
			LOG4CXX_TRACE(httpclogger, "_txnUrl=" << _txnUrl);
			_xid = last_path(_txnUrl);
		} else if (strcmp(n, "Link") == 0) {
			char *ep = strchr(v, ';');

			LOG4CXX_DEBUG(httpclogger, "check header: check link: " << ep);
			if (ep != 0 && ++v < --ep) {
				LOG4CXX_DEBUG(httpclogger, "check header: cmp " << v
					<< " with " << DUR_PARTICIPANT);
				if (strstr(v, DUR_PARTICIPANT) != 0)
					_enlistUrl = strndup(v, ep - v);
				else if (strstr(v, TERMINATOR) != 0)
					_endUrl = strndup(v, ep - v);
			}
		}
	}

	LOG4CXX_DEBUG(httpclogger, "URLS: Loc: " << _txnUrl << "\nEnlist: " << _enlistUrl << "\nEND: " << _endUrl);

	return _txnUrl != 0 && _endUrl != 0 && _enlistUrl != 0 ? TX_OK : TX_ERROR;
}

int HttpControl::start(const char* txnMgrUrl) {
	FTRACE(httpclogger, "ENTER");
	char content[32];
	http_request_info ri;
	(void) sprintf(content, "%s=%ld", TIMEOUT_PROPERTY, _ttl * 1000);

	if (_wc.send(_pool, &ri, "POST", txnMgrUrl, POST_MEDIA_TYPE, NULL, content, strlen(content),
		NULL, NULL) != 0) {
		LOG4CXX_DEBUG(httpclogger, "transaction POST failure");
		return -1;
	}

	return decode_headers(&ri);
}

bool HttpControl::headRequest()
{
	FTRACE(httpclogger, "ENTER");
	http_request_info ri;

	if (_txnUrl == NULL)
		return false;

	if (_wc.send(_pool, &ri, "HEAD", _txnUrl, TX_STATUS_MEDIA_TYPE, NULL, NULL, 0, NULL, NULL) != 0) {
		LOG4CXX_DEBUG(httpclogger, "HTTP HEAD error");
		return false;
	}

	return (decode_headers(&ri) == TX_OK);
}

int HttpControl::http_to_tx_status(const char *content) {
	int stat = (content == NULL ? -1 : STATUS_CODES[content]);

	LOG4CXX_DEBUG(httpclogger, "http_to_tx_status " << content << " -> " << stat);

	return stat;
}

int HttpControl::do_end(bool commit, bool reportHeuristics)
{
	return do_end(commit ? 0 : 1);
}

int HttpControl::do_end(int how)
{
	TX_GUARD("end", true);
	http_request_info ri;
	char * resp;
	char content[64];
	char *p = &content[0];
	size_t nread;
	size_t prefixlen = strlen(HttpControl::TXSTATUS);
	int rc = TX_FAIL;
	const char *status;

	if (_endUrl == NULL && !headRequest())
		return TX_FAIL;

	switch (how) {
		case 0:
			status = HttpControl::COMMITTED;
			break;
		case 1:
			status = HttpControl::ABORTED;
			break;
		default:
			status = HttpControl::ABORT_ONLY;
			break;
	}

	(void) apr_snprintf(p, sizeof (content), "%s%s", TXSTATUS, status);
	if (_wc.send(_pool, &ri, "PUT", _endUrl, TX_STATUS_MEDIA_TYPE, NULL, p, strlen(p), &resp, &nread) != 0) {
		LOG4CXX_DEBUG(httpclogger, "do_end: HTTP PUT error");

		return TX_FAIL;
	}

	LOG4CXX_DEBUG(httpclogger, "do_end: HTTP status: " << ri.status_code << " nread: " << nread);
	if(resp) {
		LOG4CXX_DEBUG(httpclogger, "do_end: HTTP resp: " << resp);
	} else {
		LOG4CXX_DEBUG(httpclogger, "do_end: HTTP resp content is empty");
	}

	if (ri.status_code == 404) {
		rc = TX_ROLLBACK;
	} else if (ri.status_code >= 400 && ri.status_code < 500 && ri.status_code != 412) {
		rc = TX_PROTOCOL_ERROR;
	} else if (ri.status_code >= 500) {
		rc = TX_FAIL;
	} else if (resp && nread > prefixlen) {
		//*(resp + nread) = '\0';
		rc = http_to_tx_status(resp + prefixlen);

		if (how == 0) {
			switch (rc) {
			case ABORT_ONLY_STATUS:
			case ABORTING_STATUS:
			case ABORTED_STATUS:
				rc = TX_ROLLBACK; break;
			case H_ROLLBACK_STATUS:
				rc = TX_ROLLBACK; break;
			case COMMITTED_STATUS:
				rc = TX_OK; break;
			case COMMITTING_STATUS:
				rc = TX_OK; break;	// see COMMIT RETURN
			case H_COMMIT_STATUS:
				rc = TX_HAZARD; break;
			case H_HAZARD_STATUS:
				rc = TX_HAZARD; break;
			case H_MIXED_STATUS:
				rc = TX_MIXED; break;
			case PREPARING_STATUS:
			case PREPARED_STATUS:
			case RUNNING_STATUS:
			case READONLY_STATUS:
				rc = TX_PROTOCOL_ERROR; break;
			default:
				rc = TX_FAIL; break;
			}
		} else if (how == 1) {
			switch (rc) {
			case ABORT_ONLY_STATUS:
			case ABORTING_STATUS:
			case ABORTED_STATUS:
			case H_ROLLBACK_STATUS:
				rc = TX_OK; break;
			case COMMITTING_STATUS:
			case COMMITTED_STATUS:
				rc = TX_COMMITTED; break;
			case H_COMMIT_STATUS:
				rc = TX_HAZARD; break;
			case H_HAZARD_STATUS:
				rc = TX_HAZARD; break;
			case H_MIXED_STATUS:
				rc = TX_MIXED; break;
			case PREPARING_STATUS:
			case PREPARED_STATUS:
			case RUNNING_STATUS:
			case READONLY_STATUS:
				rc = TX_FAIL; break;
			default:
				rc = TX_FAIL; break;
			}
		} else {
			rc = TX_OK;
		}
	}

	if (resp)
		free(resp);

	_wc.dispose(&ri);

	return rc;
}

int HttpControl::rollback_only() {
	FTRACE(httpclogger, "ENTER");

	_rbonly = true;

	return do_end(2);
}

int HttpControl::get_status()
{
	FTRACE(httpclogger, "ENTER");

	if (_rbonly)
		return TX_ROLLBACK_ONLY;

	http_request_info ri;
	if (_wc.send(_pool, &ri, "GET", _txnUrl, TX_STATUS_MEDIA_TYPE, NULL, NULL, 0, NULL, NULL) != 0) {
		LOG4CXX_DEBUG(httpclogger, "HTTP GET status error");

		return TX_ERROR;
	}

	LOG4CXX_DEBUG(httpclogger, "status: " << ri.status_code); // << "Txn Status: " << resp);

	_wc.dispose(&ri);

	return TX_ACTIVE;
}

void* HttpControl::get_control()
{
	LOG4CXX_TRACE(httpclogger, "_txnUrl=" << _txnUrl);
	return strdup(_txnUrl);
}

bool HttpControl::get_xid(XID& xid) {
	memset(&xid, 0, sizeof (XID));
	memset(xid.data, 0, XIDDATASIZE);
	xid.formatID = BT_XID_FORMAT;
	xid.bqual_length = 0;

#if 1
	// WARNING THIS CODE hardcodes knowledge of the REST txn URL by assuming
	// the final path component is the string form of the global txn uid
	// The assumption is safer than assuming that the full url fits in an XID
    char* p = strrchr(_txnUrl, '/');

    if (p != NULL && ++p != '\0' && (xid.gtrid_length = strlen(p)) < XIDDATASIZE) {
		memcpy(xid.data, p, xid.gtrid_length);
	} else {
		LOG4CXX_WARN(httpclogger, (char*) "Transaction URL does not terminate with an uid: " << _txnUrl);
		return false;
	}
#else
	size_t len;

	if (_txnUrl == NULL || (len = strlen(_txnUrl)) > XIDDATASIZE) {
		LOG4CXX_WARN(httpclogger, (char*) "TODO get_xid: txn URL won't fit in an XID: " << _txnUrl);
		return false;
	}

	xid.gtrid_length = len;

	memcpy(xid.data, _txnUrl, len);
#endif

	return true;
}

/**
 * release the control and remove the tx to thread association
 */
void HttpControl::suspend()
{
	FTRACE(httpclogger, "ENTER");

	destroySpecific(TSS_KEY);
}

bool HttpControl::isActive(const char *msg, bool expect)
{
	FTRACE(httpclogger, "ENTER");

    bool c = (_txnUrl != NULL);

    if (c != expect && msg) {
        LOG4CXX_WARN(httpclogger, (char*) "protocol violation: " << msg);
    }

    return c;
}

} //	namespace tx
} //namespace atmibroker

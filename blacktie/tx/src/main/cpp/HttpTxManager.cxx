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
#include "HttpTxManager.h"
#include "HttpControl.h"
#include "AtmiBrokerEnv.h"

#include "apr_thread_proc.h"
#include "apr_portable.h"
#include "apr_strings.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

namespace atmibroker {
	namespace tx {

#define BUFSZ	1024
static const char* HTTP_400 = (char *) "Bad Request";
static const char* HTTP_409 = (char *) "Conflict";
static const char* HTTP_412 = (char *) "Precondition Failed";

static const char XIDPAT[] = "/xid/";
static const char ENDPAT[] = "/terminate";

log4cxx::LoggerPtr httptxlogger(log4cxx::Logger::getLogger("TxHttpTxManager"));

static char * parse_wid(HttpClient &wc, const char *s) {
	const char *b = strstr(s, XIDPAT);
	const char *e = strstr(s, ENDPAT);
	char *wid = 0;

	if (b && e && (b += sizeof (XIDPAT) - 1) < e) {
		wid = strndup(b, e - b);
	}

	return wid;
}

HttpTxManager::HttpTxManager(const char *txmUrl, const char *resUrl) :
	TxManager() {
	FTRACE(httptxlogger, "ENTER HttpTxManager constructor");
	_txmUrl = strdup(txmUrl);
	_resUrl = strdup(resUrl);
	_ws = NULL;
	int rc = apr_pool_create(&_pool, NULL);
	if (rc != APR_SUCCESS) {
		LOG4CXX_WARN(httptxlogger, "can not create pool");
		apr_pool_destroy(_pool);
		throw new std::exception();
	}
	FTRACE(httptxlogger, "ENTER inst create " << this);
}

HttpTxManager::~HttpTxManager() {
	FTRACE(httptxlogger, "ENTER inst destroy " << this);
	if (_ws != NULL) {
		LOG4CXX_WARN(httptxlogger, "Close has not been called");
		do_close();
	}

	if(_pool != NULL)
		apr_pool_destroy(_pool);
	if (_txmUrl != NULL)
		free(_txmUrl);
	if (_resUrl != NULL)
		free(_resUrl);
}

TxManager* HttpTxManager::create(const char *txmUrl, const char *resUrl) {
	if (_instance == NULL)
		_instance = new HttpTxManager(txmUrl, resUrl);

	return _instance;
}

int HttpTxManager::associate_transaction(char* txn, long ttl) {
	FTRACE(httptxlogger, "ENTER" << txn);

	TxControl *tx = new HttpControl(txn, ttl, apr_os_thread_current());
	int rc = tx_resume(tx, TMJOIN);

	if (rc != XA_OK)
		delete tx;

	return rc;
}

// return the transaction URL
char *HttpTxManager::get_current(long* ttl) {
	return (char *) get_control(ttl);
}

void HttpTxManager::release_control(void *ctrl) {
	if (ctrl != NULL)
		free(ctrl);
}

TxControl* HttpTxManager::create_tx(long timeout) {
	HttpControl* tx = new HttpControl(timeout, apr_os_thread_current());

	if (tx->start(_txmUrl) != TX_OK) {
		LOG4CXX_INFO(httptxlogger, "Unable to start a new txn on URI ");
		delete tx;
		return NULL;
	}

	return tx;
}

char *HttpTxManager::enlist(XAWrapper* resource, TxControl *tx, const char * xid) {
	if (guard(_isOpen) != TX_OK)
		return NULL;

	char *recUrl;
	HttpControl *httpTx = dynamic_cast<HttpControl*>(tx);
	const char *enlistUrl = httpTx->enlistUrl();	

	if (enlistUrl != NULL) {
		const char *host = _ws->get_host();
		int port = _ws->get_port();

    	char hdr[BUFSZ];
    	char body[BUFSZ];
	char *hdrp[] = {hdr, 0};
    	const char *fmt = "Link: <http://%s:%d/xid/%s/terminate>;rel=\"%s\",<http://%s:%d/xid/%s/status>;rel=\"%s\"";
    	http_request_info ri;

		(void) apr_snprintf(hdr, sizeof (hdr), fmt, host, port, xid,
			HttpControl::PARTICIPANT_TERMINATOR,
			host, port, xid,
			HttpControl::PARTICIPANT_RESOURCE);

		// TODO HACK latest wildfly no longer parses the Link header
		// correctly (when JBTM-1920 is resolved remove the hack)
		(void) apr_snprintf(body, BUFSZ, "%s", hdr);

                if (_wc.send(_pool, &ri, "POST", enlistUrl,
                        HttpControl::POST_MEDIA_TYPE,
                        (const char **)hdrp,
                        (const char *)body, strlen(body), NULL, NULL) != 0) {
                        LOG4CXX_DEBUG(httptxlogger, "enlist POST error");
                        return NULL;
                }

		const char *rurl = _wc.get_header(&ri, HttpControl::LOCATION);

		recUrl = (rurl == NULL ? NULL : strdup(rurl));

//		for (int i = 0; i < ri.num_headers; i++)
//			LOG4CXX_DEBUG(httptxlogger, "Header: " << ri.http_headers[i].name << "=" << ri.http_headers[i].value);
	
		_wc.dispose(&ri);

		LOG4CXX_DEBUG(httptxlogger, "Enlisted with header: " << hdr);
	}

	if (recUrl != NULL) {
		LOG4CXX_DEBUG(httptxlogger, "Enlisted branch " << resource->get_name() << " rec url: "
			<< recUrl);
		_branches[resource->get_name()] = resource;
	} else {
		LOG4CXX_WARN(httptxlogger, "Enlistment of xid " << xid << " failed - missing recovery url");
	}

	return recUrl;
}

bool HttpTxManager::recover(XAWrapper *resource)
{
	/*
	 * Tell the TM to complete this participant resource on the current endpoint:
	 */
	const char *host = _ws->get_host();
	int port = _ws->get_port();
	const char* xid = resource->get_name();
    	char hdr[BUFSZ];
	char *hdrp[] = {hdr, 0};
    	const char *fmt = "Link: <http://%s:%d/xid/%s/terminate>;rel=\"%s\",<http://%s:%d/xid/%s/status>;rel=\"%s\"";
   	http_request_info ri;

	(void) apr_snprintf(hdr, sizeof (hdr), fmt, host, port, xid,
		HttpControl::PARTICIPANT_TERMINATOR,
		host, port, xid,
		HttpControl::PARTICIPANT_RESOURCE);

	if (_wc.send(_pool, &ri, "PUT", resource->get_recovery_coordinator(),
		HttpControl::PLAIN_MEDIA_TYPE, (const char **)hdrp, NULL, 0, NULL, NULL) != 0) {
		LOG4CXX_INFO(httptxlogger, "recovery PUT error");
		return false;
	}

	_wc.dispose(&ri);

	if (ri.status_code == 200) {
		LOG4CXX_DEBUG(httptxlogger, "recovery: told TM about participant "
			<< resource->get_name() << " xid: " << xid);

		return true;
	} else {
		// TODO what about the other HTTP codes
		// For some codes we need to periodically retry the request
		LOG4CXX_WARN(httptxlogger, "TM failed recovery request: " << ri.status_code <<
			" for participant " << xid);
		_branches[resource->get_name()] = resource;
	}

	return false;
}

static apr_thread_t     *thread;
static apr_threadattr_t *thd_attr;

static void* APR_THREAD_FUNC run_server(apr_thread_t *thd, void *data) {
	HttpServer* server = (HttpServer*)data;
	server->run();
	return NULL;
}

int HttpTxManager::do_open(void) {
	char host[1025];
	int port;

	(void) _wc.parse_url(_resUrl, host, &port);

	FTRACE(httptxlogger, "ENTER: opening HTTP server: host: " <<
		host << " port: " << port << " handler: " << this);
	_ws = new HttpServer(host, port, _pool);
	_ws->add_request_handler(this);

	apr_threadattr_create(&thd_attr, _pool);
	apr_thread_create(&thread, thd_attr, run_server, (void*)_ws, _pool);

	if (_ws->wait_for_server_startup() == false) {
		LOG4CXX_WARN(httptxlogger, "http sever has not start");
	}

	return TX_OK;
}

int HttpTxManager::do_close(void) {
	if (_ws != NULL) {
		FTRACE(httptxlogger, "ENTER: closing HTTP server: handler " << this);
		//_ws->remove(this);
		_ws->shutdown();
		apr_status_t rv;
		if(thread) apr_thread_join(&rv, thread);
		delete _ws;
		_ws = NULL;
	}

	return TX_OK;
}

XAWrapper * HttpTxManager::locate_branch(const char * xid)
{
	LOG4CXX_DEBUG(httptxlogger, "locating branch " << xid);

	for (XABranchMap::iterator iter = _branches.begin(); iter != _branches.end(); ++iter) {
		if ((*iter).first == xid) {
			return (*iter).second;
		}
	}

	return NULL;
}

static int http_printf(HttpClient &wc, http_conn_ctx *conn, const char *fmt, ...) {
	char buf[BUFSZ];
	int len;
	va_list ap;

	va_start(ap, fmt);
	len = apr_vsnprintf(buf, sizeof(buf), fmt, ap);
	va_end(ap);

	return wc.write(conn, buf, (size_t)len);
}

bool HttpTxManager::handle_request(
	http_conn_ctx *conn, const http_request_info *ri, const char *content, size_t len)
{
	int code = 400;
	const char* codestr = HTTP_400;
	const char *status = "";
	bool result = true;
	size_t plen = strlen(HttpControl::TXSTATUS);

	for (int i = 0; i < ri->num_headers; i++)
		LOG4CXX_DEBUG(httptxlogger, "\t" << ri->http_headers[i].name <<
			"=" << ri->http_headers[i].value);

	if (plen < len)
		content += plen;

	int stat = HttpControl::http_to_tx_status(content);

	if (stat != -1) {
		char* wid = parse_wid(_wc, ri->uri);
		LOG4CXX_DEBUG(httptxlogger, "looking up branch " << wid);
		XAWrapper* branch = locate_branch(wid);

		if (wid != NULL)
			free(wid);

		LOG4CXX_DEBUG(httptxlogger, "branch: 0x" << branch << " content: " << content);

		if (branch) {
			// ? TODO should this logic be moved into a subclass of XAWrapper
			// TODO check that all possible XA codes are being mapped correctly
			int res = XAER_INVAL;
			int how = 0;

			if (stat == HttpControl::PREPARED_STATUS) {
				res = branch->do_prepare();
				switch (res) {
				case XA_OK:
					code = 200;
					status = HttpControl::PREPARED;
					break;
				case XA_RDONLY:
					code = 200;
					status = HttpControl::READONLY;
					break;
				case XAER_PROTO:
					code = 412;
					codestr = HTTP_412;
					status = HttpControl::ABORTED;
					break;
				default:
					status = HttpControl::ABORTED;
					code = 409;
					codestr = HTTP_409;
					break;
				}
			} else if (stat == HttpControl::COMMITTED_STATUS) {
				res = branch->do_commit();
				how = 1;
				status = HttpControl::COMMITTED;
			} else if (stat == HttpControl::COMMITTED_ONE_PHASE_STATUS) {
				res = branch->do_commit_one_phase();
				how = 1;
				status = HttpControl::COMMITTED;
			} else if (stat == HttpControl::ABORTED_STATUS) {
				how = 2;
				res = branch->do_rollback();
				status = HttpControl::ABORTED;
			} else {
				LOG4CXX_DEBUG(httptxlogger, "invalid request: " << stat);
				code = 400;
				codestr = HTTP_400;
				status = "";
			}

			// If the operation fails, e.g., the participant cannot be prepared, then the implementation MUST
			// return 409. It is implementation dependant as to whether the participant-resource or related URIs
			// remain valid, i.e., an implementation MAY delete the resource as a result of a failure. Depending
			// upon the point in the two-phase commit protocol where such a failure occurs the transaction
			// MUST be rolled back. If the participant is not in the correct state for the requested operation,
			// eg Prepare when it has been already been prepared, then the implementation MUST return 412.
			// If the transaction coordinator receives any response other than 200 for Prepare then the
			// transaction MUST rollback.

			if (how != 0) {
				switch (res) {
				default:
					LOG4CXX_DEBUG(httptxlogger, "Return 200 OK with body: " <<
						status << " XA status: " << res);
					code = 200;
					break;
				case XAER_PROTO:
					// TODO Do all PROTO errors mean rollback for example (at least with ORACLE)
					// committing without preparing generates XAER_PROTO and rolls back the branch
					status = HttpControl::ABORTED;
					code = 412;
					codestr = HTTP_412;
					break;
				case XA_HEURHAZ:
					status = HttpControl::H_HAZARD;
					code = 409;
					codestr = HTTP_409;
					break;	
				case XA_HEURCOM:
					//status = HttpControl::H_COMMIT;
					status = (how == 0 ? HttpControl::COMMITTED : HttpControl::H_ROLLBACK);
					code = 409;
					codestr = HTTP_409;
					break;
				case XA_HEURRB:
				case XA_RBROLLBACK: // these codes may be returned only if the TMONEPHASE flag was set
				case XA_RBCOMMFAIL:
				case XA_RBDEADLOCK:
				case XA_RBINTEGRITY:
				case XA_RBOTHER:
				case XA_RBPROTO:
				case XA_RBTIMEOUT:
				case XA_RBTRANSIENT:
					//status = HttpControl::H_ROLLBACK;
					status = (how == 0 ? HttpControl::H_ROLLBACK : HttpControl::ABORTED);
					code = 409;
					codestr = HTTP_409;
					break;
				case XA_HEURMIX:
					status = HttpControl::H_MIXED;
					code = 409;
					codestr = HTTP_409;
					break;
				case -999:
					LOG4CXX_INFO(httptxlogger, "closing connection to TM");
					//_wc.close_connection(conn);
					result = false; 
					status = "GARBAGE";
					codestr = HTTP_409;
					code = res;
					break;
				case XAER_INVAL:
					status = "";
					code = 400;
					codestr = HTTP_400;
					break;
				}

				LOG4CXX_DEBUG(httptxlogger, "completion request " << content <<
					" XA status: " << res << " RTS status: " << status << " http status: " << code);
			}
		}
	} else {
		LOG4CXX_DEBUG(httptxlogger, "either no content (" << content << ") or prefix (" <<
			HttpControl::TXSTATUS << ") does not match");
	}

	LOG4CXX_DEBUG(httptxlogger, "completion request returning " <<
		" RTS status: " << status << " http status: " << code);

	if (code == -999) {
		LOG4CXX_DEBUG(httptxlogger, "skipping response");
	} else if (code == 200 || code == 409) {
		http_printf(_wc, conn, "HTTP/1.1 %d %s\r\n"
			"Content-Length: %d\r\n"
			"Content-Type: application/txstatus\r\n\r\n"
			"%s%s",
			200, "OK", strlen(status), HttpControl::TXSTATUS, status);
	} else {
		http_printf(_wc, conn, "HTTP/1.1 %d %s\r\n"
			"Content-Length: %d\r\n"
			"Content-Type: application/txstatus\r\n\r\n"
//			"Connection: %s\r\n\r\n"	// "keep-alive" or "close"
			"%s%s",
			code, codestr, strlen(status), HttpControl::TXSTATUS, status);
	}

	LOG4CXX_DEBUG(httptxlogger, "handler_request return " << result);
	return result;
}

}
}

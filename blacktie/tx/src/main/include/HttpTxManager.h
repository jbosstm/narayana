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

#ifndef _HTTPTXMANAGER_H
#define _HTTPTXMANAGER_H

#include "TxManager.h"
#include "HttpServer.h"
#include <map>
#include "xa.h"

#include "XAWrapper.h"
#include "HttpClient.h"
#include "HttpRequestHandler.h"


namespace atmibroker {
	namespace tx {

class BLACKTIE_TX_DLL HttpTxManager: public virtual TxManager, public virtual HttpRequestHandler {
public:
	static TxManager* create(const char *txmUrl, const char *resUrl);

	int associate_transaction(char* txn, long ttl);
	char *enlist(XAWrapper* xaw, TxControl *tx, const char * xid);
	bool handle_request(http_conn_ctx *conn, const http_request_info *ri,
		const char *content, size_t len);
	bool recover(XAWrapper*);

protected:
	HttpTxManager(const char *txmUrl, const char *resUrl);
	virtual ~HttpTxManager();

	char *get_current(long* ttl);
	void release_control(void *);
	TxControl* create_tx(TRANSACTION_TIMEOUT timeout);
	int do_open(void);
	int do_close(void);

private:
    typedef std::map<std::string, XAWrapper*> XABranchMap;
    XABranchMap _branches;
	apr_pool_t *_pool;
	HttpServer *_ws;
	HttpClient _wc;

    XAWrapper * locate_branch(const char * xid);

	char * _txmUrl;
	char * _resUrl;
};
}	//	namespace tx
}	//namespace atmibroker

#endif // _HTTPTXMANAGER_H

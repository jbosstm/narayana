#ifndef _XABRANCHNOTIFICATION_H
#define _XABRANCHNOTIFICATION_H

#include "XAWrapper.h"

class XAWrapper;

class XABranchNotification {
public:
	virtual ~XABranchNotification() {};

	virtual void set_complete(XAWrapper*) = 0;
	virtual void notify_error(XAWrapper*, int, bool) = 0;
};
#endif // _XABRANCHNOTIFICATION_H

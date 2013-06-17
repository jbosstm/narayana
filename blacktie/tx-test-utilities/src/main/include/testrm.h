#ifndef _TESTRO_H
#define _TESTRO_H

#include "utilitiesMacro.h"
#include "xa.h"

struct xid_array {
    int count;
    int cursor;
    XID xids[10];
};

enum XA_OP {
        O_XA_NONE,
        O_XA_OPEN,
        O_XA_CLOSE,
        O_XA_START,
        O_XA_END,
        O_XA_ROLLBACK,
        O_XA_PREPARE,
        O_XA_COMMIT,
        O_XA_RECOVER,
        O_XA_FORGET,
        O_XA_COMPLETE
};

enum X_FAULT {
        F_NONE,
        F_CB,	// callback back into code supplied in fault_t arg
        F_HALT,	// generate a fatal error (that causes the process to terminate)
        F_DELAY,	// sleep for a given period
        F_ADD_XIDS	// used to tell the RM to simulate active XIDs
};

/*
 * a definition of a fault in the XA protocol for testing purposes
 */
typedef struct UTILITIES_DLL fault {
    bool operator==(const struct fault& rhs) const {
        return (id == rhs.id);
    }
    bool operator!=(const struct fault& rhs) const {
        return !operator==(rhs);
    }

        int id;                 // unique id for this fault specification
        int rmid;               // RM id
        enum XA_OP op;          // the XA method that this fault applies to
        int rc;                 // the value that the XA method should return
        enum X_FAULT xf;        // optional extra processing
        void *arg;              // optional arg for enum X_FAULT
        int res;                // result field that the RM can use to pass back a status to the caller
        int res2;               // another result field that the RM can use to pass back a status to the caller

		/* fields private to the RM */
        struct xid_array rmstate;// state maintained by the dummy RM
        struct fault *orig;	    // a pointer to the original fault specification
} fault_t;

#ifdef __cplusplus
extern "C" {
#endif
extern UTILITIES_DLL int dummy_rm_add_fault(fault_t&);
extern UTILITIES_DLL int dummy_rm_del_fault(fault_t&);
extern UTILITIES_DLL void dummy_rm_dump();
extern UTILITIES_DLL struct xa_switch_t testxasw;
#ifdef __cplusplus
}
#endif

#endif /* _TESTRO_H */


XAResource xares = r1.getXAResource();

xares.start(xid1); // associate xid1 to the connection

..
xares.end(xid1); // disassociate xid1 to the connection
..
xares.start(xid2); // associate xid2 to the connection
..
// While the connection is associated with xid2,
// the TM starts the commit process for xid1
status = xares.prepare(xid1);
..
xares.commit(xid1, false);      

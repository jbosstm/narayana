res = setlock(new Lock(WRITE), 10); // Will attempt to set a
                  // write lock 11 times (10
                  // retries) on the object
                  // before giving up.
res = setlock(new Lock(READ), 0);      // Will attempt to set a read
                  // lock 1 time (no retries) on
                  // the object before giving up.
res = setlock(new Lock(WRITE);      // Will attempt to set a write
                  // lock 101 times (default of
                  // 100 retries) on the object
                  // before giving up.

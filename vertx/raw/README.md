TransactionalVert.x
===================

This example is NOT for Vert.x but illustrates some of the lock contention issues that can
happen in a raw TXOJ example which is similar to Vert.x.

Error such as:

May 20, 2014 3:53:40 PM com.arjuna.ats.arjuna.StateManager activate
WARN: ARJUNA012035: Activate of object with id = 0:ffffac118252:dde3:537b57b0:0 and type /StateManager/LockManager/AtomicObject unexpectedly failed
May 20, 2014 3:53:40 PM com.arjuna.ats.txoj.LockManager setlock
WARN: ARJUNA015034: LockManager::setlock() cannot activate object
0:ffffac118252:e0e0:537b6c6e:6: AtomicObject exception raised: TestException: Read lock error.
May 20, 2014 3:53:40 PM com.arjuna.ats.arjuna.StateManager activate
WARN: ARJUNA012035: Activate of object with id = 0:ffffac118252:dde3:537b57b0:0 and type /StateManager/LockManager/AtomicObject unexpectedly failed
May 20, 2014 3:53:40 PM com.arjuna.ats.txoj.LockManager setlock
WARN: ARJUNA015034: LockManager::setlock() cannot activate object
0:ffffac118252:e0e0:537b6c6e:6: AtomicObject exception raised: TestException: Read lock error.
May 20, 2014 3:53:40 PM com.arjuna.ats.arjuna.StateManager activate
WARN: ARJUNA012035: Activate of object with id = 0:ffffac118252:dde3:537b57b0:0 and type /StateManager/LockManager/AtomicObject unexpectedly failed
May 20, 2014 3:53:40 PM com.arjuna.ats.txoj.LockManager setlock
WARN: ARJUNA015034: LockManager::setlock() cannot activate object
0:ffffac118252:e0e0:537b6c6e:6: AtomicObject exception raised: TestException: Read lock error.
May 20, 2014 3:53:40 PM com.arjuna.ats.arjuna.StateManager activate
WARN: ARJUNA012035: Activate of object with id = 0:ffffac118252:dde3:537b57b0:0 and type /StateManager/LockManager/AtomicObject unexpectedly failed
May 20, 2014 3:53:40 PM com.arjuna.ats.txoj.LockManager setlock
WARN: ARJUNA015034: LockManager::setlock() cannot activate object
0:ffffac118252:e0e0:537b6c6e:6: AtomicObject exception raised: TestException: Read lock error.

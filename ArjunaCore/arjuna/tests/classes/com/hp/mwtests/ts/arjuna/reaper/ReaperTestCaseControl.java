package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * class which provides control methods inherited by the reaper test classes. these methods can be
 * used to enable, disable and trigger execution of byteman rules which regulate progress of the reaper
 * and reaper worker thread as the reaper tests progress. they rely on the code being run in a JVM
 * using the byteman agent with a suitable rule script which performs the desired actions when these
 * methods are called. See script reaper.txt for the corresponding rules.
 */
public class ReaperTestCaseControl
{
    /**
     * called to enable a rendezvous with count 2. this method works by triggering a rule which calls
     * Byteman helper builtin createRendezvous(o, 2, repeatable)
     * @param o the key identifying the rendezvous
     * @param repeatable whether the created rendezvous should be repeatable or not
     */
    protected void enableRendezvous(Object o, boolean repeatable)
    {
        // do nothing this is just used for rule triggering
    }

    /**
     * called to delete a rendezvous with count 2. this method works by triggering a rule which calls
     * Byteman helper builtin deleteRendezvous(o, 2)
     * @param o the key identifying the rendezvous
     */
    protected void disableRendezvous(Object o)
    {
        // do nothing this is just used for rule triggering
    }

    /**
     * called to trigger entry into a rendezvous with count 2. this method works by triggering a rule which
     * calls Byteman helper builtin rendezvous(o). The assumption is that the code under test will also be
     * trying to enter the rendezvous.
     * @param o the key identifying the rendezvous
     */
    protected void triggerRendezvous(Object o)
    {
        // do nothing this is just used for rule triggering
    }

    /**
     * called to trigger a delay. this method works by triggering a rule which calls Byteman helper builtin
     * delay(msecs).
     * @param msecs the number of milliseconds for which the thread should delay
     */
    protected void triggerWait(int msecs)
    {
        // do nothing this is just used for rule triggering
    }

    /**
     * called to test a rule system flag and clear it at the same time. this method works by triggering a rule
     * which calls Byteman helper builtin clear(o)
     * @param o the key which identifies the rule system flag to be tested and cleared
     */
    protected boolean checkAndClearFlag(Object o)
    {
        // return false by default -- rule system will intercept and return the relevant flag
        // setting

        return false;
    }

    /**
     * reapable which can be controlled using rule actions driven by the test class
     */
    public class TestReapable implements Reapable
    {
        /**
         * create a mock reapable
         * @param uid
         * @param doCancel true if the reapable should return ABORTED from the cancel call and false if it should
 * return RUNNING
         * @param rendezvousInCancel true iff the reapable should rendezvous with the test code when cancel is called
         * @param doRollback the value that the reapable should return true from the prevent_commit call
         * @param rendezvousInInterrupt  true iff the reapable should rendezvous with the test code when it is
         */
        public TestReapable(Uid uid, boolean doCancel, boolean rendezvousInCancel, boolean doRollback, boolean rendezvousInInterrupt)
        {
            this.uid = uid;
            this.rendezvousInCancel = rendezvousInCancel;
            this.rendezvousInInterrupt = rendezvousInInterrupt;
            this.doCancel = doCancel;
            this.doRollback = doRollback;
            cancelTried = false;
            rollbackTried = false;
            running = true;
        }

        public boolean running()
        {
            return getRunning();
        }

        public boolean preventCommit()
        {
            setRollbackTried();

            if (rendezvousInInterrupt) {
                triggerRendezvous(uid);
                triggerRendezvous(uid);
            }

            clearRunning();
            return doRollback;
        }

        public int cancel()
        {
            boolean interrupted = false;

            setCancelTried();

            // track the worker trying to do the cancel so we can
            // detect if it becomes a zombie

            setCancelThread(Thread.currentThread());

            if (rendezvousInCancel) {
                triggerRendezvous(uid);
                triggerRendezvous(uid);
            }

            if (doCancel) {
                clearRunning();
                return ActionStatus.ABORTED;
            } else {
                return ActionStatus.RUNNING;
            }
        }

        public Uid get_uid()
        {
            return uid;
        }

        private Uid uid;
        private boolean doCancel;
        private boolean doRollback;
        private boolean rendezvousInCancel;
        private boolean rendezvousInInterrupt;
        private boolean cancelTried;
        private boolean rollbackTried;
        private boolean running;
        private Thread cancelThread;

        public synchronized void setCancelTried()
        {
            cancelTried = true;
        }

        public synchronized boolean getCancelTried()
        {
            return cancelTried;
        }

        public synchronized void setCancelThread(Thread cancelThread)
        {
            this.cancelThread = cancelThread;
        }

        public synchronized Thread getCancelThread()
        {
            return cancelThread;
        }

        public synchronized void setRollbackTried()
        {
            rollbackTried = true;
        }

        public synchronized boolean getRollbackTried()
        {
            return rollbackTried;
        }

        public synchronized void clearRunning()
        {
            running = false;
        }

        public synchronized boolean getRunning()
        {
            return running;
        }
    }
}

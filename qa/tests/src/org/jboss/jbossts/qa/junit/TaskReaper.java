package org.jboss.jbossts.qa.junit;

import java.util.SortedSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * Task manager which ensures that processes created during unit tests are destroyed when they
 * do no complete after a suitable timeout.
 */
public class TaskReaper
{
    // api methods
    // n.b  all internal methods synchronize on the value in the reaperLock field when they need
    // exclusive access to the internal state. note that the api methods are not synchronized but
    // they should nto be called concurrently.

    /**
     * insert a task into the list of managed tasks
     * @param task the task to be inserted
     * @param absoluteTimeout the absolute system time measured in milliseconds from the epoch at which
     * the task's process should be destroyed if it has not been removed from the list by then
     */
    public void insert(TaskImpl task, long absoluteTimeout)
    {
        synchronized(reaperLock) {
            if (shutdown) {
                throw new RuntimeException("invalid call to TaskReaper.insert after shutdown");
            }
            TaskReapable reapable = new TaskReapable(task, absoluteTimeout);
            reapableMap.put(task, reapable);
            taskList.add(reapable);
            // notify the reaper thread
            reaperLock.notify();
        }
    }

    /**
     * remove a task from the list of managed tasks
     * @param task the task to be removed
     * @return true if the task was present in the list and was removed before a timeout caused its
     * process to be destroyed otherwise false
     */
    public boolean remove(TaskImpl task)
    {
        synchronized(reaperLock) {
            TaskReapable reapable = reapableMap.get(task);
            if (reapable != null) {
                taskList.remove(reapable);
                // notify the reaper thread
                reaperLock.notify();
                return true;
            }
        }
        return false;
    }

    /**
     * check if there are any tasks in the list
     * @return true if the list is empty otherwise false
     */
    public boolean allClear()
    {
        synchronized (reaperLock) {
            return taskList.isEmpty();
        }
    }

    /**
     * remove any remaining tasks from the list, destroying their process, and return a count of the
     * number of tasks which have failed to exit cleanly. this count includes tasks which have been killed
     * because of timeouts as well as those destroyed under the call to clear().
     * @return a count of how many tasks exited abnormally.

     */
    public int clear()
    {
        int returnCount;
        synchronized(reaperLock) {
            // set the absolute timeout of every task to zero then wake up the reaper and wait for
            // the list to empty
            Iterator<TaskReapable> iterator = taskList.iterator();
            while (iterator.hasNext()) {
                TaskReapable reapable = iterator.next();
                reapable.absoluteTimeout = 0;
            }
            reaperLock.notify();
            // ok now wait until all the tasks have gone
            // n.b this relies upon the caller not inserting new tasks while the clear operation is in progress!
            while (!taskList.isEmpty()) {
                try {
                    reaperLock.wait();
                } catch (InterruptedException e) {
                    // ignore -- we should never be interrupted here
                }
            }
            returnCount = invalidCount;
            invalidCount = 0;
        }
        return returnCount;
    }

    /**
     * shut down the task manager
     * @param immediate if true then shut down without destroying any task in the current list otherwise
     * atempt to destroy all pending tasks.
     */
    public void shutdown(boolean immediate)
    {
        // unset the current reaper instance

        clearTheReaper(this);

        // now ensure that any tasks it had pending are removed or time out

        synchronized (reaperLock) {
            shutdown = false;
            // setting shutdownWait to false makes the reaper thread exit without clearing the list
            // setting it true makes it exit once all tasks have been destroyed
            if (immediate) {
                shutdownWait = false;
            } else {
                shutdownWait = true;
                // set the absolute timeout of every task to zero then wake up the reaper and wait for
                // the list to empty
                Iterator<TaskReapable> iterator = taskList.iterator();
                while (iterator.hasNext()) {
                    TaskReapable reapable = iterator.next();
                    reapable.absoluteTimeout = 0;
                }
            }

            // notify so that the reaper thread wakes up

            reaperLock.notify();

            // we don't get out of here until the reaper thread has exited
            
            while (!threadShutdown) {
                try {
                    reaperLock.wait();
                } catch (InterruptedException e) {
                    // ignore -- we should never be interrupted here
                }
            }
        }
    }

    /**
     * obtain a handle on the currently active reaper, creating a new one if there is no reaper active
     * @return
     */
    public static synchronized TaskReaper getReaper()
    {
        if (theReaper == null) {
            createReaper();
        }

        return theReaper;
    }

    /**
     * reset the current reaper instance to null. this is called from the current reaper instanmce's shutdown
     * method to reset the current handle to null.
     */
    private static synchronized void clearTheReaper(TaskReaper theReaperReaped)
    {
        // if the current reaper still identifies the one we just shutdown then reset it to null

        if (theReaper == theReaperReaped) {
            theReaper = null;
        }
    }

    // implementation methods and state

    // package public access only for use by TaskReaperThread

    /**
     * entry point for the task reaper thread to detect timed out tasks in the background. this should not
     * be called anywhere except in TaskReaperThread.run
     * @return
     */
    void check()
    {
        synchronized(reaperLock) {
            while (!shutdown || shutdownWait) {
                if (taskList.isEmpty()) {
                    // wait as long as we need to
                    try {
                        reaperLock.wait();
                    } catch (InterruptedException e) {
                        // ignore -- we should never be interrupted here
                    }
                } else {
                    TaskReapable first = taskList.first();
                    long absoluteTime = System.currentTimeMillis();
                    long firstAbsoluteTime = first.getAbsoluteTimeout();
                    if (absoluteTime < firstAbsoluteTime) {
                        // use difference to limit wait
                        try {
                            reaperLock.wait(firstAbsoluteTime - absoluteTime);
                        } catch (InterruptedException e) {
                            // ignore -- we should never be interrupted here
                        }
                    } else {
                        // we have a task to kill so kill it, wait a brief interval so we don't hog
                        // the cpu and then loop to see if there are more to kill
                        if (timeout(first)) {
                            invalidCount++;
                        }
                        // notify here in case a thread was trying to modify the list while we were doing
                        // the timeout
                        reaperLock.notify();
                        try {
                            reaperLock.wait(1);
                        } catch (InterruptedException e) {
                            // ignore -- we should never be interrupted here
                        }
                    }
                }
            }
            threadShutdown = true;
            // notify here so we wakeup the thread which initiated the shutdown
            reaperLock.notify();
        }
    }

    private static TaskReaper theReaper = null;
    private SortedSet<TaskReapable> taskList;
    private HashMap<TaskImpl, TaskReapable> reapableMap;
    private int invalidCount;
    private boolean shutdown;
    private boolean shutdownWait;
    private boolean threadShutdown;
    private Object reaperLock;    
    private TaskReaperThread reaperThread;

    private TaskReaper()
    {
        taskList = new TreeSet<TaskReapable>();
        reapableMap = new HashMap<TaskImpl, TaskReapable>();
        invalidCount = 0;
        shutdown = false;
        shutdownWait = false;
        threadShutdown = false;
        reaperLock = new Object();
        reaperThread = new TaskReaperThread(this);
        reaperThread.start();
    }

    /**
     * start the task manager
     */
    private static void createReaper()
    {
        theReaper = new TaskReaper();
    }

    /**
     * destroy a timed out task and remove it from the task list. n.b. this must be called when
     * synchronized on the reaper lock
     * @param reapable the task to be destroyed
     * @return true if the task exited invalidly otherwise false
     */
    private boolean timeout(TaskReapable reapable)
    {
        TaskImpl task = reapable.getTask();
        reapableMap.remove(task);
        taskList.remove(reapable);
        return reapable.getTask().timeout();
    }

    /**
     * wrapper which associates a task with its absoulte timeout and provides a comparator which allows
     * tasks to be sorted in order of absolute timeout
     */
    private static class TaskReapable implements Comparable<TaskReapable>
    {
        public TaskReapable(TaskImpl task, long absoluteTimeout)
        {
            long now = System.currentTimeMillis();
            this.absoluteTimeout = now + absoluteTimeout;
            this.task = task;
        }

        public long getAbsoluteTimeout()
        {
            return absoluteTimeout;
        }

        public TaskImpl getTask() {
            return task;
        }

        private long absoluteTimeout;
        private TaskImpl task;

        public int compareTo(TaskReapable o) {
            if (this == o) {
                return 0;
            }

            if (absoluteTimeout < o.absoluteTimeout) {
                return -1;
            } else if (absoluteTimeout > o.absoluteTimeout) {
                return 1;
            } else {
                // try to sort using hash codes
                int h = hashCode();
                int oh = o.hashCode();
                if (h < oh) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    private static class TaskReaperThread extends Thread
    {
        public TaskReaperThread(TaskReaper reaper)
        {
            this.reaper = reaper;
        }
        public void run()
        {
            reaper.check();
        }

        private TaskReaper reaper;
    }
}

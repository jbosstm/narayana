package com.arjuna.ats.tools.perftest;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

import com.arjuna.ats.tools.perftest.product.Product;

public class TestRunner
{
    private final static Logger log = Logger.getLogger(Product.class);

    private Collection<Target> tests = new ArrayList<Target>();
    private long max;
    private long min;
    private long avg;
    private long tot;
    private int passCount;
    private long failCount;
    private ArrayList<Long> times = new ArrayList<Long> ();

    public Object addTest(PerfTest test)
    {
        Target t = new Target(test);

        tests.add(t);
        t.setDaemon(true); // means rogue test won't stop the VM from terminating
//        t.setPriority(Thread.MAX_PRIORITY);

        return t;
    }

    public void startTest(Object handle)
    {
        Target t = (Target) handle;

        if (tests.contains(t) && !isRunning(t))
            t.start();
    }

    public void startTests()
    {
        for (Target t : tests)
            t.run();
    }

    public boolean isRunning(Object handle)
    {
        Target t = (Target) handle;

        return tests.contains(t) && t.isAlive();
    }

    public void cancelTest(Object t)
    {
        ((Thread) t).interrupt();
    }

    public boolean waitOn()
    {
        for (Target t : tests)
            waitOn(t);

        return (failCount == 0);
    }

    public boolean waitOn(Object t)
    {
        return waitOn(t, 0);
    }

    public boolean waitOn(Object handle, long timeout)
    {
        Target t = (Target) handle;

        if (t.processed)
            return (t.getResult() != -1);

        if (!tests.contains(t))
            return false;

        if (isRunning(t))
        {
            try
            {
                t.join(timeout);
            }
            catch (InterruptedException e)
            {
            }
            if (log.isInfoEnabled()) log.info("joined with task " + t.getName() + ": t=" + t.getResult() + " e: " + t.getException());
        }
        else
        {
            if (log.isInfoEnabled()) log.info("task " + t.getName() + " is not running: t=" + t.getResult() + " e: " + t.getException());
        }

        long rt = t.getResult();

        t.processed = true;

        if (rt != -1 && t.getException() == null)
        {
            times.add(rt);
            passCount += 1;
            tot += rt;

            if (rt > max)
                max = rt;
            if (rt < min || min == 0)
                min = rt;

            avg = tot / passCount;

            return true;
        }
        else
        {
            log.warn("Test " + t.getName() + " failed: " + t.getException());            
            failCount += 1;

            return false;
        }
    }

    public String getTimes()
    {
        StringBuilder sb = new StringBuilder();

        for (Long l : times)
        {
            sb.append(l).append(',');
        }

        return sb.toString();
//        return times.toArray(new Long[times.size()]);
    }

    public long getMax()
    {
        return max;
    }

    public long getMin()
    {
        return min;
    }

    public long getAvg()
    {
        return avg == 0 ? 1 : avg;
    }

    public int getPassCount()
    {
        return passCount;
    }

    public long getFailCount()
    {
        return failCount;
    }

    private class Target extends Thread
    {
        PerfTest test;
        boolean processed;

        Target(PerfTest target)
        {
            super(target);
            this.test = target;
        }

        long getResult()
        {
            return test.getResult();
        }

        public Exception getException()
        {
            return test.getException();
        }
    }
}

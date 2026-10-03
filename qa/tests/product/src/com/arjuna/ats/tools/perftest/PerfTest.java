package com.arjuna.ats.tools.perftest;

public interface PerfTest extends Runnable
{
    long getResult();

    Exception getException();
}

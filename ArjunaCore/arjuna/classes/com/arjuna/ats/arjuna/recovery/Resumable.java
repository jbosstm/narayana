package com.arjuna.ats.arjuna.recovery;

public interface Resumable {
    void resume();
    void suspend();
    boolean isSuspended();
}

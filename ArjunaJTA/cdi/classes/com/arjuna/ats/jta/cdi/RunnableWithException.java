/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package com.arjuna.ats.jta.cdi;

/**
 * Functional interface for 'run' method
 * that throws a checked exception.
 */
@FunctionalInterface
public interface RunnableWithException {
    void run() throws Exception;
}
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit;

/**
 * Interface for a executable test element i.e. process.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 */
public interface Task
{
    public enum TaskType { EXPECT_PASS_FAIL, EXPECT_READY, EXPECT_READY_PASS_FAIL }

    /**
     * execute a type PASS_FAIL task in a subprocess passing no arguments to the Main method of the
     * implementing class then wait for the subprocess to exit.
     */
    public void perform();

    /**
     * execute a type PASS_FAIL task in a subprocess passing the supplied arguments to the Main method
     * of the implementing class then wait for the subprocess to exit.
     * @param params arguments to supply to the main method of the implementing class
     */
    public void perform(String... params);

    /**
     * execute a type PASS_FAIL or type READY task asynchronously in a subprocess passing no arguments to the Main
     * method of the implementing class. if the task type is READY do not return until it has printed Ready.
     */
    public void start();

    /**
     * execute a type PASS_FAIL or type READY task asynchronously in a subprocess passing the supplied arguments to
     * the Main method of the implementing class. if the task type is READY do not return until it has printed
     * Ready.
     * @param params arguments to supply to the main method of the implementing class
     */
    public void start(String... params);

    /**
     * check that a type PASS_FAIL task which was started asynchronously has printed either Passed or Failed
     * and exited cleanly, waiting if it has not yet completed and asserting if either condition fails.
     */
    public void waitFor();

    /**
     * terminate a type READY task
     */
    public void terminate();
}
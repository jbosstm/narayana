/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2009,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.junit;

/**
 * Interface for a executable test element i.e. process.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 */
public interface Task
{
    public enum TaskType { EXPECT_PASS_FAIL, EXPECT_READY }

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

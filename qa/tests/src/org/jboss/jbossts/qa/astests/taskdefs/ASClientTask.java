/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.taskdefs;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.jboss.jbossts.qa.astests.taskdefs.ClientAction;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Ant task for executing a chunk of java code. The interface name of the code to be executed is specified
 * the impl field:
 * @see ClientAction
 *
 * It the task completes successfully "Passed" is printed on system output, otherwise "Failed" is printed
 */
public class ASClientTask extends Task
{
    /**
     * The text to print on standard output if the client action returned the expected result.
     * Note that these values can be overridden on per task basis
     * When running under the DTF the text should correspond values defined in the file
     * nodeconfig.xml. The DTF TaskRunner controlling the test will search the output stream
     * for this text to determine success or failure.
     */
    public static final String PASS = "Passed";
    public static final String FAIL = "Failed";

    private String impl;
    private String waitFor;
    private String name;
    private String passText = PASS;
    private String failText = FAIL;
    private List<TaskProperty> params = new ArrayList<TaskProperty>();
    private boolean abortOnFail = true;

    public void execute() throws BuildException
    {
        ASTestConfig config = (ASTestConfig) getProject().getReference(ASTestConfig.CONFIG_REF);
        Map<String, String> args = new HashMap<String, String> ();

        for (TaskProperty param : params)
            args.put(param.getKey(), param.getValue());

        ClientAction action = null;

        try
        {
            suspendFor(waitFor);
            
            action = (ClientAction) Class.forName(impl).getDeclaredConstructor().newInstance();
        }
        catch (ClassCastException e)
        {
            System.err.println("Class " + impl + " does not implement " + ClientAction.class.getName());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Cannot locate class " + impl);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            System.err.println("Class " + impl + " cannot be instantiated: " + e.getMessage());
        }

        try
        {
            printResult(action.execute(config, args));
        }
        catch (Exception e)
        {
            System.out.println("Error executing test: " + e.getMessage());
            printResult(false);
        }

    }

    private void printResult(boolean passed)
    {
        StringBuilder sb = new StringBuilder();

        if (name != null)
        {
            sb.append(name).append(' ');
        }

        System.out.println(sb.append(passed ? passText : failText));

        if (!passed && abortOnFail)
            throw new BuildException("Test failed");
    }

    /**
     * Suspend the calling thread
     * @param millis the number of milli seconds to suspend for
     * @return false if interupted
     * @throws IllegalArgumentException if millis is not a number or negative
     */
    static boolean suspendFor(String millis) throws IllegalArgumentException
    {
        try
        {
            try
            {
                if (millis != null)
                    Thread.sleep(Integer.parseInt(millis));
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(e);
            }

            return true;
        }
        catch (InterruptedException e)
        {
            return false;
        }
    }

    /**
     * Task property to set the test name. This name will be printed when the test completes
     * followed by a string to indicate pass or failure.
     * see org.jboss.jbossts.qa.astests.taskdefs.ASClientTask.PASS
     * @param name the name of the test.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    /**
     * Task property to force the task to suspend before executing the client action
     *
     * @param waitFor the number of milli seconds to suspend - a null value means don't suspend
     * @throws IllegalArgumentException if waitFor is not a number or is negative
     */
    public void setWaitFor(String waitFor) throws IllegalArgumentException
    {
        this.waitFor = waitFor;
    }

    public void setAbortOnFail(String abortOnFail)
    {
        this.abortOnFail = "true".equals(abortOnFail);
    }

    /**
     * Task property containing the fully qualified class name of the action to execute.
     * This class must contain an empty constructor and implement:
     * @see ClientAction
     *
     * @param impl the class name of the action that this task will instantiate and run
     */
    public void setImpl(String impl)
    {
        this.impl = impl;
    }

    /**
     * Task parameters that are passed into the execute method of the client action
     *
     * @param param a task parameter
     */
    public void addParam(TaskProperty param)
    {
        params.add(param);
    }

    /**
     * Task property defaults to
     * see org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig.PASS
     *
     * @param passText the text to print if the test succeeds
     */
    public void setPassText(String passText)
    {
        this.passText = passText;
    }

    /**
     * Task property to
     * see org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig.FAIL
     *
     * @param failText the text to print if the test succeeds
     */
    public void setFailText(String failText)
    {
        this.failText = failText;
    }
}

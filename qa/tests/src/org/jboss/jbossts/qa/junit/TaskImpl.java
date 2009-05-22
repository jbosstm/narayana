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

import org.junit.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Implementation of the Task abstraction, essentially a test aware wrapper around a spawned process.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 */
public class TaskImpl implements Task
{
    private final static String PROPERTIES_FILE = "TaskImpl.properties";

    private static Properties properties;
    static {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(PROPERTIES_FILE));
        } catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final Set<TaskImpl> tasks = new HashSet();

    //////////////////

    private Class clazz;
    private TaskType type;
    private boolean isRunning;
    private boolean isDone;
    private boolean isChecked;
    Process process;
    TaskReaderThread taskReaderThread;

    TaskImpl(Class clazz, Task.TaskType type)
    {
        if(clazz == null || type == null) {
            throw new ExceptionInInitializerError("TaskImpl()<ctor> params may not be null");
        }

        this.clazz = clazz;
        this.type = type;
    }

    public void perform() {
        perform((String[])null);
    }

    public void perform(String... params)
    {
        if(type != TaskType.EXPECT_PASS_FAIL) {
            throw new RuntimeException("can't perform an EXPECT_READY task");
        }

        if(isDone || isChecked || isRunning) {
            throw new RuntimeException("invalid state");
        }

        String[] command = assembleCommand(clazz.getCanonicalName(), params);

        Assert.assertTrue(tasks.add(this));
/*
        int i = 0;
        for(String string : command) {
            System.out.println("["+i+"] "+string);
            i++;
        }
*/
        System.out.println(clazz.getName());

        boolean printedPassed = false;
        boolean printedFailed = false;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if("Passed".equals(line)) {
                    printedPassed = true;
                }
                if("Failed".equals(line)) {
                    printedFailed = true;
                }
                System.out.println("Line: "+line);
            }

            process.waitFor();
            Assert.assertEquals(0, process.exitValue());

        } catch (Exception e) {
            Assert.fail(e.toString());
        }


        Assert.assertTrue(tasks.remove(this));

        Assert.assertFalse(printedFailed);
        Assert.assertTrue(printedPassed);

        isDone = true;
        isChecked = true;
    }

    public void start()
    {
        start((String[])null);
    }

    public void start(String... params)
    {
        if(isDone || isChecked || isRunning) {
            throw new RuntimeException("invalid state");
        }

        String[] command = assembleCommand(clazz.getCanonicalName(), params);
/*
        int i = 0;
        for(String string : command) {
            System.out.println(string+" ");
            //System.out.println("["+i+"] "+string);
            i++;
        }
*/
        Assert.assertTrue(tasks.add(this));

        System.out.println(clazz.getName());

        try {
            process = Runtime.getRuntime().exec(command);

            taskReaderThread = new TaskReaderThread( new BufferedReader(new InputStreamReader(process.getInputStream())) );
            taskReaderThread.start();

            // TODO deal with it printing Failed

            TaskReaderThread stdErr = new TaskReaderThread( new BufferedReader(new InputStreamReader(process.getErrorStream())) );
            stdErr.start(); // if we don't consume it the pipe fills up and the process blocks.

            if(type.equals(TaskType.EXPECT_READY)) {
                taskReaderThread.blockingWaitForReady();
                System.out.println("got ready");
            }

        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        isRunning = true;
    }

    public void waitFor()
    {
        if(isDone || isChecked || !isRunning) {
            throw new RuntimeException("invalid state");
        }

        Assert.assertTrue(tasks.remove(this));

        try {
            process.waitFor();
        } catch(Exception e) {
            Assert.fail(e.toString());
        }
        Assert.assertEquals(0, process.exitValue());

        taskReaderThread.checkIsFinishedCleanly();

        if(type.equals(TaskType.EXPECT_PASS_FAIL)) {
            taskReaderThread.checkPassFail();
        } else {
            Assert.fail("should not waitFor EXPECT_READY tasks (use terminate)");
        }

        isRunning = false;
        isDone = true;
        isChecked = true;
    }

    public void terminate()
    {
        if(isDone || isChecked || !isRunning) {
            throw new RuntimeException("invalid state");
        }

        Assert.assertTrue(tasks.remove(this));

        try {
            process.destroy();
        } catch(Exception e) {
            Assert.fail(e.toString());
        }


        if(!type.equals(TaskType.EXPECT_READY)) {
            Assert.fail("Should not terminate EXPECT_PASS_FAIL tasks (use waitFor)");
        }

        isDone = true;
        isChecked = true;
    }

    /////////////////////////

    public static void assertNoTasks() {
        Assert.assertEquals(0, tasks.size());
    }

    private String[] assembleCommand(String classname, String[] params) {
        params = substituteParams(params);

        int i = 0;
        boolean done = false;
        List<String> list = new LinkedList();
        while(!done) {
            String element = properties.getProperty("COMMAND_LINE_"+i);
            if(element == null) {
                done = true;
            } else {
                list.add(element);
                i++;
            }
        }

        list.add(classname);

        if(params != null) {
            for(String param : params) {
                list.add((param));
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private String[] substituteParams(String[] params) {
        if(params == null || params.length == 0) {
            return null;
        }

        String[] result = new String[params.length];

        for(int i = 0; i < params.length; i++) {
            if(params[i].startsWith("$(")) {
                String key = params[i].substring(2, params[i].length()-1);
                String value = properties.getProperty(key);
                Assert.assertNotNull("Properties file missing key "+key, value);
                result[i] = value;
            } else {
                result[i] = params[i];
            }
        }

        return result;
    }

    private class TaskReaderThread extends Thread {

        BufferedReader bufferedReader;

        private final AtomicBoolean printedReady = new AtomicBoolean(false);
        private final AtomicBoolean isFinishedCleanly = new AtomicBoolean(false);
        private volatile boolean printedPassed = false;
        private volatile boolean printedFailed = false;

        public void blockingWaitForReady() {
            synchronized (printedReady) {
                while(!printedReady.get()) {
                    try {
                        printedReady.wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }

        public void checkIsFinishedCleanly() {

            try {
                this.join();
            } catch(InterruptedException e) {
                // do nothing
            }

            Assert.assertTrue(isFinishedCleanly.get());
            Assert.assertFalse(printedFailed);
        }

        public void checkPassFail() {
            Assert.assertFalse(printedFailed);
            Assert.assertTrue(printedPassed);
        }

        public TaskReaderThread(BufferedReader bufferedReader) {
            this.bufferedReader = bufferedReader;
        }

        public void run() {
            try {
                String line;
                while((line = bufferedReader.readLine()) != null) {
                    if("Ready".equals(line)) {
                        synchronized (printedReady) {
                            printedReady.set(true);
                            printedReady.notify();
                        }
                    }
                    if("Passed".equals(line)) {
                        printedPassed = true;
                    }
                    if("Failed".equals(line)) {
                        printedFailed = true;
                    }
                    System.out.println("Line: "+line);
                }

                synchronized (isFinishedCleanly) {
                    isFinishedCleanly.set(true);
                    isFinishedCleanly.notify();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * Reimplementation of the Task abstraction. This version cooperates with a task reaper which times out
 * tasks whose underlying subprocess has become wedged. This can be used to ensure that the thread running
 * a junit tests does not get wedged by a badly behaving subprocess. The task reaper also provides a means
 * to terminate any left over tasks during the test tearDown.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-05
 * @author Andrew Dinn (adinn@redhat.com) 2009-06
 */
public class TaskImpl implements Task
{
    public static int DEFAULT_TIMEOUT_SECONDS = 600;

    private final static String PROPERTIES_FILE = "TaskImpl.properties";

    private final static List<String> additionalCommandLineElements = new LinkedList<String>();

    private static Properties properties = new Properties();
    static {
        try {
            Properties rawproperties = new Properties();
            rawproperties.load(new FileInputStream(PROPERTIES_FILE));

            // do property value token substitution, copy result to the actual properties:

            Pattern substitutionPattern = Pattern.compile("\\$\\{(.*?)\\}");

            Enumeration propertyNames = rawproperties.propertyNames();
            while(propertyNames.hasMoreElements()) {
                String name = (String)propertyNames.nextElement();
                String rawvalue = rawproperties.getProperty(name);
                StringBuffer buffer = new StringBuffer();
                Matcher matcher = substitutionPattern.matcher(rawvalue);
                while(matcher.find()) {
                    String group = matcher.group(1);
                    String replacement = Matcher.quoteReplacement(rawproperties.getProperty(group, System.getProperty(group, "")));
                    matcher.appendReplacement(buffer, replacement);
                }
                matcher.appendTail(buffer);
                String mungedvalue = buffer.toString();
                properties.setProperty(name, mungedvalue);
            }

            // in addition to the props file, we allow properties via. "additional.properties" var

            String additionalElementString = System.getProperty("additional.elements");
            if(additionalElementString != null) {
                String[] additionalElements = additionalElementString.split("\\s+");
                for(String element : additionalElements) {
                    if(element.length() > 0) {
                        addCommandLineElement(element);
                    }
                }
            }

        } catch(Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * A string identifying this task instance for e.g. debug logging purposes.
     */
    private String taskName;

    /**
     * a class implementing a main method which gets executed in a JVM running in a subprocess in order
     * to perform this task
     */
    private Class clazz;

    /**
     * identifies whether the task is a test which is expected to print Passed or Failed or a service
     * which is expected to print Ready.
     */
    private TaskType type;

    /**
     * a timeout in seconds from when the task is started after which the task should be destroyed
     * automatically by the task reaper if it has nto yet already been destroyed
     */
    private int timeout;

    /**
     * A handle on the process which was created to execute the task
     */
    Process process;

    /**
     * tasks can only be started once. this flag gaurds against repeated start attempts
     */
    private boolean started;
    /**
     * flag identifying whether or not this task's subpricess has finished executing
     */
    private boolean isDone;
    /**
     * a flag which is set if the task's process times out and gets destroyed before the task's subprocess
     * has finished executing.
     */
    private boolean isTimedOut;
    /**
     * an output stream to which the contents of the task's stdout and stderr are redirected.
     */
    private PrintStream out;
    /**
     * a thread which reads the task's merged stdout/stderr stream and identifies whether or not a Passed/Failed
     * or a Ready message has been printed. the reader thread alos needs to write the output to a log file.
     */
    private TaskReaderThread taskReaderThread;

    /**
     * a thread which reads the task's merged stdout/stderr stream and identifies whether or not a Passed/Failed
     * or a Ready message has been printed. the reader thread alos needs to write the output to a log file.
     */
    private TaskErrorReaderThread taskErrorReaderThread;

    /**
     * create a new task
     * @param clazz the task whose main method is to be executed in a JVM running in a subprocess
     * @param type the type of the test either PASS_FAIL or READY
     * @param out the output stream to which output from the task's process shoudl be redirected.
     * @param timeout the timeout for the task in seconds
     */
    TaskImpl(String taskName, Class clazz, TaskType type, PrintStream out, int timeout)
    {
        if(clazz == null || type == null) {
            throw new ExceptionInInitializerError("TaskImpl()<ctor> params may not be null");
        }

        this.taskName = taskName;
        this.clazz = clazz;
        this.type = type;
        this.timeout = timeout;
        this.out = out;
        this.started = false;
        this.isDone = false;
        this.isTimedOut = false;
        this.taskReaderThread = null;
    }

    /**
     * execute a type READY task in a subprocess passing no arguments to the Main method of the
     * implementing class then wait for the subprocess to exit.
     */
    public void perform() {
        perform((String[])null);
    }

    /**
     * execute a type READY task in a subprocess passing the supplied arguments to the Main method
     * of the implementing class then wait for the subprocess to exit.
     * @param params arguments to supply to the main method of the implementing class
     */
    public void perform(String... params)
    {
        if(type != TaskType.EXPECT_PASS_FAIL) {
            throw new RuntimeException("can't perform an EXPECT_READY task");
        }

        boolean printedPassed = false;
        boolean printedFailed = false;

        String[] command = assembleCommand(clazz.getCanonicalName(), params);

        logCommand(System.out, "performing command: ", command);
        logCommand(out, "performing command: ", command);

        // cannot restart a task

        synchronized(this) {
            if(started) {
                throw new RuntimeException("invalid state for perform");
            }
            // first make sure we can create a subprocess

            try {
                // process = Runtime.getRuntime().exec(command);
                ProcessBuilder builder = new ProcessBuilder(command);
                // !!!! we cannot do this as we cannot be sure the spawned task does not
                // interleave its stdout and stderr streams. if it does then this messes up detection
                // of Ready, Passed and Failed output lines since they may get mingled with lines
                // from the error stream. !!!!
                // redirect errors to stdout -- avoids getting wedged when error output stream becomes full
                // builder.redirectErrorStream(true);
                process = builder.start();
            } catch (Exception e) {
                Assert.fail(e.toString());
            }

            // ok, we have started so register with the task reaper -- need to synchronize so we can set
            // started atomically

            TaskReaper.getReaper().insert(this, timeout * 1000);

            started = true;
        }
        // this is a PASS_FAIL test and we need to wait for it to complete
        BufferedReader bufferedReader = null;
        try {
            // create an error stream reader to merge error output into the output file
            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            taskErrorReaderThread = new TaskErrorReaderThread(bufferedReader, out, "err: ");
            taskErrorReaderThread.start();

            // now read stdout checking for passed or failed
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            while((line = bufferedReader.readLine()) != null) {
                // need to redirect to file
                Date date = new Date();
                out.println(simpleDateFormat.format(date)+" "+"out: " + line);
                out.flush();

                if("Passed".equals(line)) {
                    printedPassed = true;
                }
                if("Failed".equals(line)) {
                    printedFailed = true;
                }
            }
        } catch (Exception e) {
            // if we fail here then the reaper task will clean up the thread
            Assert.fail(e.toString());
        } finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ioException) {
                    // ignore
                }
        }

        // we cannot really protect properly against races here because process.waitFor takes its
        // own lock before then suspending so there will always be a window here. luckily process.destroy()
        // which gets called by the reaper is idempotent, meaning the only thing we risk is that the task
        // reaper may call destroy and register a stalled thread even though we get through the waitFor
        // call.
        try {
            process.waitFor();
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        // ok, now ensure we sort out the race between the reaper task and this one
        synchronized(this) {
            if (!isTimedOut) {
                // we got through the waitFor and relocked this before we coudl be timed out so remove the
                // task from the reaper list
                TaskReaper.getReaper().remove(this);
            }
            // setting this will forestall any pending attempt to timeout this task
            isDone = true;

            out.close();            
        }
        // we barf if we didn't exit with status 0 or print Passed or Failed

        Assert.assertEquals(0, process.exitValue());
        Assert.assertFalse("Task "+taskName+" printed failed", printedFailed);
        Assert.assertTrue(printedPassed);

        // clean exit --  hurrah!
    }

    /**
     * execute a type PASS_FAIL or type READY task asynchronously in a subprocess passing no arguments to the Main
     * method of the implementing class. if the task type is READY do not return until it has printed Ready.
     */
    public void start()
    {
        start((String[])null);
    }

    /**
     * execute a type PASS_FAIL or type READY task asynchronously in a subprocess passing the supplied arguments to
     * the Main method of the implementing class. if the task type is READY do not return until it has printed
     * Ready.
     * @param params arguments to supply to the main method of the implementing class
     */
    public void start(String... params)
    {
        String[] command = assembleCommand(clazz.getCanonicalName(), params);

        logCommand(System.out, "starting command: ", command);
        logCommand(out, "starting command: ", command);

        // cannot restart a task

        synchronized(this) {
            if(started) {
                throw new RuntimeException("invalid state for start");
            }

            // first make sure we can create a subprocess
            try {
                // process = Runtime.getRuntime().exec(command);
                ProcessBuilder builder = new ProcessBuilder(command);
                // !!!! we cannot do this as we cannot be sure the spawned task does not
                // interleave its stdout and stderr streams. if it does then this messes up detection
                // of Ready, Passed and Failed output lines since they may get mingled with lines
                // from the error stream. !!!!
                // redirect errors to stdout -- avoids getting wedged when error output stream becomes full
                // builder.redirectErrorStream(true);
                process = builder.start();
            } catch (Exception e) {
                Assert.fail(e.toString());
            }

            TaskReaper.getReaper().insert(this, timeout * 1000);

            started = true;
        }

        // set up threads to do the I/O processing

        BufferedReader bufferedReader = null;
        try {
            // create an error stream reader to merge error output into the output file
            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            taskErrorReaderThread = new TaskErrorReaderThread(bufferedReader, out, "err: ");
            taskErrorReaderThread.start();

            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            taskReaderThread = new TaskReaderThread(taskName, bufferedReader, out, "out: ");
            taskReaderThread.start();

            if(type.equals(TaskType.EXPECT_READY)) {
                taskReaderThread.blockingWaitForReady();
                // System.out.println("got ready");
            }

        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    /**
     * check that a type PASS_FAIL task which was started asynchronously has printed either Passed or Failed
     * and exited cleanly, waiting if it has not yet completed and asserting if either condition fails.
     */
    public void waitFor()
    {
        if(type.equals(TaskType.EXPECT_READY)) {
            Assert.fail("should not waitFor EXPECT_READY tasks (use terminate)");
        }

        synchronized(this) {
            if (isDone || !started) {
                throw new RuntimeException("invalid state for waitFor");
            }

            if (isTimedOut) {
                throw new RuntimeException("wait for timed out task");
            }
        }

        // we cannot really protect properly against races here because process.waitFor takes its
        // own lock before then suspending so there will always be a window here. luckily process.destroy()
        // which gets called by the reaper is idempotent, meaning the only thing we risk is that the task
        // reaper may call destroy and register a stalled thread even though we get through the waitFor
        // call.

        try {
            process.waitFor();
        } catch (Exception e) {
            Assert.fail(e.toString());
        }

        // ok, now ensure we sort out the race between the reaper task and this one

        synchronized(this) {
            if (!isTimedOut) {
                // we got through the waitFor and relocked this before we could be timed out so remove the
                // task from the reaper list
                TaskReaper.getReaper().remove(this);
            }
            // setting this will forestall any pending attempt to timeout this task
            isDone = true;

            out.close();
        }

        // throw up if we didn't exit with exit code 0
        Assert.assertEquals(0, process.exitValue());

        // the taskReaderThread will throw up if it did nto get a clean finish or get a Passed and no Failed
        taskReaderThread.checkIsFinishedCleanly();
        taskReaderThread.checkPassFail();

        // clean exit --  hurrah!
    }

    /**
     * terminate a type READY task
     */
    public void terminate()
    {
        if(type.equals(TaskType.EXPECT_PASS_FAIL)) {
            Assert.fail("Should not terminate EXPECT_PASS_FAIL tasks (use waitFor)");
        }

        synchronized(this) {
            if (isDone || !started) {
                throw new RuntimeException("invalid state for terminate");
            }

            if (isTimedOut) {
                throw new RuntimeException("terminate for timed out task");
            }

            TaskReaper.getReaper().remove(this);

            // setting this will forestall any pending attempt to timeout this task
            isDone = true;

            out.close();            
        }

        if (taskReaderThread != null) {
            taskErrorReaderThread.shutdown();
        }

        if (taskReaderThread != null) {
            // tell the reader not to throw a wobbly before we destroy the process
            taskReaderThread.shutdown();
        }

        process.destroy();
    }

    /**
     * ensure that any tasks started during the last test run are killed
     */
    public static void cleanupTasks()
    {
        TaskReaper.getReaper().clear();
    }

    /////////////////////////

    /**
     * ensure that there are no tasks left running, clearing out all such tasks and asserting if this
     * is not the case
     */
    public static void assertNoTasks() {
        // Assert.assertEquals(0, tasks.size());
        Assert.assertTrue(TaskReaper.getReaper().clear() == 0);
    }

    /////////////////////////
    // package public for use by TaskReaper and internally

    /**
     * destroy the subprocess associated with this task
     * @return true if the process was timed out and false if it exited before we got there
     */
    boolean timeout()
    {
        synchronized(this) {
            if (isDone) {
                return false;
            } else {
                isTimedOut = true;
                out.close();
            }

            if (taskReaderThread != null) {
                taskErrorReaderThread.shutdown();
            }

            if (taskReaderThread != null) {
                // tell the reader not to throw a wobbly before we destroy the process
                taskReaderThread.shutdown();
            }

            out.println("!!!TASK TIME OUT!!!");
            out.flush();
            // we timed out before the process managed to complete so kill it now
            // n.b. since this closes the process stdout we can be sure that the task
            // reader thread will exit.
            process.destroy();
        }
        return true;
    }
    /////////////////////////
    // private implementation

    /**
     * construct an executable command line to execute the supplied java class with the arguments in params,
     * substituting those in the form $(...) with corresponding values derived from the properties file.
     * @param classname
     * @param params
     * @return the command line as an array of strings
     */
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

        list.addAll(additionalCommandLineElements);

        list.add(classname);

        if(params != null) {
            for(String param : params) {
                list.add((param));
            }
        }
        
        return list.toArray(new String[list.size()]);
    }

    public static void addCommandLineElement(String additionalCommandLineElement)
    {
        additionalCommandLineElements.add(additionalCommandLineElement);
    }

    /**
     * construct a copy of the supplied argument String array replacing all terms in the form $(...) with
     * corresponding values derived from the properties file
     * @param params the original arguments to be copied
     * @return the substituted copy
     */
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

    /**
     * Log the command line to an outout
     */
    private void logCommand(PrintStream out, String prefix, String[] command) {
        out.print(prefix);
        for(String commandElement : command) {
            out.print(commandElement);
            out.print(" ");
        }
        out.println();
        out.flush();
    }

    /**
     * a thread created whenever a task is started asynchronously to forward output from the task's process
     * to an output stream and to detect printing of Ready, Passed and Failed ouptut lines.
     */
    private class TaskReaderThread extends Thread {

        BufferedReader bufferedReader;
        PrintStream out;
        private String prefix;
        private String taskName;

        private final AtomicBoolean printedReady = new AtomicBoolean(false);
        private final AtomicBoolean isFinishedCleanly = new AtomicBoolean(false);
        private volatile boolean printedPassed = false;
        private volatile boolean printedFailed = false;
        private final AtomicBoolean shutdown = new AtomicBoolean(false);

        /**
         * called by the test thread under Task.start() to ensure that a type READY task's process has printed a
         * Ready output line before returning from the start call.
         */
        public void blockingWaitForReady() {
            synchronized (printedReady) {
                // test of shutdown ensures we exit this loop if we get destroyed on a timeout
                while(!printedReady.get() && !shutdown.get()) {
                    try {
                        printedReady.wait();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }

            // make sure the test fails of we did not see ready

            Assert.assertTrue(printedReady.get());
        }

        /**
         * called by the test thread to ensure that a type PASS_FAIL or READY task's process has exited and
         * closed its output stream cleanly, thereby causing the reader thread to exit cleanly.
         */
        public void checkIsFinishedCleanly() {

            try {
                this.join();
            } catch(InterruptedException e) {
                // do nothing
            }

            Assert.assertTrue(isFinishedCleanly.get());
        }

        /**
         * called by the test thread to ensure that a type PASS_FAIL task's process has printed a Passed
         * output line and has not printed a Failed output line.
         */
        public void checkPassFail() {
            Assert.assertFalse("task "+taskName+" printed Failed.", printedFailed);
            Assert.assertTrue("task "+taskName+" did not print Passed.", printedPassed);
        }

        /**
         * create a task reader thread defaulting the prefix to "Line: " and the output stream to
         *
         */
        public TaskReaderThread(String taskName, BufferedReader bufferedReader, PrintStream out, String prefix) {
            this.taskName = taskName;
            this.bufferedReader = bufferedReader;
            this.prefix = prefix;
            this.out = out;
        }

        public void run() {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
            
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
                    Date date = new Date();
                    out.println(simpleDateFormat.format(date)+" "+prefix + line);
                }

                isFinishedCleanly.set(true);
            } catch(Exception e) {
                // make sure no one is waiting for a ready
                synchronized(printedReady) {
                    printedReady.notify();
                }
                
                // if the process is explicitly destroyed we can see an IOException because the output
                // stream gets closed under the call to readLine(). shutdown is set befofe destroy
                // is called so only trace the exception if we are not shut down
                if (shutdown.get()) {
                    return;
                }
                out.println("TaskReaderThread : exception before shutdown " + e);
                e.printStackTrace(out);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }

        public void shutdown() {
            shutdown.set(true);
        }
    }
    /**
     * a thread created whenever a task is started asynchronously to forward error output from the task's
     * process to an output stream.
     */
    private class TaskErrorReaderThread extends Thread {

        BufferedReader bufferedReader;
        PrintStream out;
        private String prefix;
        private AtomicBoolean shutdown = new AtomicBoolean(false);

        public TaskErrorReaderThread(BufferedReader bufferedReader)
        {
            this(bufferedReader, System.err, "err: ");
        }

        public TaskErrorReaderThread(BufferedReader bufferedReader, PrintStream out, String prefix)
        {
            this.bufferedReader = bufferedReader;
            this.out = out;
            this.prefix = prefix;
        }

        public void run() {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

            try {
                String line;
                while((line = bufferedReader.readLine()) != null) {
                    Date date = new Date();
                    out.println(simpleDateFormat.format(date)+" "+prefix + line);
                }
            } catch(Exception e) {
                // if the process is explicitly destroyed we can see an IOException because the output
                // stream gets closed under the call to readLine(). shutdown is set befofe destroy
                // is called so only trace the exception if we are not shut down
                if (shutdown.get()) {
                    return;
                }
                out.println("TaskErrorReaderThread : exception before shutdown " + e);
                e.printStackTrace(out);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    // ignore
                }
            }
        }
        
        public void shutdown() {
            shutdown.set(true);
        }
    }
}
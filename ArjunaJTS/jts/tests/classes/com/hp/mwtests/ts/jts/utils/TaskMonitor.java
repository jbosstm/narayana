/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jts.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Useful class for monitoring a thread and producing a stack dump if it stops making forward progress
 * (after which the thread will no longer be monitored). For a thread to be monitored it should periodically
 * report its progress via an instance of @see TaskProgress.tick
 */
public enum TaskMonitor {
    /**
     * Obtain a reference to the TaskMonitor singleton
     */
    INSTANCE;

    private final String outputDirectory = System.getProperty("user.dir");
    private final Collection<Job> jobs = new ArrayList<Job>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable progressChecker;

    private AtomicInteger maxDumps = new AtomicInteger(1);
    private long minDelay = Long.MAX_VALUE;
    private ScheduledFuture<?> progressHandle = null;

    private TaskMonitor() {
        progressChecker = new Runnable() {
            public void run() {
                long timeNow = System.currentTimeMillis();
                synchronized (this) {
                    Iterator<Job> i = jobs.iterator();

                    while (i.hasNext()) {
                        Job job = i.next();

                        if (job.isFinished()) {
                            try {
                                i.remove();
                            } catch (Exception e) {
                                System.out.printf("Error removing monitor job %s%n", e.getMessage());
                                e.printStackTrace();
                            }
                        } else if (!job.isProgressing(timeNow)) {
                            createThreadDumps();
                        }
                    }
                }
            }
        };
    }

    public synchronized TaskProgress monitorProgress(String name, String cmdLinePattern, long millisBetweenUpdates) {
        return monitorProgress(name, cmdLinePattern, new TaskProgress(0), millisBetweenUpdates);
    }

    /**
     * Monitor the progress of a task
     *
     * @param name a name for the task used in the filename of any stack dumps
     * @param cmdLinePattern pattern used to determine which java process should have there stacks dumpted
     * @param progress an object that a monnitored thread should periodically update
     * @param millisBetweenUpdates if the job does not make progress within this many milliseconds take stack dumps
     * @return get the tasks progress back
     */
    public synchronized TaskProgress monitorProgress(String name, String cmdLinePattern,
                                                     TaskProgress progress, long millisBetweenUpdates) {
        if (maxDumps.intValue() <= 0)
            return null;

        if (progress == null)
            progress = new TaskProgress(0);

        jobs.add(new Job(name, cmdLinePattern, progress, millisBetweenUpdates));

        if (millisBetweenUpdates < minDelay) {
            minDelay = millisBetweenUpdates;
            if (progressHandle != null)
                progressHandle.cancel(false);

            // TODO not a fair algorithm since the task may end up running later that desired but it does the job for now
            progressHandle = scheduler.scheduleAtFixedRate(progressChecker, millisBetweenUpdates, millisBetweenUpdates,
                    TimeUnit.MILLISECONDS);
        }

        return progress;
    }

    private void terminateScheduler() {
        progressHandle.cancel(false);
        jobs.clear();
        scheduler.shutdown();
    }

    private void createThreadDumps() {
        if (maxDumps.getAndDecrement() <= 0) {
            terminateScheduler();
            return;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder("jps", "-mlv");
            java.lang.Process process = builder.start();
            Scanner scanner = new Scanner(process.getInputStream());

            while (scanner.hasNextLine()) {
                String cmd = scanner.nextLine();

                for (Job job : jobs)
                    if (job.matches(cmd))
                        createThreadDump(job.getName(), cmd);
            }

            scanner.close();
            process.destroy();
        } catch (IOException e) {
            System.out.printf("ERROR CREATING THREAD DUMPS: %s\n", e.getMessage());
        }

        if (maxDumps.intValue() == 0)
            terminateScheduler();

    }

    private void createThreadDump(String name, String jps) {
        int i = jps.indexOf(' ');
        String pid = i > 0 ? jps.substring(0, i) : null;

        if (pid == null)
            return;

        ProcessBuilder builder = new ProcessBuilder("jstack", pid);

        System.out.printf("Creating stack dump for job %s  pid %s and cmd %s\n", name, pid, jps);

        try {
            java.lang.Process process = builder.start();
            String dumpFile = outputDirectory + "/testoutput/jstack." + name + '.' + pid;
            File df = new File(dumpFile);
            df.getParentFile().mkdirs();

            OutputStream os = new FileOutputStream(df);
            os.write(jps.getBytes());
            os.write(System.getProperty("line.separator").getBytes());
            byte[] buffer = new byte[1024];
            int len = process.getInputStream().read(buffer);

            while (len != -1) {
                os.write(buffer, 0, len);
                len = process.getInputStream().read(buffer);
            }
            os.close();
        } catch (IOException e) {
            System.out.printf("ERROR CREATING THREAD DUMP for %s: %s\n", jps, e.getMessage());
        }
    }

    class Job {
        private AtomicInteger progress;
        private long millisBetweenUpdates;
        private int lastValue;
        private long lastTimeChecked;
        private String cmdLinePattern;
        private String name;

        Job(String name, String cmdLinePattern, AtomicInteger progress, long millisBetweenUpdates) {
            this.progress = progress;
            this.millisBetweenUpdates = millisBetweenUpdates;
            this.lastValue = progress.intValue();
            this.lastTimeChecked = System.currentTimeMillis();
            this.cmdLinePattern = cmdLinePattern;
            this.name = name;
        }

        /**
         * Check that a job is incrementing an atomic integer at least every millisBetweenUpdates milliseconds.
         * If the integer is less that zero then the task has completed
         *
         * @param currentTimeMillis the current time
         * @return true if the job is making progress
         */
        boolean isProgressing(long currentTimeMillis) {
            int currentValue = progress.intValue();

            if (!isFinished() && lastTimeChecked + millisBetweenUpdates < currentTimeMillis) {
                if (currentValue == lastValue)
                    return false;

                lastTimeChecked = currentTimeMillis;
                lastValue = currentValue;
            }

            return true;
        }

        boolean isFinished() {
            return progress.intValue() < 0;
        }

        public boolean matches(String cmd) {
            return cmdLinePattern != null ? cmd.contains(cmdLinePattern) : true;
            // use Pattern.compile if more flexibility is required
        }

        public String getName() {
            return name;
        }
    }
}

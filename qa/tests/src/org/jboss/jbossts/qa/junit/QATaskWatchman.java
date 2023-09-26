/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.qa.junit;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

class QATaskWatchman extends TestWatchman {
    private long startTime;
    private QATestNameRule testName; //t he test group and name
    private String passFileName; // the file where which passes are reported
    private String failFileName; // the file where failures are reported

    /**
     * Assign streams where the test watcher will log test passes and failures
     * @param testName the test group, not nul
     * @param passFileName the file where passes are reported
     * @param failFileName the file where failures are reported
     */
    public QATaskWatchman(QATestNameRule testName, String passFileName, String failFileName) {
        this.testName = testName;
        this.passFileName = passFileName;
        this.failFileName = failFileName;
    }

    @Override
    public void succeeded(FrameworkMethod method) {
        super.succeeded(method);
        reportStatus(true);
    }

    @Override
    public void failed(Throwable e, FrameworkMethod method) {
        super.failed(e, method);
        reportStatus(false);
    }

    @Override
    public void starting(FrameworkMethod method) {
        super.starting(method);
        startTime = System.currentTimeMillis();
    }

    private PrintStream openPrintStream(String fileName) {
        try {
            return new PrintStream(new FileOutputStream(fileName, true));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    private void reportStatus(boolean succeeded) {
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) ((millis / 1000) % 60);
        int minutes = (int) ((millis / 1000) / 60);
        PrintStream printStream;
        String message;

        if (succeeded) {
            printStream = openPrintStream(passFileName);
            message = "Pass";
        } else {
            printStream = openPrintStream(failFileName);
            message = "Fail";
        }

        if (testName != null && printStream != null) {
            printStream.printf("%s %s %s (%dm%d.%03ds)\n",
                testName.getGroupName(), testName.getMethodName(), message,
                minutes, seconds, (millis % 1000));
        }
    }
}

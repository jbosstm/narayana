/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;

public abstract class CSControl extends TestCase {
    static final Logger log = LogManager.getLogger(CSControl.class);
    // the byte pattern written by a server to indicate that it has advertised
    // its services
    private static final byte[] HANDSHAKE = { 83, 69, 82, 86, 73, 67, 69, 83, 32, 82, 69, 65, 68, 89 };

    private ProcessBuilder serverBuilder;
    private ProcessBuilder clientBuilder;
    private TestProcess server;
    private String CS_EXE;
    private String REPORT_DIR;
    private static int sid = 1;
    private Connection connection;

    protected boolean isSunOS = false;
    protected boolean isWinOS = false;

    static String nextSid() {
        return Integer.toString(sid % 10);
    }

    public void tearDown() {
        log.info("tearDown");
        try {
            if (server != null) {
                log.debug("destroying server process");
                server.interrupt();

                log.debug("send shutdown command to the BTDomainAdmin");
                X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
                sendbuf.setByteArray("shutdown,testsui,0,".getBytes());
                connection.tpcall("BTDomainAdmin", sendbuf, 0);
                log.debug("destroyed server process");
                server.getProcess().waitFor();
            }
            log.debug("waited for server process");
        } catch (Throwable e) {
            throw new RuntimeException("Server shutdown error: ", e);
        }
    }

    public void setUp() throws Exception {
        log.info("setUp");
        String osName = (System.getProperty("os.name"));
        isSunOS = (osName != null && "SunOS".equals(osName));
        isWinOS = (osName != null && osName.toLowerCase().indexOf("win") >= 0);
        REPORT_DIR = System.getProperty("TEST_REPORTS_DIR", ".");
        CS_EXE = System.getProperty("CLIENT_SERVER_EXE", "./cs");
        clientBuilder = new ProcessBuilder();
        serverBuilder = new ProcessBuilder();
        // clientBuilder.redirectErrorStream(true);
        // serverBuilder.redirectErrorStream(true);

        java.util.Map<String, String> environment = serverBuilder.environment();
        // environment.clear();
        environment.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
        environment.put("BLACKTIE_CONFIGURATION_DIR", System.getenv("BLACKTIE_CONFIGURATION_DIR"));
        environment.put("BLACKTIE_SCHEMA_DIR", System.getenv("BLACKTIE_SCHEMA_DIR"));
        environment.put("JBOSSAS_IP_ADDR", System.getenv("JBOSSAS_IP_ADDR"));
        environment.put("PATH", System.getenv("PATH"));
        clientBuilder.environment().putAll(environment);
        environment.put("LOG4CXXCONFIG", "log4cxx-CSTest-server.properties");
        clientBuilder.environment().put("LOG4CXXCONFIG", "log4cxx-CSTest-client.properties");

        if (connection == null) {
            ConnectionFactory cf = ConnectionFactory.getConnectionFactory();
            connection = cf.getConnection();
        }
    }

    public void runServer(String name) {
        try {
            log.info("start server process: " + name);

            String property = System.getProperty("USE_VALGRIND");
            String[] command = null;
            String nextSid = nextSid();
            if (property != null && new Boolean(property)) {
                command = ("valgrind --tool=memcheck --leak-check=full --log-file=server-"
                        + name
                        + "-valgrind.log -v -d --track-origins=yes --leak-resolution=low --num-callers=40 "
                        + CS_EXE + " -s testsui -p 12342 -i " + nextSid).split(" ");
            } else {
                command = (CS_EXE + " -s testsui -p 12342 -i " + nextSid).split(" ");
            }
            serverBuilder.command(command);

            server = startServer(name, serverBuilder);
        } catch (IOException e) {
            throw new RuntimeException("Server io exception: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Server interrupted: ", e);
        }
    }

    public void runTest(String name) {
        if ((isWinOS && ("2121".equals(name) || "9".equals(name))) || (isSunOS && ("213".equals(name) || "4".equals(name)))) {
            log.info("skipping test " + name);
            return;
        }

        try {
            log.info("waiting for test " + name);

            String property = System.getProperty("USE_VALGRIND");
            String[] command = null;
            if (property != null && new Boolean(property)) {
                command = ("valgrind --tool=memcheck --leak-check=full --log-file=client-"
                        + name
                        + "-valgrind.log -v -d --track-origins=yes --leak-resolution=low --num-callers=40 "
                        + CS_EXE + " " + name).split(" ");
            } else {
                command = (CS_EXE + " " + name).split(" ");
            }
            clientBuilder.command(command);
            TestProcess client = startClient(name, clientBuilder);
            int res = client.exitValue();

            log.info("test " + name + (res == 0 ? " passed " : " failed ") + res);
            assertTrue(res == 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private TestProcess startClient(String testname, ProcessBuilder builder) throws IOException, InterruptedException {
        String runTime = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        FileOutputStream ostream = new FileOutputStream(REPORT_DIR + "/test-" + testname + "-" + runTime + "-out.txt");
        FileOutputStream estream = new FileOutputStream(REPORT_DIR + "/test-" + testname + "-" + runTime + "-err.txt");
        TestProcess client = new TestProcess(ostream, estream, "client", builder);
        Thread thread = new Thread(client);

        thread.start();
        log.debug("startClient: waiting to join with client thread ...");
        thread.join();
        log.debug("startClient: joined - waiting for client process to exit");
        client.getProcess().waitFor();
        log.debug("startClient: done");

        return client;
    }

    private TestProcess startServer(String testname, ProcessBuilder builder) throws IOException, InterruptedException {
        String runTime = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        FileOutputStream ostream = new FileOutputStream(REPORT_DIR + "/server-" + testname + "-" + runTime + "-out.txt");
        FileOutputStream estream = new FileOutputStream(REPORT_DIR + "/server-" + testname + "-" + runTime + "-err.txt");
        TestProcess server = new TestProcess(ostream, estream, "server", builder);
        Thread thread = new Thread(server);

        synchronized (server) {
            // start the C server and wait for it to indicate that it has
            // advertised its services
            thread.start();
            log.debug("startServer waiting for process to finish ...");
            server.wait();
        }

        return server;
    }

    class TestProcess implements Runnable {
        private String type;
        private Process proc;
        private FileOutputStream ostream;
        private FileOutputStream estream;
        private Thread thread;
        private ProcessBuilder builder;

        TestProcess(FileOutputStream ostream, FileOutputStream estream, String type, ProcessBuilder builder) {
            this.ostream = ostream;
            this.estream = estream;
            this.type = type;
            this.builder = builder;
        }

        Process getProcess() {
            return proc;
        }

        int exitValue() {
            return proc.exitValue();
        }

        void interrupt() {
            if (thread != null)
                thread.interrupt();
        }

        public void run() {
            thread = Thread.currentThread();

            try {
                proc = builder.start();

                InputStream is = proc.getInputStream();
                InputStream es = proc.getErrorStream();
                byte[] buf = new byte[1024];
                int len;
                int match = -1;

                /*
                 * redirect the process I/O to a file - if it is a server then notify any waiters when the server outputs a
                 * magic sequence indicating that it has advertised its services
                 */
                if ("server".equals(type)) {
                    // assume the HANDSHAKE sequence can be read in one go
                    /*
                     * while ((len = is.read(buf)) > 0) { if (match == -1 && (match = KMPMatch.indexOf(buf, HANDSHAKE, len)) !=
                     * -1) { synchronized (this) { this.notify(); } }
                     * 
                     * ostream.write(buf, 0, len); }
                     */
                    log.debug("server monitoring process I/O ...");
                    int pos = 0;
                    while ((len = is.read(buf, pos, buf.length - pos)) > 0) {
                        ostream.write(buf, pos, len);

                        if (match == -1) {
                            pos += len;

                            if ((match = KMPMatch.indexOf(buf, HANDSHAKE, pos)) != -1) {
                                synchronized (this) {
                                    this.notify();
                                }
                                pos = 0;
                            } else if (pos == buf.length) {
                                ostream.write("missing synchronization sequence from service - force notify".getBytes(StandardCharsets.UTF_8));
                                log.warn("missing synchronization sequence from service");
                                synchronized (this) {
                                    this.notify();
                                }
                                pos = 0;
                                match = 0;
                            }
                        } else {
                            pos = 0;
                        }
                    }
                } else {
                    while ((len = is.read(buf)) > 0)
                        ostream.write(buf, 0, len);
                }

                while ((len = es.read(buf)) > 0)
                    estream.write(buf, 0, len);
            } catch (IOException e) {
                if (!Thread.interrupted())
                    log.warn(builder.command() + ": IO error on stream write: " + e);
            }

            log.debug("server process: read termination byte sequence");

            try {
                ostream.close();
                estream.close();
            } catch (IOException e) {
            }
        }
    }
}

package com.arjuna.qa.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.arjuna.qa.extension.JBossAS7ServerKillProcessor;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

public class BaseCrashTest
{

    private static final Logger logger = Logger.getLogger(BaseCrashTest.class.getName());

    protected String XTSServiceTest = " -Dorg.jboss.jbossts.xts.servicetests.XTSServiceTestName=@TestName@";
    protected String javaVmArguments;
    protected String testName;
    protected String scriptName;
    private final static String xtstestWar = "../xtstest/target/xtstest.war";
    @ArquillianResource
    private ContainerController controller;
    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = "xtstest", testable = false, managed = false)
    @TargetsContainer("jboss-as")
    public static Archive<?> createTestArchive()
    {
        WebArchive archive = ShrinkWrap.
                createFromZipFile(WebArchive.class, new File(xtstestWar));
        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.modules,org.jboss.msc,org.jboss.jts,org.jboss.xts\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @Before
    public void setUp()
    {
        javaVmArguments = System.getProperty("server.jvm.args")
                .replaceAll("=listen","=script:target/test-classes/scripts/@BMScript@.btm,boot:target/lib/byteman.jar,listen");

        javaVmArguments = javaVmArguments.replace("@BMScript@", scriptName);


        System.out.println("Starting arquillian with java VM args: " +
                javaVmArguments + " isIPv6: " + isIPv6());

        File file = new File("testlog");
        if (file.isFile() && file.exists())
        {
            file.delete();
        }

        //Ensure ObjectStore is empty:
        String jbossHome = System.getenv("JBOSS_HOME");
        if (jbossHome == null)
        {
            Assert.fail("$JBOSS_HOME not set");
        }
        else
        {
            File objectStore = new File(jbossHome + File.separator + "standalone" + File.separator + "data" + File.separator + "tx-object-store");
            System.out.println("Deleting: " + objectStore.getPath());

            if (objectStore.exists())
            {
                boolean success = deleteDirectory(objectStore);
                if (!success)
                {
                    System.err.println("Failed to remove tx-object-store");
                    Assert.fail("Failed to remove tx-object-store: " + objectStore.getPath());
                }
                else
                {
                    System.out.println("remove tx-object-store: " + objectStore.getPath());
                }
            }
            //Remove the xts deployments under the content
            File contentDir = new File(jbossHome + File.separator + "standalone" + File.separator + "data" + File.separator + "content");
            if(contentDir.exists())
            {
                File[] files = contentDir.listFiles();
                if(files != null) 
                {
                    int i = 0;
                    for(i=0;i<files.length;i++) 
                    {
                        if(files[i].isDirectory()) 
                        {
                            deleteDirectory(files[i]);
                            System.out.println("remove " + files[i].getPath());
                        }
                    }
                }
            }
            
            File exampleXTSconfig = new File(jbossHome + File.separator + "docs" + File.separator + "examples" + File.separator + "configs" + File.separator + "standalone-xts.xml");
            File XTSconfig = new File(jbossHome + File.separator + "standalone" + File.separator + "configuration" + File.separator + "standalone-xts.xml");
            if(exampleXTSconfig.exists()) 
            {
                //copy example config to configuration directory
                try {
                    FileInputStream in = new FileInputStream(exampleXTSconfig);
                    FileOutputStream out = new FileOutputStream(XTSconfig);
                    byte[] buffer = new byte[1024];

                    int length;
                    //copy the file content in bytes 
                    while ((length = in.read(buffer)) > 0)
                    {
                        out.write(buffer, 0, length);

                    }

                    in.close();
                    out.close();
                    System.out.println("copy " + exampleXTSconfig.getPath() + " to " + XTSconfig.getPath());
                }
                catch(IOException e)
                {
                    Assert.fail("copy " + exampleXTSconfig.getPath() + " fail with " + e);
                }
            } 
            else
            {
                Assert.fail(exampleXTSconfig.getPath() + " not exists");
            }
        }
    }

    @After
    public void tearDown()
    {     
        String log = "target/log";

        String jbossHome = System.getenv().get("JBOSS_HOME");
        if(jbossHome == null) {
            Assert.fail("$JBOSS_HOME not set");
        }
        String dir = jbossHome + "/standalone/data/tx-object-store/ShadowNoFileLockStore/defaultStore/XTS/";
        File objectStore = new File(dir);
        boolean ischeck = checkTxObjectStore(objectStore);
        if(!ischeck) {
            archiveObjectStore(jbossHome, testName);
            StringBuffer buffer = exploreDirectory(objectStore, 0);
            System.out.println(buffer);
        }
        Assert.assertTrue(ischeck);

        if (testName != null && scriptName != null)
        {
            String logFileName = scriptName + "." + testName;
            File file = new File("testlog");
            File logDir = new File(log);

            if (!logDir.exists())
            {
                logDir.mkdirs();
            }

            if (file.isFile() && file.exists())
            {
                file.renameTo(new File(log + "/" + logFileName));
            }
        }
    }

    protected void runTest(String testClass) throws Exception
    {
        logger.info("Test starting, server should be down: " + scriptName + ":" + testName);

        Config config = new Config();
        config.add("javaVmArguments", javaVmArguments + XTSServiceTest.replace("@TestName@", testClass));

        controller.start("jboss-as", config.map());

        try {
            deployer.deploy("xtstest");
        } catch (java.lang.RuntimeException e) {
            //JBTM-1236 it could be ignore this exception because the container might be killed already and JVM.kill() has happened.
            System.out.println("jboss-as has been killed");
        }

        //Waiting for crashing
        controller.kill("jboss-as");

        //Boot jboss-as after crashing
        config.add("javaVmArguments", javaVmArguments);
        controller.start("jboss-as", config.map());

        //Waiting for recovery happening
        controller.kill("jboss-as");

        logger.info("Test completed, server should be down: " + scriptName + ":" + testName);
    }

    private boolean deleteDirectory(File path) 
    {
        if (path.exists())
        {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    deleteDirectory(files[i]);
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private boolean checkTxObjectStore(File objectStore) 
    {    
        if(objectStore.exists() && objectStore.isDirectory())
        {
            File[] files = objectStore.listFiles();
            if(files != null) {
                int i = 0;
                for(i=0;i<files.length;i++) {
                    if(files[i].isDirectory()) {
                        if (checkTxObjectStore(files[i]) == false)
                            return false;
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isIPv6() {
        try {
            if (InetAddress.getLocalHost() instanceof Inet6Address || System.getenv("IPV6_OPTS") != null)
                return true;
        } catch (final UnknownHostException uhe) {
        }

        return false;
    }

    private StringBuffer exploreDirectory(File directory, int level) {
        List<File> files = Arrays.asList(directory.listFiles());
        StringBuffer result = new StringBuffer();
        String NEWLINE = "\n";
        String FILE_GRAPHIC = "- ";
        String DIRECTORY_GRAPHIC = "+- ";

        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < level; i++) {
            spaces.append(" ");
        }

        for (File cur : files) {
            if (cur.isDirectory()) {
                result.append(spaces + DIRECTORY_GRAPHIC + "["+ cur.getName() +"]"
                        + NEWLINE);

                List<File> afiles = Arrays.asList(cur.listFiles());
                for (File acur : afiles) {
                    if (acur.isFile()) {
                        result.append(" " + spaces + FILE_GRAPHIC + acur.getName() + NEWLINE);
                        try {
                            FileInputStream fis = new FileInputStream(acur);
                            InputStreamReader bis = new InputStreamReader(fis);
                            BufferedReader dis  = new BufferedReader(bis);

                            String s;
                            do {
                                s = dis.readLine();
                                result.append(" " + spaces + s + NEWLINE);
                            }while(s != null);

                            fis.close();
                            bis.close();
                            dis.close();
                        } catch (IOException e) {
                            //ignore
                        }

                    }
                }
                result.append(exploreDirectory(cur, level + 1));
            } else if(level == 0) {
                result.append(spaces + FILE_GRAPHIC + cur.getName() + NEWLINE);
            }
        }
        return result;
    }

    private void archiveObjectStore(final String jbossHome, final String testName) {
        final String source = jbossHome + "/standalone/data/tx-object-store";
        String target = "target/";

        if (testName != null) {
            target += testName + "_tx-object-store.zip";
        } else {
            target += new Date().getTime() + "_tx-object-store.zip";
        }

        final ZipArchiver zipArchiver = new ZipArchiver();

        try {
            zipArchiver.createArchive(source, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

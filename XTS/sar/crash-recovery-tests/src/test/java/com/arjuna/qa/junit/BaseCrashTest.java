package com.arjuna.qa.junit;

import java.io.File;
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
    protected String XTSServiceTest = " -Dorg.jboss.jbossts.xts.servicetests.XTSServiceTestName=@TestName@";
    protected String BytemanArgs = "-Xms64m -Xmx512m -XX:MaxPermSize=256m -Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:target/test-classes/lib/byteman.jar=script:target/test-classes/scripts/@BMScript@.txt,boot:target/test-classes/lib/byteman.jar,listener:true";
    protected String javaVmArguments;
    protected String testName;
    protected String scriptName;
    private final static String xtstestWar = "../tests/target/xtstest.war";
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
            + "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts,org.jboss.xts\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @Before
    public void setUp()
    {
        javaVmArguments = BytemanArgs.replace("@BMScript@", scriptName);

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
        Assert.assertTrue(checkTxObjectStore(objectStore));

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
        Config config = new Config();
        config.add("javaVmArguments", javaVmArguments + XTSServiceTest.replace("@TestName@", testClass));

        controller.start("jboss-as", config.map());
        //deployer.undeploy("xtstest");
        deployer.deploy("xtstest");

        //Waiting for crashing
        controller.kill("jboss-as");

        //Boot jboss-as after crashing
        config.add("javaVmArguments", javaVmArguments);
        controller.start("jboss-as", config.map());

        //redeploy xtstest
        deployer.undeploy("xtstest");
        deployer.deploy("xtstest");

        //Waiting for recovery
        //Thread.sleep(waitForRecovery * 60 * 1000);

        //deployer.undeploy("xtstest");
        //controller.stop("jboss-as");
        controller.kill("jboss-as");
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
}

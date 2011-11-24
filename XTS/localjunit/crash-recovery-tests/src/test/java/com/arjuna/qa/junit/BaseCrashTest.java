package com.arjuna.qa.junit;

import java.io.File;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BaseCrashTest {
	protected String XTSServiceTest = " -Dorg.jboss.jbossts.xts.servicetests.XTSServiceTestName=@TestName@";
	protected String BytemanArgs = "-Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:target/test-classes/lib/byteman.jar=script:target/test-classes/scripts/@BMScript@.txt,boot:target/test-classes/lib/byteman.jar,listener:true";
	protected String javaVmArguments;
	protected String testName;
	protected String scriptName;
	private final static String xtstestWar = "../../sar/tests/target/xtstest.war";

	@ArquillianResource
	private ContainerController controller;

	@ArquillianResource
	private Deployer deployer;
	
	@Deployment(name = "xtstest", testable = false, managed = false)
	@TargetsContainer("jboss-as")
	public static Archive<?> createTestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(xtstestWar));
		return archive;	
	}
	@Before
	public void setUp() {
		javaVmArguments = BytemanArgs.replace("@BMScript@", scriptName);
		
		File file = new File("testlog");
		if(file.isFile() && file.exists()){
			file.delete();
		}
	}
	
	@After
	public void tearDown() {
		String log = "target/log";

		if(testName != null && scriptName != null) {
			String logFileName = scriptName + "." + testName;
			File file = new File("testlog");
			File logDir = new File(log);

			if(!logDir.exists()) {
				logDir.mkdirs();
			}

			if(file.isFile() && file.exists()){
				file.renameTo(new File(log+"/"+logFileName));
			}
		}
	}
	
	protected void runTest(String testClass) throws Exception {
		Config config = new Config();
		config.add("javaVmArguments", javaVmArguments + XTSServiceTest.replace("@TestName@", testClass));

		controller.start("jboss-as", config.map());
		deployer.deploy("xtstest");

		//Waiting for crashing
		Thread.sleep(2 * 60 * 1000);

		//Boot jboss as after crashing
		config.add("javaVmArguments", javaVmArguments);
		controller.start("jboss-as", config.map());

		//Waiting for recovery
		Thread.sleep(5 * 60 * 1000);

		deployer.undeploy("xtstest");
		controller.stop("jboss-as");
	}
}

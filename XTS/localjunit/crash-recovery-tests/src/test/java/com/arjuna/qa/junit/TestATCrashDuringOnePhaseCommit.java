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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATCrashDuringOnePhaseCommit extends BaseCrashTest {
	private final static String xtstestWar = "../../sar/tests/target/xtstest.war";

	@ArquillianResource
	private ContainerController controller;

	@ArquillianResource
	private Deployer deployer;
	
	public TestATCrashDuringOnePhaseCommit() {
		scriptName ="ATCrashDuringOnePhaseCommit";
	}

	@Deployment(name = "xtstest", testable = false, managed = false)
	@TargetsContainer("jboss-as")
	public static Archive<?> createTestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File(xtstestWar));
		return archive;	
	}

	@Test
	public void SingleParticipantPrepareAndCommit() throws Exception {
		testName = "SingleParticipantPrepareAndCommit";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";
		Config config = new Config();
		config.add("javaVmArguments", javaVmArguments + XTSServiceTest.replace("@TestName@", testClass));

		controller.start("jboss-as", config.map());
		deployer.deploy("xtstest");

		//Waiting for crash
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

package org.jboss.jbossts.xts.crash.test.at;

import java.io.File;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.servicetests.bean.XTSServiceTestRunnerBean;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTestBase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SingleParticipantPrepareAndCommitTest extends XTSServiceTestBase {
	@Inject
	private XTSServiceTestRunnerBean testRunner;

	private String testName = 
		"org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File("../xtstest.war"));
		
		return archive;
	}

	@Test
	public void ATCrashDuringOnePhaseCommit() throws Exception {
		testRunner.runTest(testName);
	}

}

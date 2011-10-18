package org.jboss.jbossts.xts.servicetests.test;

import java.io.File;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
//import org.jboss.arquillian.framework.byteman.api.BMRule;
//import org.jboss.arquillian.framework.byteman.api.BMRules;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.servicetests.bean.XTSServiceTestRunnerBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
@BMRules(
		@BMRule(
				name = "Throw exception on success",
				targetClass = "SingleParticipantPrepareAndCommitTest",
				targetMethod = "ATCrashDuringOnePhaseCommit",
				action = "throw new java.lang.RuntimeException()")
)
*/

@RunWith(Arquillian.class)
public class SingleParticipantPrepareAndCommitTest extends XTSServiceTestBase {
	@Inject
	private XTSServiceTestRunnerBean testRunner;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File("../xtstest.war"));

		return archive;
	}

	@Test
	public void ATCrashDuringOnePhaseCommit() throws Exception {
		testRunner.runTest("SingleParticipantPrepareAndCommitTest");
	}

}

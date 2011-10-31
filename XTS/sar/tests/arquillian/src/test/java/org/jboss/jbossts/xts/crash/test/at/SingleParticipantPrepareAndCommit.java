package org.jboss.jbossts.xts.crash.test.at;

import static org.junit.Assert.*;

import java.io.File;
import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.crash.test.RenameTestLog;
import org.jboss.jbossts.xts.servicetests.bean.XTSServiceTestRunnerBean;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SingleParticipantPrepareAndCommit {

	@Inject
	private XTSServiceTestRunnerBean testRunner;

	@Deployment
	public static Archive<?> createTestArchive() {
		WebArchive archive = ShrinkWrap.
		createFromZipFile(WebArchive.class, new File("target/xtstest.war"));
		return archive;	
	}

	@BeforeClass
	public static void setUp() {
		RenameTestLog.testName = "SingleParticipantPrepareAndCommit";
	}

	@Test(expected = UndeclaredThrowableException.class)
	public void runTest() throws Exception {
		String testName = 
			"org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";

		testRunner.runTest(testName);

		fail("this should be crash by byteman script");
	}
}

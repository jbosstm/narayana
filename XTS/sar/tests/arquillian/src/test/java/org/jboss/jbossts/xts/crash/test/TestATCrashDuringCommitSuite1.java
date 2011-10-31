package org.jboss.jbossts.xts.crash.test;

import org.jboss.jbossts.xts.crash.test.at.MultiParticipantPrepareAndCommit;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	MultiParticipantPrepareAndCommit.class,
	WatchRecovery.class,
	RenameTestLog.class
})
public class TestATCrashDuringCommitSuite1 extends BaseCrashTest {
	@BeforeClass
	public static void setUp() throws Exception {
		deleteTestLog();
		copyBytemanScript("ATCrashDuringCommit.txt");
		RenameTestLog.scriptName = "ATCrashDuringCommit";
		WatchRecovery.wait_time = 8;
	}
}

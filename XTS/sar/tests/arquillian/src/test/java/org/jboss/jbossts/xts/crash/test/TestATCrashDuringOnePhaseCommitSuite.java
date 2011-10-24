package org.jboss.jbossts.xts.crash.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SingleParticipantPrepareAndCommit.class,
	WatchRecoveryATCrashDuringOnePhaseCommit.class
})

public class TestATCrashDuringOnePhaseCommitSuite {

}

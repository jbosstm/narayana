package org.jboss.jbossts.xts.crash.test;

import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WatchRecoveryATCrashDuringOnePhaseCommit {
	
	@TargetsContainer("container-at-crash-during-one-phase-commit")
	
	@Test
	public void watch() throws Exception {
		// wait recovery to happen
		Thread.sleep(3 * 60 * 1000);
	}
}

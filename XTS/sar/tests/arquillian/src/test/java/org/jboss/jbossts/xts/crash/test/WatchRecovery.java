package org.jboss.jbossts.xts.crash.test;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WatchRecovery {
	public static int wait_time = 3;
	
	@Test
	public void watch() throws Exception {
		// wait recovery to happen
		Thread.sleep(wait_time * 60 * 1000);
	}
}

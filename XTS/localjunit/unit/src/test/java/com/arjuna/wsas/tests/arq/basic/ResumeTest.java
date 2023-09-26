/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wsas.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.common.GlobalId;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ResumeTest {
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				WSASTestUtils.class);
	}
	
	@Test
	 public void testResume()
	            throws Exception
	    {
	        UserActivity ua = UserActivityFactory.userActivity();

	    try
	    {
	        GlobalId ac1 = null;
	        GlobalId ac2 = null;
	        
	        ua.start("dummy");
	        
	        ac1 = ua.activityId();
	        
	        System.out.println("Started: "+ac1);
	        
	        ua.start("dummy");
	        
	        ac2 = ua.activityId();

	        System.out.println("\nStarted: "+ac2);
	        
	        ActivityHierarchy ctx = ua.suspend();
	        
	        System.out.println("\nSuspended: "+ctx);
	        
	        if (ua.currentActivity() != null) {
	            fail("Current activity shoudl be null " + ua.currentActivity());
	        }

	        ua.resume(ctx);
	        
	        if (!ac2.equals(ua.activityId()))
	        {
	            fail("Current activity id " + ua.activityId() + " should equal " + ac2);
	        }

	        ua.end();

	        if (!ac1.equals(ua.activityId())) {
	            fail("Current activity id " + ua.activityId() + " should equal " + ac1);
	        }
	    } finally {
	        WSASTestUtils.cleanup(ua);
	    }
	    }

}

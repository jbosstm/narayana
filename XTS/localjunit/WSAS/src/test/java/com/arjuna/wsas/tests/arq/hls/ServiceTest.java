package com.arjuna.wsas.tests.arq.hls;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ServiceTest {
	@Inject
	Service test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				Service.class,
				DemoHLS.class,
				WSASTestUtils.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testService();
	}
}


package com.arjuna.wsas.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.completionstatus.Failure;
import com.arjuna.mw.wsas.status.Active;
import com.arjuna.mw.wsas.status.NoActivity;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class StatusCheckTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                WSASTestUtils.class);
    }

    @Test
    public void testStatusCheck()
            throws Exception
            {
        UserActivity ua = UserActivityFactory.userActivity();

        if (ua.status() != NoActivity.instance()) {
            fail("Status should be NoActivity " + ua.status());
        }

        ua.start("dummy");

        if (ua.status() != Active.instance()) {
            fail("Status should be Active " + ua.status());
        }
        Outcome res = ua.end();

        if (!res.completedStatus().equals(Failure.instance())) {
            fail("Completed status should be Failure " + res.completedStatus());
        }
            }
}

package com.arjuna.wsas.tests;

import com.arjuna.mw.wsas.UserActivity;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Mar 12, 2008
 * Time: 11:17:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class WSASTestUtils {
    static public void cleanup(UserActivity ua)
    {
        try {
            while (ua.currentActivity() != null) {
                ua.end();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }
}

package com.arjuna.wscf11.tests;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Mar 17, 2008
 * Time: 9:26:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class WSCF11TestUtils {
    static public void cleanup(com.arjuna.mw.wscf.model.twophase.api.UserCoordinator ua)
    {
        try {
            while (ua.currentActivity() != null) {
                ua.cancel();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }

    static public void cleanup(com.arjuna.mw.wscf.UserCoordinator ua)
    {
        try {
            while (ua.currentActivity() != null) {
                ua.end();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }

    static public void cleanup(com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager cm)
    {
        try {
            while (cm.currentActivity() != null) {
                cm.cancel();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }


}
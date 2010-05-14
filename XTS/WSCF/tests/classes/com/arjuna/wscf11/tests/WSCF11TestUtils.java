package com.arjuna.wscf11.tests;

import com.arjuna.mw.wscf.model.sagas.api.UserCoordinator;

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


    public static void cleanup(com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager cm) {
        try {
            while (cm.currentActivity() != null) {
                cm.cancel();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }

    public static void cleanup(com.arjuna.mw.wscf.model.sagas.api.UserCoordinator uc) {
        try {
            while (uc.currentActivity() != null) {
                uc.cancel();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }
}
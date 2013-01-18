/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wscf.tests;

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

    public static void cleanup(UserCoordinator uc) {
        try {
            while (uc.currentActivity() != null) {
                uc.cancel();
            }
        } catch (Exception e) {
            // do nothing -- caller will be dealing with exceptions
        }
    }
}
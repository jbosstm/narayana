/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc.tests.local;

import java.security.Permission;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class SinglePermissionSecurityManager extends SecurityManager {

    private final String permissionName;

    private boolean called = false;

    public SinglePermissionSecurityManager(final String permissionName) {
        if (permissionName == null) {
            throw new IllegalArgumentException("permissionName cannot be null");
        }

        this.permissionName = permissionName;
    }

    @Override
    public synchronized void checkPermission(final Permission permission) {
        if (permission instanceof RuntimePermission && permissionName.equals(permission.getName())) {
            called = true;
        }
    }

    public boolean wasCalled() {
        return called;
    }

}
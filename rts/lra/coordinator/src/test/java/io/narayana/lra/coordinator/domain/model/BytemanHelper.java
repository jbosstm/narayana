/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.lra.coordinator.domain.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BytemanHelper {
    public void abortLRA(LongRunningAction lra) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Method method = lra.getClass().getDeclaredMethod("abortLRA");
        method.setAccessible(true);
        method.invoke(lra);
    }
}

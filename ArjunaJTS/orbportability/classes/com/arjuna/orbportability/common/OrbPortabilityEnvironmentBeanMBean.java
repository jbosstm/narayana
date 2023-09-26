/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.orbportability.common;

import java.util.List;
import java.util.Map;

/**
 * A JMX MBean interface containing assorted configuration for the Orb Portability layer.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface OrbPortabilityEnvironmentBeanMBean
{
    String getInitialReferencesRoot();

    String getInitialReferencesFile();

    String getFileDir();

    String getResolveService();

    List<String> getEventHandlerClassNames();

    String getOrbImpleClassName();

    String getPoaImpleClassName();

    String getOrbDataClassName();

    String getBindMechanism();

    public Map<String, String> getOrbInitializationProperties();
}
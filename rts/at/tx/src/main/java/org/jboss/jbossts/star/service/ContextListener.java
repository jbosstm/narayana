/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.service;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
       public void contextInitialized(ServletContextEvent event) {
//      String value = event.getServletContext().getInitParameter("resteasy.servlet.mapping.prefix");
        System.setProperty("resttx.context.path", event.getServletContext().getContextPath());
    }

public void contextDestroyed(ServletContextEvent event) {
    }
}
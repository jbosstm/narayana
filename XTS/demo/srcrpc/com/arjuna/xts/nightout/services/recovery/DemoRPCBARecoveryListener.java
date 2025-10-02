package com.arjuna.xts.nightout.services.recovery;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Listener to register and unregister teh XTS application specific listener -- we have to
 * use this because JBossWS does not currently honour the @PostConstruct and @PreDestroy
 * lifecycle annotations on web services
 */
public class DemoRPCBARecoveryListener implements ServletContextListener
{

    public void contextInitialized(ServletContextEvent event) {
        DemoRPCBARecoveryModule.register();
    }

    public void contextDestroyed(ServletContextEvent event) {
        DemoRPCBARecoveryModule.unregister();
    }
}
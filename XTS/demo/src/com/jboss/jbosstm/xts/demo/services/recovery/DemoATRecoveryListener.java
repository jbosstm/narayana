package com.jboss.jbosstm.xts.demo.services.recovery;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Listener to register and unregister teh XTS application specific listener -- we have to
 * use this because JBossWS does not currently honour the @PostConstruct and @PreDestroy
 * lifecycle annotations on web services
 */
public class DemoATRecoveryListener implements ServletContextListener
{

    public void contextInitialized(ServletContextEvent event) {
        DemoATRecoveryModule.register();
    }

    public void contextDestroyed(ServletContextEvent event) {
        DemoATRecoveryModule.unregister();
    }
}

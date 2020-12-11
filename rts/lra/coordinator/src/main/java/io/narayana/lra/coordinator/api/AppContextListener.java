package io.narayana.lra.coordinator.api;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.lra.coordinator.internal.Implementations;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// start the recovery manager early so that we can start recovering in doubt LRAs
@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Implementations.install();
        RecoveryManager.manager();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Implementations.uninstall();
    }
}
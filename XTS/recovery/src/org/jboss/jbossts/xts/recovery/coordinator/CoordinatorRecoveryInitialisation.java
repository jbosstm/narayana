package org.jboss.jbossts.xts.recovery.coordinator;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import org.jboss.jbossts.xts.environment.RecoveryEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;
import org.jboss.jbossts.xts.recovery.logging.RecoveryLogger;
import org.jboss.jbossts.xts.recovery.XTSRecoveryModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * manager allowing XTS coordinator recovery modules to be registered at startup and unregistered at shutdown.
 */
public class CoordinatorRecoveryInitialisation
{
    /**
     * the list of XTS recovery modules actually registered with the JBossTS recovery manager
     */
    private static List<XTSRecoveryModule> recoveryModules = new ArrayList<XTSRecoveryModule>();

    /**
     * flag to identify if we have already been initialised
     */

    private static boolean initialised = false;
    /**
     * initialisation routine which registers all configured XTS recovery modules
     */
    public static void startup()
    {
        if (initialised) {
            return;
        }

        RecoveryEnvironmentBean recoveryEnvironmentBean = XTSPropertyManager.getRecoveryEnvironmentBean();

        List<String> modules = recoveryEnvironmentBean.getCoordinatorRecoveryModules();
        Iterator<String> iterator = modules.iterator();

        while (iterator.hasNext())
        {
            String className = (String) iterator.next();
            Class<?> clazz = null;

            try {
                clazz = CoordinatorRecoveryInitialisation.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                RecoveryLogger.i18NLogger.error_recovery_coordinator_CoordinatorRecoveryInitialisation_1(className, cnfe);
                continue;
            }

            if (!RecoveryModule.class.isAssignableFrom(clazz)) {
                RecoveryLogger.i18NLogger.error_recovery_coordinator_CoordinatorRecoveryInitialisation_2(className);
                continue;
            }
            
            try {
                XTSRecoveryModule module = (XTSRecoveryModule)clazz.newInstance();
                module.install();
                RecoveryManager.manager().addModule(module);
                recoveryModules.add(module);
            } catch (InstantiationException ie) {
                RecoveryLogger.i18NLogger.error_recovery_coordinator_CoordinatorRecoveryInitialisation_3(className, ie);
            } catch (IllegalAccessException iae) {
                RecoveryLogger.i18NLogger.error_recovery_coordinator_CoordinatorRecoveryInitialisation_4(className, iae);
            }
        }

        initialised = true;
    }

    /**
     * shutdown routine which removes all installed recovery modules
     */
    public static void shutdown()
    {
        if (!initialised) {
            return;
        }
        
        Iterator<XTSRecoveryModule> iterator = recoveryModules.iterator();

        while (iterator.hasNext()) {
            XTSRecoveryModule module = iterator.next();
            RecoveryManager.manager().removeModule(module, true);
            module.uninstall();
        }

        recoveryModules.clear();
        
        initialised = false;
    }
}

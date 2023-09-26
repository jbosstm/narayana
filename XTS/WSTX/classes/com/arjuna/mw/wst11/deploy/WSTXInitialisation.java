/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wst11.deploy;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wst11.*;
import com.arjuna.webservices.util.ClassLoaderHelper;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Initialise WSTX.
 * @author kevin
 */
public class WSTXInitialisation
{
    private static boolean initialised = false;

    /**
     * The context has been initialized.
     *
     */
    public static void startup()
    {
        if (initialised) {
            return;
        }
        try
        {
            configure();
            initialised = true;
        }
        catch (Exception exception) {
            wstxLogger.i18NLogger.error_mw_wst11_deploy_WSTXI_1(exception);
        }
        catch (Error error)
        {
            wstxLogger.i18NLogger.error_mw_wst11_deploy_WSTXI_1(error);
        }
    }

    /**
     * Configure all configured WSTX client and participant implementations.
     *
     */
    private static void configure()
        throws Exception
    {
        WSTEnvironmentBean wstEnvironmentBean = XTSPropertyManager.getWSTEnvironmentBean();
        final String userTx = wstEnvironmentBean.getUserTransaction11();
        final String txManager = wstEnvironmentBean.getTransactionManager11();
        final String userBa = wstEnvironmentBean.getUserBusinessActivity11();
        final String baManager = wstEnvironmentBean.getBusinessActivityManager11();

        // we only load classes which have been configured
        
        if (userTx != null) {
            UserTransaction.setUserTransaction((UserTransaction)ClassLoaderHelper.forName(WSTXInitialisation.class, userTx).newInstance()) ;
        }
        if (txManager != null) {
            TransactionManager.setTransactionManager((TransactionManager)ClassLoaderHelper.forName(WSTXInitialisation.class, txManager).newInstance()) ;
        }
        if (userBa != null) {
            UserBusinessActivity.setUserBusinessActivity((UserBusinessActivity)ClassLoaderHelper.forName(WSTXInitialisation.class, userBa).newInstance()) ;
        }
        if (baManager != null) {
            BusinessActivityManager.setBusinessActivityManager((BusinessActivityManager)ClassLoaderHelper.forName(WSTXInitialisation.class, baManager).newInstance());
        }
    }

    /**
     * The context is about to be destroyed.
     */
    public static void shutdown()
    {
    }
}
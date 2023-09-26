/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance;

import org.jboss.jbossts.qa.performance.products.TxWrapper;

public abstract class PerformanceTest
{
    private String[]    _configs = null;
    private String[]    _params = null;
    private static String PRODUCT_CLASS_PARAM = "productClass=";
    private String wrapperClass = "org.jboss.jbossts.qa.performance.products.JBossTSTxWrapper";
    private TxWrapper wrapper;

    protected abstract void work() throws Exception;

    protected boolean requiresNestedTxSupport()
    {
        return false;
    }

    public void setServiceConfigs( String[] configs )
    {
        _configs = configs;
    }

    void setParameters(String[] params)
    {
        _params = params;

        for (String param : _params)
        {
            if (param.startsWith(PRODUCT_CLASS_PARAM))
            {
                wrapperClass = param.substring(PRODUCT_CLASS_PARAM.length());
                break;
            }
        }
    }

    public boolean isParameterDefined(String param)
    {
        for (int count=0;count<_params.length;count++)
        {
            if ( param.equals( _params[count] ) )
            {
                return true;
            }
        }

        return false;
    }

    public String[] getParameters()
    {
        return _params;
    }

    public String getServiceConfig( int index )
    {
        return _configs[index];
    }

    public TxWrapper getTxWrapper()
    {
        return wrapper.createWrapper();
    }

    private void initTest() throws Exception
    {
        try
        {
            wrapper = (TxWrapper) Class.forName(wrapperClass).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unable to instantiate transaction class: " + e);
        }

        if (requiresNestedTxSupport() && !wrapper.supportsNestedTx())
            throw new IllegalArgumentException("Product " + wrapper.getName() + " does not support nested transactions");
    }

    public void performWork( int numberOfIterations ) throws Exception
    {
        initTest();

        for (int count=0;count<numberOfIterations;count++)
        {
            work();
        }
    }
}
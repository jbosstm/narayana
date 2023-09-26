/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.spi.util;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.jdbc.TransactionalDriver;
import io.narayana.spi.InitializationException;

import javax.naming.*;
import java.util.*;

public class DataSourceManagerImpl {

    public void registerDataSource(String binding, String driver, String databaseUrl, String userName, String password) throws InitializationException {
        registerDataSource(binding, driver, databaseUrl, null, -1, userName, password);
    }

    public void registerDataSource(String binding, String driver, String databaseName, String host, long port, String userName, String password) throws InitializationException {
        XADSWrapper xaDataSourceToBind;
        Properties p = System.getProperties();
        String driversProp = (String) p.get("jdbc.drivers");

        if (driversProp == null)
            p.put("jdbc.drivers", driver);
        else
            p.put("jdbc.drivers", driversProp + ":" + driver);

        p.put(TransactionalDriver.userName, userName);
        p.put(TransactionalDriver.password, password);

        System.setProperties(p);

        try {
            xaDataSourceToBind = XADSWrapperObjectFactory.getXADataSource(binding, driver, databaseName, host, (int) port, userName, password);
        } catch (Exception e) {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.info("Cannot bind " + databaseName + " for driver " + driver);

            throw new InitializationException("Cannot bind " + databaseName + " for driver " + driver, e);
        }

        try
        {
            // initJndi should have already been called resulting in suitable java.naming.provider.url and java.naming.factory.initial
            // system properties being set.
            new InitialContext().rebind(binding, xaDataSourceToBind);
        }
        catch (Exception e)
        {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.infof("%s: Cannot bind datasource into JNDI: %s", binding, e.getMessage());

            throw new InitializationException(binding + ": Cannot bind datasource into JNDI", e);
        }
    }
}
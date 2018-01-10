/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.db;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
public class DBAllocator extends Allocator {
    private static final Logger LOGGER = Logger.getLogger(DBAllocator.class.getName());

    DBAllocator() {
        // Use getInstance
    }

    public DB allocateDB(final int expiryMinutes) {
        // Something between nothing and a business week...
        if (expiryMinutes < 1 || expiryMinutes > 7200) {
            throw new IllegalArgumentException("expiryMinutes out of range 1 - 7200");
        }

        final String projectBuildDirectory = getProp("project.build.directory");
        final File driversDir = new File(projectBuildDirectory);
        if (!driversDir.exists()) {
            throw new IllegalArgumentException(driversDir.getAbsolutePath() + " must exist. Check project.build.directory Maven property.");
        }

        // Timeout for fetching DB driver
        final long driverFetchTimeoutSeconds = Long.parseLong(getProp("dballocator.driver.url.timeout.seconds"));
        if (driverFetchTimeoutSeconds < 0 || driverFetchTimeoutSeconds > 1800) {
            throw new IllegalArgumentException("dballocator.driver.url.timeout.seconds out of range 0 - 1800");
        }

        // Fetching driver
        final String dballocatorDriverUrl = getProp("dballocator.driver.url");
        final URL driverJarSrc;
        try {
            driverJarSrc = new URL(dballocatorDriverUrl);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Check dballocator.driver.url property.", e);
            return null;
        }
        final String[] driverJarFilename = dballocatorDriverUrl.split("/");
        final File driverJarDst = new File(driversDir, driverJarFilename[driverJarFilename.length - 1]);
        long t = System.currentTimeMillis();
        // < 1K is a suspicious DB driver jar...
        while (!fileOK(1024, driverJarDst) && ((System.currentTimeMillis() - t) / 1000 < driverFetchTimeoutSeconds)) {
            try {
                LOGGER.finest("Downloading DB driver from " + driverJarSrc.getPath());
                FileUtils.copyURLToFile(driverJarSrc, driverJarDst);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, String.format("Check %s and %s.", driverJarSrc, driverJarDst), e);
            }
            if (!fileOK(1024, driverJarDst)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.SEVERE, "Interrupted.", e);
                }
            }
        }
        if (!fileOK(1024, driverJarDst)) {
            LOGGER.severe(String.format("Failed to download DB driver from %s:%s%s", driverJarSrc.getHost(), driverJarSrc.getPort(), driverJarSrc.getPath()));
            return null;
        }

        // Timeout for talking to DBAllocator, i.e. waiting for the database
        if (StringUtils.isEmpty(System.getProperty("dballocator.timeout.minutes"))) {
            throw new IllegalArgumentException("dballocator.timeout.minutes must not be empty");
        }
        final long dbAllocTimeoutMinutes = Long.parseLong(System.getProperty("dballocator.timeout.minutes"));
        if (dbAllocTimeoutMinutes < 0 || dbAllocTimeoutMinutes > 1440) {
            throw new IllegalArgumentException("dbAllocTimeoutMinutes out of range 0 - 1440");
        }

        // Talking to DBAllocator
        final String dballocatorHostPort = getProp("dballocator.host.port");
        final String dballocatorExpression = getProp("dballocator.expression");
        final String dballocatorRequestee = getProp("dballocator.requestee");
        final URL allocate;
        try {
            allocate = new URL(String.format("%s/Allocator/AllocatorServlet?operation=allocate&expression=%s&requestee=%s&expiry=%d",
                    dballocatorHostPort,
                    dballocatorExpression,
                    dballocatorRequestee,
                    expiryMinutes
            ));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Check dballocator.host.port property.", e);
            return null;
        }
        final File dbAllocatorPropertiesFile = new File(driversDir, "dballocator.properties");
        t = System.currentTimeMillis();
        // Smaller DBAllocator properties file than 400 bytes is definitely wrong.
        while (!fileOK(400, dbAllocatorPropertiesFile) && ((System.currentTimeMillis() - t) / 1000 / 60 < dbAllocTimeoutMinutes)) {
            try {
                LOGGER.info("Allocating database...");
                FileUtils.copyURLToFile(allocate, dbAllocatorPropertiesFile);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, String.format("Check source URL %s and destination file %s.", allocate.toString(), dbAllocatorPropertiesFile.getAbsolutePath()), e);
            }
            if (!fileOK(400, dbAllocatorPropertiesFile)) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.SEVERE, "Interrupted.", e);
                }
            }
        }
        if (!fileOK(400, dbAllocatorPropertiesFile)) {
            LOGGER.severe("Failed to allocate database in time with " + allocate.getPath());
            return null;
        }

        // We should have the DB allocated at this point
        final Properties dbaprops = new Properties();
        try (final InputStream i = new FileInputStream(dbAllocatorPropertiesFile)) {
            dbaprops.load(i);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Properties file %s doesn't exist, we cannot continue.", dbAllocatorPropertiesFile.getAbsolutePath()), e);
            return null;
        }
        dbaprops.forEach((k, v) -> LOGGER.fine(String.format("%s : %s", k, v)));

        // Construct DB
        return new DB.Builder()
                .dsType(dbaprops.getProperty("datasource.class.xa"))
                .dsUsername(dbaprops.getProperty("db.username"))
                .dsUser(dbaprops.getProperty("db.username"))
                .dsPassword(dbaprops.getProperty("db.password"))
                .dsDbName(dbaprops.getProperty("db.name"))
                .dsDbPort(dbaprops.getProperty("db.port"))
                .dsDbHostname(dbaprops.getProperty("db.hostname"))
                .dsUrl(dbaprops.getProperty("db.jdbc_url"))
                .dsLoginTimeout("0")
                .dsFactory(dbaprops.getProperty("datasource.class.xa") + "Factory")
                .dsDriverClassName(dbaprops.getProperty("db.jdbc_class"))
                .tdsType("javax.sql.DataSource")
                .tdsUrl("jdbc:arjuna:java:comp/env")
                .tdsDriverClassName("com.arjuna.ats.jdbc.TransactionalDriver")
                .dbDriverArtifact(driverJarDst.getAbsolutePath())
                .allocationProperties(dbaprops)
                .build();
    }

    public DB allocateDB() {
        return allocateDB(Integer.parseInt(getProp("dballocator.expiry.minutes")));
    }

    public boolean deallocateDB(final DB db) {
        if (db == null) {
            throw new IllegalArgumentException("db must not be null");
        }
        final String dballocatorHostPort = getProp("dballocator.host.port");
        if (db.allocationProperties == null) throw new IllegalArgumentException("db.allocationProperties must not be null");
        if (StringUtils.isEmpty(db.allocationProperties.getProperty("uuid"))) {
            throw new IllegalArgumentException("uuid must not be empty");
        }
        final URL dealloc;
        try {
            dealloc = new URL(String.format("%s/Allocator/AllocatorServlet?operation=dealloc&uuid=%s",
                    dballocatorHostPort,
                    db.allocationProperties.getProperty("uuid")
            ));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Failed to construct deallocate DB URL.", e);
            return false;
        }
        LOGGER.info("Deallocating database...");
        final int deallocstatusCode;
        try {
            deallocstatusCode = ((HttpURLConnection) dealloc.openConnection()).getResponseCode();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to deallocate DB.", e);
            return false;
        }
        LOGGER.info(String.format("Deallocate DB returned HTTP status code: %d", deallocstatusCode));
        return deallocstatusCode == 200;
    }

    public boolean reallocateDB(final int expiryMinutes, final DB db) {
        // Something between nothing and a business week...
        if (expiryMinutes < 0 || expiryMinutes > 7200) {
            throw new IllegalArgumentException("expiryMinutes out of range 0 - 7200");
        }
        final String dballocatorHostPort = getProp("dballocator.host.port");
        if (db.allocationProperties == null) {
            throw new IllegalArgumentException("db.allocationProperties must not be null");
        }
        if (StringUtils.isEmpty(db.allocationProperties.getProperty("uuid"))) {
            throw new IllegalArgumentException("uuid must not be empty");
        }
        final URL realloc;
        try {
            realloc = new URL(String.format("%s/Allocator/AllocatorServlet?operation=realloc&uuid=%s&expiry=%s",
                    dballocatorHostPort,
                    db.allocationProperties.getProperty("uuid"),
                    System.getProperty("dballocator.expiry.minutes")
            ));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Failed to construct reallocate DB URL.", e);
            return false;
        }
        LOGGER.info("Reallocating database...");
        final int reallocstatusCode;
        try {
            reallocstatusCode = ((HttpURLConnection) realloc.openConnection()).getResponseCode();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to reallocate DB.", e);
            return false;
        }
        LOGGER.info(String.format("Reallocate DB returned HTTP status code: %d", reallocstatusCode));
        return reallocstatusCode == 200;
    }

    public boolean reallocateDB(final DB db) {
        if (db == null) {
            throw new IllegalArgumentException("db must not be null");
        }
        return reallocateDB(Integer.parseInt(getProp("dballocator.expiry.minutes")), db);
    }

    public boolean cleanDB(final DB db) {
        if (db == null) {
            throw new IllegalArgumentException("db must not be null");
        }
        if (db.allocationProperties == null) {
            throw new IllegalArgumentException("db.allocationProperties must not be null");
        }
        final String dballocatorHostPort = getProp("dballocator.host.port");
        if (StringUtils.isEmpty(db.allocationProperties.getProperty("uuid"))) {
            throw new IllegalArgumentException("uuid must not be empty");
        }
        final URL clean;
        try {
            clean = new URL(String.format("%s/Allocator/AllocatorServlet?operation=erase&uuid=%s",
                    dballocatorHostPort,
                    db.allocationProperties.getProperty("uuid")
            ));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Failed to construct clean DB URL.", e);
            return false;
        }
        LOGGER.info("Cleaning database...");
        final int statusCode;
        try {
            statusCode = ((HttpURLConnection) clean.openConnection()).getResponseCode();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to clean DB.", e);
            return false;
        }
        LOGGER.info(String.format("Clean DB returned HTTP status code: %d", statusCode));
        return statusCode == 200;
    }
}

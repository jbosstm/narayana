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

/**
 * This is a remote database the TS does not have any control over.
 * The database is always present, always allocated.
 *
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
public class CIPostgreAllocator extends Allocator {

    CIPostgreAllocator() {
        // Use getInstance
    }

    public DB allocateDB(final int expiryMinutes) {
        final String versionPostgreSQLDriver = getProp("version.postgresql");
        final String user = getProp("pgsql.user");
        final String password = getProp("pgsql.password");
        final String servername = getProp("pgsql.servername");
        final String portnumber = getProp("pgsql.portnumber");
        final int port = Integer.parseInt(portnumber);
        if (port > 65535 || port < 1025) {
            throw new IllegalArgumentException("pgsql.portnumber out of expected range [1025, 65535]");
        }
        final String databasename = getProp("pgsql.databasename");

        return new DB.Builder()
                .dsType("org.postgresql.xa.PGXADataSource")
                .dsUsername(user)
                .dsUser(user)
                .dsPassword(password)
                .dsDbName(databasename)
                .dsDbPort(String.valueOf(port))
                .dsDbHostname(servername)
                .dsUrl(String.format("jdbc:postgresql://%s:%d/%s", servername, port, databasename))
                .dsLoginTimeout("0")
                .dsFactory("org.postgresql.xa.PGXADataSourceFactory")
                .tdsType("javax.sql.XADataSource")
                .dbDriverArtifact("postgresql:postgresql:" + versionPostgreSQLDriver)
                .build();
    }

    public DB allocateDB() {
        return allocateDB(0);
    }

    public boolean deallocateDB(final DB db) {
        // Intentionally nothing
        return true;
    }

    public boolean reallocateDB(final int expiryMinutes, final DB db) {
        // Intentionally nothing
        return true;
    }

    public boolean reallocateDB(final DB db) {
        // Intentionally nothing
        return true;
    }

    public boolean cleanDB(final DB db) {
        // Intentionally nothing
        return true;
    }
}

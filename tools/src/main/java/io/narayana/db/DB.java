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

import java.util.Properties;

/**
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
public class DB {
    public final String dsType;
    public final String dsUsername;
    public final String dsUser;
    public final String dsPassword;
    public final String dsDbName;
    public final String dsDbPort;
    public final String dsDbHostname;
    public final String dsUrl;
    public final String dsLoginTimeout;
    public final String dsFactory;
    public final String dsDriverClassName;
    public final String tdsType;
    public final String tdsUrl;
    public final String tdsDriverClassName;
    public final String dbDriverArtifact;
    public final Properties allocationProperties;

    public static class Builder {
        private String dsType;
        private String dsUsername;
        private String dsUser;
        private String dsPassword;
        private String dsDbName;
        private String dsDbPort;
        private String dsDbHostname;
        private String dsUrl;
        private String dsLoginTimeout;
        private String dsFactory;
        private String dsDriverClassName;
        private String tdsType;
        private String tdsUrl;
        private String tdsDriverClassName;
        private String dbDriverArtifact;
        private Properties allocationProperties;

        Builder dsType(String dsType) {
            this.dsType = dsType;
            return this;
        }

        Builder dsUsername(String dsUsername) {
            this.dsUsername = dsUsername;
            return this;
        }

        Builder dsUser(String dsUser) {
            this.dsUser = dsUser;
            return this;
        }

        Builder dsPassword(String dsPassword) {
            this.dsPassword = dsPassword;
            return this;
        }

        Builder dsDbName(String dsDbName) {
            this.dsDbName = dsDbName;
            return this;
        }

        Builder dsDbPort(String dsDbPort) {
            this.dsDbPort = dsDbPort;
            return this;
        }

        Builder dsDbHostname(String dsDbHostname) {
            this.dsDbHostname = dsDbHostname;
            return this;
        }

        Builder dsUrl(String dsUrl) {
            this.dsUrl = dsUrl;
            return this;
        }

        Builder dsLoginTimeout(String dsLoginTimeout) {
            this.dsLoginTimeout = dsLoginTimeout;
            return this;
        }

        Builder dsFactory(String dsFactory) {
            this.dsFactory = dsFactory;
            return this;
        }

        Builder dsDriverClassName(String dsDriverClassName) {
            this.dsDriverClassName = dsDriverClassName;
            return this;
        }

        Builder tdsType(String tdsType) {
            this.tdsType = tdsType;
            return this;
        }

        Builder tdsUrl(String tdsUrl) {
            this.tdsUrl = tdsUrl;
            return this;
        }

        Builder tdsDriverClassName(String tdsDriverClassName) {
            this.tdsDriverClassName = tdsDriverClassName;
            return this;
        }

        Builder dbDriverArtifact(String dbDriverArtifact) {
            this.dbDriverArtifact = dbDriverArtifact;
            return this;
        }

        Builder allocationProperties(Properties allocationProperties) {
            this.allocationProperties = allocationProperties;
            return this;
        }

        DB build() {
            return new DB(this);
        }
    }

    private DB(Builder builder) {
        this.dsType = builder.dsType;
        this.dsUsername = builder.dsUsername;
        this.dsUser = builder.dsUser;
        this.dsPassword = builder.dsPassword;
        this.dsDbName = builder.dsDbName;
        this.dsDbPort = builder.dsDbPort;
        this.dsDbHostname = builder.dsDbHostname;
        this.dsUrl = builder.dsUrl;
        this.dsLoginTimeout = builder.dsLoginTimeout;
        this.dsFactory = builder.dsFactory;
        this.dsDriverClassName = builder.dsDriverClassName;
        this.tdsType = builder.tdsType;
        this.tdsUrl = builder.tdsUrl;
        this.tdsDriverClassName = builder.tdsDriverClassName;
        this.dbDriverArtifact = builder.dbDriverArtifact;
        this.allocationProperties = builder.allocationProperties;
    }
}

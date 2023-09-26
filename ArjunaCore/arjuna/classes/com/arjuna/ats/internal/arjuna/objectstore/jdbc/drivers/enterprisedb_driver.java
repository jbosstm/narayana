/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;


/**
 * JDBC store implementation driver-specific code.
 *
 * This version for Postgres Plus DB, this string is now "EnterpriseDB JDBC Driver"
 * (at least from driver version 42.2.12.3)
 * which results in searching for driver class `enterprisedb_driver`.
 */
public class enterprisedb_driver extends postgres_driver {
}
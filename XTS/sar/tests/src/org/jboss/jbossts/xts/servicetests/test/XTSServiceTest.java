/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2009,
 * @author JBoss Inc.
 */

package org.jboss.jbossts.xts.servicetests.test;

/**
 * API implemented by classes which are used to execute a specific XTS service test
 */
public interface XTSServiceTest
{
    public void run();
    public boolean isSuccessful();
    public Exception getException();

    // System properties which can be set to configure the behaviour of tests

    public static final String SERVICE_URL1_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL1";
    public static final String SERVICE_URL2_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL2";
    public static final String SERVICE_URL3_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL3";
    public static final String SUBORDINATE_SERVICE_URL1_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.serviceURL1";
    public static final String SUBORDINATE_SERVICE_URL2_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.sserviceURL2";
    public static final String SUBORDINATE_SERVICE_URL3_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.sserviceURL3";
}

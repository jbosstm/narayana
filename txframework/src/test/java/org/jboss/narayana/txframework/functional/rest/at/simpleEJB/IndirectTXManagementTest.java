/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.impl.handlers.restat.client.UserTransaction;
import org.jboss.narayana.txframework.impl.handlers.restat.client.UserTransactionFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author paul.robinson@redhat.com 06/04/2012
 */
@RunWith(Arquillian.class)
public class IndirectTXManagementTest {

    private Service1 service1;
    private Service2 service2;

    private UserTransaction ut;

    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 8080;
    private static final String SERVICE_URL = "http://localhost:" + SERVICE_PORT + "/test";

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(ClientTXInterceptorTest.class.getPackage())
                .addPackage(EventLog.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsWebInfResource("resttx.ejb.web.xml", "web.xml");
        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.resteasy.resteasy-jaxrs,javax.ws.rs.api,javax.ejb.api,org.jboss.jts,org.jboss.narayana.txframework\n";
        archive.setManifest(new StringAsset(ManifestMF));

        return archive;

    }

    @Before
    public void setupTest() throws Exception {

        ut = UserTransactionFactory.userTransaction();
        service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        service2 = ProxyFactory.create(Service2.class, SERVICE_URL);
    }

    @After
    public void teardownTest() throws Exception {
        //assertDataAvailable();
        service1.clearLogs();
        rollbackIfActive(ut);
    }

    @Test
    public void clientDrivenCommitTest() throws Exception {

        UserTransaction ut = UserTransactionFactory.userTransaction();

        ut.begin();

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service2.VOTE_COMMIT);
        response.releaseConnection();

        ut.commit();

        assertOrder(Prepare.class, Prepare.class, Commit.class, Commit.class);
    }

    @Test
    public void multipleInvokeTest() throws Exception {

        UserTransaction ut = UserTransactionFactory.userTransaction();

        ut.begin();

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service2.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service2.VOTE_COMMIT);
        response.releaseConnection();

        ut.commit();

        assertOrder(Prepare.class, Prepare.class, Commit.class, Commit.class);
    }

    @Test
    public void clientDrivenRollbackTest() throws Exception {

        UserTransaction ut = UserTransactionFactory.userTransaction();

        ut.begin();

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        ut.rollback();

        assertOrder(Rollback.class, Rollback.class);
    }

    @Test
    public void participantDrivenRollbackTest() throws Exception {

        UserTransaction ut = UserTransactionFactory.userTransaction();

        ut.begin();

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service1.VOTE_ROLLBACK);
        response.releaseConnection();

        ut.commit();

        //todo: is this right? Why not two prepares?
        assertOrder(Prepare.class, Rollback.class, Rollback.class);
    }

    private void assertOrder(Class<? extends Annotation>... expectedOrder) {

        ClientResponse response = (ClientResponse<EventLog>) service1.getEventLog();
        String eventLog = (String) response.getEntity(String.class);
        Assert.assertEquals(EventLog.asString(Arrays.asList(expectedOrder)), eventLog);
    }

    public void rollbackIfActive(UserTransaction ut) {

        try {
            ut.rollback();
        } catch (Throwable th2) {
            // do nothing, not active
        }
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.eclipse.microprofile.lra.tck;

import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.client.LRAClient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RunTck implements ServletContextListener {
    private static final String RUN_TCK_PROP = "lra.tck.run";
    private static final String EXIT_AFTER_TCK_PROP = "lra.tck.exit";
    private static final long DELAY_TCK_RUN = 5L; // wait for all resource to deploy

    private ScheduledFuture<?> timer;
    private ScheduledExecutorService scheduler;

    private LRAClient lraClient;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if (Boolean.getBoolean(RUN_TCK_PROP)) {
            System.out.printf("Waiting %d seconds for the TCK to deploy ...%n", DELAY_TCK_RUN);

            scheduler = Executors.newScheduledThreadPool(1);

            timer = scheduler.schedule((Runnable) this::runTck, DELAY_TCK_RUN, TimeUnit.SECONDS);
        }
    }

    private void runTck() {
        if (lraClient == null) {
            try {
                lraClient = new NarayanaLRAClient();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        TckTests.beforeClass(lraClient);
        TckTests test = new TckTests();

        test.before();

        TckResult results = test.runTck(lraClient, "all", false);

        test.after();

        List<String> failures = results.getFailures();
        int exitStatus = 0;

        if (failures.size() != 0) {
            System.out.printf("There were TCK failures:%n");

            failures.forEach(f -> System.out.printf("%s%n", f));

            exitStatus = 1;
        }

        if (Boolean.getBoolean(EXIT_AFTER_TCK_PROP)) {
            System.exit(exitStatus);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (timer != null) {
            timer.cancel(true);
            timer = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }
}

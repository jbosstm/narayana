/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Originally copied from https://github.com/quarkusio/quarkus/blob/1.8.1.Final/core/deployment/src/main/java/io/quarkus/runner/bootstrap/ForkJoinClassLoading.java
 */
@ApplicationScoped
public class ForkJoinClassLoading {

    private static final Logger log = Logger.getLogger(ForkJoinClassLoading.class.getName());

    public void setForkJoinClassLoader(@Observes @Initialized(ApplicationScoped.class) Object startup) {
        CountDownLatch allDone = new CountDownLatch(ForkJoinPool.getCommonPoolParallelism());
        CountDownLatch taskRelease = new CountDownLatch(1);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for (int i = 0; i < ForkJoinPool.getCommonPoolParallelism(); ++i) {
            ForkJoinPool.commonPool().execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    allDone.countDown();
                    try {
                        taskRelease.await();
                    } catch (InterruptedException e) {
                        log.error("Failed to set fork join ClassLoader", e);
                    }
                }
            });
        }
        try {
            if (!allDone.await(1, TimeUnit.SECONDS)) {
                log.error("Timed out trying to set fork join ClassLoader");
            }
        } catch (InterruptedException e) {
            log.error("Failed to set fork join ClassLoader", e);
        } finally {
            taskRelease.countDown();
        }
    }
}

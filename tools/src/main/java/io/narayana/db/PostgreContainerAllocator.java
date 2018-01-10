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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
public class PostgreContainerAllocator extends Allocator {
    private static final Logger LOGGER = Logger.getLogger(PostgreContainerAllocator.class.getName());

    private final DockerClient dockerClient;

    PostgreContainerAllocator() {
        // Use getInstance
        final String containerDockerDaemonApiUrl = getProp("container.docker.daemon.api.url");
        final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(containerDockerDaemonApiUrl)
                .build();
        final DockerCmdExecFactory dockerCmdExecFactory = new NettyDockerCmdExecFactory()
                // The TS is not secured to control remote daemons, so 1000ms is ample for a local one.
                .withConnectTimeout(1000);
        dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }

    public DB allocateDB(final int expiryMinutes) {
        final String projectBuildDirectory = getProp("project.build.directory");
        final File driversDir = new File(projectBuildDirectory);
        if (!driversDir.exists()) {
            throw new IllegalArgumentException(driversDir.getAbsolutePath() + " must exist");
        }
        final String containerDatabaseImage = getProp("container.database.image");
        final String containerName = getProp("container.name");
        final String containerDatabaseUsername = getProp("container.database.username");
        final String containerDatabasePassword = getProp("container.database.password");
        final String containerDatabaseName = getProp("container.database.name");
        final String containerDatabaseBindHostIp = getProp("container.database.bind.host.ip");
        final String containerDatabaseBindHostPort = getProp("container.database.bind.host.port");
        final int port = Integer.parseInt(containerDatabaseBindHostPort);
        if (port > 65535 || port < 1025) {
            throw new IllegalArgumentException("container.database.bind.host.port out of expected range [1025, 65535]");
        }
        final String containerTimeoutWaitingForTcp = getProp("container.timeout.waiting.for.tcp");
        final long timeout = Long.parseLong(containerTimeoutWaitingForTcp);
        if (timeout > TimeUnit.MINUTES.toMillis(30) || timeout < 500) {
            throw new IllegalArgumentException("container.timeout.waiting.for.tcp out of expected range [500, 30*60*1000]");
        }
        final String containerDatabaseDriverArtifact = getProp("container.database.driver.artifact");
        final String containerDatabaseDriverClass = getProp("container.database.driver.class");
        final String containerDatabaseDatasourceClassXa = getProp("container.database.datasource.class.xa");

        final AtomicBoolean completed = new AtomicBoolean(false);

        try {
            dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                    .filter(c -> ArrayUtils.contains(c.getNames(), "/" + containerName)).forEach(c -> {
                final InspectContainerResponse.ContainerState s = Objects.requireNonNull(dockerClient.inspectContainerCmd(c.getId()).exec().getState(),
                        "Something went terribly wrong with container inspection.");
                if (s.getRunning() || s.getRestarting()) {
                    LOGGER.info("Killing container.");
                    dockerClient.killContainerCmd(c.getId()).exec();
                }
                LOGGER.info("Removing container.");
                dockerClient.removeContainerCmd(c.getId()).exec();
            });


            final String[] imageAndTag = containerDatabaseImage.split(":");
            if (imageAndTag.length != 2) {
                throw new IllegalArgumentException(
                        String.format("Due to the API limitation, container.database.image is expected in format image:tag, i.e. exactly one colon. It was: %s",
                                containerDatabaseImage));
            }

            dockerClient.pullImageCmd(imageAndTag[0]).withTag(imageAndTag[1]).exec(new ResultCallback<PullResponseItem>() {
                @Override
                public void onStart(Closeable closeable) {
                    LOGGER.info(String.format("Pulling image %s:%s", imageAndTag[0], imageAndTag[1]));
                }

                @Override
                public void onNext(PullResponseItem object) {
                    LOGGER.info(String.format("Next... %s", object.getId()));
                }

                @Override
                public void onError(Throwable t) {
                    LOGGER.log(Level.SEVERE, "Pulling image ended un in an error state.", t);
                    completed.set(true);
                }

                /**
                 * Constructs and configures the database container
                 * TODO: Refactor Postgres specific configuration so as it is easy to swap it for MariaDB just by editing pom.xml.
                 */
                @Override
                public void onComplete() {
                    LOGGER.info("Constructing container.");
                    final CreateContainerResponse narayanaDB = dockerClient.createContainerCmd(containerDatabaseImage)
                            .withName(containerName)
                            .withEnv(
                                    String.format("POSTGRES_PASSWORD=%s", containerDatabasePassword),
                                    String.format("POSTGRES_USER=%s", containerDatabaseUsername),
                                    String.format("POSTGRES_DB=%s", containerDatabaseName)
                            ).withCmd("postgres",
                                    "-c", "deadlock_timeout=1s",
                                    "-c", "default_transaction_deferrable=off",
                                    "-c", "default_transaction_isolation=read committed",
                                    "-c", "default_transaction_read_only=off",
                                    "-c", "log_directory=/tmp",
                                    "-c", "log_filename=db.log",
                                    "-c", "log_line_prefix=%m transaction_id: %x ",
                                    "-c", "log_statement=all",
                                    "-c", "logging_collector=on",
                                    "-c", "max_connections=20",
                                    "-c", "max_locks_per_transaction=64",
                                    "-c", "max_pred_locks_per_transaction=64",
                                    "-c", "max_prepared_transactions=50"
                            )
                            // Note the port exposed on the host and in the container might not be the same. We keep them the same for the sake of sanity.
                            .withPortBindings(new PortBinding(Ports.Binding.bindIpAndPort(containerDatabaseBindHostIp, port), ExposedPort.tcp(port)))
                            .exec();
                    LOGGER.info("Starting container.");

                    dockerClient.startContainerCmd(narayanaDB.getId()).exec();

                    if (!waitForTcp(containerDatabaseBindHostIp, port, 500, timeout)) {
                        final String msg = String.format("The database container hasn't opened TCP socket %s:%d within %dms. Warnings: %s",
                                containerDatabaseBindHostIp, port, timeout,
                                Arrays.stream(narayanaDB.getWarnings()).map(Object::toString).collect(Collectors.joining(", ")));
                        LOGGER.severe(msg);
                        throw new IllegalStateException(msg);
                    } else {
                        LOGGER.info(String.format("The database container has successfully opened TCP socket %s:%d", containerDatabaseBindHostIp, port));
                    }

                    // TODO - replace with a logical check - an SQL statement.
                    // Sometimes it takes a jiffy to avoid "PSQLException: the database system is starting up"
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, "Interrupted.", e);
                    }

                    completed.set(true);
                }

                @Override
                public void close() throws IOException {
                    completed.set(true);
                    LOGGER.info("Closed.");
                }
            });
        } catch (NotFoundException ex) {
            LOGGER.log(Level.SEVERE, String.format("Wasn't able to find image %s, see: %s", containerDatabaseImage, ex.getMessage()), ex);
            return null;
        }

        final long timestamp = System.currentTimeMillis();
        while (!completed.get() && (System.currentTimeMillis() - timestamp < timeout)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Waiting for container interrupted.", e);
            }
        }

        if (!completed.get()) {
            throw new IllegalStateException("Image was not pulled or container was not started in time.");
        }

        // Construct DB
        return new DB.Builder()
                .dsType(containerDatabaseDatasourceClassXa)
                .dsUsername(containerDatabaseUsername)
                .dsUser(containerDatabaseUsername)
                .dsPassword(containerDatabasePassword)
                .dsDbName(containerDatabaseName)
                .dsDbPort(String.valueOf(port))
                .dsDbHostname(containerDatabaseBindHostIp)
                .dsUrl(String.format("jdbc:postgresql://%s:%d/%s", containerDatabaseBindHostIp, port, containerDatabaseName))
                .dsLoginTimeout("0")
                .dsFactory(containerDatabaseDatasourceClassXa + "Factory")
                .dsDriverClassName(containerDatabaseDriverClass)
                .tdsType("javax.sql.XADataSource")
                .dbDriverArtifact(containerDatabaseDriverArtifact)
                .build();
    }

    public DB allocateDB() {
        return allocateDB(0);
    }

    public boolean deallocateDB(final DB db) {
        final String containerName = getProp("container.name");
        LOGGER.info("Listing containers for deallocation.");
        dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                .filter(c -> ArrayUtils.contains(c.getNames(), "/" + containerName)).forEach((Container c) -> {
            final InspectContainerResponse.ContainerState s = Objects.requireNonNull(dockerClient.inspectContainerCmd(c.getId()).exec().getState(),
                    "Container state cannot be null. Something went terribly wrong.");
            try (InputStream x = dockerClient.copyArchiveFromContainerCmd(c.getId(), "/tmp/db.log").exec()) {
                LOGGER.info(IOUtils.toString(x, "UTF-8"));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to read logs from the database container.", e);
            }
            if (s.getRunning() || s.getRestarting()) {
                LOGGER.info("Killing container.");
                dockerClient.killContainerCmd(c.getId()).exec();
            }
            LOGGER.info("Removing container.");
            dockerClient.removeContainerCmd(c.getId()).exec();
        });
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
        final String containerName = getProp("container.name");
        final String containerDatabaseBindHostPort = getProp("container.database.bind.host.port");
        final int port = Integer.parseInt(containerDatabaseBindHostPort);
        if (port > 65535 || port < 1025) {
            throw new IllegalArgumentException("container.database.bind.host.port out of expected range [1025, 65535]");
        }
        final String containerTimeoutWaitingForTcp = getProp("container.timeout.waiting.for.tcp");
        final long timeout = Long.parseLong(containerTimeoutWaitingForTcp);
        if (timeout > TimeUnit.MINUTES.toMillis(30) || timeout < 500) {
            throw new IllegalArgumentException("container.timeout.waiting.for.tcp out of expected range [500, 30*60*1000]");
        }
        final String containerDatabaseBindHostIp = getProp("container.database.bind.host.ip");

        LOGGER.info("Listing containers for cleanup.");
        dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                .filter(c -> ArrayUtils.contains(c.getNames(), "/" + containerName)).forEach(c -> {
            if (dockerClient.inspectContainerCmd(c.getId()).exec().getState().getRunning()) {
                LOGGER.info("Restarting running container.");
                dockerClient.restartContainerCmd(c.getId()).exec();
                if (!waitForTcp(containerDatabaseBindHostIp, port, 500, timeout)) {
                    final String msg = String.format("The container hasn't opened TCP socket %s:%d within %dms after restart.",
                            containerDatabaseBindHostIp, port, timeout);
                    LOGGER.severe(msg);
                    throw new IllegalStateException(msg);
                } else {
                    LOGGER.info(String.format("The database container has successfully restarted. TCP socket %s:%d opened.", containerDatabaseBindHostIp, port));
                }
            } else {
                LOGGER.info("Container was not running. Skipping restart.");
            }
        });
        return true;
    }
}

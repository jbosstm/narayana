/*
 * Copyright Red Hat
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.deployment.scenario;

import io.narayana.lra.arquillian.deployment.WildflyLRACoordinatorDeployment;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>This class is an implementation of Arquillian SPI DeploymentScenarioGenerator. The purpose of this class
 * is creating an lra-coordinator DeploymentDescription to be deployed to a specific container. The information
 * identifying the container are fetched from arquillian.xml in the module that uses LRACoordinatoExtension.
 * Properties have to be specified according with the pre-fixed names within this class. Moreover, if the extension
 * is activated in the file src/main/resources/META-INF/services/org.jboss.arquillian.core.spi.LoadableExtension
 * but there is not mention of the extension in arquillian.xml, this extension returns without generating a
 * DeploymentDescription.</p>
 *<p>To activate this extension in your arquillian.xml, use the following construct:</p>
 * <p>{@code <extension qualifier="LRACoordinatorDeployment">}</p>
 * <p>{@code <property name="groupName">...</property>}</p>
 * <p>{@code <property name="containerName">...</property>}</p>
 * <p>{@code <property name="deploymentName">...</property>}</p>
 * <p>{@code <property name="testable">...</property>}</p>
 * <p>{@code </extension>}</p>
 */
public class LRACoordinatorScenarioGenerator extends ScenarioGeneratorBase implements DeploymentScenarioGenerator {

    public static final String EXTENSION_NAME = "LRACoordinatorDeployment";
    public static final String EXTENSION_DEPLOYMENT_NAME = "deploymentName"; // needed
    public static final String EXTENSION_GROUP_NAME = "groupName"; // needed
    public static final String EXTENSION_CONTAINER_NAME = "containerName"; // needed
    public static final String EXTENSION_TESTABLE = "testable";

    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {

        List<DeploymentDescription> descriptions = new ArrayList<>();

        // Fetch all properties in the section EXTENSION_NAME
        Map<String, String> extensionProperties = getExtensionProperties(EXTENSION_NAME);

        // If the section of this extension is not in the arquillian.xml file, it means that this extension
        // does not need to start. As a consequence, an empty list of DeploymentDescription is returned
        if (extensionProperties == null) {
            return new ArrayList<>();
        }

        // Checks that all required properties are defined
        checkPropertiesExistence(
                extensionProperties,
                Arrays.asList(
                        EXTENSION_DEPLOYMENT_NAME,
                        EXTENSION_GROUP_NAME,
                        EXTENSION_CONTAINER_NAME,
                        EXTENSION_TESTABLE));

        GroupDef group = getGroupWithName(extensionProperties.getOrDefault(EXTENSION_GROUP_NAME, ""));

        ContainerDef container;
        if (group != null) {
            container = getContainerWithName(group, extensionProperties.get(EXTENSION_CONTAINER_NAME));
        } else {
            container = getContainerWithName(extensionProperties.get(EXTENSION_CONTAINER_NAME));
            if (container == null) {
                String message = String.format(
                        "%s: no container was found with name: %s.",
                        EXTENSION_NAME,
                        extensionProperties.get(EXTENSION_CONTAINER_NAME));

                log.error(message);
                throw new RuntimeException(message);
            }
        }

        String containerName = container.getContainerName();

        WebArchive archive = (WebArchive) new WildflyLRACoordinatorDeployment().create(extensionProperties.get(EXTENSION_DEPLOYMENT_NAME));

        DeploymentDescription deploymentDescription =
                new DeploymentDescription(extensionProperties.get(EXTENSION_DEPLOYMENT_NAME), archive)
                        .setTarget(new TargetDescription(containerName));
        deploymentDescription.shouldBeTestable(Boolean.parseBoolean(extensionProperties.get(EXTENSION_TESTABLE)));
        // Auto-define if the deployment should be managed or unmanaged
        deploymentDescription.shouldBeManaged(!container.getMode().equals("manual"));

        descriptions.add(deploymentDescription);

        return descriptions;
    }
}

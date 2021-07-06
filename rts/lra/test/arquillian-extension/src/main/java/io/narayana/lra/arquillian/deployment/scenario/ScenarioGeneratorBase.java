/*
 * Copyright Red Hat
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.deployment.scenario;

import io.narayana.lra.arquillian.deployment.Deployment;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *     This class is composed by a collection of methods useful to classes implementing
 * the interface DeploymentScenarioGenerator. In particular, this class represents
 * a main point of control when it comes to classes that use the "extension" section
 * in the Arquillian.xml file. In fact, this class supplies basic methods to identify
 * a particular extension section and handle properties defined in it.
 * </p>
 * <p>
 *     The concept behind this class comes from the project <code>deploymentscenario</code>, which can be found
 * among the showcases of Arquillian.
 * </p>
 * @see <a href="https://github.com/arquillian/arquillian-showcase/tree/master/extensions/deploymentscenario">Arquillian Deployment Scenario</a>
 */
public class ScenarioGeneratorBase {

    static final Logger log = Logger.getLogger(ScenarioGeneratorBase.class);

    @Inject
    Instance<ArquillianDescriptor> arquillianDescriptorInstance;

    /**
     * This methods return a {@link Map} representing the properties defined in the <code>extension</code>
     * section and identifiable with the parameter <code>extensionName</code>
     * @param extensionName The name assigned to the extension section
     * @return {@link Map} representing a set of properties
     */
    Map<String, String> getExtensionProperties(final String extensionName) {

        ArquillianDescriptor arquillianDescriptor = arquillianDescriptorInstance.get();

        Optional<Map<String, String>> checkExistance = arquillianDescriptor.getExtensions().stream()
                .filter(x -> x.getExtensionName().equals(extensionName))
                .map(ExtensionDef::getExtensionProperties).findAny();

        if (!checkExistance.isPresent()) {
            String message = String.format("%s: there is not extension section with name %s defined in the %s file",
                    this.getClass().getSimpleName(),
                    extensionName,
                    System.getProperty("arquillian.xml", "arquillian.xml"));

            log.warn(message);
        }

        return checkExistance.orElse(null);
    }

    /**
     * This methods checks that the properties in the {@link List} <code>toCheck</code>
     * are present in the {@link Map} <code>properties</code>
     * @param properties {@link Map} representing the properties of an extension
     * @param toCheck {@link List} of properties that need to be defined
     * @throws RuntimeException if a property is not found
     */
    void checkPropertiesExistence(final Map<String, String> properties, final List<String> toCheck) {
        // tell me what property need to be defined
        toCheck.forEach(x -> {
            if (!properties.containsKey(x)) {
                String message = String.format("%s: the property %s is not defined in the extension",
                        ScenarioGeneratorBase.class.getSimpleName(),
                        x);

                log.warn(message);
            }
        });
    }

    /**
     * This method extracts a {@link Method} that represents a deployment method that
     * should be called to create an {@link org.jboss.shrinkwrap.api.Archive} to deploy
     * in an Arquillian container
     * @param properties {@link Map} representing the properties of an extension
     * @param deploymentMethodPropertyName name of the deployment method property
     * @return {@link Method}
     * @throws ClassNotFoundException if the specified class is not valid
     * @throws NoSuchMethodException if the specified method is not valid
     * @throws RuntimeException if anything else goes wrong
     */
    Method getDeploymentMethodFromConfiguration(final Map<String, String> properties, final String deploymentMethodPropertyName)
            throws ClassNotFoundException, NoSuchMethodException, RuntimeException{

        String property = properties.get(deploymentMethodPropertyName).trim();

        String className = property.substring(0, property.lastIndexOf('.'));
        Class<?> clazz = Class.forName(className);
        // Checks if clazz is an implementation of the interface Deployment
        if (!Deployment.class.isAssignableFrom(clazz)) {
            String message = String.format(
                    "%s: the specified class %s for the property %s is not an implementation of the interface Deployment<T>",
                    this.getClass().getSimpleName(),
                    className,
                    deploymentMethodPropertyName);

            log.error(message);
            throw new RuntimeException(message);
        }

        // Fetches all methods that contains the specified method name
        // (only one method should be found)
        String methodName = property.substring(property.lastIndexOf('.') + 1);
        List<Method> methods = Arrays.stream(clazz.getMethods())
                .filter(x -> x.getName().contains(methodName))
                .collect(Collectors.toList());

        // If the list of methods is empty or contains more than one method,
        // a RuntimeException should be thrown
        if (methods.size() != 1) {
            String message = String.format(
                    "The specified class %s overloads the method %s",
                    className,
                    methodName);

            log.error(message);
            throw new RuntimeException(message);
        }

        return methods.get(0);
    }

    /**
     * This method checks if a container with qualifier name <code>containerName</code> is
     * listed in the Arquillian.xml file.
     * @param containerName the qualifier name of the container
     * @return {@link ContainerDef} representing the wanted container
     * @throws RuntimeException if the container is not found
     */
    ContainerDef getContainerWithName(String containerName)
            throws RuntimeException {

        ArquillianDescriptor arquillianDescriptor = arquillianDescriptorInstance.get();

        List<ContainerDef> containers = arquillianDescriptor.getContainers().stream()
                .filter(x -> x.getContainerName().equals(containerName)).collect(Collectors.toList());

        if (containers.isEmpty()) {
            String message = String.format("%s: there is not a standalone container with qualifier %s in the %s file!",
                    this.getClass().getSimpleName(),
                    containerName,
                    System.getProperty("arquillian.xml", "arquillian.xml"));

            log.warn(message);
            return null;
        }

        // It is pointless to check if there are more containers with the
        // same qualifier as Arquillian already checks that

        return containers.get(0);
    }

    /**
     * This method checks if a container with qualifier name <code>containerName</code> is
     * listed in the group with qualifier <code>group</code>.
     * @param group the qualifier of the group
     * @param containerName the qualifier name of the container
     * @return {@link ContainerDef} representing the wanted container
     * @throws RuntimeException if the container is not found
     */
    ContainerDef getContainerWithName(GroupDef group, String containerName)
            throws RuntimeException {

        List<ContainerDef> containers = group.getGroupContainers().stream()
                .filter(x -> x.getContainerName().equals(containerName)).collect(Collectors.toList());

        if (containers.isEmpty()) {
            String message = String.format("%s: there is not a container with qualifier %s in the group %s defined in the %s file!",
                    this.getClass().getSimpleName(),
                    containerName,
                    group.getGroupName(),
                    System.getProperty("arquillian.xml", "arquillian.xml"));

            log.warn(message);
            return null;
        }

        return containers.get(0);
    }

    /**
     * This method checks if a group with qualifier name <code>groupName</code> is
     * listed in the Arquillian.xml file.
     * @param groupName the qualifier name of the group
     * @return {@link GroupDef} representing the wanted group
     * @throws RuntimeException if the group is not found
     */
    GroupDef getGroupWithName(String groupName) {

        if (groupName.isEmpty()) {
            return null;
        }

        ArquillianDescriptor arquillianDescriptor = arquillianDescriptorInstance.get();

        List<GroupDef> groups = arquillianDescriptor.getGroups().stream()
                .filter(x -> x.getGroupName().equals(groupName)).collect(Collectors.toList());

        if (groups.isEmpty()) {
            String message = String.format("%s: there is not a group with qualifier %s in the %s file!",
                    this.getClass().getSimpleName(),
                    groupName,
                    System.getProperty("arquillian.xml", "arquillian.xml"));

            log.warn(message);
            return null;
        }
        // It is not needed to check if List<GroupDef> contains more than one group
        // with qualifier groupName as Arquillian runs already this check.

        return groups.get(0);
    }
}

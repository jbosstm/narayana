/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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


package io.narayana.lra.checker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import io.narayana.lra.checker.cdi.LraAnnotationProcessingExtension;

/**
 * <p>
 * Maven plugin expecting the provided arguments are paths to jar file or directory.<br>
 * It loads all classes (<code>.class</code> files) from the provided paths
 * and checks the LRA annotations if they're correct with use of CDI extension.
 * <p>
 * <p>
 * Expected used by adding the snipped similar to this to your <code>pom.xml</code>
 * <pre>
 * &lt;build&gt;
 *   &lt;plugins&gt;
 *     &lt;plugin&gt;
 *       &lt;groupId&gt;org.jboss.narayana.rts&lt;/groupId&gt;
 *       &lt;artifactId&gt;lra-annotation-checker-maven-plugin&lt;/artifactId&gt;
 *       &lt;version&gt;5.7.3.Final&lt;/version&gt;
 *       &lt;executions&gt;
 *         &lt;execution&gt;
 *           &lt;goals&gt;
 *             &lt;goal&gt;check&lt;/goal&gt;
 *           &lt;/goals&gt;
 *         &lt;/execution&gt;
 *       &lt;/executions&gt;
 *     &lt;/plugin&gt;
 *   &lt;/plugins&gt;
 * &lt;/build&gt;
 * </pre>
 * <p>
 * For configuration you can use parameters to configure
 * <ul>
 *   <li><code>paths</code> - path searched for classes which will be checked on LRA annotations (use directory or jar)</li>
 *   <li><code>failWhenPathNotExist</code> - define if fails when some of the <code>paths</code> param is non-existing path</li>
 * </ul>
 * <pre>
 * &lt;plugin&gt;
 *   ...
 *   &lt;configuration&gt;
 *     &lt;failWhenPathNotExist&gt;false&lt;/failWhenPathNotExist&gt;
 *     &lt;paths&gt;
 *       &lt;param&gt;${project.build.directory}/classes&lt;/param&gt;
 *     &lt;/paths&gt;
 *   &lt;/configuration&gt;
 *   ...
 * &lt;/plugin&gt;
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class LraCheckerMavenPlugin extends AbstractMojo {

    /**
     * <p>
     * Paths that will be searched for '.class' files
     * and then later checked on existence of LRA annotations.
     * <p>
     * Path to directory or jar file can be used.
     */
    @Parameter
    private String[] paths;

    /**
     * If true the exception is thrown when the provided path does not exist
     * (plugin is expected to process the path but it does not be located).<br>
     * If false then on non-existing path only warning is printed.
     */
    @Parameter
    private boolean failWhenPathNotExist = true;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // do not show weld info log messages
        if(!getLog().isDebugEnabled()) {
            LogManager lm = LogManager.getLogManager();
            Logger archiveDeployerLogger = Logger.getLogger("org.jboss.weld");
            archiveDeployerLogger.setLevel(Level.WARNING);
            lm.addLogger(archiveDeployerLogger);
        }

        if(paths == null) paths = new String[] {project.getBuild().getOutputDirectory()};

        // check argument validity
        if(paths.length <= 0) {
            throw new  MojoFailureException("No argument provided, nothing to be scanned");
        }
        String[] preprocessedPaths = paths;

        // filtering out paths that does not exist
        if(!failWhenPathNotExist) {
            preprocessedPaths =
                Arrays.asList(paths).stream().filter(path -> new File(path).exists()).toArray(String[]::new);
            if(preprocessedPaths.length <= 0) {
                getLog().warn("No provided parameter as path for checking for LRA classes '" +
                    Arrays.asList(paths) + "' does exist");
                return;
            }
        }

        CheckerUtil.setMavenLog(getLog());
        getLog().info("Loading classes from paths: " + Arrays.asList(preprocessedPaths));

        // say if path is directory or jar file
        Map<File, FileType> pathsFileMap = classifyPaths(preprocessedPaths);

        // class loading from defined urls
        ClassLoader classLoader = providePathsToClassLoader(preprocessedPaths);

        // scanning the arguments to say the classes to be processed
        List<Class<?>> clazzNames = new ArrayList<Class<?>>();
        for(Entry<File, FileType> argEntry: pathsFileMap.entrySet()) {
            switch (argEntry.getValue()) {
                case DIRECTORY:
                    clazzNames.addAll(
                            CheckerUtil.loadFromDir(argEntry.getKey(), classLoader));
                    break;
                case JAR:
                    clazzNames.addAll(
                            CheckerUtil.loadFromJar(argEntry.getKey(), classLoader));
                    break;
            }
        }

        // weld extension checking the LRA annotation to go
        @SuppressWarnings("unchecked")
        Weld weld = new Weld()
            .disableDiscovery()
            .addExtensions(LraAnnotationProcessingExtension.class)
            .setClassLoader(classLoader)
            .addBeanClasses(clazzNames.toArray(new Class[clazzNames.size()]));

        try(WeldContainer container = weld.initialize()) {
            // weld intializes and works with extensions here
        } catch (org.jboss.weld.exceptions.DeploymentException ignore) {
            if(!getLog().isDebugEnabled()) {
                getLog().debug("Error on Weld init happened but we are ignoring"
                        + "it as we care about LRA annotation and not about deploying", ignore);
            }
        } finally {
            if(!FailureCatalog.INSTANCE.isEmpty()) {
                throw new MojoFailureException(
                    String.format("LRA annotation errors:%n%s", FailureCatalog.INSTANCE.formatCatalogContent()));
            }
        }
    }

    private Map<File, FileType> classifyPaths(String[] pathsToClasify) throws MojoFailureException {
        Map<File, FileType> pathsFileMap = new HashMap<>();
        for(String arg: pathsToClasify) {
            File file = new File(arg);
            if(!file.exists()) {
                throw new IllegalArgumentException("Provided argument '" + arg + "' is not an existing file");
            }

            if(file.isDirectory()) {
                pathsFileMap.put(file, FileType.DIRECTORY);
            } else if(CheckerUtil.isZipFile(file)) {
                pathsFileMap.put(file, FileType.JAR);
            } else {
                throw new MojoFailureException("Provided path '" + arg + "' is neither directory nor jar file");
            }
        }
        return pathsFileMap;
    }

    private ClassLoader providePathsToClassLoader(String[] pathsToBeClassLoaded) throws MojoFailureException {
        URLClassLoader classLoader = null;
        String currentPathProcessed = null;
        try {
            // classes which are expected to be scanned by Weld extension
            List<URL> pathUrls = new ArrayList<>();
            for(String paramPath: pathsToBeClassLoaded) {
                currentPathProcessed = paramPath;
                pathUrls.add(new File(paramPath).toURI().toURL());
            }
            // adding compile classpath as expected to be needed for Weld can initiate the classes for evaluation
            for(String mavenCompilePath: project.getCompileClasspathElements()) {
                currentPathProcessed = mavenCompilePath;
                pathUrls.add(new File(mavenCompilePath).toURI().toURL());
            }

            URL[] urlsForClassLoader = pathUrls.toArray(new URL[pathUrls.size()]);
            getLog().debug("urls for URLClassLoader: " + Arrays.asList(urlsForClassLoader));

            // need to define parent classloader which knows all dependencies of the plugin (e.g. LRA annotations)
            classLoader = new URLClassLoader(urlsForClassLoader, LraCheckerMavenPlugin.class.getClassLoader());

        } catch (MalformedURLException | DependencyResolutionRequiredException e) {
            throw new MojoFailureException("Failed to processed '" + currentPathProcessed + "' for the URLClassLoader", e);
        }
        return classLoader;
    }
}

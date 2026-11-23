/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.taskdefs;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.jboss.jbossts.qa.astests.recovery.ASFailureSpec;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.xml.sax.SAXException;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Ant task for capturing common configuration for a group of tests
 * defined in an ant build script.
 */
public class ASTestConfig extends Task
{
     // arbitary handle for refering to the unique instance of the task configuration
    public static final String CONFIG_REF = "asTestConfig";
    // the name of the system property holding the path the product directory
    public final static String PRODUCT_DIR_PROP = "product.dir";
    // the location of a JBoss distribution relative to the product directory
    private final static String AS_ROOT_DIR = "/as/";

    private List<ASFailureSpec> specs = new ArrayList<ASFailureSpec>();
    private Map<String, ASFailureSpec> specMap = new HashMap<String, ASFailureSpec>();
    private String productDir;
    private boolean isDebug = false;
    private ServerManager manager = new ServerManager();

    public void execute() throws BuildException
    {
        System.setSecurityManager(new java.rmi.RMISecurityManager());

        // The Task Runner should have set the product directory
        productDir = System.getProperty(PRODUCT_DIR_PROP);

        if (productDir == null)
            throw new BuildException("Please specify the location of the product directory via the \""+ PRODUCT_DIR_PROP + "\" system property");

        productDir = Utils.toFile(productDir).getAbsolutePath();
        
        if (!productDir.endsWith("/"))
            productDir += '/';

        if (isDebug)
            System.out.println("ASCrashConfig: productDir=" + productDir + " and jbossHome=" + manager.getJBossHome());

        if (manager.getJBossHome() == null)
        {
            File jbh = Utils.toFile(productDir + AS_ROOT_DIR);

            if (isDebug)
                System.out.println("ASCrashConfig: jbh=" + jbh.getAbsolutePath() + " exists=" + jbh.exists());

            if (jbh.exists())
                setJbossHome(jbh.getAbsolutePath());
            else
                setJbossHome(System.getenv("JBOSS_HOME"));
        }

        // add this to the ant project so that other tasks are able to locate this
        if (getProject().getReference(CONFIG_REF) == null)
            getProject().addReference(CONFIG_REF, this);

        ASFailureSpec[] sa = getSpecs();

        // read in any failure specifications so that an ant target may look them up
        for (ASFailureSpec fs : sa)
            specMap.put(fs.getName(), fs);

        if (isDebug)
        {
            StringBuilder sb = new StringBuilder();

            sb.append("configuring ").append(specs.size()).append(" crash specifications:\n");

            for (ASFailureSpec spec : specs)
            {
                sb.append("\t").append(spec).append('\n');
            }

            System.out.print(sb);
        }
    }

    /**
     * Task parameter to add a fault injection specification.
     *
     * @param spec definition of the fault
     */
    public void addSpec(ASFailureSpec spec)
    {
        specs.add(spec);
    }

    public ASFailureSpec getSpec(String spec)
    {
        return specMap.get(spec);
    }

    public ASFailureSpec[] getSpecs()
    {
        ASFailureSpec[] sa = new ASFailureSpec[specs.size()];

        return specs.toArray(sa);
    }

    public ASFailureSpec[] parseSpecs(String specArg)
    {
        String[] specs = specArg.split(",");
        ASFailureSpec[] fspecs = new ASFailureSpec[specs.length];

        for (int i = 0; i < specs.length; i++)
            fspecs[i] = getSpec(specs[i].trim());

        return fspecs;
    }

    /**
     * Task parameter to add a server.
     *
     * @param server the server
     */
    public void addServer(Server server)
    {
        manager.addServer(server);
    }

    /**
     * JAVA_HOME to start jboss with.
     *
     * @param javaHome Path to the the java installation
     */
    public void setJavaHome(String javaHome)
    {
        manager.setJavaHome(javaHome);
    }

    public String getJBossHome()
    {
        return manager.getJBossHome();
    }
    
    /**
     * JBoss dist to start. The default is to use the AS installed with the product.
     * If no AS can be found then the environment variable JBOSS_HOME is used
     * 
     * @param jbossHome Path to the the jboss installation
     */
    public void setJbossHome(String jbossHome)
    {
        manager.setJbossHome(jbossHome);
    }

    /**
     * JVM command to use default is "java"
     * @param jvm JVM command
     */
    public void setJvm(String jvm)
    {
        manager.setJvm(jvm);
    }

    /**
     * The UDP group to pass to org.jboss.Main using
     * the -u option.
     *
     * @param udpGroup the udp group
     */
    public void setUdpGroup(String udpGroup)
    {
        manager.setUdpGroup(udpGroup);
    }

    public void setDebug(boolean debug)
    {
        isDebug = debug;
    }

    /**
     *
     * @return the location where the product being tested is installed
     */
    public String getProductDir()
    {
        return productDir;
    }

    /**
     * Return the nameing context that a given server is providing
     *
     * @param serverName the server providing the context
     * @return a naming context
     */
    public Context getNamingContext(String serverName)
    {
        try
        {
            Server server = manager.getServer(serverName);

            return (server != null ? server.getNamingContext() : null);
        }
        catch (NamingException e)
        {
            e.printStackTrace();

            throw new BuildException(e);
        }
    }

    /**
     *
     * @param serverName the name of the server whose path is being sought
     * @return the path to the JBoss instance corresponding to the provided name
     */
    public String getServerPath(String serverName)
    {
        return getJBossHome() + "/server/" + serverName + '/';
    }

    /**
     * Start a JBoss instance in a new VM
     *
     * @param serverName name of the target instance
     * @throws IOException If another process is already using a port required by this instance
     * @see org.jboss.jbossas.servermanager.ServerController#startServer(org.jboss.jbossas.servermanager.Server, org.jboss.jbossas.servermanager.ServerManager)
     */
    public void startServer(String serverName) throws IOException
    {
        Server server = manager.getServer(serverName);

        // Warning !server.isRunning() is not equivalent to server.isStopped()
        // !server.isRunning() and !server.isStopped() implies that some other entity terminated the server
        if (!server.isRunning())
            manager.startServer(serverName);
    }

    /**
     * @see org.jboss.jbossas.servermanager.ServerController#stopServer(org.jboss.jbossas.servermanager.Server, org.jboss.jbossas.servermanager.ServerManager)
     */
    public void stopServer(String serverName) throws IOException
    {
        Server server = manager.getServer(serverName);

        if (server.isRunning())
            manager.stopServer(serverName);
    }

    /**
     * Stop a group of servers. Any error stopping a server will logged and then the next one in the list will be tried.
     *
     * @param servers the names of the servers to be stopped
     * @see ASTestConfig#stopServer(String)
     */
    public void stopServers(String ... servers)
    {
        for (String server : servers)
        {
            try
            {
                stopServer(server);
            }
            catch (IOException e)
            {
                System.err.println("Unable to stop server " + server);
            }
        }
    }

    public ServerManager getServerManager()
    {
        return manager;
    }

    /**
     * Configure a target AS to start with a non-default set of bindings
     *
     * @param serverName the server whose bindings are being configured
     * @throws TransformerException error parsing the server bindings xml file
     * @throws IOException error whilst locating the bindings file in the file system
     * @throws SAXException error parsing the server bindings xml file
     * @throws ParserConfigurationException error parsing the server bindings xml file
     */
    public void configureServerBinding(String serverName) throws TransformerException, IOException, SAXException, ParserConfigurationException
    {
        Server server = manager.getServer(serverName);
        String configHome = getProductDir();
        String mbeanServiceFile = getServerPath(serverName) + "conf/jboss-service.xml";
        String bindingName = server.getSysProperty("server.binding.name");
        String bindingFile = server.getSysProperty("server.binding.location");

        if (bindingFile == null)
            return;

        bindingFile = configHome + bindingFile;

        // if bindingName is set then it is safe to check that the AS is up via the Naming service port
        // BTW the AS5 team has changed the behavior of the naming service port - it is opened before
        // all the services have been configured so any target deployments may not have been initialised
        // when the server is declared as running. Instead we force the server to use a web server port to
        // determine if the AS is running.
        if (bindingName == null)
            return;
//            bindingName = ServerBindingConfig.DEFAULT_BINDING;
//        else
//            server.setHasWebServer(true);

        ServerBindingConfig.setBinding(mbeanServiceFile, bindingName, bindingFile);

        server.setRmiPort(ServerBindingConfig.lookupRmiPort(bindingFile, bindingName, server.getRmiPort()));
        server.setHttpPort(ServerBindingConfig.lookupHttpPort(bindingFile, bindingName, server.getHttpPort()));

        System.out.println("Using: " + Context.PROVIDER_URL
                + " = jnp://"+ server.getHost() + ':' + server.getRmiPort()
                + " and http port " + server.getHttpPort());
    }
}

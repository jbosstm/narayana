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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.io.*;

public class ServerTask extends Task
{
    /** the method name for starting a server */
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String DEPLOY = "deploy";
    public static final String UNDEPLOY = "undeploy";

    private ArrayList<TaskProperty> deployArtifacts = new ArrayList<TaskProperty>();
    private ArrayList<TaskProperty> undeployArtifacts = new ArrayList<TaskProperty>();
    private String method;
    private String serverName;
    private String waitFor;

    public void execute() throws BuildException
    {
        String[] servers = serverName.split(",");

        try
        {
            if (START.equals(method))
                startServers(servers);
            else if (STOP.equals(method))
                stopServers(servers);
            else if (DEPLOY.equals(method))
                deploy(servers);
            else if (UNDEPLOY.equals(method))
                undeploy(servers);

            if (waitFor != null)
                ASClientTask.suspendFor(waitFor);
        }
        catch (ServerTaskException e)
        {
            throw new BuildException(e);
        }
    }

    /**
     * Specify which server operation is being requested
     * @param method currently supported methods are "start" and "stop"
     */
    public void setMethod(String method)
    {
        this.method = method;
    }

    /** the name of the server to operate on */
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    /** suspend after performing the task - useful for deploy operations */
    public void setWaitFor(String waitFor)
    {
        this.waitFor = waitFor;
    }

    /** list of artifacts that should be deployed to this server **/
    public void addDeploy(TaskProperty arg)
    {
        deployArtifacts.add(arg);
    }

    /** list of artifacts that should be undeployed from this server **/
    public void addUndeploy(TaskProperty arg)
    {
        undeployArtifacts.add(arg);
    }

    private void stopServers(String ... servers) throws ServerTaskException
    {
        ASTestConfig config = (ASTestConfig) getProject().getReference(ASTestConfig.CONFIG_REF);

        for (String name : servers)
        {
            try
            {
                config.getServerManager().stopServer(name);
            }
            catch (IOException e)
            {
                throw new ServerTaskException("Error stoping server " + name);
            }
        }

        undeploy(servers);
    }

    private void startServers(String ... servers) throws ServerTaskException
    {
        ASTestConfig config = (ASTestConfig) getProject().getReference(ASTestConfig.CONFIG_REF);

        deploy(servers);

        for (String name : servers)
        {
            try
            {
                config.getServerManager().startServer(name);
            }
            catch (IOException e)
            {
                throw new ServerTaskException("Error starting server " + name);
            }
        }
    }

    private void deploy(String ... servers) throws ServerTaskException
    {
        ASTestConfig config = (ASTestConfig) getProject().getReference(ASTestConfig.CONFIG_REF);
        String configHome = config.getProductDir();

        // configure the AS to start with a non-default set of bindings
        for (String name : servers)
        {
            // make sure the server config exists
            config.getServerManager().getServer(name);
            // update its bindings if required
            try
            {
                config.configureServerBinding(name);
            }
            catch (Exception e)
            {
                throw new ServerTaskException("Error configuring binding for server " + name, e);
            }

            // deploy any artifacts required by this test
            try
            {
                deploy(deployArtifacts, configHome, config.getServerPath(name));
                deploy(undeployArtifacts, config.getServerPath(name), configHome);
            }
            catch (IOException e)
            {
                throw new ServerTaskException("Error deploying artifacts to server " + name, e);
            }
        }
    }

    private void undeploy(String ... servers) throws ServerTaskException
    {
        deploy(servers);
    }

    // deploy any artifacts required by this test
    private void deploy(ArrayList<TaskProperty> artifacts, String srcDir, String dstDir) throws IOException
    {
        for (TaskProperty artifact : artifacts)
        {
            File from = Utils.toFile(srcDir + artifact.getFrom());

            if (artifact.getTo() == null)
            {
                // delete the artifact
                System.out.println("Deleting artifact " + from.getAbsolutePath());
                from.delete();
            }
            else if (!from.exists())
            {
                System.out.println("Deployment source " + from.getAbsolutePath() + " does not exist");
            }
            else
            {
                File to = Utils.toFile(dstDir + artifact.getTo());

                if (to.isDirectory())
                    to = new File(to.getAbsolutePath() + '/' + from.getName());

                if (!to.getParentFile().exists())
                {
                    // to.mkdirs() is too presumtuous
                    System.out.println("Deployment target directory " + to.getParent() + " does not exist");
                }
                else
                {
                    System.out.println("copying from " + from.getAbsolutePath() + " to " + to.getAbsolutePath());

                    InputStream in = new FileInputStream(from);
                    OutputStream out = new FileOutputStream(to);
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0)
                        out.write(buf, 0, len);

                    in.close();
                    out.close();
                }
            }
        }
    }
}

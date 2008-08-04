package org.jboss.jbossts.qa.astests.taskdefs;

import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Temporary wrapper for org.jboss.jbossas.servermanager.Server
 */
public class ServerWrapper
{
    Server server;
    private Context namingContext;

    public ServerWrapper(Server server)
    {
        this.server = server;
    }

    public String getName()
    {
        return server.getName();
    }

    public void setName(String name)
    {
        server.setName(name);
    }

    public String getUsername()
    {
        return server.getUsername();
    }

    public void setUsername(String username)
    {
        server.setUsername(username);
    }

    public String getPassword()
    {
        return server.getPassword();
    }

    public void setPassword(String password)
    {
        server.setPassword(password);
    }

    public void addArg(Argument arg)
    {
        server.addArg(arg);
    }

    public String getArgs()
    {
        return server.getArgs();
    }

    public void addJvmArg(Argument arg)
    {
        server.addJvmArg(arg);
    }

    public String getJvmArgs()
    {
        return server.getJvmArgs();
    }

    public void addSysProperty(Property property)
    {
        server.addSysProperty(property);
    }

    public String getSysProperties()
    {
        return server.getSysProperties();
    }

    public String getSysProperty(String key)
    {
        //args.append("-D" + property.getKey() + "=" + property.getValue() + " ");
        key = "-D" + key + '=';
        for (String k : server.getSysProperties().split(" "))
        {
            if (k.startsWith(key))
                return k.substring(key.length());
        }

        return null;
    }

    public void setProcess(Process process)
    {
        server.setProcess(process);
    }

    public boolean isRunning()
    {
        return server.isRunning();
    }

    public boolean isStopped()
    {
        return server.isStopped();
    }

    public Process getProcess()
    {
        return server.getProcess();
    }

    public URL getHttpUrl()
            throws MalformedURLException
    {
        return server.getHttpUrl();
    }

    public String getRmiUrl()
    {
        return server.getRmiUrl();
    }

    public String getConfig()
    {
        return server.getConfig();
    }

    public void setConfig(String config)
    {
        server.setConfig(config);
    }

    public String getHost()
    {
        return server.getHost();
    }

    public void setHost(String host)
    {
        server.setHost(host);
    }

    public void setHttpPort(Integer httpPort)
    {
        server.setHttpPort(httpPort);
    }

    /**
     * Get the httpPort
     *
     * @return the rmi port
     */
    public Integer getHttpPort()
    {
        try
        {
            return server.getHttpUrl().getPort();
        }
        catch (MalformedURLException e)
        {
            return 8080;            
        }
    }

    public void setRmiPort(Integer rmiPort)
    {
        server.setRmiPort(rmiPort);
    }

    public Integer getRmiPort()
    {
        return server.getRmiPort();
    }

    public File getErrorLog()
    {
        return server.getErrorLog();
    }

    public File getOutputLog()
    {
        return server.getOutputLog();
    }

    public void setOutWriter(PrintWriter outlog)
    {
        server.setOutWriter(outlog);
    }

    public PrintWriter getOutWriter()
    {
        return server.getOutWriter();
    }

    public PrintWriter getErrorWriter()
    {
        return server.getErrorWriter();
    }

    public void setErrorWriter(PrintWriter errorlog)
    {
        server.setErrorWriter(errorlog);
    }

    public boolean hasWebServer()
    {
        return server.hasWebServer();
    }

    public void setHasWebServer(boolean hasWebServer)
    {
        server.setHasWebServer(hasWebServer);
    }

    public void setNamingContext(Context namingContext)
    {
        this.namingContext = namingContext;
    }

    public Context getNamingContext() throws NamingException
    {
        if (namingContext == null)
        {
            Properties properties = new Properties();

            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, ("org.jboss.naming.NamingContextFactory"));
            properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
            properties.setProperty(Context.PROVIDER_URL, "jnp://"+ getHost() + ':' + getRmiPort());

            setNamingContext(new InitialContext(properties));
        }

        return namingContext;
    }
}

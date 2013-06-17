/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.btadmin.commands;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.btadmin.Command;
import org.jboss.narayana.blacktie.btadmin.CommandFailedException;
import org.jboss.narayana.blacktie.btadmin.CommandHandler;
import org.jboss.narayana.blacktie.btadmin.IncompatibleArgsException;

/**
 * The shutdown command will quit the terminal
 */
public class Help implements Command {
    /**
     * The logger to use for output
     */
    private static Logger log = LogManager.getLogger(Help.class);

    /**
     * The command to get help for
     */
    private String command;

    public String getQuickstartUsage() {
        return "[command]";
    }

    public void initializeArgs(String[] args) throws IncompatibleArgsException {
        if (args.length > 0) {
            command = args[0];
        }
    }

    public void invoke(BlacktieAdministration connection, Properties configuration) throws CommandFailedException {

        List<String> commands = new ArrayList<String>();
        try {
            Class cls = this.getClass();
            ProtectionDomain pDomain = cls.getProtectionDomain();
            CodeSource cSource = pDomain.getCodeSource();
            URL loc = cSource.getLocation(); // file:/c:/almanac14/quickstarts/

            JarFile jar = new JarFile(new File(loc.toURI()));
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry nextElement = entries.nextElement();
                String jarEntry = nextElement.getName();
                if (jarEntry.matches("org/jboss/narayana/blacktie/btadmin/commands/\\w+.class")) {
                    String commandName = jarEntry.substring(jarEntry.lastIndexOf('/') + 1, jarEntry.indexOf('.'));
                    String firstLetter = commandName.substring(0, 1);
                    String remainder = commandName.substring(1);
                    String capitalized = firstLetter.toLowerCase() + remainder;
                    commands.add(capitalized);
                }
            }
            if (commands.size() == 0) {
                log.error("Could not find any commands");
                throw new CommandFailedException(-1);
            }
        } catch (Throwable e) {
            log.debug("Was not loaded from a jar");
            try {
                // Get the location of this class
                Class[] classes = getClasses("org.jboss.narayana.blacktie.btadmin.commands");
                for (int i = 0; i < classes.length; i++) {
                    String commandName = classes[i].getName().substring(classes[i].getName().lastIndexOf('.') + 1);
                    String firstLetter = commandName.substring(0, 1);
                    String remainder = commandName.substring(1);
                    String capitalized = firstLetter.toLowerCase() + remainder;
                    commands.add(capitalized);
                }
            } catch (Throwable e2) {
                log.error("Could not find any commands");
                throw new CommandFailedException(-1);
            }
        }
        for (int i = 0; i < commands.size(); i++) {
            if (command != null && !command.equals(commands.get(i))) {
                continue;
            }
            try {
                Command command = CommandHandler.loadCommand(commands.get(i));
                log.info("Quickstart usage: " + commands.get(i) + " " + command.getQuickstartUsage());
            } catch (Exception e) {
                log.error("Could not get help for command: " + commands.get(i), e);
                throw new CommandFailedException(-1);
            }
        }
    }

    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(java.net.URLDecoder.decode(resource.getFile().toString())));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".class") && file.getName().indexOf('$') < 0) {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                }
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }

}

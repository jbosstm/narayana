/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.stats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.sun.tools.jconsole.JConsolePlugin;
import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import javax.swing.JFrame;

public class TxPerfPlugin extends JConsolePlugin implements PropertyChangeListener
{
    private TxPerfGraph graph;
    private Map<String, JPanel> tabs;

    public TxPerfPlugin() {
        // register itself as a listener
        addContextPropertyChangeListener(this);
    }

    /*
     * Returns a TxPerf tab to be added in JConsole.
     */
    public synchronized Map<String, JPanel> getTabs() {
        if (tabs == null) {
            graph = new TxPerfGraph(new JFrame("TxPerf"));
            
            graph.setMBeanServerConnection(
                getContext().getMBeanServerConnection());
            // use LinkedHashMap if you want a predictable order
            // of the tabs to be added in JConsole
            tabs = new LinkedHashMap<String, JPanel>();
            tabs.put("TxPerf", graph);
        }
        return tabs;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if (prop == null ? JConsoleContext.CONNECTION_STATE_PROPERTY == null : prop.equals(JConsoleContext.CONNECTION_STATE_PROPERTY)) {
            ConnectionState oldState = (ConnectionState)ev.getOldValue();
            ConnectionState newState = (ConnectionState)ev.getNewValue();
            // JConsole supports disconnection and reconnection
            // The MBeanServerConnection will become invalid when
            // disconnected. Need to use the new MBeanServerConnection object
            // created at reconnection time.
            if (newState == ConnectionState.CONNECTED && graph != null) {
                graph.setMBeanServerConnection(
                    getContext().getMBeanServerConnection());
            }
        }
    }

    @Override
    public void dispose() {
        graph.dispose();
        super.dispose();
    }

    @Override
    public SwingWorker<?,?> newSwingWorker() {
        return graph.newSwingWorker();
    }
}

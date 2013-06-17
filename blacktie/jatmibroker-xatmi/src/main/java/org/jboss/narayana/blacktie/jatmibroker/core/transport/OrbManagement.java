package org.jboss.narayana.blacktie.jatmibroker.core.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextExtOperations;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextOperations;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class OrbManagement implements Runnable {
    private static final Logger log = LogManager.getLogger(OrbManagement.class);
    private static final String CorbaOrbClassProp = "org.omg.CORBA.ORBClass";
    private static final String CorbaOrbClassValue = "org.jacorb.orb.ORB";
    private static final String CorbaSingletonClassProp = "org.omg.CORBA.ORBSingletonClass";
    private static final String CorbaSingletonClassValue = "org.jboss.system.ORBSingleton";
    private static OrbManagement instance;
    private ORB orb;
    private NamingContextExt nce;
    private NamingContext nc;
    private POA root_poa;
    private Thread callbackThread;

    public static synchronized OrbManagement getInstance(Properties properties) throws InvalidName, AdapterInactive, NotFound,
            CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, ConfigurationException {
        if (instance == null) {
            instance = new OrbManagement(properties);
        }
        return instance;
    }

    private OrbManagement(Properties properties) throws InvalidName, AdapterInactive, NotFound, CannotProceed,
            org.omg.CosNaming.NamingContextPackage.InvalidName, ConfigurationException {
        String namingContextExt = properties.getProperty("blacktie.domain.name");
        int numberOfOrbArgs = Integer.parseInt(properties.getProperty("blacktie.orb.args", "0"));
        List<String> orbArgs = new ArrayList<String>(numberOfOrbArgs);
        for (int i = 1; i <= numberOfOrbArgs; i++) {
            orbArgs.add(properties.getProperty("blacktie.orb.arg." + i));
        }
        String[] args = orbArgs.toArray(new String[orbArgs.size()]);

        StringBuilder sb = new StringBuilder();
        for (String s : args)
            sb.append(s).append(", ");
        log.trace("ServerProxy's connectToORB args: " + sb.toString() + " namingContext: " + namingContextExt);

        java.util.Properties p = new java.util.Properties();
        log.debug("setting properities");
        p.setProperty(CorbaOrbClassProp, CorbaOrbClassValue);
        p.setProperty(CorbaSingletonClassProp, CorbaSingletonClassValue);
        String oaiAddr = properties.getProperty("blacktie.orb.interface", "0.0.0.0");
        String oaiAddrPort = properties.getProperty("blacktie.orb.interface.port", "0");
        p.setProperty("OAIAddr", oaiAddr);
        p.setProperty("OAPort", oaiAddrPort);

        log.debug("set properities");
        log.debug(" initing orb");
        orb = org.omg.CORBA.ORB.init(args, p);
        log.debug(" inited orb");
        log.debug(" resolving NameService");
        nce = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        log.debug("NamingContextExt is " + nce);
        // inititialize POA
        log.debug("resolving roo_poa");
        root_poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        log.debug("roo_poa is " + root_poa);
        root_poa.the_POAManager().activate();
        log.debug("roo_poa is activated");
        log.debug(" finished & returning from ConnectToORBWithNameServiceProp");

        try {
            log.debug(" creating NamingContext");
            NameComponent[] aNameComponentArray = new NameComponent[1];
            aNameComponentArray[0] = new NameComponent(namingContextExt, "");
            nc = nce.bind_new_context(aNameComponentArray);
            log.debug(" created NamingContext");
        } catch (AlreadyBound e) {
            log.debug("Could not create the context");
        }

        log.debug(" resolving NamingContext");
        org.omg.CORBA.Object aObject = nce.resolve_str(namingContextExt);
        log.debug("NamingContext Object is " + aObject);

        nc = NamingContextHelper.narrow(aObject);

        callbackThread = new Thread(this);
        callbackThread.setDaemon(true);
        callbackThread.start();

        log.debug("NamingContext is " + nc);
    }

    public void run() {
        log.debug("Running the orb");
        orb.run();
    }

    public void finalize() {
        log.debug("Closing");
        orb.shutdown(true);
        orb.destroy();
        try {
            // Wait for thread to join
            callbackThread.join();
        } catch (InterruptedException e) {
            log.error("Could not join with callback: " + e, e);
        }
        log.debug("Closed");
    }

    public POA getRootPoa() {
        return root_poa;
    }

    public NamingContextExtOperations getNamingContextExt() {
        return nce;
    }

    public NamingContextOperations getNamingContext() {
        return nc;
    }

    public ORB getOrb() {
        return orb;
    }
}

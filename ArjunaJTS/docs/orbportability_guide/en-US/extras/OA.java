public abstract class OA {
	public synchronized static RootOA getRootOA(
			ORB associatedORB);

	public synchronized void initPOA()
			throws SystemException;

	public synchronized void initPOA(String[] args)
			throws SystemException;

	public synchronized void initOA()
			throws SystemException;

	public synchronized void initOA(String[] args)
			throws SystemException;

	public synchronized ChildOA createPOA(
			String adapterName, PolicyList policies)
			throws AdapterAlreadyExists, InvalidPolicy;

	public synchronized org.omg.PortableServer.POA rootPoa();

	public synchronized boolean setPoa(
			org.omg.PortableServer.POA thePOA);

	public synchronized org.omg.PortableServer.POA poa(
			String adapterName);

	public synchronized boolean setPoa(String adapterName,
			org.omg.PortableServer.POA thePOA);

	public synchronized boolean addAttribute(OAAttribute p);

	public synchronized void addPreShutdown(OAPreShutdown c);

	public synchronized void addPostShutdown(
			OAPostShutdown c);
}

public class RootOA extends OA {
	public synchronized void destroy()
			throws SystemException;

	public org.omg.CORBA.Object corbaReference(Servant obj);

	public boolean objectIsReady(Servant obj, byte[] id);

	public boolean objectIsReady(Servant obj);

	public boolean shutdownObject(org.omg.CORBA.Object obj);

	public boolean shutdownObject(Servant obj);
}

public class ChildOA extends OA {
	public synchronized boolean setRootPoa(POA thePOA);

	public synchronized void destroy()
			throws SystemException;

	public org.omg.CORBA.Object corbaReference(Servant obj);

	public boolean objectIsReady(Servant obj, byte[] id)
			throws SystemException;

	public boolean objectIsReady(Servant obj)
			throws SystemException;

	public boolean shutdownObject(org.omg.CORBA.Object obj);

	public boolean shutdownObject(Servant obj);
}
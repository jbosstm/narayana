public class ORB {
	public static ORB getInstance(String uniqueId);

	public synchronized void initORB()
			throws SystemException;

	public synchronized void initORB(Applet a, Properties p)
			throws SystemException;

	public synchronized void initORB(String[] s,
			Properties p) throws SystemException;

	public synchronized org.omg.CORBA.ORB orb();

	public synchronized boolean setOrb(
			org.omg.CORBA.ORB theORB);

	public synchronized void shutdown();

	public synchronized boolean addAttribute(Attribute p);

	public synchronized void addPreShutdown(PreShutdown c);

	public synchronized void addPostShutdown(PostShutdown c);

	public synchronized void destroy()
			throws SystemException;

	public void run();

	public void run(String name);
}
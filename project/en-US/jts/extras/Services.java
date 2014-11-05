public class Services {
	/**
	 * The various means used to locate a service.
	 */

	public static final int RESOLVE_INITIAL_REFERENCES = 0;
	public static final int NAME_SERVICE = 1;
	public static final int CONFIGURATION_FILE = 2;
	public static final int FILE = 3;
	public static final int NAMED_CONNECT = 4;
	public static final int BIND_CONNECT = 5;

	public static org.omg.CORBA.Object getService(
			String serviceName, Object[] params,
			int mechanism) throws InvalidName,
			CannotProceed, NotFound, IOException;

	public static org.omg.CORBA.Object getService(
			String serviceName, Object[] params)
			throws InvalidName, CannotProceed, NotFound,
			IOException;

	public static void registerService(
			org.omg.CORBA.Object objRef,
			String serviceName, Object[] params,
			int mechanism) throws InvalidName, IOException,
			CannotProceed, NotFound;

	public static void registerService(
			org.omg.CORBA.Object objRef,
			String serviceName, Object[] params)
			throws InvalidName, IOException, CannotProceed,
			NotFound;
}
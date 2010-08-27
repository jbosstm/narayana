public abstract class XTSATRecoveryManager {
	. . .
 	public static XTSATRecoveryManager getRecoveryManager() ;
 	public void registerRecoveryModule(XTSATRecoveryModule module);
	public abstract void unregisterRecoveryModule(XTSATRecoveryModule module)
		throws NoSuchElementException;
	. . .
}
public abstract class XTSBARecoveryManager {
    . . .
        public static XTSBARecoveryManager getRecoveryManager() ;
    public void registerRecoveryModule(XTSBARecoveryModule module);
    public abstract void unregisterRecoveryModule(XTSBARecoveryModule module)
        throws NoSuchElementException;
    . . .
}

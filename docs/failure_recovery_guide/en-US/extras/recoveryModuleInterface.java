public interface RecoveryModule {
	/**
	 * Called by the RecoveryManager at start up, and then
	 * PERIODIC_RECOVERY_PERIOD seconds after the completion, for all
	 * RecoveryModules of the second pass
	 */
	public void periodicWorkFirstPass();

	/**
	 * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds after the
	 * completion of the first pass
	 */
	public void periodicWorkSecondPass();
}

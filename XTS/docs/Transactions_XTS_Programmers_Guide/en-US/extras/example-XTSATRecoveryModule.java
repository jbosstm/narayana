public interface XTSATRecoveryModule
{
    public Durable2PCParticipant
	deserialize(String id, ObjectInputStream stream) 
	  throws Exception;
    public Durable2PCParticipant
	recreate(String id, byte[] recoveryState) 
	  throws Exception;
}
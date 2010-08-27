public interface XTSATRecoveryModule
{
    public BusinessAgreementWithParticipantCompletionParticipant
	deserializeParticipantCompletionParticipant(String id,
						    ObjectInputStream stream)
	throws Exception;
    public BusinessAgreementWithParticipantCompletionParticipant
	recreateParticipantCompletionParticipant(String id,
						 byte[] recoveryState)
	throws Exception;
    public BusinessAgreementWithCoordinatorCompletionParticipant
	deserializeCoordinatorCompletionParticipant(String id,
						    ObjectInputStream stream)
	throws Exception;
    public BusinessAgreementWithCoordinatorCompletionParticipant
	recreateCoordinatorCompletionParticipant(String id,
						 byte[] recoveryState)
	throws Exception;
}

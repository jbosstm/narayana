package com.arjuna.webservices11.wsat.processors;

import com.arjuna.wst11.CompletionCoordinatorParticipant;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsaddr.map.MAP;

import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

/**
 * The Completion Coordinator processor.
 * @author kevin
 */
public abstract class CompletionCoordinatorProcessor
{
    /**
     * The coordinator processor.
     */
    private static CompletionCoordinatorProcessor PROCESSOR ;

    /**
     * Get the processor.
     * @return The processor.
     */
    public static synchronized CompletionCoordinatorProcessor getProcessor()
    {
        return PROCESSOR;
    }

    /**
     * Set the processor.
     * @param processor The processor.
     * @return The previous processor.
     */
    public static synchronized CompletionCoordinatorProcessor setProcessor(final CompletionCoordinatorProcessor processor)
    {
        final CompletionCoordinatorProcessor origProcessor = PROCESSOR;
        PROCESSOR = processor ;
        return origProcessor ;
    }

    /**
     * Activate the participant.
     * @param participant The participant.
     * @param identifier The identifier.
     */
    public abstract void activateParticipant(final CompletionCoordinatorParticipant participant, final String identifier) ;

    /**
     * Deactivate the participant.
     * @param participant The participant.
     */
    public abstract void deactivateParticipant(final CompletionCoordinatorParticipant participant) ;

    /**
     * Commit.
     * @param commit The commit notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void commit(final Notification commit, final MAP map,
        final ArjunaContext arjunaContext) ;

    /**
     * Rollback.
     * @param rollback The rollback notification.
     * @param map The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void rollback(final Notification rollback, final MAP map,
        final ArjunaContext arjunaContext) ;
}

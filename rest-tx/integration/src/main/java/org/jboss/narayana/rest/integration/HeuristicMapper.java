package org.jboss.narayana.rest.integration;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.api.HeuristicException;
import org.jboss.narayana.rest.integration.api.HeuristicType;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@Provider
public class HeuristicMapper implements ExceptionMapper<HeuristicException> {

    @Override
    public Response toResponse(HeuristicException exception) {
        final String status = heuristicTypeToTxStatus(exception.getHeuristicType());

        return Response.ok().entity(TxSupport.toStatusContent(status)).build();
    }

    private String heuristicTypeToTxStatus(final HeuristicType heuristicType) {
        switch (heuristicType) {
            case HEURISTIC_COMMIT:
                return TxStatus.TransactionHeuristicCommit.name();

            case HEURISTIC_HAZARD:
                return TxStatus.TransactionHeuristicHazard.name();

            case HEURISTIC_MIXED:
                return TxStatus.TransactionHeuristicMixed.name();

            case HEURISTIC_ROLLBACK:
                return TxStatus.TransactionHeuristicRollback.name();
        }

        throw new IllegalArgumentException("Unknown heuristic type");
    }
}

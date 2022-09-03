package org.jboss.narayana.rest.integration.test.integration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.narayana.rest.integration.test.common.HeuristicParticipant;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.narayana.rest.integration.test.common.LoggingVolatileParticipant;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * TODO implement test for heuristics after prepare.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunWith(Arquillian.class)
public final class BasicIntegrationTestCase extends AbstractIntegrationTestCase {

    private static final String APPLICATION_ID = "org.jboss.narayana.rest.integration.test.integration.BasicIntegrationTestCase";

    private static final String DEPENDENCIES = "Dependencies: org.jboss.narayana.rts\n";

    @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = true)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addClass(AbstractIntegrationTestCase.class)
                .addClass(LoggingParticipant.class)
                .addClass(LoggingVolatileParticipant.class)
                .addClass(HeuristicParticipant.class)
                .addAsWebInfResource(new File("web.xml"), "web.xml")
                .addAsManifestResource(new StringAsset(DEPENDENCIES), "MANIFEST.MF");
    }

    @Before
    public void before() {
        super.before();
        startContainer();
        ParticipantsManagerFactory.getInstance().setBaseUrl(BASE_URL);
    }

    @Test
    public void testCommit() {
        txSupport.startTx();

        LoggingParticipant participant1 = new LoggingParticipant(new Prepared());
        LoggingParticipant participant2 = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), participant1);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), participant2);

        txSupport.commitTx();

        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participant1.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participant2.getInvocations());
    }

    @Test
    public void testCommitOnePhase() {
        txSupport.startTx();

        LoggingParticipant participant = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                participant);

        txSupport.commitTx();

        Assert.assertEquals(Arrays.asList(new String[] { "commitOnePhase" }), participant.getInvocations());
    }

    @Test
    public void testReadOnly() {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new ReadOnly()), new LoggingParticipant(new Prepared()),
                new LoggingParticipant(new Prepared()) });

        for (LoggingParticipant p : participants) {
            ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        txSupport.commitTx();

        // One of the participants was only prepared, while other two were prepared and commited.
        Assert.assertEquals(5, participants.get(0).getInvocations().size() + participants.get(1).getInvocations().size()
                + participants.get(2).getInvocations().size());

        for (LoggingParticipant p : participants) {
            if (p.getInvocations().size() == 1) {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare" }), p.getInvocations());
            } else {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), p.getInvocations());
            }
        }
    }

    @Test
    public void testRollback() {
        txSupport.startTx();

        LoggingParticipant participant1 = new LoggingParticipant(new Prepared());
        LoggingParticipant participant2 = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), participant1);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), participant2);

        txSupport.rollbackTx();

        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant1.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participant2.getInvocations());
    }

    @Test
    public void testRollbackByParticipant() {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Aborted()), new LoggingParticipant(new Aborted()), });

        for (LoggingParticipant p : participants) {
            ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        txSupport.commitTx();

        // One of the participants was prepared and then decided to rollback, the other was rolledback straight away.
        Assert.assertEquals(3, participants.get(0).getInvocations().size() + participants.get(1).getInvocations().size());

        for (LoggingParticipant p : participants) {
            if (p.getInvocations().size() == 1) {
                Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), p.getInvocations());
            } else {
                Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), p.getInvocations());
            }
        }
    }

    @Test
    public void testHeuristicRollbackBeforePrepare() throws JAXBException {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Prepared()), new LoggingParticipant(new Prepared()) });

        String lastParticipantid = null;

        for (LoggingParticipant p : participants) {
            lastParticipantid = ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                    txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        ParticipantsManagerFactory.getInstance().reportHeuristic(lastParticipantid, HeuristicType.HEURISTIC_ROLLBACK);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), txStatus);

        if (participants.get(0).getInvocations().size() == 1) {
            Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), participants.get(0).getInvocations());
        } else {
            Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), participants.get(0).getInvocations());
        }
    }

    @Test
    public void testSecondParticipantHeuristicCommitWithFirstParticipantSuccessfullPrepare() throws JAXBException {
        txSupport.startTx();

        final List<LoggingParticipant> participants = Arrays.asList(new LoggingParticipant[] {
                new LoggingParticipant(new Prepared()), new LoggingParticipant(new Prepared()) });

        String lastParticipantid = null;

        for (LoggingParticipant p : participants) {
            lastParticipantid = ParticipantsManagerFactory.getInstance().enlist(
                    APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), p);
        }

        ParticipantsManagerFactory.getInstance().reportHeuristic(lastParticipantid, HeuristicType.HEURISTIC_COMMIT);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionCommitted.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), participants.get(0).getInvocations());
        Assert.assertEquals(Collections.EMPTY_LIST, participants.get(1).getInvocations());
    }

    @Test
    public void testSecondParticipantHeuristicCommitWithFirstParticipantUnsuccessfullPrepare() throws JAXBException {
        txSupport.startTx();

        LoggingParticipant loggingParticipant1 = new LoggingParticipant(new Aborted());
        LoggingParticipant loggingParticipant2 = new LoggingParticipant(new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                loggingParticipant1);
        String lastParticipantid = ParticipantsManagerFactory.getInstance().enlist(
                APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(), loggingParticipant2);

        ParticipantsManagerFactory.getInstance().reportHeuristic(lastParticipantid, HeuristicType.HEURISTIC_COMMIT);

        System.out.println(ParticipantsContainer.getInstance().getParticipantInformation(lastParticipantid).getStatus());

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionHeuristicCommit.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }),loggingParticipant1.getInvocations());
        Assert.assertEquals(Collections.EMPTY_LIST, loggingParticipant2.getInvocations());
    }

    @Test
    public void testHeuristicCommitAfterSuccessfullPrepare() {
        txSupport.startTx();

        LoggingParticipant loggingParticipant = new LoggingParticipant(new Prepared());
        HeuristicParticipant heuristicParticipant = new HeuristicParticipant(HeuristicType.HEURISTIC_COMMIT, new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                heuristicParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionCommitted.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), heuristicParticipant.getInvocations());
    }

    @Test
    public void testHeuristicCommitAfterUnsuccessfullPrepare() {
        txSupport.startTx();

        LoggingParticipant loggingParticipant = new LoggingParticipant(new Aborted());
        HeuristicParticipant heuristicParticipant = new HeuristicParticipant(HeuristicType.HEURISTIC_COMMIT, new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                heuristicParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionHeuristicCommit.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), heuristicParticipant.getInvocations());
    }

    @Test
    public void testHeuristicRollbackAfterSuccessfullPrepare() {
        txSupport.startTx();

        LoggingParticipant loggingParticipant = new LoggingParticipant(new Prepared());
        HeuristicParticipant heuristicParticipant = new HeuristicParticipant(HeuristicType.HEURISTIC_ROLLBACK, new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                heuristicParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionHeuristicMixed.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "commit" }), heuristicParticipant.getInvocations());
    }

    @Test
    public void testHeuristicRollbackAfterUnsuccessfullPrepare() {
        txSupport.startTx();

        LoggingParticipant loggingParticipant = new LoggingParticipant(new Aborted());
        HeuristicParticipant heuristicParticipant = new HeuristicParticipant(HeuristicType.HEURISTIC_ROLLBACK, new Prepared());

        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, txSupport.getDurableParticipantEnlistmentURI(),
                heuristicParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "prepare", "rollback" }), heuristicParticipant.getInvocations());
    }

    @Test
    public void testCommitWithVolatileParticipant() {
        txSupport.startTx();
        LoggingParticipant loggingParticipant = new LoggingParticipant(new Prepared());
        LoggingVolatileParticipant loggingVolatileParticipant = new LoggingVolatileParticipant();
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                txSupport.getDurableParticipantEnlistmentURI(), loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlistVolatileParticipant(
                txSupport.getVolatileParticipantEnlistmentURI(), loggingVolatileParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.commitTx());

        Assert.assertEquals(TxStatus.TransactionCommitted.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "commitOnePhase" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "beforeCompletion", "afterCompletion" }),
                loggingVolatileParticipant.getInvocations());
        Assert.assertEquals(TxStatus.TransactionCommitted, loggingVolatileParticipant.getTxStatus());
    }

    @Test
    public void testRollbackWithVolatileParticipant() {
        txSupport.startTx();
        LoggingParticipant loggingParticipant = new LoggingParticipant(new Prepared());
        LoggingVolatileParticipant loggingVolatileParticipant = new LoggingVolatileParticipant();
        ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID,
                txSupport.getDurableParticipantEnlistmentURI(), loggingParticipant);
        ParticipantsManagerFactory.getInstance().enlistVolatileParticipant(
                txSupport.getVolatileParticipantEnlistmentURI(), loggingVolatileParticipant);

        final String txStatus = TxSupport.getStatus(txSupport.rollbackTx());

        Assert.assertEquals(TxStatus.TransactionRolledBack.name(), txStatus);
        Assert.assertEquals(Arrays.asList(new String[] { "rollback" }), loggingParticipant.getInvocations());
        Assert.assertEquals(Arrays.asList(new String[] { "afterCompletion" }),
                loggingVolatileParticipant.getInvocations());
        Assert.assertEquals(TxStatus.TransactionRolledBack, loggingVolatileParticipant.getTxStatus());
    }

}

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class BytemanControlledXAResource implements XAResource {

    private static String _fileName = "BytemanControlledXAResource.bin";
    private static int _xaExceptionCode;

    public static void setCommitReturn(int commitExceptionCode) {
        BytemanControlledXAResource._xaExceptionCode = commitExceptionCode;
    }

    /* This is a counter used internally in BytemanControlledXAResource to count how many
     * times the XAResource.commit() method was invoked.
     *
     * NOTE: AtomicInteger here is an overkill as BytemanControlledXAResource is only used
     * during tests and, as a consequence, there won't be two threads calling commit in
     * parallel.
     * Also, the recovery thread won't invoke commit in parallel to the test thread,
     * so there isn't any risk in declaring this field as int.
     * Nevertheless, it's good practice to show that this field should be declared as an
     * AtomicInteger in a production environment as multiple threads could handle this field
     * concurrently.
     */
    private static final AtomicInteger _commitCallCounter = new AtomicInteger(0);

    public static int getCommitCallCounter() {
        return BytemanControlledXAResource._commitCallCounter.get();
    }

    public static void resetCommitCallCounter() {
        BytemanControlledXAResource._commitCallCounter.set(0);
    }

    /* This is a counter used internally in BytemanControlledXAResource to count how many
     * times the "XAResource.prepare()" method was invoked.
     */
    private static final AtomicInteger _prepareCallCounter = new AtomicInteger(0);

    public static int getPrepareCallCounter() {
        return BytemanControlledXAResource._prepareCallCounter.get();
    }

    public static void resetPrepareCallCounter() {
        BytemanControlledXAResource._prepareCallCounter.set(0);
    }

    /* This is a counter used internally in BytemanControlledXAResource to count how many
     * times the "XAResource.recover()" method was invoked.
     */
    private static final AtomicInteger _recoverCallCounter = new AtomicInteger(0);

    public static int getRecoverCallCounter() {
        return BytemanControlledXAResource._recoverCallCounter.get();
    }

    public static void resetRecoverCallCounter() {
        BytemanControlledXAResource._recoverCallCounter.set(0);
    }

    /* This is a counter used internally in BytemanControlledXAResource to count how many
     * times the "XAResource.prepare()" method was invoked.
     */
    private static final AtomicInteger _rollbackCallCounter = new AtomicInteger(0);

    public static int getRollbackCallCounter() {
        return BytemanControlledXAResource._rollbackCallCounter.get();
    }

    public static void resetRollbackCallCounter() {
        BytemanControlledXAResource._rollbackCallCounter.set(0);
    }

    // Executed in the byteman script recoverySuspend.btm
    public static void resetGreenFlag() {
        // Artificially reset greenFlag
    }

    // Executed in the byteman script recoverySuspend.btm
    public static void setGreenFlag() {
        // Artificially set greenFlag to skip failures
    }

    // no timeout
    private int _timeout = 0;

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        BytemanControlledXAResource._commitCallCounter.getAndIncrement();

        deleteFile();
    }

    @Override
    public void end(Xid xid, int i) throws XAException {

    }

    @Override
    public void forget(Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return _timeout;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return xaResource == this;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        BytemanControlledXAResource._prepareCallCounter.getAndIncrement();

        try {
            final int formatId = xid.getFormatId();
            final byte[] gtrid = xid.getGlobalTransactionId();
            final int gtrid_length = gtrid.length;
            final byte[] bqual = xid.getBranchQualifier();
            final int bqual_length = bqual.length;

            File file = new File(BytemanControlledXAResource._fileName);
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
            fos.writeInt(formatId);
            fos.writeInt(gtrid_length);
            fos.write(gtrid, 0, gtrid_length);
            fos.writeInt(bqual_length);
            fos.write(bqual, 0, bqual_length);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new XAException(XAException.XAER_RMERR);
        }

        return XA_OK;
    }

    @Override
    public Xid[] recover(int flag) throws XAException {

        if (flag == XAResource.TMSTARTRSCAN) {

            _recoverCallCounter.getAndIncrement();

            File file = new File(BytemanControlledXAResource._fileName);

            if (file.exists()) {
                try {
                    DataInputStream fis = new DataInputStream(new FileInputStream(file));
                    final int formatId = fis.readInt();
                    final int gtrid_length = fis.readInt();
                    final byte[] gtrid = new byte[gtrid_length];
                    fis.read(gtrid, 0, gtrid_length);
                    final int bqual_length = fis.readInt();
                    final byte[] bqual = new byte[bqual_length];
                    fis.read(bqual, 0, bqual_length);
                    fis.close();

                    return new Xid[]{new Xid() {

                        @Override
                        public byte[] getGlobalTransactionId() {
                            return gtrid;
                        }

                        @Override
                        public int getFormatId() {
                            return formatId;
                        }

                        @Override
                        public byte[] getBranchQualifier() {
                            return bqual;
                        }
                    }};
                } catch (IOException e) {
                    throw new XAException(XAException.XAER_RMERR);
                }
            }
        }

        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        BytemanControlledXAResource._rollbackCallCounter.getAndIncrement();

        deleteFile();
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        _timeout = seconds;
        return true;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {

    }

    private void deleteFile() throws XAException {
        File file = new File(BytemanControlledXAResource._fileName);
        if (!file.exists()) {
            throw new XAException(XAException.XAER_RMERR);
        } else {
            file.delete();
        }
    }
}

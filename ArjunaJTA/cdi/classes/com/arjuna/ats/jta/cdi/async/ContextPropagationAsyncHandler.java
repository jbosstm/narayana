/*
 * Copyright 2020, Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arjuna.ats.jta.cdi.async;

import com.arjuna.ats.jta.cdi.RunnableWithException;
import com.arjuna.ats.jta.cdi.TransactionHandler;
import io.smallrye.reactive.converters.ReactiveTypeConverter;
import io.smallrye.reactive.converters.Registry;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handling asynchronous context propagation calls.
 * It extends transactions until the intercepted method's async return type is completed.
 */
public final class ContextPropagationAsyncHandler {
    private static final Logger log = Logger.getLogger(ContextPropagationAsyncHandler.class);

    // check if classes of reactive streams from smallrye for transaction asynchronous handling are available on classpath
    private static final boolean areSmallRyeReactiveClassesAvailable = areSmallRyeReactiveClassesAvailable();

    /**
     * <p>
     * Tries to handle asynchronously the returned type from the @{@link Transactional} call.
     * This CDI interceptor method checks the return type intercepted method.
     * If it's a asynchronous "type" (<code>objectToHandle</code> is <codde>instanceof</code> e.g. {@link CompletionStage})
     * then the transaction completion (committing/roll-backing) will be suspended
     * until the asynchronous code finishes.
     * </p>
     * <p>
     * If the interceptor returns nothing or just some "normal" return type then synchronous handling is processed.
     * Synchronous means that the transaction is completed just here when the method annotated with @{@link Transactional}
     * ends.
     * </p>
     *
     * @param tm  transaction manager
     * @param tx  the original transaction
     * @param transactional  link to method which is annotated with @{@link Transactional}
     * @param objectToHandleRef  on interceptor proceed this is the returned type which differentiate the action;
     *                        method changes the object when it was handled asynchronously
     * @param returnType
     * @param afterEndTransaction  a lamda invocation on transaction finalization
     * @return @{code true} if async handling is possible and it was proceeded, @{code false} means async processing is not possible;
     *   the method changes the value referenced by @{code objectToHandleRef}, when @{code true} is returned
     * @throws Exception failure on async processing error happens
     */
    public static boolean tryHandleAsynchronously(
            TransactionManager tm, Transaction tx, Transactional transactional, AtomicReference objectToHandleRef,
            Class<?> returnType, RunnableWithException afterEndTransaction) throws Exception {

        Object objectToHandle = objectToHandleRef.get();
        if (objectToHandle == null) {
            return false;
        }
        if (objectToHandle instanceof CompletionStage) {
            // checking if the returned type is CompletionStage, it's certain to be s on classpath as it's under JDK java.util.concurrent package
            objectToHandle = handleAsync(tm, tx, transactional, objectToHandle, afterEndTransaction);
        } else if (objectToHandle instanceof CompletionStage == false && areSmallRyeReactiveClassesAvailable) {
            // the smallrye reactive classes and converter utility class are available, trying to convert the returned object to the known async type
            ReactiveTypeConverter<Object> converter = null;
            if (objectToHandle instanceof Publisher == false || returnType != Publisher.class) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Optional<ReactiveTypeConverter<Object>> lookup = Registry.lookup((Class) objectToHandle.getClass());
                if (lookup.isPresent()) {
                    converter = lookup.get();
                    if (converter.emitAtMostOneItem()) {
                        objectToHandle = converter.toCompletionStage(objectToHandle);
                    } else {
                        objectToHandle = converter.toRSPublisher(objectToHandle);
                    }
                }
            }
            if (objectToHandle instanceof CompletionStage) {
                objectToHandle = handleAsync(tm, tx, transactional, objectToHandle, afterEndTransaction);
                // convert back
                if (converter != null)
                    objectToHandle = converter.fromCompletionStage((CompletionStage<?>) objectToHandle);
            } else if (objectToHandle instanceof Publisher) {
                objectToHandle = handleAsync(tm, tx, transactional, objectToHandle, afterEndTransaction);
                // convert back
                if (converter != null)
                    objectToHandle = converter.fromPublisher((Publisher<?>) objectToHandle);
            } else {
                // no way to handle the object as known asynchronous type
                return false;
            }
        } else {
            // we are not able to handle the returned type as asynchronous
            return false;
        }
        // the returned type is the asynchronous type and the type was handled
        objectToHandleRef.set(objectToHandle);
        return true;
    }

    private static Object handleAsync(TransactionManager tm, Transaction tx, Transactional transactional, Object ret, RunnableWithException afterEndTransaction) throws Exception {
        // Suspend the transaction to remove it from the main request thread
        tm.suspend();
        afterEndTransaction.run();
        if (ret instanceof CompletionStage) {
            return ((CompletionStage<?>) ret).handle((v, throwable) -> {
                try {
                    doInTransaction(tm, tx, () -> {
                        if (throwable != null) {
                            TransactionHandler.handleExceptionNoThrow(transactional, throwable, tx);
                        }
                        TransactionHandler.endTransaction(tm, tx, () -> {});
                    });
                } catch (RuntimeException e) {
                    if (throwable != null)
                        e.addSuppressed(throwable);
                    throw e;
                } catch (Exception e) {
                    CompletionException x = new CompletionException(e);
                    if (throwable != null)
                        x.addSuppressed(throwable);
                    throw x;
                }
                // pass-through the previous results
                if (throwable instanceof RuntimeException)
                    throw (RuntimeException) throwable;
                if (throwable != null)
                    throw new CompletionException(throwable);
                return v;
            });
        } else if (ret instanceof Publisher) {
            ret = ReactiveStreams.fromPublisher(((Publisher<?>) ret))
                    .onError(throwable -> {
                        try {
                            doInTransaction(tm, tx, () -> TransactionHandler.handleExceptionNoThrow(transactional, throwable, tx));
                        } catch (RuntimeException e) {
                            e.addSuppressed(throwable);
                            throw e;
                        } catch (Exception e) {
                            RuntimeException x = new RuntimeException(e);
                            x.addSuppressed(throwable);
                            throw x;
                        }
                        // pass-through the previous result
                        if (throwable instanceof RuntimeException)
                            throw (RuntimeException) throwable;
                        throw new RuntimeException(throwable);
                    }).onTerminate(() -> {
                        try {
                            doInTransaction(tm, tx, () -> TransactionHandler.endTransaction(tm, tx, () -> {
                            }));
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            RuntimeException x = new RuntimeException(e);
                            throw x;
                        }
                    })
                    .buildRs();
        }
        return ret;
    }

    private static void doInTransaction(TransactionManager tm, Transaction tx, RunnableWithException f) throws Exception {
        // Verify if this thread's transaction is the right one
        Transaction currentTransaction = tm.getTransaction();
        // If not, install the right transaction
        if (currentTransaction != tx) {
            if (currentTransaction != null)
                tm.suspend();
            tm.resume(tx);
        }
        f.run();
        if (currentTransaction != tx) {
            tm.suspend();
            if (currentTransaction != null)
                tm.resume(currentTransaction);
        }
    }

    /**
     * <p>
     * Checks if classes from the following maven artifacts are available on classpath
     * <ul>
     *   <li>io.smallrye.reactive:smallrye-reactive-converter-api (WildFly module io.smallrye.reactive.streams-operators)</li>
     *   <li>org.eclipse.microprofile.reactive-streams-operators.microprofile-reactive-streams-operators-api
     *       (WildFly module org.eclipse.microprofile.reactive-streams-operators.api)</li>
     *   <li>org.reactivestreams:reactive-streams (Wildfly module org.reactivestreams)</li>
     * </ul>
     * </p>
     *
     * @return true if the classes are available on classpath, false otherwise
     */
    private static boolean areSmallRyeReactiveClassesAvailable() {
        return Arrays.asList(
                "io.smallrye.reactive.converters.ReactiveTypeConverter",
                "io.smallrye.reactive.converters.Registry",
                "org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams",
                "org.reactivestreams.Publisher")
                .stream().allMatch(className -> {
                    try {
                        Class.forName(className);
                    } catch (ClassNotFoundException cnfe) {
                        log.debugf("Class %s is not available on classpath. Handling of asynchronous types in @Transactional " +
                                "methods could not work properly. Consider to add java artifacts " +
                                "'org.eclipse.microprofile.reactive-streams-operators.microprofile-reactive-streams-operators-api', " +
                                "'org.reactivestreams:reactive-streams' and  'io.smallrye.reactive:smallrye-reactive-converter-api' " +
                                "to your classpath", className);
                        return false;
                    }
                    return true;
                });
    }
}

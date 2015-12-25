# Vert.x STM Module

A module that exposes the Narayana STM implementation for Vert.x applications.

Documentation is a bit light at the moment, but the following links provide some background.

http://jbossts.blogspot.co.uk/2011/06/stm-arjuna.html

http://jbossts.blogspot.co.uk/2012/02/optimistic-stm.html

http://jbossts.blogspot.co.uk/2012/03/update-to-stm-api.html

http://jbossts.blogspot.co.uk/2013/03/stm-vertx-and-pi-part-1.html

In a nutshell:

(i) create an interface that defines the type(s) of your STM implementations. Annotate this using
@Transactional You can also use the @Optimistic annotation to choose to have optimistic concurrency
control for instances of the classes derived from the interface. (Default value is @Pessimistic).

(ii) instrument the interface methods with @WriteLock or @ReadLock. (Default value is @WriteLock).

(iii) define class(es) derives from the interface. Use the @NotState to select variables which are not
to be serialised as part of the transactional updates. (Default is to serialise everything). You can use
the @State annotation to make it explicit.

(iv) create a Container that can manage instances of the interface defined in step (i). This is your
"transactional memory pool". Create instances of the classes from step (iii) and pass them to the Container.

(v) manipulate the objects returned from the Container in step (iv) within the scope of AtomicActions.

Note, this is NOT distributed transactions. No transaction context will flow between address spaces.
No interposition will occur if you manage to serialise and distribute a transaction context. Distributed
transactions could be added, but since 2PC is a blocking protocol it doesn't necessarily fit naturally
with Vert.x. Perhaps something based on forward compensations, which are not blocking. However, that then
introduces a trade-off between consistency and performance. Definitely a TODO.
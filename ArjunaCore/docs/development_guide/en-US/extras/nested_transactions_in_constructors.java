AtomicAction A = new AtomicAction();
Object obj1;
Object obj2;

obj1 = new Object();       // create new object
obj2 = new Object("old");     // existing object

A.begin(0);
obj2.remember(obj1.get_uid());   // obj2 now contains reference to obj1
A.commit(true);            // obj2 saved but obj1 is not

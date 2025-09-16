AtomicAction A = new AtomicAction();
Object obj1;
Object obj2;

// create new object
obj1 = new Object();
// existing object
obj2 = new Object("old");

A.begin(0);
// obj2 now contains reference to obj1
obj2.remember(obj1.get_uid());
// obj2 saved but obj1 is not
A.commit(true);

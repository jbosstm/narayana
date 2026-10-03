try
    {
        boolean success = false;
        AtomicAction A = new AtomicAction();
        OnePhase opRes = new OnePhase();  // used OnePhase interface
       
        System.err.println("Starting top-level action.");

        A.begin();
        A.add(new LastResourceRecord(opRes));
        A.add(new ShutdownRecord(ShutdownRecord.FAIL_IN_PREPARE));
       
        A.commit();
    }
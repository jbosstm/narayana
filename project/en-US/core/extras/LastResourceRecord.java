try
    {
        boolean success = false;
        AtomicAction A = new AtomicAction();
        OnePhase opRes = new OnePhase();  // used OnePhase interface
       
        System.out.println("Starting top-level action.");

        A.begin();
        A.add(new LastResourceRecord(opRes));
        A.add( "other participants" );
       
        A.commit();
    }

try {
    boolean success = false;
    AtomicAction A = new AtomicAction();
    // used OnePhase interface
    OnePhase opRes = new OnePhase();

    System.out.println("Starting top-level action.");

    A.begin();
    A.add(new LastResourceRecord(opRes));
    A.add("other participants");

    A.commit();
}

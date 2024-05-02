public boolean increment(Control control) {
    ExplicitInterposition inter = new ExplicitInterposition();

    try {
        inter.registerTransaction(control);
    } catch (Exception e) {
        return false;
    }

    // do real work

    // should catch exceptions!
    inter.unregisterTransaction();

    // return value dependant upon outcome
}

{
    ...
    /* (i) bind to "old" persistent object A */
    O1 objct1 = new objct1(Name - A);
    /* create a "new" persistent object */
    O2 objct2 = new objct2();
    /* (ii) start of atomic action */
    OTS.current().begin();
    /* (iii) object activation and invocations */
    objct1.op(...);
    objct2.op(...);
    ...
    /* (iv) tx commits & objects deactivated */
    OTS.current().commit(true);
}
/* (v) */

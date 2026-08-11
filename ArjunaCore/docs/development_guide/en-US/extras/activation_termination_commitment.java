{
    . . .
    O1 objct1 = new objct1(Name-A);/* (i) bind to "old" persistent object A */
    O2 objct2 = new objct2();  /* create a "new" persistent object */
    OTS.current().begin();     /* (ii) start of atomic action */

    objct1.op(...);           /* (iii) object activation and invocations */
    objct2.op(...);
    . . .
    OTS.current().commit(true);  /* (iv) tx commits & objects deactivated */
}              /* (v) */
Transaction txObj = TransactionManager.getTransaction();
Transaction someOtherTxObj = ..
..

boolean isSame = txObj.equals(someOtherTxObj);      

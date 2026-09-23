//get UserTransaction 
UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();
// start transaction work..
utx.begin();

// perform transactional work

utx.commit();
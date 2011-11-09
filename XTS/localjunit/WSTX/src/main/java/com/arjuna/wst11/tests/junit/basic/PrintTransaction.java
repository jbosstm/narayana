package com.arjuna.wst11.tests.junit.basic;

import javax.inject.Named;

import com.arjuna.mw.wst11.UserTransaction;

@Named
public class PrintTransaction {
	public void testPrintTransaction()
	throws Exception
	{
		UserTransaction ut = UserTransaction.getUserTransaction();

		ut.begin();

		System.out.println("Started: "+ut);

		ut.commit();

		System.out.println("\nCurrent: "+ut);

	}
}
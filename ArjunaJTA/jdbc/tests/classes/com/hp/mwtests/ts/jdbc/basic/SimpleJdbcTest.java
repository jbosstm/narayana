package com.hp.mwtests.ts.jdbc.basic;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.UserTransaction;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class SimpleJdbcTest {
	private static final String DB_USER1 = "dtf11";
	private static final String DB_USER2 = "dtf12";
	private static final String DB_HOST = "tywin.buildnet.ncl.jboss.com";
	private static final String DB_SID = "orcl";

	@Test
	public void test() throws Exception {
		arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
		System.setProperty("java.naming.factory.initial",
				"org.apache.naming.java.javaURLContextFactory");
		System.setProperty("java.naming.factory.url.pkgs", "org.apache.naming");
		final DataSource dataSource1 = getDataSource(DB_USER1, "oracle: "
				+ DB_USER1);
		final DataSource dataSource2 = getDataSource(DB_USER2, "oracle: "
				+ DB_USER2);

		prepare(dataSource1);
		prepare(dataSource2);

		final UserTransaction userTransaction = com.arjuna.ats.jta.UserTransaction
				.userTransaction();
		userTransaction.begin();
		
		final Connection connection1 = dataSource1.getConnection();
		final Connection connection2 = dataSource2.getConnection();

		insert(connection1);
		insert(connection2);

		userTransaction.commit();
	}

	private static void insert(Connection connection) throws SQLException {
		final PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO jta_test (some_string) VALUES ('test')");
		preparedStatement.execute();
	}

	private static void prepare(DataSource dataSource) throws SQLException {
		final Connection connection = dataSource.getConnection();
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM user_tables WHERE table_name = 'JTA_TEST'");
		final ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			connection.prepareStatement("DROP TABLE jta_test").execute();
		}
		connection.prepareStatement(
				"CREATE TABLE jta_test (some_string VARCHAR2(10))").execute();
	}

	private static DataSource getDataSource(String user, String resourceName)
			throws NamingException, SQLException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		InitialContext initialContext = prepareInitialContext();

		Class clazz = Class.forName("oracle.jdbc.xa.client.OracleXADataSource");
		XADataSource xaDataSource = (XADataSource) clazz.newInstance();
		clazz.getMethod("setDriverType", new Class[] { String.class }).invoke(
				xaDataSource, new Object[] { "thin" });
		clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
				xaDataSource, new Object[] { DB_HOST });
		clazz.getMethod("setNetworkProtocol", new Class[] { String.class })
				.invoke(xaDataSource, new Object[] { "tcp" });
		clazz.getMethod("setDatabaseName", new Class[] { String.class })
				.invoke(xaDataSource, new Object[] { DB_SID });
		clazz.getMethod("setUser", new Class[] { String.class }).invoke(
				xaDataSource, new Object[] { user });
		clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
				xaDataSource, new Object[] { user });
		clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
				xaDataSource, new Object[] { 1521 });

		final String name = "java:/comp/env/jdbc/" + user;
		initialContext.bind(name, xaDataSource);

		DriverManagerDataSource dataSource = new DriverManagerDataSource(
				"jdbc:arjuna:" + name);
		dataSource
				.setDriverClassName("com.arjuna.ats.jdbc.TransactionalDriver");

		return dataSource;
	}

	private static InitialContext prepareInitialContext()
			throws NamingException {
		final InitialContext initialContext = new InitialContext();

		try {
			initialContext.lookup("java:/comp/env/jdbc");
		} catch (NamingException ne) {
			initialContext.createSubcontext("java:");
			initialContext.createSubcontext("java:/comp");
			initialContext.createSubcontext("java:/comp/env");
			initialContext.createSubcontext("java:/comp/env/jdbc");
		}

		return initialContext;
	}
}
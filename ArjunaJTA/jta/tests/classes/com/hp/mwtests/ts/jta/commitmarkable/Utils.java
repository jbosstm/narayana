/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2013
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.commitmarkable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class Utils {
	public static void createTables(Connection connection) throws SQLException {
        try {
            String driverName = connection.getMetaData().getDriverName();
            int index = driverName.indexOf(' ');
            if (index != -1)
                driverName = driverName.substring(0, index);
            driverName = driverName.replaceAll("-", "_");
            driverName = driverName.toLowerCase();

            Statement statement = connection.createStatement();
            if (driverName.equals("jconnect")) {
                try {
                    statement.execute("drop table xids");
                } catch (SQLException e) {
                    if (e.getErrorCode() != 3701) {
                        throw e;
                    }
                }
                statement
                        .execute("create table xids (xid varbinary(144), transactionManagerID varchar(64), actionuid varbinary(28))");
                try {
                    statement.execute("drop table foo");
                } catch (SQLException e) {
                    if (e.getErrorCode() != 3701) {
                        throw e;
                    }
                }
            } else if (driverName.equals("oracle")) {
                try {
                    statement.execute("drop table xids");
                    statement.execute("drop index index_xid on xids");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("42000")
                            && ex.getErrorCode() != 942) {
                        throw ex;
                    }
                }
                statement
                        .execute("create table xids (xid RAW(144), transactionManagerID varchar(64), actionuid RAW(28))");

                statement.execute("create unique index index_xid on xids (xid)");
                try {
                    statement.execute("drop table foo");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("42000")
                            && ex.getErrorCode() != 942) {
                        throw ex;
                    }
                }
            } else if (driverName.equals("ibm")) {
                try {
                    statement.execute("drop table xids");
                    statement.execute("drop index index_xid");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("42704")
                            && ex.getErrorCode() != -204) {
                        throw ex;
                    }
                }
                statement
                        .execute("create table xids (xid VARCHAR(255), transactionManagerID varchar(64), actionuid VARCHAR(255))");

                statement.execute("create unique index index_xid on xids (xid)");
                try {
                    statement.execute("drop table foo");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("42704")
                            && ex.getErrorCode() != -204) {
                        throw ex;
                    }
                }
            } else if (driverName.equals("microsoft")) {
                try {
                    statement.execute("drop table xids");
                    statement.execute("drop index index_xid on xids");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("S0005")
                            && ex.getErrorCode() != 3701) {
                        throw ex;
                    }
                }
                statement
                        .execute("create table xids (xid varbinary(144), transactionManagerID varchar(64), actionuid varbinary(28))");

                statement.execute("create unique index index_xid on xids (xid)");
                try {
                    statement.execute("drop table foo");
                } catch (SQLException ex) {
                    if (!ex.getSQLState().equals("S0005")
                            && ex.getErrorCode() != 3701) {
                        throw ex;
                    }
                }
            } else {
                statement.execute("drop table if exists xids");
                if (driverName.equals("postgresql")) {
                    statement.execute("drop index if exists index_xid");
                    statement
                            .execute("create table xids (xid bytea, transactionManagerID varchar(64), actionuid bytea)");
                    statement
                            .execute("create unique index index_xid on xids (xid)");
                } else {
                    statement
                            .execute("create table xids (xid varbinary(144), transactionManagerID varchar(64), actionuid varbinary(28))");
                    if (driverName.equals("h2")) {
                        statement.execute("drop index if exists index_xid");
                        statement
                                .execute("create unique index index_xid on xids (xid)");

                    }
                }
                statement.execute("drop table if exists foo");
            }
            statement.execute("create table foo (bar int)");
            statement.close();
        } finally {
            connection.close();
        }
	}

	public static void createTables(XADataSource xaDataSource) {
		try {
			XAConnection xaConnection = xaDataSource.getXAConnection();

			try {
				XAResource xaResource = xaConnection.getXAResource();
				Xid[] recover = xaResource.recover(XAResource.TMSTARTRSCAN);
				for (int i = 0; i < recover.length; i++) {
					xaResource.rollback(recover[i]);
				}
				xaResource.recover(XAResource.TMENDRSCAN);
			} catch (XAException e) {
				e.printStackTrace();
			}

			Connection connection = xaConnection.getConnection();
			Statement statement = connection.createStatement();
			String driverName = connection.getMetaData().getDriverName();
			int index = driverName.indexOf(' ');
			if (index != -1)
				driverName = driverName.substring(0, index);
			driverName = driverName.replaceAll("-", "_");
			driverName = driverName.toLowerCase();

			if (driverName.equals("jconnect")) {
				try {
					statement
							.execute("drop table " + Utils.getXAFooTableName());
				} catch (SQLException ex) {
					if (ex.getErrorCode() != 3701) {
						throw ex;
					}
				}
			} else if (driverName.equals("oracle")) {
				try {
					statement
							.execute("drop table " + Utils.getXAFooTableName());
				} catch (SQLException ex) {
					if (!ex.getSQLState().equals("42000")
							&& ex.getErrorCode() != 942) {
						throw ex;
					}
				}
			} else if (driverName.equals("ibm")) {
				try {
					statement
							.execute("drop table " + Utils.getXAFooTableName());
				} catch (SQLException ex) {
					if (!ex.getSQLState().equals("42704")
							&& ex.getErrorCode() != -204) {
						throw ex;
					}
				}
			} else if (driverName.equals("microsoft")) {
				try {
					statement
							.execute("drop table " + Utils.getXAFooTableName());
				} catch (SQLException ex) {
					if (!ex.getSQLState().equals("S0005")
							&& ex.getErrorCode() != 3701) {
						throw ex;
					}
				}
			} else {
				statement.execute("drop table if exists "
						+ Utils.getXAFooTableName());
			}
			statement.execute("create table " + Utils.getXAFooTableName()
					+ " (bar int)");
			statement.close();
			connection.close();
			xaConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createTables(Connection connection,
			XADataSource xaDataSource) throws SQLException {
		createTables(connection);
		createTables(xaDataSource);
	}

	public static void removeRecursive(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}

	public static String getXAFooTableName() {
		return "foo2";
	}
}
